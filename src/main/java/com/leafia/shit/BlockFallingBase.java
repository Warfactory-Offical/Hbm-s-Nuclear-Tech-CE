package com.leafia.shit;

import com.hbm.blocks.ModBlocks;
import com.hbm.main.MainRegistry;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

public class BlockFallingBase extends BlockFalling {
	
	public BlockFallingBase(Material m, String s, SoundType type){
		super(m);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setCreativeTab(MainRegistry.controlTab);
		this.setHarvestLevel("shovel", 0);
		this.setSoundType(type);
		ModBlocks.ALL_BLOCKS.add(this);
	}
	
	@Override
	public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
		if(this == ModBlocks.gravel_diamond){
			tooltip.add("There is some kind of joke here,");
			tooltip.add("but I can't quite tell what it is.");
			tooltip.add("");
			tooltip.add("Update, 2020-07-04:");
			tooltip.add("We deny any implications of a joke on");
			tooltip.add("the basis that it was so severely unfunny");
			tooltip.add("that people started stabbing their eyes out.");
			tooltip.add("");
			tooltip.add("Update, 2020-17-04:");
			tooltip.add("As it turns out, \"Diamond Gravel\" was");
			tooltip.add("never really a thing, rendering what might");
			tooltip.add("have been a joke as totally nonsensical.");
			tooltip.add("We apologize for getting your hopes up with");
			tooltip.add("this non-joke that hasn't been made.");
			tooltip.add("");
			tooltip.add("i added an item for a joke that isn't even here, what am i, stupid? can't even tell the difference between gravel and a gavel, how did i not forget how to breathe yet?");
		}

		if(this == ModBlocks.sand_boron){
			tooltip.add("Used to reduce reactivity and increase cooldown in destroyed RBMK cores.");
		}
	}
	
}
