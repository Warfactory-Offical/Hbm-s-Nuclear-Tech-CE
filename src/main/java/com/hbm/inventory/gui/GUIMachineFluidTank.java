package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMachineFluidTank;
import com.hbm.lib.RefStrings;
import com.hbm.packet.toserver.AuxButtonPacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.tileentity.machine.TileEntityMachineFluidTank;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class GUIMachineFluidTank extends GuiInfoContainer {

	private static ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/storage/gui_tank.png");
	private TileEntityMachineFluidTank tank;

	public GUIMachineFluidTank(InventoryPlayer invPlayer, TileEntityMachineFluidTank tedf) {
		super(new ContainerMachineFluidTank(invPlayer, tedf));
		tank = tedf;

		this.xSize = 176;
		this.ySize = 166;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		// tank.tank.renderTankInfo(this, mouseX, mouseY, guiLeft + 71, guiTop +
		// 69 - 52, 34, 52);
		this.renderTankInfo(mouseX, mouseY, guiLeft + 71, guiTop + 69 - 52, 34, 52);

		String[] text = new String[] { "Inserting a fuse into the marked", "slot will set the tank to output mode" };
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft - 16, guiTop + 36, 16, 16, guiLeft - 8, guiTop + 36 + 16, text);
		super.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void mouseClicked(int x, int y, int mouseButton) throws IOException {
		super.mouseClicked(x, y, mouseButton);
		if(guiLeft + 151 <= x && guiLeft + 151 + 18 > x && guiTop + 35 < y && guiTop + 35 + 18 >= y) {
			mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    		PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(tank.getPos(), 0, 0));
    	}
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.tank.hasCustomName() ? this.tank.getName() : I18n.format(this.tank.getName());

		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);

	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int i = tank.mode;
		drawTexturedModalRect(guiLeft + 151, guiTop + 34, 176, i * 18, 18, 18);

		tank.tank.renderTank(guiLeft + 71, guiTop + 69, this.zLevel, 34, 52);
		this.mc.getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft + 71, guiTop + 17, 0, 204, 34, 52);

		// Minecraft.getMinecraft().getTextureManager().bindTexture(tank.tank.getSheet());
		// tank.tank.renderTank(this, guiLeft + 71, guiTop + 69,
		// tank.tank.getTankType().textureX() * FluidTank.x,
		// tank.tank.getTankType().textureY() * FluidTank.y, 16, 52);
		// tank.tank.renderTank(this, guiLeft + 71 + 16, guiTop + 69,
		// tank.tank.getTankType().textureX() * FluidTank.x,
		// tank.tank.getTankType().textureY() * FluidTank.y, 16, 52);
		// tank.tank.renderTank(this, guiLeft + 71 + 32, guiTop + 69,
		// tank.tank.getTankType().textureX() * FluidTank.x,
		// tank.tank.getTankType().textureY() * FluidTank.y, 2, 52);
		GL11.glPopAttrib();
	}


	public void renderTankInfo(int mouseX, int mouseY, int x, int y, int width, int height) {
		if(x <= mouseX && x + width > mouseX && y < mouseY && y + height >= mouseY) {
			/*if(tank.tank.getFluid() != null) {
				this.drawFluidInfo(new String[] { I18n.format(tank.tank.getInfo().fluid.getTranslationKey()), tank.tank.getFluidAmount() + "/" + tank.tank.getCapacity() + "mB" }, mouseX, mouseY);
			} else {
				this.drawFluidInfo(new String[] { I18n.format("None"), "0/" + tank.tank.getCapacity() + "mB" }, mouseX, mouseY);
			}*/

			tank.tank.renderTankInfo(this, mouseX, mouseY, x, y, width, height);

		}
	}
}
