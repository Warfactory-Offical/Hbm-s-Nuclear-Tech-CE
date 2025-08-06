package com.hbm.world;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineBattery;
import com.hbm.config.GeneralConfig;
import com.hbm.handler.WeightedRandomChestContentFrom1710;
import com.hbm.lib.HbmChestContents;
import com.hbm.world.phased.AbstractPhasedStructure;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockStairs;
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

public class CrashedVertibird extends AbstractPhasedStructure {
	public static final CrashedVertibird INSTANCE = new CrashedVertibird();
	private CrashedVertibird() {}
	Block Block1 = ModBlocks.deco_steel;
	Block Block2 = ModBlocks.deco_tungsten;
	Block Block3 = ModBlocks.reinforced_glass;
	Block Block4 = ModBlocks.deco_titanium;
	
	protected Block[] GetValidSpawnBlocks()
	{
		return new Block[]
		{
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
	public void buildStructure(@NotNull LegacyBuilder builder, @NotNull Random rand) {
		generate_r0(builder, rand, 0, 0, 0);
	}

	@Override
	public boolean checkSpawningConditions(@NotNull World world, @NotNull BlockPos pos) {
		return LocationIsValidSpawn(world, pos.add(9, 0, 9));
	}

	@Override
	public @NotNull List<BlockPos> getValidationPoints(@NotNull BlockPos origin) {
		return Collections.singletonList(origin.add(9, 0, 9));
	}

	public boolean generate_r0(LegacyBuilder world, Random rand, int x, int y, int z)
	{
		int yOffset = 8 + rand.nextInt(4);
		MutableBlockPos pos = new BlockPos.MutableBlockPos();

		world.setBlockState(pos.setPos(x + 4, y + 0 - yOffset, z + 1), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 0 - yOffset, z + 1), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 0 - yOffset, z + 1), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 0 - yOffset, z + 2), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 1 - yOffset, z + 0), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 1 - yOffset, z + 0), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 1 - yOffset, z + 0), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 1 - yOffset, z + 1), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 1 - yOffset, z + 1), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 1 - yOffset, z + 2), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 1 - yOffset, z + 2), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 1 - yOffset, z + 2), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 1 - yOffset, z + 3), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 1 - yOffset, z + 3), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 1 - yOffset, z + 3), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 1 - yOffset, z + 3), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 1 - yOffset, z + 3), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 2 - yOffset, z + 0), Block3.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 2 - yOffset, z + 0), Block3.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 2 - yOffset, z + 1), Block3.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 2 - yOffset, z + 1), Block3.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 2 - yOffset, z + 2), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 2 - yOffset, z + 2), Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM).withProperty(BlockStairs.FACING, EnumFacing.SOUTH), 3);
		world.setBlockState(pos.setPos(x + 6, y + 2 - yOffset, z + 2), Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM).withProperty(BlockStairs.FACING, EnumFacing.SOUTH), 3);
		world.setBlockState(pos.setPos(x + 7, y + 2 - yOffset, z + 2), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 2 - yOffset, z + 3), Block4.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 2 - yOffset, z + 4), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 2 - yOffset, z + 4), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 2 - yOffset, z + 4), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 2 - yOffset, z + 4), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 2 - yOffset, z + 4), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 2 - yOffset, z + 5), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 2 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 2 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 2 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 2 - yOffset, z + 5), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 2 - yOffset, z + 7), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 2 - yOffset, z + 7), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 3 - yOffset, z + 0), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 3 - yOffset, z + 0), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 3 - yOffset, z + 0), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 3 - yOffset, z + 1), Block3.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 3 - yOffset, z + 1), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 3 - yOffset, z + 1), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 3 - yOffset, z + 1), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 3 - yOffset, z + 1), Block3.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 3 - yOffset, z + 2), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 3 - yOffset, z + 2), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 3 - yOffset, z + 3), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 3 - yOffset, z + 3), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 3 - yOffset, z + 4), Block4.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 3 - yOffset, z + 5), Block4.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 3 - yOffset, z + 5), Block4.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 3 - yOffset, z + 6), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 3 - yOffset, z + 6), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 3 - yOffset, z + 6), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 3 - yOffset, z + 7), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 3 - yOffset, z + 7), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 3 - yOffset, z + 7), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 3 - yOffset, z + 9), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 4 - yOffset, z + 1), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 4 - yOffset, z + 1), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 4 - yOffset, z + 1), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 4 - yOffset, z + 2), Block3.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 4 - yOffset, z + 3), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 4 - yOffset, z + 3), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 4 - yOffset, z + 5), Block4.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 4 - yOffset, z + 6), Block4.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 4 - yOffset, z + 6), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 4 - yOffset, z + 6), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 4 - yOffset, z + 7), Block4.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 4 - yOffset, z + 7), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 4 - yOffset, z + 7), Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, EnumFacing.NORTH),
				(worldIn, random, blockPos, chest) ->
						WeightedRandomChestContentFrom1710.generateChestContents(random, HbmChestContents.getLoot(6), chest, 8));
		world.setBlockState(pos.setPos(x + 7, y + 4 - yOffset, z + 7), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 4 - yOffset, z + 8), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 4 - yOffset, z + 8), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 4 - yOffset, z + 8), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 4 - yOffset, z + 9), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 4 - yOffset, z + 9), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 4 - yOffset, z + 9), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 5 - yOffset, z + 1), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 5 - yOffset, z + 2), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 5 - yOffset, z + 2), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 5 - yOffset, z + 2), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 5 - yOffset, z + 3), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 5 - yOffset, z + 3), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 5 - yOffset, z + 3), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 5 - yOffset, z + 3), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 5 - yOffset, z + 4), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 5 - yOffset, z + 4), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 5 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 5 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 5 - yOffset, z + 6), Block4.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 5 - yOffset, z + 6), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 5 - yOffset, z + 6), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 5 - yOffset, z + 7), Block4.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 5 - yOffset, z + 7), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 5 - yOffset, z + 7), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 5 - yOffset, z + 8), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 5 - yOffset, z + 8), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 5 - yOffset, z + 8), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 5 - yOffset, z + 9), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 5 - yOffset, z + 9), Block4.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 5 - yOffset, z + 9), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 6 - yOffset, z + 3), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 6 - yOffset, z + 3), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 6 - yOffset, z + 4), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 6 - yOffset, z + 4), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 6 - yOffset, z + 4), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 6 - yOffset, z + 4), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 14, y + 6 - yOffset, z + 4), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 6 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 6 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 6 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 6 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 6 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 6 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 6 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 6 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 8, y + 6 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 9, y + 6 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 10, y + 6 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 13, y + 6 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 6 - yOffset, z + 6), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 6 - yOffset, z + 6), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 6 - yOffset, z + 6), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 6 - yOffset, z + 6), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 6 - yOffset, z + 7), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 6 - yOffset, z + 7), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 6 - yOffset, z + 7), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 6 - yOffset, z + 7), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 6 - yOffset, z + 7), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 6 - yOffset, z + 8), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 6 - yOffset, z + 8), ModBlocks.machine_battery.getDefaultState().withProperty(MachineBattery.FACING, EnumFacing.NORTH), 3);
		world.setBlockState(pos.setPos(x + 6, y + 6 - yOffset, z + 8), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 6 - yOffset, z + 9), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 6 - yOffset, z + 9), ModBlocks.red_wire_coated.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 6 - yOffset, z + 9), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 6 - yOffset, z + 10), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 6 - yOffset, z + 10), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 6 - yOffset, z + 10), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 6 - yOffset, z + 11), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 6 - yOffset, z + 11), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 7 - yOffset, z + 4), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 7 - yOffset, z + 4), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 7 - yOffset, z + 4), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 7 - yOffset, z + 4), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 14, y + 7 - yOffset, z + 4), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 7 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 7 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 7 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 7 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 7 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 7 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 7 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 8, y + 7 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 9, y + 7 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 10, y + 7 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 11, y + 7 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 12, y + 7 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 13, y + 7 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 14, y + 7 - yOffset, z + 5), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 7 - yOffset, z + 6), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 7 - yOffset, z + 6), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 7 - yOffset, z + 6), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 7 - yOffset, z + 6), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 7 - yOffset, z + 6), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 14, y + 7 - yOffset, z + 6), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 7 - yOffset, z + 7), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 7 - yOffset, z + 7), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 7 - yOffset, z + 7), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 7 - yOffset, z + 7), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 7 - yOffset, z + 7), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 7 - yOffset, z + 8), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 7 - yOffset, z + 8), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 7 - yOffset, z + 8), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 7 - yOffset, z + 8), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 7 - yOffset, z + 8), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 7 - yOffset, z + 9), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 7 - yOffset, z + 9), ModBlocks.machine_generator.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 7 - yOffset, z + 9), ModBlocks.red_wire_coated.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 7 - yOffset, z + 9), ModBlocks.machine_generator.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 7 - yOffset, z + 9), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 7 - yOffset, z + 10), Block4.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 7 - yOffset, z + 10), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 7 - yOffset, z + 11), Block4.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 7 - yOffset, z + 11), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 8 - yOffset, z + 4), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 14, y + 8 - yOffset, z + 4), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 8 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 8 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 8 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 13, y + 8 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 14, y + 8 - yOffset, z + 5), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 15, y + 8 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 8 - yOffset, z + 6), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 8 - yOffset, z + 6), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 8 - yOffset, z + 6), Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, EnumFacing.NORTH),
				(worldIn, random, blockPos, chest) ->
						WeightedRandomChestContentFrom1710.generateChestContents(random, HbmChestContents.getLoot(3), chest, 8));
		world.setBlockState(pos.setPos(x + 6, y + 8 - yOffset, z + 6), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 8 - yOffset, z + 6), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 14, y + 8 - yOffset, z + 6), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 8 - yOffset, z + 7), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 8 - yOffset, z + 7), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 8 - yOffset, z + 7), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 8 - yOffset, z + 7), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 8 - yOffset, z + 7), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 8 - yOffset, z + 8), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 8 - yOffset, z + 8), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 8 - yOffset, z + 8), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 8 - yOffset, z + 8), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 8 - yOffset, z + 8), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 8 - yOffset, z + 9), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 8 - yOffset, z + 9), ModBlocks.machine_generator.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 8 - yOffset, z + 9), ModBlocks.red_wire_coated.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 8 - yOffset, z + 9), ModBlocks.machine_generator.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 8 - yOffset, z + 9), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 8 - yOffset, z + 10), ModBlocks.machine_generator.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 8 - yOffset, z + 10), ModBlocks.red_wire_coated.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 8 - yOffset, z + 10), ModBlocks.machine_generator.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 8 - yOffset, z + 10), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 8 - yOffset, z + 11), Block4.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 8 - yOffset, z + 11), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 8 - yOffset, z + 11), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 8 - yOffset, z + 12), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 14, y + 9 - yOffset, z + 4), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 13, y + 9 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 14, y + 9 - yOffset, z + 5), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 15, y + 9 - yOffset, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 9 - yOffset, z + 6), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 9 - yOffset, z + 6), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 9 - yOffset, z + 6), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 14, y + 9 - yOffset, z + 6), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 9 - yOffset, z + 7), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 9 - yOffset, z + 7), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 9 - yOffset, z + 7), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 9 - yOffset, z + 8), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 9 - yOffset, z + 8), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 9 - yOffset, z + 8), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 9 - yOffset, z + 8), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 9 - yOffset, z + 8), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 9 - yOffset, z + 9), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 9 - yOffset, z + 9), Block4.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 9 - yOffset, z + 9), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 9 - yOffset, z + 10), ModBlocks.machine_generator.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 9 - yOffset, z + 10), ModBlocks.red_wire_coated.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 9 - yOffset, z + 10), ModBlocks.machine_generator.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 9 - yOffset, z + 10), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 9 - yOffset, z + 11), ModBlocks.red_wire_coated.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 9 - yOffset, z + 11), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 9 - yOffset, z + 12), Block4.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 9 - yOffset, z + 12), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 9 - yOffset, z + 12), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 9 - yOffset, z + 13), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 9 - yOffset, z + 13), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 14, y + 10 - yOffset, z + 5), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 10 - yOffset, z + 8), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 10 - yOffset, z + 8), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 10 - yOffset, z + 8), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 10 - yOffset, z + 9), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 10 - yOffset, z + 10), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 10 - yOffset, z + 10), Block4.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 10 - yOffset, z + 10), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 10 - yOffset, z + 11), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 10 - yOffset, z + 11), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 10 - yOffset, z + 11), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 10 - yOffset, z + 12), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 10 - yOffset, z + 12), ModBlocks.red_wire_coated.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 10 - yOffset, z + 12), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 10 - yOffset, z + 13), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 10 - yOffset, z + 13), ModBlocks.red_wire_coated.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 10 - yOffset, z + 13), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 10 - yOffset, z + 14), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 10 - yOffset, z + 15), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 10 - yOffset, z + 17), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 10 - yOffset, z + 18), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 14, y + 11 - yOffset, z + 4), Block4.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 12, y + 11 - yOffset, z + 5), Block4.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 13, y + 11 - yOffset, z + 5), Block4.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 14, y + 11 - yOffset, z + 5), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 15, y + 11 - yOffset, z + 5), Block4.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 16, y + 11 - yOffset, z + 5), Block4.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 17, y + 11 - yOffset, z + 5), Block4.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 14, y + 11 - yOffset, z + 6), Block4.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 14, y + 11 - yOffset, z + 7), Block4.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 11 - yOffset, z + 10), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 11 - yOffset, z + 11), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 11 - yOffset, z + 12), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 11 - yOffset, z + 12), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 11 - yOffset, z + 12), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 11 - yOffset, z + 13), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 11 - yOffset, z + 14), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 11 - yOffset, z + 14), ModBlocks.red_wire_coated.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 11 - yOffset, z + 14), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 11 - yOffset, z + 15), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 11 - yOffset, z + 15), ModBlocks.red_wire_coated.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 11 - yOffset, z + 15), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 11 - yOffset, z + 16), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 11 - yOffset, z + 17), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 11 - yOffset, z + 18), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 12 - yOffset, z + 14), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 12 - yOffset, z + 16), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 12 - yOffset, z + 16), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 12 - yOffset, z + 16), ModBlocks.red_wire_coated.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 12 - yOffset, z + 16), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 12 - yOffset, z + 16), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 8, y + 12 - yOffset, z + 16), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 9, y + 12 - yOffset, z + 16), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 12 - yOffset, z + 17), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 12 - yOffset, z + 17), ModBlocks.red_wire_coated.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 6, y + 12 - yOffset, z + 17), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 7, y + 12 - yOffset, z + 17), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 8, y + 12 - yOffset, z + 17), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 9, y + 12 - yOffset, z + 17), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 10, y + 12 - yOffset, z + 17), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 5, y + 13 - yOffset, z + 17), Block1.getDefaultState(), 3);

		generate_r02_last(world, rand, x, y, z, yOffset, pos);
		return true;

	}
	public boolean generate_r02_last(LegacyBuilder world, Random rand, int x, int y, int z, int yOffset, MutableBlockPos pos)
	{

		world.setBlockState(pos.setPos(x + 4, y + 1 - yOffset, z + 1), Blocks.LEVER.getDefaultState().withProperty(BlockLever.POWERED, false).withProperty(BlockLever.FACING, BlockLever.EnumOrientation.SOUTH), 3);
		world.setBlockState(pos.setPos(x + 6, y + 1 - yOffset, z + 1), Blocks.LEVER.getDefaultState().withProperty(BlockLever.POWERED, false).withProperty(BlockLever.FACING, BlockLever.EnumOrientation.SOUTH), 3);
		if(GeneralConfig.enableDebugMode)
			System.out.print("[Debug] Successfully spawned crashed Vertibird at " + x + " " + y +" " + z + "\n");
		return true;

	}

}