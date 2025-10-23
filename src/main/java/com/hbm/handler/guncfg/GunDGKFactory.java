package com.hbm.handler.guncfg;

import com.hbm.handler.BulletConfiguration;
import com.hbm.inventory.RecipesCommon;
import com.hbm.items.ModItems.Armory;
import com.hbm.particle.SpentCasing;

public class GunDGKFactory {

	public static final SpentCasing CASINGDGK;

	static {
		CASINGDGK = new SpentCasing(SpentCasing.CasingType.STRAIGHT).setScale(1.5F).setBounceMotion(1F, 0.5F).setColor(SpentCasing.COLOR_CASE_BRASS).register("DGK").setupSmoke(0.02F, 0.5D, 60, 20).setMaxAge(60); //3 instead of 12 seconds
	}

	@Deprecated
	public static BulletConfiguration getDGKConfig() {
		
		BulletConfiguration bullet = BulletConfigFactory.standardBulletConfig();
		bullet.ammo = new RecipesCommon.ComparableStack(Armory.ammo_dgk);
		return bullet;
	}
}
