package com.hbm.capability;

import com.hbm.inventory.FluidContainer;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.fluid.FluidType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Wrapper for NTM canisters
 *
 * @author mlbv
 */
public class NTMFluidContainerWrapper implements IFluidHandlerItem {

    protected ItemStack container;

    public NTMFluidContainerWrapper(ItemStack container) {
        this.container = container.copy();
    }

    private static FluidContainer findContainerData(ItemStack stack) {
        if (stack.isEmpty()) return null;
        ItemStack testStack = stack.copy();
        testStack.setCount(1);
        for (FluidContainer fc : FluidContainerRegistry.allContainers) {
            if (ItemStack.areItemStacksEqual(fc.fullContainer, testStack) || (fc.emptyContainer != null && ItemStack.areItemStacksEqual(fc.emptyContainer, testStack))) {
                return fc;
            }
        }
        return null;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        FluidContainer containerData = findContainerData(this.container);
        if (containerData == null) return new IFluidTankProperties[0];

        FluidStack contents = getContentsInternal();

        return new IFluidTankProperties[]{new IFluidTankProperties() {
            @Nullable
            @Override
            public FluidStack getContents() {
                return contents;
            }

            @Override
            public int getCapacity() {
                return containerData.content;
            }

            @Override
            public boolean canFill() {
                return contents == null;
            }

            @Override
            public boolean canDrain() {
                return contents != null;
            }

            @Override
            public boolean canFillFluidType(FluidStack fluidStack) {
                if (fluidStack == null || !canFill()) return false;
                FluidType hbmType = NTMFluidCapabilityHandler.getHbmFluidType(fluidStack.getFluid());
                return hbmType != null && FluidContainerRegistry.getContainer(hbmType, container) != null;
            }

            @Override
            public boolean canDrainFluidType(FluidStack fluidStack) {
                if (fluidStack == null || !canDrain()) return false;
                return contents != null && contents.isFluidEqual(fluidStack);
            }
        }};
    }

    @Nullable
    private FluidStack getContentsInternal() {
        FluidContainer fc = findLenientContainerData(this.container);
        if (fc == null || fc.fullContainer == null || fc.fullContainer.getItem() != this.container.getItem()) {
            return null;
        }

        FluidType hbmFluidType = fc.type;
        Fluid forgeFluid = NTMFluidCapabilityHandler.getForgeFluid(hbmFluidType);
        if (forgeFluid != null) {
            return new FluidStack(forgeFluid, fc.content);
        }
        return null;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource == null || getContentsInternal() != null) return 0;

        FluidType hbmType = NTMFluidCapabilityHandler.getHbmFluidType(resource.getFluid());
        if (hbmType == null) return 0;

        FluidContainer fillRecipe = findLenientFillRecipe(this.container, hbmType);
        if (fillRecipe == null) return 0;

        if (fillRecipe.content > resource.amount) return 0;

        if (doFill) {
            this.container = fillRecipe.fullContainer.copy();
        }
        return fillRecipe.content;
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        FluidStack contents = getContentsInternal();
        if (contents == null) return null;

        if (maxDrain < contents.amount) return null;

        if (doDrain) {
            FluidContainer fc = findLenientContainerData(this.container);
            ItemStack emptyContainer = (fc != null && fc.emptyContainer != null) ? fc.emptyContainer : ItemStack.EMPTY;
            this.container = emptyContainer.copy();
        }
        return contents.copy();
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        if (resource == null) return null;
        FluidStack contained = drain(resource.amount, false);
        if (contained == null || !contained.isFluidEqual(resource)) return null;
        return drain(resource.amount, doDrain);
    }

    @Override
    public @NotNull ItemStack getContainer() {
        return this.container;
    }

    @Nullable
    private FluidContainer findLenientFillRecipe(ItemStack emptyStack, FluidType hbmType) {
        if (emptyStack.isEmpty()) return null;
        for (FluidContainer fc : FluidContainerRegistry.allContainers) {
            if (fc.type == hbmType && fc.emptyContainer != null && fc.emptyContainer.getItem() == emptyStack.getItem() && fc.emptyContainer.getMetadata() == emptyStack.getMetadata()) {
                return fc;
            }
        }
        return null;
    }

    @Nullable
    private FluidContainer findLenientContainerData(ItemStack stack) {
        if (stack.isEmpty()) return null;
        for (FluidContainer fc : FluidContainerRegistry.allContainers) {
            if (fc.fullContainer != null && fc.fullContainer.getItem() == stack.getItem() && fc.fullContainer.getMetadata() == stack.getMetadata())
                return fc;
            if (fc.emptyContainer != null && fc.emptyContainer.getItem() == stack.getItem() && fc.emptyContainer.getMetadata() == stack.getMetadata())
                return fc;
        }
        return null;
    }
}