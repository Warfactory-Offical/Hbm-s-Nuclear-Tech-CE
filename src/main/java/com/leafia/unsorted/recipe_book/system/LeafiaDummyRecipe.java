package com.leafia.unsorted.recipe_book.system;

import com.hbm.inventory.RecipesCommon.AStack;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class LeafiaDummyRecipe implements IRecipe {
    public final Item item;
    public final AStack[] input;
    public final int amount;
    public boolean isAvailable = true;
    public Map<String,Object> tags = new HashMap<>();
    public LeafiaDummyRecipe(Item item) {
        this.item = item;
        this.input = new AStack[]{};
        this.amount = 1;
    }
    public LeafiaDummyRecipe(Item item,AStack... input) {
        this.item = item;
        this.input = input;
        this.amount = 1;
    }
    public LeafiaDummyRecipe(Item item,int amount) {
        this.item = item;
        this.input = new AStack[]{};
        this.amount = amount;
    }
    public LeafiaDummyRecipe(Item item,int amount,AStack... input) {
        this.item = item;
        this.input = input;
        this.amount = amount;
    }
    public static LeafiaDummyRecipe copy(LeafiaDummyRecipe base) {
        LeafiaDummyRecipe clone = new LeafiaDummyRecipe(base.item,base.amount,base.input);
        clone.tags = base.tags;
        clone.isAvailable = base.isAvailable;
        return clone;
    }
    public LeafiaDummyRecipe copy() {
        return copy(this);
    }
    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(item,amount);
    }
    @Override public boolean matches(InventoryCrafting inv,World worldIn) { return false; }
    @Override public ItemStack getCraftingResult(InventoryCrafting inv) { return null; }
    @Override public boolean canFit(int width,int height) { return false; }
    @Override public IRecipe setRegistryName(ResourceLocation name) { return null; }
    @Nullable @Override public ResourceLocation getRegistryName() { return null; }
    @Override public Class<IRecipe> getRegistryType() { return null; }
}
