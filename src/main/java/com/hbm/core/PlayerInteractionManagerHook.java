package com.hbm.core;

import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.CompatExternal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;

@SuppressWarnings("unused")
public class PlayerInteractionManagerHook {
    public static void onSpectatorRightClickBlock(EntityPlayer player, World worldIn, ItemStack stack, EnumHand hand, BlockPos pos, EnumFacing facing,
                                                  float hitX, float hitY, float hitZ, TileEntity tileentity) {
        //mlbv: in case somehow a spectator is able to move an item: add a check in com.hbm.handler.GuiHandler.getServerGuiElement and wrap the returned Container
        //with a delegation container that makes methods like slotClick no-op
        //currently it works as intended; if it ever breaks, add that check and it should be fine.
        if (tileentity instanceof IGUIProvider) {
            FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
        } else if (worldIn.getBlockState(pos).getBlock() instanceof IGUIProvider) {
            FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
        } else {
            TileEntity core = CompatExternal.getCoreFromPos(worldIn, pos);
            if (core instanceof IGUIProvider) {
                FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, worldIn, core.getPos().getX(), core.getPos().getY(), core.getPos().getZ());
            }
        }
    }
}
