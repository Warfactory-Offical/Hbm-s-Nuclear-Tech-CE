package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.network.TileEntityRadioTelex;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;

@AutoRegister
public class RenderTelex extends TileEntitySpecialRenderer<TileEntityRadioTelex> implements IItemRendererProvider {
    @Override
    public void render(TileEntityRadioTelex te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        GlStateManager.enableLighting();
        GlStateManager.disableCull();

        switch(te.getBlockMetadata() - BlockDummyable.offset) {
            case 2: GlStateManager.rotate(90, 0F, 1F, 0F); break;
            case 4: GlStateManager.rotate(180, 0F, 1F, 0F); break;
            case 3: GlStateManager.rotate(270, 0F, 1F, 0F); break;
            case 5: GlStateManager.rotate(0, 0F, 1F, 0F); break;
        }

        bindTexture(ResourceManager.telex_tex);
        ResourceManager.telex.renderAll();

        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.radio_telex);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.translate(0, -2, 0);
                GlStateManager.scale(6, 6, 6);
            }
            public void renderCommon() {
                GlStateManager.translate(0, 0, -0.5);
                bindTexture(ResourceManager.telex_tex);
                ResourceManager.telex.renderAll();
            }};
    }
}
