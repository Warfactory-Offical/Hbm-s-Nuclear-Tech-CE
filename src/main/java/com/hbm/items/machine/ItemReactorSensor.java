package com.hbm.items.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemReactorSensor extends Item {

	public ItemReactorSensor(String s) {
		this.setTranslationKey(s);
		this.setRegistryName(s);

		ModItems.ALL_ITEMS.add(this);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);
		Block b = world.getBlockState(pos).getBlock();

		if (b == ModBlocks.reactor_research) {

			if(stack.getTagCompound() == null)
				stack.setTagCompound(new NBTTagCompound());

			if (!world.isRemote) {
				player.sendMessage(
						new TextComponentString("[")
								.setStyle(new Style().setColor(TextFormatting.DARK_AQUA))
								.appendSibling(new TextComponentTranslation(this.getTranslationKey() + ".name")
										.setStyle(new Style().setColor(TextFormatting.DARK_AQUA)))
								.appendSibling(new TextComponentString("] ")
										.setStyle(new Style().setColor(TextFormatting.DARK_AQUA)))
								.appendSibling(new TextComponentString("Position set!")
										.setStyle(new Style().setColor(TextFormatting.GREEN)))
				);
			}

			stack.getTagCompound().setInteger("x", pos.getX());
			stack.getTagCompound().setInteger("y", pos.getY());
			stack.getTagCompound().setInteger("z", pos.getZ());

			world.playSound(player, player.posX, player.posY, player.posZ, HBMSoundHandler.techBoop, SoundCategory.BLOCKS, 1.0F, 1.0F);

			return EnumActionResult.SUCCESS;

		}

		return EnumActionResult.PASS;
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if(stack.getTagCompound() != null) {
			tooltip.add("x: " + stack.getTagCompound().getInteger("x"));
			tooltip.add("y: " + stack.getTagCompound().getInteger("y"));
			tooltip.add("z: " + stack.getTagCompound().getInteger("z"));
		} else {
			tooltip.add("No reactor selected!");
		}
	}
}
