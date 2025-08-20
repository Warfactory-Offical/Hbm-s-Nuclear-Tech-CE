package com.hbm.items.food;

import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemChemicalDye;
import com.hbm.util.EnumUtil;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Locale;

public class ItemCrayon extends ItemFood {

    public ItemCrayon(String s) {
        super(3, false);
        this.setHasSubtypes(true);
        this.setAlwaysEdible();
        this.setTranslationKey(s);
        this.setRegistryName(s);

        ModItems.ALL_ITEMS.add(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == CreativeTabs.SEARCH || tab == this.getCreativeTab()) {
            for (int i = 0; i < ItemChemicalDye.EnumChemDye.values().length; i++) {
                items.add(new ItemStack(this, 1, i));
            }
        }
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        Enum num = EnumUtil.grabEnumSafely(ItemChemicalDye.EnumChemDye.class, stack.getItemDamage());
        return super.getTranslationKey() + "." + num.name().toLowerCase(Locale.US);
    }
}
