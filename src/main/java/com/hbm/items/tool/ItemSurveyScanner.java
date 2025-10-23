package com.hbm.items.tool;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockBedrockOreTE;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemSurveyScanner extends Item {

	public ItemSurveyScanner(String s) {
		this.setTranslationKey(s);
		this.setRegistryName(s);
		
		ModItems.ALL_ITEMS.add(this);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (!world.isRemote) {

			BlockPos playerPos = new BlockPos(player);
			int x = playerPos.getX();
			int y = playerPos.getY();
			int z = playerPos.getZ();

			boolean hasOil = false;
			boolean hasColtan = false;
			boolean hasDepth = false;
			boolean hasSchist = false;
			boolean hasAussie = false;
			BlockBedrockOreTE.TileEntityBedrockOre tile = null;

			for (int a = -5; a <= 5; a++) {
				for (int b = -5; b <= 5; b++) {
					for (int i = y + 15; i > 1; i -= 2) {

						BlockPos checkPos = new BlockPos(x + a * 5, i, z + b * 5);
						Block block = world.getBlockState(checkPos).getBlock();

						if (block == ModBlocks.ore_oil) hasOil = true;
						else if (block == ModBlocks.ore_coltan) hasColtan = true;
						else if (block == ModBlocks.stone_depth) hasDepth = true;
						else if (block == ModBlocks.stone_depth_nether) hasDepth = true;
						else if (block == ModBlocks.stone_gneiss) hasSchist = true;
						else if (block == ModBlocks.ore_australium) hasAussie = true;
					}

					BlockPos bedrockPos = new BlockPos(x + a * 2, 0, z + b * 2);
					Block bedrockBlock = world.getBlockState(bedrockPos).getBlock();

					if (bedrockBlock == ModBlocks.ore_bedrock_block) {
						TileEntity te = world.getTileEntity(bedrockPos);
						if (te instanceof BlockBedrockOreTE.TileEntityBedrockOre) {
							tile = (BlockBedrockOreTE.TileEntityBedrockOre) te;
						}
					}
				}
			}

			if (hasOil) player.sendMessage(new TextComponentTranslation("chat.surveyscanner.oil").setStyle(new Style().setColor(TextFormatting.BLACK)));
			if (hasColtan) player.sendMessage(new TextComponentTranslation("chat.surveyscanner.coltan").setStyle(new Style().setColor(TextFormatting.GOLD)));
			if (hasDepth) player.sendMessage(new TextComponentTranslation("chat.surveyscanner.depth").setStyle(new Style().setColor(TextFormatting.GRAY)));
			if (hasSchist) player.sendMessage(new TextComponentTranslation("chat.surveyscanner.schist").setStyle(new Style().setColor(TextFormatting.DARK_AQUA)));
			if (hasAussie) player.sendMessage(new TextComponentTranslation("chat.surveyscanner.australium").setStyle(new Style().setColor(TextFormatting.YELLOW)));
			if (tile != null && tile.resource != null) {
				player.sendMessage(new TextComponentTranslation("chat.surveyscanner.bedrock", tile.resource.getDisplayName()).setStyle(new Style().setColor(TextFormatting.RED)));
			}
		}

		world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.techBleep, SoundCategory.PLAYERS, 1.0F, 1.0F);
		
		player.swingArm(hand);
		
		return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		//Alcater: o_o DAMN an Easteregg
		if(world.getBlockState(pos).getBlock() == ModBlocks.block_beryllium && Library.hasInventoryItem(player.inventory, ModItems.entanglement_kit)) {
    		player.changeDimension(1);
    		return EnumActionResult.SUCCESS;
    	}
    	
    	return EnumActionResult.PASS;
	}
}
