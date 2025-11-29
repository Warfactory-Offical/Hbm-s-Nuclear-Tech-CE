package com.hbm.inventory.container;

import com.hbm.inventory.SlotCraftingOutput;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.machine.fusion.TileEntityFusionBreeder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ContainerFusionBreeder extends Container {

    protected TileEntityFusionBreeder breeder;

    public ContainerFusionBreeder(InventoryPlayer invPlayer, TileEntityFusionBreeder tedf) {
        this.breeder = tedf;

        this.addSlotToContainer(new SlotNonRetarded(breeder.inventory, 0, 26, 72));
        this.addSlotToContainer(new SlotNonRetarded(breeder.inventory, 1, 48, 45));
        this.addSlotToContainer(new SlotCraftingOutput(invPlayer.player, breeder.inventory, 2, 112, 45));

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 118 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 176));
        }
    }

    @Override
    public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if(slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            copy = stack.copy();

            if(index <= 2) {
                if(!this.mergeItemStack(stack, 3, this.inventorySlots.size(), true)) return ItemStack.EMPTY;
            } else {

                if(copy.getItem() instanceof IItemFluidIdentifier) {
                    if(!this.mergeItemStack(stack, 0, 1, false)) return ItemStack.EMPTY;
                } else {
                    if(!this.mergeItemStack(stack, 1, 2, false)) return ItemStack.EMPTY;
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
        return breeder.isUseableByPlayer(player);
    }
}
