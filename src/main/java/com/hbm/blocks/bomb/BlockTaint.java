package com.hbm.blocks.bomb;

import com.hbm.blocks.BlockBase;
import com.hbm.blocks.ModBlocks;
import com.hbm.entity.mob.EntityCreeperTainted;
import com.hbm.entity.mob.EntityTaintCrab;
import com.hbm.entity.mob.EntityTeslaCrab;
import com.hbm.main.MainRegistry;
import com.hbm.potion.HbmPotion;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockTaint extends BlockBase {

	public static final PropertyInteger TEXTURE = PropertyInteger.create("taintage", 0, 15);
	
	public BlockTaint(Material m, String s) {
		super(m, s);
		this.setTickRandomly(true);
		this.setCreativeTab(MainRegistry.controlTab);
		this.setDefaultState(this.blockState.getBaseState().withProperty(TEXTURE, 0));
	}

	@Override
	public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		return MapColor.GRAY;
	}
	
	@Override
	public void updateTick(World world, BlockPos pos1, IBlockState state, Random rand) {

		int meta = state.getValue(TEXTURE);
    	if(!world.isRemote && meta < 15) {
    		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
	    	for(int i = 0; i < 15; i++) {
	    		int a = rand.nextInt(11) + pos1.getX() - 5;
	    		int b = rand.nextInt(11) + pos1.getY() - 5;
	    		int c = rand.nextInt(11) + pos1.getZ() - 5;
	    		pos.setPos(a, b, c);
	            if(world.getBlockState(pos).getBlock().isReplaceable(world, pos) && !world.getBlockState(pos).getMaterial().isLiquid() && BlockTaint.hasPosNeightbour(world, pos))
	            	world.setBlockState(pos, ModBlocks.taint.getBlockState().getBaseState().withProperty(TEXTURE, meta + 1), 2);
	    	}
	            
		    for(int i = 0; i < 85; i++) {
		    	int a = rand.nextInt(7) + pos1.getX() - 3;
		    	int b = rand.nextInt(7) + pos1.getY() - 3;
		    	int c = rand.nextInt(7) + pos1.getZ() - 3;
		    	pos.setPos(a, b, c);
	           	if(world.getBlockState(pos).getBlock().isReplaceable(world, pos) && !world.getBlockState(pos).getMaterial().isLiquid() && BlockTaint.hasPosNeightbour(world, pos))
		           	world.setBlockState(pos, ModBlocks.taint.getBlockState().getBaseState().withProperty(TEXTURE, meta + 1), 2);
		    }
    	}
	}

	private static boolean checkAttachment(World world, BlockPos pos){
		if(!world.isAirBlock(pos)){
			return world.getBlockState(pos).getBlock() != ModBlocks.taint;
    	}
    	return false;
    }


	//Drillgon200: Ah yes, spelling.
	 public static boolean hasPosNeightbour(World world, BlockPos pos) {
	    	return checkAttachment(world, pos.add(1, 0, 0)) ||
					checkAttachment(world, pos.add(0, 1, 0)) ||
					checkAttachment(world, pos.add(0, 0, 1)) ||
					checkAttachment(world, pos.add(-1, 0, 0)) ||
					checkAttachment(world, pos.add(0, -1, 0)) ||
					checkAttachment(world, pos.add(0, 0, -1));
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
		return new AxisAlignedBB(pos, pos);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return NULL_AABB;
	}

	@Override
	public boolean isCollidable(){
		return true;
	}

	@Override
	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos){
		return false;
	}


	@Override
	public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
		int meta = world.getBlockState(pos).getBlock().getMetaFromState(state);
		int level = 15 - meta;
		
    	List<ItemStack> list = new ArrayList<>();
    	PotionEffect effect = new PotionEffect(HbmPotion.taint, 15 * 20, level);
    	effect.setCurativeItems(list);
    	
    	if(entity instanceof EntityLivingBase) {
    		if(world.rand.nextInt(50) == 0) {
    			((EntityLivingBase)entity).addPotionEffect(effect);
    		}
    	}
    	
    	if(entity instanceof EntityCreeper && !(entity instanceof EntityCreeperTainted)) {
    		EntityCreeperTainted creep = new EntityCreeperTainted(world);
    		creep.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);

    		if(!world.isRemote) {
    			entity.setDead();
    			world.spawnEntity(creep);
    		}
    	}

		if (entity instanceof EntityTeslaCrab) {
			EntityTaintCrab crab = new EntityTaintCrab(world);
			crab.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);

			if (!world.isRemote) {
				entity.setDead();
				world.spawnEntity(crab);
			}
		}
	}
	
	@Override
	public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
		tooltip.add("DO NOT TOUCH, BREATHE OR STARE AT. RUN!");
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, TEXTURE);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(TEXTURE);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(TEXTURE, meta);
	}
	
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if(!BlockTaint.hasPosNeightbour(world, pos) && !world.isRemote)
			world.setBlockToAir(pos);
	}
	
	@Override
	public boolean causesSuffocation(IBlockState state) {
		return true;
	}
}
