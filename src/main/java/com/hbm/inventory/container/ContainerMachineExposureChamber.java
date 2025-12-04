package com.hbm.inventory.container;

import com.hbm.inventory.SlotBattery;
import com.hbm.inventory.SlotTakeOnly;
import com.hbm.inventory.SlotUpgrade;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineExposureChamber;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMachineExposureChamber extends Container {

    private final TileEntityMachineExposureChamber chamber;

    public ContainerMachineExposureChamber(InventoryPlayer invPlayer, TileEntityMachineExposureChamber tedf) {
        this.chamber = tedf;

        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 8, 18));
        this.addSlotToContainer(new SlotTakeOnly(tedf.inventory, 2, 8, 54));
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 80, 36));
        this.addSlotToContainer(new SlotTakeOnly(tedf.inventory, 4, 116, 36));
        this.addSlotToContainer(new SlotBattery(tedf.inventory, 5, 152, 54));
        this.addSlotToContainer(new SlotUpgrade(tedf.inventory, 6, 44, 54));
        this.addSlotToContainer(new SlotUpgrade(tedf.inventory, 7, 62, 54));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 104 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 162));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer p_82846_1_, int par2) {
        ItemStack var3 = ItemStack.EMPTY;
        Slot var4 = this.inventorySlots.get(par2);

        if (var4 != null && var4.getHasStack()) {
            ItemStack var5 = var4.getStack();
            var3 = var5.copy();

            if (par2 <= 6) {
                if (!this.mergeItemStack(var5, 7, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {

                if (var3.getItem() instanceof ItemMachineUpgrade) {
                    if (!this.mergeItemStack(var5, 5, 7, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (Library.isItemCanStoreEnergy(var3)) {
                    if (!this.mergeItemStack(var5, 4, 5, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!this.mergeItemStack(var5, 0, 3, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (var5.getCount() == 0) {
                var4.putStack(ItemStack.EMPTY);
            } else {
                var4.onSlotChanged();
            }
        }

        return var3;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return chamber.isUseableByPlayer(player);
    }
}
