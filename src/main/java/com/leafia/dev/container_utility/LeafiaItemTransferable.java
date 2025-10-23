package com.leafia.dev.container_utility;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
public abstract class LeafiaItemTransferable extends Container {
    public class LeafiaItemTransfer {
        short slots;
        boolean __changed = false; // "true" when the affected stack was changed
        short selectedIndex;
        Slot selectedSlot;
        ItemStack backup;
        ItemStack stack;
        ItemStack __return = null;
        short sourceSlotStart = 0;
        short sourceSlotEnd = 0;

        public short __maxIndex = 0; // the maximum index of this machine's own inventory

        public LeafiaItemTransfer(int slots) {
            // INFO: in other words, the "slots" argument should be the highest slot index plus 1.
            this.slots = (short)slots;
            this.__maxIndex = (short)(slots-1);
        }

        public LeafiaItemTransfer _selected(int clickIndex) {
            this.selectedIndex = (short) clickIndex;
            this.selectedSlot = inventorySlots.get(clickIndex);
            if (selectedSlot == null || !selectedSlot.getHasStack())
                __return = ItemStack.EMPTY;
            else {
                this.stack = this.selectedSlot.getStack();
                this.backup = this.stack.copy();
            }
            return this;
        }

        boolean moveItem(int targetSlotStart, int targetSlotEnd, boolean reverse) {
            return mergeItemStack(stack,targetSlotStart,targetSlotEnd+1,reverse);
        }

        public LeafiaItemTransfer __forSlots(int sourceSlotStart, int sourceSlotEnd) {
            this.sourceSlotStart = (short)Math.min(sourceSlotStart,__maxIndex);
            this.sourceSlotEnd = (short)Math.min(sourceSlotEnd,__maxIndex);
            return this;
        }
        public LeafiaItemTransfer __forInventory() {
            this.sourceSlotStart = __maxIndex;
            this.sourceSlotEnd = (short)inventorySlots.size();
            return this;
        }

        public LeafiaItemTransfer __tryMoveToSlot(int targetSlotStart, int targetSlotEnd, boolean reverse) {
            if ((__return == null) && !__changed && !stack.isEmpty() && (selectedIndex >= sourceSlotStart) && (selectedIndex <= sourceSlotEnd)) {
                __changed = moveItem(targetSlotStart, targetSlotEnd, reverse);
            }
            return this;
        }
        public LeafiaItemTransfer __tryMoveToInventory(boolean reverse) {
            return this.__tryMoveToSlot(this.slots,inventorySlots.size()-1,reverse);
        }

        public ItemStack __getReturn() {
            if (__return != null) return __return;
            if (!__changed)
                return ItemStack.EMPTY; // tell it to stop
            if (stack.isEmpty())
                this.selectedSlot.putStack(ItemStack.EMPTY);
            else
                this.selectedSlot.onSlotChanged();
            return this.backup; // tell it to continue
        }
    }
}