package com.hbm.tileentity.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;

@AutoRegister
public class TileEntityChlorineSeal extends TileEntity implements ITickable {

	@Override
	public void update() {
		if(!world.isRemote && world.isBlockPowered(pos))
			spread(new BlockPos.MutableBlockPos(pos), 0);
	}
	
	private void spread(MutableBlockPos spread, int index) {
		
		if(index > 50)
			return;
		
		if(world.getBlockState(spread).getBlock().isReplaceable(world, spread))
			world.setBlockState(spread, ModBlocks.chlorine_gas.getDefaultState());
		
		if(world.getBlockState(spread).getBlock() != ModBlocks.chlorine_gas && world.getBlockState(spread).getBlock() != ModBlocks.vent_chlorine_seal)
			return;
		
		switch(world.rand.nextInt(6)) {
		case 0:
			spread(spread.setPos(spread.getX() + 1, spread.getY(), spread.getZ()), index + 1);
			break;
		case 1:
			spread(spread.setPos(spread.getX() - 1, spread.getY(), spread.getZ()), index + 1);
			break;
		case 2:
			spread(spread.setPos(spread.getX(), spread.getY() + 1, spread.getZ()), index + 1);
			break;
		case 3:
			spread(spread.setPos(spread.getX(), spread.getY() - 1, spread.getZ()), index + 1);
			break;
		case 4:
			spread(spread.setPos(spread.getX(), spread.getY(), spread.getZ() + 1), index + 1);
			break;
		case 5:
			spread(spread.setPos(spread.getX(), spread.getY(), spread.getZ() - 1), index + 1);
			break;
		}
	}

}
