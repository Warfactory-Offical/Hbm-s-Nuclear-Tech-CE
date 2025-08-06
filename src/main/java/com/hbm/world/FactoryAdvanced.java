package com.hbm.world;

import com.hbm.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

// not naturally generated
public class FactoryAdvanced extends WorldGenerator {

	Block Block1 = ModBlocks.factory_advanced_hull;
	Block Block2 = ModBlocks.factory_advanced_conductor;
	Block Block3 = ModBlocks.factory_advanced_furnace;
	Block Block4 = ModBlocks.factory_advanced_core;
	
	@Override
	public boolean generate(World world, Random rand, BlockPos position) {
		int i = rand.nextInt(1);

		if(i == 0)
		{
		    generate_r0(world, rand, new BlockPos.MutableBlockPos(position));
		}

       return true;
	}
	
	public boolean generate_r0(World world, Random rand, BlockPos.MutableBlockPos pos)
	{
		int x = pos.getX() - 1;
		int y = pos.getY();
		int z = pos.getZ() - 1;
		
		world.setBlockState(pos.setPos(x + 0, y + 0, z + 0), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 0, z + 0), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 0, z + 0), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 0, z + 1), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 0, z + 1), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 0, z + 1), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 0, z + 2), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 0, z + 2), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 0, z + 2), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 1, z + 0), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 1, z + 0), Block3.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.values()[2]), 3);
		world.setBlockState(pos.setPos(x + 2, y + 1, z + 0), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 1, z + 1), Block3.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.values()[4]), 3);
		world.setBlockState(pos.setPos(x + 1, y + 1, z + 1), Block4.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 1, z + 1), Block3.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.values()[5]), 3);
		world.setBlockState(pos.setPos(x + 0, y + 1, z + 2), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 1, z + 2), Block3.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.values()[3]), 3);
		world.setBlockState(pos.setPos(x + 2, y + 1, z + 2), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 2, z + 0), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 2, z + 0), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 2, z + 0), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 2, z + 1), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 2, z + 1), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 2, z + 1), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 2, z + 2), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 2, z + 2), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 2, z + 2), Block1.getDefaultState(), 3);
		return true;

	}

}
