package com.hbm.render.entity.missile;

import com.hbm.entity.missile.EntityMissileTier4;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.NTMRenderHelper;
import com.hbm.util.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
@AutoRegister(factory = "FACTORY")
public class RenderMissileDoomsday extends Render<EntityMissileTier4.EntityMissileDoomsday> {

	public static final IRenderFactory<EntityMissileTier4.EntityMissileDoomsday> FACTORY = (RenderManager man) -> {return new RenderMissileDoomsday(man);};
	
	protected RenderMissileDoomsday(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityMissileTier4.EntityMissileDoomsday missile, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
        boolean prevLighting = RenderUtil.isLightingEnabled();
        boolean prevCull = RenderUtil.isCullEnabled();
        if (!prevLighting) GlStateManager.enableLighting();
		double[] renderPos = NTMRenderHelper.getRenderPosFromMissile(missile, partialTicks);
		x = renderPos[0];
		y = renderPos[1];
		z = renderPos[2];
		GlStateManager.translate(x, y, z);
        GlStateManager.rotate(missile.prevRotationYaw + (missile.rotationYaw - missile.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(missile.prevRotationPitch + (missile.rotationPitch - missile.prevRotationPitch) * partialTicks, 0.0F, 0.0F, 1.0F);
        GlStateManager.scale(2F, 2F, 2F);

        if (prevCull) GlStateManager.disableCull();
        bindTexture(ResourceManager.missileDoomsday_tex);
        ResourceManager.missileDoomsday.renderAll();
        if (prevCull) GlStateManager.enableCull();
        if (!prevLighting) GlStateManager.disableLighting();
		GlStateManager.popMatrix();
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityMissileTier4.EntityMissileDoomsday entity) {
		return ResourceManager.missileDoomsday_tex;
	}

}
