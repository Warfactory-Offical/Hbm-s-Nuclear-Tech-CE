package com.hbm.world.generator.room;

import com.hbm.blocks.ModBlocks;
import com.hbm.world.generator.CellularDungeon;
import com.hbm.world.generator.CellularDungeonRoom;
import com.hbm.world.generator.DungeonToolbox;
import com.hbm.world.phased.AbstractPhasedStructure;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.ArrayList;

public class TestDungeonRoom5 extends CellularDungeonRoom {
	
	public TestDungeonRoom5(CellularDungeon parent) {
		super(parent);
	}

	public void generateMain(AbstractPhasedStructure.LegacyBuilder world, int x, int y, int z) {
		
		super.generateMain(world, x, y, z);
		DungeonToolbox.generateBox(world, x, y + parent.height - 2, z, parent.width, 1, parent.width, new ArrayList<IBlockState>() {
			private static final long serialVersionUID = 1323811394991457000L;

		{ add(Blocks.AIR.getDefaultState()); add(Blocks.WEB.getDefaultState()); }});

		DungeonToolbox.generateBox(world, x + 1, y, z + 1, parent.width - 2, 1, parent.width - 2, new ArrayList<IBlockState>() {
			private static final long serialVersionUID = -3394611263228863425L;

		{ add(ModBlocks.meteor_polished.getDefaultState()); add(ModBlocks.meteor_polished.getDefaultState()); add(ModBlocks.meteor_polished.getDefaultState()); add(ModBlocks.meteor_polished.getDefaultState()); add(ModBlocks.meteor_polished.getDefaultState()); add(ModBlocks.meteor_spawner.getDefaultState()); }});
	}

	public void generateWall(AbstractPhasedStructure.LegacyBuilder world, int x, int y, int z, EnumFacing wall, boolean door) {
		
		if(wall != EnumFacing.SOUTH)
			super.generateWall(world, x, y, z, wall, door);
	}
}