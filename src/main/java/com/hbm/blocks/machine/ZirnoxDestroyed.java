package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.items.ModItems;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityZirnoxDestroyed;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.Random;

public class ZirnoxDestroyed extends BlockDummyable {

    public ZirnoxDestroyed(Material mat, String s) {
        super(mat, s);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {

        if(meta >= 12)
            return new TileEntityZirnoxDestroyed();
        if(meta >= 6)
            return new TileEntityProxyCombo(false, true, true);

        return null;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return false;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {

        Block block = world.getBlockState(pos.add(0, 1, 0)).getBlock();

        if(block == Blocks.AIR) {
            if(rand.nextInt(10) == 0 && GeneralConfig.enableMeltdownGas)
                world.setBlockState(pos.add(0, 1, 0), ModBlocks.gas_meltdown.getDefaultState());

        } else if(block == ModBlocks.foam_layer || block == ModBlocks.block_foam) {
            if(rand.nextInt(25) == 0) {
                int[] posC = this.findCore(world, pos.getX(), pos.getY(), pos.getZ());

                if(posC != null) {
                    TileEntity te = world.getTileEntity(new BlockPos(posC[0], posC[1], posC[2]));

                    if(te instanceof TileEntityZirnoxDestroyed)
                        ((TileEntityZirnoxDestroyed)te).onFire = false;
                }
            }
        }

        if(rand.nextInt(10) == 0 && world.getBlockState(pos.add(0, 1, 0)).getBlock() == Blocks.AIR  && GeneralConfig.enableMeltdownGas)
            world.setBlockState(pos.add(0, 1, 0), ModBlocks.gas_meltdown.getDefaultState());

        super.updateTick(world, pos, state, rand);
    }

    @Override
    public int tickRate(World world) {

        return 100 + world.rand.nextInt(20);
    }

    public void onBlockAdded(World world, int x, int y, int z) {
        super.onBlockAdded(world, new BlockPos(x, y, z), blockState.getBaseState());

        if(!world.isRemote) {
            if(world.rand.nextInt(4) == 0) {
                NBTTagCompound data = new NBTTagCompound();
                data.setString("type", "rbmkflame");
                data.setInteger("maxAge", 90);
                PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, x + 0.25 + world.rand.nextDouble() * 0.5, y + 1.75, z + 0.25 + world.rand.nextDouble() * 0.5), new NetworkRegistry.TargetPoint(world.provider.getDimension(), x + 0.5, y + 1.75, z + 0.5, 75));
                MainRegistry.proxy.effectNT(data);
                world.playSound(null, x + 0.5F, y + 0.5, z + 0.5, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 1.0F + world.rand.nextFloat(), world.rand.nextFloat() * 0.7F + 0.3F);
            }
        }

        world.scheduleUpdate(new BlockPos(x, y, z), this, this.tickRate(world));
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune){
        drops.add(new ItemStack(ModBlocks.concrete_smooth, 6));
        drops.add(new ItemStack(ModBlocks.steel_grate, 2));
        drops.add(new ItemStack(ModItems.debris_metal, 6));
        drops.add(new ItemStack(ModItems.debris_graphite, 2));
        drops.add(new ItemStack(ModItems.fallout, 4));
    }

    @Override
    public int[] getDimensions() {
        return new int[] {1, 0, 2, 2, 2, 2,};
    }

    @Override
    public int getOffset() {
        return 2;
    }

    protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
        super.fillSpace(world, x, y, z, dir, o);
    }

}
