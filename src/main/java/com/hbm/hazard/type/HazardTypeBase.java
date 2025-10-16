package com.hbm.hazard.type;

import com.hbm.config.RadiationConfig;
import com.hbm.hazard.modifier.HazardModifier;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public abstract class HazardTypeBase {
	/**
	 * mlbv: I hate this, but I have to do it here
	 * apparently I introduced a double-counting bug in HazardSystem earlier(c8721b7e), but when I fix it, all the hazards halved,
	 * so I must have introduced the halving bug simultaneously with the double-counting bug, but I couldn't find it
	 * perhaps it's introduced along with the async hazard system, idk, but currently I just don't know if there's any other way to fix it
	 */
	public static int hazardRate = RadiationConfig.hazardRate * 2;
	
	/**
	 * Does the thing. Called by HazardEntry.applyHazard
	 * @param target the holder
	 * @param level the final level after calculating all the modifiers
	 */
    public abstract void onUpdate(EntityLivingBase target, double level, ItemStack stack);

	/**
	 * Updates the hazard for dropped items. Used for things like explosive and hydroactive items.
	 * @param item
	 * @param level
	 */
    public abstract void updateEntity(EntityItem item, double level);
	
	/**
	 * Adds item tooltip info. Called by Item.addInformation
	 * @param player
	 * @param list
	 * @param level the base level, mods are passed separately
	 * @param stack
	 * @param modifiers
	 */
	@SideOnly(Side.CLIENT)
    public abstract void addHazardInformation(EntityPlayer player, List<String> list, double level, ItemStack stack, List<HazardModifier> modifiers);

    @FunctionalInterface
    public interface HazardInfoConsumer {
        void accept(EntityPlayer player, List<String> list, double level, ItemStack stack, List<HazardModifier> modifiers);
    }
}
