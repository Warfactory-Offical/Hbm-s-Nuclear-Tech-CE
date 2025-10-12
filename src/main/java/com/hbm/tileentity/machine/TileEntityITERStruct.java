package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineITER;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.ForgeDirection;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
public class TileEntityITERStruct extends TileEntity implements ITickable {

	public static final int[][][] layout = new int[][][] {

		new int[][] {
			new int[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
			new int[] {0,0,0,0,0,1,1,1,1,1,0,0,0,0,0},
			new int[] {0,0,0,0,1,1,1,1,1,1,1,0,0,0,0},
			new int[] {0,0,0,1,1,0,0,0,0,0,1,1,0,0,0},
			new int[] {0,0,1,1,0,0,0,0,0,0,0,1,1,0,0},
			new int[] {0,1,1,0,0,0,0,0,0,0,0,0,1,1,0},
			new int[] {0,1,1,0,0,0,0,0,0,0,0,0,1,1,0},
			new int[] {0,1,1,0,0,0,0,3,0,0,0,0,1,1,0},
			new int[] {0,1,1,0,0,0,0,0,0,0,0,0,1,1,0},
			new int[] {0,1,1,0,0,0,0,0,0,0,0,0,1,1,0},
			new int[] {0,0,1,1,0,0,0,0,0,0,0,1,1,0,0},
			new int[] {0,0,0,1,1,0,0,0,0,0,1,1,0,0,0},
			new int[] {0,0,0,0,1,1,1,1,1,1,1,0,0,0,0},
			new int[] {0,0,0,0,0,1,1,1,1,1,0,0,0,0,0},
			new int[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
		},
		new int[][] {
			new int[] {0,0,0,0,0,0,1,1,1,0,0,0,0,0,0},
			new int[] {0,0,0,0,1,1,0,0,0,1,1,0,0,0,0},
			new int[] {0,0,0,1,0,0,0,0,0,0,0,1,0,0,0},
			new int[] {0,0,1,0,0,1,1,1,1,1,0,0,1,0,0},
			new int[] {0,1,0,0,1,0,2,2,2,0,1,0,0,1,0},
			new int[] {0,1,0,1,0,2,0,2,0,2,0,1,0,1,0},
			new int[] {1,0,0,1,2,0,0,2,0,0,2,1,0,0,1},
			new int[] {1,0,0,1,2,2,2,3,2,2,2,1,0,0,1},
			new int[] {1,0,0,1,2,0,0,2,0,0,2,1,0,0,1},
			new int[] {0,1,0,1,0,2,0,2,0,2,0,1,0,1,0},
			new int[] {0,1,0,0,1,0,2,2,2,0,1,0,0,1,0},
			new int[] {0,0,1,0,0,1,1,1,1,1,0,0,1,0,0},
			new int[] {0,0,0,1,0,0,0,0,0,0,0,1,0,0,0},
			new int[] {0,0,0,0,1,1,0,0,0,1,1,0,0,0,0},
			new int[] {0,0,0,0,0,0,1,1,1,0,0,0,0,0,0}
		},
		new int[][] {
			new int[] {0,0,0,0,0,0,1,1,1,0,0,0,0,0,0},
			new int[] {0,0,0,0,1,1,0,0,0,1,1,0,0,0,0},
			new int[] {0,0,0,4,0,0,0,0,0,0,0,4,0,0,0},
			new int[] {0,0,4,0,0,1,1,1,1,1,0,0,4,0,0},
			new int[] {0,1,0,0,1,0,2,2,2,0,1,0,0,1,0},
			new int[] {0,1,0,1,0,2,0,0,0,2,0,1,0,1,0},
			new int[] {1,0,0,1,2,0,0,0,0,0,2,1,0,0,1},
			new int[] {1,0,0,1,2,0,0,3,0,0,2,1,0,0,1},
			new int[] {1,0,0,1,2,0,0,0,0,0,2,1,0,0,1},
			new int[] {0,1,0,1,0,2,0,0,0,2,0,1,0,1,0},
			new int[] {0,1,0,0,1,0,2,2,2,0,1,0,0,1,0},
			new int[] {0,0,4,0,0,1,1,1,1,1,0,0,4,0,0},
			new int[] {0,0,0,4,0,0,0,0,0,0,0,4,0,0,0},
			new int[] {0,0,0,0,1,1,0,0,0,1,1,0,0,0,0},
			new int[] {0,0,0,0,0,0,1,1,1,0,0,0,0,0,0}
		}
	};
	
	public static final int[][][] collisionMask = new int[][][] {

		new int[][] {
			new int[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
			new int[] {0,0,0,0,0,1,1,1,1,1,0,0,0,0,0},
			new int[] {0,0,0,0,1,1,1,1,1,1,1,0,0,0,0},
			new int[] {0,0,0,1,1,0,0,0,0,0,1,1,0,0,0},
			new int[] {0,0,1,1,0,0,0,0,0,0,0,1,1,0,0},
			new int[] {0,1,1,0,0,0,0,0,0,0,0,0,1,1,0},
			new int[] {0,1,1,0,0,0,0,0,0,0,0,0,1,1,0},
			new int[] {0,1,1,0,0,0,0,3,0,0,0,0,1,1,0},
			new int[] {0,1,1,0,0,0,0,0,0,0,0,0,1,1,0},
			new int[] {0,1,1,0,0,0,0,0,0,0,0,0,1,1,0},
			new int[] {0,0,1,1,0,0,0,0,0,0,0,1,1,0,0},
			new int[] {0,0,0,1,1,0,0,0,0,0,1,1,0,0,0},
			new int[] {0,0,0,0,1,1,1,1,1,1,1,0,0,0,0},
			new int[] {0,0,0,0,0,1,1,1,1,1,0,0,0,0,0},
			new int[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
		},
		new int[][] {
			new int[] {0,0,0,0,0,0,1,1,1,0,0,0,0,0,0},
			new int[] {0,0,0,0,1,1,1,1,1,1,1,0,0,0,0},
			new int[] {0,0,0,1,1,1,1,1,1,1,1,1,0,0,0},
			new int[] {0,0,1,1,1,1,1,1,1,1,1,1,1,0,0},
			new int[] {0,1,1,1,1,0,2,2,2,0,1,1,1,1,0},
			new int[] {0,1,1,1,0,2,0,0,0,2,0,1,1,1,0},
			new int[] {1,1,1,1,2,0,0,0,0,0,2,1,1,1,1},
			new int[] {1,1,1,1,2,0,0,3,0,0,2,1,1,1,1},
			new int[] {1,1,1,1,2,0,0,0,0,0,2,1,1,1,1},
			new int[] {0,1,1,1,0,2,0,0,0,2,0,1,1,1,0},
			new int[] {0,1,1,1,1,0,2,2,2,0,1,1,1,1,0},
			new int[] {0,0,1,1,1,1,1,1,1,1,1,1,1,0,0},
			new int[] {0,0,0,1,1,1,1,1,1,1,1,1,0,0,0},
			new int[] {0,0,0,0,1,1,1,1,1,1,1,0,0,0,0},
			new int[] {0,0,0,0,0,0,1,1,1,0,0,0,0,0,0}
		},
		new int[][] {
			new int[] {0,0,0,0,0,0,1,1,1,0,0,0,0,0,0},
			new int[] {0,0,0,0,1,1,1,1,1,1,1,0,0,0,0},
			new int[] {0,0,0,1,1,1,1,1,1,1,1,1,0,0,0},
			new int[] {0,0,1,1,1,1,1,1,1,1,1,1,1,0,0},
			new int[] {0,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			new int[] {0,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			new int[] {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
			new int[] {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
			new int[] {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
			new int[] {0,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			new int[] {0,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
			new int[] {0,0,1,1,1,1,1,1,1,1,1,1,1,0,0},
			new int[] {0,0,0,1,1,1,1,1,1,1,1,1,0,0,0},
			new int[] {0,0,0,0,1,1,1,1,1,1,1,0,0,0,0},
			new int[] {0,0,0,0,0,0,1,1,1,0,0,0,0,0,0}
		}
	};
	
	int age;

	@Override
	public void update() {

		if(world.isRemote)
			return;

		age++;

		if(age < 20)
			return;

		age = 0;
		
		for(int y = 0; y < 5; y++) {
			for(int x = 0; x < layout[0].length; x++) {
				for(int z = 0; z < layout[0][0].length; z++) {

					int ly = y > 2 ? 4 - y : y;

					int width = 7;

					if(x == width && y == 0 && z == width)
						continue;

					int b = layout[ly][x][z];
					IBlockState state = world.getBlockState(new BlockPos(pos.getX() + x - width, pos.getY() + y, pos.getZ() + z - width));
                    Block block = state.getBlock();

                    switch (b) {
                        case 1 -> {
                            int meta = block.getMetaFromState(state);
                            if (block != ModBlocks.fusion_conductor || meta != 1) return;
                        }
                        case 2 -> {
                            if (block != ModBlocks.fusion_center) return;
                        }
                        case 3 -> {
                            if (block != ModBlocks.fusion_motor) return;
                        }
                        case 4 -> {
                            if (block != ModBlocks.reinforced_glass) return;
                        }
                    }
				}
			}
		}

		for(int x = -2; x <= 2; x++)
			for(int y = 1; y <= 3; y++)
				for(int z = -2; z <= 2; z++)
					world.setBlockToAir(new BlockPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z));

		BlockDummyable.safeRem = true;
		world.setBlockState(new BlockPos(pos.getX(), pos.getY() + 2, pos.getZ()), ModBlocks.iter.getDefaultState().withProperty(MachineITER.META, 12), 3);
		((MachineITER)ModBlocks.iter).fillSpace(world, pos.getX(), pos.getY(), pos.getZ(), ForgeDirection.UNKNOWN, 0);
		BlockDummyable.safeRem = false;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}