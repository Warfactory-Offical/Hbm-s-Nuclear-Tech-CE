package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ModItems;
import com.hbm.items.ModItems.Materials.Ingots;
import net.minecraft.item.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.hbm.inventory.OreDictManager.*;

public class ExposureChamberRecipes extends SerializableRecipe {

    public static List<ExposureChamberRecipe> recipes = new ArrayList<>();

    public static ExposureChamberRecipe getRecipe(ItemStack particle, ItemStack input) {
        for (ExposureChamberRecipe recipe : recipes)
            if (recipe.particle.matchesRecipe(particle, true) && recipe.ingredient.matchesRecipe(input, true)) return recipe;
        return null;
    }

    public static HashMap<Object, Object> getRecipes() {

        HashMap<Object, Object> recipes = new HashMap<Object, Object>();

        for (ExposureChamberRecipe recipe : ExposureChamberRecipes.recipes) {

            Object[] array = new Object[2];

            array[1] = recipe.particle;
            AStack stack = recipe.ingredient.copy();
            stack.stacksize = 8;
            array[0] = stack;
            ItemStack output = recipe.output.copy();
            output.setCount(8);

            recipes.put(array, output);
        }

        return recipes;
    }

    @Override
    public void registerDefaults() {
        recipes.add(new ExposureChamberRecipe(new ComparableStack(ModItems.particle_higgs), new OreDictStack(U.ingot()),
                new ItemStack(Ingots.ingot_schraranium)));
        recipes.add(new ExposureChamberRecipe(new ComparableStack(ModItems.particle_higgs), new OreDictStack(U238.ingot()),
                new ItemStack(Ingots.ingot_schrabidium)));
        recipes.add(new ExposureChamberRecipe(new ComparableStack(ModItems.particle_dark), new OreDictStack(PU.ingot()),
                new ItemStack(Ingots.ingot_euphemium)));
        recipes.add(new ExposureChamberRecipe(new ComparableStack(ModItems.particle_sparkticle), new OreDictStack(SBD.ingot()),
                new ItemStack(Ingots.ingot_dineutronium)));
    }

    @Override
    public String getFileName() {
        return "hbmExposureChamber.json";
    }

    @Override
    public Object getRecipeObject() {
        return recipes;
    }

    @Override
    public void deleteRecipes() {
        recipes.clear();
    }

    @Override
    public void readRecipe(JsonElement recipe) {

        JsonObject obj = (JsonObject) recipe;

        AStack particle = readAStack(obj.get("particle").getAsJsonArray());
        AStack ingredient = readAStack(obj.get("ingredient").getAsJsonArray());
        ItemStack output = readItemStack(obj.get("output").getAsJsonArray());

        ExposureChamberRecipe rec = new ExposureChamberRecipe(particle, ingredient, output);
        recipes.add(rec);
    }

    @Override
    public void writeRecipe(Object o, JsonWriter writer) throws IOException {
        ExposureChamberRecipe recipe = (ExposureChamberRecipe) o;

        writer.name("particle");
        writeAStack(recipe.particle, writer);
        writer.name("ingredient");
        writeAStack(recipe.ingredient, writer);
        writer.name("output");
        writeItemStack(recipe.output, writer);
    }

    public static class ExposureChamberRecipe {

        public AStack particle;
        public AStack ingredient;
        public ItemStack output;

        public ExposureChamberRecipe(AStack particle, AStack ingredient, ItemStack output) {
            this.particle = particle;
            this.ingredient = ingredient;
            this.output = output;
        }
    }
}
