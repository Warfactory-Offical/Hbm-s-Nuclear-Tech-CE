package com.hbm.inventory.container;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.inventory.SlotBattery;
import com.hbm.inventory.SlotTakeOnly;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.tileentity.machine.TileEntityElectrolyser;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerElectrolyserMetal extends Container {

    private TileEntityElectrolyser electrolyser;

    public ContainerElectrolyserMetal(InventoryPlayer invPlayer, TileEntityElectrolyser tedf) {
        electrolyser = tedf;

        //Battery
        this.addSlotToContainer(new SlotBattery(tedf.inventory, 0, 186, 109));
        //Upgrades
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 186, 140));
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 186, 158));
        //Input
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 14, 10, 22));
        //Outputs
        this.addSlotToContainer(new SlotTakeOnly(tedf.inventory, 15, 136, 18));
        this.addSlotToContainer(new SlotTakeOnly(tedf.inventory, 16, 154, 18));
        this.addSlotToContainer(new SlotTakeOnly(tedf.inventory, 17, 136, 36));
        this.addSlotToContainer(new SlotTakeOnly(tedf.inventory, 18, 154, 36));
        this.addSlotToContainer(new SlotTakeOnly(tedf.inventory, 19, 136, 54));
        this.addSlotToContainer(new SlotTakeOnly(tedf.inventory, 20, 154, 54));

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 122 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 180));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer p_82846_1_, int par2) {
        ItemStack var3 = ItemStack.EMPTY;
        Slot var4 = this.inventorySlots.get(par2);

        if(var4 != null && var4.getHasStack()) {
            ItemStack var5 = var4.getStack();
            var3 = var5.copy();

            if(par2 <= 10) {
                if(!this.mergeItemStack(var5, 11, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {

                if(var3.getItem() instanceof IBatteryItem || var3.getItem() == ModItems.battery_creative) {
                    if(!this.mergeItemStack(var5, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if(var3.getItem() instanceof ItemMachineUpgrade) {
                    if(!this.mergeItemStack(var5, 1, 3, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if(!this.mergeItemStack(var5, 3, 4, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if(var5.getCount() == 0) {
                var4.putStack(ItemStack.EMPTY);
            } else {
                var4.onSlotChanged();
            }
        }

        return var3;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return electrolyser.isUseableByPlayer(player);
    }
}
