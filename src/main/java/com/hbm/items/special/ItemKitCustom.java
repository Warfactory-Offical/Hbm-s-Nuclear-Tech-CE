package com.hbm.items.special;

import com.hbm.items.IDynamicModels;
import com.hbm.items.ModItems;
import com.hbm.util.ItemStackUtil;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ModelBakeEvent;

public class ItemKitCustom extends ItemKitNBT implements IDynamicModels {
    public ItemKitCustom(String s) {
        super(s);
        IDynamicModels.INSTANCES.add(this);
    }

    public static ItemStack create(String name, String lore, int color1, int color2, ItemStack... contents) {
        ItemStack stack = new ItemStack(ModItems.kit_custom);

        stack.setTagCompound(new NBTTagCompound());

        setColor(stack, color1, 1);
        setColor(stack, color2, 2);

        if (lore != null) ItemStackUtil.addTooltipToStack(stack, lore.split("\\$"));
        stack.setStackDisplayName(TextFormatting.RESET + name);
        ItemStackUtil.addStacksToNBT(stack, contents);

        return stack;
    }

    public static void setColor(ItemStack stack, int color, int index) {

        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());

        stack.getTagCompound().setInteger("color" + index, color);
    }

    public static int getColor(ItemStack stack, int index) {

        if (!stack.hasTagCompound())
            return 0;

        return stack.getTagCompound().getInteger("color" + index);
    }


    @Override
    public IItemColor getItemColorHandler() {

        return (stack, tintIndex) -> {
            if (tintIndex == 1 || tintIndex == 2) {
                return ItemKitCustom.getColor(stack, tintIndex);
            }
            return 0xffffff;
        };
    }

    @Override
    public void bakeModel(ModelBakeEvent event) {

    }

    @Override
    public void registerModel() {

    }

    @Override
    public void registerSprite(TextureMap map) {

    }

}

