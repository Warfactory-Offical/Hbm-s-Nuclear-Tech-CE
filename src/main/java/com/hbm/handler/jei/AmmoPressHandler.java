package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.AmmoPressRecipes;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

public class AmmoPressHandler extends JEIUniversalHandler {

    public AmmoPressHandler(IGuiHelper helper) {
        super(helper, JEIConfig.AMMO_PRESS, ModBlocks.machine_ammo_press.getTranslationKey(), new ItemStack[]{new ItemStack(ModBlocks.machine_ammo_press)}, AmmoPressRecipes.getRecipes());
    }
}

