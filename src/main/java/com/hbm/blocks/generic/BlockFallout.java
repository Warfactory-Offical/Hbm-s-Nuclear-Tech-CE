package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.items.ModItems;
import com.hbm.potion.HbmPotion;
import com.hbm.util.ContaminationUtil;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockFallout extends Block {

    public static final PropertyInteger META = PropertyInteger.create("meta", 0, 6);

    public BlockFallout(Material mat, String s) {
        super(mat);
        setTranslationKey(s);
        setRegistryName(s);
        setSoundType(SoundType.GROUND);
        setHarvestLevel("shovel", 0);
        // Fallout tops the chunk radiation up to a fixed cap; it does not need to be exact or prompt.
        // Random ticks cost O(loaded chunks), scheduled ticks cost O(fallout blocks) and pile up in
        // WorldServer's pending tick list forever, which makes every chunk save quadratic. See below.
        setTickRandomly(true);

        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        /*
         * Deliberately does NOT reschedule itself. Every fallout block re-arming a scheduled tick every
         * 10-40 ticks meant pendingTickListEntries held one entry per fallout block permanently, which
         * (a) saturated the 65536 blocks-per-tick cap in WorldServer#tickUpdates and (b) made
         * WorldServer#getPendingBlockUpdates - a full linear scan of that set, run once per chunk saved -
         * eat over half the server thread.
         *
         * A random tick reaches a given block roughly every 1365 ticks, but incrementRad only tops the
         * section up to a fixed cap and a fallout field has thousands of blocks per section, so the
         * section still saturates within a few ticks. Cost now scales with loaded chunks, not with
         * the number of fallout blocks.
         */
        if (!worldIn.isRemote) {
            ChunkRadiationManager.proxy.incrementRad(worldIn, pos, 1, 100);
        }
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return new AxisAlignedBB(0, 0, 0, 1, 0.125, 1);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean canEntitySpawn(IBlockState state, Entity entityIn) {
        return ContaminationUtil.isRadImmune(entityIn);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return this == ModBlocks.fallout ? ModItems.fallout : Items.AIR;
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos.down());
        Block block = state.getBlock();
        return block != Blocks.ICE && block != Blocks.PACKED_ICE && (block.isLeaves(state, world, pos.down()) || (block == this && (state.getValue(META) & 7) == 7 || state.isOpaqueCube() && state.getMaterial().blocksMovement()));
    }

    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        if (!world.isRemote && entity instanceof EntityLivingBase entityLivingBase) {
            entityLivingBase.addPotionEffect(new PotionEffect(HbmPotion.radiation, 2 * 60 * 20, 14));
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!this.canPlaceBlockAt(world, pos)) {
            world.setBlockToAir(pos);
        }
    }

    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, META);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(META);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(META, meta);
    }
}