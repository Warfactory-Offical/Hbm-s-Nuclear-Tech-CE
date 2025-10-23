package com.hbm.entity.projectile;

import com.hbm.config.CompatibilityConfig;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.HBMSoundHandler;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
@AutoRegister(name = "entity_chopper_mine", trackingRange = 1000)
public class EntityChopperMine extends Entity implements IProjectile {

	public int timer = 0;
	public Entity shooter;
	//private int field_145791_d = -1;
	//private int field_145792_e = -1;
	//private int field_145789_f = -1;
	//private boolean inGround;
	
	public EntityChopperMine(World worldIn) {
		super(worldIn);
	}
	
	public EntityChopperMine(World p_i1582_1_, double x, double y, double z, double moX, double moY, double moZ, Entity shooter) {
		super(p_i1582_1_);
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		
		this.motionX = moX;
		this.motionY = moY;
		this.motionZ = moZ;
		
		this.shooter = shooter;
		
		this.setSize(12, 12);
		
		this.isImmuneToFire = true;
	}
	
	@Override
	public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
	}

	@Override
	protected void entityInit() {
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		

		Vec3d vec31 = new Vec3d(this.posX, this.posY, this.posZ);
		Vec3d vec3 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY,
				this.posZ + this.motionZ);
		RayTraceResult RayTraceResult = this.world.rayTraceBlocks(vec31, vec3, false, true, false);
		vec31 = new Vec3d(this.posX, this.posY, this.posZ);
		vec3 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY,
				this.posZ + this.motionZ);

		if (RayTraceResult != null) {
			vec3 = new Vec3d(RayTraceResult.hitVec.x, RayTraceResult.hitVec.y, RayTraceResult.hitVec.z);
		}

		Entity entity = null;
		List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow(Math.abs(this.motionX), Math.abs(this.motionY), Math.abs(this.motionZ)).grow(1.0D, 1.0D, 1.0D));
		double d0 = 0.0D;
		int i;
		float f1;

		for (i = 0; i < list.size(); ++i) {
			Entity entity1 = (Entity) list.get(i);

			if (entity1.canBeCollidedWith() && (entity1 != this.shooter)) {
				f1 = 0.3F;
				AxisAlignedBB axisalignedbb1 = entity1.getEntityBoundingBox().expand(f1, f1, f1);
				RayTraceResult RayTraceResult1 = axisalignedbb1.calculateIntercept(vec31, vec3);

				if (RayTraceResult1 != null) {
					double d1 = vec31.distanceTo(RayTraceResult1.hitVec);

					if (d1 < d0 || d0 == 0.0D) {
						entity = entity1;
						d0 = d1;
					}
				}
			}
		}

		if (entity != null) {
			RayTraceResult = new RayTraceResult(entity);
		}

		if (RayTraceResult != null && RayTraceResult.entityHit != null
				&& RayTraceResult.entityHit instanceof EntityPlayer) {
			if(CompatibilityConfig.isWarDim(world))
				world.createExplosion(shooter, this.posX, this.posY, this.posZ, 5F, false);
			this.setDead();
		}
		
		//if(timer % 10 == 0 && timer % 20 != 0)
		//	world.playSoundAtEntity(this, "random.click", 10.0F, 1F);
		//if(timer % 20 == 0)
		//	world.playSoundAtEntity(this, "random.click", 10.0F, 1.5F);
		
		world.playSound(null, this.posX, this.posY, this.posZ, HBMSoundHandler.nullMine, SoundCategory.HOSTILE, 10.0F, 1F);
		
		if(timer >= 100 || world.getBlockState(new BlockPos((int)this.posX, (int)this.posY, (int)this.posZ)).getMaterial() != Material.AIR)
		{	
			if(CompatibilityConfig.isWarDim(world))
				world.createExplosion(shooter, this.posX, this.posY, this.posZ, 5F, false);
			this.setDead();
		}

		if(motionY > -0.85)
			this.motionY -= 0.05;
		
		this.motionX *= 0.9;
		this.motionZ *= 0.9;

		this.posX += this.motionX;
		this.posY += this.motionY;
		this.posZ += this.motionZ;
		
		timer++;
	}
}
