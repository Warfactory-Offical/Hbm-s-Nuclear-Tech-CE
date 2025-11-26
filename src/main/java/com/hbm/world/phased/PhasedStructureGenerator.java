package com.hbm.world.phased;

import com.hbm.config.GeneralConfig;
import com.hbm.main.MainRegistry;
import com.hbm.world.phased.AbstractPhasedStructure.BlockInfo;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

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
    private final Map<Integer, Set<PendingValidationStructure>> pendingValidations = new ConcurrentHashMap<>();
    private final Map<Integer, Map<ChunkPos, Queue<Runnable>>> structureParts = new ConcurrentHashMap<>();
    private final Map<Integer, Queue<ReadyToGenerateStructure>> generationQueues = new ConcurrentHashMap<>();

    private PhasedStructureGenerator() {
    }

    public void scheduleStructureForValidation(World world, BlockPos origin, IPhasedStructure structure, Map<ChunkPos, List<BlockInfo>> layout) {
        if (layout.isEmpty() && !(!structure.getValidationPoints(BlockPos.ORIGIN).isEmpty() && structure instanceof AbstractPhasedStructure abstractPhased && !abstractPhased.isCacheable())) {
            if (GeneralConfig.enableDebugWorldGen)
                MainRegistry.logger.warn("Skipping structure {} generation at {} due to empty layout.",
                        structure.getClass().getSimpleName(), origin);
            return;
        }
        PendingValidationStructure pending = new PendingValidationStructure(origin, structure, layout, world.getSeed());
        IChunkProvider chunkProvider = world.getChunkProvider();
        pending.chunksAwaitingGeneration.removeIf(chunkPos -> chunkProvider.isChunkGeneratedAt(chunkPos.x, chunkPos.z));
        if (pending.chunksAwaitingGeneration.isEmpty()) {
            ReadyToGenerateStructure validated = validate(world, pending);
            if (validated != null) {
                if (GeneralConfig.enableTickBasedWorldGenerator) {
                    generationQueues.computeIfAbsent(world.provider.getDimension(), k -> new ConcurrentLinkedQueue<>()).add(validated);
                    if (GeneralConfig.enableDebugWorldGen) MainRegistry.logger.info("All chunks present for {}. Scheduled for tick generation.", pending.structure.getClass().getSimpleName());
                } else {
                    queueValidatedStructure(world, validated);
                }
            } else if (GeneralConfig.enableDebugWorldGen) MainRegistry.logger.info("Structure {} at {} failed to validate on fast path.", pending.structure.getClass().getSimpleName(), pending.origin);
        } else {
            this.pendingValidations.computeIfAbsent(world.provider.getDimension(), k -> ConcurrentHashMap.newKeySet()).add(pending);
        }
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        int dimId = world.provider.getDimension();
        ChunkPos currentChunkPos = new ChunkPos(chunkX, chunkZ);
        processPendingValidations(world, dimId, currentChunkPos);
        if (!GeneralConfig.enableTickBasedWorldGenerator) {
            runQueuedGenerators(dimId, currentChunkPos);
        }
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        World world = event.getWorld();
        if (world.isRemote || GeneralConfig.enableTickBasedWorldGenerator) return;
        Chunk chunk = event.getChunk();
        if (chunk.isTerrainPopulated()) {
            int dimId = world.provider.getDimension();
            ChunkPos pos = chunk.getPos();
            runQueuedGenerators(dimId, pos);
        }
    }

    private void queueValidatedStructure(World world, ReadyToGenerateStructure validated) {
        int dimId = world.provider.getDimension();
        IPhasedStructure phasedStructure = validated.pending.structure;
        Random structureRand = validated.structureRand;
        Map<ChunkPos, List<BlockInfo>> layout = validated.pending.layout;
        int originChunkX = validated.finalOrigin.getX() >> 4;
        int originChunkZ = validated.finalOrigin.getZ() >> 4;

        if (GeneralConfig.enableDebugWorldGen)
            MainRegistry.logger.info("Queuing parts for {} at {}", validated.pending.structure.getClass().getSimpleName(), validated.finalOrigin);

        IChunkProvider provider = world.getChunkProvider();

        for (Map.Entry<ChunkPos, List<BlockInfo>> entry : layout.entrySet()) {
            ChunkPos relativeChunkPos = entry.getKey();
            List<BlockInfo> blocksForThisChunk = entry.getValue();
            ChunkPos absoluteChunkPos = new ChunkPos(originChunkX + relativeChunkPos.x, originChunkZ + relativeChunkPos.z);
            validated.remainingChunks.add(absoluteChunkPos);
            Chunk loadedChunk = provider.getLoadedChunk(absoluteChunkPos.x, absoluteChunkPos.z);

            if (loadedChunk != null && loadedChunk.isTerrainPopulated()) {
                try {
                    phasedStructure.generateForChunk(world, structureRand, validated.finalOrigin, absoluteChunkPos, blocksForThisChunk);
                } catch (Exception e) {
                    MainRegistry.logger.error("Error generating phased structure part at {}", absoluteChunkPos, e);
                }
                markChunkGenerated(world, validated, absoluteChunkPos);
            } else {
                structureParts.computeIfAbsent(dimId, k -> new ConcurrentHashMap<>())
                              .computeIfAbsent(absoluteChunkPos, k -> new ConcurrentLinkedQueue<>()).add(() -> {
                                  try {
                                      phasedStructure.generateForChunk(world, structureRand, validated.finalOrigin, absoluteChunkPos, blocksForThisChunk);
                                  } catch (Exception e) {
                                      MainRegistry.logger.error("Error generating phased structure part at {}", absoluteChunkPos, e);
                                  }
                                  markChunkGenerated(world, validated, absoluteChunkPos);
                              });
            }
        }
        if (validated.remainingChunks.isEmpty()) {
            callPostGenerate(world, validated);
        }
    }

    private void runQueuedGenerators(int dimId, ChunkPos chunkPos) {
        Map<ChunkPos, Queue<Runnable>> partsInDim = structureParts.get(dimId);
        if (partsInDim == null) return;
        Queue<Runnable> tasks = partsInDim.remove(chunkPos);
        if (tasks == null) return;
        while (!tasks.isEmpty()) {
            Runnable task = tasks.poll();
            if (task == null) continue;

            try {
                task.run();
            } catch (Exception e) {
                MainRegistry.logger.error("Error generating phased structure part at {}", chunkPos, e);
            }
        }
    }

    private static void markChunkGenerated(World world, ReadyToGenerateStructure validated, ChunkPos chunkPos) {
        if (!validated.remainingChunks.remove(chunkPos)) return;
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

    private void processPendingValidations(World world, int dimId, ChunkPos currentChunkPos) {
        Set<PendingValidationStructure> structuresInDim = this.pendingValidations.get(dimId);
        if (structuresInDim == null || structuresInDim.isEmpty()) {
            return;
        }

        Iterator<PendingValidationStructure> iterator = structuresInDim.iterator();
        while (iterator.hasNext()) {
            PendingValidationStructure pending = iterator.next();
            if (pending.chunksAwaitingGeneration.contains(currentChunkPos)) {
                pending.chunksAwaitingGeneration.remove(currentChunkPos);
                if (pending.chunksAwaitingGeneration.isEmpty()) {
                    ReadyToGenerateStructure validated = validate(world, pending);

                    if (validated != null) {
                        if (GeneralConfig.enableTickBasedWorldGenerator) {
                            if (GeneralConfig.enableDebugWorldGen) MainRegistry.logger.info("Scheduled {} generation at {}", pending.structure.getClass().getSimpleName(), validated.finalOrigin);
                            generationQueues.computeIfAbsent(dimId, k -> new ConcurrentLinkedQueue<>()).add(validated);
                        } else {
                            queueValidatedStructure(world, validated);
                        }
                    } else if (GeneralConfig.enableDebugWorldGen) MainRegistry.logger.info("Structure {} at {} failed to validate.", pending.structure.getClass().getSimpleName(), pending.origin);
                    iterator.remove();
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote || !GeneralConfig.enableTickBasedWorldGenerator) {
            return;
        }

        Queue<ReadyToGenerateStructure> queue = generationQueues.get(event.world.provider.getDimension());
        if (queue != null && !queue.isEmpty()) {
            ReadyToGenerateStructure validated = queue.poll();
            if (validated != null) {
                generateValidatedImmediate(event.world, validated);
            }
        }
    }

    @Nullable
    private static ReadyToGenerateStructure validate(World world, PendingValidationStructure pending) {
        return pending.structure.validate(world, pending).orElse(null);
    }

    private static void generateValidatedImmediate(World world, ReadyToGenerateStructure validated) {
        IPhasedStructure phasedStructure = validated.pending.structure;
        Random structureRand = validated.structureRand;
        Map<ChunkPos, List<BlockInfo>> layout = validated.pending.layout;
        if (GeneralConfig.enableDebugWorldGen) MainRegistry.logger.info("Generating {} at {}", validated.pending.structure.getClass().getSimpleName(), validated.finalOrigin);
        int originChunkX = validated.finalOrigin.getX() >> 4;
        int originChunkZ = validated.finalOrigin.getZ() >> 4;
        for (Map.Entry<ChunkPos, List<BlockInfo>> entry : layout.entrySet()) {
            ChunkPos relativeChunkPos = entry.getKey();
            List<BlockInfo> blocksForThisChunk = entry.getValue();
            ChunkPos absoluteChunkPos = new ChunkPos(originChunkX + relativeChunkPos.x, originChunkZ + relativeChunkPos.z);
            phasedStructure.generateForChunk(world, structureRand, validated.finalOrigin, absoluteChunkPos, blocksForThisChunk);
        }
        phasedStructure.postGenerate(world, structureRand, validated.finalOrigin);
    }

    public static void forceGenerateStructure(World world, Random rand, BlockPos origin, IPhasedStructure structure, Map<ChunkPos, List<BlockInfo>> layout) {
        int originChunkX = origin.getX() >> 4;
        int originChunkZ = origin.getZ() >> 4;
        for (Map.Entry<ChunkPos, List<BlockInfo>> entry : layout.entrySet()) {
            ChunkPos absoluteChunkPos = new ChunkPos(originChunkX + entry.getKey().x, originChunkZ + entry.getKey().z);
            structure.generateForChunk(world, rand, origin, absoluteChunkPos, entry.getValue());
        }
        structure.postGenerate(world, rand, origin);
    }

    public static class ReadyToGenerateStructure {
        final PendingValidationStructure pending;
        final BlockPos finalOrigin;
        final Random structureRand;
        final Set<ChunkPos> remainingChunks = ConcurrentHashMap.newKeySet();
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
        final Set<ChunkPos> requiredChunks;
        final Set<ChunkPos> chunksAwaitingGeneration;
        final Map<ChunkPos, List<BlockInfo>> layout;
        final long worldSeed;

        PendingValidationStructure(BlockPos origin, IPhasedStructure structure, Map<ChunkPos, List<BlockInfo>> layout, long worldSeed) {
            this.origin = origin;
            this.structure = structure;
            this.layout = layout;
            this.worldSeed = worldSeed;
            this.requiredChunks = new HashSet<>();
            int originChunkX = origin.getX() >> 4;
            int originChunkZ = origin.getZ() >> 4;
            for (ChunkPos relativeLayoutChunk : layout.keySet()) {
                this.requiredChunks.add(new ChunkPos(originChunkX + relativeLayoutChunk.x, originChunkZ + relativeLayoutChunk.z));
            }
            List<BlockPos> validationPointOffsets = structure.getValidationPoints(BlockPos.ORIGIN);
            for (BlockPos offset : validationPointOffsets) {
                BlockPos absoluteValidationPoint = origin.add(offset);
                this.requiredChunks.add(new ChunkPos(absoluteValidationPoint));
            }
            this.chunksAwaitingGeneration = new HashSet<>(this.requiredChunks);
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
