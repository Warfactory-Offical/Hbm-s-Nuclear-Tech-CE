package com.leafia.shit.recipe_book_elements;

import com.hbm.main.ModEventHandlerClient;
import com.leafia.unsorted.recipe_book.LeafiaRecipeBookProfile;
import com.leafia.unsorted.recipe_book.system.LeafiaRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButtonToggle;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashSet;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class LeafiaRecipeBookTab extends GuiButtonToggle
{
    public static final Set<Item> dontAnimate = new HashSet<>();
    private final Item[] icon;
    private float animationTime;
    public final LeafiaRecipeBookProfile.RecipeCategory category;

    public LeafiaRecipeBookTab(int buttonId,LeafiaRecipeBookProfile.RecipeCategory category,CreativeTabs tab) {
        super(buttonId, 0, 0, 35, 27, false);
        this.category = category;
        this.icon = new Item[]{tab.createIcon().getItem()};
        this.initTextureValues(153, 2, 35, 0, LeafiaRecipeBook.RECIPE_BOOK);
    }
    public LeafiaRecipeBookTab(int buttonId,LeafiaRecipeBookProfile.RecipeCategory category,Item... items) {
        super(buttonId, 0, 0, 35, 27, false);
        this.category = category;
        this.icon = items;
        this.initTextureValues(153, 2, 35, 0, LeafiaRecipeBook.RECIPE_BOOK);
    }

    public void startAnimation(Item item) {
        if (!dontAnimate.contains(item)) {
            dontAnimate.add(item);
            this.animationTime = 15.0F;
        }
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
    {
        if (this.visible)
        {
            if (this.animationTime > 0.0F)
            {
                float f = 1.0F + 0.1F * (float)Math.sin((double)(this.animationTime / 15.0F * (float)Math.PI));
                GlStateManager.pushMatrix();
                GlStateManager.translate((float)(this.x + 8), (float)(this.y + 12), 0.0F);
                GlStateManager.scale(1.0F, f, 1.0F);
                GlStateManager.translate((float)(-(this.x + 8)), (float)(-(this.y + 12)), 0.0F);
            }

            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            mc.getTextureManager().bindTexture(this.resourceLocation);
            GlStateManager.disableDepth();
            int k = this.xTexStart;
            int i = this.yTexStart;

            if (this.stateTriggered)
            {
                k += this.xDiffTex;
            }

            if (this.hovered)
            {
                i += this.yDiffTex;
            }

            int j = this.x;

            if (this.stateTriggered)
            {
                j -= 2;
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexturedModalRect(j, this.y, k, i, this.width, this.height);
            GlStateManager.enableDepth();
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.disableLighting();
            this.renderIcon(mc.getRenderItem());
            GlStateManager.enableLighting();
            RenderHelper.disableStandardItemLighting();

            if (this.animationTime > 0.0F)
            {
                GlStateManager.popMatrix();
                this.animationTime -= partialTicks;
            }
        }
    }

    private void renderIcon(RenderItem p_193920_1_)
    {
        for (int i = 0; i < icon.length; i++) {
            float posMul = i/((icon.length-1)/2f)-1;
            if (icon.length == 1) posMul = 0;
            p_193920_1_.renderItemAndEffectIntoGUI(new ItemStack(icon[i]),this.x+(int)(Math.floor(9+posMul*6)),this.y+5);
        }
    }

    public boolean updateVisibility()
    {
        this.visible = false;
        for (Item item : this.category.recipes.keySet()) {
            /*if (ModEventHandlerClient.unlockedRecipes.contains(item)) {
                this.visible = true;
                break;
            }*/
        }
        return this.visible;
    }
}
