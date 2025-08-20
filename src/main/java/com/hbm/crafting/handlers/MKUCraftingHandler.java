package com.hbm.crafting.handlers;

import com.hbm.items.ModItems;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MKUCraftingHandler extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    public static ItemStack[] MKURecipe;
    private static long lastSeed;

    public static void generateRecipe(World world) {
        Random rand = new Random(world.getSeed());

        if (lastSeed == world.getSeed() && MKURecipe != null || world.provider == null)
            return;

        lastSeed = world.getSeed();

        List<ItemStack> list = Arrays.asList(
                new ItemStack(ModItems.powder_iodine),
                new ItemStack(ModItems.powder_fire),
                new ItemStack(ModItems.dust),
                new ItemStack(ModItems.ingot_mercury),
                new ItemStack(ModItems.morning_glory),
                new ItemStack(ModItems.syringe_metal_empty),
                null, null, null);

        Collections.shuffle(list, rand);

        MKURecipe = list.toArray(new ItemStack[9]);
    }

    @Override
    public boolean matches(@NotNull InventoryCrafting inventory, @NotNull World world) {
        // Do not change the try-catch here, it's a workaround for mod compatibility
        // where some mods attempt to read the recipe before the world is initialized
        try {
            if (!world.getWorldInfo().isInitialized() || lastSeed == world.getSeed() && MKURecipe != null) return false;
        } catch (NullPointerException ignored) {
            return false;
        }

        if (MKURecipe == null || world.getSeed() != lastSeed)
            generateRecipe(world);

        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getStackInRowAndColumn(i % 3, i / 3);
            ItemStack recipe = MKURecipe[i];

            if (stack.isEmpty() && recipe == null)
                continue;

            if (!stack.isEmpty() && recipe != null && stack.getItem() == recipe.getItem() && stack.getItemDamage() == recipe.getItemDamage())
                continue;

            return false;
        }

        return true;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventory) {
        return getRecipeOutput();
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(ModItems.syringe_mkunicorn);
    }

    @Override
    public boolean canFit(int width, int height) {
        return width >= 3 && height >= 3;
    }
}