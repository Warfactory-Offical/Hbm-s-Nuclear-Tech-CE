package com.hbm.world.generator.room;

import com.hbm.blocks.ModBlocks;
import com.hbm.world.generator.CellularDungeon;
import com.hbm.world.generator.CellularDungeonRoom;
import com.hbm.world.phased.AbstractPhasedStructure;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TestDungeonRoom2 extends CellularDungeonRoom {

	public TestDungeonRoom2(CellularDungeon parent) {
		super(parent);
	}

	public void generateMain(AbstractPhasedStructure.LegacyBuilder world, int x, int y, int z) {
		
		super.generateMain(world, x, y, z);

		int j = world.rand.nextInt(2) + 2;
		int k = world.rand.nextInt(3) + 2;
		
		for(int i = 0; i < j; i++) {
			int dx = world.rand.nextInt(parent.width - 6) + 3;
			int dz = world.rand.nextInt(parent.width - 6) + 3;
			world.setBlockState(new BlockPos(x + dx, y + 1, z + dz), ModBlocks.crate_ammo.getDefaultState(), 2);
		}
		
		for(int i = 0; i < k; i++) {
			int dx = world.rand.nextInt(parent.width - 6) + 3;
			int dz = world.rand.nextInt(parent.width - 6) + 3;
			world.setBlockState(new BlockPos(x + dx, y + 1, z + dz), ModBlocks.crate_can.getDefaultState(), 2);
		}
	}
}