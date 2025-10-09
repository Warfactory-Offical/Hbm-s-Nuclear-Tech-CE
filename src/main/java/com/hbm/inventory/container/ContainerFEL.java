package com.hbm.inventory.container;

import com.hbm.inventory.SlotBattery;
import com.hbm.tileentity.machine.TileEntityFEL;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerFEL extends Container {

	private TileEntityFEL fel;

	public ContainerFEL(InventoryPlayer invPlayer, TileEntityFEL tedf) {

		fel = tedf;

		//battery
		this.addSlotToContainer(new SlotBattery(tedf.inventory, 0, 182, 144));
		//laser crystal
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 141, 23));

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 83 + i * 18));
			}
		}

		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 141));
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer p_82846_1_, int par2) {
		ItemStack var3 = ItemStack.EMPTY;
		Slot var4 = (Slot) this.inventorySlots.get(par2);

		if(var4 != null && var4.getHasStack()) {
			ItemStack var5 = var4.getStack();
			var3 = var5.copy();

			if(par2 == 0) {
				if(!this.mergeItemStack(var5, 1, this.inventorySlots.size(), false)) {
					return ItemStack.EMPTY;
				}
			} else {
				if(!this.mergeItemStack(var5, 0, 1, false))
					return ItemStack.EMPTY;
			}

			if(var5.isEmpty()) {
				var4.putStack(ItemStack.EMPTY);
			} else {
				var4.onSlotChanged();
			}
		}

		return var3;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return fel.isUseableByPlayer(player);
	}
}