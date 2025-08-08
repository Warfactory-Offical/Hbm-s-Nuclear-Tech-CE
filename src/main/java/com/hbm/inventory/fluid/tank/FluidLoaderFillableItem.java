package com.hbm.inventory.fluid.tank;

import com.hbm.api.fluidmk2.IFillableItem;
import com.hbm.handler.ArmorModHandler;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

/**
 * This is intentionally different from upstream.
 * The 1.7 version has a latent bug in armor mod handling, not writing the modified mod back to the armor's NBT.
 * @author hbm, mlbv
 */
public class FluidLoaderFillableItem implements IFluidLoadingHandler {

    @Override
    public boolean fillItem(IItemHandler slots, int in, int out, FluidTankNTM tank) {
        if (tank.pressure != 0) return false;
        ItemStack inputStack = slots.getStackInSlot(in);
        ItemStack outputStack = slots.getStackInSlot(out);
        if (inputStack.isEmpty() || !outputStack.isEmpty()) {
            return false;
        }
        ItemStack armorCopy = inputStack.copy();
        armorCopy.setCount(1);
        if (fill(armorCopy, tank)) {
            slots.extractItem(in, 1, false);
            slots.insertItem(out, armorCopy, false);
            return true;
        }
        return false;
    }

    private static boolean fill(ItemStack stack, FluidTankNTM tank) {
        if (tank.getFill() <= 0) return false;

        boolean changed = false;
        FluidType type = tank.getTankType();

        if (stack.getItem() instanceof ItemArmor && ArmorModHandler.hasMods(stack)) {
            ItemStack[] mods = ArmorModHandler.pryMods(stack);
            for (ItemStack mod : mods) {
                if (tank.getFill() <= 0) break;

                if (mod != null && !mod.isEmpty() && mod.getItem() instanceof IFillableItem fillableMod) {
                    if (fillableMod.acceptsFluid(type, mod)) {
                        int amountToFill = tank.getFill();
                        int remainder = fillableMod.tryFill(type, amountToFill, mod);

                        if (remainder < amountToFill) {
                            int amountFilled = amountToFill - remainder;
                            tank.setFill(tank.getFill() - amountFilled);
                            ArmorModHandler.applyMod(stack, mod);
                            changed = true;
                        }
                    }
                }
            }
        }

        if (stack.getItem() instanceof IFillableItem fillable) {
            if (fillable.acceptsFluid(type, stack) && tank.getFill() > 0) {
                int amountToFill = tank.getFill();
                int remainder = fillable.tryFill(type, amountToFill, stack);
                if (remainder < amountToFill) {
                    tank.setFill(remainder);
                    changed = true;
                }
            }
        }

        return changed;
    }

    @Override
    public boolean emptyItem(IItemHandler slots, int in, int out, FluidTankNTM tank) {
        ItemStack inputStack = slots.getStackInSlot(in);
        ItemStack outputStack = slots.getStackInSlot(out);
        if (inputStack.isEmpty() || !outputStack.isEmpty()) return false;
        ItemStack armorCopy = inputStack.copy();
        armorCopy.setCount(1);
        boolean wasChanged = empty(armorCopy, tank);
        if (wasChanged) {
            slots.extractItem(in, 1, false);
            slots.insertItem(out, armorCopy, false);
            return true;
        }
        return false;
    }

    private static boolean empty(ItemStack stack, FluidTankNTM tank) {
        if (tank.getFill() >= tank.getMaxFill()) return false;
        boolean success = false;
        if (stack.getItem() instanceof ItemArmor && ArmorModHandler.hasMods(stack)) {
            ItemStack[] mods = ArmorModHandler.pryMods(stack);
            for (ItemStack mod : mods) {
                if (tank.getFill() >= tank.getMaxFill()) break;
                if (empty(mod, tank)) {
                    ArmorModHandler.applyMod(stack, mod);
                    success = true;
                }
            }
        }
        if (!(stack.getItem() instanceof IFillableItem fillable)) {
            return success;
        }

        FluidType fluidToTransfer = null;
        if (tank.getTankType() == Fluids.NONE) {
            FluidType itemFluidType = fillable.getFirstFluidType(stack);
            if (itemFluidType != null && itemFluidType != Fluids.NONE) {
                tank.setTankType(itemFluidType);
                fluidToTransfer = itemFluidType;
            }
        } else {
            if (fillable.providesFluid(tank.getTankType(), stack)) {
                fluidToTransfer = tank.getTankType();
            }
        }

        if (fluidToTransfer != null) {
            int spaceInTank = tank.getMaxFill() - tank.getFill();
            if (spaceInTank > 0) {
                int amountEmptied = fillable.tryEmpty(fluidToTransfer, spaceInTank, stack);
                if (amountEmptied > 0) {
                    tank.setFill(tank.getFill() + amountEmptied);
                    success = true;
                }
            }
        }

        return success;
    }
}