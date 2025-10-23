package com.hbm.sound;

import com.hbm.items.ModItems;
import com.hbm.items.weapon.ItemGunEgon;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;

public class GunEgonSoundHandler {

	public int ticks = -1;
	public EntityPlayer player;
	public SoundLoopGunEgonFire loop;
	
	public GunEgonSoundHandler(EntityPlayer player) {
		this.player = player;
		loop = new SoundLoopGunEgonFire(HBMSoundHandler.gluonLoop, SoundCategory.PLAYERS, player);
	}
	
	public void update(){
		boolean firing = player.getHeldItemMainhand().getItem() == ModItems.gun_egon && (player == Minecraft.getMinecraft().player ? ItemGunEgon.m1 && Library.countInventoryItem(player.inventory, ItemGunEgon.getBeltType(player, player.getHeldItemMainhand(), true)) >= 2 : ItemGunEgon.getIsFiring(player.getHeldItemMainhand()));
		if(ticks < 0 && firing)
			ticks = 0;
		if(ticks >= 0){
			if(!firing || player.isDead){
				loop.setDone(true);
				ticks = -1;
				Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getRecordSoundRecord(HBMSoundHandler.gluonEnd, (float)player.posX, (float)player.posY, (float)player.posZ));
				return;
			}
			if(ticks == 0){
				Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getRecordSoundRecord(HBMSoundHandler.gluonStart, (float)player.posX, (float)player.posY, (float)player.posZ));
			} else if(ticks == 8){
				Minecraft.getMinecraft().getSoundHandler().playSound(loop);
			}
			float[] angles = ItemGunEgon.getBeamDirectionOffset(player.world.getTotalWorldTime()+1);
			Vec3d look = Library.changeByAngle(player.getLook(1), angles[0], angles[1]);
			RayTraceResult r = Library.rayTraceIncludeEntitiesCustomDirection(player, look, 50, 1);
			if(r != null && r.typeOfHit == Type.ENTITY && r.entityHit instanceof EntityLivingBase && player.world.getTotalWorldTime() % 2 == 0){
				Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getRecordSoundRecord(HBMSoundHandler.gluonHit, (float)player.posX, (float)player.posY, (float)player.posZ));
			}
			ticks ++;
		}
	}
	
	public void end(){
		loop.setDone(true);
	}
}
