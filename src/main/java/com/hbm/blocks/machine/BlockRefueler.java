package com.hbm.blocks.machine;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.render.block.BlockBakeFrame;
import com.hbm.tileentity.machine.TileEntityRefueler;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockRefueler extends BlockContainerBakeable {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    private static final float f = 1F / 16F;

    private static final AxisAlignedBB AABB_FULL   = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    private static final AxisAlignedBB AABB_NORTH  = new AxisAlignedBB(0.0D, 0.0D, 12 * f, 1.0D, 1.0D, 1.0D);
    private static final AxisAlignedBB AABB_SOUTH  = new AxisAlignedBB(0.0D, 0.0D, 0.0D,  1.0D, 1.0D, 4 * f);
    private static final AxisAlignedBB AABB_WEST   = new AxisAlignedBB(12 * f, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    private static final AxisAlignedBB AABB_EAST   = new AxisAlignedBB(0.0D,  0.0D, 0.0D, 4 * f, 1.0D, 1.0D);

    public BlockRefueler(Material mat, String name) {
        super(mat, name, new BlockBakeFrame("block_steel"));
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityRefueler();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state){
        return EnumBlockRenderType.INVISIBLE;
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
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote && !player.isSneaking()) {
            ItemStack held = player.getHeldItem(hand);
            if (!held.isEmpty() && held.getItem() instanceof IItemFluidIdentifier) {
                TileEntity te = world.getTileEntity(pos);
                if (!(te instanceof TileEntityRefueler refueler)) return false;

                FluidType type = ((IItemFluidIdentifier) held.getItem()).getType(world, pos.getX(), pos.getY(), pos.getZ(), held);
                refueler.tank.setTankType(type);
                refueler.markDirty();

                ITextComponent msg = new TextComponentString("Changed type to ")
                        .setStyle(new Style().setColor(TextFormatting.YELLOW))
                        .appendSibling(new TextComponentTranslation(type.getConditionalName()))
                        .appendSibling(new TextComponentString("!"));
                player.sendMessage(msg);

                return true;
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                            float hitX, float hitY, float hitZ,
                                            int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }


    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        EnumFacing facing = state.getValue(FACING);
        return switch (facing) {
            case NORTH -> AABB_NORTH;
            case SOUTH -> AABB_SOUTH;
            case WEST -> AABB_WEST;
            case EAST -> AABB_EAST;
            default -> AABB_FULL;
        };
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return getBoundingBox(state, worldIn, pos);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.byIndex(meta);
        if (facing.getAxis().isVertical()) facing = EnumFacing.NORTH;
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }
}
