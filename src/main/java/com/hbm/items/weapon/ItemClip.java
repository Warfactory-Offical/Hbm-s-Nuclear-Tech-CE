package com.hbm.items.weapon;

import com.hbm.items.ModItems;
import com.hbm.items.ModItems.Armory;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import java.util.List;

public class ItemClip extends Item {

	public ItemClip(String s) {
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setMaxDamage(1);
		this.setMaxStackSize(32);
		ModItems.ALL_ITEMS.add(this);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand handIn) {
		ItemStack stack = player.getHeldItem(handIn);
		stack.shrink(1);;
		if(stack.getCount() <= 0)
			stack.damageItem(5, player);

		return super.onItemRightClick(worldIn, player, handIn);
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if(this == Armory.ammo_container)
		{
			tooltip.add("Gives ammo for all held weapons.");
		}
	}
}
