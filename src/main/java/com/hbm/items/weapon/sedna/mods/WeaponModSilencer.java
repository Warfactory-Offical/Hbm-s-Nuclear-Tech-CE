package com.hbm.items.weapon.sedna.mods;

import com.hbm.items.ModItems;
import com.hbm.items.weapon.sedna.Receiver;

import com.hbm.lib.HBMSoundHandler;
import net.minecraft.item.ItemStack;

import java.util.Objects;

public class WeaponModSilencer extends WeaponModBase {

	public WeaponModSilencer(int id) {
		super(id, "SILENCER");
	}

	@Override
	public <T> T eval(T base, ItemStack gun, String key, Object parent) {
		
		if(Objects.equals(key, Receiver.S_FIRESOUND)) {
			if(gun.getItem() == ModItems.gun_amat) return (T) HBMSoundHandler.silencerShoot;
			return (T) HBMSoundHandler.fireSilenced;
		}
		
		return base;
	}
}
