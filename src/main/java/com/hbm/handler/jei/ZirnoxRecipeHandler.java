package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.tileentity.machine.TileEntityReactorZirnox;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

public class ZirnoxRecipeHandler extends JEIUniversalHandler {

    public ZirnoxRecipeHandler(IGuiHelper helper) {
        super(helper, JEIConfig.ZIRNOX, ModBlocks.reactor_zirnox.getTranslationKey(),
                new ItemStack[]{new ItemStack(ModBlocks.reactor_zirnox)}, wrapRecipes7(TileEntityReactorZirnox.fuelMap));
    }
}
