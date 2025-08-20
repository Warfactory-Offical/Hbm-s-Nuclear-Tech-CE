package com.hbm.core;

import com.hbm.events.CheckLadderEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class LadderHook {

    public static boolean onCheckLadderAllow(@NotNull IBlockState state, @NotNull World world, @NotNull BlockPos pos, @NotNull EntityLivingBase entity) {
        final CheckLadderEvent evt = new CheckLadderEvent(state, world, pos, entity);
        MinecraftForge.EVENT_BUS.post(evt);
        return evt.getResult() == Event.Result.ALLOW;
    }

    public static Event.Result onCheckLadder(@NotNull IBlockState state, @NotNull World world, @NotNull BlockPos pos, @NotNull EntityLivingBase entity) {
        final CheckLadderEvent evt = new CheckLadderEvent(state, world, pos, entity);
        MinecraftForge.EVENT_BUS.post(evt);
        return evt.getResult();
    }
}
