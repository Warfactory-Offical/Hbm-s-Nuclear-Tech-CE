package com.hbm.inventory.gui;

import com.hbm.tileentity.machine.TileEntityCrateBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

public class GUICrateBase<T extends TileEntityCrateBase, C extends Container> extends GuiContainer {

    protected final T diFurnace;
    private final ResourceLocation texture;

    GUICrateBase(T tileentity, C container, int xSize, int ySize, ResourceLocation texture) {
        super(container);
        diFurnace = tileentity;
        this.xSize = xSize;
        this.ySize = ySize;
        this.texture = texture;
    }

    static String combineTitle(String name, float percent) {
        String color;
        if (percent >= 100){
            color = TextFormatting.DARK_PURPLE.toString();
        } else if (percent >= 85) {
            color = TextFormatting.RED.toString();
        } else if (percent >= 50) {
            color = TextFormatting.GOLD.toString();
        } else {
            color = TextFormatting.GREEN.toString();
        }
        String weightString = String.format(" %s(%.1f%%)", color, percent);
        return name + weightString;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    public void initGui() {
        super.initGui();
        if (mc.player != null) {
            diFurnace.openInventory(mc.player);
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if (mc.player != null) {
            diFurnace.closeInventory(mc.player);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.diFurnace.hasCustomName() ? this.diFurnace.getName() : I18n.format(this.diFurnace.getName());
        float percent = this.diFurnace.fillPercentage;
        String title = combineTitle(name, percent);
        this.fontRenderer.drawString(title, this.xSize / 2 - this.fontRenderer.getStringWidth(title) / 2, 6, 0x3F1515);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 44, this.ySize - 96 + 2, 0x3F1515);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}