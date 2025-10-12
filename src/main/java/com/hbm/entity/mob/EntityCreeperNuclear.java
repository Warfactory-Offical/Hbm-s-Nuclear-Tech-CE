package com.hbm.entity.mob;

import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.entity.mob.ai.EntityAINuclearCreeperSwell;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.explosion.ExplosionNukeSmall;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IRadiationImmune;
import com.hbm.inventory.OreDictManager;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.sedna.factory.GunFactory;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.AdvancementManager;
import com.hbm.util.ContaminationUtil;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.List;
@AutoRegister(name = "entity_nuclear_creeper", trackingRange = 80, eggColors = {0x204131, 0x75CE00})
public class EntityCreeperNuclear extends EntityCreeper implements IRadiationImmune {

	public EntityCreeperNuclear(World worldIn) {
		super(worldIn);
		this.fuseTime = 75;
		this.explosionRadius = 20;
	}

	@Override
	protected void initEntityAI() {
		this.tasks.addTask(1, new EntityAISwimming(this));
		this.tasks.addTask(2, new EntityAINuclearCreeperSwell(this));
		this.tasks.addTask(3, new EntityAIAttackMelee(this, 1.0D, false));
		this.tasks.addTask(4, new EntityAIWander(this, 0.8D));
		this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(6, new EntityAILookIdle(this));

		this.targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
		this.targetTasks.addTask(2, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, EntityOcelot.class, true));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(50.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
        // for some reason the nuclear explosion would damage the already dead entity, reviving it and forcing it to play the death animation
        if(this.isDead) return false;

		if (source == ModDamageSource.radiation || source == ModDamageSource.mudPoisoning) {
            if(this.isEntityAlive()) this.heal(amount);
			return false;
		}
		return super.attackEntityFrom(source, amount);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (this.isEntityAlive()) {
			ContaminationUtil.radiate(world, posX, posY, posZ, 32, this.timeSinceIgnited + 25);
			if (this.getHealth() < this.getMaxHealth() && this.ticksExisted % 10 == 0) {
				this.heal(1.0F);
			}
		}
	}

    @Override
    protected Item getDropItem() {
        return Item.getItemFromBlock(Blocks.TNT);
    }

	@Override
	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
		super.dropFewItems(wasRecentlyHit, lootingModifier);

		if (rand.nextInt(3) == 0)
			this.dropItem(ModItems.coin_creeper, 1);
	}

	@Override
	public void onDeath(DamageSource cause) {
		super.onDeath(cause);

		List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow(50, 50, 50));
		for (EntityPlayer player : players) {
			AdvancementManager.grantAchievement(player, AdvancementManager.bossCreeper);
		}
		if (cause.getTrueSource() instanceof EntitySkeleton || (cause.isProjectile() && cause.getImmediateSource() instanceof EntityArrow arrow && arrow.shootingEntity == null)) {
            this.entityDropItem(OreDictManager.DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.NUKE_STANDARD), 1);
		}
	}

	@Override
	public void explode() {
		if (!this.world.isRemote) {
			boolean flag = ForgeEventFactory.getMobGriefingEvent(this.world, this);

			if (this.getPowered()) {
				EntityNukeTorex.statFac(world, posX, posY, posZ, 50);
				if (flag) {
					world.spawnEntity(EntityNukeExplosionMK5.statFac(world, 50, posX, posY, posZ).setDetonator(this));
				} else {
                    ExplosionNukeGeneric.dealDamage(world, posX, posY + 0.5, posZ, 100);
				}
			} else {
				if (flag) {
                    ExplosionNukeSmall.explode(world, posX, posY + 0.5, posZ, ExplosionNukeSmall.PARAMS_MEDIUM);
				} else {
                    ExplosionNukeSmall.explode(world, posX, posY + 0.5, posZ, ExplosionNukeSmall.PARAMS_SAFE);
                }
			}

			this.setDead();
		}
	}

	public void setPowered(boolean power) {
		this.dataManager.set(POWERED, power);
	}
}
