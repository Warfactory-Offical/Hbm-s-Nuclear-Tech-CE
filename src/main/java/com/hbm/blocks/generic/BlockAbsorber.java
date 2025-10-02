package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.radiation.ChunkRadiationManager;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BlockAbsorber extends Block {

	private final float absorb;

	public BlockAbsorber(Material materialIn, float ab, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		absorb = ab;

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public int tickRate(World worldIn) {
		return 1;
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        ChunkRadiationManager.proxy.decrementRad(world, pos, absorb / 10);

        world.scheduleUpdate(pos, this, this.tickRate(world));
	}

	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		super.onBlockAdded(worldIn, pos, state);
		worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
	}

}
