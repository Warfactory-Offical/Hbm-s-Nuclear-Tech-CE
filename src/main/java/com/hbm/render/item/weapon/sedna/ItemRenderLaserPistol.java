package com.hbm.render.item.weapon.sedna;

import com.hbm.items.ModItems.Armory;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class ItemRenderLaserPistol extends ItemRenderWeaponBase {

    public ResourceLocation texture;

    public ItemRenderLaserPistol(ResourceLocation texture) {
        this.texture = texture;
    }

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return ItemGunBaseNT.getIsAiming(stack) ? 2.5F : -0.5F; }

    @Override
    public float getViewFOV(ItemStack stack, float fov) {
        float aimingProgress = ItemGunBaseNT.prevAimingProgress + (ItemGunBaseNT.aimingProgress - ItemGunBaseNT.prevAimingProgress) * interp;
        return  fov * (1 - aimingProgress * 0.33F);
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        GlStateManager.translate(0F, 0F, 0.875F);

        float offset = 0.8F;
        standardAimingTransform(stack,
                -1.75F * offset, -2F * offset, 2.75F * offset,
                0, -10 / 8D, 1.25);
    }

    @Override
    public void renderFirstPerson(ItemStack stack) {

        ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);
        double scale = 0.375D;
        GlStateManager.scale((float) scale, (float) scale, (float) scale);

        double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");
        double[] recoil = HbmAnimationsSedna.getRelevantTransformation("RECOIL");
        double[] latch = HbmAnimationsSedna.getRelevantTransformation("LATCH");
        double[] lift = HbmAnimationsSedna.getRelevantTransformation("LIFT");
        double[] jolt = HbmAnimationsSedna.getRelevantTransformation("JOLT");
        double[] battery = HbmAnimationsSedna.getRelevantTransformation("BATTERY");
        double[] swirl = HbmAnimationsSedna.getRelevantTransformation("SWIRL");

        GlStateManager.translate(0F, -1F, -6F);
        GlStateManager.rotate((float) equip[0], 1F, 0F, 0F);
        GlStateManager.translate(0F, 1F, 6F);

        GlStateManager.translate(0F, 2F, -2F);
        GlStateManager.rotate((float) lift[0], 1F, 0F, 0F);
        GlStateManager.translate(0F, -2F, 2F);

        GlStateManager.translate(0F, -1F, -1F);
        GlStateManager.rotate((float) swirl[0], 1F, 0F, 0F);
        GlStateManager.translate(0F, 1F, 1F);

        GlStateManager.translate(0F, 0F, (float) recoil[2]);
        GlStateManager.translate((float) jolt[0], (float) jolt[1], (float) jolt[2]);

        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        ResourceManager.laser_pistol.renderPart("Gun");
        if(hasCapacitors(stack)) ResourceManager.laser_pistol.renderPart("Capacitors");
        if(hasTape(stack)) ResourceManager.laser_pistol.renderPart("Tape");

        GlStateManager.pushMatrix();
        GlStateManager.translate(1.125F, 0F, -1.9125F);
        GlStateManager.rotate((float) latch[1], 0F, 1F, 0F);
        GlStateManager.translate(-1.125F, 0F, 1.9125F);
        ResourceManager.laser_pistol.renderPart("Latch");
        GlStateManager.translate((float) battery[0], (float) battery[1], (float) battery[2]);
        ResourceManager.laser_pistol.renderPart("Battery");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0F, 2F, 4.75F);
        GlStateManager.rotate(90F, 0F, 1F, 0F);
        renderLaserFlash(gun.lastShot[0], 150, 1.5D, hasEmerald(stack) ? 0x008000 : 0xff0000);
        GlStateManager.translate(0F, 0F, -0.25F);
        renderLaserFlash(gun.lastShot[0], 150, 0.75D, hasEmerald(stack) ? 0x80ff00 : 0xff8000);
        GlStateManager.popMatrix();

        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        double scale = 1.25D;
        GlStateManager.scale((float) scale, (float) scale, (float) scale);
        GlStateManager.translate(0F, -0.5F, 1F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        double scale = 1.75D;
        GlStateManager.scale((float) scale, (float) scale, (float) scale);
        GlStateManager.rotate(25F, 1F, 0F, 0F);
        GlStateManager.rotate(45F, 0F, 1F, 0F);
        GlStateManager.translate(0F, -0.5F, 0F);
    }

    @Override
    public void setupModTable(ItemStack stack) {
        double scale = -10D;
        GlStateManager.scale((float) scale, (float) scale, (float) scale);
        GlStateManager.rotate(90F, 0F, 1F, 0F);
        GlStateManager.translate(0F, -0.5F, 0F);
    }

    @Override
    public void renderOther(ItemStack stack, Object type) {
        GlStateManager.enableLighting();

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);
        ResourceManager.laser_pistol.renderPart("Gun");
        ResourceManager.laser_pistol.renderPart("Latch");
        if(hasCapacitors(stack)) ResourceManager.laser_pistol.renderPart("Capacitors");
        if(hasTape(stack)) ResourceManager.laser_pistol.renderPart("Tape");
        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    public boolean hasCapacitors(ItemStack stack) {
        return stack.getItem() == Armory.gun_laser_pistol_pew_pew;
    }

    public boolean hasTape(ItemStack stack) {
        return stack.getItem() == Armory.gun_laser_pistol_pew_pew;
    }

    public boolean hasEmerald(ItemStack stack) {
        return stack.getItem() == Armory.gun_laser_pistol_morning_glory;
    }
}
