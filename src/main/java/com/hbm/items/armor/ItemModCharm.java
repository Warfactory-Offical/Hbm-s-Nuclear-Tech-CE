package com.hbm.items.armor;

import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems.Inserts;
import com.hbm.lib.ModDamageSource;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;

public class ItemModCharm extends ItemArmorMod {

	public ItemModCharm(String s) {
		super(ArmorModHandler.helmet_only, false, true, false, false, s);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn){
	
		list.add("§bYou feel blessed.");
		
		if(this == Inserts.protection_charm) {
			list.add("§bDiverts meteors away from the player.");
			list.add("§bMeteors no longer destroy blocks.");
			list.add("§bHalves broadcaster damage");
		}
		if(this == Inserts.meteor_charm) {
			list.add("§bDisables meteorite spawning.");
			list.add("§bNegates broadcaster damage");
		}
		
		super.addInformation(stack, worldIn, list, flagIn);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add("§6  " + stack.getDisplayName());
	}

	@Override
	public void modDamage(LivingHurtEvent event, ItemStack armor) {
		
		if(event.getSource() == ModDamageSource.broadcast) {
			
			if(this == Inserts.protection_charm)
				event.setAmount(event.getAmount()*0.5F);
			if(this == Inserts.meteor_charm)
				event.setAmount(0);
		}
	}
}
