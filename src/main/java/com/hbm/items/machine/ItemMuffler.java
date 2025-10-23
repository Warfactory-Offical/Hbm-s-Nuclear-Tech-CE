package com.hbm.items.machine;

import com.hbm.items.ItemBakedBase;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.CompatExternal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
public class ItemMuffler extends ItemBakedBase {

    public ItemMuffler(String s){
        super(s);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity te = CompatExternal.getCoreFromPos(world, pos);

        if(te != null && te instanceof TileEntityLoadedBase) {
            TileEntityLoadedBase tile = (TileEntityLoadedBase) te;
            if(!tile.muffled) {
                tile.muffled = true;
                world.playSound(player, player.posX, player.posY, player.posZ, HBMSoundHandler.upgradePlug, SoundCategory.BLOCKS, 1.0F, 1.0F);
                player.getHeldItem(hand).shrink(1);
                tile.markDirty();
                return EnumActionResult.SUCCESS;
            }
        }

        return EnumActionResult.PASS;
    }
}
