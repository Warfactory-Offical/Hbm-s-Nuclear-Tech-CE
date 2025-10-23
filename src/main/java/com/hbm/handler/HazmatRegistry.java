package com.hbm.handler;

import com.hbm.items.ModItems;
import com.hbm.items.ModItems.ArmorSets;
import com.hbm.items.armor.ItemModCladding;
import com.hbm.lib.Library;
import com.hbm.potion.HbmPotion;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class HazmatRegistry {
	private static Map<Item, Double> entries = new HashMap<>();

	public static void registerHazmat(Item item, double resistance) {

		entries.put(item, resistance);
	}

	public static double getResistance(ItemStack stack) {

		if(stack == null)
			return 0;

		float cladding = getCladding(stack);

		Double d = entries.get(stack.getItem());

		if(d != null)
			return d + cladding;

		return cladding;
	}

	public static float getCladding(ItemStack stack) {

		if (stack.hasTagCompound() && stack.getTagCompound().getFloat("hfr_cladding") > 0.0F) {
			return stack.getTagCompound().getFloat("hfr_cladding");
		} else {
			if (ArmorModHandler.hasMods(stack)) {
				ItemStack[] mods = ArmorModHandler.pryMods(stack);
				ItemStack cladding = mods[5];
				if (cladding != null && cladding.getItem() instanceof ItemModCladding) {
					return (float)((ItemModCladding)cladding.getItem()).rad;
				}
			}

			return 0.0F;
		}
	}

	public static float getResistance(EntityLivingBase player) {
		float res = 0.0F;

		if (player.getUniqueID().toString().equals(Library.HbMinecraft) || player.getUniqueID().toString().equals(Library.Drillgon) || player.getUniqueID().toString().equals(Library.Alcater)) {
			res += 1.0F;
		}

		for(ItemStack stack : player.getArmorInventoryList()) {
			if(!stack.isEmpty()) {
				res += getResistance(stack);
			}
		}
		PotionEffect radx = player.getActivePotionEffect(HbmPotion.radx);
		if(radx != null)
			res += 0.1F * (1+radx.getAmplifier());

		return res;
	}

	public static double fixRounding(double value){
		return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP).doubleValue();
	}

	public static void registerHazmats() {
		//assuming coefficient of 10
		//real coefficient turned out to be 5
		//oops

		double helmet = 0.2D;
		double chest = 0.4D;
		double legs = 0.3D;
		double boots = 0.1D;

		double iron = 0.0225D; // 5%
		double gold = 0.03D; // 5%
		double steel = 0.045D; // 10%
		double titanium = 0.045D; // 10%
		double alloy = 0.07D; // 15%
		double cobalt = 0.125D; // 25%

		double hazYellow = 0.6D; // 75%
		double hazRed = 1.0D; // 90%
		double hazGray = 2D; // 99%
		double liquidator = 2.4D; // 99.6%
		double paa = 3.0D; // 99.9%
		

		double t51 = 1D; // 90%
		double ajr = 1.3D; // 95%
		double hev = 1.6D; // 97.5%
		double bj = 1D; // 90%
		double rpa = 2D; // 99%
		double fau = 4D; // 99.99%
		double dns = 6D; // 99.9999%
		double security = 0.01D; // 2.3%
		double star = 0.25D; // 44%
		double cmb = 1.3D; // 95%
		double schrab = 3D; // 99.9%
		double euph = 10D; // 99.99999999%

		HazmatRegistry.registerHazmat(ArmorSets.hazmat_helmet, fixRounding(hazYellow * helmet));
		HazmatRegistry.registerHazmat(ArmorSets.hazmat_plate, fixRounding(hazYellow * chest));
		HazmatRegistry.registerHazmat(ArmorSets.hazmat_legs, fixRounding(hazYellow * legs));
		HazmatRegistry.registerHazmat(ArmorSets.hazmat_boots, fixRounding(hazYellow * boots));

		HazmatRegistry.registerHazmat(ArmorSets.hazmat_helmet_red, fixRounding(hazRed * helmet));
		HazmatRegistry.registerHazmat(ArmorSets.hazmat_plate_red, fixRounding(hazRed * chest));
		HazmatRegistry.registerHazmat(ArmorSets.hazmat_legs_red, fixRounding(hazRed * legs));
		HazmatRegistry.registerHazmat(ArmorSets.hazmat_boots_red, fixRounding(hazRed * boots));

		HazmatRegistry.registerHazmat(ArmorSets.hazmat_helmet_grey, fixRounding(hazGray * helmet));
		HazmatRegistry.registerHazmat(ArmorSets.hazmat_plate_grey, fixRounding(hazGray * chest));
		HazmatRegistry.registerHazmat(ArmorSets.hazmat_legs_grey, fixRounding(hazGray * legs));
		HazmatRegistry.registerHazmat(ArmorSets.hazmat_boots_grey, fixRounding(hazGray * boots));

		HazmatRegistry.registerHazmat(ArmorSets.liquidator_helmet, fixRounding(liquidator * helmet));
		HazmatRegistry.registerHazmat(ArmorSets.liquidator_plate, fixRounding(liquidator * chest));
		HazmatRegistry.registerHazmat(ArmorSets.liquidator_legs, fixRounding(liquidator * legs));
		HazmatRegistry.registerHazmat(ArmorSets.liquidator_boots, fixRounding(liquidator * boots));

		HazmatRegistry.registerHazmat(ArmorSets.t51_helmet, fixRounding(t51 * helmet));
		HazmatRegistry.registerHazmat(ArmorSets.t51_plate, fixRounding(t51 * chest));
		HazmatRegistry.registerHazmat(ArmorSets.t51_legs, fixRounding(t51 * legs));
		HazmatRegistry.registerHazmat(ArmorSets.t51_boots, fixRounding(t51 * boots));

		HazmatRegistry.registerHazmat(ArmorSets.steamsuit_helmet, 1.3 * helmet);
		HazmatRegistry.registerHazmat(ArmorSets.steamsuit_plate, 1.3 * chest);
		HazmatRegistry.registerHazmat(ArmorSets.steamsuit_legs, 1.3 * legs);
		HazmatRegistry.registerHazmat(ArmorSets.steamsuit_boots, 1.3 * boots);

		HazmatRegistry.registerHazmat(ArmorSets.ajr_helmet, fixRounding(ajr * helmet));
		HazmatRegistry.registerHazmat(ArmorSets.ajr_plate, fixRounding(ajr * chest));
		HazmatRegistry.registerHazmat(ArmorSets.ajr_legs, fixRounding(ajr * legs));
		HazmatRegistry.registerHazmat(ArmorSets.ajr_boots, fixRounding(ajr * boots));

		HazmatRegistry.registerHazmat(ArmorSets.ajro_helmet, fixRounding(ajr * helmet));
		HazmatRegistry.registerHazmat(ArmorSets.ajro_plate, fixRounding(ajr * chest));
		HazmatRegistry.registerHazmat(ArmorSets.ajro_legs, fixRounding(ajr * legs));
		HazmatRegistry.registerHazmat(ArmorSets.ajro_boots, fixRounding(ajr * boots));

		HazmatRegistry.registerHazmat(ArmorSets.rpa_helmet, fixRounding(rpa * helmet));
		HazmatRegistry.registerHazmat(ArmorSets.rpa_plate, fixRounding(rpa * chest));
		HazmatRegistry.registerHazmat(ArmorSets.rpa_legs, fixRounding(rpa * legs));
		HazmatRegistry.registerHazmat(ArmorSets.rpa_boots, fixRounding(rpa * boots));

		HazmatRegistry.registerHazmat(ArmorSets.bj_helmet, fixRounding(bj * helmet));
		HazmatRegistry.registerHazmat(ArmorSets.bj_plate, fixRounding(bj * chest));
		HazmatRegistry.registerHazmat(ArmorSets.bj_plate_jetpack, fixRounding(bj * chest));
		HazmatRegistry.registerHazmat(ArmorSets.bj_legs, fixRounding(bj * legs));
		HazmatRegistry.registerHazmat(ArmorSets.bj_boots, fixRounding(bj * boots));

		HazmatRegistry.registerHazmat(ArmorSets.hev_helmet, fixRounding(hev * helmet));
		HazmatRegistry.registerHazmat(ArmorSets.hev_plate, fixRounding(hev * chest));
		HazmatRegistry.registerHazmat(ArmorSets.hev_legs, fixRounding(hev * legs));
		HazmatRegistry.registerHazmat(ArmorSets.hev_boots, fixRounding(hev * boots));
		
		HazmatRegistry.registerHazmat(ArmorSets.fau_helmet, fixRounding(fau * helmet));
		HazmatRegistry.registerHazmat(ArmorSets.fau_plate, fixRounding(fau * chest));
		HazmatRegistry.registerHazmat(ArmorSets.fau_legs, fixRounding(fau * legs));
		HazmatRegistry.registerHazmat(ArmorSets.fau_boots, fixRounding(fau * boots));

		HazmatRegistry.registerHazmat(ArmorSets.dns_helmet, fixRounding(dns * helmet));
		HazmatRegistry.registerHazmat(ArmorSets.dns_plate, fixRounding(dns * chest));
		HazmatRegistry.registerHazmat(ArmorSets.dns_legs, fixRounding(dns * legs));
		HazmatRegistry.registerHazmat(ArmorSets.dns_boots, fixRounding(dns * boots));

		HazmatRegistry.registerHazmat(ArmorSets.paa_helmet, fixRounding(paa * helmet));
		HazmatRegistry.registerHazmat(ArmorSets.paa_plate, fixRounding(paa * chest));
		HazmatRegistry.registerHazmat(ArmorSets.paa_legs, fixRounding(paa * legs));
		HazmatRegistry.registerHazmat(ArmorSets.paa_boots, fixRounding(paa * boots));

		HazmatRegistry.registerHazmat(ArmorSets.hazmat_paa_helmet, fixRounding(paa * helmet));
		HazmatRegistry.registerHazmat(ArmorSets.hazmat_paa_plate, fixRounding(paa * chest));
		HazmatRegistry.registerHazmat(ArmorSets.hazmat_paa_legs, fixRounding(paa * legs));
		HazmatRegistry.registerHazmat(ArmorSets.hazmat_paa_boots, fixRounding(paa * boots));

		HazmatRegistry.registerHazmat(ArmorSets.security_helmet, fixRounding(security * helmet));
		HazmatRegistry.registerHazmat(ArmorSets.security_plate, fixRounding(security * chest));
		HazmatRegistry.registerHazmat(ArmorSets.security_legs, fixRounding(security * legs));
		HazmatRegistry.registerHazmat(ArmorSets.security_boots, fixRounding(security * boots));

		HazmatRegistry.registerHazmat(ArmorSets.starmetal_helmet, fixRounding(star * helmet));
		HazmatRegistry.registerHazmat(ArmorSets.starmetal_plate, fixRounding(star * chest));
		HazmatRegistry.registerHazmat(ArmorSets.starmetal_legs, fixRounding(star * legs));
		HazmatRegistry.registerHazmat(ArmorSets.starmetal_boots, fixRounding(star * boots));

		HazmatRegistry.registerHazmat(ArmorSets.jackt, 0.1);
		HazmatRegistry.registerHazmat(ArmorSets.jackt2, 0.1);

		HazmatRegistry.registerHazmat(ModItems.gas_mask, 0.07);
		HazmatRegistry.registerHazmat(ModItems.gas_mask_m65, 0.095);

		HazmatRegistry.registerHazmat(ArmorSets.steel_helmet, fixRounding(steel * helmet));
		HazmatRegistry.registerHazmat(ArmorSets.steel_plate, fixRounding(steel * chest));
		HazmatRegistry.registerHazmat(ArmorSets.steel_legs, fixRounding(steel * legs));
		HazmatRegistry.registerHazmat(ArmorSets.steel_boots, fixRounding(steel * boots));

		HazmatRegistry.registerHazmat(ArmorSets.titanium_helmet, fixRounding(titanium * helmet));
		HazmatRegistry.registerHazmat(ArmorSets.titanium_plate, fixRounding(titanium * chest));
		HazmatRegistry.registerHazmat(ArmorSets.titanium_legs, fixRounding(titanium * legs));
		HazmatRegistry.registerHazmat(ArmorSets.titanium_boots, fixRounding(titanium * boots));

		HazmatRegistry.registerHazmat(ArmorSets.cobalt_helmet, fixRounding(cobalt * helmet));
		HazmatRegistry.registerHazmat(ArmorSets.cobalt_plate, fixRounding(cobalt * chest));
		HazmatRegistry.registerHazmat(ArmorSets.cobalt_legs, fixRounding(cobalt * legs));
		HazmatRegistry.registerHazmat(ArmorSets.cobalt_boots, fixRounding(cobalt * boots));

		HazmatRegistry.registerHazmat(Items.IRON_HELMET, fixRounding(iron * helmet));
		HazmatRegistry.registerHazmat(Items.IRON_CHESTPLATE, fixRounding(iron * chest));
		HazmatRegistry.registerHazmat(Items.IRON_LEGGINGS, fixRounding(iron * legs));
		HazmatRegistry.registerHazmat(Items.IRON_BOOTS, fixRounding(iron * boots));

		HazmatRegistry.registerHazmat(Items.GOLDEN_HELMET, fixRounding(gold * helmet));
		HazmatRegistry.registerHazmat(Items.GOLDEN_CHESTPLATE, fixRounding(gold * chest));
		HazmatRegistry.registerHazmat(Items.GOLDEN_LEGGINGS, fixRounding(gold * legs));
		HazmatRegistry.registerHazmat(Items.GOLDEN_BOOTS, fixRounding(gold * boots));

		HazmatRegistry.registerHazmat(ArmorSets.alloy_helmet, fixRounding(alloy * helmet));
		HazmatRegistry.registerHazmat(ArmorSets.alloy_plate, fixRounding(alloy * chest));
		HazmatRegistry.registerHazmat(ArmorSets.alloy_legs, fixRounding(alloy * legs));
		HazmatRegistry.registerHazmat(ArmorSets.alloy_boots, fixRounding(alloy * boots));

		HazmatRegistry.registerHazmat(ArmorSets.cmb_helmet, fixRounding(cmb * helmet));
		HazmatRegistry.registerHazmat(ArmorSets.cmb_plate, fixRounding(cmb * chest));
		HazmatRegistry.registerHazmat(ArmorSets.cmb_legs, fixRounding(cmb * legs));
		HazmatRegistry.registerHazmat(ArmorSets.cmb_boots, fixRounding(cmb * boots));

		HazmatRegistry.registerHazmat(ArmorSets.schrabidium_helmet, fixRounding(schrab * helmet));
		HazmatRegistry.registerHazmat(ArmorSets.schrabidium_plate, fixRounding(schrab * chest));
		HazmatRegistry.registerHazmat(ArmorSets.schrabidium_legs, fixRounding(schrab * legs));
		HazmatRegistry.registerHazmat(ArmorSets.schrabidium_boots, fixRounding(schrab * boots));

		HazmatRegistry.registerHazmat(ArmorSets.euphemium_helmet, fixRounding(euph * helmet));
		HazmatRegistry.registerHazmat(ArmorSets.euphemium_plate, fixRounding(euph * chest));
		HazmatRegistry.registerHazmat(ArmorSets.euphemium_legs, fixRounding(euph * legs));
		HazmatRegistry.registerHazmat(ArmorSets.euphemium_boots, fixRounding(euph * boots));
	}
}
