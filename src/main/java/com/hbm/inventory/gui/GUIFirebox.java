package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerFirebox;
import com.hbm.tileentity.machine.TileEntityFireboxBase;
import com.hbm.tileentity.machine.TileEntityHeaterOven;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

import java.util.List;

public class GUIFirebox extends GuiInfoContainer {
	
	private TileEntityFireboxBase firebox;
	private final ResourceLocation texture;

	public GUIFirebox(InventoryPlayer invPlayer, TileEntityFireboxBase tedf, ResourceLocation texture) {
		super(new ContainerFirebox(invPlayer, tedf));
		firebox = tedf;
		this.texture = texture;
		
		this.xSize = 176;
		this.ySize = 168;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float interp) {
		super.drawScreen(mouseX, mouseY, interp);
		if (this.mc.player.inventory.getItemStack().isEmpty()) {

			for (int i = 0; i < 2; ++i) {
				Slot slot = this.inventorySlots.inventorySlots.get(i);

				if (this.isMouseOverSlot(slot, mouseX, mouseY) && !slot.getHasStack()) {

					List<String> bonuses = this.firebox.getModule().getDesc();

					if (!bonuses.isEmpty()) {
						this.drawHoveringText(bonuses, mouseX, mouseY, this.fontRenderer);
					}
				}
			}
		}


		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 80, guiTop + 27, 71, 7, mouseX, mouseY, new String[] { String.format("%,d", firebox.heatEnergy) + " / " + String.format("%,d", firebox.getMaxHeat()) + "TU" });
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 80, guiTop + 36, 71, 7, mouseX, mouseY, new String[] { firebox.burnHeat + "TU/t", (firebox.burnTime / 20) + "s" });
		super.renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.firebox.hasCustomName() ? this.firebox.getName() : I18n.format(this.firebox.getName());

		int color = firebox instanceof TileEntityHeaterOven ? 0xffffff : 4210752;

		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, color);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		int i = firebox.heatEnergy * 69 / firebox.getMaxHeat();
		drawTexturedModalRect(guiLeft + 81, guiTop + 28, 176, 0, i, 5);
		
		int j = firebox.burnTime * 70 / Math.max(firebox.maxBurnTime, 1);
		drawTexturedModalRect(guiLeft + 81, guiTop + 37, 176, 5, j, 5);
		
		if(firebox.wasOn) {
			drawTexturedModalRect(guiLeft + 25, guiTop + 26, 176, 10, 18, 18);
		}
	}
}
