package com.leafia.unsorted.recipe_book.system;

import com.hbm.interfaces.Spaghetti;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.lib.RefStrings;
import com.hbm.main.ModEventHandlerClient;
import com.hbm.packet.PacketDispatcher;
import com.hbm.util.I18nUtil;
import com.hbm.util.Tuple;
import com.hbm.util.Tuple.Pair;
import com.leafia.shit.recipe_book_elements.LeafiaRecipeBookTab;
import com.leafia.shit.recipe_book_elements.LeafiaRecipeButton;
import com.leafia.unsorted.recipe_book.LeafiaGuiWorkstation;
import com.leafia.unsorted.recipe_book.LeafiaRecipeBookProfile;
import com.llib.math.range.RangeInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.recipebook.GhostRecipe;
import net.minecraft.client.gui.recipebook.IRecipeUpdateListener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.play.client.CPacketRecipeInfo;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.*;

@SideOnly(Side.CLIENT)
public class LeafiaRecipeBook extends Gui implements IRecipeUpdateListener
{
    private static final ResourceLocation CRAFTING_TABLE_GUI_TEXTURES = new ResourceLocation("textures/gui/container/crafting_table.png");
    public GuiButtonImage recipeButton;
    public static final ResourceLocation RECIPE_BOOK = new ResourceLocation("textures/gui/recipe_book.png");
    protected static final ResourceLocation FILTER_ICONS = new ResourceLocation(RefStrings.MODID+":textures/gui/book/recipe_book.png");
    public int xOffset;
    private int width;
    private int height;
    private final GhostRecipe ghostRecipe = new GhostRecipe();
    private final List<LeafiaRecipeBookTab> recipeTabs; //Lists.newArrayList(new GuiButtonRecipeTab(0, CreativeTabs.SEARCH), new GuiButtonRecipeTab(0, CreativeTabs.TOOLS), new GuiButtonRecipeTab(0, CreativeTabs.BUILDING_BLOCKS), new GuiButtonRecipeTab(0, CreativeTabs.MISC), new GuiButtonRecipeTab(0, CreativeTabs.REDSTONE));
    private LeafiaRecipeBookTab currentTab;
    private GuiButtonToggle toggleRecipesBtn;
    //private InventoryCrafting craftingSlots;
    private Minecraft mc;
    private GuiTextField searchBar;
    private String lastSearch = "";
    private RecipeBook recipeBook;
    //private final RecipeBookPage recipeBookPage = new RecipeBookPage(); YOU KNOW WHAT
    private RecipeItemHelper stackedContents = new RecipeItemHelper();
    private int timesInventoryChanged;

    LeafiaGuiWorkstation boundGui;
    public int bookButtonX;
    public int bookButtonY;
    short filterIconIndex;
    final LeafiaRecipeBookProfile profile;

    private GuiButtonToggle forwardButton;
    private GuiButtonToggle backButton;
    final Container container;
    boolean isFirst = true;
    public LeafiaRecipeBook(Minecraft minecraft,List<GuiButton> buttonList,LeafiaGuiWorkstation gui,int buttonID,LeafiaRecipeBookProfile profile,Container container) {
        gui.recipeBookGui = this;
        this.xOffset = 147;//boundGui.getXSize();
        this.boundGui = gui;
        this.mc = minecraft;
        this.recipeButton = new GuiButtonImage(buttonID,0,0, 20, 18, 0, 168, 19, CRAFTING_TABLE_GUI_TEXTURES);
        buttonList.add(this.recipeButton);
        this.profile = profile;
        this.recipeTabs = new ArrayList<>();
        this.recipeTabs.add(new LeafiaRecipeBookTab(LeafiaRecipeBookProfile.RecipeCategory.ID,null,Items.COMPASS));
        for (LeafiaRecipeBookProfile.RecipeCategory category : profile.getCategories()) {
            this.recipeTabs.add(category.tab);
        }
        this.forwardButton = new GuiButtonToggle(0, 0, 0, 12, 17, false);
        this.forwardButton.initTextureValues(1, 208, 13, 18, RECIPE_BOOK);
        this.backButton = new GuiButtonToggle(0, 0, 0, 12, 17, true);
        this.backButton.initTextureValues(1, 208, 13, 18, RECIPE_BOOK);
        this.container = container;
    }
    public LeafiaRecipeBook updateBookPosition() {
        this.recipeButton.setPosition(this.boundGui.getGuiLeft()+this.bookButtonX,this.boundGui.getGuiTop()+this.bookButtonY);
        return this;
    }
    public LeafiaRecipeBook setup(int filterIconIndex,int x,int y)//, boolean p_194303_4_, InventoryCrafting p_194303_5_)
    {
        this.bookButtonX = x;
        this.bookButtonY = y;
        this.filterIconIndex = (short)filterIconIndex;
        this.width = boundGui.width;
        this.height = boundGui.height;
        //this.craftingSlots = p_194303_5_;
        this.recipeBook = this.mc.player.getRecipeBook();
        this.timesInventoryChanged = this.mc.player.inventory.getTimesChanged();
        this.currentTab = this.recipeTabs.get(0);
        this.currentTab.setStateTriggered(true);

        //if (this.isVisible())
        //{
        //    this.initVisuals(p_194303_4_, p_194303_5_);
        //}
        this.initVisuals();

        Keyboard.enableRepeatEvents(true);
        return this;
    }
    public int getActualX() { return (boundGui.overlayMode ? (this.width-147)/2 : boundGui.getGuiLeft()-this.xOffset); }
    public int getActualY() { return boundGui.getGuiTop(); }
    public void initVisuals()//, InventoryCrafting p_193014_2_)
    {
        boundGui.offsetGuiForRecipeBook();
        updateBookPosition();
        int i = getActualX();
        int j = getActualY();
        this.stackedContents.clear();
        this.mc.player.inventory.fillStackedContents(this.stackedContents, false);
        //p_193014_2_.fillStackedContents(this.stackedContents);
        this.searchBar = new GuiTextField(0, this.mc.fontRenderer, i + 25, j + 14, 80, this.mc.fontRenderer.FONT_HEIGHT + 5);
        this.searchBar.setMaxStringLength(50);
        this.searchBar.setEnableBackgroundDrawing(false);
        this.searchBar.setVisible(true);
        this.searchBar.setTextColor(16777215);
        //this.recipeBookPage.init(this.mc, i, j);
        this.toggleRecipesBtn = new GuiButtonToggle(0, i + 110, j + 12, 26, 16, this.recipeBook.isFilteringCraftable());
        this.toggleRecipesBtn.initTextureValues(0, 0, 26, 16, FILTER_ICONS);
        this.updateCollections(false);
        this.updateTabs();
    }

    public void removed()
    {
        Keyboard.enableRepeatEvents(false);
    }

    public void toggleVisibility()
    {
        this.setVisible(!this.isVisible());
    }

    public boolean isVisible()
    {
        return this.recipeBook.isGuiOpen();
    }

    private void setVisible(boolean p_193006_1_)
    {
        this.recipeBook.setGuiOpen(p_193006_1_);

        if (!p_193006_1_)
        {
            //this.recipeBookPage.setInvisible();
        }

        this.sendUpdateSettings();
    }

    public void slotClicked(@Nullable Slot slotIn)
    {
        if (slotIn != null && slotIn.slotNumber <= 9)
        {
            this.ghostRecipe.clear();

            if (this.isVisible())
            {
                this.updateStackedContents();
            }
        }
    }
    public static boolean compareSlotToAStack(Slot slot,AStack item) {
        if (slot.getHasStack()) {
            if (item instanceof OreDictStack) {
                for (ItemStack stack : ((OreDictStack) item).toStacks()) {
                    if (slot.getStack().getItem().equals(stack.getItem())) {
                        return true;
                    }
                }
            } else {
                if (slot.getStack().getItem().equals(item.getStack().getItem()))
                    return true;
            }
        }
        return false;
    }
    public boolean hasItems(AStack[] items) {
        for (AStack item : items) {
            int required = item.count();
            for (Slot slot : this.container.inventorySlots) {
                if (compareSlotToAStack(slot,item))
                    required -= slot.getStack().getCount();
            }
            if (required > 0)
                return false;
        }
        return true;
    }
    int page = 0;
    int maxPages = 0;
    List<LeafiaRecipeButton> shownButtons = new ArrayList<>();
    @Spaghetti("TOP 10 PAINFUL ARGUMENT TYPES vv")
    private void iterateRecipes(Map<Item,Map<Integer,List<LeafiaDummyRecipe>>> recipes,List<Pair<Pair<Item,Integer>,List<LeafiaDummyRecipe>>> filteredArray) {

    }
    private void updateCollections(boolean regenerateTabs)
    {
    }

    private void updateTabs()
    {
        int i = this.getActualX() - 30;
        int j = this.getActualY() + 3;
        int k = 27;
        int l = 0;

        for (LeafiaRecipeBookTab tab : this.recipeTabs)
        {
            if (tab.category == null) {
                tab.visible = true;
                tab.setPosition(i, j + 27 * l++);
            } else if (tab.updateVisibility()) {
                //tab.startAnimation();
                tab.setPosition(i, j + 27 * l++);
            }
        }
    }

    public void tick()
    {
        if (this.isVisible())
        {
            if (this.timesInventoryChanged != this.mc.player.inventory.getTimesChanged())
            {
                this.updateStackedContents();
                this.timesInventoryChanged = this.mc.player.inventory.getTimesChanged();
            }
        }
    }

    private void updateStackedContents()
    {
        this.stackedContents.clear();
        this.mc.player.inventory.fillStackedContents(this.stackedContents, false);
        //this.craftingSlots.fillStackedContents(this.stackedContents);
        this.updateCollections(false);
    }

    public void render(int mouseX, int mouseY, float partialTicks)
    {
        if (this.isVisible())
        {
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, 0.0F, 100.0F);
            this.mc.getTextureManager().bindTexture(RECIPE_BOOK);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            int i = this.getActualX();
            int j = this.getActualY();
            this.drawTexturedModalRect(i, j, 1, 1, 147, 166);
            this.searchBar.drawTextBox();
            RenderHelper.disableStandardItemLighting();

            for (LeafiaRecipeBookTab tab : this.recipeTabs)
            {
                tab.drawButton(this.mc, mouseX, mouseY, partialTicks);
            }
            this.mc.getTextureManager().bindTexture(FILTER_ICONS);
            this.toggleRecipesBtn.drawButton(this.mc, mouseX, mouseY, partialTicks);
            this.drawTexturedModalRect(this.toggleRecipesBtn.x+11,this.toggleRecipesBtn.y+1,Math.floorMod(filterIconIndex,100)*14,Math.floorDiv(filterIconIndex,100)*14+3*14,14,14);

            this.mc.getTextureManager().bindTexture(RECIPE_BOOK);

            for (LeafiaRecipeButton button : this.shownButtons) {
                button.drawButton(this.mc,mouseX,mouseY,partialTicks);
            }

            this.forwardButton.drawButton(this.mc,mouseX,mouseY,partialTicks);
            this.backButton.drawButton(this.mc,mouseX,mouseY,partialTicks);
            if (this.maxPages > 0)//(this.totalPages > 1)
            {
                String s = (this.page+1)+"/"+(this.maxPages+1);
                int stringWidth = this.mc.fontRenderer.getStringWidth(s);
                this.mc.fontRenderer.drawString(s,getActualX()-stringWidth/2+73,getActualY()+141,-1);
            }
            //this.recipeBookPage.render(i, j, mouseX, mouseY, partialTicks);
            GlStateManager.popMatrix();
        }
    }

    public void renderTooltip(int guiLeft, int guiTop, int mouseX, int mouseY)
    {
        if (this.isVisible())
        {
            //this.recipeBookPage.renderTooltip(mouseX, mouseY);
            if (this.mc.currentScreen != null) {
                if (this.toggleRecipesBtn.isMouseOver()) {
                    String s1 = I18n.format(this.toggleRecipesBtn.isStateTriggered() ? "gui.recipebook.toggleRecipes.craftable" : "gui.recipebook.toggleRecipes.all");
                    this.mc.currentScreen.drawHoveringText(s1,mouseX,mouseY);
                }
                for (LeafiaRecipeBookTab tab : recipeTabs) {
                    if (tab.isMouseOver()) {
                        if (tab.category == null)
                            this.mc.currentScreen.drawHoveringText("All Categories",mouseX,mouseY);
                        else
                            this.mc.currentScreen.drawHoveringText(I18nUtil.resolveKey(tab.category.label),mouseX,mouseY);
                    }
                }
                for (LeafiaRecipeButton button : shownButtons) {
                    if (button.isMouseOver())
                        this.mc.currentScreen.drawHoveringText(button.getToolTipText(this.mc.currentScreen),mouseX,mouseY);
                }
            }

            this.renderGhostRecipeTooltip(guiLeft, guiTop, mouseX, mouseY);
        }
    }
    Map<Integer,ItemStack> ghostTooltipStacks = new HashMap<>();
    private void renderGhostRecipeTooltip(int guiLeft, int guiTop, int mouseX, int mouseY)
    {
        ItemStack itemstack = null;

        for (int i = 0; i < this.ghostRecipe.size(); ++i)
        {
            GhostRecipe.GhostIngredient ghostrecipe$ghostingredient = this.ghostRecipe.get(i);
            int j = ghostrecipe$ghostingredient.getX() + guiLeft;
            int k = ghostrecipe$ghostingredient.getY() + guiTop;

            if (mouseX >= j && mouseY >= k && mouseX < j + 16 && mouseY < k + 16)
            {
                itemstack = ghostrecipe$ghostingredient.getItem();
            }
        }

        for (Slot slot : this.container.inventorySlots) {
            if (ghostTooltipStacks.containsKey(slot.slotNumber)) {
                if ((mouseX >= slot.xPos+guiLeft) && (mouseY >= slot.yPos+guiTop) && (mouseX < slot.xPos+guiLeft+16) && (mouseY < slot.yPos+guiTop+16)) {
                    itemstack = ghostTooltipStacks.get(slot.slotNumber);
                }
            }
        }

        if (itemstack != null && this.mc.currentScreen != null)
        {
            this.mc.currentScreen.drawHoveringText(this.mc.currentScreen.getItemToolTip(itemstack), mouseX, mouseY);
        }
    }
    float ghostRecipeTimer = 0;
    public void renderGhostRecipe(int guiLeft, int guiTop, boolean p_191864_3_, float partialTicks)
    {
        ghostTooltipStacks.clear();
        ghostRecipeTimer += partialTicks;
        if (this.highlights.size() > 0) {
            for (RangeInt slotRange : this.highlights.keySet()) {
                AStack[] stacks = this.highlights.get(slotRange);
                int stackIndex = 0;
                int remaining = 0;
                ItemStack stack = null;
                for (Integer slot : slotRange) {
                    Slot slotInstance = container.getSlot(slot);
                    if (remaining <= 0) {
                        if (stackIndex >= stacks.length) break;
                        AStack aStack = stacks[stackIndex++];
                        remaining = aStack.count();
                        if (aStack instanceof OreDictStack) {
                            List<ItemStack> variants = ((OreDictStack) aStack).toStacks();
                            stack = null;
                            if (slotInstance.getHasStack()) {
                                for (ItemStack variant : variants) {
                                    if (variant.getItem() == slotInstance.getStack().getItem()) {
                                        stack = variant;
                                        break;
                                    }
                                }
                            }
                            if (stack == null)
                                stack = variants.get(MathHelper.floor(ghostRecipeTimer/30f)%(variants.size()));
                        } else
                            stack = aStack.getStack();
                    }
                    if (remaining > 0) {
                        ItemStack copy = stack.copy();
                        int count = Math.min(copy.getMaxStackSize(),remaining);
                        remaining -= count;
                        copy.setCount(count);

                        boolean matches = false;
                        if (slotInstance.getHasStack()) {
                            if (slotInstance.getStack().getItem() == copy.getItem())
                                matches = true;
                        }
                        if (!matches) {
                            RenderItem renderer = this.mc.getRenderItem();
                            renderer.renderItemAndEffectIntoGUI(copy,guiLeft + slotInstance.xPos,guiTop + slotInstance.yPos);
                            renderer.renderItemOverlayIntoGUI(mc.fontRenderer,copy,guiLeft + slotInstance.xPos,guiTop + slotInstance.yPos,null);
                        }
                        if (!slotInstance.getHasStack()) {
                            ghostTooltipStacks.put(slot,copy);
                            GlStateManager.depthFunc(516);
                            Gui.drawRect(guiLeft+slotInstance.xPos,guiTop+slotInstance.yPos,guiLeft+slotInstance.xPos+16,guiTop+slotInstance.yPos+16,0x50FFFFFF);
                            GlStateManager.depthFunc(515);
                        }
                    }
                }
            }
        }


        this.ghostRecipe.render(this.mc, guiLeft, guiTop, p_191864_3_, partialTicks);
    }
    //@Nullable
    public Pair<Map<Integer,Integer>,Integer> pickItem(Item item,int required,Map<Integer,Integer> replacementMap,Set<Integer> blockedSlots) {
        Map<Integer,Integer> outMap = new HashMap<>();
        while (required > 0) {
            boolean changed = false;
            int minimumSlotIndex = -1;
            int minimumStackCount = Integer.MAX_VALUE;
            for (Slot slot : this.container.inventorySlots) {
                if (blockedSlots.contains(slot.slotNumber)) continue;
                int count = 0;
                if (slot.getHasStack()) {
                    if (slot.getStack().getItem() == item) {
                        if (replacementMap.containsKey(slot.slotNumber)) {
                            count = replacementMap.get(slot.slotNumber);
                        } else {
                            count = slot.getStack().getCount();
                        }
                    }
                }
                if (outMap.containsKey(slot.slotNumber)) {
                    count -= outMap.get(slot.slotNumber);
                }
                if (count > 0) {
                    if (count < minimumStackCount) {
                        minimumStackCount = count;
                        minimumSlotIndex = slot.slotNumber;
                    }
                }
            }
            if (minimumSlotIndex >= 0) {
                int decrease = Math.min(required,minimumStackCount);
                required -= decrease;
                outMap.put(minimumSlotIndex,decrease);
                changed = true;
            }
            if (!changed) break;
        }
        for (Integer slot : outMap.keySet()) {
            int count = outMap.get(slot);
            if (replacementMap.containsKey(slot))
                replacementMap.put(slot,replacementMap.get(slot)-count);
            else
                replacementMap.put(slot,this.container.getSlot(slot).getStack().getCount()-count);
        }
        return new Pair(outMap,required);
    }
    public List<Pair<Item,Integer>> getHasMost(OreDictStack item,Map<Integer,Integer> replacementMap,Set<Integer> blockedSlots) {
        Map<Item,Integer> counts = new HashMap<>();
        List<Pair<Item,Integer>> outList = new ArrayList<>();
        Set<Item> validItems = new HashSet<>();
        for (ItemStack stack : item.toStacks()) {
            validItems.add(stack.getItem());
        }
        for (Slot slot : this.container.inventorySlots) {
            if (blockedSlots.contains(slot.slotNumber)) continue;
            if (slot.getHasStack()) {
                Item thisItem = slot.getStack().getItem();
                if (validItems.contains(thisItem)) {
                    int count = 0;
                    if (replacementMap.containsKey(slot.slotNumber))
                        count = replacementMap.get(slot.slotNumber);
                    else
                        count = slot.getStack().getCount();
                    if (count > 0) {
                        if (counts.containsKey(thisItem))
                            counts.put(thisItem,counts.get(thisItem)+count);
                        else
                            counts.put(thisItem,count);
                    }
                }
            }
        }
        for (Item thisItem : counts.keySet()) {
            outList.add(new Pair<>(thisItem,counts.get(thisItem)));
        }
        outList.sort(new Comparator<Pair<Item,Integer>>() {
            @Override
            public int compare(Pair<Item,Integer> o1,Pair<Item,Integer> o2) {
                return Integer.compare(o2.getB(),o1.getB());
            }
        });
        return outList;
    }
    /*
    public Map<Item,Pair<Map<Integer,Integer>,Integer>> getCombinations(AStack item,Map<Integer,Integer> replacementMap) {
        Map<Item,Pair<Map<Integer,Integer>,Integer>> combinations = new HashMap<>();
        if (item instanceof OreDictStack) {
            for (ItemStack stack : ((OreDictStack) item).toStacks()) {
                combinations.put(stack.getItem(),pickItem(stack.getItem(),item.count(),repl));
            }
        }
    }*/
    @Nullable
    @Spaghetti("SOMEBODY PLEASE HELP ME")
    public Map<ItemStack,Map<Integer,Integer>> getSlotsToTakeFrom(AStack item,Map<Integer,Integer> replacementMap,Set<Integer> blockedSlots) {
        Map<ItemStack,Map<Integer,Integer>> itemsTake = new HashMap<>();
        int required = item.count();
        if (item instanceof OreDictStack) {
            List<Pair<Item,Integer>> most = getHasMost((OreDictStack)item,replacementMap,blockedSlots);
            /*int sum = 0;
            for (Pair<Item,Integer> pair : most) {
                sum+=pair.getValue();
            }*/
            //if (sum < required)
            for (Pair<Item,Integer> pair : most) {
                Map<Integer,Integer> slotsTake = new HashMap<>();
                boolean doTry = true;
                while (doTry) {
                    int dec = Math.max(required - pair.getA().getItemStackLimit(),0);
                    Pair<Map<Integer,Integer>,Integer> picks = pickItem(pair.getA(),required - dec,replacementMap,blockedSlots);
                    int taken = (required - dec) - picks.getB();
                    required = picks.getB() + dec;
                    for (Integer slotIndex : picks.getA().keySet()) {
                        if (slotsTake.containsKey(slotIndex))
                            slotsTake.put(slotIndex,slotsTake.get(slotIndex) + picks.getA().get(slotIndex));
                        else
                            slotsTake.put(slotIndex,picks.getA().get(slotIndex));
                    }
                    doTry = false;
                    if (slotsTake.size() > 0) {
                        itemsTake.put(new ItemStack(pair.getA(),taken),slotsTake);
                        if (dec > 0)
                            doTry = true;
                    }
                    if (required <= 0) break;
                }
                if (required <= 0) break;
            }
        } else {
            Map<Integer,Integer> slotsTake = new HashMap<>();
            boolean doTry = true;
            while (doTry) {
                int dec = Math.max(required - item.getStack().getMaxStackSize(),0);
                Pair<Map<Integer,Integer>,Integer> picks = pickItem(item.getStack().getItem(),required - dec,replacementMap,blockedSlots);
                int taken = (required - dec) - picks.getB();
                required = picks.getB() + dec;
                for (Integer slotIndex : picks.getA().keySet()) {
                    if (slotsTake.containsKey(slotIndex))
                        slotsTake.put(slotIndex,slotsTake.get(slotIndex) + picks.getA().get(slotIndex));
                    else
                        slotsTake.put(slotIndex,picks.getA().get(slotIndex));
                }
                doTry = false;
                if (slotsTake.size() > 0) {
                    itemsTake.put(new ItemStack(item.getStack().getItem(),taken),slotsTake);
                    if (dec > 0)
                        doTry = true;
                }
                if (required <= 0) break;
            }
        }
        if (required > 0)
            return null; // return nothing but replacementMap still modified
        else
            return itemsTake;
    }
    void unselectRecipesExcept(LeafiaRecipeButton exception) {
        for (LeafiaRecipeButton shownButton : shownButtons) {
            if (shownButton != exception)
                shownButton.selectedRecipe = -1;
        }
    }
    Map<RangeInt,AStack[]> highlights = new HashMap<>();
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (this.isVisible() && !this.mc.player.isSpectator())
        {
            for (LeafiaRecipeButton shownButton : shownButtons) {
                if (shownButton.mousePressed(this.mc,mouseX,mouseY)) {
                    shownButton.playPressSound(this.mc.getSoundHandler());
                    LeafiaDummyRecipe recipe = null;
                    if (mouseButton == 0) {
                        if (shownButton.selectedRecipe < 0) {
                            int recipeID = -1;
                            for (LeafiaDummyRecipe reciple : shownButton.reciples) {
                                recipeID++;
                                if (recipe == null) recipe = reciple;
                                if (reciple.isAvailable) {
                                    recipe = reciple;
                                    break;
                                }
                            }
                            unselectRecipesExcept(shownButton);
                            shownButton.selectedRecipe = recipeID;
                        } else {
                            if (shownButton.reciples.size() > shownButton.selectedRecipe)
                                recipe = shownButton.reciples.get(shownButton.selectedRecipe);
                        }
                    } else if (mouseButton == 1) {
                        unselectRecipesExcept(shownButton);
                        shownButton.selectedRecipe = (shownButton.selectedRecipe+1)%shownButton.reciples.size();
                        if (shownButton.reciples.size() > shownButton.selectedRecipe)
                            recipe = shownButton.reciples.get(shownButton.selectedRecipe);
                    }
                    if (recipe != null) {
                        highlights.clear();
                        if (mouseButton == 0) {
                            /////////////////////////////////////////////////// SEARCHING
                            @Spaghetti("AAAAAAAAAAAAAAAAAAAAAAAAAAAA")
                            Map<RangeInt,AStack[]> slots = this.profile.showRecipe(recipe);
                            Set<Integer> blockedSlots = new HashSet<>();
                            Map<Integer,Integer> replMap = new HashMap<>();
                            for (RangeInt slotRange : slots.keySet()) {
                                for (Integer slot : slotRange) {
                                    blockedSlots.add(slot);
                                }
                            }
                            Map<Integer,Pair<ItemStack,Map<Integer,Integer>>> slotsTakeFrom = new HashMap<>();
                            Map<Integer,AStack> slotsGhost = new HashMap<>();
                            for (RangeInt slotRange : slots.keySet()) {
                                AStack ghostStack = null;
                                int ghostCount = 0;
                                int curIndex = 0;
                                AStack[] stacks = slots.get(slotRange);
                                Map<ItemStack,Map<Integer,Integer>> takeFrom = null;
                                for (Integer slot : slotRange) {
                                    if (curIndex >= stacks.length) break;
                                    if (ghostStack == null) {
                                        if (takeFrom == null) {
                                            AStack stack = stacks[curIndex++];
                                            takeFrom = getSlotsToTakeFrom(stack,replMap,blockedSlots);
                                            if (takeFrom == null) {
                                                ghostStack = stack;
                                                ghostCount = 0;
                                            }
                                        }
                                    }
                                    if (ghostStack != null) {
                                        AStack copystack = ghostStack.copy();
                                        int stackLim = ghostStack.getStack().getMaxStackSize();
                                        if (copystack.count()-ghostCount > stackLim) {
                                            copystack.setCount(stackLim);
                                            ghostCount += stackLim;
                                        } else {
                                            copystack.setCount(copystack.count()-ghostCount);
                                            ghostStack = null;
                                        }
                                        slotsGhost.put(slot,copystack);
                                    } else if (takeFrom != null) {
                                        ItemStack firstItem = (ItemStack)(takeFrom.keySet().toArray()[0]);
                                        slotsTakeFrom.put(slot,new Pair<>(firstItem,takeFrom.remove(firstItem)));
                                    }
                                    if (takeFrom != null) {
                                        if (takeFrom.keySet().size() <= 0) takeFrom = null;
                                    }
                                }
                                boolean didFit = true;
                                if (curIndex < stacks.length) {
                                    didFit = false;
                                    // TODO: highlight stacks[curIndex+]
                                } else if (takeFrom != null) {
                                    didFit = false;
                                    // TODO: highlight items in takeFrom
                                } else if (ghostStack != null) {
                                    didFit = false;
                                    // TODO: uhhh
                                }
                            }
                            /////////////////////////////////////////////////// MOVING slotsTakeFrom slotsGhost

                            //compareSlotToAStack();
                        }
                        else if (mouseButton == 1) {
                            ghostRecipe.clear();
                            highlights = this.profile.showRecipe(recipe);
                        }
                    }
                }
            }
            /*
            if (this.recipeBookPage.mouseClicked(mouseX, mouseY, mouseButton, this.getActualX(), this.getActualY(), 147, 166))
            {
                IRecipe irecipe = this.recipeBookPage.getLastClickedRecipe();
                RecipeList recipelist = this.recipeBookPage.getLastClickedRecipeList();

                if (irecipe != null && recipelist != null)
                {
                    if (!recipelist.isCraftable(irecipe) && this.ghostRecipe.getRecipe() == irecipe)
                    {
                        return false;
                    }

                    this.ghostRecipe.clear();
                    this.mc.playerController.func_194338_a(this.mc.player.openContainer.windowId, irecipe, GuiScreen.isShiftKeyDown(), this.mc.player);

                    if (!this.isOffsetNextToMainGUI() && mouseButton == 0)
                    {
                        this.setVisible(false);
                    }
                }

                return true;
            }
            else */if (mouseButton != 0)
            {
                return false;
            }
            else if (this.searchBar.mouseClicked(mouseX, mouseY, mouseButton))
            {
                return true;
            }
            else if (this.toggleRecipesBtn.mousePressed(this.mc, mouseX, mouseY))
            {
                boolean flag = !this.recipeBook.isFilteringCraftable();
                this.recipeBook.setFilteringCraftable(flag);
                this.toggleRecipesBtn.setStateTriggered(flag);
                this.toggleRecipesBtn.playPressSound(this.mc.getSoundHandler());
                this.sendUpdateSettings();
                this.updateCollections(false);
                return true;
            }
            else
            {
                for (LeafiaRecipeBookTab guibuttonrecipetab : this.recipeTabs)
                {
                    if (guibuttonrecipetab.mousePressed(this.mc, mouseX, mouseY))
                    {
                        if (this.currentTab != guibuttonrecipetab)
                        {
                            guibuttonrecipetab.playPressSound(this.mc.getSoundHandler());
                            this.currentTab.setStateTriggered(false);
                            this.currentTab = guibuttonrecipetab;
                            this.currentTab.setStateTriggered(true);
                            this.updateCollections(true);
                        }

                        return true;
                    }
                }

                return false;
            }
        }
        else
        {
            return false;
        }
    }

    public boolean hasClickedOutside(int p_193955_1_, int p_193955_2_, int p_193955_3_, int p_193955_4_, int p_193955_5_, int p_193955_6_)
    {
        if (!this.isVisible())
        {
            return true;
        }
        else
        {
            boolean flag = p_193955_1_ < p_193955_3_ || p_193955_2_ < p_193955_4_ || p_193955_1_ >= p_193955_3_ + p_193955_5_ || p_193955_2_ >= p_193955_4_ + p_193955_6_;
            boolean flag1 = p_193955_3_ - 147 < p_193955_1_ && p_193955_1_ < p_193955_3_ && p_193955_4_ < p_193955_2_ && p_193955_2_ < p_193955_4_ + p_193955_6_;
            return flag && !flag1 && !this.currentTab.mousePressed(this.mc, p_193955_1_, p_193955_2_);
        }
    }

    public boolean keyPressed(char typedChar, int keycode)
    {
        if (this.isVisible() && !this.mc.player.isSpectator())
        {
            if (keycode == 1 && !this.isOffsetNextToMainGUI())
            {
                this.setVisible(false);
                return true;
            }
            else
            {
                if (GameSettings.isKeyDown(this.mc.gameSettings.keyBindChat) && !this.searchBar.isFocused())
                {
                    this.searchBar.setFocused(true);
                }
                else if (this.searchBar.textboxKeyTyped(typedChar, keycode))
                {
                    String s1 = this.searchBar.getText().toLowerCase(Locale.ROOT);
                    this.pirateRecipe(s1);

                    if (!s1.equals(this.lastSearch))
                    {
                        this.updateCollections(false);
                        this.lastSearch = s1;
                    }

                    return true;
                }

                return false;
            }
        }
        else
        {
            return false;
        }
    }

    private void pirateRecipe(String text)
    {
        if ("excitedze".equals(text))
        {
            LanguageManager languagemanager = this.mc.getLanguageManager();
            Language language = languagemanager.getLanguage("en_pt");

            if (languagemanager.getCurrentLanguage().compareTo(language) == 0)
            {
                return;
            }

            languagemanager.setCurrentLanguage(language);
            this.mc.gameSettings.language = language.getLanguageCode();
            net.minecraftforge.fml.client.FMLClientHandler.instance().refreshResources(net.minecraftforge.client.resource.VanillaResourceType.LANGUAGES);
            this.mc.fontRenderer.setUnicodeFlag(this.mc.getLanguageManager().isCurrentLocaleUnicode() || this.mc.gameSettings.forceUnicodeFont);
            this.mc.fontRenderer.setBidiFlag(languagemanager.isCurrentLanguageBidirectional());
            this.mc.gameSettings.saveOptions();
        }
    }

    private boolean isOffsetNextToMainGUI()
    {
        return !this.boundGui.overlayMode;
    }

    public void recipesUpdated()
    {
        this.updateTabs();

        if (this.isVisible())
        {
            this.updateCollections(false);
        }
    }

    public void recipesShown(List<IRecipe> recipes)
    {
        for (IRecipe irecipe : recipes)
        {
            this.mc.player.removeRecipeHighlight(irecipe);
        }
    }

    public void setupGhostRecipe(IRecipe p_193951_1_, List<Slot> p_193951_2_)
    {
        ItemStack itemstack = p_193951_1_.getRecipeOutput();
        this.ghostRecipe.setRecipe(p_193951_1_);
        this.ghostRecipe.addIngredient(Ingredient.fromStacks(itemstack), (p_193951_2_.get(0)).xPos, (p_193951_2_.get(0)).yPos);

        /*
        int i = this.craftingSlots.getWidth();
        int j = this.craftingSlots.getHeight();
        int k = p_193951_1_ instanceof net.minecraftforge.common.crafting.IShapedRecipe ? ((net.minecraftforge.common.crafting.IShapedRecipe)p_193951_1_).getRecipeWidth() : i;
        int l = 1;
        Iterator<Ingredient> iterator = p_193951_1_.getIngredients().iterator();

        for (int i1 = 0; i1 < j; ++i1)
        {
            for (int j1 = 0; j1 < k; ++j1)
            {
                if (!iterator.hasNext())
                {
                    return;
                }

                Ingredient ingredient = iterator.next();

                if (ingredient.getMatchingStacks().length > 0)
                {
                    Slot slot = p_193951_2_.get(l);
                    this.ghostRecipe.addIngredient(ingredient, slot.xPos, slot.yPos);
                }

                ++l;
            }

            if (k < i)
            {
                l += i - k;
            }
        }*/
    }

    private void sendUpdateSettings()
    {
        if (this.mc.getConnection() != null)
        {
            this.mc.getConnection().sendPacket(new CPacketRecipeInfo(this.isVisible(), this.recipeBook.isFilteringCraftable()));
        }
    }
}
