package com.hbm.items.tool;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.util.I18nUtil;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class ItemCrateCaller extends Item {

	Random rand = new Random();
	
	public ItemCrateCaller(String s) {
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.canRepair = false;
		this.setMaxDamage(4);
		
		ModItems.ALL_ITEMS.add(this);
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add(I18nUtil.resolveKey("desc.callsp"));
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return enchantment != Enchantments.UNBREAKING && enchantment != Enchantments.MENDING && super.canApplyAtEnchantingTable(stack, enchantment);
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		if (book.getItem() == Items.ENCHANTED_BOOK) {
			Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(book);
			if (enchantments.containsKey(Enchantments.MENDING) || enchantments.containsKey(Enchantments.UNBREAKING)) {
				return false;
			}
		}
		return super.isBookEnchantable(stack, book);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		stack.damageItem(1, player);

		int x = rand.nextInt(31) - 15;
		int z = rand.nextInt(31) - 15;
		
		Block crate = ModBlocks.crate;
		
		int i = rand.nextInt(1000);
		
		if(i < 350)
			crate = ModBlocks.crate_weapon;
		if(i < 100)
			crate = ModBlocks.crate_metal;
		if(i < 50)
			crate = ModBlocks.crate_lead;
		if(i == 0)
			crate = ModBlocks.crate_red;

		if(!world.isRemote)
		{
			if(world.getBlockState(new BlockPos((int)player.posX + x, 255, (int)player.posZ + z)).getBlock() == Blocks.AIR)
				world.setBlockState(new BlockPos((int)player.posX + x, 255, (int)player.posZ + z), crate.getDefaultState());
		}
		if(world.isRemote)
		{
			player.sendMessage(new TextComponentTranslation("chat.callsp"));
		}

    	world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.techBleep, SoundCategory.PLAYERS, 1.0F, 1.0F);
		
		player.swingArm(hand);
		
		return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
	}
}
