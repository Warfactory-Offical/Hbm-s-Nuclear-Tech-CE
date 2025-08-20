package com.hbm.items.special;

import com.hbm.items.ItemBakedBase;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemHot extends ItemBakedBase {

	public static int heat;
	
	public ItemHot(int heat, String s) {
		super(s);
		ItemHot.heat = heat;
	}
	
	public static ItemStack heatUp(ItemStack stack) {

		if(!(stack.getItem() instanceof ItemHot))
			return stack;

		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setInteger("heat", heat);
		return stack;
	}

	public static ItemStack heatUp(ItemStack stack, double d) {

		if(!(stack.getItem() instanceof ItemHot))
			return stack;

		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setInteger("heat", (int)(d * heat));
		return stack;
	}
	
	public static double getHeat(ItemStack stack) {

		if(!(stack.getItem() instanceof ItemHot))
			return 0;

		if(!stack.hasTagCompound())
			return 0;

		int h = stack.getTagCompound().getInteger("heat");

		return (double)h / (double)heat;
	}
	
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
		if(!world.isRemote && stack.hasTagCompound()) {

    		int h = stack.getTagCompound().getInteger("heat");

    		if(h > 0) {
    			stack.getTagCompound().setInteger("heat", h - 1);
    		} else {
    			stack.getTagCompound().removeTag("heat");
    		}
    	}
	}
	
}
