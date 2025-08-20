package com.hbm.tileentity;

import com.hbm.interfaces.AutoRegister;
import net.minecraft.util.math.BlockPos;
@AutoRegister
public class TileEntityProxyDyn extends TileEntityProxyCombo {

    @Override
    public Object getCoreObject() {

        Object o = super.getCoreObject();

        if(o instanceof IProxyDelegateProvider) {
            Object delegate = ((IProxyDelegateProvider) o).getDelegateForPosition(pos);
            if(delegate != null) return delegate;
        }

        return o;
    }

    /** Based on the position of the proxy, produces a delegate instead of returning the core tile entity. God this fucking sucks. */
    public interface IProxyDelegateProvider {

        /** Returns the delegate based on the proxy's position. Retunring NULL skips the delegate and reverts back to original core behavior */
        Object getDelegateForPosition(BlockPos pos);
    }
}
