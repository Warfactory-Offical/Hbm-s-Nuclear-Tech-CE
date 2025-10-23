package com.hbm.particle.bullet_hit;

import com.hbm.config.GeneralConfig;
import com.hbm.entity.projectile.EntityBulletBase;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.ModDamageSource;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.PacketSpecialDeath;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.Map.Entry;

public class EntityHitDataHandler {

	public static Map<EntityLivingBase, List<BulletHit>> hitData = new HashMap<>();
	
	public static void updateSystem(){
		if(hitData.isEmpty())
			return;
		Iterator<Entry<EntityLivingBase, List<BulletHit>>> itr = hitData.entrySet().iterator();
		while(itr.hasNext()){
			Entry<EntityLivingBase, List<BulletHit>> entry = itr.next();
			if(GeneralConfig.bloodFX && entry.getValue().size() > 8 && entry.getKey().getHealth() <= 0){
				EntityLivingBase ent = entry.getKey();
				List<BulletHit> val = entry.getValue();
				BulletHit hit = val.get(val.size()-1);
				DamageSource source = ModDamageSource.causeBulletGibDamage(hit.bullet, hit.bullet.shooter == null ? hit.bullet : hit.bullet.shooter);
				PacketDispatcher.wrapper.sendToAllTracking(new PacketSpecialDeath(ent, 4), ent);
				ent.getCombatTracker().trackDamage(source, ent.getHealth(), ent.getHealth());
				ent.setDead();
				ent.onDeath(source);
				//For ender dragon so it spawns a portal and all that
				ent.onKillCommand();
				
				if(ent instanceof EntityPlayerMP){
					PacketDispatcher.wrapper.sendTo(new PacketSpecialDeath(ent, 4), (EntityPlayerMP) ent);
				}
				ent.world.playSound(null, ent.posX, ent.posY, ent.posZ, HBMSoundHandler.mob_gib, SoundCategory.HOSTILE, 1, 1);
				itr.remove();
				continue;
			}
			Iterator<BulletHit> listItr = entry.getValue().iterator();
			while(listItr.hasNext()){
				BulletHit hit = listItr.next();
				hit.age --;
				if(hit.age <= 0){
					listItr.remove();
				}
			}
			if(entry.getKey().isDead || entry.getValue().isEmpty()){
				itr.remove();
			}
		}
	}
	
	public static void addHit(EntityLivingBase ent, EntityBulletBase bullet, Vec3d pos, Vec3d dir){
		List<BulletHit> data = hitData.get(ent);
		if(data == null){
			data = new ArrayList<>();
			hitData.put(ent, data);
		}
		if(data.size() > 35)
			return;
		BulletHit hit = new BulletHit();
		hit.pos = pos;
		hit.direction = dir;
		hit.bullet = bullet;
		data.add(hit);
	}
	
	public static void encodeData(Entity ent, ByteBuf buf){
		List<BulletHit> data = hitData.get(ent);
		if(data == null){
			buf.writeByte(0);
			return;
		}
		buf.writeByte(data.size());
		for(int i = 0; i < data.size(); i ++){
			BulletHit hit = data.get(i);
			buf.writeDouble(hit.pos.x);
			buf.writeDouble(hit.pos.y);
			buf.writeDouble(hit.pos.z);
			buf.writeFloat((float) hit.direction.x);
			buf.writeFloat((float) hit.direction.y);
			buf.writeFloat((float) hit.direction.z);
		}
	}
	
	public static List<BulletHit> decodeData(ByteBuf buf){
		List<BulletHit> list = new ArrayList<>();
		byte size = buf.readByte();
		for(int i = 0; i < size; i ++){
			BulletHit hit = new BulletHit();
			hit.pos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
			hit.direction = new Vec3d(buf.readFloat(), buf.readFloat(), buf.readFloat());
			list.add(hit);
		}
		return list;
	}
	
	public static class BulletHit {
		public EntityBulletBase bullet;
		public Vec3d pos;
		public Vec3d direction;
		public int age = 2;
	}
}
