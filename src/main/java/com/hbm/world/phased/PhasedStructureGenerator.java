package com.hbm.world.phased;

import com.hbm.config.GeneralConfig;
import com.hbm.main.MainRegistry;
import com.hbm.world.phased.AbstractPhasedStructure.BlockInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Solution to cascading worldgen.
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
    private final Map<Integer, Queue<ReadyToGenerateStructure>> generationQueues = new ConcurrentHashMap<>();

    private PhasedStructureGenerator() {
    }

    public void scheduleStructureForValidation(World world, BlockPos origin, IPhasedStructure structure, Map<ChunkPos, List<BlockInfo>> layout) {
        if (layout.isEmpty()) {
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
                    if (GeneralConfig.enableDebugWorldGen) MainRegistry.logger.info("All chunks present for {}. Scheduled for immediate generation.", pending.structure.getClass().getSimpleName());
                } else {
                    generateValidated(world, validated);
                }
            } else if (GeneralConfig.enableDebugWorldGen) MainRegistry.logger.info("Structure {} at {} failed to validate on fast path.", pending.structure.getClass().getSimpleName(), pending.origin);
        } else {
            this.pendingValidations.computeIfAbsent(world.provider.getDimension(), k -> ConcurrentHashMap.newKeySet()).add(pending);
        }
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        int dimId = world.provider.getDimension();
        Set<PendingValidationStructure> structuresInDim = this.pendingValidations.get(dimId);
        if (structuresInDim == null || structuresInDim.isEmpty()) {
            return;
        }

        ChunkPos currentChunkPos = new ChunkPos(chunkX, chunkZ);
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
                            generateValidated(world, validated);
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
                generateValidated(event.world, validated);
            }
        }
    }

    @Nullable
    private ReadyToGenerateStructure validate(World world, PendingValidationStructure pending) {
        return pending.structure.validate(world, pending).orElse(null);
    }

    private void generateValidated(World world, ReadyToGenerateStructure validated) {
        IPhasedStructure phasedStructure = validated.pending.structure;
        Random structureRand = validated.pending.createRandom();
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

    public void forceGenerateStructure(World world, Random rand, BlockPos origin, IPhasedStructure structure, Map<ChunkPos, List<BlockInfo>> layout) {
        int originChunkX = origin.getX() >> 4;
        int originChunkZ = origin.getZ() >> 4;
        for (Map.Entry<ChunkPos, List<BlockInfo>> entry : layout.entrySet()) {
            ChunkPos absoluteChunkPos = new ChunkPos(originChunkX + entry.getKey().x, originChunkZ + entry.getKey().z);
            structure.generateForChunk(world, rand, origin, absoluteChunkPos, entry.getValue());
        }
    }

    public static class ReadyToGenerateStructure {
        final PendingValidationStructure pending;
        final BlockPos finalOrigin;
        public ReadyToGenerateStructure(PendingValidationStructure pending, BlockPos finalOrigin) {
            this.pending = pending;
            this.finalOrigin = finalOrigin;
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
