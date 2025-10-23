package com.hbm.items.weapon.sedna.mods;

import com.hbm.items.ModItems.Armory;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.ItemGunBaseNT.WeaponQuality;
import com.hbm.items.weapon.sedna.factory.XFactory44;
import com.hbm.items.weapon.sedna.factory.XFactory556mm;
import com.hbm.items.weapon.sedna.factory.XFactoryTool;
import net.minecraft.item.ItemStack;

import java.util.Objects;

public class WeaponModScope extends WeaponModBase {

	public WeaponModScope(int id) {
		super(id, "SCOPE");
	}

	@Override
	public <T> T eval(T base, ItemStack gun, String key, Object parent) {

		if(Objects.equals(key, GunConfig.O_SCOPETEXTURE)) {
			if(gun.getItem() == Armory.gun_heavy_revolver) return (T) XFactory44.scope_lilmac;
			if(((ItemGunBaseNT) gun.getItem()).quality == WeaponQuality.UTILITY) return (T) XFactoryTool.scope;
			return (T) XFactory556mm.scope;
		}
		if(Objects.equals(key, GunConfig.B_HIDECROSSHAIR)) return cast(true, base); // just in case
		
		return base;
	}
}
