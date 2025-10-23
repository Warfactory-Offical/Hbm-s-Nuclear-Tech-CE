package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.items.food.ItemConserve;
import com.hbm.lib.HBMSoundHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockCanCrate extends Block {

    public BlockCanCrate(Material materialIn, String s) {
        super(materialIn);
        this.setTranslationKey(s);
        this.setRegistryName(s);

        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        if (playerIn.getHeldItem(EnumHand.MAIN_HAND).getItem().equals(ModItems.crowbar)) {
            if (!worldIn.isRemote) {
                dropContents(worldIn, pos);
                worldIn.setBlockToAir(pos);
                worldIn.playSound(null, pos, HBMSoundHandler.crateBreak, SoundCategory.BLOCKS, 0.5F, 1.0F);
            }
            return true;
        }
        return false;
    }

    public void dropContents(World world, BlockPos pos) {
        ArrayList<ItemStack> items = getContents(world, pos);

        for (ItemStack item : items) {
            spawnAsEntity(world, pos, item);
        }
    }

    public ArrayList<ItemStack> getContents(World world, BlockPos pos) {
        ArrayList<ItemStack> ret = new ArrayList<ItemStack>();

        int count = getContentAmount(world.rand);
        for (int i = 0; i < count; i++) {
            ret.add(getRandomItem(world.rand));
        }

        return ret;
    }

    public ItemStack getRandomItem(Random rand) {

        List<ItemStack> items = new ArrayList();
        for (int a = 0; a < ItemConserve.EnumFoodType.values().length; a++)
            items.add(new ItemStack(ModItems.canned_conserve, 1, a));
        items.add(new ItemStack(ModItems.can_smart));
        items.add(new ItemStack(ModItems.can_creature));
        items.add(new ItemStack(ModItems.can_redbomb));
        items.add(new ItemStack(ModItems.can_mrsugar));
        items.add(new ItemStack(ModItems.can_overcharge));
        items.add(new ItemStack(ModItems.can_luna));
        items.add(new ItemStack(ModItems.can_breen));
        items.add(new ItemStack(ModItems.can_bepis));
        items.add(new ItemStack(ModItems.pudding));

        return items.get(rand.nextInt(items.size()));
    }

    public int getContentAmount(Random rand) {
        return 5 + rand.nextInt(4);
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
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
    public boolean isNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

}
