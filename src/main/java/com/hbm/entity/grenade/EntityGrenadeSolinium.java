package com.hbm.entity.grenade;

import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityCloudSolinium;
import com.hbm.entity.logic.EntityNukeExplosionMK3;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems.Armory;
import com.hbm.items.weapon.ItemGrenade;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

import java.util.Random;
@AutoRegister(name = "entity_grenade_solinium")
public class EntityGrenadeSolinium extends EntityGrenadeBouncyBase {
    private static Random rand = new Random();

    public EntityGrenadeSolinium(World p_i1773_1_)
    {
        super(p_i1773_1_);
    }

    public EntityGrenadeSolinium(World p_i1774_1_, EntityLivingBase p_i1774_2_, EnumHand hand)
    {
        super(p_i1774_1_, p_i1774_2_, hand);
    }

    public EntityGrenadeSolinium(World p_i1775_1_, double p_i1775_2_, double p_i1775_4_, double p_i1775_6_)
    {
        super(p_i1775_1_, p_i1775_2_, p_i1775_4_, p_i1775_6_);
    }

    @Override
    public void explode() {
    	
        if (!this.world.isRemote)
        {
            this.setDead();

            world.playSound(null, posX, posY, posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 1.0f, world.rand.nextFloat() * 0.1F + 0.9F);

            EntityNukeExplosionMK3 entity = new EntityNukeExplosionMK3(world);
            entity.posX = posX;
            entity.posY = posY;
            entity.posZ = posZ;
            entity.destructionRange = (int)BombConfig.soliniumRadius/10;
            entity.speed = BombConfig.blastSpeed;
            entity.coefficient = 1.0F;
            entity.waste = false;
            entity.extType = 1;

            world.spawnEntity(entity);

            EntityCloudSolinium cloud = new EntityCloudSolinium(world, (int)BombConfig.soliniumRadius/10);
            cloud.posX = posX;
            cloud.posY = posY;
            cloud.posZ = posZ;
            world.spawnEntity(cloud);
        }
    }

	@Override
	protected int getMaxTimer() {
		return ItemGrenade.getFuseTicks(Armory.grenade_solinium);
	}

	@Override
	protected double getBounceMod() {
		return 0.25D;
	}
}
