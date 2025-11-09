package com.hbm.tileentity.network;

import com.hbm.tileentity.TileEntityMachineBase;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

public abstract class TileEntityCraneBase extends TileEntityMachineBase implements ITickable {

    public TileEntityCraneBase(int scount) {
        super(scount, false, false);
    }

    public TileEntityCraneBase(int scount, int slotlimit) {
        super(scount, slotlimit, false, false);
    }

    // extension to the meta system
    // for compatibility purposes, normal meta values are still used by default
    private EnumFacing outputOverride = null;

    // for extra stability in case the screwdriver action doesn't get synced to
    // other clients
    private EnumFacing cachedOutputOverride = null;

    @Override
    public void update() {
        if(hasWorld() && world.isRemote) {
            if(cachedOutputOverride != outputOverride) {
                world.markBlockRangeForRenderUpdate(pos, pos);
                cachedOutputOverride = outputOverride;
            }
        }
    }

    public EnumFacing getInputSide() {
        IBlockState state = world.getBlockState(pos);
        EnumFacing currentFacing = state.getValue(BlockHorizontal.FACING);
        return switch (currentFacing) {
            case NORTH -> EnumFacing.NORTH;
            case EAST -> EnumFacing.EAST;
            case WEST -> EnumFacing.WEST;
            default -> EnumFacing.SOUTH;
        };
    }

    public EnumFacing getOutputSide() {
        EnumFacing override = getOutputOverride();
        if (override != null) {
            return override;
        }
        IBlockState state = world.getBlockState(pos);
        EnumFacing currentFacing = state.getValue(BlockHorizontal.FACING);

        return switch (currentFacing) {
            case NORTH -> EnumFacing.SOUTH;
            case EAST -> EnumFacing.WEST;
            case WEST -> EnumFacing.EAST;
            default -> EnumFacing.NORTH;
        };
    }

    public EnumFacing getOutputOverride() {
        return outputOverride;
    }

    public void setOutputOverride(EnumFacing direction) {
        EnumFacing  oldSide = getOutputSide();
        if(oldSide == direction) direction = direction.getOpposite();

        outputOverride = direction;

        if(direction == getInputSide())
            setInput(oldSide);
        else
            onBlockChanged();
    }

    public void setInput(EnumFacing direction) {
        outputOverride = getOutputSide(); // save the current output, if it isn't saved yet

        EnumFacing  oldSide = getInputSide();
        if(oldSide == direction) direction = direction.getOpposite();

        boolean needSwapOutput = direction == getOutputSide();
        world.setBlockState(pos, getBlockType().getDefaultState().withProperty(BlockHorizontal.FACING, direction), needSwapOutput ? 4 : 3);

        if(needSwapOutput)
            setOutputOverride(oldSide);
    }

    protected void onBlockChanged() {
        if(!hasWorld()) return;
        world.markBlockRangeForRenderUpdate(pos, pos);
        world.notifyBlockUpdate(pos, getBlockType().getDefaultState(), getBlockType().getDefaultState(), 3);
        markDirty();
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);
        return new SPacketUpdateTileEntity(pos, 0, nbt);
    }

    @Override
    public void onDataPacket(@NotNull NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if(nbt.hasKey("CraneOutputOverride", Constants.NBT.TAG_BYTE)) {
            outputOverride = EnumFacing.values()[nbt.getByte("CraneOutputOverride")];
        } else {
                outputOverride = null;
        }
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if (outputOverride != null) {
            nbt.setByte("CraneOutputOverride", (byte) outputOverride.ordinal());
        }
        return nbt;
    }
}
