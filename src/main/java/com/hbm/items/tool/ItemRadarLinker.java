package com.hbm.items.tool;

import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.tileentity.IRadarCommandReceiver;
import com.hbm.tileentity.machine.TileEntityMachineRadarScreen;
import com.hbm.util.CompatExternal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemRadarLinker extends ItemCoordinateBase {

    public ItemRadarLinker(String s){
        this.setTranslationKey(s);
        this.setRegistryName(s);

        ModItems.ALL_ITEMS.add(this);
    }
    @Override
    public boolean canGrabCoordinateHere(World world, BlockPos pos) {
        TileEntity tile = CompatExternal.getCoreFromPos(world, pos);
        return tile instanceof IRadarCommandReceiver || tile instanceof TileEntityMachineRadarScreen;
    }

    @Override
    public BlockPos getCoordinates(World world, BlockPos pos) {
        TileEntity tile = CompatExternal.getCoreFromPos(world, pos);
        return new BlockPos(tile.getPos());
    }

    @Override
    public void onTargetSet(World world, BlockPos pos, EntityPlayer player) {
        world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.techBleep, SoundCategory.AMBIENT, 1.0F, 1.0F);
    }
}
