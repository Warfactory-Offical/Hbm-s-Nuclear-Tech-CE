package com.hbm.world.feature;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

public class OreLayer3D {
    private static final int MIN_Y = 6;
    private static final int MAX_Y = 64;
    private static final int HEIGHT = MAX_Y - MIN_Y + 1;
    private static final BlockMatcher STONE_MATCHER = BlockMatcher.forBlock(Blocks.STONE);
    public static int counter = 0;
    public int id;
    private NoiseGeneratorPerlin noiseX;
    private NoiseGeneratorPerlin noiseY;
    private NoiseGeneratorPerlin noiseZ;

    private double scaleH;
    private double scaleV;
    private double threshold;

    private final Block block;
    private final int meta;
    private int dim = 0;
    private double[][] noiseXCache;
    private double[][] noiseYCache;
    private double[][] noiseZCache;

    public OreLayer3D(Block block, int meta) {
        this.block = block;
        this.meta = meta;
        MinecraftForge.EVENT_BUS.register(this);
        this.id = counter++;
    }

    public OreLayer3D setDimension(int dim) {
        this.dim = dim;
        return this;
    }

    public OreLayer3D setScaleH(double scale) {
        this.scaleH = scale;
        return this;
    }

    public OreLayer3D setScaleV(double scale) {
        this.scaleV = scale;
        return this;
    }

    public OreLayer3D setThreshold(double threshold) {
        this.threshold = threshold;
        return this;
    }

    @SubscribeEvent
    public void onDecorate(DecorateBiomeEvent.Pre event) {

        World world = event.getWorld();

        if (world.provider == null || world.provider.getDimension() != this.dim) return;
        if (world.isRemote) return;
        IBlockState blockState = block.getStateFromMeta(meta);
        if (this.noiseX == null) this.noiseX = new NoiseGeneratorPerlin(new Random(world.getSeed() + 101 + id), 4);
        if (this.noiseY == null) this.noiseY = new NoiseGeneratorPerlin(new Random(world.getSeed() + 102 + id), 4);
        if (this.noiseZ == null) this.noiseZ = new NoiseGeneratorPerlin(new Random(world.getSeed() + 103 + id), 4);

        final int cX = event.getPos().getX();
        final int cZ = event.getPos().getZ();

        final int startX = cX + 8;
        final int startZ = cZ + 8;

        //mlbv: added some caching here but behavior should be identical to 1.7 as of writing
        if (noiseXCache == null) {
            noiseXCache = new double[16][HEIGHT];
            noiseYCache = new double[16][16];
            noiseZCache = new double[16][HEIGHT];
        }

        for (int zOff = 0; zOff < 16; zOff++) {
            int worldZ = startZ + zOff;
            for (int yIndex = 0; yIndex < HEIGHT; yIndex++) {
                int y = MAX_Y - yIndex;
                noiseXCache[zOff][yIndex] = this.noiseX.getValue(y * scaleV, worldZ * scaleH);
            }
        }

        for (int xOff = 0; xOff < 16; xOff++) {
            int worldX = startX + xOff;
            for (int zOff = 0; zOff < 16; zOff++) {
                int worldZ = startZ + zOff;
                noiseYCache[xOff][zOff] = this.noiseY.getValue(worldX * scaleH, worldZ * scaleH);
            }
        }

        for (int xOff = 0; xOff < 16; xOff++) {
            int worldX = startX + xOff;
            for (int yIndex = 0; yIndex < HEIGHT; yIndex++) {
                int y = MAX_Y - yIndex;
                noiseZCache[xOff][yIndex] = this.noiseZ.getValue(worldX * scaleH, y * scaleV);
            }
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int xOff = 0; xOff < 16; xOff++) {
            int worldX = startX + xOff;
            for (int zOff = 0; zOff < 16; zOff++) {
                int worldZ = startZ + zOff;
                double nY = noiseYCache[xOff][zOff];
                for (int yIndex = 0; yIndex < HEIGHT; yIndex++) {
                    int y = MAX_Y - yIndex;
                    double nX = noiseXCache[zOff][yIndex];
                    double nZ = noiseZCache[xOff][yIndex];
                    if (nX * nY * nZ <= threshold) continue;
                    pos.setPos(worldX, y, worldZ);
                    IBlockState state = world.getBlockState(pos);
                    Block target = state.getBlock();

                    if (target.isNormalCube(state, world, pos) && state.getMaterial() == Material.ROCK && target.isReplaceableOreGen(state, world, pos, STONE_MATCHER)) {
                        world.setBlockState(pos, blockState, 2 | 16);
                    }
                }
            }
        }
    }
}
