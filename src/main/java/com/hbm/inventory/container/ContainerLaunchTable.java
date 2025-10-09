package com.hbm.inventory.container;

import com.hbm.inventory.SlotBattery;
import com.hbm.tileentity.bomb.TileEntityLaunchTable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerLaunchTable extends Container {

	private TileEntityLaunchTable nukeBoy;
	
	public ContainerLaunchTable(InventoryPlayer invPlayer, TileEntityLaunchTable tedf) {
		
		nukeBoy = tedf;

		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 26, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 26, 72));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 116, 90-18));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 134, 90-18));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 4, 152, 90));
		this.addSlotToContainer(new SlotBattery(tedf.inventory, 5, 116, 108));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 6, 116, 90));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 7, 134, 90));
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18 + 56));
			}
		}
		
		for(int i = 0; i < 9; i++)
		{
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142 + 56));
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
			
            if (par2 <= 7) {
				if (!this.mergeItemStack(var5, 8, this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if (!this.mergeItemStack(var5, 0, 8, false))
				return ItemStack.EMPTY;
			
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
		return nukeBoy.isUseableByPlayer(player);
	}
}
