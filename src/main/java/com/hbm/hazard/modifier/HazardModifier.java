package com.hbm.hazard.modifier;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

import java.util.List;

public abstract class HazardModifier {

    public abstract double modify(ItemStack stack, EntityLivingBase holder, double level);
	
	/**
	 * Returns the level after applying all modifiers to it, in order.
	 * @param stack
	 * @param entity nullable
	 * @param level
	 * @param mods
	 * @return
	 */
    public static double evalAllModifiers(final ItemStack stack, final EntityLivingBase entity, double level, final List<HazardModifier> mods) {
		for(final HazardModifier mod : mods) {
			level = mod.modify(stack, entity, level);
		}
		return level;
	}
}
