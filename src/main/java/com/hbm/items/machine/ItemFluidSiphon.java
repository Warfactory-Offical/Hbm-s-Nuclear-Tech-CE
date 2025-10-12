package com.hbm.items.machine;

import com.hbm.api.fluidmk2.IFluidStandardReceiverMK2;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FluidTraitSimple;
import com.hbm.items.ItemBakedBase;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemPipette;
import com.hbm.util.CompatExternal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemFluidSiphon extends ItemBakedBase {
    
    public ItemFluidSiphon(String s) { super(s); } 

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity te = CompatExternal.getCoreFromPos(world, pos);

        if(te instanceof IFluidStandardReceiverMK2) {
            FluidTankNTM[] tanks = ((IFluidStandardReceiverMK2) te).getReceivingTanks();

            boolean hasDrainedTank = false;

            // We need to iterate through the inventory for _each_ siphonable
            // tank, so we can handle fluids that can only go into certain containers
            // After we successfully siphon any fluid from a tank, we stop
            // further processing, multiple fluid types require multiple clicks
            for(FluidTankNTM tank : tanks) {
                if(tank.getFill() <= 0)
                    continue;

                ItemStack availablePipette = null;
                FluidType tankType = tank.getTankType();

                if(tankType.hasTrait(FluidTraitSimple.FT_Unsiphonable.class))
                    continue;

                for(int j = 0; j < player.inventory.mainInventory.size(); j++) {
                    ItemStack inventoryStack = player.inventory.mainInventory.get(j);
                    if(inventoryStack.isEmpty())
                        continue;

                    FluidContainerRegistry.FluidContainer container = FluidContainerRegistry.getFluidContainer(inventoryStack);

                    if(availablePipette == null && inventoryStack.getItem() instanceof ItemPipette pipette) {
                        if(!pipette.willFizzle(tankType) && pipette != ModItems.pipette_laboratory) { // Ignoring laboratory pipettes for now
                            availablePipette = inventoryStack;
                        }
                    }

                    if(container == null)
                        continue;

                    ItemStack full = FluidContainerRegistry.getFullContainer(inventoryStack, tankType);

                    while(tank.getFill() >= container.content() && inventoryStack.getCount() > 0) {
                        hasDrainedTank = true;

                        inventoryStack.shrink(1);
                        if(inventoryStack.getCount() <= 0) {
                            player.inventory.mainInventory.set(j, ItemStack.EMPTY);
                        }

                        assert full != null;
                        ItemStack filledContainer = full.copy();
                        tank.setFill(tank.getFill() - container.content());
                        player.inventory.addItemStackToInventory(filledContainer);
                    }
                }

                // If the remainder of the tank can only fit into a pipette,
                // fill a pipette with the remainder
                // Will not auto-fill fizzlable pipettes, there is no feedback
                // for the fizzle in this case, and that's a touch too unfair
                if(availablePipette != null && tank.getFill() < 1000) {
                    ItemPipette pipette = (ItemPipette) availablePipette.getItem();

                    if(pipette.acceptsFluid(tankType, availablePipette)) {
                        hasDrainedTank = true;
                        tank.setFill(pipette.tryFill(tankType, tank.getFill(), availablePipette));
                    }
                }

                if(hasDrainedTank)
                    return EnumActionResult.SUCCESS;
            }
        }

        return EnumActionResult.PASS;
    }

}
