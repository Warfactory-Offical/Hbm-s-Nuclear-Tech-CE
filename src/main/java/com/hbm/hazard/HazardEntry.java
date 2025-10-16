package com.hbm.hazard;

import com.hbm.hazard.modifier.HazardModifier;
import com.hbm.hazard.type.HazardTypeBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class HazardEntry {

	HazardTypeBase type;
    double baseLevel;
	
	/*
	 * Modifiers are evaluated in the order they're being applied to the entry.
	 */
	List<HazardModifier> mods = new ArrayList<>();
	
	public HazardEntry(final HazardTypeBase type) {
        this(type, 1D);
	}

    public HazardEntry(final HazardTypeBase type, final double level) {
		this.type = type;
		this.baseLevel = level;
	}
	
	public HazardEntry addMod(final HazardModifier mod) {
		this.mods.add(mod);
		return this;
	}
	
	public void applyHazard(final ItemStack stack, final EntityLivingBase entity) {
		type.onUpdate(entity, HazardModifier.evalAllModifiers(stack, entity, baseLevel, mods), stack);
	}
	
	public HazardEntry clone() {
        return clone(1D);
	}

    public HazardEntry clone(final double mult) {
		final HazardEntry clone = new HazardEntry(type, baseLevel * mult);
		clone.mods = this.mods;
		return clone;
	}
}
