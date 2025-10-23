package com.leafia.shit;

import com.leafia.shit.Bruh;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BlockFallingLeafia extends BlockFallingBase {
    public BlockFallingLeafia(Material m, String s, SoundType type) {
        super(m, s, type);
    }
    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (!worldIn.isRemote)
        {
            this.checkFallable(worldIn, pos);
        }
    }
    public void blockLanded(World worldIn, BlockPos pos, IBlockState block, Bruh entity, Random rand) {}
    public boolean spaceBelow(World worldIn, BlockPos pos) {
        return worldIn.isAirBlock(pos.down());
    }
    public boolean shouldFall(World worldIn, BlockPos pos) {
        return this.spaceBelow(worldIn,pos) || canFallThrough(worldIn.getBlockState(pos.down()));
    }
    public void checkFallable(World worldIn, BlockPos pos)
    {
        if (this.shouldFall(worldIn,pos) && pos.getY() >= 0)
        {
            int i = 32;

            if (!fallInstantly && worldIn.isAreaLoaded(pos.add(-32, -32, -32), pos.add(32, 32, 32)))
            {
                if (!worldIn.isRemote)
                {
                    Bruh entityfallingblock = new Bruh(worldIn, (double)pos.getX() + 0.5D, (double)pos.getY(), (double)pos.getZ() + 0.5D, worldIn.getBlockState(pos));
                    this.onStartFalling(entityfallingblock);
                    worldIn.spawnEntity(entityfallingblock);
                }
            }
            else
            {
                IBlockState state = worldIn.getBlockState(pos);
                worldIn.setBlockToAir(pos);
                BlockPos blockpos;

                for (blockpos = pos.down(); (worldIn.isAirBlock(blockpos) || canFallThrough(worldIn.getBlockState(blockpos))) && blockpos.getY() > 0; blockpos = blockpos.down())
                {
                    ;
                }

                if (blockpos.getY() > 0)
                {
                    worldIn.setBlockState(blockpos.up(), state); //Forge: Fix loss of state information during world gen.
                }
            }
        }
    }
}
