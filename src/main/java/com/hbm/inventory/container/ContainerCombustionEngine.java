package com.hbm.inventory.container;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.inventory.SlotBattery;
import com.hbm.inventory.SlotTakeOnly;
import com.hbm.items.ModItems;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.machine.TileEntityMachineCombustionEngine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerCombustionEngine extends Container {

  private final TileEntityMachineCombustionEngine engine;

  public ContainerCombustionEngine(
      InventoryPlayer invPlayer, TileEntityMachineCombustionEngine tile) {
    this.engine = tile;

    this.addSlotToContainer(new SlotItemHandler(tile.inventory, 0, 17, 17));
    this.addSlotToContainer(new SlotTakeOnly(tile.inventory, 1, 17, 53));
    this.addSlotToContainer(new SlotItemHandler(tile.inventory, 2, 88, 71));
    this.addSlotToContainer(new SlotBattery(tile.inventory, 3, 143, 71));
    this.addSlotToContainer(new SlotItemHandler(tile.inventory, 4, 35, 71));

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
  public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
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

        if (rStack.getItem() instanceof IBatteryItem) {
          if (!this.mergeItemStack(stack, 3, 4, false)) {
            return ItemStack.EMPTY;
          }
        } else if (rStack.getItem() instanceof IItemFluidIdentifier) {
          if (!this.mergeItemStack(stack, 4, 5, false)) {
            return ItemStack.EMPTY;
          }
        } else if (rStack.getItem() == ModItems.piston_set) {
          if (!this.mergeItemStack(stack, 2, 3, false)) {
            return ItemStack.EMPTY;
          }
        } else {
          if (!this.mergeItemStack(stack, 0, 1, false)) {
            return ItemStack.EMPTY;
          }
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
  public boolean canInteractWith(@NotNull EntityPlayer player) {
    return engine.isUseableByPlayer(player);
  }
}
