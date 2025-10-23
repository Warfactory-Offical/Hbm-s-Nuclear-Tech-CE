package com.hbm.blocks.bomb;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems.Materials.Powders;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockCloudResidue extends Block {

	public BlockCloudResidue(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setCreativeTab(null);

		ModBlocks.ALL_BLOCKS.add(this);
	}
	
	public static boolean hasPosNeightbour(World world, BlockPos pos) {
		Block b0 = world.getBlockState(pos.add(1, 0, 0)).getBlock();
		Block b1 = world.getBlockState(pos.add(0, 1, 0)).getBlock();
		Block b2 = world.getBlockState(pos.add(0, 0, 1)).getBlock();
		Block b3 = world.getBlockState(pos.add(-1, 0, 0)).getBlock();
		Block b4 = world.getBlockState(pos.add(0, -1, 0)).getBlock();
		Block b5 = world.getBlockState(pos.add(0, 0, -1)).getBlock();
		boolean b = b0.isNormalCube(world.getBlockState(pos.add(1, 0, 0)), world, pos)
				|| b1.isNormalCube(world.getBlockState(pos.add(0, 1, 0)), world, pos)
				|| b2.isNormalCube(world.getBlockState(pos.add(0, 0, 1)), world, pos)
				|| b3.isNormalCube(world.getBlockState(pos.add(-1, 0, 0)), world, pos)
				|| b4.isNormalCube(world.getBlockState(pos.add(0, -1, 0)), world, pos)
				|| b5.isNormalCube(world.getBlockState(pos.add(0, 0, -1)), world, pos);
		return b;
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
	public boolean isNormalCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		return hasPosNeightbour(worldIn, pos);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if (!BlockCloudResidue.hasPosNeightbour(world, pos) && !world.isRemote) {
			world.setBlockToAir(pos);
		}
	}

	@Override
	public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		return MapColor.RED;
	}
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		if(rand.nextInt(25) == 1){
			return Powders.powder_cloud;
		}
		return Items.AIR;
	}
}
