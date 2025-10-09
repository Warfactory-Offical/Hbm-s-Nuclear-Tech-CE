package com.hbm.inventory.container;

import com.hbm.inventory.SlotBattery;
import com.hbm.inventory.SlotUpgrade;
import com.hbm.tileentity.machine.TileEntityMachineMixer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMixer extends Container {
	
	private TileEntityMachineMixer mixer;
	
	public ContainerMixer(InventoryPlayer player, TileEntityMachineMixer mixer) {
		this.mixer = mixer;

		//Battery
		this.addSlotToContainer(new SlotBattery(mixer.inventory, 0, 23, 77));
		//Item Input
		this.addSlotToContainer(new SlotItemHandler(mixer.inventory, 1, 43, 77));
		//Fluid ID
		this.addSlotToContainer(new SlotItemHandler(mixer.inventory, 2, 117, 77));
		//Upgrades
		this.addSlotToContainer(new SlotUpgrade(mixer.inventory, 3, 137, 24));
		this.addSlotToContainer(new SlotUpgrade(mixer.inventory, 4, 137, 42));

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(player, j + i * 9 + 9, 8 + j * 18, 122 + i * 18));
			}
		}

		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(player, i, 8 + i * 18, 180));
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
			
            if (par2 <= 3) {
				if (!this.mergeItemStack(var5, 4, this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if (!this.mergeItemStack(var5, 0, 3, false))
			{
				return ItemStack.EMPTY;
			}
			
			if (var5.getCount() == 0)
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
		return mixer.isUseableByPlayer(player);
	}
}
