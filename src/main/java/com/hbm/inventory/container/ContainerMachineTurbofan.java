package com.hbm.inventory.container;

import com.hbm.inventory.SlotBattery;
import com.hbm.inventory.SlotTakeOnly;
import com.hbm.inventory.SlotUpgrade;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineTurbofan;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerMachineTurbofan extends Container {

    private final TileEntityMachineTurbofan turbofan;
    private int afterburner;

    public ContainerMachineTurbofan(InventoryPlayer invPlayer, TileEntityMachineTurbofan tedf) {
        afterburner = 0;

        turbofan = tedf;

        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 17, 17));
        this.addSlotToContainer(new SlotTakeOnly(tedf.inventory, 1, 17, 53));
        this.addSlotToContainer(new SlotUpgrade(tedf.inventory, 2, 98, 71));
        this.addSlotToContainer(new SlotBattery(tedf.inventory, 3, 143, 71));
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 4, 44, 71));
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 121 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 179));
        }
    }

    @Override
    public void addListener(IContainerListener crafting) {
        super.addListener(crafting);
        crafting.sendWindowProperty(this, 1, this.turbofan.afterburner);
    }

    @NotNull
    @Override
    public ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
        ItemStack rStack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            rStack = stack.copy();
            if (index <= 4) {
                if (!this.mergeItemStack(stack, 5, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (Library.isChargeableBattery(rStack)) {
                    if (!this.mergeItemStack(stack, 3, 4, false)) return ItemStack.EMPTY;
                } else if (rStack.getItem() instanceof IItemFluidIdentifier) {
                    if (!this.mergeItemStack(stack, 4, 5, false)) return ItemStack.EMPTY;
                } else if (rStack.getItem() instanceof ItemMachineUpgrade) {
                    if (!this.mergeItemStack(stack, 2, 3, false)) return ItemStack.EMPTY;
                } else {
                    if (!this.mergeItemStack(stack, 0, 1, false)) return ItemStack.EMPTY;
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

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return turbofan.isUseableByPlayer(player);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (IContainerListener par1 : this.listeners) {
            if (this.afterburner != this.turbofan.afterburner) {
                par1.sendWindowProperty(this, 1, this.turbofan.afterburner);
            }
        }

        this.afterburner = this.turbofan.afterburner;
    }

    @Override
    public void updateProgressBar(int id, int data) {
        if (id == 1) {
            turbofan.afterburner = data;
        }
    }

}
