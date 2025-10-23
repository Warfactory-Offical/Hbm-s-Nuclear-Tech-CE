package com.hbm.inventory.recipes;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.NbtComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.items.ModItems.Materials.Crystals;
import com.hbm.items.ModItems.Materials.Ingots;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.HashMap;
import java.util.LinkedHashMap;

import static com.hbm.inventory.OreDictManager.U;

public class NuclearTransmutationRecipes {

	public static LinkedHashMap<AStack, ItemStack> recipesOutput = new LinkedHashMap<>();
	public static HashMap<AStack, Long> recipesEnergy = new HashMap<>();
	
	public static void registerRecipes() {

		//input, output
		addRecipe(new OreDictStack(U.crystal()), new ItemStack(Crystals.crystal_schraranium, 1), 5_000_000L);
		addRecipe(new OreDictStack(U.ingot()), new ItemStack(Ingots.ingot_schraranium, 1), 5_000_000L);
		addRecipe(new OreDictStack(U.block()), new ItemStack(ModBlocks.block_schraranium, 1), 50_000_000L);
	}

	public static void addRecipe(AStack input, ItemStack output, long energy){
		recipesOutput.put(input, output);
		recipesEnergy.put(input, energy);
	}
	
	public static ItemStack getOutput(ItemStack stack) {
		if(stack == null || stack.isEmpty())
			return null;
		
		ItemStack outputItem = recipesOutput.get(new ComparableStack(stack));
		if(outputItem != null)
			return outputItem;
		outputItem = recipesOutput.get(new NbtComparableStack(stack));
		if(outputItem != null)
			return outputItem;
		int[] ids = OreDictionary.getOreIDs(new ItemStack(stack.getItem(), 1, stack.getItemDamage()));
		for(int id = 0; id < ids.length; id++) {
			OreDictStack oreStack = new OreDictStack(OreDictionary.getOreName(ids[id]));
			outputItem = recipesOutput.get(oreStack);
			if(outputItem != null)
				return outputItem;
		}
		return null;
	}

	public static long getEnergy(ItemStack stack) {
		if(stack == null || stack.isEmpty())
			return -1;

		Long outputItem = recipesEnergy.get(new ComparableStack(stack));
		if(outputItem != null)
			return outputItem;
		outputItem = recipesEnergy.get(new NbtComparableStack(stack));
		if(outputItem != null)
			return outputItem;
		int[] ids = OreDictionary.getOreIDs(new ItemStack(stack.getItem(), 1, stack.getItemDamage()));
		for(int id = 0; id < ids.length; id++) {
			OreDictStack oreStack = new OreDictStack(OreDictionary.getOreName(ids[id]));
			outputItem = recipesEnergy.get(oreStack);
			if(outputItem != null)
				return outputItem;
		}
		return -1;
	}
}
