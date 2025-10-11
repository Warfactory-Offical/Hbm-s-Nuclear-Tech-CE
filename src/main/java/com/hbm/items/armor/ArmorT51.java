package com.hbm.items.armor;

import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.model.ModelArmorT51;
import com.hbm.render.tileentity.IItemRendererProvider;
import com.hbm.render.util.ViewModelPositonDebugger;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ArmorT51 extends ArmorFSBPowered implements IItemRendererProvider {

    @SideOnly(Side.CLIENT)
    ModelArmorT51[] models;

    public ArmorT51(ArmorMaterial material, int layer, EntityEquipmentSlot slot, String texture, long maxPower, long chargeRate, long consumption, long drain, String s) {
        super(material, layer, slot, texture, maxPower, chargeRate, consumption, drain, s);
    }

    protected ViewModelPositonDebugger offsets = new ViewModelPositonDebugger()
            .get(ItemCameraTransforms.TransformType.GUI)
            .setScale(5.60d).setPosition(-3.75, -1.00, 1.5).setRotation(325, 90, -135)
            .getHelper()
            .get(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND)
            .setPosition(-1.00, -31.30, -4.95).setRotation(-23, -139, 85)
            .getHelper()
            .get(ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND)
            .setPosition(-0.5, 3, -2.75).setRotation(610, -115, -100)
            .getHelper()
            .get(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND)
            .setScale(3.65d).setPosition(-6.5, -0.5, 0.25).setRotation(540, -125, 55)
            .getHelper()
            .get(ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND)
            .setScale(0.25d).setPosition(-8, -5.50, -1.00).setRotation(0, 330, 180)
            .getHelper()
            .get(ItemCameraTransforms.TransformType.GROUND)
            .setPosition(6.25, 0, 0.25).setRotation(455, -60, 55).setScale(5.40d)
            .getHelper();
    ViewModelPositonDebugger.offset corrections = new ViewModelPositonDebugger.offset(offsets)
            .setRotation(0, 0, 0);

    @Override
    @SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped _default) {
        if (models == null) {
            models = new ModelArmorT51[4];

            for (int i = 0; i < 4; i++)
                models[i] = new ModelArmorT51(i);
        }

        return models[3 - armorSlot.getIndex()];
    }

    @Override
    public Item getItemForRenderer() {
        return this;
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            @Override
            public void renderInventory() {
                setupRenderInv();
            }

            @Override
            public void renderNonInv() {
                setupRenderNonInv();
            }

            @Override
            public void renderCommon() {
                offsets.apply(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND);
                renderStandard(ResourceManager.armor_t51, armorType, ResourceManager.t51_helmet, ResourceManager.t51_chest, ResourceManager.t51_arm, ResourceManager.t51_leg, "Helmet", "Chest", "LeftArm", "RightArm", "LeftLeg", "RightLeg", "LeftBoot", "RightBoot");
            }
        };
    }
}

