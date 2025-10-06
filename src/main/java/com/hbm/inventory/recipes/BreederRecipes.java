package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBreedingRod;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BreederRecipes extends SerializableRecipe {

	private static final HashMap<ComparableStack, BreederRecipe> recipes = new HashMap<>();

    public static void removeRecipe(ComparableStack key) {
        recipes.remove(key);

    }

    public static void addRecipe(ComparableStack key, ItemStack output, int flux) {
        recipes.put(key, new BreederRecipe(output, flux));
    }

    @Override
    public void registerDefaults() {

    }

	public static void setRecipe(ItemBreedingRod.BreedingRodType inputType, ItemBreedingRod.BreedingRodType outputType, int flux) {
		recipes.put(new ComparableStack(new ItemStack(ModItems.rod, 1, inputType.ordinal())), new BreederRecipe(new ItemStack(ModItems.rod, 1, outputType.ordinal()), flux));
		recipes.put(new ComparableStack(new ItemStack(ModItems.rod_dual, 1, inputType.ordinal())), new BreederRecipe(new ItemStack(ModItems.rod_dual, 1, outputType.ordinal()), flux * 2));
		recipes.put(new ComparableStack(new ItemStack(ModItems.rod_quad, 1, inputType.ordinal())), new BreederRecipe(new ItemStack(ModItems.rod_quad, 1, outputType.ordinal()), flux * 3));
	}

	public static HashMap<ItemStack, BreederRecipe> getAllRecipes() {

		HashMap<ItemStack, BreederRecipe> map = new HashMap();

		for(Map.Entry<ComparableStack, BreederRecipe> recipe : recipes.entrySet()) {
			map.put(recipe.getKey().toStack(), recipe.getValue());
		}

		return map;
	}

	public static BreederRecipe getOutput(ItemStack stack) {

		if(stack == null || stack.isEmpty())
			return null;

		ComparableStack sta = new ComparableStack(stack).makeSingular();
		return BreederRecipes.recipes.get(sta);
	}
	
	//nicer than opaque object arrays
	public static class BreederRecipe {
		
		public ItemStack output;
		public int flux;
		
		public BreederRecipe() { }
		
		public BreederRecipe(Item output, int heat) {
			this(new ItemStack(output), heat);
		}
		
		public BreederRecipe(ItemStack output, int heat) {
			this.output = output;
			this.flux = heat;
		}
	}

	@Override
	public String getFileName() {
		return "hbmBreeder.json";
	}

	@Override
	public Object getRecipeObject() {
		return recipes;
	}

	@Override
	public void readRecipe(JsonElement recipe) {
		JsonObject obj = (JsonObject) recipe;

		RecipesCommon.AStack in = this.readAStack(obj.get("input").getAsJsonArray());
		int flux = obj.get("flux").getAsInt();
		ItemStack out = this.readItemStack(obj.get("output").getAsJsonArray());
		recipes.put(((ComparableStack) in), new BreederRecipe(out, flux));
	}

	@Override
	public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
		Map.Entry<ComparableStack, BreederRecipe> rec = (Map.Entry<ComparableStack, BreederRecipe>) recipe;
		ComparableStack in = rec.getKey();

		writer.name("input");
		this.writeAStack(in, writer);
		writer.name("flux").value(rec.getValue().flux);
		writer.name("output");
		this.writeItemStack(rec.getValue().output, writer);
	}

	@Override
	public void deleteRecipes() {
		recipes.clear();
	}
}
