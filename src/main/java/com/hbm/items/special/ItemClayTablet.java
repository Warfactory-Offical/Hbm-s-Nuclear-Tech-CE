package com.hbm.items.special;

import com.hbm.inventory.gui.GUIScreenClayTablet;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemClayTablet extends Item implements IGUIProvider {
    public ItemClayTablet(String s) {
        this.setRegistryName(s);
        this.setTranslationKey(s);
        this.setMaxDamage(0);
        this.setMaxStackSize(1);

        ModItems.ALL_ITEMS.add(this);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (hand == EnumHand.OFF_HAND) return new ActionResult<>(EnumActionResult.FAIL, player.getHeldItemOffhand());
        ItemStack stack = player.getHeldItemMainhand();
        if(!world.isRemote && !stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setLong("tabletSeed", player.getRNG().nextLong());
        }
        if(world.isRemote) player.openGui(MainRegistry.instance, 0, world, 0, 0, 0);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Override
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIScreenClayTablet();
    }
}
