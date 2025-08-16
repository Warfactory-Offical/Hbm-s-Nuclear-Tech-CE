package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.ArcFurnaceRecipes;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

public class ArcFurnaceFluidHandler extends JEIUniversalHandler {

    public ArcFurnaceFluidHandler(IGuiHelper help){
        super(help, JEIConfig.ARC_FURNACE_FLUID, ModBlocks.machine_arc_furnace.getTranslationKey(), new ItemStack[]{new ItemStack(ModBlocks.machine_arc_furnace)}, ArcFurnaceRecipes.getFluidRecipes());
    }
}
