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

public class ItemRenderMinigun extends ItemRenderWeaponBase {

	protected ResourceLocation texture;

	public ItemRenderMinigun(ResourceLocation texture) {
		this.texture = texture;
	}

	@Override
	protected float getTurnMagnitude(ItemStack stack) {
		return ItemGunBaseNT.getIsAiming(stack) ? 2.5F : -0.5F;
	}

	@Override
	public float getViewFOV(ItemStack stack, float fov) {
		float aimingProgress = ItemGunBaseNT.prevAimingProgress +
				(ItemGunBaseNT.aimingProgress - ItemGunBaseNT.prevAimingProgress) * interp;
		return fov * (1 - aimingProgress * 0.33F);
	}

	@Override
	public void setupFirstPerson(ItemStack stack) {
		GlStateManager.translate(0, 0, 0.875);

		float offset = 0.8F;
		standardAimingTransform(stack,
				-1.75F * offset, -1.75F * offset, 3.5F * offset,
				0, -6.25 / 8D, 1);
	}

	@Override
	public void renderFirstPerson(ItemStack stack) {

		ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		double scale = 0.375D;
		GlStateManager.scale(scale, scale, scale);

		double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");
		double[] recoil = HbmAnimationsSedna.getRelevantTransformation("RECOIL");
		double[] rotate = HbmAnimationsSedna.getRelevantTransformation("ROTATE");

		GlStateManager.translate(0, 3, -6);
		GlStateManager.rotate((float) equip[0], 1, 0, 0);
		GlStateManager.translate(0, -3, 6);

		GlStateManager.translate(0, 0, recoil[2]);

		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		ResourceManager.minigun.renderPart("Gun");
		ResourceManager.minigun.renderPart("Grip");

		GlStateManager.pushMatrix();
		GlStateManager.rotate((float) rotate[2], 0, 0, 1);
		ResourceManager.minigun.renderPart("Barrels");
		GlStateManager.popMatrix();

		double smokeScale = 0.5;

		GlStateManager.pushMatrix();
		GlStateManager.translate(-2, 1.25, -3.5);
		GlStateManager.rotate(45, 0, 1, 0);
		GlStateManager.scale(smokeScale, smokeScale, smokeScale);
		this.renderSmokeNodes(gun.getConfig(stack, 0).smokeNodes, 0.5D);
		GlStateManager.popMatrix();

		GlStateManager.shadeModel(GL11.GL_FLAT);

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, 12);
		GlStateManager.rotate(90, 0, 1, 0);
		if(stack.getItem() == Armory.gun_minigun_lacunae) {
			renderLaserFlash(gun.lastShot[0], 50, 1D, 0xff00ff);
			GlStateManager.translate(0, 0, -0.25);
			renderLaserFlash(gun.lastShot[0], 50, 0.5D, 0xff0080);
		} else {
			GlStateManager.rotate(gun.shotRand * 90, 1, 0, 0);
			GlStateManager.scale(1.5, 1.5, 1.5);
			this.renderMuzzleFlash(gun.lastShot[0], 75, 5);
		}
		GlStateManager.popMatrix();
	}

	@Override
	public void setupThirdPerson(ItemStack stack) {
		super.setupThirdPerson(stack);
		double scale = 1.75D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.translate(1, -3.5, 8);
	}

	@Override
	public void setupInv(ItemStack stack) {
		super.setupInv(stack);
		double scale = 0.875D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(25, 1, 0, 0);
		GlStateManager.rotate(45, 0, 1, 0);
		GlStateManager.translate(-0.25, 0.5, 0);
	}

	@Override
	public void setupModTable(ItemStack stack) {
		double scale = -6.25D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(90, 0, 1, 0);
	}

	@Override
	public void renderOther(ItemStack stack, Object type) {
		GlStateManager.enableLighting();

		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		ResourceManager.minigun.renderPart("Gun");
		ResourceManager.minigun.renderPart("Grip");
		ResourceManager.minigun.renderPart("Barrels");
		GlStateManager.shadeModel(GL11.GL_FLAT);
	}
}

