package com.hbm.util;

import com.hbm.interfaces.Untested;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

import java.util.ArrayList;
import java.util.List;

public class Compat {
	private static final boolean MOD_EIDS = Loader.isModLoaded("jeid") || Loader.isModLoaded("neid");

	public static boolean isIDExtensionModLoaded() {
		return MOD_EIDS;
	}

	public static Item tryLoadItem(String domain, String name) {
		return Item.REGISTRY.getObject(new ResourceLocation(domain, name));
	}
	
	public static Block tryLoadBlock(String domain, String name){
		return Block.REGISTRY.getObject(new ResourceLocation(domain, name));
	}

	public static TileEntity getTileStandard(World world, int x, int y, int z) {
		if(!world.getChunkProvider().isChunkGeneratedAt(x >> 4, z >> 4)) return null;
		return world.getTileEntity(new BlockPos(x, y, z));
	}

	@Untested
	//FIXME: didnt do much with it yet
	public static List<ItemStack> scrapeItemFromME(final ItemStack meDrive) {
		final List<ItemStack> stacks = new ArrayList();

		try {
			if(meDrive != null && meDrive.hasTagCompound()) {
				final NBTTagCompound nbt = meDrive.getTagCompound();
				final int types = nbt.getShort("it"); //ITEM_TYPE_TAG

				for(int i = 0; i < types; i++) {
					final NBTBase stackTag = nbt.getTag("#" + i);

					if(stackTag instanceof NBTTagCompound compound) {
						final ItemStack stack = ItemStackUtil.itemStackFrom(compound);

						final int count = nbt.getInteger("@" + i);
						stack.setCount(count);
						stacks.add(stack);
					}
				}
			}
		} catch(final Exception ex) { }

		return stacks;
	}

    public static final class ModIds {
        public static final String GROOVY_SCRIPT = "groovyscript";
        public static final String OPEN_COMPUTERS = "opencomputers";
        public static final String CTM = "CTM";
    }
}
