package com.hbm.items.machine;

import com.hbm.items.ItemBase;
import com.hbm.items.weapon.IMetaItemTesr;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemGear extends ItemBase implements IMetaItemTesr {
    //Norwood: this entire class for single item? what a shame
    public ItemGear(String s) {
        super(s);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
        IMetaItemTesr.INSTANCES.add(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            for (int i = 0; i < 2; i++) {
                items.add(new ItemStack(this, 1, i));
            }
        }
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return super.getTranslationKey() + (stack.getMetadata() == 1 ? "_steel" : "");
    }

    @Override
    public int getSubitemCount() {
        return 2;
    }

    @Override
    public String getResourceLocationAsString() {
        return getRegistryName().toString();
    }
}