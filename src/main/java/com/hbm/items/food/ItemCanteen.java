package com.hbm.items.food;

import com.hbm.config.VersatileConfig;
import com.hbm.items.ModItems;
import com.hbm.items.ModItems.Foods;
import com.hbm.main.MainRegistry;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import java.util.List;

public class ItemCanteen extends Item {

	public ItemCanteen(int cooldown, String s) {
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setMaxDamage(cooldown);
		
		ModItems.ALL_ITEMS.add(this);
	}
	
	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		if (stack.getItemDamage() > 0 && entityIn.ticksExisted % 20 == 0)
			stack.setItemDamage(stack.getItemDamage() - 1);
	}
	
	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
		stack.setItemDamage(stack.getMaxDamage());

		if (this == Foods.canteen_13) {
			entityLiving.heal(5F);
		}
		if (this == Foods.canteen_vodka) {
			entityLiving.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 10 * 20, 0));
			entityLiving.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 30 * 20, 2));
		}
		if (this == Foods.canteen_fab) {
			entityLiving.heal(10F);
			entityLiving.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 15 * 20, 0));
			entityLiving.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 60 * 20, 2));
			entityLiving.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 60 * 20, 2));
			entityLiving.addPotionEffect(new PotionEffect(MobEffects.SPEED, 60 * 20, 1));
		}

		VersatileConfig.applyPotionSickness(entityLiving, 5);
		
		return stack;
	}
	
	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 10;
	}
	
	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.DRINK;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		if(VersatileConfig.hasPotionSickness(playerIn))
			return super.onItemRightClick(worldIn, playerIn, handIn);
		if(playerIn.getHeldItem(handIn).getItemDamage() == 0 && !VersatileConfig.hasPotionSickness(playerIn))
			playerIn.setActiveHand(handIn);
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if(this == Foods.canteen_13)
    	{
			tooltip.add("Cooldown: 1 minute");
			tooltip.add("Restores 2.5 hearts");
			tooltip.add("");
			
    		if(MainRegistry.polaroidID == 11)
    			tooltip.add("You sip a sip from your trusty Vault 13 SIPPP");
    		else
    			tooltip.add("You take a sip from your trusty Vault 13 canteen.");
    	}
    	if(this == Foods.canteen_vodka)
    	{
    		tooltip.add("Cooldown: 3 minutes");
    		tooltip.add("Nausea I for 10 seconds");
			tooltip.add("Strength III for 30 seconds");
			tooltip.add("");
			
    		if(MainRegistry.polaroidID == 11)
    			//list.add("Why sipp when you can succ?");
    			tooltip.add("Time to get hammered & sickled!");
    		else
    			tooltip.add("Smells like disinfectant, tastes like disinfectant.");
    	}
    	if(this == Foods.canteen_fab)
    	{
    		tooltip.add("Cooldown: 2 minutes");
    		tooltip.add("Engages the fab drive");
    	}
	}
}
