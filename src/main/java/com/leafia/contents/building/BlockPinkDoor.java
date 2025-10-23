package com.leafia.contents.building;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockModDoor;
import com.hbm.entity.grenade.EntityGrenadeZOMG;
import com.hbm.entity.projectile.EntityBoxcar;
import com.hbm.entity.projectile.EntityBulletBase;
import com.hbm.entity.projectile.EntityFallingNuke;
import com.hbm.entity.projectile.EntityRBMKDebris;
import com.hbm.entity.projectile.EntityRBMKDebris.DebrisType;
import com.hbm.explosion.ExplosionChaos;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.handler.BulletConfigSyncingUtil;
import com.hbm.interfaces.IBomb;
import com.hbm.lib.HBMSoundEvents;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;

// Why. Just why. This is brutal.
public class BlockPinkDoor extends BlockModDoor implements IBomb {
	public BlockPinkDoor(Material materialIn,String s) {
		super(materialIn,s);
		this.setSoundType(SoundType.WOOD);
	}
	protected void spawnDebris(World world,BlockPos pos,DebrisType type) { // oh boy
		EntityRBMKDebris debris = new EntityRBMKDebris(world, pos.getX() + 0.5D, pos.getY() + 1, pos.getZ() + 0.5D, type);
		debris.motionX = world.rand.nextGaussian() * 0.25D * 2;
		debris.motionZ = world.rand.nextGaussian() * 0.25D * 2;
		debris.motionY = 0.5D + world.rand.nextDouble() * 1.5D;

		if(type == DebrisType.LID) {
			debris.motionX *= 0.5D;
			debris.motionY += 0.5D;
			debris.motionZ *= 0.5D;
		}

		world.spawnEntity(debris);
	}
	protected void fuckyou(World world,BlockPos pos,int height) {
		EntityBulletBase projectile = new EntityBulletBase(world,BulletConfigSyncingUtil.NUKE_NORMAL);
		projectile.setPosition(pos.getX(),world.getHeight(pos.getX(),pos.getY())+height,pos.getZ());
		projectile.setVelocity(0,-0.15,0);
		world.spawnEntity(projectile);
	}
	@Override
	public BombReturnCode explode(World world,BlockPos pos,Entity detonator) {
		world.setBlockToAir(pos);
		/*PacketDispatcher.wrapper.sendToAllAround(
				new CommandLeaf.ShakecamPacket(new String[]{
						"type=smooth",
						"preset=RUPTURE",
						"duration/2",
						"blurDulling*4","blurExponent*2",
						"speed*1.5","duration/2",
						"range="+300
				}).setPos(pos),
				new NetworkRegistry.TargetPoint(world.provider.getDimension(),pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5,350)
		);*/
		world.createExplosion(null,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,15,true);
		//new PinkRBMK().emit(new Vec3d(pos).add(.5,-1,.5),new Vec3d(0,0,0),world.provider.getDimension());
		world.playSound(null,pos,HBMSoundEvents.rbmk_explosion,SoundCategory.BLOCKS,30,1);
		for (int i = 0; i < 15; i++) spawnDebris(world,pos,DebrisType.BLANK);
		for (int i = 0; i < 10; i++) spawnDebris(world,pos,DebrisType.GRAPHITE);
		for (int i = 0; i < 4; i++) spawnDebris(world,pos,DebrisType.LID);
		for (int i = 0; i < 6; i++) spawnDebris(world,pos,DebrisType.FUEL);
		for (int i = 0; i < 9; i++) spawnDebris(world,pos,DebrisType.ROD);
		ExplosionLarge.spawnShrapnelShower(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 1D, 0, 35, 0.2D);
		ExplosionLarge.spawnShrapnels(world, pos.getZ() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8);
		ExplosionChaos.zomg(
				world,pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5,30,null,
				new EntityGrenadeZOMG(world,pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5)
		);
		for (int i = 0; i < 25; i++) {
			world.spawnEntity(new EntityFallingBlock(world,
					pos.getX() + world.rand.nextInt(201)-100,
					world.rand.nextInt(50)+455,
					pos.getZ() + world.rand.nextInt(201)-100,
					ModBlocks.corium_block.getDefaultState()
			));
		}
		for (int i = 0; i < 19; i++) {
			if (i == 2 || i == 3) {
				EntityBoxcar pippo = new EntityBoxcar(world);
				pippo.posX = pos.getX() + world.rand.nextGaussian() * 25;
				pippo.posY = 350;
				pippo.posZ = pos.getZ() + world.rand.nextGaussian() * 25;
				world.spawnEntity(pippo);
				continue;
			}
			fuckyou(world,pos.add((world.rand.nextDouble()*2-0.5)*20,0,(world.rand.nextDouble()*2-0.5)*20),50+i*15);
		}
		EntityFallingNuke nuke = new EntityFallingNuke(world,detonator,0,0,174,0,50,0,0,0);
		nuke.setPosition(pos.getX(),world.getHeight(pos.getX(),pos.getZ())+80,pos.getZ());
		world.spawnEntity(nuke);
		return BombReturnCode.DETONATED;
	}
}