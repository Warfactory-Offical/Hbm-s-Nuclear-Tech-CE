package com.hbm.blocks.machine;

import com.hbm.render.block.BlockBakeFrame;
import com.hbm.tileentity.machine.TileEntityCharger;
import com.hbm.world.gen.nbt.INBTBlockTransformable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class Charger extends BlockContainerBakeable implements INBTBlockTransformable {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    private static final float f = 1F / 16F;

    private static final AxisAlignedBB AABB_CENTER = new AxisAlignedBB(5 * f, 0.25D, 5 * f, 11 * f, 0.75D, 11 * f);
    private static final AxisAlignedBB AABB_NORTH  = new AxisAlignedBB(5 * f, 0.25D, 12 * f, 11 * f, 0.75D, 1.0D);
    private static final AxisAlignedBB AABB_SOUTH  = new AxisAlignedBB(5 * f, 0.25D, 0.0D, 11 * f, 0.75D, 4 * f);
    private static final AxisAlignedBB AABB_WEST   = new AxisAlignedBB(12 * f, 0.25D, 5 * f, 1.0D, 0.75D, 11 * f);
    private static final AxisAlignedBB AABB_EAST   = new AxisAlignedBB(0.0D, 0.25D, 5 * f, 4 * f, 0.75D, 11 * f);

    public Charger(Material mat, String s) {
        super(mat, s, new BlockBakeFrame("block_steel"));
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityCharger();
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
            default -> AABB_CENTER;
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

    @Override
    public int transformMeta(int meta, int coordBaseMode) {
        return INBTBlockTransformable.transformMetaDeco(meta, coordBaseMode);
    }
}
