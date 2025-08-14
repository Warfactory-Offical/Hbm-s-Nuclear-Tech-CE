package com.hbm.items.tool;

import com.hbm.items.ItemBase;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.network.IDroneLinkable;
import com.hbm.util.ChatBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemDroneLinker extends ItemBase {
    public ItemDroneLinker(String s) {
        super(s);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float fX, float fY, float fZ) {

        TileEntity tile = world.getTileEntity(pos);

        if(tile instanceof IDroneLinkable) {

            if(!world.isRemote) {
                ItemStack stack = player.getHeldItem(hand);
                if(!stack.hasTagCompound()) {
                    stack.setTagCompound(new NBTTagCompound());
                    stack.getTagCompound().setInteger("x", pos.getX());
                    stack.getTagCompound().setInteger("y", pos.getY());
                    stack.getTagCompound().setInteger("z", pos.getZ());

                    player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
                            .nextTranslation(this.getTranslationKey() + ".name").color(TextFormatting.DARK_AQUA)
                            .next("] ").color(TextFormatting.DARK_AQUA)
                            .next("Set initial position!").color(TextFormatting.AQUA).flush());

                } else {

                    int tx = stack.getTagCompound().getInteger("x");
                    int ty = stack.getTagCompound().getInteger("y");
                    int tz = stack.getTagCompound().getInteger("z");

                    TileEntity prev = world.getTileEntity(new BlockPos(tx, ty, tz));

                    if(prev instanceof IDroneLinkable) {

                        BlockPos dest = ((IDroneLinkable) tile).getPoint();
                        ((IDroneLinkable) prev).setNextTarget(dest.getX(), dest.getY(), dest.getZ());

                        player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
                                .nextTranslation(this.getTranslationKey() + ".name").color(TextFormatting.DARK_AQUA)
                                .next("] ").color(TextFormatting.DARK_AQUA)
                                .next("Link set!").color(TextFormatting.AQUA).flush());
                    } else {
                        player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
                                .nextTranslation(this.getTranslationKey() + ".name").color(TextFormatting.DARK_AQUA)
                                .next("] ").color(TextFormatting.DARK_AQUA)
                                .next("Previous link lost!").color(TextFormatting.RED).flush());
                    }

                    stack.getTagCompound().setInteger("x", pos.getX());
                    stack.getTagCompound().setInteger("y", pos.getY());
                    stack.getTagCompound().setInteger("z", pos.getZ());
                }
            }

            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.FAIL;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean inhand) {

        if(world.isRemote && inhand) {
            if(stack.hasTagCompound()) {
                int x = stack.getTagCompound().getInteger("x");
                int y = stack.getTagCompound().getInteger("y");
                int z = stack.getTagCompound().getInteger("z");
                MainRegistry.proxy.displayTooltip("Prev pos: " + x + " / " + y + " / " + z);
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote && stack.hasTagCompound()) {
            stack.setTagCompound(null);

            player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
                    .nextTranslation(this.getTranslationKey() + ".name").color(TextFormatting.DARK_AQUA)
                    .next("] ").color(TextFormatting.DARK_AQUA)
                    .next("Position cleared!").color(TextFormatting.GREEN).flush());
        }

        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }
}
