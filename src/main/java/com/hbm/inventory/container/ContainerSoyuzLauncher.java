package com.hbm.inventory.container;

import com.hbm.inventory.SlotBattery;
import com.hbm.tileentity.machine.TileEntitySoyuzLauncher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerSoyuzLauncher extends Container {

	private TileEntitySoyuzLauncher nukeBoy;
	
	public ContainerSoyuzLauncher(InventoryPlayer invPlayer, TileEntitySoyuzLauncher tedf) {
		
		nukeBoy = tedf;

		//Soyuz
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 62, 18));
		//Designator
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 62, 36));
		//Satellite
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 116, 18));
		//Landing module
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 116, 36));
		//Kerosene IN
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 4, 8, 90));
		//Kerosene OUT
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 5, 8, 108));
		//Peroxide IN
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 6, 26, 90));
		//Peroxide OUT
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 7, 26, 108));
		//Battery
		this.addSlotToContainer(new SlotBattery(tedf.inventory, 8, 44, 108));
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 6; j++)
			{
				this.addSlotToContainer(new SlotItemHandler(tedf.inventory, j + i * 6 + 9, 62 + j * 18, 72 + i * 18));
			}
		}
		
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
			
            if (par2 <= 27) {
				if (!this.mergeItemStack(var5, 9, this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if (!this.mergeItemStack(var5, 0, 1, false))
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