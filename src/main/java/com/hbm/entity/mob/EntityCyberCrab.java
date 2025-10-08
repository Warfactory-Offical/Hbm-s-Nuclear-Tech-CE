package com.hbm.entity.mob;

import com.google.common.base.Predicate;
import com.hbm.entity.projectile.EntityBullet;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IRadiationImmune;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.ModDamageSource;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIAttackRanged;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
@AutoRegister(name = "entity_cyber_crab", eggColors = {0xAAAAAA, 0x444444})
public class EntityCyberCrab extends EntityMob implements IRangedAttackMob, IRadiationImmune {

	private static final Predicate<EntityLiving> selector = new Predicate<EntityLiving>(){
		@Override
		public boolean apply(EntityLiving input) {
			return !(input instanceof EntityCyberCrab || input instanceof EntityCreeper || input instanceof EntityCreeperNuclear);
		}
	};
	
	public EntityCyberCrab(World worldIn) {
		super(worldIn);
		this.setSize(0.75F, 0.35F);
		if(!(this instanceof EntityTaintCrab))
			this.tasks.addTask(0, new EntityAIPanic(this, 0.75D));
        this.tasks.addTask(1, new EntityAIWanderAvoidWater(this, 0.5F));
        //this.tasks.addTask(2, new EntityAIAvoidEntity(this, EntityPlayer.class, 3, 0.75D, 1.0D));
        this.tasks.addTask(4, arrowAttack());
        this.targetTasks.addTask(1, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, 3, true, false, null));
        this.targetTasks.addTask(1, new EntityAINearestAttackableTarget<EntityLiving>(this, EntityLiving.class, 3, true, false, selector));
	}
	
	protected EntityAIAttackRanged arrowAttack(){
		return new EntityAIAttackRanged(this, 0.5D, 60, 80, 15.0F);
	}
	
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(4.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5F);
	}
	
	@Override
	public boolean isAIDisabled() {
		return false;
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if(ModDamageSource.getIsTau(source))
    		return false;
		return super.attackEntityFrom(source, amount);
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
        
        if(this.isInWater() || this.isWet() || this.isBurning())
        	this.attackEntityFrom(DamageSource.GENERIC, 10F);
        
        if(this.getHealth() <= 0) {
        	this.setDead();

            if(this instanceof EntityTaintCrab)
            	world.createExplosion(this, this.posX, this.posY, this.posZ, 3F, false);
            else
            	world.createExplosion(this, this.posX, this.posY, this.posZ, 0.1F, false);
        }
	}
	
	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return HBMSoundHandler.cybercrab;
	}
	
	@Override
	protected SoundEvent getDeathSound() {
		return HBMSoundHandler.cybercrab;
	}
	
	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		return true;
	}
	
	@Override
	public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
		EntityBullet bullet = new EntityBullet(world, this, target, 1.6F, 2);
		bullet.setIsCritical(true);
		bullet.setTau(true);
		bullet.damage = 2;
        this.world.spawnEntity(bullet);
        this.playSound(HBMSoundHandler.sawShoot, 1.0F, 2.0F);
	}

	@Override
	public void setSwingingArms(boolean swingingArms) {
	}

}
