package com.hbm.items.tool;

import com.hbm.items.ItemBakedBase;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ItemFertilizer extends ItemBakedBase {

    public ItemFertilizer(String s){
        super(s);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        ItemStack stack = player.getHeldItem(hand);

        if (!player.canPlayerEdit(pos, facing, stack)) {
            return EnumActionResult.FAIL;
        }

        world.captureBlockSnapshots = false;

        boolean didSomething = false;

        for (int i = pos.getX() - 1; i <= pos.getX() + 1; i++) {
            for (int j = pos.getY() - 1; j <= pos.getY() + 1; j++) {
                for (int k = pos.getZ() - 1; k <= pos.getZ() + 1; k++) {
                    BlockPos p = new BlockPos(i, j, k);
                    boolean success = fertilize(world, i, j, k, player, hand, stack, p.equals(pos));
                    didSomething = didSomething || success;
                    if (success && !world.isRemote) {
                        world.playEvent(2005, p, 0);
                    }
                }
            }
        }

        if (didSomething && !player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }

        return didSomething ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
    }

    public static boolean useFertillizer(ItemStack stack, World world, int x, int y, int z) {

        if (!(world instanceof WorldServer)) return false;
        EntityPlayer player = FakePlayerFactory.getMinecraft((WorldServer) world);

        boolean didSomething = false;

        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                for (int k = z - 1; k <= z + 1; k++) {
                    boolean success = fertilize(world, i, j, k, player, EnumHand.MAIN_HAND, stack, i == x && j == y && k == z);
                    didSomething = didSomething || success;
                    if (success && !world.isRemote) {
                        world.playEvent(2005, new BlockPos(i, j, k), 0);
                    }
                }
            }
        }

        if (didSomething) stack.shrink(1);

        return didSomething;
    }

    public static boolean fertilize(World world, int x, int y, int z, EntityPlayer player, EnumHand hand, ItemStack stack, boolean force) {

        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        BonemealEvent event = new BonemealEvent(player, world, pos, state, hand, stack);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return false;
        }

        if (event.getResult() == Event.Result.ALLOW) {
            return true;
        }

        if (block instanceof IGrowable growable) {

            if (growable.canGrow(world, pos, state, world.isRemote)) {

                if (!world.isRemote) {
                    if (force || growable.canUseBonemeal(world, world.rand, pos, state)) {
                        growable.grow(world, world.rand, pos, state);
                    }
                }

                return true;
            }
        }

        return false;
    }
}
