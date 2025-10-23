package com.hbm.items.food;

import com.hbm.items.ModItems;
import com.hbm.items.ModItems.ArmorSets;
import com.hbm.items.gear.ArmorFSB;
import com.hbm.lib.Library;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemPancake extends ItemFood {

	public ItemPancake(int amount, float saturation, boolean isWolfFood, String s) {
		super(amount, saturation, isWolfFood);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setAlwaysEdible();
		
		ModItems.ALL_ITEMS.add(this);
	}

	@Override
	protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
		for(ItemStack st : player.inventory.armorInventory) {
    		if(st == null) continue;
			Library.chargeBatteryIfValid(st, Long.MAX_VALUE, true);
    	}
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		if(ArmorFSB.hasFSBArmorIgnoreCharge(player) && player.inventory.armorInventory.get(3).getItem() == ArmorSets.bj_helmet) {
        	return super.onItemRightClick(world, player, hand);
    	}

    	if(!world.isRemote)
    		player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Your teeth are too soft to eat this."));
		return ActionResult.newResult(EnumActionResult.FAIL, player.getHeldItem(hand));
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
		list.add("Can be eaten to recharge lunar cybernetic armor");
		list.add("Not for people with weak molars");
		list.add("");
		list.add("Half burnt and smells horrible");
	}
}
