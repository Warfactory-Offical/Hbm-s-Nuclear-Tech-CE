package com.hbm.inventory.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ItemEnums;
import com.hbm.items.ModItems;
import com.hbm.items.ModItems.Materials.Crystals;
import com.hbm.items.ModItems.Materials.Ingots;
import com.hbm.items.ModItems.Materials.Nuggies;
import com.hbm.items.ModItems.Materials.Powders;
import com.hbm.items.machine.ItemFluidIcon;
import com.hbm.items.machine.ItemScraps;
import com.hbm.items.special.ItemBedrockOreNew;
import com.hbm.util.ItemStackUtil;
import com.hbm.util.Tuple;
import net.minecraft.item.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElectrolyserMetalRecipes extends SerializableRecipe {

    public static HashMap<RecipesCommon.AStack, ElectrolysisMetalRecipe> recipes = new HashMap<>();

    @Override
    public void registerDefaults() {

        recipes.put(new RecipesCommon.ComparableStack(Crystals.crystal_iron), new ElectrolysisMetalRecipe(
                new Mats.MaterialStack(Mats.MAT_IRON, MaterialShapes.INGOT.q(6)),
                new Mats.MaterialStack(Mats.MAT_TITANIUM, MaterialShapes.INGOT.q(2)),
                new ItemStack(Powders.powder_lithium_tiny, 3)));
        recipes.put(new RecipesCommon.ComparableStack(Crystals.crystal_gold), new ElectrolysisMetalRecipe(
                new Mats.MaterialStack(Mats.MAT_GOLD, MaterialShapes.INGOT.q(6)),
                new Mats.MaterialStack(Mats.MAT_LEAD, MaterialShapes.INGOT.q(2)),
                new ItemStack(Powders.powder_lithium_tiny, 3),
                new ItemStack(Nuggies.ingot_mercury, 2)));

        recipes.put(new RecipesCommon.ComparableStack(Crystals.crystal_uranium), new ElectrolysisMetalRecipe(
                new Mats.MaterialStack(Mats.MAT_URANIUM, MaterialShapes.INGOT.q(6)),
                new Mats.MaterialStack(Mats.MAT_RADIUM, MaterialShapes.NUGGET.q(4)),
                new ItemStack(Powders.powder_lithium_tiny, 3)));

        recipes.put(new RecipesCommon.ComparableStack(Crystals.crystal_thorium), new ElectrolysisMetalRecipe(
                new Mats.MaterialStack(Mats.MAT_THORIUM, MaterialShapes.INGOT.q(6)),
                new Mats.MaterialStack(Mats.MAT_URANIUM, MaterialShapes.INGOT.q(2)),
                new ItemStack(Powders.powder_lithium_tiny, 3)));

        recipes.put(new RecipesCommon.ComparableStack(Crystals.crystal_plutonium), new ElectrolysisMetalRecipe(
                new Mats.MaterialStack(Mats.MAT_PLUTONIUM, MaterialShapes.INGOT.q(6)),
                new Mats.MaterialStack(Mats.MAT_POLONIUM, MaterialShapes.INGOT.q(2)),
                new ItemStack(Powders.powder_lithium_tiny, 3)));

        recipes.put(new RecipesCommon.ComparableStack(Crystals.crystal_titanium), new ElectrolysisMetalRecipe(
                new Mats.MaterialStack(Mats.MAT_TITANIUM, MaterialShapes.INGOT.q(6)),
                new Mats.MaterialStack(Mats.MAT_IRON, MaterialShapes.INGOT.q(2)),
                new ItemStack(Powders.powder_lithium_tiny, 3)));

        recipes.put(new RecipesCommon.ComparableStack(Crystals.crystal_copper), new ElectrolysisMetalRecipe(
                new Mats.MaterialStack(Mats.MAT_COPPER, MaterialShapes.INGOT.q(6)),
                new Mats.MaterialStack(Mats.MAT_LEAD, MaterialShapes.NUGGET.q(4)),
                new ItemStack(Powders.powder_lithium_tiny, 3),
                new ItemStack(Powders.sulfur, 2)));

        recipes.put(new RecipesCommon.ComparableStack(Crystals.crystal_tungsten), new ElectrolysisMetalRecipe(
                new Mats.MaterialStack(Mats.MAT_TUNGSTEN, MaterialShapes.INGOT.q(6)),
                new Mats.MaterialStack(Mats.MAT_IRON, MaterialShapes.INGOT.q(2)),
                new ItemStack(Powders.powder_lithium_tiny, 3)));

        recipes.put(new RecipesCommon.ComparableStack(Crystals.crystal_aluminium), new ElectrolysisMetalRecipe(
                new Mats.MaterialStack(Mats.MAT_ALUMINIUM, MaterialShapes.INGOT.q(2)),
                new Mats.MaterialStack(Mats.MAT_IRON, MaterialShapes.INGOT.q(2)),
                new ItemStack(ModItems.chunk_ore, 4, ItemEnums.EnumChunkType.CRYOLITE.ordinal()),
                new ItemStack(Powders.powder_lithium_tiny, 3)));


        recipes.put(new RecipesCommon.ComparableStack(Crystals.crystal_beryllium), new ElectrolysisMetalRecipe(
                new Mats.MaterialStack(Mats.MAT_BERYLLIUM, MaterialShapes.INGOT.q(6)),
                new Mats.MaterialStack(Mats.MAT_LEAD, MaterialShapes.NUGGET.q(4)),
                new ItemStack(Powders.powder_lithium_tiny, 3),
                new ItemStack(Powders.powder_quartz, 2)));

        recipes.put(new RecipesCommon.ComparableStack(Crystals.crystal_lead), new ElectrolysisMetalRecipe(
                new Mats.MaterialStack(Mats.MAT_LEAD, MaterialShapes.INGOT.q(6)),
                new Mats.MaterialStack(Mats.MAT_GOLD, MaterialShapes.INGOT.q(2)),
                new ItemStack(Powders.powder_lithium_tiny, 3)));

        recipes.put(new RecipesCommon.ComparableStack(Crystals.crystal_schraranium), new ElectrolysisMetalRecipe(
                new Mats.MaterialStack(Mats.MAT_SCHRABIDIUM, MaterialShapes.NUGGET.q(5)),
                new Mats.MaterialStack(Mats.MAT_URANIUM, MaterialShapes.NUGGET.q(2)),
                new ItemStack(Nuggies.nugget_neptunium, 2)));

        recipes.put(new RecipesCommon.ComparableStack(Crystals.crystal_schrabidium), new ElectrolysisMetalRecipe(
                new Mats.MaterialStack(Mats.MAT_SCHRABIDIUM, MaterialShapes.INGOT.q(6)),
                new Mats.MaterialStack(Mats.MAT_PLUTONIUM, MaterialShapes.INGOT.q(2)),
                new ItemStack(Powders.powder_lithium_tiny, 3)));

        recipes.put(new RecipesCommon.ComparableStack(Crystals.crystal_rare), new ElectrolysisMetalRecipe(
                new Mats.MaterialStack(Mats.MAT_ZIRCONIUM, MaterialShapes.NUGGET.q(6)),
                new Mats.MaterialStack(Mats.MAT_BORON, MaterialShapes.NUGGET.q(2)),
                new ItemStack(Powders.powder_desh_mix, 3)));

        recipes.put(new RecipesCommon.ComparableStack(Crystals.crystal_trixite), new ElectrolysisMetalRecipe(
                new Mats.MaterialStack(Mats.MAT_PLUTONIUM, MaterialShapes.INGOT.q(3)),
                new Mats.MaterialStack(Mats.MAT_COBALT, MaterialShapes.INGOT.q(4)),
                new ItemStack(Powders.powder_niobium, 4),
                new ItemStack(Powders.powder_nitan_mix, 2)));

        recipes.put(new RecipesCommon.ComparableStack(Crystals.crystal_lithium), new ElectrolysisMetalRecipe(
                new Mats.MaterialStack(Mats.MAT_LITHIUM, MaterialShapes.INGOT.q(6)),
                new Mats.MaterialStack(Mats.MAT_BORON, MaterialShapes.INGOT.q(2)),
                new ItemStack(Powders.powder_quartz, 2),
                new ItemStack(Ingots.fluorite, 2)));

        recipes.put(new RecipesCommon.ComparableStack(Crystals.crystal_starmetal), new ElectrolysisMetalRecipe(
                new Mats.MaterialStack(Mats.MAT_DURA, MaterialShapes.INGOT.q(4)),
                new Mats.MaterialStack(Mats.MAT_COBALT, MaterialShapes.INGOT.q(4)),
                new ItemStack(Powders.powder_astatine, 3),
                new ItemStack(Nuggies.ingot_mercury, 8)));

        recipes.put(new RecipesCommon.ComparableStack(Crystals.crystal_cobalt), new ElectrolysisMetalRecipe(
                new Mats.MaterialStack(Mats.MAT_COBALT, MaterialShapes.INGOT.q(3)),
                new Mats.MaterialStack(Mats.MAT_IRON, MaterialShapes.INGOT.q(4)),
                new ItemStack(Powders.powder_copper, 4),
                new ItemStack(Powders.powder_lithium_tiny, 3)));

        for(ItemBedrockOreNew.BedrockOreType type : ItemBedrockOreNew.BedrockOreType.values()) {
            ArrayList<Tuple.Pair<Object, Integer>> productsF = new ArrayList<>();
            productsF.add(new Tuple.Pair<>(type.primary1, 8));
            productsF.add(new Tuple.Pair<>(type.primary2, 4));
            productsF.add(new Tuple.Pair<>(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.CRUMBS, type), 1));
            recipes.put(new RecipesCommon.ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_FIRST, type)), makeBedrockOreProduct(productsF));

            ArrayList<Tuple.Pair<Object, Integer>> productsS = new ArrayList<>();
            productsS.add(new Tuple.Pair<>(type.primary1, 4));
            productsS.add(new Tuple.Pair<>(type.primary2, 8));
            productsS.add(new Tuple.Pair<>(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.CRUMBS, type), 1));

            recipes.put(new RecipesCommon.ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_SECOND, type)), makeBedrockOreProduct(productsS));

            ArrayList<Tuple.Pair<Object, Integer>> productsC = new ArrayList<>();
            productsC.add(new Tuple.Pair<>(type.primary1, 2));
            productsC.add(new Tuple.Pair<>(type.primary2, 2));

            recipes.put(new RecipesCommon.ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.CRUMBS, type)), makeBedrockOreProduct(productsC));
        }
    }

    public static ElectrolysisMetalRecipe makeBedrockOreProduct(ArrayList<Tuple.Pair<Object, Integer>> products){
        ArrayList<Mats.MaterialStack> moltenProducts = new ArrayList<>();
        ArrayList<ItemStack> solidProducts = new ArrayList<>();

        for(Tuple.Pair<Object, Integer> product : products){
            if(moltenProducts.size() < 2 && product.getKey() instanceof ItemBedrockOreNew.BedrockOreOutput) {
                Mats.MaterialStack melt = ItemBedrockOreNew.toFluid((ItemBedrockOreNew.BedrockOreOutput) product.getKey(), product.getValue());
                if (melt != null) {
                    moltenProducts.add(melt);
                    continue;
                }
            }

            if(product.getKey() instanceof ItemBedrockOreNew.BedrockOreOutput) solidProducts.add(ItemBedrockOreNew.extract((ItemBedrockOreNew.BedrockOreOutput) product.getKey(), product.getValue()));
            if(product.getKey() instanceof ItemStack) solidProducts.add(((ItemStack) product.getKey()).copy());
        }
        if(moltenProducts.size() == 0) moltenProducts.add(new Mats.MaterialStack(Mats.MAT_SLAG, MaterialShapes.INGOT.q(2)));

        return new ElectrolysisMetalRecipe(
                moltenProducts.get(0),
                moltenProducts.size() > 1 ? moltenProducts.get(1) : null,
                20,
                solidProducts.toArray(new ItemStack[0]));
    }

    public static ElectrolysisMetalRecipe getRecipe(ItemStack stack) {
        if(stack == null) return null;
        RecipesCommon.ComparableStack comp = new RecipesCommon.ComparableStack(stack).makeSingular();

        if(recipes.containsKey(comp)) return recipes.get(comp);

        List<String> names = ItemStackUtil.getOreDictNames(stack);

        for(String name : names) {
            RecipesCommon.OreDictStack ore = new RecipesCommon.OreDictStack(name);
            if(recipes.containsKey(ore)) return recipes.get(ore);
        }

        return null;
    }

    public static HashMap<Object[], Object[]> getRecipes() {

        HashMap<Object[], Object[]> recipes = new HashMap<>();

        for(Map.Entry<RecipesCommon.AStack, ElectrolysisMetalRecipe> entry : ElectrolyserMetalRecipes.recipes.entrySet()) {

            ElectrolysisMetalRecipe recipe = entry.getValue();
            Object[] input = new Object[] { entry.getKey().copy(), ItemFluidIcon.make(Fluids.NITRIC_ACID, 100) };
            List outputs = new ArrayList();
            if(recipe.output1 != null) outputs.add(ItemScraps.create(recipe.output1, true));
            if(recipe.output2 != null) outputs.add(ItemScraps.create(recipe.output2, true));
            for(ItemStack byproduct : recipe.byproduct) outputs.add(byproduct);

            recipes.put(input, outputs.toArray());
        }

        return recipes;
    }

    @Override
    public String getFileName() {
        return "hbmElectrolyzerMetal.json";
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

        RecipesCommon.AStack input = this.readAStack(obj.get("input").getAsJsonArray());

        Mats.MaterialStack output1 = null;
        Mats.MaterialStack output2 = null;

        if(obj.has("output1")) {
            JsonArray out1 = obj.get("output1").getAsJsonArray();
            String name1 = out1.get(0).getAsString();
            int amount1 = out1.get(1).getAsInt();
            output1 = new Mats.MaterialStack(Mats.matByName.get(name1), amount1);
        }

        if(obj.has("output2")) {
            JsonArray out2 = obj.get("output2").getAsJsonArray();
            String name2 = out2.get(0).getAsString();
            int amount2 = out2.get(1).getAsInt();
            output2 = new Mats.MaterialStack(Mats.matByName.get(name2), amount2);
        }

        ItemStack[] byproducts = new ItemStack[0];
        if(obj.has("byproducts")) byproducts = this.readItemStackArray(obj.get("byproducts").getAsJsonArray());

        int duration = 600;
        if(obj.has("duration")) duration = obj.get("duration").getAsInt();

        recipes.put(input, new ElectrolysisMetalRecipe(output1, output2, duration, byproducts));
    }

    @Override
    public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
        Map.Entry<RecipesCommon.AStack, ElectrolysisMetalRecipe> rec = (Map.Entry) recipe;

        writer.name("input"); this.writeAStack(rec.getKey(), writer);

        if(rec.getValue().output1 != null) {
            writer.name("output1");
            writer.beginArray();
            writer.setIndent("");
            writer.value(rec.getValue().output1.material.names[0]).value(rec.getValue().output1.amount);
            writer.endArray();
            writer.setIndent("  ");
        }

        if(rec.getValue().output2 != null) {
            writer.name("output2");
            writer.beginArray();
            writer.setIndent("");
            writer.value(rec.getValue().output2.material.names[0]).value(rec.getValue().output2.amount);
            writer.endArray();
            writer.setIndent("  ");
        }

        if(rec.getValue().byproduct != null && rec.getValue().byproduct.length > 0) {
            writer.name("byproducts").beginArray();
            for(ItemStack stack : rec.getValue().byproduct) this.writeItemStack(stack, writer);
            writer.endArray();
        }

        writer.name("duration").value(rec.getValue().duration);
    }

    public static class ElectrolysisMetalRecipe {

        public Mats.MaterialStack output1;
        public Mats.MaterialStack output2;
        public ItemStack[] byproduct;
        public int duration;

        public ElectrolysisMetalRecipe(Mats.MaterialStack output1, Mats.MaterialStack output2, ItemStack... byproduct) {
            this.output1 = output1;
            this.output2 = output2;
            this.byproduct = byproduct;
            this.duration = 600;
        }
        public ElectrolysisMetalRecipe(Mats.MaterialStack output1, Mats.MaterialStack output2, int duration, ItemStack... byproduct) {
            this.output1 = output1;
            this.output2 = output2;
            this.byproduct = byproduct;
            this.duration = duration;
        }
    }
}
