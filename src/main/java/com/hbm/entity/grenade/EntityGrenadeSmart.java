package com.hbm.entity.grenade;

import com.hbm.explosion.ExplosionLarge;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems.Armory;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
@AutoRegister(name = "entity_grenade_smart")
public class EntityGrenadeSmart extends EntityGrenadeBase {

    public EntityGrenadeSmart(World p_i1773_1_)
    {
        super(p_i1773_1_);
    }

    public EntityGrenadeSmart(World p_i1774_1_, EntityLivingBase p_i1774_2_, EnumHand hand)
    {
        super(p_i1774_1_, p_i1774_2_, hand);
    }

    public EntityGrenadeSmart(World p_i1775_1_, double p_i1775_2_, double p_i1775_4_, double p_i1775_6_)
    {
        super(p_i1775_1_, p_i1775_2_, p_i1775_4_, p_i1775_6_);
    }

    @Override
    public void explode() {
    	
        if (!this.world.isRemote)
        {
            this.setDead();
            
            if(this.ticksExisted > 10)
            	ExplosionLarge.explode(world, thrower, posX, posY, posZ, 5.0F, true, false, false);
            else
            	world.spawnEntity(new EntityItem(world, posX, posY, posZ, new ItemStack(Armory.grenade_smart)));
        }
    }
}
