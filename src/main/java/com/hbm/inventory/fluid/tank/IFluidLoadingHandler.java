package com.hbm.inventory.fluid.tank;

import net.minecraftforge.items.IItemHandler;

public interface IFluidLoadingHandler {

	boolean fillItem(IItemHandler slots, int in, int out, FluidTankNTM tank);
	boolean emptyItem(IItemHandler slots, int in, int out, FluidTankNTM tank);
}
