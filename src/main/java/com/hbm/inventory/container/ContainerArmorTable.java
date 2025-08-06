package com.hbm.inventory.container;

import com.hbm.handler.ArmorModHandler;
import com.hbm.items.armor.ItemArmorMod;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ContainerArmorTable extends Container {

	public final InventoryBasic upgrades = new InventoryBasic("Upgrades", false, ArmorModHandler.MOD_SLOTS);
	public final IInventory armor = new InventoryCraftResult();
	private final EntityPlayer player;

	public ContainerArmorTable(InventoryPlayer inventory) {
		this.player = inventory.player;

		this.addSlotToContainer(new UpgradeSlot(upgrades, ArmorModHandler.helmet_only, 26 + 22, 27));	// helmet only
		this.addSlotToContainer(new UpgradeSlot(upgrades, ArmorModHandler.plate_only, 62 + 22, 27));	// chestplate only
		this.addSlotToContainer(new UpgradeSlot(upgrades, ArmorModHandler.legs_only, 98 + 22, 27));		// leggins only
		this.addSlotToContainer(new UpgradeSlot(upgrades, ArmorModHandler.boots_only, 134 + 22, 45));	// boots only
		this.addSlotToContainer(new UpgradeSlot(upgrades, ArmorModHandler.servos, 134 + 22, 81));		//servos/frame
		this.addSlotToContainer(new UpgradeSlot(upgrades, ArmorModHandler.cladding, 98 + 22, 99));		//radiation cladding
		this.addSlotToContainer(new UpgradeSlot(upgrades, ArmorModHandler.kevlar, 62 + 22, 99));		//kevlar/sapi/(ERA? :) )
		this.addSlotToContainer(new UpgradeSlot(upgrades, ArmorModHandler.extra, 26 + 22, 99));			//special parts
		this.addSlotToContainer(new UpgradeSlot(upgrades, ArmorModHandler.battery, 8 + 22, 63));		//special parts

		this.addSlotToContainer(new Slot(armor, 0, 44 + 22, 63) {
			@Override
			public boolean isItemValid(@NotNull ItemStack stack) {
				return stack.getItem() instanceof ItemArmor;
			}

			@Override
			public void putStack(@NotNull ItemStack stack) {
				super.putStack(stack);
				upgrades.clear();
				if (!stack.isEmpty()) {
					ItemStack[] mods = ArmorModHandler.pryMods(stack);
					if (mods.length != 0) {
						for (int i = 0; i < ArmorModHandler.MOD_SLOTS; i++) {
							if (mods[i] != null && !mods[i].isEmpty()) {
								upgrades.setInventorySlotContents(i, mods[i]);
							}
						}
					}
				}
			}

			@Override
			public @NotNull ItemStack onTake(@NotNull EntityPlayer thePlayer, @NotNull ItemStack stack) {
				for (int i = 0; i < ArmorModHandler.MOD_SLOTS; i++) {
					ItemStack mod = upgrades.getStackInSlot(i);
					if (!mod.isEmpty()) {
						ArmorModHandler.applyMod(stack, mod);
					}
				}
				upgrades.clear();
				return super.onTake(thePlayer, stack);
			}
		});

		final EntityEquipmentSlot[] equipmentSlots = new EntityEquipmentSlot[]{EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET};
		for (int i = 0; i < 4; ++i) {
			final EntityEquipmentSlot slotType = equipmentSlots[i];
			this.addSlotToContainer(new Slot(inventory, 39 - i, -17 + 22, 36 + i * 18) {
				@Override
				public int getSlotStackLimit() {
					return 1;
				}

				@Override
				public boolean isItemValid(@NotNull ItemStack stack) {
					return stack.getItem().isValidArmor(stack, slotType, player);
				}

				@Override
				public boolean canTakeStack(@NotNull EntityPlayer playerIn) {
					ItemStack itemstack = this.getStack();
					return !(!itemstack.isEmpty() && !playerIn.isCreative() && EnchantmentHelper.hasBindingCurse(itemstack)) && super.canTakeStack(playerIn);
				}

				@Nullable
				@Override
				public String getSlotTexture() {
					return ItemArmor.EMPTY_SLOT_NAMES[slotType.getIndex()];
				}
			});
		}

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(inventory, j + i * 9 + 9, 8 + j * 18 + 22, 140 + i * 18));
			}
		}

		for (int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(inventory, i, 8 + i * 18 + 22, 198));
		}

		this.onCraftMatrixChanged(this.upgrades);
	}

	@Override
	public boolean canInteractWith(@NotNull EntityPlayer player) {
		return true;
	}

	@Override
	public void onContainerClosed(@NotNull EntityPlayer playerIn) {
		super.onContainerClosed(playerIn);

		if (!playerIn.world.isRemote) {
			ItemStack armorStack = this.armor.removeStackFromSlot(0);
			if (!armorStack.isEmpty()) {
				for(int i = 0; i < this.upgrades.getSizeInventory(); ++i) {
					ItemStack modStack = this.upgrades.getStackInSlot(i);
					if(!modStack.isEmpty()) {
						ArmorModHandler.removeMod(armorStack, i);
					}
				}
				playerIn.dropItem(armorStack, false);
			}

			for (int i = 0; i < this.upgrades.getSizeInventory(); ++i) {
				ItemStack modStack = this.upgrades.removeStackFromSlot(i);
				if (!modStack.isEmpty()) {
					playerIn.dropItem(modStack, false);
				}
			}
		}
	}

	@Override
	public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			ItemStack stackInSlot = slot.getStack();
			itemstack = stackInSlot.copy();
			if (index <= ArmorModHandler.MOD_SLOTS) {
				if (!this.mergeItemStack(stackInSlot, ArmorModHandler.MOD_SLOTS + 1, this.inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
				slot.onSlotChange(stackInSlot, itemstack);
			}
			else {
				if (stackInSlot.getItem() instanceof ItemArmor) {
					if (!this.mergeItemStack(stackInSlot, ArmorModHandler.MOD_SLOTS, ArmorModHandler.MOD_SLOTS + 1, false)) {
						return ItemStack.EMPTY;
					}
				}
				else if (stackInSlot.getItem() instanceof ItemArmorMod) {
					ItemArmorMod mod = (ItemArmorMod) stackInSlot.getItem();
					int targetSlot = mod.type;

					if (targetSlot >= 0 && targetSlot < ArmorModHandler.MOD_SLOTS && this.inventorySlots.get(targetSlot).isItemValid(stackInSlot)) {
						if (!this.mergeItemStack(stackInSlot, targetSlot, targetSlot + 1, false)) {
							return ItemStack.EMPTY;
						}
					} else {
						return ItemStack.EMPTY;
					}
				}
				else if (index >= ArmorModHandler.MOD_SLOTS + 1 + 4 && index < ArmorModHandler.MOD_SLOTS + 1 + 4 + 27) {
					if (!this.mergeItemStack(stackInSlot, ArmorModHandler.MOD_SLOTS + 1 + 4 + 27, this.inventorySlots.size(), false)) {
						return ItemStack.EMPTY;
					}
				} else if (index >= ArmorModHandler.MOD_SLOTS + 1 + 4 + 27 && index < this.inventorySlots.size()) {
					if (!this.mergeItemStack(stackInSlot, ArmorModHandler.MOD_SLOTS + 1 + 4, ArmorModHandler.MOD_SLOTS + 1 + 4 + 27, false)) {
						return ItemStack.EMPTY;
					}
				}
			}

			if (stackInSlot.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}

			if (stackInSlot.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(playerIn, stackInSlot);
		}

		return itemstack;
	}

	public class UpgradeSlot extends Slot {
		public UpgradeSlot(IInventory inventory, int index, int x, int y) {
			super(inventory, index, x, y);
		}

		@Override
		public boolean isItemValid(@NotNull ItemStack stack) {
			if (armor.getStackInSlot(0).isEmpty() || !(stack.getItem() instanceof ItemArmorMod)) {
				return false;
			}
			if (!ArmorModHandler.isApplicable(armor.getStackInSlot(0), stack)) {
				return false;
			}
			return ((ItemArmorMod) stack.getItem()).type == this.getSlotIndex();
		}

		@Override
		public void putStack(@NotNull ItemStack stack) {
			super.putStack(stack);
			if (!stack.isEmpty()) {
				ArmorModHandler.applyMod(armor.getStackInSlot(0), stack);
			}
		}

		@NotNull
		@Override
		public ItemStack onTake(@NotNull EntityPlayer thePlayer, ItemStack stack) {
			if (!stack.isEmpty()) {
				ArmorModHandler.removeMod(armor.getStackInSlot(0), this.getSlotIndex());
			}
			return super.onTake(thePlayer, stack);
		}

		@Override
		public void onSlotChange(@NotNull ItemStack current, @NotNull ItemStack original) {
			if (current.isEmpty() && !original.isEmpty()) {
				ArmorModHandler.removeMod(armor.getStackInSlot(0), this.getSlotIndex());
			}
		}
	}
}