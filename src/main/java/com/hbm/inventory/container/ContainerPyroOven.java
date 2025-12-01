package com.hbm.inventory.container;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.inventory.SlotBattery;
import com.hbm.inventory.SlotTakeOnly;
import com.hbm.inventory.SlotUpgrade;
import com.hbm.items.ModItems;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.tileentity.machine.oil.TileEntityMachinePyroOven;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerPyroOven extends Container {

    private TileEntityMachinePyroOven pyro;

    public ContainerPyroOven(InventoryPlayer invPlayer, TileEntityMachinePyroOven tedf) {
        pyro = tedf;

        //Battery
        this.addSlotToContainer(new SlotBattery(tedf.inventory, 0, 152, 72));
        //Input
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 35, 45));
        //Output
        this.addSlotToContainer(new SlotTakeOnly(tedf.inventory, 2, 89, 45));
        //Fluid ID
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 8, 72));
        //Upgrades
        this.addSlotToContainer(new SlotUpgrade(tedf.inventory, 4, 71, 72));
        this.addSlotToContainer(new SlotUpgrade(tedf.inventory, 5, 89, 72));

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
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack rStack = ItemStack.EMPTY;
        Slot slot = (Slot) this.inventorySlots.get(index);

        if(slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            rStack = stack.copy();

            if(index <= 5) {
                if(!this.mergeItemStack(stack, 6, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {

                if(rStack.getItem() instanceof IBatteryItem || rStack.getItem() == ModItems.battery_creative) {
                    if(!this.mergeItemStack(stack, 0, 1, false)) return ItemStack.EMPTY;
                } else if(rStack.getItem() instanceof IItemFluidIdentifier) {
                    if(!this.mergeItemStack(stack, 3, 4, false)) return ItemStack.EMPTY;
                } else if(rStack.getItem() instanceof ItemMachineUpgrade) {
                    if(!this.mergeItemStack(stack, 4, 6, false)) return ItemStack.EMPTY;
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

        return rStack;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return pyro.isUseableByPlayer(player);
    }
}
