package com.hbm.world.phased;

import com.hbm.config.GeneralConfig;
import com.hbm.main.MainRegistry;
import com.hbm.world.phased.AbstractPhasedStructure.BlockInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@ParametersAreNonnullByDefault
public interface IPhasedStructure {

    /**
     * Generates the part of the structure that lies within a specific chunk.
     *
     * @param world           The world
     * @param rand            A random object
     * @param structureOrigin The absolute origin (corner) of the entire structure in the world.
     * @param chunkPos        The position of the chunk to generate blocks in.
     * @param blockInfos      The list of blocks to generate.
     */
    void generateForChunk(World world, Random rand, BlockPos structureOrigin, ChunkPos chunkPos, List<BlockInfo> blockInfos);

    @NotNull
    default Optional<PhasedStructureGenerator.ReadyToGenerateStructure> validate(World world, PhasedStructureGenerator.PendingValidationStructure pending) {
        BlockPos originAtY0 = pending.origin;
        List<BlockPos> validationPoints = getValidationPoints(originAtY0);
        if (GeneralConfig.enableDebugWorldGen) {
            IChunkProvider chunkProvider = world.getChunkProvider();
            for (BlockPos validationPoint : validationPoints) {
                int chunkX = validationPoint.getX() >> 4;
                int chunkZ = validationPoint.getZ() >> 4;
                if (!chunkProvider.isChunkGeneratedAt(chunkX, chunkZ)) {
                    throw new IllegalStateException(String.format(
                            "Structure %s attempted to validate in an ungenerated chunk at [%d, %d] (validation point: %s). " +
                                    "This is a bug!",
                            this.getClass().getName(), chunkX, chunkZ, validationPoint
                    ));
                }
            }
        }
        int newY = validationPoints.stream()
                .mapToInt(p -> world.getHeight(p.getX(), p.getZ()))
                .min()
                .orElse(world.getHeight(originAtY0.getX(), originAtY0.getZ()));

        if (newY > 0 && newY < world.getHeight()) {
            BlockPos realOrigin = new BlockPos(originAtY0.getX(), newY, originAtY0.getZ());
            if (checkSpawningConditions(world, realOrigin)) {
                return Optional.of(new PhasedStructureGenerator.ReadyToGenerateStructure(pending, realOrigin));
            } else if (GeneralConfig.enableDebugWorldGen) {
                MainRegistry.logger.info("Structure {} at {} did not pass spawn condition check.", this.getClass().getSimpleName(), realOrigin);
            }
        }
        return Optional.empty();
    }

    /**
     * Dynamic part. All chunks required must be either explicitly declared by {@link #getValidationPoints},
     * or is covered by {@link #generateForChunk}.<br>
     * Violation is guaranteed to cause cascading worldgen.
     */
    default void postGenerate(@NotNull World world, @NotNull Random rand, @NotNull BlockPos finalOrigin){
    }

    /**
     * Points used to determine the y of the structure. Structures will not spawn until all chunks required to validate has been generated.
     */
    @Contract("_ -> new")
    List<@NotNull BlockPos> getValidationPoints(@NotNull BlockPos origin);

    /**
     * Override to use custom spawning condition.
     * @return true if the structure can spawn at the given position.
     */
    default boolean checkSpawningConditions(@NotNull World world, @NotNull BlockPos origin) {
        return true;
    }
}
