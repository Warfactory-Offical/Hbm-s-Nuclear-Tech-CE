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

public class ContainerMachineChemicalFactory extends ContainerBase {

    public ContainerMachineChemicalFactory(InventoryPlayer invPlayer, IItemHandler chemicalPlant) {
        super(invPlayer, chemicalPlant);

        // Battery
        this.addSlotToContainer(new SlotNonRetarded(chemicalPlant, 0, 224, 88));
        // Upgrades
        this.addSlots(chemicalPlant, 1, 206, 125, 3, 1);

        for(int i = 0; i < 4; i++) {
            // Template
            this.addSlots(chemicalPlant, 4 + i * 7, 93, 20 + i * 22, 1, 1, 16);
            // Solid Input
            this.addSlots(chemicalPlant, 5 + i * 7, 10, 20 + i * 22, 1, 3, 16);
            // Solid Output
            this.addOutputSlots(invPlayer.player, chemicalPlant, 8 + i * 7, 139, 20 + i * 22, 1, 3, 16);
        }

        this.playerInv(invPlayer, 26, 134);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack slotOriginal = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

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
                    if(!this.mergeItemStack(slotStack, 4, 5, false)) return ItemStack.EMPTY;
                } else if(slotOriginal.getItem() instanceof ItemBlueprints) {
                    if(!this.mergeItemStack(slotStack, 11, 12, false)) return ItemStack.EMPTY;
                } else if(slotOriginal.getItem() instanceof ItemBlueprints) {
                    if(!this.mergeItemStack(slotStack, 18, 19, false)) return ItemStack.EMPTY;
                } else if(slotOriginal.getItem() instanceof ItemBlueprints) {
                    if(!this.mergeItemStack(slotStack, 25, 26, false)) return ItemStack.EMPTY;
                } else if(slotOriginal.getItem() instanceof ItemMachineUpgrade) {
                    if(!this.mergeItemStack(slotStack, 1, 4, false)) return ItemStack.EMPTY;
                } else {
                    if(!InventoryUtil.mergeItemStack(this.inventorySlots, slotStack, 5, 8, false) &&
                            !InventoryUtil.mergeItemStack(this.inventorySlots, slotStack, 12, 15, false) &&
                            !InventoryUtil.mergeItemStack(this.inventorySlots, slotStack, 19, 22, false) &&
                            !InventoryUtil.mergeItemStack(this.inventorySlots, slotStack, 26, 29, false)) return ItemStack.EMPTY;
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
