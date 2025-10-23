package com.hbm.entity.grenade;

import com.hbm.explosion.ExplosionChaos;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems.Armory;
import com.hbm.items.weapon.ItemGrenade;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
@AutoRegister(name = "entity_grenade_pulse")
public class EntityGrenadePulse extends EntityGrenadeBouncyBase {
	

	public EntityGrenadePulse(World p_i1773_1_) {
		super(p_i1773_1_);
	}

    public EntityGrenadePulse(World p_i1774_1_, EntityLivingBase p_i1774_2_, EnumHand hand)
    {
        super(p_i1774_1_, p_i1774_2_, hand);
    }

    public EntityGrenadePulse(World p_i1775_1_, double p_i1775_2_, double p_i1775_4_, double p_i1775_6_)
    {
        super(p_i1775_1_, p_i1775_2_, p_i1775_4_, p_i1775_6_);
    }

	@Override
    public void explode() {
    	

		if (!this.world.isRemote) {
			this.setDead();
			ExplosionChaos.pulse(this.world, (int) this.posX, (int) this.posY, (int) this.posZ, 7);
    		this.world.playSound(null, (int)this.posX, (int)this.posY, (int)this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.AMBIENT, 4.0F, (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F);
    		ExplosionLarge.spawnShock(world, posX, posY, posZ, 24, 2);
		}
    }

	@Override
	protected int getMaxTimer() {
		return ItemGrenade.getFuseTicks(Armory.grenade_pulse);
	}

	@Override
	protected double getBounceMod() {
		return 0.25D;
	}
}
