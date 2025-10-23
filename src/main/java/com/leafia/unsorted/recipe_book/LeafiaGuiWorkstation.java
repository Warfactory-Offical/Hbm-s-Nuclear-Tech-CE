package com.leafia.unsorted.recipe_book;

import com.hbm.inventory.gui.GuiInfoContainer;
import com.leafia.unsorted.recipe_book.system.LeafiaRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import java.io.IOException;

public abstract class LeafiaGuiWorkstation extends GuiInfoContainer {
    public LeafiaRecipeBook recipeBookGui;
    public boolean overlayMode = false;
    public LeafiaGuiWorkstation(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }
    public LeafiaRecipeBook createRecipeBook() {
        return new LeafiaRecipeBook(Minecraft.getMinecraft(),this.buttonList,this,-886859,getRecipeProfile(),inventorySlots);
    };
    public abstract LeafiaRecipeBookProfile getRecipeProfile();
    public void resetGuiPosition() {
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
    }
    public void offsetGuiForRecipeBook() { // Override this for custom positions
        this.resetGuiPosition();
        this.guiLeft += (recipeBookGui.isVisible() && !this.overlayMode) ? this.recipeBookGui.xOffset/2 : 0;
    }
    public void initGui() {
        super.initGui();
        this.createRecipeBook();
        this.overlayMode = this.width < this.getXSize()+203;
        this.recipeBookGui.initVisuals();
        ///this.guiLeft = (width-getXSize())/2 + getXSize()/2*(this.recipeBookGui.isVisible() ? 1 : 0); // spaghetti :D
    }
    public void updateScreen()
    {
        this.recipeBookGui.tick();
        super.updateScreen();
    }
    public void onGuiClosed()
    {
        this.recipeBookGui.removed();
        super.onGuiClosed();
    }
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (!this.recipeBookGui.keyPressed(typedChar, keyCode))
        {
            super.keyTyped(typedChar, keyCode);
        }
    }
    protected boolean isPointInRegion(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY)
    {
        return (!this.overlayMode || !this.recipeBookGui.isVisible()) && super.isPointInRegion(rectX, rectY, rectWidth, rectHeight, pointX, pointY);
    }
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if (!this.recipeBookGui.mouseClicked(mouseX, mouseY, mouseButton))
        {
            if (!this.overlayMode || !this.recipeBookGui.isVisible())
            {
                super.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }
    }
    protected boolean hasClickedOutside(int p_193983_1_, int p_193983_2_, int p_193983_3_, int p_193983_4_)
    {
        boolean flag = p_193983_1_ < p_193983_3_ || p_193983_2_ < p_193983_4_ || p_193983_1_ >= p_193983_3_ + this.xSize || p_193983_2_ >= p_193983_4_ + this.ySize;
        return this.recipeBookGui.hasClickedOutside(p_193983_1_, p_193983_2_, this.guiLeft, this.guiTop, this.xSize, this.ySize) && flag;
    }
    protected void handleMouseClick(Slot slotIn,int slotId,int mouseButton,ClickType type)
    {
        super.handleMouseClick(slotIn, slotId, mouseButton, type);
        this.recipeBookGui.slotClicked(slotIn);
    }
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == this.recipeBookGui.recipeButton.id)
        {
            //this.recipeBookGui.initVisuals(this.widthTooNarrow, ((ContainerWorkbench)this.inventorySlots).craftMatrix);
            this.recipeBookGui.toggleVisibility();
            this.recipeBookGui.initVisuals();
            //this.guiLeft = this.recipeBookGui.updateScreenPosition(this.widthTooNarrow, this.width, this.xSize);
            //this.recipeButton.setPosition(this.guiLeft + 5, this.height / 2 - 49);
        }
    }
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.overlayMode) {
            this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
            this.recipeBookGui.render(mouseX, mouseY, partialTicks);
        } else {
            super.drawScreen(mouseX, mouseY, partialTicks);
            this.recipeBookGui.render(mouseX, mouseY, partialTicks);
            this.recipeBookGui.renderGhostRecipe(this.guiLeft, this.guiTop, true, partialTicks);
        }
        this.renderHoveredToolTip(mouseX, mouseY);
        this.recipeBookGui.renderTooltip(this.guiLeft, this.guiTop, mouseX, mouseY);
    }
}
