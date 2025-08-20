package com.hbm.inventory;

import com.github.bsideup.jabel.Desugar;
import com.hbm.blocks.ModBlocks;
import com.hbm.capability.NTMFluidCapabilityHandler;
import com.hbm.config.GeneralConfig;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class FluidContainerRegistry {

    public static final List<FluidContainer> allContainers = new ArrayList<>();
    private static final HashMap<Item, HashMap<Integer, FluidContainer>> fullContainerMapByItem = new HashMap<>();
    private static final HashMap<Item, HashMap<Integer, List<FluidContainer>>> emptyContainerMapByItem = new HashMap<>();

    public static void register() {
        FluidContainerRegistry.registerContainer(new FluidContainer(new ItemStack(Items.WATER_BUCKET), new ItemStack(Items.BUCKET), Fluids.WATER, 1000));
        FluidContainerRegistry.registerContainer(new FluidContainer(new ItemStack(Items.POTIONITEM), new ItemStack(Items.GLASS_BOTTLE), Fluids.WATER, 250));
        FluidContainerRegistry.registerContainer(new FluidContainer(new ItemStack(Items.LAVA_BUCKET), new ItemStack(Items.BUCKET), Fluids.LAVA, 1000));

        FluidContainerRegistry.registerContainer(new FluidContainer(new ItemStack(ModBlocks.red_barrel), new ItemStack(ModItems.tank_steel), Fluids.DIESEL, 10000));
        FluidContainerRegistry.registerContainer(new FluidContainer(new ItemStack(ModBlocks.pink_barrel), new ItemStack(ModItems.tank_steel), Fluids.KEROSENE, 10000));
        FluidContainerRegistry.registerContainer(new FluidContainer(new ItemStack(ModBlocks.lox_barrel), new ItemStack(ModItems.tank_steel), Fluids.OXYGEN, 10000));

        FluidContainerRegistry.registerContainer(new FluidContainer(new ItemStack(ModBlocks.ore_oil), null, Fluids.OIL, 250));
        FluidContainerRegistry.registerContainer(new FluidContainer(new ItemStack(ModBlocks.ore_gneiss_gas), null, Fluids.PETROLEUM, GeneralConfig.enable528 ? 50 : 250));
        FluidContainerRegistry.registerContainer(new FluidContainer(new ItemStack(ModItems.bottle_mercury), new ItemStack(Items.GLASS_BOTTLE), Fluids.MERCURY, 1000));
        FluidContainerRegistry.registerContainer(new FluidContainer(new ItemStack(ModItems.ingot_mercury), null, Fluids.MERCURY, 125));

        FluidContainerRegistry.registerContainer(new FluidContainer(new ItemStack(ModItems.particle_hydrogen), new ItemStack(ModItems.particle_empty), Fluids.HYDROGEN, 1000));
        FluidContainerRegistry.registerContainer(new FluidContainer(new ItemStack(ModItems.particle_amat), new ItemStack(ModItems.particle_empty), Fluids.AMAT, 1000));
        FluidContainerRegistry.registerContainer(new FluidContainer(new ItemStack(ModItems.particle_aschrab), new ItemStack(ModItems.particle_empty), Fluids.ASCHRAB, 1000));

        FluidContainerRegistry.registerContainer(new FluidContainer(new ItemStack(ModItems.iv_blood), new ItemStack(ModItems.iv_empty), Fluids.BLOOD, 100));
        FluidContainerRegistry.registerContainer(new FluidContainer(new ItemStack(ModItems.iv_xp), new ItemStack(ModItems.iv_xp_empty), Fluids.XPJUICE, 100));
        FluidType[] fluids = Fluids.getAll();
        for(int i = 1; i < fluids.length; i++) {

            FluidType type = fluids[i];
            int id = type.getID();

            if (type.getContainer(Fluids.CD_Canister.class) != null)
                FluidContainerRegistry.registerContainer(new FluidContainer(new ItemStack(ModItems.canister_full, 1, id), new ItemStack(ModItems.canister_empty), type, 1000));
            if (type.getContainer(Fluids.CD_Gastank.class) != null)
                FluidContainerRegistry.registerContainer(new FluidContainer(new ItemStack(ModItems.gas_full, 1, id), new ItemStack(ModItems.gas_empty), type, 1000));

            if (type.hasNoContainer()) continue;

            FluidContainerRegistry.registerContainer(new FluidContainer(new ItemStack(ModItems.fluid_tank_lead_full, 1, id), new ItemStack(ModItems.fluid_tank_lead_empty), type, 1000));

            if (type.needsLeadContainer()) continue;

            FluidContainerRegistry.registerContainer(new FluidContainer(new ItemStack(ModItems.fluid_tank_full, 1, id), new ItemStack(ModItems.fluid_tank_empty), type, 1000));
            FluidContainerRegistry.registerContainer(new FluidContainer(new ItemStack(ModItems.fluid_barrel_full, 1, id), new ItemStack(ModItems.fluid_barrel_empty), type, 16000));
        }
        for (FluidType type : com.hbm.forgefluid.SpecialContainerFillLists.EnumCell.getFluids()) {
            if (type != null) {
                FluidContainer cell = new FluidContainer(new ItemStack(ModItems.cell, 1, type.getID()), new ItemStack(ModItems.cell, 1, 0), type, 1000);
                FluidContainerRegistry.registerContainer(cell);
            }
        }
        NTMFluidCapabilityHandler.initialize();
    }

    public static void registerContainer(FluidContainer con) {
        allContainers.add(con);
        OreDictionary.registerOre(con.type().getDict(con.content()), con.fullContainer());
        fullContainerMapByItem.computeIfAbsent(con.fullContainer().getItem(), k -> new HashMap<>()).put(con.fullContainer().getMetadata(), con);

        if (con.emptyContainer() != null && !con.emptyContainer().isEmpty()) {
            emptyContainerMapByItem.computeIfAbsent(con.emptyContainer().getItem(), k -> new HashMap<>()).computeIfAbsent(con.emptyContainer().getMetadata(), k -> new ArrayList<>()).add(con);
        }
    }

    /**
     * @return  the amount of a specific fluid in a given full container stack.
     */
    public static int getFluidContent(ItemStack stack, FluidType type) {
        if (stack == null || stack.isEmpty()) return 0;
        FluidContainer recipe = getFluidContainer(stack);
        if (recipe != null && recipe.type() == type) return recipe.content();
        return 0;
    }

    /**
     * Gets the FluidType contained in a full container stack.
     */
    @NotNull
    public static FluidType getFluidType(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) return Fluids.NONE;
        FluidContainer recipe = getFluidContainer(stack);
        return recipe != null ? recipe.type() : Fluids.NONE;
    }

    /**
     * Gets the full container item for a given empty container and fluid type.
     */
    @Nullable
    public static ItemStack getFullContainer(ItemStack stack, FluidType type) {
        if (stack == null || stack.isEmpty()) return null;
        FluidContainer recipe = getFillRecipe(stack, type);
        return recipe != null ? recipe.fullContainer().copy() : null;
    }

    /**
     * Gets the empty container item for a given full container stack.
     */
    @Nullable
    public static ItemStack getEmptyContainer(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        FluidContainer recipe = getFluidContainer(stack);
        if (recipe != null && recipe.emptyContainer() != null) {
            return recipe.emptyContainer().copy();
        }
        return null;
    }

    /**
     * @return the {@link FluidContainer} of the given full container stack, or null if none is found.
     */
    @Nullable
    public static FluidContainer getFluidContainer(@NotNull ItemStack fullStack) {
        if (fullStack.isEmpty()) return null;
        HashMap<Integer, FluidContainer> metaMap = fullContainerMapByItem.get(fullStack.getItem());
        if (metaMap == null) return null;
        return metaMap.get(fullStack.getMetadata());
    }

    /**
     * @return the {@link FluidContainer} of the given empty container and {@link FluidType}, or null if none is found.
     */
    @Nullable
    public static FluidContainer getFillRecipe(@NotNull ItemStack emptyStack, @Nullable FluidType type) {
        if (emptyStack.isEmpty() || type == null) return null;
        HashMap<Integer, List<FluidContainer>> metaMap = emptyContainerMapByItem.get(emptyStack.getItem());
        if (metaMap == null) return null;
        List<FluidContainer> candidates = metaMap.get(emptyStack.getMetadata());
        if (candidates == null) return null;
        for (FluidContainer fc : candidates) {
            if (fc.type() == type) return fc;
        }
        return null;
    }

    @Nullable
    public static FluidContainer getFillRecipe(@NotNull ItemStack emptyStack, @NotNull FluidStack fluid) {
        return getFillRecipe(emptyStack, NTMFluidCapabilityHandler.getFluidType(fluid.getFluid()));
    }

    /**
     * Gets all possible fill recipes for a given empty item stack.
     *
     * @return A list of possible {@link FluidContainer} recipes, or an empty list if none are found.
     */
    @NotNull
    public static List<FluidContainer> getFillRecipes(@NotNull ItemStack emptyStack) {
        if (emptyStack.isEmpty()) return Collections.emptyList();
        HashMap<Integer, List<FluidContainer>> metaMap = emptyContainerMapByItem.get(emptyStack.getItem());
        if (metaMap == null) return Collections.emptyList();
        List<FluidContainer> candidates = metaMap.get(emptyStack.getMetadata());
        return candidates != null ? candidates : Collections.emptyList();
    }

    @Desugar
    public record FluidContainer(@NotNull ItemStack fullContainer, @Nullable ItemStack emptyContainer,
                                 @NotNull FluidType type, int content) {
    }
}