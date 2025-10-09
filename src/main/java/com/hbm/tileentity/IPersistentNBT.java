package com.hbm.tileentity;

import com.hbm.api.tile.IWorldRenameable;
import com.hbm.util.CompatExternal;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.World;

/**
 * <p><strong>Warning:</strong> This works differently from upstream due to change in block destruction sequence!</p>
 *
 * <p>For blocks implementing this feature, the following methods <strong>must</strong> be overridden:
 * <ul>
 *   <li>{@link Block#breakBlock}</li>
 *   <li>{@link Block#dropBlockAsItemWithChance} (made empty)</li>
 *   <li>{@link Block#onBlockHarvested}</li>
 *   <li>{@link Block#onBlockPlacedBy} (unnecessary for subclasses of {@link com.hbm.blocks.BlockDummyable BlockDummyable})</li>
 * </ul>
 * </p>
 *
 * <p>Reference design: {@link net.minecraft.block.BlockShulkerBox BlockShulkerBox} and
 * {@link net.minecraft.tileentity.TileEntityShulkerBox TileEntityShulkerBox}</p>
 */
public interface IPersistentNBT {

    String NBT_PERSISTENT_KEY = "persistent";

    /**
     * Call super.breakBlock after this. Serverside guaranteed.
     */
    static void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tile = worldIn.getTileEntity(pos); // intentionally avoided CompatExternal.getCoreFromPos to prevent duplicates
        if (tile instanceof IPersistentNBT persistentTE && persistentTE.shouldDrop()) {
            ItemStack itemstack = new ItemStack(Item.getItemFromBlock(state.getBlock()), 1, state.getBlock().damageDropped(state));
            NBTTagCompound data = new NBTTagCompound();
            persistentTE.writeNBT(data);
            if (!data.isEmpty()) itemstack.setTagCompound(data);
            if (tile instanceof IWorldNameable nameable && nameable.hasCustomName()) {
                itemstack.setStackDisplayName(nameable.getName());
                if (tile instanceof IWorldRenameable rn) rn.setCustomName("");
            }
            Block.spawnAsEntity(worldIn, pos, itemstack);
        }
        if (state.hasComparatorInputOverride()) {
            worldIn.updateComparatorOutputLevel(pos, state.getBlock());
        }
    }

    static void onBlockPlacedBy(World worldIn, BlockPos pos, ItemStack stack) {
        if (!worldIn.isRemote && stack.hasTagCompound() && worldIn.getTileEntity(pos) instanceof IPersistentNBT persistentTE) {
            persistentTE.readNBT(stack.getTagCompound());
        }
        if (stack.hasDisplayName() && worldIn.getTileEntity(pos) instanceof IWorldRenameable renameable) {
            renameable.setCustomName(stack.getDisplayName());
        }
    }

    static void onBlockHarvested(World worldIn, BlockPos pos, EntityPlayer player){
        if (player.capabilities.isCreativeMode && CompatExternal.getCoreFromPos(worldIn, pos) instanceof IPersistentNBT persistentTE) {
            persistentTE.setDestroyedByCreativePlayer();
        }
    }

    default boolean shouldDrop() {
        return !isDestroyedByCreativePlayer();
    }

    void setDestroyedByCreativePlayer();

    boolean isDestroyedByCreativePlayer();

    void writeNBT(NBTTagCompound nbt);

    void readNBT(NBTTagCompound nbt);
}
