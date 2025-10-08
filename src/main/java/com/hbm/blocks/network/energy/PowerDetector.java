package com.hbm.blocks.network.energy;

import com.hbm.blocks.ModBlocks;
import com.hbm.tileentity.network.energy.TileEntityMachineDetector;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class PowerDetector extends BlockContainer {

	public static final PropertyBool IS_ON = PropertyBool.create("is_on");
	
	public PowerDetector(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		
		ModBlocks.ALL_BLOCKS.add(this);
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityMachineDetector();
	}
	
	@Override
	public boolean canProvidePower(IBlockState state) {
		return true;
	}
	
	@Override
	public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		return blockState.getValue(IS_ON) ? 15 : 0;
	}

	public static void updateBlockState(boolean isOn, World world, BlockPos pos){
		IBlockState i = world.getBlockState(pos);
		TileEntity entity = world.getTileEntity(pos);
		if(i.getBlock() == ModBlocks.machine_detector){
			world.setBlockState(pos, world.getBlockState(pos).withProperty(PowerDetector.IS_ON, isOn));
		}
		if (entity != null) {
			entity.validate();
			world.setTileEntity(pos, entity);
		}
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, IS_ON);
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(IS_ON) ? 1 : 0;
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return meta > 0 ? this.getDefaultState().withProperty(IS_ON, true) : this.getDefaultState().withProperty(IS_ON, false);
	}
}