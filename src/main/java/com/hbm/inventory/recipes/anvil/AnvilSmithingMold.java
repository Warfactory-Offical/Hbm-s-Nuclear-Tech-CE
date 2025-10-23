package com.hbm.inventory.recipes.anvil;

import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.items.ModItems.Foundry;
import com.hbm.util.ItemStackUtil;
import net.minecraft.item.ItemStack;

import java.util.List;

// Th3_Sl1ze: special for Alcater - we don't use OreNames now, they're moved into MaterialShapes
public class AnvilSmithingMold extends AnvilSmithingRecipe {
	
	OreDictStack matchesPrefix;
	ItemStack[] matchesStack;

	public AnvilSmithingMold(int meta, AStack demo, Object o) {
		super(1, new ItemStack(Foundry.mold, 1, meta), demo, new ComparableStack(Foundry.mold_base));
		
		if(o instanceof OreDictStack)
			matchesPrefix = (OreDictStack) o;
		if(o instanceof ItemStack[])
			matchesStack = (ItemStack[]) o;
	}
	
	@Override
	public boolean matches(ItemStack left, ItemStack right) {
		if(!doesStackMatch(right, this.right)) return false;
		
		if(matchesPrefix != null && left.getCount() == matchesPrefix.count()) {
			List<String> names = ItemStackUtil.getOreDictNames(left);
			
			for(String name : names) {

				for(MaterialShapes shape : MaterialShapes.allShapes) for(String otherPrefix : shape.prefixes) {
					if(otherPrefix.length() > matchesPrefix.name.length() && name.startsWith(otherPrefix)) {
						return false; //ignore if there's a longer prefix that matches (i.e. a more accurate match)
					}
				}
				
				if(name.startsWith(matchesPrefix.name)) {
					return true;
				}
			}
		}
		
		if(matchesStack != null) {
			
			for(ItemStack stack : matchesStack) {
				if(left.getItem() == stack.getItem() && left.getItemDamage() == stack.getItemDamage() && left.getCount() == stack.getCount()) {
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public int matchesInt(ItemStack left, ItemStack right) {
		return matches(left, right) ? 0 : -1;
	}
	
	public int amountConsumed(int index, boolean mirrored) {
		return index;
	}
}
