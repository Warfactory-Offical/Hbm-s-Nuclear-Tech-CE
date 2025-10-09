package com.hbm.integration.groovy.util;

import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.helper.ingredient.OreDictIngredient;
import com.hbm.inventory.RecipesCommon;

public class IngredientUtils {

    public static RecipesCommon.AStack convertIngredient2Astack(IIngredient ingredient){
        if(ingredient instanceof OreDictIngredient){
            return new RecipesCommon.OreDictStack(((OreDictIngredient) ingredient).getOreDict(), ingredient.getAmount());
        }
        return new RecipesCommon.ComparableStack(ingredient.getMatchingStacks()[0].getItem(), ingredient.getAmount());
    }

    public static RecipesCommon.ComparableStack convertIngredient2ComparableStack(IIngredient ingredient){
        return new RecipesCommon.ComparableStack(ingredient.getMatchingStacks()[0].getItem(), ingredient.getAmount());
    }
}
