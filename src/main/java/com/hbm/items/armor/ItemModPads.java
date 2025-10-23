package com.hbm.items.armor;

import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems.Inserts;
import com.hbm.items.gear.ArmorFSB;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;

public class ItemModPads extends ItemArmorMod {

	float damageMod;
	
	public ItemModPads(float damageMod, String s) {
		super(ArmorModHandler.boots_only, false, false, false, true, s);
		this.damageMod = damageMod;
	}
	
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn){
		if(damageMod != 1F)
			list.add(TextFormatting.RED + "-" + Math.round((1F - damageMod) * 100) + "% fall damage");
		
		if(this == Inserts.pads_static)
			list.add(TextFormatting.DARK_PURPLE + "Passively charges electric armor when walking");
		
		list.add("");
		super.addInformation(stack, worldIn, list, flagIn);
	}
	
	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		
		if(this == Inserts.pads_static)
			list.add(TextFormatting.DARK_PURPLE + "  " + stack.getDisplayName() + " (-" + Math.round((1F - damageMod) * 100) + "% fall dmg / passive charge)");
		else
			list.add(TextFormatting.DARK_PURPLE + "  " + stack.getDisplayName() + " (-" + Math.round((1F - damageMod) * 100) + "% fall dmg)");
	}

	@Override
	public void modDamage(LivingHurtEvent event, ItemStack armor) {
		
		if(event.getSource() == DamageSource.FALL)
			event.setAmount(event.getAmount() * damageMod);
	}
	
	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {
		
		if(!entity.world.isRemote && this == Inserts.pads_static && entity instanceof EntityPlayer) {
			
			EntityPlayer player = (EntityPlayer) entity;
			
			if(player.distanceWalkedModified != player.prevDistanceWalkedModified) {
				
				if(ArmorFSB.hasFSBArmorIgnoreCharge(player)) {
					
					for(int i = 0; i < 4; i++) {
						
						ItemStack stack = player.inventory.armorInventory.get(i);
						
						if(stack.getItem() instanceof ArmorFSBPowered powered) {

                            long charge = powered.drain / 2;
							
							if(charge == 0)
								charge = powered.consumption / 40;
							
							long power = Math.min(powered.getMaxCharge(stack), powered.getCharge(stack) + charge);
							powered.setCharge(stack, power);
						}
					}
				}
			}
		}
	}
}