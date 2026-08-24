package com.hbm.integration.ae2;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.main.MainRegistry;
import com.hbm.modules.machine.ModuleMachineBase;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Bridges AE2's crafting CPU system (appeng.api.implementations.tiles.ICraftingMachine) to NTM's
 * {@link ModuleMachineBase}-family machines: Assembly Machine, Chemical Plant, Precision Assembly,
 * Rock Mill, Fusion Reactor, PUREX and Plasma Forge.
 *
 * These machines gate item insertion behind a single "currently selected recipe" field
 * ({@link ModuleMachineBase#recipe}), normally only ever written by a player clicking the GUI's
 * recipe-select arrows (see the various TileEntity#receiveControl implementations). A plain item
 * push (Import Bus, hopper, etc) can't set that field, so ModuleMachineBase#isItemValid rejects it
 * even when it's carrying exactly the right ingredients for some recipe. This helper does what the
 * GUI does: find the recipe the incoming pattern actually represents, select it, then hand-place the
 * ingredients directly into the machine's inventory - the same trust level the GUI/container code
 * already operates at, bypassing the capability-level insertItem validation that only guards against
 * the *currently selected* recipe.
 *
 * Only the item side of a recipe can be driven this way. AE2's ICraftingPatternDetails format has no
 * concept of a fluid ingredient, so for Chemical Plant/Fusion Reactor/PUREX/Plasma Forge recipes that
 * also need fluid input, that fluid must already be arriving through a separate, permanent connection -
 * the CPU can supply the solids and select the recipe, but can't order or guarantee the fluid.
 */
public class NTMCraftingMachineHelper {

    private NTMCraftingMachineHelper() { }

    /**
     * Debug hook: call from an AE2 subclass's getCapability override, passing along what super()
     * returned. Logs any EXTERNAL (facing != null - internal GUI/container access always passes
     * facing == null, see TileEntityMachineBase's own "Contract" comment) request for the item
     * handler capability, which is the very first thing anything - AE2's Interface included - has
     * to successfully do before it can even attempt to push a pattern or insert items. Silence here
     * despite a live crafting CPU job means the Interface isn't even reaching this block at all.
     */
    public static <T> T debugCapability(TileEntity te, T result, Capability<T> capability, EnumFacing facing) {
        if (facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            MainRegistry.logger.info("[AE2] getCapability(ITEM_HANDLER) on {} from facing={} -> {}",
                    te.getClass().getSimpleName(), facing, result != null ? "provided" : "null");
        }
        return result;
    }

    /**
     * Debug hook: call from an AE2 subclass's isItemValidForSlot override, passing along what
     * super() returned. Only logs rejections - this is the exact gate that makes the generic
     * fallback path (a plain Import Bus / hopper-style push) require the recipe to already be
     * manually selected, so a burst of these for a nonempty stack right when a CPU job is ordered
     * is the signature of AE2 falling back to that path instead of using pushPattern.
     */
    public static boolean debugItemValid(TileEntity te, int slot, ItemStack stack, boolean valid) {
        if (!valid && !stack.isEmpty()) {
            MainRegistry.logger.info("[AE2] isItemValidForSlot REJECTED on {}: slot={} stack={}",
                    te.getClass().getSimpleName(), slot, stack);
        }
        return valid;
    }

    /** Refuses new plans while the machine is mid-craft or its input slots are already occupied. */
    public static boolean acceptsPlans(ModuleMachineBase module) {
        if (module.progress > 0D) {
            MainRegistry.logger.info("[AE2] acceptsPlans({}) -> false, mid-craft (progress={})", module.getClass().getSimpleName(), module.progress);
            return false;
        }
        for (int slot : module.inputSlots) {
            if (!module.inventory.getStackInSlot(slot).isEmpty()) {
                MainRegistry.logger.info("[AE2] acceptsPlans({}) -> false, input slot {} occupied by {}", module.getClass().getSimpleName(), slot, module.inventory.getStackInSlot(slot));
                return false;
            }
        }
        return true;
    }

    public static boolean pushPattern(ModuleMachineBase module, ICraftingPatternDetails patternDetails, InventoryCrafting table) {
        MainRegistry.logger.info("[AE2] pushPattern({}) called, pattern output={}, condensedInputs={}",
                module.getClass().getSimpleName(), patternDetails.getPrimaryOutput(),
                java.util.Arrays.toString(patternDetails.getCondensedInputs()));

        if (!acceptsPlans(module)) return false;

        GenericRecipe matched = findMatchingRecipe(module, patternDetails);
        if (matched == null) {
            MainRegistry.logger.info("[AE2] pushPattern({}) -> false, no recipe in {} matched this pattern", module.getClass().getSimpleName(), module.getRecipeSet().getClass().getSimpleName());
            return false;
        }
        if (matched.inputItem == null || matched.inputItem.length == 0) return false;
        if (matched.inputItem.length > module.inputSlots.length) return false; // shouldn't happen, but don't risk voiding items

        if (module.outputSlots != null) {
            for (int slot : module.outputSlots) {
                if (!module.inventory.getStackInSlot(slot).isEmpty()) {
                    MainRegistry.logger.info("[AE2] pushPattern({}) -> false, output slot {} occupied", module.getClass().getSimpleName(), slot);
                    return false;
                }
            }
        }

        // pull the actual ingredient stacks the network supplied out of the crafting table
        List<ItemStack> pool = new ArrayList<>();
        for (int i = 0; i < table.getSizeInventory(); i++) {
            ItemStack stack = table.getStackInSlot(i);
            if (!stack.isEmpty()) pool.add(stack.copy());
        }

        ItemStack[] placement = new ItemStack[matched.inputItem.length];
        for (int i = 0; i < matched.inputItem.length; i++) {
            ItemStack found = extractFromPool(pool, matched.inputItem[i]);
            if (found == null) {
                MainRegistry.logger.info("[AE2] pushPattern({}) -> false, matched recipe '{}' but table didn't actually contain slot {}'s ingredient (pool={})",
                        module.getClass().getSimpleName(), matched.getInternalName(), i, pool);
                return false; // network didn't actually supply what this recipe needs
            }
            placement[i] = found;
        }

        // commit: select the recipe (same field the GUI's arrows write to), then place the items
        module.recipe = matched.getInternalName();
        for (int i = 0; i < placement.length; i++) {
            module.inventory.setStackInSlot(module.inputSlots[i], placement[i]);
        }
        module.markDirty = true;
        MainRegistry.logger.info("[AE2] pushPattern({}) -> true, selected recipe '{}'", module.getClass().getSimpleName(), matched.getInternalName());
        return true;
    }

    /** Finds the GenericRecipe this pattern was encoded from, matching both inputs and primary output. */
    @SuppressWarnings("unchecked")
    private static GenericRecipe findMatchingRecipe(ModuleMachineBase module, ICraftingPatternDetails patternDetails) {
        IAEItemStack[] condensedInputs = patternDetails.getCondensedInputs();
        IAEItemStack primaryOutput = patternDetails.getPrimaryOutput();
        List<GenericRecipe> recipes = module.getRecipeSet().recipeOrderedList;

        recipeLoop:
        for (GenericRecipe recipe : recipes) {
            if (recipe.inputItem == null || recipe.inputItem.length == 0) continue;

            if (primaryOutput != null && recipe.outputItem != null && recipe.outputItem.length > 0) {
                ItemStack single = recipe.outputItem[0].getSingle();
                if (single != null && !single.isEmpty()
                        && (single.getItem() != primaryOutput.getItem() || single.getItemDamage() != primaryOutput.getItemDamage())) {
                    continue;
                }
            }

            List<IAEItemStack> scratch = new ArrayList<>(condensedInputs.length);
            for (IAEItemStack s : condensedInputs) if (s != null) scratch.add(s.copy());

            for (AStack want : recipe.inputItem) {
                boolean satisfied = false;
                for (IAEItemStack avail : scratch) {
                    if (avail.getStackSize() <= 0) continue;
                    if (avail.getStackSize() >= want.stacksize && want.matchesRecipe(avail.createItemStack(), true)) {
                        avail.decStackSize(want.stacksize);
                        satisfied = true;
                        break;
                    }
                }
                if (!satisfied) continue recipeLoop;
            }

            return recipe;
        }
        return null;
    }

    private static ItemStack extractFromPool(List<ItemStack> pool, AStack want) {
        for (ItemStack candidate : pool) {
            if (candidate.isEmpty()) continue;
            if (want.matchesRecipe(candidate, false)) {
                return candidate.splitStack(want.stacksize);
            }
        }
        return null;
    }
}
