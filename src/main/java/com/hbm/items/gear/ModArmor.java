package com.hbm.items.gear;

import com.hbm.items.ModItems;
import com.hbm.items.ModItems.ArmorSets;
import com.hbm.lib.RefStrings;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

public class ModArmor extends ItemArmor {

	public ModArmor(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn, String s) {
		super(materialIn, renderIndexIn, equipmentSlotIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setCreativeTab(CreativeTabs.COMBAT);
		
		ModItems.ALL_ITEMS.add(this);
	}
	
	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
		if(stack.getItem().equals(ArmorSets.steel_helmet) || stack.getItem().equals(ArmorSets.steel_plate) || stack.getItem().equals(ArmorSets.steel_boots)) {
			return (RefStrings.MODID + ":textures/armor/steel_1.png");
		}
		if(stack.getItem().equals(ArmorSets.steel_legs)) {
			return (RefStrings.MODID + ":textures/armor/steel_2.png");
		}
		if(stack.getItem().equals(ArmorSets.titanium_helmet) || stack.getItem().equals(ArmorSets.titanium_plate) || stack.getItem().equals(ArmorSets.titanium_boots)) {
			return (RefStrings.MODID + ":textures/armor/titanium_1.png");
		}
		if(stack.getItem().equals(ArmorSets.titanium_legs)) {
			return (RefStrings.MODID + ":textures/armor/titanium_2.png");
		}
		if(stack.getItem().equals(ArmorSets.alloy_helmet) || stack.getItem().equals(ArmorSets.alloy_plate) || stack.getItem().equals(ArmorSets.alloy_boots)) {
			return (RefStrings.MODID + ":textures/armor/alloy_1.png");
		}
		if(stack.getItem().equals(ArmorSets.alloy_legs)) {
			return (RefStrings.MODID + ":textures/armor/alloy_2.png");
		}
		if(stack.getItem().equals(ArmorSets.cmb_helmet) || stack.getItem().equals(ArmorSets.cmb_plate) || stack.getItem().equals(ArmorSets.cmb_boots)) {
			return (RefStrings.MODID + ":textures/armor/cmb_1.png");
		}
		if(stack.getItem().equals(ArmorSets.cmb_legs)) {
			return (RefStrings.MODID + ":textures/armor/cmb_2.png");
		}
		if(stack.getItem().equals(ArmorSets.paa_helmet) || stack.getItem().equals(ArmorSets.paa_plate) || stack.getItem().equals(ArmorSets.paa_boots)) {
			return (RefStrings.MODID + ":textures/armor/paa_1.png");
		}
		if(stack.getItem().equals(ArmorSets.paa_legs)) {
			return (RefStrings.MODID + ":textures/armor/paa_2.png");
		}
		if(stack.getItem().equals(ArmorSets.asbestos_helmet) || stack.getItem().equals(ArmorSets.asbestos_plate) || stack.getItem().equals(ArmorSets.asbestos_boots)) {
			return (RefStrings.MODID + ":textures/armor/asbestos_1.png");
		}
		if(stack.getItem().equals(ArmorSets.asbestos_legs)) {
			return (RefStrings.MODID + ":textures/armor/asbestos_2.png");
		}
		if(stack.getItem().equals(ArmorSets.jackt)) {
			return (RefStrings.MODID + ":textures/armor/jackt.png");
		}
		if(stack.getItem().equals(ArmorSets.jackt2)) {
			return (RefStrings.MODID + ":textures/armor/jackt2.png");
		}
		if(stack.getItem().equals(ArmorSets.security_helmet) || stack.getItem().equals(ArmorSets.security_plate) || stack.getItem().equals(ArmorSets.security_boots)) {
			return (RefStrings.MODID + ":textures/armor/security_1.png");
		}
		if(stack.getItem().equals(ArmorSets.security_legs)) {
			return (RefStrings.MODID + ":textures/armor/security_2.png");
		}
		
		return null;
	}

}
