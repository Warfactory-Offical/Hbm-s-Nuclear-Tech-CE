package com.hbm.entity.item;

import com.hbm.render.amlfrom1710.Vec3;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityDroneBase extends Entity {
    public static final DataParameter<Byte> APPEARANCE = EntityDataManager.createKey(EntityDroneBase.class, DataSerializers.BYTE);
    public static final DataParameter<Boolean> IS_EXPRESS = EntityDataManager.createKey(EntityDroneBase.class, DataSerializers.BOOLEAN);
    protected int turnProgress;
    protected double syncPosX;
    protected double syncPosY;
    protected double syncPosZ;
    @SideOnly(Side.CLIENT) protected double velocityX;
    @SideOnly(Side.CLIENT) protected double velocityY;
    @SideOnly(Side.CLIENT) protected double velocityZ;

    public double targetX = -1;
    public double targetY = -1;
    public double targetZ = -1;

    public EntityDroneBase(World world) {
        super(world);
        this.setSize(1.5F, 2.0F);
    }

    public void setTarget(double x, double y, double z) {
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean hitByEntity(Entity attacker) {

        if(attacker instanceof EntityPlayer) {
            this.setDead();
        }

        return false;
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(APPEARANCE, (byte) 0);
    }

    /**
     * 0: Empty<br>
     * 1: Crate<br>
     * 2: Barrel<br>
     */
    public void setAppearance(int style) {
        this.dataManager.set(APPEARANCE, (byte) style);
    }

    public int getAppearance() {
        return this.dataManager.get(APPEARANCE);
    }

    @Override
    public void onUpdate() {
        if(world.isRemote) {
            if(this.turnProgress > 0) {
                double interpX = this.posX + (this.syncPosX - this.posX) / (double) this.turnProgress;
                double interpY = this.posY + (this.syncPosY - this.posY) / (double) this.turnProgress;
                double interpZ = this.posZ + (this.syncPosZ - this.posZ) / (double) this.turnProgress;
                --this.turnProgress;
                this.setPosition(interpX, interpY, interpZ);
            } else {
                this.setPosition(this.posX, this.posY, this.posZ);
            }

            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX + 1.125, posY + 0.75, posZ, 0, -0.2, 0);
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX - 1.125, posY + 0.75, posZ, 0, -0.2, 0);
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX, posY + 0.75, posZ + 1.125, 0, -0.2, 0);
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX, posY + 0.75, posZ - 1.125, 0, -0.2, 0);
        } else {

            this.motionX = 0;
            this.motionY = 0;
            this.motionZ = 0;

            if(this.targetY != -1) {

                Vec3 dist = Vec3.createVectorHelper(targetX - posX, targetY - posY, targetZ - posZ);
                double speed = Math.min(getSpeed(), dist.length());

                dist = dist.normalize();
                this.motionX = dist.xCoord * speed;
                this.motionY = dist.yCoord * speed;
                this.motionZ = dist.zCoord * speed;
            }
            if(collidedHorizontally){
                motionY += 1;
            }
            this.loadNeighboringChunks();
            this.move(MoverType.SELF, motionX, motionY, motionZ);
        }

        super.onUpdate();
    }

    protected void loadNeighboringChunks() {}

    public double getSpeed() {
        return 0.125D;
    }

    @SideOnly(Side.CLIENT)
    public void setVelocity(double motionX, double motionY, double motionZ) {
        this.velocityX = this.motionX = motionX;
        this.velocityY = this.motionY = motionY;
        this.velocityZ = this.motionZ = motionZ;
    }

    @SideOnly(Side.CLIENT)
    public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int theNumberThree) {
        this.syncPosX = x;
        this.syncPosY = y;
        this.syncPosZ = z;
        this.turnProgress = theNumberThree;
        this.motionX = this.velocityX;
        this.motionY = this.velocityY;
        this.motionZ = this.velocityZ;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setDouble("tX", targetX);
        compound.setDouble("tY", targetY);
        compound.setDouble("tZ", targetZ);

        compound.setByte("app", this.dataManager.get(APPEARANCE));
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        if(compound.hasKey("tY")) {
            this.targetX = compound.getDouble("tX");
            this.targetY = compound.getDouble("tY");
            this.targetZ = compound.getDouble("tZ");
        }

        this.dataManager.set(APPEARANCE, compound.getByte("app"));
    }
}
