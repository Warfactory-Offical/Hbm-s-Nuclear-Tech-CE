package com.hbm.inventory.fluid.tank;

import com.hbm.capability.NTMFluidCapabilityHandler;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class FluidLoaderStandard extends FluidLoadingHandler {

	@Override
	public boolean fillItem(IItemHandler slots, int in, int out, FluidTankNTM tank) {
		if(tank.pressure != 0) return false;
		ItemStack inputStack = slots.getStackInSlot(in);
		ItemStack outputStack = slots.getStackInSlot(out);
		if(inputStack == ItemStack.EMPTY) return true;
		if (!NTMFluidCapabilityHandler.isHbmFluidContainer(inputStack.getItem())) return false;
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
		
		if(slots.getStackInSlot(in) == ItemStack.EMPTY)
			return true;

		FluidType tankType = tank.getTankType();
		FluidType itemTankType = FluidContainerRegistry.getFluidType(slots.getStackInSlot(in));
		if(tankType == Fluids.NONE && itemTankType != Fluids.NONE)
			tank.setTankType(itemTankType);

		int amount = FluidContainerRegistry.getFluidContent(slots.getStackInSlot(in), tankType);

		if(amount > 0 && tank.getFill() + amount <= tank.maxFluid) {
			
			ItemStack emptyContainer = FluidContainerRegistry.getEmptyContainer(slots.getStackInSlot(in));
			
			if(slots.getStackInSlot(out) == ItemStack.EMPTY) {
				
				tank.setFill(tank.getFill() + amount);
				slots.insertItem(out, emptyContainer, false);

				slots.getStackInSlot(in).setCount(slots.getStackInSlot(in).getCount() - 1);
				if(slots.getStackInSlot(in).getCount() <= 0) {
					slots.insertItem(in, ItemStack.EMPTY, false);
				}
				
			} else if(slots.getStackInSlot(out) != ItemStack.EMPTY && (emptyContainer == null || (slots.getStackInSlot(out).getItem() == emptyContainer.getItem() && slots.getStackInSlot(out).getItemDamage() == emptyContainer.getItemDamage() && slots.getStackInSlot(out).getCount() < slots.getStackInSlot(out).getMaxStackSize()))) {
				
				tank.setFill(tank.getFill() + amount);
				slots.getStackInSlot(in).setCount(slots.getStackInSlot(in).getCount() - 1);
				
				if(slots.getStackInSlot(in).getCount() <= 0) {
					slots.insertItem(in, ItemStack.EMPTY, false);
				}
				
				if(emptyContainer != null) {
					slots.getStackInSlot(out).setCount(slots.getStackInSlot(out).getCount() + 1);
				}
			}
			
			return true;
		}
		
		return false;
	}

}
