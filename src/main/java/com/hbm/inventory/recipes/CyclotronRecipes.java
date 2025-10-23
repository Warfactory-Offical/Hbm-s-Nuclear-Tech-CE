package com.hbm.inventory.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ModItems;
import com.hbm.items.ModItems.Materials.Nuggies;
import com.hbm.items.ModItems.Materials.Powders;
import com.hbm.main.MainRegistry;
import com.hbm.util.Tuple;
import net.minecraft.item.ItemStack;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.hbm.inventory.OreDictManager.*;

public class CyclotronRecipes extends SerializableRecipe {

	public static HashMap<Tuple.Pair<ComparableStack, RecipesCommon.AStack>, Tuple.Pair<ItemStack, Integer>> recipes = new HashMap<>();

	@Override
	public void registerDefaults() {

		/// LITHIUM START ///
		int liA = 50;

		makeRecipe(new ComparableStack(ModItems.part_lithium), new RecipesCommon.OreDictStack("dustLithium"), new ItemStack(Powders.powder_beryllium), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium), new RecipesCommon.OreDictStack("dustBeryllium"), new ItemStack(Powders.powder_boron), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium), new RecipesCommon.OreDictStack("dustBoron"), new ItemStack(Powders.powder_coal), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium), new RecipesCommon.OreDictStack("dustNetherQuartz"), new ItemStack(Powders.powder_fire), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium), new RecipesCommon.OreDictStack("dustPhosphorus"), new ItemStack(Powders.sulfur), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium), new RecipesCommon.OreDictStack("dustIron"), new ItemStack(Powders.powder_cobalt), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium), new ComparableStack(Powders.powder_strontium), new ItemStack(Powders.powder_zirconium), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium), new RecipesCommon.OreDictStack("dustGold"), new ItemStack(Nuggies.ingot_mercury), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium), new RecipesCommon.OreDictStack("dustPolonium"), new ItemStack(Powders.powder_astatine), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium), new RecipesCommon.OreDictStack("dustLanthanium"), new ItemStack(Powders.powder_cerium), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium), new RecipesCommon.OreDictStack("dustActinium"), new ItemStack(Powders.powder_thorium), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium), new RecipesCommon.OreDictStack(U.dust()), new ItemStack(Powders.powder_neptunium), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium), new RecipesCommon.OreDictStack(NP237.dust()), new ItemStack(Powders.powder_plutonium), liA);
		/// LITHIUM END ///

		/// BERYLLIUM START ///
		int beA = 25;

		makeRecipe(new ComparableStack(ModItems.part_beryllium), new RecipesCommon.OreDictStack("dustLithium"), new ItemStack(Powders.powder_boron), beA);
		makeRecipe(new ComparableStack(ModItems.part_beryllium), new RecipesCommon.OreDictStack("dustNetherQuartz"), new ItemStack(Powders.sulfur), beA);
		makeRecipe(new ComparableStack(ModItems.part_beryllium), new RecipesCommon.OreDictStack("dustTitanium"), new ItemStack(Powders.powder_iron), beA);
		makeRecipe(new ComparableStack(ModItems.part_beryllium), new RecipesCommon.OreDictStack("dustCobalt"), new ItemStack(Powders.powder_copper), beA);
		makeRecipe(new ComparableStack(ModItems.part_beryllium), new ComparableStack(Powders.powder_strontium), new ItemStack(Powders.powder_niobium), beA);
		makeRecipe(new ComparableStack(ModItems.part_beryllium), new ComparableStack(Powders.powder_cerium), new ItemStack(Powders.powder_neodymium), beA);
		makeRecipe(new ComparableStack(ModItems.part_beryllium), new RecipesCommon.OreDictStack("dustThorium"), new ItemStack(Powders.powder_uranium), beA);
		/// BERYLLIUM END ///

		/// CARBON START ///
		int caA = 10;

		makeRecipe(new ComparableStack(ModItems.part_carbon), new RecipesCommon.OreDictStack("dustBoron"), new ItemStack(Powders.powder_aluminium), caA);
		makeRecipe(new ComparableStack(ModItems.part_carbon), new RecipesCommon.OreDictStack("dustSulfur"), new ItemStack(Powders.powder_titanium), caA);
		makeRecipe(new ComparableStack(ModItems.part_carbon), new RecipesCommon.OreDictStack("dustTitanium"), new ItemStack(Powders.powder_cobalt), caA);
		makeRecipe(new ComparableStack(ModItems.part_carbon), new ComparableStack(Powders.powder_caesium), new ItemStack(Powders.powder_lanthanium), caA);
		makeRecipe(new ComparableStack(ModItems.part_carbon), new ComparableStack(Powders.powder_neodymium), new ItemStack(Powders.powder_gold), caA);
		makeRecipe(new ComparableStack(ModItems.part_carbon), new ComparableStack(Nuggies.ingot_mercury), new ItemStack(Powders.powder_polonium), caA);
		makeRecipe(new ComparableStack(ModItems.part_carbon), new RecipesCommon.OreDictStack(PB.dust()), new ItemStack(Powders.powder_ra226),caA);
		makeRecipe(new ComparableStack(ModItems.part_carbon), new ComparableStack(Powders.powder_astatine), new ItemStack(Powders.powder_actinium), caA);
		/// CARBON END ///

		/// COPPER START ///
		int coA = 15;

		makeRecipe(new ComparableStack(ModItems.part_copper), new RecipesCommon.OreDictStack("dustBeryllium"), new ItemStack(Powders.powder_quartz), coA);
		makeRecipe(new ComparableStack(ModItems.part_copper), new RecipesCommon.OreDictStack("dustCoal"), new ItemStack(Powders.powder_bromine), coA);
		makeRecipe(new ComparableStack(ModItems.part_copper), new RecipesCommon.OreDictStack("dustTitanium"), new ItemStack(Powders.powder_strontium), coA);
		makeRecipe(new ComparableStack(ModItems.part_copper), new RecipesCommon.OreDictStack("dustIron"), new ItemStack(Powders.powder_niobium), coA);
		makeRecipe(new ComparableStack(ModItems.part_copper), new ComparableStack(Powders.powder_bromine), new ItemStack(Powders.powder_iodine), coA);
		makeRecipe(new ComparableStack(ModItems.part_copper), new ComparableStack(Powders.powder_strontium), new ItemStack(Powders.powder_neodymium), coA);
		makeRecipe(new ComparableStack(ModItems.part_copper), new ComparableStack(Powders.powder_niobium), new ItemStack(Powders.powder_caesium), coA);
		makeRecipe(new ComparableStack(ModItems.part_copper), new ComparableStack(Powders.powder_iodine), new ItemStack(Powders.powder_polonium), coA);
		makeRecipe(new ComparableStack(ModItems.part_copper), new ComparableStack(Powders.powder_caesium), new ItemStack(Powders.powder_actinium), coA);
		makeRecipe(new ComparableStack(ModItems.part_copper), new RecipesCommon.OreDictStack("dustGold"), new ItemStack(Powders.powder_uranium), coA);
		/// COPPER END ///

		/// PLUTONIUM START ///
		int plA = 100;

		makeRecipe(new ComparableStack(ModItems.part_plutonium), new RecipesCommon.OreDictStack("dustPhosphorus"), new ItemStack(Powders.powder_tennessine), plA);
		makeRecipe(new ComparableStack(ModItems.part_plutonium), new RecipesCommon.OreDictStack(PU.dust()), new ItemStack(Powders.powder_tennessine), plA);
		makeRecipe(new ComparableStack(ModItems.part_plutonium), new ComparableStack(Powders.powder_tennessine), new ItemStack(Powders.powder_australium), plA);
		makeRecipe(new ComparableStack(ModItems.part_plutonium), new ComparableStack(ModItems.pellet_charged), new ItemStack(Nuggies.nugget_schrabidium), 1000);
		makeRecipe(new ComparableStack(ModItems.part_plutonium), new ComparableStack(ModItems.cell, 1, Fluids.AMAT.getID()), new ItemStack(ModItems.cell, 1, Fluids.ASCHRAB.getID()), 0);
		/// PLUTONIUM END ///
	}

	private static void makeRecipe(ComparableStack part, RecipesCommon.AStack in, ItemStack out, int amat) {
		recipes.put(new Tuple.Pair(part, in), new Tuple.Pair(out, amat));
	}

	public static Object[] getOutput(ItemStack stack, ItemStack box) {

		if(stack == null || stack.getItem() == null || box == null)
			return null;

		ComparableStack boxStack = new ComparableStack(box).makeSingular();
		ComparableStack comp = new ComparableStack(stack).makeSingular();

		//boo hoo we iterate over a hash map, cry me a river
		for(Entry<Tuple.Pair<ComparableStack, RecipesCommon.AStack>, Tuple.Pair<ItemStack, Integer>> entry : recipes.entrySet()) {

			if(entry.getKey().getKey().isApplicable(boxStack) && entry.getKey().getValue().isApplicable(comp)) {
				return new Object[] { entry.getValue().getKey().copy(), entry.getValue().getValue() };
			}
		}

		return null;
	}

	public static Map<Object[], Object> getRecipes() {

		Map<Object[], Object> map = new HashMap<Object[], Object>();

		for(Entry<Tuple.Pair<ComparableStack, RecipesCommon.AStack>, Tuple.Pair<ItemStack, Integer>> entry : recipes.entrySet()) {
			List<ItemStack> stack = entry.getKey().getValue().extractForJEI();

			for(ItemStack ingredient : stack) {
				map.put(new ItemStack[] { entry.getKey().getKey().toStack(), ingredient }, entry.getValue().getKey());
			}
		}

		return map;
	}

	@Override
	public String getFileName() {
		return "hbmCyclotron.json";
	}

	@Override
	public Object getRecipeObject() {
		return this.recipes;
	}

	@Override
	public void readRecipe(JsonElement recipe) {
		JsonArray particle = ((JsonObject)recipe).get("particle").getAsJsonArray();
		JsonArray input = ((JsonObject)recipe).get("input").getAsJsonArray();
		JsonArray output = ((JsonObject)recipe).get("output").getAsJsonArray();
		int antimatter = ((JsonObject)recipe).get("antimatter").getAsInt();
		ItemStack partStack = this.readItemStack(particle);
		RecipesCommon.AStack inStack = this.readAStack(input);
		ItemStack outStack = this.readItemStack(output);

		this.recipes.put(new Tuple.Pair(new ComparableStack(partStack), inStack),  new Tuple.Pair(outStack, antimatter));
	}

	@Override
	public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
		try{
			Entry<Tuple.Pair<ComparableStack, RecipesCommon.AStack>, Tuple.Pair<ItemStack, Integer>> rec = (Entry<Tuple.Pair<ComparableStack, RecipesCommon.AStack>, Tuple.Pair<ItemStack, Integer>>) recipe;

			writer.name("particle");
			this.writeItemStack(rec.getKey().getKey().toStack(), writer);
			writer.name("input");
			this.writeAStack(rec.getKey().getValue(), writer);
			writer.name("output");
			this.writeItemStack(rec.getValue().getKey(), writer);
			writer.name("antimatter").value(rec.getValue().getValue());

		} catch(Exception ex) {
			MainRegistry.logger.error(ex);
			ex.printStackTrace();
		}
	}

	@Override
	public void deleteRecipes() {
		this.recipes.clear();
	}

	@Override
	public String getComment() {
		return "The particle item, while being an input, has to be defined as an item stack without ore dictionary support.";
	}
}
