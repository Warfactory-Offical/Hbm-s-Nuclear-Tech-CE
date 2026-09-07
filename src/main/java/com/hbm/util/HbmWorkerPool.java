package com.hbm.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Shared worker pool for the per-tick work the server thread fans out and then immediately joins on
 * ({@code ModEventHandler#serverTickFirst}, {@code ModEventHandler#serverTickLast} and
 * {@code RadiationSystemNT#onServerTickLast}).
 * <p>
 * This work used to run on {@link ForkJoinPool#commonPool()}, which is a poor fit for anything on the tick
 * critical path on two counts.
 * <p>
 * The common pool's parallelism is {@code max(1, availableProcessors() - 1)}, and on Java 8 - which is what
 * this runs on - {@code CompletableFuture.screenExecutor} silently substitutes its internal thread-per-task
 * executor for the common pool whenever that parallelism is not greater than one. So on a server with two
 * cores or fewer, every {@code runAsync} against the common pool spawns a brand new OS thread that is
 * discarded when the task finishes: the seven-way fan-out in {@code serverTickFirst} creates seven fresh
 * threads per tick, every tick, and none of them is ever reused.
 * <p>
 * It is also shared with the entire JVM - every parallel stream and every default-executor
 * CompletableFuture in Minecraft, Forge and any other mod - so an unrelated long task can hold up the join
 * the tick is waiting on.
 * <p>
 * Parallelism keeps the common pool's formula with a floor of two so the fan-out is always genuinely
 * concurrent. Workers are started on demand, are daemons so they never hold the JVM open, and are named so
 * a profiler can attribute this work instead of it disappearing into anonymous common-pool workers.
 */
public final class HbmWorkerPool {

    private static final Logger LOGGER = LogManager.getLogger("HbmWorkerPool");
    private static final AtomicInteger THREAD_ID = new AtomicInteger();

    public static final ForkJoinPool POOL = new ForkJoinPool(
            Math.max(2, Runtime.getRuntime().availableProcessors() - 1),
            pool -> {
                ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                thread.setName("HBM Worker " + THREAD_ID.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            },
            (thread, throwable) -> LOGGER.error("Uncaught exception on {}", thread.getName(), throwable),
            // LIFO, matching the common pool this replaces: RadiationSystemNT#runParallelSimulation forks
            // and joins its per-dimension tasks, which is what LIFO local queues are for.
            false
    );

    private HbmWorkerPool() {
    }
}
