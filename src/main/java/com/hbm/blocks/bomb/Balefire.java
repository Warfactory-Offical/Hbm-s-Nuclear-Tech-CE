package com.hbm.blocks.bomb;

import com.hbm.blocks.ModBlocks;
import com.hbm.potion.HbmPotion;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.Random;

public class Balefire extends BlockFire {

	public Balefire(String s) {
		super();
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setCreativeTab(null);
		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (!world.getGameRules().getBoolean("doFireTick")) return;
		if (!world.isAreaLoaded(pos, 4)) return;

		if (!this.canPlaceBlockAt(world, pos)) {
			world.setBlockToAir(pos);
			return;
		}

		final int age = state.getValue(AGE);

		if (age < 15) {
			world.scheduleUpdate(pos, this, this.tickRate(world) + rand.nextInt(10));
		}

		if (!hasNeighborThatCanCatchFire(world, pos) &&
			!world.getBlockState(pos.down()).isSideSolid(world, pos.down(), EnumFacing.UP)) {
			world.setBlockToAir(pos);
			return;
		}

		if (age < 15) {
			tryCatchFire(world, pos.east(),  500, rand, age, EnumFacing.WEST);
			tryCatchFire(world, pos.west(),  500, rand, age, EnumFacing.EAST);
			tryCatchFire(world, pos.north(), 500, rand, age, EnumFacing.SOUTH);
			tryCatchFire(world, pos.south(), 500, rand, age, EnumFacing.NORTH);
			tryCatchFire(world, pos.up(),    300, rand, age, EnumFacing.DOWN);
			tryCatchFire(world, pos.down(),  300, rand, age, EnumFacing.UP);

			final int h = 3;
			for (int dx = -h; dx <= h; dx++) {
				for (int dz = -h; dz <= h; dz++) {
					for (int dy = -1; dy <= 4; dy++) {
						if (dx == 0 && dy == 0 && dz == 0) continue;

						BlockPos p = pos.add(dx, dy, dz);
						IBlockState s = world.getBlockState(p);

						if (s.getBlock() == this) {
							int theirAge = s.getValue(AGE);
							if (theirAge > age + 1) {
								world.setBlockState(p, s.withProperty(AGE, Math.min(age + 1, 15)), 3);
							}
							continue;
						}

						int neighborChance = getChanceOfNeighborsEncouragingFire(world, p);
						if (neighborChance <= 0) continue;

						int fireLimit = 100;
						if (dy > 1) fireLimit += (dy - 1) * 100;

						int adjusted = (neighborChance + 40 + world.getDifficulty().getId() * 7) / (age + 30);

						if (adjusted > 0 && rand.nextInt(fireLimit) <= adjusted) {
							int newAge = Math.min(age + 1, 15);
							world.setBlockState(p, getDefaultState().withProperty(AGE, newAge), 3);
						}
					}
				}
			}
		}
	}

	private boolean hasNeighborThatCanCatchFire(World world, BlockPos pos) {
		for (EnumFacing f : EnumFacing.values()) {
			if (this.canCatchFire(world, pos.offset(f), f.getOpposite())) return true;
		}
		return false;
	}

	private int getChanceOfNeighborsEncouragingFire(World world, BlockPos pos) {
		if (!world.isAirBlock(pos)) return 0;
		int spread = 0;
		for (EnumFacing f : EnumFacing.values()) {
			BlockPos n = pos.offset(f);
			spread = Math.max(
					world.getBlockState(n).getBlock().getFireSpreadSpeed(world, n, f.getOpposite()),
					spread
			);
		}
		return spread;
	}

	private void tryCatchFire(World world, BlockPos pos, int chance, Random rand, int fireAge, EnumFacing face) {
		Block target = world.getBlockState(pos).getBlock();
		int flammability = target.getFlammability(world, pos, face);
		if (rand.nextInt(chance) < flammability) {
			boolean tnt = (target == Blocks.TNT);
			int newAge = Math.min(fireAge + 1, 15);
			world.setBlockState(pos, this.getDefaultState().withProperty(AGE, newAge), 3);

			if (tnt) {
				IBlockState ibs = Blocks.TNT.getDefaultState().withProperty(BlockTNT.EXPLODE, Boolean.TRUE);
				Blocks.TNT.onPlayerDestroy(world, pos, ibs);
			}
		}
	}

	@Override
	public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		entityIn.setFire(10);

		if (entityIn instanceof EntityLivingBase)
			((EntityLivingBase) entityIn).addPotionEffect(new PotionEffect(HbmPotion.radiation, 5 * 20, 9));
	}

	/* TODO
	@SideOnly(Side.CLIENT)
	public static void registerColorHandler(ColorHandlerEvent.Block evt) {
		IBlockColor balefireColor = (state, world, pos, tintIndex) -> {
			int age = state.getValue(BlockFire.AGE);
			return Color.HSBtoRGB(0F, 0F, 1F - age / 30F);
		};
		evt.getBlockColors().registerBlockColorHandler(balefireColor, ModBlocks.balefire);
	}
	 */
}
