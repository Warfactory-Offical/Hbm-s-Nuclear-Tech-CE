package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.machine.ItemFluidIcon;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

import java.util.HashMap;

public class DeuteriumHandler extends JEIUniversalHandler {

    public DeuteriumHandler(IGuiHelper helper) {
        super(helper, JEIConfig.DEUTERIUM, ModBlocks.machine_deuterium_extractor.getTranslationKey(),
                new ItemStack[]{new ItemStack(ModBlocks.machine_deuterium_extractor)}, generateRecipes());
    }

    public static HashMap<Object, Object> generateRecipes() {
        HashMap<Object, Object> map = new HashMap<>();
        map.put(ItemFluidIcon.make(Fluids.WATER, 1_000), ItemFluidIcon.make(Fluids.HEAVYWATER, 20));
        return map;
    }

}
