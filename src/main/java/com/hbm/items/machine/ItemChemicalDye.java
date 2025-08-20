package com.hbm.items.machine;

import com.hbm.items.ItemEnumMulti;
import com.hbm.items.ModItems;
import com.hbm.util.EnumUtil;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemChemicalDye extends ItemEnumMulti {

    public ItemChemicalDye(String s) {
        super(s, EnumChemDye.class, true, false);
    }

    @SideOnly(Side.CLIENT)
    public static void registerColorHandlers(ColorHandlerEvent.Item evt) {
        ItemColors itemColors = evt.getItemColors();
        itemColors.registerItemColorHandler(new ColorHandler(), ModItems.chemical_dye);
        itemColors.registerItemColorHandler(new ColorHandler(), ModItems.crayon);
    }

    private static class ColorHandler implements IItemColor {
        @Override
        public int colorMultiplier(ItemStack stack, int tintIndex) {
            if(tintIndex == 1) {
                EnumChemDye dye = EnumUtil.grabEnumSafely(EnumChemDye.class, stack.getItemDamage());
                return dye.color;
            }

            return 0xffffff;
        }

    }

    public static enum EnumChemDye {
        BLACK(1973019, "Black"),
        RED(11743532, "Red"),
        GREEN(3887386, "Green"),
        BROWN(5320730, "Brown"),
        BLUE(2437522, "Blue"),
        PURPLE(8073150, "Purple"),
        CYAN(2651799, "Cyan"),
        SILVER(11250603, "LightGray"),
        GRAY(4408131, "Gray"),
        PINK(14188952, "Pink"),
        LIME(4312372, "Lime"),
        YELLOW(14602026, "Yellow"),
        LIGHTBLUE(6719955, "LightBlue"),
        MAGENTA(12801229, "Magenta"),
        ORANGE(15435844, "Orange"),
        WHITE(15790320, "White");

        public int color;
        public String dictName;

        private EnumChemDye(int color, String name) {
            this.color = color;
            this.dictName = name;
        }
    }
}
