package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.world.gen.nbt.INBTBlockTransformable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockPipe extends Block implements INBTBlockTransformable {

    public static final PropertyEnum<EnumFacing.Axis> AXIS = PropertyEnum.create("axis", EnumFacing.Axis.class);

    public BlockPipe(Material mat, String s) {
        super(mat);
        this.setTranslationKey(s);
        this.setRegistryName(s);

        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        if(state.getBlock() == ModBlocks.deco_pipe_framed || state.getBlock() == ModBlocks.deco_pipe_framed_green ||
                state.getBlock() == ModBlocks.deco_pipe_framed_marked || state.getBlock() == ModBlocks.deco_pipe_framed_red ||
                state.getBlock() == ModBlocks.deco_pipe_framed_rusted || state.getBlock() == ModBlocks.deco_pipe_framed_green_rusted)
            return BlockFaceShape.SOLID;
        else return BlockFaceShape.UNDEFINED;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isBlockNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return this.getDefaultState().withProperty(AXIS, facing.getAxis());
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AXIS);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        EnumFacing.Axis axis = state.getValue(AXIS);
        return switch (axis) {
            case X -> 4;
            case Z -> 8;
            case Y -> 0;
        };
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        int rot = meta & 12;
        EnumFacing.Axis axis;
        if (rot == 4) axis = EnumFacing.Axis.X;
        else if (rot == 8) axis = EnumFacing.Axis.Z;
        else axis = EnumFacing.Axis.Y;
        return this.getDefaultState().withProperty(AXIS, axis);
    }

    @Override
    public int transformMeta(int meta, int coordBaseMode) {
        return INBTBlockTransformable.transformMetaPillar(meta, coordBaseMode);
    }
}