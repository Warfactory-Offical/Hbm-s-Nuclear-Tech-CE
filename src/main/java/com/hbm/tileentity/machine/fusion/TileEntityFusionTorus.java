package com.hbm.tileentity.machine.fusion;

import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerFusionTorus;
import com.hbm.inventory.gui.GUIFusionTorus;
import com.hbm.lib.DirPos;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.machine.albion.TileEntityCooledBase;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
public class TileEntityFusionTorus extends TileEntityCooledBase implements IGUIProvider {

    public TileEntityFusionTorus() {
        super(3);
    }

    @Override
    public String getDefaultName() {
        return "container.fusionTorus";
    }

    public void updateEntity() {

    }

    public long getMaxPower() {
        return 0;
    }

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

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerFusionTorus(player.inventory, getCheckedInventory());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIFusionTorus(player.inventory, this);
    }
}
