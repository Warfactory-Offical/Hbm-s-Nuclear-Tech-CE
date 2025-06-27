package com.hbm.capability;

import com.hbm.inventory.FluidContainer;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.main.MainRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Deal with fluid type conversions
 *
 * @author mlbv
 */
public class NTMFluidCapabilityHandler {

    public static final ResourceLocation HBM_FLUID_CAPABILITY = new ResourceLocation("hbm", "fluid_container_wrapper");

    private static final Set<Item> HBM_FLUID_ITEMS = new HashSet<>();
    private static final Map<FluidType, Fluid> HBM_TO_FORGE_MAP = new HashMap<>();
    private static final Map<Fluid, FluidType> FORGE_TO_HBM_MAP = new HashMap<>();

    private static boolean initialized = false;
    private static boolean containerRegistryReady = false;
    private static boolean typeHandlerReady = false;

    public static void setContainerRegistryReady() {
        containerRegistryReady = true;
        tryInitialize();
    }

    public static void setTypeHandlerReady() {
        typeHandlerReady = true;
        tryInitialize();
    }

    private static synchronized void tryInitialize() {
        if (initialized || !containerRegistryReady || !typeHandlerReady) {
            return;
        }
        for (FluidType type : Fluids.getAll()) {
            if (type == null || type == Fluids.NONE || type.getName() == null) continue;

            String hbmFluidName = type.getName();
            Fluid forgeFluid = FluidRegistry.getFluid(type.getFFName());

            if (forgeFluid != null) {
                HBM_TO_FORGE_MAP.put(type, forgeFluid);
                FORGE_TO_HBM_MAP.put(forgeFluid, type);
            } else {
                MainRegistry.logger.warn("Could not find matching Forge Fluid for HBM FluidType with name: {}", hbmFluidName);
            }
        }

        for (FluidContainer container : FluidContainerRegistry.allContainers) {
            if (container.fullContainer != null && !container.fullContainer.isEmpty()) {
                HBM_FLUID_ITEMS.add(container.fullContainer.getItem());
            }
            if (container.emptyContainer != null && !container.emptyContainer.isEmpty()) {
                HBM_FLUID_ITEMS.add(container.emptyContainer.getItem());
            }
        }

        MinecraftForge.EVENT_BUS.register(new NTMFluidCapabilityHandler());
        initialized = true;
        MainRegistry.logger.info("Initialization complete. Mapped {} fluids. Tracking {} items.", HBM_TO_FORGE_MAP.size(), HBM_FLUID_ITEMS.size());
    }

    @Nullable
    public static Fluid getForgeFluid(FluidType hbmType) {
        return HBM_TO_FORGE_MAP.get(hbmType);
    }

    @Nullable
    public static FluidType getHbmFluidType(Fluid forgeFluid) {
        return FORGE_TO_HBM_MAP.get(forgeFluid);
    }

    public static boolean isHbmFluidContainer(@NotNull Item item) {
        return HBM_FLUID_ITEMS.contains(item);
    }

    @SubscribeEvent
    public void onAttachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (stack.isEmpty() || !HBM_FLUID_ITEMS.contains(stack.getItem())) {
            return;
        }

        event.addCapability(HBM_FLUID_CAPABILITY, new ICapabilityProvider() {
            private NTMFluidContainerWrapper instance;

            @Override
            public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
                return capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY;
            }

            @Nullable
            @Override
            public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
                if (capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY) {
                    if (instance == null) {
                        instance = new NTMFluidContainerWrapper(stack);
                    }
                    return CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY.cast(instance);
                }
                return null;
            }
        });
    }
}