package com.hbm.api.fluid;

import com.hbm.api.fluidmk2.IFluidStandardSenderMK2;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.ForgeDirection;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Deprecated
public interface IFluidStandardSender extends IFluidStandardSenderMK2 {

    FluidTankNTM[] getSendingTanks();

    default void sendFluid(FluidTankNTM tank, World world, int x, int y, int z, ForgeDirection dir) {
        tryProvide(tank, world, x, y, z, dir);
    }

    default void sendFluid(FluidTankNTM tank, World world, BlockPos pos, ForgeDirection dir) {
        sendFluid(tank, world, pos.getX(), pos.getY(), pos.getZ(), dir);
    }

    default void sendFluidToAll(FluidTankNTM tank, TileEntity tile) {
        for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            tryProvide(tank, tile.getWorld(), tile.getPos().getX() + dir.offsetX, tile.getPos().getY() + dir.offsetY, tile.getPos().getZ() + dir.offsetZ, dir);
        }
    }
}
