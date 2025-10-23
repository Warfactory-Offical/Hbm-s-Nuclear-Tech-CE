package com.hbm.entity.logic;

import com.hbm.config.CompatibilityConfig;
import com.hbm.config.GeneralConfig;
import com.hbm.entity.projectile.EntityBombletZeta;
import com.hbm.entity.projectile.EntityBoxcar;
import com.hbm.explosion.ExplosionChaos;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IConstantRenderer;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.MainRegistry;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.LoopedEntitySoundPacket;
import com.hbm.util.ParticleUtil;
import net.minecraft.entity.Entity;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
@AutoRegister(name = "entity_bomber", trackingRange = 1000)
public class EntityBomber extends Entity implements IChunkLoader, IConstantRenderer {

	public static final DataParameter<Integer> HEALTH = EntityDataManager.createKey(EntityBomber.class, DataSerializers.VARINT);
	public static final DataParameter<Byte> STYLE = EntityDataManager.createKey(EntityBomber.class, DataSerializers.BYTE);
	
	int timer = 200;
	int bombStart = 75;
	int bombStop = 125;
	int bombRate = 3;
	int type = 0;
	
	public int health = 50;
	
	public EntityBomber(World worldIn) {
		super(worldIn);
		
		this.ignoreFrustumCheck = true;
    	this.setSize(8.0F, 4.0F);
	}
	
	@Override
	public boolean canBeCollidedWith() {
		return health > 0;
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if(source == ModDamageSource.nuclearBlast)
    		return false;
    	
        if (this.isEntityInvulnerable(source))
        {
            return false;
        }
        else
        {
            if (!this.isDead && !this.world.isRemote && this.health > 0)
            {
            	health -= amount;
            	
                if (this.health <= 0)
                {
                    this.killBomber();
                }
            }

            return true;
        }
	}
	
	private void killBomber() {
        ExplosionLarge.explode(world, this, posX, posY, posZ, 5, true, false, true);
    	world.playSound((double)(posX + 0.5F), (double)(posY + 0.5F), (double)(posZ + 0.5F), HBMSoundHandler.planeShotDown, SoundCategory.HOSTILE, 25.0F, 1.0F, false);
    }
	
	public boolean isBomberAlive(){
		return this.health > 0;
	}
	
	@Override
	public void onUpdate() {

		this.lastTickPosX = this.prevPosX = posX;
		this.lastTickPosY = this.prevPosY = posY;
		this.lastTickPosZ = this.prevPosZ = posZ;

		this.setPosition(posX + motionX, posY + motionY, posZ + motionZ);
		
		if(!world.isRemote) {
			
			this.getDataManager().set(HEALTH, health);
			
			if(health > 0)
				PacketDispatcher.wrapper.sendToAll(new LoopedEntitySoundPacket(this.getEntityId()));
		} else {
			health = this.getDataManager().get(HEALTH);
		}
		
		this.rotation();
		
		if(this.health <= 0) {
			motionY -= 0.025;
			if(!CompatibilityConfig.isWarDim(world)){
				this.setDead();
				return;
			}
			for(int i = 0; i < 10; i++) ParticleUtil.spawnGasFlame(this.world, this.posX + rand.nextGaussian() * 0.5 - motionX * 2, this.posY + rand.nextGaussian() * 0.5 - motionY * 2, this.posZ + rand.nextGaussian() * 0.5 - motionZ * 2, 0.0, 0.1, 0.0);
			
			if(world.getBlockState(new BlockPos((int)posX, (int)posY, (int)posZ)).isNormalCube() && !world.isRemote) {
				this.setDead();

				new ExplosionVNT(world, posX, posY, posZ, 15F).makeStandard().explode();
		    	world.playSound((double)(posX + 0.5F), (double)(posY + 0.5F), (double)(posZ + 0.5F), HBMSoundHandler.planeCrash, SoundCategory.HOSTILE, 10.0F, 1.0F, true);
				
				return;
			}
		}
		
		if(this.ticksExisted > timer)
			this.setDead();
		
		if(!world.isRemote && this.health > 0 && this.ticksExisted > bombStart && this.ticksExisted < bombStop && this.ticksExisted % bombRate == 0) {
			if(!CompatibilityConfig.isWarDim(world)){
				return;
			}
			if(type == 3) {

	        	world.playSound((double)(posX + 0.5F), (double)(posY + 0.5F), (double)(posZ + 0.5F), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.HOSTILE, 5.0F, 2.6F + (rand.nextFloat() - rand.nextFloat()) * 0.8F, true);
				ExplosionChaos.spawnChlorine(world, this.posX, this.posY - 1F, this.posZ, 10, 0.5, 3);
				
			} else if(type == 6) {
				
	        	world.playSound((double)(posX + 0.5F), (double)(posY + 0.5F), (double)(posZ + 0.5F), HBMSoundHandler.missileTakeoff, SoundCategory.HOSTILE, 10.0F, 0.9F + rand.nextFloat() * 0.2F, true);
	        	EntityBoxcar rocket = new EntityBoxcar(world);
				
	        	rocket.posX = posX + rand.nextDouble() - 0.5;
	        	rocket.posY = posY - rand.nextDouble();
	        	rocket.posZ = posZ + rand.nextDouble() - 0.5;
	        	
				world.spawnEntity(rocket);
	        	
			} else if(type == 7) {

	        	world.playSound((double)(posX + 0.5F), (double)(posY + 0.5F), (double)(posZ + 0.5F), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.HOSTILE, 5.0F, 2.6F + (rand.nextFloat() - rand.nextFloat()) * 0.8F, true);
				ExplosionChaos.spawnChlorine(world, this.posX, world.getHeight((int)this.posX, (int)this.posZ) + 2, this.posZ, 10, 1, 2);
				
			} else {
				
	        	world.playSound((double)(posX + 0.5F), (double)(posY + 0.5F), (double)(posZ + 0.5F), HBMSoundHandler.bombWhistle, SoundCategory.HOSTILE, 10.0F, 0.9F + rand.nextFloat() * 0.2F, true);
	        	
				EntityBombletZeta zeta = new EntityBombletZeta(world);
				
				zeta.rotation();
				
				zeta.type = type;
				
				zeta.posX = posX + rand.nextDouble() - 0.5;
				zeta.posY = posY - rand.nextDouble();
				zeta.posZ = posZ + rand.nextDouble() - 0.5;
				
				if(type == 0) {
					zeta.motionX = motionX + rand.nextGaussian() * 0.15;
					zeta.motionZ = motionZ + rand.nextGaussian() * 0.15;
				} else {
					zeta.motionX = motionX;
					zeta.motionZ = motionZ;
				}
				
				world.spawnEntity(zeta);
			}
		}
	}
	
	public void fac(World world, double x, double y, double z) {
    	
    	Vec3d vector = new Vec3d(world.rand.nextDouble() - 0.5, 0, world.rand.nextDouble() - 0.5);
    	vector = vector.normalize();
    	vector = new Vec3d(vector.x * (GeneralConfig.enableBomberShortMode ? 1 : 2), vector.y, vector.z * (GeneralConfig.enableBomberShortMode ? 1 : 2));
    	
    	this.setLocationAndAngles(x - vector.x * 100, y + 50, z - vector.z * 100, 0.0F, 0.0F);
    	
    	this.motionX = vector.x;
    	this.motionZ = vector.z;
    	this.motionY = 0.0D;
    	
    	this.rotation();
    	
    	int i = 1;
    	
    	int rand = world.rand.nextInt(7);
    	
    	switch(rand) {
    	case 0:
    	case 1: i = 1; break;
    	case 2:
    	case 3: i = 2; break;
    	case 4: i = 5; break;
    	case 5: i = 6; break;
    	case 6: i = 7; break;
    	}
    	
    	if(world.rand.nextInt(100) == 0) {
        	rand = world.rand.nextInt(4);

        	switch(rand) {
        	case 0: i = 0; break;
        	case 1: i = 3; break;
        	case 2: i = 4; break;
        	case 3: i = 8; break;
        	}
    	}
    	
    	this.getDataManager().set(STYLE, (byte)i);
    	this.setSize(8.0F, 4.0F);
    }
    
    public static EntityBomber statFacCarpet(World world, double x, double y, double z) {
    	
    	EntityBomber bomber = new EntityBomber(world);
    	
    	bomber.timer = 200;
    	bomber.bombStart = 50;
    	bomber.bombStop = 100;
    	bomber.bombRate = 2;

    	bomber.fac(world, x, y, z);
    	
    	bomber.type = 0;
    	
    	return bomber;
    }
    
    public static EntityBomber statFacNapalm(World world, double x, double y, double z) {
    	
    	EntityBomber bomber = new EntityBomber(world);
    	
    	bomber.timer = 200;
    	bomber.bombStart = 50;
    	bomber.bombStop = 100;
    	bomber.bombRate = 5;

    	bomber.fac(world, x, y, z);
    	
    	bomber.type = 1;
    	
    	return bomber;
    }
    
    public static EntityBomber statFacChlorine(World world, double x, double y, double z) {
    	
    	EntityBomber bomber = new EntityBomber(world);
    	
    	bomber.timer = 200;
    	bomber.bombStart = 50;
    	bomber.bombStop = 100;
    	bomber.bombRate = 4;

    	bomber.fac(world, x, y, z);
    	
    	bomber.type = 2;
    	
    	return bomber;
    }
    
    public static EntityBomber statFacOrange(World world, double x, double y, double z) {
    	
    	EntityBomber bomber = new EntityBomber(world);
    	
    	bomber.timer = 200;
    	bomber.bombStart = 75;
    	bomber.bombStop = 125;
    	bomber.bombRate = 1;

    	bomber.fac(world, x, y, z);
    	
    	bomber.type = 3;
    	
    	return bomber;
    }
    
    public static EntityBomber statFacABomb(World world, double x, double y, double z) {
    	
    	EntityBomber bomber = new EntityBomber(world);
    	
    	bomber.timer = 200;
    	bomber.bombStart = 60;
    	bomber.bombStop = 70;
    	bomber.bombRate = 65;

    	bomber.fac(world, x, y, z);
    	
    	int i = 1;
    	
    	int rand = world.rand.nextInt(3);
    	
    	switch(rand) {
    	case 0: i = 5; break;
    	case 1: i = 6; break;
    	case 2: i = 7; break;
    	}
    	
    	if(world.rand.nextInt(100) == 0) {
        	i = 8;
    	}
    	
    	bomber.getDataManager().set(STYLE, (byte)i);
    	
    	bomber.type = 4;
    	
    	return bomber;
    }
    
    public static EntityBomber statFacStinger(World world, double x, double y, double z) {
    	
    	EntityBomber bomber = new EntityBomber(world);
    	
    	bomber.timer = 200;
    	bomber.bombStart = 50;
    	bomber.bombStop = 150;
    	bomber.bombRate = 10;

    	bomber.fac(world, x, y, z);
    	
    	bomber.getDataManager().set(STYLE, (byte)4);
    	
    	bomber.type = 5;
    	
    	return bomber;
    }
    
    public static EntityBomber statFacBoxcar(World world, double x, double y, double z) {
    	
    	EntityBomber bomber = new EntityBomber(world);
    	
    	bomber.timer = 200;
    	bomber.bombStart = 50;
    	bomber.bombStop = 150;
    	bomber.bombRate = 10;

    	bomber.fac(world, x, y, z);
    	
    	bomber.getDataManager().set(STYLE, (byte)6);
    	
    	bomber.type = 6;
    	
    	return bomber;
    }
    
    public static EntityBomber statFacPC(World world, double x, double y, double z) {
    	
    	EntityBomber bomber = new EntityBomber(world);
    	
    	bomber.timer = 200;
    	bomber.bombStart = 75;
    	bomber.bombStop = 125;
    	bomber.bombRate = 1;

    	bomber.fac(world, x, y, z);
    	
    	bomber.getDataManager().set(STYLE, (byte)6);
    	
    	bomber.type = 7;
    	
    	return bomber;
    }

    private Ticket loaderTicket;
    List<ChunkPos> loadedChunks = new ArrayList<ChunkPos>();
    
	@Override
	public void init(Ticket ticket) {
		if(!world.isRemote) {
			
            if(ticket != null) {
            	
                if(loaderTicket == null) {
                	
                	loaderTicket = ticket;
                	loaderTicket.bindEntity(this);
                	loaderTicket.getModData();
                }
                
        		
                ForgeChunkManager.forceChunk(loaderTicket, new ChunkPos(chunkCoordX, chunkCoordZ));
            }
        }
	}
	
	public void loadNeighboringChunks(int newChunkX, int newChunkZ)
    {
        if(!world.isRemote && loaderTicket != null)
        {
            for(ChunkPos chunk : loadedChunks)
            {
                ForgeChunkManager.unforceChunk(loaderTicket, chunk);
            }

            loadedChunks.clear();
            loadedChunks.add(new ChunkPos(newChunkX, newChunkZ));
            loadedChunks.add(new ChunkPos(newChunkX + 1, newChunkZ + 1));
            loadedChunks.add(new ChunkPos(newChunkX - 1, newChunkZ - 1));
            loadedChunks.add(new ChunkPos(newChunkX + 1, newChunkZ - 1));
            loadedChunks.add(new ChunkPos(newChunkX - 1, newChunkZ + 1));
            loadedChunks.add(new ChunkPos(newChunkX + 1, newChunkZ));
            loadedChunks.add(new ChunkPos(newChunkX, newChunkZ + 1));
            loadedChunks.add(new ChunkPos(newChunkX - 1, newChunkZ));
            loadedChunks.add(new ChunkPos(newChunkX, newChunkZ - 1));

            for(ChunkPos chunk : loadedChunks)
            {
                ForgeChunkManager.forceChunk(loaderTicket, chunk);
            }
        }
    }

	@Override
	protected void entityInit() {
		init(ForgeChunkManager.requestTicket(MainRegistry.instance, world, Type.ENTITY));
        this.getDataManager().register(STYLE, Byte.valueOf((byte)0));
        this.getDataManager().register(HEALTH, Integer.valueOf((int)50));
		
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		ticksExisted = nbt.getInteger("ticksExisted");
		bombStart = nbt.getInteger("bombStart");
		bombStop = nbt.getInteger("bombStop");
		bombRate = nbt.getInteger("bombRate");
		type = nbt.getInteger("type");

    	this.getDataManager().set(STYLE, nbt.getByte("style"));
    	this.getDataManager().set(HEALTH, nbt.getInteger("health"));
    	this.setSize(8.0F, 4.0F);
		
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setInteger("ticksExisted", ticksExisted);
		nbt.setInteger("bombStart", bombStart);
		nbt.setInteger("bombStop", bombStop);
		nbt.setInteger("bombRate", bombRate);
		nbt.setInteger("type", type);
		nbt.setByte("style", this.getDataManager().get(STYLE));
		nbt.setInteger("health", this.getDataManager().get(HEALTH));
		
	}
	
	protected void rotation() {
        float f2 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);

        for (this.rotationPitch = (float)(Math.atan2(this.motionY, f2) * 180.0D / Math.PI) - 90; this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
        {
            ;
        }

        while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
        {
            this.prevRotationPitch += 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw < -180.0F)
        {
            this.prevRotationYaw -= 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
        {
            this.prevRotationYaw += 360.0F;
        }
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return distance < 500000;
	}

}
