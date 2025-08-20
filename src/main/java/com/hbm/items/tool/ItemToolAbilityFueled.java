package com.hbm.items.tool;

import com.hbm.api.fluidmk2.IFillableItem;
import com.hbm.inventory.fluid.FluidType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class ItemToolAbilityFueled extends ItemToolAbility implements IFillableItem {

    protected int fillRate;
    protected int consumption;
    protected int maxFuel;
    protected HashSet<FluidType> acceptedFuels = new HashSet();

    public ItemToolAbilityFueled(String s, float damage, float attackSpeed, double movement, ToolMaterial material, EnumToolType type, int maxFuel, int consumption, int fillRate, FluidType... acceptedFuels) {
        super(damage, attackSpeed, movement, material, type, s);
        this.maxFuel = maxFuel;
        this.consumption = consumption;
        this.fillRate = fillRate;
        this.setMaxDamage(1);
        Collections.addAll(this.acceptedFuels, acceptedFuels);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {

        list.add(TextFormatting.GOLD + "Fuel: " + this.getFill(stack) + "/" + this.maxFuel + "mB");

        for(FluidType type : acceptedFuels) {
            list.add(TextFormatting.YELLOW + "- " + type.getLocalizedName());
        }

        super.addInformation(stack, worldIn, list, flagIn);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return getFill(stack) < maxFuel;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1 - (double) getFill(stack) / (double) maxFuel;
    }

    @Override
    public boolean canOperate(ItemStack stack) {
        return getFill(stack) >= this.consumption;
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
        this.setFill(stack, Math.max(this.getFill(stack) - damage * consumption, 0));
    }

    @Override
    public boolean isDamageable() {
        return true;
    }

    @Override
    public int getFill(ItemStack stack) {
        if(stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
            setFill(stack, maxFuel);
            return maxFuel;
        }

        return stack.getTagCompound().getInteger("fuel");
    }

    public void setFill(ItemStack stack, int fill) {
        if(stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
        }

        stack.getTagCompound().setInteger("fuel", fill);
    }

    @Override
    public boolean acceptsFluid(FluidType type, ItemStack stack) {
        return this.acceptedFuels.contains(type);
    }

    @Override
    public int tryFill(FluidType type, int amount, ItemStack stack) {

        if(!acceptsFluid(type, stack))
            return amount;

        int toFill = Math.min(amount, this.fillRate);
        toFill = Math.min(toFill, this.maxFuel - this.getFill(stack));
        this.setFill(stack, this.getFill(stack) + toFill);

        return amount - toFill;
    }

    @Override
    public boolean providesFluid(FluidType type, ItemStack stack) {
        return false;
    }

    @Override
    public int tryEmpty(FluidType type, int amount, ItemStack stack) {
        return amount;
    }

    public static ItemStack getEmptyTool(Item item) {
        ItemToolAbilityFueled tool = (ItemToolAbilityFueled) item;
        ItemStack stack = new ItemStack(item);
        tool.setFill(stack, 0);
        return stack;
    }

    @Override
    public FluidType getFirstFluidType(ItemStack stack) {
        return null;
    }
}