package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityStirling;
import com.hbm.wiaj.WorldInAJar;
import com.hbm.wiaj.actors.ITileActorRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

@AutoRegister
public class RenderStirling extends TileEntitySpecialRenderer<TileEntityStirling>
        implements IItemRendererProvider, ITileActorRenderer {

    @Override
    public void render(
            TileEntityStirling tile,
            double x,
            double y,
            double z,
            float interp,
            int destroyStage,
            float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GlStateManager.enableCull();

        switch (tile.getBlockMetadata() - BlockDummyable.offset) {
            case 3:
                GlStateManager.rotate(0, 0F, 1F, 0F);
                break;
            case 5:
                GlStateManager.rotate(90, 0F, 1F, 0F);
                break;
            case 2:
                GlStateManager.rotate(180, 0F, 1F, 0F);
                break;
            case 4:
                GlStateManager.rotate(270, 0F, 1F, 0F);
                break;
        }

        TileEntityStirling stirling = tile;

        float rot = stirling.lastSpin + (stirling.spin - stirling.lastSpin) * interp;
        renderCommon(rot, stirling.hasCog, stirling.getGeatMeta());

        GlStateManager.popMatrix();
    }

    private void renderCommon(float rot, boolean hasCog, int type) {

        if (type == 0) bindTexture(ResourceManager.stirling_tex);
        else if (type == 2) bindTexture(ResourceManager.stirling_creative_tex);
        else bindTexture(ResourceManager.stirling_steel_tex);

        ResourceManager.stirling.renderPart("Base");

        if (hasCog) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 1.375, 0);
            GlStateManager.rotate(-rot, 0, 0, 1);
            GlStateManager.translate(0, -1.375, 0);
            ResourceManager.stirling.renderPart("Cog");
            GlStateManager.popMatrix();
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 1.375, 0.25);
        GlStateManager.rotate(rot * 2 + 3, 1, 0, 0);
        GlStateManager.translate(0, -1.375, -0.25);
        ResourceManager.stirling.renderPart("CogSmall");
        GlStateManager.popMatrix();

        GlStateManager.translate(Math.sin(rot * Math.PI / 90D) * 0.25 + 0.125, 0, 0);
        ResourceManager.stirling.renderPart("Piston");
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.machine_stirling);
    }

    @Override
    public Item[] getItemsForRenderer() {
        return new Item[]{
                Item.getItemFromBlock(ModBlocks.machine_stirling),
                Item.getItemFromBlock(ModBlocks.machine_stirling_steel),
                Item.getItemFromBlock(ModBlocks.machine_stirling_creative)
        };
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.translate(0, -1.5, 0);
                GlStateManager.scale(3.25, 3.25, 3.25);
            }

            public void renderCommon(ItemStack item) {
                GlStateManager.rotate(90, 0F, 1F, 0F);
                boolean cog = item.getItemDamage() != 1;
                RenderStirling.this.renderCommon(
                        cog ? System.currentTimeMillis() % 3600 * 0.1F : 0,
                        cog,
                        item.getItem() == Item.getItemFromBlock(ModBlocks.machine_stirling)
                                ? 0
                                : item.getItem() == Item.getItemFromBlock(ModBlocks.machine_stirling_creative)
                                ? 2
                                : 1);
            }
        };
    }

    @Override
    public void renderActor(WorldInAJar world, int ticks, float interp, NBTTagCompound data) {
        double x = data.getDouble("x");
        double y = data.getDouble("y");
        double z = data.getDouble("z");
        int rotation = data.getInteger("rotation");
        int type = data.getInteger("type");
        boolean hasCog = data.getBoolean("hasCog");
        float lastSpin = data.getFloat("lastSpin");
        float spin = data.getFloat("spin");

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);

        switch (rotation) {
            case 3:
                GlStateManager.rotate(0, 0F, 1F, 0F);
                break;
            case 5:
                GlStateManager.rotate(90, 0F, 1F, 0F);
                break;
            case 2:
                GlStateManager.rotate(180, 0F, 1F, 0F);
                break;
            case 4:
                GlStateManager.rotate(270, 0F, 1F, 0F);
                break;
        }

        renderCommon(lastSpin + (spin - lastSpin) * interp, hasCog, type);

        GlStateManager.popMatrix();
    }

    @Override
    public void updateActor(int ticks, NBTTagCompound data) {

        float lastSpin = 0;
        float spin = data.getFloat("spin");
        float speed = data.getFloat("speed");

        lastSpin = spin;
        spin += speed;

        if (spin >= 360) {
            lastSpin -= 360;
            spin -= 360;
        }

        data.setFloat("lastSpin", lastSpin);
        data.setFloat("spin", spin);
    }
}
