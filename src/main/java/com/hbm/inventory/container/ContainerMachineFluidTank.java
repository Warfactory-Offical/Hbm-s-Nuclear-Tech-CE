package com.hbm.inventory.container;

import com.hbm.inventory.SlotTakeOnly;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineFluidTank;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerMachineFluidTank extends Container {

  private final TileEntityMachineFluidTank fluidTank;

  public ContainerMachineFluidTank(InventoryPlayer invPlayer, TileEntityMachineFluidTank tedf) {
    fluidTank = tedf;

    this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 8, 17));
    this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 8, 53));
    this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 53 - 18, 17));
    this.addSlotToContainer(new SlotTakeOnly(tedf.inventory, 3, 53 - 18, 53));
    this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 4, 125, 17));
    this.addSlotToContainer(new SlotTakeOnly(tedf.inventory, 5, 125, 53));

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
  public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
    ItemStack rStack = ItemStack.EMPTY;
    Slot slot = this.inventorySlots.get(index);

    if (slot != null && slot.getHasStack()) {
      ItemStack stack = slot.getStack();
      rStack = stack.copy();

      if (index <= 5) {
        if (!this.mergeItemStack(stack, 6, this.inventorySlots.size(), true)) {
          return ItemStack.EMPTY;
        }
      } else {
        if (Library.isStackDrainableForTank(stack, fluidTank.tank)) {
          if (!this.mergeItemStack(stack, 2, 3, false)) {
            return ItemStack.EMPTY;
          }
        } else if (!this.mergeItemStack(stack, 0, 5, false)) {
          return ItemStack.EMPTY;
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
    return fluidTank.isUseableByPlayer(player);
  }
}
