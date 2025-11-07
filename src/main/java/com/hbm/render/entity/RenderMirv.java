package com.hbm.render.entity;

import com.hbm.entity.missile.EntityMIRV;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.RefStrings;
import com.hbm.render.loader.IModelCustom;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
@AutoRegister(factory = "FACTORY")
public class RenderMirv extends Render<EntityMIRV> {

	public static final IRenderFactory<EntityMIRV> FACTORY = (RenderManager man) -> {return new RenderMirv(man);};
	
	private static final ResourceLocation objTesterModelRL = new ResourceLocation(/*"/assets/" + */RefStrings.MODID, "models/Mirv.obj");
	private IModelCustom boyModel;
    private ResourceLocation boyTexture;
	
	protected RenderMirv(RenderManager renderManager) {
		super(renderManager);
		boyModel = new HFRWavefrontObject(objTesterModelRL);
		boyTexture = new ResourceLocation(RefStrings.MODID, "textures/models/misc/universaldark.png");
	}

	@Override
	public void doRender(EntityMIRV entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
        GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 0.0F, 0.0F, 1.0F);
        
        bindTexture(boyTexture);
        boyModel.renderAll();
		GlStateManager.popMatrix();
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityMIRV entity) {
		return boyTexture;
	}

}
