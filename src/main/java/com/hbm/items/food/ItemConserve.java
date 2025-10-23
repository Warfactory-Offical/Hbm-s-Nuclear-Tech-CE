package com.hbm.items.food;

import com.hbm.entity.effect.EntityVortex;
import com.hbm.items.ItemEnumMultiFood;
import com.hbm.items.ModItems.Foods;
import com.hbm.util.I18nUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class ItemConserve extends ItemEnumMultiFood<ItemConserve.EnumFoodType> {

    public ItemConserve() {
        super("canned_conserve", EnumFoodType.class, true, true);
    }

    @Override
    protected String getSeparator() {
        return "_";
    }

    @Override
    protected String getPrefix() {
        return "canned";
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.EAT;
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
        super.onFoodEaten(stack, worldIn, player);
        player.inventory.addItemStackToInventory(new ItemStack(Foods.can_key));
        EnumFoodType type = getVariant(stack);
        if (type == null) return;
        if (type == EnumFoodType.BHOLE && !worldIn.isRemote) {
            EntityVortex vortex = new EntityVortex(worldIn, 0.5F);
            vortex.posX = player.posX;
            vortex.posY = player.posY;
            vortex.posZ = player.posZ;
            worldIn.spawnEntity(vortex);
        } else if (type == EnumFoodType.RECURSION && worldIn.rand.nextInt(10) > 0) {
            player.inventory.addItemStackToInventory(stackFromEnum(EnumFoodType.RECURSION));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        String base = this.getTranslationKey(stack) + ".desc";
        String resolved = I18nUtil.resolveKey(base);
        if (!base.equals(resolved)) {
            Collections.addAll(tooltip, resolved.split("\\$"));
        }
    }

    public enum EnumFoodType implements ItemEnumMultiFood.FoodSpec {
        BEEF(8, 0.75F),
        TUNA(4, 0.75F),
        MYSTERY(6, 0.5F),
        PASHTET(4, 0.5F),
        CHEESE(3, 1F),
        JIZZ(15, 5F), // :3
        MILK(5, 0.25F),
        ASS(6, 0.75F), // :3
        PIZZA(8, 075F), // mlbv: 1.7 has it at 075F, idk why, typo maybe?
        TUBE(2, 0.25F),
        TOMATO(4, 0.5F),
        ASBESTOS(7, 1F),
        BHOLE(10, 1F),
        HOTDOGS(5, 0.75F),
        LEFTOVERS(1, 0.1F),
        YOGURT(3, 0.5F),
        STEW(5, 0.5F),
        CHINESE(6, 0.1F),
        OIL(3, 1F),
        FIST(6, 0.75F),
        SPAM(8, 1F),
        FRIED(10, 0.75F),
        NAPALM(6, 1F),
        DIESEL(6, 1F),
        KEROSENE(6, 1F),
        RECURSION(1, 1F),
        BARK(2, 1F);

        final int food;
        final float sat;

        EnumFoodType(int food, float sat) {
            this.food = food;
            this.sat = sat;
        }

        @Override
        public int foodLevel() {
            return food;
        }

        @Override
        public float saturation() {
            return sat;
        }
    }
}
