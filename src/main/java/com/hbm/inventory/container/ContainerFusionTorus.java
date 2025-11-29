package com.hbm.inventory.container;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.items.ModItems;
import com.hbm.tileentity.machine.fusion.TileEntityFusionTorus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerFusionTorus extends Container {

    private TileEntityFusionTorus torus;

    public ContainerFusionTorus(InventoryPlayer invPlayer, TileEntityFusionTorus torus) {
        this.torus = torus;

        this.addSlotToContainer(new SlotNonRetarded(torus.inventory, 0, 8, 82));
        this.addSlotToContainer(new SlotNonRetarded(torus.inventory, 1, 71, 81));
        this.addSlotToContainer(new SlotNonRetarded(torus.inventory, 2, 130, 36));

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 35 + j * 18, 162 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 35 + i * 18, 220));
        }
    }

    @Override
    public @NotNull ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if(slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            copy = stack.copy();

            if(index <= 2) {
                if(!this.mergeItemStack(stack, 3, this.inventorySlots.size(), true)) return ItemStack.EMPTY;
            } else {

                if(copy.getItem() == ModItems.blueprints) {
                    if(!this.mergeItemStack(stack, 1, 2, false)) return ItemStack.EMPTY;
                } else if(copy.getItem() instanceof IBatteryItem || copy.getItem() == ModItems.battery_creative) {
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
    public boolean canInteractWith(EntityPlayer player) {
        return torus.isUseableByPlayer(player);
    }

}
