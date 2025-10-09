package com.hbm.render.model;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import static com.hbm.render.NTMRenderHelper.bindTexture;

public class ModelArmorWings extends ModelArmorBase {

	ModelRendererObj wingLB;
	ModelRendererObj wingLT;
	ModelRendererObj wingRB;
	ModelRendererObj wingRT;
	
	public ModelArmorWings(int type) {
		super(type);

		wingLB = new ModelRendererObj(ResourceManager.armor_wings, "LeftBase");
		wingLT = new ModelRendererObj(ResourceManager.armor_wings, "LeftTip");
		wingRB = new ModelRendererObj(ResourceManager.armor_wings, "RightBase");
		wingRT = new ModelRendererObj(ResourceManager.armor_wings, "RightTip");

		//i should really stop doing that
		head = new ModelRendererObj(ResourceManager.anvil);
		body = new ModelRendererObj(ResourceManager.anvil);
		leftArm = new ModelRendererObj(ResourceManager.anvil).setRotationPoint(-5.0F, 2.0F, 0.0F);
		rightArm = new ModelRendererObj(ResourceManager.anvil).setRotationPoint(5.0F, 2.0F, 0.0F);
		leftLeg = new ModelRendererObj(ResourceManager.anvil).setRotationPoint(1.9F, 12.0F, 0.0F);
		rightLeg = new ModelRendererObj(ResourceManager.anvil).setRotationPoint(-1.9F, 12.0F, 0.0F);
		leftFoot = new ModelRendererObj(ResourceManager.anvil).setRotationPoint(1.9F, 12.0F, 0.0F);
		rightFoot = new ModelRendererObj(ResourceManager.anvil).setRotationPoint(-1.9F, 12.0F, 0.0F);
	}

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		
		setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
		
		GlStateManager.pushMatrix();

        float yaw = entity instanceof EntityLivingBase living ? living.renderYawOffset : entity.rotationYaw;
        GlStateManager.rotate(yaw - 180.0, 0.0F, 1.0F, 0.0F);
		bindTexture(this.getTexture());
		
		double px = 0.0625D;

		double rot = Math.sin((entity.ticksExisted) * 0.2D) * 20;
		double rot2 = Math.sin((entity.ticksExisted) * 0.2D - Math.PI * 0.5) * 50 + 30;
		
		int pivotSideOffset = 1;
		int pivotFrontOffset = 5;
		int pivotZOffset = 3;
		int tipSideOffset = 16;
		int tipZOffset = 2;
		double inwardAngle = 10D;
		
		GlStateManager.pushMatrix();

		GlStateManager.translate(body.offsetX * (float) px, body.offsetY * (float) px, body.offsetZ * (float) px);
		GlStateManager.translate(body.rotationPointX * (float) px, body.rotationPointY * (float) px, body.rotationPointZ * (float) px);

		if(body.rotateAngleZ != 0.0F) {
			GlStateManager.rotate(body.rotateAngleZ * (180F / (float) Math.PI), 0.0F, 0.0F, 1.0F);
		}

		if(body.rotateAngleY != 0.0F) {
			GlStateManager.rotate(body.rotateAngleY * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
		}

		if(body.rotateAngleX != 0.0F) {
			GlStateManager.rotate(body.rotateAngleX * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
		}
		
		if(this.type != 1 && entity.onGround) {
			rot = 20;
			rot2 = 160;
		}
		
		if(this.type == 1) {

            if(entity.onGround) {
				rot = 30;
				rot2 = -30;
			} else if(entity.motionY < -0.1) {
				rot = 0;
				rot2 = 10;
			} else {
				rot = 30;
				rot2 = 20;
			}
		}
		
		GlStateManager.translate(0, -2 * px, 0);
		
		GlStateManager.enableCull();
		GlStateManager.pushMatrix();
			
			GL11.glRotated(-inwardAngle, 0, 1, 0);
			
			GlStateManager.translate(pivotSideOffset * px, pivotFrontOffset * px, pivotZOffset * px);
			GL11.glRotated(rot * 0.5, 0, 1, 0);
			GL11.glRotated(rot + 5, 0, 0, 1);
			GL11.glRotated(45, 1, 0, 0);
			GlStateManager.translate(-pivotSideOffset * px, -pivotFrontOffset * px, -pivotZOffset * px);
			
			GlStateManager.translate(pivotSideOffset * px, pivotFrontOffset * px, pivotZOffset * px);
			GL11.glRotated(rot, 0, 0, 1);
			GlStateManager.translate(-pivotSideOffset * px, -pivotFrontOffset * px, -pivotZOffset * px);
			wingLB.render(scale);
			
			GlStateManager.translate(tipSideOffset * px, pivotFrontOffset * px, tipZOffset * px);
			GL11.glRotated(rot2, 0, 1, 0);
			if(doesRotateZ())
				GL11.glRotated(rot2 * 0.25 + 5, 0, 0, 1);
			GlStateManager.translate(-tipSideOffset * px, -pivotFrontOffset * px, -tipZOffset * px);
			wingLT.render(scale);
			
		GlStateManager.popMatrix();
		
		GlStateManager.pushMatrix();
			
			GL11.glRotated(inwardAngle, 0, 1, 0);
			
			GlStateManager.translate(-pivotSideOffset * px, pivotFrontOffset * px, pivotZOffset * px);
			GL11.glRotated(-rot * 0.5, 0, 1, 0);
			GL11.glRotated(-rot - 5, 0, 0, 1);
			GL11.glRotated(45, 1, 0, 0);
			GlStateManager.translate(pivotSideOffset * px, -pivotFrontOffset * px, -pivotZOffset * px);
			
			GlStateManager.translate(-pivotSideOffset * px, pivotFrontOffset * px, pivotZOffset * px);
			GL11.glRotated(-rot, 0, 0, 1);
			GlStateManager.translate(pivotSideOffset * px, -pivotFrontOffset * px, -pivotZOffset * px);
			wingRB.render(scale);
			
			GlStateManager.translate(-tipSideOffset * px, pivotFrontOffset * px, tipZOffset * px);
			GL11.glRotated(-rot2, 0, 1, 0);
			if(doesRotateZ())
				GL11.glRotated(-rot2 * 0.25 - 5, 0, 0, 1);
			GlStateManager.translate(tipSideOffset * px, -pivotFrontOffset * px, -tipZOffset * px);
			wingRT.render(scale);
			
		GlStateManager.popMatrix();
		GlStateManager.disableCull();
			
		GlStateManager.popMatrix();
		
		GlStateManager.popMatrix();
	}

	@Override
	protected void renderArmor(Entity entity, float scale) {

	}

	protected boolean doesRotateZ() {
		return true;
	}
	
	protected ResourceLocation getTexture() {
		
		if(this.type == 2)
			return ResourceManager.wings_bob;
		
		return ResourceManager.wings_murk;
	}
}