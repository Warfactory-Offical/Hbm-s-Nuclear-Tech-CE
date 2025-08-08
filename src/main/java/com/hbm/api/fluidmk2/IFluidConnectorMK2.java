package com.hbm.api.fluidmk2;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.lib.ForgeDirection;

public interface IFluidConnectorMK2 {
    /**
     * Whether the given side can be connected to
     */
    default boolean canConnect(FluidType type, ForgeDirection dir) {
        return dir != ForgeDirection.UNKNOWN;
    }
}
