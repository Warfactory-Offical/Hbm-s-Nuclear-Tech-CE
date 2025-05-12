package com.hbm.inventory.fluid.tank;

import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.fluid.FluidType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class FluidLoaderStandard extends FluidLoadingHandler {

	@Override
	public boolean fillItem(IItemHandler slots, int in, int out, FluidTankNTM tank) {
		
		if(tank.pressure != 0) return false;
		
		if(slots.getStackInSlot(in) == ItemStack.EMPTY)
			return true;
		
		FluidType type = tank.getTankType();
		ItemStack full = FluidContainerRegistry.getFullContainer(slots.getStackInSlot(in), type);
		
		if(full != null && slots.getStackInSlot(in) != ItemStack.EMPTY && tank.getFill() - FluidContainerRegistry.getFluidContent(full, type) >= 0) {
			
			if(slots.getStackInSlot(out) == ItemStack.EMPTY) {
				
				tank.setFill(tank.getFill() - FluidContainerRegistry.getFluidContent(full, type));
				slots.insertItem(out, full.copy(), false);
				slots.getStackInSlot(in).setCount(slots.getStackInSlot(in).getCount() - 1);
				if(slots.getStackInSlot(in).getCount() <= 0) {
					slots.insertItem(in, ItemStack.EMPTY, false);
				}
				
			} else if(slots.getStackInSlot(out) != ItemStack.EMPTY &&
					slots.getStackInSlot(out).getItem() == full.getItem() &&
					slots.getStackInSlot(out).getItemDamage() == full.getItemDamage() &&
					slots.getStackInSlot(out).getCount() < slots.getStackInSlot(out).getMaxStackSize()) {
				
				tank.setFill(tank.getFill() - FluidContainerRegistry.getFluidContent(full, type));
				slots.getStackInSlot(in).shrink(1);
				
				if(slots.getStackInSlot(in).getCount() <= 0) {
					slots.insertItem(in, ItemStack.EMPTY, false);
				}
				slots.getStackInSlot(out).grow(1);
			}
		}
		
		return false;
	}

	@Override
	public boolean emptyItem(IItemHandler slots, int in, int out, FluidTankNTM tank) {
		
		if(slots.getStackInSlot(in) == ItemStack.EMPTY)
			return true;
		
		FluidType type = tank.getTankType();
		int amount = FluidContainerRegistry.getFluidContent(slots.getStackInSlot(in), type);
		
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
