package com.hbm.inventory.container;

import com.hbm.inventory.SlotBattery;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.albion.TileEntityPAQuadrupole;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerPAQuadrupole extends Container {

    private final TileEntityPAQuadrupole quadrupole;

    public ContainerPAQuadrupole(InventoryPlayer playerInv, TileEntityPAQuadrupole tile) {
        quadrupole = tile;

        //Battery
        this.addSlotToContainer(new SlotBattery(tile.inventory, 0, 26, 72));
        //Coil
        this.addSlotToContainer(new SlotItemHandler(tile.inventory, 1, 71, 36));

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
        return quadrupole.isUseableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack rStack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            rStack = stack.copy();

            if (index <= 1) {
                if (!this.mergeItemStack(stack, 2, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (Library.isItemBattery(rStack)) {
                    if (!this.mergeItemStack(stack, 0, 1, false)) return ItemStack.EMPTY;
                } else {
                    if (!this.mergeItemStack(stack, 1, 2, false)) return ItemStack.EMPTY;
                }
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
