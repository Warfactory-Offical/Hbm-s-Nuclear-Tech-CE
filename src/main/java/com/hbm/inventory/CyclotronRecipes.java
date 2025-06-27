package com.hbm.inventory;

import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.NbtComparableStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemCell;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.hbm.inventory.OreDictManager.*;

public class CyclotronRecipes {

	private static LinkedHashMap<Object, ItemStack> lithium = new LinkedHashMap<>();
	private static LinkedHashMap<Object, ItemStack> beryllium = new LinkedHashMap<>();
	private static LinkedHashMap<Object, ItemStack> carbon = new LinkedHashMap<>();
	private static LinkedHashMap<Object, ItemStack> copper = new LinkedHashMap<>();
	private static LinkedHashMap<Object, ItemStack> plutonium = new LinkedHashMap<>();
	private static HashMap<Object, Integer> liAmat = new HashMap<>();
	private static HashMap<Object, Integer> beAmat = new HashMap<>();
	private static HashMap<Object, Integer> caAmat = new HashMap<>();
	private static HashMap<Object, Integer> coAmat = new HashMap<>();
	private static HashMap<Object, Integer> plAmat = new HashMap<>();

	public static void register() {

		/// LITHIUM START ///
		int liA = 50;

		makeRecipe(lithium, liAmat, LI.dust(), new ItemStack(ModItems.powder_beryllium), liA);
		makeRecipe(lithium, liAmat, BE.dust(), new ItemStack(ModItems.powder_boron), liA);
		makeRecipe(lithium, liAmat, B.dust(), new ItemStack(ModItems.powder_coal), liA);
		makeRecipe(lithium, liAmat, NETHERQUARTZ.dust(), new ItemStack(ModItems.powder_fire), liA);
		makeRecipe(lithium, liAmat, P_RED.dust(), new ItemStack(ModItems.sulfur), liA);
		makeRecipe(lithium, liAmat, IRON.dust(), new ItemStack(ModItems.powder_cobalt), liA);
		makeRecipe(lithium, liAmat, ST.dust(), new ItemStack(ModItems.powder_zirconium), liA);
		makeRecipe(lithium, liAmat, GOLD.dust(), new ItemStack(ModItems.bottle_mercury), liA);
		makeRecipe(lithium, liAmat, PO210.dust(), new ItemStack(ModItems.powder_astatine), liA);
		makeRecipe(lithium, liAmat, LA.dust(), new ItemStack(ModItems.powder_cerium), liA);
		makeRecipe(lithium, liAmat, AC.dust(), new ItemStack(ModItems.powder_thorium), liA);
		makeRecipe(lithium, liAmat, U.dust(), new ItemStack(ModItems.powder_neptunium), liA);
		makeRecipe(lithium, liAmat, NP237.dust(), new ItemStack(ModItems.powder_plutonium), liA);
		makeRecipe(lithium, liAmat, REIIUM.dust(), new ItemStack(ModItems.powder_weidanium), 150);
		/// LITHIUM END ///

		/// BERYLLIUM START ///
		int beA = 25;

		makeRecipe(beryllium, beAmat, LI.dust(), new ItemStack(ModItems.powder_boron), beA);
		makeRecipe(beryllium, beAmat, NETHERQUARTZ.dust(), new ItemStack(ModItems.sulfur), beA);
		makeRecipe(beryllium, beAmat, TI.dust(), new ItemStack(ModItems.powder_iron), beA);
		makeRecipe(beryllium, beAmat, CO.dust(), new ItemStack(ModItems.powder_copper), beA);
		makeRecipe(beryllium, beAmat, ST.dust(), new ItemStack(ModItems.powder_niobium), beA);
		makeRecipe(beryllium, beAmat, CE.dust(), new ItemStack(ModItems.powder_neodymium), beA);
		makeRecipe(beryllium, beAmat, TH232.dust(), new ItemStack(ModItems.powder_uranium), beA);
		makeRecipe(beryllium, beAmat, WEIDANIUM.dust(), new ItemStack(ModItems.powder_australium), 100);
		/// BERYLLIUM END ///

		/// CARBON START ///
		int caA = 10;

		makeRecipe(carbon, caAmat, B.dust(), new ItemStack(ModItems.powder_aluminium), caA);
		makeRecipe(carbon, caAmat, S.dust(), new ItemStack(ModItems.powder_titanium), caA);
		makeRecipe(carbon, caAmat, TI.dust(), new ItemStack(ModItems.powder_cobalt), caA);
		makeRecipe(carbon, caAmat, CS.dust(), new ItemStack(ModItems.powder_lanthanium), caA);
		makeRecipe(carbon, caAmat, ND.dust(), new ItemStack(ModItems.powder_gold), caA);
		makeRecipe(carbon, caAmat, new ComparableStack(ModItems.bottle_mercury), new ItemStack(ModItems.powder_polonium), caA);
		makeRecipe(carbon, caAmat, PB.dust(), new ItemStack(ModItems.powder_ra226), caA);
		makeRecipe(carbon, caAmat, AT.dust(), new ItemStack(ModItems.powder_actinium), caA);
		/// CARBON END ///

		/// COPPER START ///
		int coA = 15;

		makeRecipe(copper, coAmat, BE.dust(), new ItemStack(ModItems.powder_quartz), coA);
		makeRecipe(copper, coAmat, COAL.dust(), new ItemStack(ModItems.powder_bromine), coA);
		makeRecipe(copper, coAmat, TI.dust(), new ItemStack(ModItems.powder_strontium), coA);
		makeRecipe(copper, coAmat, IRON.dust(), new ItemStack(ModItems.powder_niobium), coA);
		makeRecipe(copper, coAmat, BR.dust(), new ItemStack(ModItems.powder_iodine), coA);
		makeRecipe(copper, coAmat, ST.dust(), new ItemStack(ModItems.powder_neodymium), coA);
		makeRecipe(copper, coAmat, NB.dust(), new ItemStack(ModItems.powder_caesium), coA);
		makeRecipe(copper, coAmat, I.dust(), new ItemStack(ModItems.powder_polonium), coA);
		makeRecipe(copper, coAmat, CS.dust(), new ItemStack(ModItems.powder_actinium), coA);
		makeRecipe(copper, coAmat, GOLD.dust(), new ItemStack(ModItems.powder_uranium), coA);
		/// COPPER END ///

		/// PLUTONIUM START ///
		int plA = 100;

		makeRecipe(plutonium, plAmat, PU.dust(), new ItemStack(ModItems.powder_tennessine), plA);
		makeRecipe(plutonium, plAmat, new ComparableStack(ModItems.powder_tennessine), new ItemStack(ModItems.powder_reiium), plA);
		makeRecipe(plutonium, plAmat, new ComparableStack(ModItems.pellet_charged), new ItemStack(ModItems.nugget_schrabidium, 6), 200);
		makeRecipe(plutonium, plAmat, new ComparableStack(ItemCell.getFullCell(Fluids.AMAT)), ItemCell.getFullCell(Fluids.ASCHRAB), 0);
		/// PLUTONIUM END ///

		///TODO: fictional elements
	}
	
	private static void makeRecipe(HashMap<Object, ItemStack> map, HashMap<Object, Integer> aMap, Object in, ItemStack out, int amat) {
		map.put(in, out);
		aMap.put(in, amat);
	}

	public static Object[] getOutput(ItemStack stack, ItemStack box) {

		if(stack == null || stack.getItem() == null || box == null || stack.isEmpty() || box.isEmpty())
			return null;

		HashMap<Object, ItemStack> pool = null;
		HashMap<Object, Integer> aPool = null;

		if(box.getItem() == ModItems.part_lithium) {
			pool = lithium;
			aPool = liAmat;
		} else if(box.getItem() == ModItems.part_beryllium) {
			pool = beryllium;
			aPool = beAmat;
		} else if(box.getItem() == ModItems.part_carbon) {
			pool = carbon;
			aPool = caAmat;
		} else if(box.getItem() == ModItems.part_copper) {
			pool = copper;
			aPool = coAmat;
		} else if(box.getItem() == ModItems.part_plutonium) {
			pool = plutonium;
			aPool = plAmat;
		}

		if(pool == null)
			return null;

		ComparableStack comp = stack.hasTagCompound() ? new NbtComparableStack(stack).makeSingular() : new ComparableStack(stack.getItem(), 1, stack.getItemDamage());
		
		if(pool.containsKey(comp))
			return new Object[] {pool.get(comp).copy(), aPool.get(comp)};

		String[] dictKeys = comp.getDictKeys();

		for(String key : dictKeys) {

			if(pool.containsKey(key))
				return new Object[] {pool.get(key).copy(), aPool.get(key)};
		}

		return null;
	}
	
	public static Map<ItemStack[], ItemStack> getRecipes() {

		Map<ItemStack[], ItemStack> recipes = new LinkedHashMap<>();

		addRecipes(recipes, lithium, ModItems.part_lithium);
		addRecipes(recipes, beryllium, ModItems.part_beryllium);
		addRecipes(recipes, carbon, ModItems.part_carbon);
		addRecipes(recipes, copper, ModItems.part_copper);
		addRecipes(recipes, plutonium, ModItems.part_plutonium);

		return recipes;
	}

	private static void addRecipes(Map<ItemStack[], ItemStack> recipes, HashMap<Object, ItemStack> map, Item part) {

		for(Entry<Object, ItemStack> entry : map.entrySet()) {

			if(entry.getKey() instanceof ComparableStack) {

				recipes.put(new ItemStack[] { new ItemStack(part), ((ComparableStack) entry.getKey()).toStack() }, entry.getValue());

			} else if(entry.getKey() instanceof String) {

				List<ItemStack> ores = OreDictionary.getOres((String) entry.getKey());

				for(ItemStack ore : ores) {
					recipes.put(new ItemStack[] { new ItemStack(part), ore }, entry.getValue());
				}
			}
		}
	}
}
