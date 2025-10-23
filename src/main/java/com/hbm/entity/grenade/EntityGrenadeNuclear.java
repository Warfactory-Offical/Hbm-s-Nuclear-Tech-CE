package com.hbm.entity.grenade;

import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems.Armory;
import com.hbm.items.weapon.ItemGrenade;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import java.util.Random;
@AutoRegister(name = "entity_grenade_nuclear")
public class EntityGrenadeNuclear extends EntityGrenadeBouncyBase {
    private static Random rand = new Random();

    public EntityGrenadeNuclear(World p_i1773_1_)
    {
        super(p_i1773_1_);
    }

    public EntityGrenadeNuclear(World p_i1774_1_, EntityLivingBase p_i1774_2_, EnumHand hand)
    {
        super(p_i1774_1_, p_i1774_2_, hand);
    }

    public EntityGrenadeNuclear(World p_i1775_1_, double p_i1775_2_, double p_i1775_4_, double p_i1775_6_)
    {
        super(p_i1775_1_, p_i1775_2_, p_i1775_4_, p_i1775_6_);
    }

    @Override
    public void explode() {
    	
        if (!this.world.isRemote)
        {
            this.setDead();

            world.spawnEntity(EntityNukeExplosionMK5.statFac(world, BombConfig.nukaRadius, posX + 0.5, posY + 0.5, posZ + 0.5).setDetonator(thrower));
            if(rand.nextInt(100) == 0){
                EntityNukeTorex.statFacBale(world, posX + 0.5, posY + 0.5, posZ + 0.5, BombConfig.nukaRadius);
            } else {
                EntityNukeTorex.statFac(world, posX + 0.5, posY + 0.5, posZ + 0.5, BombConfig.nukaRadius);
            }
        }
    }

	@Override
	protected int getMaxTimer() {
		return ItemGrenade.getFuseTicks(Armory.grenade_nuclear);
	}

	@Override
	protected double getBounceMod() {
		return 0.25D;
	}
}
