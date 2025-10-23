package com.leafia.unsorted.recipe_book.profiles;

import com.hbm.inventory.recipes.PressRecipes;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.items.ModItems;
import com.hbm.util.Tuple.Pair;
import com.leafia.unsorted.recipe_book.LeafiaRecipeBookProfile;
import com.leafia.unsorted.recipe_book.system.LeafiaDummyRecipe;
import com.llib.math.range.RangeInt;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeBookPress extends LeafiaRecipeBookProfile {
    @Override
    public List<RecipeCategory> getCategories() {
        /*
        List<RecipeCategory> cats = new ArrayList<>();
        cats.add(new RecipeCategory("Plate",ModItems.stamp_iron_plate));
        cats.add(new RecipeCategory("Wire",ModItems.stamp_iron_wire));
        cats.add(new RecipeCategory("Other",ModItems.stamp_iron_flat));
        cats.add(new RecipeCategory("Ammo",ModItems.stamp_44));
        for (Map.Entry<Pair<PressRecipes.PressType,AStack>,ItemStack> entry : PressRecipes.pressRecipes.entrySet()) {
            int index = -1;
            switch(entry.getKey().getA()) {
                case CIRCUIT:
                case FLAT: index = 2; break;
                case PLATE: index = 0; break;
                case WIRE: index = 1; break;
                default: index = 3;
            }
            if (index >= 0) {
                LeafiaDummyRecipe dummy = new LeafiaDummyRecipe(entry.getValue().getItem(),entry.getValue().getCount(),entry.getKey().getB());
                dummy.tags.put("stamp",entry.getKey().getA());
                cats.get(index).addItem(entry.getValue().getItem(),entry.getValue().getCount(),dummy);
            }
        }
        return cats;*/
        return null;
    }
    @Override
    public Map<RangeInt,AStack[]> showRecipe(LeafiaDummyRecipe recipe) {
        /*
        Map<RangeInt,AStack[]> slotMap = new HashMap<>();
        PressRecipes.PressType stamp = (PressRecipes.PressType)recipe.tags.get("stamp");
        switch(stamp) {
            case FLAT: slotMap.put(new RangeInt(1,1),new AStack[]{new RecipesCommon.OreDictStack("stampFlat")});
            case PLATE: slotMap.put(new RangeInt(1,1),new AStack[]{new RecipesCommon.OreDictStack("stampPlate")});
            case WIRE: slotMap.put(new RangeInt(1,1),new AStack[]{new RecipesCommon.OreDictStack("stampWire")});
            case CIRCUIT: slotMap.put(new RangeInt(1,1),new AStack[]{new RecipesCommon.OreDictStack("stampCircuit")});
            case NINE: slotMap.put(new RangeInt(1,1),new AStack[]{new RecipesCommon.OreDictStack("stamp9")});
            case FOURFOUR: slotMap.put(new RangeInt(1,1),new AStack[]{new RecipesCommon.OreDictStack("stamp44")});
            case FIVEZERO: slotMap.put(new RangeInt(1,1),new AStack[]{new RecipesCommon.OreDictStack("stamp50")});
            case THREEFIFESEVEN: slotMap.put(new RangeInt(1,1),new AStack[]{new RecipesCommon.OreDictStack("stamp357")});
        }
        slotMap.put(new RangeInt(2,2),new AStack[]{recipe.input[0]});
        return slotMap;*/
        return null;
    }
}
