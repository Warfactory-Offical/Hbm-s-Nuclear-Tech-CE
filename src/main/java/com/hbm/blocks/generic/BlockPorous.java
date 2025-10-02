package com.hbm.blocks.generic;

import com.google.common.base.Predicate;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.interfaces.Untested;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockPorous extends Block {

    public BlockPorous(String s) {
        super(Material.ROCK);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setHardness(1.5F); //stone tier
        this.setResistance(30.0F); //ha

        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Untested
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        //in theory this should keep the block silk-harvestable, but dropping smooth stone instead
        return Item.getItemFromBlock(Blocks.STONE);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        world.scheduleUpdate(pos, this, this.tickRate(world));
    }

    @Override
    public int tickRate(World world) {
        return 90 + world.rand.nextInt(20);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        ChunkRadiationManager.proxy.decrementRad(worldIn, pos, 10F);
    }

    @Override
    public boolean isReplaceableOreGen(IBlockState state, IBlockAccess world, BlockPos pos, Predicate<IBlockState> target) {
        return target.apply(this.getDefaultState()) || target.apply(Blocks.STONE.getDefaultState());
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState();
    }

}