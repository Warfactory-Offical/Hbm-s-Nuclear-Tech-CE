package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.handler.radiation.ChunkRadiationManager;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BlockOutgas extends BlockNTMOre {

    boolean randomTick;
    int rate;
    boolean onNeighbour;
    private static final int ANCIENT_SCRAP_GAS_RADIUS = 2;

    public BlockOutgas(boolean randomTick, int rate, String s) {
        super(s, 1);
        this.setTickRandomly(randomTick);
        this.randomTick = randomTick;
        this.rate = rate;
        this.onNeighbour = false;
    }

    public BlockOutgas(boolean randomTick, int rate, boolean onNeighbour, String s) {
        this(randomTick, rate, s);
        this.onNeighbour = onNeighbour;
    }

    public boolean isOnNeighbour() {
        return this.onNeighbour;
    }

    @Override
    public int tickRate(World world) {
        return rate;
    }

    public Block getGas() {

        if (GeneralConfig.enableRadon) {
            if (this == ModBlocks.ore_uranium || this == ModBlocks.ore_uranium_scorched ||
                    this == ModBlocks.ore_gneiss_uranium || this == ModBlocks.ore_gneiss_uranium_scorched ||
                    this == ModBlocks.ore_nether_uranium || this == ModBlocks.ore_nether_uranium_scorched) {
                return ModBlocks.gas_radon;
            }

            if (this == ModBlocks.block_corium_cobble)
                return ModBlocks.gas_radon_dense;

            if (this == ModBlocks.ancient_scrap)
                return ModBlocks.gas_radon_tomb;
        }

        if (GeneralConfig.enableCarbonMonoxide) {
            if (this == ModBlocks.ore_coal_oil_burning || this == ModBlocks.ore_nether_coal) {
                return ModBlocks.gas_monoxide;
            }
        }

        if (GeneralConfig.enableAsbestosDust) {
            if (this == ModBlocks.ore_asbestos || this == ModBlocks.ore_gneiss_asbestos ||
                    this == ModBlocks.block_asbestos || this == ModBlocks.deco_asbestos ||
                    this == ModBlocks.brick_asbestos || this == ModBlocks.tile_lab ||
                    this == ModBlocks.tile_lab_cracked || this == ModBlocks.tile_lab_broken
            ) {
                return ModBlocks.gas_asbestos;
            }
        }
        return Blocks.AIR;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state);

        Block block = state.getBlock();

        if (!(block instanceof BlockOutgas outgas)) {
            return;
        }

        Block gas = outgas.getGas();

        if (gas == Blocks.AIR) {
            return;
        }

        // Spawn gas at broken block location
        if (isAirBlock(world, pos)) {
            world.setBlockState(pos, gas.getDefaultState(), 3);
        }

        // Spawn gas in neighboring blocks if applicable
        if (outgas.isOnNeighbour()) {
            spawnGasInAdjacentBlocks(world, pos, gas);
        }

        // Special handling for ancient scrap - larger radius
        if (block == ModBlocks.ancient_scrap) {
            spawnGasInRadius(world, pos, gas, ANCIENT_SCRAP_GAS_RADIUS);
        }
    }

    private void spawnGasInAdjacentBlocks(World world, BlockPos pos, Block gas) {
        for (EnumFacing dir : EnumFacing.values()) {
            BlockPos adjacentPos = pos.offset(dir);
            if (isAirBlock(world, adjacentPos)) {
                world.setBlockState(adjacentPos, gas.getDefaultState(), 3);
            }
        }
    }

    private void spawnGasInRadius(World world, BlockPos center, Block gas, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    int manhattan = Math.abs(x + y + z);

                    // Skip center and blocks too far away
                    if (manhattan > 0 && manhattan < 5) {
                        BlockPos targetPos = center.add(x, y, z);
                        if (isAirBlock(world, targetPos)) {
                            world.setBlockState(targetPos, gas.getDefaultState(), 3);
                        }
                    }
                }
            }
        }
    }

    private boolean isAirBlock(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() == Blocks.AIR;
    }


    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (this == ModBlocks.block_corium_cobble) ChunkRadiationManager.proxy.incrementRad(world, pos, 1000F, 10000F);
        if (this == ModBlocks.ancient_scrap) ChunkRadiationManager.proxy.incrementRad(world, pos, 150F, 1500F);
    }
}