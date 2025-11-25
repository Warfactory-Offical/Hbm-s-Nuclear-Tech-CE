package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.tileentity.network.TileEntityRadioTorchBase;
import com.hbm.tileentity.network.TileEntityRadioTorchSender;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Arrays;

public class GUIScreenRadioTorch extends GuiScreen {

	protected ResourceLocation texture;
	protected static final ResourceLocation textureSender = new ResourceLocation(Tags.MODID + ":textures/gui/machine/gui_rtty_sender.png");
	protected static final ResourceLocation textureReceiver = new ResourceLocation(Tags.MODID + ":textures/gui/machine/gui_rtty_receiver.png");
	protected TileEntityRadioTorchBase radio;
	protected static boolean isSender;
	protected String title = "";
	protected int xSize = 256;
	protected int ySize = 204;
	protected int guiLeft;
	protected int guiTop;
	protected GuiTextField frequency;
	protected GuiTextField[] remap;

	protected static final int bluTextColor = 0x32D6FF;
	protected static final int bluTextColorDarker = 0x00C8FF;
	protected static final int oraTextColor = 0xFF9232;
	protected static final int oraTextColorDarker = 0xFF7A00;
	
	public GUIScreenRadioTorch(TileEntityRadioTorchBase radio) {
		this.radio = radio;
		
		if(radio instanceof TileEntityRadioTorchSender) {
			this.texture = textureSender;
			this.title = "container.rttySender";
			this.isSender = true;
		} else {
			this.texture = textureReceiver;
			this.title = "container.rttyReceiver";
			this.isSender = false;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;

		Keyboard.enableRepeatEvents(true);
		
		int oX = 4;
		int oY = 4;
		int in = radio instanceof TileEntityRadioTorchSender ? 18 : 0;

		this.frequency = new GuiTextField(0, this.fontRenderer, guiLeft + 25 + oX, guiTop + 18 + oY, 90 - oX * 2, 14);
		this.frequency.setTextColor(this.isSender ? bluTextColor : oraTextColor);
		this.frequency.setDisabledTextColour(this.isSender ? bluTextColorDarker : oraTextColorDarker);
		this.frequency.setEnableBackgroundDrawing(false);
		this.frequency.setMaxStringLength(10);
		this.frequency.setText(radio.channel == null ? "" : radio.channel);
		
		this.remap = new GuiTextField[16];
		
		for(int i = 0; i < 16; i++) {
			this.remap[i] = new GuiTextField(i+1, this.fontRenderer, guiLeft + 7 + (130 * (i / 8)) + oX + in, guiTop + 54 + (18 * (i % 8)) + oY, 90 - oX * 2, 14);
			this.remap[i].setTextColor(this.isSender ? oraTextColor : bluTextColor);
			this.remap[i].setDisabledTextColour(this.isSender ? oraTextColorDarker : bluTextColorDarker);
			this.remap[i].setEnableBackgroundDrawing(false);
			this.remap[i].setMaxStringLength(15);
			this.remap[i].setText(radio.mapping[i] == null ? "" : radio.mapping[i]);
		}
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	public void drawCustomInfoStat(int mouseX, int mouseY, int x, int y, int width, int height, int tPosX, int tPosY, String[] text) {
		if(x <= mouseX && x + width > mouseX && y < mouseY && y + height >= mouseY){
			this.drawHoveringText(Arrays.asList(text), tPosX, tPosY);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		this.drawDefaultBackground();
        this.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);
        GlStateManager.enableLighting();
        GlStateManager.disableLighting();
        this.drawGuiContainerForegroundLayer(mouseX, mouseY);
        GlStateManager.enableLighting();
		if(radio != null) {
			this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 137, guiTop + 17, 18, 18, mouseX, mouseY, new String[] { radio.customMap ? "Custom Mapping" : "Redstone Passthrough" });
			this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 173, guiTop + 17, 18, 18, mouseX, mouseY, new String[] { radio.polling ? "Polling" : "State Change" });
		}
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 209, guiTop + 17, 18, 18, mouseX, mouseY, new String[] { "Save Settings" });
	}


	private void drawGuiContainerForegroundLayer(int x, int y) {
		String name = I18nUtil.resolveKey(this.title);
		this.fontRenderer.drawString(name, this.guiLeft + this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, this.guiTop + 6, this.isSender ? 4210752 : 0xE9E9E9);
	}

	private void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);

		if(radio.customMap) {
			drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
			drawTexturedModalRect(guiLeft + 137, guiTop + 17, 0, 204, 18, 18);
			for(int j = 0; j < 16; j++) {
				this.remap[j].drawTextBox();
			}
		} else {
			drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, 35);
			drawTexturedModalRect(guiLeft, guiTop + 35, 0, 197, xSize, 7);
		}

		if(radio.polling) {
			drawTexturedModalRect(guiLeft + 173, guiTop + 17, 0, 222, 18, 18);
		}
		
		this.frequency.drawTextBox();
	}

	@Override
	protected void mouseClicked(int x, int y, int i) throws IOException {
		super.mouseClicked(x, y, i);
		
		this.frequency.mouseClicked(x, y, i);
		
		if(radio.customMap) {
			for(int j = 0; j < 16; j++) {
				this.remap[j].mouseClicked(x, y, i);
			}
		}
		
		if(guiLeft + 137 <= x && guiLeft + 137 + 18 > x && guiTop + 17 < y && guiTop + 17 + 18 >= y) {
			mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			NBTTagCompound data = new NBTTagCompound();
			data.setBoolean("hasMapping", !radio.customMap);
			PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, radio.getPos()));
		}
		
		if(guiLeft + 173 <= x && guiLeft + 173 + 18 > x && guiTop + 17 < y && guiTop + 17 + 18 >= y) {
			mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			NBTTagCompound data = new NBTTagCompound();
			data.setBoolean("isPolling", !radio.polling);
			PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, radio.getPos()));
		}
		
		if(guiLeft + 209 <= x && guiLeft + 209 + 18 > x && guiTop + 17 < y && guiTop + 17 + 18 >= y) {
			mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			NBTTagCompound data = new NBTTagCompound();
			data.setString("channel", this.frequency.getText());
			for(int j = 0; j < 16; j++) {
				data.setString("mapping" + j, this.remap[j].getText());
			}
			PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, radio.getPos()));
		}
	}

	@Override
	protected void keyTyped(char c, int key) throws IOException {
		
		if(this.frequency.textboxKeyTyped(c, key)){
			return;
		}

		if(radio.customMap) {
			for(int j = 0; j < 16; j++) {
				if(this.remap[j].textboxKeyTyped(c, key)) 
					return;
			}
		}
		
		if(key == 1 || key == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
			this.mc.player.closeScreen();
		}
		super.keyTyped(c, key);
	}
}
