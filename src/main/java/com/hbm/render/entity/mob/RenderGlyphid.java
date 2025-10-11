package com.hbm.render.entity.mob;

import com.hbm.entity.mob.glyphid.*;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.registry.IRenderFactory;

@AutoRegister(entity = EntityGlyphid.class, factory = "FACTORY")
@AutoRegister(entity = EntityGlyphidBehemoth.class, factory = "FACTORY")
@AutoRegister(entity = EntityGlyphidBlaster.class, factory = "FACTORY")
@AutoRegister(entity = EntityGlyphidBombardier.class, factory = "FACTORY")
@AutoRegister(entity = EntityGlyphidBrawler.class, factory = "FACTORY")
@AutoRegister(entity = EntityGlyphidBrenda.class, factory = "FACTORY")
@AutoRegister(entity = EntityGlyphidDigger.class, factory = "FACTORY")
@AutoRegister(entity = EntityGlyphidScout.class, factory = "FACTORY")
public class RenderGlyphid extends RenderLiving<EntityGlyphid> {
    public static final ResourceLocation glyphid_infested_tex = new ResourceLocation(RefStrings.MODID, "textures/entity/glyphid_infestation.png");

    public static final IRenderFactory<EntityGlyphid> FACTORY = RenderGlyphid::new;

    public RenderGlyphid(RenderManager renderManager) {
        super(renderManager, new ModelGlyphid(), 1.0F);
        this.shadowOpaque = 0.0F;
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityGlyphid glyphid) {
        return glyphid.getSkin();
    }

    @Override
    public boolean shouldRender(EntityGlyphid entity, ICamera camera, double camX, double camY, double camZ) {
        if(entity.getDataManager().get(EntityGlyphid.SUBTYPE) == EntityGlyphid.TYPE_INFECTED) {
            this.bindTexture(glyphid_infested_tex);
            GlStateManager.enableBlend();
            GlStateManager.disableAlpha();
        }
        return true;
    }

    public static class ModelGlyphid extends ModelBase {

        double bite = 0;

        @Override
        public void setLivingAnimations(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float interp) {
            bite = entity.getSwingProgress(interp);
        }

        @Override
        public void render(Entity entity, float limbSwing, float limbSwingAmount, float rotationYaw, float rotationHeadYaw, float rotationPitch, float scale) {
            GlStateManager.pushMatrix();

            GlStateManager.rotate(180, 1, 0, 0);
            GlStateManager.translate(0, -1.5F, 0);
            GlStateManager.enableLighting();
            GlStateManager.disableCull();

            this.renderModel(entity, limbSwing);

            GlStateManager.popMatrix();
        }

        public void renderModel(Entity entity, float limbSwing) {

            GlStateManager.pushMatrix();

            double s = ((EntityGlyphid) entity).getScale();
            GlStateManager.scale(s, s, s);

            EntityLivingBase living = (EntityLivingBase) entity;
            byte armor = living.getDataManager().get(EntityGlyphid.ARMOR);

            double cy0 = Math.sin((double) limbSwing % (Math.PI * 2));
            double cy1 = Math.sin((double) limbSwing % (Math.PI * 2) - Math.PI * 0.5);
            double cy2 = Math.sin((double) limbSwing % (Math.PI * 2) - Math.PI);
            double cy3 = Math.sin((double) limbSwing % (Math.PI * 2) - Math.PI * 0.75);

            double bite = MathHelper.clamp(Math.sin(this.bite * Math.PI * 2 - Math.PI * 0.5), 0, 1) * 20;
            double headTilt = Math.sin(this.bite * Math.PI) * 30;

            ResourceManager.glyphid.renderPart("Body");
            if((armor & 1) > 0) ResourceManager.glyphid.renderPart("ArmorFront");
            if((armor & (1 << 1)) > 0) ResourceManager.glyphid.renderPart("ArmorLeft");
            if((armor & (1 << 2)) > 0) ResourceManager.glyphid.renderPart("ArmorRight");

            /// LEFT ARM ///
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.25, 0.625, 0.0625);
            GlStateManager.rotate(10, 0, 1, 0);
            GlStateManager.rotate(35 + cy1 * 20, 1, 0, 0);
            GlStateManager.translate(-0.25, -0.625, -0.0625);
            ResourceManager.glyphid.renderPart("ArmLeftUpper");
            GlStateManager.translate(0.25, 0.625, 0.4375);
            GlStateManager.rotate(-75 - cy1 * 20 + cy0 * 20, 1, 0, 0);
            GlStateManager.translate(-0.25, -0.625, -0.4375);
            ResourceManager.glyphid.renderPart("ArmLeftMid");
            GlStateManager.translate(0.25, 0.625, 0.9375);
            GlStateManager.rotate(90 - cy0 * 45, 1, 0, 0);
            GlStateManager.translate(-0.25, -0.625, -0.9375);
            ResourceManager.glyphid.renderPart("ArmLeftLower");
            if((armor & (1 << 3)) > 0) ResourceManager.glyphid.renderPart("ArmLeftArmor");
            GlStateManager.popMatrix();

            /// RIGHT ARM ///
            GlStateManager.pushMatrix();
            GlStateManager.translate(-0.25, 0.625, 0.0625);
            GlStateManager.rotate(-10, 0, 1, 0);
            GlStateManager.rotate(35 + cy2 * 20, 1, 0, 0);
            GlStateManager.translate(0.25, -0.625, -0.0625);
            ResourceManager.glyphid.renderPart("ArmRightUpper");
            GlStateManager.translate(-0.25, 0.625, 0.4375);
            GlStateManager.rotate(-75 - cy2 * 20 + cy3 * 20, 1, 0, 0);
            GlStateManager.translate(0.25, -0.625, -0.4375);
            ResourceManager.glyphid.renderPart("ArmRightMid");
            GlStateManager.translate(-0.25, 0.625, 0.9375);
            GlStateManager.rotate(90 - cy3 * 45, 1, 0, 0);
            GlStateManager.translate(0.25, -0.625, -0.9375);
            ResourceManager.glyphid.renderPart("ArmRightLower");
            if((armor & (1 << 4)) > 0) ResourceManager.glyphid.renderPart("ArmRightArmor");
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();

            GlStateManager.translate(0, 0.5, 0.25);
            GlStateManager.rotate(headTilt, 0, 0, 1);
            GlStateManager.translate(0, -0.5, -0.25);

            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 0.5, 0.25);
            GlStateManager.rotate(-bite, 1, 0, 0);
            GlStateManager.translate(0, -0.5, -0.25);
            ResourceManager.glyphid.renderPart("JawTop");
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 0.5, 0.25);
            GlStateManager.rotate(bite, 0, 1, 0);
            GlStateManager.rotate(bite, 1, 0, 0);
            GlStateManager.translate(0, -0.5, -0.25);
            ResourceManager.glyphid.renderPart("JawLeft");
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 0.5, 0.25);
            GlStateManager.rotate(-bite, 0, 1, 0);
            GlStateManager.rotate(bite, 1, 0, 0);
            GlStateManager.translate(0, -0.5, -0.25);
            ResourceManager.glyphid.renderPart("JawRight");
            GlStateManager.popMatrix();

            GlStateManager.popMatrix();

            double steppy = 15;
            double bend = 60;

            for(int i = 0; i < 3; i++) {

                double c0 = cy0 * (i == 1 ? -1 : 1);
                double c1 = cy1 * (i == 1 ? -1 : 1);

                GlStateManager.pushMatrix();
                GlStateManager.translate(0, 0.25, 0);
                GlStateManager.rotate(i * 30 - 15 + c0 * 7.5, 0, 1, 0);
                GlStateManager.rotate(steppy + c1 * steppy, 0, 0, 1);
                GlStateManager.translate(0, -0.25, 0);
                ResourceManager.glyphid.renderPart("LegLeftUpper");
                GlStateManager.translate(0.5625, 0.25, 0);
                GlStateManager.rotate(-bend - c1 * steppy, 0, 0, 1);
                GlStateManager.translate(-0.5625, -0.25, 0);
                ResourceManager.glyphid.renderPart("LegLeftLower");
                GlStateManager.popMatrix();

                GlStateManager.pushMatrix();
                GlStateManager.translate(0, 0.25, 0);
                GlStateManager.rotate(i * 30 - 45 + c0 * 7.5, 0, 1, 0);
                GlStateManager.rotate(-steppy + c1 * steppy, 0, 0, 1);
                GlStateManager.translate(0, -0.25, 0);
                ResourceManager.glyphid.renderPart("LegRightUpper");
                GlStateManager.translate(-0.5625, 0.25, 0);
                GlStateManager.rotate(bend - c1 * steppy, 0, 0, 1);
                GlStateManager.translate(0.5625, -0.25, 0);
                ResourceManager.glyphid.renderPart("LegRightLower");
                GlStateManager.popMatrix();
            }

            GlStateManager.popMatrix();
        }
    }
}
