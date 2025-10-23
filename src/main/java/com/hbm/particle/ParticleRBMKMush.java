package com.hbm.particle;

import com.hbm.render.NTMRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;

public class ParticleRBMKMush extends Particle {

	private static final ResourceLocation texture = new ResourceLocation(com.hbm.lib.RefStrings.MODID + ":textures/particle/rbmk_mush.png");
	
	public ParticleRBMKMush(World worldIn, double posXIn, double posYIn, double posZIn, float scale){
		super(worldIn, posXIn, posYIn, posZIn);
		particleMaxAge = 80;

		this.particleRed = this.particleGreen = this.particleBlue = 0;
		
		this.particleScale = scale;
	}
	
	@Override
	public void onUpdate(){
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		++this.particleAge;

		if(this.particleMaxAge == this.particleAge) {
			this.setExpired();
		}
	}
	
	@Override
	public int getFXLayer(){
		return 3;
	}
	
	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ){
        GlStateManager.enableAlpha();
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		NTMRenderHelper.resetParticleInterpPos(entityIn, partialTicks);

		int segs = 30;

		// the size of one frame
		double frame = 1D / (double) segs;
		// how many frames we're in
		int prog = particleAge * segs / particleMaxAge;

		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.glNormal3f(0, 1, 0);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.depthMask(false);
		RenderHelper.disableStandardItemLighting();

		Tessellator tes = Tessellator.getInstance();
		BufferBuilder buf = tes.getBuffer();
		
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

		float scale = this.particleScale;
		float pX = (float) ((this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - interpPosX));
		float pY = (float) ((this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - interpPosY)) + particleScale;
		float pZ = (float) ((this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - interpPosZ));

		buf.pos((double) (pX - rotationX * scale - rotationXY * scale), (double) (pY - rotationZ * scale), (double) (pZ - rotationYZ * scale - rotationXZ * scale)).tex(1, (prog + 1) * frame).endVertex();
		buf.pos((double) (pX - rotationX * scale + rotationXY * scale), (double) (pY + rotationZ * scale), (double) (pZ - rotationYZ * scale + rotationXZ * scale)).tex(1, prog * frame).endVertex();
		buf.pos((double) (pX + rotationX * scale + rotationXY * scale), (double) (pY + rotationZ * scale), (double) (pZ + rotationYZ * scale + rotationXZ * scale)).tex(0, prog * frame).endVertex();
		buf.pos((double) (pX + rotationX * scale - rotationXY * scale), (double) (pY - rotationZ * scale), (double) (pZ + rotationYZ * scale - rotationXZ * scale)).tex(0, (prog + 1) * frame).endVertex();
		tes.draw();

		GlStateManager.doPolygonOffset(0, 0);
		GlStateManager.enableLighting();
	}

}
