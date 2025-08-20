package com.hbm.crafting.handlers;

import com.hbm.inventory.material.Mats;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemScraps;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class ScrapsCraftingHandler extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    @Override
    public boolean matches(InventoryCrafting inventory, World world) {
        Mats.MaterialStack mat = null;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getStackInRowAndColumn(i % 3, i / 3);

            if (stack.isEmpty()) continue;
            if (stack.getItem() != ModItems.scraps) return false;
            if (mat != null) return false;

            mat = ItemScraps.getMats(stack);
            if (mat.amount < 2) return false;
        }

        return mat != null;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventory) {
        Mats.MaterialStack mat = null;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getStackInRowAndColumn(i % 3, i / 3);

            if (stack.isEmpty()) continue;
            if (stack.getItem() != ModItems.scraps) return ItemStack.EMPTY;
            if (mat != null) return ItemStack.EMPTY;

            mat = ItemScraps.getMats(stack);
            if (mat.amount < 2) return ItemStack.EMPTY;
        }

        if (mat == null) return ItemStack.EMPTY;

        ItemStack scrap = ItemScraps.create(new Mats.MaterialStack(mat.material, mat.amount / 2));
        scrap.setCount(2);
        return scrap;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(ModItems.scraps);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inventory) {
        return NonNullList.withSize(inventory.getSizeInventory(), ItemStack.EMPTY);
    }
}
