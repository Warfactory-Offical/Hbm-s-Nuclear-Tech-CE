package com.hbm.api.fluidmk2;

import com.hbm.lib.DirPos;
import com.hbm.uninos.GenNode;
import com.hbm.uninos.INetworkProvider;
import net.minecraft.util.math.BlockPos;

public class FluidNode extends GenNode<FluidNetMK2> {
    public FluidNode(INetworkProvider<FluidNetMK2> provider, BlockPos... positions) {
        super(provider, positions);
    }

    @Override
    public FluidNode setConnections(DirPos... connections) {
        super.setConnections(connections);
        return this;
    }
}
