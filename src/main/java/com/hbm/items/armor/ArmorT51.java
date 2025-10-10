package com.hbm.items.armor;

import com.hbm.render.model.ModelArmorT51;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ArmorT51 extends ArmorFSBPowered {

    public ArmorT51(ArmorMaterial material, int layer, EntityEquipmentSlot slot, String texture, long maxPower, long chargeRate, long consumption, long drain, String s) {
        super(material, layer, slot, texture, maxPower, chargeRate, consumption, drain, s);
    }

    @SideOnly(Side.CLIENT)
    ModelArmorT51[] models;

    @Override
    @SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped _default) {
        if(models == null) {
            models = new ModelArmorT51[4];

            for(int i = 0; i < 4; i++)
                models[i] = new ModelArmorT51(i);
        }

        return models[3-armorSlot.getIndex()];
    }

}

