package com.hbm.render.tileentity;

import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.deco.TileEntityLanternBehemoth;
import com.hbm.util.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

@AutoRegister
public class RenderLanternBehemoth extends TileEntitySpecialRenderer<TileEntityLanternBehemoth> {
    @Override
    public void render(TileEntityLanternBehemoth lantern, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GlStateManager.disableCull();

        if(lantern.isBroken) {
            GlStateManager.rotate(5, 1, 0, 0);
            GlStateManager.rotate(10, 0, 0, 1);
        }

        bindTexture(ResourceManager.lantern_rusty_tex);
        ResourceManager.lantern.renderPart("Lantern");

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        boolean prevLighting = RenderUtil.isLightingEnabled();
        if (prevLighting) GlStateManager.disableLighting();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);

        if(lantern.isBroken) {
            float mult = (float) (Math.sin(System.currentTimeMillis() / 200D) / 2 + 0.5);
            GlStateManager.color(mult, 0, 0);
        } else {
            float mult = (float) (Math.sin(System.currentTimeMillis() / 200D) / 2 + 0.5) * 0.5F + 0.5F;
            GlStateManager.color(0, mult, 0);
        }
        ResourceManager.lantern.renderPart("Light");
        GlStateManager.color(1F, 1F, 1F);
        if (prevLighting) GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();

        GlStateManager.popMatrix();
    }
}
