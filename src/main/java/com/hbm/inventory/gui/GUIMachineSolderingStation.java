package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMachineSolderingStation;
import com.hbm.lib.RefStrings;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityMachineSolderingStation;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GUIMachineSolderingStation extends GuiInfoContainer {
  private static final ResourceLocation texture =
      new ResourceLocation(RefStrings.MODID + ":textures/gui/processing/gui_soldering_station.png");
  private final TileEntityMachineSolderingStation soldering_station;

  public GUIMachineSolderingStation(
      InventoryPlayer playerInv, TileEntityMachineSolderingStation tile) {
    super(new ContainerMachineSolderingStation(playerInv, tile));

    this.soldering_station = tile;
    this.xSize = 176;
    this.ySize = 204;
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    super.drawScreen(mouseX, mouseY, partialTicks);

    soldering_station.tank.renderTankInfo(this, mouseX, mouseY, guiLeft + 35, guiTop + 63, 34, 16);
    this.drawElectricityInfo(
        this,
        mouseX,
        mouseY,
        guiLeft + 152,
        guiTop + 18,
        16,
        52,
        soldering_station.getPower(),
        soldering_station.getMaxPower());

    this.drawCustomInfoStat(
        mouseX,
        mouseY,
        guiLeft + 78,
        guiTop + 67,
        8,
        8,
        guiLeft + 78,
        guiTop + 67,
        this.getUpgradeInfo(soldering_station));

    List<String> lines =
        new ArrayList<>(
            Arrays.asList(
                "Recipe Collision Prevention: "
                    + (soldering_station.collisionPrevention
                        ? ChatFormatting.GREEN + "ON"
                        : ChatFormatting.RED + "OFF"),
                "Prevents no-fluid recipes from being processed",
                "when fluid is present."));

    this.drawCustomInfoStat(
        mouseX, mouseY, guiLeft + 5, guiTop + 66, 10, 10, mouseX, mouseY, lines);
    super.renderHoveredToolTip(mouseX, mouseY);
  }

  @Override
  protected void mouseClicked(int x, int y, int i) throws IOException {
    super.mouseClicked(x, y, i);

    if (guiLeft + 5 <= x && guiLeft + 5 + 10 > x && guiTop + 66 < y && guiTop + 66 + 10 >= y) {
      playPressSound();
      NBTTagCompound data = new NBTTagCompound();
      data.setBoolean("collision", true);
      PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, soldering_station.getPos()));
    }
  }

  @Override
  protected void drawGuiContainerForegroundLayer(int i, int j) {
    this.fontRenderer.drawString(
        I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    super.drawDefaultBackground();
    GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
    drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

    int p = (int) (soldering_station.power * 52 / Math.max(soldering_station.maxPower, 1));
    drawTexturedModalRect(guiLeft + 152, guiTop + 70 - p, 176, 52 - p, 16, p);

    int i = soldering_station.progress * 33 / Math.max(soldering_station.processTime, 1);
    drawTexturedModalRect(guiLeft + 72, guiTop + 28, 192, 0, i, 14);

    if (soldering_station.power >= soldering_station.consumption) {
      drawTexturedModalRect(guiLeft + 156, guiTop + 4, 176, 52, 9, 12);
    }

    this.drawInfoPanel(guiLeft + 78, guiTop + 67, 8, 8, 8);
    soldering_station.tank.renderTank(guiLeft + 35, guiTop + 79, this.zLevel, 34, 16, 1);
  }
}
