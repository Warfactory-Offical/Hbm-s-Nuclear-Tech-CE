package com.hbm.inventory.container;

import com.hbm.tileentity.network.TileEntityPneumoStorageMono;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerPneumoStorageMono extends ContainerBase {

	public ContainerPneumoStorageMono(InventoryPlayer invPlayer, TileEntityPneumoStorageMono mono) {
		super(invPlayer, mono.inventory);

		addSlots(mono.inventory, 0, 62, 35, 1, 3);
		playerInv(invPlayer, 8, 103);
	}
}
