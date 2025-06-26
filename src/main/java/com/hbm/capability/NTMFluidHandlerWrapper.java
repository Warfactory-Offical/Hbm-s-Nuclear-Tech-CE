package com.hbm.capability;

import com.hbm.inventory.fluid.tank.FluidTankNTM;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;
import java.util.*;

public class NTMFluidHandlerWrapper implements IFluidHandler {
    protected final List<FluidTankNTM> inputTanks;
    protected final List<FluidTankNTM> outputTanks;
    /**
     * Tanks that can be both filled and drained.
     */
    protected final List<FluidTankNTM> allTanks;

    public NTMFluidHandlerWrapper(@Nullable Collection<FluidTankNTM> fillableTanks, @Nullable Collection<FluidTankNTM> drainableTanks) {
        this.inputTanks  = (fillableTanks  != null) ? new ArrayList<>(fillableTanks)  : new ArrayList<>();
        this.outputTanks = (drainableTanks != null) ? new ArrayList<>(drainableTanks) : new ArrayList<>();
        Set<FluidTankNTM> uniqueTanks = new LinkedHashSet<>(this.inputTanks);
        uniqueTanks.addAll(this.outputTanks);
        this.allTanks = new ArrayList<>(uniqueTanks);
    }

    public NTMFluidHandlerWrapper(@Nullable FluidTankNTM[] fillableTanks, @Nullable FluidTankNTM[] drainableTanks) {
        this(fillableTanks == null ? null : Arrays.asList(fillableTanks), drainableTanks == null ? null : Arrays.asList(drainableTanks));
    }

    /**
     * @param dualPurposeTanks Tanks that can be used for both filling and draining.
     */
    public NTMFluidHandlerWrapper(Collection<FluidTankNTM> dualPurposeTanks) {
        this(dualPurposeTanks, dualPurposeTanks);
    }

    /**
     * @param dualPurposeTanks Tanks that can be used for both filling and draining.
     */
    public NTMFluidHandlerWrapper(FluidTankNTM[] dualPurposeTanks) {
        this(dualPurposeTanks, dualPurposeTanks);
    }

    /**
     * @param dualPurposeTank Tank that can be used for both filling and draining.
     */
    public NTMFluidHandlerWrapper(FluidTankNTM dualPurposeTank) {
        this(Collections.singletonList(dualPurposeTank), Collections.singletonList(dualPurposeTank));
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        List<IFluidTankProperties> properties = new ArrayList<>();
        for (FluidTankNTM tank : allTanks) {
            Collections.addAll(properties, tank.getTankProperties());
        }
        return properties.toArray(new IFluidTankProperties[0]);
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource == null || resource.amount <= 0 || inputTanks.isEmpty()) {
            return 0;
        }

        int totalFilled = 0;
        FluidStack resourceLeftToFill = resource.copy();
        for (FluidTankNTM tank : inputTanks) {
            int filled = tank.fill(resourceLeftToFill, doFill);
            if (filled > 0) {
                totalFilled += filled;
                resourceLeftToFill.amount -= filled;
                if (resourceLeftToFill.amount <= 0) break;
            }
        }
        return totalFilled;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        if (resource == null || resource.amount <= 0 || outputTanks.isEmpty()) {
            return null;
        }

        FluidStack totalDrained = null;
        FluidStack resourceLeftToDrain = resource.copy();
        for (FluidTankNTM tank : outputTanks) {
            FluidStack drained = tank.drain(resourceLeftToDrain, doDrain);
            if (drained != null && drained.amount > 0) {
                if (totalDrained == null) {
                    totalDrained = drained.copy();
                } else {
                    totalDrained.amount += drained.amount;
                }
                resourceLeftToDrain.amount -= drained.amount;
                if (resourceLeftToDrain.amount <= 0) break;
            }
        }
        return totalDrained;
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        if (maxDrain <= 0 || outputTanks.isEmpty()) {
            return null;
        }
        for (FluidTankNTM tank : outputTanks) {
            FluidStack drained = tank.drain(maxDrain, doDrain);
            if (drained != null && drained.amount > 0) {
                return drained;
            }
        }
        return null;
    }
}