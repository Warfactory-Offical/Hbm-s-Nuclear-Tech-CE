package com.hbm.inventory.container;

import com.hbm.inventory.SlotBattery;
import com.hbm.inventory.SlotTakeOnly;
import com.hbm.inventory.SlotUpgrade;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineGasCent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerMachineGasCent extends Container {

  private final TileEntityMachineGasCent gasCent;

  public ContainerMachineGasCent(InventoryPlayer invPlayer, TileEntityMachineGasCent teGasCent) {

    gasCent = teGasCent;

    // Output
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        this.addSlotToContainer(
            new SlotTakeOnly(gasCent.inventory, j + i * 2, 71 + j * 18, 53 + i * 18));
      }
    }

    // Battery
    this.addSlotToContainer(new SlotBattery(gasCent.inventory, 4, 182, 71));

    // Fluid ID IO
    this.addSlotToContainer(new SlotItemHandler(gasCent.inventory, 5, 91, 15));

    // Upgrade
    this.addSlotToContainer(new SlotUpgrade(gasCent.inventory, 6, 69, 15));

    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 9; j++) {
        this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 122 + i * 18));
      }
    }

    for (int i = 0; i < 9; i++) {
      this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 180));
    }
  }

  @Override
  public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
    ItemStack rStack = ItemStack.EMPTY;
    Slot slot = this.inventorySlots.get(index);

    if (slot != null && slot.getHasStack()) {
      ItemStack stack = slot.getStack();
      rStack = stack.copy();

      if (index <= 6) {
        if (!this.mergeItemStack(stack, 7, this.inventorySlots.size(), true)) {
          return ItemStack.EMPTY;
        }
      } else {
        if (Library.isItemCanStoreEnergy(rStack)) {
          if (!this.mergeItemStack(stack, 4, 5, false)) return ItemStack.EMPTY;
        } else if (rStack.getItem() instanceof IItemFluidIdentifier) {
          if (!this.mergeItemStack(stack, 5, 6, false)) return ItemStack.EMPTY;
        } else if (rStack.getItem() instanceof ItemMachineUpgrade) {
          if (!this.mergeItemStack(stack, 6, 7, false)) return ItemStack.EMPTY;
        } else return ItemStack.EMPTY;
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
    return gasCent.isUseableByPlayer(player);
  }
}
