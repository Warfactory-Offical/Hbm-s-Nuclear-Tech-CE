package com.hbm.entity.mob;

import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.entity.mob.ai.EntityAINuclearCreeperSwell;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IRadiationImmune;
import com.hbm.items.ModItems;
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
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import java.util.List;
@AutoRegister(name = "entity_nuclear_creeper", trackingRange = 80)
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
		if (source == ModDamageSource.radiation || source == ModDamageSource.mudPoisoning) {
			this.heal(amount);
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
	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
		super.dropFewItems(wasRecentlyHit, lootingModifier);

		if (rand.nextInt(3) == 0)
			this.dropItem(ModItems.coin_creeper, 1);
	}

	@Override
	public void onDeath(DamageSource cause) {
		super.onDeath(cause);

		List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow(10, 10, 10));
		for (EntityPlayer player : players) {
			AdvancementManager.grantAchievement(player, AdvancementManager.bossCreeper);
		}
		if (cause.getTrueSource() instanceof EntitySkeleton || (cause.isProjectile() && cause.getImmediateSource() instanceof EntityArrow && ((EntityArrow) (cause.getImmediateSource())).shootingEntity == null)) {
			int i = rand.nextInt(11);
			int j = rand.nextInt(3);
            switch (i) {
                case 0 -> this.dropItem(ModItems.nugget_u235, j);
                case 1 -> this.dropItem(ModItems.nugget_pu238, j);
                case 2 -> this.dropItem(ModItems.nugget_pu239, j);
                case 3 -> this.dropItem(ModItems.nugget_neptunium, j);
                case 4 -> this.dropItem(ModItems.man_core, 1);
                case 5 -> {
                    this.dropItem(ModItems.sulfur, j * 2);
                    this.dropItem(ModItems.niter, j * 2);
                }
                case 6 -> this.dropItem(ModItems.syringe_awesome, 1);
                case 7 -> this.dropItem(ModItems.fusion_core, 1);
                case 8 -> this.dropItem(ModItems.syringe_metal_stimpak, 1);
                case 9 -> {
                    switch (rand.nextInt(4)) {
                        case 0 -> this.dropItem(ModItems.t45_helmet, 1);
                        case 1 -> this.dropItem(ModItems.t45_plate, 1);
                        case 2 -> this.dropItem(ModItems.t45_legs, 1);
                        case 3 -> this.dropItem(ModItems.t45_boots, 1);
                    }
                    this.dropItem(ModItems.fusion_core, 1);
                }
                case 10 -> this.dropItem(ModItems.nothing, 1); //ammo_nuke
            }
		}
	}

	@Override
	public void explode() {
		if (!this.world.isRemote) {
			boolean flag = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this);

			if (this.getPowered()) {
				EntityNukeTorex.statFac(world, posX, posY, posZ, 70);
				if (flag) {
					world.spawnEntity(EntityNukeExplosionMK5.statFac(world, 70, posX, posY, posZ).setDetonator(this));
				} else {
					ContaminationUtil.radiate(world, posX, posY + 0.5, posZ, 70, 1000, 0, 100, 500);
				}
			} else {
				EntityNukeTorex.statFac(world, posX, posY, posZ, 20);
				if (flag) {
					world.spawnEntity(EntityNukeExplosionMK5.statFacNoRad(world, 20, posX, posY, posZ).setDetonator(this));
				} else {
					ContaminationUtil.radiate(world, posX, posY + 0.5, posZ, 20, 1000, 0, 100, 500);
				}
			}

			this.setDead();
		}
	}

	public void setPowered(boolean power) {
		this.dataManager.set(POWERED, power);
	}
}
