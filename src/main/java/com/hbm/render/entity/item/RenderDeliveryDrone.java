package com.hbm.render.entity.item;

import com.hbm.entity.item.EntityDeliveryDrone;
import com.hbm.entity.item.EntityDroneBase;
import com.hbm.entity.item.EntityRequestDrone;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

@AutoRegister(entity = EntityRequestDrone.class, factory = "FACTORY_REQUEST")
@AutoRegister(entity = EntityDeliveryDrone.class, factory = "FACTORY_DELIVERY")
public class RenderDeliveryDrone extends Render<EntityDroneBase> {
    protected RenderDeliveryDrone(RenderManager renderManager) {
        super(renderManager);
    }

    public static final IRenderFactory<EntityRequestDrone> FACTORY_REQUEST = RenderDeliveryDrone::new;
    public static final IRenderFactory<EntityDeliveryDrone> FACTORY_DELIVERY = RenderDeliveryDrone::new;

    @Override
    public void doRender(EntityDroneBase entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        GlStateManager.disableCull();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        if(entity instanceof EntityRequestDrone) {
            bindTexture(ResourceManager.delivery_drone_request_tex);
        } else if(entity.getDataManager().get(EntityDroneBase.IS_EXPRESS))
            bindTexture(ResourceManager.delivery_drone_express_tex);
        else
            bindTexture(ResourceManager.delivery_drone_tex);
        ResourceManager.delivery_drone.renderPart("Drone");

        EntityDroneBase drone = entity;
        int style = drone.getAppearance();

        if(style == 1) ResourceManager.delivery_drone.renderPart("Crate");
        if(style == 2) ResourceManager.delivery_drone.renderPart("Barrel");

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableCull();

        GlStateManager.popMatrix();
    }

    @Override
    protected @Nullable ResourceLocation getEntityTexture(EntityDroneBase entity) {
        return ResourceManager.delivery_drone_tex;
    }
}
