package com.hbm.events;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Fired during ladder checks (injected before Forge's own logic).
 * <p>
 * Use {@link #setResult(Result)} to override:
 * - {@link Result#ALLOW}: return true immediately (entity is on a ladder)
 * - {@link Result#DENY}:  return false immediately (entity is NOT on a ladder)
 * - {@link Result#DEFAULT}: fall through to vanilla/Forge checks
 * <p>
 * Posted on the {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS EVENT_BUS}.
 */
@Event.HasResult
public final class CheckLadderEvent extends Event {

    private final IBlockState state;
    private final World world;
    private final BlockPos pos;
    private final EntityLivingBase entity;

    public CheckLadderEvent(@NotNull IBlockState state, @NotNull World world, @NotNull BlockPos pos, @NotNull EntityLivingBase entity) {
        this.state = state;
        this.world = world;
        this.pos = pos.toImmutable();
        this.entity = entity;
    }

    @NotNull
    public IBlockState getState() {
        return state;
    }

    @NotNull
    public World getWorld() {
        return world;
    }

    @NotNull
    public BlockPos getPos() {
        return pos;
    }

    @NotNull
    public EntityLivingBase getEntity() {
        return entity;
    }
}
