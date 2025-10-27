package com.hbm.entity.projectile;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.CompatibilityConfig;
import com.hbm.config.GeneralConfig;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.world.Meteorite;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@AutoRegister(name = "entity_meteor", trackingRange = 1000)
public class EntityMeteor extends EntityThrowable {

	public boolean safe = false;
	private AudioWrapper audio;

	public EntityMeteor(World world) {
		super(world);
		this.ignoreFrustumCheck = true;
		this.isImmuneToFire = true;
	}



	public List<BlockPos> getBlocksInRadius(World world, int x, int y, int z, int radius) {
		List<BlockPos> foundBlocks = new ArrayList();

		int rSq = radius * radius;
		for(int dx = -radius; dx <= radius; dx++) {
			for(int dy = -radius; dy <= radius; dy++) {
				for(int dz = -radius; dz <= radius; dz++) {
					// Check if point (dx, dy, dz) lies inside the sphere
					if(dx * dx + dy * dy + dz * dz <= rSq) {
						foundBlocks.add(new BlockPos(x + dx, y + dy, z + dz));
					}
				}
			}
		}
		return foundBlocks;
	}

	public void damageOrDestroyBlock(World world, int x, int y, int z) {
		if (safe) return;

		BlockPos pos = new BlockPos(x, y, z);
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		if (block == Blocks.AIR) return; // skip air blocks

		float hardness = block.getBlockHardness(state, world, pos);

		// Check if the block is weak and can be destroyed
		if (block == Blocks.LEAVES || block == Blocks.LOG || (hardness >= 0 && hardness <= 0.3F)) {
			world.setBlockToAir(pos);
		} else {
			// Found a solid block
			if (hardness < 0 || hardness > 5F) return;

			if (rand.nextInt(6) == 1) {
				// Turn blocks into damaged variants
				if (block == Blocks.DIRT) {
					world.setBlockState(pos, ModBlocks.dirt_dead.getDefaultState());
				} else if (block == Blocks.SAND) {
					if (rand.nextInt(2) == 1) {
						world.setBlockState(pos, Blocks.SANDSTONE.getDefaultState());
					} else {
						world.setBlockState(pos, Blocks.GLASS.getDefaultState());
					}
				} else if (block == Blocks.STONE) {
					world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState());
				} else if (block == Blocks.GRASS) {
					world.setBlockState(pos, ModBlocks.waste_earth.getDefaultState());
				}
			}
		}
	}


	public void clearMeteorPath(World world, int x, int y, int z) {
		for(BlockPos blockPos : getBlocksInRadius(world, x, y, z, 5)) {
			damageOrDestroyBlock(world, blockPos.getX(), blockPos.getY(), blockPos.getZ());
		}
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
		if(motionY < -2.5)
			motionY = -2.5;

        if(this.world.getBlockState(new BlockPos((int)this.posX, (int)this.posY, (int)this.posZ)).getMaterial() != Material.AIR)
        {
			if (!this.world.isRemote && CompatibilityConfig.isWarDim(world)) {
				BlockPos pos = new BlockPos(this.posX, this.posY, this.posZ);
				if (world.getBlockState(pos).getMaterial() != Material.AIR) {
					clearMeteorPath(world, pos.getX(), pos.getY(), pos.getZ());
					world.createExplosion(this, this.posX, this.posY, this.posZ, 5 + rand.nextFloat(), !safe);
					if (GeneralConfig.enableMeteorTails) {
						ExplosionLarge.spawnParticles(world, posX, posY + 5, posZ, 75);
						ExplosionLarge.spawnParticles(world, posX + 5, posY, posZ, 75);
						ExplosionLarge.spawnParticles(world, posX - 5, posY, posZ, 75);
						ExplosionLarge.spawnParticles(world, posX, posY, posZ + 5, 75);
						ExplosionLarge.spawnParticles(world, posX, posY, posZ - 5, 75);
					}
					(new Meteorite()).generate(world, rand,
							(int)Math.round(this.posX - 0.5D),
							(int)Math.round(this.posY - 0.5D),
							(int)Math.round(this.posZ - 0.5D));
					this.world.playSound(null, this.posX, this.posY, this.posZ,
							HBMSoundHandler.oldExplosion, SoundCategory.HOSTILE,
							12.5F, 0.5F + this.rand.nextFloat() * 0.1F);
					this.setDead();
				}
			}
			if (world.isRemote) {
				// Initialize the looping meteor sound if it doesn't exist
				if (this.audio == null) {
					this.audio = createAudioLoop();
					if (this.audio != null) {
						this.audio.setDoesRepeat(true);
						this.audio.updateVolume(1.0F);
						this.audio.updateRange(200F);
						this.audio.setKeepAlive(40); // initial buffer for AudioDynamic
						this.audio.startSound();
					}
				}

				if (this.audio != null) {
					this.audio.keepAlive();
					this.audio.updatePosition((float) this.posX, (float) this.posY, (float) this.posZ);
					if (this.isDead && this.audio != null) {
						this.audio.stopSound();
						this.audio = null;
					}
					}
				} else {
					if (this.audio != null && this.audio.isPlaying()) {
						this.audio.keepAlive();
						this.audio.updateVolume(1F);
						this.audio.updatePosition((float) this.posX, (float) this.posY, (float) this.posZ);
					} else {
						EntityPlayer player = MainRegistry.proxy.me();
						if (player != null) {
							double distance = player.getDistanceSq(this.posX, this.posY, this.posZ);
							if (distance < 110 * 110 && this.audio != null) {
								this.audio.startSound();
							}
						}
					}
				}
			}

        if(GeneralConfig.enableMeteorTails && world.isRemote && world.isAreaLoaded(new BlockPos(posX, posY, posZ), 6)) {

    		NBTTagCompound data = new NBTTagCompound();
    		data.setString("type", "exhaust");
    		data.setString("mode", "meteor");
    		data.setInteger("count", 10);
    		data.setDouble("width", 1);
    		data.setDouble("posX", posX - motionX);
    		data.setDouble("posY", posY - motionY);
    		data.setDouble("posZ", posZ - motionZ);

    		MainRegistry.proxy.effectNT(data);
        }

	}

	public AudioWrapper createAudioLoop() {
		return MainRegistry.proxy.getLoopedSound(
				HBMSoundHandler.meteoriteFallingLoop,
				SoundCategory.BLOCKS,
				(float) this.posX,
				(float) this.posY,
				(float) this.posZ,
				100F,
				0.9F + this.rand.nextFloat() * 0.2F,
				0
		);
	}
	@Override
	@SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        return distance < 25000;
    }

    @Override
	@SideOnly(Side.CLIENT)
    public int getBrightnessForRender() {
        return 15728880;
    }

    @Override
	public float getBrightness() {
        return 1.0F;
    }
	
	@Override
	protected void onImpact(RayTraceResult result) {
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		this.safe = nbt.getBoolean("safe");
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setBoolean("safe", safe);
	}
}
