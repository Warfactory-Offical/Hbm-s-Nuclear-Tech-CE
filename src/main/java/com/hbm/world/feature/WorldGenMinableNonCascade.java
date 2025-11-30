package com.hbm.world.feature;

import com.google.common.base.Predicate;
import com.hbm.config.GeneralConfig;
import com.hbm.main.MainRegistry;
import com.hbm.world.phased.AbstractPhasedStructure;
import com.hbm.world.phased.PhasedStructureGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.Random;

// mlbv: vanilla WorldGenMinable DOES cascade
public class WorldGenMinableNonCascade extends AbstractPhasedStructure {

    private final IBlockState oreBlock;
    private final int numberOfBlocks;
    private final Predicate<IBlockState> predicate;
    private final List<ChunkPos> chunkOffsets;

    public WorldGenMinableNonCascade(@NotNull IBlockState state, int blockCount) {
        this(state, blockCount, BlockMatcher.forBlock(Blocks.STONE));
    }

    public WorldGenMinableNonCascade(@NotNull IBlockState state, int blockCount, @NotNull Predicate<IBlockState> predicate) {
        this.oreBlock = state;
        this.numberOfBlocks = blockCount;
        this.predicate = predicate;
        this.chunkOffsets = collectChunkOffsetsByRadius(computeHorizontalRadius(blockCount) + 32);
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
        } else if (GeneralConfig.enableDebugWorldGen) {
            MainRegistry.logger.info("WorldGenMinableNonCascade at {} did not pass spawn condition check.", pending.origin);
        }
        return Optional.empty();
    }

    @Override
    public void postGenerate(@NotNull World world, @NotNull Random rand, @NotNull BlockPos finalOrigin) {
        float f = rand.nextFloat() * (float) Math.PI;
        int numberOfBlocks1 = this.numberOfBlocks;

        double d0 = (finalOrigin.getX() + 8F) + MathHelper.sin(f) * numberOfBlocks1 / 8.0F;
        double d1 = (finalOrigin.getX() + 8F) - MathHelper.sin(f) * numberOfBlocks1 / 8.0F;
        double d2 = (finalOrigin.getZ() + 8F) + MathHelper.cos(f) * numberOfBlocks1 / 8.0F;
        double d3 = (finalOrigin.getZ() + 8F) - MathHelper.cos(f) * numberOfBlocks1 / 8.0F;
        double d4 = finalOrigin.getY() + rand.nextInt(3) - 2;
        double d5 = finalOrigin.getY() + rand.nextInt(3) - 2;

        BlockPos.MutableBlockPos blockpos = new BlockPos.MutableBlockPos();

        for (int i = 0; i < numberOfBlocks1; ++i) {
            float f1 = (float) i / (float) numberOfBlocks1;
            double d6 = d0 + (d1 - d0) * f1;
            double d7 = d4 + (d5 - d4) * f1;
            double d8 = d2 + (d3 - d2) * f1;
            double d9 = rand.nextDouble() * numberOfBlocks1 / 16.0D;
            double d10 = (MathHelper.sin((float) Math.PI * f1) + 1.0F) * d9 + 1.0D;
            double d11 = (MathHelper.sin((float) Math.PI * f1) + 1.0F) * d9 + 1.0D;
            int j = MathHelper.floor(d6 - d10 / 2.0D);
            int k = MathHelper.floor(d7 - d11 / 2.0D);
            int l = MathHelper.floor(d8 - d10 / 2.0D);
            int i1 = MathHelper.floor(d6 + d10 / 2.0D);
            int j1 = MathHelper.floor(d7 + d11 / 2.0D);
            int k1 = MathHelper.floor(d8 + d10 / 2.0D);

            for (int l1 = j; l1 <= i1; ++l1) {
                double d12 = ((double) l1 + 0.5D - d6) / (d10 / 2.0D);

                if (d12 * d12 < 1.0D) {
                    for (int i2 = k; i2 <= j1; ++i2) {
                        double d13 = ((double) i2 + 0.5D - d7) / (d11 / 2.0D);

                        if (d12 * d12 + d13 * d13 < 1.0D) {
                            for (int j2 = l; j2 <= k1; ++j2) {
                                double d14 = ((double) j2 + 0.5D - d8) / (d10 / 2.0D);

                                if (d12 * d12 + d13 * d13 + d14 * d14 < 1.0D) {
                                    blockpos.setPos(l1, i2, j2);

                                    IBlockState state = world.getBlockState(blockpos);
                                    if (state.getBlock().isReplaceableOreGen(state, world, blockpos, this.predicate)) {
                                        world.setBlockState(blockpos, this.oreBlock, 2 | 16);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<ChunkPos> getAdditionalChunks(@NotNull BlockPos origin) {
        return translateOffsets(origin, chunkOffsets);
    }

    private static int computeHorizontalRadius(int blockCount) {
        double radius = 8.5 + (3.0 * blockCount) / 16.0;
        return (int) Math.ceil(radius);
    }
}
