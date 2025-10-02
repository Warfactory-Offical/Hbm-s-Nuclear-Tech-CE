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
    @Deprecated
    public static final class proxy {
        /**
         * Updates the radiation system, i.e. all worlds.
         * Doesn't need parameters because it governs the ENTIRE system.
         */
        @DoNotCall("unless you know what you are doing")
        public static void updateSystem() {
            RadiationSystemNT.forceUpdateAll();
        }

        public static float getRadiation(World world, BlockPos pos) {
            if (world.isRemote) return 0F;
            return RadiationSystemNT.getRadForCoord((WorldServer) world, pos);
        }

        public static void setRadiation(World world, BlockPos pos, float rad) {
            if (world.isRemote) return;
            RadiationSystemNT.setRadForCoord((WorldServer) world, pos, rad);
        }

        @DoNotCall("no max rad limit")
        public static void incrementRad(World world, BlockPos pos, float rad) {
            if (world.isRemote) return;
            RadiationSystemNT.incrementRad((WorldServer) world, pos, rad, Float.MAX_VALUE);
        }

        public static void decrementRad(World world, BlockPos pos, float rad) {
            if (world.isRemote) return;
            RadiationSystemNT.decrementRad((WorldServer) world, pos, rad);
        }

        public static void clearSystem(World world) {
            if (world.isRemote) return;
            RadiationSystemNT.jettisonData((WorldServer) world);
        }
    }
}
