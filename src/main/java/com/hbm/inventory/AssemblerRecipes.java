package com.hbm.inventory;

import com.google.gson.Gson;
import com.hbm.blocks.BlockEnums;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.NbtComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemAssemblyTemplate;
import com.hbm.items.machine.ItemFluidTank;
import com.hbm.items.special.ItemCell;
import com.hbm.main.MainRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.Level;

import java.io.*;
import java.util.*;

import static com.hbm.inventory.OreDictManager.*;

public class AssemblerRecipes {

	public static File config;
	public static File template;
	private static final Gson gson = new Gson();
	private static IForgeRegistry<Item> itemRegistry;
	private static IForgeRegistry<Block> blockRegistry;
	public static LinkedHashMap<ComparableStack, AStack[]> recipes = new LinkedHashMap<>();
	public static HashMap<ComparableStack, Integer> time = new HashMap<>();
	public static List<ComparableStack> recipeList = new ArrayList<>();
	public static HashSet<ComparableStack> hidden = new HashSet<>();
	
	//Backup client recipes
	public static LinkedHashMap<ComparableStack, AStack[]> backupRecipes;
	public static HashMap<ComparableStack, Integer> backupTime;
	public static List<ComparableStack> backupRecipeList;
	public static HashSet<ComparableStack> backupHidden = new HashSet<>();

	/**
	 * Pre-Init phase: Finds the recipe config (if exists) and checks if a
	 * template is present, if not it generates one.
	 * 
	 * @param dir
	 *            The suggested config folder
	 */
	public static void preInit(File dir) {

		if(dir == null || !dir.isDirectory())
			return;

		template = dir;

		List<File> files = Arrays.asList(dir.listFiles());
		for(File file : files) {
			if(file.getName().equals("hbmAssembler.json")) {
				config = file;
			}
		}
	}

	public static void loadRecipes() {
		registerDefaults();
		loadRecipesFromConfig();
		generateList();
	}

	public static ItemStack getOutputFromTempate(ItemStack stack) {

		if(stack != null && stack.getItem() instanceof ItemAssemblyTemplate) {

			int i = ItemAssemblyTemplate.getRecipeIndex(stack);
			if(i >= 0 && i < recipeList.size()) {
				return recipeList.get(i).toStack();
			}
		}

		return null;
	}

	public static List<AStack> getRecipeFromTempate(ItemStack stack) {

		if(stack != null && stack.getItem() instanceof ItemAssemblyTemplate) {

			int i = ItemAssemblyTemplate.getRecipeIndex(stack);

			if(i >= 0 && i < recipeList.size()) {
				ItemStack out = recipeList.get(i).toStack();

				if(out != null) {
					ComparableStack comp = new ComparableStack(out);
					AStack[] ret = recipes.get(comp);
					return Arrays.asList(ret);
				}
			}
		}

		return null;
	}

	/**
	 * Generates an ordered list of outputs, used by the template item to
	 * generate subitems
	 */
	public static void generateList() {

		List<ComparableStack> list = new ArrayList<>(recipes.keySet());
		Collections.sort(list);
		recipeList = list;
	}

	/**
	 * Registers regular recipes if there's no custom confiuration
	 */

	//FFS 7 you need to become more precise and thorough.
	private static void registerDefaults() {
		makeRecipe(new ComparableStack(ModItems.plate_iron, 2), new AStack[] { new OreDictStack(IRON.ingot(), 3), }, 30);
		makeRecipe(new ComparableStack(ModItems.plate_gold, 2), new AStack[] { new OreDictStack(GOLD.ingot(), 3), }, 30);
		makeRecipe(new ComparableStack(ModItems.plate_titanium, 2), new AStack[] { new OreDictStack(TI.ingot(), 3), }, 30);
		makeRecipe(new ComparableStack(ModItems.plate_aluminium, 2), new AStack[] { new OreDictStack(AL.ingot(), 3), }, 30);
		makeRecipe(new ComparableStack(ModItems.plate_steel, 2), new AStack[] { new OreDictStack(STEEL.ingot(), 3), }, 30);
		makeRecipe(new ComparableStack(ModItems.plate_lead, 2), new AStack[] { new OreDictStack(PB.ingot(), 3), }, 30);
		makeRecipe(new ComparableStack(ModItems.plate_copper, 2), new AStack[] { new OreDictStack(CU.ingot(), 3), }, 30);
		makeRecipe(new ComparableStack(ModItems.plate_advanced_alloy, 2), new AStack[] { new OreDictStack(ALLOY.ingot(), 3), }, 30);
		makeRecipe(new ComparableStack(ModItems.plate_schrabidium, 2), new AStack[] { new OreDictStack(SA326.ingot(), 3), }, 30);
		makeRecipe(new ComparableStack(ModItems.plate_combine_steel, 2), new AStack[] { new OreDictStack(CMB.ingot(), 3), }, 30);
		makeRecipe(new ComparableStack(ModItems.plate_saturnite, 2), new AStack[] { new OreDictStack(BIGMT.ingot(), 3), }, 30);
		makeRecipe(new ComparableStack(ModItems.plate_mixed, 6), new AStack[] { new OreDictStack(ALLOY.plate(), 2), new OreDictStack(OreDictManager.getReflector(), 2), new OreDictStack(CMB.plate(), 1), new OreDictStack(PB.plate(), 4), }, 100);
		makeRecipe(new ComparableStack(ModItems.wire_fine, 6, Mats.MAT_ALUMINIUM.id), new AStack[] { new OreDictStack(AL.ingot(), 1), }, 20);
		makeRecipe(new ComparableStack(ModItems.wire_fine, 6, Mats.MAT_COPPER.id), new AStack[] { new OreDictStack(CU.ingot(), 1), }, 20);
		makeRecipe(new ComparableStack(ModItems.wire_fine, 6, Mats.MAT_TUNGSTEN.id), new AStack[] { new OreDictStack(W.ingot(), 1), }, 20);
		makeRecipe(new ComparableStack(ModItems.wire_fine, 6, Mats.MAT_MINGRADE.id), new AStack[] { new OreDictStack(MINGRADE.ingot(), 1), }, 20);
		makeRecipe(new ComparableStack(ModItems.wire_fine, 6, Mats.MAT_ALLOY.id), new AStack[] { new OreDictStack(ALLOY.ingot(), 1), }, 20);
		makeRecipe(new ComparableStack(ModItems.wire_fine, 6, Mats.MAT_GOLD.id), new AStack[] { new ComparableStack(Items.GOLD_INGOT, 1), }, 20);
		makeRecipe(new ComparableStack(ModItems.wire_fine, 6, Mats.MAT_SCHRABIDIUM.id), new AStack[] { new OreDictStack(SA326.ingot(), 1), }, 20);
		makeRecipe(new ComparableStack(ModItems.wire_fine, 6, Mats.MAT_MAGTUNG.id), new AStack[] { new OreDictStack(MAGTUNG.ingot(), 1), }, 20);
		makeRecipe(new ComparableStack(ModItems.hazmat_cloth, 4), new AStack[] { new OreDictStack(PB.dust(), 4), new ComparableStack(Items.STRING, 8), }, 50);
		makeRecipe(new ComparableStack(ModItems.asbestos_cloth, 4), new AStack[] { new OreDictStack(ASBESTOS.ingot(), 2), new ComparableStack(Items.STRING, 6), new ComparableStack(Blocks.WOOL, 1), }, 50);
		makeRecipe(new ComparableStack(ModItems.filter_coal, 1), new AStack[] { new OreDictStack(COAL.dust(), 4), new ComparableStack(Items.STRING, 6), new ComparableStack(Items.PAPER, 1), }, 50);
		makeRecipe(new ComparableStack(ModItems.centrifuge_element, 1), new AStack[] { new OreDictStack(STEEL.plate(), 4), new OreDictStack(TI.plate(), 4), new ComparableStack(ModItems.motor, 1), }, 100);
		// makeRecipe(new ComparableStack(ModItems.centrifuge_tower, 1), new AStack[] { new ComparableStack(ModItems.centrifuge_element, 4), new OreDictStack(STEEL.plate(), 4), new ComparableStack(ModItems.wire_fine, 6, Mats.MAT_MINGRADE.id), new OreDictStack(LAPIS.dust(), 2), new OreDictStack(ANY_PLASTIC.ingot(), 2), }, 150);
		// makeRecipe(new ComparableStack(ModItems.magnet_dee, 1), new AStack[] { new ComparableStack(ModBlocks.fusion_conductor, 6), new OreDictStack(STEEL.ingot(), 3), new ComparableStack(ModItems.coil_advanced_torus, 1), }, 150);
		makeRecipe(new ComparableStack(ModItems.magnet_circular, 1), new AStack[] { new ComparableStack(ModBlocks.fusion_conductor, 5), new OreDictStack(STEEL.ingot(), 4), new OreDictStack(ALLOY.plate(), 6), }, 150);
		//makeRecipe(new ComparableStack(ModItems.cyclotron_tower, 1), new AStack[] { new ComparableStack(ModItems.magnet_circular, 6), new ComparableStack(ModItems.magnet_dee, 3), new OreDictStack(STEEL.plate(), 12), new ComparableStack(ModItems.wire_advanced_alloy, 8), new ComparableStack(ModItems.plate_polymer, 24), }, 100);
		makeRecipe(new ComparableStack(ModItems.reactor_core, 1), new AStack[] { new OreDictStack(PB.ingot(), 8), new OreDictStack(BE.ingot(), 6), new OreDictStack(STEEL.plate(), 16), new OreDictStack(OreDictManager.getReflector(), 8), new OreDictStack(FIBER.ingot(), 2) }, 100);
		makeRecipe(new ComparableStack(ModItems.rtg_unit, 1), new AStack[] { new ComparableStack(ModItems.thermo_element, 2), new ComparableStack(ModItems.board_copper, 1), new OreDictStack(PB.ingot(), 2), new OreDictStack(STEEL.plate(), 2), new ComparableStack(ModItems.circuit_copper, 1), }, 100);
		makeRecipe(new ComparableStack(ModItems.thermo_unit_empty, 1), new AStack[] { new ComparableStack(ModItems.coil_copper_torus, 3), new OreDictStack(STEEL.ingot(), 3), new OreDictStack(TI.plate(), 6), new ComparableStack(ModItems.plate_polymer, 12), new OreDictStack(BR.ingot(), 2), }, 100);
		makeRecipe(new ComparableStack(ModItems.levitation_unit, 1), new AStack[] { new ComparableStack(ModItems.coil_copper, 4), new ComparableStack(ModItems.coil_tungsten, 2), new OreDictStack(TI.plate(), 6), new OreDictStack(SA326.nugget(), 2), }, 100);
		makeRecipe(new ComparableStack(ModItems.drill_titanium, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 2), new OreDictStack(DURA.ingot(), 2), new ComparableStack(ModItems.bolt_dura_steel, 2), new OreDictStack(TI.plate(), 6), }, 100);
		makeRecipe(new ComparableStack(ModBlocks.machine_excavator, 1), new AStack[] {
				new ComparableStack(Blocks.STONEBRICK, 8),
				new OreDictStack(STEEL.ingot(), 16),
				new OreDictStack(IRON.ingot(), 16),
				new ComparableStack(ModBlocks.steel_scaffold, 16),
				new ComparableStack(ModItems.motor, 2),
				new ComparableStack(ModItems.tank_steel, 1),
				new ComparableStack(ModItems.circuit_red_copper, 1)
			}, 300);
		makeRecipe(new ComparableStack(ModItems.drillbit_steel), new AStack[] {
				new OreDictStack(STEEL.ingot(), 12),
				new OreDictStack(W.ingot(), 4)
			}, 200);
		makeRecipe(new ComparableStack(ModItems.drillbit_steel_diamond), new AStack[] {
				new ComparableStack(ModItems.drillbit_steel),
				new OreDictStack(DIAMOND.dust(), 16)
			}, 100);
		
		makeRecipe(new ComparableStack(ModItems.drillbit_hss), new AStack[] {
				new OreDictStack(DURA.ingot(), 12),
				new OreDictStack(ANY_PLASTIC.ingot(), 12),
				new OreDictStack(TI.ingot(), 8)
			}, 200);
		makeRecipe(new ComparableStack(ModItems.drillbit_hss_diamond), new AStack[] {
				new ComparableStack(ModItems.drillbit_hss),
				new OreDictStack(DIAMOND.dust(), 24)
			}, 100);
		
		makeRecipe(new ComparableStack(ModItems.drillbit_desh), new AStack[] {
				new OreDictStack(DESH.ingot(), 16),
				new OreDictStack(RUBBER.ingot(), 12),
				new OreDictStack(NB.ingot(), 4)
			}, 200);
		makeRecipe(new ComparableStack(ModItems.drillbit_desh_diamond), new AStack[] {
				new ComparableStack(ModItems.drillbit_desh),
				new OreDictStack(DIAMOND.dust(), 32)
			}, 100);
		
		makeRecipe(new ComparableStack(ModItems.drillbit_tcalloy), new AStack[] {
				new OreDictStack(TCALLOY.ingot(), 20),
				new OreDictStack(DESH.ingot(), 12),
				new OreDictStack(RUBBER.ingot(), 8)
			}, 200);
		makeRecipe(new ComparableStack(ModItems.drillbit_tcalloy_diamond), new AStack[] {
				new ComparableStack(ModItems.drillbit_tcalloy),
				new OreDictStack(DIAMOND.dust(), 48)
			}, 100);
		
		makeRecipe(new ComparableStack(ModItems.drillbit_ferro), new AStack[] {
				new OreDictStack(FERRO.ingot(), 24),
				new OreDictStack(CDALLOY.ingot(), 12),
				new OreDictStack(ANY_BISMOID.ingot(), 4),
			}, 200);
		makeRecipe(new ComparableStack(ModItems.drillbit_ferro_diamond), new AStack[] {
				new ComparableStack(ModItems.drillbit_ferro),
				new OreDictStack(DIAMOND.dust(), 64)
			}, 100);
		makeRecipe(new ComparableStack(ModItems.drillbit_dnt), new AStack[] {
				new ComparableStack(ModItems.ingot_dineutronium, 32),
				new OreDictStack(GH336.ingot(), 24),
				new ComparableStack(ModItems.ingot_chainsteel, 8),
			}, 2000);
		makeRecipe(new ComparableStack(ModItems.telepad, 1), new AStack[] { new OreDictStack(ANY_PLASTIC.ingot(), 12), new OreDictStack(SA326.plate(), 2), new OreDictStack(CMB.plate(), 4), new OreDictStack(STEEL.plate(), 2), new ComparableStack(ModItems.wire_fine, 6, Mats.MAT_GOLD.id), new ComparableStack(ModItems.circuit_schrabidium, 1), }, 300);
		makeRecipe(new ComparableStack(ModItems.entanglement_kit, 1), new AStack[] { new ComparableStack(ModItems.coil_magnetized_tungsten, 6), new OreDictStack(PB.plate(), 16), new OreDictStack(OreDictManager.getReflector(), 4), new ComparableStack(ModItems.singularity_counter_resonant, 1), new ComparableStack(ModItems.singularity_super_heated, 1), new ComparableStack(ModItems.powder_power, 4), }, 200);
		makeRecipe(new ComparableStack(ModItems.dysfunctional_reactor, 1), new AStack[] { new OreDictStack(STEEL.plate(), 15), new OreDictStack(PB.ingot(), 5), new ComparableStack(ModItems.rod_quad_empty, 10), new OreDictStack(KEY_BROWN, 3), }, 200);
		//makeRecipe(new ComparableStack(ModItems.generator_front, 1), new AStack[] {new OreDictStack(STEEL.ingot(), 3), new OreDictStack(STEEL.plate(), 6), new ComparableStack(ModItems.tank_steel, 4), new ComparableStack(ModItems.turbine_titanium, 1), new ComparableStack(ModItems.wire_fine, 6, Mats.MAT_MINGRADE.id), new ComparableStack(ModItems.wire_fine, 4, Mats.MAT_GOLD.id), },200);
		makeRecipe(new ComparableStack(ModItems.missile_assembly, 1), new AStack[] { new ComparableStack(ModItems.hull_small_steel, 1), new ComparableStack(ModItems.hull_small_aluminium, 4), new OreDictStack(STEEL.ingot(), 2), new OreDictStack(TI.plate(), 6), new ComparableStack(ModItems.wire_fine, 6, Mats.MAT_ALUMINIUM.id), new ComparableStack(ModItems.canister_generic, 3, Fluids.KEROSENE.getID()), new ComparableStack(ModItems.circuit_targeting_tier1, 1), }, 200);
		makeRecipe(new ComparableStack(ModItems.missile_carrier, 1), new AStack[] { new NbtComparableStack(ItemFluidTank.getFullBarrel(Fluids.KEROSENE, 16)), new ComparableStack(ModItems.thruster_medium, 4), new ComparableStack(ModItems.thruster_large, 1), new ComparableStack(ModItems.hull_big_titanium, 6), new ComparableStack(ModItems.hull_big_steel, 2), new ComparableStack(ModItems.hull_small_aluminium, 12), new OreDictStack(TI.plate(), 24), new OreDictStack(ANY_RUBBER.ingot(), 64), new OreDictStack(ANY_RUBBER.ingot(), 64), new ComparableStack(ModBlocks.det_cord, 8), new ComparableStack(ModItems.circuit_targeting_tier3, 12), new ComparableStack(ModItems.circuit_targeting_tier4, 3), }, 4800);
		makeRecipe(new ComparableStack(ModItems.warhead_generic_small, 1), new AStack[] { new OreDictStack(TI.plate(), 5), new OreDictStack(STEEL.plate(), 3), new ComparableStack(Blocks.TNT, 2), }, 100);
		makeRecipe(new ComparableStack(ModItems.warhead_generic_medium, 1), new AStack[] { new OreDictStack(TI.plate(), 8), new OreDictStack(STEEL.plate(), 5), new OreDictStack(ANY_HIGHEXPLOSIVE.ingot(), 4), }, 150);
		makeRecipe(new ComparableStack(ModItems.warhead_generic_large, 1), new AStack[] { new OreDictStack(TI.plate(), 15), new OreDictStack(STEEL.plate(), 8), new OreDictStack(ANY_HIGHEXPLOSIVE.ingot(), 8), }, 200);
		makeRecipe(new ComparableStack(ModItems.warhead_incendiary_small, 1), new AStack[] { new ComparableStack(ModItems.warhead_generic_small, 1), new OreDictStack(P_RED.dust(), 4), }, 100);
		makeRecipe(new ComparableStack(ModItems.warhead_incendiary_medium, 1), new AStack[] { new ComparableStack(ModItems.warhead_generic_medium, 1), new OreDictStack(P_RED.dust(), 8), }, 150);
		makeRecipe(new ComparableStack(ModItems.warhead_incendiary_large, 1), new AStack[] { new ComparableStack(ModItems.warhead_generic_large, 1), new OreDictStack(P_RED.dust(), 16), }, 200);
		makeRecipe(new ComparableStack(ModItems.warhead_cluster_small, 1), new AStack[] { new ComparableStack(ModItems.warhead_generic_small, 1), new ComparableStack(ModItems.pellet_cluster, 4), }, 100);
		makeRecipe(new ComparableStack(ModItems.warhead_cluster_medium, 1), new AStack[] { new ComparableStack(ModItems.warhead_generic_medium, 1), new ComparableStack(ModItems.pellet_cluster, 8), }, 150);
		makeRecipe(new ComparableStack(ModItems.warhead_cluster_large, 1), new AStack[] { new ComparableStack(ModItems.warhead_generic_large, 1), new ComparableStack(ModItems.pellet_cluster, 16), }, 200);
		makeRecipe(new ComparableStack(ModItems.warhead_buster_small, 1), new AStack[] { new ComparableStack(ModItems.warhead_generic_small, 1), new ComparableStack(ModBlocks.det_cord, 8), }, 100);
		makeRecipe(new ComparableStack(ModItems.warhead_buster_medium, 1), new AStack[] { new ComparableStack(ModItems.warhead_generic_medium, 1), new ComparableStack(ModBlocks.det_cord, 4), new ComparableStack(ModBlocks.det_charge, 4), }, 150);
		makeRecipe(new ComparableStack(ModItems.warhead_buster_large, 1), new AStack[] { new ComparableStack(ModItems.warhead_generic_large, 1), new ComparableStack(ModBlocks.det_charge, 8), }, 200);
		makeRecipe(new ComparableStack(ModItems.warhead_n2, 1), new AStack[] { new ComparableStack(ModItems.n2_charge, 5), new ComparableStack(ModItems.hull_big_steel, 2), new ComparableStack(ModItems.hull_small_steel, 1), new ComparableStack(ModItems.wire_fine, 6, Mats.MAT_COPPER.id), new OreDictStack(STEEL.plate(), 20), new OreDictStack(TI.plate(), 12), new ComparableStack(ModItems.circuit_targeting_tier3, 1), }, 300);
		makeRecipe(new ComparableStack(ModItems.warhead_nuclear, 1), new AStack[] { new ComparableStack(ModItems.boy_shielding, 1), new ComparableStack(ModItems.boy_target, 1), new ComparableStack(ModItems.boy_bullet, 1), new ComparableStack(ModItems.boy_propellant, 1), new ComparableStack(ModItems.wire_fine, 6, Mats.MAT_MINGRADE.id), new OreDictStack(TI.plate(), 20), new OreDictStack(STEEL.plate(), 12), }, 300);
		makeRecipe(new ComparableStack(ModItems.warhead_mirvlet, 1), new AStack[] { new OreDictStack(W.ingot(), 1), new OreDictStack(STEEL.plate(), 3), new OreDictStack(PU239.ingot(), 1), new OreDictStack(ANY_PLASTICEXPLOSIVE.ingot(), 1) }, 100);
		makeRecipe(new ComparableStack(ModItems.warhead_mirv, 1), new AStack[] { new OreDictStack(TI.plate(), 20), new OreDictStack(STEEL.plate(), 12), new OreDictStack(PU239.ingot(), 1), new OreDictStack(ANY_PLASTICEXPLOSIVE.ingot(), 8), new OreDictStack(BE.ingot(), 4), new OreDictStack(LI.ingot(), 4), new ComparableStack(ItemCell.getFullCell(Fluids.DEUTERIUM, 6))}, 500);
		makeRecipe(new ComparableStack(ModItems.warhead_volcano, 1), new AStack[] {new OreDictStack(TI.plate(), 24), new OreDictStack(STEEL.plate(), 16), new ComparableStack(ModBlocks.det_nuke, 3), new OreDictStack(U238.block(), 24), new ComparableStack(ModItems.circuit_tantalium, 5) }, 600);
		makeRecipe(new ComparableStack(ModItems.warhead_thermo_endo, 1), new AStack[] { new ComparableStack(ModBlocks.therm_endo, 2), new OreDictStack(TI.plate(), 12), new OreDictStack(STEEL.plate(), 6), }, 300);
		makeRecipe(new ComparableStack(ModItems.warhead_thermo_exo, 1), new AStack[] { new ComparableStack(ModBlocks.therm_exo, 2), new OreDictStack(TI.plate(), 12), new OreDictStack(STEEL.plate(), 6), }, 300);
		makeRecipe(new ComparableStack(ModItems.fuel_tank_small, 1), new AStack[] { new ComparableStack(ModItems.canister_generic, 6, Fluids.KEROSENE.getID()), new OreDictStack(TI.plate(), 6), new OreDictStack(STEEL.plate(), 2), }, 100);
		makeRecipe(new ComparableStack(ModItems.fuel_tank_medium, 1), new AStack[] { new ComparableStack(ModItems.canister_generic, 8, Fluids.KEROSENE.getID()), new OreDictStack(TI.plate(), 12), new OreDictStack(STEEL.plate(), 4), }, 150);
		makeRecipe(new ComparableStack(ModItems.fuel_tank_large, 1), new AStack[] { new ComparableStack(ModItems.canister_generic, 12, Fluids.KEROSENE.getID()), new OreDictStack(TI.plate(), 24), new OreDictStack(STEEL.plate(), 8), }, 200);
		makeRecipe(new ComparableStack(ModItems.thruster_small, 1), new AStack[] { new OreDictStack(STEEL.plate(), 4), new OreDictStack(W.ingot(), 4), new ComparableStack(ModItems.wire_fine, 4, Mats.MAT_ALUMINIUM.id), }, 100);
		makeRecipe(new ComparableStack(ModItems.thruster_medium, 1), new AStack[] { new OreDictStack(STEEL.plate(), 8), new OreDictStack(W.ingot(), 8), new ComparableStack(ModItems.motor, 1), new ComparableStack(ModItems.wire_fine, 16, Mats.MAT_COPPER.id), }, 150);
		makeRecipe(new ComparableStack(ModItems.thruster_large, 1), new AStack[] { new OreDictStack(DURA.ingot(), 16), new OreDictStack(W.ingot(), 16), new ComparableStack(ModItems.motor, 2), new ComparableStack(ModItems.wire_fine, 32, Mats.MAT_GOLD.id), new ComparableStack(ModItems.circuit_red_copper, 1), }, 200);
		makeRecipe(new ComparableStack(ModItems.thruster_nuclear, 1), new AStack[] { new OreDictStack(DURA.ingot(), 32), new OreDictStack(B.ingot(), 8), new OreDictStack(PB.plate(), 16), new ComparableStack(ModItems.pipes_steel), new ComparableStack(ModItems.circuit_gold, 1), }, 600);
		makeRecipe(new ComparableStack(ModItems.sat_base, 1), new AStack[] { new ComparableStack(ModItems.thruster_large, 1), new OreDictStack(STEEL.plate(), 6), new ComparableStack(ModItems.plate_desh, 4), new ComparableStack(ModItems.hull_big_titanium, 3), new NbtComparableStack(ItemFluidTank.getFullBarrel(Fluids.KEROSENE)), new ComparableStack(ModItems.photo_panel, 24), new ComparableStack(ModItems.board_copper, 12), new ComparableStack(ModItems.circuit_gold, 6), new ComparableStack(ModItems.battery_lithium_cell_6, 1), }, 500);
		makeRecipe(new ComparableStack(ModItems.sat_head_mapper, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 4), new OreDictStack(STEEL.plate(), 6), new ComparableStack(ModItems.hull_small_steel, 3), new ComparableStack(ModItems.plate_desh, 2), new ComparableStack(ModItems.circuit_gold, 2), new OreDictStack(RUBBER.ingot(), 12), new ComparableStack(Items.REDSTONE, 6), new ComparableStack(Items.DIAMOND, 1), new ComparableStack(Blocks.GLASS_PANE, 6), }, 400);
		makeRecipe(new ComparableStack(ModItems.sat_head_scanner, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 6), new OreDictStack(TI.plate(), 32), new ComparableStack(ModItems.plate_desh, 6), new ComparableStack(ModItems.magnetron, 6), new ComparableStack(ModItems.coil_advanced_torus, 2), new ComparableStack(ModItems.circuit_gold, 6), new OreDictStack(POLYMER.ingot(), 6), new ComparableStack(Items.DIAMOND, 1), }, 400);
		makeRecipe(new ComparableStack(ModItems.sat_head_radar, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 4), new OreDictStack(TI.plate(), 32), new ComparableStack(ModItems.magnetron, 12), new OreDictStack(RUBBER.ingot(), 16), new ComparableStack(ModItems.wire_fine, 16, Mats.MAT_MINGRADE.id), new ComparableStack(ModItems.coil_gold, 3), new ComparableStack(ModItems.circuit_gold, 5), new ComparableStack(Items.DIAMOND, 1), }, 400);
		makeRecipe(new ComparableStack(ModItems.sat_head_laser, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 12), new OreDictStack(W.ingot(), 16), new OreDictStack(ANY_PLASTIC.ingot(), 6), new OreDictStack(RUBBER.ingot(), 16), new ComparableStack(ModItems.board_copper, 24), new ComparableStack(ModItems.circuit_targeting_tier5, 2), new ComparableStack(Items.REDSTONE, 16), new ComparableStack(Items.DIAMOND, 5), new ComparableStack(Blocks.GLASS_PANE, 16), }, 450);
		makeRecipe(new ComparableStack(ModItems.sat_head_resonator, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 32), new OreDictStack(ANY_PLASTIC.ingot(), 48), new OreDictStack(RUBBER.ingot(), 8), new ComparableStack(ModItems.crystal_xen, 1), new OreDictStack(STAR.ingot(), 7), new ComparableStack(ModItems.circuit_targeting_tier5, 6), new ComparableStack(ModItems.circuit_targeting_tier6, 2), }, 1000);
		makeRecipe(new ComparableStack(ModItems.sat_foeq, 1), new AStack[] {
				new OreDictStack(TI.shell(), 3),
				new ComparableStack(ModItems.plate_desh, 8),
				new ComparableStack(ModItems.fluid_barrel_full, 1, Fluids.HYDROGEN.getID()),
				new ComparableStack(ModItems.photo_panel, 16),
				new ComparableStack(ModItems.thruster_nuclear, 1),
				new ComparableStack(ModItems.ingot_uranium_fuel, 6),
				//new ComparableStack(ModItems.circuit, 24, EnumCircuitType.BASIC),
				new ComparableStack(ModItems.circuit_targeting_tier5, 6),
				new ComparableStack(ModItems.magnetron, 3),
				new ComparableStack(ModBlocks.machine_lithium_battery, 1)
		},1200);
		makeRecipe(new ComparableStack(ModItems.sat_miner, 1), new AStack[] { new OreDictStack(BIGMT.plate(), 24), new ComparableStack(ModItems.plate_desh, 8), new ComparableStack(ModItems.motor, 2), new ComparableStack(ModItems.drill_titanium, 2), new ComparableStack(ModItems.circuit_targeting_tier4, 2), new NbtComparableStack(ItemFluidTank.getFullBarrel(Fluids.KEROSENE)), new ComparableStack(ModItems.thruster_small, 1), new ComparableStack(ModItems.photo_panel, 12), new ComparableStack(ModItems.centrifuge_element, 4), new ComparableStack(ModItems.magnetron, 3), new OreDictStack(RUBBER.ingot(), 12), new ComparableStack(ModItems.battery_lithium_cell_6, 1), }, 600);
		makeRecipe(new ComparableStack(ModItems.chopper_head, 1), new AStack[] { new ComparableStack(ModBlocks.reinforced_glass, 2), new ComparableStack(ModBlocks.fwatz_computer, 1), new OreDictStack(CMB.ingot(), 22), new ComparableStack(ModItems.wire_fine, 4, Mats.MAT_MAGTUNG.id), }, 300);
		makeRecipe(new ComparableStack(ModItems.chopper_gun, 1), new AStack[] { new OreDictStack(CMB.plate(), 4), new OreDictStack(CMB.ingot(), 2), new ComparableStack(ModItems.wire_fine, 6, Mats.MAT_TUNGSTEN.id), new ComparableStack(ModItems.coil_magnetized_tungsten, 1), new ComparableStack(ModItems.motor, 1), }, 150);
		makeRecipe(new ComparableStack(ModItems.chopper_torso, 1), new AStack[] { new OreDictStack(CMB.ingot(), 26), new ComparableStack(ModBlocks.fwatz_computer, 1), new ComparableStack(ModItems.wire_fine, 4, Mats.MAT_MAGTUNG.id), new ComparableStack(ModItems.motor, 2), new ComparableStack(ModItems.chopper_blades, 2), }, 350);
		makeRecipe(new ComparableStack(ModItems.chopper_tail, 1), new AStack[] { new OreDictStack(CMB.plate(), 8), new OreDictStack(CMB.ingot(), 5), new ComparableStack(ModItems.wire_fine, 4, Mats.MAT_MAGTUNG.id), new ComparableStack(ModItems.motor, 1), new ComparableStack(ModItems.chopper_blades, 2), }, 200);
		makeRecipe(new ComparableStack(ModItems.chopper_wing, 1), new AStack[] { new OreDictStack(CMB.plate(), 6), new OreDictStack(CMB.ingot(), 3), new ComparableStack(ModItems.wire_fine, 2, Mats.MAT_MAGTUNG.id), }, 150);
		makeRecipe(new ComparableStack(ModItems.chopper_blades, 1), new AStack[] { new OreDictStack(CMB.plate(), 8), new OreDictStack(STEEL.plate(), 2), new OreDictStack(CMB.ingot(), 2), }, 200);
		makeRecipe(new ComparableStack(ModItems.circuit_aluminium, 1), new AStack[] { new ComparableStack(ModItems.circuit_raw, 1), }, 50);
		makeRecipe(new ComparableStack(ModItems.circuit_copper, 1), new AStack[] {new ComparableStack(ModItems.circuit_aluminium, 1), new ComparableStack(ModItems.wire_fine, 4, Mats.MAT_COPPER.id), new OreDictStack(NETHERQUARTZ.dust(), 1), new OreDictStack(CU.plate(), 1), },100);
		makeRecipe(new ComparableStack(ModItems.circuit_red_copper, 1), new AStack[] {new ComparableStack(ModItems.circuit_copper, 1), new ComparableStack(ModItems.wire_fine, 4, Mats.MAT_MINGRADE.id), new OreDictStack(GOLD.dust(), 1), new ComparableStack(ModItems.plate_polymer, 1), },150);
		makeRecipe(new ComparableStack(ModItems.tritium_deuterium_cake, 1), new AStack[] { new ComparableStack(ItemCell.getFullCell(Fluids.DEUTERIUM, 6)), new ComparableStack(ItemCell.getFullCell(Fluids.TRITIUM, 2)), new ComparableStack(ModItems.lithium, 4), }, 150);
		makeRecipe(new ComparableStack(ModItems.pellet_cluster, 1), new AStack[] { new OreDictStack(STEEL.plate(), 4), new ComparableStack(Blocks.TNT, 1), }, 50);
		makeRecipe(new ComparableStack(ModItems.pellet_buckshot, 1), new AStack[] { new OreDictStack(PB.nugget(), 6), }, 50);
		makeRecipe(new ComparableStack(ModItems.australium_iii, 1), new AStack[] { new ComparableStack(ModItems.rod_australium, 1), new OreDictStack(STEEL.ingot(), 1), new OreDictStack(STEEL.plate(), 6), new OreDictStack(CU.plate(), 2), new ComparableStack(ModItems.wire_fine, 6, Mats.MAT_COPPER.id), }, 150);
		makeRecipe(new ComparableStack(ModItems.magnetron, 1), new AStack[] { new OreDictStack(ALLOY.ingot(), 1), new OreDictStack(ALLOY.plate(), 2), new ComparableStack(ModItems.wire_fine, 1, Mats.MAT_TUNGSTEN.id), new ComparableStack(ModItems.coil_tungsten, 1), }, 100);
		makeRecipe(new ComparableStack(ModItems.upgrade_template, 1), new AStack[] { new OreDictStack(STEEL.plate(), 1), new OreDictStack(IRON.plate(), 4), new OreDictStack(CU.plate(), 2), new ComparableStack(ModItems.wire_fine, 6, Mats.MAT_COPPER.id), }, 100);
		makeRecipe(new ComparableStack(ModItems.upgrade_speed_1, 1), new AStack[] { new ComparableStack(ModItems.upgrade_template, 1), new OreDictStack(MINGRADE.dust(), 4), new ComparableStack(Items.REDSTONE, 6), new ComparableStack(ModItems.wire_fine, 4, Mats.MAT_MINGRADE.id), }, 200);
		makeRecipe(new ComparableStack(ModItems.upgrade_speed_2, 1), new AStack[] { new ComparableStack(ModItems.upgrade_speed_1, 1), new OreDictStack(MINGRADE.dust(), 2), new ComparableStack(Items.REDSTONE, 4), new ComparableStack(ModItems.circuit_red_copper, 4), new ComparableStack(ModItems.wire_fine, 4, Mats.MAT_MINGRADE.id), new OreDictStack(ANY_PLASTIC.ingot(), 2), }, 300);
		makeRecipe(new ComparableStack(ModItems.upgrade_speed_3, 1), new AStack[] { new ComparableStack(ModItems.upgrade_speed_2, 1), new OreDictStack(MINGRADE.dust(), 2), new ComparableStack(Items.REDSTONE, 6), new OreDictStack(DESH.ingot(), 4), }, 500);
		makeRecipe(new ComparableStack(ModItems.upgrade_effect_1, 1), new AStack[] { new ComparableStack(ModItems.upgrade_template, 1), new OreDictStack(DURA.dust(), 4), new OreDictStack(STEEL.dust(), 6), new ComparableStack(ModItems.wire_fine, 4, Mats.MAT_MINGRADE.id), }, 200);
		makeRecipe(new ComparableStack(ModItems.upgrade_effect_2, 1), new AStack[] { new ComparableStack(ModItems.upgrade_effect_1, 1), new OreDictStack(DURA.dust(), 2), new OreDictStack(STEEL.dust(), 4), new ComparableStack(ModItems.circuit_red_copper, 4), new ComparableStack(ModItems.wire_fine, 4, Mats.MAT_MINGRADE.id), new OreDictStack(ANY_PLASTIC.ingot(), 2), }, 300);
		makeRecipe(new ComparableStack(ModItems.upgrade_effect_3, 1), new AStack[] { new ComparableStack(ModItems.upgrade_effect_2, 1), new OreDictStack(DURA.dust(), 2), new OreDictStack(STEEL.dust(), 6), new OreDictStack(DESH.ingot(), 4), }, 500);
		makeRecipe(new ComparableStack(ModItems.upgrade_power_1, 1), new AStack[] { new ComparableStack(ModItems.upgrade_template, 1), new OreDictStack(LAPIS.dust(), 4), new ComparableStack(Items.GLOWSTONE_DUST, 6), new ComparableStack(ModItems.wire_fine, 4, Mats.MAT_MINGRADE.id), }, 200);
		makeRecipe(new ComparableStack(ModItems.upgrade_power_2, 1), new AStack[] { new ComparableStack(ModItems.upgrade_power_1, 1), new OreDictStack(LAPIS.dust(), 2), new ComparableStack(Items.GLOWSTONE_DUST, 4), new ComparableStack(ModItems.circuit_red_copper, 4), new ComparableStack(ModItems.wire_fine, 4, Mats.MAT_MINGRADE.id), new OreDictStack(ANY_PLASTIC.ingot(), 2), }, 300);
		makeRecipe(new ComparableStack(ModItems.upgrade_power_3, 1), new AStack[] { new ComparableStack(ModItems.upgrade_power_2, 1), new OreDictStack(LAPIS.dust(), 2), new ComparableStack(Items.GLOWSTONE_DUST, 6), new OreDictStack(DESH.ingot(), 4), }, 500);
		makeRecipe(new ComparableStack(ModItems.upgrade_fortune_1, 1), new AStack[] { new ComparableStack(ModItems.upgrade_template, 1), new OreDictStack(DIAMOND.dust(), 4), new OreDictStack(IRON.dust(), 6), new ComparableStack(ModItems.wire_fine, 4, Mats.MAT_MINGRADE.id), }, 200);
		makeRecipe(new ComparableStack(ModItems.upgrade_fortune_2, 1), new AStack[] { new ComparableStack(ModItems.upgrade_fortune_1, 1), new OreDictStack(DIAMOND.dust(), 2), new OreDictStack(IRON.dust(), 4), new ComparableStack(ModItems.circuit_red_copper, 4), new ComparableStack(ModItems.wire_fine, 4, Mats.MAT_MINGRADE.id), new OreDictStack(ANY_PLASTIC.ingot(), 2), }, 300);
		makeRecipe(new ComparableStack(ModItems.upgrade_fortune_3, 1), new AStack[] { new ComparableStack(ModItems.upgrade_fortune_2, 1), new OreDictStack(DIAMOND.dust(), 2), new OreDictStack(IRON.dust(), 6), new OreDictStack(DESH.ingot(), 4), }, 500);
		makeRecipe(new ComparableStack(ModItems.upgrade_afterburn_1, 1), new AStack[] { new ComparableStack(ModItems.upgrade_template, 1), new OreDictStack(ANY_PLASTIC.dust(), 4), new OreDictStack(W.dust(), 6), new ComparableStack(ModItems.wire_fine, 4, Mats.MAT_MINGRADE.id), }, 200);
		makeRecipe(new ComparableStack(ModItems.upgrade_afterburn_2, 1), new AStack[] { new ComparableStack(ModItems.upgrade_afterburn_1, 1), new OreDictStack(ANY_PLASTIC.dust(), 2), new OreDictStack(W.dust(), 4), new ComparableStack(ModItems.circuit_red_copper, 4), new ComparableStack(ModItems.wire_fine, 4, Mats.MAT_MINGRADE.id), new OreDictStack(ANY_PLASTIC.ingot(), 2), }, 300);
		makeRecipe(new ComparableStack(ModItems.upgrade_afterburn_3, 1), new AStack[] { new ComparableStack(ModItems.upgrade_afterburn_2, 1), new OreDictStack(ANY_PLASTIC.dust(), 2), new OreDictStack(W.dust(), 6), new OreDictStack(DESH.ingot(), 4), }, 500);
		makeRecipe(new ComparableStack(ModItems.upgrade_radius, 1), new AStack[] { new ComparableStack(ModItems.upgrade_template, 1), new ComparableStack(Items.GLOWSTONE_DUST, 6), new OreDictStack(DIAMOND.dust(), 4), }, 500);
		makeRecipe(new ComparableStack(ModItems.upgrade_health, 1), new AStack[] { new ComparableStack(ModItems.upgrade_template, 1), new ComparableStack(Items.GLOWSTONE_DUST, 6), new OreDictStack(TI.dust(), 4), }, 500);
		makeRecipe(new ComparableStack(ModItems.upgrade_overdrive_1, 1), new AStack[] {new ComparableStack(ModItems.upgrade_speed_3, 1), new ComparableStack(ModItems.upgrade_effect_3, 1), new OreDictStack(DESH.ingot(), 8), new ComparableStack(ModItems.powder_power, 16), new ComparableStack(ModItems.crystal_lithium, 4), new ComparableStack(ModItems.circuit_schrabidium, 1), }, 200);
		makeRecipe(new ComparableStack(ModItems.upgrade_overdrive_2, 1), new AStack[] {new ComparableStack(ModItems.upgrade_overdrive_1, 1), new ComparableStack(ModItems.upgrade_afterburn_1, 1), new ComparableStack(ModItems.upgrade_speed_3, 1), new ComparableStack(ModItems.upgrade_effect_3, 1), new ComparableStack(ModItems.crystal_lithium, 8), new ComparableStack(ModItems.circuit_tantalium, 16), }, 300);
		makeRecipe(new ComparableStack(ModItems.upgrade_overdrive_3, 1), new AStack[] {new ComparableStack(ModItems.upgrade_overdrive_2, 1), new ComparableStack(ModItems.upgrade_afterburn_1, 1), new ComparableStack(ModItems.upgrade_speed_3, 1), new ComparableStack(ModItems.upgrade_effect_3, 1), new ComparableStack(ModItems.crystal_lithium, 16), new OreDictStack(KEY_CIRCUIT_BISMUTH), }, 500);
		makeRecipe(new ComparableStack(ModItems.fuse, 1), new AStack[] { new OreDictStack(STEEL.plate(), 2), new ComparableStack(Blocks.GLASS_PANE, 1), new ComparableStack(ModItems.wire_fine, 1, Mats.MAT_ALUMINIUM.id), }, 100);
		makeRecipe(new ComparableStack(ModItems.redcoil_capacitor, 1), new AStack[] { new OreDictStack(GOLD.plate(), 3), new ComparableStack(ModItems.fuse, 1), new ComparableStack(ModItems.wire_fine, 4, Mats.MAT_ALLOY.id), new ComparableStack(ModItems.coil_advanced_alloy, 6), new ComparableStack(Blocks.REDSTONE_BLOCK, 2), }, 200);
		makeRecipe(new ComparableStack(ModItems.part_lithium, 4), new AStack[] { new OreDictStack(ANY_RUBBER.ingot(), 1), new OreDictStack(LI.dust(), 1), }, 50);
		makeRecipe(new ComparableStack(ModItems.part_beryllium, 4), new AStack[] { new OreDictStack(ANY_RUBBER.ingot(), 1), new OreDictStack(BE.dust(), 1), }, 50);
		makeRecipe(new ComparableStack(ModItems.part_carbon, 4), new AStack[] { new OreDictStack(ANY_RUBBER.ingot(), 1), new OreDictStack(COAL.dust(), 1), }, 50);
		makeRecipe(new ComparableStack(ModItems.part_copper, 4), new AStack[] { new OreDictStack(ANY_RUBBER.ingot(), 1), new OreDictStack(CU.dust(), 1), }, 50);
		makeRecipe(new ComparableStack(ModItems.part_plutonium, 4), new AStack[] { new OreDictStack(ANY_RUBBER.ingot(), 1), new OreDictStack(PU.dust(), 1), }, 50);
		makeRecipe(new ComparableStack(ModItems.thermo_element, 1), new AStack[] { new OreDictStack(STEEL.plate(), 1), new OreDictStack(CU.plate(), 2), new ComparableStack(ModItems.wire_fine, 2, Mats.MAT_MINGRADE.id), new ComparableStack(ModItems.wire_fine, 2, Mats.MAT_ALUMINIUM.id), new OreDictStack(NETHERQUARTZ.dust(), 2), }, 150);
		//makeRecipe(new ComparableStack(ModItems.limiter, 1), new AStack[] {new OreDictStack(STEEL.plate(), 3), new OreDictStack(IRON.plate(), 1), new ComparableStack(ModItems.circuit_copper, 2), new ComparableStack(ModItems.wire_fine, 4, Mats.MAT_COPPER.id), },150);
		makeRecipe(new ComparableStack(ModItems.plate_dalekanium, 1), new AStack[] { new ComparableStack(ModBlocks.block_meteor, 1), }, 50);
		makeRecipe(new ComparableStack(ModBlocks.block_meteor, 1), new AStack[] { new ComparableStack(ModItems.fragment_meteorite, 64), new ComparableStack(ModItems.fragment_meteorite, 36) }, 500);
		makeRecipe(new ComparableStack(ModBlocks.cmb_brick, 8), new AStack[] { new OreDictStack(CMB.ingot(), 1), new OreDictStack(CMB.plate(), 8), }, 100);
		makeRecipe(new ComparableStack(ModBlocks.cmb_brick_reinforced, 8), new AStack[] { new ComparableStack(ModBlocks.block_magnetized_tungsten, 4), new ComparableStack(ModBlocks.brick_concrete, 4), new ComparableStack(ModBlocks.cmb_brick, 1), new OreDictStack(STEEL.plate(), 4), }, 200);
		makeRecipe(new ComparableStack(ModBlocks.seal_frame, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 3), new ComparableStack(ModItems.wire_fine, 4, Mats.MAT_ALUMINIUM.id), new ComparableStack(Items.REDSTONE, 2), new ComparableStack(ModBlocks.steel_roof, 5), }, 50);
		makeRecipe(new ComparableStack(ModBlocks.seal_controller, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 3), new OreDictStack(ANY_PLASTIC.ingot(), 4), new OreDictStack(MINGRADE.ingot(), 1), new ComparableStack(Items.REDSTONE, 4), new ComparableStack(ModBlocks.steel_roof, 5), }, 100);
		makeRecipe(new ComparableStack(ModBlocks.vault_door, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 64), new OreDictStack(STEEL.ingot(), 64), new OreDictStack(W.ingot(), 32), new OreDictStack(PB.plate(), 48), new OreDictStack(ALLOY.plate(), 8), new OreDictStack(ANY_RUBBER.ingot(), 16), new ComparableStack(ModItems.bolt_tungsten, 18), new ComparableStack(ModItems.bolt_dura_steel, 27), new ComparableStack(ModItems.motor, 5), }, 200);
		makeRecipe(new ComparableStack(ModBlocks.blast_door, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 16), new OreDictStack(W.ingot(), 8), new OreDictStack(PB.plate(), 12), new OreDictStack(ALLOY.plate(), 3), new OreDictStack(ANY_RUBBER.ingot(), 3), new ComparableStack(ModItems.bolt_tungsten, 3), new ComparableStack(ModItems.bolt_dura_steel, 3), new ComparableStack(ModItems.motor, 1), }, 300);
		makeRecipe(new ComparableStack(ModBlocks.sliding_blast_door_2, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 16), new OreDictStack(W.ingot(), 8), new ComparableStack(ModItems.circuit_gold, 3), new OreDictStack(ANY_RUBBER.ingot(), 4), new ComparableStack(ModItems.bolt_dura_steel, 16), new ComparableStack(ModItems.motor, 2), }, 300);
		makeRecipe(new ComparableStack(ModBlocks.sliding_blast_door, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 16), new OreDictStack(W.ingot(), 8), new ComparableStack(ModBlocks.reinforced_glass, 4), new OreDictStack(ANY_RUBBER.ingot(), 4), new ComparableStack(ModItems.bolt_dura_steel, 16), new ComparableStack(ModItems.motor, 2), }, 300);
		makeRecipe(new ComparableStack(ModBlocks.machine_centrifuge, 1), new AStack[] { new ComparableStack(ModItems.centrifuge_element, 1), new OreDictStack(ANY_PLASTIC.ingot(), 2), new OreDictStack(STEEL.plate(), 8), new OreDictStack(CU.plate(), 8), new ComparableStack(ModItems.circuit_copper, 1), }, 200);
		makeRecipe(new ComparableStack(ModBlocks.machine_gascent, 1), new AStack[] { new ComparableStack(ModItems.centrifuge_element, 4), new OreDictStack(ANY_PLASTIC.ingot(), 4), new OreDictStack(DESH.ingot(), 2), new OreDictStack(STEEL.plate(), 8), new ComparableStack(ModItems.coil_tungsten, 4), new ComparableStack(ModItems.circuit_red_copper, 1), }, 300);
		makeRecipe(new ComparableStack(ModBlocks.machine_rtg_furnace_off, 1), new AStack[] { new ComparableStack(Blocks.FURNACE, 1), new ComparableStack(ModItems.rtg_unit, 3), new OreDictStack(PB.plate(), 6), new OreDictStack(OreDictManager.getReflector(), 4), new OreDictStack(CU.plate(), 2), }, 150);
		makeRecipe(new ComparableStack(ModBlocks.machine_difurnace_rtg_off, 1), new AStack[] {
				new ComparableStack(ModBlocks.machine_difurnace_off, 1),
				new ComparableStack(ModItems.rtg_unit, 3),
				new OreDictStack(DESH.ingot(), 4),
				new OreDictStack(PB.plate(), 6),
				new OreDictStack(OreDictManager.getReflector(), 8),
				new OreDictStack(CU.plate(), 12)
			}, 150);
		makeRecipe(new ComparableStack(ModBlocks.machine_radgen, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 8), new OreDictStack(STEEL.plate(), 32), new ComparableStack(ModItems.coil_magnetized_tungsten, 6), new ComparableStack(ModItems.wire_fine, 24, Mats.MAT_MAGTUNG.id), new ComparableStack(ModItems.circuit_gold, 4), new ComparableStack(ModItems.reactor_core, 3), new OreDictStack(STAR.ingot(), 1), new OreDictStack(KEY_RED, 1), }, 400);
		makeRecipe(new ComparableStack(ModBlocks.machine_diesel, 1), new AStack[] { new ComparableStack(ModItems.hull_small_steel, 4), new ComparableStack(Blocks.PISTON, 4), new OreDictStack(STEEL.ingot(), 6), new OreDictStack(MINGRADE.ingot(), 2), new OreDictStack(CU.plate(), 4), new ComparableStack(ModItems.wire_fine, 6, Mats.MAT_MINGRADE.id), }, 200);
		//makeRecipe(new ComparableStack(ModBlocks.machine_industrial_generator, 1), new AStack[] {new ComparableStack(ModItems.generator_front, 1), new ComparableStack(ModItems.generator_steel, 3), new ComparableStack(ModItems.rotor_steel, 3), new OreDictStack(STEEL.ingot(), 6), new ComparableStack(ModItems.board_copper, 4), new ComparableStack(ModItems.wire_fine, 8, Mats.MAT_GOLD.id), new ComparableStack(ModBlocks.red_wire_coated, 2), new ComparableStack(ModItems.pedestal_steel, 2), new ComparableStack(ModItems.circuit_copper, 4), },500);
		makeRecipe(new ComparableStack(ModBlocks.machine_rtg_grey, 1), new AStack[] { new ComparableStack(ModItems.rtg_unit, 3), new OreDictStack(STEEL.plate(), 4), new ComparableStack(ModItems.wire_fine, 4, Mats.MAT_MINGRADE.id), new OreDictStack(ANY_PLASTIC.ingot(), 3), }, 200);
		
		//Batteries
		makeRecipe(new ComparableStack(ModBlocks.machine_battery, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 4), new OreDictStack(S.dust(), 12), new OreDictStack(PB.dust(), 12), new OreDictStack(MINGRADE.ingot(), 2), new ComparableStack(ModItems.wire_fine, 4, Mats.MAT_MINGRADE.id), }, 200);
		makeRecipe(new ComparableStack(ModBlocks.machine_lithium_battery, 1), new AStack[] { 
			new OreDictStack(POLYMER.ingot(), 6), 
			new OreDictStack(CO.dust(), 12), 
			new OreDictStack(LI.dust(), 12), 
			new OreDictStack(ALLOY.ingot(), 3), 
			new ComparableStack(ModItems.wire_fine, 4, Mats.MAT_MINGRADE.id), }, 300);
		makeRecipe(new ComparableStack(ModBlocks.machine_desh_battery, 1), new AStack[] { 
			new OreDictStack(DESH.ingot(), 12), 
			new OreDictStack(BAKELITE.dust(), 12), 
			new OreDictStack(P_RED.dust(), 12), 
			new OreDictStack(GRAPHITE.ingot(), 6), 
			new OreDictStack(RA226.nugget(), 2), }, 400);
		makeRecipe(new ComparableStack(ModBlocks.machine_saturnite_battery, 1), new AStack[] {
			new OreDictStack(BIGMT.ingot(), 8), 
			new OreDictStack(DURA.dust(), 12), 
			new OreDictStack(ZR.dust(), 12), 
			new OreDictStack(PU238.ingot(), 4), 
			new OreDictStack(TC99.nugget(), 2), }, 600);
		makeRecipe(new ComparableStack(ModBlocks.machine_schrabidium_battery, 1), new AStack[] { 
			new OreDictStack(SA326.ingot(), 16), 
			new OreDictStack(SA326.dust(), 12), 
			new OreDictStack(NP237.dust(), 12), 
			new OreDictStack(REIIUM.ingot(), 8), 
			new OreDictStack(ANY_BISMOID.nugget(), 2), }, 800);
		makeRecipe(new ComparableStack(ModBlocks.machine_euphemium_battery, 1), new AStack[] { 
			new OreDictStack(EUPH.ingot(), 24), 
			new OreDictStack(XE135.dust(), 16), 
			new OreDictStack(SBD.dust(), 16), 
			new OreDictStack(STAR.ingot(), 12), 
			new OreDictStack(GH336.nugget(), 8), }, 1600);
		makeRecipe(new ComparableStack(ModBlocks.machine_radspice_battery, 1), new AStack[] { 
			new ComparableStack(ModItems.ingot_radspice, 32), 
			new ComparableStack(ModItems.powder_chlorophyte, 24), 
			new ComparableStack(ModItems.powder_balefire, 24), 
			new OreDictStack(VERTICIUM.ingot(), 16), 
			new OreDictStack(AS.nugget(), 8), }, 3200);
		makeRecipe(new ComparableStack(ModBlocks.machine_dineutronium_battery, 1), new AStack[] { 
			new OreDictStack(DNT.ingot(), 48), 
			new ComparableStack(ModItems.powder_spark_mix, 32), 
			new ComparableStack(ModItems.battery_spark_cell_1000, 1), 
			new OreDictStack(CMB.ingot(), 24), 
			new ComparableStack(ModItems.coil_magnetized_tungsten, 12), }, 4800);
		makeRecipe(new ComparableStack(ModBlocks.machine_electronium_battery, 1), new AStack[] { 
			new ComparableStack(ModItems.ingot_electronium, 64), 
			new OreDictStack(OSMIRIDIUM.dust(), 48), 
			new ComparableStack(ModItems.battery_spark_cell_10000, 1), 
			new OreDictStack(AU198.ingot(), 32), 
			new ComparableStack(ModItems.nugget_u238m2, 16), }, 6400);
		
		makeRecipe(new ComparableStack(ModBlocks.machine_shredder, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 2), new OreDictStack(STEEL.plate(), 4), new ComparableStack(ModItems.motor, 2), new ComparableStack(ModItems.wire_fine, 2, Mats.MAT_MINGRADE.id), new ComparableStack(ModBlocks.steel_beam, 2), new ComparableStack(Blocks.IRON_BARS, 2), new ComparableStack(ModBlocks.red_wire_coated, 1), }, 200);
		makeRecipe(new ComparableStack(ModBlocks.machine_well, 1), new AStack[] { new ComparableStack(ModBlocks.steel_scaffold, 20), new ComparableStack(ModBlocks.steel_beam, 8), new ComparableStack(ModItems.tank_steel, 2), new ComparableStack(ModItems.motor, 1), new ComparableStack(ModItems.pipes_steel, 3), new ComparableStack(ModItems.drill_titanium, 1), new ComparableStack(ModItems.wire_fine, 6, Mats.MAT_MINGRADE.id), }, 250);
		makeRecipe(new ComparableStack(ModBlocks.machine_pumpjack, 1), new AStack[] { new ComparableStack(ModBlocks.steel_scaffold, 8), new OreDictStack(STEEL.block(), 8), new OreDictStack(ANY_PLASTIC.block(), 2), new ComparableStack(ModItems.pipes_steel, 4), new ComparableStack(ModItems.tank_steel, 4), new OreDictStack(STEEL.ingot(), 24), new OreDictStack(STEEL.plate(), 16), new OreDictStack(AL.plate(), 6), new ComparableStack(ModItems.drill_titanium, 1), new ComparableStack(ModItems.motor, 2), new ComparableStack(ModItems.wire_fine, 8, Mats.MAT_MINGRADE.id), }, 400);
		makeRecipe(new ComparableStack(ModBlocks.machine_fracking_tower, 1), new AStack[] { new ComparableStack(ModBlocks.steel_scaffold, 40), new ComparableStack(ModBlocks.concrete_smooth, 64), new ComparableStack(ModItems.drill_titanium, 1), new ComparableStack(ModItems.motor_desh, 2), new ComparableStack(ModItems.plate_desh, 6), new OreDictStack(NB.ingot(), 8), new ComparableStack(ModItems.tank_steel, 24), new ComparableStack(ModItems.pipes_steel, 2), }, 600);
		makeRecipe(new ComparableStack(ModBlocks.machine_catalytic_cracker), new AStack[] {
				new ComparableStack(ModBlocks.steel_scaffold, 16),
				new ComparableStack(ModItems.hull_big_steel, 4),
				new ComparableStack(ModItems.tank_steel, 3),
				new OreDictStack(ANY_PLASTIC.ingot(), 4),
				new OreDictStack(NB.ingot(), 2),
				new OreDictStack(LA.ingot(), 1),
				new ComparableStack(ModItems.catalyst_clay, 12),
				}, 300);
		makeRecipe(new ComparableStack(ModBlocks.machine_flare, 1), new AStack[] { new ComparableStack(ModBlocks.steel_scaffold, 28), new ComparableStack(ModItems.tank_steel, 2), new ComparableStack(ModItems.pipes_steel, 2), new ComparableStack(ModItems.hull_small_steel, 1), new ComparableStack(ModItems.thermo_element, 3), }, 200);
		makeRecipe(new ComparableStack(ModBlocks.machine_refinery, 1), new AStack[] {new OreDictStack(STEEL.ingot(), 16), new OreDictStack(STEEL.plate(), 20), new OreDictStack(CU.plate(), 16), new ComparableStack(ModItems.hull_big_steel, 6), new ComparableStack(ModItems.pipes_steel, 2), new ComparableStack(ModItems.coil_tungsten, 8), new ComparableStack(ModItems.wire_fine, 8, Mats.MAT_MINGRADE.id), new ComparableStack(ModItems.circuit_copper, 2), new ComparableStack(ModItems.circuit_red_copper, 1), new ComparableStack(ModItems.plate_polymer, 8), },350);
		makeRecipe(new ComparableStack(ModBlocks.machine_epress, 1), new AStack[] { new OreDictStack(STEEL.plate(), 8), new OreDictStack(ANY_RUBBER.ingot(), 4), new ComparableStack(ModItems.pipes_steel, 1), new ComparableStack(ModItems.bolt_tungsten, 4), new ComparableStack(ModItems.coil_copper, 2), new ComparableStack(ModItems.motor, 1), new ComparableStack(ModItems.circuit_copper, 1), new ComparableStack(ModItems.canister_generic, 1, Fluids.LUBRICANT.getID()), }, 160);
		makeRecipe(new ComparableStack(ModBlocks.machine_chemplant, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 8), new OreDictStack(CU.plate(), 6), new ComparableStack(ModItems.hull_small_steel, 2), new ComparableStack(ModItems.tank_steel, 4), new ComparableStack(ModItems.hull_big_steel, 1), new ComparableStack(ModItems.wire_fine, 16, Mats.MAT_MINGRADE.id), new ComparableStack(ModItems.wire_fine, 3, Mats.MAT_TUNGSTEN.id), new ComparableStack(ModItems.circuit_copper, 4), new ComparableStack(ModItems.circuit_red_copper, 2), new ComparableStack(ModItems.plate_polymer, 8), }, 200);
		makeRecipe(new ComparableStack(ModBlocks.machine_assemfac, 1), new AStack[] {new OreDictStack(STEEL.ingot(), 48), new OreDictStack(ANY_RESISTANTALLOY.ingot(), 8), new OreDictStack(B.ingot(), 4), new OreDictStack(RUBBER.ingot(), 16), new OreDictStack(KEY_ANYPANE, 64), new ComparableStack(ModItems.motor, 18), new ComparableStack(ModItems.bolt_tungsten, 16), new ComparableStack(ModItems.pipes_steel, 8), new ComparableStack(ModItems.circuit_aluminium, 16)}, 400);
		makeRecipe(new ComparableStack(ModBlocks.machine_chemfac, 1), new AStack[]{new OreDictStack(STEEL.ingot(), 48), new OreDictStack(ANY_RESISTANTALLOY.dust(), 8), new OreDictStack(NB.ingot(), 4), new OreDictStack(RUBBER.ingot(), 16), new ComparableStack(ModItems.hull_big_steel, 12), new ComparableStack(ModItems.tank_steel, 8), new ComparableStack(ModItems.motor_desh, 4), new ComparableStack(ModItems.coil_tungsten, 24), new ComparableStack(ModItems.pipes_steel, 1), new ComparableStack(ModItems.circuit_gold, 3)}, 400);
		makeRecipe(new ComparableStack(ModBlocks.machine_crystallizer, 1), new AStack[] { new ComparableStack(ModItems.hull_big_steel, 4), new ComparableStack(ModItems.pipes_steel, 4), new OreDictStack(DESH.ingot(), 4), new ComparableStack(ModItems.motor, 2), new ComparableStack(ModItems.blades_advanced_alloy, 2), new OreDictStack(STEEL.ingot(), 16), new OreDictStack(TI.plate(), 16), new ComparableStack(Blocks.GLASS, 4), new ComparableStack(ModItems.circuit_gold, 1), }, 400);
		makeRecipe(new ComparableStack(ModBlocks.machine_fluidtank, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 2), new OreDictStack(STEEL.plate(), 6), new ComparableStack(ModItems.hull_big_steel, 4), }, 150);
		makeRecipe(new ComparableStack(ModBlocks.machine_mining_laser, 1), new AStack[] { new ComparableStack(ModItems.tank_steel, 3), new OreDictStack(STEEL.ingot(), 8), new OreDictStack(STEEL.plate(), 12), new ComparableStack(ModItems.crystal_redstone, 3), new ComparableStack(Items.DIAMOND, 5), new OreDictStack(ANY_PLASTIC.ingot(), 8), new ComparableStack(ModItems.motor, 3), new OreDictStack(DURA.ingot(), 4), new ComparableStack(ModItems.bolt_dura_steel, 6), new ComparableStack(ModBlocks.machine_saturnite_battery, 1), }, 400);
		makeRecipe(new ComparableStack(ModBlocks.machine_turbofan, 1), new AStack[] { new ComparableStack(ModItems.hull_big_steel, 1), new ComparableStack(ModItems.hull_big_titanium, 3), new ComparableStack(ModItems.hull_small_steel, 2), new ComparableStack(ModItems.turbine_tungsten, 1), new ComparableStack(ModItems.turbine_titanium, 7), new ComparableStack(ModItems.bolt_compound, 8), new OreDictStack(MINGRADE.ingot(), 12), new ComparableStack(ModItems.wire_fine, 24, Mats.MAT_MINGRADE.id), }, 500);
		makeRecipe(new ComparableStack(ModBlocks.machine_teleporter, 1), new AStack[] { new OreDictStack(TI.ingot(), 6), new OreDictStack(ALLOY.plate(), 12), new OreDictStack(CMB.plate(), 4), new ComparableStack(ModItems.telepad, 1), new ComparableStack(ModItems.entanglement_kit, 1), new ComparableStack(ModBlocks.machine_battery, 2), new ComparableStack(ModItems.coil_magnetized_tungsten, 4), }, 300);
		makeRecipe(new ComparableStack(ModBlocks.machine_schrabidium_transmutator, 1), new AStack[] { new OreDictStack(MAGTUNG.ingot(), 1), new OreDictStack(TI.ingot(), 24), new OreDictStack(ALLOY.plate(), 18), new OreDictStack(STEEL.plate(), 12), new ComparableStack(ModItems.plate_desh, 6), new OreDictStack(RUBBER.ingot(), 8), new ComparableStack(ModBlocks.machine_battery, 5), new ComparableStack(ModItems.circuit_gold, 5), }, 500);
		makeRecipe(new ComparableStack(ModBlocks.machine_combine_factory, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 8), new OreDictStack(ANY_PLASTIC.ingot(), 6), new OreDictStack(TI.plate(), 4), new OreDictStack(CU.plate(), 6), new ComparableStack(ModItems.circuit_gold, 6), new ComparableStack(ModItems.coil_advanced_alloy, 8), new ComparableStack(ModItems.coil_tungsten, 4), new OreDictStack(MAGTUNG.ingot(), 12), }, 150);
		makeRecipe(new ComparableStack(ModBlocks.factory_advanced_hull, 1), new AStack[] { new OreDictStack(ALLOY.ingot(), 4), new OreDictStack(ALLOY.plate(), 4), new OreDictStack(POLYMER.ingot(), 2), new ComparableStack(ModItems.wire_fine, 6, Mats.MAT_ALLOY.id), }, 50);
		makeRecipe(new ComparableStack(ModBlocks.factory_advanced_furnace, 1), new AStack[] { new OreDictStack(ALLOY.ingot(), 4), new OreDictStack(ALLOY.plate(), 4), new OreDictStack(STEEL.plate(), 8), new OreDictStack(POLYMER.ingot(), 4), new ComparableStack(ModItems.coil_advanced_alloy, 2), }, 100);
		makeRecipe(new ComparableStack(ModBlocks.factory_advanced_core, 1), new AStack[] { new OreDictStack(ALLOY.ingot(), 6), new OreDictStack(ALLOY.plate(), 6), new OreDictStack(STEEL.plate(), 8), new ComparableStack(ModItems.coil_advanced_alloy, 2), new ComparableStack(ModItems.motor, 16), new ComparableStack(Blocks.PISTON, 6), new OreDictStack(POLYMER.block(), 4) }, 100);
		makeRecipe(new ComparableStack(ModBlocks.factory_advanced_conductor, 1), new AStack[] { new OreDictStack(ALLOY.ingot(), 8), new OreDictStack(ALLOY.plate(), 6), new OreDictStack(POLYMER.ingot(), 4), new ComparableStack(ModItems.wire_fine, 4, Mats.MAT_ALLOY.id), new ComparableStack(ModItems.fuse, 6), }, 50);
		makeRecipe(new ComparableStack(ModBlocks.reactor_element, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 2), new OreDictStack(OreDictManager.getReflector(), 4), new OreDictStack(PB.plate(), 2), new ComparableStack(ModItems.rod_empty, 8), }, 150);
		makeRecipe(new ComparableStack(ModBlocks.reactor_control, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 4), new OreDictStack(PB.ingot(), 6), new ComparableStack(ModItems.bolt_tungsten, 6), new ComparableStack(ModItems.motor, 1), }, 100);
		makeRecipe(new ComparableStack(ModBlocks.reactor_hatch, 1), new AStack[] { new ComparableStack(ModBlocks.brick_concrete, 1), new OreDictStack(STEEL.plate(), 6), }, 150);
		makeRecipe(new ComparableStack(ModBlocks.reactor_conductor, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 4), new OreDictStack(CU.plate(), 12), new ComparableStack(ModItems.wire_fine, 4, Mats.MAT_TUNGSTEN.id), }, 130);
		makeRecipe(new ComparableStack(ModBlocks.reactor_computer, 1), new AStack[] { new ComparableStack(ModBlocks.reactor_conductor, 2), new ComparableStack(ModItems.circuit_targeting_tier3, 4), new ComparableStack(ModItems.circuit_gold, 1), }, 250);
		makeRecipe(new ComparableStack(ModBlocks.fusion_conductor, 3), new AStack[] {new OreDictStack(ND.ingot()) , new OreDictStack(STEEL.ingot(), 4), new ComparableStack(ModItems.coil_advanced_alloy, 10), }, 150);
		makeRecipe(new ComparableStack(ModBlocks.fusion_center, 1), new AStack[] { new OreDictStack(W.ingot(), 4), new OreDictStack(STEEL.plate(), 6), new ComparableStack(ModItems.wire_fine, 24, Mats.MAT_ALLOY.id), }, 200);
		makeRecipe(new ComparableStack(ModBlocks.fusion_motor, 1), new AStack[] { new OreDictStack(TI.ingot(), 4), new OreDictStack(STEEL.ingot(), 2), new ComparableStack(ModItems.motor, 4), }, 250);
		makeRecipe(new ComparableStack(ModBlocks.fusion_heater, 1), new AStack[] { new OreDictStack(W.ingot(), 4), new OreDictStack(STEEL.plate(), 2), new OreDictStack(OreDictManager.getReflector(), 2), new OreDictStack(CU.plate(), 4), new ComparableStack(ModItems.magnetron, 1), new ComparableStack(ModItems.wire_fine, 4, Mats.MAT_ALLOY.id), }, 150);
		makeRecipe(new ComparableStack(ModBlocks.watz_cooler, 1), new AStack[] { new OreDictStack(W.ingot(), 2), new OreDictStack(STEEL.ingot(), 2), new OreDictStack(KNO.dust(), 4), }, 300);
//TODO: add new watz crafting
		makeRecipe(new ComparableStack(ModBlocks.fwatz_computer, 1), new AStack[] { new ComparableStack(ModBlocks.block_meteor, 1), new ComparableStack(ModItems.wire_fine, 16, Mats.MAT_MAGTUNG.id), new OreDictStack(SBD.dust(), 4), new ComparableStack(ModItems.circuit_schrabidium, 4), }, 300);
		makeRecipe(new ComparableStack(ModBlocks.nuke_gadget, 1), new AStack[] { new ComparableStack(ModItems.sphere_steel, 1), new ComparableStack(ModItems.fins_flat, 2), new ComparableStack(ModItems.pedestal_steel, 1), new ComparableStack(ModItems.circuit_targeting_tier3, 1), new ComparableStack(ModItems.wire_fine, 6, Mats.MAT_GOLD.id), new OreDictStack(KEY_GRAY, 6), }, 300);
		makeRecipe(new ComparableStack(ModBlocks.nuke_boy, 1), new AStack[] { new ComparableStack(ModItems.hull_small_steel, 2), new ComparableStack(ModItems.fins_small_steel, 1), new ComparableStack(ModItems.circuit_targeting_tier2, 1), new ComparableStack(ModItems.wire_fine, 6, Mats.MAT_ALUMINIUM.id), new OreDictStack(KEY_BLUE, 4), }, 300);
		makeRecipe(new ComparableStack(ModBlocks.nuke_man, 1), new AStack[] { new ComparableStack(ModItems.sphere_steel, 1), new ComparableStack(ModItems.hull_big_steel, 2), new ComparableStack(ModItems.fins_big_steel, 1), new ComparableStack(ModItems.circuit_targeting_tier2, 2), new ComparableStack(ModItems.wire_fine, 6, Mats.MAT_COPPER.id), new OreDictStack(KEY_YELLOW, 6), }, 300);
		makeRecipe(new ComparableStack(ModBlocks.nuke_mike, 1), new AStack[] { new ComparableStack(ModItems.sphere_steel, 1), new ComparableStack(ModItems.hull_big_aluminium, 4), new ComparableStack(ModItems.cap_aluminium, 1), new ComparableStack(ModItems.circuit_targeting_tier4, 3), new ComparableStack(ModItems.wire_fine, 18, Mats.MAT_GOLD.id), new OreDictStack(KEY_LIGHTGRAY, 12), }, 300);
		makeRecipe(new ComparableStack(ModBlocks.nuke_tsar, 1), new AStack[] { new ComparableStack(ModItems.sphere_steel, 1), new ComparableStack(ModItems.hull_big_titanium, 6), new ComparableStack(ModItems.hull_small_steel, 2), new ComparableStack(ModItems.fins_tri_steel, 1), new ComparableStack(ModItems.circuit_targeting_tier4, 5), new ComparableStack(ModItems.wire_fine, 24, Mats.MAT_GOLD.id), new ComparableStack(ModItems.wire_fine, 12, Mats.MAT_TUNGSTEN.id), new OreDictStack(KEY_BLACK, 6), }, 600);
		makeRecipe(new ComparableStack(ModBlocks.nuke_prototype, 1), new AStack[] { new ComparableStack(ModItems.dysfunctional_reactor, 1), new ComparableStack(ModItems.hull_small_steel, 2), new OreDictStack(EUPH.ingot(), 3), new ComparableStack(ModItems.circuit_targeting_tier5, 1), new ComparableStack(ModItems.wire_fine, 16, Mats.MAT_GOLD.id), }, 500);
		makeRecipe(new ComparableStack(ModBlocks.nuke_fleija, 1), new AStack[] { new ComparableStack(ModItems.hull_small_aluminium, 1), new ComparableStack(ModItems.fins_quad_titanium, 1), new ComparableStack(ModItems.circuit_targeting_tier4, 2), new ComparableStack(ModItems.wire_fine, 8, Mats.MAT_GOLD.id), new OreDictStack(KEY_WHITE, 4), }, 400);
		makeRecipe(new ComparableStack(ModBlocks.nuke_solinium, 1), new AStack[] { new ComparableStack(ModItems.hull_small_steel, 2), new ComparableStack(ModItems.fins_quad_titanium, 1), new ComparableStack(ModItems.circuit_targeting_tier4, 3), new ComparableStack(ModItems.wire_fine, 10, Mats.MAT_GOLD.id), new ComparableStack(ModItems.pipes_steel, 4), new OreDictStack(KEY_CYAN, 4), }, 400);
		makeRecipe(new ComparableStack(ModBlocks.nuke_n2, 1), new AStack[] { new ComparableStack(ModItems.hull_big_steel, 3), new ComparableStack(ModItems.hull_small_steel, 2), new ComparableStack(ModItems.wire_fine, 12, Mats.MAT_COPPER.id), new ComparableStack(ModItems.pipes_steel, 1), new ComparableStack(ModItems.circuit_targeting_tier3, 3), new OreDictStack(KEY_BLACK, 12), }, 300);
		makeRecipe(new ComparableStack(ModBlocks.nuke_fstbmb, 1), new AStack[] { new ComparableStack(ModItems.sphere_steel, 1), new ComparableStack(ModItems.hull_big_titanium, 6), new ComparableStack(ModItems.fins_big_steel, 1), new ComparableStack(ModItems.powder_magic, 8), new ComparableStack(ModItems.wire_fine, 12, Mats.MAT_GOLD.id), new ComparableStack(ModItems.circuit_targeting_tier4, 4), new OreDictStack(KEY_GRAY, 6), }, 600);
		makeRecipe(new ComparableStack(ModBlocks.nuke_custom, 1), new AStack[] { new ComparableStack(ModItems.hull_small_steel, 2), new ComparableStack(ModItems.fins_small_steel, 1), new ComparableStack(ModItems.circuit_gold, 1), new ComparableStack(ModItems.wire_fine, 12, Mats.MAT_GOLD.id), new OreDictStack(KEY_GRAY, 4), }, 300);
		makeRecipe(new ComparableStack(ModBlocks.float_bomb, 1), new AStack[] { new OreDictStack(TI.plate(), 12), new ComparableStack(ModItems.levitation_unit, 1), new ComparableStack(ModItems.circuit_gold, 4), new ComparableStack(ModItems.wire_fine, 6, Mats.MAT_GOLD.id), }, 250);
		makeRecipe(new ComparableStack(ModBlocks.therm_endo, 1), new AStack[] { new OreDictStack(TI.plate(), 12), new ComparableStack(ModItems.thermo_unit_endo, 1), new ComparableStack(ModItems.circuit_gold, 2), new ComparableStack(ModItems.wire_fine, 6, Mats.MAT_GOLD.id), }, 250);
		makeRecipe(new ComparableStack(ModBlocks.therm_exo, 1), new AStack[] { new OreDictStack(TI.plate(), 12), new ComparableStack(ModItems.thermo_unit_exo, 1), new ComparableStack(ModItems.circuit_gold, 2), new ComparableStack(ModItems.wire_fine, 6, Mats.MAT_GOLD.id), }, 250);
		makeRecipe(new ComparableStack(ModBlocks.launch_pad, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 4), new OreDictStack(ANY_PLASTIC.ingot(), 2), new OreDictStack(STEEL.plate(), 12), new ComparableStack(ModBlocks.machine_battery, 1), new ComparableStack(ModItems.circuit_gold, 2), }, 250);
		makeRecipe(new ComparableStack(ModItems.spawn_chopper, 1), new AStack[] { new ComparableStack(ModItems.chopper_blades, 5), new ComparableStack(ModItems.chopper_gun, 1), new ComparableStack(ModItems.chopper_head, 1), new ComparableStack(ModItems.chopper_tail, 1), new ComparableStack(ModItems.chopper_torso, 1), new ComparableStack(ModItems.chopper_wing, 2), }, 300);
		makeRecipe(new ComparableStack(ModItems.missile_generic, 1), new AStack[] { new ComparableStack(ModItems.warhead_generic_small, 1), new ComparableStack(ModItems.fuel_tank_small, 1), new ComparableStack(ModItems.thruster_small, 1), new OreDictStack(TI.plate(), 6), new ComparableStack(ModItems.circuit_targeting_tier1, 1), }, 200);
		makeRecipe(new ComparableStack(ModItems.missile_incendiary, 1), new AStack[] { new ComparableStack(ModItems.warhead_incendiary_small, 1), new ComparableStack(ModItems.fuel_tank_small, 1), new ComparableStack(ModItems.thruster_small, 1), new OreDictStack(TI.plate(), 6), new ComparableStack(ModItems.circuit_targeting_tier1, 1), }, 200);
		makeRecipe(new ComparableStack(ModItems.missile_cluster, 1), new AStack[] { new ComparableStack(ModItems.warhead_cluster_small, 1), new ComparableStack(ModItems.fuel_tank_small, 1), new ComparableStack(ModItems.thruster_small, 1), new OreDictStack(TI.plate(), 6), new ComparableStack(ModItems.circuit_targeting_tier1, 1), }, 200);
		makeRecipe(new ComparableStack(ModItems.missile_buster, 1), new AStack[] { new ComparableStack(ModItems.warhead_buster_small, 1), new ComparableStack(ModItems.fuel_tank_small, 1), new ComparableStack(ModItems.thruster_small, 1), new OreDictStack(TI.plate(), 6), new ComparableStack(ModItems.circuit_targeting_tier1, 1), }, 200);
		makeRecipe(new ComparableStack(ModItems.missile_strong, 1), new AStack[] { new ComparableStack(ModItems.warhead_generic_medium, 1), new ComparableStack(ModItems.fuel_tank_medium, 1), new ComparableStack(ModItems.thruster_medium, 1), new OreDictStack(TI.plate(), 10), new OreDictStack(STEEL.plate(), 14), new ComparableStack(ModItems.circuit_targeting_tier2, 1), }, 250);
		makeRecipe(new ComparableStack(ModItems.missile_incendiary_strong, 1), new AStack[] { new ComparableStack(ModItems.warhead_incendiary_medium, 1), new ComparableStack(ModItems.fuel_tank_medium, 1), new ComparableStack(ModItems.thruster_medium, 1), new OreDictStack(TI.plate(), 10), new OreDictStack(STEEL.plate(), 14), new ComparableStack(ModItems.circuit_targeting_tier2, 1), }, 250);
		makeRecipe(new ComparableStack(ModItems.missile_cluster_strong, 1), new AStack[] { new ComparableStack(ModItems.warhead_cluster_medium, 1), new ComparableStack(ModItems.fuel_tank_medium, 1), new ComparableStack(ModItems.thruster_medium, 1), new OreDictStack(TI.plate(), 10), new OreDictStack(STEEL.plate(), 14), new ComparableStack(ModItems.circuit_targeting_tier2, 1), }, 250);
		makeRecipe(new ComparableStack(ModItems.missile_buster_strong, 1), new AStack[] { new ComparableStack(ModItems.warhead_buster_medium, 1), new ComparableStack(ModItems.fuel_tank_medium, 1), new ComparableStack(ModItems.thruster_medium, 1), new OreDictStack(TI.plate(), 10), new OreDictStack(STEEL.plate(), 14), new ComparableStack(ModItems.circuit_targeting_tier2, 1), }, 250);
		makeRecipe(new ComparableStack(ModItems.missile_emp_strong, 1), new AStack[] {new ComparableStack(ModBlocks.emp_bomb, 3), new ComparableStack(ModItems.fuel_tank_medium, 1), new ComparableStack(ModItems.thruster_medium, 1), new OreDictStack(TI.plate(), 10), new OreDictStack(STEEL.plate(), 14), new ComparableStack(ModItems.circuit_targeting_tier2, 1), },250);
		makeRecipe(new ComparableStack(ModItems.missile_burst, 1), new AStack[] { new ComparableStack(ModItems.warhead_generic_large, 1), new ComparableStack(ModItems.fuel_tank_large, 1), new ComparableStack(ModItems.thruster_large, 1), new OreDictStack(TI.plate(), 14), new OreDictStack(STEEL.plate(), 20), new OreDictStack(AL.plate(), 12), new ComparableStack(ModItems.circuit_targeting_tier3, 1), }, 350);
		makeRecipe(new ComparableStack(ModItems.missile_inferno, 1), new AStack[] { new ComparableStack(ModItems.warhead_incendiary_large, 1), new ComparableStack(ModItems.fuel_tank_large, 1), new ComparableStack(ModItems.thruster_large, 1), new OreDictStack(TI.plate(), 14), new OreDictStack(STEEL.plate(), 20), new OreDictStack(AL.plate(), 12), new ComparableStack(ModItems.circuit_targeting_tier3, 1), }, 350);
		makeRecipe(new ComparableStack(ModItems.missile_rain, 1), new AStack[] { new ComparableStack(ModItems.warhead_cluster_large, 1), new ComparableStack(ModItems.fuel_tank_large, 1), new ComparableStack(ModItems.thruster_large, 1), new OreDictStack(TI.plate(), 14), new OreDictStack(STEEL.plate(), 20), new OreDictStack(AL.plate(), 12), new ComparableStack(ModItems.circuit_targeting_tier3, 1), }, 350);
		makeRecipe(new ComparableStack(ModItems.missile_drill, 1), new AStack[] { new ComparableStack(ModItems.warhead_buster_large, 1), new ComparableStack(ModItems.fuel_tank_large, 1), new ComparableStack(ModItems.thruster_large, 1), new OreDictStack(TI.plate(), 14), new OreDictStack(STEEL.plate(), 20), new OreDictStack(AL.plate(), 12), new ComparableStack(ModItems.circuit_targeting_tier3, 1), }, 350);
		makeRecipe(new ComparableStack(ModItems.missile_n2, 1), new AStack[] { new ComparableStack(ModItems.warhead_n2, 1), new ComparableStack(ModItems.fuel_tank_large, 1), new ComparableStack(ModItems.thruster_large, 1), new OreDictStack(TI.plate(), 20), new OreDictStack(STEEL.plate(), 24), new OreDictStack(AL.plate(), 16), new ComparableStack(ModItems.circuit_targeting_tier4, 1), }, 500);
		makeRecipe(new ComparableStack(ModItems.missile_nuclear, 1), new AStack[] { new ComparableStack(ModItems.warhead_nuclear, 1), new ComparableStack(ModItems.fuel_tank_large, 1), new ComparableStack(ModItems.thruster_large, 1), new OreDictStack(TI.plate(), 20), new OreDictStack(STEEL.plate(), 24), new OreDictStack(AL.plate(), 16), new ComparableStack(ModItems.circuit_targeting_tier4, 1), }, 500);
		makeRecipe(new ComparableStack(ModItems.missile_nuclear_cluster, 1), new AStack[] { new ComparableStack(ModItems.warhead_mirv, 1), new ComparableStack(ModItems.fuel_tank_large, 1), new ComparableStack(ModItems.thruster_large, 1), new OreDictStack(TI.plate(), 20), new OreDictStack(STEEL.plate(), 24), new OreDictStack(AL.plate(), 16), new ComparableStack(ModItems.circuit_targeting_tier5, 1), }, 600);
		makeRecipe(new ComparableStack(ModItems.missile_volcano, 1), new AStack[]{new ComparableStack(ModItems.warhead_volcano, 1), new ComparableStack(ModItems.fuel_tank_large, 1), new ComparableStack(ModItems.thruster_large, 1), new OreDictStack(TI.plate(), 20), new OreDictStack(STEEL.plate(), 24), new OreDictStack(AL.plate(), 16), new ComparableStack(ModItems.circuit_targeting_tier5, 1)}, 600);
		makeRecipe(new ComparableStack(ModItems.missile_endo, 1), new AStack[] { new ComparableStack(ModItems.warhead_thermo_endo, 1), new ComparableStack(ModItems.fuel_tank_large, 1), new ComparableStack(ModItems.thruster_large, 1), new OreDictStack(TI.plate(), 14), new OreDictStack(STEEL.plate(), 20), new OreDictStack(AL.plate(), 12), new ComparableStack(ModItems.circuit_targeting_tier4, 1), }, 350);
		makeRecipe(new ComparableStack(ModItems.missile_exo, 1), new AStack[] { new ComparableStack(ModItems.warhead_thermo_exo, 1), new ComparableStack(ModItems.fuel_tank_large, 1), new ComparableStack(ModItems.thruster_large, 1), new OreDictStack(TI.plate(), 14), new OreDictStack(STEEL.plate(), 20), new OreDictStack(AL.plate(), 12), new ComparableStack(ModItems.circuit_targeting_tier4, 1), }, 350);
		
		makeRecipe(new ComparableStack(ModItems.gun_defabricator, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 2), new OreDictStack(ANY_PLASTIC.ingot(), 8), new OreDictStack(IRON.plate(), 5), new ComparableStack(ModItems.mechanism_special, 3), new ComparableStack(Items.DIAMOND, 1), new ComparableStack(ModItems.plate_dalekanium, 3), }, 200);
		makeRecipe(new ComparableStack(ModItems.gun_osipr_ammo, 24), new AStack[] { new OreDictStack(STEEL.plate(), 2), new ComparableStack(Items.REDSTONE, 1), new ComparableStack(Items.GLOWSTONE_DUST, 1), }, 50);
		makeRecipe(new ComparableStack(ModItems.gun_osipr_ammo2, 1), new AStack[] { new OreDictStack(CMB.plate(), 4), new ComparableStack(Items.REDSTONE, 7), new ComparableStack(ModItems.powder_power, 3), }, 200);
		
		//AMMO CLIP CRAFTING
		makeRecipe(new ComparableStack(ModItems.clip_revolver_iron), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_revolver_iron_ammo, 24) }, 40);	
		makeRecipe(new ComparableStack(ModItems.clip_revolver), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_revolver_ammo, 12) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_revolver_gold), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_revolver_gold_ammo, 12) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_revolver_lead), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_revolver_lead_ammo, 12) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_revolver_schrabidium), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_revolver_schrabidium_ammo, 12) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_revolver_cursed), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_revolver_cursed_ammo, 17) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_revolver_nightmare), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_revolver_nightmare_ammo, 6) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_revolver_nightmare2), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_revolver_nightmare2_ammo, 6) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_revolver_pip), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.ammo_44_pip, 6) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_revolver_nopip), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.ammo_44, 12) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_rpg), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.ammo_rocket, 6) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_stinger), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_stinger_ammo, 6) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_fatman), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.ammo_nuke, 6) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_mirv), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.ammo_mirv, 3) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_bf), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_bf_ammo, 2) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_mp40), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.ammo_9mm, 32) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_uzi), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.ammo_22lr, 32) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_uboinik), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.ammo_12gauge, 24) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_lever_action), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.ammo_20gauge, 24) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_bolt_action), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.ammo_20gauge_slug, 24) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_osipr), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_osipr_ammo, 30) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_immolator), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_immolator_ammo, 64) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_cryolator), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_cryolator_ammo, 64) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_mp), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.ammo_566_gold, 40) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_xvl1456), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_xvl1456_ammo, 64) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_emp), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_emp_ammo, 6) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_jack), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_jack_ammo, 12) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_spark), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_spark_ammo, 4) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_hp), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_hp_ammo, 12) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_euthanasia), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_euthanasia_ammo, 16) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_defabricator), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_defabricator_ammo, 16) }, 40);

		makeRecipe(new ComparableStack(ModItems.grenade_fire, 1), new AStack[] { new ComparableStack(ModItems.grenade_frag, 1), new OreDictStack(P_RED.dust(), 1), new OreDictStack(CU.plate(), 2), }, 150);
		makeRecipe(new ComparableStack(ModItems.grenade_shrapnel, 1), new AStack[] { new ComparableStack(ModItems.grenade_frag, 1), new ComparableStack(ModItems.pellet_buckshot, 1), new OreDictStack(STEEL.plate(), 2), }, 150);
		makeRecipe(new ComparableStack(ModItems.grenade_cluster, 1), new AStack[] { new ComparableStack(ModItems.grenade_frag, 1), new ComparableStack(ModItems.pellet_cluster, 1), new OreDictStack(STEEL.plate(), 2), }, 200);
		makeRecipe(new ComparableStack(ModItems.grenade_flare, 1), new AStack[] { new ComparableStack(ModItems.grenade_generic, 1), new ComparableStack(Items.GLOWSTONE_DUST, 1), new OreDictStack(AL.plate(), 2), new OreDictStack(CS.ingot(), 1), }, 100);
		makeRecipe(new ComparableStack(ModItems.grenade_electric, 1), new AStack[] { new ComparableStack(ModItems.grenade_generic, 1), new ComparableStack(ModItems.circuit_red_copper, 1), new OreDictStack(GOLD.plate(), 2), }, 200);
		makeRecipe(new ComparableStack(ModItems.grenade_pulse, 4), new AStack[] { new OreDictStack(STEEL.plate(), 1), new OreDictStack(IRON.plate(), 3), new ComparableStack(ModItems.wire_fine, 6, Mats.MAT_MINGRADE.id), new ComparableStack(Items.DIAMOND, 1), }, 300);
		makeRecipe(new ComparableStack(ModItems.grenade_plasma, 2), new AStack[] { new OreDictStack(STEEL.plate(), 3), new OreDictStack(ALLOY.plate(), 1), new ComparableStack(ModItems.coil_advanced_torus, 1), new ComparableStack(ItemCell.getFullCell(Fluids.DEUTERIUM)), new ComparableStack(ItemCell.getFullCell(Fluids.TRITIUM)), }, 300);
		makeRecipe(new ComparableStack(ModItems.grenade_tau, 2), new AStack[] { new OreDictStack(PB.plate(), 3), new OreDictStack(ALLOY.plate(), 1), new ComparableStack(ModItems.coil_advanced_torus, 1), new ComparableStack(ModItems.gun_xvl1456_ammo, 1), }, 300);
		makeRecipe(new ComparableStack(ModItems.grenade_schrabidium, 1), new AStack[] { new ComparableStack(ModItems.grenade_flare, 1), new OreDictStack(SA326.dust(), 1), new OreDictStack(OreDictManager.getReflector(), 2), }, 300);
		makeRecipe(new ComparableStack(ModItems.grenade_nuclear, 1), new AStack[] { new OreDictStack(IRON.plate(), 1), new OreDictStack(STEEL.plate(), 1), new OreDictStack(PU239.nugget(), 2), new ComparableStack(ModItems.wire_fine, 2, Mats.MAT_MINGRADE.id), }, 200);
		makeRecipe(new ComparableStack(ModItems.grenade_zomg, 1), new AStack[] { new ComparableStack(ModItems.plate_paa, 3), new OreDictStack(OreDictManager.getReflector(), 1), new ComparableStack(ModItems.coil_magnetized_tungsten, 3), new ComparableStack(ModItems.powder_power, 3), new ComparableStack(ModItems.nugget_radspice, 1), }, 300);
		makeRecipe(new ComparableStack(ModItems.grenade_solinium, 1), new AStack[] { new OreDictStack(CMB.plate(), 6), new OreDictStack(UNOBTAINIUM.ingot(), 3),  new ComparableStack(ModItems.coil_gold, 12), new ComparableStack(ModItems.solinium_propellant, 1), new ComparableStack(ModItems.solinium_igniter, 1), new ComparableStack(ModItems.solinium_core, 1), }, 400);
		makeRecipe(new ComparableStack(ModItems.grenade_black_hole, 1), new AStack[] { new OreDictStack(ANY_PLASTIC.ingot(), 6), new OreDictStack(OreDictManager.getReflector(), 3), new ComparableStack(ModItems.coil_magnetized_tungsten, 2), new ComparableStack(ModItems.black_hole, 1), }, 500);
		makeRecipe(new ComparableStack(ModItems.multitool_dig, 1), new AStack[] { new ComparableStack(ModItems.rod_reiium, 1), new ComparableStack(ModItems.rod_weidanium, 1), new ComparableStack(ModItems.rod_australium, 1), new ComparableStack(ModItems.rod_verticium, 1), new ComparableStack(ModItems.rod_unobtainium, 1), new ComparableStack(ModItems.rod_daffergon, 1), new OreDictStack(ANY_PLASTIC.ingot(), 4), new ComparableStack(ModItems.circuit_gold, 1), new ComparableStack(ModItems.ducttape, 1), }, 600);
		

		makeRecipe(new ComparableStack(ModItems.gadget_explosive, 4), new AStack[] {new OreDictStack(AL.plate(), 4), new ComparableStack(ModItems.wire_fine, 8, Mats.MAT_GOLD.id), new ComparableStack(ModBlocks.det_cord, 4), new OreDictStack(CU.plate(), 1), new OreDictStack(ANY_HIGHEXPLOSIVE.ingot(), 10), new OreDictStack(ANY_PLASTIC.ingot(), 2)}, 200); //8 HE lenses (polymer inserts since no baratol) w/ bridge-wire detonators, aluminum pushers, & duraluminum shell
		makeRecipe(new ComparableStack(ModItems.gadget_wireing, 1), new AStack[] { new OreDictStack(IRON.plate(), 1), new ComparableStack(ModItems.wire_fine, 12, Mats.MAT_GOLD.id), }, 100);
		makeRecipe(new ComparableStack(ModItems.gadget_core, 1), new AStack[] { new OreDictStack(PU239.nugget(), 7), new OreDictStack(U238.nugget(), 3), }, 200);
		makeRecipe(new ComparableStack(ModItems.boy_shielding, 1), new AStack[] { new OreDictStack(OreDictManager.getReflector(), 12), new OreDictStack(STEEL.plate(), 4), }, 150);
		makeRecipe(new ComparableStack(ModItems.boy_target, 1), new AStack[] { new OreDictStack(U235.nugget(), 7), }, 200);
		makeRecipe(new ComparableStack(ModItems.boy_bullet, 1), new AStack[] { new OreDictStack(U235.nugget(), 3), }, 100);
		makeRecipe(new ComparableStack(ModItems.boy_propellant, 1), new AStack[] { new ComparableStack(Blocks.TNT, 3), new OreDictStack(IRON.plate(), 8), new OreDictStack(AL.plate(), 4), new ComparableStack(ModItems.wire_fine, 4, Mats.MAT_MINGRADE.id), }, 100);
		makeRecipe(new ComparableStack(ModItems.boy_igniter, 1), new AStack[] {new OreDictStack(ANY_HIGHEXPLOSIVE.ingot(), 1), new OreDictStack(AL.plate(), 6), new OreDictStack(STEEL.plate(), 1), new ComparableStack(ModItems.circuit_red_copper, 1), new ComparableStack(ModItems.wire_fine, 3, Mats.MAT_MINGRADE.id), }, 150); //HE for gating purposes

		makeRecipe(new ComparableStack(ModItems.man_explosive, 4), new AStack[] {new OreDictStack(AL.plate(), 4), new ComparableStack(ModItems.wire_fine, 8, Mats.MAT_MINGRADE.id), new OreDictStack(ANY_PLASTICEXPLOSIVE.ingot(), 2), new OreDictStack(CU.plate(), 1), new OreDictStack(ANY_HIGHEXPLOSIVE.ingot(), 8), new OreDictStack(RUBBER.ingot(), 1)}, 250); //8 HE (To use 16 PBX ingots; rubber inserts) lenses w/ improved bridge-wire detonators, thin aluminum pushers, & duraluminum shell
		makeRecipe(new ComparableStack(ModItems.man_igniter, 1), new AStack[] { new OreDictStack(STEEL.plate(), 6), new ComparableStack(ModItems.circuit_red_copper, 1), new ComparableStack(ModItems.wire_fine, 9, Mats.MAT_MINGRADE.id), }, 200);
		makeRecipe(new ComparableStack(ModItems.man_core, 1), new AStack[] { new OreDictStack(PU239.nugget(), 8), new OreDictStack(BE.nugget(), 2), }, 150);
		makeRecipe(new ComparableStack(ModItems.mike_core, 1), new AStack[] { new OreDictStack(U238.nugget(), 24), new OreDictStack(PB.ingot(), 6), }, 250);
		makeRecipe(new ComparableStack(ModItems.mike_deut, 1), new AStack[] { new OreDictStack(IRON.plate(), 12), new OreDictStack(STEEL.plate(), 16), new ComparableStack(ItemCell.getFullCell(Fluids.DEUTERIUM, 10)), }, 250);
		makeRecipe(new ComparableStack(ModItems.mike_cooling_unit, 1), new AStack[] { new OreDictStack(IRON.plate(), 8), new ComparableStack(ModItems.coil_copper, 5), new ComparableStack(ModItems.coil_tungsten, 5), new ComparableStack(ModItems.motor, 2), }, 200);
		makeRecipe(new ComparableStack(ModItems.fleija_igniter, 1), new AStack[] { new OreDictStack(TI.plate(), 6), new ComparableStack(ModItems.wire_fine, 2, Mats.MAT_SCHRABIDIUM.id), new ComparableStack(ModItems.circuit_schrabidium, 1), }, 300);
		makeRecipe(new ComparableStack(ModItems.fleija_core, 1), new AStack[] { new OreDictStack(U235.nugget(), 8), new OreDictStack(NP237.nugget(), 2), new OreDictStack(BE.nugget(), 4), new ComparableStack(ModItems.coil_copper, 2), }, 500);
		makeRecipe(new ComparableStack(ModItems.fleija_propellant, 1), new AStack[] { new OreDictStack(ANY_HIGHEXPLOSIVE.ingot(), 3), new OreDictStack(SA326.plate(), 8), new ComparableStack(ModItems.ingot_tennessine, 2), }, 400);
		makeRecipe(new ComparableStack(ModItems.solinium_igniter, 1), new AStack[] { new OreDictStack(TI.plate(), 4), new ComparableStack(ModItems.wire_fine, 2, Mats.MAT_ALLOY.id), new ComparableStack(ModItems.circuit_schrabidium, 1), new ComparableStack(ModItems.coil_gold, 1), }, 400);
		makeRecipe(new ComparableStack(ModItems.solinium_core, 1), new AStack[] { new OreDictStack(SA327.ingot(), 3), new OreDictStack(EUPH.nugget(), 4), new ComparableStack(ModItems.ingot_tennessine, 5), }, 400);
		makeRecipe(new ComparableStack(ModItems.solinium_propellant, 1), new AStack[] { new OreDictStack(ANY_HIGHEXPLOSIVE.ingot(), 3), new OreDictStack(OreDictManager.getReflector(), 2), new ComparableStack(ModItems.plate_polymer, 6), new ComparableStack(ModItems.wire_fine, 6, Mats.MAT_TUNGSTEN.id), new ComparableStack(ModItems.biomass_compressed, 4), }, 350);
		makeRecipe(new ComparableStack(ModItems.schrabidium_hammer, 1), new AStack[] {new OreDictStack(SA326.block(), 35), new ComparableStack(ModItems.billet_yharonite, 64), new ComparableStack(ModItems.billet_yharonite, 64), new OreDictStack("netherStar", 3), new ComparableStack(ModItems.fragment_meteorite, 64), new ComparableStack(ModItems.fragment_meteorite, 64), new ComparableStack(ModItems.fragment_meteorite, 64), new ComparableStack(ModItems.fragment_meteorite, 64), new ComparableStack(ModItems.fragment_meteorite, 64), new ComparableStack(ModItems.fragment_meteorite, 64), new ComparableStack(ModItems.fragment_meteorite, 64), new ComparableStack(ModItems.fragment_meteorite, 64) },1000);
		makeRecipe(new ComparableStack(ModItems.component_limiter, 1), new AStack[] { new ComparableStack(ModItems.hull_big_steel, 2), new OreDictStack(STEEL.plate(), 32), new OreDictStack(TI.plate(), 18), new ComparableStack(ModItems.plate_desh, 12), new ComparableStack(ModItems.pipes_steel, 4), new ComparableStack(ModItems.circuit_gold, 8), new ComparableStack(ModItems.circuit_schrabidium, 4), new OreDictStack(STAR.ingot(), 14), new ComparableStack(ModItems.plate_dalekanium, 5), new ComparableStack(ModItems.powder_magic, 16), new ComparableStack(ModBlocks.fwatz_computer, 3), }, 2500);
		makeRecipe(new ComparableStack(ModItems.component_emitter, 1), new AStack[] { new ComparableStack(ModItems.hull_big_steel, 3), new ComparableStack(ModItems.hull_big_titanium, 2), new OreDictStack(STEEL.plate(), 32), new OreDictStack(PB.plate(), 24), new ComparableStack(ModItems.plate_desh, 24), new ComparableStack(ModItems.pipes_steel, 8), new ComparableStack(ModItems.circuit_gold, 12), new ComparableStack(ModItems.circuit_schrabidium, 8), new OreDictStack(STAR.ingot(), 26), new ComparableStack(ModItems.powder_magic, 48), new ComparableStack(ModBlocks.fwatz_computer, 2), new ComparableStack(ModItems.crystal_xen, 1), }, 2500);
		makeRecipe(new ComparableStack(ModBlocks.machine_radar, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 8), new OreDictStack(STEEL.plate(), 16), new OreDictStack(ANY_PLASTIC.ingot(), 4), new OreDictStack(ANY_RUBBER.ingot(), 24), new ComparableStack(ModItems.magnetron, 10), new ComparableStack(ModItems.motor, 3), new ComparableStack(ModItems.circuit_gold, 4), new ComparableStack(ModItems.coil_copper, 12), }, 300);
		makeRecipe(new ComparableStack(ModBlocks.machine_forcefield, 1), new AStack[] { new OreDictStack(ALLOY.plate(), 8), new ComparableStack(ModItems.plate_desh, 4), new ComparableStack(ModItems.coil_gold_torus, 6), new ComparableStack(ModItems.coil_magnetized_tungsten, 12), new ComparableStack(ModItems.motor, 1), new ComparableStack(ModItems.upgrade_radius, 1), new ComparableStack(ModItems.upgrade_health, 1), new ComparableStack(ModItems.circuit_targeting_tier5, 1), new ComparableStack(ModBlocks.machine_transformer, 1), }, 1000);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_10_kerosene, 1), new AStack[] { new ComparableStack(ModItems.seg_10, 1), new ComparableStack(ModItems.pipes_steel, 1), new OreDictStack(W.ingot(), 4), new OreDictStack(STEEL.plate(), 4), }, 100);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_10_solid, 1), new AStack[] { new ComparableStack(ModItems.seg_10, 1), new ComparableStack(ModItems.coil_tungsten, 1), new OreDictStack(DURA.ingot(), 4), new OreDictStack(STEEL.plate(), 4), }, 100);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_10_xenon, 1), new AStack[] { new ComparableStack(ModItems.seg_10, 1), new OreDictStack(STEEL.plate(), 4), new ComparableStack(ModItems.pipes_steel, 2), new ComparableStack(ModItems.arc_electrode, 4), }, 100);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_15_kerosene, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new ComparableStack(ModItems.pipes_steel, 4), new OreDictStack(W.ingot(), 8), new OreDictStack(STEEL.plate(), 6), new OreDictStack(DESH.ingot(), 4), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_15_kerosene_dual, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new ComparableStack(ModItems.pipes_steel, 2), new OreDictStack(W.ingot(), 4), new OreDictStack(STEEL.plate(), 6), new OreDictStack(DESH.ingot(), 1), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_15_kerosene_triple, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new ComparableStack(ModItems.pipes_steel, 3), new OreDictStack(W.ingot(), 6), new OreDictStack(STEEL.plate(), 6), new OreDictStack(DESH.ingot(), 2), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_15_solid, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new OreDictStack(STEEL.plate(), 6), new OreDictStack(DURA.ingot(), 6), new ComparableStack(ModItems.coil_tungsten, 3), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_15_solid_hexdecuple, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new OreDictStack(STEEL.plate(), 6), new OreDictStack(DURA.ingot(), 12), new ComparableStack(ModItems.coil_tungsten, 6), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_15_hydrogen, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new ComparableStack(ModItems.pipes_steel, 4), new OreDictStack(W.ingot(), 8), new OreDictStack(STEEL.plate(), 6), new ComparableStack(ModItems.tank_steel, 1), new OreDictStack(DESH.ingot(), 4), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_15_hydrogen_dual, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new ComparableStack(ModItems.pipes_steel, 2), new OreDictStack(W.ingot(), 4), new OreDictStack(STEEL.plate(), 6), new ComparableStack(ModItems.tank_steel, 1), new OreDictStack(DESH.ingot(), 1), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_15_balefire_short, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new ComparableStack(ModItems.plate_polymer, 8), new ComparableStack(ModBlocks.reactor_element, 1), new OreDictStack(DESH.ingot(), 8), new OreDictStack(BIGMT.plate(), 12), new ComparableStack(ModItems.board_copper, 2), new ComparableStack(ModItems.ingot_uranium_fuel, 4), new ComparableStack(ModItems.pipes_steel, 2), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_15_balefire, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new ComparableStack(ModItems.plate_polymer, 16), new ComparableStack(ModBlocks.reactor_element, 2), new OreDictStack(DESH.ingot(), 16), new OreDictStack(BIGMT.plate(), 24), new ComparableStack(ModItems.board_copper, 4), new ComparableStack(ModItems.ingot_uranium_fuel, 8), new ComparableStack(ModItems.pipes_steel, 2), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_15_balefire_large, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new ComparableStack(ModItems.plate_polymer, 16), new ComparableStack(ModBlocks.reactor_element, 2), new OreDictStack(DESH.ingot(), 24), new OreDictStack(BIGMT.plate(), 32), new ComparableStack(ModItems.board_copper, 4), new ComparableStack(ModItems.ingot_uranium_fuel, 8), new ComparableStack(ModItems.pipes_steel, 2), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_20_kerosene, 1), new AStack[] { new ComparableStack(ModItems.seg_20, 1), new ComparableStack(ModItems.pipes_steel, 8), new OreDictStack(W.ingot(), 16), new OreDictStack(STEEL.plate(), 12), new OreDictStack(DESH.ingot(), 8), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_20_kerosene_dual, 1), new AStack[] { new ComparableStack(ModItems.seg_20, 1), new ComparableStack(ModItems.pipes_steel, 4), new OreDictStack(W.ingot(), 8), new OreDictStack(STEEL.plate(), 6), new OreDictStack(DESH.ingot(), 4), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_20_kerosene_triple, 1), new AStack[] { new ComparableStack(ModItems.seg_20, 1), new ComparableStack(ModItems.pipes_steel, 6), new OreDictStack(W.ingot(), 12), new OreDictStack(STEEL.plate(), 8), new OreDictStack(DESH.ingot(), 6), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_20_solid, 1), new AStack[] { new ComparableStack(ModItems.seg_20, 1), new ComparableStack(ModItems.coil_tungsten, 8), new OreDictStack(DURA.ingot(), 16), new OreDictStack(STEEL.plate(), 12), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_20_solid_multi, 1), new AStack[] { new ComparableStack(ModItems.seg_20, 1), new ComparableStack(ModItems.coil_tungsten, 12), new OreDictStack(DURA.ingot(), 18), new OreDictStack(STEEL.plate(), 12), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_20_solid_multier, 1), new AStack[] { new ComparableStack(ModItems.seg_20, 1), new ComparableStack(ModItems.coil_tungsten, 16), new OreDictStack(DURA.ingot(), 20), new OreDictStack(STEEL.plate(), 12), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_fuselage_10_kerosene, 1), new AStack[] { new ComparableStack(ModItems.seg_10, 2), new ComparableStack(ModBlocks.steel_scaffold, 3), new OreDictStack(TI.plate(), 12), new OreDictStack(STEEL.plate(), 3), }, 100);
		makeRecipe(new ComparableStack(ModItems.mp_fuselage_10_solid, 1), new AStack[] { new ComparableStack(ModItems.seg_10, 2), new ComparableStack(ModBlocks.steel_scaffold, 3), new OreDictStack(TI.plate(), 12), new OreDictStack(AL.plate(), 3), }, 100);
		makeRecipe(new ComparableStack(ModItems.mp_fuselage_10_xenon, 1), new AStack[] { new ComparableStack(ModItems.seg_10, 2), new ComparableStack(ModBlocks.steel_scaffold, 3), new OreDictStack(TI.plate(), 12), new ComparableStack(ModItems.board_copper, 3), }, 100);
		makeRecipe(new ComparableStack(ModItems.mp_fuselage_10_long_kerosene, 1), new AStack[] { new ComparableStack(ModItems.seg_10, 2), new ComparableStack(ModBlocks.steel_scaffold, 6), new OreDictStack(TI.plate(), 24), new OreDictStack(STEEL.plate(), 6), }, 200);
		makeRecipe(new ComparableStack(ModItems.mp_fuselage_10_long_solid, 1), new AStack[] { new ComparableStack(ModItems.seg_10, 2), new ComparableStack(ModBlocks.steel_scaffold, 6), new OreDictStack(TI.plate(), 24), new OreDictStack(AL.plate(), 6), }, 200);
		makeRecipe(new ComparableStack(ModItems.mp_fuselage_10_15_kerosene, 1), new AStack[] { new ComparableStack(ModItems.seg_10, 1), new ComparableStack(ModItems.seg_15, 1), new ComparableStack(ModBlocks.steel_scaffold, 9), new OreDictStack(TI.plate(), 36), new OreDictStack(STEEL.plate(), 9), }, 300);
		makeRecipe(new ComparableStack(ModItems.mp_fuselage_10_15_solid, 1), new AStack[] { new ComparableStack(ModItems.seg_10, 1), new ComparableStack(ModItems.seg_15, 1), new ComparableStack(ModBlocks.steel_scaffold, 9), new OreDictStack(TI.plate(), 36), new OreDictStack(AL.plate(), 9), }, 300);
		makeRecipe(new ComparableStack(ModItems.mp_fuselage_10_15_hydrogen, 1), new AStack[] { new ComparableStack(ModItems.seg_10, 1), new ComparableStack(ModItems.seg_15, 1), new ComparableStack(ModBlocks.steel_scaffold, 9), new OreDictStack(TI.plate(), 36), new OreDictStack(IRON.plate(), 9), }, 300);
		makeRecipe(new ComparableStack(ModItems.mp_fuselage_10_15_balefire, 1), new AStack[] { new ComparableStack(ModItems.seg_10, 1), new ComparableStack(ModItems.seg_15, 1), new ComparableStack(ModBlocks.steel_scaffold, 9), new OreDictStack(TI.plate(), 36), new OreDictStack(BIGMT.plate(), 9), }, 300);
		makeRecipe(new ComparableStack(ModItems.mp_fuselage_15_kerosene, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 2), new ComparableStack(ModBlocks.steel_scaffold, 12), new OreDictStack(TI.plate(), 48), new OreDictStack(STEEL.plate(), 12), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_fuselage_15_solid, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 2), new ComparableStack(ModBlocks.steel_scaffold, 12), new OreDictStack(TI.plate(), 48), new OreDictStack(AL.plate(), 12), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_fuselage_15_hydrogen, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 2), new ComparableStack(ModBlocks.steel_scaffold, 12), new OreDictStack(TI.plate(), 48), new OreDictStack(IRON.plate(), 12), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_fuselage_15_balefire, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 2), new ComparableStack(ModBlocks.steel_scaffold, 12), new OreDictStack(TI.plate(), 48), new OreDictStack(BIGMT.plate(), 12), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_fuselage_15_20_kerosene, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new ComparableStack(ModItems.seg_20, 1), new ComparableStack(ModBlocks.steel_scaffold, 16), new OreDictStack(TI.plate(), 64), new OreDictStack(STEEL.plate(), 16), }, 600);
		makeRecipe(new ComparableStack(ModItems.mp_fuselage_15_20_solid, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new ComparableStack(ModItems.seg_20, 1), new ComparableStack(ModBlocks.steel_scaffold, 16), new OreDictStack(TI.plate(), 64), new OreDictStack(AL.plate(), 16), }, 600);
		makeRecipe(new ComparableStack(ModItems.mp_warhead_10_he, 1), new AStack[] {new ComparableStack(ModItems.seg_10, 1), new OreDictStack(STEEL.plate(), 6), new OreDictStack(ANY_HIGHEXPLOSIVE.ingot(), 3), new ComparableStack(ModItems.circuit_targeting_tier2, 1), },100);
		makeRecipe(new ComparableStack(ModItems.mp_warhead_10_incendiary, 1), new AStack[] {new ComparableStack(ModItems.seg_10, 1), new OreDictStack(TI.plate(), 4), new OreDictStack(P_RED.dust(), 3), new OreDictStack(ANY_HIGHEXPLOSIVE.ingot(), 2), new ComparableStack(ModItems.circuit_targeting_tier2, 1), },100);
		makeRecipe(new ComparableStack(ModItems.mp_warhead_10_buster, 1), new AStack[] { new ComparableStack(ModItems.seg_10, 1), new OreDictStack(TI.plate(), 4), new ComparableStack(ModBlocks.det_charge, 1), new ComparableStack(ModBlocks.det_cord, 4), new ComparableStack(ModItems.board_copper, 4), new ComparableStack(ModItems.circuit_targeting_tier3, 1), }, 100);
		makeRecipe(new ComparableStack(ModItems.mp_warhead_10_nuclear, 1), new AStack[] {new ComparableStack(ModItems.seg_10, 1), new OreDictStack(STEEL.plate(), 6), new OreDictStack(PU239.ingot(), 1), new OreDictStack(OreDictManager.getReflector(), 2), new OreDictStack(ANY_HIGHEXPLOSIVE.ingot(), 4), new ComparableStack(ModItems.circuit_targeting_tier3, 1), },200);
		makeRecipe(new ComparableStack(ModItems.mp_warhead_10_nuclear_large, 1), new AStack[] { new ComparableStack(ModItems.seg_10, 1), new OreDictStack(STEEL.plate(), 8), new OreDictStack(AL.plate(), 4), new OreDictStack(PU239.ingot(), 2), new ComparableStack(ModBlocks.det_charge, 2), new ComparableStack(ModItems.circuit_targeting_tier4, 1), }, 300);
		makeRecipe(new ComparableStack(ModItems.mp_warhead_10_taint, 1), new AStack[] { new ComparableStack(ModItems.seg_10, 1), new OreDictStack(STEEL.plate(), 12), new ComparableStack(ModBlocks.det_cord, 2), new ComparableStack(ModItems.powder_magic, 12), new NbtComparableStack(FluidUtil.getFilledBucket(new FluidStack(ModForgeFluids.mud_fluid, 1000))), }, 100);
		makeRecipe(new ComparableStack(ModItems.mp_warhead_10_cloud, 1), new AStack[] { new ComparableStack(ModItems.seg_10, 1), new OreDictStack(STEEL.plate(), 12), new ComparableStack(ModBlocks.det_cord, 2), new ComparableStack(ModItems.grenade_pink_cloud, 2), }, 100);
		makeRecipe(new ComparableStack(ModItems.mp_warhead_15_he, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new OreDictStack(STEEL.plate(), 16), new ComparableStack(ModBlocks.det_charge, 4), new ComparableStack(ModItems.circuit_targeting_tier3, 1), }, 200);
		makeRecipe(new ComparableStack(ModItems.mp_warhead_15_incendiary, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new OreDictStack(STEEL.plate(), 16), new ComparableStack(ModBlocks.det_charge, 2), new OreDictStack(P_RED.dust(), 8), new ComparableStack(ModItems.circuit_targeting_tier3, 1), }, 200);
		makeRecipe(new ComparableStack(ModItems.mp_warhead_15_nuclear, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new OreDictStack(STEEL.plate(), 24), new OreDictStack(TI.plate(), 12), new OreDictStack(PU239.ingot(), 3), new ComparableStack(ModBlocks.det_nuke, 1), new ComparableStack(ModItems.circuit_targeting_tier4, 1), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_warhead_15_thermo, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new OreDictStack(STEEL.plate(), 32), new OreDictStack(TI.plate(), 16), new ComparableStack(ModBlocks.det_nuke, 2), new ComparableStack(ModItems.mike_deut, 1), new ComparableStack(ModItems.mike_core, 1), new ComparableStack(ModItems.circuit_targeting_tier5, 1), }, 600);
		makeRecipe(new ComparableStack(ModItems.mp_warhead_15_volcano, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new OreDictStack(STEEL.plate(), 32), new OreDictStack(TI.plate(), 16), new ComparableStack(ModBlocks.det_nuke, 4), new OreDictStack(U238.block(), 32), new ComparableStack(ModItems.circuit_tantalium, 6), new ComparableStack(ModItems.circuit_targeting_tier4, 1) }, 600);
		makeRecipe(new ComparableStack(ModItems.mp_warhead_15_mirv, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new OreDictStack(STEEL.plate(), 24), new OreDictStack(TI.plate(), 16), new ComparableStack(ModItems.warhead_mirvlet, 7), new OreDictStack(ANY_PLASTICEXPLOSIVE.ingot(), 2), new ComparableStack(ModItems.hull_small_steel, 1), new ComparableStack(ModItems.circuit_targeting_tier5, 1), }, 600);
		makeRecipe(new ComparableStack(ModItems.mp_warhead_15_n2, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new OreDictStack(STEEL.plate(), 8), new OreDictStack(TI.plate(), 20), new ComparableStack(ModBlocks.det_charge, 24), new ComparableStack(Blocks.REDSTONE_BLOCK, 12), new OreDictStack(MAGTUNG.dust(), 6), new ComparableStack(ModItems.circuit_targeting_tier5, 1), }, 400);
		makeRecipe(new ComparableStack(ModItems.missile_soyuz0, 1), new AStack[] { new ComparableStack(ModItems.rocket_fuel, 40), new ComparableStack(ModBlocks.det_cord, 20), new ComparableStack(ModItems.thruster_medium, 12), new ComparableStack(ModItems.thruster_small, 12), new ComparableStack(ModItems.tank_steel, 10), new ComparableStack(ModItems.circuit_targeting_tier4, 4), new ComparableStack(ModItems.circuit_targeting_tier3, 8), new OreDictStack(RUBBER.ingot(), 64), new ComparableStack(ModItems.fins_small_steel, 4), new ComparableStack(ModItems.hull_big_titanium, 40), new ComparableStack(ModItems.hull_big_steel, 24), new OreDictStack(FIBER.ingot(), 64), }, 600);
		makeRecipe(new ComparableStack(ModItems.missile_soyuz_lander, 1), new AStack[] { new ComparableStack(ModItems.rocket_fuel, 10), new ComparableStack(ModItems.thruster_small, 3), new ComparableStack(ModItems.tank_steel, 2), new ComparableStack(ModItems.circuit_targeting_tier3, 4), new OreDictStack(ANY_RUBBER.ingot(), 32), new ComparableStack(ModItems.hull_big_aluminium, 2), new ComparableStack(ModItems.sphere_steel, 1), new OreDictStack(FIBER.ingot(), 12), }, 600);
		makeRecipe(new ComparableStack(ModItems.fusion_shield_tungsten, 1), new AStack[] { new OreDictStack(W.block(), 32), new OreDictStack(OreDictManager.getReflector(), 64), new OreDictStack(OreDictManager.getReflector(), 32) }, 600);
		makeRecipe(new ComparableStack(ModItems.fusion_shield_desh, 1), new AStack[] { new OreDictStack(DESH.block(), 16), new OreDictStack(CO.block(), 16), new OreDictStack(BIGMT.plate(), 64), new OreDictStack(BIGMT.plate(), 32) }, 600);
		makeRecipe(new ComparableStack(ModItems.fusion_shield_chlorophyte, 1), new AStack[] { new OreDictStack(W.block(), 16), new ComparableStack(ModBlocks.block_dura_steel, 16), new OreDictStack(OreDictManager.getReflector(), 48), new ComparableStack(ModItems.powder_chlorophyte, 48) }, 600);
		makeRecipe(new ComparableStack(ModItems.fusion_shield_vaporwave, 1), new AStack[] { new OreDictStack(DNT.block(), 16), new ComparableStack(ModBlocks.block_euphemium_cluster, 16), new OreDictStack(SBD.dust(), 64), new ComparableStack(ModItems.powder_spark_mix, 48), new ComparableStack(ModItems.powder_daffergon, 48) }, 4800);
		makeRecipe(new ComparableStack(ModBlocks.machine_fensu, 1), new AStack[] { new ComparableStack(ModItems.ingot_electronium, 32), new ComparableStack(ModBlocks.machine_dineutronium_battery, 16), new ComparableStack(ModBlocks.block_dura_steel, 16), new OreDictStack(STAR.block(), 64), new ComparableStack(ModBlocks.machine_transformer_dnt, 8), new ComparableStack(ModItems.coil_magnetized_tungsten, 24), new ComparableStack(ModItems.powder_magic, 64), new ComparableStack(ModItems.plate_dineutronium, 24), new ComparableStack(ModItems.ingot_u238m2), new OreDictStack(FIBER.ingot(), 64), new ComparableStack(ModItems.ingot_radspice, 64) }, 1200);
		makeRecipe(new ComparableStack(ModBlocks.struct_iter_core, 1), new AStack[] {
				new OreDictStack(STEEL.ingot(), 6),
				new OreDictStack(W.ingot(), 6),
				new OreDictStack(OreDictManager.getReflector(), 12),
				new ComparableStack(ModItems.coil_advanced_alloy, 12),
				new OreDictStack(ANY_PLASTIC.ingot(), 8),
				new ComparableStack(ModItems.circuit_red_copper, 8),
				new OreDictStack(KEY_CIRCUIT_BISMUTH, 1)
			}, 600);
		makeRecipe(new ComparableStack(ModBlocks.machine_large_turbine, 1), new AStack[] { new ComparableStack(ModItems.hull_big_steel, 1), new OreDictStack(STEEL.plate(), 12), new ComparableStack(ModItems.turbine_titanium, 3), new ComparableStack(ModItems.rotor_steel, 2), new ComparableStack(ModItems.generator_steel, 2), new ComparableStack(ModItems.bolt_compound, 3), new ComparableStack(ModItems.pipes_steel, 1), new ComparableStack(ModItems.circuit_aluminium, 1), }, 300);

		makeRecipe(new ComparableStack(ModItems.pellet_chlorophyte, 2), new AStack[] { new ComparableStack(ModItems.powder_chlorophyte, 1), new OreDictStack(PB.nugget(), 12), }, 50);
		makeRecipe(new ComparableStack(ModItems.pellet_mercury, 2), new AStack[] { new ComparableStack(ModItems.nugget_mercury, 1), new OreDictStack(PB.nugget(), 12), }, 50);
		makeRecipe(new ComparableStack(ModItems.pellet_meteorite, 2), new AStack[] { new ComparableStack(ModItems.powder_meteorite, 1), new OreDictStack(PB.nugget(), 12), }, 50);
		makeRecipe(new ComparableStack(ModItems.pellet_canister, 2), new AStack[] { new OreDictStack(IRON.ingot(), 3), }, 50);
		makeRecipe(new ComparableStack(ModItems.mp_warhead_15_balefire, 1), new AStack[] {new ComparableStack(ModItems.seg_15, 1), new OreDictStack(OreDictManager.getReflector(), 16), new ComparableStack(ModItems.powder_magic, 6), new ComparableStack(ModItems.egg_balefire_shard, 4), new OreDictStack(ANY_HIGHEXPLOSIVE.ingot(), 8), new ComparableStack(ModItems.circuit_targeting_tier4, 1), }, 60);
		
		makeRecipe(new ComparableStack(ModBlocks.machine_cyclotron, 1), new AStack[] { 
				new ComparableStack(ModBlocks.machine_desh_battery, 3), 
				new ComparableStack(ModBlocks.hadron_coil_neodymium, 8), 
				new ComparableStack(ModItems.wire_fine, 96, Mats.MAT_ALLOY.id),
				new OreDictStack(STEEL.ingot(), 16),
				new OreDictStack(STEEL.plate(), 32),
				new OreDictStack(AL.plate(), 32),
				new OreDictStack(ANY_PLASTIC.ingot(), 24),
				new OreDictStack(RUBBER.ingot(), 24),
				new ComparableStack(ModItems.board_copper, 8),
				new ComparableStack(ModItems.circuit_red_copper, 8),
				new ComparableStack(ModItems.circuit_gold, 3), }, 600);
		makeRecipe(new ComparableStack(ModItems.gun_zomg, 1), new AStack[] { new ComparableStack(ModItems.crystal_xen, 2), new ComparableStack(ModItems.singularity_counter_resonant, 1), new ComparableStack(ModItems.mechanism_special, 3), new ComparableStack(ModItems.plate_paa, 12), new OreDictStack(OreDictManager.getReflector(), 8), new ComparableStack(ModItems.coil_magnetized_tungsten, 5), new ComparableStack(ModItems.powder_magic, 4), new OreDictStack(ASBESTOS.ingot(), 8) }, 200);
		
		makeRecipe(new ComparableStack(ModBlocks.machine_industrial_generator, 1), new AStack[] {
				new ComparableStack(ModBlocks.machine_boiler_off, 2),
				new ComparableStack(ModBlocks.machine_large_turbine, 1),
				new ComparableStack(ModBlocks.machine_transformer, 1),
				new ComparableStack(ModBlocks.steel_scaffold, 20),
				new OreDictStack(STEEL.ingot(), 12),
				new OreDictStack(PB.plate(), 8),
				new OreDictStack(AL.plate(), 12),
				new ComparableStack(ModItems.pipes_steel, 1)
			}, 200);
		makeRecipe(new ComparableStack(ModItems.euphemium_capacitor, 1), new AStack[] {
				new OreDictStack(NB.ingot(), 4),
				new ComparableStack(ModItems.redcoil_capacitor, 1),
				new ComparableStack(ModItems.ingot_euphemium, 4),
				new ComparableStack(ModItems.circuit_tantalium, 6),
				new ComparableStack(ModItems.powder_nitan_mix, 18),
			}, 600);

		makeRecipe(new ComparableStack(ModBlocks.block_cap, 1, BlockEnums.EnumBlockCapType.NUKA.ordinal()), new AStack[] { new ComparableStack(ModItems.cap_nuka, 64), new ComparableStack(ModItems.cap_nuka, 64) }, 10);
		makeRecipe(new ComparableStack(ModBlocks.block_cap, 1, BlockEnums.EnumBlockCapType.SPARKLE.ordinal()), new AStack[] { new ComparableStack(ModItems.cap_sparkle, 64), new ComparableStack(ModItems.cap_sparkle, 64) }, 10);
		makeRecipe(new ComparableStack(ModBlocks.block_cap, 1, BlockEnums.EnumBlockCapType.QUANTUM.ordinal()), new AStack[] { new ComparableStack(ModItems.cap_quantum, 64), new ComparableStack(ModItems.cap_quantum, 64) }, 10);
		makeRecipe(new ComparableStack(ModBlocks.block_cap, 1, BlockEnums.EnumBlockCapType.RAD.ordinal()), new AStack[] { new ComparableStack(ModItems.cap_rad, 64), new ComparableStack(ModItems.cap_rad, 64) }, 10);
		makeRecipe(new ComparableStack(ModBlocks.block_cap, 1, BlockEnums.EnumBlockCapType.KORL.ordinal()), new AStack[] { new ComparableStack(ModItems.cap_korl, 64), new ComparableStack(ModItems.cap_korl, 64) }, 10);
		makeRecipe(new ComparableStack(ModBlocks.block_cap, 1, BlockEnums.EnumBlockCapType.FRITZ.ordinal()), new AStack[] { new ComparableStack(ModItems.cap_fritz, 64), new ComparableStack(ModItems.cap_fritz, 64) }, 10);
		makeRecipe(new ComparableStack(ModBlocks.block_cap, 1, BlockEnums.EnumBlockCapType.SUNSET.ordinal()), new AStack[] { new ComparableStack(ModItems.cap_sunset, 64), new ComparableStack(ModItems.cap_sunset, 64) }, 10);
		makeRecipe(new ComparableStack(ModBlocks.block_cap, 1, BlockEnums.EnumBlockCapType.STAR.ordinal()), new AStack[] { new ComparableStack(ModItems.cap_star, 64), new ComparableStack(ModItems.cap_star, 64) }, 10);
		
		makeRecipe(new ComparableStack(ModItems.ammo_75bolt, 2), new AStack[] {
				new OreDictStack(STEEL.plate(), 2),
				new OreDictStack(CU.plate(), 1),
				new ComparableStack(ModItems.primer_50, 5),
				new ComparableStack(ModItems.casing_50, 5),
				new OreDictStack(ANY_HIGHEXPLOSIVE.ingot(), 5),
				new ComparableStack(ModItems.cordite, 5),
				new OreDictStack(REDSTONE.dust(), 3)
			}, 60);
		makeRecipe(new ComparableStack(ModItems.ammo_75bolt_incendiary, 2), new AStack[] {
				new OreDictStack(STEEL.plate(), 2),
				new OreDictStack(CU.plate(), 1),
				new ComparableStack(ModItems.primer_50, 5),
				new ComparableStack(ModItems.casing_50, 5),
				new OreDictStack(ANY_PLASTICEXPLOSIVE.ingot(), 3),
				new ComparableStack(ModItems.cordite, 3),
				new OreDictStack(P_WHITE.ingot(), 3)
			}, 60);

		makeRecipe(new ComparableStack(ModItems.ammo_75bolt_he, 2), new AStack[] {
				new OreDictStack(STEEL.plate(), 2),
				new OreDictStack(CU.plate(), 1),
				new ComparableStack(ModItems.primer_50, 5),
				new ComparableStack(ModItems.casing_50, 5),
				new OreDictStack(ANY_PLASTICEXPLOSIVE.ingot(), 5),
				new ComparableStack(ModItems.cordite, 5),
				new ComparableStack(Items.REDSTONE, 3)
			}, 60);
		makeRecipe(new ComparableStack(ModItems.spawn_worm, 1), new AStack[] {
				new OreDictStack(TI.block(), 64),
				new OreDictStack(TI.block(), 11),
				new ComparableStack(ModItems.motor, 64),
				new ComparableStack(ModItems.motor, 11),
				new ComparableStack(ModBlocks.glass_trinitite, 25),
				new ComparableStack(Items.REDSTONE, 64),
				new ComparableStack(Items.REDSTONE, 11),
				new ComparableStack(ModItems.wire_fine, 64, Mats.MAT_GOLD.id),
				new ComparableStack(ModItems.wire_fine, 11, Mats.MAT_GOLD.id),
				new OreDictStack(PO210.block(), 10),
				new ComparableStack(ModItems.plate_armor_titanium, 50),
				new ComparableStack(ModItems.coin_worm, 1)
			}, 1200);
		makeRecipe(new ComparableStack(ModBlocks.turret_chekhov, 1), new AStack[] {
				new ComparableStack(ModBlocks.machine_battery, 1),
				new OreDictStack(STEEL.ingot(), 16),
				new OreDictStack(DURA.ingot(), 4),
				new ComparableStack(ModItems.motor, 3),
				new ComparableStack(ModItems.circuit_targeting_tier3, 1),
				new ComparableStack(ModItems.pipes_steel, 1),
				new ComparableStack(ModItems.mechanism_rifle_2, 1),
				new ComparableStack(ModBlocks.crate_iron, 1)
			}, 200);
		makeRecipe(new ComparableStack(ModBlocks.turret_friendly, 1), new AStack[] {
				new ComparableStack(ModBlocks.machine_battery, 1),
				new OreDictStack(STEEL.ingot(), 16),
				new OreDictStack(DURA.ingot(), 4),
				new ComparableStack(ModItems.motor, 3),
				new ComparableStack(ModItems.circuit_targeting_tier2, 1),
				new ComparableStack(ModItems.pipes_steel, 1),
				new ComparableStack(ModItems.mechanism_rifle_1, 1),
				new ComparableStack(ModBlocks.crate_iron, 1)
			}, 200);
		makeRecipe(new ComparableStack(ModBlocks.turret_jeremy, 1), new AStack[] {
				new ComparableStack(ModBlocks.machine_saturnite_battery, 1),
				new OreDictStack(STEEL.ingot(), 16),
				new OreDictStack(DURA.ingot(), 4),
				new ComparableStack(ModItems.motor, 2),
				new ComparableStack(ModItems.circuit_targeting_tier4, 1),
				new ComparableStack(ModItems.motor_desh, 1),
				new ComparableStack(ModItems.hull_small_steel, 3),
				new ComparableStack(ModItems.mechanism_launcher_2, 1),
				new ComparableStack(ModBlocks.crate_steel, 1)
			}, 200);
		makeRecipe(new ComparableStack(ModBlocks.turret_tauon, 1), new AStack[] {
				new ComparableStack(ModBlocks.machine_lithium_battery, 1),
				new OreDictStack(STEEL.ingot(), 16),
				new OreDictStack(ANY_PLASTIC.ingot(), 4),
				new ComparableStack(ModItems.motor, 2),
				new ComparableStack(ModItems.circuit_targeting_tier4, 1),
				new ComparableStack(ModItems.motor_desh, 1),
				new OreDictStack(CU.ingot(), 32),
				new ComparableStack(ModItems.mechanism_special, 1),
				new ComparableStack(ModItems.battery_lithium, 1)
			}, 200);
		makeRecipe(new ComparableStack(ModBlocks.turret_richard, 1), new AStack[] {
				new ComparableStack(ModBlocks.machine_battery, 1),
				new OreDictStack(STEEL.ingot(), 16),
				new OreDictStack(DURA.ingot(), 4),
				new ComparableStack(ModItems.motor, 2),
				new ComparableStack(ModItems.circuit_targeting_tier4, 1),
				new OreDictStack(ANY_PLASTIC.ingot(), 2),
				new ComparableStack(ModItems.hull_small_steel, 8),
				new ComparableStack(ModItems.mechanism_launcher_2, 1),
				new ComparableStack(ModBlocks.crate_steel, 1)
			}, 200);
		makeRecipe(new ComparableStack(ModBlocks.turret_howard, 1), new AStack[] {
				new ComparableStack(ModBlocks.machine_saturnite_battery, 1),
				new OreDictStack(STEEL.ingot(), 24),
				new OreDictStack(DURA.ingot(), 6),
				new ComparableStack(ModItems.motor, 2),
				new ComparableStack(ModItems.motor_desh, 2),
				new ComparableStack(ModItems.circuit_targeting_tier3, 2),
				new ComparableStack(ModItems.pipes_steel, 2),
				new ComparableStack(ModItems.mechanism_rifle_2, 2),
				new ComparableStack(ModBlocks.crate_steel, 1)
			}, 200);
		makeRecipe(new ComparableStack(ModBlocks.turret_maxwell, 1), new AStack[] {
				new ComparableStack(ModBlocks.machine_lithium_battery, 1),
				new OreDictStack(STEEL.ingot(), 24),
				new OreDictStack(DURA.ingot(), 6),
				new ComparableStack(ModItems.motor, 2),
				new ComparableStack(ModItems.circuit_targeting_tier4, 2),
				new ComparableStack(ModItems.pipes_steel, 1),
				new ComparableStack(ModItems.mechanism_special, 3),
				new ComparableStack(ModItems.magnetron, 16),
				new OreDictStack(ANY_RESISTANTALLOY.ingot(), 8)
			}, 200);
		makeRecipe(new ComparableStack(ModBlocks.turret_fritz, 1), new AStack[] {
				new ComparableStack(ModBlocks.machine_battery, 1),
				new OreDictStack(STEEL.ingot(), 16),
				new OreDictStack(DURA.ingot(), 4),
				new ComparableStack(ModItems.motor, 3),
				new ComparableStack(ModItems.circuit_targeting_tier3, 1),
				new ComparableStack(ModItems.pipes_steel, 1),
				new ComparableStack(ModItems.mechanism_launcher_1, 1),
				new ComparableStack(ModBlocks.barrel_steel, 1)
			}, 200);
		makeRecipe(new ComparableStack(ModBlocks.turret_arty, 1), new AStack[] {
				new ComparableStack(ModBlocks.machine_battery, 1),
				new OreDictStack(STEEL.ingot(), 128),
				new OreDictStack(DURA.ingot(), 32),
				new ComparableStack(ModItems.motor_desh, 5),
				new ComparableStack(ModItems.circuit_targeting_tier3, 3),
				new ComparableStack(ModItems.pipes_steel, 12),
				new ComparableStack(ModItems.mechanism_launcher_2, 3),
				new ComparableStack(ModBlocks.machine_radar, 1)
		}, 200);
		makeRecipe(new ComparableStack(ModItems.gun_egon, 1), new AStack[] {new ComparableStack(ModItems.mechanism_special, 4), new OreDictStack(STEEL.plate(), 16), new OreDictStack(PB.plate(), 24), new ComparableStack(ModItems.coil_advanced_torus, 32), new ComparableStack(ModItems.circuit_targeting_tier6, 4), new ComparableStack(ModItems.plate_polymer, 8), new ComparableStack(ModBlocks.machine_schrabidium_battery, 2), new ComparableStack(ModBlocks.machine_waste_drum, 1), new ComparableStack(ModItems.wire_fine, 8, Mats.MAT_COPPER.id)}, 256);
		makeRecipe(new ComparableStack(ModBlocks.silo_hatch, 1), new AStack[]{new ComparableStack(ModItems.motor, 8), new OreDictStack(STEEL.ingot(), 32), new ComparableStack(ModItems.hull_big_steel, 8), new ComparableStack(ModItems.plate_polymer, 4), new ComparableStack(ModItems.pipes_steel, 2), new OreDictStack(REDSTONE.dust(), 4), }, 300);
		
		makeRecipe(new ComparableStack(ModItems.sat_gerald, 1), new AStack[] {
				new ComparableStack(ModItems.burnt_bark, 1),
				new ComparableStack(ModItems.combine_scrap, 1),
				new ComparableStack(ModItems.crystal_horn, 1),
				new ComparableStack(ModItems.crystal_charred, 1),
				new ComparableStack(ModBlocks.pink_log, 1),
				new ComparableStack(ModItems.mp_warhead_15_balefire, 1),
				new ComparableStack(ModBlocks.det_nuke, 16),
				new OreDictStack(STAR.ingot(), 32),
				new ComparableStack(ModItems.coin_creeper, 1),
				new ComparableStack(ModItems.coin_radiation, 1),
				new ComparableStack(ModItems.coin_maskman, 1),
				new ComparableStack(ModItems.coin_worm, 1),
			}, 1200);
		
		makeRecipe(new ComparableStack(ModBlocks.machine_chungus, 1), new AStack[] {
				new ComparableStack(ModItems.hull_big_steel, 6),
				new OreDictStack(STEEL.plate(), 32),
				new OreDictStack(TI.plate(), 12),
				new OreDictStack(ANY_RESISTANTALLOY.ingot(), 16),
				new ComparableStack(ModItems.turbine_tungsten, 5),
				new ComparableStack(ModItems.turbine_titanium, 3),
				new ComparableStack(ModItems.flywheel_beryllium, 1),
				new ComparableStack(ModItems.generator_steel, 10),
				new ComparableStack(ModItems.bolt_compound, 16),
				new ComparableStack(ModItems.pipes_steel, 3)
			}, 600);
		
		makeRecipe(new ComparableStack(ModBlocks.machine_silex, 1), new AStack[] {
				new ComparableStack(Blocks.GLASS, 12),
				new ComparableStack(ModItems.motor, 2),
				new OreDictStack(DURA.ingot(), 4),
				new OreDictStack(STEEL.plate(), 8),
				new OreDictStack(DESH.ingot(), 2),
				new ComparableStack(ModItems.tank_steel, 1),
				new ComparableStack(ModItems.pipes_steel, 1),
				new OreDictStack(DIAMOND.crystal(), 1)
			}, 400);

		makeRecipe(new ComparableStack(ModBlocks.machine_fel, 1), new AStack[] {
				new ComparableStack(ModBlocks.fusion_conductor, 16),
				new ComparableStack(ModBlocks.machine_saturnite_battery, 2),
				new OreDictStack(STEEL.ingot(), 16),
				new OreDictStack(STEEL.plate(), 24),
				new OreDictStack(DESH.ingot(), 8),
				new ComparableStack(ModItems.circuit_red_copper, 4),
				new ComparableStack(ModItems.wire_fine, 64, Mats.MAT_MINGRADE.id),
				new ComparableStack(ModItems.coil_advanced_torus, 16),
				new ComparableStack(ModItems.circuit_gold, 1)
		}, 400);
		
		makeRecipe(new ComparableStack(ModBlocks.rbmk_blank, 1), new AStack[] {
				new ComparableStack(ModBlocks.concrete_asbestos, 4),
				new OreDictStack(STEEL.plate(), 4),
				new OreDictStack(CU.ingot(), 4),
				new ComparableStack(ModItems.plate_polymer, 4)
			}, 100);
		
		makeRecipe(new ComparableStack(ModItems.multitool_hit, 1), new AStack[] {
				new OreDictStack(CDALLOY.ingot(), 4),
				new OreDictStack(STEEL.plate(), 4),
				new ComparableStack(ModItems.wire_fine, 12, Mats.MAT_GOLD.id),
				new ComparableStack(ModItems.motor, 4),
				new ComparableStack(ModItems.circuit_tantalium, 16)
			}, 100);
		
		if(!GeneralConfig.enable528) {
			makeRecipe(new ComparableStack(ModBlocks.reactor_element, 1), new AStack[] {new OreDictStack(STEEL.ingot(), 2), new OreDictStack(OreDictManager.getReflector(), 4), new OreDictStack(PB.plate(), 2), new OreDictStack(ZR.ingot(), 2), },150);
			makeRecipe(new ComparableStack(ModBlocks.reactor_control, 1), new AStack[] {new OreDictStack(STEEL.ingot(), 4), new OreDictStack(PB.ingot(), 6), new ComparableStack(ModItems.bolt_tungsten, 6), new ComparableStack(ModItems.motor, 1), },100);
			makeRecipe(new ComparableStack(ModBlocks.reactor_hatch, 1), new AStack[] {new ComparableStack(ModBlocks.brick_concrete, 1), new OreDictStack(STEEL.plate(), 6), },150);
			makeRecipe(new ComparableStack(ModBlocks.reactor_conductor, 1), new AStack[] {new OreDictStack(STEEL.ingot(), 4), new OreDictStack(CU.plate(), 12), new ComparableStack(ModItems.wire_fine, 4, Mats.MAT_TUNGSTEN.id), },130);
			makeRecipe(new ComparableStack(ModBlocks.reactor_computer, 1), new AStack[] {new ComparableStack(ModBlocks.reactor_conductor, 2), new ComparableStack(ModItems.circuit_targeting_tier3, 4), new ComparableStack(ModItems.circuit_gold, 1), },250);
			makeRecipe(new ComparableStack(ModBlocks.machine_radgen, 1), new AStack[] {new OreDictStack(STEEL.ingot(), 8), new OreDictStack(STEEL.plate(), 32), new ComparableStack(ModItems.coil_magnetized_tungsten, 6), new ComparableStack(ModItems.wire_fine, 24, Mats.MAT_MAGTUNG.id), new ComparableStack(ModItems.circuit_gold, 4), new ComparableStack(ModItems.reactor_core, 3), new OreDictStack(STAR.ingot(), 1), new OreDictStack(KEY_RED, 1), },400);
			makeRecipe(new ComparableStack(ModBlocks.machine_reactor_breeding, 1), new AStack[] {new ComparableStack(ModItems.reactor_core, 1), new OreDictStack(STEEL.ingot(), 12), new OreDictStack(PB.plate(), 16), new ComparableStack(ModBlocks.reinforced_glass, 4), new OreDictStack(ASBESTOS.ingot(), 4), new OreDictStack(ANY_RESISTANTALLOY.ingot(), 4), new ComparableStack(ModItems.crt_display, 1)},150);
			makeRecipe(new ComparableStack(ModBlocks.reactor_research, 1), new AStack[] {new OreDictStack(STEEL.ingot(), 8), new OreDictStack(ANY_RESISTANTALLOY.ingot(), 4), new ComparableStack(ModItems.motor_desh, 2), new OreDictStack(B.ingot(), 5), new OreDictStack(PB.plate(), 8), new ComparableStack(ModItems.crt_display, 3), new ComparableStack(ModItems.circuit_copper, 4 /*TODO: EnumCircuitType.BASIC*/), },300);
			// makeRecipe(new ComparableStack(ModBlocks.machine_reactor_small, 1), new AStack[] {new OreDictStack(STEEL.ingot(), 6), new OreDictStack(ANY_PLASTIC.ingot(), 4), new OreDictStack(PB.plate(), 8), new OreDictStack(CU.plate(), 4), new OreDictStack(PB.ingot(), 12), new OreDictStack(MINGRADE.ingot(), 6), new ComparableStack(ModItems.circuit_copper, 8), new ComparableStack(ModItems.circuit_red_copper, 4), },300);
		
		} else {
			addTantalium(new ComparableStack(ModBlocks.machine_centrifuge, 1), 5);
			addTantalium(new ComparableStack(ModBlocks.machine_gascent, 1), 25);
			addTantalium(new ComparableStack(ModBlocks.machine_crystallizer, 1), 15);
			addTantalium(new ComparableStack(ModBlocks.machine_large_turbine, 1), 10);
			addTantalium(new ComparableStack(ModBlocks.machine_chungus, 1), 50);
			addTantalium(new ComparableStack(ModBlocks.machine_refinery, 1), 3);
			addTantalium(new ComparableStack(ModBlocks.machine_silex, 1), 15);
			addTantalium(new ComparableStack(ModBlocks.machine_radar, 1), 20);
			addTantalium(new ComparableStack(ModBlocks.machine_mining_laser, 1), 30);
			
			addTantalium(new ComparableStack(ModBlocks.turret_chekhov, 1), 3);
			addTantalium(new ComparableStack(ModBlocks.turret_friendly, 1), 3);
			addTantalium(new ComparableStack(ModBlocks.turret_jeremy, 1), 3);
			addTantalium(new ComparableStack(ModBlocks.turret_tauon, 1), 3);
			addTantalium(new ComparableStack(ModBlocks.turret_richard, 1), 3);
			addTantalium(new ComparableStack(ModBlocks.turret_howard, 1), 3);
			addTantalium(new ComparableStack(ModBlocks.turret_maxwell, 1), 3);
			addTantalium(new ComparableStack(ModBlocks.turret_fritz, 1), 3);
			addTantalium(new ComparableStack(ModBlocks.launch_pad, 1), 5);
			
			makeRecipe(new ComparableStack(ModBlocks.machine_cyclotron, 1), new AStack[] {
					new ComparableStack(ModBlocks.machine_lithium_battery, 3),
					new ComparableStack(ModBlocks.hadron_coil_neodymium, 8),
					new ComparableStack(ModItems.wire_fine, 64, Mats.MAT_ALLOY.id),
					new OreDictStack(STEEL.ingot(), 16),
					new OreDictStack(STEEL.plate(), 32),
					new OreDictStack(AL.plate(), 32),
					new OreDictStack(ANY_PLASTIC.ingot(), 24),
					new ComparableStack(ModItems.plate_polymer, 64),
					new ComparableStack(ModItems.board_copper, 8),
					new ComparableStack(ModItems.circuit_red_copper, 8),
					new ComparableStack(ModItems.circuit_gold, 3),
					new ComparableStack(ModItems.circuit_tantalium, 50),
				}, 600);
			
			makeRecipe(new ComparableStack(ModBlocks.rbmk_console, 1), new AStack[] {
					new OreDictStack(STEEL.ingot(), 16),
					new OreDictStack(AL.plate(), 32),
					new OreDictStack(ANY_RUBBER.ingot(), 16),
					new ComparableStack(ModItems.circuit_gold, 5),
					new ComparableStack(ModItems.circuit_tantalium, 20),
				}, 300);
			
			makeRecipe(new ComparableStack(ModBlocks.rbmk_console, 1), new AStack[] {
					new OreDictStack(STEEL.ingot(), 16),
					new OreDictStack(AL.plate(), 32),
					new ComparableStack(ModItems.plate_polymer, 16),
					new ComparableStack(ModItems.circuit_gold, 5),
					new ComparableStack(ModItems.circuit_tantalium, 20),
				}, 300);
			
			makeRecipe(new ComparableStack(ModBlocks.hadron_core, 1), new AStack[] {
					new ComparableStack(ModBlocks.hadron_coil_alloy, 24),
					new OreDictStack(STEEL.ingot(), 8),
					new OreDictStack(ANY_PLASTIC.ingot(), 16),
					new OreDictStack(ANY_RESISTANTALLOY.ingot(), 8),
					new ComparableStack(ModItems.circuit_gold, 5),
					new ComparableStack(ModItems.circuit_schrabidium, 5),
					new ComparableStack(ModItems.circuit_tantalium, 64),
					new ComparableStack(ModItems.circuit_tantalium, 64),
					new ComparableStack(ModItems.circuit_tantalium, 64),
				}, 300);
			
			makeRecipe(new ComparableStack(ModBlocks.struct_launcher_core, 1), new AStack[] {
					new ComparableStack(ModBlocks.machine_battery, 3),
					new ComparableStack(ModBlocks.steel_scaffold, 10),
					new OreDictStack(STEEL.ingot(), 16),
					new OreDictStack(ANY_PLASTIC.ingot(), 8),
					new ComparableStack(ModItems.circuit_red_copper, 5),
					new ComparableStack(ModItems.circuit_tantalium, 15),
				}, 200);
			
			makeRecipe(new ComparableStack(ModBlocks.struct_launcher_core_large, 1), new AStack[] {
					new ComparableStack(ModBlocks.machine_battery, 5),
					new ComparableStack(ModBlocks.steel_scaffold, 10),
					new OreDictStack(STEEL.ingot(), 24),
					new OreDictStack(ANY_PLASTIC.ingot(), 12),
					new ComparableStack(ModItems.circuit_gold, 5),
					new ComparableStack(ModItems.circuit_tantalium, 25),
				}, 200);
			
			makeRecipe(new ComparableStack(ModBlocks.struct_soyuz_core, 1), new AStack[] {
					new ComparableStack(ModBlocks.machine_lithium_battery, 5),
					new ComparableStack(ModBlocks.steel_scaffold, 24),
					new OreDictStack(STEEL.ingot(), 32),
					new OreDictStack(ANY_PLASTIC.ingot(), 24),
					new ComparableStack(ModItems.circuit_gold, 5),
					new ComparableStack(ModItems.upgrade_power_3, 3),
					new ComparableStack(ModItems.circuit_tantalium, 64),
					new ComparableStack(ModItems.circuit_tantalium, 36),
				}, 200);
		}
		makeRecipe(new ComparableStack(ModItems.missile_inferno, 1), new AStack[] {new ComparableStack(ModItems.warhead_incendiary_large, 1), new ComparableStack(ModItems.fuel_tank_large, 1), new ComparableStack(ModItems.thruster_large, 1), new OreDictStack(TI.plate(), 14), new OreDictStack(STEEL.plate(), 20), new OreDictStack(AL.plate(), 12), new ComparableStack(ModItems.circuit_targeting_tier3, 1), },350);
		
		makeRecipe(new ComparableStack(ModBlocks.machine_bat9000, 1), new AStack[] {new OreDictStack(STEEL.plate(), 16), new OreDictStack(ANY_RESISTANTALLOY.ingot(), 16), new ComparableStack(ModBlocks.steel_scaffold, 16), new ComparableStack(ModItems.oil_tar, 16), },150);
		makeRecipe(new ComparableStack(ModBlocks.machine_orbus, 1), new AStack[] {new OreDictStack(STEEL.ingot(), 12), new OreDictStack(ANY_RESISTANTALLOY.ingot(), 12), new OreDictStack(BIGMT.plate(), 12), new ComparableStack(ModItems.coil_advanced_alloy, 12), new ComparableStack(ModItems.battery_sc_polonium, 1) }, 200);
		
		makeRecipe(new ComparableStack(ModBlocks.large_vehicle_door, 1), new AStack[]{new OreDictStack(PB.plate(), 24), new OreDictStack(STEEL.plate(), 36), new OreDictStack(ALLOY.plate(), 4), new ComparableStack(ModItems.plate_polymer, 2), new OreDictStack(STEEL.block(), 4), new ComparableStack(ModItems.motor, 4), new ComparableStack(ModItems.bolt_dura_steel, 12), new OreDictStack(KEY_GREEN, 4)}, 500);
		makeRecipe(new ComparableStack(ModBlocks.water_door, 1), new AStack[]{new OreDictStack(STEEL.plate(), 12), new OreDictStack(ALLOY.plate(), 2), new ComparableStack(ModItems.bolt_dura_steel, 2), new OreDictStack(KEY_RED, 1)}, 500);
		makeRecipe(new ComparableStack(ModBlocks.qe_containment, 1), new AStack[]{new OreDictStack(PB.plate(), 12), new OreDictStack(STEEL.plate(), 24), new OreDictStack(ALLOY.plate(), 8), new ComparableStack(ModItems.plate_polymer, 8), new OreDictStack(STEEL.block(), 2), new ComparableStack(ModItems.motor, 4), new ComparableStack(ModItems.bolt_dura_steel, 16), new OreDictStack(KEY_BLACK, 4)}, 500);
		makeRecipe(new ComparableStack(ModBlocks.qe_sliding_door, 1), new AStack[]{new OreDictStack(STEEL.plate(), 12), new ComparableStack(ModItems.plate_polymer, 2), new OreDictStack(STEEL.block(), 1), new ComparableStack(ModItems.motor, 2), new ComparableStack(ModItems.bolt_dura_steel, 2), new OreDictStack(KEY_WHITE, 4), new ComparableStack(Blocks.GLASS, 4)}, 200);
		makeRecipe(new ComparableStack(ModBlocks.fire_door, 1), new AStack[]{new OreDictStack(PB.plate(), 12), new OreDictStack(STEEL.plate(), 36), new OreDictStack(ASBESTOS.ingot(), 12), new ComparableStack(ModItems.plate_polymer, 6), new OreDictStack(STEEL.block(), 4), new ComparableStack(ModItems.motor, 4), new ComparableStack(ModItems.bolt_dura_steel, 6), new OreDictStack(KEY_RED, 8)}, 500);
		makeRecipe(new ComparableStack(ModBlocks.small_hatch, 1), new AStack[]{new OreDictStack(PB.plate(), 4), new OreDictStack(STEEL.plate(), 8), new OreDictStack(ALLOY.plate(), 2), new ComparableStack(ModItems.bolt_dura_steel, 1), new ComparableStack(ModBlocks.brick_concrete, 1), new ComparableStack(ModBlocks.ladder_red, 1)}, 200);
		makeRecipe(new ComparableStack(ModBlocks.round_airlock_door, 1), new AStack[]{new OreDictStack(PB.plate(), 16), new OreDictStack(STEEL.plate(), 32), new OreDictStack(ALLOY.plate(), 12), new ComparableStack(ModItems.plate_polymer, 12), new OreDictStack(STEEL.block(), 6), new ComparableStack(ModItems.motor, 6), new ComparableStack(ModItems.bolt_dura_steel, 12), new OreDictStack(KEY_GRAY, 4)}, 500);
		makeRecipe(new ComparableStack(ModBlocks.secure_access_door, 1), new AStack[]{new OreDictStack(PB.plate(), 32), new OreDictStack(STEEL.plate(), 48), new OreDictStack(ALLOY.plate(), 16), new ComparableStack(ModItems.plate_polymer, 2), new OreDictStack(STEEL.block(), 6), new ComparableStack(ModItems.motor, 4), new ComparableStack(ModItems.bolt_dura_steel, 24), new OreDictStack(KEY_CYAN, 8)}, 1000);
		makeRecipe(new ComparableStack(ModBlocks.sliding_seal_door, 1), new AStack[]{new OreDictStack(STEEL.plate(), 6), new OreDictStack(ALLOY.plate(), 2), new ComparableStack(ModItems.plate_polymer, 1), new ComparableStack(ModItems.motor, 1), new ComparableStack(ModItems.bolt_dura_steel, 1), new OreDictStack(KEY_LIGHTGRAY, 1)}, 300);
		makeRecipe(new ComparableStack(ModBlocks.sliding_gate_door, 1), new AStack[]{new OreDictStack(PB.plate(), 4), new OreDictStack(STEEL.plate(), 12), new OreDictStack(ALLOY.plate(), 4), new ComparableStack(ModItems.plate_polymer, 2), new OreDictStack(STEEL.block(), 1), new ComparableStack(ModItems.motor, 2), new ComparableStack(ModItems.bolt_dura_steel, 2), new OreDictStack(KEY_WHITE, 2)}, 500);
		makeRecipe(new ComparableStack(ModBlocks.transition_seal, 1), new AStack[]{new ComparableStack(ModBlocks.cmb_brick_reinforced, 16), new OreDictStack(STEEL.plate(), 64), new OreDictStack(ALLOY.plate(), 40), new OreDictStack(ANY_RUBBER.ingot(), 36), new OreDictStack(STEEL.block(), 24), new ComparableStack(ModItems.motor_desh, 16), new ComparableStack(ModItems.bolt_dura_steel, 12), new OreDictStack(KEY_YELLOW, 4)}, 5000);
		
		makeRecipe(new ComparableStack(ModBlocks.control_panel_custom, 1), new AStack[]{new ComparableStack(ModItems.circuit_targeting_tier5), new OreDictStack(STEEL.block(), 1), new ComparableStack(ModItems.wire_fine, 24, Mats.MAT_COPPER.id), new ComparableStack(ModBlocks.pole_top)}, 100);
		makeRecipe(new ComparableStack(ModBlocks.railgun_plasma, 1), new AStack[]{new OreDictStack(STEEL.plate(), 24), new ComparableStack(ModItems.hull_big_steel, 2), new ComparableStack(ModItems.hull_small_steel, 6), new ComparableStack(ModItems.pipes_steel, 2), new ComparableStack(ModBlocks.machine_desh_battery, 4), new ComparableStack(ModItems.coil_copper, 16), new ComparableStack(ModItems.coil_copper_torus, 8), new ComparableStack(ModItems.plate_desh, 4), new ComparableStack(ModItems.circuit_targeting_tier4, 4), new ComparableStack(ModItems.circuit_targeting_tier3, 2), new OreDictStack(ANY_PLASTIC.ingot(), 4)}, 500);
		
		/// HIDDEN ///
		hidden.add(new ComparableStack(ModBlocks.machine_radgen, 1));
	}

	public static void addTantalium(ComparableStack out, int amount) {
		
		AStack[] ins = recipes.get(out);
		
		if(ins != null) {
			
			AStack[] news = new AStack[ins.length + 1];
			
			for(int i = 0; i < ins.length; i++)
				news[i] = ins[i];
			
			news[news.length - 1] = new ComparableStack(ModItems.circuit_tantalium, amount);
			
			recipes.put(out, news);
		}
	}
	
	public static void makeRecipe(ComparableStack out, AStack[] in, int duration) {

		if(out == null || Item.REGISTRY.getNameForObject(out.item) == null) {
			MainRegistry.logger.error("Canceling assembler registration, item was null!");
			return;
		}

		recipes.put(out, in);
		time.put(out, duration);
	}

	public static void removeRecipe(ComparableStack out){
		if(out == null || Item.REGISTRY.getNameForObject(out.item) == null) {
			MainRegistry.logger.error("Canceling assembler recipe removal, item was null!");
			return;
		}
		recipes.remove(out);
		time.remove(out);
		recipeList.remove(out);
	}

	public static void loadRecipesFromConfig() {
		itemRegistry = GameRegistry.findRegistry(Item.class);
		blockRegistry = GameRegistry.findRegistry(Block.class);
		
		File recipeConfig = new File(MainRegistry.proxy.getDataDir().getPath() + "/config/hbm/assemblerConfig.cfg");
		if (!recipeConfig.exists())
			try {
				recipeConfig.getParentFile().mkdirs();
				FileWriter write = new FileWriter(recipeConfig);
				write.write("# Format: time;itemName,meta,amount|nextItemName,meta,amount;productName,meta,amount\n"
						  + "# One line per recipe.\n"
						  + "# For an oredict input item, replace the mod id with oredict, like oredict:plateSteel. These do not require metatdata\n"
						  + "# Example for iron plates: 30;minecraft:iron_ingot,0,3;oredict:plateIron,2\n"
						  + "# For an NBT item, use a 4th item parameter with the nbt string of the tag.\n"
						  + "# The NBT string format is the same as used in commands\n"
						  + "# Example for turning kerosene canisters into steel plates:\n"
						  + "# 20;hbm:canister_fuel,0,2,{HbmFluidKey:{FluidName:\"kerosene\",Amount:1000}};hbm:plate_steel,0,32\n"
						  + "#\n"
						  + "# To remove a recipe, use the format: \n"
						  + "# remove hbm:plate_iron,0,2\n"
						  + "# This will remove any recipe with the output of two iron plates");
				addConfigRecipes(write);
				write.close();
				
			} catch (IOException e) {
				MainRegistry.logger.log(Level.ERROR, "ERROR: Could not create config file: " + recipeConfig.getAbsolutePath());
				e.printStackTrace();
				return;
			}
		
		
		
		BufferedReader read = null;
		try {
			read = new BufferedReader(new FileReader(recipeConfig));
			String currentLine = null;
			int lineCount = 0;
			
			while((currentLine = read.readLine()) != null){
				lineCount ++;
				if(currentLine.startsWith("#") || currentLine.length() == 0)
					continue;
				if(currentLine.startsWith("remove"))
					parseRemoval(currentLine, lineCount);
				else
					parseRecipe(currentLine, lineCount);
			}
		} catch (FileNotFoundException e) {
			MainRegistry.logger.log(Level.ERROR, "Could not find assembler config file! This should never happen.");
			e.printStackTrace();
		} catch (IOException e){
			MainRegistry.logger.log(Level.ERROR, "Error reading assembler config!");
			e.printStackTrace();
		} finally {
			if(read != null)
				try {
					read.close();
				} catch (IOException e) {}
		}
		
	}
	
	public static void parseRemoval(String currentLine, int line){
		String[] parts = currentLine.split(" ");
		if(parts.length != 2){
			MainRegistry.logger.log(Level.WARN, "Could not parse assembler recipe removal on line " + line + ": does not have two parts. Skipping...");
			return;
		}
		AStack stack = parseAStack(parts[1], 64);
		if(stack == null){
			MainRegistry.logger.log(Level.WARN, "Could not parse assembler output itemstack from \"" + parts[1] + "\" on line " + line + ". Skipping...");
			return;
		}
		if(!(stack instanceof ComparableStack)){
			MainRegistry.logger.log(Level.WARN, "Oredict stacks are not allowed as assembler outputs! Line number: " + line + " Skipping...");
			return;
		}
		ComparableStack cStack = (ComparableStack) stack;
		recipes.remove(cStack);
		time.remove(cStack);
		recipeList.remove(cStack);
	}

	private static void parseRecipe(String currentLine, int line) {
		String[] parts = currentLine.split(";");
		if(parts.length != 3){
			MainRegistry.logger.log(Level.WARN, "Could not parse assembler recipe on line " + line + ": does not have three parts. Skipping...");
			return;
		}
		int recipeTime = 0;
		try {
			recipeTime = Integer.parseInt(parts[0]);
		} catch (NumberFormatException e){
			MainRegistry.logger.log(Level.WARN, "Could not parse assembler process time from \"" + parts[0] + "\" on line " + line + ". Skipping...");
			return;
		}
		List<AStack> input = new ArrayList<>();
		for(String s : parts[1].split("\\|")){
			AStack stack = parseAStack(s, 12*64);
			if(stack == null){
				MainRegistry.logger.log(Level.WARN, "Could not parse assembler input itemstack from \"" + s + "\" on line " + line + ". Skipping...");
				return;
			}
			input.add(stack);
		}
		AStack output = parseAStack(parts[2], 64);
		if(output == null){
			MainRegistry.logger.log(Level.WARN, "Could not parse assembler output itemstack from \"" + parts[2] + "\" on line " + line + ". Skipping...");
			return;
		}
		if(!(output instanceof ComparableStack)){
			MainRegistry.logger.log(Level.WARN, "Oredict stacks are not allowed as assembler outputs! Line number: " + line + " Skipping...");
			return;
		}
		if(recipes.containsKey(output)){
			MainRegistry.logger.log(Level.WARN, "Found duplicate assembler recipe outputs! This is not allowed! Line number: " + line + " Skipping...");
		}
		recipes.put((ComparableStack) output, input.toArray(new AStack[input.size()]));
		time.put((ComparableStack) output, recipeTime);
		recipeList.add((ComparableStack) output);
	}
	
	//The whole point of these two methods below is to ignore the part inside braces for nbt tags.
	//I'm sure there's a cleaner way to do this, but I'm not going to spend more time trying to figure it out.
	private static String readSplitPart(int idx, String s){
		StringBuilder build = new StringBuilder();
		int braceCount = 0;
		for(int i = idx; i < s.length(); i ++){
			char c = s.charAt(i);
			if(c == '{'){
				braceCount ++;
			} else if(c == '}'){
				braceCount --;
			}
			if(braceCount == 0 && (c == '|' || c == ',' || c == ';'))
				break;
			build.append(c);
		}
		if(build.length() == 0)
			return null;
		return build.toString();
	}
	
	private static String[] splitStringIgnoreBraces(String s){
		List<String> list = new ArrayList<>();
		int idx = 0;
		while(idx < s.length()){
			String part = readSplitPart(idx, s);
			if(part != null)
				list.add(part);
			else
				break;
			if(list.size() >= 4)
				break;
			idx += part.length()+1;
		}
		return list.toArray(new String[list.size()]);
	}

	private static AStack parseAStack(String s, int maxSize){
		String[] parts = splitStringIgnoreBraces(s);
		if(parts.length == 3 || parts.length == 4){
			Block block = null;
			Item item = itemRegistry.getValue(new ResourceLocation(parts[0]));
			if(item == null)
				block = blockRegistry.getValue(new ResourceLocation(parts[0]));
			if(item == null && block == null){
				MainRegistry.logger.log(Level.WARN, "Could not parse item or block from \"" + parts[0] + "\", probably isn't a registered item. Skipping...");
				return null;
			}
			int meta = 0;
			try {
				meta = Integer.parseInt(parts[1]);
			} catch (NumberFormatException e) {
				MainRegistry.logger.log(Level.WARN, "Could not parse item metadata from \"" + parts[1] + "\". Skipping...");
				return null;
			}
			if(meta < 0){
				MainRegistry.logger.log(Level.WARN, "Bad item meta: " + meta + ". Skipping...");
				return null;
			}
			int amount = 0;
			try {
				amount = Integer.parseInt(parts[2]);
			} catch (NumberFormatException e){
				MainRegistry.logger.log(Level.WARN, "Could not parse item amount from \"" + parts[2] + "\". Skipping...");
				return null;
			}
			if(amount < 0 || amount > maxSize){
				MainRegistry.logger.log(Level.WARN, "Bad item amount: " + amount + ". Skipping...");
				return null;
			}
			if(parts.length == 4){
				String name = parts[3];
				name.trim();
				NBTTagCompound tag = parseNBT(name);
				if(tag == null){
					MainRegistry.logger.log(Level.WARN, "Failed to parse NBT tag: " + parts[3] + ". Skipping...");
					return null;
				}
				ItemStack stack;
				if(item == null)
					stack = new ItemStack(block, amount, meta);
				else
					stack = new ItemStack(item, amount, meta);
				stack.setTagCompound(tag);
				return new NbtComparableStack(stack);
			} else {
				if(item == null)
					return new ComparableStack(block, amount, meta);
				return new ComparableStack(item, amount, meta);
			}
		}
		if(parts.length == 2){
			String[] ore = parts[0].split(":");
			if(ore.length == 2 && ore[0].equals("oredict")){
				String name = ore[1];
				int amount = 0;
				try {
					amount = Integer.parseInt(parts[1]);
				} catch (NumberFormatException e){
					MainRegistry.logger.log(Level.WARN, "Could not parse item amount from \"" + parts[1] + "\". Skipping...");
					return null;
				}
				if(amount < 0 || amount > 12*64){
					MainRegistry.logger.log(Level.WARN, "Bad item amount: " + amount + ". Skipping...");
					return null;
				}
				return new OreDictStack(name, amount);
			} else {
				MainRegistry.logger.log(Level.WARN, "Could not parse ore dict name from \"" + parts[0] + "\". Skipping...");
			}
		}
		return null;
	}
	
	private static NBTTagCompound parseNBT(String json){
		try {
			return JsonToNBT.getTagFromJson(json);
		} catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	private static void addConfigRecipes(FileWriter write) throws IOException {
			write.write("\n");
	}
}