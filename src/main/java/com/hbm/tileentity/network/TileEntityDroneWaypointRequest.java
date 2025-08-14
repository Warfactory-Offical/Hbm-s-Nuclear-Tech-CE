package com.hbm.tileentity.network;

import com.hbm.interfaces.AutoRegister;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;

@AutoRegister
public class TileEntityDroneWaypointRequest extends TileEntityRequestNetwork {
    public static final int height = 2;

    @Override
    public void update() {
        super.update();

        if (world.isRemote) {
            if(world.getTotalWorldTime() % 2 == 0) {
                world.spawnParticle(EnumParticleTypes.REDSTONE, pos.getX() + 0.5, pos.getY() + height + 0.5, pos.getZ() + 0.5, 0, 0, 0);
            }
        }
    }

    @Override
    public BlockPos getCoord() {
        return new BlockPos(pos.getX() + 0.5, pos.getY() + height + 0.5, pos.getZ() + 0.5);
    }

    @Override
    public RequestNetwork.PathNode createNode(BlockPos pos) {
        return new RequestNetwork.PathNode(pos, this.reachableNodes);
    }
}
