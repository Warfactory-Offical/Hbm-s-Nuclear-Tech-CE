package com.hbm.blocks.bomb;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityBalefire;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.interfaces.IBomb;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class DetCord extends Block implements IBomb {

	public DetCord(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		
		ModBlocks.ALL_BLOCKS.add(this);
	}
	
	@Override
	public void onExplosionDestroy(World world, BlockPos pos, Explosion explosionIn) {
		this.explode(world, pos, explosionIn.exploder);
	}
	
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if(worldIn.isBlockPowered(pos)){
			explode(worldIn, pos, null);
		}
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Items.AIR;
	}

	@Override
	public BombReturnCode explode(World world, BlockPos pos, Entity detonator) {
		if(!world.isRemote) {
			
			world.setBlockState(pos, Blocks.AIR.getDefaultState());
			if(this == ModBlocks.det_cord) {
				world.createExplosion(detonator, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.5F, true);
			}
			if(this == ModBlocks.det_charge) {
				ExplosionLarge.explode(world, detonator, pos.getX(), pos.getY(), pos.getZ(), 20, true, false, false);
			}
			if(this == ModBlocks.det_n2) {
				world.spawnEntity(EntityNukeExplosionMK5.statFacNoRad(world, (int)(BombConfig.n2Radius/12) * 5, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5).setDetonator(detonator));
				if(BombConfig.enableNukeClouds) {
					EntityNukeTorex.statFac(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, (int)(BombConfig.n2Radius/12) * 5);
				}
			}
			if(this == ModBlocks.det_nuke) {
				world.spawnEntity(EntityNukeExplosionMK5.statFac(world, BombConfig.missileRadius, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5).setDetonator(detonator));
				if(BombConfig.enableNukeClouds) {
					EntityNukeTorex.statFac(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, BombConfig.missileRadius);
				}
			}
			if(this == ModBlocks.det_bale) {
				EntityBalefire bf = new EntityBalefire(world);
				bf.posX = pos.getX() + 0.5;
				bf.posY = pos.getY() + 0.5;
				bf.posZ = pos.getZ() + 0.5;
				bf.destructionRange = (int) 130;
				bf.setDetonator(detonator);
				world.spawnEntity(bf);
				if(BombConfig.enableNukeClouds) {
					EntityNukeTorex.statFacBale(world, pos.getX() + 0.5, pos.getY() + 5, pos.getZ() + 0.5, 130F);
				}
			}
		}
		return BombReturnCode.DETONATED;
	}

	@Override
	public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
		if(this == ModBlocks.det_n2){
			tooltip.add("§c[Extreme Bomb]§r");
			tooltip.add(" §eRadius: "+((int)(BombConfig.n2Radius/12) * 5)+"m§r");
		}
		if(this == ModBlocks.det_nuke){
			tooltip.add("§2[Nuclear Bomb]§r");
			tooltip.add(" §eRadius: "+BombConfig.missileRadius+"m§r");
			if(!BombConfig.disableNuclear){
				tooltip.add("§2[Fallout]§r");
				tooltip.add(" §aRadius: "+(int)BombConfig.missileRadius*(1+BombConfig.falloutRange/100)+"m§r");
			}
		}
		if(this == ModBlocks.det_bale){
			tooltip.add("§a[Balefire Bomb]§r");
			tooltip.add(" §eRadius: 130m§r");
		}
	}
}
