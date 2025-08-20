package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.HmfController;
import com.hbm.tileentity.machine.TileEntityMachineChemplant;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderChemplant extends TileEntitySpecialRenderer<TileEntityMachineChemplant> implements IItemRendererProvider {

    @Override
    public void render(
            TileEntityMachineChemplant te,
            double x,
            double y,
            double z,
            float partialTicks,
            int destroyStage,
            float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GlStateManager.disableCull();
        GlStateManager.rotate(180, 0F, 1F, 0F);
        switch (te.getBlockMetadata() - BlockDummyable.offset) {
            case 5 -> GlStateManager.rotate(180, 0F, 1F, 0F);
            case 2 -> GlStateManager.rotate(270, 0F, 1F, 0F);
            case 4 -> GlStateManager.rotate(0, 0F, 1F, 0F);
            case 3 -> GlStateManager.rotate(90, 0F, 1F, 0F);
        }

        GlStateManager.translate(-0.5D, 0.0D, 0.5D);

        bindTexture(ResourceManager.chemplant_body_tex);

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        ResourceManager.chemplant_body.renderAll();
        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.popMatrix();

        renderExtras(te, x, y, z, partialTicks);
    }

    public void renderExtras(TileEntity tileEntity, double x, double y, double z, float f) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GlStateManager.disableCull();
        GlStateManager.rotate(180, 0F, 1F, 0F);
        TileEntityMachineChemplant chem = (TileEntityMachineChemplant) tileEntity;
        switch (tileEntity.getBlockMetadata() - BlockDummyable.offset) {
            case 5 -> GlStateManager.rotate(180, 0F, 1F, 0F);
            case 2 -> GlStateManager.rotate(270, 0F, 1F, 0F);
            case 4 -> GlStateManager.rotate(0, 0F, 1F, 0F);
            case 3 -> GlStateManager.rotate(90, 0F, 1F, 0F);
        }

        GlStateManager.translate(-0.5D, 0.0D, 0.5D);

        bindTexture(ResourceManager.chemplant_spinner_tex);

        int rotation = (int) (System.currentTimeMillis() % (360 * 5)) / 5;

        GlStateManager.pushMatrix();
        GlStateManager.translate(-0.625, 0, 0.625);

        if (chem.tanksNew[0].getTankType() != Fluids.NONE && chem.isProgressing)
            GlStateManager.rotate(-rotation, 0F, 1F, 0F);
        else
            GlStateManager.rotate(-45, 0F, 1F, 0F);

        ResourceManager.chemplant_spinner.renderAll();
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.625, 0, 0.625);

        if (chem.tanksNew[1].getTankType() != Fluids.NONE && chem.isProgressing)
            GlStateManager.rotate(rotation, 0F, 1F, 0F);
        else
            GlStateManager.rotate(45, 0F, 1F, 0F);

        ResourceManager.chemplant_spinner.renderAll();
        GlStateManager.popMatrix();

        double push = Math.sin((System.currentTimeMillis() % 2000) / 1000D * Math.PI) * 0.25 - 0.25;

        bindTexture(ResourceManager.chemplant_piston_tex);

        GlStateManager.pushMatrix();

        if (chem.isProgressing)
            GlStateManager.translate(0, push, 0);
        else
            GlStateManager.translate(0, -0.25, 0);

        ResourceManager.chemplant_piston.renderAll();
        GlStateManager.popMatrix();

        bindTexture(ResourceManager.chemplant_fluid_tex);

        GlStateManager.disableLighting();
        if (chem.tanksNew[0].getTankType() != Fluids.NONE) {
            GlStateManager.pushMatrix();

            if (chem.isProgressing)
                HmfController.setMod(50000D, -250D);
            else
                HmfController.setMod(50000D, -50000D);

            int color = chem.tanksNew[0].getTankType().getColor();
            GlStateManager.color(
                    ((color >> 16) & 0xFF) / 255.0f,
                    ((color >> 8) & 0xFF) / 255.0f,
                    (color & 0xFF) / 255.0f);

            GlStateManager.translate(-0.625, 0, 0.625);

            int count = chem.tanksNew[0].getFill() * 16 / 24000;
            for (int i = 0; i < count; i++) {
                if (i < count - 1)
                    ResourceManager.chemplant_fluid.renderAll();
                else
                    ResourceManager.chemplant_fluidcap.renderAll();
                GlStateManager.translate(0, 0.125, 0);
            }
            GlStateManager.popMatrix();
        }

        if (chem.tanksNew[1].getTankType() != Fluids.NONE) {
            GlStateManager.pushMatrix();

            if (chem.isProgressing)
                HmfController.setMod(50000D, 250D);
            else
                HmfController.setMod(50000D, 50000D);

            int color = chem.tanksNew[1].getTankType().getColor();
            GlStateManager.color(
                    ((color >> 16) & 0xFF) / 255.0f,
                    ((color >> 8) & 0xFF) / 255.0f,
                    (color & 0xFF) / 255.0f);

            GlStateManager.translate(0.625, 0, 0.625);

            int count = chem.tanksNew[1].getFill() * 16 / 24000;
            for (int i = 0; i < count; i++) {
                if (i < count - 1)
                    ResourceManager.chemplant_fluid.renderAll();
                else
                    ResourceManager.chemplant_fluidcap.renderAll();
                GlStateManager.translate(0, 0.125, 0);
            }
            GlStateManager.popMatrix();
        }

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableLighting();

        HmfController.resetMod();

        GlStateManager.popMatrix();
    }


    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.machine_chemplant);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.translate(0, -2, 0);
                GlStateManager.scale(3.5, 3.5, 3.5);
            }

            public void renderCommon() {
                GlStateManager.disableCull();
                bindTexture(ResourceManager.chemplant_body_tex);
                ResourceManager.chemplant_body.renderAll();
                bindTexture(ResourceManager.chemplant_piston_tex);
                ResourceManager.chemplant_piston.renderAll();
                bindTexture(ResourceManager.chemplant_spinner_tex);
                GlStateManager.translate(-0.625, 0, 0.625);
                ResourceManager.chemplant_spinner.renderAll();
                GlStateManager.translate(1.25, 0, 0);
                ResourceManager.chemplant_spinner.renderAll();
                GlStateManager.enableCull();
            }
        };
    }
}
