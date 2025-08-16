package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.RefineryRecipes;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

public class VacuumRecipeHandler extends JEIUniversalHandler {

    public VacuumRecipeHandler(IGuiHelper helper) {
        super(helper, JEIConfig.VACUUM, ModBlocks.machine_vacuum_distill.getTranslationKey(),
                new ItemStack[]{new ItemStack(ModBlocks.machine_vacuum_distill)}, wrapRecipes2(RefineryRecipes.getVacuumRecipe()));
    }
}
