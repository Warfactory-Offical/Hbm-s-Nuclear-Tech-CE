package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.hazard.HazardSystem;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockHazardFalling extends BlockFalling {

	private float rad = 0.0F;

	private boolean beaconable = false;

	public BlockHazardFalling(Material mat, String s) {
		super(mat);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setHarvestLevel("shovel", 0);

		ModBlocks.ALL_BLOCKS.add(this);
	}

	public BlockHazardFalling(Material mat, String s, SoundType type, float rads) {
		this(mat, s);
		this.setSoundType(type);
		this.rad = rads;
	}

	public BlockHazardFalling(SoundType type, String s, float rads) {
		this(Material.SAND, s, type, rads);
	}

	public BlockHazardFalling(Material mat, String s, SoundType type) {
		this(mat, s, type, 0);
	}

	public BlockHazardFalling makeBeaconable() {
		this.beaconable = true;
		return this;
	}

	@Override
	public boolean isBeaconBase(IBlockAccess worldObj, BlockPos pos, BlockPos beacon){
		return beaconable;
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand){
		if(this.rad > 0) {
            ChunkRadiationManager.proxy.incrementRad(world, pos, rad*0.01F, rad);
        }
		super.updateTick(world, pos, state, rand);
	}

	@Override
	public int tickRate(World world) {

		if(this.rad > 0)
			return 20;

		return super.tickRate(world);
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state){
		super.onBlockAdded(world, pos, state);
		if(this.rad > 0){
			this.setTickRandomly(true);
			world.scheduleUpdate(pos, this, this.tickRate(world));
		}
	}

	@Override
	public void onEntityWalk(World worldIn, BlockPos pos, Entity entity) {
		if(entity instanceof EntityLivingBase)
			HazardSystem.applyHazards(this, (EntityLivingBase)entity);
	}

	@Override
	public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entity){
		if(entity instanceof EntityLivingBase)
			HazardSystem.applyHazards(this, (EntityLivingBase)entity);
	}
}