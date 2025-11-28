package com.hbm.world.phased;

import com.hbm.config.GeneralConfig;
import com.hbm.main.MainRegistry;
import com.hbm.util.ChunkUtil;
import com.hbm.world.phased.AbstractPhasedStructure.BlockInfo;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;


/**
 * Solution to cascading worldgen.
 * <p>
 * For some reason the worldgen can still cascade with the phased generator in <strong>extremely rare cases</strong>, idk why and I can't fix it.<br>
 * So instead there is a {@link GeneralConfig#enableTickBasedWorldGenerator} flag that can <strong>eliminate</strong> the cascading.<br>
 * This would be incompatible to most, if not all chunk pregenerators.<br>
 * Note that even with {@link GeneralConfig#enableTickBasedWorldGenerator} disabled, the mitigation still works.
 *
 * @author mlbv
 */
public class PhasedStructureGenerator implements IWorldGenerator {
    public static final PhasedStructureGenerator INSTANCE = new PhasedStructureGenerator();
    private final Int2ObjectMap<Long2ObjectMap<ArrayList<PendingValidationStructure>>> pendingByChunk = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Long2ObjectMap<Queue<Runnable>>> structureParts = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Queue<ReadyToGenerateStructure>> generationQueues = new Int2ObjectOpenHashMap<>();

    private PhasedStructureGenerator() {
    }

    public void scheduleStructureForValidation(World world, BlockPos origin, IPhasedStructure structure,
                                               Long2ObjectOpenHashMap<List<BlockInfo>> layout) {
        if (layout.isEmpty() && !(!structure.getValidationPoints(BlockPos.ORIGIN)
                                            .isEmpty() && structure instanceof AbstractPhasedStructure abstractPhased && !abstractPhased.isCacheable())) {

            if (GeneralConfig.enableDebugWorldGen) {
                MainRegistry.logger.warn("Skipping structure {} generation at {} due to empty layout.", structure.getClass().getSimpleName(), origin);
            }
            return;
        }

        PendingValidationStructure pending = new PendingValidationStructure(origin, structure, layout, world.getSeed());
        IChunkProvider chunkProvider = world.getChunkProvider();
        LongIterator it = pending.chunksAwaitingGeneration.iterator();
        while (it.hasNext()) {
            long key = it.nextLong();
            int cx = ChunkUtil.getChunkPosX(key);
            int cz = ChunkUtil.getChunkPosZ(key);
            if (chunkProvider.isChunkGeneratedAt(cx, cz)) {
                it.remove();
            }
        }

        int dimId = world.provider.getDimension();

        if (pending.chunksAwaitingGeneration.isEmpty()) {
            ReadyToGenerateStructure validated = validate(world, pending);
            if (validated != null) {
                if (GeneralConfig.enableTickBasedWorldGenerator) {
                    enqueueForTickGeneration(dimId, validated);
                    if (GeneralConfig.enableDebugWorldGen) {
                        MainRegistry.logger.info("All chunks present for {}. Scheduled for tick generation.", pending.structure.getClass()
                                                                                                                               .getSimpleName());
                    }
                } else {
                    queueValidatedStructure(world, validated);
                }
            } else if (GeneralConfig.enableDebugWorldGen) {
                MainRegistry.logger.info("Structure {} at {} failed to validate on fast path.", pending.structure.getClass()
                                                                                                                 .getSimpleName(), pending.origin);
            }
        } else {
            Long2ObjectMap<ArrayList<PendingValidationStructure>> dimMap = pendingByChunk.get(dimId);
            if (dimMap == null) {
                dimMap = new Long2ObjectOpenHashMap<>();
                pendingByChunk.put(dimId, dimMap);
            }

            LongIterator it2 = pending.chunksAwaitingGeneration.iterator();
            while (it2.hasNext()) {
                long key = it2.nextLong();
                ArrayList<PendingValidationStructure> list = dimMap.get(key);
                if (list == null) {
                    list = new ArrayList<>();
                    dimMap.put(key, list);
                }
                list.add(pending);
            }
        }
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        int dimId = world.provider.getDimension();
        long chunkKey = ChunkPos.asLong(chunkX, chunkZ);

        processPendingValidations(world, dimId, chunkKey);

        if (!GeneralConfig.enableTickBasedWorldGenerator) {
            runQueuedGenerators(dimId, chunkKey);
        }
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        World world = event.getWorld();
        if (world.isRemote || GeneralConfig.enableTickBasedWorldGenerator) return;

        Chunk chunk = event.getChunk();
        if (chunk.isTerrainPopulated()) {
            int dimId = world.provider.getDimension();
            long chunkKey = ChunkPos.asLong(chunk.x, chunk.z);
            runQueuedGenerators(dimId, chunkKey);
        }
    }

    private void queueValidatedStructure(World world, ReadyToGenerateStructure validated) {
        int dimId = world.provider.getDimension();
        IPhasedStructure phasedStructure = validated.pending.structure;
        Random structureRand = validated.structureRand;
        Long2ObjectOpenHashMap<List<BlockInfo>> layout = validated.pending.layout;
        int originChunkX = validated.finalOrigin.getX() >> 4;
        int originChunkZ = validated.finalOrigin.getZ() >> 4;

        if (GeneralConfig.enableDebugWorldGen) {
            MainRegistry.logger.info("Queuing parts for {} at {}", phasedStructure.getClass().getSimpleName(), validated.finalOrigin);
        }

        IChunkProvider provider = world.getChunkProvider();

        Long2ObjectMap<Queue<Runnable>> partsInDim = structureParts.get(dimId);
        if (partsInDim == null) {
            partsInDim = new Long2ObjectOpenHashMap<>();
            structureParts.put(dimId, partsInDim);
        }

        ObjectIterator<Long2ObjectMap.Entry<List<BlockInfo>>> iterator = layout.long2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Long2ObjectMap.Entry<List<BlockInfo>> entry = iterator.next();
            long relKey = entry.getLongKey();
            List<BlockInfo> blocksForThisChunk = entry.getValue();

            int relChunkX = ChunkUtil.getChunkPosX(relKey);
            int relChunkZ = ChunkUtil.getChunkPosZ(relKey);
            int absChunkX = originChunkX + relChunkX;
            int absChunkZ = originChunkZ + relChunkZ;

            ChunkPos absoluteChunkPos = new ChunkPos(absChunkX, absChunkZ);
            long chunkKey = ChunkPos.asLong(absChunkX, absChunkZ);

            validated.remainingChunks.add(chunkKey);

            Chunk loadedChunk = provider.getLoadedChunk(absChunkX, absChunkZ);

            if (loadedChunk != null && loadedChunk.isTerrainPopulated()) {
                try {
                    phasedStructure.generateForChunk(world, structureRand, validated.finalOrigin, absoluteChunkPos, blocksForThisChunk);
                } catch (Exception e) {
                    MainRegistry.logger.error("Error generating phased structure part at {}", absoluteChunkPos, e);
                }
                markChunkGenerated(world, validated, chunkKey);
            } else {
                Queue<Runnable> tasks = partsInDim.get(chunkKey);
                if (tasks == null) {
                    tasks = new ArrayDeque<>();
                    partsInDim.put(chunkKey, tasks);
                }

                final List<BlockInfo> blocksCopy = blocksForThisChunk;
                final ChunkPos absPosCopy = absoluteChunkPos;
                tasks.add(() -> {
                    try {
                        phasedStructure.generateForChunk(world, structureRand, validated.finalOrigin, absPosCopy, blocksCopy);
                    } catch (Exception e) {
                        MainRegistry.logger.error("Error generating phased structure part at {}", absPosCopy, e);
                    }
                    markChunkGenerated(world, validated, chunkKey);
                });
            }
        }

        if (validated.remainingChunks.isEmpty()) {
            callPostGenerate(world, validated);
        }
    }

    private void runQueuedGenerators(int dimId, long chunkKey) {
        Long2ObjectMap<Queue<Runnable>> partsInDim = structureParts.get(dimId);
        if (partsInDim == null) return;

        Queue<Runnable> tasks = partsInDim.remove(chunkKey);
        if (tasks == null || tasks.isEmpty()) {
            if (partsInDim.isEmpty()) {
                structureParts.remove(dimId);
            }
            return;
        }

        while (!tasks.isEmpty()) {
            Runnable task = tasks.poll();
            if (task == null) continue;
            try {
                task.run();
            } catch (Exception e) {
                MainRegistry.logger.error("Error generating phased structure part in dim {} at chunk {}", dimId, chunkKey, e);
            }
        }

        if (partsInDim.isEmpty()) {
            structureParts.remove(dimId);
        }
    }

    private static void markChunkGenerated(World world, ReadyToGenerateStructure validated, long chunkKey) {
        if (!validated.remainingChunks.remove(chunkKey)) return;
        if (validated.remainingChunks.isEmpty()) {
            callPostGenerate(world, validated);
        }
    }

    private static void callPostGenerate(World world, ReadyToGenerateStructure validated) {
        if (validated.postGenerated) return;
        validated.postGenerated = true;
        IPhasedStructure phasedStructure = validated.pending.structure;
        try {
            phasedStructure.postGenerate(world, validated.structureRand, validated.finalOrigin);
        } catch (Exception e) {
            MainRegistry.logger.error("Error running postGenerate for {}", phasedStructure.getClass().getSimpleName(), e);
        }
    }

    private void processPendingValidations(World world, int dimId, long currentChunkKey) {
        Long2ObjectMap<ArrayList<PendingValidationStructure>> dimMap = pendingByChunk.get(dimId);
        if (dimMap == null) {
            return;
        }

        ArrayList<PendingValidationStructure> list = dimMap.remove(currentChunkKey);
        if (list == null || list.isEmpty()) {
            if (dimMap.isEmpty()) {
                pendingByChunk.remove(dimId);
            }
            return;
        }

        for (int i = list.size() - 1; i >= 0; --i) {
            PendingValidationStructure pending = list.get(i);
            if (!pending.chunksAwaitingGeneration.remove(currentChunkKey)) continue;
            if (pending.chunksAwaitingGeneration.isEmpty()) {
                ReadyToGenerateStructure validated = validate(world, pending);

                if (validated != null) {
                    if (GeneralConfig.enableTickBasedWorldGenerator) {
                        if (GeneralConfig.enableDebugWorldGen) {
                            MainRegistry.logger.info("Scheduled {} generation at {}", pending.structure.getClass()
                                                                                                       .getSimpleName(), validated.finalOrigin);
                        }
                        enqueueForTickGeneration(dimId, validated);
                    } else {
                        queueValidatedStructure(world, validated);
                    }
                } else if (GeneralConfig.enableDebugWorldGen) {
                    MainRegistry.logger.info("Structure {} at {} failed to validate.", pending.structure.getClass().getSimpleName(), pending.origin);
                }
            }
        }

        if (dimMap.isEmpty()) {
            pendingByChunk.remove(dimId);
        }
    }

    private void enqueueForTickGeneration(int dimId, ReadyToGenerateStructure validated) {
        Queue<ReadyToGenerateStructure> queue = generationQueues.get(dimId);
        if (queue == null) {
            queue = new ArrayDeque<>();
            generationQueues.put(dimId, queue);
        }
        queue.add(validated);
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote || !GeneralConfig.enableTickBasedWorldGenerator) {
            return;
        }

        int dimId = event.world.provider.getDimension();
        Queue<ReadyToGenerateStructure> queue = generationQueues.get(dimId);
        if (queue == null || queue.isEmpty()) {
            return;
        }

        ReadyToGenerateStructure validated = queue.poll();
        if (validated != null) {
            generateValidatedImmediate(event.world, validated);
        }

        if (queue.isEmpty()) {
            generationQueues.remove(dimId);
        }
    }

    @Nullable
    private static ReadyToGenerateStructure validate(World world, PendingValidationStructure pending) {
        return pending.structure.validate(world, pending).orElse(null);
    }

    private static void generateValidatedImmediate(World world, ReadyToGenerateStructure validated) {
        IPhasedStructure phasedStructure = validated.pending.structure;
        Random structureRand = validated.structureRand;
        Long2ObjectOpenHashMap<List<BlockInfo>> layout = validated.pending.layout;

        if (GeneralConfig.enableDebugWorldGen) {
            MainRegistry.logger.info("Generating {} at {}", phasedStructure.getClass().getSimpleName(), validated.finalOrigin);
        }

        int originChunkX = validated.finalOrigin.getX() >> 4;
        int originChunkZ = validated.finalOrigin.getZ() >> 4;

        ObjectIterator<Long2ObjectMap.Entry<List<BlockInfo>>> iterator = layout.long2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Long2ObjectMap.Entry<List<BlockInfo>> entry = iterator.next();
            long relKey = entry.getLongKey();
            List<BlockInfo> blocksForThisChunk = entry.getValue();

            int relChunkX = ChunkUtil.getChunkPosX(relKey);
            int relChunkZ = ChunkUtil.getChunkPosZ(relKey);
            int absChunkX = originChunkX + relChunkX;
            int absChunkZ = originChunkZ + relChunkZ;

            ChunkPos absoluteChunkPos = new ChunkPos(absChunkX, absChunkZ);

            phasedStructure.generateForChunk(world, structureRand, validated.finalOrigin, absoluteChunkPos, blocksForThisChunk);
        }
        phasedStructure.postGenerate(world, structureRand, validated.finalOrigin);
    }

    public static void forceGenerateStructure(World world, Random rand, BlockPos origin, IPhasedStructure structure,
                                              Long2ObjectOpenHashMap<List<BlockInfo>> layout) {
        int originChunkX = origin.getX() >> 4;
        int originChunkZ = origin.getZ() >> 4;

        ObjectIterator<Long2ObjectMap.Entry<List<BlockInfo>>> iterator = layout.long2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Long2ObjectMap.Entry<List<BlockInfo>> entry = iterator.next();
            long relKey = entry.getLongKey();
            List<BlockInfo> blocksForThisChunk = entry.getValue();

            int relChunkX = ChunkUtil.getChunkPosX(relKey);
            int relChunkZ = ChunkUtil.getChunkPosZ(relKey);
            int absChunkX = originChunkX + relChunkX;
            int absChunkZ = originChunkZ + relChunkZ;

            ChunkPos absoluteChunkPos = new ChunkPos(absChunkX, absChunkZ);
            structure.generateForChunk(world, rand, origin, absoluteChunkPos, blocksForThisChunk);
        }
        structure.postGenerate(world, rand, origin);
    }

    public static class ReadyToGenerateStructure {
        final PendingValidationStructure pending;
        final BlockPos finalOrigin;
        final Random structureRand;
        final LongOpenHashSet remainingChunks = new LongOpenHashSet();
        volatile boolean postGenerated;

        public ReadyToGenerateStructure(PendingValidationStructure pending, BlockPos finalOrigin) {
            this.pending = pending;
            this.finalOrigin = finalOrigin;
            this.structureRand = pending.createRandom();
        }
    }

    public static class PendingValidationStructure {
        public final BlockPos origin;
        final IPhasedStructure structure;
        final LongOpenHashSet chunksAwaitingGeneration;
        final Long2ObjectOpenHashMap<List<BlockInfo>> layout;
        final long worldSeed;

        PendingValidationStructure(BlockPos origin, IPhasedStructure structure, Long2ObjectOpenHashMap<List<BlockInfo>> layout, long worldSeed) {
            this.origin = origin;
            this.structure = structure;
            this.layout = layout;
            this.worldSeed = worldSeed;

            this.chunksAwaitingGeneration = new LongOpenHashSet();

            int originChunkX = origin.getX() >> 4;
            int originChunkZ = origin.getZ() >> 4;
            LongIterator it = layout.keySet().iterator();
            while (it.hasNext()) {
                long relKey = it.nextLong();
                int relChunkX = ChunkUtil.getChunkPosX(relKey);
                int relChunkZ = ChunkUtil.getChunkPosZ(relKey);
                int absX = originChunkX + relChunkX;
                int absZ = originChunkZ + relChunkZ;
                this.chunksAwaitingGeneration.add(ChunkPos.asLong(absX, absZ));
            }

            List<BlockPos> validationPointOffsets = structure.getValidationPoints(BlockPos.ORIGIN);
            for (BlockPos offset : validationPointOffsets) {
                BlockPos absoluteValidationPoint = origin.add(offset);
                int chunkX = absoluteValidationPoint.getX() >> 4;
                int chunkZ = absoluteValidationPoint.getZ() >> 4;
                this.chunksAwaitingGeneration.add(ChunkPos.asLong(chunkX, chunkZ));
            }
        }

        Random createRandom() {
            Random rand = new Random(this.worldSeed);
            long x = rand.nextLong() ^ this.origin.getX();
            long z = rand.nextLong() ^ this.origin.getZ();
            rand.setSeed(x * this.origin.getX() + z * this.origin.getZ() ^ this.worldSeed);
            return rand;
        }
    }
}
