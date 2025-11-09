package com.hbm.blocks.machine.fusion;

import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.fusion.TileEntityFusionTorus;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class MachineFusionTorus extends BlockDummyable {
    public MachineFusionTorus(String s) {
        super(Material.IRON, s);
    }

    public static final int[][][] layout = new int[][][] {

            new int[][] {
                    new int[] {0,0,0,0,3,3,3,3,3,3,3,0,0,0,0},
                    new int[] {0,0,0,3,1,1,1,1,1,1,1,3,0,0,0},
                    new int[] {0,0,3,1,1,1,1,1,1,1,1,1,3,0,0},
                    new int[] {0,3,1,1,1,1,1,1,1,1,1,1,1,3,0},
                    new int[] {3,1,1,1,1,3,3,3,3,3,1,1,1,1,3},
                    new int[] {3,1,1,1,3,3,3,3,3,3,3,1,1,1,3},
                    new int[] {3,1,1,1,3,3,3,3,3,3,3,1,1,1,3},
                    new int[] {3,1,1,1,3,3,3,3,3,3,3,1,1,1,3},
                    new int[] {3,1,1,1,3,3,3,3,3,3,3,1,1,1,3},
                    new int[] {3,1,1,1,3,3,3,3,3,3,3,1,1,1,3},
                    new int[] {3,1,1,1,1,3,3,3,3,3,1,1,1,1,3},
                    new int[] {0,3,1,1,1,1,1,1,1,1,1,1,1,3,0},
                    new int[] {0,0,3,1,1,1,1,1,1,1,1,1,3,0,0},
                    new int[] {0,0,0,3,1,1,1,1,1,1,1,3,0,0,0},
                    new int[] {0,0,0,0,3,3,3,3,3,3,3,0,0,0,0},
            },
            new int[][] {
                    new int[] {0,0,0,0,1,1,3,3,3,1,1,0,0,0,0},
                    new int[] {0,0,0,1,1,1,1,1,1,1,1,1,0,0,0},
                    new int[] {0,0,1,1,2,2,2,2,2,2,2,1,1,0,0},
                    new int[] {0,1,1,2,1,1,1,1,1,1,1,2,1,1,0},
                    new int[] {1,1,2,1,1,1,1,1,1,1,1,1,2,1,1},
                    new int[] {1,1,2,1,1,3,3,3,3,3,1,1,2,1,1},
                    new int[] {3,1,2,1,1,3,3,3,3,3,1,1,2,1,3},
                    new int[] {3,1,2,1,1,3,3,3,3,3,1,1,2,1,3},
                    new int[] {3,1,2,1,1,3,3,3,3,3,1,1,2,1,3},
                    new int[] {1,1,2,1,1,3,3,3,3,3,1,1,2,1,1},
                    new int[] {1,1,2,1,1,1,1,1,1,1,1,1,2,1,1},
                    new int[] {0,1,1,2,1,1,1,1,1,1,1,2,1,1,0},
                    new int[] {0,0,1,1,2,2,2,2,2,2,2,1,1,0,0},
                    new int[] {0,0,0,1,1,1,1,1,1,1,1,1,0,0,0},
                    new int[] {0,0,0,0,1,1,3,3,3,1,1,0,0,0,0},
            },
            new int[][] {
                    new int[] {0,0,0,0,1,1,3,3,3,1,1,0,0,0,0},
                    new int[] {0,0,0,1,2,2,2,2,2,2,2,1,0,0,0},
                    new int[] {0,0,1,2,2,2,2,2,2,2,2,2,1,0,0},
                    new int[] {0,1,2,2,2,2,2,2,2,2,2,2,2,1,0},
                    new int[] {1,2,2,2,1,1,1,1,1,1,1,2,2,2,1},
                    new int[] {1,2,2,2,1,3,3,3,3,3,1,2,2,2,1},
                    new int[] {3,2,2,2,1,3,3,3,3,3,1,2,2,2,3},
                    new int[] {3,2,2,2,1,3,3,3,3,3,1,2,2,2,3},
                    new int[] {3,2,2,2,1,3,3,3,3,3,1,2,2,2,3},
                    new int[] {1,2,2,2,1,3,3,3,3,3,1,2,2,2,1},
                    new int[] {1,2,2,2,1,1,1,1,1,1,1,2,2,2,1},
                    new int[] {0,1,2,2,2,2,2,2,2,2,2,2,2,1,0},
                    new int[] {0,0,1,2,2,2,2,2,2,2,2,2,1,0,0},
                    new int[] {0,0,0,1,2,2,2,2,2,2,2,1,0,0,0},
                    new int[] {0,0,0,0,1,1,3,3,3,1,1,0,0,0,0},
            }
    };

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        if(meta >= 12) return new TileEntityFusionTorus();
        if(meta >= 6) return new TileEntityProxyCombo().inventory().power().fluid();

        return null;
    }

    @Override
    public int[] getDimensions() {
        return new int[] { 4, 0, 7, 7, 7, 7 };
    }

    @Override
    public int getOffset() {
        return 7;
    }

    @Override
    public boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {
        if (!super.checkRequirement(world, x, y, z, dir, o)) return false;

        x += dir.offsetX * o;
        z += dir.offsetZ * o;

        for (int iy = 0; iy < 5; iy++) {
            int l = iy > 2 ? 4 - iy : iy;

            int size;
            switch (l) {
                case 0: size = 15; break;
                case 1: size = 13; break;
                default: size = 11; break;
            }

            int half = size / 2;

            if (!MultiblockHandlerXR.checkSpace(world,
                    x - half, y + iy, z - half,
                    new int[]{0, 0, 0, size, 1, size},
                    x, y, z, dir))
                return false;
        }
        return true;
    }

    @Override
    public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
        super.fillSpace(world, x, y, z, dir, o);

        x += dir.offsetX * o;
        z += dir.offsetZ * o;

        for (int iy = 0; iy < 5; iy++) {
            int l = iy > 2 ? 4 - iy : iy;

            switch (l) {
                case 0:
                    MultiblockHandlerXR.fillSpace(world, x, y + iy, z, new int[]{-7, 0, -7, 15, 1, 15}, this, dir);
                    break;

                case 1:
                    MultiblockHandlerXR.fillSpace(world, x, y + iy, z, new int[]{-6, 0, -6, 13, 1, 13}, this, dir);
                    break;

                case 2:
                    MultiblockHandlerXR.fillSpace(world, x, y + iy, z, new int[]{-6, 0, -6, 13, 1, 13}, this, dir);
                    break;
            }
        }

        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
        this.makeExtra(world, x + dir.offsetX * 2 + rot.offsetX * 6, y + 3, z + dir.offsetZ * 2 + rot.offsetZ * 6);
        this.makeExtra(world, x + dir.offsetX * 2 - rot.offsetX * 6, y + 3, z + dir.offsetZ * 2 - rot.offsetZ * 6);
        this.makeExtra(world, x - dir.offsetX * 2 + rot.offsetX * 6, y + 3, z - dir.offsetZ * 2 + rot.offsetZ * 6);
        this.makeExtra(world, x - dir.offsetX * 2 - rot.offsetX * 6, y + 3, z - dir.offsetZ * 2 - rot.offsetZ * 6);
    }


}

