package com.hbm.world.generator.room;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.TrappedBrick;
import com.hbm.blocks.generic.TrappedBrick.Trap;
import com.hbm.world.generator.CellularDungeon;
import com.hbm.world.phased.AbstractPhasedStructure;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public class JungleDungeonRoomSpiders extends JungleDungeonRoom {

	public JungleDungeonRoomSpiders(CellularDungeon parent) {
		super(parent);
	}

	public void generateMain(final AbstractPhasedStructure.LegacyBuilder world, final int x, final int y, final int z) {
		super.generateMain(world, x, y, z);
		Block bl = world.getBlockState(new BlockPos(x + 2, y + 4, z + 2)).getBlock();
		if(bl == ModBlocks.brick_jungle || bl == ModBlocks.brick_jungle_cracked || bl == ModBlocks.brick_jungle_lava) {
			world.setBlockState(new BlockPos(x + 2, y + 4, z + 2), ModBlocks.brick_jungle_trap.getDefaultState().withProperty(TrappedBrick.TYPE, Trap.SPIDERS.ordinal()), 3);
		}
	}
}