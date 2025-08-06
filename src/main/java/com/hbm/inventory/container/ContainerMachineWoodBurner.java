package com.hbm.inventory.container;

import com.hbm.inventory.SlotTakeOnly;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.machine.TileEntityMachineWoodBurner;

import com.hbm.api.energymk2.IBatteryItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMachineWoodBurner extends Container {
	
	protected TileEntityMachineWoodBurner burner;
	
	public ContainerMachineWoodBurner(InventoryPlayer Playerinv, TileEntityMachineWoodBurner burner) {
		this.burner = burner;
		//this.burner.openInventory();

		//Fuel
		this.addSlotToContainer(new SlotItemHandler(burner.inventory, 0, 26, 18));
		//Ashes
		this.addSlotToContainer(new SlotTakeOnly(burner.inventory, 1, 26, 54));
		//Fluid ID
		this.addSlotToContainer(new SlotItemHandler(burner.inventory, 2, 98, 54));
		//Fluid Container
		this.addSlotToContainer(new SlotItemHandler(burner.inventory, 3, 98, 18));
		this.addSlotToContainer(new SlotTakeOnly(burner.inventory, 4, 98, 36));
		//Battery
		this.addSlotToContainer(new SlotItemHandler(burner.inventory, 5, 143, 54));
		
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(Playerinv, j + i * 9 + 9, 8 + j * 18, 104 + i * 18));
			}
		}

		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(Playerinv, i, 8 + i * 18, 162));
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = (Slot) this.inventorySlots.get(index);

		if(slot != null && slot.getHasStack()) {
			ItemStack originalStack = slot.getStack();
			stack = originalStack.copy();

			if(index <= 5) {
				if(!this.mergeItemStack(originalStack, 6, this.inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
				
				slot.onSlotChange(originalStack, stack);
				
			} else {
				
				if(stack.getItem() instanceof IBatteryItem) {
					if(!this.mergeItemStack(originalStack, 5, 6, false)) {
						return ItemStack.EMPTY;
					}
				} else if(stack.getItem() instanceof IItemFluidIdentifier) {
					if(!this.mergeItemStack(originalStack, 2, 3, false)) {
						return ItemStack.EMPTY;
					}
				} else if(TileEntityFurnace.isItemFuel(stack)) {
					if(!this.mergeItemStack(originalStack, 0, 1, false)) {
						return ItemStack.EMPTY;
					}
				} else {
					if(!this.mergeItemStack(originalStack, 3, 4, false)) {
						return ItemStack.EMPTY;
					}
				}
			}

			if(stack.isEmpty()) {
				slot.putStack((ItemStack.EMPTY));
			} else {
				slot.onSlotChanged();
			}
		}

		return stack;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return burner.isUseableByPlayer(player);
	}
/*
	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		this.burner.closeInventory();
	}
 */
}
