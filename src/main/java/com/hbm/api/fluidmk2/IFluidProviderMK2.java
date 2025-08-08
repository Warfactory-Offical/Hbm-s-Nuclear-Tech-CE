package com.hbm.api.fluidmk2;

import com.hbm.inventory.fluid.FluidType;

public interface IFluidProviderMK2 extends IFluidUserMK2 {

    void useUpFluid(FluidType type, int pressure, long amount);
    default long getProviderSpeed(FluidType type, int pressure) { return 1_000_000_000; }
    long getFluidAvailable(FluidType type, int pressure);

    default int[] getProvidingPressureRange(FluidType type) { return DEFAULT_PRESSURE_RANGE; }
}
