package com.hbm.inventory.container;

import com.hbm.inventory.SlotTakeOnly;
import com.hbm.tileentity.machine.TileEntityDiFurnace;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerDiFurnace extends Container {
  private final TileEntityDiFurnace diFurnace;

  public ContainerDiFurnace(InventoryPlayer invPlayer, TileEntityDiFurnace tile) {
    diFurnace = tile;

    // Inputs
    this.addSlotToContainer(new SlotItemHandler(tile.inventory, 0, 80, 18));
    this.addSlotToContainer(new SlotItemHandler(tile.inventory, 1, 80, 54));
    // Fuel
    this.addSlotToContainer(new SlotItemHandler(tile.inventory, 2, 8, 36));
    // Output
    this.addSlotToContainer(new SlotTakeOnly(tile.inventory, 3, 134, 36));

    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 9; j++) {
        this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
      }
    }

    for (int i = 0; i < 9; i++) {
      this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142));
    }
  }

  @Override
  public @NotNull ItemStack slotClick(
      int index, int button, @NotNull ClickType mode, @NotNull EntityPlayer player) {

    if (index >= 0 && index < 3 && button == 1 && mode == ClickType.PICKUP) {
      Slot slot = this.getSlot(index);
      if (!slot.getHasStack() && player.inventory.getItemStack().isEmpty()) {
        if (!player.world.isRemote) {
          if (index == 0) diFurnace.sideUpper = (byte) ((diFurnace.sideUpper + 1) % 6);
          if (index == 1) diFurnace.sideLower = (byte) ((diFurnace.sideLower + 1) % 6);
          if (index == 2) diFurnace.sideFuel = (byte) ((diFurnace.sideFuel + 1) % 6);

          diFurnace.markDirty();
        }
        return ItemStack.EMPTY;
      }
    }

    return super.slotClick(index, button, mode, player);
  }

  @Override
  public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
    ItemStack rStack = ItemStack.EMPTY;
    Slot slot = this.inventorySlots.get(index);

    if (slot != null && slot.getHasStack()) {
      ItemStack stack = slot.getStack();
      rStack = stack.copy();

      if (index <= 3) {
        if (!this.mergeItemStack(stack, 4, this.inventorySlots.size(), true)) {
          return ItemStack.EMPTY;
        }
      } else {
        if (diFurnace.hasItemPower(stack)) {
            if (!this.mergeItemStack(stack, 2, 3, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.mergeItemStack(stack, 0, 3, false)) {
          return ItemStack.EMPTY;
        }
      }

      if (stack.getCount() == 0) {
        slot.putStack(ItemStack.EMPTY);
      } else {
        slot.onSlotChanged();
      }
    }

    return rStack;
  }

  @Override
  public boolean canInteractWith(@NotNull EntityPlayer player) {
    return diFurnace.isUsableByPlayer(player);
  }
}
