package com.hbm.inventory.fluid.tank;

import api.hbm.fluid.IFillableItem;
import com.hbm.handler.ArmorModHandler;
import com.hbm.inventory.fluid.FluidType;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class FluidLoaderFillableItem implements IFluidLoadingHandler {

	@Override
	public boolean fillItem(IItemHandler slots, int in, int out, FluidTankNTM tank) {
		return fill(slots.getStackInSlot(in), tank);
	}
	
	public boolean fill(ItemStack stack, FluidTankNTM tank) {
		
		if(tank.pressure != 0) return false;
		
		if(stack == null)
			return false;
		
		FluidType type = tank.getTankType();
		
		if(stack.getItem() instanceof ItemArmor && ArmorModHandler.hasMods(stack)) {
			for(ItemStack mod : ArmorModHandler.pryMods(stack)) {
				
				if(mod != null && mod.getItem() instanceof IFillableItem) {
					fill(mod, tank);
				}
			}
		}
		
		if(!(stack.getItem() instanceof IFillableItem)) return false;
		
		IFillableItem fillable = (IFillableItem) stack.getItem();
		
		if(fillable.acceptsFluid(type, stack)) {
			tank.setFill(fillable.tryFill(type, tank.getFill(), stack));
		}
		
		return true;
	}

	@Override
	public boolean emptyItem(IItemHandler slots, int in, int out, FluidTankNTM tank) {
		return empty(slots.getStackInSlot(in), tank);
	}
	
	public boolean empty(ItemStack stack, FluidTankNTM tank) {

		FluidType type = tank.getTankType();
		
		if(stack.getItem() instanceof ItemArmor && ArmorModHandler.hasMods(stack)) {
			for(ItemStack mod : ArmorModHandler.pryMods(stack)) {
				
				if(mod != null && mod.getItem() instanceof IFillableItem) {
					empty(mod, tank);
				}
			}
		}
		
		if(!(stack.getItem() instanceof IFillableItem)) return false;
		
		IFillableItem fillable = (IFillableItem) stack.getItem();
		
		if(fillable.providesFluid(type, stack)) {
			tank.setFill(tank.getFill() + fillable.tryEmpty(type, tank.getMaxFill() - tank.getFill(), stack));
		}
		
		return tank.getFill() == tank.getMaxFill();
	}
}
