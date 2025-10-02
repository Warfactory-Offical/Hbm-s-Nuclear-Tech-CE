package com.hbm.handler.radiation;

import com.google.errorprone.annotations.DoNotCall;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

/**
 * We only have one radiation system, unlike upstream
 * this proxy is made to make porting easier.
 * Always call RadiationSystemNT directly when possible.
 *
 * @author mlbv
 */
public final class ChunkRadiationManager {
    public static final ProxyClass proxy = new ProxyClass();

    @SuppressWarnings("MethodMayBeStatic")
    public static final class ProxyClass {
        /**
         * Updates the radiation system, i.e. all worlds.
         * Doesn't need parameters because it governs the ENTIRE system.
         */
        @DoNotCall("unless you know what you are doing")
        public void updateSystem() {
            RadiationSystemNT.forceUpdateAll();
        }

        public float getRadiation(World world, BlockPos pos) {
            if (world.isRemote) return 0F;
            return RadiationSystemNT.getRadForCoord((WorldServer) world, pos);
        }

        public void setRadiation(World world, BlockPos pos, float rad) {
            if (world.isRemote) return;
            RadiationSystemNT.setRadForCoord((WorldServer) world, pos, rad);
        }

        @DoNotCall("no max rad limit")
        public void incrementRad(World world, BlockPos pos, float rad) {
            if (world.isRemote) return;
            RadiationSystemNT.incrementRad((WorldServer) world, pos, rad, Float.MAX_VALUE);
        }

        public void incrementRad(WorldServer world, BlockPos pos, float rad) {
            RadiationSystemNT.incrementRad(world, pos, rad, Float.MAX_VALUE);
        }

        public void incrementRad(World world, BlockPos pos, float rad, float max) {
            if (world.isRemote) return;
            RadiationSystemNT.incrementRad((WorldServer) world, pos, rad, max);
        }

        public void incrementRad(WorldServer world, BlockPos pos, float rad, float max) {
            RadiationSystemNT.incrementRad(world, pos, rad, max);
        }

        public void decrementRad(World world, BlockPos pos, float rad) {
            if (world.isRemote) return;
            RadiationSystemNT.decrementRad((WorldServer) world, pos, rad);
        }

        public void clearSystem(World world) {
            if (world.isRemote) return;
            RadiationSystemNT.jettisonData((WorldServer) world);
        }
    }
}
