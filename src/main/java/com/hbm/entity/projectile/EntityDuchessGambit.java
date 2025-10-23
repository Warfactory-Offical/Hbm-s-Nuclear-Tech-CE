package com.hbm.entity.projectile;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.CompatibilityConfig;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.ModDamageSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
@AutoRegister(name = "entity_duchessgambit", trackingRange = 1000)
public class EntityDuchessGambit extends EntityThrowable {

	public EntityDuchessGambit(World p_i1582_1_) {
		super(p_i1582_1_);
		this.ignoreFrustumCheck = true;
		this.isImmuneToFire = true;
	}
	
	@Override
	public void onUpdate() {


		this.lastTickPosX = this.prevPosX = posX;
		this.lastTickPosY = this.prevPosY = posY;
		this.lastTickPosZ = this.prevPosZ = posZ;
		this.setPosition(posX + this.motionX, posY + this.motionY, posZ + this.motionZ);
		
		/*this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		
		this.posX += this.motionX;
		this.posY += this.motionY;
		this.posZ += this.motionZ;*/
		
		this.motionY -= 0.03;
		if(motionY < -1.5)
			motionY = -1.5;
        
        if(!this.world.isRemote && this.world.getBlockState(new BlockPos((int)this.posX, (int)this.posY, (int)this.posZ)).getBlock() != Blocks.AIR)
        {
            this.world.playSound(null, this.posX, this.posY, this.posZ, HBMSoundHandler.alarmGambit, SoundCategory.BLOCKS, 10000.0F, 1F);
    		this.setDead();
    		if(!world.isRemote && CompatibilityConfig.isWarDim(world)){
	    		List<Entity> list = (List<Entity>)world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(posX - 5, posY - 2, posZ - 9, posX + 5, posY + 2, posZ + 9));
	    			
	    		for(Entity e : list) {
	    			e.attackEntityFrom(ModDamageSource.boat, 1000);
	    		}

        		ExplosionLarge.explode(world, thrower, posX, posY, posZ - 6, 2, true, false, false);
        		ExplosionLarge.explode(world, thrower, posX, posY, posZ - 3, 2, true, false, false);
        		ExplosionLarge.explode(world, thrower, posX, posY, posZ, 2, true, false, false);
        		ExplosionLarge.explode(world, thrower, posX, posY, posZ + 3, 2, true, false, false);
        		ExplosionLarge.explode(world, thrower, posX, posY, posZ + 6, 2, true, false, false);
        		
    			world.setBlockState(new BlockPos((int)(this.posX - 0.5), (int)(this.posY + 0.5), (int)(this.posZ - 0.5)), ModBlocks.boat.getDefaultState());
    		}
        	ExplosionLarge.spawnShock(world, posX, posY + 1, posZ, 24, 3);
    		ExplosionLarge.spawnShock(world, posX, posY + 1, posZ, 24, 2.5);
    		ExplosionLarge.spawnShock(world, posX, posY + 1, posZ, 24, 2);
    		ExplosionLarge.spawnShock(world, posX, posY + 1, posZ, 24, 1.5);
    		ExplosionLarge.spawnShock(world, posX, posY + 1, posZ, 24, 1);
        }
    }

	@Override
	protected void onImpact(RayTraceResult p_70184_1_) {
		
	}
	
    @Override
	@SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance)
    {
        return distance < 25000;
    }
}
