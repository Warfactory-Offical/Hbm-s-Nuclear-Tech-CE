package com.hbm.integration.groovy.script.recipes;

import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.helper.ingredient.IngredientList;
import com.hbm.integration.groovy.util.IngredientUtils;
import com.hbm.inventory.recipes.anvil.AnvilSmithingRecipe;
import net.minecraft.item.ItemStack;

public class GroovyAnvilSmithingRecipes extends AnvilSmithingRecipe {

    public GroovyAnvilSmithingRecipes(int tier, ItemStack out, IngredientList<IIngredient> input) {
        super(tier, out, IngredientUtils.convertIngredient2Astack(input.get(0)), IngredientUtils.convertIngredient2Astack(input.get(1)));
    }
}
