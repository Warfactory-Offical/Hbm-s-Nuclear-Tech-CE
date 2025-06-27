package com.hbm.items.special;

import com.hbm.config.GeneralConfig;
import com.hbm.handler.ArmorUtil;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Random;

public class ItemCustomLore extends Item {

	EnumRarity rarity;
	
	public ItemCustomLore(String s) {
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setCreativeTab(MainRegistry.controlTab);
		ModItems.ALL_ITEMS.add(this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flagIn) {
		String unloc = this.getTranslationKey() + ".desc";
		String loc = I18nUtil.resolveKey(unloc);

		if(!unloc.equals(loc)) {

			String[] locs = loc.split("\\$");

			for(String s : locs) {
				list.add(s);
			}
		}
		if(this == ModItems.powder_asbestos)
		{
			list.add(TextFormatting.ITALIC + "\"Sniffffffff- MHHHHHHMHHHHHHHHH\"");
		}
		if(this == ModItems.bismuth_tool){
			list.add("§eRight-click a Dud while having empty cells in inventory gives you antimatter cells.");
			list.add("§8§oMight set off the Dud tho");
		}
		if(this == ModItems.iv_empty) {
			list.add(I18nUtil.resolveKey("desc.ivempty"));
		}
		if(this == ModItems.radaway) {
			list.add(I18nUtil.resolveKey("desc.radamount", -250));
			list.add(I18nUtil.resolveKey("desc.radspeed", -25));
			list.add(I18nUtil.resolveKey("desc.duration", 10));
		}
		if(this == ModItems.radaway_strong) {
			list.add(I18nUtil.resolveKey("desc.radamount", -500));
			list.add(I18nUtil.resolveKey("desc.radspeed", -100));
			list.add(I18nUtil.resolveKey("desc.duration", 5));
		}
		if(this == ModItems.radaway_flush) {
			list.add(I18nUtil.resolveKey("desc.radamount", -1000));
			list.add(I18nUtil.resolveKey("desc.radspeed", -400));
			list.add(I18nUtil.resolveKey("desc.duration", 2.5));
		}
		if(this == ModItems.ingot_schraranium)
		{
			if(GeneralConfig.enableBabyMode)
				list.add("Peer can go die, I'm not putting any retarded niko stuff in the mod.");
			else
				list.add("Made from uranium in a nuclear transmutator");
		}
		if(this == ModItems.ingot_fiberglass)
		{
			list.add("High in fiber, high in glass. Everything the body needs.");
		}
		if(this == ModItems.missile_soyuz_lander)
		{
			list.add("Doubles as a crappy lander!");
		}
		if(this == ModItems.book_of_)
		{
			list.add("Denn wer den Walzer richtig tritt,");
			list.add("der ist auch für den Abgang fit.");
		}
		if(this == ModItems.watch)
		{
			list.add("A small blue pocket watch.");
			list.add("It's glass has a few cracks in it,");
			list.add("and some shards are missing.");
			list.add("It stopped ticking at 2:34.");
		}
		if(this == ModItems.reacher)
		{
			list.add("Holding this in main hand or off hand reduces radiation coming from items to its square-root.");
			list.add("It also is useful to handle very hot or cold items.");
		}
		if(this == ModItems.crystal_horn)
		{
			if(MainRegistry.polaroidID == 11)
				list.add("An actual horn.");
			else
				list.add("Not an actual horn.");
		}
		
		if(this == ModItems.crystal_charred)
		{
			if(MainRegistry.polaroidID == 11)
				list.add("Also a real horn. Weird, right?");
			else
				list.add("High quality silicate, slightly burned.");
		}
		if(this == ModItems.ingot_asbestos)
		{
			list.add(TextFormatting.ITALIC + "\"Filled with life, self-doubt and asbestos. That comes with the air.\"");
		}
		if(this == ModItems.entanglement_kit)
		{
			list.add("Teleporter crafting item.");
			list.add("Enables dimension-shifting via");
			list.add("beryllium-enhanced resource scanner.");
		}
		if(this == ModItems.ams_focus_limiter)
		{
			list.add("Maximum performance for restriction field:");
			list.add("Standard cooling, no energy bonus.");
		}
		
		if(this == ModItems.ams_focus_booster)
		{
			list.add("Weaker restriction field and core energy injection:");
			list.add("More heat generation, extra energy.");
		}
		
		if(this == ModItems.ams_muzzle)
		{
			list.add("...it emits an energy-beam thingy.");
		}
		if(this == ModItems.powder_poison)
		{
			list.add("Used in multi purpose bombs:");
			list.add("Warning: Poisonous!");
		}
		if(this == ModItems.pellet_cluster)
		{
			list.add("Used in multi purpose bombs:");
			list.add("Adds some extra boom!");
		}

		if(this == ModItems.powder_fire)
		{
			list.add("Used in multi purpose bombs:");
			list.add("Incendiary bombs are fun!");
		}
		if(this == ModItems.pellet_gas)
		{
			list.add("Used in multi purpose bombs:");
			list.add("*cough cough* Halp pls!");
		}
		if(this == ModItems.powder_tektite)
		{
			list.add("Collected via Geralds Miningfleet from §3outer space");
		}
		if(this == ModItems.igniter)
		{
			list.add("(Used by right-clicking the Prototype)");
			list.add("It's a green metal handle with a");
			list.add("bright red button and a small lid.");
			list.add("At the bottom, the initials N.E. are");
			list.add("engraved. Whoever N.E. was, he had");
			list.add("a great taste in shades of green.");
		}
		if(this == ModItems.overfuse)
		{
			list.add("Say what?");
		}
		if(this == ModItems.tritium_deuterium_cake)
		{
			list.add("Not actual cake, but great");
			list.add("universal fusion fuel!");
		}
		if(this == ModItems.pin) {
			list.add("Can be used with a screwdriver to pick locks.");
			if(Minecraft.getMinecraft().player != null) {
				EntityPlayer player = Minecraft.getMinecraft().player;
				if(ArmorUtil.checkArmorPiece(player, ModItems.jackt, 2) || ArmorUtil.checkArmorPiece(player, ModItems.jackt2, 2))
					list.add("Success rate of picking standard lock is 100%!");
				else
					list.add("Success rate of picking standard lock is ~10%");
			}
		}
		if(this == ModItems.key_red) {
			if(MainRegistry.polaroidID == 11) {
				list.add(TextFormatting.DARK_RED + "" + TextFormatting.BOLD + "e");
			} else {
				list.add("Explore the other side.");
			}
		}
		if(this == ModItems.crystal_energy) {
			list.add("Densely packed energy powder.");
			list.add("Not edible.");
		}
		if(this == ModItems.pellet_coolant) {
			list.add("Required for cyclotron operation.");
			list.add("Do NOT operate cyclotron without it!");
		}
		if(this == ModItems.fuse) {
			list.add("This item is needed for every large");
			list.add("nuclear reactor, as it allows the");
			list.add("reactor to generate electricity and");
			list.add("use up it's fuel. Removing the fuse");
			list.add("from a reactor will instantly shut");
			list.add("it down.");
		}
		if(this == ModItems.gun_super_shotgun) {
			list.add("It's super broken!");
		}

		if(this == ModItems.burnt_bark) {
			list.add("A piece of bark from an exploded golden oak tree.");
		}

		if(this == ModItems.flame_pony) {
			// list.add("Blue horse beats yellow horse, look it up!");
			list.add(I18nUtil.resolveKey("desc.flamepony"));
		}
		
		if(this == ModItems.flame_conspiracy)
		{
			list.add(I18nUtil.resolveKey("desc.flameconspiracy"));
		}
		if(this == ModItems.flame_politics)
		{
			list.add(I18nUtil.resolveKey("desc.flamepolitics"));
		}
		if(this == ModItems.flame_opinion)
		{
			list.add(I18nUtil.resolveKey("desc.flameopinion"));
		}

		if(this == ModItems.ingot_neptunium) {
			if(MainRegistry.polaroidID == 11) {
				list.add("Woo, scary!");
			} else
				list.add("That one's my favourite!");
		}

		if(this == ModItems.pellet_rtg) {
			if(MainRegistry.polaroidID == 11)
				list.add("Contains ~100% Pu238 oxide.");
			else
				list.add("RTG fuel pellet for infinite energy! (almost)");
		}
		if(this == ModItems.ingot_combine_steel) {
			/*list.add("\"I mean, it's a verb for crying out loud.");
			list.add("The aliens aren't verbs. They're nouns!\"");
			list.add("\"Actually, I think it's also the name");
			list.add("of some kind of farm equipment, like a");
			list.add("thresher or something.\"");
			list.add("\"That's even worse. Now we have a word");
			list.add("that could mean 'to mix things together',");
			list.add("a piece of farm equipment, and let's see...");
			list.add("oh yea, it can also mean 'the most advanced");
			list.add("form of life in the known universe'.\"");
			list.add("\"So?\"");
			list.add("\"'So?' C'mon man, they're ALIENS!\"");*/
			list.add("*insert Civil Protection reference here*");
		}
		if(this == ModItems.ingot_euphemium) {
			list.add("A very special and yet strange element.");
		}
		if(this == ModItems.powder_euphemium) {
			list.add("Pulverized pink.");
			list.add("Tastes like strawberries.");
		}
		if(this == ModItems.nugget_euphemium) {
			list.add("A small piece of a pink metal.");
			list.add("It's properties are still unknown,");
			list.add("DEAL WITH IT carefully.");
		}
		if(this == ModItems.rod_quad_euphemium) {
			list.add("A quad fuel rod which contains a");
			list.add("very small ammount of a strange new element.");
		}
		if(this == ModItems.pellet_rtg_polonium)
		{
			if(MainRegistry.polaroidID == 11)
				list.add("Polonium 4 U and me.");
			else
				list.add("Tastes nice in Tea");
		}
		if(this == ModItems.mech_key)
		{
			list.add("It pulses with power.");
		}
		if(this == ModItems.nugget_mox_fuel) {
			list.add("Moxie says: " + TextFormatting.BOLD + "TAX EVASION.");
		}
		if(this == ModItems.billet_mox_fuel) {
			list.add(TextFormatting.ITALIC + "Pocket-Moxie!");
		}
		
		if(this == ModItems.ingot_lanthanium)
		{
			list.add("'Lanthanum'");
		}

		if(this == ModItems.ingot_gh336 || this == ModItems.billet_gh336 || this == ModItems.nugget_gh336)
		{
			list.add("Seaborgium's colleague");
		}

		if(this == ModItems.billet_flashlead)
		{
			list.add("The lattice decays, causing antimatter-matter annihilation reactions, causing the release of pions, decaying into muons, catalyzing fusion of the nuclei, creating the new element. Please try to keep up.");
		}
		
		if(this == ModItems.ingot_tantalium || this == ModItems.nugget_tantalium || this == ModItems.gem_tantalium || this == ModItems.powder_tantalium)
		{
			list.add("'Tantalum'");
		}
		if(this == ModItems.euphemium_capacitor)
		{
			list.add("Permits passive dispersion of accumulated positive energy.");
		}

		if(this == ModItems.factory_core_titanium || this == ModItems.factory_core_advanced)
		{
			list.add("Used in factories to make the speed change");
		}
		if(this == ModItems.undefined && world != null) {
			
			if(world.rand.nextInt(10) == 0) {
				list.add(TextFormatting.DARK_RED + "UNDEFINED");
			} else {
				Random rand = new Random(System.currentTimeMillis() / 500);
				
				if(setSize == 0)
					setSize = Item.REGISTRY.getKeys().size();
				
				int r = rand.nextInt(setSize);
				
				Item item = Item.getItemById(r);
				
				if(item != null) {
					list.add(new ItemStack(item).getDisplayName());
				} else {
					list.add(TextFormatting.RED + "ERROR #" + r);
				}
			}
		}
	}
	
	static int setSize = 0;

	@Override
	public EnumRarity getRarity(ItemStack stack) {
		if(this == ModItems.plate_euphemium || 
			this == ModItems.ingot_euphemium || 
			this == ModItems.ingot_osmiridium || 
			this == ModItems.ingot_astatine || 
			this == ModItems.ingot_iodine || 
			this == ModItems.ingot_i131 || 
			this == ModItems.ingot_strontium || 
			this == ModItems.ingot_sr90 || 
			this == ModItems.ingot_cobalt || 
			this == ModItems.ingot_co60 || 
			this == ModItems.ingot_bromine || 
			this == ModItems.ingot_tennessine || 
			this == ModItems.ingot_cerium || 
			this == ModItems.ingot_caesium || 
			this == ModItems.ingot_niobium || 
			this == ModItems.ingot_neodymium || 
			this == ModItems.ingot_gh336 || 
			this == ModItems.euphemium_capacitor ||

			this == ModItems.nugget_euphemium || 
			this == ModItems.nugget_osmiridium || 
			this == ModItems.nugget_strontium || 
			this == ModItems.nugget_sr90 || 
			this == ModItems.nugget_cobalt || 
			this == ModItems.nugget_co60 || 
			this == ModItems.nugget_gh336 || 

			this == ModItems.billet_gh336 || 
			this == ModItems.billet_co60 || 
			this == ModItems.billet_sr90 || 
			
			this == ModItems.powder_neptunium ||
			this == ModItems.powder_euphemium || 
			this == ModItems.powder_osmiridium || 
			this == ModItems.powder_iodine || 
			this == ModItems.powder_i131 || 
			this == ModItems.powder_strontium || 
			this == ModItems.powder_sr90 || 
			this == ModItems.powder_astatine || 
			this == ModItems.powder_at209 || 
			this == ModItems.powder_cobalt || 
			this == ModItems.powder_co60 || 
			this == ModItems.powder_bromine || 
			this == ModItems.powder_niobium || 
			this == ModItems.powder_cerium || 
			this == ModItems.powder_neodymium || 
			this == ModItems.powder_tennessine || 
			this == ModItems.powder_xe135 || 
			this == ModItems.powder_caesium || 
			this == ModItems.powder_cs137 || 
			this == ModItems.powder_cs137 || 
			this == ModItems.powder_nitan_mix || 
			this == ModItems.powder_spark_mix || 
			this == ModItems.powder_magic || 


			this == ModItems.powder_sr90_tiny || 
			this == ModItems.powder_iodine_tiny || 
			this == ModItems.powder_i131_tiny || 
			this == ModItems.powder_co60_tiny || 
			this == ModItems.powder_cobalt_tiny || 
			this == ModItems.powder_niobium_tiny || 
			this == ModItems.powder_cerium_tiny || 
			this == ModItems.powder_neodymium_tiny || 
			this == ModItems.powder_xe135_tiny || 
			this == ModItems.powder_cs137_tiny || 
			this == ModItems.nugget_daffergon || 
			this == ModItems.powder_daffergon || 
			this == ModItems.ingot_daffergon || 
			
			this == ModItems.bathwater_mk3 || 
			this == ModItems.plate_euphemium ||  
			this == ModItems.rod_euphemium ||  
			this == ModItems.rod_quad_euphemium || 
			this == ModItems.rod_daffergon || 
			this == ModItems.watch || 
			this == ModItems.undefined) {
			return EnumRarity.EPIC;
		}

		if(this == ModItems.ingot_schrabidium ||
			this == ModItems.ingot_schraranium || 
			this == ModItems.ingot_schrabidate || 
			this == ModItems.ingot_saturnite ||  
			this == ModItems.ingot_solinium || 
			this == ModItems.nugget_schrabidium || 
			this == ModItems.nugget_solinium || 
			this == ModItems.ingot_electronium || 
			this == ModItems.billet_solinium || 
			this == ModItems.billet_schrabidium || 
			
			this == ModItems.powder_schrabidate || 
			this == ModItems.powder_schrabidium || 

			this == new ItemStack(ModItems.wire_fine, 1, Mats.MAT_SCHRABIDIUM.id).getItem() ||

			this == ModItems.plate_schrabidium || 
			this == ModItems.plate_saturnite || 
			
			this == ModItems.circuit_schrabidium || 
			this == ModItems.gun_revolver_schrabidium_ammo || 
			this == ModItems.powder_unobtainium || 
			this == ModItems.nugget_unobtainium || 
			this == ModItems.ingot_unobtainium || 
			this == ModItems.nugget_unobtainium_greater || 
			this == ModItems.nugget_unobtainium_lesser || 
			this == ModItems.billet_unobtainium ||
			
			this == ModItems.solinium_core ||
			this == ModItems.powder_impure_osmiridium ||
			this == ModItems.crystal_osmiridium ||
			this == ModItems.crystal_schrabidium ||
    		this == ModItems.crystal_schraranium ||
    		this == ModItems.crystal_trixite ||
    		ItemCell.hasFluid(stack, Fluids.SAS3) ||
    		this == ModItems.rod_unobtainium) {
			return EnumRarity.RARE;
		}

		if(this == ModItems.bathwater_mk2 || 
			this == ModItems.plate_paa || 
			this == ModItems.cladding_paa || 
			this == ModItems.ammo_566_gold || 
			this == ModItems.gun_revolver_cursed_ammo || 
			this == ModItems.powder_power || 
			this == ModItems.powder_yellowcake || 
			this == ModItems.billet_australium || 
			this == ModItems.billet_australium_greater || 
			this == ModItems.billet_australium_lesser || 

			this == ModItems.ingot_australium || 
			this == ModItems.ingot_weidanium || 
			this == ModItems.ingot_reiium || 
			this == ModItems.ingot_verticium || 
			this == ModItems.powder_paleogenite || 
			this == ModItems.powder_paleogenite_tiny || 

			this == ModItems.nugget_australium || 
			this == ModItems.nugget_australium_greater || 
			this == ModItems.nugget_australium_lesser || 
			this == ModItems.nugget_weidanium || 
			this == ModItems.nugget_reiium || 
			this == ModItems.nugget_verticium || 

			this == ModItems.powder_australium || 
			this == ModItems.powder_weidanium || 
			this == ModItems.powder_reiium || 
			this == ModItems.powder_verticium) {
			return EnumRarity.UNCOMMON;
		}

		return this.rarity != null ? rarity : EnumRarity.COMMON;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack) {
		if(this == ModItems.rune_isa ||
    			this == ModItems.rune_dagaz ||
    			this == ModItems.rune_hagalaz ||
    			this == ModItems.rune_jera ||
    			this == ModItems.rune_thurisaz ||
    			this == ModItems.egg_balefire_shard ||
    			this == ModItems.egg_balefire ||
    			this == ModItems.coin_maskman || 
    			this == ModItems.coin_radiation || 
    			this == ModItems.coin_worm || 
    			this == ModItems.coin_ufo || 
    			this == ModItems.coin_creeper) 
		{
    		return true;
    	}
		return super.hasEffect(stack);
	}
	
	public ItemCustomLore setRarity(EnumRarity rarity) {
    	this.rarity = rarity;
		return this;
    }

}
