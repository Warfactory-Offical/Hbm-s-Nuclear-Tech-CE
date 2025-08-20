package com.hbm.crafting.handlers;

import com.hbm.items.ModItems;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class CargoShellCraftingHandler extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    @Override
    public boolean matches(InventoryCrafting inventory, World world) {
        int itemCount = 0;
        int shellCount = 0;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getStackInRowAndColumn(i % 3, i / 3);

            if (!stack.isEmpty()) {
                if (stack.getItem().hasContainerItem(stack))
                    return false;

                itemCount++;

                if (stack.getItem() == ModItems.ammo_arty && stack.getMetadata() == 8 && !stack.hasTagCompound()) {
                    shellCount++;
                }
            }
        }

        return itemCount == 2 && shellCount == 1;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventory) {
        ItemStack shell = ItemStack.EMPTY;
        ItemStack cargo = ItemStack.EMPTY;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getStackInRowAndColumn(i % 3, i / 3);

            if (stack.isEmpty())
                continue;

            if (stack.getItem() == ModItems.ammo_arty && stack.getMetadata() == 8 && !stack.hasTagCompound()) {
                ItemStack copy = stack.copy();
                copy.setCount(1);
                shell = copy;
            } else {
                ItemStack copy = stack.copy();
                copy.setCount(1);
                cargo = copy;
            }
        }

        if (shell.isEmpty() || cargo.isEmpty())
            return ItemStack.EMPTY;

        if (!shell.hasTagCompound())
            shell.setTagCompound(new NBTTagCompound());

        shell.getTagCompound().setTag("cargo", cargo.writeToNBT(new NBTTagCompound()));

        return shell;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 9;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(ModItems.ammo_shell, 1, 8);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inventory) {
        return NonNullList.withSize(inventory.getSizeInventory(), ItemStack.EMPTY);
    }
}
