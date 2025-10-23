package com.leafia.shit;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Bruh extends EntityFallingBlock {
    private IBlockState fallTile;

    public Bruh(World worldIn,double x,double y,double z,IBlockState fallingBlockState) {
        super(worldIn, x, y, z, fallingBlockState);
        this.fallTile = fallingBlockState;
    }
    public void onUpdate()
    {
        Block block = this.fallTile.getBlock();

        if (this.fallTile.getMaterial() == Material.AIR)
        {
            this.setDead();
        }
        else
        {
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;

            if (this.fallTime++ == 0)
            {
                BlockPos blockpos = new BlockPos(this);

                if (this.world.getBlockState(blockpos).getBlock() == block)
                {
                    this.world.setBlockToAir(blockpos);
                }
                else if (!this.world.isRemote)
                {
                    this.setDead();
                    return;
                }
            }

            if (!this.hasNoGravity())
            {
                this.motionY -= 0.03999999910593033D;
            }

            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);

            if (!this.world.isRemote)
            {
                BlockPos blockpos1 = new BlockPos(this);
                boolean flag = this.fallTile.getBlock() == Blocks.CONCRETE_POWDER;
                boolean flag1 = flag && this.world.getBlockState(blockpos1).getMaterial() == Material.WATER;
                double d0 = this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ;

                if (flag && d0 > 1.0D)
                {
                    RayTraceResult raytraceresult = this.world.rayTraceBlocks(new Vec3d(this.prevPosX, this.prevPosY, this.prevPosZ), new Vec3d(this.posX, this.posY, this.posZ), true);

                    if (raytraceresult != null && this.world.getBlockState(raytraceresult.getBlockPos()).getMaterial() == Material.WATER)
                    {
                        blockpos1 = raytraceresult.getBlockPos();
                        flag1 = true;
                    }
                }

                if (!this.onGround && !flag1)
                {
                    if (this.fallTime > 100 && !this.world.isRemote && (blockpos1.getY() < 1 || blockpos1.getY() > 256) || this.fallTime > 600)
                    {
                        if (this.shouldDropItem && this.world.getGameRules().getBoolean("doEntityDrops"))
                        {
                            this.entityDropItem(new ItemStack(block, 1, block.damageDropped(this.fallTile)), 0.0F);
                        }

                        this.setDead();
                    }
                }
                else
                {
                    IBlockState iblockstate = this.world.getBlockState(blockpos1);

                    if (this.world.isAirBlock(new BlockPos(this.posX, this.posY - 0.009999999776482582D, this.posZ))) //Forge: Don't indent below.
                        if (!flag1 && BlockFalling.canFallThrough(this.world.getBlockState(new BlockPos(this.posX, this.posY - 0.009999999776482582D, this.posZ))))
                        {
                            this.onGround = false;
                            return;
                        }

                    this.motionX *= 0.699999988079071D;
                    this.motionZ *= 0.699999988079071D;
                    this.motionY *= -0.5D;

                    if (iblockstate.getBlock() != Blocks.PISTON_EXTENSION)
                    {
                        this.setDead();

                        if (block instanceof BlockFallingLeafia)
                        {
                            ((BlockFallingLeafia)block).blockLanded(this.world, blockpos1, this.fallTile, this,rand);
                        }
                    }
                }
            }

            this.motionX *= 0.9800000190734863D;
            this.motionY *= 0.9800000190734863D;
            this.motionZ *= 0.9800000190734863D;
        }
    }
}
