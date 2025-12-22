package com.hbm.inventory.container;

import com.hbm.inventory.SlotBattery;
import com.hbm.inventory.SlotTakeOnly;
import com.hbm.inventory.SlotUpgrade;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineArcWelder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerMachineArcWelder extends Container {
    private final TileEntityMachineArcWelder welder;

    public ContainerMachineArcWelder(InventoryPlayer playerInv, TileEntityMachineArcWelder tile) {
        welder = tile;

        // Inputs
        this.addSlotToContainer(new SlotItemHandler(welder.inventory, 0, 17, 36));
        this.addSlotToContainer(new SlotItemHandler(welder.inventory, 1, 35, 36));
        this.addSlotToContainer(new SlotItemHandler(welder.inventory, 2, 53, 36));
        // Output
        this.addSlotToContainer(new SlotTakeOnly(welder.inventory, 3, 107, 36));
        //Battery
        this.addSlotToContainer(new SlotBattery(welder.inventory, 4, 152, 72));
        //Fluid ID
        this.addSlotToContainer(new SlotItemHandler(welder.inventory, 5, 17, 63));
        //Upgrades
        this.addSlotToContainer(new SlotUpgrade(welder.inventory, 6, 89, 63));
        this.addSlotToContainer(new SlotUpgrade(welder.inventory, 7, 107, 63));

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 122 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 180));
        }
    }

    @Override
    public boolean canInteractWith(@NotNull EntityPlayer player) {
        return welder.isUseableByPlayer(player);
    }

    @Override
    public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
        ItemStack rStack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if(slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            rStack = stack.copy();

            if(index <= 7) {
                if(!this.mergeItemStack(stack, 8, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {

                if(Library.isBattery(rStack)) {
                    if(!this.mergeItemStack(stack, 4, 5, false)) return ItemStack.EMPTY;
                } else if(rStack.getItem() instanceof IItemFluidIdentifier) {
                    if(!this.mergeItemStack(stack, 5, 6, false)) return ItemStack.EMPTY;
                } else if(rStack.getItem() instanceof ItemMachineUpgrade) {
                    if(!this.mergeItemStack(stack, 6, 8, false)) return ItemStack.EMPTY;
                } else {
                    if(!this.mergeItemStack(stack, 0, 3, false)) return ItemStack.EMPTY;
                }
            }

            if(stack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return rStack;
    }
}
