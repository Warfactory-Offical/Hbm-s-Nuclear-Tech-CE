package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.MixerRecipes;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

public class MixerRecipeHandler extends JEIUniversalHandler {
	
	public MixerRecipeHandler(IGuiHelper helper) {
		super(helper, JEIConfig.MIXER, ModBlocks.machine_mixer.getTranslationKey(), new ItemStack[]{new ItemStack(ModBlocks.machine_mixer)}, wrapRecipes3(MixerRecipes.getRecipes()));
	}

}
