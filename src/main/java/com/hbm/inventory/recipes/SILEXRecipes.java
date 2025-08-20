package com.hbm.inventory.recipes;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.NbtComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFELCrystal.EnumWavelengths;
import com.hbm.items.machine.ItemFluidIcon;
import com.hbm.items.special.ItemWasteLong;
import com.hbm.items.special.ItemWasteShort;
import com.hbm.util.WeightedRandomObject;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;
import java.util.Map.Entry;

import static com.hbm.inventory.OreDictManager.*;

public class SILEXRecipes {

	private static LinkedHashMap<Object, SILEXRecipe> recipes = new LinkedHashMap<>();
	private static HashMap<ComparableStack, ComparableStack> itemTranslation = new HashMap<>();
	private static HashMap<String, String> dictTranslation = new HashMap<>();
	
	public static void register() {

		itemTranslation.put(new NbtComparableStack(ItemFluidIcon.make(Fluids.UF6, 1)), new ComparableStack(ModItems.ingot_uranium));
		dictTranslation.put(U.dust(), U.ingot());
		recipes.put(U.ingot(), new SILEXRecipe(900, 100, EnumWavelengths.UV)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_u235), 1))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_u238), 8))
				);
		
		recipes.put(new ComparableStack(ModItems.ingot_pu_mix), new SILEXRecipe(900, 100, EnumWavelengths.UV)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu239), 6))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu240), 3))
				);
		
		recipes.put(new ComparableStack(ModItems.ingot_am_mix), new SILEXRecipe(900, 100, EnumWavelengths.XRAY)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_am241), 3))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_am242), 6))
				);

		itemTranslation.put(new NbtComparableStack(ItemFluidIcon.make(Fluids.PUF6, 1)), new ComparableStack(ModItems.ingot_plutonium));
		dictTranslation.put(PU.dust(), PU.ingot());
		recipes.put(PU.ingot(), new SILEXRecipe(900, 100, EnumWavelengths.VISIBLE)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu238), 3))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu239), 4))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu240), 2))
				);

		recipes.put(SRN.ingot(), new SILEXRecipe(900, 100, EnumWavelengths.UV)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_schrabidium), 4))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_uranium), 3))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_plutonium), 2))
				);

		dictTranslation.put(AUSTRALIUM.dust(), AUSTRALIUM.ingot());
		recipes.put(AUSTRALIUM.ingot(), new SILEXRecipe(900, 100, EnumWavelengths.UV)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_australium_lesser), 5))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_australium_greater), 1))
				);

		dictTranslation.put(UNOBTAINIUM.dust(), UNOBTAINIUM.ingot());
		recipes.put(UNOBTAINIUM.ingot(), new SILEXRecipe(800, 100, EnumWavelengths.GAMMA)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_unobtainium_lesser), 99))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_unobtainium_greater), 1))
				);

		recipes.put(GOLD.ingot(), new SILEXRecipe(800, 100, EnumWavelengths.GAMMA)
				.addOut(new WeightedRandomObject(new ItemStack(Items.GOLD_NUGGET), 99))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_au198), 1))
				);
		recipes.put(PB.ingot(), new SILEXRecipe(800, 100, EnumWavelengths.GAMMA)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_lead), 99))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pb209), 1))
				);

		dictTranslation.put(LAPIS.dust(), KEY_BLUE);
		recipes.put(new ComparableStack(Items.DYE, 1, 4), new SILEXRecipe(100, 100, EnumWavelengths.RADIO)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.sulfur), 4))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_aluminium), 3))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_cobalt), 3))
				);

		for(int i = 0; i < 5; i++) {
			
			// UEU //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_ueu, 1, i), new SILEXRecipe(600, 100, EnumWavelengths.IR)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_uranium), 86 - i * 11))	//NU is unenriched to the point where it'll always be lower burnup; so more Pu239 for longer
					.addOut(new WeightedRandomObject(i < 2 ? new ItemStack(ModItems.nugget_pu239) : new ItemStack(ModItems.nugget_pu_mix), 10 + i * 3)) 
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.URANIUM235.ordinal()), 2 + 3 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.URANIUM235.ordinal()), 2 + 5 * i)) );
		}
		for(int i = 0; i < 5; i++) {
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_ueu, 1, i + 5), new SILEXRecipe(600, 100, EnumWavelengths.IR)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_xe135_tiny), 1))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_uranium), 86 - i * 11))
					.addOut(new WeightedRandomObject(i < 2 ? new ItemStack(ModItems.nugget_pu239) : new ItemStack(ModItems.nugget_pu_mix), 10 + i * 3))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.URANIUM235.ordinal()), 2 + 3 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.URANIUM235.ordinal()), 1 + 5 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			// MEU //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_meu, 1, i), new SILEXRecipe(600, 100, EnumWavelengths.IR)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_uranium_fuel), 84 - i * 16))
					.addOut(new WeightedRandomObject(i < 1 ? new ItemStack(ModItems.nugget_pu239) : new ItemStack(ModItems.nugget_pu_mix), 6 + i * 4))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.URANIUM235.ordinal()), 4 + 5 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.URANIUM235.ordinal()), 6 + 7 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_meu, 1, i + 5), new SILEXRecipe(600, 100, EnumWavelengths.IR)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_xe135_tiny), 1))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_uranium_fuel), 83 - i * 16))
					.addOut(new WeightedRandomObject(i < 1 ? new ItemStack(ModItems.nugget_pu239) : new ItemStack(ModItems.nugget_pu_mix), 6 + i * 4))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.URANIUM235.ordinal()), 4 + 5 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.URANIUM235.ordinal()), 6 + 7 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			// HEU233 //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_heu233, 1, i), new SILEXRecipe(600, 100, EnumWavelengths.IR)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_u233), 90 - i * 20))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.URANIUM233.ordinal()), 4 + 8 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.URANIUM233.ordinal()), 6 + 12 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_heu233, 1, i + 5), new SILEXRecipe(600, 100, EnumWavelengths.IR)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_xe135_tiny), 3))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_u233), 87 - i * 20))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.URANIUM233.ordinal()), 4 + 8 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.URANIUM233.ordinal()), 6 + 12 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			// MEU //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_heu235, 1, i), new SILEXRecipe(600, 100, EnumWavelengths.IR)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_u235), 90 - i * 20))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.URANIUM235.ordinal()), 4 + 8 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.URANIUM235.ordinal()), 6 + 12 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_heu235, 1, i + 5), new SILEXRecipe(600, 100, EnumWavelengths.IR)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_xe135_tiny), 3))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_u235), 87 - i * 20))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.URANIUM235.ordinal()), 4 + 8 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.URANIUM235.ordinal()), 6 + 12 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			// TH232 //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_thmeu, 1, i), new SILEXRecipe(600, 100, EnumWavelengths.IR)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_thorium_fuel), 84 - i * 20))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_u233), 6 + i * 4))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.THORIUM.ordinal()), 10 + 16 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_thmeu, 1, i + 5), new SILEXRecipe(600, 100, EnumWavelengths.IR)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_xe135_tiny), 1))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_thorium_fuel), 83 - i * 20))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_u233), 6 + i * 4))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.THORIUM.ordinal()), 10 + 16 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			// LEP //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_lep, 1, i), new SILEXRecipe(600, 100, EnumWavelengths.IR)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_plutonium_fuel), 84 - i * 14))
					.addOut(new WeightedRandomObject(i < 1 ? new ItemStack(ModItems.nugget_pu239) : new ItemStack(ModItems.nugget_pu_mix), 6 + i * 2))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.PLUTONIUM239.ordinal()), 7 + 8 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.PLUTONIUM240.ordinal()), 3 + 4 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_lep, 1, i + 5), new SILEXRecipe(600, 100, EnumWavelengths.IR)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_xe135_tiny), 1))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_plutonium_fuel), 83 - i * 14))
					.addOut(new WeightedRandomObject(i < 1 ? new ItemStack(ModItems.nugget_pu239) : new ItemStack(ModItems.nugget_pu_mix), 6 + i * 2))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.PLUTONIUM239.ordinal()), 7 + 8 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.PLUTONIUM240.ordinal()), 3 + 4 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			// MEP //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_mep, 1, i), new SILEXRecipe(600, 100, EnumWavelengths.VISIBLE)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu_mix), 85 - i * 15))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.PLUTONIUM239.ordinal()), 10 + 10 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.PLUTONIUM240.ordinal()), 5 + 5 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_mep, 1, i + 5), new SILEXRecipe(600, 100, EnumWavelengths.VISIBLE)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_xe135_tiny), 3))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu_mix), 82 - i * 15))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.PLUTONIUM239.ordinal()), 10 + 10 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.PLUTONIUM240.ordinal()), 5 + 5 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			// HEP239 //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_hep239, 1, i), new SILEXRecipe(600, 100, EnumWavelengths.UV)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu239), 85 - i * 20))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.PLUTONIUM239.ordinal()), 15 + 20 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_hep239, 1, i + 5), new SILEXRecipe(600, 100, EnumWavelengths.UV)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_xe135_tiny), 3))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu239), 82 - i * 20))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.PLUTONIUM239.ordinal()), 15 + 20 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			// HEP241 //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_hep241, 1, i), new SILEXRecipe(600, 100, EnumWavelengths.UV)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu241), 85 - i * 20))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.PLUTONIUM241.ordinal()), 15 + 20 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_hep241, 1, i + 5), new SILEXRecipe(600, 100, EnumWavelengths.UV)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_xe135_tiny), 3))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu241), 82 - i * 20))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.PLUTONIUM241.ordinal()), 15 + 20 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			// MEN //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_men, 1, i), new SILEXRecipe(600, 100, EnumWavelengths.UV)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_neptunium_fuel), 84 - i * 14))
					.addOut(new WeightedRandomObject(i < 1 ? new ItemStack(ModItems.nugget_pu239) : new ItemStack(ModItems.nugget_pu_mix), 6 + i * 2))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.NEPTUNIUM.ordinal()), 4 + 5 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.NEPTUNIUM.ordinal()), 6 + 7 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_men, 1, i + 5), new SILEXRecipe(600, 100, EnumWavelengths.UV)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_xe135_tiny), 1))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_neptunium_fuel), 83 - i * 14))
					.addOut(new WeightedRandomObject(i < 1 ? new ItemStack(ModItems.nugget_pu239) : new ItemStack(ModItems.nugget_pu_mix), 6 + i * 2))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.NEPTUNIUM.ordinal()), 4 + 5 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.NEPTUNIUM.ordinal()), 6 + 7 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			// HEN //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_hen, 1, i), new SILEXRecipe(600, 100, EnumWavelengths.UV)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_neptunium), 84 - i * 14))
					.addOut(new WeightedRandomObject(i < 1 ? new ItemStack(ModItems.nugget_pu240) : new ItemStack(ModItems.nugget_technetium), 6 + i * 7))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.NEPTUNIUM.ordinal()), 4 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.NEPTUNIUM.ordinal()), 10 + 3 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_hen, 1, i + 5), new SILEXRecipe(600, 100, EnumWavelengths.UV)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_xe135_tiny), 4))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_neptunium), 80 - i * 14))
					.addOut(new WeightedRandomObject(i < 1 ? new ItemStack(ModItems.nugget_pu241) : new ItemStack(ModItems.nugget_technetium), 6 + i * 7))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.NEPTUNIUM.ordinal()), 4 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.NEPTUNIUM.ordinal()), 10 + 3 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			// MOX //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_mox, 1, i), new SILEXRecipe(600, 100, EnumWavelengths.VISIBLE)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_mox_fuel), 84 - i * 20))
					.addOut(new WeightedRandomObject(i < 1 ? new ItemStack(ModItems.nugget_pu239) : new ItemStack(ModItems.nugget_pu_mix), 6 + i * 4))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.URANIUM235.ordinal()), 2 + 3 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.URANIUM235.ordinal()), 3 + 5 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.PLUTONIUM239.ordinal()), 3 + 5 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.PLUTONIUM240.ordinal()), 2 + 3 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_mox, 1, i + 5), new SILEXRecipe(600, 100, EnumWavelengths.VISIBLE)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_mox_fuel), 84 - i * 20))		//To prevent people from taking advantage of differing waste types, conform to the latter
					.addOut(new WeightedRandomObject(i < 1 ? new ItemStack(ModItems.nugget_pu239) : new ItemStack(ModItems.nugget_pu_mix), 6 + i * 4))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.URANIUM235.ordinal()), 2 + 3 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.URANIUM235.ordinal()), 3 + 5 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.PLUTONIUM239.ordinal()), 3 + 5 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.PLUTONIUM240.ordinal()), 2 + 3 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			// LEAUS //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_leaus, 1, i), new SILEXRecipe(600, 100, EnumWavelengths.VISIBLE)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_australium_lesser), 90 - i * 20))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_lead), 6 + 12 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pb209), 4 + 8 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_leaus, 1, i + 5), new SILEXRecipe(600, 100, EnumWavelengths.VISIBLE)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_xe135_tiny), 1))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_australium_lesser), 89 - i * 20))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_lead), 6 + 12 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pb209), 4 + 8 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			// HEAUS //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_heaus, 1, i), new SILEXRecipe(600, 100, EnumWavelengths.VISIBLE)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_australium_greater), 90 - i * 20))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_au198), 5 + 10 * i))
					.addOut(new WeightedRandomObject(new ItemStack(Items.GOLD_NUGGET), 3 + 6 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pb209), 2 + 4 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_heaus, 1, i + 5), new SILEXRecipe(600, 100, EnumWavelengths.VISIBLE)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_xe135_tiny), 1))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_australium_greater), 89 - i * 20))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_au198), 5 + 10 * i))
					.addOut(new WeightedRandomObject(new ItemStack(Items.GOLD_NUGGET), 3 + 6 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pb209), 2 + 4 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			// LES //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_les, 1, i), new SILEXRecipe(600, 100, EnumWavelengths.UV)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_les), 90 - i * 20))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.NEPTUNIUM.ordinal()), 2 + 3 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.NEPTUNIUM.ordinal()), 2 + 5 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.SCHRABIDIUM.ordinal()), 1 + 2 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.SCHRABIDIUM.ordinal()), 1 + 2 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_coal_tiny), 4 + 8 * i)) );
		}
		for(int i = 0; i < 5; i++) {				
			//TODO: Readd xenon processing if/when the NEI handler can display more than 6 outputs properly
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_les, 1, i + 5), new SILEXRecipe(600, 100, EnumWavelengths.UV)	//I'd rather not fuck up the NEI handler, so six items it is
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_les), 90 - i * 20))			//Just bullshit something about "not enough np237 for extractable amounts of xe135"
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.NEPTUNIUM.ordinal()), 2 + 3 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.NEPTUNIUM.ordinal()), 2 + 5 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.SCHRABIDIUM.ordinal()), 1 + 2 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.SCHRABIDIUM.ordinal()), 1 + 2 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_coal_tiny), 4 + 8 * i)) ); 
		}
		for(int i = 0; i < 5; i++) {				
			// MES //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_mes, 1, i), new SILEXRecipe(600, 100, EnumWavelengths.UV)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_schrabidium_fuel), 90 - i * 20))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.NEPTUNIUM.ordinal()), 1 + 3 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.NEPTUNIUM.ordinal()), 2 + 4 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.SCHRABIDIUM.ordinal()), 1 + 3 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.SCHRABIDIUM.ordinal()), 2 + 4 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_coal_tiny), 4 + 6 * i)) );
		}
		for(int i = 0; i < 5; i++) {		    
			//TODO: Readd xenon processing if/when the NEI handler can display more than 6 outputs properly
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_mes, 1, i + 5), new SILEXRecipe(600, 100, EnumWavelengths.UV)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_schrabidium_fuel), 90 - i * 20)) //ditto
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.NEPTUNIUM.ordinal()), 1 + 3 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.NEPTUNIUM.ordinal()), 2 + 4 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.SCHRABIDIUM.ordinal()), 1 + 3 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.SCHRABIDIUM.ordinal()), 2 + 4 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_coal_tiny), 4 + 6 * i)) );
		}
		for(int i = 0; i < 5; i++) {		    
			// HES //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_hes, 1, i), new SILEXRecipe(600, 100, EnumWavelengths.UV)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_hes), 90 - i * 20))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.NEPTUNIUM.ordinal()), 1 + 2 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.NEPTUNIUM.ordinal()), 1 + 3 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.SCHRABIDIUM.ordinal()), 2 + 5 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.SCHRABIDIUM.ordinal()), 4 + 6 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_coal_tiny), 2 + 4 * i)) );
		}
		for(int i = 0; i < 5; i++) {		    
			//TODO: Readd xenon processing if/when the NEI handler can display more than 6 outputs properly
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_hes, 1, i + 5), new SILEXRecipe(600, 100, EnumWavelengths.UV)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_hes), 90 - i * 20)) //ditto
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.NEPTUNIUM.ordinal()), 1 + 2 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.NEPTUNIUM.ordinal()), 1 + 3 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.SCHRABIDIUM.ordinal()), 2 + 5 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.SCHRABIDIUM.ordinal()), 4 + 6 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_coal_tiny), 2 + 4 * i)) );
		}
		for(int i = 0; i < 5; i++) {
			// LEA //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_lea, 1, i), new SILEXRecipe(600, 100, EnumWavelengths.UV)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_am_mix), 45 - 8 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.PLUTONIUM240.ordinal()), 10 + 2 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.NEPTUNIUM.ordinal()), 10 + 2 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.NEPTUNIUM.ordinal()), 10 + i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.AMERICIUM242.ordinal()), 5 + 2 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_coal_tiny), 20 + i)) );
		}
		for(int i = 0; i < 5; i++) {				
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_lea, 1, i + 5), new SILEXRecipe(600, 100, EnumWavelengths.UV)	//I'd rather not fuck up the NEI handler, so six items it is
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_am_mix), 45 - 8 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.PLUTONIUM240.ordinal()), 10 + 2 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.NEPTUNIUM.ordinal()), 10 + 2 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.NEPTUNIUM.ordinal()), 10 + i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.AMERICIUM242.ordinal()), 5 + 3 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_xe135_tiny), 20))
					);
		}
		for(int i = 0; i < 5; i++) {				
			// MEA //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_mea, 1, i), new SILEXRecipe(600, 100, EnumWavelengths.UV)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_am_mix), 72 - i * 12))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.PLUTONIUM241.ordinal()), 5 + 3 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.NEPTUNIUM.ordinal()), 5 + 2 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.NEPTUNIUM.ordinal()), 5 + 2 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.AMERICIUM242.ordinal()), 3 + 4 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_coal_tiny), 10 + i)) 
					);
		}
		for(int i = 0; i < 5; i++) {		    
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_mea, 1, i + 5), new SILEXRecipe(600, 100, EnumWavelengths.UV)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_am_mix), 72 - i * 12))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.PLUTONIUM241.ordinal()), 5 + 3 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.NEPTUNIUM.ordinal()), 5 + 2 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.NEPTUNIUM.ordinal()), 5 + 3 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.AMERICIUM242.ordinal()), 3 + 4 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_xe135_tiny), 10))
					);
		}
		for(int i = 0; i < 5; i++) {
			// HEA241 //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_hea241, 1, i), new SILEXRecipe(600, 100, EnumWavelengths.UV)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_am241), 85 - 15 * i)) //ditto
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.PLUTONIUM241.ordinal()), 6 + 3 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.NEPTUNIUM.ordinal()), 4 + 1 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.NEPTUNIUM.ordinal()), 2 + 4 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.AMERICIUM242.ordinal()), 1 + 5 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_coal_tiny), 2 + 2 * i)) );
		}
		for(int i = 0; i < 5; i++) {
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_hea241, 1, i + 5), new SILEXRecipe(600, 100, EnumWavelengths.UV)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_am241), 85 - 15 * i)) //ditto
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.PLUTONIUM241.ordinal()), 6 + 3 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.NEPTUNIUM.ordinal()), 4 + 1 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.NEPTUNIUM.ordinal()), 2 + 4 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.AMERICIUM242.ordinal()), 1 + 5 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_xe135_tiny), 2 + 2 * i)) );
		}
		for(int i = 0; i < 5; i++) {
			// HEA242 //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_hea242, 1, i), new SILEXRecipe(600, 100, EnumWavelengths.UV)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_am241), 91 - 16 * i)) //ditto
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.PLUTONIUM241.ordinal()), 2 + 5 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.NEPTUNIUM.ordinal()), 2 + 3 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.NEPTUNIUM.ordinal()), 2 + 1 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.AMERICIUM242.ordinal()), 1 + 5 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_coal_tiny), 2 + 2 * i)) );
		}
		for(int i = 0; i < 5; i++) {
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_hea242, 1, i + 5), new SILEXRecipe(600, 100, EnumWavelengths.UV)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_am241), 91 - 16 * i)) //ditto
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.PLUTONIUM241.ordinal()), 2 + 5 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.NEPTUNIUM.ordinal()), 2 + 3 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_long_tiny, 1, ItemWasteLong.WasteClass.NEPTUNIUM.ordinal()), 2 + 1 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_short_tiny, 1, ItemWasteShort.WasteClass.AMERICIUM242.ordinal()), 1 + 5 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_xe135_tiny), 2 + 2 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			// BALEFIRE //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_balefire, 1, i), new SILEXRecipe(400, 100, EnumWavelengths.GAMMA)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_balefire), 90 - i * 20))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_tiny), 10 + 20 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_balefire, 1, i + 5), new SILEXRecipe(400, 100, EnumWavelengths.GAMMA)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_xe135_tiny), 3))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_balefire), 87 - i * 20))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_tiny), 10 + 20 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			// FLASHGOLD //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_balefire_gold, 1, i), new SILEXRecipe(600, 100, EnumWavelengths.XRAY)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_au198), 90 - 15 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_balefire), 10 + 15 * i)) 
					);
		}
		for(int i = 0; i < 5; i++) {	
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_balefire_gold, 1, i + 5), new SILEXRecipe(600, 100, EnumWavelengths.XRAY)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_au198), 80 - 15 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_balefire), 10 + 15 * i)) 
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_xe135_tiny), 10))
					);
		}
		for(int i = 0; i < 5; i++) {
			// FLASHLEAD //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_flashlead, 1, i), new SILEXRecipe(600, 100, EnumWavelengths.XRAY)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_au198), 44 - 10 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pb209), 44 - 10 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_bismuth), 1 + 6 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.ingot_mercury), 1 + 6 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_gh336), 10 + 8 * i)) ); //Reimumunch
		}
		for(int i = 0; i < 5; i++) {
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_flashlead, 1, i + 5), new SILEXRecipe(600, 100, EnumWavelengths.XRAY)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_xe135_tiny), 1))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pb209), 8 - 1 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_au198), 8 - 1 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_balefire), 1 + 2 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			// POBE //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_po210be, 1, i), new SILEXRecipe(600, 100, EnumWavelengths.IR)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_polonium), 45 - 10 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_beryllium), 45 - 10 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_lead), 5 + 10 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_coal_tiny), 5 + 10 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_po210be, 1, i + 5), new SILEXRecipe(600, 100, EnumWavelengths.IR)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_xe135_tiny), 3))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_polonium), 43 - 10 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_beryllium), 44 - 10 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_lead), 5 + 10 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_coal_tiny), 5 + 10 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			// PUBE //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_pu238be, 1, i), new SILEXRecipe(600, 100, EnumWavelengths.VISIBLE)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu238), 45 - 10 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_beryllium), 45 - 10 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_lead), 3 + 5 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_tiny), 2 + 5 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_coal_tiny), 5 + 10 * i)) );
		}
		for(int i = 0; i < 5; i++) {	
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_pu238be, 1, i + 5), new SILEXRecipe(600, 100, EnumWavelengths.VISIBLE)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_xe135_tiny), 3))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu238), 42 - 10 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_beryllium), 45 - 10 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_lead), 3 + 5 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_tiny), 2 + 5 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_coal_tiny), 5 + 10 * i)) );
		}
		for(int i = 0; i < 5; i++) {
			// RABE //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_ra226be, 1, i), new SILEXRecipe(600, 100, EnumWavelengths.MICRO)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_ra226), 45 - 10 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_beryllium), 45 - 10 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_lead), 3 + 5 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_polonium), 2 + 5 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_coal_tiny), 5 + 10 * i)) );
		}
		for(int i = 0; i < 5; i++) {
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_ra226be, 1, i + 5), new SILEXRecipe(600, 100, EnumWavelengths.MICRO)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_xe135_tiny), 3))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_ra226), 44 - 10 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_beryllium), 43 - 10 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_lead), 3 + 5 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_polonium), 2 + 5 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_coal_tiny), 5 + 10 * i)) );
		}
		for(int i = 0; i < 5; i++) {
			// FLASHGOLD //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_drx, 1, i), new SILEXRecipe(600, 100, EnumWavelengths.GAMMA)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.undefined), 1)));
		}
		for(int i = 0; i < 5; i++) {
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_drx, 1, i + 5), new SILEXRecipe(600, 100, EnumWavelengths.GAMMA)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.undefined), 1)));
		}
		for(int i = 0; i < 5; i++) {
			// ZFB BI //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_zfb_bismuth, 1, i), new SILEXRecipe(600, 100, EnumWavelengths.VISIBLE)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_uranium), 50 - i * 10))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu241), 50 - i * 10))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_bismuth), 50 + i * 20))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_zirconium), 150)) );
		}
		for(int i = 0; i < 5; i++) {
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_zfb_bismuth, 1, i + 5), new SILEXRecipe(600, 100, EnumWavelengths.VISIBLE)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_xe135_tiny), 3))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_uranium), 50 - i * 10))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu241), 50 - i * 10))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_bismuth), 50 + i * 20))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_zirconium), 147)) );
		}
		for(int i = 0; i < 5; i++) {
			// ZFB PU-241 //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_zfb_pu241, 1, i), new SILEXRecipe(600, 100, EnumWavelengths.UV)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_u235), 50 - i * 10))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu240), 50 - i * 10))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu241), 50 + i * 20))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_zirconium), 150)) );
		}
		for(int i = 0; i < 5; i++) {
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_zfb_pu241, 1, i + 5), new SILEXRecipe(600, 100, EnumWavelengths.UV)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_xe135_tiny), 3))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_u235), 50 - i * 10))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu240), 50 - i * 10))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu241), 50 + i * 20))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_zirconium), 147)) );
		}
		for(int i = 0; i < 5; i++) {
			// ZFB RG-AM //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_zfb_am_mix, 1, i), new SILEXRecipe(600, 100, EnumWavelengths.XRAY)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu241), 100 - i * 20))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_am_mix), 50 + i * 20))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_zirconium), 150)) );
		}
		for(int i = 0; i < 5; i++) {
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_zfb_am_mix, 1, i + 5), new SILEXRecipe(600, 100, EnumWavelengths.XRAY)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_xe135_tiny), 3))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu241), 100 - i * 20))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_am_mix), 50 + i * 20))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_zirconium), 147)) );
		}
		for(int i = 0; i < 5; i++) {
			// Unobtainium //
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_unobtainium, 1, i), new SILEXRecipe(600, 100, EnumWavelengths.XRAY)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_unobtainium_greater), 90 - 18 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_unobtainium_lesser), 8 + i * 10))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_radspice), 2 + i * 4)) 
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_gh336), i * 4))
					);
		}
		for(int i = 0; i < 5; i++) {
			recipes.put(new ComparableStack(ModItems.rbmk_pellet_unobtainium, 1, i + 5), new SILEXRecipe(600, 100, EnumWavelengths.XRAY)
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_unobtainium_greater), 87 - 18 * i))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_unobtainium_lesser), 8 + i * 10))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_radspice), 2 + i * 4)) 
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_gh336), i * 4))
					.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_xe135_tiny), 3))
					);
		}

		recipes.put(new ComparableStack(ModItems.nuclear_waste_long, 1, ItemWasteLong.WasteClass.URANIUM235.ordinal()), new SILEXRecipe(900, 100, EnumWavelengths.MICRO)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_neptunium), 20))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu239), 45))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu240), 20))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_technetium), 15))
				);
		recipes.put(new ComparableStack(ModItems.nuclear_waste_long_depleted, 1, ItemWasteLong.WasteClass.URANIUM235.ordinal()), new SILEXRecipe(900, 100, EnumWavelengths.MICRO)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_lead), 65))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_bismuth), 20))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.dust), 15))
				);
		recipes.put(new ComparableStack(ModItems.nuclear_waste_short, 1, ItemWasteShort.WasteClass.URANIUM235.ordinal()), new SILEXRecipe(900, 100, EnumWavelengths.MICRO)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu238), 12))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_sr90_tiny), 10))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_i131_tiny), 10))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_cs137_tiny), 12))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_tiny), 56))
				);
		recipes.put(new ComparableStack(ModItems.nuclear_waste_short_depleted, 1, ItemWasteShort.WasteClass.URANIUM235.ordinal()), new SILEXRecipe(900, 100, EnumWavelengths.MICRO)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_zirconium), 10))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.dust), 32))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_lead), 22))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_u238), 5))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_bismuth), 15))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_tiny), 16))
				);

		recipes.put(new ComparableStack(ModItems.nuclear_waste_long, 1, ItemWasteLong.WasteClass.URANIUM233.ordinal()), new SILEXRecipe(900, 100, EnumWavelengths.RADIO)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_u235), 15))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_neptunium), 25))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu239), 45))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_technetium), 15))
				);
		recipes.put(new ComparableStack(ModItems.nuclear_waste_long_depleted, 1, ItemWasteLong.WasteClass.URANIUM233.ordinal()), new SILEXRecipe(900, 100, EnumWavelengths.RADIO)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_lead), 60))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_bismuth), 25))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.dust), 15))
				);
		recipes.put(new ComparableStack(ModItems.nuclear_waste_short, 1, ItemWasteShort.WasteClass.URANIUM233.ordinal()), new SILEXRecipe(900, 100, EnumWavelengths.RADIO)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu238), 4))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_sr90_tiny), 12))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_i131_tiny), 10))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_cs137_tiny), 14))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_tiny), 60))
				);
		recipes.put(new ComparableStack(ModItems.nuclear_waste_short_depleted, 1, ItemWasteShort.WasteClass.URANIUM233.ordinal()), new SILEXRecipe(900, 100, EnumWavelengths.RADIO)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_zirconium), 12))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.dust), 34))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_lead), 13))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_u238), 2))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_bismuth), 10))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_tiny), 29))
				);
		
		recipes.put(new ComparableStack(ModItems.nuclear_waste_short, 1, ItemWasteShort.WasteClass.PLUTONIUM239.ordinal()), new SILEXRecipe(900, 100, EnumWavelengths.IR)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu240), 10))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu241), 25))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_sr90_tiny), 2))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_i131_tiny), 5))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_cs137_tiny), 6))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_tiny), 52))
				);
		recipes.put(new ComparableStack(ModItems.nuclear_waste_short_depleted, 1, ItemWasteShort.WasteClass.PLUTONIUM239.ordinal()), new SILEXRecipe(900, 100, EnumWavelengths.IR)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_zirconium), 2))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.dust), 16))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_lead), 40))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_u238), 3))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_tiny), 39))
				);
		
		recipes.put(new ComparableStack(ModItems.nuclear_waste_short, 1, ItemWasteShort.WasteClass.PLUTONIUM240.ordinal()), new SILEXRecipe(900, 100, EnumWavelengths.IR)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu241), 15))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_neptunium), 5))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_sr90_tiny), 2))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_i131_tiny), 5))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_cs137_tiny), 7))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_tiny), 66))
				);
		recipes.put(new ComparableStack(ModItems.nuclear_waste_short_depleted, 1, ItemWasteShort.WasteClass.PLUTONIUM240.ordinal()), new SILEXRecipe(900, 100, EnumWavelengths.IR)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_zirconium), 2))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.dust), 22))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_bismuth), 20))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_lead), 17))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_u238), 3))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_tiny), 36))
				);
		
		recipes.put(new ComparableStack(ModItems.nuclear_waste_short, 1, ItemWasteShort.WasteClass.PLUTONIUM241.ordinal()), new SILEXRecipe(900, 100, EnumWavelengths.XRAY)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_am241), 25))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_am242), 35))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_technetium), 5))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_i131_tiny), 3))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_cs137_tiny), 7))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_tiny), 25))
				);
		recipes.put(new ComparableStack(ModItems.nuclear_waste_short_depleted, 1, ItemWasteShort.WasteClass.PLUTONIUM241.ordinal()), new SILEXRecipe(900, 100, EnumWavelengths.XRAY)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_bismuth), 60))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.dust), 20))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_lead), 15))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_tiny), 5))
				);

		recipes.put(new ComparableStack(ModItems.nuclear_waste_long, 1, ItemWasteLong.WasteClass.THORIUM.ordinal()), new SILEXRecipe(900, 100, EnumWavelengths.RADIO)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_u233), 40))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_u235), 35))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_tiny), 25))
				);
		recipes.put(new ComparableStack(ModItems.nuclear_waste_long_depleted, 1, ItemWasteLong.WasteClass.THORIUM.ordinal()), new SILEXRecipe(900, 100, EnumWavelengths.RADIO)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_lead), 35))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_bismuth), 40))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.dust), 15))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_tiny), 10))
				);

		recipes.put(new ComparableStack(ModItems.nuclear_waste_long, 1, ItemWasteLong.WasteClass.NEPTUNIUM.ordinal()), new SILEXRecipe(900, 100, EnumWavelengths.UV)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_u238), 15))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu239), 40))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu240), 15))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_technetium), 15))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_tiny), 15))
				);
		recipes.put(new ComparableStack(ModItems.nuclear_waste_long_depleted, 1, ItemWasteLong.WasteClass.NEPTUNIUM.ordinal()), new SILEXRecipe(900, 100, EnumWavelengths.UV)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_u238), 16))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_lead), 55))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.dust), 20))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_tiny), 9))
				);
		recipes.put(new ComparableStack(ModItems.nuclear_waste_short, 1, ItemWasteShort.WasteClass.NEPTUNIUM.ordinal()), new SILEXRecipe(900, 100, EnumWavelengths.UV)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu238), 40))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_sr90_tiny), 7))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_i131_tiny), 5))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_cs137_tiny), 8))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_tiny), 40))
				);
		recipes.put(new ComparableStack(ModItems.nuclear_waste_short_depleted, 1, ItemWasteShort.WasteClass.NEPTUNIUM.ordinal()), new SILEXRecipe(900, 100, EnumWavelengths.UV)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_zirconium), 7))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.dust), 29))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_u238), 2))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_lead), 45))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_tiny), 17))
				);

		recipes.put(new ComparableStack(ModItems.nuclear_waste_short, 1, ItemWasteShort.WasteClass.AMERICIUM242.ordinal()), new SILEXRecipe(900, 100, EnumWavelengths.XRAY)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_am242), 45))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pu241), 15))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_technetium), 5))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_i131_tiny), 8))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_co60_tiny), 2))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_tiny), 25))
				);

		recipes.put(new ComparableStack(ModItems.nuclear_waste_short_depleted, 1, ItemWasteShort.WasteClass.AMERICIUM242.ordinal()), new SILEXRecipe(900, 100, EnumWavelengths.XRAY)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_bismuth), 55))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_sr90_tiny), 8))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.dust), 20))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_lead), 12))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_tiny), 5))
				);

		recipes.put(new ComparableStack(ModItems.nuclear_waste_long, 1, ItemWasteLong.WasteClass.SCHRABIDIUM.ordinal()), new SILEXRecipe(900, 100, EnumWavelengths.UV)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_solinium), 25))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_euphemium), 18))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_gh336), 16))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_tantalium), 8))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_neodymium_tiny), 8))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_tiny), 25))
				);
		recipes.put(new ComparableStack(ModItems.nuclear_waste_long_depleted, 1, ItemWasteLong.WasteClass.SCHRABIDIUM.ordinal()), new SILEXRecipe(900, 100, EnumWavelengths.UV)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_solinium), 20))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_euphemium), 18))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_gh336), 15))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_tantalium), 8))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_neodymium_tiny), 8))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_tiny), 31))
				);
		recipes.put(new ComparableStack(ModItems.nuclear_waste_short, 1, ItemWasteShort.WasteClass.SCHRABIDIUM.ordinal()), new SILEXRecipe(900, 100, EnumWavelengths.VISIBLE)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_pb209), 7)) //We don't have any spicy lanthanides, and lead 209 + gold 198 is already *severely* pushing it, but there's no
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_au198), 7)) //point in contributing to pointless item bloat, so this will have to do
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_cs137_tiny), 5)) 
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_i131_tiny), 5))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_tiny), 76))
				);
		recipes.put(new ComparableStack(ModItems.nuclear_waste_short_depleted, 1, ItemWasteShort.WasteClass.SCHRABIDIUM.ordinal()), new SILEXRecipe(900, 100, EnumWavelengths.VISIBLE)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_bismuth), 7))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.ingot_mercury), 12))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_cerium_tiny), 14))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_lanthanium_tiny), 15))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.dust), 20))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_tiny), 32))
				);
		
		recipes.put(new ComparableStack(ModItems.fallout, 1), new SILEXRecipe(50, 100, EnumWavelengths.UV)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.dust), 90))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_co60), 2))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_sr90_tiny), 3))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_i131_tiny), 1))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_cs137_tiny), 3))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_au198), 1))
				);
		
		recipes.put(new ComparableStack(Blocks.GRAVEL, 1), new SILEXRecipe(1000, 250, EnumWavelengths.UV)
				.addOut(new WeightedRandomObject(new ItemStack(Items.FLINT), 80))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_boron), 5))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_lithium), 10))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.fluorite), 5))
				);

		recipes.put(new ComparableStack(Blocks.SAND, 1), new SILEXRecipe(100, 400, EnumWavelengths.XRAY)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_quartz), 85))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.niter), 10))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_lithium_tiny), 5))
				);

		recipes.put(new ComparableStack(ModItems.trinitite, 1), new SILEXRecipe(100, 640, EnumWavelengths.MICRO)
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.SAND), 81))
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.GRAVEL), 70))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_tiny), 30))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.fallout), 12))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_sr90_tiny), 4))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_i131_tiny), 3))
				);
		recipes.put(new ComparableStack(ModBlocks.waste_earth, 1), new SILEXRecipe(100, 640, EnumWavelengths.VISIBLE)
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.DIRT), 54))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_tiny), 30))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.fallout), 10))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_cs137_tiny), 5))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_xe135_tiny), 1))
				);
		recipes.put(new ComparableStack(ModBlocks.waste_dirt, 1), new SILEXRecipe(100, 640, EnumWavelengths.UV)
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.DIRT), 111))
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.GRAVEL), 50))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_tiny), 35))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_lead), 3))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_ra226), 1))
				);
		recipes.put(new ComparableStack(ModBlocks.waste_mycelium, 1), new SILEXRecipe(1000, 640, EnumWavelengths.XRAY)
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.DIRT), 100))
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.GRAVEL), 50))
				.addOut(new WeightedRandomObject(new ItemStack(ModBlocks.mush), 35))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nuclear_waste_tiny), 15))
				);
		recipes.put(new ComparableStack(ModBlocks.mush_block, 1), new SILEXRecipe(100, 640, EnumWavelengths.DRX)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.biomass_compressed), 110))
				.addOut(new WeightedRandomObject(new ItemStack(ModBlocks.mush), 80))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_poison), 6))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_radspice), 3))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.egg_balefire_shard), 1))
				);
		recipes.put(new ComparableStack(ModBlocks.ancient_scrap, 1), new SILEXRecipe(1000, 640, EnumWavelengths.DRX)
				.addOut(new WeightedRandomObject(new ItemStack(ModBlocks.block_electrical_scrap), 720))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.undefined), 20))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.chlorine_pinwheel), 2))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.ingot_electronium), 1))
				);


		//Crystals orders by Tier
		recipes.put(IRON.crystal(), new SILEXRecipe(900, 100, EnumWavelengths.RADIO)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_iron), 64))
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.GRAVEL), 24))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_titanium), 12))
				);
		recipes.put(COAL.crystal(), new SILEXRecipe(900, 100, EnumWavelengths.RADIO)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_coal), 70))
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.GRAVEL), 20))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_lignite), 10))
				);
		recipes.put(GOLD.crystal(), new SILEXRecipe(900, 100, EnumWavelengths.RADIO)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_gold), 64))
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.GRAVEL), 24))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.ingot_mercury), 12))
				);
		recipes.put(LAPIS.crystal(), new SILEXRecipe(900, 100, EnumWavelengths.RADIO)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_lapis), 64))
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.GRAVEL), 24))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_cobalt_tiny), 12))
				);
		recipes.put(REDSTONE.crystal(), new SILEXRecipe(900, 100, EnumWavelengths.RADIO)
				.addOut(new WeightedRandomObject(new ItemStack(Items.REDSTONE), 64))
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.GRAVEL), 12))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.bottle_mercury), 24))
				);


		recipes.put(CU.crystal(), new SILEXRecipe(900, 100, EnumWavelengths.MICRO)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_copper), 64))
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.GRAVEL), 24))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.sulfur), 12))
				);
		recipes.put(PB.crystal(), new SILEXRecipe(900, 100, EnumWavelengths.MICRO)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_lead), 64))
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.GRAVEL), 24))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_gold), 12))
				);
		recipes.put(TI.crystal(), new SILEXRecipe(900, 100, EnumWavelengths.MICRO)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_titanium), 64))
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.GRAVEL), 24))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_iron), 12))
				);
		recipes.put(W.crystal(), new SILEXRecipe(900, 100, EnumWavelengths.MICRO)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_tungsten), 64))
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.GRAVEL), 24))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_iron), 12))
				);
		recipes.put(AL.crystal(), new SILEXRecipe(900, 100, EnumWavelengths.MICRO)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_aluminium), 64))
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.GRAVEL), 24))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_iron), 12))
				);


		recipes.put(LI.crystal(), new SILEXRecipe(900, 100, EnumWavelengths.IR)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_lithium), 64))
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.GRAVEL), 24))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_quartz), 12))
				);
		recipes.put(S.crystal(), new SILEXRecipe(900, 100, EnumWavelengths.IR)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.sulfur), 64))
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.GRAVEL), 24))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.ingot_mercury), 12))
				);
		recipes.put(F.crystal(), new SILEXRecipe(900, 100, EnumWavelengths.IR)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.fluorite), 64))
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.GRAVEL), 24))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_lithium_tiny), 12))
				);
		recipes.put(KNO.crystal(), new SILEXRecipe(900, 100, EnumWavelengths.IR)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.niter), 64))
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.GRAVEL), 24))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_lithium), 12))
				);


		recipes.put(CO.crystal(), new SILEXRecipe(900, 100, EnumWavelengths.VISIBLE)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_cobalt), 75))
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.GRAVEL), 16))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_copper), 9))
				);
		recipes.put(BE.crystal(), new SILEXRecipe(900, 100, EnumWavelengths.VISIBLE)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_beryllium), 64))
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.GRAVEL), 24))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_emerald), 12))
				);
		recipes.put(P_RED.crystal(), new SILEXRecipe(900, 100, EnumWavelengths.VISIBLE)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_fire), 66))
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.GRAVEL), 27))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.ingot_phosphorus), 7))
				);
		recipes.put(CINNABAR.crystal(), new SILEXRecipe(900, 100, EnumWavelengths.VISIBLE)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.cinnabar), 64))
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.GRAVEL), 24))
				.addOut(new WeightedRandomObject(new ItemStack(Items.REDSTONE), 12))
				);
		recipes.put(U.crystal(), new SILEXRecipe(900, 100, EnumWavelengths.VISIBLE)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_uranium), 65))
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.GRAVEL), 14))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_ra226), 21))
				);
		recipes.put(ASBESTOS.crystal(), new SILEXRecipe(900, 100, EnumWavelengths.VISIBLE)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_asbestos), 64))
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.GRAVEL), 24))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_boron_tiny), 12))
				);


		recipes.put(TH232.crystal(), new SILEXRecipe(900, 100, EnumWavelengths.UV)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_thorium), 64))
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.GRAVEL), 24))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_uranium), 12))
				);
		recipes.put(DIAMOND.crystal(), new SILEXRecipe(900, 100, EnumWavelengths.UV)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_diamond), 64))
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.GRAVEL), 24))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_meteorite_tiny), 12))
				);
		recipes.put(PU.crystal(), new SILEXRecipe(900, 100, EnumWavelengths.UV)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_plutonium), 67))
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.GRAVEL), 22))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_polonium), 11))
				);


		recipes.put(SA326.crystal(), new SILEXRecipe(900, 100, EnumWavelengths.XRAY)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_schrabidium), 64))
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.GRAVEL), 24))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_plutonium), 12))
				);
		recipes.put(RAREEARTH.gem(), new SILEXRecipe(600, 100, EnumWavelengths.XRAY)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_boron_tiny), 15))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_cobalt_tiny), 15))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_neodymium_tiny), 5))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_niobium_tiny), 5))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_cerium_tiny), 4))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_lanthanium_tiny), 2))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_actinium_tiny), 2))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_lithium_tiny), 2))
				);
		recipes.put(RAREEARTH.crystal(), new SILEXRecipe(300, 100, EnumWavelengths.XRAY)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_boron), 15))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_cobalt), 15))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_neodymium), 5))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_niobium), 5))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_cerium), 4))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_lanthanium), 2))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_actinium), 2))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_lithium), 2))
				);
		recipes.put(new OreDictStack(SRN.crystal()), new SILEXRecipe(900, 100, EnumWavelengths.UV)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_schrabidium), 5))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_uranium), 2))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_plutonium), 2))
				);
		recipes.put(new ComparableStack(ModItems.crystal_trixite), new SILEXRecipe(1200, 100, EnumWavelengths.XRAY)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_plutonium), 42))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_cobalt), 34))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_spark_mix), 6))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_nitan_mix), 18))
				);
		recipes.put(STAR.crystal(), new SILEXRecipe(900, 100, EnumWavelengths.XRAY)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_astatine), 20))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_dura_steel), 24))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.powder_cobalt), 12))
				.addOut(new WeightedRandomObject(new ItemStack(Blocks.GRAVEL), 12))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.ingot_mercury), 32))
				);
		recipes.put(OSMIRIDIUM.crystal(), new SILEXRecipe(900, 100, EnumWavelengths.GAMMA)
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_osmiridium), 90))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_schrabidium), 8))
				.addOut(new WeightedRandomObject(new ItemStack(ModItems.nugget_bismuth), 2))
				);
		
		//crystals minerals
	}

	public static void addRecipe(int wavelength, int solution, int consumption, ItemStack input, ItemStack[] outputItems, int[] outputWeights){
		SILEXRecipe newRecipe = new SILEXRecipe(solution, consumption, EnumWavelengths.values()[wavelength]);
		for(int i = 0; i < outputItems.length; i++){
			newRecipe = newRecipe.addOut(new WeightedRandomObject(outputItems[i], outputWeights[i]));
		}
		recipes.put(new ComparableStack(input), newRecipe);
	}

	public static void removeRecipe(ItemStack input){
		recipes.remove(new ComparableStack(input));
	}
	
	public static SILEXRecipe getOutput(ItemStack stack) {
		
		if(stack == null || stack.getItem() == null)
			return null;
		
		ComparableStack comp = translateItem(stack);
		
		if(recipes.containsKey(comp))
			return recipes.get(comp);
		
		String[] dictKeys = comp.getDictKeys();
		
		for(String key : dictKeys) {
			
			String translation = translateDict(key);
			if(recipes.containsKey(translation))
				return recipes.get(translation);
		}
		
		return null;
	}
	
	public static ComparableStack translateItem(ItemStack stack) {
		ComparableStack orig = new ComparableStack(stack.getItem(), 1, stack.getItemDamage());
		ComparableStack translation = itemTranslation.get(orig);
		
		if(translation != null)
			return translation;
		
		return orig;
	}
	
	public static String translateDict(String key) {
		
		String translation = dictTranslation.get(key);
		
		if(translation != null)
			return translation;
		
		return key;
	}
	
	public static List<Object> getAllIngredients() {
		List<Object> ing = new ArrayList<>();
		
		for(Entry<Object, SILEXRecipe> entry : SILEXRecipes.recipes.entrySet()) {
			ing.add(entry.getKey());
		}
		for(Entry<ComparableStack, ComparableStack> entry : SILEXRecipes.itemTranslation.entrySet()) {
			ing.add(entry.getKey());
		}
		for(Entry<String, String> entry : SILEXRecipes.dictTranslation.entrySet()) {
			ing.add(entry.getKey());
		}
		
		return ing;
	}

	public static Map<List<ItemStack>, SILEXRecipe> getRecipes() {
		
		Map<List<ItemStack>, SILEXRecipe> recipes = new LinkedHashMap<>();
		List<Object> ing = getAllIngredients();
		
		for(Object ingredient : ing) {
			
			if(ingredient instanceof String) {
				List<ItemStack> ingredients = OreDictionary.getOres((String)ingredient);
				if(ingredients.size() > 0) {
					SILEXRecipe output = getOutput(ingredients.get(0));
					if(output != null)
						recipes.put(ingredients, output);
				}
				
			} else if(ingredient instanceof ComparableStack) {
				SILEXRecipe output = getOutput(((ComparableStack) ingredient).toStack());
				List<ItemStack> ingredients = new ArrayList<>(1);
				if(output != null){
					ingredients.add(((ComparableStack)ingredient).toStack());
					recipes.put(ingredients, output);
				}
			}
		}
		
		return recipes;
	}
	
	public static class SILEXRecipe {
		
		public int fluidProduced;
		public int fluidConsumed;
		public EnumWavelengths laserStrength;
		public List<WeightedRandomObject> outputs = new ArrayList<>();
		
		public SILEXRecipe(int fluidProduced, int fluidConsumed, EnumWavelengths laserStrength) {
			this.fluidProduced = fluidProduced;
			this.fluidConsumed = fluidConsumed;
			this.laserStrength = laserStrength;
		}
		
		public SILEXRecipe addOut(WeightedRandomObject entry) {
			outputs.add(entry);
			return this;
		}
	}
}