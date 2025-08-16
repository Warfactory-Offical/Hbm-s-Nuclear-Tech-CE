package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.tileentity.machine.TileEntitySawmill;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

public class SawmillHandler extends JEIUniversalHandler {
    public SawmillHandler(IGuiHelper helper) {
        super(helper, JEIConfig.SAWMILL, ModBlocks.machine_sawmill.getTranslationKey(),
                new ItemStack[]{new ItemStack(ModBlocks.machine_sawmill)}, wrapRecipes2(TileEntitySawmill.getRecipes()));
    }
}
