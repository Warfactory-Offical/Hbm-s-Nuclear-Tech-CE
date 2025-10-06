package com.hbm.lib;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class ItemStackHandlerWrapper implements IItemHandlerModifiable {

	protected ItemStackHandler handle;
	private int[] validSlots;
	
	public ItemStackHandlerWrapper(ItemStackHandler handle) {
		this.handle = handle;
		validSlots = new int[]{};
	}
	
	public ItemStackHandlerWrapper(ItemStackHandler handle, int[] validSlots) {
		this.handle = handle;
		this.validSlots = validSlots;
	}
	
	@Override
	public int getSlots() {
		return handle.getSlots();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return handle.getStackInSlot(slot);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		for(int i : validSlots)
			if(i == slot)
				return handle.insertItem(slot, stack, simulate);
		return stack;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		for(int i : validSlots)
			if(i == slot)
				return handle.extractItem(slot, amount, simulate);
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot) {
		return handle.getSlotLimit(slot);
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		handle.setStackInSlot(slot, stack);
	}

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        for(int i : validSlots)
            if(i == slot)
                return handle.isItemValid(slot, stack);
        return false;
    }
}
