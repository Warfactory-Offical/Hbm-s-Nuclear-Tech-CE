package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.handler.radiation.ChunkRadiationManager;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BlockOutgas extends BlockNTMOre {

    boolean randomTick;
    int rate;
    boolean onNeighbour;

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

        if (this == ModBlocks.ore_uranium || this == ModBlocks.ore_uranium_scorched ||
                this == ModBlocks.ore_gneiss_uranium || this == ModBlocks.ore_gneiss_uranium_scorched ||
                this == ModBlocks.ore_nether_uranium || this == ModBlocks.ore_nether_uranium_scorched) {
            return ModBlocks.gas_radon;
        }

        if (this == ModBlocks.block_corium_cobble)
            return ModBlocks.gas_radon_dense;

        if (this == ModBlocks.ancient_scrap)
            return ModBlocks.gas_radon_tomb;

        if (this == ModBlocks.ore_coal_oil_burning || this == ModBlocks.ore_nether_coal) {
            return ModBlocks.gas_monoxide;
        }

        if (GeneralConfig.enableAsbestos) {
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
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (this == ModBlocks.block_corium_cobble) ChunkRadiationManager.proxy.incrementRad(world, pos, 1000F, 10000F);
        if (this == ModBlocks.ancient_scrap) ChunkRadiationManager.proxy.incrementRad(world, pos, 150F, 1500F);
    }
}