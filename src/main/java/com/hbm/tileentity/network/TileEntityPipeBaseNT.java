package com.hbm.tileentity.network;

import com.hbm.api.fluidmk2.FluidNode;
import com.hbm.api.fluidmk2.IFluidPipeMK2;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.ForgeDirection;
import com.hbm.uninos.UniNodespace;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.world.WorldServer;

@AutoRegister
public class TileEntityPipeBaseNT extends TileEntity implements IFluidPipeMK2, ITickable {

    protected FluidNode node;
    protected FluidType type = Fluids.NONE;
    protected FluidType lastType = Fluids.NONE;

    @Override
    public void update() {
        if(world.isRemote && lastType != type) {
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
            lastType = type;
        }

        if(!world.isRemote && canUpdate()) {
            if(this.node == null || this.node.expired) {

                if(this.shouldCreateNode()) {
                    this.node = (FluidNode) UniNodespace.getNode(world, pos, type.getNetworkProvider());

                    if(this.node == null || this.node.expired) {
                        this.node = this.createNode(type);
                        UniNodespace.createNode(world, this.node);
                    }
                }
            }
        }
    }

    public boolean shouldCreateNode() {
        return true;
    }

    public FluidType getType() {
        return this.type;
    }

    public void setType(FluidType type) {
        FluidType prev = this.type;
        this.type = type;
        this.markDirty();

        if(world instanceof WorldServer) {
            WorldServer server = (WorldServer) world;
            server.getPlayerChunkMap().markBlockForUpdate(pos);
        }

        UniNodespace.destroyNode(world, pos, prev.getNetworkProvider());

        if(this.node != null) {
            this.node = null;
        }
    }

    @Override
    public boolean canConnect(FluidType type, ForgeDirection dir) {
        return dir != ForgeDirection.UNKNOWN && type == this.type;
    }

    @Override
    public void invalidate() {
        super.invalidate();

        if(!world.isRemote) {
            if(this.node != null) {
                UniNodespace.destroyNode(world, pos, type.getNetworkProvider());
            }
        }
    }

    /**
     * Only update until a power net is formed, in >99% of the cases it should be the first tick. Everything else is handled by neighbors and the net itself.
     */
    public boolean canUpdate() {
        return (this.node == null || this.node.net == null || !this.node.net.isValid()) && !this.isInvalid();
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound nbt = super.getUpdateTag();
        writeToNBT(nbt);
        return nbt;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        readFromNBT(tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.type = Fluids.fromID(nbt.getInteger("type"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("type", this.type.getID());
        return nbt;
    }
}
