package com.hbm.util;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.hbm.lib.RefStrings;
import com.hbm.lib.maps.NonBlockingHashMapLong;
import com.hbm.main.MainRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;

@Mod.EventBusSubscriber(modid = RefStrings.MODID)
public final class DelayedTick {

    private static final NonBlockingHashMapLong<MpscCollector<Ticket>> SERVER_TASKS = new NonBlockingHashMapLong<>();
    private static final NonBlockingHashMapLong<NonBlockingHashMapLong<MpscCollector<Ticket>>> WORLD_TASKS = new NonBlockingHashMapLong<>();

    private DelayedTick() {
    }

    @CanIgnoreReturnValue
    public static Ticket scheduleServer(MinecraftServer server, int delayTicks, Runnable task) {
        if (server == null || task == null) return null;
        final long when = Integer.toUnsignedLong(server.getTickCounter() + Math.max(1, delayTicks));
        final Ticket t = new Ticket(task);
        SERVER_TASKS.computeIfAbsent(when, k -> new MpscCollector<>()).push(t);
        return t;
    }

    @CanIgnoreReturnValue
    public static Ticket scheduleServer(World world, int delayTicks, Runnable task) {
        if (world == null || world.isRemote) return null;
        return scheduleServer(world.getMinecraftServer(), delayTicks, task);
    }

    @CanIgnoreReturnValue
    public static Ticket scheduleWorld(World world, int delayTicks, Runnable task) {
        if (world == null || world.isRemote || task == null) return null;
        final long runAt = world.getTotalWorldTime() + Math.max(1, delayTicks);
        final long dim = world.provider.getDimension();
        final Ticket t = new Ticket(task);
        WORLD_TASKS.computeIfAbsent(dim, k -> new NonBlockingHashMapLong<>()).computeIfAbsent(runAt, k -> new MpscCollector<>()).push(t);
        return t;
    }

    @CanIgnoreReturnValue
    public static Ticket nextServerTick(MinecraftServer server, Runnable task) {
        return scheduleServer(server, 1, task);
    }

    @CanIgnoreReturnValue
    public static Ticket nextWorldTick(World world, Runnable task) {
        return scheduleWorld(world, 1, task);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        final MinecraftServer srv = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (srv == null) return;

        final long now = Integer.toUnsignedLong(srv.getTickCounter());
        final MpscCollector<Ticket> col = SERVER_TASKS.remove(now);
        runAll(col);
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent e) {
        if (e.world.isRemote || e.phase != TickEvent.Phase.END) return;

        final long dim = e.world.provider.getDimension();
        final long now = e.world.getTotalWorldTime();

        final NonBlockingHashMapLong<MpscCollector<Ticket>> byTime = WORLD_TASKS.get(dim);
        if (byTime == null) return;

        final MpscCollector<Ticket> col = byTime.remove(now);
        runAll(col);

        if (byTime.isEmpty()) {
            WORLD_TASKS.remove(dim, byTime);
        }
    }

    private static void runAll(MpscCollector<Ticket> collector) {
        if (collector == null) return;
        final List<Ticket> tasks = collector.drain();
        for (int i = tasks.size() - 1; i >= 0; i--) {
            tasks.get(i).runIfNotCancelled();
        }
    }

    public static final class Ticket {
        private final Runnable task;
        private volatile boolean cancelled;

        private Ticket(Runnable task) {
            this.task = task;
        }

        public void cancel() {
            cancelled = true;
        }

        private void runIfNotCancelled() {
            if (!cancelled) {
                try {
                    task.run();
                } catch (Throwable t) {
                    MainRegistry.logger.error("Exception in delayed task", t);
                }
            }
        }
    }
}
