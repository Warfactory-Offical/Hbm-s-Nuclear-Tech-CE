package com.hbm.inventory.recipes;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.items.ModItems;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public class ShredderRecipes {

	public static LinkedHashMap<ComparableStack, ItemStack> shredderRecipes = new LinkedHashMap<ComparableStack, ItemStack>();
	public static List<ShredderRecipe> jeiShredderRecipes = null;
	
	public static void registerShredder() {
		
		String[] names = OreDictionary.getOreNames();
		
		for(int i = 0; i < names.length; i++) {
			
			String name = names[i];
			
			//if the dict contains invalid names, skip
			if(name == null || name.isEmpty())
				continue;
			
			List<ItemStack> matches = OreDictionary.getOres(name);
			
			//if the name isn't assigned to an ore, also skip
			if(matches == null || matches.isEmpty())
				continue;

			if(name.length() > 5 && name.substring(0, 5).equals("ingot")) {
				ItemStack dust = getDustByName(name.substring(5));
				
				if(dust != null && dust.getItem() != ModItems.scrap) {

					for(ItemStack stack : matches) {
						shredderRecipes.put(new ComparableStack(stack), dust);
					}
				}
			} else if(name.length() > 6 && name.substring(0, 6).equals("nugget")) {
				ItemStack dust = getTinyDustByName(name.substring(6));
				
				if(dust != null && dust.getItem() != ModItems.scrap) {

					for(ItemStack stack : matches) {
						shredderRecipes.put(new ComparableStack(stack), dust);
					}
				}
			} else if(name.length() > 3 && name.substring(0, 3).equals("ore")) {
				ItemStack dust = getDustByName(name.substring(3));
				
				if(dust != null && dust.getItem() != ModItems.scrap) {
					
					dust.setCount(2);

					for(ItemStack stack : matches) {
						shredderRecipes.put(new ComparableStack(stack), dust);
					}
				}
			} else if(name.length() > 5 && name.substring(0, 5).equals("block")) {
				ItemStack dust = getDustByName(name.substring(5));
				
				if(dust != null && dust.getItem() != ModItems.scrap) {
					
					dust.setCount(9);

					for(ItemStack stack : matches) {
						shredderRecipes.put(new ComparableStack(stack), dust);
					}
				}
			} else if(name.length() > 3 && name.substring(0, 3).equals("gem")) {
				ItemStack dust = getDustByName(name.substring(3));
				
				if(dust != null && dust.getItem() != ModItems.scrap) {

					for(ItemStack stack : matches) {
						shredderRecipes.put(new ComparableStack(stack), dust);
					}
				}
			} else if(name.length() > 3 && name.substring(0, 4).equals("dust")) {

				for(ItemStack stack : matches) {
					if(stack != null && !stack.isEmpty() && Item.REGISTRY.getNameForObject(stack.getItem()) != null)
						shredderRecipes.put(new ComparableStack(stack), new ItemStack(ModItems.dust));
				}
			}
		}
	}
	
	public static void registerOverrides() {

		ShredderRecipes.setRecipe(ModItems.scrap, new ItemStack(ModItems.dust));
		ShredderRecipes.setRecipe(ModItems.dust, new ItemStack(ModItems.dust));
		ShredderRecipes.setRecipe(Blocks.GLOWSTONE, new ItemStack(Items.GLOWSTONE_DUST, 4));
		ShredderRecipes.setRecipe(new ItemStack(Blocks.QUARTZ_BLOCK, 1, 0), new ItemStack(ModItems.powder_quartz, 4));
		ShredderRecipes.setRecipe(new ItemStack(Blocks.QUARTZ_BLOCK, 1, 1), new ItemStack(ModItems.powder_quartz, 4));
		ShredderRecipes.setRecipe(new ItemStack(Blocks.QUARTZ_BLOCK, 1, 2), new ItemStack(ModItems.powder_quartz, 4));
		ShredderRecipes.setRecipe(Blocks.QUARTZ_STAIRS, new ItemStack(ModItems.powder_quartz, 3));
		ShredderRecipes.setRecipe(new ItemStack(Blocks.STONE_SLAB, 1, 7), new ItemStack(ModItems.powder_quartz, 2));
		ShredderRecipes.setRecipe(Items.QUARTZ, new ItemStack(ModItems.powder_quartz));
		ShredderRecipes.setRecipe(Blocks.QUARTZ_ORE, new ItemStack(ModItems.powder_quartz, 2));
		ShredderRecipes.setRecipe(ModBlocks.ore_nether_fire, new ItemStack(ModItems.powder_fire, 6));
		ShredderRecipes.setRecipe(Blocks.PACKED_ICE, new ItemStack(ModItems.powder_ice, 1));
		ShredderRecipes.setRecipe(ModBlocks.brick_light, new ItemStack(Items.CLAY_BALL, 4));
		ShredderRecipes.setRecipe(ModBlocks.concrete, new ItemStack(Blocks.GRAVEL, 1));
		ShredderRecipes.setRecipe(ModBlocks.concrete_smooth, new ItemStack(Blocks.GRAVEL, 1));
		ShredderRecipes.setRecipe(ModBlocks.brick_concrete, new ItemStack(Blocks.GRAVEL, 1));
		ShredderRecipes.setRecipe(ModBlocks.brick_obsidian, new ItemStack(ModBlocks.gravel_obsidian, 1));
		ShredderRecipes.setRecipe(Blocks.OBSIDIAN, new ItemStack(ModBlocks.gravel_obsidian, 1));
		ShredderRecipes.setRecipe(Blocks.STONE, new ItemStack(Blocks.GRAVEL, 1));
		ShredderRecipes.setRecipe(Blocks.COBBLESTONE, new ItemStack(Blocks.GRAVEL, 1));
		ShredderRecipes.setRecipe(Blocks.STONEBRICK, new ItemStack(Blocks.GRAVEL, 1));
		ShredderRecipes.setRecipe(Blocks.GRAVEL, new ItemStack(Blocks.SAND, 1));
		ShredderRecipes.setRecipe(Blocks.SAND, new ItemStack(ModItems.dust, 2));
		ShredderRecipes.setRecipe(Blocks.BRICK_BLOCK, new ItemStack(Items.CLAY_BALL, 4));
		ShredderRecipes.setRecipe(Blocks.BRICK_STAIRS, new ItemStack(Items.CLAY_BALL, 3));
		ShredderRecipes.setRecipe(Items.FLOWER_POT, new ItemStack(Items.CLAY_BALL, 3));
		ShredderRecipes.setRecipe(Items.BRICK, new ItemStack(Items.CLAY_BALL, 1));
		ShredderRecipes.setRecipe(Blocks.SANDSTONE, new ItemStack(Blocks.SAND, 4));
		ShredderRecipes.setRecipe(Blocks.SANDSTONE_STAIRS, new ItemStack(Blocks.SAND, 6));
		ShredderRecipes.setRecipe(Blocks.CLAY, new ItemStack(Items.CLAY_BALL, 4));
		ShredderRecipes.setRecipe(Blocks.HARDENED_CLAY, new ItemStack(Items.CLAY_BALL, 4));
		ShredderRecipes.setRecipe(Blocks.TNT, new ItemStack(Items.GUNPOWDER, 5));
		ShredderRecipes.setRecipe(Items.BONE, new ItemStack(Items.DYE, 5, 15));
		ShredderRecipes.setRecipe(Blocks.CACTUS, new ItemStack(Items.DYE, 2, 2));
		ShredderRecipes.setRecipe(ModBlocks.stone_gneiss, new ItemStack(ModItems.powder_lithium_tiny, 1));
		ShredderRecipes.setRecipe(ModItems.powder_lapis, new ItemStack(ModItems.powder_cobalt_tiny, 1));
		ShredderRecipes.setRecipe(ModItems.fragment_neodymium, new ItemStack(ModItems.powder_neodymium_tiny, 1));
		ShredderRecipes.setRecipe(ModItems.fragment_cobalt, new ItemStack(ModItems.powder_cobalt_tiny, 1));
		ShredderRecipes.setRecipe(ModItems.fragment_niobium, new ItemStack(ModItems.powder_niobium_tiny, 1));
		ShredderRecipes.setRecipe(ModItems.fragment_cerium, new ItemStack(ModItems.powder_cerium_tiny, 1));
		ShredderRecipes.setRecipe(ModItems.fragment_lanthanium, new ItemStack(ModItems.powder_lanthanium_tiny, 1));
		ShredderRecipes.setRecipe(ModItems.fragment_actinium, new ItemStack(ModItems.powder_actinium_tiny, 1));
		ShredderRecipes.setRecipe(ModItems.fragment_boron, new ItemStack(ModItems.powder_boron_tiny, 1));
		ShredderRecipes.setRecipe(ModItems.fragment_meteorite, new ItemStack(ModItems.powder_meteorite_tiny, 1));
		ShredderRecipes.setRecipe(ModBlocks.block_meteor, new ItemStack(ModItems.powder_meteorite, 10));
		ShredderRecipes.setRecipe(Items.ENCHANTED_BOOK, new ItemStack(ModItems.powder_magic, 1));
		ShredderRecipes.setRecipe(ModItems.arc_electrode_burnt, new ItemStack(ModItems.powder_coal, 1));
		ShredderRecipes.setRecipe(ModBlocks.meteor_polished, new ItemStack(ModItems.powder_meteorite, 1));
		ShredderRecipes.setRecipe(ModBlocks.meteor_brick, new ItemStack(ModItems.powder_meteorite, 1));
		ShredderRecipes.setRecipe(ModBlocks.meteor_brick_mossy, new ItemStack(ModItems.powder_meteorite, 1));
		ShredderRecipes.setRecipe(ModBlocks.meteor_brick_cracked, new ItemStack(ModItems.powder_meteorite, 1));
		ShredderRecipes.setRecipe(ModBlocks.meteor_brick_chiseled, new ItemStack(ModItems.powder_meteorite, 1));
		ShredderRecipes.setRecipe(ModBlocks.meteor_pillar, new ItemStack(ModItems.powder_meteorite, 1));
		ShredderRecipes.setRecipe(ModBlocks.ore_tektite_osmiridium, new ItemStack(ModItems.powder_tektite, 1));
		ShredderRecipes.setRecipe(ModBlocks.ore_rare, new ItemStack(ModItems.powder_desh_mix, 1));
		ShredderRecipes.setRecipe(Blocks.DIAMOND_ORE, new ItemStack(ModBlocks.gravel_diamond, 2));
		ShredderRecipes.setRecipe(ModBlocks.boxcar, new ItemStack(ModItems.powder_steel, 32));
		ShredderRecipes.setRecipe(ModItems.ingot_schrabidate, new ItemStack(ModItems.powder_schrabidate, 1));
		ShredderRecipes.setRecipe(ModBlocks.block_schrabidate, new ItemStack(ModItems.powder_schrabidate, 9));
		ShredderRecipes.setRecipe(ModItems.coal_infernal, new ItemStack(ModItems.powder_coal, 3));
		ShredderRecipes.setRecipe(Items.REEDS, new ItemStack(Items.SUGAR, 2));
		ShredderRecipes.setRecipe(Items.FERMENTED_SPIDER_EYE, new ItemStack(ModItems.powder_poison, 3));
		ShredderRecipes.setRecipe(Items.POISONOUS_POTATO, new ItemStack(ModItems.powder_poison, 1));

		ShredderRecipes.setRecipe(ModBlocks.dirt_dead, new ItemStack(ModItems.scrap_oil, 1));
		ShredderRecipes.setRecipe(ModBlocks.dirt_oily, new ItemStack(ModItems.scrap_oil, 1));
		ShredderRecipes.setRecipe(ModBlocks.sand_dirty, new ItemStack(ModItems.scrap_oil, 1));
		ShredderRecipes.setRecipe(ModBlocks.sand_dirty_red, new ItemStack(ModItems.scrap_oil, 1));
		ShredderRecipes.setRecipe(ModBlocks.stone_cracked, new ItemStack(ModItems.scrap_oil, 1));
		ShredderRecipes.setRecipe(ModBlocks.stone_porous, new ItemStack(ModItems.scrap_oil, 1));

		ShredderRecipes.setRecipe(ModBlocks.deco_pipe, new ItemStack(ModItems.powder_steel, 1));
		ShredderRecipes.setRecipe(ModBlocks.deco_pipe_rusted, new ItemStack(ModItems.powder_steel, 1));
		ShredderRecipes.setRecipe(ModBlocks.deco_pipe_green, new ItemStack(ModItems.powder_steel, 1));
		ShredderRecipes.setRecipe(ModBlocks.deco_pipe_green_rusted, new ItemStack(ModItems.powder_steel, 1));
		ShredderRecipes.setRecipe(ModBlocks.deco_pipe_red, new ItemStack(ModItems.powder_steel, 1));
		ShredderRecipes.setRecipe(ModBlocks.deco_pipe_marked, new ItemStack(ModItems.powder_steel, 1));
		ShredderRecipes.setRecipe(ModBlocks.deco_pipe_rim, new ItemStack(ModItems.powder_steel, 1));
		ShredderRecipes.setRecipe(ModBlocks.deco_pipe_rim_rusted, new ItemStack(ModItems.powder_steel, 1));
		ShredderRecipes.setRecipe(ModBlocks.deco_pipe_rim_green, new ItemStack(ModItems.powder_steel, 1));
		ShredderRecipes.setRecipe(ModBlocks.deco_pipe_rim_green_rusted, new ItemStack(ModItems.powder_steel, 1));
		ShredderRecipes.setRecipe(ModBlocks.deco_pipe_rim_red, new ItemStack(ModItems.powder_steel, 1));
		ShredderRecipes.setRecipe(ModBlocks.deco_pipe_rim_marked, new ItemStack(ModItems.powder_steel, 1));
		ShredderRecipes.setRecipe(ModBlocks.deco_pipe_quad, new ItemStack(ModItems.powder_steel, 1));
		ShredderRecipes.setRecipe(ModBlocks.deco_pipe_quad_rusted, new ItemStack(ModItems.powder_steel, 1));
		ShredderRecipes.setRecipe(ModBlocks.deco_pipe_quad_green, new ItemStack(ModItems.powder_steel, 1));
		ShredderRecipes.setRecipe(ModBlocks.deco_pipe_quad_green_rusted, new ItemStack(ModItems.powder_steel, 1));
		ShredderRecipes.setRecipe(ModBlocks.deco_pipe_quad_red, new ItemStack(ModItems.powder_steel, 1));
		ShredderRecipes.setRecipe(ModBlocks.deco_pipe_quad_marked, new ItemStack(ModItems.powder_steel, 1));
		ShredderRecipes.setRecipe(ModBlocks.deco_pipe_framed, new ItemStack(ModItems.powder_steel, 1));
		ShredderRecipes.setRecipe(ModBlocks.deco_pipe_framed_rusted, new ItemStack(ModItems.powder_steel, 1));
		ShredderRecipes.setRecipe(ModBlocks.deco_pipe_framed_green, new ItemStack(ModItems.powder_steel, 1));
		ShredderRecipes.setRecipe(ModBlocks.deco_pipe_framed_green_rusted, new ItemStack(ModItems.powder_steel, 1));
		ShredderRecipes.setRecipe(ModBlocks.deco_pipe_framed_red, new ItemStack(ModItems.powder_steel, 1));
		ShredderRecipes.setRecipe(ModBlocks.deco_pipe_framed_marked, new ItemStack(ModItems.powder_steel, 1));
		
		ShredderRecipes.setRecipe(ModItems.ingot_schraranium, new ItemStack(ModItems.nugget_schrabidium, 2));
		ShredderRecipes.setRecipe(ModItems.crystal_coal, new ItemStack(ModItems.powder_coal, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_iron, new ItemStack(ModItems.powder_iron, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_gold, new ItemStack(ModItems.powder_gold, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_redstone, new ItemStack(Items.REDSTONE, 8));
		ShredderRecipes.setRecipe(ModItems.crystal_lapis, new ItemStack(ModItems.powder_lapis, 12));
		ShredderRecipes.setRecipe(ModItems.crystal_diamond, new ItemStack(ModItems.powder_diamond, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_uranium, new ItemStack(ModItems.powder_uranium, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_plutonium, new ItemStack(ModItems.powder_plutonium, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_thorium, new ItemStack(ModItems.powder_thorium, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_titanium, new ItemStack(ModItems.powder_titanium, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_sulfur, new ItemStack(ModItems.sulfur, 8));
		ShredderRecipes.setRecipe(ModItems.crystal_niter, new ItemStack(ModItems.niter, 8));
		ShredderRecipes.setRecipe(ModItems.crystal_copper, new ItemStack(ModItems.powder_copper, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_tungsten, new ItemStack(ModItems.powder_tungsten, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_aluminium, new ItemStack(ModItems.powder_aluminium, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_fluorite, new ItemStack(ModItems.fluorite, 8));
		ShredderRecipes.setRecipe(ModItems.crystal_beryllium, new ItemStack(ModItems.powder_beryllium, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_schraranium, new ItemStack(ModItems.nugget_schrabidium, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_lead, new ItemStack(ModItems.powder_lead, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_schrabidium, new ItemStack(ModItems.powder_schrabidium, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_rare, new ItemStack(ModItems.powder_desh_mix, 2));
		ShredderRecipes.setRecipe(ModItems.crystal_phosphorus, new ItemStack(ModItems.powder_fire, 8));
		ShredderRecipes.setRecipe(ModItems.crystal_trixite, new ItemStack(ModItems.powder_plutonium, 6));
		ShredderRecipes.setRecipe(ModItems.crystal_lithium, new ItemStack(ModItems.powder_lithium, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_starmetal, new ItemStack(ModItems.powder_dura_steel, 6));
		ShredderRecipes.setRecipe(ModItems.crystal_cobalt, new ItemStack(ModItems.powder_cobalt, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_cinnabar, new ItemStack(ModItems.cinnabar, 3));
		ShredderRecipes.setRecipe(ModItems.crystal_asbestos, new ItemStack(ModItems.powder_asbestos, 3));
		
		ShredderRecipes.setRecipe(ModBlocks.steel_poles, new ItemStack(ModItems.powder_steel_tiny, 3));
		ShredderRecipes.setRecipe(ModBlocks.pole_top, new ItemStack(ModItems.powder_tungsten, 4));
		ShredderRecipes.setRecipe(ModBlocks.tape_recorder, new ItemStack(ModItems.powder_steel, 1));
		ShredderRecipes.setRecipe(ModBlocks.pole_satellite_receiver, new ItemStack(ModItems.powder_steel, 5));
		ShredderRecipes.setRecipe(ModBlocks.steel_roof, new ItemStack(ModItems.powder_steel_tiny, 13));
		ShredderRecipes.setRecipe(ModBlocks.steel_wall, new ItemStack(ModItems.powder_steel_tiny, 13));
		ShredderRecipes.setRecipe(ModBlocks.steel_corner, new ItemStack(ModItems.powder_steel_tiny, 26));
		ShredderRecipes.setRecipe(ModBlocks.steel_beam, new ItemStack(ModItems.powder_steel_tiny, 3));
		ShredderRecipes.setRecipe(ModBlocks.steel_scaffold, new ItemStack(ModItems.powder_steel_tiny, 7));
		ShredderRecipes.setRecipe(ModItems.coil_copper, new ItemStack(ModItems.powder_red_copper, 1));
		ShredderRecipes.setRecipe(ModItems.coil_copper_torus, new ItemStack(ModItems.powder_red_copper, 2));
		ShredderRecipes.setRecipe(ModItems.coil_advanced_alloy, new ItemStack(ModItems.powder_advanced_alloy, 1));
		ShredderRecipes.setRecipe(ModItems.coil_advanced_torus, new ItemStack(ModItems.powder_advanced_alloy, 2));
		ShredderRecipes.setRecipe(ModItems.coil_gold, new ItemStack(ModItems.powder_gold, 1));
		ShredderRecipes.setRecipe(ModItems.coil_gold_torus, new ItemStack(ModItems.powder_gold, 2));
		ShredderRecipes.setRecipe(ModItems.coil_tungsten, new ItemStack(ModItems.powder_tungsten, 1));
		ShredderRecipes.setRecipe(ModItems.coil_magnetized_tungsten, new ItemStack(ModItems.powder_magnetized_tungsten, 1));
		ShredderRecipes.setRecipe(ModBlocks.crate_iron, new ItemStack(ModItems.powder_iron, 8));
		ShredderRecipes.setRecipe(ModBlocks.crate_steel, new ItemStack(ModItems.powder_steel, 8));
		ShredderRecipes.setRecipe(ModBlocks.crate_tungsten, new ItemStack(ModItems.powder_tungsten, 36));
		ShredderRecipes.setRecipe(Blocks.ANVIL, new ItemStack(ModItems.powder_iron, 31));
		ShredderRecipes.setRecipe(ModBlocks.chain, new ItemStack(ModItems.powder_steel_tiny, 1));
		ShredderRecipes.setRecipe(ModBlocks.steel_grate, new ItemStack(ModItems.powder_steel_tiny, 3));
		ShredderRecipes.setRecipe(ModItems.pipes_steel, new ItemStack(ModItems.powder_steel, 27));
		
		ShredderRecipes.setRecipe(ModItems.ingot_schrabidate, new ItemStack(ModItems.powder_schrabidate, 1));
		ShredderRecipes.setRecipe(ModBlocks.block_schrabidate, new ItemStack(ModItems.powder_schrabidate, 9));
		ShredderRecipes.setRecipe(ModItems.ingot_ac227, new ItemStack(ModItems.powder_ac227, 1));
		ShredderRecipes.setRecipe(ModItems.ingot_co60, new ItemStack(ModItems.powder_co60, 1));
		ShredderRecipes.setRecipe(ModItems.ingot_ra226, new ItemStack(ModItems.powder_ra226, 1));
		ShredderRecipes.setRecipe(ModItems.ingot_sr90, new ItemStack(ModItems.powder_sr90, 1));
		ShredderRecipes.setRecipe(ModItems.ingot_ra226, new ItemStack(ModItems.powder_ra226, 1));
		ShredderRecipes.setRecipe(ModItems.ingot_pb209, new ItemStack(ModItems.powder_pb209, 1));
		ShredderRecipes.setRecipe(ModItems.ingot_astatine, new ItemStack(ModItems.powder_astatine, 1));

		ShredderRecipes.setRecipe(ModItems.ingot_tennessine, new ItemStack(ModItems.powder_tennessine, 1));
		ShredderRecipes.setRecipe(ModItems.ingot_bromine, new ItemStack(ModItems.powder_bromine, 1));
		ShredderRecipes.setRecipe(ModItems.ingot_caesium, new ItemStack(ModItems.powder_caesium, 1));
		ShredderRecipes.setRecipe(ModItems.ingot_cerium, new ItemStack(ModItems.powder_cerium, 1));
		ShredderRecipes.setRecipe(ModItems.ingot_co60, new ItemStack(ModItems.powder_co60, 1));
		ShredderRecipes.setRecipe(ModItems.ingot_strontium, new ItemStack(ModItems.powder_strontium, 1));
		ShredderRecipes.setRecipe(ModItems.ingot_sr90, new ItemStack(ModItems.powder_sr90, 1));
		ShredderRecipes.setRecipe(ModItems.ingot_iodine, new ItemStack(ModItems.powder_iodine, 1));
		ShredderRecipes.setRecipe(ModItems.ingot_i131, new ItemStack(ModItems.powder_i131, 1));
		ShredderRecipes.setRecipe(ModItems.ingot_radspice, new ItemStack(ModItems.powder_radspice, 1));
		ShredderRecipes.setRecipe(ModItems.ingot_polymer, new ItemStack(ModItems.powder_polymer, 1));
		ShredderRecipes.setRecipe(ModItems.ingot_bakelite, new ItemStack(ModItems.powder_bakelite, 1));

		ShredderRecipes.setRecipe(ModBlocks.ore_nether_coal, new ItemStack(ModItems.coal_infernal, 2));
		ShredderRecipes.setRecipe(ModBlocks.ore_cinnabar, new ItemStack(ModItems.cinnabar, 2));
		ShredderRecipes.setRecipe(ModBlocks.block_slag, new ItemStack(ModItems.powder_cement, 4));

		for(int i = 0; i < 16; i++) {
			ShredderRecipes.setRecipe(new ItemStack(Blocks.STAINED_HARDENED_CLAY, 1, i), new ItemStack(Items.CLAY_BALL, 4));
			ShredderRecipes.setRecipe(new ItemStack(Blocks.WOOL, 1, i), new ItemStack(Items.STRING, 4));
		}
		setRecipe("crystalEnder", "dustEnder");
		setRecipe("crystalFluix", "dustFluix");
		setRecipe("crystalCertusQuartz", "dustCertusQuartz");
		setRecipe("enderpearl", "dustEnderPearl");
	}
	
	public static ItemStack getDustByName(String name) {
		
		return getOredictByName("dust" + name);
	}

	public static ItemStack getTinyDustByName(String name) {
		
		return getOredictByName("dustTiny" + name);
	}

	public static ItemStack getOredictByName(String name) {
		
		List<ItemStack> matches = OreDictionary.getOres(name);
		if(matches != null && !matches.isEmpty())
			return matches.get(0).copy();
		
		return new ItemStack(ModItems.scrap);
	}
	
	public static void setRecipe(Item in, ItemStack out) {
		
		shredderRecipes.put(new ComparableStack(in), out);
	}
	
	public static void setRecipe(Block in, ItemStack out) {
		
		shredderRecipes.put(new ComparableStack(in), out);
	}
	
	public static void setRecipe(ItemStack in, ItemStack out) {
		
		shredderRecipes.put(new ComparableStack(in), out);
	}

	public static void setRecipe(String in, String out) {
		if(OreDictionary.doesOreNameExist(in) && OreDictionary.doesOreNameExist(out)) 
			setRecipe(getOredictByName(in), getOredictByName(out));
	}

	public static void removeRecipe(ItemStack in) {
		
		shredderRecipes.remove(new ComparableStack(in));
	}
	
	public static List<ShredderRecipe> getShredderRecipes() {
		
		if(jeiShredderRecipes == null){
			jeiShredderRecipes = new ArrayList<ShredderRecipe>();
			for(Entry<ComparableStack, ItemStack> e : shredderRecipes.entrySet()){
				jeiShredderRecipes.add(new ShredderRecipe(e.getKey().toStack(), e.getValue()));
			}
		}
		
		return jeiShredderRecipes;
	}
	
	public static ItemStack getShredderResult(ItemStack stack) {
		
		if(stack == null || stack.getItem() == null || stack.isEmpty())
			return new ItemStack(ModItems.scrap);
		
		ItemStack sta = shredderRecipes.get(new ComparableStack(stack).makeSingular());
		
		return sta == null ? new ItemStack(ModItems.scrap) : sta;
	}
	
	public static class ShredderRecipe implements IRecipeWrapper {
		
		private final ItemStack input;
		private final ItemStack output;
		
		public ShredderRecipe(ItemStack input, ItemStack output) {
			this.input = input;
			this.output = output; 
		}
		
		@Override
		public void getIngredients(IIngredients ingredients) {
			ingredients.setInput(VanillaTypes.ITEM, input);
			ingredients.setOutput(VanillaTypes.ITEM, output);
		}
		
	}
}
