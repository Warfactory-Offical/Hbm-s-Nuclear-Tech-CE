package com.hbm.inventory.container;

import com.hbm.inventory.SlotBattery;
import com.hbm.inventory.SlotTakeOnly;
import com.hbm.tileentity.machine.TileEntityMachineSchrabidiumTransmutator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
// Th3_Sl1ze: it was deleted on 1.7, I'll mark it for deletion
@Deprecated
public class ContainerMachineSchrabidiumTransmutator extends Container {

private TileEntityMachineSchrabidiumTransmutator nukeBoy;

	public ContainerMachineSchrabidiumTransmutator(InventoryPlayer invPlayer, TileEntityMachineSchrabidiumTransmutator tedf) {
		
		nukeBoy = tedf;

		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 44, 63));
		this.addSlotToContainer(new SlotTakeOnly(tedf.inventory, 1, 134, 63));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 26, 18));
		this.addSlotToContainer(new SlotBattery(tedf.inventory, 3, 8, 108));
		
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
		public void addListener(IContainerListener listener) {
			super.addListener(listener);
			listener.sendWindowProperty(this, 0, this.nukeBoy.process);
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
			else if (!this.mergeItemStack(var5, 0, 1, false))
			{
				if (!this.mergeItemStack(var5, 3, 4, false))
					if (!this.mergeItemStack(var5, 2, 3, false))
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
		return nukeBoy.isUseableByPlayer(player);
	}
	
	@Override
	public void updateProgressBar(int i, int j) {
		if(i == 0)
		{
			nukeBoy.process = j;
		}
	}
}
