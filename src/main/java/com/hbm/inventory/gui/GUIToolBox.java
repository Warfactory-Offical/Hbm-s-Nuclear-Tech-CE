package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerToolBox;
import com.hbm.items.tool.ItemToolBox.InventoryToolBox;
import com.hbm.lib.RefStrings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUIToolBox extends GuiContainer {
    private final static ResourceLocation texture = new ResourceLocation(RefStrings.MODID, "textures/gui/gui_toolbox.png");
    private final InventoryToolBox inventory;

    public GUIToolBox(InventoryPlayer invPlayer, InventoryToolBox box) {
        super(new ContainerToolBox(invPlayer, box));
        this.inventory = box;

        this.xSize = 176;
        this.ySize = 211;
    }

    @Override
    public void drawScreen(int x, int y, float interp) {
        super.drawScreen(x, y, interp);
        renderHoveredToolTip(x, y);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = I18n.format("container.toolBox");

        if(inventory.hasCustomName()) {
            name = inventory.target.getDisplayName();
        }

        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 37, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float x, int y, int i) {
        drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}
