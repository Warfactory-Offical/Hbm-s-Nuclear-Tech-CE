package com.hbm.creativetabs;

import com.hbm.items.ModItems.Foods;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ConsumableTab extends CreativeTabs {

	public ConsumableTab(int index, String label) {
		super(index, label);
	}

	@Override
    @SideOnly(Side.CLIENT)
	public ItemStack createIcon() {
		if(Foods.bottle_nuka != null){
			return new ItemStack(Foods.bottle_nuka);
		}
		return new ItemStack(Items.IRON_PICKAXE);
	}

}
