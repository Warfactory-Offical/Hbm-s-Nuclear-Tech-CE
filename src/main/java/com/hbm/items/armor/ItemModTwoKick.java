package com.hbm.items.armor;

import com.hbm.handler.ArmorModHandler;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemModTwoKick extends ItemArmorMod {

    public ItemModTwoKick(String s) {
        super(ArmorModHandler.servos, false, true, false, false, s);
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flagIn) {

        list.add(TextFormatting.ITALIC + "\"I've had worse\"");
        list.add(TextFormatting.YELLOW + "Punches fire 12 gauge shells");
        list.add("");
        super.addInformation(stack, world, list, flagIn);
    }

    @Override
    public void addDesc(List list, ItemStack stack, ItemStack armor) {
        list.add(TextFormatting.YELLOW + "  " + stack.getDisplayName() + " (Shotgun punches)");
    }
}
