package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ModItems;
import com.hbm.util.ItemStackUtil;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.util.HashMap;

public class ConstructionHandler extends JEIUniversalHandler {

    public ConstructionHandler(IGuiHelper helper) {
        super(helper, JEIConfig.CONSTRUCTION, "Construction",
                new ItemStack[]{
                        new ItemStack(ModItems.acetylene_torch),
                        new ItemStack(ModItems.blowtorch),
                        new ItemStack(ModItems.boltgun)}, wrapRecipes3(getRecipesForJEI()));
    }


    public static HashMap<Object[], Object> bufferedRecipes = new HashMap<>();

    public static HashMap<Object[], Object> getRecipesForJEI() {

        if(!bufferedRecipes.isEmpty()) {
            return bufferedRecipes;
        }

        /* WATZ */
        ItemStack[] watz = new ItemStack[] {
                new ItemStack(ModBlocks.watz_casing, 48),
                Mats.MAT_DURA.make(ModItems.bolt, 64),
                Mats.MAT_DURA.make(ModItems.bolt, 64),
                Mats.MAT_DURA.make(ModItems.bolt, 64),
                new ItemStack(ModBlocks.watz_element, 36),
                new ItemStack(ModBlocks.watz_cooler, 26),
                new ItemStack(ModItems.boltgun)};

        bufferedRecipes.put(watz, new ItemStack(ModBlocks.watz));

        /* ITER */
        ItemStack[] iter = new ItemStack[] {
                new ItemStack(ModBlocks.fusion_conductor, 36),
                ItemStackUtil.addTooltipToStack(new ItemStack(ModBlocks.fusion_conductor, 256), TextFormatting.RED + "4x64"),
                new ItemStack(ModItems.plate_cast, 36, Mats.MAT_STEEL.id),
                ItemStackUtil.addTooltipToStack(new ItemStack(ModItems.plate_cast, 256, Mats.MAT_STEEL.id), TextFormatting.RED + "4x64"),
                new ItemStack(ModBlocks.fusion_center, 64),
                new ItemStack(ModBlocks.fusion_motor, 4),
                new ItemStack(ModBlocks.reinforced_glass, 8),
                new ItemStack(ModItems.blowtorch)};

        bufferedRecipes.put(iter, new ItemStack(ModBlocks.iter));

        /* PLASMA HEATER */
        ItemStack[] heater = new ItemStack[] {
                new ItemStack(ModBlocks.fusion_heater, 7),
                new ItemStack(ModBlocks.fusion_heater, 64),
                new ItemStack(ModBlocks.fusion_heater, 64) };

        bufferedRecipes.put(heater, new ItemStack(ModBlocks.plasma_heater));

        /* COMPACT LAUNCHER */
        ItemStack[] launcher = new ItemStack[] { new ItemStack(ModBlocks.struct_launcher, 8) };

        bufferedRecipes.put(launcher, new ItemStack(ModBlocks.compact_launcher));

        /* LAUNCH TABLE */
        ItemStack[] table = new ItemStack[] {
                new ItemStack(ModBlocks.struct_launcher, 16),
                new ItemStack(ModBlocks.struct_launcher, 64),
                new ItemStack(ModBlocks.struct_scaffold, 11)};

        bufferedRecipes.put(table, new ItemStack(ModBlocks.launch_table));

        /* SOYUZ LAUNCHER */
        ItemStack[] soysauce = new ItemStack[] {
                new ItemStack(ModBlocks.struct_launcher, 30),
                ItemStackUtil.addTooltipToStack(new ItemStack(ModBlocks.struct_launcher, 384), TextFormatting.RED + "6x64"),
                new ItemStack(ModBlocks.struct_scaffold, 63),
                ItemStackUtil.addTooltipToStack(new ItemStack(ModBlocks.struct_scaffold, 384), TextFormatting.RED + "6x64"),
                new ItemStack(ModBlocks.concrete_smooth, 38),
                ItemStackUtil.addTooltipToStack(new ItemStack(ModBlocks.concrete_smooth, 320), TextFormatting.RED + "5x64"),};

        bufferedRecipes.put(soysauce, new ItemStack(ModBlocks.soyuz_launcher));

        /* ICF */
        ItemStack[] icf = new ItemStack[] {
                new ItemStack(ModBlocks.icf_component, 50, 0),
                ItemStackUtil.addTooltipToStack(new ItemStack(ModBlocks.icf_component, 240, 3), TextFormatting.RED + "3x64 + 48"),
                ItemStackUtil.addTooltipToStack(Mats.MAT_DURA.make(ModItems.bolt, 960), TextFormatting.RED + "15x64"),
                ItemStackUtil.addTooltipToStack(Mats.MAT_STEEL.make(ModItems.plate_cast, 240), TextFormatting.RED + "3x64 + 48"),
                ItemStackUtil.addTooltipToStack(new ItemStack(ModBlocks.icf_component, 117, 1), TextFormatting.RED + "64 + 53"),
                ItemStackUtil.addTooltipToStack(Mats.MAT_BBRONZE.make(ModItems.plate_cast, 117), TextFormatting.RED + "64 + 53"),
                new ItemStack(ModItems.blowtorch),
                new ItemStack(ModItems.boltgun) };

        bufferedRecipes.put(icf, new ItemStack(ModBlocks.icf));

        return bufferedRecipes;
    }
}
