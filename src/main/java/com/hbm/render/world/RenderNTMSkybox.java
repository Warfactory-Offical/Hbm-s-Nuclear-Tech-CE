package com.hbm.render.world;

import com.hbm.capability.HbmLivingProps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.opengl.GL11;

public class RenderNTMSkybox extends IRenderHandler { //why an abstract class uses the I-prefix is beyond me but ok, alright, whatever
	
	/*
	 * To get the terrain render order right, making a sky rendering handler is absolutely necessary. Turns out MC can only handle one of these, so what do we do?
	 * We make out own renderer, grab any existing renderers that are already occupying the slot, doing what is effectively chainloading while adding our own garbage.
	 * If somebody does the exact same thing as we do we might be screwed due to increasingly long recursive loops but we can fix that too, no worries.
	 */
	private IRenderHandler parent;

	private static final ResourceLocation digammaStar = new ResourceLocation("hbm:textures/misc/star_digamma.png");
	private static final ResourceLocation bobmazonSat = new ResourceLocation("hbm:textures/misc/sat_bobmazon.png");
	
	/*
	 * If the skybox was rendered successfully in the last tick (even from other mods' skyboxes chainloading this one) then we don't need to add it again
	 */
	public static boolean didLastRender = false;
	
	public RenderNTMSkybox(IRenderHandler parent) {
		this.parent = parent;
	}
	
	@Override
	public void render(float partialTicks, WorldClient world, Minecraft mc) {
		
		if(parent != null) {
			parent.render(partialTicks, world, mc);
		} else{
			RenderGlobal rg = Minecraft.getMinecraft().renderGlobal;
			world.provider.setSkyRenderer(null);
			rg.renderSky(partialTicks, 2);
			world.provider.setSkyRenderer(this);
		}
		
		GlStateManager.pushMatrix();
		GlStateManager.depthMask(false);

		GlStateManager.enableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.disableFog();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
		
		float brightness = (float) Math.sin(world.getCelestialAngle(partialTicks) * Math.PI);
		brightness *= brightness;
		
		GlStateManager.color(brightness, brightness, brightness, 1.0F);
		
		GlStateManager.pushMatrix();
		GlStateManager.scale(0.9999F, 0.9999F, 0.9999F);
		GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(world.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(140.0F, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(-40.0F, 0.0F, 0.0F, 1.0F);
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(digammaStar);

        double digamma = HbmLivingProps.getDigamma(Minecraft.getMinecraft().player);
        double var12 = (1D + digamma * 0.25D);
        double dist = 100D - digamma * 2.5D;
		
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buf = tessellator.getBuffer();
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(-var12, dist, -var12).tex(0, 0).endVertex();
        buf.pos(var12, dist, -var12).tex(0, 1).endVertex();
        buf.pos(var12, dist, var12).tex(1, 1).endVertex();
        buf.pos(-var12, dist, var12).tex(1, 0).endVertex();
		tessellator.draw();
		GlStateManager.popMatrix();
		
		GlStateManager.pushMatrix();
		GlStateManager.rotate(-40.0F, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate((System.currentTimeMillis() % (360 * 1000) / 1000F), 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate((System.currentTimeMillis() % (360 * 100) / 100F), 1.0F, 0.0F, 0.0F);
		
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(bobmazonSat);
		
		var12 = 0.5F;
		dist = 100D;
		
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buf.pos(-var12, dist, -var12).tex(0, 0).endVertex();
		buf.pos(var12, dist, -var12).tex(0, 1).endVertex();
		buf.pos(var12, dist, var12).tex(1, 1).endVertex();
		buf.pos(-var12, dist, var12).tex(1, 0).endVertex();
		tessellator.draw();
		GlStateManager.popMatrix();
		
		GlStateManager.depthMask(true);

		GlStateManager.enableFog();
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.color(1, 1, 1, 1);
		
		GlStateManager.popMatrix();
		
		didLastRender = true;
	}

}
