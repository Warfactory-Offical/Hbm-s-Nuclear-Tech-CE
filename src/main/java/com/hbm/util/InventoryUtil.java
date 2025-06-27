package com.hbm.util;

import java.util.Arrays;
import java.util.List;

import com.hbm.inventory.AnvilRecipes.AnvilOutput;
import com.hbm.inventory.RecipesCommon.AStack;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.oredict.OreDictionary;

//'t was about time
public class InventoryUtil {

	/**
	 * Will attempt to cram a much of the given itemstack into the stack array as possible
	 * The rest will be returned
	 * @param inv the stack array, usually a TE's inventory
	 * @param start the starting index (inclusive)
	 * @param end the end index (inclusive)
	 * @param stack the stack to be added to the inventory
	 * @return the remainder of the stack that could not have been added, can return null
	 */
	public static ItemStack tryAddItemToInventory(ItemStack[] inv, int start, int end, ItemStack stack) {

		ItemStack rem = tryAddItemToExistingStack(inv, start, end, stack);

		if(rem == null || rem.isEmpty())
			return ItemStack.EMPTY;

		boolean didAdd = tryAddItemToNewSlot(inv, start, end, rem);

		if(didAdd)
			return ItemStack.EMPTY;
		else
			return rem;
	}

	/**
	 * Functionally equal to tryAddItemToInventory, but will not try to create new stacks in empty slots
	 * @param inv
	 * @param start
	 * @param end
	 * @param stack
	 * @return
	 */
	public static ItemStack tryAddItemToExistingStack(ItemStack[] inv, int start, int end, ItemStack stack) {

		if(stack == null || stack.isEmpty())
			return ItemStack.EMPTY;

		for(int i = start; i <= end; i++) {

			if(doesStackDataMatch(inv[i], stack)) {

				int transfer = Math.min(stack.getCount(), inv[i].getMaxStackSize() - inv[i].getCount());

				if(transfer > 0) {
					inv[i].setCount(inv[i].getCount() + transfer);
					stack.setCount(stack.getCount() - transfer);

					if(stack.isEmpty())
						return ItemStack.EMPTY;
				}
			}
		}

		return stack;
	}

	/**
	 * Will place the stack in the first empty slot
	 * @param inv
	 * @param start
	 * @param end
	 * @param stack
	 * @return whether the stack could be added or not
	 */
	public static boolean tryAddItemToNewSlot(ItemStack[] inv, int start, int end, ItemStack stack) {

		if(stack == null || stack.isEmpty())
			return true;

		for(int i = start; i <= end; i++) {

			if(inv[i] == null || inv[i].isEmpty()) {
				inv[i] = stack;
				return true;
			}
		}

		return false;
	}

	/**
	 * Much of the same but with an ISidedInventory instance instead of a slot array
	 * @param inv
	 * @param start
	 * @param end
	 * @param stack
	 * @return
	 */
	public static ItemStack tryAddItemToInventory(IItemHandlerModifiable inv, int start, int end, ItemStack stack) {

		ItemStack rem = tryAddItemToExistingStack(inv, start, end, stack);

		if(rem.isEmpty())
			return ItemStack.EMPTY;

		boolean didAdd = tryAddItemToNewSlot(inv, start, end, rem);

		if(didAdd)
			return ItemStack.EMPTY;
		else
			return rem;
	}

	public static ItemStack tryAddItemToExistingStack(IItemHandlerModifiable inv, int start, int end, ItemStack stack) {

		if(stack == null || stack.isEmpty())
			return ItemStack.EMPTY;

		for(int i = start; i <= end; i++) {

			if(doesStackDataMatch(inv.getStackInSlot(i), stack)) {

				int transfer = Math.min(stack.getCount(), inv.getStackInSlot(i).getMaxStackSize() - inv.getStackInSlot(i).getCount());

				if(transfer > 0) {
					inv.getStackInSlot(i).setCount(inv.getStackInSlot(i).getCount() + transfer);
					stack.setCount(stack.getCount() - transfer);

					if(stack.isEmpty())
						return ItemStack.EMPTY;
				}
			}
		}

		return stack;
	}

	public static boolean tryAddItemToNewSlot(IItemHandlerModifiable inv, int start, int end, ItemStack stack) {

		if(stack == null || stack.isEmpty())
			return true;

		for(int i = start; i <= end; i++) {

			if(inv.getStackInSlot(i).isEmpty()) {
				inv.setStackInSlot(i, stack);
				return true;
			}
		}

		return false;
	}

	/**
	 * Compares item, metadata and NBT data of two stacks. Also handles null values!
	 * @param stack1
	 * @param stack2
	 * @return
	 */
	public static boolean doesStackDataMatch(ItemStack stack1, ItemStack stack2) {

		if(stack1 == null && stack2 == null)
			return true;

		if(stack1 == null && stack2 != null)
			return false;

		if(stack1 != null && stack2 == null)
			return false;

		if(stack1.getItem() != stack2.getItem())
			return false;

		if(stack1.getItemDamage() != stack2.getItemDamage())
			return false;

		if(!stack1.hasTagCompound() && !stack2.hasTagCompound())
			return true;

		if(stack1.hasTagCompound() && !stack2.hasTagCompound())
			return false;

		if(!stack1.hasTagCompound() && stack2.hasTagCompound())
			return false;

		return stack1.getTagCompound().equals(stack2.getTagCompound());
	}
	
	/**
	 * Checks if a player has matching item stacks in his inventory and removes them if so desired
	 * @param player
	 * @param stacks the AStacks (comparable or ore-dicted)
	 * @param shouldRemove whether it should just return true or false or if a successful check should also remove all the items
	 * @return whether the player has the required item stacks or not
	 */
	public static boolean doesPlayerHaveAStacks(EntityPlayer player, List<AStack> stacks, boolean shouldRemove) {
		
		NonNullList<ItemStack> original = player.inventory.mainInventory;
		ItemStack[] inventory = new ItemStack[original.size()];
		AStack[] input = new AStack[stacks.size()];
		
		//first we copy the inputs into an array because 1. it's easier to deal with and 2. we can dick around with the stack sized with no repercussions
		for(int i = 0; i < input.length; i++) {
			input[i] = stacks.get(i).copy();
		}
		
		//then we copy the inventory so we can dick around with it as well without making actual modifications to the player's inventory
		for(int i = 0; i < original.size(); i++) {
			inventory[i] = original.get(i).copy();
		}
		
		//now we go through every ingredient...
		for(int i = 0; i < input.length; i++) {
			
			AStack stack = input[i];
			
			//...and compare each ingredient to every stack in the inventory
			for(int j = 0; j < inventory.length; j++) {
				
				ItemStack inv = inventory[j];
				
				//we check if it matches but ignore stack size for now
				if(stack.matchesRecipe(inv, true)) {
					//and NOW we care about the stack size
					int size = Math.min(stack.count(), inv.getCount());
					stack.setCount(stack.count()-size);
					inv.setCount(inv.getCount()-size);
					
					//spent stacks are removed from the equation so that we don't cross ourselves later on
					if(stack.count() <= 0) {
						input[i] = null;
						break;
					}
					
					if(inv.getCount() <= 0) {
						inventory[j] = ItemStack.EMPTY;
					}
				}
			}
		}
		
		for(AStack stack : input) {
			if(stack != null) {
				return false;
			}
		}
		
		if(shouldRemove) {
			for(int i = 0; i < original.size(); i++) {
				if(inventory[i] != null && inventory[i].getCount() <= 0)
					original.set(i, ItemStack.EMPTY);
				else
					original.set(i, inventory[i]);
			}
		}
		
		return true;
	}
	
	public static void giveChanceStacksToPlayer(EntityPlayer player, List<AnvilOutput> stacks) {
		
		for(AnvilOutput out : stacks) {
			if(out.chance == 1.0F || player.getRNG().nextFloat() < out.chance) {
				if(!player.inventory.addItemStackToInventory(out.stack.copy())) {
					player.dropItem(out.stack.copy(), false);
				}
			}
		}
	}
	
	public static boolean hasOreDictMatches(EntityPlayer player, String dict, int count) {
		return countOreDictMatches(player, dict) >= count;
	}
	
	public static int countOreDictMatches(EntityPlayer player, String dict) {
		
		int count = 0;
		
		for(int i = 0; i < player.inventory.mainInventory.size(); i++) {
			
			ItemStack stack = player.inventory.mainInventory.get(i);
			
			if(stack != null) {
				
				int[] ids = OreDictionary.getOreIDs(stack);
				
				for(int id : ids) {
					if(OreDictionary.getOreName(id).equals(dict)) {
						count += stack.getCount();
						break;
					}
				}
			}
		}
		
		return count;
	}
	
	public static void consumeOreDictMatches(EntityPlayer player, String dict, int count) {
		
		for(int i = 0; i < player.inventory.mainInventory.size(); i++) {
			
			ItemStack stack = player.inventory.mainInventory.get(i);
			
			if(stack != null) {
				
				int[] ids = OreDictionary.getOreIDs(stack);
				
				for(int id : ids) {
					if(OreDictionary.getOreName(id).equals(dict)) {
						
						int toConsume = Math.min(count, stack.getCount());
						player.inventory.decrStackSize(i, toConsume);
						count -= toConsume;
						break;
					}
				}
			}
		}
	}

	public static boolean doesArrayHaveIngredients(IItemHandler inv, int start, int end, List<AStack> ingredients) {
		ItemStack[] copy = ItemStackUtil.carefulCopyArrayTruncate(inv, start, end);
		AStack[] req = new AStack[ingredients.size()];
		for (int idx = 0; idx < req.length; ++idx) {
			req[idx] = ingredients.get(idx) == null ? null : ingredients.get(idx).copy();
		}

		for (AStack ingredient : req) {
			if (ingredient == null) {
				continue;
			}

			for (ItemStack input : copy) {
				if (input == null || input.isEmpty()) {
					continue;
				}

				if (ingredient.matchesRecipe(input, true)) {
					int size = Math.min(input.getCount(), ingredient.count());
					ingredient.setCount(ingredient.count() - size);
					input.setCount(input.getCount() - size);

					if (ingredient.count() == 0) {
						break;
					}
				}
			}

			if (ingredient.count() > 0) {
				return false;
			}
		}

		return true;
	}

	public static boolean doesArrayHaveIngredients(IItemHandler inv, int start, int end, AStack... ingredients) {
		return doesArrayHaveIngredients(inv, start, end, Arrays.asList(ingredients));
	}

	public static boolean doesArrayHaveSpace(IItemHandler inv, int start, int end, ItemStack[] items) {
		ItemStack[] copy = ItemStackUtil.carefulCopyArrayTruncate(inv, start, end);

		for (ItemStack item : items) {
			if (item == null || item.isEmpty()) {
				continue;
			}

			ItemStack remainder = tryAddItemToInventory(copy, 0, copy.length - 1, item.copy());
			if (!remainder.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	public static boolean tryConsumeAStack(IItemHandlerModifiable inv, int start, int end, AStack stack) {
		AStack copy = stack.copy();
		for (int i = start; i <= end; ++i) {
			ItemStack input = inv.getStackInSlot(i);

			if (stack.matchesRecipe(input, true)) {
				int size = Math.min(copy.count(), input.getCount());
				inv.extractItem(i, size, false);
				copy.setCount(copy.count() - size);

				if (copy.count() == 0) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * A fixed re-implementation of the original Container.mergeItemStack that repects stack size and slot restrictions.
	 * @param slots
	 * @param stack
	 * @param start
	 * @param end
	 * @param reverse
	 * @return
	 */
	public static boolean mergeItemStack(List<Slot> slots, ItemStack stack, int start, int end, boolean reverse) {

		boolean success = false;
		int index = start;

		if(reverse) {
			index = end - 1;
		}

		Slot slot;
		ItemStack current;

		if(stack.isStackable()) {

			while(stack.getCount() > 0 && (!reverse && index < end || reverse && index >= start)) {
				slot = slots.get(index);
				current = slot.getStack();

				if(!current.isEmpty()) {
					int max = Math.min(stack.getMaxStackSize(), slot.getSlotStackLimit());
					int toRemove = Math.min(stack.getCount(), max);

					if(slot.isItemValid(ItemStackUtil.carefulCopyWithSize(stack, toRemove)) && current.getItem() == stack.getItem() &&
							(!stack.getHasSubtypes() || stack.getItemDamage() == current.getItemDamage()) && ItemStack.areItemStackTagsEqual(stack, current)) {

						int currentSize = current.getCount() + stack.getCount();
						if(currentSize <= max) {
							stack.setCount(0);
							current.setCount(currentSize);
							slot.putStack(current);
							success = true;
						} else if(current.getCount() < max) {
							stack.shrink(max - current.getCount());
							current.setCount(max);
							slot.putStack(current);
							success = true;
						}
					}
				}

				if(reverse) {
					--index;
				} else {
					++index;
				}
			}
		}

		if(stack.getCount() > 0) {
			if(reverse) {
				index = end - 1;
			} else {
				index = start;
			}

			while((!reverse && index < end || reverse && index >= start) && stack.getCount() > 0) {
				slot = slots.get(index);
				current = slot.getStack();

				if(current.isEmpty()) {

					int max = Math.min(stack.getMaxStackSize(), slot.getSlotStackLimit());
					int toRemove = Math.min(stack.getCount(), max);

					if(slot.isItemValid(ItemStackUtil.carefulCopyWithSize(stack, toRemove))) {
						current = stack.splitStack(toRemove);
						slot.putStack(current);
						success = true;
					}
				}

				if(reverse) {
					--index;
				} else {
					++index;
				}
			}
		}

		return success;
	}
}