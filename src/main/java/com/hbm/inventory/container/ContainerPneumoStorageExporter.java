package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotPattern;
import com.hbm.tileentity.network.TileEntityPneumoStorageExporter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ContainerPneumoStorageExporter extends ContainerBase {

	public ContainerPneumoStorageExporter(InventoryPlayer invPlayer, TileEntityPneumoStorageExporter exporter) {
		super(invPlayer, exporter.inventory);

		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new SlotPattern(exporter.inventory, i, 17 + (i % 3) * 18, 17 + (i / 3) * 18).allowStackSize());
		}

		addTakeOnlySlots(exporter.inventory, 9, 80, 17, 3, 3);
		playerInv(invPlayer, 8, 103);
	}

	@Override
	public @NotNull ItemStack slotClick(int index, int button, net.minecraft.inventory.ClickType type, @NotNull EntityPlayer player) {

		if(index < 0 || index >= 9) {
			return super.slotClick(index, button, type, player);
		}

		Slot slot = this.getSlot(index);
		ItemStack ret = ItemStack.EMPTY;
		ItemStack held = player.inventory.getItemStack();

		if(slot.getHasStack()) ret = slot.getStack().copy();
		slot.putStack(held.isEmpty() ? ItemStack.EMPTY : held.copy());
		return ret;
	}
}
