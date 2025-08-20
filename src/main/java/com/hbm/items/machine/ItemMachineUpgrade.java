package com.hbm.items.machine;

import com.google.common.collect.Sets;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.ItemBakedBase;
import com.hbm.items.ModItems;
import com.hbm.util.I18nUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;
import java.util.Set;

public class ItemMachineUpgrade extends ItemBakedBase {
	public UpgradeType type;
	public int tier;

	public ItemMachineUpgrade(String s) {
		this(s, UpgradeType.SPECIAL, 0);
	}

	public ItemMachineUpgrade(String s, UpgradeType type) {
		this(s, type, 0);
	}

	public ItemMachineUpgrade(String s, UpgradeType type, int tier) {
		super(s);
		this.type = type;
		this.tier = tier;
	}

	public int getSpeed(){
		if(this == ModItems.upgrade_speed_1) return 1;
		if(this == ModItems.upgrade_speed_2) return 2;
		if(this == ModItems.upgrade_speed_3) return 3;
		if(this == ModItems.upgrade_overdrive_1) return 4;
		if(this == ModItems.upgrade_overdrive_2) return 6;
		if(this == ModItems.upgrade_overdrive_3) return 8;
		if(this == ModItems.upgrade_screm) return 10;
		return 0;
	}

	public static int getSpeed(ItemStack stack){
		if(stack == null || stack.isEmpty()) return 0;
		Item upgrade = stack.getItem();
		if(upgrade == ModItems.upgrade_speed_1) return 1;
		if(upgrade == ModItems.upgrade_speed_2) return 2;
		if(upgrade == ModItems.upgrade_speed_3) return 3;
		if(upgrade == ModItems.upgrade_overdrive_1) return 4;
		if(upgrade == ModItems.upgrade_overdrive_2) return 6;
		if(upgrade == ModItems.upgrade_overdrive_3) return 8;
		if(upgrade == ModItems.upgrade_screm) return 10;
		return 0;
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
		if(this == ModItems.upgrade_speed_1)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade1"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradesp11"));
			list.add("");
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade2"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradesp12"));
			list.add("");
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade3"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradesp13"));
			list.add("");
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade4"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradesp14"));
			list.add("");
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade5"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradesp15"));
		}
		
		if(this == ModItems.upgrade_speed_2)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade1"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradesp21"));
			list.add("");
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade2"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradesp22"));
			list.add("");
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade3"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradesp23"));
			list.add("");
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade4"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradesp24"));
			list.add("");
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade5"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradesp25"));
		}

		if(this == ModItems.upgrade_speed_3)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade1"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradesp31"));
			list.add("");
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade2"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradesp32"));
			list.add("");
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade3"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradesp33"));
			list.add("");
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade4"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradesp34"));
			list.add("");
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade5"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradesp35"));
		}

		if(this == ModItems.upgrade_effect_1)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade1"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradeef11"));
			list.add("");
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade4"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradeef12"));
			list.add("");
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade5"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradeef13"));
		}

		if(this == ModItems.upgrade_effect_2)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade1"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradeef21"));
			list.add("");
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade4"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradeef22"));
			list.add("");
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade5"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradeef23"));
		}

		if(this == ModItems.upgrade_effect_3)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade1"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradeef31"));
			list.add("");
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade4"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradeef32"));
			list.add("");
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade5"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradeef33"));
		}

		if(this == ModItems.upgrade_power_1)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade2"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradecs11"));
			list.add("");
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade3"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradecs11"));
			list.add("");
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade5"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradecs12"));
		}

		if(this == ModItems.upgrade_power_2)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade2"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradecs21"));
			list.add("");
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade3"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradecs21"));
			list.add("");
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade5"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradecs22"));
		}

		if(this == ModItems.upgrade_power_3)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade2"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradecs31"));
			list.add("");
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade3"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradecs31"));
			list.add("");
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade5"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradecs32"));
		}

		if(this == ModItems.upgrade_fortune_1)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade1"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradeft1"));
		}

		if(this == ModItems.upgrade_fortune_2)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade1"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradeft2"));
		}

		if(this == ModItems.upgrade_fortune_3)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade1"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradeft3"));
		}

		if(this == ModItems.upgrade_afterburn_1)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade6"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradeaf1"));
		}

		if(this == ModItems.upgrade_afterburn_2)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade6"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradeaf2"));
		}

		if(this == ModItems.upgrade_afterburn_3)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade6"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradeaf3"));
		}

		if(this == ModItems.upgrade_radius)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade7"));
			list.add(" "+I18nUtil.resolveKey("desc.upgraderd"));
			list.add("");
			list.add(" "+I18nUtil.resolveKey("desc.upgradestack"));
		}

		if(this == ModItems.upgrade_health)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade8"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradeht"));
			list.add("");
			list.add(" "+I18nUtil.resolveKey("desc.upgradestack"));
		}
		
		if(this == ModItems.upgrade_smelter)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade9"));
			list.add(" "+I18nUtil.resolveKey("desc.upgrade12"));
		}

		if(this == ModItems.upgrade_shredder)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade9"));
			list.add(" "+I18nUtil.resolveKey("desc.upgrade13"));
		}

		if(this == ModItems.upgrade_centrifuge)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade9"));
			list.add(" "+I18nUtil.resolveKey("desc.upgrade21"));
		}

		if(this == ModItems.upgrade_crystallizer)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade9"));
			list.add(" "+I18nUtil.resolveKey("desc.upgrade14"));
		}

		if(this == ModItems.upgrade_screm)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade9"));
			list.add(" "+I18nUtil.resolveKey("desc.upgrade15"));
			list.add(" "+I18nUtil.resolveKey("desc.upgrade16"));
			list.add(" "+I18nUtil.resolveKey("desc.upgrade17"));
			list.add("");
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade11"));
			list.add(" "+I18nUtil.resolveKey("desc.upgrade18"));
		}
		
		if(this == ModItems.upgrade_nullifier)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade10"));
			list.add(" "+I18nUtil.resolveKey("desc.upgrade19"));
		}

		if(this == ModItems.upgrade_ejector_1)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade22"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradeej1"));
		}

		if(this == ModItems.upgrade_ejector_2)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade22"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradeej2"));
		}

		if(this == ModItems.upgrade_ejector_3)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade22"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradeej3"));
		}

		if(this == ModItems.upgrade_stack_1)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade22"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradeej4"));
		}

		if(this == ModItems.upgrade_stack_2)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade22"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradeej5"));
		}

		if(this == ModItems.upgrade_stack_3)
		{
			list.add(TextFormatting.GOLD+I18nUtil.resolveKey("desc.upgrade22"));
			list.add(" "+I18nUtil.resolveKey("desc.upgradeej6"));
		}
		// I'm not translating this shit for now
		if(this == ModItems.upgrade_gc_speed) {
			list.add(TextFormatting.RED + "Gas Centrifuge Upgrade");
			list.add("Allows for total isotopic separation of HEUF6");
			list.add(TextFormatting.YELLOW + "also your centrifuge goes sicko mode");
		}
	}

	public static final Set<Item> scrapItems = Sets.newHashSet(Item.getItemFromBlock(Blocks.GRASS),
			Item.getItemFromBlock(Blocks.DIRT),
			Item.getItemFromBlock(Blocks.STONE),
			Item.getItemFromBlock(Blocks.COBBLESTONE),
			Item.getItemFromBlock(Blocks.SAND),
			Item.getItemFromBlock(Blocks.SANDSTONE),
			Item.getItemFromBlock(Blocks.GRAVEL),
			Item.getItemFromBlock(Blocks.NETHERRACK),
			Item.getItemFromBlock(Blocks.END_STONE),
			Item.getItemFromBlock(ModBlocks.stone_gneiss),
			Items.FLINT,
			Items.SNOWBALL,
			Items.WHEAT_SEEDS,
			Items.STICK);

	public enum UpgradeType {
		SPEED,
		EFFECT,
		POWER,
		FORTUNE,
		AFTERBURN,
		OVERDRIVE,
		NULLIFIER,
		SCREAM,
		SPECIAL
	}
}
