package com.hbm.creativetabs;

import com.hbm.items.ModItems;
import com.hbm.items.ModItems.MissileParts;
import com.hbm.items.weapon.ItemCustomMissile;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MissileTab extends CreativeTabs {

	public MissileTab(int index, String label) {
		super(index, label);
	}

	@Override
    @SideOnly(Side.CLIENT)
	public ItemStack createIcon() {
		if(ModItems.missile_nuclear != null){
			return new ItemStack(ModItems.missile_nuclear);
		}
		return new ItemStack(Items.IRON_PICKAXE);
	}

	@Override
    @SideOnly(Side.CLIENT)
	public void displayAllRelevantItems(NonNullList<ItemStack> list) {
		super.displayAllRelevantItems(list);
		
		list.add(ItemCustomMissile.buildMissile(
				MissileParts.mp_chip_3,
				MissileParts.mp_warhead_10_he,
				MissileParts.mp_fuselage_10_kerosene,
				MissileParts.mp_stability_10_flat,
				MissileParts.mp_thruster_10_kerosene).setStackDisplayName(TextFormatting.DARK_PURPLE + "Lil Bub"));
		
		list.add(ItemCustomMissile.buildMissile(
				MissileParts.mp_chip_3,
				MissileParts.mp_warhead_10_incendiary,
				MissileParts.mp_fuselage_10_long_solid,
				MissileParts.mp_stability_10_space,
				MissileParts.mp_thruster_10_solid).setStackDisplayName(TextFormatting.DARK_PURPLE + "Long Boy"));
		
		list.add(ItemCustomMissile.buildMissile(
				MissileParts.mp_chip_3,
				MissileParts.mp_warhead_10_nuclear,
				MissileParts.mp_fuselage_10_15_kerosene,
				MissileParts.mp_stability_15_flat,
				MissileParts.mp_thruster_15_kerosene).setStackDisplayName(TextFormatting.DARK_PURPLE + "Uncle Kim"));
		
		list.add(ItemCustomMissile.buildMissile(
				MissileParts.mp_chip_3,
				MissileParts.mp_warhead_10_nuclear_large,
				MissileParts.mp_fuselage_10_15_balefire,
				MissileParts.mp_stability_15_flat,
				MissileParts.mp_thruster_15_balefire_large).setStackDisplayName(TextFormatting.GREEN + "Trotty's Toy Rocket"));
		
		list.add(ItemCustomMissile.buildMissile(
				MissileParts.mp_chip_3,
				MissileParts.mp_warhead_15_nuclear_shark,
				MissileParts.mp_fuselage_15_kerosene_camo,
				MissileParts.mp_stability_15_thin,
				MissileParts.mp_thruster_15_kerosene_triple).setStackDisplayName(TextFormatting.DARK_PURPLE + "Stealthy Shark"));
		
		list.add(ItemCustomMissile.buildMissile(
				MissileParts.mp_chip_3,
				MissileParts.mp_warhead_15_he,
				MissileParts.mp_fuselage_15_kerosene_polite,
				MissileParts.mp_stability_15_thin,
				MissileParts.mp_thruster_15_kerosene_dual).setStackDisplayName(TextFormatting.DARK_PURPLE + "Polite Lad"));
		
		list.add(ItemCustomMissile.buildMissile(
				MissileParts.mp_chip_3,
				MissileParts.mp_warhead_15_n2,
				MissileParts.mp_fuselage_15_solid_desh,
				MissileParts.mp_stability_15_thin,
				MissileParts.mp_thruster_15_solid_hexdecuple).setStackDisplayName(TextFormatting.DARK_PURPLE + "NERV's Leftover Missile"));
		
		list.add(ItemCustomMissile.buildMissile(
				MissileParts.mp_chip_5,
				MissileParts.mp_warhead_15_mirv,
				MissileParts.mp_fuselage_15_kerosene_lambda,
				MissileParts.mp_stability_15_soyuz,
				MissileParts.mp_thruster_15_kerosene).setStackDisplayName(TextFormatting.BLUE + "7 For 1 Package Deal"));
		
		list.add(ItemCustomMissile.buildMissile(
				MissileParts.mp_chip_4,
				MissileParts.mp_warhead_15_balefire,
				MissileParts.mp_fuselage_15_20_kerosene_magnusson,
				null,
				MissileParts.mp_thruster_20_kerosene).setStackDisplayName(TextFormatting.GREEN + "Hightower Missile"));
		
	}
}
