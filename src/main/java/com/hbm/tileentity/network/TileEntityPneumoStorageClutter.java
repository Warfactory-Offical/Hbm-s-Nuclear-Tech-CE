package com.hbm.tileentity.network;

import com.hbm.api.fluidmk2.IFluidStandardReceiverMK2;
import com.hbm.api.ntl.IPneumaticConnector;
import com.hbm.api.ntl.ISlotMonitorProvider;
import com.hbm.api.ntl.SlotMonitor;
import com.hbm.api.ntl.StackCache;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerPneumoStorageClutter;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIPneumoStorageClutter;
import com.hbm.lib.DirPos;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.uninos.UniNodespace;
import com.hbm.uninos.networkproviders.PneumaticNetwork;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
public class TileEntityPneumoStorageClutter extends TileEntityMachineBase implements ITickable, IFluidStandardReceiverMK2, ISlotMonitorProvider, IPneumaticConnector, IGUIProvider {

    public final FluidTankNTM compair;
    public final SlotMonitor[] monitors;

    protected TileEntityPneumoTube.PneumaticNode node;
    protected boolean wasAvailable = false;

    public TileEntityPneumoStorageClutter() {
        super(6 * 9);
        this.compair = new FluidTankNTM(Fluids.AIR, 4_000).withOwner(this).withPressure(1);
        this.monitors = new SlotMonitor[6 * 9];
        for (int i = 0; i < this.monitors.length; i++) {
            this.monitors[i] = new SlotMonitor(i, this);
        }
    }

    @Override
    public String getDefaultName() {
        return "container.pneumoStorageClutter";
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            boolean isAvailable = this.isAvailable();

            if (isAvailable != wasAvailable) {
                this.wasAvailable = isAvailable;
                for (SlotMonitor monitor : monitors) monitor.availabilityHasChanged();
            }

            if (this.node == null || this.node.expired) {
                this.node = UniNodespace.getNode(world, pos, PneumaticNetwork.THE_PNEUMATIC_PROVIDER);
                if (this.node == null || this.node.expired) {
                    this.node = new TileEntityPneumoTube.PneumaticNode(pos).setConnections(
                            new DirPos(pos.getX() + 1, pos.getY(), pos.getZ(), Library.POS_X),
                            new DirPos(pos.getX() - 1, pos.getY(), pos.getZ(), Library.NEG_X),
                            new DirPos(pos.getX(), pos.getY() + 1, pos.getZ(), Library.POS_Y),
                            new DirPos(pos.getX(), pos.getY() - 1, pos.getZ(), Library.NEG_Y),
                            new DirPos(pos.getX(), pos.getY(), pos.getZ() + 1, Library.POS_Z),
                            new DirPos(pos.getX(), pos.getY(), pos.getZ() - 1, Library.NEG_Z)
                    );
                    UniNodespace.createNode(world, this.node);
                }
            }

            if (this.node != null && !this.node.expired && this.node.hasValidNet()) {
                this.node.net.storages.put(this, System.currentTimeMillis());
            }

            this.updateMonitors();
            this.networkPackNT(15);
        }
    }

    public boolean isAvailable() {
        return this.isLoaded && !this.isInvalid();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (!world.isRemote) {
            for (SlotMonitor monitor : this.monitors) {
                for (StackCache.CacheSlot cache : monitor.viewedBy) cache.removeMonitor(monitor);
            }
            if (this.node != null && !this.node.expired && this.node.hasValidNet()) {
                this.node.net.storages.remove(this);
            }
            if (this.node != null) {
                UniNodespace.destroyNode(world, pos, PneumaticNetwork.THE_PNEUMATIC_PROVIDER);
                this.node = null;
            }
        }
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        if (!world.isRemote) {
            for (SlotMonitor monitor : this.monitors) {
                for (StackCache.CacheSlot cache : monitor.viewedBy) cache.removeMonitor(monitor);
            }
            if (this.node != null && !this.node.expired && this.node.hasValidNet()) {
                this.node.net.storages.remove(this);
            }
        }
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return true;
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[] { this.compair };
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[] { this.compair };
    }

    @Override
    public SlotMonitor[] getMonitors() {
        return this.monitors;
    }

    @Override
    public ItemStack getSlotAt(int index) {
        return this.inventory.getStackInSlot(index);
    }

    @Override
    public long getAmountAt(int index) {
        ItemStack stack = getSlotAt(index);
        return stack.isEmpty() ? 0 : stack.getCount();
    }

    @Override
    public long useUpItem(int index, long amount) {
        ItemStack stack = this.inventory.getStackInSlot(index);

        if (!stack.isEmpty()) {
            int toRemove = (int) Math.min(stack.getCount(), amount);
            stack.shrink(toRemove);
            this.markDirty();
            return amount - toRemove;
        }

        return amount;
    }

    @Override
    public boolean allowTypeSetting() {
        return true;
    }

    @Override
    public long addItem(int index, long amount) {
        ItemStack stack = this.inventory.getStackInSlot(index);

        if (!stack.isEmpty()) {
            int capacity = Math.min(stack.getMaxStackSize(), this.inventory.getSlotLimit(index));
            int toAdd = (int) Math.min(amount, capacity - stack.getCount());
            stack.grow(toAdd);
            this.markDirty();
            return amount - toAdd;
        }

        return amount;
    }

    @Override
    public long setupType(int index, ItemStack zeroStack, long amount) {
        int capacity = Math.min(zeroStack.getMaxStackSize(), this.inventory.getSlotLimit(index));
        int finalSize = (int) Math.min(amount, capacity);
        ItemStack placed = zeroStack.copy();
        placed.setCount(finalSize);
        this.inventory.setStackInSlot(index, placed);
        this.markDirty();
        return amount - finalSize;
    }

    @Override
    public boolean isAvailableToCache(StackCache cache) {
        return this.isLoaded && !this.isInvalid();
    }

    @Override
    public PneumaticNetwork getRelevantNetwork() {
        if (this.node == null || this.node.expired || !this.node.hasValidNet()) return null;
        return this.node.net;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerPneumoStorageClutter(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIPneumoStorageClutter(player.inventory, this);
    }
}
