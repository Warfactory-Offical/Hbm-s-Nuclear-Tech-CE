package com.hbm.dim;

import com.hbm.capability.HbmLivingProps;
import com.hbm.dim.SolarSystem.AstroMetric;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.dim.trait.CelestialBodyTrait.CBT_Destroyed;
import com.hbm.lib.RefStrings;
import com.hbm.render.Shader;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.saveddata.satellites.SatelliteSavedData;
import com.hbm.util.BobMathUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.IRenderHandler;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Map;

public class SkyProviderCelestial extends IRenderHandler {
	
	private static final ResourceLocation planetTexture = new ResourceLocation(RefStrings.MODID, "textures/misc/space/planet.png");
	private static final ResourceLocation flareTexture = new ResourceLocation(RefStrings.MODID, "textures/misc/space/sunspike.png");
	private static final ResourceLocation nightTexture = new ResourceLocation(RefStrings.MODID, "textures/misc/space/night.png");
	private static final ResourceLocation digammaStar = new ResourceLocation(RefStrings.MODID, "textures/misc/space/star_digamma.png");

	private static final ResourceLocation noise = new ResourceLocation(RefStrings.MODID, "shaders/iChannel1.png");

	protected static final Shader planetShader = new Shader(new ResourceLocation(RefStrings.MODID, "shaders/crescent.frag"));
	protected static final Shader swarmShader = new Shader(new ResourceLocation(RefStrings.MODID, "shaders/swarm.vert"), new ResourceLocation(RefStrings.MODID, "shaders/swarm.frag"));

	public static boolean displayListsInitialized = false;
	public static int skyVBO;
	public static int sky2VBO;

	public SkyProviderCelestial() {
		if (!displayListsInitialized) {
			initializeDisplayLists();
		}
	}

	private void initializeDisplayLists() {
		Minecraft mc = Minecraft.getMinecraft();
		skyVBO = mc.renderGlobal.glSkyList;
		sky2VBO = mc.renderGlobal.glSkyList2;

		displayListsInitialized = true;
	}

	private static int lastBrightestPixel = 0;

	@Override
	public void render(float partialTicks, WorldClient world, Minecraft mc) {
		float fogIntensity = 0;

		if(world.provider instanceof WorldProviderCelestial) {
			DynamicTexture lightmapTexture = mc.entityRenderer.lightmapTexture;
			int[] lightmapColors = mc.entityRenderer.lightmapColors;
			// Without mixins, we have to resort to some very wacky ways of checking that the lightmap needs to be updated
			// fortunately, thanks to torch flickering, we can just check to see if the brightest pixel has been modified
			if(lastBrightestPixel != lightmapColors[255] + lightmapColors[250]) {
				if(((WorldProviderCelestial)world.provider).updateLightmap(lightmapColors)) {
					lightmapTexture.updateDynamicTexture();
				}

				lastBrightestPixel = lightmapColors[255] + lightmapColors[250];
			}

			fogIntensity = ((WorldProviderCelestial) world.provider).fogDensity() * 30;
		}

		CelestialBody body = CelestialBody.getBody(world);
		CelestialBody sun = body.getStar();
		CBT_Atmosphere atmosphere = body.getTrait(CBT_Atmosphere.class);

		boolean hasAtmosphere = atmosphere != null;

		float pressure = hasAtmosphere ? (float)atmosphere.getPressure() : 0.0F;
		float visibility = hasAtmosphere ? MathHelper.clamp(2.0F - pressure, 0.1F, 1.0F) : 1.0F;

		GlStateManager.disableTexture2D();
		Vec3d skyColor = world.getSkyColor(mc.getRenderViewEntity(), partialTicks);

		float skyR = (float) skyColor.x;
		float skyG = (float) skyColor.y;
		float skyB = (float) skyColor.z;

		// Diminish sky colour when leaving the atmosphere
		if(mc.getRenderViewEntity().posY > 300) {
			double curvature = MathHelper.clamp((800.0F - (float) mc.getRenderViewEntity().posY) / 500.0F, 0.0F, 1.0F);
			skyR *= curvature;
			skyG *= curvature;
			skyB *= curvature;
		}

		if(mc.gameSettings.anaglyph) {
			float[] anaglyphColor = applyAnaglyph(skyR, skyG, skyB);
			skyR = anaglyphColor[0];
			skyG = anaglyphColor[1];
			skyB = anaglyphColor[2];
		}

		float planetR = skyR;
		float planetG = skyG;
		float planetB = skyB;

		if(fogIntensity > 0.01F) {
			Vec3d fogColor = world.getFogColor(partialTicks);
			planetR = (float)BobMathUtil.clampedLerp(skyR, fogColor.x, fogIntensity);
			planetG = (float)BobMathUtil.clampedLerp(skyG, fogColor.y, fogIntensity);
			planetB = (float)BobMathUtil.clampedLerp(skyB, fogColor.z, fogIntensity);
		}

		Vec3d planetTint = new Vec3d(planetR, planetG, planetB);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();

		GlStateManager.depthMask(false);
		GlStateManager.enableFog();
		GlStateManager.color(skyR, skyG, skyB);

		GlStateManager.pushMatrix();
		{
			GlStateManager.translate(0.0F, mc.gameSettings.renderDistanceChunks - 12.0F, 0.0F);

			GlStateManager.callList(skyVBO);
		}
		GlStateManager.popMatrix();

		GlStateManager.disableFog();
		GlStateManager.disableAlpha();
		GlStateManager.enableTexture2D();

		GlStateManager.enableBlend();
		RenderHelper.disableStandardItemLighting();

		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

		float starBrightness = world.getStarBrightness(partialTicks) * visibility;
		float celestialAngle = world.getCelestialAngle(partialTicks);

		// Handle any special per-body sunset rendering
		renderSunset(partialTicks, world, mc);

		renderStars(partialTicks, world, mc, starBrightness, celestialAngle, body.axialTilt);


		GlStateManager.pushMatrix();
		{
			GlStateManager.rotate(body.axialTilt, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(celestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);

			// Draw DIGAMMA STAR
			renderDigamma(partialTicks, world, mc, celestialAngle);

			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);

			double sunSize = SolarSystem.calculateSunSize(body);
			double coronaSize = sunSize * (3 - MathHelper.clamp(pressure, 0.0F, 1.0F));

			renderSun(partialTicks, world, mc, sun, sunSize, coronaSize, visibility, pressure);

			float blendAmount = hasAtmosphere ? MathHelper.clamp(1 - world.getSunBrightnessFactor(partialTicks), 0.25F, 1F) : 1F;

			double longitude = 0;
			CelestialBody tidalLockedBody = body.tidallyLockedTo != null ? CelestialBody.getBody(body.tidallyLockedTo) : null;

			if(tidalLockedBody != null) {
				longitude = SolarSystem.calculateSingleAngle(world, partialTicks, body, tidalLockedBody) + celestialAngle * 360.0 + 60.0;
			}

			// Get our orrery of bodies
			List<AstroMetric> metrics = SolarSystem.calculateMetricsFromBody(world, partialTicks, longitude, body);

			renderCelestials(partialTicks, world, mc, metrics, celestialAngle, tidalLockedBody, planetTint, visibility, blendAmount, null, 24);

			GlStateManager.enableBlend();

			if(visibility > 0.2F) {
				// JEFF BOZOS WOULD LIKE TO KNOW YOUR LOCATION
				// ... to send you a pakedge :)))
				if(world.provider.getDimension() == 0) {
					renderSatellite(partialTicks, world, mc, celestialAngle, 1916169, new float[] { 1.0F, 0.534F, 0.385F });
				}

				// Light up the sky
                ObjectIterator<Int2ObjectMap.Entry<Satellite>> iterator = SatelliteSavedData.getClientSats().int2ObjectEntrySet().fastIterator();
                while (iterator.hasNext()) {
                    var entry = iterator.next();
                    renderSatellite(partialTicks, world, mc, celestialAngle, entry.getIntKey(), entry.getValue().getColor());
                }
            }
		}
		GlStateManager.popMatrix();

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableFog();

		GlStateManager.disableTexture2D();
		GlStateManager.color(0.0F, 0.0F, 0.0F);

		Vec3d pos = mc.player.getPositionVector();
		double heightAboveHorizon = pos.y - world.getHorizon();

		if(heightAboveHorizon < 0.0D) {
			GlStateManager.pushMatrix();
			{
				GlStateManager.translate(0.0F, 12.0F, 0.0F);

				GlStateManager.callList(sky2VBO);
			}
			GlStateManager.popMatrix();

			float f8 = 1.0F;
			float f9 = -((float) (heightAboveHorizon + 65.0D));
			float opposite = -f8;

			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
			bufferBuilder.pos(-f8, f9, f8).color(0, 0, 0, 255).endVertex();
			bufferBuilder.pos(f8, f9, f8).color(0, 0, 0, 255).endVertex();
			bufferBuilder.pos(f8, opposite, f8).color(0, 0, 0, 255).endVertex();
			bufferBuilder.pos(-f8, opposite, f8).color(0, 0, 0, 255).endVertex();
			bufferBuilder.pos(-f8, opposite, -f8).color(0, 0, 0, 255).endVertex();
			bufferBuilder.pos(f8, opposite, -f8).color(0, 0, 0, 255).endVertex();
			bufferBuilder.pos(f8, f9, -f8).color(0, 0, 0, 255).endVertex();
			bufferBuilder.pos(-f8, f9, -f8).color(0, 0, 0, 255).endVertex();
			bufferBuilder.pos(f8, opposite, -f8).color(0, 0, 0, 255).endVertex();
			bufferBuilder.pos(f8, opposite, f8).color(0, 0, 0, 255).endVertex();
			bufferBuilder.pos(f8, f9, f8).color(0, 0, 0, 255).endVertex();
			bufferBuilder.pos(f8, f9, -f8).color(0, 0, 0, 255).endVertex();
			bufferBuilder.pos(-f8, f9, -f8).color(0, 0, 0, 255).endVertex();
			bufferBuilder.pos(-f8, f9, f8).color(0, 0, 0, 255).endVertex();
			bufferBuilder.pos(-f8, opposite, f8).color(0, 0, 0, 255).endVertex();
			bufferBuilder.pos(-f8, opposite, -f8).color(0, 0, 0, 255).endVertex();
			bufferBuilder.pos(-f8, opposite, -f8).color(0, 0, 0, 255).endVertex();
			bufferBuilder.pos(-f8, opposite, f8).color(0, 0, 0, 255).endVertex();
			bufferBuilder.pos(f8, opposite, f8).color(0, 0, 0, 255).endVertex();
			bufferBuilder.pos(f8, opposite, -f8).color(0, 0, 0, 255).endVertex();
			tessellator.draw();
		}

		if(world.provider.isSkyColored()) {
			GlStateManager.color(skyR * 0.2F + 0.04F, skyG * 0.2F + 0.04F, skyB * 0.6F + 0.1F);
		} else {
			GlStateManager.color(skyR, skyG, skyB);
		}

		GlStateManager.pushMatrix();
		{
			GlStateManager.translate(0.0F, -((float) (heightAboveHorizon - 16.0D)), 0.0F);

			GlStateManager.callList(sky2VBO);
		}
		GlStateManager.popMatrix();

		double sc = 1 / (pos.y / 1000);
		double uvOffset = (pos.x / 1024) % 1;
		GlStateManager.pushMatrix();
		{
			GlStateManager.enableTexture2D();
			GlStateManager.disableAlpha();
			GlStateManager.disableFog();
			GlStateManager.enableBlend();

			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

			float sunBrightness = world.getSunBrightness(partialTicks);

			GlStateManager.color(sunBrightness, sunBrightness, sunBrightness, ((float)pos.y - 200.0F) / 300.0F);
			mc.getTextureManager().bindTexture(body.texture);
			GlStateManager.rotate(180, 1, 0, 0);

			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			bufferBuilder.pos(-115 * sc, 100.0D, -115 * sc).tex(0.0D + uvOffset, 0.0D).endVertex();
			bufferBuilder.pos(115 * sc, 100.0D, -115 * sc).tex(1.0D + uvOffset, 0.0D).endVertex();
			bufferBuilder.pos(115 * sc, 100.0D, 115 * sc).tex(1.0D + uvOffset, 1.0D).endVertex();
			bufferBuilder.pos(-115 * sc, 100.0D, 115 * sc).tex(0.0D + uvOffset, 1.0D).endVertex();
			tessellator.draw();

			GlStateManager.disableTexture2D();
			GlStateManager.enableAlpha();
			GlStateManager.enableFog();
			GlStateManager.disableBlend();

			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
		}
		GlStateManager.popMatrix();

		GlStateManager.enableTexture2D();
		GlStateManager.depthMask(true);
	}
	
	protected void renderSunset(float partialTicks, WorldClient world, Minecraft mc) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();

		float[] sunsetColor = world.provider.calcSunriseSunsetColors(world.getCelestialAngle(partialTicks), partialTicks);

		if(sunsetColor != null) {
			float[] anaglyphColor = mc.gameSettings.anaglyph ? applyAnaglyph(sunsetColor) : sunsetColor;

			GlStateManager.disableTexture2D();
			GlStateManager.shadeModel(GL11.GL_SMOOTH);

			GlStateManager.pushMatrix();
			{
				GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
				GlStateManager.rotate(MathHelper.sin(world.getCelestialAngleRadians(partialTicks)) < 0.0F ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
				GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);

				bufferBuilder.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
				bufferBuilder.pos(0.0, 100.0, 0.0).color(anaglyphColor[0], anaglyphColor[1], anaglyphColor[2], sunsetColor[3]).endVertex();
				byte segments = 16;

				for(int j = 0; j <= segments; ++j) {
					float angle = (float)j * 3.1415927F * 2.0F / (float)segments;
					float sinAngle = MathHelper.sin(angle);
					float cosAngle = MathHelper.cos(angle);
					bufferBuilder.pos((double)(sinAngle * 120.0F), (double)(cosAngle * 120.0F), (double)(-cosAngle * 40.0F * sunsetColor[3]))
							.color(sunsetColor[0], sunsetColor[1], sunsetColor[2], 0.0F).endVertex();
				}

				tessellator.draw();
			}
			GlStateManager.popMatrix();

			GlStateManager.shadeModel(GL11.GL_FLAT);
			GlStateManager.enableTexture2D();
		}
	}

	protected void renderStars(float partialTicks, WorldClient world, Minecraft mc, float starBrightness, float celestialAngle, float axialTilt) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();

		if(starBrightness > 0.0F) {
			GlStateManager.pushMatrix();
			{
				GlStateManager.rotate(axialTilt, 1.0F, 0.0F, 0.0F);

				mc.getTextureManager().bindTexture(nightTexture);

				GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);

				float starBrightnessAlpha = starBrightness * 0.6f;
				GlStateManager.color(1.0F, 1.0F, 1.0F, starBrightnessAlpha);

				GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);

				GlStateManager.rotate(celestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);
				GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
				GlStateManager.color(1.0F, 1.0F, 1.0F, starBrightnessAlpha);

				GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
				GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
				renderSkyboxSide(tessellator, bufferBuilder, 4);

				GlStateManager.pushMatrix();
				GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
				renderSkyboxSide(tessellator, bufferBuilder, 1);
				GlStateManager.popMatrix();

				GlStateManager.pushMatrix();
				GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
				renderSkyboxSide(tessellator, bufferBuilder, 0);
				GlStateManager.popMatrix();

				GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
				renderSkyboxSide(tessellator, bufferBuilder, 5);

				GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
				renderSkyboxSide(tessellator, bufferBuilder, 2);

				GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
				renderSkyboxSide(tessellator, bufferBuilder, 3);

				GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			}
			GlStateManager.popMatrix();
		}
	}

	protected void renderSun(float partialTicks, WorldClient world, Minecraft mc, CelestialBody sun, double sunSize, double coronaSize, float visibility, float pressure) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();

		if(sun.shader != null && sun.hasTrait(CBT_Destroyed.class)) {
			// BLACK HOLE SUN
			// WON'T YOU COME
			// AND WASH AWAY THE RAIN

			Shader shader = sun.shader;
			double shaderSize = sunSize * sun.shaderScale;

			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

			shader.use();

			float time = ((float)world.getWorldTime() + partialTicks) / 20.0F;
			int textureUnit = 0;

			mc.getTextureManager().bindTexture(noise);

			shader.setTime(time);
			shader.setTextureUnit(textureUnit);

			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			bufferBuilder.pos(-shaderSize, 100.0D, -shaderSize).tex(0.0D, 0.0D).endVertex();
			bufferBuilder.pos(shaderSize, 100.0D, -shaderSize).tex(1.0D, 0.0D).endVertex();
			bufferBuilder.pos(shaderSize, 100.0D, shaderSize).tex(1.0D, 1.0D).endVertex();
			bufferBuilder.pos(-shaderSize, 100.0D, shaderSize).tex(0.0D, 1.0D).endVertex();
			tessellator.draw();

			shader.stop();

			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
		} else {
			// Some blanking to conceal the stars
			GlStateManager.disableTexture2D();
			GlStateManager.color(0.0F, 0.0F, 0.0F, 1.0F);

			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
			bufferBuilder.pos(-sunSize, 99.9D, -sunSize).endVertex();
			bufferBuilder.pos(sunSize, 99.9D, -sunSize).endVertex();
			bufferBuilder.pos(sunSize, 99.9D, sunSize).endVertex();
			bufferBuilder.pos(-sunSize, 99.9D, sunSize).endVertex();
			tessellator.draw();

			// Draw the sun to the depth buffer to block swarm members that are behind
			GlStateManager.depthMask(true);
			GlStateManager.color(0.0F, 0.0F, 0.0F, 0.0F);

			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			bufferBuilder.pos(-sunSize * 0.25D, 100.1D, -sunSize * 0.25D).tex(0.0D, 0.0D).endVertex();
			bufferBuilder.pos(sunSize * 0.25D, 100.1D, -sunSize * 0.25D).tex(1.0D, 0.0D).endVertex();
			bufferBuilder.pos(sunSize * 0.25D, 100.1D, sunSize * 0.25D).tex(1.0D, 1.0D).endVertex();
			bufferBuilder.pos(-sunSize * 0.25D, 100.1D, sunSize * 0.25D).tex(0.0D, 1.0D).endVertex();
			tessellator.draw();

			GlStateManager.depthMask(false);

			GlStateManager.enableTexture2D();
			GlStateManager.color(1.0F, 1.0F, 1.0F, visibility);

			// Draw the MIGHTY SUN
			mc.getTextureManager().bindTexture(sun.texture);

			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			bufferBuilder.pos(-sunSize, 100.0D, -sunSize).tex(0.0D, 0.0D).endVertex();
			bufferBuilder.pos(sunSize, 100.0D, -sunSize).tex(1.0D, 0.0D).endVertex();
			bufferBuilder.pos(sunSize, 100.0D, sunSize).tex(1.0D, 1.0D).endVertex();
			bufferBuilder.pos(-sunSize, 100.0D, sunSize).tex(0.0D, 1.0D).endVertex();
			tessellator.draw();

			// Draw a big ol' spiky flare! Less so when there is an atmosphere
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1 - MathHelper.clamp(pressure, 0.0F, 1.0F) * 0.75F);

			mc.getTextureManager().bindTexture(flareTexture);

			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			bufferBuilder.pos(-coronaSize, 99.9D, -coronaSize).tex(0.0D, 0.0D).endVertex();
			bufferBuilder.pos(coronaSize, 99.9D, -coronaSize).tex(1.0D, 0.0D).endVertex();
			bufferBuilder.pos(coronaSize, 99.9D, coronaSize).tex(1.0D, 1.0D).endVertex();
			bufferBuilder.pos(-coronaSize, 99.9D, coronaSize).tex(0.0D, 1.0D).endVertex();
			tessellator.draw();

			// Draw the swarm members with depth occlusion
			// We do this last so we can render transparency against the sun
			renderSwarm(partialTicks, world, mc, sunSize * 0.5, 0);

			// Clear and disable the depth buffer once again, buffer has to be writable to clear it
			GlStateManager.depthMask(true);
			GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
			GlStateManager.depthMask(false);
		}
	}

	private void renderSwarm(float partialTicks, WorldClient world, Minecraft mc, double swarmRadius, int swarmCount) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();

		// bloodseeking, parasitic, ecstatically tracing decay
		// thriving in the glow that death emits, the warm perfume it radiates

		swarmShader.use();

		// swarm members render as pixels, which can vary based on screen resolution
		// because of this, we make the pixels more transparent based on their apparent size, which varies by a fair few factors
		// this isn't a foolproof solution, analyzing the projection matrices would be best, but it works for now.
		float swarmScreenSize = (float)((mc.displayHeight / mc.gameSettings.fovSetting) * swarmRadius * 0.002);
		float time = ((float)world.getWorldTime() + partialTicks) / 800.0F;
		int textureUnit = 0;

		swarmShader.setTime(time);
		swarmShader.setTextureUnit(textureUnit);

		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(0.0F, 0.0F, 0.0F, MathHelper.clamp(swarmScreenSize, 0, 1));

		GlStateManager.pushMatrix();
		{
			GlStateManager.translate(0.0F, 100.0F, 0.0F);
			GlStateManager.scale(swarmRadius, swarmRadius, swarmRadius);

			GlStateManager.pushMatrix();
			{
				GlStateManager.rotate(80.0F, 1, 0, 0);

				bufferBuilder.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION);
				for(int i = 0; i < swarmCount; i += 3) {
					swarmShader.setOffset(i);

					float t = i + time;
					double x = Math.cos(t);
					double z = Math.sin(t);

					bufferBuilder.pos(x, 0, z).endVertex();
				}
				tessellator.draw();
			}
			GlStateManager.popMatrix();

			GlStateManager.pushMatrix();
			{
				GlStateManager.rotate(60.0F, 0, 1, 0);
				GlStateManager.rotate(80.0F, 1, 0, 0);

				bufferBuilder.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION);
				for(int i = 1; i < swarmCount; i += 3) {
					swarmShader.setOffset(i);

					float t = i + time;
					double x = Math.cos(t);
					double z = Math.sin(t);

					bufferBuilder.pos(x, 0, z).endVertex();
				}
				tessellator.draw();
			}
			GlStateManager.popMatrix();

			GlStateManager.pushMatrix();
			{
				GlStateManager.rotate(-60.0F, 0, 1, 0);
				GlStateManager.rotate(80.0F, 1, 0, 0);

				bufferBuilder.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION);
				for(int i = 2; i < swarmCount; i += 3) {
					swarmShader.setOffset(i);

					float t = i + time;
					double x = Math.cos(t);
					double z = Math.sin(t);

					bufferBuilder.pos(x, 0, z).endVertex();
				}
				tessellator.draw();
			}
			GlStateManager.popMatrix();
		}
		GlStateManager.popMatrix();

		swarmShader.stop();

		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
	}

	protected void renderCelestials(float partialTicks, WorldClient world, Minecraft mc, List<AstroMetric> metrics, float celestialAngle, CelestialBody tidalLockedBody, Vec3d planetTint, float visibility, float blendAmount, CelestialBody orbiting, float maxSize) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		double minSize = 1D;
		float blendDarken = 0.1F;

		for(AstroMetric metric : metrics) {
			// Ignore self
			if(metric.distance == 0)
				continue;

			boolean orbitingThis = metric.body == orbiting;

			double uvOffset = orbitingThis ? 1 - ((((double)world.getWorldTime() + partialTicks) / 1024) % 1) : 0;
			float axialTilt = orbitingThis ? 0 : metric.body.axialTilt;

			GlStateManager.pushMatrix();
			{
				double size = MathHelper.clamp(metric.apparentSize, 0, maxSize);
				boolean renderAsPoint = size < minSize;

				if(renderAsPoint) {
					float alpha = MathHelper.clamp((float)size * 100.0F, 0.0F, 1.0F);
					GlStateManager.color(metric.body.color[0], metric.body.color[1], metric.body.color[2], alpha * visibility);
					mc.getTextureManager().bindTexture(planetTexture);

					size = minSize;
				} else {
					GlStateManager.disableBlend();
					GlStateManager.color(1.0F, 1.0F, 1.0F, visibility);
					mc.getTextureManager().bindTexture(metric.body.texture);
				}

				if(metric.body == tidalLockedBody) {
					GlStateManager.rotate((float)(celestialAngle * -360.0 - 60.0), 1.0F, 0.0F, 0.0F);
				} else {
					GlStateManager.rotate((float)metric.angle, 1.0F, 0.0F, 0.0F);
				}
				GlStateManager.rotate(axialTilt + 90.0F, 0.0F, 1.0F, 0.0F);

				bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
				bufferBuilder.pos(-size, 100.0D, -size).tex(0.0D + uvOffset, 0.0D).endVertex();
				bufferBuilder.pos(size, 100.0D, -size).tex(1.0D + uvOffset, 0.0D).endVertex();
				bufferBuilder.pos(size, 100.0D, size).tex(1.0D + uvOffset, 1.0D).endVertex();
				bufferBuilder.pos(-size, 100.0D, size).tex(0.0D + uvOffset, 1.0D).endVertex();
				tessellator.draw();

				if(!renderAsPoint) {
					GlStateManager.enableBlend();

					// Draw a shader on top to render celestial phase
					GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

					GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

					planetShader.use();
					planetShader.setTime((float)-metric.phase);
					planetShader.setOffset((float)uvOffset);

					bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
					bufferBuilder.pos(-size, 100.0D, -size).tex(0.0D, 0.0D).endVertex();
					bufferBuilder.pos(size, 100.0D, -size).tex(1.0D, 0.0D).endVertex();
					bufferBuilder.pos(size, 100.0D, size).tex(1.0D, 1.0D).endVertex();
					bufferBuilder.pos(-size, 100.0D, size).tex(0.0D, 1.0D).endVertex();
					tessellator.draw();

					planetShader.stop();

					GlStateManager.disableTexture2D();

					// Draw another layer on top to blend with the atmosphere
					GlStateManager.color((float)(planetTint.x - blendDarken), (float)(planetTint.y - blendDarken), (float)(planetTint.z - blendDarken), (float)(1 - blendAmount * visibility));
					GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);

					bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
					bufferBuilder.pos(-size, 100.0D, -size).tex(0.0D, 0.0D).endVertex();
					bufferBuilder.pos(size, 100.0D, -size).tex(1.0D, 0.0D).endVertex();
					bufferBuilder.pos(size, 100.0D, size).tex(1.0D, 1.0D).endVertex();
					bufferBuilder.pos(-size, 100.0D, size).tex(0.0D, 1.0D).endVertex();
					tessellator.draw();

					GlStateManager.enableTexture2D();
				}
			}
			GlStateManager.popMatrix();
		}
	}

	protected void renderDigamma(float partialTicks, WorldClient world, Minecraft mc, float celestialAngle) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();

		GlStateManager.pushMatrix();
		{
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);

			float brightness = (float) Math.sin(celestialAngle * Math.PI);
			brightness *= brightness;
			GlStateManager.color(brightness, brightness, brightness, brightness);
			GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(celestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(140.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(-40.0F, 0.0F, 0.0F, 1.0F);

			mc.getTextureManager().bindTexture(digammaStar);

            double digamma = HbmLivingProps.getDigamma(Minecraft.getMinecraft().player);
            double var12 = 1D + digamma * 0.25D;
            double dist = 100D - digamma * 2.5D;

			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            bufferBuilder.pos(-var12, dist, -var12).tex(0.0D, 0.0D).endVertex();
            bufferBuilder.pos(var12, dist, -var12).tex(0.0D, 1.0D).endVertex();
            bufferBuilder.pos(var12, dist, var12).tex(1.0D, 1.0D).endVertex();
            bufferBuilder.pos(-var12, dist, var12).tex(1.0D, 0.0D).endVertex();
			tessellator.draw();
		}
		GlStateManager.popMatrix();
	}

	// Does anyone even play with 3D glasses anymore?
	protected float[] applyAnaglyph(float... colors) {
		float r = (colors[0] * 30.0F + colors[1] * 59.0F + colors[2] * 11.0F) / 100.0F;
		float g = (colors[0] * 30.0F + colors[1] * 70.0F) / 100.0F;
		float b = (colors[0] * 30.0F + colors[2] * 70.0F) / 100.0F;

		return new float[] { r, g, b };
	}

	protected void renderSatellite(float partialTicks, WorldClient world, Minecraft mc, float celestialAngle, long seed, float[] color) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();

		double ticks = (double)(System.currentTimeMillis() % (600 * 50)) / 50;

		GlStateManager.pushMatrix();
		{
			GlStateManager.rotate(celestialAngle * -360.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(-40.0F + (float)(seed % 800) * 0.1F - 5.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate((float)(seed % 50) * 0.1F - 20.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate((float)(seed % 80) * 0.1F - 2.5F, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate((float)((ticks / 600.0D) * 360.0D), 1.0F, 0.0F, 0.0F);

			GlStateManager.color(color[0], color[1], color[2], 1F);

			mc.getTextureManager().bindTexture(planetTexture);

			float size = 0.5F;

			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			bufferBuilder.pos(-size, 100.0, -size).tex(0.0D, 0.0D).endVertex();
			bufferBuilder.pos(size, 100.0, -size).tex(0.0D, 1.0D).endVertex();
			bufferBuilder.pos(size, 100.0, size).tex(1.0D, 1.0D).endVertex();
			bufferBuilder.pos(-size, 100.0, size).tex(1.0D, 0.0D).endVertex();
			tessellator.draw();
		}
		GlStateManager.popMatrix();
	}

	// is just drawing a big cube with UVs prepared to draw a gradient
	private void renderSkyboxSide(Tessellator tessellator, BufferBuilder bufferBuilder, int side) {
		double u = side % 3 / 3.0D;
		double v = side / 3 / 2.0D;
		bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		bufferBuilder.pos(-100.0D, -100.0D, -100.0D).tex(u, v).endVertex();
		bufferBuilder.pos(-100.0D, -100.0D, 100.0D).tex(u, v + 0.5D).endVertex();
		bufferBuilder.pos(100.0D, -100.0D, 100.0D).tex(u + 0.3333333333333333D, v + 0.5D).endVertex();
		bufferBuilder.pos(100.0D, -100.0D, -100.0D).tex(u + 0.3333333333333333D, v).endVertex();
		tessellator.draw();
	}

}
