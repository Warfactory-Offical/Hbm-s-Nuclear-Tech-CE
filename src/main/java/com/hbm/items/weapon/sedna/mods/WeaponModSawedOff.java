package com.hbm.items.weapon.sedna.mods;

import com.hbm.items.ModItems.Armory;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.factory.XFactory12ga;

import net.minecraft.item.ItemStack;

import java.util.Objects;

public class WeaponModSawedOff extends WeaponModBase {

	public WeaponModSawedOff(int id) {
		super(id, "BARREL");
	}

	@Override
	public <T> T eval(T base, ItemStack gun, String key, Object parent) {

		if(Objects.equals(key, Receiver.F_SPREADINNATE)) { return cast(Math.max(0.025F, (Float) base), base); }
		if(Objects.equals(key, Receiver.F_SPREADAMMO)) { return cast((Float) base * 1.5F, base); }
		if(Objects.equals(key, Receiver.F_BASEDAMAGE)) { return cast((Float) base * 1.35F, base); }
		
		if(gun.getItem() == Armory.gun_maresleg) {
			if(Objects.equals(key, GunConfig.FUN_ANIMNATIONS)) return (T) XFactory12ga.LAMBDA_MARESLEG_SHORT_ANIMS;
			if(Objects.equals(key, GunConfig.I_DRAWDURATION)) return cast(5, base);
		}
		
		return base;
	}
}
