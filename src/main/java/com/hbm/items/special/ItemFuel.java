package com.hbm.items.special;

import com.hbm.items.ItemBakedBase;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

public class ItemFuel extends ItemBakedBase {

	private int burntime;
	
	public ItemFuel(String s, int burntime){
		super(s);
		this.setCreativeTab(MainRegistry.controlTab);
		this.burntime = burntime;
	}
	
	@Override
	public int getItemBurnTime(ItemStack itemStack) {
		return burntime;
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
		if(this == ModItems.dust)
		{
			if(MainRegistry.polaroidID == 11)
				list.add("Another one bites the dust!");
			else
				list.add("I hate dust!");
		}
		if(this == ModItems.powder_fire)
		{
			list.add("Used in multi purpose bombs:");
			list.add("Incendiary bombs are fun!");
		}
	}
}
