package com.hbm.world.generator.room;

import com.hbm.blocks.ModBlocks;
import com.hbm.world.generator.CellularDungeon;
import com.hbm.world.generator.CellularDungeonRoom;
import com.hbm.world.generator.DungeonToolbox;
import com.hbm.world.phased.AbstractPhasedStructure;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TestDungeonRoom6 extends CellularDungeonRoom {

	public TestDungeonRoom6(CellularDungeon parent) {
		super(parent);
	}

	public void generateMain(AbstractPhasedStructure.LegacyBuilder world, int x, int y, int z) {
		
		super.generateMain(world, x, y, z);
		DungeonToolbox.generateBox(world, x + 1, y, z + 1, parent.width - 2, 1, parent.width - 2, ModBlocks.toxic_block.getDefaultState());
		DungeonToolbox.generateBox(world, x + parent.width / 2 - 1, y, z + parent.width / 2 - 1, 3, 1, 3, ModBlocks.meteor_brick_chiseled.getDefaultState());
		world.setBlockState(new BlockPos(x + parent.width / 2, y, z + parent.width / 2), ModBlocks.meteor_polished.getDefaultState());

		world.setBlockState(new BlockPos(x + 1, y, z + parent.width / 2), ModBlocks.meteor_polished.getDefaultState());
		world.setBlockState(new BlockPos(x + parent.width / 2, y, z + 1), ModBlocks.meteor_polished.getDefaultState());
		
		world.setBlockState(new BlockPos(x + parent.width - 2, y, z + parent.width / 2), ModBlocks.meteor_polished.getDefaultState());
		world.setBlockState(new BlockPos(x + parent.width / 2, y, z + parent.width - 2), ModBlocks.meteor_polished.getDefaultState());
		DungeonToolbox.generateBox(world, x, y-1, z, parent.width, 1, parent.width, Blocks.CONCRETE.getStateFromMeta(15));
	}
	
	public void generateWall(AbstractPhasedStructure.LegacyBuilder world, int x, int y, int z, EnumFacing wall, boolean door) {

		super.generateWall(world, x, y, z, wall, door);
		
		if(!door)
			return;
		
		if(wall == EnumFacing.NORTH) {
			DungeonToolbox.generateBox(world, x + parent.width / 2, y, z + 1, 1, 1, parent.width / 2 - 2, ModBlocks.meteor_polished.getDefaultState());
		}
		
		if(wall == EnumFacing.SOUTH) {
			DungeonToolbox.generateBox(world, x + parent.width / 2, y, z + parent.width / 2 + 2, 1, 1, parent.width / 2 - 2, ModBlocks.meteor_polished.getDefaultState());
		}
		
		if(wall == EnumFacing.WEST) {
			DungeonToolbox.generateBox(world, x + 1, y, z + parent.width / 2, parent.width / 2 - 2, 1, 1, ModBlocks.meteor_polished.getDefaultState());
		}
		
		if(wall == EnumFacing.EAST) {
			DungeonToolbox.generateBox(world, x + parent.width / 2 + 2, y, z + parent.width / 2, parent.width / 2 - 2, 1, 1, ModBlocks.meteor_polished.getDefaultState());
		}
	}
}