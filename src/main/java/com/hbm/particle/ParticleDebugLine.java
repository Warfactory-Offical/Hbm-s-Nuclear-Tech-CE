package com.hbm.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class ParticleDebugLine extends Particle {

    int color;

    public ParticleDebugLine(World world, double x, double y, double z, double lx, double ly, double lz, int color) {
        super(world, x, y, z, lx, ly, lz);
        this.motionX = lx;
        this.motionY = ly;
        this.motionZ = lz;
        this.color = color;
        this.particleMaxAge = 60;
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if(this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
        }
    }

    @Override
    public int getFXLayer() {
        return 3;
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        double pX = this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - interpPosX;
        double pY = this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - interpPosY;
        double pZ = this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - interpPosZ;

        double mX = pX + motionX;
        double mY = pY + motionY;
        double mZ = pZ + motionZ;

        GlStateManager.pushMatrix();
        GlStateManager.disableColorMaterial();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GL11.glEnable(GL11.GL_POINT_SMOOTH); //i couldnt find any GlStateManger equivalent to this so ill keep gl11 here
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        buffer.pos(pX, pY, pZ).tex(0, 0)
                .color((color >> 16) & 0xff, (color >> 8) & 0xff, color & 0xff, 255)
                .lightmap(0, (int) (240 - (240 * (this.particleAge + partialTicks) / this.particleMaxAge)))
                .endVertex();
        buffer.pos(mX, mY, mZ).tex(0, 0)
                .color((color >> 16) & 0xff, (color >> 8) & 0xff, color & 0xff, 255)
                .lightmap(0, (int) (240 - (240 * (this.particleAge + partialTicks) / this.particleMaxAge)))
                .endVertex();
        Tessellator.getInstance().draw();

        GlStateManager.enableColorMaterial();
        GlStateManager.enableTexture2D();
        GL11.glDisable(GL11.GL_POINT_SMOOTH);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }
}
