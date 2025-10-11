package com.hbm.entity.mob;

import com.hbm.interfaces.AutoRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AutoRegister(name = "entity_parasite_maggot", eggColors = {0xd0d0d0, 0x808080})
public class EntityParasiteMaggot extends EntityMob {
    public EntityParasiteMaggot(World world) {
        super(world);
        this.setSize(0.3F, 0.7F);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(1.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public @Nullable EntityLivingBase getAttackTarget() {
        return this.world.getNearestAttackablePlayer(this, 16, 8);
    }

    @Override
    public void onUpdate() {
        this.renderYawOffset = this.rotationYaw;
        super.onUpdate();
    }

    @Override
    protected boolean isValidLightLevel() {
        return true;
    }

    @Override
    public @NotNull EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.ARTHROPOD;
    }
}
