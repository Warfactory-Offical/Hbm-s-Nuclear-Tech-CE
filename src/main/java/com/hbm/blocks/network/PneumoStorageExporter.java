package com.hbm.blocks.network;

import com.hbm.blocks.machine.BlockMachineBase;
import com.hbm.tileentity.network.TileEntityPneumoStorageExporter;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class PneumoStorageExporter extends BlockMachineBase {

	public PneumoStorageExporter(Material materialIn, String s) {
		super(materialIn, 0, s);
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		return new TileEntityPneumoStorageExporter();
	}
}
