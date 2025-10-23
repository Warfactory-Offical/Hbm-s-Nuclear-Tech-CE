package com.leafia.unsorted;

import com.leafia.transformer.LeafiaGls;
import net.minecraft.client.particle.ParticleRedstone;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class ParticleRedstoneLight extends ParticleRedstone {
    public ParticleRedstoneLight(World worldIn,double xCoordIn,double yCoordIn,double zCoordIn,float scale,double mx,double my,double mz,float red,float green,float blue) {
        super(worldIn,xCoordIn,yCoordIn,zCoordIn,scale,0f,0f,0f);
        this.motionX = mx;
        this.motionY = my;
        this.motionZ = mz;

        if (red == 0.0F)
        {
            red = 1.0F;
        }
        this.particleRed = red;
        this.particleGreen = green;
        this.particleBlue = blue;
    }

    @Override
    public void renderParticle(BufferBuilder buffer,Entity entityIn,float partialTicks,float rotationX,float rotationZ,float rotationYZ,float rotationXY,float rotationXZ) {
        Tessellator tes = Tessellator.getInstance();
        tes.draw(); // ah fuck it

        LeafiaGls._push();
        LeafiaGls.pushMatrix();
        LeafiaGls.color(1, 1, 1, 1);
        LeafiaGls.disableLighting();
        LeafiaGls.enableBlend();
        LeafiaGls.alphaFunc(GL11.GL_GREATER, 0);
        LeafiaGls.depthMask(false);
        LeafiaGls.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        buffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        super.renderParticle(buffer,entityIn,partialTicks,rotationX,rotationZ,rotationYZ,rotationXY,rotationXZ);
        tes.draw();
        LeafiaGls.popMatrix();
        LeafiaGls._pop();

        buffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
    }

    @Override
    public int getBrightnessForRender(float p_189214_1_) {
        return 255;
    }
}
