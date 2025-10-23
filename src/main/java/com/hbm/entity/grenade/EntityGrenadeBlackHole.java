package com.hbm.entity.grenade;

import com.hbm.entity.effect.EntityBlackHole;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems.Armory;
import com.hbm.items.weapon.ItemGrenade;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
@AutoRegister(name = "entity_grenade_black_hole")
public class EntityGrenadeBlackHole extends EntityGrenadeBouncyBase
{

    public EntityGrenadeBlackHole(World p_i1773_1_)
    {
        super(p_i1773_1_);
    }

    public EntityGrenadeBlackHole(World p_i1774_1_, EntityLivingBase p_i1774_2_, EnumHand hand)
    {
        super(p_i1774_1_, p_i1774_2_, hand);
    }

    public EntityGrenadeBlackHole(World p_i1775_1_, double p_i1775_2_, double p_i1775_4_, double p_i1775_6_)
    {
        super(p_i1775_1_, p_i1775_2_, p_i1775_4_, p_i1775_6_);
    }

    @Override
    public void explode() {
    	
        if (!this.world.isRemote)
        {
            this.setDead();
            this.world.createExplosion(this, this.posX, this.posY, this.posZ, 1.5F, true);

        	EntityBlackHole bl = new EntityBlackHole(this.world, 1.5F);
        	bl.posX = this.posX;
        	bl.posY = this.posY;
        	bl.posZ = this.posZ;
        	this.world.spawnEntity(bl);
        }
    }

	@Override
	protected int getMaxTimer() {
		return ItemGrenade.getFuseTicks(Armory.grenade_black_hole);
	}

	@Override
	protected double getBounceMod() {
		return 0.25D;
	}
}
