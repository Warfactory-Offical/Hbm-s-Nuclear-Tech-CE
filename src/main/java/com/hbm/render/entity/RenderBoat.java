package com.hbm.render.entity;

import com.hbm.blocks.ModBlocks;
import com.hbm.entity.projectile.EntityDuchessGambit;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.tileentity.IItemRendererProvider;
import com.hbm.util.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

@AutoRegister(factory = "FACTORY")
public class RenderBoat extends Render<EntityDuchessGambit> implements IItemRendererProvider {

    public static final IRenderFactory<EntityDuchessGambit> FACTORY = RenderBoat::new;

    protected RenderBoat(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityDuchessGambit entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        final boolean prevCull = RenderUtil.isCullEnabled();
        final boolean prevLighting = RenderUtil.isLightingEnabled();
        final boolean prevTex2D = RenderUtil.isTexture2DEnabled();
        if (!prevTex2D) GlStateManager.enableTexture2D();
        if (!prevCull) GlStateManager.enableCull();
        if (!prevLighting) GlStateManager.enableLighting();
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.translate(0F, 0F, -1.0F);
        bindTexture(ResourceManager.duchessgambit_tex);
        ResourceManager.duchessgambit.renderAll();
        if (!prevLighting) GlStateManager.disableLighting();
        if (!prevCull) GlStateManager.disableCull();
        if (!prevTex2D) GlStateManager.disableTexture2D();

        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.boat);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            @Override
            public void renderInventory() {
                GlStateManager.rotate(-90F, 0F, 1F, 0F);
                GlStateManager.translate(0F, 1F, 0F);
                GlStateManager.scale(1.75F, 1.75F, 1.75F);
            }

            @Override
            public void renderCommon() {
                GlStateManager.scale(0.5F, 0.5F, 0.5F);
                GlStateManager.translate(0F, 0F, -3F);
                bindTexture(ResourceManager.duchessgambit_tex);
                ResourceManager.duchessgambit.renderAll();
            }
        };
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityDuchessGambit entity) {
        return ResourceManager.duchessgambit_tex;
    }
}
