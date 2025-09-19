package com.hbm.blocks.generic;

import com.hbm.blocks.machine.BlockContainerBakeable;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.block.BlockBakeFrame;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BlockSkeletonHolder extends BlockContainerBakeable {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public BlockSkeletonHolder(String regName, BlockBakeFrame blockFrame) {
        super(Material.ROCK, regName, blockFrame);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntitySkeletonHolder();
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntitySkeletonHolder();
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.byHorizontalIndex(meta & 3);
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack itemStack) {
        int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        EnumFacing facing;
        switch (i) {
            default:
            case 0: facing = EnumFacing.EAST; break;
            case 1: facing = EnumFacing.SOUTH; break;
            case 2: facing = EnumFacing.WEST; break;
            case 3: facing = EnumFacing.NORTH; break;
        }
        world.setBlockState(pos, state.withProperty(FACING, facing), 2);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (hand != EnumHand.MAIN_HAND) return true;
        if (player.isSneaking()) return false;

        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntitySkeletonHolder)) return false;
        TileEntitySkeletonHolder pedestal = (TileEntitySkeletonHolder) te;

        ItemStack held = player.getHeldItem(hand);

        if (pedestal.item.isEmpty() && !held.isEmpty()) {
            if (world.isRemote) return true;
            pedestal.item = held.copy();
            player.setHeldItem(hand, ItemStack.EMPTY);
            pedestal.markDirty();
            world.notifyBlockUpdate(pos, state, state, 3);
            return true;
        } else if (!pedestal.item.isEmpty() && held.isEmpty()) {
            if (world.isRemote) return true;
            player.setHeldItem(hand, pedestal.item.copy());
            pedestal.item = ItemStack.EMPTY;
            pedestal.markDirty();
            world.notifyBlockUpdate(pos, state, state, 3);
            return true;
        }

        return false;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntitySkeletonHolder) {
                TileEntitySkeletonHolder entity = (TileEntitySkeletonHolder) te;
                if (!entity.item.isEmpty()) {
                    EntityItem item = new EntityItem(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, entity.item.copy());
                    world.spawnEntity(item);
                }
            }
        }
        super.breakBlock(world, pos, state);
    }
    @AutoRegister
    public static class TileEntitySkeletonHolder extends TileEntity {
        public ItemStack item = ItemStack.EMPTY;

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            super.writeToNBT(nbt);
            if (!this.item.isEmpty()) {
                NBTTagCompound stack = new NBTTagCompound();
                this.item.writeToNBT(stack);
                nbt.setTag("item", stack);
            }
            return nbt;
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            super.readFromNBT(nbt);
            if (nbt.hasKey("item", 10)) {
                this.item = new ItemStack(nbt.getCompoundTag("item"));
            } else {
                this.item = ItemStack.EMPTY;
            }
        }

        @Override
        public SPacketUpdateTileEntity getUpdatePacket() {
            NBTTagCompound nbt = new NBTTagCompound();
            this.writeToNBT(nbt);
            return new SPacketUpdateTileEntity(this.pos, 0, nbt);
        }

        @Override
        public NBTTagCompound getUpdateTag() {
            NBTTagCompound nbt = super.getUpdateTag();
            this.writeToNBT(nbt);
            return nbt;
        }

        @Override
        public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
            this.readFromNBT(pkt.getNbtCompound());
        }
    }
}
