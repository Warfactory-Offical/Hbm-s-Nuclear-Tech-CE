package com.hbm.util;

import appeng.api.AEApi;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    @Optional.Method(modid = ModIds.AE2)
    public static IItemList<IAEItemStack> scrapeItemFromME(final ItemStack cell) {
        final IStorageChannel<IAEItemStack> ch = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
        final ICellInventoryHandler<IAEItemStack> handler = AEApi.instance().registries().cell().getCellInventory(cell, null, ch);
        if (handler == null) return null;
        final ICellInventory<IAEItemStack> inv = handler.getCellInv();
        final IItemList<IAEItemStack> list = ch.createList();
        inv.getAvailableItems(list);
        return list;
    }

    public static final class ModIds {
        public static final String GROOVY_SCRIPT = "groovyscript";
        public static final String OPEN_COMPUTERS = "opencomputers";
        public static final String CTM = "ctm";
        public static final String AE2 = "appliedenergistics2";
        public static final String MODERN_SPLASH = "modernsplash";
    }
}
