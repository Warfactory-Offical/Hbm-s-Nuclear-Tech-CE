package com.leafia.shit.recipe_book_elements;

import com.leafia.unsorted.recipe_book.system.LeafiaDummyRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class LeafiaRecipeButton extends GuiButton
{
    private static final ResourceLocation RECIPE_BOOK = new ResourceLocation("textures/gui/recipe_book.png");
    private float time;
    private float animationTime;
    private int currentIndex;
    final int count;
    final Item item;
    public final List<LeafiaDummyRecipe> reciples;
    public int selectedRecipe = -1;
    boolean containsCraftable = false;
    public LeafiaRecipeButton(List<LeafiaDummyRecipe> reciples,int count,Item item) {
        super(0, 0, 0, 25, 25, "");
        this.reciples = reciples;
        this.item = item;
        this.count = count;
        for (LeafiaDummyRecipe recipe : reciples) {
            if (recipe.isAvailable) {
                containsCraftable = true;
                break;
            }
        }
        init();
    }

    public void init()
    {
        if (!LeafiaRecipeBookTab.dontAnimate.contains(item)) {
            this.animationTime = 15.0F;
        }
    }

    public void setPosition(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
    {
        if (this.visible)
        {
            if (!GuiScreen.isCtrlKeyDown())
            {
                this.time += partialTicks;
            }

            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            RenderHelper.enableGUIStandardItemLighting();
            mc.getTextureManager().bindTexture(RECIPE_BOOK);
            GlStateManager.disableLighting();
            int i = 29;

            if (!containsCraftable)
            {
                i += 25;
            }

            int j = 206;

            if (reciples.size() > 1)
            {
                j += 25;
            }

            boolean flag = this.animationTime > 0.0F;

            if (flag)
            {
                float f = 1.0F + 0.1F * (float)Math.sin((double)(this.animationTime / 15.0F * (float)Math.PI));
                GlStateManager.pushMatrix();
                GlStateManager.translate((float)(this.x + 8), (float)(this.y + 12), 0.0F);
                GlStateManager.scale(f, f, 1.0F);
                GlStateManager.translate((float)(-(this.x + 8)), (float)(-(this.y + 12)), 0.0F);
                this.animationTime -= partialTicks;
                if (this.animationTime <= 0) {
                    LeafiaRecipeBookTab.dontAnimate.add(item);
                }
            }

            this.drawTexturedModalRect(this.x, this.y, i, j, this.width, this.height);
            /*
            //List<IRecipe> list = this.getOrderedRecipes(); reciples
            this.currentIndex = MathHelper.floor(this.time / 30.0F) % list.size();
            ItemStack itemstack = ((IRecipe)list.get(this.currentIndex)).getRecipeOutput();

            if (this.list.hasSingleResultItem() && this.getOrderedRecipes().size() > 1)
            {
                mc.getRenderItem().renderItemAndEffectIntoGUI(itemstack, this.x + k + 1, this.y + k + 1);
                --k; ?
            }
*/
            int k = 4;
            mc.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(item,count),this.x + k,this.y + k);
            mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer,new ItemStack(item,count),this.x + k,this.y + k,null);

            if (flag)
            {
                GlStateManager.popMatrix();
            }

            GlStateManager.enableLighting();
            RenderHelper.disableStandardItemLighting();
        }
    }
    public List<String> getToolTipText(GuiScreen currentScreen)
    {
        ItemStack itemstack = new ItemStack(item,count);
        List<String> list = new ArrayList<>();//currentScreen.getItemToolTip(itemstack);
        list.add(itemstack.getDisplayName());
        list.add(TextFormatting.DARK_GRAY+"Left Click to insert items "+TextFormatting.RED+"(EXPERIMENTAL!)");
        list.add(TextFormatting.DARK_GRAY+"Right Click to cycle recipes from below");
        for (int i = 0; i < reciples.size(); i++)
            list.add(((i == selectedRecipe) ? TextFormatting.YELLOW : TextFormatting.DARK_GRAY)+"-> Recipe #"+i);
        /*
        if (this.list.getRecipes(this.book.isFilteringCraftable()).size() > 1)
        {
            list.add(I18n.format("gui.recipebook.moreRecipes"));
        }
        */
        return list;
    }

    public int getButtonWidth()
    {
        return 25;
    }
}
