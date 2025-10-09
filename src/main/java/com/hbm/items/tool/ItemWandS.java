package com.hbm.items.tool;

import com.hbm.items.ModItems;
import com.hbm.util.I18nUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class ItemWandS extends Item {

	public ItemWandS(String s) {
		this.setTranslationKey(s);
		this.setRegistryName(s);
		
		ModItems.ALL_ITEMS.add(this);
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add(I18nUtil.resolveKey("desc.creative"));
		tooltip.add(I18nUtil.resolveKey("desc.structurewand.1"));
		tooltip.add(I18nUtil.resolveKey("desc.structurewand.2"));
		tooltip.add(I18nUtil.resolveKey("desc.structurewand.3"));
		if(stack.getTagCompound() != null)
		{
			switch(stack.getTagCompound().getInteger("building"))
			{
			case 0:
				tooltip.add(I18nUtil.resolveKey("desc.structurewand.factory"));
				break;
			case 1:
				tooltip.add(I18nUtil.resolveKey("desc.structurewand.factoryadvanced"));
				break;
			case 2:
				tooltip.add(I18nUtil.resolveKey("desc.structurewand.hadron"));
				break;
			case 3:
				tooltip.add(I18nUtil.resolveKey("desc.structurewand.watz"));
				break;
			}
		}
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);
		if(stack.getTagCompound() == null)
		{
			stack.setTagCompound(new NBTTagCompound());
			stack.getTagCompound().setInteger("building", 0);
		}
		
		boolean up = player.rotationPitch <= 0.5F;
		
		if(!world.isRemote)
		{
			Random rand = new Random();

			if (stack.getTagCompound().getInteger("building") == 0) {
			}

		}
		
		return EnumActionResult.SUCCESS;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand handIn) {
		if(player.isSneaking())
		{
			ItemStack stack = player.getHeldItem(handIn);
			if(stack.getTagCompound() == null)
			{
				stack.setTagCompound(new NBTTagCompound());
				stack.getTagCompound().setInteger("building", 0);
				if(world.isRemote)
					player.sendMessage(new TextComponentTranslation("chat.structurewand.set.factory"));
			} else {
				int i = stack.getTagCompound().getInteger("building");
				i++;
				stack.getTagCompound().setInteger("building", i);
				if(i >= 6) {
					stack.getTagCompound().setInteger("building", 0);
				}
				
				if(world.isRemote)
				{
				switch(i)
				{
					case 0:
						player.sendMessage(new TextComponentTranslation("chat.structurewand.set.factory"));
						break;
					case 1:
						player.sendMessage(new TextComponentTranslation("chat.structurewand.set.factoryadvanced"));
						break;
					case 2:
						player.sendMessage(new TextComponentTranslation("chat.structurewand.set.nuclear"));
						break;
					case 3:
						player.sendMessage(new TextComponentTranslation("chat.structurewand.set.hadron"));
						break;
					case 4:
						player.sendMessage(new TextComponentTranslation("chat.structurewand.set.watz"));
						break;
					default:
						player.sendMessage(new TextComponentTranslation("chat.structurewand.set.factory"));
						break;
					}
				}
			}
		}
				
		return super.onItemRightClick(world, player, handIn);
	}
}
