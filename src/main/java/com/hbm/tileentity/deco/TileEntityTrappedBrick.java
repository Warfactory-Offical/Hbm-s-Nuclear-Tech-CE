package com.hbm.tileentity.deco;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.TrappedBrick.Trap;
import com.hbm.entity.projectile.EntityBulletBase;
import com.hbm.entity.projectile.EntityRubble;
import com.hbm.handler.BulletConfigSyncingUtil;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems.ToolSets;
import com.hbm.lib.ForgeDirection;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@AutoRegister
public class TileEntityTrappedBrick extends TileEntity implements ITickable {

	AxisAlignedBB detector = null;
	ForgeDirection dir = ForgeDirection.UNKNOWN;
	
	@Override
	public void update() {
		if(!world.isRemote) {

			if(detector == null) {
				setDetector();
			}
			
			//Apparently I still need to do a check because some chunk pregenerators are buggy.
			if(detector != null){
				List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, detector);

				if(!players.isEmpty())
					trigger();
			}
		}
	}

	private void trigger() {

		Trap trap = Trap.get(this.getBlockMetadata());

		switch(trap) {
		case FALLING_ROCKS:
			for(int x = 0; x < 3; x++) {
				for(int z = 0; z < 3; z++) {
					EntityRubble rubble = new EntityRubble(world, pos.getX() - 0.5 + x, pos.getY() - 0.5, pos.getZ() - 0.5 + z);
					rubble.setMetaBasedOnBlock(ModBlocks.reinforced_stone, 0);
					world.spawnEntity(rubble);
				}
			}
			break;
		case ARROW:
			EntityArrow arrow = new EntityTippedArrow(world);
			arrow.setPosition(pos.getX() + 0.5 + dir.offsetX, pos.getY() + 0.5, pos.getZ() + 0.5 + dir.offsetZ);
			arrow.motionX = dir.offsetX;
			arrow.motionZ = dir.offsetZ;
			world.spawnEntity(arrow);
			break;
		case FLAMING_ARROW:
			EntityArrow farrow = new EntityTippedArrow(world);
			farrow.setPosition(pos.getX() + 0.5 + dir.offsetX, pos.getY() + 0.5, pos.getZ() + 0.5 + dir.offsetZ);
			farrow.motionX = dir.offsetX;
			farrow.motionZ = dir.offsetZ;
			farrow.setFire(60);
			world.spawnEntity(farrow);
			break;
		case PILLAR:
			for(int i = 0; i < 3; i++)
				world.setBlockState(new BlockPos(pos.getX(), pos.getY() - 1 - i, pos.getZ()), ModBlocks.concrete_pillar.getDefaultState());
			break;
		case POISON_DART:
			EntityBulletBase dart = new EntityBulletBase(world, BulletConfigSyncingUtil.G20_CAUSTIC);
			dart.setPosition(pos.getX() + 0.5 + dir.offsetX, pos.getY() + 0.5, pos.getZ() + 0.5 + dir.offsetZ);
			dart.motionX = dir.offsetX;
			dart.motionZ = dir.offsetZ;
			world.spawnEntity(dart);
			break;
		case ZOMBIE:
			EntityZombie zombie = new EntityZombie(world);
			zombie.setPosition(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
			switch(world.rand.nextInt(3)) {
			case 0: zombie.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ToolSets.chernobylsign)); break;
			case 1: zombie.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ToolSets.cobalt_sword)); break;
			case 2: zombie.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ToolSets.cmb_hoe)); break;
			}
			zombie.setDropChance(EntityEquipmentSlot.MAINHAND, 1.0F);
			world.spawnEntity(zombie);
			break;
		case SPIDERS:
			for(int i = 0; i < 3; i++) {
				EntityCaveSpider spider = new EntityCaveSpider(world);
				spider.setPosition(pos.getX() + 0.5, pos.getY() - 1, pos.getZ() + 0.5);
				world.spawnEntity(spider);
			}
			break;
		default:
			break;
		}
		world.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SoundEvents.BLOCK_TRIPWIRE_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.6F);
		world.setBlockState(pos, ModBlocks.brick_jungle.getDefaultState());
	}
	
	private void setDetector() {

		Trap trap = Trap.get(this.getBlockMetadata());

		switch(trap) {
		case FALLING_ROCKS:
			detector = new AxisAlignedBB(pos.getX() - 1, pos.getY() - 3, pos.getZ() - 1, pos.getX() + 2, pos.getY(), pos.getZ() + 2);
			break;

		case PILLAR: 
			detector = new AxisAlignedBB(pos.getX() + 0.2, pos.getY() - 3, pos.getZ() + 0.2, pos.getX() + 0.8, pos.getY(), pos.getZ() + 0.8);
			break;

		case ARROW:
		case FLAMING_ARROW:
		case POISON_DART:
			setDetectorDirectional();
			break;

		case ZOMBIE:
			detector = new AxisAlignedBB(pos.getX() - 1, pos.getY() + 1, pos.getZ() - 1, pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2);
			break;

		case SPIDERS:
			detector = new AxisAlignedBB(pos.getX() - 1, pos.getY() - 3, pos.getZ() - 1, pos.getX() + 2, pos.getY(), pos.getZ() + 2);
			break;
		default:
			break;
		}
	}

	private void setDetectorDirectional() {

		List<ForgeDirection> dirs = Arrays.asList(ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.EAST, ForgeDirection.WEST);

		Collections.shuffle(dirs);

		for(ForgeDirection dir : dirs) {

			if(world.getBlockState(new BlockPos(pos.getX() + dir.offsetX, pos.getY(), pos.getZ() + dir.offsetZ)).getBlock() == Blocks.AIR) {

				double minX = pos.getX() + 0.4;
				double minY = pos.getY() + 0.4;
				double minZ = pos.getZ() + 0.4;
				double maxX = pos.getX() + 0.6;
				double maxY = pos.getY() + 0.6;
				double maxZ = pos.getZ() + 0.6;

				if(dir.offsetX > 0)
					maxX += 3;
				else if(dir.offsetX < 0)
					minX -= 3;

				if(dir.offsetZ > 0)
					maxZ += 3;
				else if(dir.offsetZ < 0)
					minZ -= 3;

				detector = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
				this.dir = dir;
				return;
			}
		}
		detector = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
	}

}
