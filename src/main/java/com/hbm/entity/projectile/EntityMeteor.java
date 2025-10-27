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
	private AudioWrapper audioFly;

	public EntityMeteor(World world) {
		super(world);
		this.ignoreFrustumCheck = true;
		this.isImmuneToFire = true;
        this.setSize(4F, 4F);
        //mlbv: yeah upstream did this in the ctor with keepAlive = 0; not really sure what they are doing..
        if(world.isRemote)
            audioFly = MainRegistry.proxy.getLoopedSound(HBMSoundHandler.meteoriteFallingLoop, SoundCategory.BLOCKS, (float) this.posX, (float) this.posY, (float) this.posZ, 1F, 100F, 0.9F + this.rand.nextFloat() * 0.2F, 0);
    }

	public List<BlockPos> getBlocksInRadius(BlockPos pos, int radius) {
		List<BlockPos> foundBlocks = new ArrayList<>();

		int rSq = radius * radius;
		for(int dx = -radius; dx <= radius; dx++) {
			for(int dy = -radius; dy <= radius; dy++) {
				for(int dz = -radius; dz <= radius; dz++) {
					// Check if point (dx, dy, dz) lies inside the sphere
					if(dx * dx + dy * dy + dz * dz <= rSq) {
						foundBlocks.add(pos.add(dx, dy, dz));
					}
				}
			}
		}
		return foundBlocks;
	}

	public void damageOrDestroyBlock(World world, BlockPos pos) {
		if (safe) return;

        // Get current block info
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (block.isAir(state, world, pos)) return;
		float hardness = state.getBlockHardness(world, pos);

		// Check if the block is weak and can be destroyed
		if (block == Blocks.LEAVES || block == Blocks.LOG || (hardness >= 0 && hardness <= 0.3F)) {
            // Destroy the block
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


	public void clearMeteorPath(World world, BlockPos pos) {
		for(BlockPos blockPos : getBlocksInRadius(pos, 5)) {
			damageOrDestroyBlock(world, blockPos);
		}
	}


	@Override
	public void onUpdate() {
        if(!world.isRemote && !GeneralConfig.enableMeteorStrikes) {
            this.setDead();
            return;
        }

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
        BlockPos pos = getPosition();
        if (!this.world.isRemote && this.posY < 260 && CompatibilityConfig.isWarDim(world)) {
            clearMeteorPath(world, pos);
            IBlockState currentBlock = world.getBlockState(pos);
            if (!currentBlock.getBlock().isAir(currentBlock, world, pos)) {
                world.createExplosion(this, this.posX, this.posY, this.posZ, 5 + rand.nextFloat(), !safe);
                if (GeneralConfig.enableMeteorTails) {
                    ExplosionLarge.spawnRubble(world, this.posX, this.posY, this.posZ, 25);

                    ExplosionLarge.spawnParticles(world, posX, posY + 5, posZ, 75);
                    ExplosionLarge.spawnParticles(world, posX + 5, posY, posZ, 75);
                    ExplosionLarge.spawnParticles(world, posX - 5, posY, posZ, 75);
                    ExplosionLarge.spawnParticles(world, posX, posY, posZ + 5, 75);
                    ExplosionLarge.spawnParticles(world, posX, posY, posZ - 5, 75);
                }
                // Bury the meteor into the ground
                int spawnPosX = (int) (Math.round(this.posX - 0.5D) + (safe ? 0 : (this.motionZ * 4)));
                int spawnPosY = (int) Math.round(this.posY - (safe ? 0 : 4));
                int spawnPosZ = (int) (Math.round(this.posZ - 0.5D) + (safe ? 0 : (this.motionZ * 4)));
                new Meteorite().generate(world, rand, spawnPosX, spawnPosY, spawnPosZ, safe, true, true);
                clearMeteorPath(world, new BlockPos(spawnPosX, spawnPosY, spawnPosZ));
                this.world.playSound(null, this.posX, this.posY, this.posZ,
                        HBMSoundHandler.oldExplosion, SoundCategory.HOSTILE,
                        10000.5F, 0.5F + this.rand.nextFloat() * 0.1F);
                this.setDead();
            }
        }

        // Sound
        if (world.isRemote) {
            if(this.isDead) {
                if (this.audioFly != null) this.audioFly.stopSound();

            } else if(this.audioFly.isPlaying()) {
                // Update sound
                this.audioFly.keepAlive();
                this.audioFly.updateVolume(1F);
                this.audioFly.updatePosition((float) this.posX, (float) this.posY, (float) this.posZ);
            } else {
                EntityPlayer player = MainRegistry.proxy.me();
                double distance = player.getDistanceSq(this.posX, this.posY, this.posZ);
                if(distance < 110 * 110) {
                    this.audioFly.startSound();
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
