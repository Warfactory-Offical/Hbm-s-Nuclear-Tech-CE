package com.hbm.sound;

import com.hbm.items.ModItems;
import com.hbm.items.weapon.ItemCrucible;
import com.hbm.lib.HBMSoundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;

public class SoundLoopCrucible extends MovingSound {

	public int ticks;
	public EntityPlayer player;
	public ISound start;
	
	public SoundLoopCrucible(EntityPlayer player) {
		super(HBMSoundHandler.crucibleLoop, SoundCategory.PLAYERS);
		this.repeat = true;
		this.repeatDelay = 0;
		this.volume = 0.1F;
		this.player = player;
	}

	@Override
	public void update() {
		this.xPosF = (float) player.posX;
		this.yPosF = (float) player.posY;
		this.zPosF = (float) player.posZ;
		if(start != null && (player.isDead || player.getHeldItemMainhand().getItem() != ModItems.crucible || ItemCrucible.getCharges(player.getHeldItemMainhand()) == 0)){
			this.donePlaying = true;
			Minecraft.getMinecraft().getSoundHandler().stopSound(start);
			start = null;
			Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getRecordSoundRecord(HBMSoundHandler.crucibleEnd, (float)player.posX, (float)player.posY, (float)player.posZ));
		}
		if(ticks == 0){
			start = PositionedSoundRecord.getRecordSoundRecord(HBMSoundHandler.crucibleStart, (float)player.posX, (float)player.posY, (float)player.posZ);
			Minecraft.getMinecraft().getSoundHandler().playSound(start);
		}
		if(ticks == 8){
			this.volume = 1;
		}
		ticks ++;
	}

}
