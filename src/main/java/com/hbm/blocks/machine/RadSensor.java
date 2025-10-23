package com.hbm.blocks.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.tileentity.machine.TileEntityRadSensor;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public class RadSensor extends BlockContainer {

	public RadSensor(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		
		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(world.isRemote)
		{
			return true;
		} else if(player != null){
	    	world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.techBoop, SoundCategory.BLOCKS, 1.0F, 1.0F);

	    	TileEntityRadSensor entity = (TileEntityRadSensor) world.getTileEntity(pos);
	    	player.sendMessage(new TextComponentString("§6===== ☢ Radiaton Sensor ☢ =====§r"));
    		player.sendMessage(new TextComponentString("§eCurrent chunk radiation: §a"+entity.chunkRads+" RAD/s§r"));
			player.sendMessage(new TextComponentString("§eRedstone signal output: §c"+entity.redstoneOutput+"§r"));
			player.sendMessage(new TextComponentString("§eRecieved radiation dose: §a"+entity.recievedDose+" RAD§r"));
			player.sendMessage(new TextComponentString("§eComparator signal output: §c"+entity.comparatorOutput+"§r"));
			return true;
		} else {
			return false;
		}
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityRadSensor();
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}



	@Override
	public boolean canProvidePower(IBlockState state) {
		return true;
	}

	@Override
	public boolean getWeakChanges(IBlockAccess world, BlockPos pos){
		return false;
	}
	
	@Override
	public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		if(side == EnumFacing.UP)
			return 0;
		TileEntityRadSensor entity = (TileEntityRadSensor) blockAccess.getTileEntity(pos);
        return entity.redstoneOutput;
	}

	@Override
	public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		return getWeakPower(blockState, blockAccess, pos, side);
	}




	@Override
	public boolean hasComparatorInputOverride(IBlockState state){
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos){
		TileEntityRadSensor entity = (TileEntityRadSensor) worldIn.getTileEntity(pos);
        return entity.comparatorOutput;
	}

	@Override
	public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
		super.addInformation(stack, player, tooltip, advanced);
		tooltip.add("Power with redstone from below to reset dose counter");
	}
}
