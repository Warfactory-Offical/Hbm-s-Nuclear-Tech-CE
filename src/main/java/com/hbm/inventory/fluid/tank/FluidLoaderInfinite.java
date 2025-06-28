package com.hbm.inventory.fluid.tank;

import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.tool.ItemFluidContainerInfinite;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.Random;

public class FluidLoaderInfinite implements IFluidLoadingHandler {
	
	private static Random rand = new Random();

	@Override
	public boolean fillItem(IItemHandler slots, int in, int out, FluidTankNTM tank) {
		
		if(slots.getStackInSlot(in) == ItemStack.EMPTY || !(slots.getStackInSlot(in).getItem() instanceof ItemFluidContainerInfinite)) return false;

		ItemFluidContainerInfinite item = (ItemFluidContainerInfinite) slots.getStackInSlot(in).getItem();
		
		if(!item.allowPressure(tank.pressure)) return false;
		if(item.getType() != null && tank.type != item.getType()) return false;
		
		if(item.getChance() <= 1 || rand.nextInt(item.getChance()) == 0) {
			tank.setFill(Math.max(tank.getFill() - item.getAmount(), 0));
		}
		
		return true;
	}

	@Override
	public boolean emptyItem(IItemHandler slots, int in, int out, FluidTankNTM tank) {
		
		if(slots.getStackInSlot(in) == ItemStack.EMPTY || !(slots.getStackInSlot(in).getItem() instanceof ItemFluidContainerInfinite) || tank.getTankType() == Fluids.NONE) return false;

		ItemFluidContainerInfinite item = (ItemFluidContainerInfinite) slots.getStackInSlot(in).getItem();
		
		if(item.getType() != null && tank.type != item.getType()) return false;
		
		if(item.getChance() <= 1 || rand.nextInt(item.getChance()) == 0) {
			tank.setFill(Math.min(tank.getFill() + item.getAmount(), tank.getMaxFill()));
		}
		
		return true;
	}
}
