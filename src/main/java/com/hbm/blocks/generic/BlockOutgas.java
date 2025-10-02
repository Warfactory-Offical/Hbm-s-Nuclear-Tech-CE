package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.lib.ForgeDirection;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BlockOutgas extends BlockNTMOre {

    boolean randomTick;
    int rate;
    boolean onBreak;
    boolean onNeighbour;

    public BlockOutgas(boolean randomTick, int rate, boolean onBreak, String s) {
        super(s, 1);
        this.setTickRandomly(randomTick);
        this.randomTick = randomTick;
        this.rate = rate;
        this.onBreak = onBreak;
        this.onNeighbour = false;
    }

    public BlockOutgas(boolean randomTick, int rate, boolean onBreak, boolean onNeighbour, String s) {
        this(randomTick, rate, onBreak, s);
        this.onNeighbour = onNeighbour;
    }

    @Override
    public int tickRate(World p_149738_1_) {
        return rate;
    }

    protected Block getGas() {

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
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        if (this.randomTick && getGas() == ModBlocks.gas_asbestos) {

            if (world.getBlockState(pos.up()).getBlock() == Blocks.AIR) {

                if (world.rand.nextInt(10) == 0)
                    world.setBlockState(pos.up(), ModBlocks.gas_asbestos.getDefaultState());

                for (int i = 0; i < 5; i++)
                    world.spawnParticle(EnumParticleTypes.TOWN_AURA, pos.getX() + world.rand.nextFloat(), pos.getY() + 1.1, pos.getZ() + world.rand.nextFloat(), 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (this == ModBlocks.block_corium_cobble) ChunkRadiationManager.proxy.incrementRad(world, pos, 1000F, 10000F);
        if (this == ModBlocks.ancient_scrap) ChunkRadiationManager.proxy.incrementRad(world, pos, 150F, 1500F);

        ForgeDirection dir = ForgeDirection.getOrientation(rand.nextInt(6));

        if (world.getBlockState(new BlockPos(pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ)).getBlock() == Blocks.AIR) {
            world.setBlockState(new BlockPos(pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ), getGas().getDefaultState());
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (onNeighbour) {

            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {

                if (world.getBlockState(new BlockPos(pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ)).getBlock() == Blocks.AIR) {
                    world.setBlockState(new BlockPos(pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ), getGas().getDefaultState());
                }
            }
        }
    }

    @Override
    public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, float chance, int fortune) {
        if (onBreak) {
            world.setBlockState(pos, getGas().getDefaultState());
        }

        super.dropBlockAsItemWithChance(world, pos, state, chance, fortune);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state);

        if (this == ModBlocks.ancient_scrap) {
            for (int ix = -2; ix <= 2; ix++) {
                for (int iy = -2; iy <= 2; iy++) {
                    for (int iz = -2; iz <= 2; iz++) {

                        if (Math.abs(ix + iy + iz) < 5 && Math.abs(ix + iy + iz) > 0 && world.getBlockState(new BlockPos(pos.getX() + ix, pos.getY() + iy, pos.getZ() + iz)).getBlock() == Blocks.AIR) {
                            world.setBlockState(new BlockPos(pos.getX() + ix, pos.getY() + iy, pos.getZ() + iz), this.getGas().getDefaultState());
                        }
                    }
                }
            }
        }
    }
}