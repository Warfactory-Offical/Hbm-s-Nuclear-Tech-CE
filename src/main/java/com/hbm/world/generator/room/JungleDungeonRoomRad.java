package com.hbm.world.generator.room;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.TrappedBrick;
import com.hbm.blocks.generic.TrappedBrick.Trap;
import com.hbm.world.generator.CellularDungeon;
import com.hbm.world.phased.AbstractPhasedStructure;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public class JungleDungeonRoomRad extends JungleDungeonRoom {

	public JungleDungeonRoomRad(CellularDungeon parent) {
		super(parent);
	}

	public void generateMain(final AbstractPhasedStructure.LegacyBuilder world, final int x, final int y, final int z) {
		super.generateMain(world, x, y, z);
		int ix = world.rand.nextInt(3) + 1;
		int iz = world.rand.nextInt(3) + 1;

		Block bl = world.getBlockState(new BlockPos(x + ix, y, z + iz)).getBlock();
		if(bl == ModBlocks.brick_jungle || bl == ModBlocks.brick_jungle_cracked || bl == ModBlocks.brick_jungle_lava) {
			world.setBlockState(new BlockPos(x + ix, y, z + iz), ModBlocks.brick_jungle_trap.getDefaultState().withProperty(TrappedBrick.TYPE, Trap.RAD_CONVERSION.ordinal()), 3);
		}
	}
}