package com.hbm.explosion.vanillant.standard;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.interfaces.IExplosionSFX;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class ExplosionEffectAmat implements IExplosionSFX {

	@Override
	public void doEffect(ExplosionVNT explosion, World world, double x, double y, double z, float size) {
		
		if(size < 15)
			world.playSound(x, y, z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.4F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F, false);
		else
			world.playSound(x, y, z, HBMSoundHandler.mukeExplosion, SoundCategory.BLOCKS, 15.0F, 1.0F, false);
		
		NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "amat");
		data.setFloat("scale", size);
		PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, x, y, z), new NetworkRegistry.TargetPoint(world.provider.getDimension(), x, y, z, 200));
	}
}
