package com.hbm.world;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

// mlbv: this can't cause cascading worldgen..
public class Dud extends WorldGenerator
{
	Block Block1 = ModBlocks.steel_scaffold;
	Block Block2 = ModBlocks.machine_difurnace_off;
	Block Block3 = ModBlocks.factory_titanium_core;
	Block Block4 = ModBlocks.steel_wall;
	Block Block5 = ModBlocks.reinforced_light;
	
	protected Block[] GetValidSpawnBlocks()
	{
		return new Block[]
		{
			Blocks.GRASS,
			Blocks.DIRT,
			Blocks.STONE,
			Blocks.SAND,
			Blocks.SANDSTONE,
		};
	}

	public boolean LocationIsValidSpawn(World world, BlockPos pos)
 {

		IBlockState checkBlockState = world.getBlockState(pos.down());
		Block checkBlock = checkBlockState.getBlock();
		Block blockAbove = world.getBlockState(pos).getBlock();
		Block blockBelow = world.getBlockState(pos.down(2)).getBlock();

		for (Block i : GetValidSpawnBlocks())
		{
			if (blockAbove != Blocks.AIR)
			{
				return false;
			}
			if (checkBlock == i)
			{
				return true;
			}
			else if (checkBlock == Blocks.SNOW_LAYER && blockBelow == i)
			{
				return true;
			}
			else if (checkBlockState.getMaterial() == Material.PLANTS && blockBelow == i)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean generate(World world, Random rand, BlockPos pos)
	{
		return generate(world, rand, pos, false);

	}
	
	public boolean generate(World world, Random rand, BlockPos pos, boolean force)
	{
		int i = rand.nextInt(1);

		if(i == 0)
		{
		    generate_r0(world, rand, pos, false);
		}

       return true;

	}

	public boolean generate_r0(World world, Random rand, BlockPos pos, boolean force)
	{
		if(!force && !LocationIsValidSpawn(world, pos))
		{
			return false;
		}
		
		world.setBlockState(pos, ModBlocks.crashed_balefire.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.values()[rand.nextInt(4) + 2]), 3);
		if(GeneralConfig.enableDebugMode)
			System.out.print("[Debug] Successfully spawned dud at " + pos.getX() + " " + pos.getY() +" " + pos.getZ() + "\n");
		return true;

	}
}
