package com.hbm.inventory.container;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.inventory.slot.SlotNonRetarded;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBlueprints;
import com.hbm.tileentity.machine.TileEntityMachineSuperComputer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public class ContainerMachineSuperComputer extends ContainerBase {

	public ContainerMachineSuperComputer(InventoryPlayer invPlayer, TileEntityMachineSuperComputer computer) {
		super(invPlayer, computer.inventory);

		// Battery
		this.addSlotToContainer(new SlotNonRetarded(computer.inventory, 0, 152, 81));
		// Schematic
		this.addSlotToContainer(new SlotNonRetarded(computer.inventory, 1, 35, 80));
		// Input
		this.addSlots(computer.inventory, 2, 8, 27, 1, 3);
		// Output
		this.addOutputSlots(invPlayer.player, computer.inventory, 5, 80, 27, 1, 3);

		this.playerInv(invPlayer, 8, 129);
	}
}
