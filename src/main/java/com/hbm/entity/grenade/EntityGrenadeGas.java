package com.hbm.entity.grenade;

import com.hbm.entity.effect.EntityMist;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems.Armory;
import com.hbm.items.weapon.ItemGrenade;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import java.util.Random;
@AutoRegister(name = "entity_grenade_gas")
public class EntityGrenadeGas extends EntityGrenadeBouncyBase {
	Random rand = new Random();

	public EntityGrenadeGas(World p_i1773_1_) {
		super(p_i1773_1_);
	}

	public EntityGrenadeGas(World p_i1774_1_, EntityLivingBase p_i1774_2_, EnumHand hand) {
		super(p_i1774_1_, p_i1774_2_, hand);
	}

	public EntityGrenadeGas(World p_i1775_1_, double p_i1775_2_, double p_i1775_4_, double p_i1775_6_) {
		super(p_i1775_1_, p_i1775_2_, p_i1775_4_, p_i1775_6_);
	}

	@Override
	public void explode() {

		if (!this.world.isRemote) {
			this.setDead();
			this.world.createExplosion(this, this.posX, this.posY, this.posZ, 0.0F, true);
			for(int i = 0; i< 5; i++) {
				EntityMist mist = new EntityMist(world);
				mist.setType(Fluids.CHLORINE);
				mist.setPosition(posX, posY - 5+(i*0.2), posZ);
				mist.setArea(15, 10);
				world.spawnEntity(mist);
			}
		}
	}

	@Override
	protected int getMaxTimer() {
		return ItemGrenade.getFuseTicks(Armory.grenade_gas);
	}

	@Override
	protected double getBounceMod() {
		return 0.25D;
	}

}
