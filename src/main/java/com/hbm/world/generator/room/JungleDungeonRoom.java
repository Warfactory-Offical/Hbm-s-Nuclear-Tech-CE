package com.hbm.world.generator.room;

import com.hbm.blocks.ModBlocks;
import com.hbm.world.generator.*;
import com.hbm.world.phased.AbstractPhasedStructure;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.List;

public class JungleDungeonRoom extends CellularDungeonRoom {

	public JungleDungeonRoom(CellularDungeon parent) {
		super(parent);
	}
	
	@Override
	public void generateMain(final AbstractPhasedStructure.LegacyBuilder world, final int x, final int y, final int z) {
		if(!(this.parent instanceof JungleDungeon))
			return; //just to be safe
		DungeonToolbox.generateBox(world, x, y, z, parent.width, 1, parent.width, parent.floor);
		DungeonToolbox.generateBox(world, x, y + 1, z, parent.width, parent.height - 1, parent.width, Blocks.AIR.getDefaultState());
		DungeonToolbox.generateBox(world, x, y + parent.height - 1, z, parent.width, 1, parent.width, parent.ceiling);

		int rtd = world.rand.nextInt(50);

		// 1:10 chance to have a lava floor
		if(rtd < 5) {
			List<IBlockState> metas = Arrays.asList(
				ModBlocks.brick_jungle_cracked.getDefaultState(),
				ModBlocks.brick_jungle_lava.getDefaultState(),
				ModBlocks.brick_jungle_lava.getDefaultState()
			);

			DungeonToolbox.generateBox(world, x + parent.width / 2 - 1, y, z + parent.width / 2 - 1, 3, 1, 3, metas);

		// 1:5 chance to have a jungle crate
		} else if(rtd < 10) {
			world.setBlockState(new BlockPos(x + 1 + world.rand.nextInt(parent.width - 1), y + 1, z + world.rand.nextInt(parent.width - 1)), ModBlocks.crate_jungle.getDefaultState(), 2);

		// 1:5 chance to try for making a hole
		} else if(rtd < 20) {

			if(!((JungleDungeon)JungleDungeonRoom.this.parent).hasHole) {

				boolean punched = false;

				for(int a = 0; a < 3; a++) {
					for(int b = 0; b < 3; b++) {

						Block bl = world.getBlockState(new BlockPos(x + 1 + a, y - 4, z + 1 + b)).getBlock();

						if(world.getBlockState(new BlockPos(x + 1 + a, y - 1, z + 1 + b)).getBlock() == Blocks.AIR) {
							if(bl == ModBlocks.brick_jungle || bl == ModBlocks.brick_jungle_cracked || bl == ModBlocks.brick_jungle_lava || bl == ModBlocks.brick_jungle_trap) {
								world.setBlockToAir(new BlockPos(x + 1 + a, y, z + 1 + b));
								punched = true;
							}
						}
					}
				}

				if(punched)
					((JungleDungeon)JungleDungeonRoom.this.parent).hasHole = true;
			}
		}
	}

	@Override
	public void generateWall(final AbstractPhasedStructure.LegacyBuilder world, final int x, final int y, final int z, final EnumFacing wall, final boolean door) {
		if(wall == EnumFacing.NORTH) {
			DungeonToolbox.generateBox(world, x, y + 1, z, parent.width, parent.height - 2, 1, parent.wall);

			if(door)
				DungeonToolbox.generateBox(world, x + parent.width / 2 - 1, y + 1, z, 3, 3, 1, Blocks.AIR.getDefaultState());
		}

		if(wall == EnumFacing.SOUTH) {
			DungeonToolbox.generateBox(world, x, y + 1, z + parent.width - 1, parent.width, parent.height - 2, 1, parent.wall);

			if(door)
				DungeonToolbox.generateBox(world, x + parent.width / 2 - 1, y + 1, z + parent.width - 1, 3, 3, 1, Blocks.AIR.getDefaultState());
		}

		if(wall == EnumFacing.WEST) {
			DungeonToolbox.generateBox(world, x, y + 1, z, 1, parent.height - 2, parent.width, parent.wall);

			if(door)
				DungeonToolbox.generateBox(world, x, y + 1, z + parent.width / 2 - 1, 1, 3, 3, Blocks.AIR.getDefaultState());
		}

		if(wall == EnumFacing.EAST) {
			DungeonToolbox.generateBox(world, x + parent.width - 1, y + 1, z, 1, parent.height - 2, parent.width, parent.wall);

			if(door)
				DungeonToolbox.generateBox(world, x + parent.width - 1, y + 1, z + parent.width / 2 - 1, 1, 3, 3, Blocks.AIR.getDefaultState());
		}
	}
}