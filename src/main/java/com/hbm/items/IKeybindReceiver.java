package com.hbm.items;

import com.hbm.handler.HbmKeybinds;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IKeybindReceiver {
    public boolean canHandleKeybind(EntityPlayer player, ItemStack stack, HbmKeybinds.EnumKeybind keybind);
    public void handleKeybind(EntityPlayer player, ItemStack stack, HbmKeybinds.EnumKeybind keybind, boolean state);
    public default void handleKeybindClient(EntityPlayer player, ItemStack stack, HbmKeybinds.EnumKeybind keybind, boolean state) { }
}
