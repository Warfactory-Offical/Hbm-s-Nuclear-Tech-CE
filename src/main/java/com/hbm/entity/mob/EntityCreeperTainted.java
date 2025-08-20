package com.hbm.entity.mob;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.bomb.BlockTaint;
import com.hbm.config.GeneralConfig;
import com.hbm.entity.mob.ai.EntityAITaintedCreeperSwell;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IRadiationImmune;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
@AutoRegister(name = "entity_tainted_creeper", trackingRange = 80)
public class EntityCreeperTainted extends EntityCreeper implements IRadiationImmune {

    public EntityCreeperTainted(World world) {
        super(world);
        this.fuseTime = 30;
        this.explosionRadius = 20;
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAITaintedCreeperSwell(this));
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
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(15.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.35D);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.isEntityAlive() && this.getHealth() < this.getMaxHealth() && this.ticksExisted % 10 == 0) {
            this.heal(1.0F);
        }
    }

    @Override
    public void explode() {
        if (!this.world.isRemote) {
            boolean isPowered = this.getPowered();
            boolean mobGriefing = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this);
            this.world.newExplosion(this, this.posX, this.posY, this.posZ, 5.0F, false, mobGriefing);

            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

            if (isPowered) {
                for (int i = 0; i < 255; i++) {
                    int a = this.rand.nextInt(15) + (int) this.posX - 7;
                    int b = this.rand.nextInt(15) + (int) this.posY - 7;
                    int c = this.rand.nextInt(15) + (int) this.posZ - 7;
                    pos.setPos(a, b, c);
                    if (this.world.getBlockState(pos).getBlock().isReplaceable(this.world, pos) && BlockTaint.hasPosNeightbour(this.world, pos)) {
                        if (GeneralConfig.enableHardcoreTaint)
                            this.world.setBlockState(pos, ModBlocks.taint.getDefaultState().withProperty(BlockTaint.TEXTURE, this.rand.nextInt(3) + 5), 2);
                        else
                            this.world.setBlockState(pos, ModBlocks.taint.getDefaultState().withProperty(BlockTaint.TEXTURE, this.rand.nextInt(3)), 2);
                    }
                }
            } else {
                for (int i = 0; i < 85; i++) {
                    int a = this.rand.nextInt(7) + (int) this.posX - 3;
                    int b = this.rand.nextInt(7) + (int) this.posY - 3;
                    int c = this.rand.nextInt(7) + (int) this.posZ - 3;
                    pos.setPos(a, b, c);
                    if (this.world.getBlockState(pos).getBlock().isReplaceable(this.world, pos) && BlockTaint.hasPosNeightbour(this.world, pos)) {
                        if (GeneralConfig.enableHardcoreTaint)
                            this.world.setBlockState(pos, ModBlocks.taint.getDefaultState().withProperty(BlockTaint.TEXTURE, this.rand.nextInt(6) + 10), 2);
                        else
                            this.world.setBlockState(pos, ModBlocks.taint.getDefaultState().withProperty(BlockTaint.TEXTURE, this.rand.nextInt(3) + 4), 2);
                    }
                }
            }

            this.setDead();
        }
    }
}
