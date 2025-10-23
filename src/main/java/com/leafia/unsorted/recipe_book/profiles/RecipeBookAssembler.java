package com.leafia.unsorted.recipe_book.profiles;

import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.recipes.AssemblerRecipes;
import com.hbm.main.MainRegistry;
import com.hbm.util.I18nUtil;
import com.leafia.unsorted.recipe_book.LeafiaRecipeBookProfile;
import com.leafia.unsorted.recipe_book.system.LeafiaDummyRecipe;
import com.llib.math.range.RangeInt;
import net.minecraft.creativetab.CreativeTabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecipeBookAssembler extends LeafiaRecipeBookProfile {
    @Override
    public List<RecipeCategory> getCategories() {
        List<RecipeCategory> cats = new ArrayList<>();
        cats.add(new RecipeCategory(MainRegistry.partsTab,I18nUtil.resolveKey(MainRegistry.partsTab.getTabLabel())));
        cats.add(new RecipeCategory(MainRegistry.controlTab,I18nUtil.resolveKey(MainRegistry.controlTab.getTabLabel())));
        cats.add(new RecipeCategory(MainRegistry.templateTab,I18nUtil.resolveKey(MainRegistry.templateTab.getTabLabel())));
        cats.add(new RecipeCategory(MainRegistry.resourceTab,I18nUtil.resolveKey(MainRegistry.resourceTab.getTabLabel())));
        cats.add(new RecipeCategory(MainRegistry.blockTab,I18nUtil.resolveKey(MainRegistry.blockTab.getTabLabel())));
        cats.add(new RecipeCategory(MainRegistry.machineTab,I18nUtil.resolveKey(MainRegistry.machineTab.getTabLabel())));
        cats.add(new RecipeCategory(MainRegistry.nukeTab,I18nUtil.resolveKey(MainRegistry.nukeTab.getTabLabel())));
        cats.add(new RecipeCategory(MainRegistry.missileTab,I18nUtil.resolveKey(MainRegistry.missileTab.getTabLabel())));
        cats.add(new RecipeCategory(MainRegistry.weaponTab,I18nUtil.resolveKey(MainRegistry.weaponTab.getTabLabel())));
        cats.add(new RecipeCategory(MainRegistry.consumableTab,I18nUtil.resolveKey(MainRegistry.consumableTab.getTabLabel())));
        for (RecipesCommon.ComparableStack stack : AssemblerRecipes.recipeList) {
            CreativeTabs tab = stack.item.getCreativeTab();
            if (cats.contains(tab)) {
                RecipeCategory category = cats.get(cats.indexOf(tab));
                category.addItem(stack.item,stack.count(),new LeafiaDummyRecipe(stack.item,AssemblerRecipes.recipes.get(stack).ingredients));
            }
        }
        return cats;
    }
    @Override
    public Map<RangeInt,RecipesCommon.AStack[]> showRecipe(LeafiaDummyRecipe recipe) {
        return null;
    }
}
