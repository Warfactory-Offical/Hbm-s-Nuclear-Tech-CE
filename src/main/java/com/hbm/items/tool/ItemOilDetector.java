package com.hbm.items.tool;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemOilDetector extends Item {

	public ItemOilDetector(String s) {
		this.setTranslationKey(s);
		this.setRegistryName(s);
		
		ModItems.ALL_ITEMS.add(this);
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add(I18n.format(this.getTranslationKey() + ".desc1"));
		tooltip.add(I18n.format(this.getTranslationKey() + ".desc2"));
	}

	public static boolean isOil(World world, BlockPos b){
		return world.getBlockState(b).getBlock() == ModBlocks.ore_oil;
	}

	public static boolean isBedrockOil(World world, BlockPos b){
		return world.getBlockState(b).getBlock() == ModBlocks.ore_bedrock_oil;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		boolean bedrockoil = false;
		boolean oil = false;
		int x = (int)player.posX;
		int y = (int)player.posY;
		int z = (int)player.posZ;
		MutableBlockPos mPos = new BlockPos.MutableBlockPos();
		
		boolean directoil = false;
		for(int ly = y + 15; ly > 0; ly--){
			directoil |= isOil(world, mPos.setPos(x, ly, z));
			if(directoil) break;
		}
		boolean directBedrock = isBedrockOil(world, new BlockPos(x, 0, z));
		
		int range = 25;
		int samples = 50;

		int lx = 0;
		int lz = 0;
		for(int i = 0; i < samples; i++){
			if(oil || bedrockoil) break;
			lx = (int)MathHelper.clamp(world.rand.nextGaussian() * range/2F, -range, range);
			lz = (int)MathHelper.clamp(world.rand.nextGaussian() * range/2F, -range, range);
			for(int ly = y + 15; ly > 0; ly--){
				oil |= isOil(world, mPos.setPos(x + lx, ly, z + lz));
				if(oil) break;
			}
			bedrockoil |= isBedrockOil(world, mPos.setPos(x + lx, 0, z + lz));
		}
		
		if(!world.isRemote) {
			
			if(directBedrock) {
				player.sendMessage(new TextComponentTranslation(this.getTranslationKey() + ".bullseyeBedrock").setStyle(new Style().setColor(TextFormatting.DARK_GREEN)));
			} else if(directoil) {
				player.sendMessage(new TextComponentTranslation(this.getTranslationKey() + ".bullseye").setStyle(new Style().setColor(TextFormatting.GREEN)));
			
			} else if(bedrockoil) {
				player.sendMessage(new TextComponentTranslation(this.getTranslationKey() + ".detectedBedrock").setStyle(new Style().setColor(TextFormatting.GOLD)));
			} else if(oil) {
				player.sendMessage(new TextComponentTranslation(this.getTranslationKey() + ".detected").setStyle(new Style().setColor(TextFormatting.YELLOW)));
			
			} else {
				player.sendMessage(new TextComponentTranslation(this.getTranslationKey() + ".noOil").setStyle(new Style().setColor(TextFormatting.RED)));
			}
		}

    	world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.techBleep, SoundCategory.PLAYERS, 1.0F, 1.0F);
		
		player.swingArm(hand);
		
		return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
	}
}
