package com.hbm.inventory.container;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.items.ModItems;
import com.hbm.tileentity.machine.fusion.TileEntityFusionTorus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ContainerFusionTorus extends ContainerBase {

    private TileEntityFusionTorus torus;

    public ContainerFusionTorus(InventoryPlayer invPlayer, IItemHandler torus) {
        super(invPlayer, torus);

        this.addSlotToContainer(new SlotNonRetarded(torus, 0, 8, 82));
        this.addSlotToContainer(new SlotNonRetarded(torus, 1, 71, 81));
        this.addSlotToContainer(new SlotNonRetarded(torus, 2, 130, 36));

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
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack copy = null;
        Slot slot = (Slot) this.inventorySlots.get(index);

        if(slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            copy = stack.copy();

            if(index <= 2) {
                if(!this.mergeItemStack(stack, 3, this.inventorySlots.size(), true)) return null;
            } else {

                if(copy.getItem() == ModItems.blueprints) {
                    if(!this.mergeItemStack(stack, 1, 2, false)) return null;
                } else if(copy.getItem() instanceof IBatteryItem || copy.getItem() == ModItems.battery_creative) {
                    if(!this.mergeItemStack(stack, 0, 1, false)) return null;
                } else {
                    return null;
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
