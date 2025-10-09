package com.hbm.inventory.container;

import com.hbm.inventory.SlotBattery;
import com.hbm.inventory.SlotTakeOnly;
import com.hbm.tileentity.machine.TileEntityHadron;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerHadron extends Container {

	private TileEntityHadron hadron;

	public ContainerHadron(InventoryPlayer invPlayer, TileEntityHadron tedf) {

		hadron = tedf;

		//Inputs
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 17, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 35, 36));
		//Outputs
		this.addSlotToContainer(new SlotTakeOnly(tedf.inventory, 2, 125, 36));
		this.addSlotToContainer(new SlotTakeOnly(tedf.inventory, 3, 143, 36));
		//Battery
		this.addSlotToContainer(new SlotBattery(tedf.inventory, 4, 44, 108));

		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18 + (18 * 3) + 2));
			}
		}

		for(int i = 0; i < 9; i++)
		{
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142 + (18 * 3) + 2));
		}
	}

	@Override
    public ItemStack transferStackInSlot(EntityPlayer p_82846_1_, int par2)
    {
		ItemStack var3 = ItemStack.EMPTY;
		Slot var4 = (Slot) this.inventorySlots.get(par2);

		if (var4 != null && var4.getHasStack())
		{
			ItemStack var5 = var4.getStack();
			var3 = var5.copy();

            if (par2 <= 4) {
				if (!this.mergeItemStack(var5, 5, this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(var5, 0, 2, false)) {
							return ItemStack.EMPTY;
			}

			if (var5.isEmpty())
			{
				var4.putStack(ItemStack.EMPTY);
			}
			else
			{
				var4.onSlotChanged();
			}
		}

		return var3;
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return hadron.isUseableByPlayer(player);
	}
}