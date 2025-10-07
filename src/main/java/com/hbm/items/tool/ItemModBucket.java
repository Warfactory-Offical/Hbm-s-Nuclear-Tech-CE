package com.hbm.items.tool;

import com.hbm.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemBucket;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ItemModBucket extends ItemBucket {

  protected int overrideFluidMeta = 0;
  protected Supplier<Block> containedFluidSupplier;
  protected Block containedFluid;

  public ItemModBucket(String name, Supplier<Block> fluidSupplier) {
    super(fluidSupplier.get());
    this.setTranslationKey(name);
    this.setRegistryName(name);
    this.containedFluidSupplier = fluidSupplier;

    ModItems.ALL_ITEMS.add(this);
  }

  public ItemModBucket(String name, Supplier<Block> fluid, int meta) {
    this(name, fluid);
    this.overrideFluidMeta = meta;
  }

  private Block getContainedFluid() {
    if (containedFluid == null) {
      containedFluid = containedFluidSupplier.get();
    }
    return containedFluid;
  }

  @Override
  public boolean tryPlaceContainedLiquid(@Nullable EntityPlayer player, World world, BlockPos pos) {

    Block fluid = getContainedFluid();
    if (fluid == Blocks.AIR) {
      return false;
    } else {
      Material material = world.getBlockState(pos).getMaterial();
      boolean flag = !material.isSolid();

      if (!world.isAirBlock(pos) && !flag) {
        return false;
      } else {
        if (world.provider.isNether() && fluid == Blocks.FLOWING_WATER) {
          world.playSound(
              null,
              (float) pos.getX() + 0.5F,
              (float) pos.getY() + 0.5F,
              (float) pos.getZ() + 0.5F,
              SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE,
              SoundCategory.BLOCKS,
              0.5F,
              2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);

          for (int l = 0; l < 8; ++l) {
            world.spawnParticle(
                EnumParticleTypes.SMOKE_LARGE,
                (double) pos.getX() + Math.random(),
                (double) pos.getY() + Math.random(),
                (double) pos.getZ() + Math.random(),
                0.0D,
                0.0D,
                0.0D);
          }
        } else {
          if (!world.isRemote && flag && !material.isLiquid()) {
            world.destroyBlock(pos, true);
          }

          IBlockState state = fluid.getStateFromMeta(overrideFluidMeta);
          world.setBlockState(pos, state, 3);
        }

        return true;
      }
    }
  }
}
