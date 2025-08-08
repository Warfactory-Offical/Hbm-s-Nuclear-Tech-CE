package com.hbm.api.fluidmk2;

import com.hbm.api.tile.ILoadedTile;
import com.hbm.inventory.fluid.tank.FluidTankNTM;

public interface IFluidUserMK2 extends IFluidConnectorMK2, ILoadedTile {

    int HIGHEST_VALID_PRESSURE = 5;
    int[] DEFAULT_PRESSURE_RANGE = new int[] {0, 0};

    boolean particleDebug = false;

    FluidTankNTM[] getAllTanks();
}
