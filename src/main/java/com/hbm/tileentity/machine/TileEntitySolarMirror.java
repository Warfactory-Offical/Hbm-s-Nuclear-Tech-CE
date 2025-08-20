package com.hbm.tileentity.machine;

import com.hbm.interfaces.AutoRegister;
import com.hbm.tileentity.TileEntityTickingBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntitySolarMirror extends TileEntityTickingBase {

    public int tX;
    public int tY;
    public int tZ;
    public boolean isOn;

    @Override
    public String getInventoryName() {
        return null;
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            if (world.getTotalWorldTime() % 20 == 0)
                networkPackNT(200);

            if (tY < pos.getY()) {
                isOn = false;
                return;
            }

            int sun = world.getLightFor(EnumSkyBlock.SKY, pos) - world.getSkylightSubtracted() - 11;

            if (sun <= 0 || !world.canSeeSky(pos.up())) {
                isOn = false;
                return;
            }

            isOn = true;

            TileEntity te = world.getTileEntity(new BlockPos(tX, tY - 1, tZ));
            if (te instanceof TileEntitySolarBoiler boiler) {
                boiler.heatInput += sun;
            }
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeInt(tX);
        buf.writeInt(tY);
        buf.writeInt(tZ);
        buf.writeBoolean(isOn);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        tX = buf.readInt();
        tY = buf.readInt();
        tZ = buf.readInt();
        isOn = buf.readBoolean();
    }

    public void setTarget(int x, int y, int z) {
        tX = x;
        tY = y;
        tZ = z;
        this.markDirty();
        networkPackNT(200);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        tX = compound.getInteger("targetX");
        tY = compound.getInteger("targetY");
        tZ = compound.getInteger("targetZ");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("targetX", tX);
        compound.setInteger("targetY", tY);
        compound.setInteger("targetZ", tZ);
        return super.writeToNBT(compound);
    }

    private AxisAlignedBB bb = null;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 25, pos.getY() - 25, pos.getZ() - 25,
                    pos.getX() + 25, pos.getY() + 25, pos.getZ() + 25
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
