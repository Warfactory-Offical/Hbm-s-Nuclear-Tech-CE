package com.hbm.tileentity.machine;

import com.hbm.api.entity.RadarEntry;
import com.hbm.interfaces.AutoRegister;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.TileEntityLoadedBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
// FIXME: sometimes causes CME just because
@AutoRegister
public class TileEntityMachineRadarScreen extends TileEntityLoadedBase implements ITickable, IBufPacketReceiver {

    public List<RadarEntry> entries = new ArrayList<>();
    public int refX;
    public int refY;
    public int refZ;
    public int range;
    public boolean linked;

    @Override
    public void update() {

        if(!world.isRemote) {
            this.networkPackNT(100);
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeBoolean(linked);
        buf.writeInt(refX);
        buf.writeInt(refY);
        buf.writeInt(refZ);
        buf.writeInt(range);
        buf.writeInt(entries.size());
        for(RadarEntry entry : entries) entry.toBytes(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        linked = buf.readBoolean();
        refX = buf.readInt();
        refY = buf.readInt();
        refZ = buf.readInt();
        range = buf.readInt();
        int count = buf.readInt();
        this.entries.clear();
        for(int i = 0; i < count; i++) {
            RadarEntry entry = new RadarEntry();
            entry.fromBytes(buf);
            this.entries.add(entry);
        }
    }
    // fuck it, I'll make sure the data is actually SENT even after reconnecting to the world
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.linked = nbt.getBoolean("linked");
        this.refX = nbt.getInteger("refX");
        this.refY = nbt.getInteger("refY");
        this.refZ = nbt.getInteger("refZ");
        this.range = nbt.getInteger("range");
    }


    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setBoolean("linked", this.linked);
        nbt.setInteger("refX", this.refX);
        nbt.setInteger("refY", this.refY);
        nbt.setInteger("refZ", this.refZ);
        nbt.setInteger("range", this.range);
        return super.writeToNBT(nbt);
    }

    AxisAlignedBB bb = null;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if(bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 1,
                    pos.getY(),
                    pos.getZ() - 1,
                    pos.getX() + 2,
                    pos.getY() + 2,
                    pos.getZ() + 2
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
