package com.hbm.inventory.container;

import com.hbm.tileentity.network.TileEntityPneumoStorageExporter;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerPneumoStorageExporter extends ContainerBase {

	public ContainerPneumoStorageExporter(InventoryPlayer invPlayer, TileEntityPneumoStorageExporter exporter) {
		super(invPlayer, exporter.inventory);

		addSlots(exporter.inventory, 0, 8, 17, 3, 3);
		addSlots(exporter.inventory, 9, 116, 17, 3, 3);
		playerInv(invPlayer, 8, 103);
	}
}
