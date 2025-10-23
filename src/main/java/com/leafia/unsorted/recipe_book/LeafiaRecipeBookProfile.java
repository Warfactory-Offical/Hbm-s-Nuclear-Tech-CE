package com.leafia.unsorted.recipe_book;

import com.hbm.inventory.RecipesCommon.AStack;
import com.leafia.shit.recipe_book_elements.LeafiaRecipeBookTab;
import com.leafia.unsorted.recipe_book.system.LeafiaDummyRecipe;
import com.llib.math.range.RangeInt;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class LeafiaRecipeBookProfile {
    public static class RecipeCategory {
        public final Map<Item,Map<Integer,List<LeafiaDummyRecipe>>> recipes;
        public final LeafiaRecipeBookTab tab;
        public final String label;
        public static final int ID = 0;
        public RecipeCategory(CreativeTabs tab,String label) {
            // Vanilla tabs are under CreativeTabs.*
            // Mod tabs are under MainRegistry.*
            this.recipes = new HashMap<>();
            this.tab = new LeafiaRecipeBookTab(ID,this,tab);
            this.label = label;
        }
        public RecipeCategory(String label,Item... icon) {
            // Vanilla tabs are under CreativeTabs.*
            // Mod tabs are under MainRegistry.*
            this.recipes = new HashMap<>();
            this.tab = new LeafiaRecipeBookTab(ID,this,icon);
            this.label = label;
        }
        public RecipeCategory addItem(Item item,int count,LeafiaDummyRecipe reciple) {
            if (!this.recipes.containsKey(item))
                this.recipes.put(item,new HashMap<>());
            if (!this.recipes.get(item).containsKey(count))
                this.recipes.get(item).put(count,new ArrayList<>());
            this.recipes.get(item).get(count).add(reciple);
            return this;
        }
    }
    public abstract List<RecipeCategory> getCategories();
    public abstract Map<RangeInt,AStack[]> showRecipe(LeafiaDummyRecipe recipe);
}
