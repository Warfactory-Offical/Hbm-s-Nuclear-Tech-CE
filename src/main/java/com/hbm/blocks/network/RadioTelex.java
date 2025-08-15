package com.hbm.blocks.network;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.network.TileEntityRadioTelex;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;

public class RadioTelex extends BlockDummyable {
    public RadioTelex(String s) {
        super(Material.WOOD, s);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        if(meta >= 12) return new TileEntityRadioTelex();
        return null;
    }

    @Override
    public int[] getDimensions() {
        return new int[] {0, 0, 0, 0, 1, 0};
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote && !player.isSneaking()) {
            BlockPos corePos = this.findCore(world, pos);
            if (corePos == null) return false;
            FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, corePos.getX(), corePos.getY(), corePos.getZ());
            return true;
        } else {
            return !player.isSneaking();
        }
    }
}
