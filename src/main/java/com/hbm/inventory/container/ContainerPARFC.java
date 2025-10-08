package com.hbm.inventory.container;

import com.hbm.inventory.SlotBattery;
import com.hbm.tileentity.machine.albion.TileEntityPARFC;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ContainerPARFC extends Container {

    private final TileEntityPARFC rfc;

    public ContainerPARFC(InventoryPlayer playerInv, TileEntityPARFC tile) {
        rfc = tile;

        //Battery
        this.addSlotToContainer(new SlotBattery(tile.inventory, 0, 53, 72));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 122 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 180));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return rfc.isUseableByPlayer(player);
    }

    @Override
    public @NotNull ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack rStack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            rStack = stack.copy();

            if (index <= 0) {
                if (!this.mergeItemStack(stack, 1, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.mergeItemStack(stack, 0, 1, false)) return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return rStack;
    }
}
