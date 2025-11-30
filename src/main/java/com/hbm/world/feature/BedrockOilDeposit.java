package com.hbm.world.feature;

import com.google.common.base.Predicate;
import com.hbm.blocks.ModBlocks;
import com.hbm.world.generator.DungeonToolbox;
import com.hbm.world.phased.AbstractPhasedStructure;
import com.hbm.world.phased.PhasedStructureGenerator;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Random;

public class BedrockOilDeposit extends AbstractPhasedStructure {

    private static final int WORLDGEN_FLAGS = 2 | 16;
    private static final int DXZ_LIMIT = 4;
    private static final int MAX_Y = 4;
    private static final int L1_MAX = 6;
    private static final int OIL_SPOT_RADIUS = 5;
    private static final int OIL_SPOT_HEIGHT = 50;
    private static final boolean OIL_SPOT_RICH = true;
    private static final int ADDITIONAL_RADIUS = 32;
    private static final LongArrayList CHUNK_OFFSETS = collectChunkOffsetsByRadius(ADDITIONAL_RADIUS);

    private static final Predicate<IBlockState> BEDROCK_MATCHER = BlockMatcher.forBlock(Blocks.BEDROCK);

    private BedrockOilDeposit() {
    }

    public static void generate(@NotNull World world, int x, int z) {
        BedrockOilDeposit task = new BedrockOilDeposit();
        BlockPos origin = new BlockPos(x, 0, z);
        task.generate(world, world.rand, origin);
    }

    @Override
    protected boolean useDynamicScheduler() {
        return true;
    }

    private static boolean isBedrock(@NotNull World world, @NotNull BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getBlock().isReplaceableOreGen(state, world, pos, BEDROCK_MATCHER);
    }

    @Override
    protected boolean isCacheable() {
        return false;
    }

    @Override
    protected void buildStructure(@NotNull LegacyBuilder builder, @NotNull Random rand) {
    }

    @NotNull
    @Override
    public Optional<PhasedStructureGenerator.ReadyToGenerateStructure> validate(@NotNull World world,
                                                                                @NotNull PhasedStructureGenerator.PendingValidationStructure pending) {
        if (checkSpawningConditions(world, pending.origin)) {
            return Optional.of(new PhasedStructureGenerator.ReadyToGenerateStructure(pending, pending.origin));
        }
        return Optional.empty();
    }

    @Override
    public void postGenerate(@NotNull World world, @NotNull Random rand, @NotNull BlockPos finalOrigin) {
        executeOriginalLogic(world, rand, finalOrigin);
    }

    @Override
    public LongArrayList getWatchedChunkOffsets(@NotNull BlockPos origin) {
        return CHUNK_OFFSETS;
    }

    private static void executeOriginalLogic(@NotNull World world, @NotNull Random rand, @NotNull BlockPos finalOrigin) {
        int centerX = finalOrigin.getX();
        int centerZ = finalOrigin.getZ();

        BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain();

        for (int dx = -DXZ_LIMIT; dx <= DXZ_LIMIT; dx++) {
            for (int y = 0; y <= MAX_Y; y++) {
                for (int dz = -DXZ_LIMIT; dz <= DXZ_LIMIT; dz++) {
                    if (Math.abs(dx) + Math.abs(y) + Math.abs(dz) <= L1_MAX) {
                        int x = centerX + dx;
                        int z = centerZ + dz;
                        pos.setPos(x, y, z);
                        if (isBedrock(world, pos)) {
                            world.setBlockState(pos, ModBlocks.ore_bedrock_oil.getDefaultState(), WORLDGEN_FLAGS);
                        }
                    }
                }
            }
        }
        pos.release();

        int chunkMinX = (centerX >> 4) << 4;
        int chunkMinZ = (centerZ >> 4) << 4;
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, 16, 8, 10, 50, ModBlocks.stone_porous);
        OilSpot.generateOilSpot(world, centerX, centerZ, OIL_SPOT_RADIUS, OIL_SPOT_HEIGHT, OIL_SPOT_RICH);
    }
}
