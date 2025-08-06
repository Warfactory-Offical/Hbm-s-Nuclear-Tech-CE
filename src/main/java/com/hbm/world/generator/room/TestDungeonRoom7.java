package com.hbm.world.generator.room;

import com.hbm.blocks.ModBlocks;
import com.hbm.world.generator.CellularDungeon;
import com.hbm.world.generator.CellularDungeonRoom;
import com.hbm.world.generator.DungeonToolbox;
import com.hbm.world.phased.AbstractPhasedStructure;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TestDungeonRoom7 extends CellularDungeonRoom {

	public TestDungeonRoom7(CellularDungeon parent) {
		super(parent);
	}

	public void generateMain(AbstractPhasedStructure.LegacyBuilder world, int x, int y, int z) {
		
		super.generateMain(world, x, y, z);

		DungeonToolbox.generateBox(world, x, y, z, parent.width, 1, parent.width, ModBlocks.meteor_polished.getDefaultState());
		DungeonToolbox.generateBox(world, x + 2, y, z + 2, parent.width - 4, 1, parent.width - 4, ModBlocks.deco_red_copper.getDefaultState());
		DungeonToolbox.generateBox(world, x + 3, y, z + 3, parent.width - 6, 1, parent.width - 6, ModBlocks.meteor_polished.getDefaultState());
		DungeonToolbox.generateBox(world, x + 4, y, z + 4, parent.width - 8, 1, parent.width - 8, ModBlocks.deco_red_copper.getDefaultState());

		world.setBlockState(new BlockPos(x + parent.width / 2, y, z + parent.width / 2), ModBlocks.meteor_battery.getDefaultState());
		world.setBlockState(new BlockPos(x + parent.width / 2, y + 1, z + parent.width / 2), ModBlocks.tesla.getDefaultState());
	}
}