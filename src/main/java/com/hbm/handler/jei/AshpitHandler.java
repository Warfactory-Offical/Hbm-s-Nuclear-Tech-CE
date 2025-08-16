package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ItemEnums;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFluidIcon;
import mezz.jei.api.IGuiHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.HashMap;

public class AshpitHandler extends JEIUniversalHandler {

    public AshpitHandler(IGuiHelper helper) {
        super(helper, JEIConfig.ASHPIT, ModBlocks.machine_ashpit.getTranslationKey(), new ItemStack[]{new ItemStack(ModBlocks.machine_ashpit)}, getRecipesForJEI());
    }

    public static HashMap<Object, Object> getRecipesForJEI() {

        HashMap<Object, Object> recipes = new HashMap<>();

        ItemStack[] ovens = new ItemStack[] {new ItemStack(ModBlocks.heater_firebox), new ItemStack(ModBlocks.heater_oven)};
        ItemStack[] chimneys = new ItemStack[] {new ItemStack(ModBlocks.chimney_brick), new ItemStack(ModBlocks.chimney_industrial)};
        ItemStack[] coals = new ItemStack[] {new ItemStack(Items.COAL, 1, 0), new ItemStack(ModItems.lignite), new ItemStack(ModItems.coke)};
        ItemStack[] wood = new ItemStack[] {new ItemStack(Blocks.LOG), new ItemStack(Blocks.LOG2), new ItemStack(Blocks.PLANKS), new ItemStack(Blocks.SAPLING)};
        ItemStack[] misc = new ItemStack[] {new ItemStack(ModItems.solid_fuel), new ItemStack(ModItems.scrap), new ItemStack(ModItems.dust), new ItemStack(ModItems.rocket_fuel)};
        FluidType[] smokes = new FluidType[] {Fluids.SMOKE, Fluids.SMOKE_LEADED, Fluids.SMOKE_POISON};

        recipes.put(new ItemStack[][] {ovens, coals}, OreDictManager.DictFrame.fromOne(ModItems.powder_ash, ItemEnums.EnumAshType.COAL));
        recipes.put(new ItemStack[][] {ovens, wood}, OreDictManager.DictFrame.fromOne(ModItems.powder_ash, ItemEnums.EnumAshType.WOOD));
        recipes.put(new ItemStack[][] {ovens, misc}, OreDictManager.DictFrame.fromOne(ModItems.powder_ash, ItemEnums.EnumAshType.MISC));

        for(FluidType smoke : smokes) {
            recipes.put(new ItemStack[][] {chimneys, new ItemStack[] {ItemFluidIcon.make(smoke, 2_000)}}, OreDictManager.DictFrame.fromOne(ModItems.powder_ash, ItemEnums.EnumAshType.FLY));
            recipes.put(new ItemStack[][] {new ItemStack[] {new ItemStack(ModBlocks.chimney_industrial)}, new ItemStack[] {ItemFluidIcon.make(smoke, 8_000)}}, OreDictManager.DictFrame.fromOne(ModItems.powder_ash, ItemEnums.EnumAshType.SOOT));
        }

        return recipes;
    }
}
