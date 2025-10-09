package com.hbm.entity.mob;

import com.hbm.entity.mob.ai.EntityAIMaskmanCasualApproach;
import com.hbm.entity.mob.ai.EntityAIMaskmanLasergun;
import com.hbm.entity.mob.ai.EntityAIMaskmanMinigun;
import com.hbm.handler.ArmorUtil;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IRadiationImmune;
import com.hbm.items.ModItems;
import com.hbm.main.AdvancementManager;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;

import java.util.List;
@AutoRegister(name = "entity_mask_man", trackingRange = 1000, eggColors = {0x818572, 0xC7C1B7})
public class EntityMaskMan extends EntityMob implements IRadiationImmune {

	private final BossInfoServer bossInfo = (BossInfoServer)(new BossInfoServer(this.getDisplayName(), BossInfo.Color.PURPLE, BossInfo.Overlay.PROGRESS));
	
	public float prevHealth;
	
	public EntityMaskMan(World worldIn) {
		super(worldIn);
		this.tasks.addTask(1, new EntityAISwimming(this));
		this.tasks.addTask(2, new EntityAIMaskmanCasualApproach(this, EntityPlayer.class, 1.0D, false));
        this.tasks.addTask(2, new EntityAIMaskmanMinigun(this, true, true, 3));
        this.tasks.addTask(3, new EntityAIMaskmanLasergun(this, true, true));
        this.tasks.addTask(3, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(4, new EntityAILookIdle(this));
        this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true));

		this.setSize(2F, 5F);
		this.isImmuneToFire = true;
		
		this.experienceValue = 100;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(100.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(15.0D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(1000.0D);
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {

		if(source instanceof EntityDamageSourceIndirect && ((EntityDamageSourceIndirect) source).getImmediateSource() instanceof EntityEgg && rand.nextInt(10) == 0) {
			this.experienceValue = 0;
			this.setHealth(0);
			return true;
		}

		if(source.isFireDamage())
    		amount = 0;
    	if(source.isMagicDamage())
    		amount = 0;
    	if(source.isProjectile())
    		amount *= 0.25F;
    	if(source.isExplosion())
    		amount *= 0.5F;
    	if(amount > 50) {
			amount = 50 + (amount - 50) * 0.25F;
		}

    	return super.attackEntityFrom(source, amount);
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();

        if(this.prevHealth >= this.getMaxHealth() / 2 && this.getHealth() < this.getMaxHealth() / 2) {
        	prevHealth = this.getHealth();

        	if(!world.isRemote)
        		world.createExplosion(this, posX, posY + 4, posZ, 2.5F, true);
        }
	}
	
	@Override
	public void onDeath(DamageSource cause) {
		super.onDeath(cause);
		List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow(50, 50, 50));
			
		for(EntityPlayer player : players) {
			AdvancementManager.grantAchievement(player, AdvancementManager.bossMaskman);
		}
	}
	
	//ool in the shed
	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
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
	public void addTrackingPlayer(EntityPlayerMP player) {
		super.addTrackingPlayer(player);
		bossInfo.addPlayer(player);
	}
	
	@Override
	public void removeTrackingPlayer(EntityPlayerMP player) {
		super.removeTrackingPlayer(player);
		bossInfo.removePlayer(player);
	}

	@Override
	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
		if(!world.isRemote){

			ItemStack mask = new ItemStack(ModItems.gas_mask_m65);
			ArmorUtil.installGasMaskFilter(mask, new ItemStack(ModItems.gas_mask_filter_combo));
			
			this.entityDropItem(mask, 0F);
			this.dropItem(ModItems.coin_maskman, 1);
			this.dropItem(ModItems.v1, 1);
			this.dropItem(Items.SKULL, 1);
		}
	}
}
