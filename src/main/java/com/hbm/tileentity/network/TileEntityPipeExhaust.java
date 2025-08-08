package com.hbm.tileentity.network;

import com.hbm.api.fluidmk2.FluidNode;
import com.hbm.api.fluidmk2.IFluidPipeMK2;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.ForgeDirection;
import com.hbm.uninos.UniNodespace;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

@AutoRegister
public class TileEntityPipeExhaust extends TileEntity implements IFluidPipeMK2, ITickable {

    protected FluidNode[] nodes = new FluidNode[3];
    protected FluidType[] smokes = new FluidType[] {Fluids.SMOKE, Fluids.SMOKE_LEADED, Fluids.SMOKE_POISON};

    public FluidType[] getSmokes() {
        return smokes;
    }

    @Override
    public void update() {
        if (!world.isRemote && canUpdate()) {
            for(int i = 0; i < getSmokes().length; i++) {
                if(this.nodes[i] == null || this.nodes[i].expired) {
                    this.nodes[i] = (FluidNode) UniNodespace.getNode(world, pos, getSmokes()[i].getNetworkProvider());

                    if(this.nodes[i] == null || this.nodes[i].expired) {
                        this.nodes[i] = this.createNode(getSmokes()[i]);
                        UniNodespace.createNode(world, this.nodes[i]);
                    }
                }
            }
        }
    }

    public boolean canUpdate() {
        return (this.nodes == null || this.nodes[0] == null || this.nodes[1] == null || this.nodes[2] == null
                || this.nodes[0].net == null || this.nodes[1].net == null || this.nodes[2].net == null
                || !this.nodes[0].net.isValid() || !this.nodes[1].net.isValid() || !this.nodes[2].net.isValid()) && !this.isInvalid();
    }

    @Override
    public void invalidate() {
        super.invalidate();

        if(!world.isRemote) {
            for(int i = 0; i < getSmokes().length; i++) {
                if(this.nodes[i] != null) {
                    UniNodespace.destroyNode(world, pos, getSmokes()[i].getNetworkProvider());
                }
            }
        }
    }

    @Override
    public boolean canConnect(FluidType type, ForgeDirection dir) {
        return dir != ForgeDirection.UNKNOWN && (type == Fluids.SMOKE || type == Fluids.SMOKE_LEADED || type == Fluids.SMOKE_POISON);
    }
}
