package com.hbm.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AudioDynamic extends MovingSound {

	public float maxVolume = 1;
	public float range;
	public float intendedVolume;
	public int keepAlive;
	public int timeSinceKA;
	public boolean shouldExpire = false;
	private final boolean nonLegacy;

	protected AudioDynamic(SoundEvent loc, SoundCategory cat, boolean useNewSystem) {
		super(loc, cat);
		this.repeat = true;
		this.attenuationType = ISound.AttenuationType.NONE;
		this.intendedVolume = 10;
		this.nonLegacy = useNewSystem;
	}
	
	public void setPosition(float x, float y, float z) {
		this.xPosF = x;
		this.yPosF = y;
		this.zPosF = z;
	}

	public void setAttenuation(ISound.AttenuationType type){
		this.attenuationType = type;
		volume = intendedVolume;
	}
	
	@Override
	public void update() {
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		float f;
		if(nonLegacy) {
			if(player != null) {
				f = (float)Math.sqrt(Math.pow(xPosF - player.posX, 2) + Math.pow(yPosF - player.posY, 2) + Math.pow(zPosF - player.posZ, 2));
				volume = func(f);
			} else {
				volume = maxVolume;
			}

			if(this.shouldExpire) {

				if(this.timeSinceKA > this.keepAlive) {
					this.stop();
				}

				this.timeSinceKA++;
			}
		} else {
			if(player != null) {
				if(attenuationType == ISound.AttenuationType.LINEAR){
				} else {
					f = (float)Math.sqrt(Math.pow(xPosF - player.posX, 2) + Math.pow(yPosF - player.posY, 2) + Math.pow(zPosF - player.posZ, 2));
					volume = func(f, intendedVolume);
				}
			} else {
				volume = intendedVolume;
			}
		}
	}
	
	public void start() {
		Minecraft.getMinecraft().getSoundHandler().playSound(this);
	}
	
	public void stop() {
		Minecraft.getMinecraft().getSoundHandler().stopSound(this);
	}
	
	public void setVolume(float volume) {
		this.intendedVolume = volume;
	}

	public void setRange(float range) {
		this.range = range;
	}
	
	public void setPitch(float pitch) {
		this.pitch = pitch;
	}
	public void setKeepAlive(int keepAlive) {
		this.keepAlive = keepAlive;
		this.shouldExpire = true;
	}

	public void keepAlive() {
		this.timeSinceKA = 0;
	}
	
	public float func(float f, float v) {
		return (f / v) * -2 + 2;
	}

	public float func(float dist) {
		return (dist / range) * -maxVolume + maxVolume;
	}

	public boolean isPlaying() {
		return Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(this);
	}
}
