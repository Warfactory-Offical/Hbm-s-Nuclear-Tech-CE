package com.hbm.items.tool;

import com.hbm.handler.ConsumableHandler;
import com.hbm.items.ItemBakedBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemRepairKit extends ItemBakedBase {
    public ItemRepairKit(String s, String texturePath, int dura) {
        super(s, texturePath);
        this.setMaxStackSize(1);
        this.setMaxDamage(dura - 1);
    }

    public ItemRepairKit(String s, int dura) {
        super(s);
        this.setMaxStackSize(1);
        this.setMaxDamage(dura - 1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        return ConsumableHandler.handleItemUse(world, player, hand, this);
    }


}
