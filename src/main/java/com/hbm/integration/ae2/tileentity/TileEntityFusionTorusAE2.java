package com.hbm.integration.ae2.tileentity;

import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import com.hbm.integration.ae2.NTMCraftingMachineHelper;
import com.hbm.tileentity.machine.fusion.TileEntityFusionTorus;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

/**
 * AE2-only variant, see TileEntityMachineAssemblyMachineAE2 for the general rationale.
 * NOTE: item side only - Fusion Reactor recipes' fluid inputs still need to be piped in
 * separately for the craft to actually complete.
 */
public class TileEntityFusionTorusAE2 extends TileEntityFusionTorus implements ICraftingMachine {

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table, EnumFacing ejectionDirection) {
        return NTMCraftingMachineHelper.pushPattern(this.fusionModule, patternDetails, table);
    }

    @Override
    public boolean acceptsPlans() {
        return NTMCraftingMachineHelper.acceptsPlans(this.fusionModule);
    }

    // --- debug instrumentation, see NTMCraftingMachineHelper#debugCapability/#debugItemValid ---
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return NTMCraftingMachineHelper.debugCapability(this, super.getCapability(capability, facing), capability, facing);
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return NTMCraftingMachineHelper.debugItemValid(this, slot, stack, super.isItemValidForSlot(slot, stack));
    }
}
