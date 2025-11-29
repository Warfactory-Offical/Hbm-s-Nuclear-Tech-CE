package com.hbm.inventory.container;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.items.ModItems;
import com.hbm.tileentity.machine.fusion.TileEntityFusionKlystron;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ContainerFusionKlystron extends Container {

    protected TileEntityFusionKlystron klystron;

    public ContainerFusionKlystron(InventoryPlayer invPlayer, TileEntityFusionKlystron tedf) {
        this.klystron = tedf;

        this.addSlotToContainer(new SlotNonRetarded(klystron.inventory, 0, 8, 72));

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 17 + j * 18, 118 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 17 + i * 18, 176));
        }
    }

    @Override
    public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if(slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            copy = stack.copy();

            if(index == 0) {
                if(!this.mergeItemStack(stack, 1, this.inventorySlots.size(), true)) return ItemStack.EMPTY;
            } else {

                if(copy.getItem() instanceof IBatteryItem || copy.getItem() == ModItems.battery_creative) {
                    if(!this.mergeItemStack(stack, 0, 1, false)) return ItemStack.EMPTY;
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if(stack.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return copy;
    }

    @Override
    public boolean canInteractWith(@NotNull EntityPlayer player) {
        return klystron.isUseableByPlayer(player);
    }
}
