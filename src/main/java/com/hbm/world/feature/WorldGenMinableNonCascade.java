package com.hbm.world.feature;

import com.google.common.base.Predicate;
import com.hbm.lib.Library;
import com.hbm.world.WorldUtil;
import com.hbm.world.phased.AbstractPhasedStructure;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

// mlbv: vanilla WorldGenMinable DOES cascade
public class WorldGenMinableNonCascade extends AbstractPhasedStructure {

    private static final Int2ObjectMap<LongArrayList> CHUNK_OFFSETS_CACHE = new Int2ObjectOpenHashMap<>();


    private final IBlockState oreBlock;
    private final int numberOfBlocks;
    private final Block target;
    private final Predicate<IBlockState> predicate;
    private final LongArrayList chunkOffsets;

    public WorldGenMinableNonCascade(@NotNull IBlockState state, int blockCount) {
        this.oreBlock = state;
        this.numberOfBlocks = blockCount;
        this.target = Blocks.STONE;
        this.predicate = WorldUtil.STONE_PREDICATE;
        this.chunkOffsets = getOrCreateChunkOffsets(computeHorizontalRadius(blockCount));
    }

    public WorldGenMinableNonCascade(@NotNull IBlockState state, int blockCount, Block target) {
        this.oreBlock = state;
        this.numberOfBlocks = blockCount;
        this.target = target;
        this.predicate = BlockMatcher.forBlock(target);
        this.chunkOffsets = getOrCreateChunkOffsets(computeHorizontalRadius(blockCount));
    }

    private static LongArrayList getOrCreateChunkOffsets(int horizontalRadius) {
        LongArrayList cached = CHUNK_OFFSETS_CACHE.get(horizontalRadius);
        if (cached != null) return cached;
        LongArrayList offsets = collectChunkOffsetsByRadius(horizontalRadius);
        CHUNK_OFFSETS_CACHE.put(horizontalRadius, offsets);
        return offsets;
    }

    private static int computeHorizontalRadius(int blockCount) {
        double radius = 23.5 + (3.0 * blockCount) / 16.0;
        return (int) Math.ceil(radius);
    }

    @Override
    protected boolean useDynamicScheduler() {
        return true;
    }

    @Override
    public LongArrayList getWatchedChunkOffsets(long origin) {
        return chunkOffsets;
    }

    @Override
    protected boolean isCacheable() {
        return false;
    }

    @Override
    public void postGenerate(@NotNull World world, @NotNull Random rand, long finalOrigin) {
        float f = rand.nextFloat() * (float) Math.PI;
        int numberOfBlocks1 = this.numberOfBlocks;
        int x = Library.getBlockPosX(finalOrigin);
        int y = Library.getBlockPosY(finalOrigin);
        int z = Library.getBlockPosZ(finalOrigin);

        double d0 = x + 8F + MathHelper.sin(f) * numberOfBlocks1 / 8.0F;
        double d1 = x + 8F - MathHelper.sin(f) * numberOfBlocks1 / 8.0F;
        double d2 = z + 8F + MathHelper.cos(f) * numberOfBlocks1 / 8.0F;
        double d3 = z + 8F - MathHelper.cos(f) * numberOfBlocks1 / 8.0F;
        double d4 = y + rand.nextInt(3) - 2;
        double d5 = y + rand.nextInt(3) - 2;

        MutableBlockPos blockpos = this.mutablePos;

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
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setString("oreBlock", oreBlock.getBlock().getRegistryName().toString());
        nbt.setInteger("numberOfBlocks", numberOfBlocks);
        nbt.setString("target", target.getRegistryName().toString());
    }

    public static WorldGenMinableNonCascade readFromNBT(NBTTagCompound nbt) {
        Block oreBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(nbt.getString("oreBlock")));
        int numberOfBlocks = nbt.getInteger("numberOfBlocks");
        Block target = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(nbt.getString("target")));
        if (oreBlock == null || target == null || numberOfBlocks == 0) return null;
        if (oreBlock == Blocks.STONE) return new WorldGenMinableNonCascade(oreBlock.getDefaultState(), numberOfBlocks);
        return new WorldGenMinableNonCascade(oreBlock.getDefaultState(), numberOfBlocks, target);
    }
}
