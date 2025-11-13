package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.fusion.TileEntityFusionBreeder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderFusionBreeder extends TileEntitySpecialRenderer<TileEntityFusionBreeder>
        implements IItemRendererProvider {

    @Override
    public void render(
            TileEntityFusionBreeder breeder,
            double x,
            double y,
            double z,
            float partialTicks,
            int destroyStage,
            float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        switch (breeder.getBlockMetadata() - BlockDummyable.offset) {
		case 2: GlStateManager.rotate(90, 0F, 1F, 0F);
		case 4: GlStateManager.rotate(180, 0F, 1F, 0F);
		case 3: GlStateManager.rotate(270, 0F, 1F, 0F);
		case 5: GlStateManager.rotate(0, 0F, 1F, 0F);
		}

        bindTexture(ResourceManager.fusion_breeder_tex);
        ResourceManager.fusion_breeder.renderAll();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.popMatrix();
    }

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.fusion_breeder);
	}

	@Override
	public ItemRenderBase getRenderer(Item item) {
		return new ItemRenderBase() {
			public void renderInventory() {
				GL11.glTranslated(0, -3, 0);
				GL11.glScaled(5, 5, 5);
				GL11.glRotated(90, 0, 1, 0);
			}
			public void renderCommon() {
                GlStateManager.scale(0.5, 0.5, 0.5);
                GlStateManager.rotate(90, 0F, 1F, 0F);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
				bindTexture(ResourceManager.fusion_breeder_tex);
				ResourceManager.fusion_breeder.renderPart("Breeder");
                GlStateManager.shadeModel(GL11.GL_FLAT);
			}};
	}
}
