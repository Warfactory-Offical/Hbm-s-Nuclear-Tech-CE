package com.hbm.inventory.fluid.tank;

import com.hbm.capability.NTMFluidCapabilityHandler;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;

public class FluidLoaderForge implements IFluidLoadingHandler {

    @Override
    public boolean fillItem(IItemHandler slots, int in, int out, FluidTankNTM tank) {
        if (tank.pressure != 0) {
            return false;
        }
        ItemStack inputStack = slots.getStackInSlot(in);
        FluidType tankFluidType = tank.getTankType();
        if (inputStack.isEmpty() || tank.getFill() <= 0 || tankFluidType == Fluids.NONE) {
            return false;
        }
        Fluid forgeFluid = NTMFluidCapabilityHandler.getForgeFluid(tankFluidType);
        if (forgeFluid == null) {
            return false;
        }
        ItemStack singleItemCopy = inputStack.copy();
        singleItemCopy.setCount(1);
        if (!singleItemCopy.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            return false;
        }
        IFluidHandlerItem handler = singleItemCopy.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (handler == null) {
            return false;
        }

        int amountToTransfer = handler.fill(new FluidStack(forgeFluid, tank.getFill()), false);

        if (amountToTransfer <= 0) {
            return false;
        }

        handler.fill(new FluidStack(forgeFluid, amountToTransfer), true);
        ItemStack filledContainer = handler.getContainer();

        ItemStack outputStack = slots.getStackInSlot(out);
        if (!outputStack.isEmpty() && (!ItemStack.areItemsEqual(outputStack, filledContainer) || !ItemStack.areItemStackTagsEqual(outputStack, filledContainer) || outputStack.getCount() >= outputStack.getMaxStackSize())) {
            return false;
        }
        tank.setFill(tank.getFill() - amountToTransfer);
        slots.getStackInSlot(in).shrink(1);
        if (outputStack.isEmpty()) {
            slots.insertItem(out, filledContainer, false);
        } else {
            slots.getStackInSlot(out).grow(1);
        }
        return true;
    }

    @Override
    public boolean emptyItem(IItemHandler slots, int in, int out, FluidTankNTM tank) {
        ItemStack inputStack = slots.getStackInSlot(in);
        if (inputStack.isEmpty()) return false;

        ItemStack singleItemCopy = inputStack.copy();
        singleItemCopy.setCount(1);

        if (!singleItemCopy.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            return false;
        }

        IFluidHandlerItem handler = singleItemCopy.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (handler == null) {
            return false;
        }
        FluidStack fluidInContainer = handler.drain(Integer.MAX_VALUE, false);

        if (fluidInContainer == null || fluidInContainer.amount <= 0) {
            return false;
        }
        FluidType ntmType = NTMFluidCapabilityHandler.getHbmFluidType(fluidInContainer.getFluid());

        if (ntmType == null || ntmType == Fluids.NONE) {
            return false;
        }
        if (tank.getTankType() != Fluids.NONE && tank.getTankType() != ntmType) {
            return false;
        }
        int spaceInTank = tank.getMaxFill() - tank.getFill();
        int amountToDrain = Math.min(spaceInTank, fluidInContainer.amount);

        if (amountToDrain <= 0) {
            return false;
        }
        handler.drain(amountToDrain, true);
        ItemStack emptyContainer = handler.getContainer();
        ItemStack outputStack = slots.getStackInSlot(out);
        if (!outputStack.isEmpty() && (!ItemStack.areItemsEqual(outputStack, emptyContainer) || !ItemStack.areItemStackTagsEqual(outputStack, emptyContainer) || outputStack.getCount() >= outputStack.getMaxStackSize())) {
            return false;
        }
        if (tank.getTankType() == Fluids.NONE) {
            tank.setTankType(ntmType);
        }
        tank.setFill(tank.getFill() + amountToDrain);
        slots.getStackInSlot(in).shrink(1);
        if (outputStack.isEmpty()) {
            slots.insertItem(out, emptyContainer, false);
        } else {
            slots.getStackInSlot(out).grow(1);
        }
        return true;
    }
}