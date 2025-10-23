package com.hbm.items.tool;

import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

import java.util.List;
import java.util.Set;

public class ItemMultitoolTool extends ItemTool {

	public ItemMultitoolTool(float f, ToolMaterial materialIn, Set<Block> effectiveBlocksIn, String s) {
		super(f, 0, materialIn, effectiveBlocksIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		
		ModItems.ALL_ITEMS.add(this);
	}
	
	@Override
	public float getDestroySpeed(ItemStack stack, IBlockState state) {
		return this.efficiency;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if(player.isSneaking()) {
			
	        world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.techBoop, SoundCategory.PLAYERS, 2.0F, 1.0F);
	        
			if(this == ModItems.multitool_dig) {
				ItemStack item = new ItemStack(ModItems.multitool_silk, 1, stack.getItemDamage());
				item.addEnchantment(Enchantments.SILK_TOUCH, 3);
				return ActionResult.newResult(EnumActionResult.SUCCESS, item);
			} else if (this == ModItems.multitool_silk) {
				ItemStack item = new ItemStack(ModItems.multitool_ext, 1, stack.getItemDamage());
				item.addEnchantment(Enchantments.FIRE_ASPECT, 3);
				return ActionResult.newResult(EnumActionResult.SUCCESS, item);
			}
		}
		
		return ActionResult.newResult(EnumActionResult.PASS, stack);
	}
	
	@Override
	public boolean canHarvestBlock(IBlockState blockIn) {
		return true;
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if(this == ModItems.multitool_dig) {
			tooltip.add("Breaks blocks extremely fast");
			tooltip.add("Extra drops for ores");
		}
		if(this == ModItems.multitool_silk) {
			tooltip.add("Breaks blocks extremely fast");
			tooltip.add("Ores will drop themselves via silk touch");
		}
	}
	
}
