package com.hbm.modules.machine;

import com.hbm.api.energymk2.IEnergyHandlerMK2;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.recipes.ChemicalPlantRecipes;
import com.hbm.inventory.recipes.GenericRecipes;
import net.minecraftforge.items.ItemStackHandler;

public class ModuleMachineChemplant extends ModuleMachineBase {

    public ModuleMachineChemplant(int index, IEnergyHandlerMK2 battery, ItemStackHandler slots) {
        super(index, battery, slots);
        this.inputSlots = new int[3];
        this.outputSlots = new int[3];
        this.inputTanks = new FluidTankNTM[3];
        this.outputTanks = new FluidTankNTM[3];
    }

    @Override
    public GenericRecipes getRecipeSet() {
        return ChemicalPlantRecipes.INSTANCE;
    }

    public ModuleMachineChemplant itemInput(int a, int b, int c) { inputSlots[0] = a; inputSlots[1] = b; inputSlots[2] = c; return this; }
    public ModuleMachineChemplant itemOutput(int a, int b, int c) { outputSlots[0] = a; outputSlots[1] = b; outputSlots[2] = c; return this; }
    public ModuleMachineChemplant fluidInput(FluidTankNTM a, FluidTankNTM b, FluidTankNTM c) { inputTanks[0] = a; inputTanks[1] = b; inputTanks[2] = c; return this; }
    public ModuleMachineChemplant fluidOutput(FluidTankNTM a, FluidTankNTM b, FluidTankNTM c) { outputTanks[0] = a; outputTanks[1] = b; outputTanks[2] = c; return this; }
}
