package com.hbm.tileentity;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluidmk2.IFluidConnectorMK2;
import com.hbm.api.fluidmk2.IFluidReceiverMK2;
import com.hbm.api.tile.IHeatSource;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.CapabilityContextProvider;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.MainRegistry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AutoRegister
public class TileEntityProxyCombo extends TileEntityProxyBase implements IEnergyReceiverMK2, IHeatSource, IFluidReceiverMK2, IFluidHandler {

	TileEntity tile;
	boolean inventory;
	boolean power;
	boolean fluid;
	public boolean moltenMetal;

	boolean heat;

	public TileEntityProxyCombo() {
	}

	public TileEntityProxyCombo(boolean inventory, boolean power, boolean fluid) {
		this.inventory = inventory;
		this.power = power;
		this.fluid = fluid;
		this.heat = false;
	}

	public TileEntityProxyCombo(boolean inventory, boolean power, boolean fluid, boolean heat) {
		this.inventory = inventory;
		this.power = power;
		this.fluid = fluid;
		this.heat = heat;
	}


	public TileEntityProxyCombo inventory() {
		this.inventory = true;
		return this;
	}

	public TileEntityProxyCombo power() {
		this.power = true;
		return this;
	}
	public TileEntityProxyCombo moltenMetal() {
		this.moltenMetal = true;
		return this;
	}
	public TileEntityProxyCombo fluid() {
		this.fluid = true;
		return this;
	}

	public TileEntityProxyCombo heatSource() {
		this.heat = true;
		return this;
	}

	/** Returns the actual tile entity that represents the core. Only for internal use, and EnergyControl. */
	public TileEntity getTile() {
		if(tile == null || tile.isInvalid() || (tile instanceof TileEntityLoadedBase && !((TileEntityLoadedBase) tile).isLoaded)) {
			tile = this.getTE();
		}
		return tile;
	}

	/** Returns the core tile entity, or a delegate object. */
	protected Object getCoreObject() {
		return getTile();
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if(tile == null) {
			tile = this.getTE();
			if(tile == null){
				return super.getCapability(capability, facing);
			}
		}
		return CapabilityContextProvider.runWithContext(this.pos, () -> {
			if(inventory && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
				return tile.getCapability(capability, facing);
			}
			if(power && capability == CapabilityEnergy.ENERGY){
				return tile.getCapability(capability, facing);
			}
			if(fluid && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
				return tile.getCapability(capability, facing);
			}
			return super.getCapability(capability, facing);
		});
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if(tile == null) {
			tile = this.getTE();
			if(tile == null){
				return super.hasCapability(capability, facing);
			}
		}
		return CapabilityContextProvider.runWithContext(this.pos, () -> {
			if(inventory && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
				return tile.hasCapability(capability, facing);
			}
			if(power && capability == CapabilityEnergy.ENERGY){
				return tile.hasCapability(capability, facing);
			}
			if(fluid && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
				return tile.hasCapability(capability, facing);
			}
			return super.hasCapability(capability, facing);
		});
	}

	@Override
	public void setPower(long i) {

		if(!power)
			return;

		if(getCoreObject() instanceof IEnergyReceiverMK2) {
			((IEnergyReceiverMK2)getCoreObject()).setPower(i);
		}
	}

	@Override
	public long getPower() {

		if(!power)
			return 0;

		if(getCoreObject() instanceof IEnergyReceiverMK2) {
			return ((IEnergyReceiverMK2)getCoreObject()).getPower();
		}

		return 0;
	}

	@Override
	public long getMaxPower() {

		if(!power)
			return 0;

		if(getCoreObject() instanceof IEnergyReceiverMK2) {
			return ((IEnergyReceiverMK2)getCoreObject()).getMaxPower();
		}

		return 0;
	}

	@Override
	public long transferPower(long power, boolean simulate) {

		if(!this.power)
			return power;

		if(getCoreObject() instanceof IEnergyReceiverMK2) {
			return ((IEnergyReceiverMK2)getCoreObject()).transferPower(power, simulate);
		}

		return power;
	}

	@Override
	public boolean canConnect(ForgeDirection dir) {

		if(!power)
			return false;

		if(getCoreObject() instanceof IEnergyReceiverMK2) {
			return ((IEnergyReceiverMK2)getCoreObject()).canConnect(dir);
		}

		return true;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		inventory = compound.getBoolean("inv");
		fluid = compound.getBoolean("flu");
		power = compound.getBoolean("pow");
		heat = compound.getBoolean("hea");

		super.readFromNBT(compound);
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setBoolean("inv", inventory);
		compound.setBoolean("flu", fluid);
		compound.setBoolean("pow", power);
		compound.setBoolean("hea", heat);
		return super.writeToNBT(compound);
	}

	public static final FluidTankNTM[] EMPTY_TANKS = new FluidTankNTM[0];

	@Override
	public FluidTankNTM[] getAllTanks() {
		if(!fluid) return EMPTY_TANKS;

		if(getCoreObject() instanceof IFluidReceiverMK2) {
			return ((IFluidReceiverMK2)getCoreObject()).getAllTanks();
		}

		return EMPTY_TANKS;
	}

	@Override
	public long transferFluid(FluidType type, int pressure, long amount) {
		if(!fluid) return amount;

		if(getCoreObject() instanceof IFluidReceiverMK2) {
			return ((IFluidReceiverMK2)getCoreObject()).transferFluid(type, pressure, amount);
		}

		return amount;
	}

	@Override
	public long getDemand(FluidType type, int pressure) {
		if(!fluid) return 0;

		if(getCoreObject() instanceof IFluidReceiverMK2) {
			return ((IFluidReceiverMK2)getCoreObject()).getDemand(type, pressure);
		}

		return 0;
	}

	@Override
	public boolean canConnect(FluidType type, ForgeDirection dir) {

		if(!this.fluid)
			return false;

		if(getCoreObject() instanceof IFluidConnectorMK2) {
			return ((IFluidConnectorMK2) getCoreObject()).canConnect(type, dir);
		}
		return true;
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
	}

	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		this.readFromNBT(tag);
	}

	@Override
	public int getHeatStored() {
		if (!this.heat) {
			return 0;
		}

		if (getCoreObject() instanceof IHeatSource) {
			return ((IHeatSource) getCoreObject()).getHeatStored();
		}
		return 0;
	}

	@Override
	public void useUpHeat(int heat) {
		if (!this.heat) {
			return;
		}

		if (getCoreObject() instanceof IHeatSource) {
			((IHeatSource) getCoreObject()).useUpHeat(heat);
		}
	}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		if(tile instanceof IFluidHandler){
			return ((IFluidHandler) tile).getTankProperties();
		}
			MainRegistry.logger.error("Tile Entity: {} doesn't support IFluidHandler. Very likely a bug!", tile.toString());
		return new IFluidTankProperties[0];
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		if(tile instanceof IFluidHandler){
			return ((IFluidHandler) tile).fill(resource, doFill);
		}
		MainRegistry.logger.error("Tile Entity: {} doesn't support IFluidHandler. Very likely a bug!", tile.toString());
		return 0;
	}

	@Override
	public @Nullable FluidStack drain(FluidStack resource, boolean doDrain) {
		if(tile instanceof IFluidHandler){
			return ((IFluidHandler) tile).drain(resource, doDrain);
		}
		MainRegistry.logger.error("Tile Entity: {} doesn't support IFluidHandler. Very likely a bug!", tile.toString());
		return null;
	}

	@Override
	public @Nullable FluidStack drain(int maxDrain, boolean doDrain) {

		if(tile instanceof IFluidHandler){
			return ((IFluidHandler) tile).drain(maxDrain, doDrain);
		}
		MainRegistry.logger.error("Tile Entity: {} doesn't support IFluidHandler. Very likely a bug!", tile.toString());
		return null;

	}
}
