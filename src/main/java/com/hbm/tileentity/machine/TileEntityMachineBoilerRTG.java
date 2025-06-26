package com.hbm.tileentity.machine;

import api.hbm.fluid.IFluidStandardTransceiver;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineBoiler;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.forgefluid.FFUtils;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.interfaces.IFFtoNTMF;
import com.hbm.interfaces.ITankPacketAcceptor;
import com.hbm.inventory.MachineRecipes;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.machine.ItemRTGPellet;
import com.hbm.packet.AuxElectricityPacket;
import com.hbm.packet.AuxGaugePacket;
import com.hbm.packet.FluidTankPacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.RTGUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

import javax.annotation.Nullable;

public class TileEntityMachineBoilerRTG extends TileEntityMachineBase implements ITickable, IFluidStandardTransceiver, ITankPacketAcceptor, IFFtoNTMF {

	public int rtgPower;
	public int heat = 2000;
	public static final int maxRTGPower = 6000;
	public static final int maxHeat = 100000;
	public FluidTankNTM[] tanksNew;
	public FluidTank[] tanks;

	private static final int[] slots_top = new int[] {4};
	private static final int[] slots_bottom = new int[] {7};
	private static final int[] slots_side = new int[] {4};

	private static boolean converted = false;

	public TileEntityMachineBoilerRTG() {
		super(8);
		tanksNew = new FluidTankNTM[2];
		tanksNew[0] = new FluidTankNTM(Fluids.OIL, 16000, 0);
		tanksNew[1] = new FluidTankNTM(Fluids.HOTOIL, 16000, 1);

		tanks = new FluidTank[2];
		tanks[0] = new FluidTank(16000);
		tanks[1] = new FluidTank(16000);

		converted = true;
	}
	
	@Override
	public String getName(){
		return "container.machineRTGBoiler";
	}

	public boolean isUseableByPlayer(EntityPlayer player) {
		if(world.getTileEntity(pos) != this) {
			return false;
		} else {
			return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64;
		}
	}

	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing e){
		int i = e.ordinal();
		return i == 0 ? slots_bottom : (i == 1 ? slots_top : slots_side);
	}
	
	@Override
	public boolean canInsertItem(int slot, ItemStack itemStack, int amount){
		return this.isItemValidForSlot(slot, itemStack);
	}
	
	@Override
	public boolean canExtractItem(int slot, ItemStack itemStack, int amount){
		return false;
	}
	
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		if(i == 4 || i == 5){
			return (stack != null && stack.getItem() instanceof ItemRTGPellet);
		}
		if(i == 2 || i == 6){
			return (stack != null && !(stack.getItem() instanceof ItemRTGPellet));
		}
		return false;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		heat = nbt.getInteger("heat");
		rtgPower = nbt.getInteger("rtgPower");
		if (!converted) {
			if (nbt.hasKey("tanks")) FFUtils.deserializeTankArray(nbt.getTagList("tanks", 10), tanks);
		} else{
			tanksNew[0].readFromNBT(nbt, "water");
			tanksNew[1].readFromNBT(nbt, "steam");
			if (nbt.hasKey("tanks")) nbt.removeTag("tanks");
		}
		super.readFromNBT(nbt);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("heat", heat);
		nbt.setInteger("rtgPower", rtgPower);
		if(!converted){
			nbt.setTag("tanks", FFUtils.serializeTankArray(tanks));
		} else {
			tanksNew[0].writeToNBT(nbt, "water");
			tanksNew[1].writeToNBT(nbt, "steam");
		}
		return super.writeToNBT(nbt);
	}

	public int getHeatScaled(int i) {
		return (heat * i) / maxHeat;
	}

	public long getRTGPowerScaled(int i) {
		return (rtgPower * i) / maxRTGPower;
	}

	@Override
	public void update() {
		boolean mark = false;
		if(!world.isRemote) {
			this.subscribeToAllAround(tanksNew[0].getTankType(), this);
			this.sendFluidToAll(tanksNew[1], this);

			tanksNew[0].setType(0, 1, inventory);
			tanksNew[0].loadTank(2, 3, inventory);

			Object[] outs = MachineRecipes.getBoilerOutput(tanksNew[0].getTankType());

			if(!converted) {
				PacketDispatcher.wrapper.sendToAllAround(new FluidTankPacket(pos.getX(), pos.getY(), pos.getZ(), new FluidTank[]{tanks[0], tanks[1]}), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 10));
				convertAndSetFluids(getFluids(tanks), tanks, tanksNew);
				converted = true;
			}

			if(outs == null) {
				tanksNew[1].setTankType(Fluids.NONE);
			} else {
				tanksNew[1].setTankType((FluidType) outs[0]);
			}
			tanksNew[1].unloadTank(5, 6, inventory);

			for(int i = 0; i < 2; i++)
				tanksNew[i].updateTank(pos.getX(), pos.getY(), pos.getZ(), world.provider.getDimension());

			

			if(heat > 2000) {
				heat -= 40;
			}
			rtgPower = Math.min(RTGUtil.updateRTGs(inventory, new int[] {4, 5}), maxRTGPower);
			if(rtgPower > 0) {
				heat += rtgPower*5;
			} else {
				heat -= 100;
			}

			if(rtgPower <= 0 && world.getBlockState(pos).getBlock() == ModBlocks.machine_boiler_rtg_on) {
				rtgPower = 0;
				MachineBoiler.updateBlockState(false, world, pos);
				mark = true;
			}

			if(heat > maxHeat)
				heat = maxHeat;

			if(rtgPower > 0 && world.getBlockState(pos).getBlock() == ModBlocks.machine_boiler_rtg_off) {
				MachineBoiler.updateBlockState(true, world, pos);
				mark = true;
			}

			if(outs != null) {
				for(int i = 0; i < (heat / ((Integer)outs[3]).intValue()); i++) {
					if(tanksNew[0].getFill() >= ((Integer)outs[2]).intValue() && tanksNew[1].getFill() + ((Integer)outs[1]).intValue() <= tanksNew[1].getMaxFill()) {
						tanksNew[0].setFill(tanksNew[0].getFill() - ((Integer)outs[2]).intValue());
						tanksNew[1].setFill(tanksNew[1].getFill() + ((Integer)outs[1]).intValue());

						if(i == 0)
							heat -= 45;
						else
							heat -= 60;
					}
				}
			}

			if(heat < 2000) {
				heat = 2000;
			}
			PacketDispatcher.wrapper.sendToAllAround(new AuxElectricityPacket(pos.getX(), pos.getY(), pos.getZ(), rtgPower), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 50));
			PacketDispatcher.wrapper.sendToAllAround(new AuxGaugePacket(pos.getX(), pos.getY(), pos.getZ(), heat, 0), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 50));
		}

		if(mark) {
			this.markDirty();
		}
	}

	@Override
	public void recievePacket(NBTTagCompound[] tags) {
		if(tags.length != 2) {
			return;
		} else {
			tanks[0].readFromNBT(tags[0]);
			tanks[1].readFromNBT(tags[1]);
		}
	}

	public Fluid[] getFluids(FluidTank[] tanks){
		Fluid fluid1;
		Fluid fluid2;
		if(tanks[0].getFluid() != null) {
			fluid1 = tanks[0].getFluid().getFluid();
		} else {
			fluid1 = ModForgeFluids.none;
		}
		if(tanks[1].getFluid() != null) {
			fluid2 = tanks[1].getFluid().getFluid();
		} else {
			fluid2 = ModForgeFluids.none;
		}
		return new Fluid[]{fluid1, fluid2};
	}

	@Override
	public FluidTankNTM[] getSendingTanks() {
		return new FluidTankNTM[] {tanksNew[1]};
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return new FluidTankNTM[] {tanksNew[0]};
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return tanksNew;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(
					new NTMFluidHandlerWrapper(this.getReceivingTanks(), this.getSendingTanks())
			);
		}
		return super.getCapability(capability, facing);
	}
}
