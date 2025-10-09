package com.hbm.tileentity.machine;

import com.hbm.api.tile.IHeatSource;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerRadioThermal;
import com.hbm.inventory.gui.GUIRadioThermal;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.RTGUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

@AutoRegister
public class TileEntityHeaterRadioThermal extends TileEntityMachineBase implements IHeatSource, ITickable, IGUIProvider {
    
    public int heatGen;
    public int heatEnergy;
    public static final int maxHeatEnergy = 750_000;

    public TileEntityHeaterRadioThermal() {
        super(15);
    }

    @Override
    public void update() {
        
        if(!world.isRemote) {
            
            this.heatEnergy *= 0.999;
            
            this.tryPullHeat();

            this.heatGen = RTGUtil.updateRTGs(inventory, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14}) * 10;
            this.heatEnergy += heatGen;
            if(heatEnergy > maxHeatEnergy) this.heatEnergy = maxHeatEnergy;

            networkPackNT(25);
        }
    }

    @Override
    public String getDefaultName() {
        return "container.heaterRadioThermal";
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(this.heatGen);
        buf.writeInt(this.heatEnergy);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.heatGen = buf.readInt();
        this.heatEnergy = buf.readInt();
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.heatEnergy = nbt.getInteger("heatEnergy");
    }
    
    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("heatEnergy", heatEnergy);
        return nbt;
    }
    
    protected void tryPullHeat() {
        TileEntity con = world.getTileEntity(pos.add(0, -1, 0));
        
        if(con instanceof IHeatSource) {
            IHeatSource source = (IHeatSource) con;
            this.heatEnergy += source.getHeatStored() * 0.85;
            source.useUpHeat(source.getHeatStored());
        }
    }

    @Override
    public int getHeatStored() {
        return this.heatEnergy;
    }

    @Override
    public void useUpHeat(int heat) {
        this.heatEnergy = Math.max(0, this.heatEnergy - heat);
    }
    
    AxisAlignedBB bb = null;
    @Override
    @Nonnull
    public AxisAlignedBB getRenderBoundingBox() {

        if (bb == null) {
            bb = new AxisAlignedBB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2);
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
        return new ContainerRadioThermal(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIRadioThermal(player.inventory, this);
    }

    public boolean hasHeatGen(){
        return heatGen > 0;
    }

    public boolean hasHeat(){
        return heatEnergy > 0;
    }

    public int getHeatGenScaled(int i){
        if(heatGen == 0) return 0;
        return (int) (Math.log(heatGen) * i / Math.log(90000));
    }

    public int getHeatScaled(int i){
        return (heatEnergy * i) / maxHeatEnergy;
    }
}
