package com.hbm.items.special;

import com.hbm.items.machine.ItemBattery;
import com.hbm.lib.HBMSoundHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class ItemPotatos extends ItemBattery {

	public ItemPotatos(long dura, long chargeRate, long dischargeRate, String s) {
		super(dura, chargeRate, dischargeRate, s);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
		if(getCharge(stack) == 0)
    		return;
    	
    	if(getTimer(stack) > 0) {
    		setTimer(stack, getTimer(stack) - 1);
    	} else {
    		if(entity instanceof EntityPlayer) {
    			EntityPlayer p = (EntityPlayer) entity;
    			
    			if(p.getHeldItemMainhand() == stack || p.getHeldItemOffhand() == stack) {
    				
    		    	float pitch = (float)getCharge(stack) / (float)this.getMaxCharge(stack) * 0.5F + 0.5F;
    		    	
    				world.playSound(null, p.posX, p.posY, p.posZ, HBMSoundHandler.potatOSRandom, SoundCategory.PLAYERS, 1.0F, pitch);
    				setTimer(stack, 200 + itemRand.nextInt(100));
    			}
    		}
    	}
	}
	
	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return oldStack.getItem() != newStack.getItem() || oldStack.getMetadata() != newStack.getMetadata();
	}
	
	private static int getTimer(ItemStack stack) {
		if(stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
			return 0;
		}
		
		return stack.getTagCompound().getInteger("timer");
		
	}
	
	private static void setTimer(ItemStack stack, int i) {
		if(stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
		}
		
		stack.getTagCompound().setInteger("timer", i);
		
	}
}
