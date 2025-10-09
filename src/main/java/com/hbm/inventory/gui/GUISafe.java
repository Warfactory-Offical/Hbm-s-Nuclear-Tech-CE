package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerSafe;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntitySafe;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUISafe extends GUICrateBase<TileEntitySafe, ContainerSafe> {

    private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/storage/gui_safe.png");

    public GUISafe(InventoryPlayer invPlayer, TileEntitySafe tedf) {
        super(tedf, new ContainerSafe(invPlayer, tedf), 176, 168, texture);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.diFurnace.hasCustomName() ? this.diFurnace.getName() : I18n.format(this.diFurnace.getName());
        float percent = this.diFurnace.fillPercentage;
        String title = combineTitle(name, percent);
        this.fontRenderer.drawString(title, this.xSize / 2 - this.fontRenderer.getStringWidth(title) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }
}
