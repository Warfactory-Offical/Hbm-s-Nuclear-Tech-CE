package com.hbm.inventory.container;

import com.hbm.inventory.SlotBattery;
import com.hbm.inventory.SlotTakeOnly;
import com.hbm.items.ModItems;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.oil.TileEntityMachineCatalyticReformer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMachineCatalyticReformer extends Container {

    private TileEntityMachineCatalyticReformer reformer;

    public ContainerMachineCatalyticReformer(InventoryPlayer invPlayer, TileEntityMachineCatalyticReformer tedf) {

        reformer = tedf;

        //Battery
        this.addSlotToContainer(new SlotBattery(tedf.inventory, 0, 17, 90));
        //Canister Input
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 35, 90));
        //Canister Output
        this.addSlotToContainer(new SlotTakeOnly(tedf.inventory, 2, 35, 108));
        //Reformate Input
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 107, 90));
        //Reformate Output
        this.addSlotToContainer(new SlotTakeOnly(tedf.inventory, 4, 107, 108));
        //Gas Input
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 5, 125, 90));
        //Gas Output
        this.addSlotToContainer(new SlotTakeOnly(tedf.inventory, 6, 125, 108));
        //Hydrogen Input
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 7, 143, 90));
        //Hydrogen Oil Output
        this.addSlotToContainer(new SlotTakeOnly(tedf.inventory, 8, 143, 108));
        //Fluid ID
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 9, 17, 108));
        //Catalyst
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 10, 71, 36));

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 156 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 214));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer p_82846_1_, int par2) {
        ItemStack var3 = ItemStack.EMPTY;
        Slot var4 = (Slot) this.inventorySlots.get(par2);

        if(var4 != null && var4.getHasStack()) {
            ItemStack var5 = var4.getStack();
            var3 = var5.copy();

            if(par2 <= 10) {
                if(!this.mergeItemStack(var5, 11, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {

                if(Library.isItemBattery(var3)) {
                    if(!this.mergeItemStack(var5, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if(var3.getItem() instanceof IItemFluidIdentifier) {
                    if(!this.mergeItemStack(var5, 9, 10, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if(var3.getItem() == ModItems.catalytic_converter) {
                    if(!this.mergeItemStack(var5, 10, 11, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if(!this.mergeItemStack(var5, 1, 2, false))
                        if(!this.mergeItemStack(var5, 3, 4, false))
                            if(!this.mergeItemStack(var5, 5, 6, false))
                                if(!this.mergeItemStack(var5, 7, 8, false))
                                    return ItemStack.EMPTY;
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
        return reformer.isUseableByPlayer(player);
    }
}
