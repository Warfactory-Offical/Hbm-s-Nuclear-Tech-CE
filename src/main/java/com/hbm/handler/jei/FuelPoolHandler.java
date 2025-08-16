package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.WasteDrumRecipes;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

public class FuelPoolHandler extends JEIUniversalHandler {

    public FuelPoolHandler(IGuiHelper helper) {
        super(helper, JEIConfig.WASTEDRUM, ModBlocks.machine_waste_drum.getTranslationKey(),
                new ItemStack[]{new ItemStack(ModBlocks.machine_waste_drum)}, wrapRecipes7(WasteDrumRecipes.recipes));
    }
}
