package com.hbm.items.armor;

import com.google.common.collect.Multimap;
import com.hbm.config.PotionConfig;
import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems.Inserts;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemModServos extends ItemArmorMod {

	public ItemModServos(String s){
		super(ArmorModHandler.servos, false, true, true, false, s);
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn){
		if(this == Inserts.servo_set) {
			list.add(TextFormatting.DARK_PURPLE + "Chestplate: Haste I / Damage +50%");
			list.add(TextFormatting.DARK_PURPLE + "Leggings: Speed +25% / Jump II");
		}
		if(this == Inserts.servo_set_desh) {
			list.add(TextFormatting.DARK_PURPLE + "Chestplate: Haste III / Damage +150%");
			list.add(TextFormatting.DARK_PURPLE + "Leggings: Speed +50% / Jump III");
		}
		
		list.add("");
		super.addInformation(stack, worldIn, list, flagIn);
	}
	
	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor){
		ItemArmor item = (ItemArmor)armor.getItem();
		
		if(item.armorType == EntityEquipmentSlot.CHEST) {

			if(this == Inserts.servo_set) {
				list.add(TextFormatting.DARK_PURPLE + "  " + stack.getDisplayName() + " (Haste I / Damage +50%)");
			}
			if(this == Inserts.servo_set_desh) {
				list.add(TextFormatting.DARK_PURPLE + "  " + stack.getDisplayName() + " (Haste III / Damage +150%)");
			}
		}
		
		if(item.armorType == EntityEquipmentSlot.LEGS) {

			if(this == Inserts.servo_set) {
				list.add(TextFormatting.DARK_PURPLE + "  " + stack.getDisplayName() + " (Speed +25% / Jump II)");
			}
			if(this == Inserts.servo_set_desh) {
				list.add(TextFormatting.DARK_PURPLE + "  " + stack.getDisplayName() + " (Speed +50% / Jump III)");
			}
		}
	}
	
	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor){
		ItemArmor item = (ItemArmor)armor.getItem();
		
		if(item.armorType == EntityEquipmentSlot.CHEST) {

			if(this == Inserts.servo_set) {
				entity.addPotionEffect(new PotionEffect(MobEffects.HASTE, 65, 0, false, false));
			}
			if(this == Inserts.servo_set_desh) {
				entity.addPotionEffect(new PotionEffect(MobEffects.HASTE, 65, 2, false, false));
			}
		}
		
		if(PotionConfig.doJumpBoost && item.armorType == EntityEquipmentSlot.LEGS) {

			if(this == Inserts.servo_set) {
				entity.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 65, 1, false, false));
			}
			if(this == Inserts.servo_set_desh) {
				entity.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 65, 2, false, false));
			}
		}
	}
	
	@Override
	public Multimap<String, AttributeModifier> getModifiers(EntityEquipmentSlot slot, ItemStack armor){
		Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, armor);
		
		ItemArmor item = (ItemArmor)armor.getItem();
		
		if(item.armorType == EntityEquipmentSlot.CHEST) {
			if(this == Inserts.servo_set)
				multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
						new AttributeModifier(ArmorModHandler.UUIDs[((ItemArmor)armor.getItem()).armorType.getIndex()], "NTM Armor Mod Servos", 0.5, 2));
			
			if(this == Inserts.servo_set_desh)
				multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
						new AttributeModifier(ArmorModHandler.UUIDs[((ItemArmor)armor.getItem()).armorType.getIndex()], "NTM Armor Mod Servos", 1.5, 2));
		}
		
		if(item.armorType == EntityEquipmentSlot.LEGS) {
			if(this == Inserts.servo_set)
				multimap.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(),
						new AttributeModifier(ArmorModHandler.UUIDs[((ItemArmor)armor.getItem()).armorType.getIndex()], "NTM Armor Mod Servos", 0.25, 2));
			
			if(this == Inserts.servo_set_desh)
				multimap.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(),
						new AttributeModifier(ArmorModHandler.UUIDs[((ItemArmor)armor.getItem()).armorType.getIndex()], "NTM Armor Mod Servos", 0.5, 2));
		}
		
		return multimap;
	}
}
