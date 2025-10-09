package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.hbm.integration.groovy.GroovyScriptModule;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.recipes.RBMKOutgasserRecipes;
import com.hbm.inventory.RecipesCommon;
import com.hbm.util.Tuple;
import net.minecraft.item.ItemStack;

import java.util.Iterator;

import static com.hbm.inventory.recipes.RBMKOutgasserRecipes.recipes;

public class IrradiationChannel extends VirtualizedRegistry<Tuple.Pair<RecipesCommon.AStack, Tuple.Pair<ItemStack, FluidStack>>> {
    @Override
    public void onReload() {
        this.removeScripted().forEach(this::removeRecipe);
        this.restoreFromBackup().forEach(this::addRecipe);
    }

    public void removeRecipe(Tuple.Pair<RecipesCommon.AStack, Tuple.Pair<ItemStack, FluidStack>> pair){
        recipes.remove(pair.getKey(), pair.getValue());
        this.addBackup(pair);
    }

    public void removeAll(){
        for (Iterator<RecipesCommon.AStack> it = recipes.keySet().iterator(); it.hasNext(); ) {
            RecipesCommon.AStack stack = it.next();
            Tuple.Pair<ItemStack, FluidStack> param = recipes.get(stack);
            this.addBackup(new Tuple.Pair<>(stack, param));
            it.remove();
        }
    }

    public void removeRecipe(IIngredient ingredient){
        for(ItemStack stack:ingredient.getMatchingStacks()){
            removeRecipe(stack);
        }
    }

    public void removeRecipe(ItemStack stack){
        RecipesCommon.AStack comparableStack = new RecipesCommon.ComparableStack(stack);
        Tuple.Pair<ItemStack, FluidStack> param = recipes.get(comparableStack);
        recipes.remove(comparableStack, param);
        this.addBackup(new Tuple.Pair<>(comparableStack, param));
    }

    public void addRecipe(ItemStack input, ItemStack outItem, FluidStack outFluid){
        RecipesCommon.ComparableStack stack = new RecipesCommon.ComparableStack(input);
        Tuple.Pair<ItemStack, FluidStack> objects = new Tuple.Pair<>(outItem, new FluidStack(outFluid.type, outFluid.fill));
        addRecipe(new Tuple.Pair<>(stack, objects));
    }

    public void addRecipe(ItemStack input, FluidStack out){
        addRecipe(input, null, out);
    }

    public void addRecipe(ItemStack input, ItemStack out){
        addRecipe(input, out, null);
    }

    public void addRecipe(Tuple.Pair<RecipesCommon.AStack, Tuple.Pair<ItemStack, FluidStack>> pair){
        recipes.put(pair.getKey(), pair.getValue());
        this.addScripted(pair);
    }
}
