package com.hbm.items.tool;

import com.hbm.inventory.container.ContainerToolBox;
import com.hbm.inventory.gui.GUIToolBox;
import com.hbm.items.ItemInventory;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.ItemStackUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ItemToolBox extends Item implements IGUIProvider {
    public ItemToolBox(String s) {
        this.setMaxStackSize(1);
        this.setRegistryName(s);
        this.setTranslationKey(s);
        this.addPropertyOverride(new ResourceLocation("open"), (stack, worldIn, entityIn) -> {
            if (stack.hasTagCompound()) {
                if (stack.getTagCompound().hasKey("isOpen") && stack.getTagCompound().getBoolean("isOpen")) {
                    return 1;
                }
            }
            return 0;
        });

        ModItems.ALL_ITEMS.add(this);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(TextFormatting.GRAY + "Click with the toolbox to swap hotbars in/out of the toolbox.");
        tooltip.add(TextFormatting.GRAY + "Shift-click with the toolbox to open the toolbox.");
    }

    // Finds active rows in the toolbox (rows with items inside them).
    public List<Integer> getActiveRows(ItemStack box) {
        ItemStack[] stacks = ItemStackUtil.readStacksFromNBT(box, 24);
        if(stacks == null)
            return new ArrayList<>();
        List<Integer> activeRows = new ArrayList<>();
        for (int row = 0; row < 3; row++) {
            for (int slot = 0; slot < 8; slot++) {
                if(stacks[row * 8 + slot] != null) {
                    activeRows.add(row);
                    break;
                }
            }
        }
        return activeRows;
    }

    // This function genuinely hurts my soul, but it works...
    public void moveRows(ItemStack box, EntityPlayer player) {

        // Move from hotbar into array in preparation for boxing.
        ItemStack[] endingHotBar = new ItemStack[9];
        ItemStack[] stacksToTransferToBox = new ItemStack[8];

        boolean hasToolbox = false;
        int extraToolboxes = 0;
        for (int i = 0; i < 9; i++) { // Maximum allowed HotBar size is 9.

            ItemStack slot = player.inventory.getStackInSlot(i);

            if(slot != ItemStack.EMPTY && slot.getItem() == ModItems.toolbox && i != player.inventory.currentItem) {

                extraToolboxes++;
                player.dropItem(slot, true);
                player.inventory.setInventorySlotContents(i, ItemStack.EMPTY);

            } else if(i == player.inventory.currentItem) {
                hasToolbox = true;
                endingHotBar[i] = slot;
            } else {
                stacksToTransferToBox[i - (hasToolbox ? 1 : 0)] = slot;
            }
        }

        if(extraToolboxes > 0) {
            if(extraToolboxes == 1)
                player.sendMessage(new TextComponentString(I18n.format("item.toolbox.error_toolbox_toolbox")).setStyle(new Style().setColor(TextFormatting.RED)));
            else
                player.sendMessage(new TextComponentString(I18n.format("item.toolbox.error_toolbox_toolbox") + "(x" + extraToolboxes + ")").setStyle(new Style().setColor(TextFormatting.RED)));
        }

        // Move stacks around inside the box, mostly shifts rows to other rows and shifts the top row to the hotbar.
        ItemStack[] stacks = ItemStackUtil.readStacksFromNBT(box, 24);
        ItemStack[] endingStacks = new ItemStack[24];

        int lowestActiveIndex = Integer.MAX_VALUE; // Lowest active index to find which row to move *to* the hotbar.
        int lowestInactiveIndex = Integer.MAX_VALUE; // Lowest *inactive* index to find which row to move the hotbar to.

        if(stacks != null) {
            List<Integer> activeRows = getActiveRows(box);

            { // despair
                for (int i = 0; i < 3; i++) {
                    if(activeRows.contains(i))
                        lowestActiveIndex = Math.min(i, lowestActiveIndex);
                    else
                        lowestInactiveIndex = Math.min(i, lowestInactiveIndex);
                }

                if(lowestInactiveIndex > 2) // No inactive rows...
                    lowestInactiveIndex = 2; // Set to the last possible row; the items will be moved out of the way in time.
                else
                    lowestInactiveIndex = Math.max(0, lowestInactiveIndex - 1); // A little shittery to make items pop into the row that's *going* to be empty.
            }

            // This entire section sucks, but honestly it's not actually that bad; it works so....
            for (Integer activeRowIndex : activeRows) {

                int activeIndex = 8 * activeRowIndex;

                if (activeRowIndex == lowestActiveIndex) { // Items to "flow" to the hotbar.
                    hasToolbox = false;
                    for (int i = 0; i < 9; i++) {
                        if(i == player.inventory.currentItem) {
                            hasToolbox = true;
                            continue;
                        }
                        endingHotBar[i] = stacks[activeIndex + i - (hasToolbox ? 1 : 0)];
                    }
                    continue;
                }

                int targetIndex = 8 * (activeRowIndex - 1);

                System.arraycopy(stacks, activeIndex, endingStacks, targetIndex, 8);
            }
        }

        if(stacks == null)
            lowestInactiveIndex = 0; // Fix crash relating to a null NBT causing this value to be Integer.MAX_VALUE.

        // Finally, move all temporary arrays into their respective locations.
        System.arraycopy(stacksToTransferToBox, 0, endingStacks, lowestInactiveIndex * 8, 8);

        for (int i = 0; i < endingHotBar.length; i++) {
            player.inventory.setInventorySlotContents(i, endingHotBar[i]);
        }

        box.setTagCompound(new NBTTagCompound());
        ItemStackUtil.addStacksToNBT(box, endingStacks);

        NBTTagCompound nbt = box.getTagCompound();

        if(nbt != null && !nbt.isEmpty()) {
            Random random = new Random();

            try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                CompressedStreamTools.writeCompressed(nbt, stream);
                byte[] abyte = stream.toByteArray();

                if (abyte.length > 6000) {
                    player.sendMessage(new TextComponentString(TextFormatting.RED + "Warning: Container NBT exceeds 6kB, contents will be ejected!"));
                    ItemStack[] stacks1 = ItemStackUtil.readStacksFromNBT(box, 24 /* Toolbox inv size. */);
                    if(stacks1 == null)
                        return;
                    for (ItemStack itemstack : stacks1) {

                        if (itemstack != null) {
                            float f = random.nextFloat() * 0.8F + 0.1F;
                            float f1 = random.nextFloat() * 0.8F + 0.1F;
                            float f2 = random.nextFloat() * 0.8F + 0.1F;

                            while (itemstack.getCount() > 0) {
                                int j1 = random.nextInt(21) + 10;

                                if (j1 > itemstack.getCount()) {
                                    j1 = itemstack.getCount();
                                }

                                itemstack.shrink(j1);
                                EntityItem entityitem = new EntityItem(player.world, player.posX + f, player.posY + f1, player.posZ + f2, new ItemStack(itemstack.getItem(), j1, itemstack.getItemDamage()));

                                if (itemstack.hasTagCompound()) {
                                    entityitem.getItem().setTagCompound(itemstack.getTagCompound().copy());
                                }

                                float f3 = 0.05F;
                                entityitem.motionX = (float) random.nextGaussian() * f3 + player.motionX;
                                entityitem.motionY = (float) random.nextGaussian() * f3 + 0.2F + player.motionY;
                                entityitem.motionZ = (float) random.nextGaussian() * f3 + player.motionZ;
                                player.world.spawnEntity(entityitem);
                            }
                        }
                    }

                    box.setTagCompound(new NBTTagCompound()); // Reset.
                }
            } catch (IOException ignored) {}
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand handIn) {
        ItemStack stack = Library.getMainHeldItem(player);
        if (!stack.getItem().equals(player.getHeldItem(handIn).getItem()))
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        if(!world.isRemote) {
            if (!player.isSneaking()) {
                moveRows(stack, player);
                player.inventoryContainer.detectAndSendChanges();
            } else {
                if(stack.getTagCompound() == null)
                    stack.setTagCompound(new NBTTagCompound());
                stack.getTagCompound().setBoolean("isOpen", true);
                player.openGui(MainRegistry.instance, 0, world, 0, 0, 0);
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerToolBox(player.inventory, new InventoryToolBox(player, Library.getMainHeldItem(player)));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIToolBox(player.inventory, new InventoryToolBox(player, Library.getMainHeldItem(player)));
    }

    public static class InventoryToolBox extends ItemInventory {
        public InventoryToolBox(EntityPlayer player, ItemStack box) {
            super(player, box, 24);
            this.player = player;
            this.target = box;

            if(!box.hasTagCompound())
                box.setTagCompound(new NBTTagCompound());

            ItemStack[] fromNBT = ItemStackUtil.readStacksFromNBT(box, getSlots());

            if(fromNBT != null) {
                for (int i = 0; i < getSlots(); i++) {
                    fromNBT[i] = getStackInSlot(i);
                }
            }
        }

        @Override
        public void closeInventory() {
            super.closeInventory();

            if (!this.target.hasTagCompound()) {
                this.target.setTagCompound(new NBTTagCompound());
            }

            this.target.getTagCompound().setBoolean("isOpen", false);
            this.target.getTagCompound().setInteger("rand", player.world.rand.nextInt()); // a boolean changing isn't sufficient to detect the change
            player.inventoryContainer.detectAndSendChanges();
        }

        public boolean hasCustomName() {
            return this.target.hasDisplayName();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() != ModItems.toolbox;
        }
    }
}
