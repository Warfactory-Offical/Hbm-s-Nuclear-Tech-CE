package com.hbm.inventory.container;

import com.hbm.inventory.SlotBattery;
import com.hbm.inventory.SlotTakeOnly;
import com.hbm.tileentity.machine.oil.TileEntityMachineVacuumDistill;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMachineVacuumDistill extends Container {

    private TileEntityMachineVacuumDistill distill;

    public ContainerMachineVacuumDistill(InventoryPlayer invPlayer, TileEntityMachineVacuumDistill tedf) {

        distill = tedf;

        //Battery
        this.addSlotToContainer(new SlotBattery(tedf.inventory, 0, 26, 90));
        //Canister Input (removed, requires pressurization)
        // this.addSlotToContainer(new SlotDeprecated(tedf, 1, 44, 90));
        //Canister Output (same as above)
        // this.addSlotToContainer(new SlotDeprecated(tedf, 2, 44, 108));
        //Heavy Oil Input
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 80, 90));
        //Heavy Oil Output
        this.addSlotToContainer(new SlotTakeOnly(tedf.inventory, 2, 80, 108));
        //Nahptha Input
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 98, 90));
        //Nahptha Output
        this.addSlotToContainer(new SlotTakeOnly(tedf.inventory, 4, 98, 108));
        //Light Oil Input
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 5, 116, 90));
        //Light Oil Output
        this.addSlotToContainer(new SlotTakeOnly(tedf.inventory, 6, 116, 108));
        //Petroleum Input
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 7, 134, 90));
        //Petroleum Output
        this.addSlotToContainer(new SlotTakeOnly(tedf.inventory, 8, 134, 108));
        //Fluid ID
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 9, 26, 108));

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
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
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
            } else if(!this.mergeItemStack(var5, 0, 1, false))
                if(!this.mergeItemStack(var5, 1, 2, false))
                    if(!this.mergeItemStack(var5, 3, 4, false))
                        if(!this.mergeItemStack(var5, 5, 6, false))
                            if(!this.mergeItemStack(var5, 7, 8, false))
                                if(!this.mergeItemStack(var5, 9, 10, false)) {
                                    return ItemStack.EMPTY;
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
        return distill.isUseableByPlayer(player);
    }
}
