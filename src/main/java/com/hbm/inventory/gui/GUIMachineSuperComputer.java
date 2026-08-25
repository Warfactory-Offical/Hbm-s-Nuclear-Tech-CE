package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerMachineSuperComputer;
import com.hbm.inventory.gui.element.GUIElements;
import com.hbm.inventory.recipes.SuperComputerRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.items.machine.ItemBlueprints;
import com.hbm.tileentity.machine.TileEntityMachineSuperComputer;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class GUIMachineSuperComputer extends GuiInfoContainer {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_supercomputer.png");

	private final TileEntityMachineSuperComputer computer;

	public GUIMachineSuperComputer(InventoryPlayer invPlayer, TileEntityMachineSuperComputer computer) {
		super(new ContainerMachineSuperComputer(invPlayer, computer));
		this.computer = computer;

		this.xSize = 176;
		this.ySize = 211;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);
		super.renderHoveredToolTip(mouseX, mouseY);

		computer.inputTank.renderTankInfo(this, mouseX, mouseY, guiLeft + 8, guiTop + 54, 52, 16);
		computer.outputTank.renderTankInfo(this, mouseX, mouseY, guiLeft + 80, guiTop + 54, 52, 16);

		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 152, guiTop + 18, 16, 61, computer.power, computer.maxPower);

		if(guiLeft + 7 <= mouseX && guiLeft + 7 + 18 > mouseX && guiTop + 80 < mouseY && guiTop + 80 + 18 >= mouseY) {
			GenericRecipe recipe = this.computer.computerModule.getRecipe();
			if(recipe != null) {
				GUIElements.drawHoveringTextRecipe(recipe.print(), mouseX, mouseY, this.fontRenderer, itemRender, this.width, this.height);
			} else {
				this.drawHoveringText(TextFormatting.YELLOW + I18nUtil.resolveKey("gui.recipe.setRecipe"), mouseX, mouseY);
			}
		}
	}

	@Override
	protected void mouseClicked(int x, int y, int button) throws IOException {
		super.mouseClicked(x, y, button);

		if(this.checkClick(x, y, 7, 80, 18, 18)) GUIScreenRecipeSelector.openSelector(SuperComputerRecipes.INSTANCE, computer, computer.computerModule.recipe, 0, ItemBlueprints.grabPool(computer.inventory.getStackInSlot(1)), this);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String name = this.computer.hasCustomName() ? this.computer.getName() : I18n.format(this.computer.getDefaultName());

		this.fontRenderer.drawString(name, 70 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawDefaultBackground();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int p = (int) (computer.power * 61 / computer.maxPower);
		drawTexturedModalRect(guiLeft + 152, guiTop + 79 - p, 176, 61 - p, 16, p);

		if(computer.computerModule.progress > 0) {
			int j = (int) Math.ceil(70 * computer.computerModule.progress);
			drawTexturedModalRect(guiLeft + 62, guiTop + 81, 176, 61, j, 16);
		}

		GenericRecipe recipe = computer.computerModule.getRecipe();

		/// LEFT LED
		if(computer.didProcess) {
			drawTexturedModalRect(guiLeft + 51, guiTop + 76, 195, 0, 3, 6);
		} else if(recipe != null) {
			drawTexturedModalRect(guiLeft + 51, guiTop + 76, 192, 0, 3, 6);
		}

		/// RIGHT LED
		if(computer.didProcess) {
			drawTexturedModalRect(guiLeft + 56, guiTop + 76, 195, 0, 3, 6);
		} else if(recipe != null && computer.power >= recipe.power) {
			drawTexturedModalRect(guiLeft + 56, guiTop + 76, 192, 0, 3, 6);
		}

		this.renderItem(recipe != null ? recipe.getIcon() : TEMPLATE_FOLDER, 8, 81);

		if(recipe != null && recipe.inputItem != null) {
			for(int i = 0; i < recipe.inputItem.length; i++) {
				Slot slot = (Slot) this.inventorySlots.inventorySlots.get(computer.computerModule.inputSlots[i]);
				if(!slot.getHasStack()) this.renderItem(recipe.inputItem[i].extractForCyclingDisplay(20), slot.xPos, slot.yPos, 10F);
			}

			Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glColor4f(1F, 1F, 1F, 0.5F);
			GL11.glEnable(GL11.GL_BLEND);
			this.zLevel = 300F;
			for(int i = 0; i < recipe.inputItem.length; i++) {
				Slot slot = (Slot) this.inventorySlots.inventorySlots.get(computer.computerModule.inputSlots[i]);
				if(!slot.getHasStack()) drawTexturedModalRect(guiLeft + slot.xPos, guiTop + slot.yPos, slot.xPos, slot.yPos, 16, 16);
			}
			this.zLevel = 0F;
			GL11.glColor4f(1F, 1F, 1F, 1F);
			GL11.glDisable(GL11.GL_BLEND);
		}

		computer.inputTank.renderTank(guiLeft + 8, guiTop + 70, this.zLevel, 52, 16, 1);
		computer.outputTank.renderTank(guiLeft + 80, guiTop + 70, this.zLevel, 52, 16, 1);
	}
}
