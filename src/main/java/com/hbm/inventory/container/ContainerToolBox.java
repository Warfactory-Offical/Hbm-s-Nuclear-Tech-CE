package com.hbm.inventory.container;

import com.hbm.inventory.SlotNonRetarded;
import com.hbm.items.tool.ItemToolBox.InventoryToolBox;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerToolBox extends Container {
    private final InventoryToolBox box;

    public ContainerToolBox(InventoryPlayer invPlayer, InventoryToolBox box) {
        this.box = box;
        this.box.openInventory();

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 8; j++) {
                this.addSlotToContainer(new SlotNonRetarded(box, j + i * 8, 17 + j * 18, 49 + i * 18));
            }
        }

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 129 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 187));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int par2) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(par2);

        if(slot != null && slot.getHasStack()) {
            ItemStack stackInSlot = slot.getStack();
            stack = stackInSlot.copy();

            if(par2 <= box.getSlots() - 1) {
                if(!InventoryUtil.mergeItemStack(this.inventorySlots, stackInSlot, box.getSlots(), this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if(!InventoryUtil.mergeItemStack(this.inventorySlots, stackInSlot, 0, box.getSlots(), false)) {
                return ItemStack.EMPTY;
            }

            if(stackInSlot.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            slot.onTake(player, stackInSlot);
        }

        return stack;
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType mode, EntityPlayer player) {
        // prevents the player from moving around the currently open box
        if(mode == ClickType.SWAP && dragType == player.inventory.currentItem) return ItemStack.EMPTY;
        if(slotId == player.inventory.currentItem + 51) return ItemStack.EMPTY;
        return super.slotClick(slotId, dragType, mode, player);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return box.player.equals(player);
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        this.box.closeInventory();
    }
}
