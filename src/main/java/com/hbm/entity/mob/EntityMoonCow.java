package com.hbm.entity.mob;

import com.hbm.entity.mob.ai.EntityMoonWalkHelper;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems.Foods;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.world.World;
@AutoRegister(name = "entity_moon_cow", trackingRange = 80)
public class EntityMoonCow extends EntityCow {

    public EntityMoonCow(World world) {
        super(world);

        moveHelper = new EntityMoonWalkHelper(this);
    }

    @Override
    public EntityMoonCow createChild(EntityAgeable entity) {
        return new EntityMoonCow(this.world);
    }

    @Override
    protected float getSoundVolume() {
        return 0.1F; // muffled by helmet
    }

    @Override
    protected Item getDropItem() {
        return Foods.cheese;
    }

    @Override
    protected void dropFewItems(boolean hitByPlayer, int looting) {
        int j = this.rand.nextInt(3) + this.rand.nextInt(1 + looting);
        int k;

        for(k = 0; k < j; ++k) {
            this.dropItem(Items.LEATHER, 1);
        }

        j = this.rand.nextInt(3) + 1 + this.rand.nextInt(1 + looting);

        for(k = 0; k < j; ++k) {
            this.dropItem(Foods.cheese, 1);
        }
    }

}
