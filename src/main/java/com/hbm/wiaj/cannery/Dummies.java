package com.hbm.wiaj.cannery;

import com.hbm.api.energymk2.IEnergyConnectorMK2;
import com.hbm.api.fluidmk2.IFluidConnectorMK2;
import net.minecraft.tileentity.TileEntity;

public class Dummies {

	public static class JarDummyConnector extends TileEntity implements IEnergyConnectorMK2, IFluidConnectorMK2 {}
}
