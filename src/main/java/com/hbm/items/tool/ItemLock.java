package com.hbm.items.tool;

import com.hbm.blocks.BlockDummyable;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.tileentity.machine.TileEntityDummy;
import com.hbm.tileentity.machine.TileEntityLockableBase;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemLock extends ItemKeyPin {

	public double lockMod = 0.1D;
	
	public ItemLock(double mod, String s) {
		super(s);
		lockMod = mod;
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);
		if(!world.isRemote && getPins(stack) != 0) {
			TileEntity thing;
			Block b = world.getBlockState(pos).getBlock();
			
			if(b instanceof BlockDummyable){
				int[] cpos = ((BlockDummyable)b).findCore(world, pos.getX(), pos.getY(), pos.getZ());
				if(cpos == null)
					return EnumActionResult.FAIL;
				thing = world.getTileEntity(new BlockPos(cpos[0], cpos[1], cpos[2]));
			} else {
				thing = world.getTileEntity(pos);
			}
			
			if(thing != null && thing instanceof TileEntityDummy && ((TileEntityDummy)thing).target != null){
				thing = world.getTileEntity(((TileEntityDummy)thing).target);
			}


			if(thing != null && thing instanceof TileEntityLockableBase) {
				TileEntityLockableBase lockTe = (TileEntityLockableBase) thing;

				if(lockTe != null && lockTe instanceof TileEntityLockableBase) {
					if(!lockTe.isLocked() && lockTe.canLock(player, hand, facing)) {
						lockTe.setPins(getPins(stack));
						lockTe.lock();
						lockTe.setMod(this.lockMod);
						world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ, HBMSoundHandler.lockHang, SoundCategory.PLAYERS, 1.0F, 1.0F);
						stack.shrink(1);
						return EnumActionResult.SUCCESS;
					}

					return EnumActionResult.FAIL;
				}
			}
		}
		
		return EnumActionResult.PASS;
	}
}
