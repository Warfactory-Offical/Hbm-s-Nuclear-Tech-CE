package com.hbm.handler;

import com.hbm.api.item.IGasMask;
import com.hbm.items.ModItems;
import com.hbm.items.ModItems.ArmorSets;
import com.hbm.items.ModItems.Inserts;
import com.hbm.lib.Library;
import com.hbm.potion.HbmPotion;
import com.hbm.util.ArmorRegistry;
import com.hbm.util.ArmorRegistry.HazardClass;
import com.hbm.util.Compat;
import com.hbm.util.I18nUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.ArrayList;
import java.util.List;

public class ArmorUtil {

	public static void register() {
		ArmorRegistry.registerHazard(ModItems.gas_mask_filter, HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.GAS_CHLORINE, HazardClass.BACTERIA, HazardClass.NERVE_AGENT);
		ArmorRegistry.registerHazard(ModItems.gas_mask_filter_mono, HazardClass.PARTICLE_COARSE, HazardClass.GAS_MONOXIDE);
		ArmorRegistry.registerHazard(ModItems.gas_mask_filter_combo, HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.GAS_CHLORINE, HazardClass.BACTERIA, HazardClass.GAS_MONOXIDE, HazardClass.NERVE_AGENT);
		ArmorRegistry.registerHazard(ModItems.gas_mask_filter_radon, HazardClass.RAD_GAS, HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.GAS_CHLORINE, HazardClass.BACTERIA, HazardClass.GAS_MONOXIDE, HazardClass.NERVE_AGENT);
		ArmorRegistry.registerHazard(ModItems.gas_mask_filter_rag, HazardClass.PARTICLE_COARSE);
		ArmorRegistry.registerHazard(ModItems.gas_mask_filter_piss, HazardClass.PARTICLE_COARSE, HazardClass.GAS_CHLORINE);

		ArmorRegistry.registerHazard(ModItems.gas_mask, HazardClass.SAND, HazardClass.LIGHT);
		ArmorRegistry.registerHazard(ModItems.gas_mask_m65, HazardClass.SAND);
		ArmorRegistry.registerHazard(ModItems.mask_damp, HazardClass.PARTICLE_COARSE);
		ArmorRegistry.registerHazard(ModItems.mask_piss, HazardClass.PARTICLE_COARSE, HazardClass.GAS_CHLORINE);
		
		ArmorRegistry.registerHazard(ModItems.goggles, HazardClass.LIGHT, HazardClass.SAND);
		ArmorRegistry.registerHazard(ModItems.ashglasses, HazardClass.LIGHT, HazardClass.SAND);

		ArmorRegistry.registerHazard(ModItems.attachment_mask, HazardClass.SAND);
		ArmorRegistry.registerHazard(Inserts.spider_milk, HazardClass.LIGHT);
		
		ArmorRegistry.registerHazard(ArmorSets.asbestos_helmet, HazardClass.SAND, HazardClass.LIGHT);
		ArmorRegistry.registerHazard(ArmorSets.hazmat_helmet, HazardClass.SAND);
		ArmorRegistry.registerHazard(ArmorSets.hazmat_helmet_red, HazardClass.SAND);
		ArmorRegistry.registerHazard(ArmorSets.hazmat_helmet_grey, HazardClass.SAND);
		ArmorRegistry.registerHazard(ArmorSets.liquidator_helmet, HazardClass.LIGHT, HazardClass.SAND);
		ArmorRegistry.registerHazard(ArmorSets.hazmat_paa_helmet, HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.LIGHT, HazardClass.SAND);
		ArmorRegistry.registerHazard(ArmorSets.paa_helmet, HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.GAS_CHLORINE, HazardClass.BACTERIA, HazardClass.GAS_MONOXIDE, HazardClass.LIGHT, HazardClass.SAND);
		ArmorRegistry.registerHazard(ArmorSets.t51_helmet, HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.GAS_CHLORINE, HazardClass.BACTERIA, HazardClass.GAS_MONOXIDE, HazardClass.LIGHT, HazardClass.SAND);
		ArmorRegistry.registerHazard(ArmorSets.ajr_helmet, HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.GAS_CHLORINE, HazardClass.BACTERIA, HazardClass.GAS_MONOXIDE, HazardClass.LIGHT, HazardClass.SAND);
		ArmorRegistry.registerHazard(ArmorSets.steamsuit_helmet, HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, /*HazardClass.GAS_LUNG,*/ HazardClass.BACTERIA, /*HazardClass.GAS_BLISTERING,*/ HazardClass.GAS_MONOXIDE, HazardClass.LIGHT, HazardClass.SAND);
		ArmorRegistry.registerHazard(ArmorSets.ajro_helmet, HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.GAS_CHLORINE, HazardClass.BACTERIA, HazardClass.GAS_MONOXIDE, HazardClass.LIGHT, HazardClass.SAND);
		ArmorRegistry.registerHazard(ArmorSets.rpa_helmet, HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.GAS_CHLORINE, HazardClass.BACTERIA, HazardClass.GAS_MONOXIDE, HazardClass.LIGHT, HazardClass.SAND);
		ArmorRegistry.registerHazard(ArmorSets.hev_helmet, HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.GAS_CHLORINE, HazardClass.BACTERIA, HazardClass.GAS_MONOXIDE, HazardClass.LIGHT, HazardClass.SAND);
		ArmorRegistry.registerHazard(ArmorSets.fau_helmet, HazardClass.RAD_GAS, HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.GAS_CHLORINE, HazardClass.BACTERIA, HazardClass.GAS_MONOXIDE, HazardClass.LIGHT, HazardClass.SAND);
		ArmorRegistry.registerHazard(ArmorSets.dns_helmet, HazardClass.RAD_GAS, HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.GAS_CHLORINE, HazardClass.BACTERIA, HazardClass.GAS_MONOXIDE, HazardClass.LIGHT, HazardClass.SAND, HazardClass.GAS_CORROSIVE);
		ArmorRegistry.registerHazard(ArmorSets.schrabidium_helmet, HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.GAS_CHLORINE, HazardClass.BACTERIA, HazardClass.GAS_MONOXIDE, HazardClass.LIGHT, HazardClass.SAND);
		ArmorRegistry.registerHazard(ArmorSets.euphemium_helmet, HazardClass.RAD_GAS, HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.GAS_CHLORINE, HazardClass.BACTERIA, HazardClass.GAS_MONOXIDE, HazardClass.LIGHT, HazardClass.SAND);
		
		//Ob ihr wirklich richtig steht, seht ihr wenn das Licht angeht!
		registerIfExists("gregtech", "gt.armor.hazmat.universal.head", HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.GAS_CHLORINE, HazardClass.BACTERIA, HazardClass.GAS_MONOXIDE, HazardClass.LIGHT, HazardClass.SAND);
		registerIfExists("gregtech", "gt.armor.hazmat.biochemgas.head", HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.GAS_CHLORINE, HazardClass.BACTERIA, HazardClass.GAS_MONOXIDE, HazardClass.LIGHT, HazardClass.SAND);
		registerIfExists("gregtech", "gt.armor.hazmat.radiation.head", HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.GAS_CHLORINE, HazardClass.BACTERIA, HazardClass.GAS_MONOXIDE, HazardClass.LIGHT, HazardClass.SAND);
	}
	
	private static void registerIfExists(String domain, String name, HazardClass... classes) {
		Item item = Compat.tryLoadItem(domain, name);
		if(item != null)
			ArmorRegistry.registerHazard(item, classes);
	}
	
	public static boolean checkForFaraday(EntityPlayer player) {
		
		NonNullList<ItemStack> armor = player.inventory.armorInventory;
		
		if(armor.get(0).isEmpty() || armor.get(1).isEmpty() || armor.get(2).isEmpty() || armor.get(3).isEmpty()) return false;
		
		if(ArmorUtil.isFaradayArmor(armor.get(0)) &&
				ArmorUtil.isFaradayArmor(armor.get(1)) &&
				ArmorUtil.isFaradayArmor(armor.get(2)) &&
				ArmorUtil.isFaradayArmor(armor.get(3)))
			return true;
		
		return false;
	}

	public static boolean isFaradayArmor(ItemStack item) {
		
		String name = item.getTranslationKey();
		
		if(HazmatRegistry.getCladding(item) > 0)
			return true;
		
		for(String metal : metals) {
			
			if(name.toLowerCase().contains(metal))
				return true;
		}
		
		return false;
	}
	
	public static boolean checkArmorNull(EntityLivingBase player, EntityEquipmentSlot slot) {
		return player.getItemStackFromSlot(slot) == null || player.getItemStackFromSlot(slot).isEmpty();
	}

	public static final String[] metals = new String[] {
			"chainmail",
			"iron",
			"silver",
			"gold",
			"platinum",
			"tin",
			"lead",
			"liquidator",
			"schrabidium",
			"euphemium",
			"steel",
			"cmb",
			"titanium",
			"alloy",
			"copper",
			"bronze",
			"electrum",
			"t45",
			"bj",
			"starmetal",
			"hazmat", //also count because rubber is insulating
			"rubber",
			"hev",
			"ajr",
			"rpa",
			"spacesuit"
	};

	public static void damageSuit(EntityPlayer player, int slot, int amount) {
	
		if(player.inventory.armorInventory.get(slot) == ItemStack.EMPTY)
			return;
	
		int j = player.inventory.armorInventory.get(slot).getItemDamage();
		player.inventory.armorInventory.get(slot).setItemDamage(j += amount);
	
		if(player.inventory.armorInventory.get(slot).getItemDamage() >= player.inventory.armorInventory.get(slot).getMaxDamage()) {
			player.inventory.armorInventory.set(slot, ItemStack.EMPTY);
		}
	}

	public static void resetFlightTime(EntityPlayer player) {
		if(player instanceof EntityPlayerMP mp) {
            mp.connection.floatingTickCount = 0;
		}
	}

	public static boolean checkForFiend2(EntityPlayer player) {
		
		return ArmorUtil.checkArmorPiece(player, ArmorSets.jackt2, 2) && Library.checkForHeld(player, ModItems.shimmer_axe);
	}

	public static boolean checkForFiend(EntityPlayer player) {
		
		return ArmorUtil.checkArmorPiece(player, ArmorSets.jackt, 2) && Library.checkForHeld(player, ModItems.shimmer_sledge);
	}

	public static boolean checkPAA(EntityLivingBase player){
		return checkArmor(player, ArmorSets.paa_helmet, ArmorSets.paa_plate, ArmorSets.paa_legs, ArmorSets.paa_boots);
	}

	// Drillgon200: Is there a reason for this method? I don't know and I don't
	// care to find out.
	// Alcater: Looks like some kind of hazmat tier 2 check
	public static boolean checkForHaz2(EntityLivingBase player) {
	
		if(checkArmor(player, ArmorSets.hazmat_paa_helmet, ArmorSets.hazmat_paa_plate, ArmorSets.hazmat_paa_legs, ArmorSets.hazmat_paa_boots) ||
			checkArmor(player, ArmorSets.paa_helmet, ArmorSets.paa_plate, ArmorSets.paa_legs, ArmorSets.paa_boots) ||
			checkArmor(player, ArmorSets.liquidator_helmet, ArmorSets.liquidator_plate, ArmorSets.liquidator_legs, ArmorSets.liquidator_boots) ||
			checkArmor(player, ArmorSets.euphemium_helmet, ArmorSets.euphemium_plate, ArmorSets.euphemium_legs, ArmorSets.euphemium_boots) ||
			checkArmor(player, ArmorSets.hev_helmet, ArmorSets.hev_plate, ArmorSets.hev_legs, ArmorSets.hev_boots) ||
			checkArmor(player, ArmorSets.ajr_helmet, ArmorSets.ajr_plate, ArmorSets.ajr_legs, ArmorSets.ajr_boots) ||
			checkArmor(player, ArmorSets.ajro_helmet, ArmorSets.ajro_plate, ArmorSets.ajro_legs, ArmorSets.ajro_boots) ||
			checkArmor(player, ArmorSets.rpa_helmet, ArmorSets.rpa_plate, ArmorSets.rpa_legs, ArmorSets.rpa_boots) ||
			checkArmor(player, ArmorSets.fau_helmet, ArmorSets.fau_plate, ArmorSets.fau_legs, ArmorSets.fau_boots) ||
			checkArmor(player, ArmorSets.dns_helmet, ArmorSets.dns_plate, ArmorSets.dns_legs, ArmorSets.dns_boots)) {
			return true;
		}
	
		return false;
	}

	public static boolean checkForHazmatOnly(EntityLivingBase player) {
		if(checkArmor(player, ArmorSets.hazmat_helmet, ArmorSets.hazmat_plate, ArmorSets.hazmat_legs, ArmorSets.hazmat_boots) ||
			checkArmor(player, ArmorSets.hazmat_helmet_red, ArmorSets.hazmat_plate_red, ArmorSets.hazmat_legs_red, ArmorSets.hazmat_boots_red) ||
			checkArmor(player, ArmorSets.hazmat_helmet_grey, ArmorSets.hazmat_plate_grey, ArmorSets.hazmat_legs_grey, ArmorSets.hazmat_boots_grey) ||
			checkArmor(player, ArmorSets.hazmat_paa_helmet, ArmorSets.hazmat_paa_plate, ArmorSets.hazmat_paa_legs, ArmorSets.hazmat_paa_boots) ||
			checkArmor(player, ArmorSets.paa_helmet, ArmorSets.paa_plate, ArmorSets.paa_legs, ArmorSets.paa_boots) ||
			checkArmor(player, ArmorSets.liquidator_helmet, ArmorSets.liquidator_plate, ArmorSets.liquidator_legs, ArmorSets.liquidator_boots)){
			return true;
		}
		return false;
	}

	public static boolean checkForHazmat(EntityLivingBase player) {
		if(ArmorUtil.checkArmor(player, ArmorSets.hazmat_helmet, ArmorSets.hazmat_plate, ArmorSets.hazmat_legs, ArmorSets.hazmat_boots) ||
			ArmorUtil.checkArmor(player, ArmorSets.hazmat_helmet_red, ArmorSets.hazmat_plate_red, ArmorSets.hazmat_legs_red, ArmorSets.hazmat_boots_red) ||
			ArmorUtil.checkArmor(player, ArmorSets.hazmat_helmet_grey, ArmorSets.hazmat_plate_grey, ArmorSets.hazmat_legs_grey, ArmorSets.hazmat_boots_grey) ||
			ArmorUtil.checkArmor(player, ArmorSets.t51_helmet, ArmorSets.t51_plate, ArmorSets.t51_legs, ArmorSets.t51_boots) ||
			ArmorUtil.checkArmor(player, ArmorSets.schrabidium_helmet, ArmorSets.schrabidium_plate, ArmorSets.schrabidium_legs, ArmorSets.schrabidium_boots) ||
			checkForHaz2(player)) {
	
			return true;
		}
	
		if(player.isPotionActive(HbmPotion.mutation))
			return true;
	
		return false;
	}

	public static boolean checkForAsbestos(EntityLivingBase player) {
	
		if(ArmorUtil.checkArmor(player, ArmorSets.asbestos_helmet, ArmorSets.asbestos_plate, ArmorSets.asbestos_legs, ArmorSets.asbestos_boots)) {
			return true;
		}
	
		return false;
	}

	public static boolean checkArmor(EntityLivingBase player, Item helm, Item chest, Item leg, Item shoe) {
		if(player.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() == shoe && 
			player.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem() == leg && 
			player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == chest && 
			player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == helm) {
			return true;
		}
	
		return false;
	}

	public static boolean checkArmorPiece(EntityPlayer player, Item armor, int slot) {
		if(player.inventory.armorInventory.get(slot) != null && player.inventory.armorInventory.get(slot).getItem() == armor) {
			return true;
		}
	
		return false;
	}
	
	/*
	 * Default implementations for IGasMask items
	 */
	public static final String FILTERK_KEY = "hfrFilter";
	
	public static void damageGasMaskFilter(EntityLivingBase entity, int damage) {
		
		ItemStack mask = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
		
		if(mask == null)
			return;
		
		if(!(mask.getItem() instanceof IGasMask)) {
			
			if(ArmorModHandler.hasMods(mask)) {
				
				ItemStack mods[] = ArmorModHandler.pryMods(mask);
				
				if(mods[ArmorModHandler.helmet_only] != null && mods[ArmorModHandler.helmet_only].getItem() instanceof IGasMask)
					mask = mods[ArmorModHandler.helmet_only];
			}
		}
		
		if(mask != null)
			damageGasMaskFilter(mask, damage);
	}
	
	public static void damageGasMaskFilter(ItemStack mask, int damage) {
		ItemStack filter = getGasMaskFilter(mask);
		
		if(filter == null) {
			if(ArmorModHandler.hasMods(mask)) {
				ItemStack mods[] = ArmorModHandler.pryMods(mask);
				
				if(mods[ArmorModHandler.helmet_only] != null && mods[ArmorModHandler.helmet_only].getItem() instanceof IGasMask)
					filter = getGasMaskFilter(mods[ArmorModHandler.helmet_only]);
			}
		}
		
		if(filter == null || filter.getMaxDamage() == 0)
			return;
		
		filter.setItemDamage(filter.getItemDamage() + damage);
		
		if(filter.getItemDamage() > filter.getMaxDamage()){
			removeFilter(mask);
		}
		else{
			installGasMaskFilter(mask, filter);
		}
	}
	
	public static void installGasMaskFilter(ItemStack mask, ItemStack filter) {
		
		if(mask == null || filter == null)
			return;
		
		if(!mask.hasTagCompound())
			mask.setTagCompound(new NBTTagCompound());
		
		NBTTagCompound attach = new NBTTagCompound();
		filter.writeToNBT(attach);
		
		mask.getTagCompound().setTag(FILTERK_KEY, attach);
	}
	
	public static void removeFilter(ItemStack mask) {
		
		if(mask == null)
			return;
		
		if(!mask.hasTagCompound())
			return;
		
		mask.getTagCompound().removeTag(FILTERK_KEY);
		if(mask.getTagCompound().isEmpty())
			mask.setTagCompound(null);
	}
	
	public static ItemStack getGasMaskFilter(ItemStack mask) {
		
		if(mask == null)
			return null;
		
		if(!mask.hasTagCompound())
			return null;
		
		NBTTagCompound attach = mask.getTagCompound().getCompoundTag(FILTERK_KEY);
		ItemStack filter = new ItemStack(attach);
		if(filter.isEmpty())
			return null;
		return filter;
	}
	
	public static boolean checkForDigamma(EntityPlayer player) {
		
		if(checkArmor(player, ArmorSets.fau_helmet, ArmorSets.fau_plate, ArmorSets.fau_legs, ArmorSets.fau_boots))
			return true;

		if(checkArmor(player, ArmorSets.dns_helmet, ArmorSets.dns_plate, ArmorSets.dns_legs, ArmorSets.dns_boots))
			return true;
		
		if(player.isPotionActive(HbmPotion.stability))
			return true; 
		
		return false;
	}
	
	public static boolean checkForMonoMask(EntityPlayer player) {

		if(checkArmorPiece(player, ModItems.gas_mask, 3)) {
			return true;
		}
		if(checkArmorPiece(player, ModItems.gas_mask_m65, 3)) {
			return true;
		}

		if(checkArmorPiece(player, ModItems.gas_mask_mono, 3))
			return true;
		
		if(checkArmorPiece(player, ArmorSets.liquidator_helmet, 3))
			return true;

		if(player.isPotionActive(HbmPotion.mutation))
			return true;
		
		ItemStack helmet = player.inventory.armorInventory.get(3);
		if(helmet != null && ArmorModHandler.hasMods(helmet)) {
			
			ItemStack mods[] = ArmorModHandler.pryMods(helmet);
			
			if(mods[ArmorModHandler.helmet_only] != null && mods[ArmorModHandler.helmet_only].getItem() == ModItems.attachment_mask_mono)
				return true;
		}
		
		return false;
	}
	
	public static boolean checkForGoggles(EntityPlayer player) {

		if(checkArmorPiece(player, ModItems.goggles, 3))
		{
			return true;
		}
		if(checkArmorPiece(player, ModItems.ashglasses, 3))
		{
			return true;
		}
		if(checkArmorPiece(player, ArmorSets.t51_helmet, 3))
		{
			return true;
		}
		if(checkArmorPiece(player, ArmorSets.ajr_helmet, 3))
		{
			return true;
		}
		if(checkArmorPiece(player, ArmorSets.rpa_helmet, 3))
		{
			return true;
		}
		if(checkArmorPiece(player, ArmorSets.bj_helmet, 3))
		{
			return true;
		}
		if(checkArmorPiece(player, ArmorSets.hev_helmet, 3))
		{
			return true;
		}
		if(checkArmorPiece(player, ArmorSets.hazmat_paa_helmet, 3))
		{
			return true;
		}
		if(checkArmorPiece(player, ArmorSets.paa_helmet, 3))
		{
			return true;
		}
		return false;
	}

	/**
	 * Grabs the installed filter or the filter of the attachment, used for attachment rendering
	 * @param mask
	 * @param entity
	 * @return
	 */
	public static ItemStack getGasMaskFilterRecursively(ItemStack mask) {
		
		ItemStack filter = getGasMaskFilter(mask);
		
		if((filter == null || filter.isEmpty()) && ArmorModHandler.hasMods(mask)) {
			
			ItemStack mods[] = ArmorModHandler.pryMods(mask);
			
			if(mods[ArmorModHandler.helmet_only] != null && mods[ArmorModHandler.helmet_only].getItem() instanceof IGasMask)
				filter = ((IGasMask)mods[ArmorModHandler.helmet_only].getItem()).getFilter(mods[ArmorModHandler.helmet_only]);
		}
		
		return filter;
	}

	public static void addGasMaskTooltip(ItemStack mask, World world, List<String> list, ITooltipFlag flagIn){
		
		if(mask == null || !(mask.getItem() instanceof IGasMask))
			return;
		
		ItemStack filter = ((IGasMask)mask.getItem()).getFilter(mask);
		
		if(filter == null) {
			list.add("§c" + I18nUtil.resolveKey("desc.nofilter"));
			return;
		}
		
		list.add("§6" + I18nUtil.resolveKey("desc.infilter"));
		
		int meta = filter.getItemDamage();
		int max = filter.getMaxDamage();
		
		String append = "";
		
		if(max > 0) {
			append = " (" + Library.getPercentage((max - meta) / (double)max) + "%) "+(max-meta)+"/"+max;
		}
		
		List<String> lore = new ArrayList();
		list.add("  " + filter.getDisplayName() + append);
		filter.getItem().addInformation(filter, world, lore, flagIn);
		ForgeEventFactory.onItemTooltip(filter, null, lore, flagIn);
		lore.forEach(x -> list.add("§e  " + x));
	}
	
	// public static boolean isWearingEmptyMask(EntityPlayer player) {
		
	// 	ItemStack mask = player.getEquipmentInSlot(4);
		
	// 	if(mask == null)
	// 		return false;
		
	// 	if(mask.getItem() instanceof IGasMask) {
	// 		return getGasMaskFilter(mask) == null;
	// 	}
		
	// 	ItemStack mod = ArmorModHandler.pryMods(mask)[ArmorModHandler.helmet_only];
		
	// 	if(mod != null && mod.getItem() instanceof IGasMask) {
	// 		return getGasMaskFilter(mod) == null;
	// 	}
		
	// 	return false;
	// }
}