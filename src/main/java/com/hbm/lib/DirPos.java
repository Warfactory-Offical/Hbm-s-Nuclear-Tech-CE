package com.hbm.lib;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class DirPos extends Object {
    
    protected ForgeDirection dir;
    protected BlockPos pos;
    

    public DirPos(int x, int y, int z, ForgeDirection dir) {
        this.pos = new BlockPos(x, y, z);
        this.dir = dir;
    }

    public DirPos(BlockPos pos, ForgeDirection dir) {
        this.pos = pos;
        this.dir = dir;
    }

    public DirPos(TileEntity te, ForgeDirection dir) {
        this.pos = te.getPos();
        this.dir = dir;
    }

    public DirPos(double x, double y, double z, ForgeDirection dir) {
        this.pos = new BlockPos(x, y, z);
        this.dir = dir;
    }
    
    public ForgeDirection getDir() {
        return this.dir;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public boolean compare(int x, int y, int z) {
        return this.pos.getX() == x && this.pos.getY() == y && this.pos.getZ() == z;
    }
}
