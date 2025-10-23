package com.hbm.items.armor;

import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems.Inserts;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemModRevive extends ItemArmorMod {

	public ItemModRevive(int durability, String s) {
		super(ArmorModHandler.extra, false, false, true, false, s);
		this.setMaxDamage(durability);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn){
		if(this == Inserts.scrumpy) {
			list.add(TextFormatting.GOLD + "But how did you survive?");
			list.add(TextFormatting.RED + "I was drunk.");
		}
		if(this == Inserts.wild_p) {
			list.add(TextFormatting.DARK_GRAY + "Explosive " + TextFormatting.RED + "Reactive " + TextFormatting.DARK_GRAY + "Plot " + TextFormatting.RED + "Armor");
		}
		if(this == Inserts.fabsols_vodka) {
			list.add(TextFormatting.ITALIC + "In the news:");
			list.add(TextFormatting.RED + "" + TextFormatting.BOLD + "Man literally too angry to die.");
			list.add("");
			list.add(TextFormatting.ITALIC + "\"I ain't got time to die\" says local");
			list.add(TextFormatting.ITALIC + "man after ripping the physical manifestation");
			list.add(TextFormatting.ITALIC + "of disaster itself in half.");
		}
		
		list.add("");
		list.add(TextFormatting.GOLD + "" + (stack.getMaxDamage() - stack.getItemDamage()) + " revives left");
		list.add("");
		super.addInformation(stack, worldIn, list, flagIn);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.GOLD + "  " + stack.getDisplayName() + " (" + (stack.getMaxDamage() - stack.getItemDamage()) + " revives left)");
	}
}