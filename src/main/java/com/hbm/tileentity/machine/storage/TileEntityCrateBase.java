package com.hbm.tileentity.machine.storage;

import com.hbm.api.tile.IWorldRenameable;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.ItemStackHandlerWrapper;
import com.hbm.tileentity.machine.TileEntityLockableBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.IWorldNameable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public abstract class TileEntityCrateBase extends TileEntityLockableBase implements IWorldRenameable {

	public ItemStackHandler inventory;
	public String customName;

	public TileEntityCrateBase(int scount) {
		this(scount, 64);
	}

	public TileEntityCrateBase(int scount, int slotlimit) {
		inventory = getNewInventory(scount, slotlimit);
	}

	public ItemStackHandler getNewInventory(int scount, int slotlimit){
		return new ItemStackHandler(scount){
			@Override
			protected void onContentsChanged(int slot) {
				super.onContentsChanged(slot);
				markDirty();
			}

			@Override
			public int getSlotLimit(int slot) {
				return slotlimit;
			}
		};
	}

	public ItemStack getStackInSlotOnClosing(int i) {
		if (!inventory.getStackInSlot(i).isEmpty()) {
			ItemStack itemStack = inventory.getStackInSlot(i);
			inventory.setStackInSlot(i, ItemStack.EMPTY);
			return itemStack;
		} else {
			return null;
		}
	}

    @Override
	public boolean hasCustomName() {
		return this.customName != null && !this.customName.isEmpty();
	}

    @Override
	public void setCustomName(String name) {
		this.customName = name;
	}

	public boolean isUseableByPlayer(EntityPlayer player) {
		if (world.getTileEntity(pos) != this) {
			return false;
		} else {
			return player.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) <= 64;
		}
	}


	public void openInventory(EntityPlayer player) {
		player.world.playSound(player.posX, player.posY, player.posZ, HBMSoundHandler.crateOpen, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
	}


	public void closeInventory(EntityPlayer player) {
		player.world.playSound(player.posX, player.posY, player.posZ, HBMSoundHandler.crateClose, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
	}


	public boolean isItemValidForSlot(int i, ItemStack stack) {
		return true;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		if(compound.hasKey("inventory"))
			inventory.deserializeNBT(compound.getCompoundTag("inventory"));
		super.readFromNBT(compound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("inventory", inventory.serializeNBT());
		return super.writeToNBT(compound);
	}


	public int[] getAccessibleSlotsFromSide(EnumFacing e) {
		int[] slots = new int[this.inventory.getSlots()];
		for(int i = 0; i < slots.length; i++) slots[i] = i;
		return slots;
	}

	public boolean canInsertItem(int slot, ItemStack itemStack, int amount) {
		return this.isItemValidForSlot(slot, itemStack);
	}

	public boolean canExtractItem(int slot, ItemStack itemStack, int amount) {
		return true;
	}


	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && inventory != null){
			if(facing == null)
				return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new ItemStackHandlerWrapper(inventory, getAccessibleSlotsFromSide(facing)){
				@Override
				public ItemStack extractItem(int slot, int amount, boolean simulate) {
					if(canExtractItem(slot, inventory.getStackInSlot(slot), amount))
						return super.extractItem(slot, amount, simulate);
					return ItemStack.EMPTY;
				}

				@Override
				public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
					if(canInsertItem(slot, stack, stack.getCount()))
						return super.insertItem(slot, stack, simulate);
					return stack;
				}
			});
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && inventory != null) || super.hasCapability(capability, facing);
	}
}
