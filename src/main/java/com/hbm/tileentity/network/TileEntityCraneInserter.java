package com.hbm.tileentity.network;

import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerCraneInserter;
import com.hbm.inventory.gui.GUICraneInserter;
import com.hbm.tileentity.IGUIProvider;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

@AutoRegister
public class TileEntityCraneInserter extends TileEntityCraneBase implements IGUIProvider {

    public TileEntityCraneInserter() {
        super(21);
    }

    @Override
    public String getDefaultName() {
        return "container.craneInserter";
    }

    @Override
    public void update() {
        super.update();
        if(!world.isRemote) {

            tryFillTe();

        }
    }

    public void tryFillTe(){
        EnumFacing outputSide = getOutputSide();
        TileEntity te = world.getTileEntity(pos.offset(outputSide));

        int meta = this.getBlockMetadata();
        if(te != null){
            ICapabilityProvider capte = te;
            if(capte.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, outputSide)) {
                IItemHandler cap = capte.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, outputSide);
            
                for(int i = 0; i < inventory.getSlots(); i++) {
                    tryFillContainerCap(cap, i);
                }
            }
        }
    }

    public boolean tryFillTeDirect(ItemStack stack){
        return tryInsertItemCap(inventory, stack);
    }

    //Unloads output into chests. Capability version.
    public boolean tryFillContainerCap(IItemHandler target, int invSlot) {
        ItemStack stack = inventory.getStackInSlot(invSlot);
        if(stack.isEmpty()) return false;
        return tryInsertItemCap(target, stack);
    }

    //Unloads output into chests. Capability version.
    public boolean tryInsertItemCap(IItemHandler target, ItemStack stack) {
        if(stack.isEmpty())
            return false;

        boolean movedAny = false;

        for(int i = 0; i < target.getSlots() && !stack.isEmpty(); i++) {
            ItemStack probe = stack.copy();
            probe.setCount(1);
            ItemStack simOne = target.insertItem(i, probe, true);
            if(!simOne.isEmpty()) {
                continue;
            }

            int maxTry = Math.min(stack.getCount(), target.getSlotLimit(i));
            int accepted = findMaxInsertable(target, i, stack, maxTry);

            if(accepted > 0) {
                ItemStack toInsert = stack.copy();
                toInsert.setCount(accepted);
                ItemStack rest = target.insertItem(i, toInsert, false);

                int actuallyInserted = accepted - (!rest.isEmpty() ? rest.getCount() : 0);
                if(actuallyInserted > 0) {
                    stack.shrink(actuallyInserted);
                    movedAny = true;
                }
            }
        }

        return movedAny;
    }

    private int findMaxInsertable(IItemHandler target, int slot, ItemStack stack, int upperBound) {
        int lo = 0;
        int hi = upperBound;

        while (lo < hi) {
            int mid = (lo + hi + 1) >>> 1;

            ItemStack test = stack.copy();
            test.setCount(mid);
            ItemStack res = target.insertItem(slot, test, true);

            if (res.isEmpty()) {
                lo = mid;
            } else {
                hi = mid - 1;
            }
        }

        return lo;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerCraneInserter(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUICraneInserter(player.inventory, this);
    }

}
