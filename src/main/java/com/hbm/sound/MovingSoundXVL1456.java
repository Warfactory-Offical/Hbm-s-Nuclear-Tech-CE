package com.hbm.sound;

import com.hbm.items.ModItems.DummyTexs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public class MovingSoundXVL1456 extends MovingSoundPlayerLoop {

	public MovingSoundXVL1456(SoundEvent p_i45104_1_, SoundCategory c, Entity player, EnumHbmSound type) {
		super(p_i45104_1_, c, player, type);
		this.setPitch(0.5F);
	}

	@Override
	public void update() {
		super.update();
		
		ItemStack i = null;
		
		if(this.player != null) {
			i = ((EntityPlayer)this.player).getActiveItemStack();
		}
		
		//this.setPitch(this.getPitch() + 0.1F);
		                                                 //gun_xvl1456
		if(i == null || (i != null && i.getItem() != DummyTexs.nothing) || !((EntityPlayer)this.player).isSneaking() || ((EntityPlayer)this.player).getItemInUseCount() <= 0)
			this.stop();
	}
}