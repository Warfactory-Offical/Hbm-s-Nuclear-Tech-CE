package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerRadioThermal;
import com.hbm.tileentity.machine.TileEntityHeaterRadioThermal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

public class GUIRadioThermal extends GuiInfoContainer {

	private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/machine/gui_heater_rtg.png");
	private TileEntityHeaterRadioThermal heater;
	
	public GUIRadioThermal(InventoryPlayer invPlayer, TileEntityHeaterRadioThermal tedf) {
		super(new ContainerRadioThermal(invPlayer, tedf));
		heater = tedf;
		
		this.xSize = 176;
		this.ySize = 176;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 152, guiTop + 69 + 4 - 52, 16, 52, mouseX, mouseY, new String[]{String.format("%,d", Math.min(heater.heatEnergy, TileEntityHeaterRadioThermal.maxHeatEnergy)) + " / " + String.format("%,d", TileEntityHeaterRadioThermal.maxHeatEnergy) + " TU"});
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 134, guiTop + 69 + 4 - 52, 16, 52, mouseX, mouseY, new String[]{String.format("%,d", heater.heatGen) + " TU/t"});
		
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerForegroundLayer( int i, int j) {
		String name = this.heater.hasCustomName() ? this.heater.getName() : I18n.format(this.heater.getName());
		
		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		if(heater.hasHeatGen()){
			int i = heater.getHeatGenScaled(52);
			drawTexturedModalRect(guiLeft + 134, guiTop + 74 - i, 176, 52 - i, 16, i);
		}
		if(heater.hasHeat()){
			int i = (int)heater.getHeatScaled(52);
			drawTexturedModalRect(guiLeft + 152, guiTop + 74 - i, 192, 52 - i, 16, i);
		}
	}
}
