package com.hbm.blocks.machine;

import com.hbm.tileentity.machine.TileEntityMachineTapeDrive;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class MachineTapeDrive extends BlockMachineBase {

	public MachineTapeDrive(Material materialIn, String s) {
		super(materialIn, 0, s);
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		return new TileEntityMachineTapeDrive();
	}
}
