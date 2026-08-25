package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerPneumoStorageExporter;
import com.hbm.tileentity.network.TileEntityPneumoStorageExporter;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GUIPneumoStorageExporter extends GuiInfoContainer {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MODID + ":textures/gui/storage/gui_pneumatic_exporter.png");

	private final TileEntityPneumoStorageExporter exporter;

	public GUIPneumoStorageExporter(InventoryPlayer invPlayer, TileEntityPneumoStorageExporter exporter) {
		super(new ContainerPneumoStorageExporter(invPlayer, exporter));
		this.exporter = exporter;

		this.xSize = 176;
		this.ySize = 185;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String name = this.exporter.getDisplayName().getUnformattedText();

		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 5, 4210752);
		this.fontRenderer.drawString(I18nUtil.resolveKey("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
	}
}
