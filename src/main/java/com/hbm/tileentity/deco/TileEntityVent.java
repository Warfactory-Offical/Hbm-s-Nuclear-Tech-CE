package com.hbm.tileentity.deco;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.Random;

@AutoRegister
public class TileEntityVent extends TileEntity implements ITickable {

	Random rand = new Random();
	
	@Override
	public void update() {
		if(!world.isRemote && world.isBlockPowered(pos)) {
			Block b = world.getBlockState(pos).getBlock();

			if(b == ModBlocks.vent_chlorine) {
				//if(rand.nextInt(1) == 0) {
					double x = rand.nextGaussian() * 1.5;
					double y = rand.nextGaussian() * 1.5;
					double z = rand.nextGaussian() * 1.5;

					if(!world.getBlockState(new BlockPos(pos.getX() + (int)x, pos.getY() + (int)y, pos.getZ() + (int)z)).isNormalCube()) {

						NBTTagCompound data = new NBTTagCompound();
						data.setDouble("moX", x / 2.0D);
						data.setDouble("moY", y / 2.0D);
						data.setDouble("moZ", z / 2.0D);
						data.setString("type", "chlorinefx");
						PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, pos.getX() + (int) x, pos.getY() + (int) y, pos.getZ() + (int) z), new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX() + (int) x, pos.getY() + (int) y, pos.getZ() + (int) z, 128));
					}
				//}
			}
			if(b == ModBlocks.vent_cloud) {
				//if(rand.nextInt(50) == 0) {
				double x = rand.nextGaussian() * 1.75;
				double y = rand.nextGaussian() * 1.75;
				double z = rand.nextGaussian() * 1.75;

				if(!world.getBlockState(new BlockPos(pos.getX() + (int)x, pos.getY() + (int)y, pos.getZ() + (int)z)).isNormalCube()) {

					NBTTagCompound data = new NBTTagCompound();
					data.setDouble("moX", x / 2.0D);
					data.setDouble("moY", y / 2.0D);
					data.setDouble("moZ", z / 2.0D);
					data.setString("type", "cloudfx");
					PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, pos.getX() + (int) x, pos.getY() + (int) y, pos.getZ() + (int) z), new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX() + (int) x, pos.getY() + (int) y, pos.getZ() + (int) z, 128));
				}
				//}
			}
			if(b == ModBlocks.vent_pink_cloud) {
				//if(rand.nextInt(65) == 0) {
				double x = rand.nextGaussian() * 2;
				double y = rand.nextGaussian() * 2;
				double z = rand.nextGaussian() * 2;

				if(!world.getBlockState(new BlockPos(pos.getX() + (int)x, pos.getY() + (int)y, pos.getZ() + (int)z)).isNormalCube()) {

					NBTTagCompound data = new NBTTagCompound();
					data.setDouble("moX", x / 2.0D);
					data.setDouble("moY", y / 2.0D);
					data.setDouble("moZ", z / 2.0D);
					data.setString("type", "pinkcloudfx");
					PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, pos.getX() + (int) x, pos.getY() + (int) y, pos.getZ() + (int) z), new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX() + (int) x, pos.getY() + (int) y, pos.getZ() + (int) z, 128));
				}
			}
		}
	}

}
