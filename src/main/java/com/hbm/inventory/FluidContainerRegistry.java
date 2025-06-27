package com.hbm.inventory;

import com.hbm.blocks.ModBlocks;
import com.hbm.capability.NTMFluidCapabilityHandler;
import com.hbm.config.GeneralConfig;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FluidContainerRegistry {

    //TODO: continue incorporating hashmaps into this
    public static List<FluidContainer> allContainers = new ArrayList<FluidContainer>();
    private static final HashMap<FluidType, List<FluidContainer>> containerMap = new HashMap<FluidType, List<FluidContainer>>();

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
        FluidContainerRegistry.registerContainer(new FluidContainer(new ItemStack(ModItems.nugget_mercury), null, Fluids.MERCURY, 125));

        FluidContainerRegistry.registerContainer(new FluidContainer(new ItemStack(ModItems.particle_hydrogen), new ItemStack(ModItems.particle_empty), Fluids.HYDROGEN, 1000));
        FluidContainerRegistry.registerContainer(new FluidContainer(new ItemStack(ModItems.particle_amat), new ItemStack(ModItems.particle_empty), Fluids.AMAT, 1000));
        FluidContainerRegistry.registerContainer(new FluidContainer(new ItemStack(ModItems.particle_aschrab), new ItemStack(ModItems.particle_empty), Fluids.ASCHRAB, 1000));

        FluidContainerRegistry.registerContainer(new FluidContainer(new ItemStack(ModItems.iv_blood), new ItemStack(ModItems.iv_empty), Fluids.BLOOD, 100));
        FluidContainerRegistry.registerContainer(new FluidContainer(new ItemStack(ModItems.iv_xp), new ItemStack(ModItems.iv_xp_empty), Fluids.XPJUICE, 100));
        FluidType[] fluids = Fluids.getAll();
        for(int i = 1; i < fluids.length; i++) {

            FluidType type = fluids[i];
            int id = type.getID();

            if(type.getContainer(Fluids.CD_Canister.class) != null) FluidContainerRegistry.registerContainer(new FluidContainer(new ItemStack(ModItems.canister_generic, 1, id), new ItemStack(ModItems.canister_empty), type, 1000));
            if(type.getContainer(Fluids.CD_Gastank.class) != null) FluidContainerRegistry.registerContainer(new FluidContainer(new ItemStack(ModItems.gas_full, 1, id), new ItemStack(ModItems.gas_empty), type, 1000));

            if(type.hasNoContainer()) continue;

            FluidContainerRegistry.registerContainer(new FluidContainer(new ItemStack(ModItems.fluid_tank_lead_full, 1, id), new ItemStack(ModItems.fluid_tank_lead_empty), type, 1000));

            if(type.needsLeadContainer()) continue;

            FluidContainerRegistry.registerContainer(new FluidContainer(new ItemStack(ModItems.fluid_tank_full, 1, id), new ItemStack(ModItems.fluid_tank_empty), type, 1000));
            FluidContainerRegistry.registerContainer(new FluidContainer(new ItemStack(ModItems.fluid_barrel_full, 1, id), new ItemStack(ModItems.fluid_barrel_empty), type, 16000));
        }
        for(FluidType type : com.hbm.forgefluid.SpecialContainerFillLists.EnumCell.getFluids()) {
            if(type != null) {
                FluidContainer cell = new FluidContainer(new ItemStack(ModItems.cell, 1, type.getID()), new ItemStack(ModItems.cell, 1, 0), type, 1000);
                FluidContainerRegistry.registerContainer(cell);
            }
        }
        NTMFluidCapabilityHandler.setContainerRegistryReady();
    }

    public static void registerContainer(FluidContainer con) {
        allContainers.add(con);
        OreDictionary.registerOre(con.type.getDict(con.content), con.fullContainer);

        if(!containerMap.containsKey(con.type))
            containerMap.put(con.type, new ArrayList<FluidContainer>());

        List<FluidContainer> items = containerMap.get(con.type);
        items.add(con);
    }

    public static List<FluidContainer> getContainers(FluidType type) {
        return containerMap.get(type);
    }

    @Nullable
    public static FluidContainer getContainer(FluidType type, ItemStack stack) {
        if(stack == null)
            return null;

        ItemStack sta = stack.copy();
        sta.setCount(1);

        if (!containerMap.containsKey(type))
            return null;

        for (FluidContainer container : getContainers(type)) {
            if (ItemStack.areItemStacksEqual(container.emptyContainer, sta) && ItemStack.areItemStackTagsEqual(container.emptyContainer, sta)) {
                return container;
            }
        }

        return null;
    }

    public static int getFluidContent(ItemStack stack, FluidType type) {

        if(stack == null)
            return 0;

        ItemStack sta = stack.copy();
        sta.setCount(1);

        if (!containerMap.containsKey(type))
            return 0;

        for(FluidContainer container : containerMap.get(type)) {
            if(ItemStack.areItemStacksEqual(container.fullContainer, sta) && ItemStack.areItemStackTagsEqual(container.fullContainer, sta))
                return container.content;
        }

        return 0;
    }

    public static FluidType getFluidType(ItemStack stack) {

        if(stack == null)
            return Fluids.NONE;

        ItemStack sta = stack.copy();
        sta.setCount(1);

        for(FluidContainer container : allContainers) {
            if(ItemStack.areItemStacksEqual(container.fullContainer, sta) && ItemStack.areItemStackTagsEqual(container.fullContainer, sta))
                return container.type;
        }

        return Fluids.NONE;
    }

    @Nullable
    public static ItemStack getFullContainer(ItemStack stack, FluidType type) {
        if(stack.isEmpty())
            return null;

        ItemStack sta = stack.copy();
        sta.setCount(1);

        if (!containerMap.containsKey(type))
            return null;

        for(FluidContainer container : containerMap.get(type)) {
            if (container.emptyContainer == null) continue;
            if(ItemStack.areItemStacksEqual(container.emptyContainer, sta) &&  ItemStack.areItemStackTagsEqual(container.emptyContainer, sta))
                return container.fullContainer.copy();
        }

        return null;
    }

    @Nullable
    public static ItemStack getEmptyContainer(ItemStack stack) {
        if(stack == null)
            return null;

        ItemStack sta = stack.copy();
        sta.setCount(1);

        for(FluidContainer container : allContainers) {
            if(ItemStack.areItemStacksEqual(container.fullContainer, sta) && ItemStack.areItemStackTagsEqual(container.fullContainer, sta))
                return container.emptyContainer == null ? null : container.emptyContainer.copy();
        }

        return null;
    }

}
