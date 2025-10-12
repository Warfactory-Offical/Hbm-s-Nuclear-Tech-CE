package com.hbm.inventory.fluid.tank;

import com.hbm.capability.NTMFluidCapabilityHandler;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class FluidLoaderStandard implements IFluidLoadingHandler {

	@Override
	public boolean fillItem(IItemHandler slots, int in, int out, FluidTankNTM tank) {
		if(tank.pressure != 0) return false;
		ItemStack inputStack = slots.getStackInSlot(in);
		ItemStack outputStack = slots.getStackInSlot(out);
		if(inputStack == ItemStack.EMPTY) return true;
        if (!NTMFluidCapabilityHandler.isNtmFluidContainer(inputStack.getItem())) return false;
		FluidType type = tank.getTankType();
		ItemStack full = FluidContainerRegistry.getFullContainer(inputStack, type);
		
		if(full != null && tank.getFill() - FluidContainerRegistry.getFluidContent(full, type) >= 0) {
			
			if(outputStack == ItemStack.EMPTY) {
				
				tank.setFill(tank.getFill() - FluidContainerRegistry.getFluidContent(full, type));
				slots.insertItem(out, full.copy(), false);
				inputStack.setCount(inputStack.getCount() - 1);
				if(inputStack.getCount() <= 0) {
					slots.insertItem(in, ItemStack.EMPTY, false);
				}
				
			} else if(outputStack.getItem() == full.getItem() && outputStack.getItemDamage() == full.getItemDamage() && outputStack.getCount() < outputStack.getMaxStackSize()) {
				
				tank.setFill(tank.getFill() - FluidContainerRegistry.getFluidContent(full, type));
				inputStack.shrink(1);
				
				if(inputStack.getCount() <= 0) {
					slots.insertItem(in, ItemStack.EMPTY, false);
				}
				outputStack.grow(1);
			}
		}
		
		return false;
	}

	@Override
	public boolean emptyItem(IItemHandler slots, int in, int out, FluidTankNTM tank) {
		ItemStack inputStack = slots.getStackInSlot(in);
		if (inputStack.isEmpty()) return true;
		FluidType itemFluidType = FluidContainerRegistry.getFluidType(inputStack);
		if (itemFluidType == Fluids.NONE) return false;
		int amount = FluidContainerRegistry.getFluidContent(inputStack, itemFluidType);
		if (amount <= 0) return false;
		boolean canAccept = (tank.getTankType() == Fluids.NONE) || (tank.getTankType() == itemFluidType);
		if (!canAccept) return false;
		if (tank.getFill() + amount > tank.getMaxFill()) return false;

		ItemStack emptyContainer = FluidContainerRegistry.getEmptyContainer(inputStack);
		ItemStack outputStack = slots.getStackInSlot(out);

		boolean canOutput = false;
		if (emptyContainer != null) {
			if (outputStack.isEmpty()) {
				canOutput = true;
			} else if (ItemStack.areItemsEqual(outputStack, emptyContainer) && ItemStack.areItemStackTagsEqual(outputStack, emptyContainer)) {
				if (outputStack.getCount() < outputStack.getMaxStackSize()) {
					canOutput = true;
				}
			}
		} else canOutput = true;

		if (!canOutput) return false;
		if (tank.getTankType() == Fluids.NONE) tank.setTankType(itemFluidType);
		tank.setFill(tank.getFill() + amount);
		slots.extractItem(in, 1, false);
		if (emptyContainer != null) {
			if (outputStack.isEmpty()) {
				slots.insertItem(out, emptyContainer.copy(), false);
			} else {
				outputStack.grow(1);
			}
		}
		return true;
	}

}
