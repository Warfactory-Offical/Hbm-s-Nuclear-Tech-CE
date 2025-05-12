package com.hbm.capability;

import com.hbm.inventory.fluid.tank.FluidTankNTM;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class NTMFluidHandlerWrapper implements IFluidHandler {
    protected final IFluidHandler delegate;
    public NTMFluidHandlerWrapper(FluidTankNTM tank) {
        delegate = tank;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return delegate.getTankProperties();
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        return delegate.fill(resource, doFill);
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        return delegate.drain(resource, doDrain);
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return delegate.drain(maxDrain, doDrain);
    }
}
