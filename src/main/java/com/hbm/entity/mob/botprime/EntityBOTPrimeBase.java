package com.hbm.entity.mob.botprime;

import com.google.common.base.Predicate;
import com.hbm.entity.projectile.EntityBulletBase;
import com.hbm.handler.BulletConfigSyncingUtil;
import com.hbm.lib.HBMSoundHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class EntityBOTPrimeBase extends EntityWormBaseNT {

	public int attackCounter = 0;

	protected final Predicate<Entity> selector = ent -> {

		if(ent instanceof EntityWormBaseNT && ((EntityWormBaseNT) ent).getHeadID() == EntityBOTPrimeBase.this.getHeadID())
			return false;

		return true;
	};

	public EntityBOTPrimeBase(World world) {
		super(world);
		this.setSize(2.0F, 2.0F);
		this.isImmuneToFire = true;
		this.isAirBorne = true;
		this.noClip = true;
		this.dragInAir = 0.995F;
		this.dragInGround = 0.98F;
		this.knockbackDivider = 1.0D;
	}

	@Override
	public boolean canEntityBeSeen(Entity p_70685_1_) {
		return this.world.rayTraceBlocks(new Vec3d(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ), new Vec3d(p_70685_1_.posX, p_70685_1_.posY + (double)p_70685_1_.getEyeHeight(), p_70685_1_.posZ), false, true, false) == null;
	}
	
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(15000.0D);
		this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
	}
	
	@Override
	public boolean isAIDisabled() {
		return false;
	}
	
	@Override
	protected boolean canDespawn() {
		return false;
	}
	
	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.ENTITY_BLAZE_HURT;
	}
	
	@Override
	protected SoundEvent getDeathSound() {
		return HBMSoundHandler.bombDet;
	}
	
	protected void laserAttack(Entity target, boolean head) {

		if(!(target instanceof EntityLivingBase))
			return;

		EntityLivingBase living = (EntityLivingBase) target;

		if(head) {

			for(int i = 0; i < 5; i++) {

				EntityBulletBase bullet = new EntityBulletBase(this.world, BulletConfigSyncingUtil.WORM_LASER, this, living, 1.0F, i * 0.05F);
				this.world.spawnEntity(bullet);
			}

			this.playSound(HBMSoundHandler.ballsLaser, 5.0F, 0.75F);

		} else {
			EntityBulletBase bullet = new EntityBulletBase(this.world, BulletConfigSyncingUtil.WORM_BOLT, this, living, 0.5F, 0.125F);
			this.world.spawnEntity(bullet);
			this.playSound(HBMSoundHandler.ballsLaser, 5.0F, 1.0F);
		}
	}

}
