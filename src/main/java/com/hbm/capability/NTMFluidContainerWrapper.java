package com.hbm.capability;

import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.fluid.FluidType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Wrapper for NTM canisters
 *
 * @author mlbv
 */
public class NTMFluidContainerWrapper implements IFluidHandlerItem {

    protected ItemStack container;

    public NTMFluidContainerWrapper(ItemStack container) {
        this.container = container.copy();
        this.container.setCount(1);
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        FluidStack contents = getContentsInternal();
        FluidContainerRegistry.FluidContainer containerData = FluidContainerRegistry.getContainerData(this.container);

        if (containerData == null) {
            return new IFluidTankProperties[0];
        }

        return new IFluidTankProperties[]{new IFluidTankProperties() {
            @Nullable
            @Override
            public FluidStack getContents() {
                return contents;
            }

            @Override
            public int getCapacity() {
                return containerData.content();
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
                return hbmType != null && FluidContainerRegistry.getFillRecipe(container, hbmType) != null;
            }

            @Override
            public boolean canDrainFluidType(FluidStack fluidStack) {
                if (fluidStack == null || !canDrain()) return false;
                return contents != null && contents.isFluidEqual(fluidStack);
            }
        }};
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource == null || getContentsInternal() != null) return 0;
        FluidType hbmType = NTMFluidCapabilityHandler.getHbmFluidType(resource.getFluid());
        if (hbmType == null) return 0;
        FluidContainerRegistry.FluidContainer fillRecipe = FluidContainerRegistry.getFillRecipe(this.container, hbmType);
        if (fillRecipe == null) return 0;
        if (resource.amount < fillRecipe.content()) return 0;
        if (doFill) this.container = fillRecipe.fullContainer().copy();
        return fillRecipe.content();
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        if (resource == null || resource.amount <= 0) return null;
        FluidStack contents = getContentsInternal();
        if (contents == null || !contents.isFluidEqual(resource) || resource.amount < contents.amount) return null;
        if (doDrain) {
            FluidContainerRegistry.FluidContainer fc = FluidContainerRegistry.getDrainRecipe(this.container);
            if (fc != null && fc.emptyContainer() != null) this.container = fc.emptyContainer().copy();
            else return null;
        }
        return contents.copy();
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        FluidStack contents = getContentsInternal();
        if (contents == null || maxDrain < contents.amount) return null;
        return this.drain(contents, doDrain);
    }

    @NotNull
    @Override
    public ItemStack getContainer() {
        return this.container;
    }

    @Nullable
    private FluidStack getContentsInternal() {
        FluidContainerRegistry.FluidContainer fc = FluidContainerRegistry.getDrainRecipe(this.container);
        if (fc == null) return null;
        Fluid forgeFluid = NTMFluidCapabilityHandler.getForgeFluid(fc.type());
        return forgeFluid != null ? new FluidStack(forgeFluid, fc.content()) : null;
    }
}