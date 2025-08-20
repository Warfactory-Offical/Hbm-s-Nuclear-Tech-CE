package com.hbm.items.armor;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.ArmorModHandler;
import com.hbm.lib.HBMSoundHandler;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemModSensor extends ItemArmorMod {

    public ItemModSensor(String s) {
        super(ArmorModHandler.extra, true, true, true, true, s);
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flagIn) {

        list.add(TextFormatting.YELLOW + "Beeps near hazardous gasses");
        list.add(TextFormatting.YELLOW + "Works in the inventory or when applied to armor");
        list.add("");
        super.addInformation(stack, world, list, flagIn);
    }

    @Override
    public void addDesc(List list, ItemStack stack, ItemStack armor) {
        list.add(TextFormatting.YELLOW + "  " + stack.getDisplayName() + " (Detects gasses)");
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean equipped) {
        if(entity instanceof EntityLivingBase) {
            modUpdate((EntityLivingBase) entity, null);
        }
    }

    @Override
    public void modUpdate(EntityLivingBase entity, ItemStack armor) {

        if(entity.world.isRemote || entity.world.getTotalWorldTime() % 20 != 0) return;

        int x = (int) Math.floor(entity.posX);
        int y = (int) Math.floor(entity.posY + entity.getEyeHeight() - entity.getYOffset());
        int z = (int) Math.floor(entity.posZ);

        boolean poison = false;
        boolean explosive = false;

        for(int i = -3; i <= 3; i++) {
            for(int j = -1; j <= 1; j++) {
                for(int k = -3; k <= 3; k++) {
                    Block b = entity.world.getBlockState(new BlockPos(x + i * 2, y + j * 2, z + k * 2)).getBlock();
                    if(b == ModBlocks.gas_asbestos || b == ModBlocks.gas_coal || b == ModBlocks.gas_radon || b == ModBlocks.gas_monoxide || b == ModBlocks.gas_radon_dense || b == ModBlocks.chlorine_gas) {
                        poison = true;
                    }
                    if(b == ModBlocks.gas_flammable || b == ModBlocks.gas_explosive) {
                        explosive = true;
                    }
                }
            }
        }

        if(explosive) {
            entity.world.playSound(entity.posX, entity.posY, entity.posZ, HBMSoundHandler.follyAquired, SoundCategory.PLAYERS, 0.5F, 1.0F, false);
        } else if(poison) {
            entity.world.playSound(entity.posX, entity.posY, entity.posZ, HBMSoundHandler.techBoop, SoundCategory.PLAYERS,  2F, 1.5F, false);
        }
    }
}
