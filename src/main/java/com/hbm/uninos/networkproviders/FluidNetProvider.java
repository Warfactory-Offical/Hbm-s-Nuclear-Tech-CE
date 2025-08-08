package com.hbm.uninos.networkproviders;

import com.hbm.api.fluidmk2.FluidNetMK2;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.uninos.INetworkProvider;

public class FluidNetProvider implements INetworkProvider<FluidNetMK2> {

    protected FluidType type;

    public FluidNetProvider(FluidType type) {
        this.type = type;
    }

    @Override
    public FluidNetMK2 provideNetwork() {
        return new FluidNetMK2(type);
    }
}
