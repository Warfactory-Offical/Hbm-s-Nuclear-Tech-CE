package com.hbm.api.fluidmk2;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.uninos.GenNode;
import com.hbm.uninos.UniNodespace;
import com.hbm.util.Compat;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public interface IFluidReceiverMK2 extends IFluidUserMK2 {

    /** Sends fluid of the desired type and pressure to the receiver, returns the remainder */
    long transferFluid(FluidType type, int pressure, long amount);
    default long getReceiverSpeed(FluidType type, int pressure) { return 1_000_000_000; }
    long getDemand(FluidType type, int pressure);

    default int[] getReceivingPressureRange(FluidType type) { return DEFAULT_PRESSURE_RANGE; }

    default void trySubscribe(FluidType type, World world, DirPos pos) { trySubscribe(type, world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir()); }
    default void trySubscribe(FluidType type, World world, BlockPos pos, ForgeDirection dir) { trySubscribe(type, world, pos.getX(), pos.getY(), pos.getZ(), dir); }
    default void trySubscribe(FluidType type, World world, int x, int y, int z, ForgeDirection dir) {

        TileEntity te = Compat.getTileStandard(world, x, y, z);
        boolean red = false;

        if(te instanceof IFluidConnectorMK2) {
            IFluidConnectorMK2 con = (IFluidConnectorMK2) te;
            if(!con.canConnect(type, dir.getOpposite())) return;

            GenNode node = UniNodespace.getNode(world, new BlockPos(x, y, z), type.getNetworkProvider());

            if(node != null && node.net != null) {
                node.net.addReceiver(this);
                red = true;
            }
        }

        if(particleDebug) {
            NBTTagCompound data = new NBTTagCompound();
            data.setString("type", "network");
            data.setString("mode", "fluid");
            data.setInteger("color", type.getColor());
            double posX = x + 0.5 + dir.offsetX * 0.5 + world.rand.nextDouble() * 0.5 - 0.25;
            double posY = y + 0.5 + dir.offsetY * 0.5 + world.rand.nextDouble() * 0.5 - 0.25;
            double posZ = z + 0.5 + dir.offsetZ * 0.5 + world.rand.nextDouble() * 0.5 - 0.25;
            data.setDouble("mX", -dir.offsetX * (red ? 0.025 : 0.1));
            data.setDouble("mY", -dir.offsetY * (red ? 0.025 : 0.1));
            data.setDouble("mZ", -dir.offsetZ * (red ? 0.025 : 0.1));
            PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, posX, posY, posZ), new NetworkRegistry.TargetPoint(world.provider.getDimension(), posX, posY, posZ, 25));
        }
    }

    default IEnergyReceiverMK2.ConnectionPriority getFluidPriority() {
        return IEnergyReceiverMK2.ConnectionPriority.NORMAL;
    }
}