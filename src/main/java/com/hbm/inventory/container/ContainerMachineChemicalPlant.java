package com.hbm.inventory.container;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.inventory.SlotCraftingOutput;
import com.hbm.inventory.SlotNonRetarded;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBlueprints;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ContainerMachineChemicalPlant extends ContainerBase {

    public ContainerMachineChemicalPlant(InventoryPlayer invPlayer, IItemHandler chemicalPlant) {
        super(invPlayer, chemicalPlant);

        // Battery
        this.addSlotToContainer(new SlotNonRetarded(chemicalPlant, 0, 152, 81));
        // Schematic
        this.addSlotToContainer(new SlotNonRetarded(chemicalPlant, 1, 35, 126));
        // Upgrades
        this.addSlots(chemicalPlant, 2, 152, 108, 2, 1);
        // Solid Input
        this.addSlots(chemicalPlant, 4, 8, 99, 1, 3);
        // Solid Output
        this.addOutputSlots(invPlayer.player, chemicalPlant, 7, 80, 99, 1, 3);
        // Fluid Input
        this.addSlots(			chemicalPlant, 10, 8, 54, 1, 3);
        this.addTakeOnlySlots(	chemicalPlant, 13, 8, 72, 1, 3);
        // Fluid Output
        this.addSlots(			chemicalPlant, 16, 80, 54, 1, 3);
        this.addTakeOnlySlots(	chemicalPlant, 19, 80, 72, 1, 3);

        this.playerInv(invPlayer, 8, 174);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack slotOriginal = ItemStack.EMPTY;
        Slot slot = (Slot) this.inventorySlots.get(index);

        if(slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            slotOriginal = slotStack.copy();

            if(index <= tile.getSlots() - 1) {
                SlotCraftingOutput.checkAchievements(player, slotStack);
                if(!this.mergeItemStack(slotStack, tile.getSlots(), this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {

                if(slotOriginal.getItem() instanceof IBatteryItem || slotOriginal.getItem() == ModItems.battery_creative) {
                    if(!this.mergeItemStack(slotStack, 0, 1, false)) return ItemStack.EMPTY;
                } else if(slotOriginal.getItem() instanceof ItemBlueprints) {
                    if(!this.mergeItemStack(slotStack, 1, 2, false)) return ItemStack.EMPTY;
                } else if(slotOriginal.getItem() instanceof ItemMachineUpgrade) {
                    if(!this.mergeItemStack(slotStack, 2, 4, false)) return ItemStack.EMPTY;
                } else {
                    if(!InventoryUtil.mergeItemStack(this.inventorySlots, slotStack, 4, 7, false)) return ItemStack.EMPTY;
                }
            }

            if(slotStack.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            slot.onTake(player, slotStack);
        }

        return slotOriginal;
    }
}