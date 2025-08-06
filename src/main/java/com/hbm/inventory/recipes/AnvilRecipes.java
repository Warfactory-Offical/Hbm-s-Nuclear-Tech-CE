package com.hbm.inventory.recipes;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.ItemEnums.EnumCircuitType;
import com.hbm.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

import static com.hbm.inventory.OreDictManager.*;
import static net.minecraft.item.ItemStack.areItemStacksEqual;

public class AnvilRecipes {

	private static List<AnvilSmithingRecipe> smithingRecipes = new ArrayList<>();
	private static List<AnvilConstructionRecipe> constructionRecipes = new ArrayList<>();
	
	public static void register() {
		registerSmithing();
		registerConstruction();
	}
	
	/*
	 *      //////  //      //  //  //////  //  //  //  //    //  //////
	 *     //      ////  ////  //    //    //  //  //  ////  //  //
	 *    //////  //  //  //  //    //    //////  //  //  ////  //  //
	 *       //  //      //  //    //    //  //  //  //    //  //  //
	 *  //////  //      //  //    //    //  //  //  //    //  //////
	 */
	public static void registerSmithing() {
		
		Block[] anvils = new Block[]{ModBlocks.anvil_iron, ModBlocks.anvil_lead};
		
		for(Block anvil : anvils) {
			smithingRecipes.add(new AnvilSmithingRecipe(1, new ItemStack(ModBlocks.anvil_bismuth, 1), new ComparableStack(anvil), new ComparableStack(ModItems.ingot_bismuth, 10)));
			smithingRecipes.add(new AnvilSmithingRecipe(1, new ItemStack(ModBlocks.anvil_dnt, 1), new ComparableStack(anvil), new OreDictStack(DNT.ingot(), 10)));
			smithingRecipes.add(new AnvilSmithingRecipe(1, new ItemStack(ModBlocks.anvil_osmiridium, 1), new ComparableStack(anvil), new OreDictStack(OSMIRIDIUM.ingot(), 10)));
			smithingRecipes.add(new AnvilSmithingRecipe(1, new ItemStack(ModBlocks.anvil_ferrouranium, 1), new ComparableStack(anvil), new OreDictStack(FERRO.ingot(), 10)));
			smithingRecipes.add(new AnvilSmithingRecipe(1, new ItemStack(ModBlocks.anvil_meteorite, 1), new ComparableStack(anvil), new ComparableStack(ModItems.ingot_meteorite, 10)));
			smithingRecipes.add(new AnvilSmithingRecipe(1, new ItemStack(ModBlocks.anvil_schrabidate, 1), new ComparableStack(anvil), new OreDictStack(SBD.ingot(), 10)));
			smithingRecipes.add(new AnvilSmithingRecipe(1, new ItemStack(ModBlocks.anvil_starmetal, 1), new ComparableStack(anvil), new OreDictStack(STAR.ingot(), 10)));
			smithingRecipes.add(new AnvilSmithingRecipe(1, new ItemStack(ModBlocks.anvil_steel, 1), new ComparableStack(anvil), new OreDictStack(STEEL.ingot(), 10)));
		}
		
		for(int i = 0; i < 9; i++)
			smithingRecipes.add(new AnvilSmithingHotRecipe(3, new ItemStack(ModItems.ingot_steel_dusted, 1, i + 1),
					new ComparableStack(ModItems.ingot_steel_dusted, 1, i), new ComparableStack(ModItems.ingot_steel_dusted, 1, i)));
		
		smithingRecipes.add(new AnvilSmithingHotRecipe(3, new ItemStack(ModItems.ingot_chainsteel, 1),
				new ComparableStack(ModItems.ingot_steel_dusted, 1, 9), new ComparableStack(ModItems.ingot_steel_dusted, 1, 9)));
		
		smithingRecipes.add(new AnvilSmithingHotRecipe(3, new ItemStack(ModItems.ingot_meteorite_forged, 1), new ComparableStack(ModItems.ingot_meteorite), new ComparableStack(ModItems.ingot_meteorite)));
		smithingRecipes.add(new AnvilSmithingHotRecipe(3, new ItemStack(ModItems.blade_meteorite, 1), new ComparableStack(ModItems.ingot_meteorite_forged), new ComparableStack(ModItems.ingot_meteorite_forged)));
		smithingRecipes.add(new AnvilSmithingHotRecipe(3, new ItemStack(ModItems.meteorite_sword_reforged, 1), new ComparableStack(ModItems.meteorite_sword_seared), new ComparableStack(ModItems.ingot_meteorite_forged)));
		smithingRecipes.add(new AnvilSmithingRecipe(1916169, new ItemStack(ModItems.wings_murk, 1), new ComparableStack(ModItems.wings_limp), new ComparableStack(ModItems.particle_tachyon)));

		smithingRecipes.add(new AnvilSmithingMold(0, new OreDictStack(IRON.nugget()), new OreDictStack("nugget")));
		smithingRecipes.add(new AnvilSmithingMold(1, new OreDictStack(U.billet()),  new OreDictStack("billet")));
		smithingRecipes.add(new AnvilSmithingMold(2, new OreDictStack(IRON.ingot()),  new OreDictStack("ingot")));
		smithingRecipes.add(new AnvilSmithingMold(3, new OreDictStack(IRON.plate()),  new OreDictStack("plate")));
		smithingRecipes.add(new AnvilSmithingMold(4, new OreDictStack(CU.wireFine(), 1), new OreDictStack("wireFine")));
		smithingRecipes.add(new AnvilSmithingMold(5, new ComparableStack(ModItems.blade_titanium), new ItemStack[] {
				new ItemStack(ModItems.blade_titanium),
				new ItemStack(ModItems.blade_tungsten)
		}));
		smithingRecipes.add(new AnvilSmithingMold(6, new ComparableStack(ModItems.blades_iron), new ItemStack[] {
				new ItemStack(ModItems.blades_aluminum),
				new ItemStack(ModItems.blades_gold),
				new ItemStack(ModItems.blades_iron),
				new ItemStack(ModItems.blades_steel),
				new ItemStack(ModItems.blades_titanium),
				new ItemStack(ModItems.blades_advanced_alloy),
				new ItemStack(ModItems.blades_combine_steel),
				new ItemStack(ModItems.blades_schrabidium),
				new ItemStack(ModItems.blades_desh)
		}));
		smithingRecipes.add(new AnvilSmithingMold(7, new ComparableStack(ModItems.stamp_iron_flat), new ItemStack[] {
				new ItemStack(ModItems.stamp_stone_flat),
				new ItemStack(ModItems.stamp_iron_flat),
				new ItemStack(ModItems.stamp_steel_flat),
				new ItemStack(ModItems.stamp_titanium_flat),
				new ItemStack(ModItems.stamp_obsidian_flat),
				new ItemStack(ModItems.stamp_desh_flat)
		}));
		smithingRecipes.add(new AnvilSmithingMold(8, new OreDictStack(STEEL.shell()), new OreDictStack(MaterialShapes.SHELL.name())));
		smithingRecipes.add(new AnvilSmithingMold(9, new OreDictStack(STEEL.pipe()), new OreDictStack(MaterialShapes.PIPE.name())));
		smithingRecipes.add(new AnvilSmithingMold(10, new OreDictStack(IRON.ingot(), 9), new OreDictStack("ingot", 9)));
		smithingRecipes.add(new AnvilSmithingMold(11, new OreDictStack(IRON.plate(), 9), new OreDictStack("plate", 9)));
		smithingRecipes.add(new AnvilSmithingMold(12, new OreDictStack(IRON.block()), new OreDictStack("block")));
		smithingRecipes.add(new AnvilSmithingMold(13, new ComparableStack(ModItems.pipes_steel), new ItemStack(ModItems.pipes_steel)));
		//smithingRecipes.add(new AnvilSmithingMold(14, new OreDictStack(ALLOY.wireDense(), 9),  new OreDictStack("wireDense", 9)));
		//smithingRecipes.add(new AnvilSmithingMold(15, new OreDictStack(IRON.block()), new OreDictStack("block")));
		
		//smithingRecipes.add(new AnvilSmithingMold(16, new ComparableStack(ModItems.pipes_steel), new ItemStack[] {new ItemStack(ModItems.pipes_steel)}));
		//smithingRecipes.add(new AnvilSmithingMold(17, new ComparableStack(ModItems.casing_357), new ItemStack[] {new ItemStack(ModItems.casing_357)}));
		//smithingRecipes.add(new AnvilSmithingMold(18, new ComparableStack(ModItems.casing_44), new ItemStack[] {new ItemStack(ModItems.casing_44)}));
		smithingRecipes.add(new AnvilSmithingMold(19, new OreDictStack(IRON.plateCast(), 1),  new OreDictStack("plateTriple", 1)));
		smithingRecipes.add(new AnvilSmithingMold(20, new OreDictStack(ALLOY.wireDense(), 1),  new OreDictStack("wireDense", 1)));
		smithingRecipes.add(new AnvilSmithingMold(21, new OreDictStack(ALLOY.wireDense(), 9),  new OreDictStack("wireDense", 9)));
		
		
		smithingRecipes.add(new AnvilSmithingCyanideRecipe());
		smithingRecipes.add(new AnvilSmithingRenameRecipe());
	}
	
	/*
	 *      //////  //////  //    //  //////  //////  ////    //  //  //////  //////  //  //////  //    //
	 *     //      //  //  ////  //  //        //    //  //  //  //  //        //    //  //  //  ////  //
	 *    //      //  //  //  ////  //////    //    ////    //  //  //        //    //  //  //  //  ////
	 *   //      //  //  //    //      //    //    //  //  //  //  //        //    //  //  //  //    //
	 *  //////  //////  //    //  //////    //    //  //  //////  //////    //    //  //////  //    //
	 */
	public static void registerConstruction() {
		
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(IRON.ingot()), new AnvilOutput(new ItemStack(ModItems.plate_iron))).setTier(3));
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(GOLD.ingot()), new AnvilOutput(new ItemStack(ModItems.plate_gold))).setTier(3));
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(TI.ingot()), new AnvilOutput(new ItemStack(ModItems.plate_titanium))).setTier(3));
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(AL.ingot()), new AnvilOutput(new ItemStack(ModItems.plate_aluminium))).setTier(3));
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(STEEL.ingot()), new AnvilOutput(new ItemStack(ModItems.plate_steel))).setTier(3));
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(PB.ingot()), new AnvilOutput(new ItemStack(ModItems.plate_lead))).setTier(3));
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(CU.ingot()), new AnvilOutput(new ItemStack(ModItems.plate_copper))).setTier(3));
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(ALLOY.ingot()), new AnvilOutput(new ItemStack(ModItems.plate_advanced_alloy))).setTier(3));
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(SA326.ingot()), new AnvilOutput(new ItemStack(ModItems.plate_schrabidium))).setTier(3));
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(CMB.ingot()), new AnvilOutput(new ItemStack(ModItems.plate_combine_steel))).setTier(3));
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(BIGMT.ingot()), new AnvilOutput(new ItemStack(ModItems.plate_saturnite))).setTier(3));

		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(AL.ingot()), new AnvilOutput(new ItemStack(ModItems.wire_fine, 8, Mats.MAT_ALUMINIUM.id))).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(CU.ingot()), new AnvilOutput(new ItemStack(ModItems.wire_fine, 8, Mats.MAT_COPPER.id))).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(W.ingot()), new AnvilOutput(new ItemStack(ModItems.wire_fine, 8, Mats.MAT_TUNGSTEN.id))).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(MINGRADE.ingot()), new AnvilOutput(new ItemStack(ModItems.wire_fine, 8, Mats.MAT_MINGRADE.id))).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(ALLOY.ingot()), new AnvilOutput(new ItemStack(ModItems.wire_fine, 8, Mats.MAT_ALLOY.id))).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(GOLD.ingot()), new AnvilOutput(new ItemStack(ModItems.wire_fine, 8, Mats.MAT_GOLD.id))).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(SA326.ingot()), new AnvilOutput(new ItemStack(ModItems.wire_fine, 8, Mats.MAT_SCHRABIDIUM.id))).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(MAGTUNG.ingot()), new AnvilOutput(new ItemStack(ModItems.wire_fine, 8, Mats.MAT_MAGTUNG.id))).setTier(4));

		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(COAL.dust()), new AnvilOutput(new ItemStack(Items.COAL))).setTier(3));
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(NETHERQUARTZ.dust()), new AnvilOutput(new ItemStack(Items.QUARTZ))).setTier(3));
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(LAPIS.dust()), new AnvilOutput(new ItemStack(Items.DYE, 1, 4))).setTier(3));
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(DIAMOND.dust()), new AnvilOutput(new ItemStack(Items.DIAMOND))).setTier(3));
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(EMERALD.dust()), new AnvilOutput(new ItemStack(Items.EMERALD))).setTier(3));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new OreDictStack(RAREEARTH.gem()),
				new AnvilOutput[] {
						new AnvilOutput(new ItemStack(ModItems.fragment_boron)),
						new AnvilOutput(new ItemStack(ModItems.fragment_boron), 0.5F),
						new AnvilOutput(new ItemStack(ModItems.fragment_cobalt)),
						new AnvilOutput(new ItemStack(ModItems.fragment_cobalt), 0.5F),
						new AnvilOutput(new ItemStack(ModItems.fragment_neodymium), 0.5F),
						new AnvilOutput(new ItemStack(ModItems.fragment_niobium), 0.5F),
						new AnvilOutput(new ItemStack(ModItems.fragment_cerium), 0.4F),
						new AnvilOutput(new ItemStack(ModItems.fragment_lanthanium), 0.3F),
						new AnvilOutput(new ItemStack(ModItems.fragment_actinium), 0.3F)
						
				}
		).setTier(2));

		registerConstructionRecipes();
		registerConstructionAmmo();
		registerConstructionUpgrades();
		registerConstructionRecycling();
	}
	
	public static void registerConstructionRecipes() {

		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(AL.ingot(), 1), new AnvilOutput(new ItemStack(ModBlocks.deco_aluminium))).setTier(3).setOverlay(OverlayType.CONSTRUCTION));
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(BE.ingot(), 1), new AnvilOutput(new ItemStack(ModBlocks.deco_beryllium))).setTier(3).setOverlay(OverlayType.CONSTRUCTION));
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(PB.ingot(), 1), new AnvilOutput(new ItemStack(ModBlocks.deco_lead))).setTier(3).setOverlay(OverlayType.CONSTRUCTION));
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(MINGRADE.ingot(), 1), new AnvilOutput(new ItemStack(ModBlocks.deco_red_copper))).setTier(3).setOverlay(OverlayType.CONSTRUCTION));
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(STEEL.ingot(), 1), new AnvilOutput(new ItemStack(ModBlocks.deco_steel))).setTier(3).setOverlay(OverlayType.CONSTRUCTION));
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(TI.ingot(), 1), new AnvilOutput(new ItemStack(ModBlocks.deco_titanium))).setTier(3).setOverlay(OverlayType.CONSTRUCTION));
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(W.ingot(), 1), new AnvilOutput(new ItemStack(ModBlocks.deco_tungsten))).setTier(3).setOverlay(OverlayType.CONSTRUCTION));
		float decoToIngotChance = (float) GeneralConfig.decoToIngotRate / 100.0F;
		constructionRecipes.add(new AnvilConstructionRecipe(new ComparableStack(ModBlocks.deco_aluminium, 1), new AnvilOutput[] {new AnvilOutput(new ItemStack(ModItems.ingot_aluminium), decoToIngotChance)}).setTier(1).setOverlay(OverlayType.CONSTRUCTION));
		constructionRecipes.add(new AnvilConstructionRecipe(new ComparableStack(ModBlocks.deco_beryllium, 1), new AnvilOutput[] {new AnvilOutput(new ItemStack(ModItems.ingot_beryllium), decoToIngotChance)}).setTier(1).setOverlay(OverlayType.CONSTRUCTION));
		constructionRecipes.add(new AnvilConstructionRecipe(new ComparableStack(ModBlocks.deco_lead, 1), new AnvilOutput[] {new AnvilOutput(new ItemStack(ModItems.ingot_lead), decoToIngotChance)}).setTier(1).setOverlay(OverlayType.CONSTRUCTION));
		constructionRecipes.add(new AnvilConstructionRecipe(new ComparableStack(ModBlocks.deco_red_copper, 1), new AnvilOutput[] {new AnvilOutput(new ItemStack(ModItems.ingot_red_copper), decoToIngotChance)}).setTier(1).setOverlay(OverlayType.CONSTRUCTION));
		constructionRecipes.add(new AnvilConstructionRecipe(new ComparableStack(ModBlocks.deco_steel, 1), new AnvilOutput[] {new AnvilOutput(new ItemStack(ModItems.ingot_steel), decoToIngotChance)}).setTier(1).setOverlay(OverlayType.CONSTRUCTION));
		constructionRecipes.add(new AnvilConstructionRecipe(new ComparableStack(ModBlocks.deco_titanium, 1), new AnvilOutput[] {new AnvilOutput(new ItemStack(ModItems.ingot_titanium), decoToIngotChance)}).setTier(1).setOverlay(OverlayType.CONSTRUCTION));
		constructionRecipes.add(new AnvilConstructionRecipe(new ComparableStack(ModBlocks.deco_tungsten, 1), new AnvilOutput[] {new AnvilOutput(new ItemStack(ModItems.ingot_tungsten), decoToIngotChance)}).setTier(1).setOverlay(OverlayType.CONSTRUCTION));

		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {new OreDictStack(DNT.ingot(), 4), new ComparableStack(ModBlocks.depth_brick)},
				new AnvilOutput(new ItemStack(ModBlocks.depth_dnt))).setTier(1916169));

		for(NTMMaterial mat : Mats.orderedList) if(mat.autogen.contains(MaterialShapes.SHELL)) constructionRecipes.add(new AnvilConstructionRecipe(
				new OreDictStack(MaterialShapes.PLATE.name() + mat.names[0], 4),
				new AnvilOutput(new ItemStack(ModItems.shell, 1, mat.id))).setTier(1));
		for(NTMMaterial mat : Mats.orderedList) if(mat.autogen.contains(MaterialShapes.PIPE)) {
			String key = (OreDictionary.doesOreNameExist(MaterialShapes.PLATE.name() + mat.names[0]) ?
					MaterialShapes.PLATE.name() + mat.names[0] : MaterialShapes.INGOT.name() + mat.names[0]);
			constructionRecipes.add(new AnvilConstructionRecipe(
					new OreDictStack(key, 3),
					new AnvilOutput(new ItemStack(ModItems.pipe, 1, mat.id))).setTier(1));
		}

		constructionRecipes.add(new AnvilConstructionRecipe(
				new OreDictStack(CU.plate(), 4),
				new AnvilOutput(new ItemStack(ModItems.board_copper))).setTier(1));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModItems.coil_copper, 2),
				new AnvilOutput(new ItemStack(ModItems.coil_copper_torus))).setTier(1).setOverlay(OverlayType.CONSTRUCTION));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModItems.coil_advanced_alloy, 2),
				new AnvilOutput(new ItemStack(ModItems.coil_advanced_torus))).setTier(1).setOverlay(OverlayType.CONSTRUCTION));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModItems.coil_gold, 2),
				new AnvilOutput(new ItemStack(ModItems.coil_gold_torus))).setTier(1).setOverlay(OverlayType.CONSTRUCTION));

		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {new OreDictStack(IRON.plate(), 2), new ComparableStack(ModItems.coil_copper), new ComparableStack(ModItems.coil_copper_torus)},
				new AnvilOutput(new ItemStack(ModItems.motor, 2))).setTier(1));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {new ComparableStack(ModItems.motor), new OreDictStack(ANY_PLASTIC.ingot(), 2), new OreDictStack(DESH.ingot(), 2), new ComparableStack(ModItems.coil_gold_torus)},
				new AnvilOutput(new ItemStack(ModItems.motor_desh, 1))).setTier(3));

		pullFromAssembler(new ComparableStack(ModItems.filter_coal), 2);
		pullFromAssembler(new ComparableStack(ModItems.thermo_element), 2);
		
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {
						new ComparableStack(Blocks.STONEBRICK, 4),
						new ComparableStack(ModItems.ingot_firebrick, 4),
						new ComparableStack(ModItems.board_copper, 2)
				},
				new AnvilOutput(new ItemStack(ModBlocks.machine_difurnace_off))).setTier(1));
		
		int ukModifier = 1;

		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {
						new OreDictStack(STEEL.ingot(), 8 * ukModifier),
						new OreDictStack(CU.plate(), 4 * ukModifier),
						new ComparableStack(ModItems.motor, 2 * ukModifier),
						new ComparableStack(ModItems.circuit, 4 * ukModifier, EnumCircuitType.VACUUM_TUBE.ordinal())
				}, new AnvilOutput(new ItemStack(ModBlocks.machine_assembler))).setTier(2));

		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {
						new OreDictStack(KEY_COBBLESTONE, 8),
						new OreDictStack(KEY_PLANKS, 16),
						new OreDictStack(CU.plate(), 8),
						new OreDictStack(PB.pipe(), 2)
				}, new AnvilOutput(new ItemStack(ModBlocks.pump_steam))).setTier(2));

		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {
						new ComparableStack(Blocks.STONEBRICK, 8),
						new OreDictStack(STEEL.plate(), 16),
						new OreDictStack(PB.pipe(), 4),
						new ComparableStack(ModItems.motor, 2),
						new ComparableStack(ModItems.circuit, 4, EnumCircuitType.VACUUM_TUBE.ordinal())
				}, new AnvilOutput(new ItemStack(ModBlocks.pump_electric))).setTier(3));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {
						new OreDictStack(KEY_PLANKS, 16),
						new OreDictStack(STEEL.plate(), 6),
						new OreDictStack(CU.ingot(), 8),
						new OreDictStack(IRON.ingot(), 4),
						new ComparableStack(ModItems.sawblade)
				}, new AnvilOutput(new ItemStack(ModBlocks.machine_sawmill))).setTier(2));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {
						new OreDictStack(STEEL.plateCast(), 2),
						new ComparableStack(ModItems.coil_copper, 4),
						new OreDictStack(W.bolt(), 4),
						new ComparableStack(ModItems.circuit, 2, EnumCircuitType.VACUUM_TUBE.ordinal())
				}, new AnvilOutput(new ItemStack(ModBlocks.machine_soldering_station))).setTier(2));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {
						new OreDictStack(STEEL.plateCast(), 4),
						new OreDictStack(W.ingot(), 8),
						new ComparableStack(ModBlocks.machine_transformer, 1),
						new ComparableStack(ModItems.arc_electrode, 2)
				}, new AnvilOutput(new ItemStack(ModBlocks.machine_arc_welder))).setTier(2));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {
						new OreDictStack(STEEL.plateCast(), 8),
						new OreDictStack(CU.ingot(), 8),
						new OreDictStack(ANY_PLASTIC.ingot(), 4)
				}, new AnvilOutput(new ItemStack(ModBlocks.machine_industrial_boiler))).setTier(3));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {
						new OreDictStack(STEEL.plate(), 4),
						new OreDictStack(IRON.ingot(), 12),
						new OreDictStack(CU.ingot(), 2),
						new ComparableStack(ModItems.circuit, 2, EnumCircuitType.VACUUM_TUBE.ordinal()),
						new ComparableStack(ModItems.sawblade)
				}, new AnvilOutput(new ItemStack(ModBlocks.machine_autosaw))).setTier(2));

		pullFromAssembler(new ComparableStack(ModBlocks.machine_diesel), 2);
		
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {
						new ComparableStack(Blocks.FURNACE),
						new OreDictStack(STEEL.plate(), 8),
						new OreDictStack(CU.ingot(), 8)
				}, new AnvilOutput(new ItemStack(ModBlocks.heater_firebox))).setTier(2));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[]{
						new ComparableStack(ModItems.ingot_firebrick, 16),
						new OreDictStack(STEEL.plate(), 4),
						new OreDictStack(CU.ingot(), 8),
				} ,new AnvilOutput(new ItemStack(ModBlocks.heater_oven))).setTier(2));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[]{
						new ComparableStack(ModItems.tank_steel, 4),
						new ComparableStack(ModItems.pipes_steel, 1),
						new OreDictStack(TI.ingot(), 12),
						new OreDictStack(CU.ingot(), 8)
				} ,new AnvilOutput(new ItemStack(ModBlocks.heater_oilburner))).setTier(2));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {
						new ComparableStack(Blocks.STONE, 8),
						new OreDictStack(STEEL.plate(), 2),
						new OreDictStack(IRON.ingot(), 4)
				}, new AnvilOutput(new ItemStack(ModBlocks.machine_ashpit))).setTier(2));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {
						new OreDictStack(ANY_PLASTIC.ingot(), 4),
						new OreDictStack(CU.ingot(), 8),
						new OreDictStack(STEEL.plate528(), 8),
						new ComparableStack(ModItems.coil_tungsten, 8),
						new ComparableStack(ModItems.circuit, 1, EnumCircuitType.BASIC.ordinal())
				}, new AnvilOutput(new ItemStack(ModBlocks.heater_electric))).setTier(3));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {
						new OreDictStack(RUBBER.ingot(), 4),
						new OreDictStack(CU.ingot(), 16),
						new OreDictStack(STEEL.plate(), 16),
						new ComparableStack(ModItems.pipes_steel, 1),
				}, new AnvilOutput(new ItemStack(ModBlocks.heater_heatex))).setTier(3));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {
						new ComparableStack(ModItems.rtg_unit, 5),
						new OreDictStack(getReflector(), 8),
						new OreDictStack(CU.ingot(), 16),
						new OreDictStack(TCALLOY.ingot(), 6),
						new OreDictStack(STEEL.plate(), 8),
				}, new AnvilOutput(new ItemStack(ModBlocks.heater_rt))).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {
						new ComparableStack(Blocks.STONEBRICK, 16),
						new OreDictStack(IRON.ingot(), 4),
						new OreDictStack(STEEL.plate(), 16),
						new OreDictStack(CU.ingot(), 8),
						new ComparableStack(ModBlocks.steel_grate, 16)
				}, new AnvilOutput(new ItemStack(ModBlocks.furnace_steel))).setTier(2));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {
						new OreDictStack(STEEL.ingot(), 4),
						new OreDictStack(CU.plate(), 16),
						new ComparableStack(ModItems.plate_polymer, 8)
				}, new AnvilOutput(new ItemStack(ModBlocks.heat_boiler))).setTier(2));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {
						new ComparableStack(ModBlocks.reinforced_stone, 16),
						new OreDictStack(STEEL.plate(), 12),
						new OreDictStack(STEEL.shell(), 2),
						new ComparableStack(ModItems.coil_copper, 4),
						new ComparableStack(ModItems.gear_large, 1)
				}, new AnvilOutput(new ItemStack(ModBlocks.machine_steam_engine))).setTier(2));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {
						new ComparableStack(ModItems.ingot_firebrick, 20),
						new OreDictStack(CU.ingot(), 8),
						new OreDictStack(STEEL.plate(), 8)
				}, new AnvilOutput(new ItemStack(ModBlocks.machine_crucible))).setTier(2));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {
						new ComparableStack(ModBlocks.brick_concrete, 64),
						new ComparableStack(Blocks.IRON_BARS, 128),
						new ComparableStack(ModBlocks.machine_condenser, 5),
				},
				new AnvilOutput(new ItemStack(ModBlocks.machine_tower_small))).setTier(3));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {
						new ComparableStack(ModBlocks.concrete_smooth, 128),
						new ComparableStack(ModBlocks.steel_scaffold, 32),
						new ComparableStack(ModBlocks.machine_condenser, 25),
						new ComparableStack(ModItems.pipes_steel, 2)
				},
				new AnvilOutput(new ItemStack(ModBlocks.machine_tower_large))).setTier(4));
		
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {
						new OreDictStack(ANY_CONCRETE.any(), 2),
						new ComparableStack(ModBlocks.steel_scaffold, 8),
						new ComparableStack(ModItems.plate_polymer, 8),
						new ComparableStack(ModItems.coil_copper, 4)
				},
				new AnvilOutput(new ItemStack(ModBlocks.red_pylon_large))).setTier(2));
		
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {
						new OreDictStack(ANY_CONCRETE.any(), 6),
						new OreDictStack(STEEL.ingot(), 4),
						new ComparableStack(ModBlocks.steel_scaffold, 2),
						new ComparableStack(ModItems.plate_polymer, 8),
						new ComparableStack(ModItems.coil_copper, 2),
						new ComparableStack(ModItems.coil_copper_torus, 2)
				},
				new AnvilOutput(new ItemStack(ModBlocks.substation))).setTier(2));

		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {
						new OreDictStack(STEEL.plate(), 4),
						new ComparableStack(Blocks.BRICK_BLOCK, 16),
						new ComparableStack(ModBlocks.steel_grate, 2)
				},
				new AnvilOutput(new ItemStack(ModBlocks.chimney_brick))).setTier(2));

		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {
						new OreDictStack(STEEL.plate(), 16),
						new OreDictStack(ANY_CONCRETE.any(), 64),
						new ComparableStack(ModBlocks.steel_grate, 4),
						new ComparableStack(ModItems.filter_coal, 4)
				},
				new AnvilOutput(new ItemStack(ModBlocks.chimney_industrial))).setTier(3));

		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {
						new ComparableStack(ModBlocks.steel_wall, 2),
						new OreDictStack(REDSTONE.dust(), 4),
						new ComparableStack(Blocks.LEVER, 2),
						new ComparableStack(ModItems.wire_fine, 3, Mats.MAT_ALLOY.id)
				},
				new AnvilOutput(new ItemStack(ModBlocks.bm_power_box))).setTier(5));
		
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {
						new ComparableStack(Items.BONE, 16),
						new ComparableStack(Items.LEATHER, 4),
						new ComparableStack(Items.FEATHER, 24)
				},
				new AnvilOutput(new ItemStack(ModItems.wings_limp))).setTier(2));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {
						new ComparableStack(ModItems.sulfur, 12),
						new OreDictStack(STEEL.shell(), 4),
						new OreDictStack(CU.plateCast(), 6),
						new ComparableStack(ModItems.circuit, 2, EnumCircuitType.BASIC.ordinal())
				},
				new AnvilOutput(new ItemStack(ModBlocks.machine_deuterium_extractor))).setTier(2));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {
						new ComparableStack(ModItems.deuterium_filter, 2),
						new ComparableStack(ModItems.hull_big_steel, 5),
						new ComparableStack(ModBlocks.concrete_smooth, 8),
						new ComparableStack(ModBlocks.concrete_asbestos, 4),
						new ComparableStack(ModBlocks.steel_scaffold, 16),
						new ComparableStack(ModBlocks.deco_pipe_quad, 12),
						new OreDictStack(S.dust(), 32),
				},
				new AnvilOutput(new ItemStack(ModBlocks.machine_deuterium_tower))).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {new OreDictStack(DESH.ingot(), 4), new OreDictStack(ANY_PLASTIC.dust(), 2), new OreDictStack(DURA.ingot(), 1)},
				new AnvilOutput(new ItemStack(ModItems.plate_desh, 4))).setTier(3));
		/*constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {new ComparableStack(ModItems.nugget_bismuth, 2), new OreDictStack(U238.billet(), 2), new OreDictStack(NB.dust(), 1)},
				new AnvilOutput(new ItemStack(ModItems.plate_bismuth, 1))).setTier(4));*/ // TODO: bismuth plates
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {new OreDictStack(EUPH.ingot(), 4), new OreDictStack(AT.dust(), 3), new OreDictStack(BI.dust(), 1), new OreDictStack(VOLCANIC.gem(), 1), new ComparableStack(ModItems.ingot_osmiridium)},
				new AnvilOutput(new ItemStack(ModItems.plate_euphemium, 4))).setTier(6));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {new OreDictStack(DNT.ingot(), 4), new ComparableStack(ModItems.powder_spark_mix, 2), new OreDictStack(DESH.ingot(), 1)},
				new AnvilOutput(new ItemStack(ModItems.plate_dineutronium, 4))).setTier(7));

		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {new OreDictStack(TI.plate(), 2), new OreDictStack(STEEL.ingot(), 1), new OreDictStack(STEEL.bolt(), 4)},
				new AnvilOutput(new ItemStack(ModItems.plate_armor_titanium))).setTier(2));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {new OreDictStack(IRON.plate(), 6), new OreDictStack(NB.ingot(), 1), new ComparableStack(ModItems.plate_armor_titanium, 1)},
				new AnvilOutput(new ItemStack(ModItems.plate_armor_ajr, 2))).setTier(3));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {new OreDictStack(ALLOY.plate(), 4), new ComparableStack(ModItems.plate_armor_titanium, 1), new OreDictStack(W.wireFine(), 8)},
				new AnvilOutput(new ItemStack(ModItems.plate_armor_hev))).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {new OreDictStack(OreDictManager.getReflector(), 4), new OreDictStack(STAR.ingot(), 1), new OreDictStack(MAGTUNG.wireFine(), 8)},
				new AnvilOutput(new ItemStack(ModItems.plate_armor_lunar))).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {new ComparableStack(ModItems.ingot_meteorite_forged, 4), new OreDictStack(DESH.ingot(), 1), new ComparableStack(ModItems.billet_yharonite, 1)},
				new AnvilOutput(new ItemStack(ModItems.plate_armor_fau))).setTier(6));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {new ComparableStack(ModItems.plate_dineutronium, 4), new ComparableStack(ModItems.particle_sparkticle, 1), new ComparableStack(ModItems.plate_armor_fau, 6)},
				new AnvilOutput(new ItemStack(ModItems.plate_armor_dnt))).setTier(7));
		constructionRecipes.add((new AnvilConstructionRecipe(
				new AStack[]{
						new OreDictStack(KEY_PLANKS, 16),
						new OreDictStack(STEEL.plate(), 6),
						new OreDictStack(CU.ingot(), 8),
						new ComparableStack(ModItems.coil_copper, 4),
						new ComparableStack(ModItems.gear_large, 1, 0)},
				new AnvilOutput(new ItemStack(ModBlocks.machine_stirling)))).setTier(2));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[]{
						new OreDictStack(STEEL.plate(), 16),
						new OreDictStack(BE.ingot(), 6),
						new OreDictStack(CU.ingot(), 8),
						new ComparableStack(ModItems.coil_gold, 16),
						new ComparableStack(ModItems.gear_large, 1, 1)
				}, new AnvilOutput(new ItemStack(ModBlocks.machine_stirling_steel))).setTier(2));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {
						new ComparableStack(Blocks.STONEBRICK, 8),
						new ComparableStack(ModItems.ingot_firebrick, 16),
						new OreDictStack(IRON.ingot(), 4),
						new OreDictStack(CU.plate(), 8),
				}, new AnvilOutput(new ItemStack(ModBlocks.machine_rotary_furnace))).setTier(2));
		pullFromAssembler(new ComparableStack(ModItems.plate_mixed, 4), 3);

		/* constructionRecipes.add(new AnvilConstructionRecipe(
				new AStack[] {
						new ComparableStack(ModItems.missile_doomsday_rusted, 1),
						new OreDictStack(ANY_HARDPLASTIC.ingot(), 8),
						new OreDictStack(AL.plateWelded(), 2),
						new OreDictStack(PU239.billet(), 3)
				}, new AnvilOutput(new ItemStack(ModItems.missile_doomsday))).setTier(5)); */ // TODO: doomsday rusted missile

		constructionRecipes.add(new AnvilConstructionRecipe(new ComparableStack(ModItems.ingot_u233, 1), new AnvilOutput(new ItemStack(ModItems.plate_fuel_u233))).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(new ComparableStack(ModItems.ingot_u235, 1), new AnvilOutput(new ItemStack(ModItems.plate_fuel_u235))).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(new ComparableStack(ModItems.ingot_mox_fuel, 1), new AnvilOutput(new ItemStack(ModItems.plate_fuel_mox))).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(new ComparableStack(ModItems.ingot_pu239, 1), new AnvilOutput(new ItemStack(ModItems.plate_fuel_pu239))).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(new ComparableStack(ModItems.ingot_schrabidium, 1), new AnvilOutput(new ItemStack(ModItems.plate_fuel_sa326))).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(new ComparableStack(ModItems.billet_ra226be, 1), new AnvilOutput(new ItemStack(ModItems.plate_fuel_ra226be))).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(new ComparableStack(ModItems.billet_pu238be, 1), new AnvilOutput(new ItemStack(ModItems.plate_fuel_pu238be))).setTier(4));

		for(int i = 0; i < 15; i += 3) {
			constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(IRON.plate(), 1), new AnvilOutput(new ItemStack(ModBlocks.fluid_duct_box, 1, i))).setTier(2).setOverlay(OverlayType.CONSTRUCTION));
			constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(CU.plate(), 1), new AnvilOutput(new ItemStack(ModBlocks.fluid_duct_box, 1, i + 1))).setTier(2).setOverlay(OverlayType.CONSTRUCTION));
			constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(AL.plate(), 1), new AnvilOutput(new ItemStack(ModBlocks.fluid_duct_box, 1, i + 2))).setTier(2).setOverlay(OverlayType.CONSTRUCTION));
			constructionRecipes.add(new AnvilConstructionRecipe(new ComparableStack(ModBlocks.fluid_duct_box, 1, i), new AnvilOutput(new ItemStack(ModItems.plate_iron))).setTier(2).setOverlay(OverlayType.RECYCLING));
			constructionRecipes.add(new AnvilConstructionRecipe(new ComparableStack(ModBlocks.fluid_duct_box, 1, i + 1), new AnvilOutput(new ItemStack(ModItems.plate_copper))).setTier(2).setOverlay(OverlayType.RECYCLING));
			constructionRecipes.add(new AnvilConstructionRecipe(new ComparableStack(ModBlocks.fluid_duct_box, 1, i + 2), new AnvilOutput(new ItemStack(ModItems.plate_aluminium))).setTier(2).setOverlay(OverlayType.RECYCLING));

			constructionRecipes.add(new AnvilConstructionRecipe(new AStack[] {new OreDictStack(IRON.plate(), 1), new ComparableStack(ModItems.plate_polymer)}, new AnvilOutput(new ItemStack(ModBlocks.fluid_duct_exhaust, 8, i))).setTier(2).setOverlay(OverlayType.CONSTRUCTION));
			constructionRecipes.add(new AnvilConstructionRecipe(new ComparableStack(ModBlocks.fluid_duct_exhaust, 8, i), new AnvilOutput[] {new AnvilOutput(new ItemStack(ModItems.plate_iron)), new AnvilOutput(new ItemStack(ModItems.plate_polymer))}).setTier(2));
		}
		
	}
	
	public static void registerConstructionAmmo() {
		
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(CU.plate()), new AnvilOutput(new ItemStack(ModItems.casing_357))).setTier(1));
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(CU.plate()), new AnvilOutput(new ItemStack(ModItems.casing_44))).setTier(1));
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(CU.plate()), new AnvilOutput(new ItemStack(ModItems.casing_9))).setTier(1));
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(CU.plate()), new AnvilOutput(new ItemStack(ModItems.casing_50))).setTier(1));
		constructionRecipes.add(new AnvilConstructionRecipe(new OreDictStack(CU.plate()), new AnvilOutput(new ItemStack(ModItems.casing_buckshot))).setTier(1));
		constructionRecipes.add(new AnvilConstructionRecipe(new AStack[] {new OreDictStack(IRON.plate()), new ComparableStack(Items.REDSTONE)}, new AnvilOutput(new ItemStack(ModItems.primer_357))).setTier(1));
		constructionRecipes.add(new AnvilConstructionRecipe(new AStack[] {new OreDictStack(IRON.plate()), new ComparableStack(Items.REDSTONE)}, new AnvilOutput(new ItemStack(ModItems.primer_44))).setTier(1));
		constructionRecipes.add(new AnvilConstructionRecipe(new AStack[] {new OreDictStack(IRON.plate()), new ComparableStack(Items.REDSTONE)}, new AnvilOutput(new ItemStack(ModItems.primer_9))).setTier(1));
		constructionRecipes.add(new AnvilConstructionRecipe(new AStack[] {new OreDictStack(IRON.plate()), new ComparableStack(Items.REDSTONE)}, new AnvilOutput(new ItemStack(ModItems.primer_50))).setTier(1));
		constructionRecipes.add(new AnvilConstructionRecipe(new AStack[] {new OreDictStack(IRON.plate()), new ComparableStack(Items.REDSTONE)}, new AnvilOutput(new ItemStack(ModItems.primer_buckshot))).setTier(1));

		// PORTED CRAFTS BELOW

		constructionRecipes.add(new AnvilConstructionRecipe(new AStack[]{new ComparableStack(ModItems.stamp_iron_flat), new OreDictStack(GUNMETAL.ingot(), 2)}, new AnvilOutput(new ItemStack(ModItems.stamp_9))).setTier(2));
		constructionRecipes.add(new AnvilConstructionRecipe(new AStack[]{new ComparableStack(ModItems.stamp_iron_flat), new OreDictStack(GUNMETAL.ingot(), 2)}, new AnvilOutput(new ItemStack(ModItems.stamp_50))).setTier(2));
		constructionRecipes.add(new AnvilConstructionRecipe(new AStack[]{new ComparableStack(ModItems.stamp_desh_flat), new OreDictStack(WEAPONSTEEL.ingot(), 4)}, new AnvilOutput(new ItemStack(ModItems.stamp_desh_9))).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(new AStack[]{new ComparableStack(ModItems.stamp_desh_flat), new OreDictStack(WEAPONSTEEL.ingot(), 4)}, new AnvilOutput(new ItemStack(ModItems.stamp_desh_50))).setTier(4));

		constructionRecipes.add(new AnvilConstructionRecipe(new AStack[]{new ComparableStack(ModItems.mold_base), new OreDictStack(IRON.ingot(), 2)}, new AnvilOutput(new ItemStack(ModItems.mold, 1, 16))).setTier(1));
		constructionRecipes.add(new AnvilConstructionRecipe(new AStack[]{new ComparableStack(ModItems.mold_base), new OreDictStack(IRON.ingot(), 2)}, new AnvilOutput(new ItemStack(ModItems.mold, 1, 17))).setTier(1));

		constructionRecipes.add(new AnvilConstructionRecipe(new AStack[]{new ComparableStack(ModItems.mold_base), new OreDictStack(STEEL.ingot(), 4)}, new AnvilOutput(new ItemStack(ModItems.mold, 1, 22))).setTier(2));
		constructionRecipes.add(new AnvilConstructionRecipe(new AStack[]{new ComparableStack(ModItems.mold_base), new OreDictStack(STEEL.ingot(), 4)}, new AnvilOutput(new ItemStack(ModItems.mold, 1, 23))).setTier(2));
		constructionRecipes.add(new AnvilConstructionRecipe(new AStack[]{new ComparableStack(ModItems.mold_base), new OreDictStack(STEEL.ingot(), 4)}, new AnvilOutput(new ItemStack(ModItems.mold, 1, 24))).setTier(2));
		constructionRecipes.add(new AnvilConstructionRecipe(new AStack[]{new ComparableStack(ModItems.mold_base), new OreDictStack(STEEL.ingot(), 4)}, new AnvilOutput(new ItemStack(ModItems.mold, 1, 25))).setTier(2));
		constructionRecipes.add(new AnvilConstructionRecipe(new AStack[]{new ComparableStack(ModItems.mold_base), new OreDictStack(STEEL.ingot(), 4)}, new AnvilOutput(new ItemStack(ModItems.mold, 1, 26))).setTier(2));
		constructionRecipes.add(new AnvilConstructionRecipe(new AStack[]{new ComparableStack(ModItems.mold_base), new OreDictStack(STEEL.ingot(), 4)}, new AnvilOutput(new ItemStack(ModItems.mold, 1, 27))).setTier(2));
		constructionRecipes.add(new AnvilConstructionRecipe(new AStack[]{new ComparableStack(ModItems.mold_base), new OreDictStack(STEEL.ingot(), 4)}, new AnvilOutput(new ItemStack(ModItems.mold, 1, 28))).setTier(2));

		pullFromAssembler(new ComparableStack(ModItems.pellet_buckshot), 1);
	}
	
	public static void registerConstructionUpgrades() {
		pullFromAssembler(new ComparableStack(ModItems.upgrade_template), 2);
		pullFromAssembler(new ComparableStack(ModItems.upgrade_speed_1), 2);
		pullFromAssembler(new ComparableStack(ModItems.upgrade_speed_2), 3);
		pullFromAssembler(new ComparableStack(ModItems.upgrade_speed_3), 4);
		pullFromAssembler(new ComparableStack(ModItems.upgrade_power_1), 2);
		pullFromAssembler(new ComparableStack(ModItems.upgrade_power_2), 3);
		pullFromAssembler(new ComparableStack(ModItems.upgrade_power_3), 4);
		pullFromAssembler(new ComparableStack(ModItems.upgrade_effect_1), 2);
		pullFromAssembler(new ComparableStack(ModItems.upgrade_effect_2), 3);
		pullFromAssembler(new ComparableStack(ModItems.upgrade_effect_3), 4);
		pullFromAssembler(new ComparableStack(ModItems.upgrade_fortune_1), 2);
		pullFromAssembler(new ComparableStack(ModItems.upgrade_fortune_2), 3);
		pullFromAssembler(new ComparableStack(ModItems.upgrade_fortune_3), 4);
		pullFromAssembler(new ComparableStack(ModItems.upgrade_afterburn_1), 2);
		pullFromAssembler(new ComparableStack(ModItems.upgrade_afterburn_2), 3);
		pullFromAssembler(new ComparableStack(ModItems.upgrade_afterburn_3), 4);

		pullFromAssembler(new ComparableStack(ModItems.upgrade_radius), 4);
		pullFromAssembler(new ComparableStack(ModItems.upgrade_health), 4);
		pullFromAssembler(new ComparableStack(ModItems.upgrade_smelter), 4);
		pullFromAssembler(new ComparableStack(ModItems.upgrade_shredder), 4);
		pullFromAssembler(new ComparableStack(ModItems.upgrade_centrifuge), 4);
		pullFromAssembler(new ComparableStack(ModItems.upgrade_crystallizer), 4);
		pullFromAssembler(new ComparableStack(ModItems.upgrade_nullifier), 4);
		pullFromAssembler(new ComparableStack(ModItems.upgrade_screm), 4);
	}

	public static void registerConstructionRecycling() {
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModBlocks.radiorec),
				new AnvilOutput[] {
						new AnvilOutput(new ItemStack(ModItems.plate_steel, 4)),
						new AnvilOutput(new ItemStack(ModItems.wire_fine, 1, Mats.MAT_COPPER.id)),
						new AnvilOutput(new ItemStack(ModItems.circuit, 1, EnumCircuitType.VACUUM_TUBE.ordinal()), 0.5F),
						new AnvilOutput(new ItemStack(ModItems.ingot_polymer, 1), 0.25F),
				}
		).setTier(2));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModBlocks.pole_satellite_receiver),
				new AnvilOutput[] {
						new AnvilOutput(new ItemStack(ModItems.ingot_steel, 3)),
						new AnvilOutput(new ItemStack(ModItems.ingot_steel, 2), 0.5F),
						new AnvilOutput(new ItemStack(ModItems.circuit, 1, EnumCircuitType.VACUUM_TUBE.ordinal()), 0.5F),
						new AnvilOutput(new ItemStack(ModItems.wire_fine, 1, Mats.MAT_MINGRADE.id)),
				}
		).setTier(2));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModBlocks.solar_mirror),
				new AnvilOutput[] {new AnvilOutput(new ItemStack(ModItems.ingot_steel, 1)), new AnvilOutput(new ItemStack(ModItems.plate_aluminium, 1))}).setTier(3));

		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModBlocks.barrel_tcalloy),
				new AnvilOutput[] {
						new AnvilOutput(new ItemStack(ModItems.ingot_titanium, 2)),
						new AnvilOutput(new ItemStack(ModItems.ingot_tcalloy, 4)),
						new AnvilOutput(new ItemStack(ModItems.ingot_tcalloy, 1), 0.50F),
						new AnvilOutput(new ItemStack(ModItems.ingot_tcalloy, 1), 0.25F)
				}
		).setTier(3));
//
//		constructionRecipes.add(new AnvilConstructionRecipe(
//				new ComparableStack(ModItems.circuit_raw),
//				new AnvilOutput[] {
//						new AnvilOutput(new ItemStack(ModItems.plate_steel, 1)),
//						new AnvilOutput(new ItemStack(ModItems.wire_fine, 1, Mats.MAT_ALUMINIUM.id)),
//						new AnvilOutput(new ItemStack(Items.REDSTONE, 1))
//				}
//		).setTier(1));
//		constructionRecipes.add(new AnvilConstructionRecipe(
//				new ComparableStack(ModItems.circuit_aluminium),
//				new AnvilOutput[] {
//						new AnvilOutput(new ItemStack(ModItems.plate_steel, 1)),
//						new AnvilOutput(new ItemStack(ModItems.wire_fine, 1, Mats.MAT_ALUMINIUM.id), 0.5F),
//						new AnvilOutput(new ItemStack(Items.REDSTONE, 1), 0.25F)
//				}
//		).setTier(1));
//		constructionRecipes.add(new AnvilConstructionRecipe(
//				new ComparableStack(ModItems.circuit_copper),
//				new AnvilOutput[] {
//						new AnvilOutput(new ItemStack(ModItems.circuit_aluminium, 1)),
//						new AnvilOutput(new ItemStack(ModItems.wire_fine, 2, Mats.MAT_COPPER.id)),
//						new AnvilOutput(new ItemStack(ModItems.wire_fine, 1, Mats.MAT_COPPER.id), 0.5F),
//						new AnvilOutput(new ItemStack(ModItems.wire_fine, 1, Mats.MAT_COPPER.id), 0.25F),
//						new AnvilOutput(new ItemStack(ModItems.powder_quartz, 1), 0.25F),
//						new AnvilOutput(new ItemStack(ModItems.plate_copper, 1), 0.5F)
//				}
//		).setTier(2));
//		constructionRecipes.add(new AnvilConstructionRecipe(
//				new ComparableStack(ModItems.circuit_red_copper),
//				new AnvilOutput[] {
//						new AnvilOutput(new ItemStack(ModItems.circuit_copper, 1)),
//						new AnvilOutput(new ItemStack(ModItems.wire_fine, 2, Mats.MAT_MINGRADE.id)),
//						new AnvilOutput(new ItemStack(ModItems.wire_fine, 1, Mats.MAT_MINGRADE.id), 0.5F),
//						new AnvilOutput(new ItemStack(ModItems.wire_fine, 1, Mats.MAT_MINGRADE.id), 0.25F),
//						new AnvilOutput(new ItemStack(ModItems.powder_gold, 1), 0.25F),
//						new AnvilOutput(new ItemStack(ModItems.plate_polymer, 1), 0.5F)
//				}
//		).setTier(3));
//		constructionRecipes.add(new AnvilConstructionRecipe(
//				new ComparableStack(ModItems.circuit_gold),
//				new AnvilOutput[] {
//						new AnvilOutput(new ItemStack(ModItems.circuit_red_copper, 1)),
//						new AnvilOutput(new ItemStack(ModItems.wire_fine, 2, Mats.MAT_GOLD.id)),
//						new AnvilOutput(new ItemStack(ModItems.wire_fine, 1, Mats.MAT_GOLD.id), 0.5F),
//						new AnvilOutput(new ItemStack(ModItems.wire_fine, 1, Mats.MAT_GOLD.id), 0.25F),
//						new AnvilOutput(new ItemStack(ModItems.powder_lapis, 1), 0.25F),
//						new AnvilOutput(new ItemStack(ModItems.ingot_polymer, 1), 0.5F)
//				}
//		).setTier(4));
//		constructionRecipes.add(new AnvilConstructionRecipe(
//				new ComparableStack(ModItems.circuit_schrabidium),
//				new AnvilOutput[] {
//						new AnvilOutput(new ItemStack(ModItems.circuit_gold, 1)),
//						new AnvilOutput(new ItemStack(ModItems.wire_fine, 2, Mats.MAT_SCHRABIDIUM.id)),
//						new AnvilOutput(new ItemStack(ModItems.wire_fine, 1, Mats.MAT_SCHRABIDIUM.id), 0.5F),
//						new AnvilOutput(new ItemStack(ModItems.wire_fine, 1, Mats.MAT_SCHRABIDIUM.id), 0.25F),
//						new AnvilOutput(new ItemStack(ModItems.powder_diamond, 1), 0.25F),
//						new AnvilOutput(new ItemStack(ModItems.ingot_desh, 1), 0.5F)
//				}
//		).setTier(6));
//
//		constructionRecipes.add(new AnvilConstructionRecipe(
//				new ComparableStack(ModItems.circuit_tantalium_raw),
//				new AnvilOutput[] {
//						new AnvilOutput(new ItemStack(Items.REDSTONE, 4)),
//						new AnvilOutput(new ItemStack(ModItems.wire_fine, 2, Mats.MAT_GOLD.id)),
//						new AnvilOutput(new ItemStack(ModItems.plate_copper, 2)),
//						new AnvilOutput(new ItemStack(ModItems.nugget_tantalium, 1))
//				}
//		).setTier(4));
//		constructionRecipes.add(new AnvilConstructionRecipe(
//				new ComparableStack(ModItems.circuit_tantalium),
//				new AnvilOutput[] {
//						new AnvilOutput(new ItemStack(Items.REDSTONE, 2)),
//						new AnvilOutput(new ItemStack(ModItems.wire_fine, 1, Mats.MAT_GOLD.id)),
//						new AnvilOutput(new ItemStack(ModItems.wire_fine, 1, Mats.MAT_GOLD.id), 0.5F),
//						new AnvilOutput(new ItemStack(ModItems.plate_copper, 1)),
//						new AnvilOutput(new ItemStack(ModItems.nugget_tantalium, 1), 0.75F)
//				}
//		).setTier(4));
//
//		constructionRecipes.add(new AnvilConstructionRecipe(
//				new ComparableStack(ModItems.circuit_bismuth_raw),
//				new AnvilOutput[] {
//						new AnvilOutput(new ItemStack(Items.REDSTONE, 4)),
//						new AnvilOutput(new ItemStack(ModItems.ingot_polymer, 2)),
//						new AnvilOutput(new ItemStack(ModItems.ingot_asbestos, 2)),
//						new AnvilOutput(new ItemStack(ModItems.ingot_bismuth, 1))
//				}
//		).setTier(4));
//		constructionRecipes.add(new AnvilConstructionRecipe(
//				new ComparableStack(ModItems.circuit_bismuth),
//				new AnvilOutput[] {
//						new AnvilOutput(new ItemStack(Items.REDSTONE, 2)),
//						new AnvilOutput(new ItemStack(ModItems.ingot_polymer, 1)),
//						new AnvilOutput(new ItemStack(ModItems.ingot_polymer, 1), 0.5F),
//						new AnvilOutput(new ItemStack(ModItems.ingot_asbestos, 1)),
//						new AnvilOutput(new ItemStack(ModItems.ingot_bismuth, 1), 0.75F)
//				}
//		).setTier(4));

		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModItems.pile_rod_uranium),
				new AnvilOutput[] {new AnvilOutput(new ItemStack(ModItems.billet_uranium, 3)), new AnvilOutput(new ItemStack(ModItems.plate_iron, 2))}).setTier(3));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModItems.pile_rod_plutonium),
				new AnvilOutput[] {new AnvilOutput(new ItemStack(ModItems.billet_plutonium, 3)), new AnvilOutput(new ItemStack(ModItems.plate_iron, 2))}).setTier(3));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModItems.pile_rod_source),
				new AnvilOutput[] {new AnvilOutput(new ItemStack(ModItems.billet_ra226be, 3)), new AnvilOutput(new ItemStack(ModItems.plate_iron, 2))}).setTier(3));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModItems.pile_rod_boron),
				new AnvilOutput[] {new AnvilOutput(new ItemStack(ModItems.ingot_boron, 2)), new AnvilOutput(new ItemStack(Items.STICK, 2))}).setTier(3));

		//RBMK
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModBlocks.rbmk_moderator), new AnvilOutput[] {
						new AnvilOutput(new ItemStack(ModBlocks.rbmk_blank, 1)),
						new AnvilOutput(new ItemStack(ModBlocks.block_graphite, 4))
				}).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModBlocks.rbmk_absorber), new AnvilOutput[] {
						new AnvilOutput(new ItemStack(ModBlocks.rbmk_blank, 1)),
						new AnvilOutput(new ItemStack(ModItems.ingot_boron, 8))
				}).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModBlocks.rbmk_reflector), new AnvilOutput[] {
						new AnvilOutput(new ItemStack(ModBlocks.rbmk_blank, 1)),
						new AnvilOutput(new ItemStack(ModItems.neutron_reflector, 8))
				}).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModBlocks.rbmk_control), new AnvilOutput[] {
						new AnvilOutput(new ItemStack(ModBlocks.rbmk_absorber, 1)),
						new AnvilOutput(new ItemStack(ModItems.ingot_graphite, 2)),
						new AnvilOutput(new ItemStack(ModItems.motor, 2))
				}).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModBlocks.rbmk_control_mod), new AnvilOutput[] {
						new AnvilOutput(new ItemStack(ModBlocks.rbmk_control, 1)),
						new AnvilOutput(new ItemStack(ModBlocks.block_graphite, 4)),
						new AnvilOutput(new ItemStack(ModItems.nugget_bismuth, 4))
				}).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModBlocks.rbmk_control_auto), new AnvilOutput[] {
				new AnvilOutput(new ItemStack(ModBlocks.rbmk_control, 1)),
				new AnvilOutput(new ItemStack(ModItems.circuit, 1, EnumCircuitType.ADVANCED.ordinal())),
				new AnvilOutput(new ItemStack(ModItems.crt_display, 1))
		}).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModBlocks.rbmk_rod_reasim), new AnvilOutput[] {
						new AnvilOutput(new ItemStack(ModBlocks.rbmk_blank, 1)),
						new AnvilOutput(new ItemStack(ModItems.ingot_zirconium, 4)),
						new AnvilOutput(new ItemStack(ModItems.hull_small_steel, 2))
				}).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModBlocks.rbmk_rod_reasim_mod), new AnvilOutput[] {
						new AnvilOutput(new ItemStack(ModBlocks.rbmk_rod_reasim, 1)),
						new AnvilOutput(new ItemStack(ModBlocks.block_graphite, 4)),
						new AnvilOutput(new ItemStack(ModItems.ingot_tcalloy, 4))
				}).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModBlocks.rbmk_outgasser), new AnvilOutput[] {
						new AnvilOutput(new ItemStack(ModBlocks.rbmk_blank, 1)),
						new AnvilOutput(new ItemStack(ModBlocks.steel_grate, 6)),
						new AnvilOutput(new ItemStack(ModItems.tank_steel, 1)),
						new AnvilOutput(new ItemStack(Blocks.HOPPER, 1))
				}).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModBlocks.rbmk_storage), new AnvilOutput[] {
						new AnvilOutput(new ItemStack(ModBlocks.rbmk_blank, 1)),
						new AnvilOutput(new ItemStack(ModBlocks.crate_steel, 2))
				}).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(
					new ComparableStack(ModBlocks.rbmk_rod), new AnvilOutput[] {
							new AnvilOutput(new ItemStack(ModBlocks.rbmk_blank, 1)),
							new AnvilOutput(new ItemStack(ModItems.hull_small_steel, 2))
					}).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModBlocks.rbmk_rod_mod), new AnvilOutput[] {
						new AnvilOutput(new ItemStack(ModBlocks.rbmk_rod, 1)),
						new AnvilOutput(new ItemStack(ModBlocks.block_graphite, 4)),
						new AnvilOutput(new ItemStack(ModItems.nugget_bismuth, 4))
				}).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModBlocks.rbmk_boiler), new AnvilOutput[] {
						new AnvilOutput(new ItemStack(ModBlocks.rbmk_blank, 1)),
						new AnvilOutput(new ItemStack(ModItems.board_copper, 6)),
						new AnvilOutput(new ItemStack(ModItems.pipes_steel, 2))
				}).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModBlocks.rbmk_cooler), new AnvilOutput[] {
						new AnvilOutput(new ItemStack(ModBlocks.rbmk_blank, 1)),
						new AnvilOutput(new ItemStack(ModBlocks.steel_grate, 4)),
						new AnvilOutput(new ItemStack(ModItems.plate_polymer, 4))
				}).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModBlocks.rbmk_heater), new AnvilOutput[] {
						new AnvilOutput(new ItemStack(ModBlocks.rbmk_blank, 1)),
						new AnvilOutput(new ItemStack(ModItems.pipes_steel, 2)),
						new AnvilOutput(new ItemStack(ModItems.plate_polymer, 2))
				}).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModBlocks.reactor_research), new AnvilOutput[] {
				new AnvilOutput(new ItemStack(ModItems.ingot_steel, 8)),
				new AnvilOutput(new ItemStack(ModItems.ingot_tcalloy, 4)),
				new AnvilOutput(new ItemStack(ModItems.motor_desh, 2)),
				new AnvilOutput(new ItemStack(ModItems.ingot_boron, 5)),
				new AnvilOutput(new ItemStack(ModItems.plate_lead, 8)),
				new AnvilOutput(new ItemStack(ModItems.crt_display, 3)),
				new AnvilOutput(new ItemStack(ModItems.circuit, 1, EnumCircuitType.BASIC.ordinal())),
				new AnvilOutput(new ItemStack(ModItems.circuit, 1, EnumCircuitType.BASIC.ordinal()), 0.5F),
		}).setTier(4));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModBlocks.machine_turbine), new AnvilOutput[] {
						new AnvilOutput(new ItemStack(ModItems.turbine_titanium, 1)),
						new AnvilOutput(new ItemStack(ModItems.coil_copper, 2)),
						new AnvilOutput(new ItemStack(ModItems.ingot_steel, 4))
						}).setTier(3));
		
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModBlocks.yellow_barrel), new AnvilOutput[] {
						new AnvilOutput(new ItemStack(ModItems.tank_steel, 1)),
						new AnvilOutput(new ItemStack(ModItems.plate_lead, 2)),
						new AnvilOutput(new ItemStack(ModItems.nuclear_waste, 9))
				}).setTier(3));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModBlocks.vitrified_barrel), new AnvilOutput[] {
						new AnvilOutput(new ItemStack(ModItems.tank_steel, 1)),
						new AnvilOutput(new ItemStack(ModItems.plate_lead, 2)),
						new AnvilOutput(new ItemStack(ModItems.nuclear_waste_vitrified, 9))
				}).setTier(3));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModBlocks.machine_stirling),
				new AnvilOutput[]{
						new AnvilOutput(new ItemStack(ModItems.plate_steel, 6)),
						new AnvilOutput(new ItemStack(ModItems.ingot_copper, 8)),
						new AnvilOutput(new ItemStack(ModItems.coil_copper, 4)),
						new AnvilOutput(new ItemStack(ModItems.gear_large, 1))
				}
		).setTier(2));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModBlocks.machine_stirling, 1, 1),
				new AnvilOutput[]{
						new AnvilOutput(new ItemStack(ModItems.plate_steel, 6)),
						new AnvilOutput(new ItemStack(ModItems.ingot_copper, 8)),
						new AnvilOutput(new ItemStack(ModItems.coil_copper, 4))
				}
		).setTier(2));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModBlocks.machine_stirling_steel),
				new AnvilOutput[]{
						new AnvilOutput(new ItemStack(ModItems.plate_steel, 16)),
						new AnvilOutput(new ItemStack(ModItems.ingot_beryllium, 6)),
						new AnvilOutput(new ItemStack(ModItems.ingot_copper, 8)),
						new AnvilOutput(new ItemStack(ModItems.coil_gold, 16)),
						new AnvilOutput(new ItemStack(ModItems.gear_large, 1, 1))
				}
		).setTier(2));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModItems.gear_large, 1, 1),
				new AnvilOutput[]{
						new AnvilOutput(new ItemStack(ModItems.plate_steel, 8)),
						new AnvilOutput(new ItemStack(ModItems.ingot_titanium, 1)),

				}
		).setTier(2));
		constructionRecipes.add(new AnvilConstructionRecipe(
				new ComparableStack(ModItems.gear_large),
				new AnvilOutput[]{
						new AnvilOutput(new ItemStack(ModItems.plate_iron, 8)),
						new AnvilOutput(new ItemStack(ModItems.ingot_copper, 1)),

				}
		).setTier(2));
	}

	private static void pullFromAssembler(ComparableStack result, int tier) {
		AssemblerRecipes.AssemblerRecipe assemblerRecipe = AssemblerRecipes.recipes.get(result);
		if (assemblerRecipe != null) {
			AStack[] ingredients = assemblerRecipe.ingredients;
			AnvilConstructionRecipe anvilRecipe = new AnvilConstructionRecipe(ingredients, new AnvilOutput(result.toStack()));
			AnvilRecipes.constructionRecipes.add(anvilRecipe.setTier(tier));
		}
	}
	
	public static List<AnvilSmithingRecipe> getSmithing() {
		return smithingRecipes;
	}
	
	public static List<AnvilConstructionRecipe> getConstruction() {
		return constructionRecipes;
	}

	public static boolean removeConstructionRecipe(ItemStack[] outputs) {
		start:
		for(AnvilConstructionRecipe constructionRecipe : constructionRecipes) {
			// check length same
			if(constructionRecipe.output.size() != outputs.length) continue;
			// check outputs same
			for(int i = 0; i < outputs.length; i++) {
				if(!areItemStacksEqual(constructionRecipe.output.get(i).stack,outputs[i])){
					continue start;
				}
			}
			constructionRecipes.remove(constructionRecipe);
			return true;

		}
		return false;
	}

	public static boolean removeConstructionRecipeByInput(AStack[] inputs) {
		start:
		for(AnvilConstructionRecipe constructionRecipe : constructionRecipes) {
			// check length same
			if(constructionRecipe.input.size() != inputs.length) continue;
			// check outputs same
			for(int i = 0; i < inputs.length; i++) {
				if(!inputs[i].equals(constructionRecipe.input.get(i))){
					continue start;
				}
			}
			constructionRecipes.remove(constructionRecipe);
			return true;
		}
		return false;
	}

	public static void addConstructionRecipe(AStack[] inputs, ItemStack[] output, int tier) {
		AnvilOutput[] anvilOutputs = new AnvilOutput[output.length];
		for(int i = 0; i < output.length; i++) {
			anvilOutputs[i] = new AnvilOutput(output[i]);
		}
		constructionRecipes.add(new AnvilConstructionRecipe(inputs, anvilOutputs).setTier(tier));
	}

	public static class AnvilConstructionRecipe {
		public List<AStack> input = new ArrayList<>();
		public List<AnvilOutput> output = new ArrayList<>();
		public int tierLower = 0;
		public int tierUpper = -1;
		OverlayType overlay = OverlayType.NONE;
		
		public AnvilConstructionRecipe(AStack input, AnvilOutput output) {
			this.input.add(input);
			this.output.add(output);
			this.setOverlay(OverlayType.SMITHING); //preferred overlay for 1:1 conversions is smithing
		}
		
		public AnvilConstructionRecipe(AStack[] input, AnvilOutput output) {
			for(AStack stack : input) this.input.add(stack);
			this.output.add(output);
			this.setOverlay(OverlayType.CONSTRUCTION); //preferred overlay for many:1 conversions is construction
		}
		
		public AnvilConstructionRecipe(AStack input, AnvilOutput[] output) {
			this.input.add(input);
			for(AnvilOutput out : output) this.output.add(out);
			this.setOverlay(OverlayType.RECYCLING); //preferred overlay for 1:many conversions is recycling
		}
		
		public AnvilConstructionRecipe(AStack[] input, AnvilOutput[] output) {
			for(AStack stack : input) this.input.add(stack);
			for(AnvilOutput out : output) this.output.add(out);
			this.setOverlay(OverlayType.NONE); //no preferred overlay for many:many conversions
		}
		
		public AnvilConstructionRecipe setTier(int tier) {
			this.tierLower = tier;
			if(GeneralConfig.enableBabyMode) this.tierLower = 1;
			return this;
		}
		
		public AnvilConstructionRecipe setTierRange(int lower, int upper) {
			this.tierLower = lower;
			this.tierUpper = upper;
			if(GeneralConfig.enableBabyMode) this.tierLower = this.tierUpper = 1;
			return this;
		}
		
		public boolean isTierValid(int tier) {
			
			if(this.tierUpper == -1)
				return tier >= this.tierLower;
			
			return tier >= this.tierLower && tier <= this.tierUpper;
		}
		
		public AnvilConstructionRecipe setOverlay(OverlayType overlay) {
			this.overlay = overlay;
			return this;
		}
		
		public OverlayType getOverlay() {
			return this.overlay;
		}
		
		public ItemStack getDisplay() {
			switch(this.overlay) {
			case NONE: return this.output.get(0).stack.copy();
			case CONSTRUCTION: return this.output.get(0).stack.copy();
			case SMITHING: return this.output.get(0).stack.copy();
			case RECYCLING:
				for(AStack stack : this.input) {
					if(stack instanceof ComparableStack)
						return ((ComparableStack)stack).toStack();
				}
				return this.output.get(0).stack.copy();
			default: return new ItemStack(Items.IRON_PICKAXE);
			}
		}
	}
	
	public static class AnvilOutput {
		public ItemStack stack;
		public float chance;
		
		public AnvilOutput(ItemStack stack) {
			this(stack, 1F);
		}
		
		public AnvilOutput(ItemStack stack, float chance) {
			this.stack = stack;
			this.chance = chance;
		}
	}

	public enum OverlayType {
		NONE,
		CONSTRUCTION,
		RECYCLING,
		SMITHING;
	}
}