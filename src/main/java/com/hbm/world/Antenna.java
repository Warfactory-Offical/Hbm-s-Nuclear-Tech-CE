package com.hbm.world;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.handler.WeightedRandomChestContentFrom1710;
import com.hbm.lib.HbmChestContents;
import com.hbm.world.phased.AbstractPhasedStructure;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Antenna extends AbstractPhasedStructure {
	public static final Antenna INSTANCE = new Antenna();
	private Antenna() {}
	protected Block[] GetValidSpawnBlocks()
	{
		return new Block[]
		{
			Blocks.GRASS,
			Blocks.DIRT,
			Blocks.STONE,
			Blocks.SAND,
		};
	}

	@Override
	public boolean checkSpawningConditions(@NotNull World world, @NotNull BlockPos pos) {
		return locationIsValidSpawn(world, pos.add(1, 0, 1));
	}

	@Override
	public @NotNull List<BlockPos> getValidationPoints(@NotNull BlockPos origin) {
		return Collections.singletonList(origin.add(1, 0, 1));
	}

	public boolean locationIsValidSpawn(World world, BlockPos pos) {

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
	public void buildStructure(@NotNull LegacyBuilder builder, @NotNull Random rand) {
		generate_r0(builder, rand, 0, 0, 0);
	}

	public boolean generate_r0(LegacyBuilder world, Random rand, int x, int y, int z) {
		MutableBlockPos pos = new BlockPos.MutableBlockPos();
		world.setBlockState(pos.setPos(x + 0, y + 0, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 0, z + 0), ModBlocks.steel_poles.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.values()[2]), 3);
		world.setBlockState(pos.setPos(x + 2, y + 0, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 0, z + 1), ModBlocks.steel_poles.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.values()[4]), 3);
		world.setBlockState(pos.setPos(x + 1, y + 0, z + 1), ModBlocks.deco_steel.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 0, z + 1), ModBlocks.tape_recorder.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.values()[5]), 3);
		world.setBlockState(pos.setPos(x + 0, y + 0, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 0, z + 2), ModBlocks.steel_poles.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.values()[3]), 3);
		world.setBlockState(pos.setPos(x + 2, y + 0, z + 2), Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, EnumFacing.EAST),
				(worldIn, random, blockPos, chest) -> WeightedRandomChestContentFrom1710.generateChestContents(random, HbmChestContents.getLoot(2), chest, 8));
		world.setBlockState(pos.setPos(x + 0, y + 1, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 1, z + 0), ModBlocks.steel_poles.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.values()[2]), 3);
		world.setBlockState(pos.setPos(x + 2, y + 1, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 1, z + 1), ModBlocks.steel_poles.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.values()[4]), 3);
		world.setBlockState(pos.setPos(x + 1, y + 1, z + 1), ModBlocks.deco_steel.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 1, z + 1), ModBlocks.tape_recorder.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.values()[5]), 3);
		world.setBlockState(pos.setPos(x + 0, y + 1, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 1, z + 2), ModBlocks.steel_poles.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.values()[3]), 3);
		world.setBlockState(pos.setPos(x + 2, y + 1, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 2, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 2, z + 0), ModBlocks.deco_steel.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 2, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 2, z + 1), ModBlocks.deco_steel.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 2, z + 1), ModBlocks.deco_steel.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 2, z + 1), ModBlocks.deco_steel.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 2, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 2, z + 2), ModBlocks.deco_steel.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 2, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 3, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 3, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 3, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 3, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 3, z + 1), ModBlocks.steel_poles.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.values()[4]), 3);
		world.setBlockState(pos.setPos(x + 2, y + 3, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 3, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 3, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 3, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 4, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 4, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 4, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 4, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 4, z + 1), ModBlocks.steel_poles.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.values()[4]), 3);
		world.setBlockState(pos.setPos(x + 2, y + 4, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 4, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 4, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 4, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 5, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 5, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 5, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 5, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 5, z + 1), ModBlocks.steel_poles.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.values()[4]), 3);
		world.setBlockState(pos.setPos(x + 2, y + 5, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 5, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 5, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 5, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 6, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 6, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 6, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 6, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 6, z + 1), ModBlocks.steel_poles.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.values()[4]), 3);
		world.setBlockState(pos.setPos(x + 2, y + 6, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 6, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 6, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 6, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 7, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 7, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 7, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 7, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 7, z + 1), ModBlocks.steel_poles.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.values()[4]), 3);
		world.setBlockState(pos.setPos(x + 2, y + 7, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 7, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 7, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 7, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 8, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 8, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 8, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 8, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 8, z + 1), ModBlocks.steel_poles.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.values()[4]), 3);
		world.setBlockState(pos.setPos(x + 2, y + 8, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 8, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 8, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 8, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 9, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 9, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 9, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 9, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 9, z + 1), ModBlocks.steel_poles.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.values()[4]), 3);
		world.setBlockState(pos.setPos(x + 2, y + 9, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 9, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 9, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 9, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 10, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 10, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 10, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 10, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 10, z + 1), ModBlocks.steel_poles.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.values()[4]), 3);
		world.setBlockState(pos.setPos(x + 2, y + 10, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 10, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 10, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 10, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 11, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 11, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 11, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 11, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 11, z + 1), ModBlocks.steel_poles.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.values()[4]), 3);
		world.setBlockState(pos.setPos(x + 2, y + 11, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 11, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 11, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 11, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 12, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 12, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 12, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 12, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 12, z + 1), ModBlocks.steel_poles.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.values()[4]), 3);
		world.setBlockState(pos.setPos(x + 2, y + 12, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 12, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 12, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 12, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 13, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 13, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 13, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 13, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 13, z + 1), ModBlocks.pole_satellite_receiver.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.values()[3]), 3);
		world.setBlockState(pos.setPos(x + 2, y + 13, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 13, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 13, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 13, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 14, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 14, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 14, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 14, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 14, z + 1), ModBlocks.steel_poles.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.values()[4]), 3);
		world.setBlockState(pos.setPos(x + 2, y + 14, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 14, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 14, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 14, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 15, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 15, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 15, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 15, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 15, z + 1), ModBlocks.steel_poles.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.values()[4]), 3);
		world.setBlockState(pos.setPos(x + 2, y + 15, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 15, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 15, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 15, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 16, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 16, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 16, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 16, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 16, z + 1), ModBlocks.steel_poles.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.values()[4]), 3);
		world.setBlockState(pos.setPos(x + 2, y + 16, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 16, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 16, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 16, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 17, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 17, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 17, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 17, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 17, z + 1), ModBlocks.pole_satellite_receiver.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.values()[2]), 3);
		world.setBlockState(pos.setPos(x + 2, y + 17, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 17, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 17, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 17, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 18, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 18, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 18, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 18, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 18, z + 1), ModBlocks.pole_satellite_receiver.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.values()[4]), 3);
		world.setBlockState(pos.setPos(x + 2, y + 18, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 18, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 18, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 18, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 19, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 19, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 19, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 19, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 19, z + 1), ModBlocks.steel_poles.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.values()[4]), 3);
		world.setBlockState(pos.setPos(x + 2, y + 19, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 19, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 19, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 19, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 20, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 20, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 20, z + 0), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 20, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 20, z + 1), ModBlocks.pole_top.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 20, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 20, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 20, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 20, z + 2), Blocks.AIR.getDefaultState(), 3);
		if(GeneralConfig.enableDebugMode)
			System.out.print("[Debug] Successfully spawned antenna at " + x + " " + y +" " + z + "\n");
		return true;

	}

}
