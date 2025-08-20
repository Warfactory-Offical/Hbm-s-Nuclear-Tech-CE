package com.hbm.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.orbit.WorldProviderOrbit;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.handler.atmosphere.AtmosphereBlob;
import com.hbm.handler.atmosphere.ChunkAtmosphereManager;
import com.hbm.interfaces.Spaghetti;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.CapabilityContextProvider;
import com.hbm.lib.DirPos;
import com.hbm.lib.ItemStackHandlerWrapper;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

@Spaghetti("Not spaghetti in itself, but for the love of god please use this base class for all machines")
public abstract class TileEntityMachineBase extends TileEntityLoadedBase {

    public ItemStackHandler inventory;
    private boolean enablefluidWrapper = false;
    private boolean enableEnergyWrapper = false;
    private String customName;

    @Deprecated
    public TileEntityMachineBase(int scount) {
        this(scount, 64);
    }

    @Deprecated
    public TileEntityMachineBase(int scount, int slotlimit) {
        inventory = getNewInventory(scount, slotlimit);
    }

    public TileEntityMachineBase(int scount, boolean enableFluidWrapper, boolean enableEnergyWrapper){
        this(scount);
        this.enablefluidWrapper = enableFluidWrapper;
        this.enableEnergyWrapper = enableEnergyWrapper;
    }

    public TileEntityMachineBase(int scount, int slotlimit, boolean enableFluidWrapper, boolean enableEnergyWrapper){
        this(scount, slotlimit);
        this.enablefluidWrapper = enableFluidWrapper;
        this.enableEnergyWrapper = enableEnergyWrapper;
    }

    protected ItemStackHandler getNewInventory(int scount, int slotlimit) {
        return new ItemStackHandler(scount) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                markDirty();
            }

            @Override
            public int getSlotLimit(int slot) {
                return slotlimit;
            }

        };
    }

    // This is for cases like barrels - in 2.0.3 there are 6 slots instead of 4
    protected void resizeInventory(int newSlotCount) {
        ItemStackHandler newInventory = getNewInventory(newSlotCount, inventory.getSlotLimit(0));
        for (int i = 0; i < Math.min(inventory.getSlots(), newSlotCount); i++) {
            newInventory.setStackInSlot(i, inventory.getStackInSlot(i));
        }
        this.inventory = newInventory;
        markDirty();
    }

    /** The "chunks is modified, pls don't forget to save me" effect of markDirty, minus the block updates */
    public void markChanged() {
        this.world.markChunkDirty(this.pos, this);
    }

    public String getInventoryName() {
        return this.hasCustomInventoryName() ? this.customName : getName();
    }

    public abstract String getName();

    public boolean hasCustomInventoryName() {
        return this.customName != null && !this.customName.isEmpty();
    }

    public void setCustomName(String name) {
        this.customName = name;
    }

    public boolean isUseableByPlayer(EntityPlayer player) {
        if (world.getTileEntity(pos) != this) {
            return false;
        } else {
            return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 128;
        }
    }

    /**
     * It mimics the 1.7 IConditionalInvAccess behavior.
     *
     * @param side The side of the block being accessed.
     * @param accessorPos The position of the block DOING the accessing (the proxy).
     * @return An array of slots accessible from this proxy at this side.
     */
    public int[] getAccessibleSlotsFromSide(EnumFacing side, BlockPos accessorPos) {
        return getAccessibleSlotsFromSide(side);
    }

    public int[] getAccessibleSlotsFromSide(EnumFacing e) {
        return new int[]{};
    }

    public int getGaugeScaled(int i, FluidTank tank) {
        return tank.getFluidAmount() * i / tank.getCapacity();
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeBoolean(muffled);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.muffled = buf.readBoolean();
    }

    public void handleButtonPacket(int value, int meta) {
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("inventory", inventory.serializeNBT());
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("inventory"))
            inventory.deserializeNBT(compound.getCompoundTag("inventory"));
        super.readFromNBT(compound);
    }

    public boolean isItemValidForSlot(int i, ItemStack stack) {
        return true;
    }

    /**
     * Checks if an item can be inserted into a slot from a specific side and accessor position.
     * Mimics the 1.7 IConditionalInvAccess behavior.
     */
    public boolean canInsertItem(int slot, ItemStack stack, EnumFacing side, BlockPos accessorPos) {
        return canInsertItem(slot, stack);
    }

    public boolean canInsertItem(int slot, ItemStack itemStack) {
        return this.isItemValidForSlot(slot, itemStack);
    }

    /**
     * Checks if an item can be extracted from a slot from a specific side and accessor position.
     * Mimics the 1.7 IConditionalInvAccess behavior.
     */
    public boolean canExtractItem(int slot, ItemStack stack, int amount, EnumFacing side, BlockPos accessorPos) {
        return canExtractItem(slot, stack, amount);
    }

    public boolean canExtractItem(int slot, ItemStack itemStack, int amount) {
        return true;
    }

    public int countMufflers() {

        int count = 0;

        for (EnumFacing dir : EnumFacing.VALUES)
            if (world.getBlockState(pos.offset(dir)).getBlock() == ModBlocks.muffler)
                count++;

        return count;
    }

    public float getVolume(int toSilence) {

        float volume = 1 - (countMufflers() / (float) toSilence);

        return Math.max(volume, 0);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && enablefluidWrapper) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(NTMFluidHandlerWrapper.from(this));
        } else if(capability == CapabilityEnergy.ENERGY && enableEnergyWrapper) {
            return CapabilityEnergy.ENERGY.cast(NTMEnergyCapabilityWrapper.from(this));
        } else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && inventory != null) {
            if (facing == null)
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
            final BlockPos accessorPos = CapabilityContextProvider.getAccessor(this.pos);
            final EnumFacing side = facing;
            int[] accessibleSlots = getAccessibleSlotsFromSide(side, accessorPos);
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new ItemStackHandlerWrapper(inventory, accessibleSlots) {
                @Override
                public ItemStack extractItem(int slot, int amount, boolean simulate) {
                    if (canExtractItem(slot, inventory.getStackInSlot(slot), amount, side, accessorPos)) {
                        return super.extractItem(slot, amount, simulate);
                    }
                    return ItemStack.EMPTY;
                }

                @Override
                public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                    if (canInsertItem(slot, stack, side, accessorPos)) {
                        return super.insertItem(slot, stack, simulate);
                    }
                    return stack;
                }
            });
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && enablefluidWrapper) return true;
        if (capability == CapabilityEnergy.ENERGY && enableEnergyWrapper) return true;
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && inventory != null) {
            if (facing == null) return true;
            BlockPos accessorPos = CapabilityContextProvider.getAccessor(this.pos);
            return getAccessibleSlotsFromSide(facing, accessorPos).length > 0;
        }
        return super.hasCapability(capability, facing);
    }

    protected void updateRedstoneConnection(DirPos pos) {
        BlockPos blockPos = pos.getPos();
        IBlockState state1 = world.getBlockState(blockPos);
        Block block1 = state1.getBlock();

        block1.onNeighborChange(world, blockPos, this.getPos());

        block1.neighborChanged(state1, world, blockPos, this.getBlockType(), this.getPos());

        if (state1.isNormalCube()) {
            BlockPos offsetPos = blockPos.offset(pos.getDir().toEnumFacing());
            Block block2 = world.getBlockState(offsetPos).getBlock();

            if (block2.getWeakChanges(world, offsetPos)) {
                block2.onNeighborChange(world, offsetPos, this.getPos());
                block2.neighborChanged(world.getBlockState(offsetPos), world, offsetPos, this.getBlockType(), this.getPos());
            }
        }
    }

    // TODO: Consume air from connected tanks if available
    protected boolean breatheAir(int amount) {
        CBT_Atmosphere atmosphere = world.provider instanceof WorldProviderOrbit ? null : CelestialBody.getTrait(world, CBT_Atmosphere.class);
        if (atmosphere != null) {
            if (atmosphere.hasFluid(Fluids.AIR, 0.19) || atmosphere.hasFluid(Fluids.OXYGEN, 0.09)) {
                return true;
            }
        }

        List<AtmosphereBlob> blobs = ChunkAtmosphereManager.proxy.getBlobs(world, pos.getX(), pos.getY(), pos.getZ());
        for (AtmosphereBlob blob : blobs) {
            if (blob.hasFluid(Fluids.AIR, 0.19) || blob.hasFluid(Fluids.OXYGEN, 0.09)) {
                blob.consume(amount);
                return true;
            }
        }

        return false;
    }
}
