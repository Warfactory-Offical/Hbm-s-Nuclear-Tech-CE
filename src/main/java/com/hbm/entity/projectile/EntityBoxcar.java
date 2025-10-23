package com.hbm.entity.projectile;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.CompatibilityConfig;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IConstantRenderer;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.ModDamageSource;
import net.minecraft.block.Block;
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
@AutoRegister(name = "entity_boxcar", trackingRange = 1000)
public class EntityBoxcar extends EntityThrowable implements IConstantRenderer {

	public EntityBoxcar(World worldIn) {
		super(worldIn);
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
        BlockPos pos = new BlockPos((int)this.posX, (int)this.posY, (int)this.posZ);
        Block b = this.world.getBlockState(pos).getBlock();
        if(b != Blocks.AIR && b != Blocks.WATER && b != Blocks.FLOWING_WATER && !b.isReplaceable(world, pos))
        {
            this.world.playSound(null, this.posX, this.posY, this.posZ, HBMSoundHandler.oldExplosion, SoundCategory.HOSTILE, 10000.0F, 0.5F + this.rand.nextFloat() * 0.1F);
            this.setDead();
        	ExplosionLarge.spawnShock(world, posX, posY + 1, posZ, 24, 3);
    		ExplosionLarge.spawnShock(world, posX, posY + 1, posZ, 24, 2.5);
    		ExplosionLarge.spawnShock(world, posX, posY + 1, posZ, 24, 2);
    		ExplosionLarge.spawnShock(world, posX, posY + 1, posZ, 24, 1.5);
    		ExplosionLarge.spawnShock(world, posX, posY + 1, posZ, 24, 1);
    		if(CompatibilityConfig.isWarDim(world)){

	    		List<Entity> list = (List<Entity>)world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(posX - 2, posY - 2, posZ - 2, posX + 2, posY + 2, posZ + 2));
	    			
	    		for(Entity e : list) {
	    			e.attackEntityFrom(ModDamageSource.boxcar, 1000);
	    		}
	    		
	    		if(!world.isRemote)
	    			world.setBlockState(new BlockPos((int)(this.posX - 0.5), (int)(this.posY + 0.5), (int)(this.posZ - 0.5)), ModBlocks.boxcar.getDefaultState());
	    	}
        }
	}
	
	@Override
	protected void onImpact(RayTraceResult result) {
		
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return distance < 25000;
	}

}
