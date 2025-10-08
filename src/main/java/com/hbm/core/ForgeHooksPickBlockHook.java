package com.hbm.core;

import com.hbm.tileentity.IPersistentNBT;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

@SuppressWarnings("unused")
public final class ForgeHooksPickBlockHook {

    private ForgeHooksPickBlockHook() {
    }

    public static boolean handlePickBlock(ItemStack stack, RayTraceResult target, World world, TileEntity tileEntity) {
        if (stack == null || stack.isEmpty() || target == null || world == null || tileEntity == null) return false;
        if (target.typeOfHit != RayTraceResult.Type.BLOCK) return false;
        if (tileEntity instanceof IPersistentNBT persistent) {
            NBTTagCompound nbt = new NBTTagCompound();
            persistent.writeNBT(nbt);
            if (!nbt.isEmpty()) stack.setTagCompound(nbt);
            return true;
        }
        return false;
    }
}
