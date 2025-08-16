package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.trait.FT_Heatable;
import com.hbm.items.machine.ItemFluidIcon;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

import java.util.HashMap;

public class BoilingHandler extends JEIUniversalHandler {

    public BoilingHandler(IGuiHelper helper) {
        super(helper, JEIConfig.BOILER, ModBlocks.heat_boiler.getLocalizedName(), new ItemStack[] { new ItemStack(ModBlocks.heat_boiler), new ItemStack(ModBlocks.machine_industrial_boiler) }, generateRecipes());
    }

    public static HashMap<Object, Object> cache;
    public static boolean isReload=false;

    public static HashMap<Object, Object> generateRecipes() {

        if(cache != null && !isReload) return cache;

        cache = new HashMap<>();

        for(FluidType type : Fluids.getInNiceOrder()) {

            if(type.hasTrait(FT_Heatable.class)) {
                FT_Heatable trait = type.getTrait(FT_Heatable.class);

                if(trait.getEfficiency(FT_Heatable.HeatingType.BOILER) > 0) {
                    FT_Heatable.HeatingStep step = trait.getFirstStep();
                    cache.put(ItemFluidIcon.make(type, step.amountReq), ItemFluidIcon.make(step.typeProduced, step.amountProduced));
                }
            }
        }
        isReload=false;
        return cache;
    }
}
