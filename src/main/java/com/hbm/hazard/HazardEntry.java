package com.hbm.hazard;

import com.hbm.hazard.modifier.IHazardModifier;
import com.hbm.hazard.type.IHazardType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class HazardEntry {

	IHazardType type;
    double baseLevel;
	
	/*
	 * Modifiers are evaluated in the order they're being applied to the entry.
	 */
	List<IHazardModifier> mods = new ArrayList<>();
	
	public HazardEntry(final IHazardType type) {
        this(type, 1D);
	}

    public HazardEntry(final IHazardType type, final double level) {
		this.type = type;
		this.baseLevel = level;
	}
	
	public HazardEntry addMod(final IHazardModifier mod) {
		this.mods.add(mod);
		return this;
	}
	
	public void applyHazard(final ItemStack stack, final EntityLivingBase entity) {
		type.onUpdate(entity, IHazardModifier.evalAllModifiers(stack, entity, baseLevel, mods), stack);
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
