package com.hbm.inventory.fluid.tank;

import com.hbm.api.fluidmk2.IFillableItem;
import com.hbm.handler.ArmorModHandler;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Contract;

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
        if (inputStack.getCount() > 1) {
            ItemStack armorCopy = inputStack.copy();
            armorCopy.setCount(1);
            if (fill(armorCopy, tank)) {
                slots.extractItem(in, 1, false);
                slots.insertItem(out, armorCopy, false);
                return true;
            }
            return false;
        }

        ItemStack singleItem = slots.extractItem(in, 1, false);
        if (singleItem.isEmpty()) return false;

        ItemStack workingCopy = singleItem.copy();
        boolean changed = fill(workingCopy, tank);

        if (!changed) {
            slots.insertItem(in, singleItem, false);
            return false;
        }

        boolean itemIsFull = isItemFull(workingCopy, tank.getTankType());
        boolean tankIsEmpty = tank.getFill() == 0;

        if (itemIsFull || tankIsEmpty) {
            slots.insertItem(out, workingCopy, false);
        } else {
            slots.insertItem(in, workingCopy, false);
        }

        return true;
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
        if (inputStack.getCount() > 1) {
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
        ItemStack singleItem = slots.extractItem(in, 1, false);
        if (singleItem.isEmpty()) return false;

        ItemStack workingCopy = singleItem.copy();
        boolean wasChanged = empty(workingCopy, tank);

        if (!wasChanged) {
            slots.insertItem(in, singleItem, false);
            return false;
        }

        boolean itemIsEmpty = isItemEmpty(workingCopy);
        boolean tankIsFull = tank.getFill() >= tank.getMaxFill();

        if (itemIsEmpty || tankIsFull) {
            slots.insertItem(out, workingCopy, false);
        } else {
            slots.insertItem(in, workingCopy, false);
        }

        return true;
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

    @Contract(pure = true)
    private static boolean isItemFull(ItemStack stack, FluidType type) {
        if (stack.getItem() instanceof ItemArmor && ArmorModHandler.hasMods(stack)) {
            ItemStack armorCopy = stack.copy();
            ItemStack[] mods = ArmorModHandler.pryMods(armorCopy);
            for (ItemStack mod : mods) {
                if (mod != null && !mod.isEmpty() && mod.getItem() instanceof IFillableItem fillableMod) {
                    if (fillableMod.acceptsFluid(type, mod)) {
                        if (fillableMod.tryFill(type, 1, mod.copy()) < 1) {
                            return false;
                        }
                    }
                }
            }
        }
        if (stack.getItem() instanceof IFillableItem fillable) {
            if (fillable.acceptsFluid(type, stack)) {
                return fillable.tryFill(type, 1, stack.copy()) >= 1;
            }
        }

        return true;
    }

    @Contract(pure = true)
    private static boolean isItemEmpty(ItemStack stack) {
        if (stack.getItem() instanceof ItemArmor && ArmorModHandler.hasMods(stack)) {
            ItemStack armorCopy = stack.copy();
            ItemStack[] mods = ArmorModHandler.pryMods(armorCopy);
            for (ItemStack mod : mods) {
                if (mod != null && !mod.isEmpty() && mod.getItem() instanceof IFillableItem fillableMod) {
                    FluidType modFluid = fillableMod.getFirstFluidType(mod);
                    if (modFluid != null && modFluid != Fluids.NONE) {
                        if (fillableMod.tryEmpty(modFluid, 1, mod.copy()) > 0) {
                            return false;
                        }
                    }
                }
            }
        }
        if (stack.getItem() instanceof IFillableItem fillable) {
            FluidType itemFluid = fillable.getFirstFluidType(stack);
            if (itemFluid != null && itemFluid != Fluids.NONE) {
                return fillable.tryEmpty(itemFluid, 1, stack.copy()) <= 0;
            }
        }
        return true;
    }
}
