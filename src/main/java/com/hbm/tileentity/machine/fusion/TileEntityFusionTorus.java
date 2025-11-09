package com.hbm.tileentity.machine.fusion;

import com.hbm.lib.DirPos;
import com.hbm.tileentity.machine.albion.TileEntityCooledBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityFusionTorus extends TileEntityCooledBase {

    public TileEntityFusionTorus() {
        super(3);
    }

    @Override
    public String getDefaultName() {
        return "container.fusionTorus";
    }

    public void updateEntity() {

    }

    @Override
    public long getMaxPower() {
        return 0;
    }

    @Override
    public DirPos[] getConPos() {
        return null;
    }

    AxisAlignedBB bb = null;


    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if(bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 8,
                    pos.getY(),
                    pos.getZ() - 8,
                    pos.getX() + 9,
                    pos.getY() + 5,
                    pos.getZ() + 9
            );
        }

        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }
}
