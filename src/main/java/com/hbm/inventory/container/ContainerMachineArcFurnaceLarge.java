package com.hbm.inventory.container;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.inventory.SlotBattery;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.inventory.SlotUpgrade;
import com.hbm.inventory.recipes.ArcFurnaceRecipes;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.tileentity.machine.TileEntityMachineArcFurnaceLarge;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ContainerMachineArcFurnaceLarge extends Container {

    private final TileEntityMachineArcFurnaceLarge furnace;

    public ContainerMachineArcFurnaceLarge(InventoryPlayer playerInv, TileEntityMachineArcFurnaceLarge tile) {
        furnace = tile;

        //Electrodes
        for(int i = 0; i < 3; i++) this.addSlotToContainer(new SlotNonRetarded(tile.inventory, i, 62 + i * 18, 22));
        //Battery
        this.addSlotToContainer(new SlotBattery(tile.inventory, 3, 8, 108));
        //Upgrade
        this.addSlotToContainer(new SlotUpgrade(tile.inventory, 4, 152, 108));
        //Inputs
        for(int i = 0; i < 4; i++) for(int j = 0; j < 5; j++) this.addSlotToContainer(new SlotArcFurnace(tile, tile.inventory, 5 + j + i * 5, 44 + j * 18, 54 + i * 18));

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 158 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 216));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack rStack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if(slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            rStack = stack.copy();

            if(index <= 24) {
                if(!this.mergeItemStack(stack, 25, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {

                if(rStack.getItem() instanceof IBatteryItem || rStack.getItem() == ModItems.battery_creative) {
                    if(!InventoryUtil.mergeItemStack(this.inventorySlots, stack, 3, 4, false)) return ItemStack.EMPTY;
                } else if(rStack.getItem() == ModItems.arc_electrode) {
                    if(!InventoryUtil.mergeItemStack(this.inventorySlots, stack, 0, 3, false)) return ItemStack.EMPTY;
                } else if(rStack.getItem() instanceof ItemMachineUpgrade) {
                    if(!InventoryUtil.mergeItemStack(this.inventorySlots, stack, 4, 5, false)) return ItemStack.EMPTY;
                } else {
                    if(!InventoryUtil.mergeItemStack(this.inventorySlots, stack, 5, 25, false)) return ItemStack.EMPTY;
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
        return furnace.isUseableByPlayer(player);
    }

    public static class SlotArcFurnace extends SlotNonRetarded {
        TileEntityMachineArcFurnaceLarge furnace;

        SlotArcFurnace(TileEntityMachineArcFurnaceLarge furnace, IItemHandler inventory, int id, int x, int y) {
            super(inventory, id, x, y);
            this.furnace = furnace;
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            if(furnace.liquidMode) return true;
            ArcFurnaceRecipes.ArcFurnaceRecipe recipe = ArcFurnaceRecipes.getOutput(stack, false);
            if(recipe != null && recipe.solidOutput != null) {
                return recipe.solidOutput.getCount() * stack.getCount() <= recipe.solidOutput.getMaxStackSize() && stack.getCount() <= furnace.getMaxInputSize();
            }
            return false;
        }

        @Override
        public int getSlotStackLimit() {;
            return this.getHasStack() ? furnace.getMaxInputSize() : 1;
        }
    }
}
