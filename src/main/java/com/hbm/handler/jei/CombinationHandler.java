package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.CombinationRecipes;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

public class CombinationHandler extends JEIUniversalHandler {

    public CombinationHandler(IGuiHelper helper) {
        super(helper, JEIConfig.COMBINATION, ModBlocks.furnace_combination.getTranslationKey(),
                new ItemStack[]{new ItemStack(ModBlocks.furnace_combination)}, wrapRecipes2(CombinationRecipes.getRecipes()));
    }
}