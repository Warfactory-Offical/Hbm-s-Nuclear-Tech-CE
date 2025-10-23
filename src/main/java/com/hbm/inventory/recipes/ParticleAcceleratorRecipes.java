package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ModItems;
import com.hbm.items.ModItems.Foods;
import com.hbm.items.ModItems.Materials.Powders;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ParticleAcceleratorRecipes extends SerializableRecipe {

    public static final List<ParticleAcceleratorRecipe> recipes = new ArrayList<>();

    @Override
    public String getFileName() {
        return "hbmParticleAccelerator.json";
    }

    @Override
    public Object getRecipeObject() {
        return recipes;
    }

    @Override
    public void readRecipe(JsonElement recipe) {
        JsonObject obj = (JsonObject) recipe;
        int momentum = obj.get("momentum").getAsInt();
        RecipesCommon.AStack[] in = readAStackArray(obj.get("inputs").getAsJsonArray());
        ItemStack[] out = readItemStackArray(obj.get("outputs").getAsJsonArray());

        recipes.add(new ParticleAcceleratorRecipe(in[0], in.length > 1 ? in[1] : null, momentum, out[0], out.length > 1 ? out[1] : null));
    }

    @Override
    public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
        ParticleAcceleratorRecipe rec = (ParticleAcceleratorRecipe) recipe;

        writer.name("momentum").value(rec.momentum);

        writer.name("inputs").beginArray();
        writeAStack(rec.input1, writer);
        writeAStack(rec.input2, writer);
        writer.endArray();

        writer.name("outputs").beginArray();
        writeItemStack(rec.output1, writer);
        if (rec.output2 != null) writeItemStack(rec.output2, writer);
        writer.endArray();
    }

    @Override
    public void registerDefaults() {
        recipes.add(new ParticleAcceleratorRecipe(new RecipesCommon.ComparableStack(ModItems.particle_hydrogen),
                new RecipesCommon.ComparableStack(ModItems.particle_copper), 300, new ItemStack(ModItems.particle_amat), null));
        recipes.add(new ParticleAcceleratorRecipe(new RecipesCommon.ComparableStack(ModItems.particle_amat),
                new RecipesCommon.ComparableStack(ModItems.particle_amat), 400, new ItemStack(ModItems.particle_aschrab), null));
        recipes.add(new ParticleAcceleratorRecipe(new RecipesCommon.ComparableStack(ModItems.particle_aschrab),
                new RecipesCommon.ComparableStack(ModItems.particle_aschrab), 10_000, new ItemStack(ModItems.particle_dark), null));
        recipes.add(new ParticleAcceleratorRecipe(new RecipesCommon.ComparableStack(ModItems.particle_hydrogen),
                new RecipesCommon.ComparableStack(ModItems.particle_amat), 2_500, new ItemStack(ModItems.particle_muon), null));
        recipes.add(new ParticleAcceleratorRecipe(new RecipesCommon.ComparableStack(ModItems.particle_hydrogen),
                new RecipesCommon.ComparableStack(ModItems.particle_lead), 6_500, new ItemStack(ModItems.particle_higgs), null));
        recipes.add(new ParticleAcceleratorRecipe(new RecipesCommon.ComparableStack(ModItems.particle_muon),
                new RecipesCommon.ComparableStack(ModItems.particle_higgs), 5_000, new ItemStack(ModItems.particle_tachyon), null));
        recipes.add(new ParticleAcceleratorRecipe(new RecipesCommon.ComparableStack(ModItems.particle_muon),
                new RecipesCommon.ComparableStack(ModItems.particle_dark), 12_500, new ItemStack(ModItems.particle_strange), null));
        recipes.add(new ParticleAcceleratorRecipe(new RecipesCommon.ComparableStack(ModItems.particle_strange),
                new RecipesCommon.ComparableStack(Powders.powder_magic), 12_500, new ItemStack(ModItems.particle_sparkticle),
                new ItemStack(ModItems.dust)));
        recipes.add(new ParticleAcceleratorRecipe(new RecipesCommon.ComparableStack(ModItems.particle_sparkticle),
                new RecipesCommon.ComparableStack(ModItems.particle_higgs), 70_000, new ItemStack(ModItems.particle_digamma), null));
        recipes.add(new ParticleAcceleratorRecipe(new RecipesCommon.ComparableStack(Items.CHICKEN),
                new RecipesCommon.ComparableStack(Items.CHICKEN), 100, new ItemStack(Foods.nugget), new ItemStack(Foods.nugget)));
    }

    @Override
    public void deleteRecipes() {
        recipes.clear();
    }

    public static ParticleAcceleratorRecipe getOutput(ItemStack input1, ItemStack input2) {

        for(ParticleAcceleratorRecipe recipe : recipes) {

            if(((recipe.input1.matchesRecipe(input1, true) && recipe.input2.matchesRecipe(input2, true)) ||
                    (recipe.input1.matchesRecipe(input2, true) && recipe.input2.matchesRecipe(input1, true)))) {
                return recipe;
            }
        }

        return null;
    }

    public static HashMap<Object[], Object> getRecipes() {

        HashMap<Object[], Object> recipes = new HashMap<>();

        for(ParticleAcceleratorRecipe entry : ParticleAcceleratorRecipes.recipes) {
            List<ItemStack> outputs = new ArrayList<>();
            outputs.add(entry.output1);
            if(entry.output2 != null) outputs.add(entry.output2);
            recipes.put(new Object[] {entry.input1, entry.input2}, outputs.toArray(new ItemStack[0]));
        }

        return recipes;
    }

    public static class ParticleAcceleratorRecipe {
        @NotNull
        public RecipesCommon.AStack input1;
        @NotNull
        public RecipesCommon.AStack input2;
        public int momentum;
        @NotNull
        public ItemStack output1;
        @Nullable
        public ItemStack output2;

        ParticleAcceleratorRecipe(RecipesCommon.@NotNull AStack in1, RecipesCommon.@NotNull AStack in2, int momentum, @NotNull ItemStack out1,
                                  @Nullable ItemStack out2) {
            this.input1 = in1;
            this.input2 = in2;
            this.momentum = momentum;
            this.output1 = out1;
            this.output2 = out2;
        }

        // it makes more sense to have this logic here
        public boolean matchesRecipe(ItemStack in1, ItemStack in2) {
            return this.input1.matchesRecipe(in1, true) && this.input2.matchesRecipe(in2, true) || this.input1.matchesRecipe(in2, true) && this.input2.matchesRecipe(in1, true);
        }
    }
}
