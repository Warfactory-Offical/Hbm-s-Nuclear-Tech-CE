package com.hbm.tileentity.network;

import net.minecraft.util.math.BlockPos;

public interface IDroneLinkable {
    BlockPos getPoint();
    void setNextTarget(int x, int y, int z);
}
