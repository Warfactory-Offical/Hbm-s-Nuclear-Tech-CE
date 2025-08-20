package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.ChemicalPlantRecipes;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

public class ChemicalPlantRecipeHandler extends JEIGenericRecipeHandler {

    public ChemicalPlantRecipeHandler(IGuiHelper helper) {
        super(helper, JEIConfig.CHEMICAL_PLANT, ModBlocks.machine_chemical_plant.getTranslationKey(), ChemicalPlantRecipes.INSTANCE, new ItemStack(ModBlocks.machine_chemical_plant));
    }
}
