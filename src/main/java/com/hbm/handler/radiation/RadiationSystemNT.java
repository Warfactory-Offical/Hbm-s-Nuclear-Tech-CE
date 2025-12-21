package com.hbm.handler.radiation;

import com.hbm.Tags;
import com.hbm.config.CompatibilityConfig;
import com.hbm.config.GeneralConfig;
import com.hbm.config.RadiationConfig;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.IRadResistantBlock;
import com.hbm.interfaces.ServerThread;
import com.hbm.lib.Library;
import com.hbm.lib.TLPool;
import com.hbm.lib.internal.UnsafeHolder;
import com.hbm.lib.maps.NonBlockingHashMapLong;
import com.hbm.lib.maps.NonBlockingHashSetLong;
import com.hbm.lib.maps.NonBlockingLong2LongHashMap;
import com.hbm.lib.queues.MpscUnboundedXaddArrayLongQueue;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.saveddata.AuxSavedData;
import com.hbm.util.DecodeException;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BitArray;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.*;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.*;

import static com.hbm.lib.internal.UnsafeHolder.U;

/**
 * A concurrent radiation system using Operator Splitting with exact pairwise exchange.
 * <p>
 * It solves for radiation density (&rho;) using the analytical solution for 2-node diffusion:
 * <center>
 * &Delta;&rho; = (&rho;<sub>eq</sub>&minus; &rho;) &times; (1 &minus; e<sup>-k&Delta;t</sup>)
 * </center>
 *
 * @author mlbv
 */
@Mod.EventBusSubscriber(modid = Tags.MODID)
public final class RadiationSystemNT {

    private static final int NO_POCKET = 15, NEI_SLOTS = 16, NEI_SHIFT = 1;
    private static final int[] BOUNDARY_MASKS = {0, 0, 0xF00, 0xF00, 0xFF0, 0xFF0}, LINEAR_OFFSETS = {-256, 256, -16, 16, -1, 1};
    private static final int[] FACE_DX = {0, 0, 0, 0, -1, 1}, FACE_DY = {-1, 1, 0, 0, 0, 0}, FACE_DZ = {0, 0, -1, 1, 0, 0};
    private static final int[] FACE_PLANE = new int[6 * 256];
    // 9950x, 32 view distance, extremely irradiated worst-case overworld takes < 3.2ms per step;
    // for normal world without strong artificial radiation source, it takes < 1ms and scales w.r.t. active pocket count.
    private static final boolean PROFILING = true;

    private static final int SECTION_BLOCK_COUNT = 4096;

    private static final String TAG_RAD = "hbmRadDataNT";
    private static final byte MAGIC_0 = (byte) 'N', MAGIC_1 = (byte) 'T', MAGIC_2 = (byte) 'X', FMT = 6;
    private static final Object NOT_RES = new Object();
    private static final ForkJoinPool RAD_POOL = ForkJoinPool.commonPool(); // safe: we don't lock in sim path
    private static final ConcurrentMap<WorldServer, WorldRadiationData> worldMap = new ConcurrentHashMap<>(4);
    private static final ThreadLocal<int[]> TL_FF_QUEUE = ThreadLocal.withInitial(() -> new int[SECTION_BLOCK_COUNT]);
    private static final ThreadLocal<PalScratch> TL_PAL_SCRATCH = ThreadLocal.withInitial(PalScratch::new);
    private static final ThreadLocal<int[]> TL_VOL_COUNTS = ThreadLocal.withInitial(() -> new int[NO_POCKET]);
    private static final ThreadLocal<double[]> TL_NEW_MASS = ThreadLocal.withInitial(() -> new double[NO_POCKET]);
    private static final ThreadLocal<double[]> TL_OLD_MASS = ThreadLocal.withInitial(() -> new double[NO_POCKET]);
    private static final ThreadLocal<int[]> TL_OVERLAPS = ThreadLocal.withInitial(() -> new int[NO_POCKET * NO_POCKET]);
    private static final ThreadLocal<long[]> TL_SUM_X = ThreadLocal.withInitial(() -> new long[NO_POCKET]);
    private static final ThreadLocal<long[]> TL_SUM_Y = ThreadLocal.withInitial(() -> new long[NO_POCKET]);
    private static final ThreadLocal<long[]> TL_SUM_Z = ThreadLocal.withInitial(() -> new long[NO_POCKET]);

    private static final int ACTIVE_STRIPES = computeActiveStripes(2);
    private static final int ACTIVE_STRIPE_MASK = ACTIVE_STRIPES - 1;
    private static ByteBuffer BUF = ByteBuffer.allocate(524_288);
    private static long ticks;
    private static CompletableFuture<Void> radiationFuture = CompletableFuture.completedFuture(null);
    private static Object[] STATE_CLASS;
    private static int tickDelay = 1;
    private static double dT = tickDelay / 20.0D;
    private static double diffusionDt = 10.0 * dT;
    private static double UU_E = Math.exp(-(diffusionDt / 128.0d));
    private static double retentionDt = Math.pow(0.99424, dT); // 2min
    private static double fogThreshold = 0.0D;

    static {
        int[] rowShifts = {4, 4, 8, 8, 8, 8}, colShifts = {0, 0, 0, 0, 4, 4}, bases = {0, 15 << 8, 0, 15 << 4, 0, 15};
        for (int face = 0; face < 6; face++) {
            int base = face << 8;
            int rowShift = rowShifts[face];
            int colShift = colShifts[face];
            int fixedBits = bases[face];
            int t = 0;
            for (int r = 0; r < 16; r++) {
                int rBase = r << rowShift;
                for (int c = 0; c < 16; c++) {
                    FACE_PLANE[base + (t++)] = rBase | (c << colShift) | fixedBits;
                }
            }
        }
    }

    private RadiationSystemNT() {
    }

    private static int computeActiveStripes(int scale) {
        int p = RAD_POOL.getParallelism();
        if (p < 3) p = 1;
        int desired = p * scale;
        if (desired > 128) desired = 128;
        return HashCommon.nextPowerOfTwo(desired);
    }

    public static void onLoadComplete() {
        // noinspection deprecation
        STATE_CLASS = new Object[Block.BLOCK_STATE_IDS.size() + 1024];
        tickDelay = RadiationConfig.radTickRate;
        if (tickDelay <= 0) throw new IllegalStateException("Radiation tick rate must be positive");
        dT = tickDelay / 20.0D;
        diffusionDt = RadiationConfig.radDiffusivity * dT;
        if (diffusionDt <= 0.0D || !Double.isFinite(diffusionDt))
            throw new IllegalStateException("Radiation diffusivity must be positive and finite");
        double hl = RadiationConfig.radHalfLifeSeconds;
        if (hl <= 0.0D || !Double.isFinite(hl)) throw new IllegalStateException("Radiation HalfLife must be positive and finite");
        retentionDt = Math.exp(Math.log(0.5) * (dT / hl));
        double ch = RadiationConfig.fogCh;
        fogThreshold = ch <= 0.0D ? 0.0D : (dT / ch);
        UU_E = Math.exp(-(diffusionDt / 128.0d));
    }

    public static void onServerStarting(FMLServerStartingEvent event) {
    }

    public static void onServerStopping(FMLServerStoppingEvent ignoredEvent) {
        try {
            if (radiationFuture != null) radiationFuture.join();
        } catch (Exception e) {
            MainRegistry.logger.error("Radiation system error during shutdown.", e);
        } finally {
            worldMap.clear();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onServerTickFirst(TickEvent.ServerTickEvent e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation || e.phase != Phase.START) return;
        if (radiationFuture != null && !radiationFuture.isDone()) {
            try {
                // this provides a quiescent server thread and sufficient happens-before for structural updates
                radiationFuture.join();
            } catch (Exception ex) {
                MainRegistry.logger.error("Radiation simulation failed", ex);
            }
        }
        if (ticks % 200 == 13) {
            for (WorldRadiationData wd : worldMap.values()) {
                wd.cleanupPools();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onServerTickLast(TickEvent.ServerTickEvent e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation || e.phase != Phase.END) return;
        ticks++;
        if ((ticks + 17) % tickDelay == 0) {
            radiationFuture = CompletableFuture.runAsync(RadiationSystemNT::runParallelSimulation, RAD_POOL);
        }
    }

    @SubscribeEvent
    public static void onWorldUpdate(TickEvent.WorldTickEvent e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation || e.world.isRemote) return;
        WorldServer worldServer = (WorldServer) e.world;

        if (e.phase == Phase.START) {
            RadiationWorldHandler.handleWorldDestruction(worldServer);
        }
        if (GeneralConfig.enableRads) {
            int thunder = AuxSavedData.getThunder(worldServer);
            if (thunder > 0) AuxSavedData.setThunder(worldServer, thunder - 1);
        }
    }

    @ServerThread
    public static void jettisonData(WorldServer world) {
        WorldRadiationData data = worldMap.get(world);
        if (data == null) return;
        data.clearAllSections();
        data.clearDirtyAll();
        data.clearQueues();
        data.destructionQueue.clear(true);
        data.clearPendingAll();
        data.cleanupPools();
    }

    @ServerThread
    public static void incrementRad(WorldServer world, BlockPos pos, double amount, double max) {
        if (isOutsideWorldY(pos.getY())) return;
        if (amount <= 0 || max <= 0) return;
        long posLong = pos.toLong();
        long sck = Library.blockPosToSectionLong(posLong);
        long ck = Library.sectionToChunkLong(sck);
        Chunk chunk = world.getChunkProvider().loadedChunks.get(ck);
        if (chunk == null) return;
        if (isResistantAt(world, chunk, pos)) return;
        WorldRadiationData data = getWorldRadData(world);
        ChunkRef owner = data.getOrCreateChunkRef(ck);
        owner.mcChunk = chunk;
        SectionRef sc = ensureSectionForWrite(data, owner, sck);
        if (sc.pocketCount <= 0) return;
        int pocketIndex = sc.getPocketIndex(posLong);
        if (pocketIndex < 0) return;
        double current = sc.getRad(pocketIndex);
        if (current >= max) return;
        double next = Math.min(current + amount, max);
        if (!Double.isFinite(next) || next < 0) {
            MainRegistry.logger.warn(
                    "[RadiationSystemNT] Cancelled invalid incrementRad at {} in dimension {} ({}) due to overflow. Old: {}, Adding: {}, Result: {}.",
                    pos, world.provider.getDimension(), world.provider.getDimensionType().getName(), current, amount, next);
            return;
        }
        sc.setRad(pocketIndex, next);
        if (sc.owner.setActiveBit(sc.sy, pocketIndex)) {
            data.enqueueActiveNext(pocketKey(sck, pocketIndex));
        }
        chunk.markDirty();
    }

    @ServerThread
    public static void decrementRad(WorldServer world, BlockPos pos, double amount) {
        if (isOutsideWorldY(pos.getY()) || amount <= 0) return;
        long posLong = pos.toLong();
        long sck = Library.blockPosToSectionLong(posLong);
        long ck = Library.sectionToChunkLong(sck);
        Chunk chunk = world.getChunkProvider().loadedChunks.get(ck);
        if (chunk == null) return;
        if (isResistantAt(world, chunk, pos)) return;
        WorldRadiationData data = getWorldRadData(world);
        ChunkRef owner = data.getOrCreateChunkRef(ck);
        owner.mcChunk = chunk;
        SectionRef sc = ensureSectionForWrite(data, owner, sck);
        int pocketIndex = sc.getPocketIndex(posLong);
        if (pocketIndex < 0) return;
        double current = sc.getRad(pocketIndex);
        double next = Math.max(data.minBound, current - amount);
        sc.setRad(pocketIndex, next);
        long pk = pocketKey(sck, pocketIndex);
        if (!nearZero(next)) {
            if (sc.owner.setActiveBit(sc.sy, pocketIndex)) {
                data.enqueueActiveNext(pk);
            }
        } else {
            sc.owner.clearActiveBit(sc.sy, pocketIndex);
        }
        chunk.markDirty();
    }

    /**
     * @param amount clamped to [-backGround, Double.MAX_VALUE / 1536]
     */
    @ServerThread
    public static void setRadForCoord(WorldServer world, BlockPos pos, double amount) {
        if (isOutsideWorldY(pos.getY())) return;
        long posLong = pos.toLong();
        long sck = Library.blockPosToSectionLong(posLong);
        long ck = Library.sectionToChunkLong(sck);
        Long2ObjectMap<Chunk> loaded = world.getChunkProvider().loadedChunks;
        Chunk chunk = loaded.get(ck);
        if (chunk == null) return;
        if (isResistantAt(world, chunk, pos)) return;
        WorldRadiationData data = getWorldRadData(world);
        ChunkRef owner = data.getOrCreateChunkRef(ck);
        owner.mcChunk = chunk;
        SectionRef sc = ensureSectionForWrite(data, owner, sck);
        int pocketIndex = sc.getPocketIndex(posLong);
        if (pocketIndex < 0) return;
        double v = Math.max(data.minBound, Math.min(amount, Double.MAX_VALUE / 1536));
        sc.setRad(pocketIndex, v);
        long pk = pocketKey(sck, pocketIndex);
        if (!nearZero(v)) {
            if (sc.owner.setActiveBit(sc.sy, pocketIndex)) {
                data.enqueueActiveNext(pk);
            }
        } else {
            sc.owner.clearActiveBit(sc.sy, pocketIndex);
        }
        chunk.markDirty();
    }

    @ServerThread
    public static double getRadForCoord(WorldServer world, BlockPos pos) {
        if (isOutsideWorldY(pos.getY())) return 0D;
        long posLong = pos.toLong();
        long sck = Library.blockPosToSectionLong(posLong);
        long ck = Library.sectionToChunkLong(sck);
        Chunk chunk = world.getChunkProvider().loadedChunks.get(ck);
        if (chunk == null) return 0D;
        WorldRadiationData data = worldMap.get(world);
        if (data == null) return 0D;
        if (isResistantAt(world, chunk, pos)) return 0D;
        SectionRef sc = data.getSection(sck);
        if (sc == null || sc.pocketCount <= 0) {
            data.markDirty(sck);
            return 0D;
        }
        int pocketIndex = sc.getPocketIndex(posLong);
        return (pocketIndex >= 0) ? sc.getRad(pocketIndex) : 0D;
    }

    @ServerThread
    public static void markSectionForRebuild(World world, BlockPos pos) {
        if (world.isRemote || !GeneralConfig.advancedRadiation) return;
        if (isOutsideWorldY(pos.getY())) return;
        WorldServer ws = (WorldServer) world;
        long sck = Library.blockPosToSectionLong(pos);
        long ck = Library.sectionToChunkLong(sck);
        Chunk chunk = ws.getChunkProvider().loadedChunks.get(ck);
        if (chunk == null) return;
        WorldRadiationData data = getWorldRadData(ws);
        data.markDirty(sck);
        chunk.markDirty();
    }

    @ServerThread
    static void handleWorldDestruction(WorldServer world) {
        WorldRadiationData data = worldMap.get(world);
        if (data == null) return;

        long pocketKey;
        if (tickDelay == 1) {
            pocketKey = data.pocketToDestroy;
            data.pocketToDestroy = Long.MIN_VALUE;
        } else {
            pocketKey = data.destructionQueue.poll();
        }
        if (pocketKey == Long.MIN_VALUE) return;

        int cx = Library.getSectionX(pocketKey);
        int yz = Library.getSectionY(pocketKey);
        int cz = Library.getSectionZ(pocketKey);
        int cy = yz >>> 4;
        int targetPocketIndex = yz & 15;

        SectionRef sc = data.getSection(Library.sectionToLong(cx, cy, cz));
        if (sc == null) return;

        int baseX = cx << 4;
        int baseY = cy << 4;
        int baseZ = cz << 4;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        Chunk chunk = sc.owner.getChunk(world.getChunkProvider().loadedChunks);
        if (chunk == null) return;
        ExtendedBlockStorage storage = chunk.getBlockStorageArray()[cy];
        if (storage == null || storage.isEmpty()) return;
        BlockStateContainer container = storage.data;

        if (sc instanceof UniformSectionRef) {
            if (targetPocketIndex != 0) return;
            for (int i = 0; i < SECTION_BLOCK_COUNT; i++) {
                if (world.rand.nextInt(3) != 0) continue;
                IBlockState state = container.get(i);
                pos.setPos(baseX + Library.getLocalX(i), baseY + Library.getLocalY(i), baseZ + Library.getLocalZ(i));
                if (state.getMaterial() == Material.AIR) continue;
                RadiationWorldHandler.decayBlock(world, pos, state, false);
            }
            return;
        }

        for (int i = 0; i < SECTION_BLOCK_COUNT; i++) {
            if (world.rand.nextInt(3) != 0) continue;
            int actualPocketIndex = sc.paletteIndexOrNeg(i);
            if (actualPocketIndex < 0) continue;
            if (actualPocketIndex != targetPocketIndex) continue;
            IBlockState state = container.get(i);
            if (state.getMaterial() == Material.AIR) continue;
            pos.setPos(baseX + Library.getLocalX(i), baseY + Library.getLocalY(i), baseZ + Library.getLocalZ(i));
            RadiationWorldHandler.decayBlock(world, pos, state, false);
        }
    }

    private static void runParallelSimulation() {
        WorldRadiationData[] all = worldMap.values().toArray(new WorldRadiationData[0]);
        int n = all.length;
        if (n == 0) return;

        if (n == 1) {
            WorldRadiationData data = all[0];
            if (data.world.getMinecraftServer() == null) return;
            try {
                processWorldSimulation(data);
            } catch (Throwable t) {
                MainRegistry.logger.error("Error in async rad simulation for world {}", data.world.provider.getDimension(), t);
            }
        } else {
            ForkJoinTask<?>[] tasks = new ForkJoinTask<?>[n];
            for (int i = 0; i < n; i++) {
                final WorldRadiationData data = all[i];
                tasks[i] = ForkJoinTask.adapt(() -> {
                    if (data.world.getMinecraftServer() == null) return;
                    try {
                        processWorldSimulation(data);
                    } catch (Throwable t) {
                        MainRegistry.logger.error("Error in async rad simulation for world {}", data.world.provider.getDimension(), t);
                    }
                });
            }
            ForkJoinTask.invokeAll(tasks);
        }
    }

    private static void processWorldSimulation(WorldRadiationData data) {
        long time = System.nanoTime();
        rebuildDirtySections(data);
        data.swapQueues();

        final int epoch = data.nextWorkEpoch();
        final MpscUnboundedXaddArrayLongQueue[] qs = data.activeQueuesCurrent;

        if (ACTIVE_STRIPES == 1) {
            new DrainStripeTask(data, qs[0], 0, epoch).invoke();
        } else {
            ForkJoinTask<?>[] drainTasks = new ForkJoinTask<?>[ACTIVE_STRIPES];
            for (int s = 0; s < ACTIVE_STRIPES; s++) {
                drainTasks[s] = new DrainStripeTask(data, qs[s], s, epoch);
            }
            ForkJoinTask.invokeAll(drainTasks);
        }

        int activeCount = 0;
        int totalTouchedCount = 0;
        for (int s = 0; s < ACTIVE_STRIPES; s++) {
            activeCount += data.activeStripeCounts[s];
            totalTouchedCount += data.touchedStripeCounts[s];
        }
        if (activeCount == 0) {
            logProfilingMessage(data, time);
            return;
        }
        long[] buf = data.activeBuf;
        SectionRef[] refs = data.activeRefs;
        if (buf.length < activeCount) {
            buf = data.activeBuf = new long[activeCount + (activeCount >>> 1)];
            refs = data.activeRefs = new SectionRef[buf.length];
        }
        int pos = 0;
        for (int s = 0; s < ACTIVE_STRIPES; s++) {
            int c = data.activeStripeCounts[s];
            if (c == 0) continue;
            System.arraycopy(data.activeStripeBufs[s], 0, buf, pos, c);
            System.arraycopy(data.activeStripeRefs[s], 0, refs, pos, c);
            Arrays.fill(data.activeStripeRefs[s], 0, c, null);
            pos += c;
        }
        Arrays.fill(data.parityCounts, 0);
        for (int b = 0; b < 4; b++) {
            if (data.parityBuckets[b].length < totalTouchedCount) data.parityBuckets[b] = new ChunkRef[totalTouchedCount + 128];
        }
        for (int s = 0; s < ACTIVE_STRIPES; s++) {
            int c = data.touchedStripeCounts[s];
            if (c == 0) continue;
            ChunkRef[] src = data.touchedStripeRefs[s];
            for (int i = 0; i < c; i++) {
                ChunkRef cr = src[i];
                long ck = cr.ck;
                int bucketIdx = (int) ((ck & 1) | ((ck >>> 31) & 2));
                int idx = data.parityCounts[bucketIdx]++;
                data.parityBuckets[bucketIdx][idx] = cr;
            }
            Arrays.fill(src, 0, c, null);
        }
        final int maxWake = (activeCount <= (Integer.MAX_VALUE / 90)) ? (activeCount * 90) : Integer.MAX_VALUE;
        final LongBag wokenBag = data.getWokenBag(maxWake);
        wokenBag.clear();
        runExactExchangeSweeps(data, wokenBag);
        final int wokenCount = wokenBag.size();
        if (wokenCount > 0) {
            ForkJoinTask.invokeAll(new FinalizeTask(data, buf, refs, 0, activeCount), new FinalizeBagTask(data, wokenBag));
        } else {
            new FinalizeTask(data, buf, refs, 0, activeCount).invoke();
        }
        Arrays.fill(data.activeRefs, 0, activeCount, null);
        logProfilingMessage(data, time);
    }

    private static void logProfilingMessage(WorldRadiationData data, long time) {
        if (!PROFILING) return;
        double durationMs = (System.nanoTime() - time) / 1_000_000.0D;
        data.executionTimeAccumulator += durationMs;
        data.executionSampleCount++;
        if (data.executionSampleCount >= 200) {
            double average = data.executionTimeAccumulator / 200.0D;
            MainRegistry.logger.info("dim {} = {} ms", data.world.provider.getDimension(), average);
            data.executionTimeAccumulator = 0.0D;
            data.executionSampleCount = 0;
        }
    }

    private static void runExactExchangeSweeps(WorldRadiationData data, LongBag wakeBag) {
        ChunkRef[][] buckets = data.parityBuckets;
        int[] counts = data.parityCounts;
        ForkJoinTask.invokeAll(new DiffuseXTask(data, buckets[0], 0, counts[0], wakeBag), new DiffuseXTask(data, buckets[2], 0, counts[2], wakeBag));
        ForkJoinTask.invokeAll(new DiffuseXTask(data, buckets[1], 0, counts[1], wakeBag), new DiffuseXTask(data, buckets[3], 0, counts[3], wakeBag));
        ForkJoinTask.invokeAll(new DiffuseZTask(data, buckets[0], 0, counts[0], wakeBag), new DiffuseZTask(data, buckets[1], 0, counts[1], wakeBag));
        ForkJoinTask.invokeAll(new DiffuseZTask(data, buckets[2], 0, counts[2], wakeBag), new DiffuseZTask(data, buckets[3], 0, counts[3], wakeBag));
        ForkJoinTask<?>[] yTasks0 = new ForkJoinTask[4];
        for (int b = 0; b < 4; b++) {
            yTasks0[b] = new DiffuseYTask(data, buckets[b], 0, counts[b], 0, wakeBag);
        }
        ForkJoinTask.invokeAll(yTasks0);
        ForkJoinTask<?>[] yTasks1 = new ForkJoinTask[4];
        for (int b = 0; b < 4; b++) {
            yTasks1[b] = new DiffuseYTask(data, buckets[b], 0, counts[b], 1, wakeBag);
        }
        ForkJoinTask.invokeAll(yTasks1);
    }

    private static boolean exchangePairExact(SectionRef a, int ai, int faceA, SectionRef b, int bi, int faceB, int area) {
        if (area <= 0) return false;

        double ra = a.getRad(ai);
        double rb = b.getRad(bi);
        if (ra == rb) return false;

        double invVa = a.getInvVolume(ai);
        double invVb = b.getInvVolume(bi);
        double denomInv = invVa + invVb;
        if (!(denomInv > 0.0d)) return false;

        double distA = a.getFaceDist(ai, faceA);
        double distB = b.getFaceDist(bi, faceB);
        double distSum = distA + distB;
        if (!(distSum > 0.0d)) return false;

        double g = ((double) area) / distSum;
        double rate = g * denomInv * diffusionDt;
        if (!(rate > 0.0d)) return false;

        double e = Math.exp(-rate);
        double rStar = (ra * invVb + rb * invVa) / denomInv;
        double na = rStar + (ra - rStar) * e;
        double nb = rStar + (rb - rStar) * e;

        if (na == ra && nb == rb) return false;

        a.setRad(ai, na);
        b.setRad(bi, nb);
        return true;
    }

    private static boolean exchangeFaceExact(Long2ObjectMap<Chunk> loaded, long aKey, SectionRef a, int faceA, long bKey, SectionRef b, int faceB,
                                             LongBag wakeBag) {
        boolean changedA = false, changedB = false;

        if (a instanceof MultiSectionRef ma) {
            if (b instanceof MultiSectionRef mb) {
                int aCount = ma.pocketCount & 0xFF;
                int bCount = Math.min(mb.pocketCount & 0xFF, NO_POCKET);
                int stride = 6 * NEI_SLOTS;
                char[] conn = ma.connectionArea;
                byte[] faceAct = ma.faceActive;
                int faceBase0 = faceA << 4;

                for (int pi = 0; pi < aCount; pi++) {
                    if (faceAct[pi * 6 + faceA] == 0) continue;
                    int base = pi * stride + faceBase0;
                    if (conn[base] == 0) continue;

                    for (int npi = 0; npi < bCount; npi++) {
                        int area = conn[base + NEI_SHIFT + npi];
                        if (area == 0) continue;
                        if (exchangePairExact(ma, pi, faceA, mb, npi, faceB, area)) {
                            changedA = changedB = true;
                            wakeIfNeeded(aKey, ma, pi, wakeBag);
                            wakeIfNeeded(bKey, mb, npi, wakeBag);
                        }
                    }
                }
            } else {
                int aCount = ma.pocketCount & 0xFF;
                int stride = 6 * NEI_SLOTS;
                char[] conn = ma.connectionArea;
                byte[] faceAct = ma.faceActive;
                int slot0 = (faceA << 4) + NEI_SHIFT;

                for (int pi = 0; pi < aCount; pi++) {
                    if (faceAct[pi * 6 + faceA] == 0) continue;
                    int area = conn[pi * stride + slot0];
                    if (area == 0) continue;
                    if (exchangePairExact(ma, pi, faceA, b, 0, faceB, area)) {
                        changedA = true;
                        changedB = true;
                        wakeIfNeeded(aKey, ma, pi, wakeBag);
                        wakeIfNeeded(bKey, b, 0, wakeBag);
                    }
                }
            }
        } else if (b instanceof MultiSectionRef mb) {
            changedB = exchangeFaceExact(loaded, bKey, mb, faceB, aKey, a, faceA, wakeBag);
            changedA = changedB;
        } else {
            if (a instanceof UniformSectionRef ua && b instanceof UniformSectionRef ub) {
                double ra = ua.rad, rb = ub.rad;
                if (ra != rb) {
                    double avg = 0.5d * (ra + rb);
                    double delta = 0.5d * (ra - rb) * UU_E;
                    ua.rad = avg + delta;
                    ub.rad = avg - delta;
                    changedA = changedB = true;
                    wakeIfNeeded(aKey, ua, 0, wakeBag);
                    wakeIfNeeded(bKey, ub, 0, wakeBag);
                }
            } else {
                int area;
                if (a instanceof SingleMaskedSectionRef sa && b instanceof UniformSectionRef) {
                    area = sa.getFaceCount(faceA);
                } else if (a instanceof UniformSectionRef && b instanceof SingleMaskedSectionRef sb) {
                    area = sb.getFaceCount(faceB);
                } else if (a instanceof SingleMaskedSectionRef sa && b instanceof SingleMaskedSectionRef) {
                    long conns = sa.connections;
                    area = (int) ((conns >>> (faceA * 9)) & 0x1FFL);
                } else {
                    area = 0;
                }
                if (area > 0 && exchangePairExact(a, 0, faceA, b, 0, faceB, area)) {
                    changedA = changedB = true;
                    wakeIfNeeded(aKey, a, 0, wakeBag);
                    wakeIfNeeded(bKey, b, 0, wakeBag);
                }
            }
        }

        if (changedA) {
            Chunk ca = a.owner.getChunk(loaded);
            if (ca != null) ca.markDirty();
        }
        if (changedB) {
            Chunk cb = b.owner.getChunk(loaded);
            if (cb != null) cb.markDirty();
        }
        return changedA | changedB;
    }

    private static void wakeIfNeeded(long sectionKey, SectionRef sc, int pi, LongBag wakeBag) {
        if (wakeBag == null) return;
        double v = sc.getRad(pi);
        if (!Double.isFinite(v) || nearZero(v)) return;
        if (sc.owner.setActiveBit(sc.sy, pi)) {
            wakeBag.tryAdd(pocketKey(sectionKey, pi));
        }
    }

    private static void finalizeRange(WorldRadiationData data, long[] keys, @Nullable SectionRef[] refs, int start, int end) {
        Long2ObjectMap<Chunk> loaded = data.world.getChunkProvider().loadedChunks;
        double minB = data.minBound;
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        for (int i = start; i < end; i++) {
            long pk = keys[i];
            int pi = pocketIndexFromPocketKey(pk);
            SectionRef sc;
            if (refs != null) {
                sc = refs[i];
            } else {
                sc = data.getSection(sectionKeyFromPocketKey(pk));
            }
            if (sc == null || sc.pocketCount <= 0) {
                data.deactivatePocket(pk);
                continue;
            }

            int len = sc.pocketCount & 0xFF;
            if (pi < 0 || pi >= len) {
                sc.owner.clearActiveBit(sc.sy, pi);
                continue;
            }

            double prev = sc.getRad(pi);
            if (prev > RadiationConfig.fogRad && rnd.nextDouble() < fogThreshold) {
                data.spawnFog(sc, pi, rnd);
            }

            double next = prev * retentionDt;
            if (!Double.isFinite(next)) {
                next = 0.0d;
            }
            if (next < minB) next = minB;
            if (nearZero(next)) next = 0.0d;

            if (next != prev) {
                sc.setRad(pi, next);
                Chunk c = sc.owner.getChunk(loaded);
                if (c != null) c.markDirty();
                if (prev >= 5.0 && rnd.nextDouble() < 0.01 && pk != Long.MIN_VALUE) {
                    if (tickDelay == 1) data.pocketToDestroy = pk;
                    else data.destructionQueue.offer(pk);
                }
            }

            if (next == 0.0d) {
                sc.owner.clearActiveBit(sc.sy, pi);
            } else {
                data.enqueueActiveNext(pk);
            }
        }
    }

    private static void rebuildDirtySections(WorldRadiationData data) {
        if (data.dirtyQueue.isEmpty()) return;
        final LongArrayList toRebuild = data.dirtyToRebuildScratch;
        final Long2IntOpenHashMap chunkMasks = data.dirtyChunkMasksScratch;
        toRebuild.clear();
        chunkMasks.clear();

        while (true) {
            final long sck = data.dirtyQueue.poll();
            if (sck == Long.MIN_VALUE) break;
            if (!data.dirtySections.remove(sck)) continue;
            final int sy = Library.getSectionY(sck);
            if (isInvalidSectionY(sy)) continue;
            toRebuild.add(sck);
            final long ck = Library.sectionToChunkLong(sck);
            chunkMasks.put(ck, chunkMasks.get(ck) | (1 << sy));
        }

        final int size = toRebuild.size();
        if (size == 0) return;

        final int batchCount = chunkMasks.size();
        data.ensureDirtyBatchCapacity(batchCount);

        final long[] chunkKeys = data.dirtyChunkKeysScratch;
        final int[] masks = data.dirtyChunkMasksScratchArr;

        int bi = 0;
        ObjectIterator<Long2IntMap.Entry> iterator = chunkMasks.long2IntEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Long2IntMap.Entry e = iterator.next();
            chunkKeys[bi] = e.getLongKey();
            masks[bi] = e.getIntValue();
            bi++;
        }

        new RebuildChunkBatchTask(data, chunkKeys, masks, 0, batchCount).invoke();
        data.relinkKeys(toRebuild.elements(), size);
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation) return;
        if (e.getWorld().isRemote) return;

        WorldRadiationData data = getWorldRadData((WorldServer) e.getWorld());
        Chunk chunk = e.getChunk();
        ChunkRef cr = data.onChunkLoaded(chunk);
        for (int sy = 0; sy < 16; sy++) {
            if (cr.sec[sy] == null) {
                data.markDirty(Library.sectionToLong(chunk.x, sy, chunk.z));
            }
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation) return;
        if (e.getWorld().isRemote) return;

        WorldRadiationData data = getWorldRadData((WorldServer) e.getWorld());
        Chunk chunk = e.getChunk();
        data.unloadChunk(chunk.x, chunk.z);
    }

    @SubscribeEvent
    public static void onChunkDataLoad(ChunkDataEvent.Load e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation) return;
        if (e.getWorld().isRemote) return;
        WorldRadiationData data = getWorldRadData((WorldServer) e.getWorld());

        Chunk chunk = e.getChunk();
        NBTTagCompound nbt = e.getData();
        try {
            byte[] payload = null;
            int id = nbt.getTagId(TAG_RAD);
            if (id == Constants.NBT.TAG_COMPOUND) {
                WorldProvider provider = e.getWorld().provider;
                MainRegistry.logger.warn("[RadiationSystemNT] Skipped legacy radiation data for chunk {} in dimension {} ({})", chunk.getPos(),
                        provider.getDimension(), provider.getDimensionType().getName());
            } else if (id == Constants.NBT.TAG_BYTE_ARRAY) {
                byte[] raw = nbt.getByteArray(TAG_RAD);
                payload = verifyPayload(raw);
            }
            data.readPayload(chunk.x, chunk.z, payload);
        } catch (BufferUnderflowException | DecodeException ex) {
            WorldProvider provider = e.getWorld().provider;
            MainRegistry.logger.error("[RadiationSystemNT] Failed to decode data for chunk {} in dimension {} ({})", chunk.getPos(),
                    provider.getDimension(), provider.getDimensionType().getName(), ex);
            nbt.removeTag(TAG_RAD);
        }
    }

    @SubscribeEvent
    public static void onChunkDataSave(ChunkDataEvent.Save e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation) return;
        if (e.getWorld().isRemote) return;
        WorldRadiationData data = getWorldRadData((WorldServer) e.getWorld());

        Chunk chunk = e.getChunk();
        byte[] payload = tryEncodePayload(data, chunk.x, chunk.z);

        if (payload != null && payload.length > 0) {
            e.getData().setByteArray(TAG_RAD, payload);
        } else if (e.getData().hasKey(TAG_RAD)) {
            e.getData().removeTag(TAG_RAD);
        }
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation) return;
        if (e.getWorld().isRemote) return;
        worldMap.computeIfAbsent((WorldServer) e.getWorld(), WorldRadiationData::new);
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload e) {
        if (e.getWorld().isRemote) return;
        worldMap.remove((WorldServer) e.getWorld());
    }

    private static byte[] verifyPayload(byte[] raw) throws DecodeException {
        if (raw.length == 0) return null;
        if (raw.length < 6) throw new DecodeException("Payload too short: " + raw.length);
        if (raw[0] != MAGIC_0 || raw[1] != MAGIC_1 || raw[2] != MAGIC_2) throw new DecodeException("Invalid magic");
        byte fmt = raw[3];
        if (fmt != FMT) throw new DecodeException("Unknown format: " + fmt);
        return raw;
    }

    private static @Nullable byte[] tryEncodePayload(WorldRadiationData data, int cx, int cz) {
        ByteBuffer buf = BUF; // by contract ChunkDataEvent.Save is never called off-thread
        buf.clear();
        buf = ensureCapacity(buf, 2);
        buf.putShort((short) 0);
        int count = 0;

        for (int sy = 0; sy < 16; sy++) {
            long sck = Library.sectionToLong(cx, sy, cz);
            SectionRef sc = data.getSection(sck);
            if (sc == null || sc.pocketCount <= 0) continue;

            int len = sc.pocketCount & 0xFF;
            if (len > NO_POCKET) len = NO_POCKET;
            if (sc instanceof UniformSectionRef) len = 1;

            for (int p = 0; p < len; p++) {
                double v = sc.getRad(p);
                if (!Double.isFinite(v) || nearZero(v)) continue;
                buf = ensureCapacity(buf, 1 + 8);
                buf.put((byte) ((sy << 4) | (p & 15)));
                buf.putDouble(v);
                count++;
            }
        }

        BUF = buf;
        if (count == 0) return null;
        buf.putShort(0, (short) count);
        buf.flip();
        byte[] out = new byte[4 + buf.limit()];
        out[0] = MAGIC_0;
        out[1] = MAGIC_1;
        out[2] = MAGIC_2;
        out[3] = FMT;
        buf.get(out, 4, buf.limit());
        return out;
    }

    private static ByteBuffer ensureCapacity(ByteBuffer buf, int extra) {
        if (buf.remaining() >= extra) return buf;
        int need = buf.position() + extra;
        int ncap = buf.capacity();
        while (ncap < need) ncap = ncap + (ncap >>> 1) + 64;
        ByteBuffer nb = ByteBuffer.allocate(ncap);
        buf.flip();
        nb.put(buf);
        return nb;
    }

    @NotNull
    private static WorldRadiationData getWorldRadData(WorldServer world) {
        return worldMap.computeIfAbsent(world, WorldRadiationData::new);
    }

    private static boolean isResistantAt(WorldServer w, Chunk chunk, BlockPos pos) {
        Block b = chunk.getBlockState(pos).getBlock();
        return (b instanceof IRadResistantBlock r) && r.isRadResistant(w, pos);
    }

    private static boolean isOutsideWorldY(int y) {
        return (y & ~255) != 0;
    }

    private static boolean isInvalidSectionY(int sy) {
        return (sy & ~15) != 0;
    }

    private static boolean nearZero(double v) {
        return Math.abs(v) < 1.0e-5d;
    }

    private static long pocketKey(long sectionKey, int pocketIndex) {
        int sy = Library.getSectionY(sectionKey);
        // sectionKey allows sy ∈ [-524_288, 524_287]
        return Library.setSectionY(sectionKey, (sy << 4) | (pocketIndex & 15));
    }

    private static int pocketIndexFromPocketKey(long pocketKey) {
        return Library.getSectionY(pocketKey) & 15;
    }

    private static long sectionKeyFromPocketKey(long pocketKey) {
        int yz = Library.getSectionY(pocketKey);
        return Library.setSectionY(pocketKey, (yz >>> 4) & 15);
    }

    private static long offsetKey(long key, int fo) {
        return Library.sectionToLong(Library.getSectionX(key) + FACE_DX[fo], Library.getSectionY(key) + FACE_DY[fo],
                Library.getSectionZ(key) + FACE_DZ[fo]);
    }

    private static void writeNibble(byte[] pocketData, int blockIndex, int paletteIndex) {
        int byteIndex = blockIndex >> 1;
        int b = pocketData[byteIndex] & 0xFF;
        if ((blockIndex & 1) == 0) {
            b = (b & 0x0F) | ((paletteIndex & 0x0F) << 4);
        } else {
            b = (b & 0xF0) | (paletteIndex & 0x0F);
        }
        pocketData[byteIndex] = (byte) b;
    }

    private static int readNibble(byte[] pocketData, int blockIndex) {
        int byteIndex = blockIndex >> 1;
        int b = pocketData[byteIndex] & 0xFF;
        return ((blockIndex & 1) == 0) ? ((b >> 4) & 0x0F) : (b & 0x0F);
    }

    private static SectionRef ensureSectionForWrite(WorldRadiationData data, ChunkRef owner, long sectionKey) {
        int sy = Library.getSectionY(sectionKey);
        SectionRef sc = owner.sec[sy];
        if (sc != null && sc.pocketCount > 0) return sc;
        UniformSectionRef stub = new UniformSectionRef(owner, sy);
        SectionRef prev = owner.sec[sy];
        owner.clearActiveBitMask(sy);
        owner.sec[sy] = stub;
        if (prev != null) data.retireIfNeeded(prev);
        data.markDirty(sectionKey);
        return stub;
    }

    @SuppressWarnings("deprecation")
    private static SectionMask scanResistantMask(WorldServer world, long sectionKey, ExtendedBlockStorage ebs) {
        if (ebs == null || ebs.isEmpty()) return null;

        BlockStateContainer c = ebs.getData();
        BitArray storage = c.storage;
        IBlockStatePalette pal = c.palette;

        int baseX = Library.getSectionX(sectionKey) << 4;
        int baseY = Library.getSectionY(sectionKey) << 4;
        int baseZ = Library.getSectionZ(sectionKey) << 4;
        BlockPos.MutableBlockPos mp = new BlockPos.MutableBlockPos();

        long[] data = storage.getBackingLongArray();
        int bits = c.bits;
        long entryMask = (1L << bits) - 1L;
        int stateSize = Block.BLOCK_STATE_IDS.size();
        Object[] cache = STATE_CLASS;
        if (cache == null || cache.length < stateSize) cache = ensureStateClassCapacity(stateSize);

        if (pal == BlockStateContainer.REGISTRY_BASED_PALETTE) {
            SectionMask mask = null;
            int li = 0, bo = 0;

            for (int idx = 0; idx < SECTION_BLOCK_COUNT; idx++) {
                int globalId;
                if (bo + bits <= 64) {
                    globalId = (int) ((data[li] >>> bo) & entryMask);
                    bo += bits;
                    if (bo == 64) {
                        bo = 0;
                        li++;
                    }
                } else {
                    int spill = 64 - bo;
                    long v = (data[li] >>> bo) | (data[li + 1] << spill);
                    globalId = (int) (v & entryMask);
                    li++;
                    bo = bits - spill;
                }

                if (globalId < 0 || globalId >= stateSize) {
                    int newSize = Block.BLOCK_STATE_IDS.size();
                    if (globalId < 0 || globalId >= newSize) continue;
                    stateSize = newSize;
                    if (cache.length < stateSize) cache = ensureStateClassCapacity(stateSize);
                }

                Object cls = cache[globalId];
                if (cls == null) {
                    IBlockState s = Block.BLOCK_STATE_IDS.getByValue(globalId);
                    if (s == null) {
                        cache[globalId] = NOT_RES;
                        continue;
                    }
                    Block b = s.getBlock();
                    Object nv = (b instanceof IRadResistantBlock) ? b : NOT_RES;
                    cache[globalId] = nv;
                    cls = nv;
                }

                if (cls == NOT_RES) continue;

                int x = Library.getLocalX(idx);
                int y = Library.getLocalY(idx);
                int z = Library.getLocalZ(idx);
                mp.setPos(baseX + x, baseY + y, baseZ + z);

                if (((IRadResistantBlock) cls).isRadResistant(world, mp)) {
                    if (mask == null) mask = new SectionMask();
                    mask.set(idx);
                }
            }
            return mask;
        }

        PalScratch sc = TL_PAL_SCRATCH.get();
        int gen = sc.nextGen();
        Object[] lcls = sc.cls;
        int[] lstamp = sc.stamp;

        boolean anyCandidate = false;

        if (pal instanceof BlockStatePaletteLinear p) {
            IBlockState[] states = p.states;
            int n = p.arraySize;

            for (int i = 0; i < n; i++) {
                IBlockState s = states[i];
                if (s == null) continue;

                int gid = Block.BLOCK_STATE_IDS.get(s);
                Object cls;
                if (gid < 0) {
                    cls = NOT_RES;
                } else {
                    if (gid >= stateSize) {
                        int newSize = Block.BLOCK_STATE_IDS.size();
                        if (gid >= newSize) {
                            cls = NOT_RES;
                        } else {
                            stateSize = newSize;
                            if (cache.length < stateSize) cache = ensureStateClassCapacity(stateSize);
                            cls = cache[gid];
                        }
                    } else {
                        cls = cache[gid];
                    }

                    if (cls == null) {
                        Block b = s.getBlock();
                        Object nv = (b instanceof IRadResistantBlock) ? b : NOT_RES;
                        cache[gid] = nv;
                        cls = nv;
                    }
                }

                lcls[i] = cls;
                lstamp[i] = gen;
                if (cls != NOT_RES) anyCandidate = true;
            }

            if (!anyCandidate) return null;

            SectionMask mask = null;
            int li = 0, bo = 0;

            for (int idx = 0; idx < SECTION_BLOCK_COUNT; idx++) {
                int localId;
                if (bo + bits <= 64) {
                    localId = (int) ((data[li] >>> bo) & entryMask);
                    bo += bits;
                    if (bo == 64) {
                        bo = 0;
                        li++;
                    }
                } else {
                    int spill = 64 - bo;
                    long v = (data[li] >>> bo) | (data[li + 1] << spill);
                    localId = (int) (v & entryMask);
                    li++;
                    bo = bits - spill;
                }
                if ((localId & ~255) != 0) continue;
                if (lstamp[localId] != gen) continue;
                Object cls = lcls[localId];
                if (cls == NOT_RES) continue;

                int x = Library.getLocalX(idx);
                int y = Library.getLocalY(idx);
                int z = Library.getLocalZ(idx);
                mp.setPos(baseX + x, baseY + y, baseZ + z);

                if (((IRadResistantBlock) cls).isRadResistant(world, mp)) {
                    if (mask == null) mask = new SectionMask();
                    mask.set(idx);
                }
            }
            return mask;
        }

        if (pal instanceof BlockStatePaletteHashMap p) {
            IntIdentityHashBiMap<IBlockState> map = p.statePaletteMap;
            Object[] byId = map.byId; // erasure

            int cap = 1 << bits;
            int lim = Math.min(cap, byId.length);

            for (int i = 0; i < lim; i++) {
                IBlockState s = (IBlockState) byId[i];
                if (s == null) continue;

                int gid = Block.BLOCK_STATE_IDS.get(s);
                Object cls;
                if (gid < 0) {
                    cls = NOT_RES;
                } else {
                    if (gid >= stateSize) {
                        int newSize = Block.BLOCK_STATE_IDS.size();
                        if (gid >= newSize) {
                            cls = NOT_RES;
                        } else {
                            stateSize = newSize;
                            if (cache.length < stateSize) cache = ensureStateClassCapacity(stateSize);
                            cls = cache[gid];
                        }
                    } else {
                        cls = cache[gid];
                    }

                    if (cls == null) {
                        Block b = s.getBlock();
                        Object nv = (b instanceof IRadResistantBlock) ? b : NOT_RES;
                        cache[gid] = nv;
                        cls = nv;
                    }
                }

                lcls[i] = cls;
                lstamp[i] = gen;
                if (cls != NOT_RES) anyCandidate = true;
            }

            if (!anyCandidate) return null;

            SectionMask mask = null;
            int li2 = 0, bo2 = 0;

            for (int idx = 0; idx < SECTION_BLOCK_COUNT; idx++) {
                int localId;
                if (bo2 + bits <= 64) {
                    localId = (int) ((data[li2] >>> bo2) & entryMask);
                    bo2 += bits;
                    if (bo2 == 64) {
                        bo2 = 0;
                        li2++;
                    }
                } else {
                    int spill = 64 - bo2;
                    long v = (data[li2] >>> bo2) | (data[li2 + 1] << spill);
                    localId = (int) (v & entryMask);
                    li2++;
                    bo2 = bits - spill;
                }

                if ((localId & ~255) != 0) continue;
                if (localId >= lim) continue;
                if (lstamp[localId] != gen) continue;

                Object cls = lcls[localId];
                if (cls == NOT_RES) continue;

                int x = Library.getLocalX(idx);
                int y = Library.getLocalY(idx);
                int z = Library.getLocalZ(idx);
                mp.setPos(baseX + x, baseY + y, baseZ + z);

                if (((IRadResistantBlock) cls).isRadResistant(world, mp)) {
                    if (mask == null) mask = new SectionMask();
                    mask.set(idx);
                }
            }
            return mask;
        }

        throw new UnsupportedOperationException("Unexpected palette format: " + pal.getClass());
    }

    // it seems that the size of total blockstate id count can fucking grow after FMLLoadCompleteEvent, making STATE_CLASS throw AIOOBE
    // I can't explain it, either there are registration happening after that event, or that ObjectIntIdentityMap went out
    // of sync internally (it uses IdentityHashMap to map blockstate to id, with an ArrayList to map ids back)
    // Anyway, we introduce a manual resize here to address this weird growth issue.
    private static Object[] ensureStateClassCapacity(int minSize) {
        Object[] a = STATE_CLASS;
        if (a != null && a.length >= minSize) return a;
        synchronized (RadiationSystemNT.class) {
            a = STATE_CLASS;
            if (a != null && a.length >= minSize) return a;

            int newLen = (a == null) ? 256 : a.length;
            while (newLen < minSize) newLen = newLen + (newLen >>> 1) + 16;
            STATE_CLASS = (a == null) ? new Object[newLen] : Arrays.copyOf(a, newLen);
            return STATE_CLASS;
        }
    }

    private static void processDiffuseGroup(Long2ObjectMap<Chunk> loaded, ChunkRef aCr, ChunkRef bCr, long unionMask, int group, LongBag wakeBag,
                                            int faceA, int faceB) {
        SectionRef[] aSec = aCr.sec, bSec = bCr.sec;
        while (unionMask != 0L) {
            int lane = Long.numberOfTrailingZeros(unionMask) >>> 4;
            int sy = (group << 2) + lane;
            long laneMask = 0xFFFFL << (lane << 4);
            unionMask &= ~laneMask;
            SectionRef a = aSec[sy];
            SectionRef b = bSec[sy];
            if (a == null || b == null || a.pocketCount <= 0 || b.pocketCount <= 0) continue;
            long aKey = Library.sectionToLong(aCr.ck, sy);
            long bKey = Library.sectionToLong(bCr.ck, sy);
            exchangeFaceExact(loaded, aKey, a, faceA, bKey, b, faceB, wakeBag);
        }
    }

    // padding boilerplate to address false sharing
    // @formatter:off
    private static abstract sealed class ChunkRefBase permits ChunkRefPad0 {
        final long ck;
        final @Nullable SectionRef @NotNull [] sec = new SectionRef[16];
        @Nullable ChunkRef north, south, west, east;
        @Deprecated volatile Chunk mcChunk;// don't use directly
        volatile int touchedEpoch;
        ChunkRefBase(long ck) { this.ck = ck; }
    }
    private static abstract sealed class ChunkRefPad0 extends ChunkRefBase permits ChunkRefM0 {
        long p00, p01, p02, p03, p04, p05, p06;
        ChunkRefPad0(long ck) { super(ck); }
    }
    private static abstract sealed class ChunkRefM0 extends ChunkRefPad0 permits ChunkRefPad1 {
        volatile long mask0;
        ChunkRefM0(long ck) { super(ck); }
    }
    private static abstract sealed class ChunkRefPad1 extends ChunkRefM0 permits ChunkRefM1 {
        long p10, p11, p12, p13, p14, p15, p16;
        ChunkRefPad1(long ck) { super(ck); }
    }
    private static abstract sealed class ChunkRefM1 extends ChunkRefPad1 permits ChunkRefPad2 {
        volatile long mask1;
        ChunkRefM1(long ck) { super(ck); }
    }
    private static abstract sealed class ChunkRefPad2 extends ChunkRefM1 permits ChunkRefM2 {
        long p20, p21, p22, p23, p24, p25, p26;
        ChunkRefPad2(long ck) { super(ck); }
    }
    private static abstract sealed class ChunkRefM2 extends ChunkRefPad2 permits ChunkRefPad3 {
        volatile long mask2;
        ChunkRefM2(long ck) { super(ck); }
    }
    private static abstract sealed class ChunkRefPad3 extends ChunkRefM2 permits ChunkRefM3 {
        long p30, p31, p32, p33, p34, p35, p36;
        ChunkRefPad3(long ck) { super(ck); }
    }
    private static abstract sealed class ChunkRefM3 extends ChunkRefPad3 permits ChunkRef {
        volatile long mask3;
        ChunkRefM3(long ck) { super(ck); }
    }
    // @formatter:on

    private static final class ChunkRef extends ChunkRefM3 {
        static final long MASK_0_OFF = UnsafeHolder.fieldOffset(ChunkRefM0.class, "mask0");
        static final long MASK_1_OFF = UnsafeHolder.fieldOffset(ChunkRefM1.class, "mask1");
        static final long MASK_2_OFF = UnsafeHolder.fieldOffset(ChunkRefM2.class, "mask2");
        static final long MASK_3_OFF = UnsafeHolder.fieldOffset(ChunkRefM3.class, "mask3");
        static final long TOUCHED_EPOCH_OFF = UnsafeHolder.fieldOffset(ChunkRefBase.class, "touchedEpoch");

        ChunkRef(long ck) {
            super(ck);
        }

        static long getMaskOffset(int sy) {
            return switch (sy >> 2) {
                case 0 -> MASK_0_OFF;
                case 1 -> MASK_1_OFF;
                case 2 -> MASK_2_OFF;
                default -> MASK_3_OFF;
            };
        }

        boolean isInactive(int sy, int pi) {
            long maskGroup = switch (sy >> 2) {
                case 0 -> mask0;
                case 1 -> mask1;
                case 2 -> mask2;
                default -> mask3;
            };
            int localShift = (sy & 3) << 4;
            return ((maskGroup >>> localShift) & (1L << pi)) == 0;
        }

        boolean setActiveBit(int sy, int pi) {
            long offset = getMaskOffset(sy);
            int localShift = (sy & 3) << 4;
            long bit = 1L << (localShift + pi);
            while (true) {
                long cur = U.getLongVolatile(this, offset);
                if ((cur & bit) != 0) return false;
                long next = cur | bit;
                if (U.compareAndSetLong(this, offset, cur, next)) return true;
            }
        }

        void clearActiveBit(int sy, int pi) {
            long offset = getMaskOffset(sy);
            int localShift = (sy & 3) << 4;
            long bit = 1L << (localShift + pi);
            while (true) {
                long cur = U.getLongVolatile(this, offset);
                if ((cur & bit) == 0) return;
                long next = cur & ~bit;
                if (U.compareAndSetLong(this, offset, cur, next)) return;
            }
        }

        void clearActiveBitMask(int sy) {
            long offset = getMaskOffset(sy);
            int localShift = (sy & 3) << 4;
            long mask = ~(0xFFFFL << localShift);
            while (true) {
                long cur = U.getLongVolatile(this, offset);
                long next = cur & mask;
                if (cur == next || U.compareAndSetLong(this, offset, cur, next)) return;
            }
        }

        boolean tryMarkTouched(int epoch) {
            int cur = touchedEpoch;
            if (cur == epoch) return false;
            return U.compareAndSetInt(this, TOUCHED_EPOCH_OFF, cur, epoch);
        }

        Chunk getChunk(Long2ObjectMap<Chunk> loaded) {
            Chunk c = mcChunk;
            return c != null && c.loaded ? c : (mcChunk = loaded.get(ck));
        }
    }

    private static final class PalScratch {
        final Object[] cls = new Object[256]; // localId -> (IRadResistantBlock instance) or NOT_RES
        final int[] stamp = new int[256];     // localId -> generation
        int gen = 1;

        int nextGen() {
            int g = gen + 1;
            gen = g == 0 ? 1 : g;
            return gen;
        }
    }

    private static final class SectionMask {
        final long[] words = new long[64];

        boolean get(int bit) {
            int w = bit >>> 6;
            return (words[w] & (1L << (bit & 63))) != 0L;
        }

        void set(int bit) {
            int w = bit >>> 6;
            words[w] |= (1L << (bit & 63));
        }

        boolean isEmpty() {
            for (long w : words) if (w != 0L) return false;
            return true;
        }
    }

    private static abstract class PocketTask extends RecursiveAction {
        final int lo, hi;

        PocketTask(int lo, int hi) {
            this.lo = lo;
            this.hi = hi;
        }

        protected abstract void work(int start, int end);

        protected abstract PocketTask createSubtask(int start, int end);

        @Override
        protected void compute() {
            if (hi - lo <= /*heuristic*/ 128) {
                work(lo, hi);
            } else {
                int mid = (lo + hi) >>> 1;
                invokeAll(createSubtask(lo, mid), createSubtask(mid, hi));
            }
        }
    }

    private static abstract class BagTask extends RecursiveAction {
        final LongBag bag;
        final int lo, hi;

        BagTask(LongBag bag, int lo, int hi) {
            this.bag = bag;
            this.lo = lo;
            this.hi = hi;
        }

        protected abstract void work(long[] chunk, int start, int end);

        protected abstract BagTask createSubtask(int start, int end);

        @Override
        protected void compute() {
            int n = hi - lo;
            if (n <= 0) return;

            int c1 = lo >>> LongBag.CHUNK_SHIFT;
            int c2 = (hi - 1) >>> LongBag.CHUNK_SHIFT;

            if (c1 == c2) {
                long[] chunk = bag.chunks[c1];
                if (chunk == null) return;
                int startIn = lo & LongBag.CHUNK_MASK;
                int endIn = hi - (c1 << LongBag.CHUNK_SHIFT);
                work(chunk, startIn, endIn);
                return;
            }

            if (n <= LongBag.CHUNK_SIZE) {
                int split = (c1 + 1) << LongBag.CHUNK_SHIFT;
                invokeAll(createSubtask(lo, split), createSubtask(split, hi));
                return;
            }

            int mid = (lo + hi) >>> 1;
            int aligned = (mid >>> LongBag.CHUNK_SHIFT) << LongBag.CHUNK_SHIFT;
            if (aligned > lo && aligned < hi) mid = aligned;
            invokeAll(createSubtask(lo, mid), createSubtask(mid, hi));
        }
    }

    private static abstract class ChunkTask extends RecursiveAction {
        final int lo, hi;

        ChunkTask(int lo, int hi) {
            this.lo = lo;
            this.hi = hi;
        }

        protected abstract void work(int start, int end);

        protected abstract ChunkTask createSubtask(int start, int end);

        @Override
        protected void compute() {
            if (hi - lo <= /*heuristic*/ 8) {
                work(lo, hi);
            } else {
                int mid = (lo + hi) >>> 1;
                invokeAll(createSubtask(lo, mid), createSubtask(mid, hi));
            }
        }
    }

    private static final class RebuildChunkBatchTask extends ChunkTask {
        final WorldRadiationData data;
        final long[] chunkKeys;
        final int[] masks;

        RebuildChunkBatchTask(WorldRadiationData data, long[] chunkKeys, int[] masks, int lo, int hi) {
            super(lo, hi);
            this.data = data;
            this.chunkKeys = chunkKeys;
            this.masks = masks;
        }

        @Override
        protected void work(int start, int end) {
            Long2ObjectMap<Chunk> loadedChunks = data.world.getChunkProvider().loadedChunks;
            for (int i = start; i < end; i++) {
                long ck = chunkKeys[i];
                int mask = masks[i];
                int cx = Library.getChunkPosX(ck);
                int cz = Library.getChunkPosZ(ck);
                Chunk chunk = loadedChunks.get(ck);
                if (chunk == null) {
                    int m = mask;
                    while (m != 0) {
                        int sy = Integer.numberOfTrailingZeros(m);
                        m &= (m - 1);
                        data.markDirty(Library.sectionToLong(cx, sy, cz));
                    }
                    continue;
                }

                data.onChunkLoaded(chunk);

                ExtendedBlockStorage[] stor = chunk.getBlockStorageArray();
                int m = mask;
                while (m != 0) {
                    int sy = Integer.numberOfTrailingZeros(m);
                    m &= (m - 1);
                    long sck = Library.sectionToLong(cx, sy, cz);
                    data.rebuildChunkPocketsLoaded(sck, stor[sy]);
                }
            }
        }

        @Override
        protected ChunkTask createSubtask(int start, int end) {
            return new RebuildChunkBatchTask(data, chunkKeys, masks, start, end);
        }
    }

    private static final class DrainStripeTask extends RecursiveAction {
        final WorldRadiationData data;
        final MpscUnboundedXaddArrayLongQueue q;
        final int stripe;
        final int epoch;

        DrainStripeTask(WorldRadiationData data, MpscUnboundedXaddArrayLongQueue q, int stripe, int epoch) {
            this.data = data;
            this.q = q;
            this.stripe = stripe;
            this.epoch = epoch;
        }

        @Override
        protected void compute() {
            long[] outPockets = data.activeStripeBufs[stripe];
            SectionRef[] outRefs = data.activeStripeRefs[stripe];
            int pocketCount = 0;
            ChunkRef[] outTouched = data.touchedStripeRefs[stripe];
            int touchedCount = 0;

            long pk;
            while ((pk = q.plainPoll()) != Long.MIN_VALUE) {
                long ck = Library.sectionToChunkLong(pk);
                ChunkRef cr = data.chunkRefs.get(ck);
                if (cr == null) continue;

                int yz = Library.getSectionY(pk);
                int sy = (yz >>> 4) & 15;
                int pi = yz & 15;

                if (cr.isInactive(sy, pi)) continue;

                SectionRef sc = cr.sec[sy];
                if (sc == null || sc.pocketCount <= 0) {
                    cr.clearActiveBit(sy, pi);
                    continue;
                }

                int len = sc.pocketCount & 0xFF;
                if (pi >= len) {
                    cr.clearActiveBit(sy, pi);
                    continue;
                }

                if (!sc.markEpochIfNot(pi, epoch)) continue;

                if (pocketCount == outPockets.length) {
                    int n = outPockets.length << 1;
                    outPockets = Arrays.copyOf(outPockets, n);
                    outRefs = Arrays.copyOf(outRefs, n);
                }
                outPockets[pocketCount] = pk;
                outRefs[pocketCount] = sc;
                pocketCount++;

                // Mark Center
                if (cr.tryMarkTouched(epoch)) {
                    if (touchedCount == outTouched.length) outTouched = Arrays.copyOf(outTouched, outTouched.length << 1);
                    outTouched[touchedCount++] = cr;
                }

                ChunkRef n = cr.north, s = cr.south, e = cr.east, w = cr.west;
                if (n != null && n.tryMarkTouched(epoch)) {
                    if (touchedCount == outTouched.length) outTouched = Arrays.copyOf(outTouched, outTouched.length << 1);
                    outTouched[touchedCount++] = n;
                }
                if (s != null && s.tryMarkTouched(epoch)) {
                    if (touchedCount == outTouched.length) outTouched = Arrays.copyOf(outTouched, outTouched.length << 1);
                    outTouched[touchedCount++] = s;
                }
                if (e != null && e.tryMarkTouched(epoch)) {
                    if (touchedCount == outTouched.length) outTouched = Arrays.copyOf(outTouched, outTouched.length << 1);
                    outTouched[touchedCount++] = e;
                }
                if (w != null && w.tryMarkTouched(epoch)) {
                    if (touchedCount == outTouched.length) outTouched = Arrays.copyOf(outTouched, outTouched.length << 1);
                    outTouched[touchedCount++] = w;
                }

                // diagonals
                ChunkRef ne = (n != null) ? n.east : (e != null) ? e.north : null;
                if (ne != null && ne.tryMarkTouched(epoch)) {
                    if (touchedCount == outTouched.length) outTouched = Arrays.copyOf(outTouched, outTouched.length << 1);
                    outTouched[touchedCount++] = ne;
                }
                ChunkRef nw = (n != null) ? n.west : (w != null) ? w.north : null;
                if (nw != null && nw.tryMarkTouched(epoch)) {
                    if (touchedCount == outTouched.length) outTouched = Arrays.copyOf(outTouched, outTouched.length << 1);
                    outTouched[touchedCount++] = nw;
                }
                ChunkRef se = (s != null) ? s.east : (e != null) ? e.south : null;
                if (se != null && se.tryMarkTouched(epoch)) {
                    if (touchedCount == outTouched.length) outTouched = Arrays.copyOf(outTouched, outTouched.length << 1);
                    outTouched[touchedCount++] = se;
                }
                ChunkRef sw = (s != null) ? s.west : (w != null) ? w.south : null;
                if (sw != null && sw.tryMarkTouched(epoch)) {
                    if (touchedCount == outTouched.length) outTouched = Arrays.copyOf(outTouched, outTouched.length << 1);
                    outTouched[touchedCount++] = sw;
                }
            }
            data.activeStripeBufs[stripe] = outPockets;
            data.activeStripeRefs[stripe] = outRefs;
            data.activeStripeCounts[stripe] = pocketCount;
            data.touchedStripeRefs[stripe] = outTouched;
            data.touchedStripeCounts[stripe] = touchedCount;
        }
    }

    private static final class FinalizeTask extends PocketTask {
        final WorldRadiationData data;
        final long[] keys;
        final SectionRef[] refs;

        FinalizeTask(WorldRadiationData data, long[] keys, SectionRef[] refs, int lo, int hi) {
            super(lo, hi);
            this.data = data;
            this.keys = keys;
            this.refs = refs;
        }

        @Override
        protected void work(int start, int end) {
            finalizeRange(data, keys, refs, start, end);
        }

        @Override
        protected PocketTask createSubtask(int start, int end) {
            return new FinalizeTask(data, keys, refs, start, end);
        }
    }

    private static final class FinalizeBagTask extends BagTask {
        final WorldRadiationData data;

        FinalizeBagTask(WorldRadiationData data, LongBag bag) {
            super(bag, 0, bag.size());
            this.data = data;
        }

        private FinalizeBagTask(WorldRadiationData data, LongBag bag, int lo, int hi) {
            super(bag, lo, hi);
            this.data = data;
        }

        @Override
        protected void work(long[] chunk, int start, int end) {
            finalizeRange(data, chunk, null, start, end);
        }

        @Override
        protected BagTask createSubtask(int start, int end) {
            return new FinalizeBagTask(data, bag, start, end);
        }
    }

    private static final class DiffuseXTask extends RecursiveAction {
        final WorldRadiationData data;
        final ChunkRef[] chunks;
        final int lo, hi;
        final LongBag wakeBag;

        DiffuseXTask(WorldRadiationData data, ChunkRef[] chunks, int lo, int hi, LongBag wakeBag) {
            this.data = data;
            this.chunks = chunks;
            this.lo = lo;
            this.hi = hi;
            this.wakeBag = wakeBag;
        }

        @Override
        protected void compute() {
            if (hi - lo <= 64) {
                work(lo, hi);
            } else {
                int mid = (lo + hi) >>> 1;
                invokeAll(new DiffuseXTask(data, chunks, lo, mid, wakeBag), new DiffuseXTask(data, chunks, mid, hi, wakeBag));
            }
        }

        private void work(int start, int end) {
            Long2ObjectMap<Chunk> loaded = data.world.getChunkProvider().loadedChunks;
            for (int i = start; i < end; i++) {
                ChunkRef aCr = chunks[i];
                ChunkRef bCr = aCr.east;
                if (bCr == null) continue;
                long a0 = aCr.mask0, a1 = aCr.mask1, a2 = aCr.mask2, a3 = aCr.mask3;
                long b0 = bCr.mask0, b1 = bCr.mask1, b2 = bCr.mask2, b3 = bCr.mask3;
                if (((a0 | a1 | a2 | a3) | (b0 | b1 | b2 | b3)) == 0L) continue;
                processDiffuseGroup(loaded, aCr, bCr, a0 | b0, 0, wakeBag, 5, 4);
                processDiffuseGroup(loaded, aCr, bCr, a1 | b1, 1, wakeBag, 5, 4);
                processDiffuseGroup(loaded, aCr, bCr, a2 | b2, 2, wakeBag, 5, 4);
                processDiffuseGroup(loaded, aCr, bCr, a3 | b3, 3, wakeBag, 5, 4);
            }
        }
    }

    private static final class DiffuseZTask extends RecursiveAction {
        final WorldRadiationData data;
        final ChunkRef[] chunks;
        final int lo, hi;
        final LongBag wakeBag;

        DiffuseZTask(WorldRadiationData data, ChunkRef[] chunks, int lo, int hi, LongBag wakeBag) {
            this.data = data;
            this.chunks = chunks;
            this.lo = lo;
            this.hi = hi;
            this.wakeBag = wakeBag;
        }

        @Override
        protected void compute() {
            if (hi - lo <= 64) {
                work(lo, hi);
            } else {
                int mid = (lo + hi) >>> 1;
                invokeAll(new DiffuseZTask(data, chunks, lo, mid, wakeBag), new DiffuseZTask(data, chunks, mid, hi, wakeBag));
            }
        }

        private void work(int start, int end) {
            Long2ObjectMap<Chunk> loaded = data.world.getChunkProvider().loadedChunks;
            for (int i = start; i < end; i++) {
                ChunkRef aCr = chunks[i];
                ChunkRef bCr = aCr.south;
                if (bCr == null) continue;
                long a0 = aCr.mask0, a1 = aCr.mask1, a2 = aCr.mask2, a3 = aCr.mask3;
                long b0 = bCr.mask0, b1 = bCr.mask1, b2 = bCr.mask2, b3 = bCr.mask3;
                if (((a0 | a1 | a2 | a3) | (b0 | b1 | b2 | b3)) == 0L) continue;
                processDiffuseGroup(loaded, aCr, bCr, a0 | b0, 0, wakeBag, 3, 2);
                processDiffuseGroup(loaded, aCr, bCr, a1 | b1, 1, wakeBag, 3, 2);
                processDiffuseGroup(loaded, aCr, bCr, a2 | b2, 2, wakeBag, 3, 2);
                processDiffuseGroup(loaded, aCr, bCr, a3 | b3, 3, wakeBag, 3, 2);
            }
        }
    }

    private static final class DiffuseYTask extends RecursiveAction {
        final WorldRadiationData data;
        final ChunkRef[] chunks;
        final int lo, hi;
        final int parity;
        final LongBag wakeBag;

        DiffuseYTask(WorldRadiationData data, ChunkRef[] chunks, int lo, int hi, int parity, LongBag wakeBag) {
            this.data = data;
            this.chunks = chunks;
            this.lo = lo;
            this.hi = hi;
            this.parity = parity;
            this.wakeBag = wakeBag;
        }

        @Override
        protected void compute() {
            if (hi - lo <= 64) {
                work(lo, hi);
            } else {
                int mid = (lo + hi) >>> 1;
                invokeAll(new DiffuseYTask(data, chunks, lo, mid, parity, wakeBag), new DiffuseYTask(data, chunks, mid, hi, parity, wakeBag));
            }
        }

        private void work(int start, int end) {
            Long2ObjectMap<Chunk> loaded = data.world.getChunkProvider().loadedChunks;
            for (int i = start; i < end; i++) {
                ChunkRef cr = chunks[i];
                long m0 = cr.mask0, m1 = cr.mask1, m2 = cr.mask2, m3 = cr.mask3;
                if ((m0 | m1 | m2 | m3) == 0L) continue;

                for (int sy = parity; sy < 15; sy += 2) {
                    long mask = switch (sy >> 2) {
                        case 0 -> m0;
                        case 1 -> m1;
                        case 2 -> m2;
                        default -> m3;
                    };
                    long nextMask = switch ((sy + 1) >> 2) {
                        case 0 -> m0;
                        case 1 -> m1;
                        case 2 -> m2;
                        default -> m3;
                    };
                    int shift = (sy & 3) << 4;
                    int nextShift = ((sy + 1) & 3) << 4;
                    if (((mask >>> shift) & 0xFFFFL) == 0L && ((nextMask >>> nextShift) & 0xFFFFL) == 0L) continue;
                    SectionRef a = cr.sec[sy];
                    SectionRef b = cr.sec[sy + 1];
                    if (a == null || b == null || a.pocketCount <= 0 || b.pocketCount <= 0) continue;

                    long aKey = Library.sectionToLong(cr.ck, sy);
                    long bKey = Library.sectionToLong(cr.ck, sy + 1);
                    exchangeFaceExact(loaded, aKey, a, 1, bKey, b, 0, wakeBag);
                }
            }
        }
    }

    private static final class LinkDirTask extends RecursiveAction {
        final WorldRadiationData data;
        final long[] keys;
        final int lo, hi;
        final int canonicalFace;

        LinkDirTask(WorldRadiationData data, long[] keys, int lo, int hi, int canonicalFace) {
            this.data = data;
            this.keys = keys;
            this.lo = lo;
            this.hi = hi;
            this.canonicalFace = canonicalFace;
        }

        @Override
        protected void compute() {
            int n = hi - lo;
            if (n <= 64) {
                for (int i = lo; i < hi; i++) data.linkCanonical(keys[i], canonicalFace);
            } else {
                int mid = (lo + hi) >>> 1;
                invokeAll(new LinkDirTask(data, keys, lo, mid, canonicalFace), new LinkDirTask(data, keys, mid, hi, canonicalFace));
            }
        }
    }

    private static abstract sealed class SectionRef permits MultiSectionRef, SingleMaskedSectionRef, UniformSectionRef {
        final ChunkRef owner;
        final int sy;
        final byte pocketCount;

        SectionRef(ChunkRef owner, int sy, byte pocketCount) {
            this.owner = owner;
            this.sy = sy;
            this.pocketCount = pocketCount;
        }

        // @formatter:off
        abstract boolean isMultiPocket();
        abstract byte @Nullable [] getPocketData();
        abstract double getRad(int idx);
        abstract void setRad(int idx, double val);
        abstract double getInvVolume(int idx);
        abstract boolean markEpochIfNot(int idx, int epoch);
        abstract int getPocketIndex(long pos);
        abstract int paletteIndexOrNeg(int blockIndex);
        abstract void clearFaceAllPockets(int faceOrdinal);
        abstract boolean markSentinelPlane16x16(int ordinal);
        abstract void linkFaceTo(SectionRef b, int faceA);
        abstract void linkFaceToSingle(SectionRef single, int faceA);
        abstract double getFaceDist(int pocketIndex, int faceOrdinal);
        // @formatter:on
    }

    private static final class UniformSectionRef extends SectionRef {
        volatile int neighborMarkEpoch;
        double rad;

        UniformSectionRef(ChunkRef owner, int sy) {
            super(owner, sy, (byte) 1);
        }

        // @formatter:off
        @Override boolean isMultiPocket() { return false; }
        @Override byte[] getPocketData() { return null; }
        @Override double getRad(int idx) { return rad; }
        @Override void setRad(int idx, double val) { rad = val; }
        @Override double getInvVolume(int idx) { return 1.0d / 4096.0d; }
        @Override int getPocketIndex(long pos) { return 0; }
        @Override int paletteIndexOrNeg(int blockIndex) { return 0; }
        @Override void clearFaceAllPockets(int faceOrdinal) { }
        @Override boolean markSentinelPlane16x16(int ordinal) { return false; }
        @Override void linkFaceTo(SectionRef b, int faceA) { }
        @Override void linkFaceToSingle(SectionRef single, int faceA) { }
        @Override double getFaceDist(int pocketIndex, int faceOrdinal) { return 8.0d; }
        // @formatter:on

        @Override
        boolean markEpochIfNot(int idx, int epoch) {
            if (neighborMarkEpoch == epoch) return false;
            neighborMarkEpoch = epoch;
            return true;
        }
    }

    private static final class SingleMaskedSectionRef extends SectionRef {
        static final long CONN_OFF = UnsafeHolder.fieldOffset(SingleMaskedSectionRef.class, "connections");
        final byte[] pocketData;
        final int volume;
        final double invVolume;
        final long packedFaceCounts;
        final double cx, cy, cz;
        volatile long connections;
        volatile int neighborMarkEpoch;
        double rad;

        SingleMaskedSectionRef(ChunkRef owner, int sy, byte[] pocketData, int volume, short[] faceCountsInput, double cx, double cy, double cz) {
            super(owner, sy, (byte) 1);
            this.pocketData = pocketData;
            this.volume = volume;
            this.invVolume = 1.0d / volume;
            this.cx = cx;
            this.cy = cy;
            this.cz = cz;
            long packed = 0L;
            for (int i = 0; i < 6; i++) {
                long val = faceCountsInput[i] & 0x1FFL;
                packed |= (val << (i * 9));
            }
            this.packedFaceCounts = packed;
        }

        // @formatter:off
        @Override boolean isMultiPocket() { return false; }
        @Override byte[] getPocketData() { return pocketData; }
        @Override double getRad(int idx) { return rad; }
        @Override void setRad(int idx, double val) { rad = val; }
        @Override double getInvVolume(int idx) { return invVolume; }
        @Override void clearFaceAllPockets(int faceOrdinal) { updateConnections(faceOrdinal, 0); }
        @Override boolean markSentinelPlane16x16(int ordinal) { return false; }
        @Override void linkFaceTo(SectionRef b, int faceA) { }
        // @formatter:on

        @Override
        double getFaceDist(int pocketIndex, int face) {
            return switch (face) {
                case 0 -> cy + 0.5d;
                case 1 -> 15.5d - cy;
                case 2 -> cz + 0.5d;
                case 3 -> 15.5d - cz;
                case 4 -> cx + 0.5d;
                case 5 -> 15.5d - cx;
                default -> throw new IllegalArgumentException("Invalid face ordinal: " + face);
            };
        }

        void updateConnections(int face, int value) {
            final int shift = face * 9;
            final long mask = 0x1FFL << shift;
            final long bits = ((long) value & 0x1FFL) << shift;
            while (true) {
                long current = this.connections;
                long next = (current & ~mask) | bits;
                if (current == next) return;
                if (U.compareAndSetLong(this, CONN_OFF, current, next)) return;
            }
        }

        int getFaceCount(int face) {
            return (int) ((packedFaceCounts >>> (face * 9)) & 0x1FFL);
        }

        @Override
        boolean markEpochIfNot(int idx, int epoch) {
            if (neighborMarkEpoch == epoch) return false;
            neighborMarkEpoch = epoch;
            return true;
        }

        @Override
        int getPocketIndex(long pos) {
            int blockIndex = Library.blockPosToLocal(pos);
            int nibble = readNibble(pocketData, blockIndex);
            return (nibble == 0) ? 0 : -1;
        }

        @Override
        int paletteIndexOrNeg(int blockIndex) {
            int nibble = readNibble(pocketData, blockIndex);
            return (nibble == 0) ? 0 : -1;
        }

        @Override
        void linkFaceToSingle(SectionRef neighbor, int faceA) {
            int area;
            if (neighbor instanceof UniformSectionRef) {
                area = getFaceCount(faceA);
            } else if (neighbor instanceof SingleMaskedSectionRef other) {
                int faceB = faceA ^ 1;
                int baseA = faceA << 8;
                int baseB = faceB << 8;
                int count = 0;
                byte[] myData = this.pocketData;
                byte[] otherData = other.pocketData;
                for (int t = 0; t < 256; t++) {
                    int idxA = FACE_PLANE[baseA + t];
                    if (readNibble(myData, idxA) != 0) continue;
                    int idxB = FACE_PLANE[baseB + t];
                    if (readNibble(otherData, idxB) != 0) continue;
                    count++;
                }
                area = count;
                other.updateConnections(faceB, area);
            } else {
                return;
            }
            this.updateConnections(faceA, area);
        }
    }

    private static final class MultiSectionRef extends SectionRef {
        final byte[] pocketData, faceActive;
        final char[] connectionArea;
        final double[] rad, invVolume;
        final int[] volume, neighborMarkEpoch;
        final double[] faceDist;

        MultiSectionRef(ChunkRef owner, int sy, byte pocketCount, byte[] pocketData, double[] faceDist) {
            super(owner, sy, pocketCount);
            this.pocketData = pocketData;
            this.faceDist = faceDist;

            int count = pocketCount & 0xFF;
            this.connectionArea = new char[count * 6 * NEI_SLOTS];
            this.faceActive = new byte[count * 6];
            this.rad = new double[count];
            this.volume = new int[count];
            this.invVolume = new double[count];
            this.neighborMarkEpoch = new int[count];
        }

        // @formatter:off
        @Override boolean isMultiPocket() { return true; }
        @Override byte[] getPocketData() { return pocketData; }
        @Override double getRad(int idx) { return rad[idx]; }
        @Override void setRad(int idx, double val) { rad[idx] = val; }
        @Override double getInvVolume(int idx) { return invVolume[idx]; }
        @Override double getFaceDist(int pocketIndex, int face) { return faceDist[pocketIndex * 6 + face]; }
        // @formatter:on

        @Override
        boolean markEpochIfNot(int idx, int epoch) {
            if (neighborMarkEpoch[idx] == epoch) return false;
            neighborMarkEpoch[idx] = epoch;
            return true;
        }

        @Override
        int getPocketIndex(long pos) {
            int blockIndex = Library.blockPosToLocal(pos);
            int nibble = readNibble(pocketData, blockIndex);
            return (nibble == NO_POCKET || nibble >= (pocketCount & 0xFF)) ? -1 : nibble;
        }

        @Override
        int paletteIndexOrNeg(int blockIndex) {
            int nibble = readNibble(pocketData, blockIndex);
            if (nibble == NO_POCKET) return -1;
            if (nibble >= (pocketCount & 0xFF)) return -2;
            return nibble;
        }

        @Override
        void clearFaceAllPockets(int faceOrdinal) {
            int len = pocketCount & 0xFF;
            int stride = 6 * NEI_SLOTS;
            int faceBase = faceOrdinal * NEI_SLOTS;
            for (int p = 0; p < len; p++) {
                int off = p * stride + faceBase;
                Arrays.fill(connectionArea, off, off + NEI_SLOTS, (char) 0);
                faceActive[p * 6 + faceOrdinal] = 0;
            }
        }

        @Override
        boolean markSentinelPlane16x16(int ordinal) {
            boolean dirty = false;
            int slotBase = ordinal * NEI_SLOTS;
            int planeBase = ordinal << 8;
            char[] conn = connectionArea;
            byte[] face = faceActive;
            for (int t = 0; t < 256; t++) {
                int idx = FACE_PLANE[planeBase + t];
                int pi = paletteIndexOrNeg(idx);
                if (pi == -2) {
                    dirty = true;
                    continue;
                }
                if (pi >= 0) {
                    conn[pi * 6 * NEI_SLOTS + slotBase] = 1;
                    face[pi * 6 + ordinal] = 1;
                }
            }
            return dirty;
        }

        @Override
        void linkFaceTo(SectionRef b, int faceA) {
            if (!(b instanceof MultiSectionRef multiB)) return;
            int faceB = faceA ^ 1;
            this.clearFaceAllPockets(faceA);
            multiB.clearFaceAllPockets(faceB);
            char[] aConn = this.connectionArea, bConn = multiB.connectionArea;
            byte[] aFace = this.faceActive, bFace = multiB.faceActive;
            int aFaceBase0 = faceA * NEI_SLOTS;
            int bFaceBase0 = faceB * NEI_SLOTS;

            int planeA = faceA << 8;
            int planeB = faceB << 8;

            for (int t = 0; t < 256; t++) {
                int aIdx = FACE_PLANE[planeA + t];
                int bIdx = FACE_PLANE[planeB + t];

                int pa = this.paletteIndexOrNeg(aIdx);
                if (pa < 0) continue;

                int pb = multiB.paletteIndexOrNeg(bIdx);
                if (pb < 0) continue;

                int aOff = pa * 6 * NEI_SLOTS + aFaceBase0;
                aConn[aOff] = 1; // sentinel
                aConn[aOff + NEI_SHIFT + pb]++;

                aFace[pa * 6 + faceA] = 1;

                int bOff = pb * 6 * NEI_SLOTS + bFaceBase0;
                bConn[bOff] = 1; // sentinel
                bConn[bOff + NEI_SHIFT + pa]++;

                bFace[pb * 6 + faceB] = 1;
            }
        }

        @Override
        void linkFaceToSingle(SectionRef single, int faceA) {
            int faceB = faceA ^ 1;
            this.clearFaceAllPockets(faceA);
            char[] aConn = this.connectionArea;
            byte[] aFace = this.faceActive;
            int aFaceBase0 = faceA * NEI_SLOTS;
            int planeA = faceA << 8;
            int planeB = faceB << 8;
            boolean singleAlwaysOpen = (single instanceof UniformSectionRef);
            for (int t = 0; t < 256; t++) {
                int aIdx = FACE_PLANE[planeA + t];
                int pa = this.paletteIndexOrNeg(aIdx);
                if (pa < 0) continue;
                if (!singleAlwaysOpen) {
                    int bIdx = FACE_PLANE[planeB + t];
                    if (single.paletteIndexOrNeg(bIdx) < 0) continue;
                }
                int aOff = pa * 6 * NEI_SLOTS + aFaceBase0;
                aConn[aOff] = 1;
                aConn[aOff + NEI_SHIFT]++; // neighbor pocket index 0
                aFace[pa * 6 + faceA] = 1;
            }
        }
    }

    private static final class WorldRadiationData {
        final WorldServer world;
        final NonBlockingHashMapLong<ChunkRef> chunkRefs = new NonBlockingHashMapLong<>(4096);

        final NonBlockingHashSetLong dirtySections = new NonBlockingHashSetLong(16384);
        // reserved value: Long.MIN_VALUE
        final MpscUnboundedXaddArrayLongQueue dirtyQueue = new MpscUnboundedXaddArrayLongQueue(16384);
        // only used when tickrate != 1.
        final MpscUnboundedXaddArrayLongQueue destructionQueue = new MpscUnboundedXaddArrayLongQueue(64);
        final TLPool<byte[]> pocketDataPool = new TLPool<>(() -> new byte[2048], _ -> /*@formatter:off*/{}/*@formatter:on*/, 256, 4096);
        final SectionRetireBag retiredSections = new SectionRetireBag(16384);
        final NonBlockingLong2LongHashMap pendingPocketRadBits = new NonBlockingLong2LongHashMap(16384);
        final LongArrayList dirtyToRebuildScratch = new LongArrayList(16384);
        final Long2IntOpenHashMap dirtyChunkMasksScratch = new Long2IntOpenHashMap(16384);
        final double minBound;

        final long[][] activeStripeBufs = new long[ACTIVE_STRIPES][];
        final ChunkRef[][] touchedStripeRefs = new ChunkRef[ACTIVE_STRIPES][];
        final int[] activeStripeCounts = new int[ACTIVE_STRIPES];
        final int[] touchedStripeCounts = new int[ACTIVE_STRIPES];
        final SectionRef[][] activeStripeRefs = new SectionRef[ACTIVE_STRIPES][];
        final ChunkRef[][] parityBuckets = new ChunkRef[4][4096];
        final int[] parityCounts = new int[4];

        SectionRef[] activeRefs = new SectionRef[32768];
        volatile MpscUnboundedXaddArrayLongQueue[] activeQueuesCurrent, activeQueuesNext;
        LongBag wokenBag = new LongBag(32768);
        long[] activeBuf = new long[32768];
        long[] linkScratch = new long[512];
        long[] dirtyChunkKeysScratch = new long[4096];
        int[] dirtyChunkMasksScratchArr = new int[4096];
        // only used when tickrate == 1. races are tolerable.
        long pocketToDestroy = Long.MIN_VALUE;
        int workEpoch = 0;
        double executionTimeAccumulator = 0.0D;
        int executionSampleCount = 0;

        WorldRadiationData(WorldServer world) {
            this.world = world;
            Object v = CompatibilityConfig.dimensionRad.get(world.provider.getDimension());
            minBound = -((v instanceof Number n) ? n.doubleValue() : 0D);
            dirtyChunkMasksScratch.defaultReturnValue(0);
            int p = Math.max(256, 65536 / ACTIVE_STRIPES);
            MpscUnboundedXaddArrayLongQueue[] cur = new MpscUnboundedXaddArrayLongQueue[ACTIVE_STRIPES];
            MpscUnboundedXaddArrayLongQueue[] nxt = new MpscUnboundedXaddArrayLongQueue[ACTIVE_STRIPES];
            for (int i = 0; i < ACTIVE_STRIPES; i++) {
                cur[i] = new MpscUnboundedXaddArrayLongQueue(p);
                nxt[i] = new MpscUnboundedXaddArrayLongQueue(p);
                activeStripeBufs[i] = new long[p];
                touchedStripeRefs[i] = new ChunkRef[p];
                activeStripeRefs[i] = new SectionRef[p];
            }
            activeQueuesCurrent = cur;
            activeQueuesNext = nxt;
        }

        private static int buildLinkKeys(long[] keys, int hi, int negFace, long[] out) {
            int n = 0;
            for (int i = 0; i < hi; i++) {
                long k = keys[i];
                out[n++] = k;
                out[n++] = offsetKey(k, negFace);
            }
            Arrays.parallelSort(out, 0, n);

            int u = 0;
            long prev = Long.MIN_VALUE;
            for (int i = 0; i < n; i++) {
                long v = out[i];
                if (i == 0 || v != prev) out[u++] = v;
                prev = v;
            }
            return u;
        }

        // tested to be uniformly distributed
        private static int stripeIndex(long pocketKey) {
            long x = pocketKey ^ (pocketKey >>> 33);
            x ^= (x >>> 17);
            return ((int) x) & ACTIVE_STRIPE_MASK;
        }

        void clearAllSections() {
            chunkRefs.clear(true);
        }

        void clearQueues() {
            MpscUnboundedXaddArrayLongQueue[] cur = activeQueuesCurrent;
            MpscUnboundedXaddArrayLongQueue[] nxt = activeQueuesNext;
            for (int i = 0; i < ACTIVE_STRIPES; i++) {
                cur[i].clear(true);
                nxt[i].clear(true);
            }
        }

        void swapQueues() {
            MpscUnboundedXaddArrayLongQueue[] cur = activeQueuesCurrent;
            for (int i = 0; i < ACTIVE_STRIPES; i++) cur[i].clear(false);
            activeQueuesCurrent = activeQueuesNext;
            activeQueuesNext = cur;
        }

        void enqueueActiveNext(long pocketKey) {
            if (pocketKey == Long.MIN_VALUE) return;
            activeQueuesNext[stripeIndex(pocketKey)].offer(pocketKey);
        }

        void deactivatePocket(long pocketKey) {
            long sck = sectionKeyFromPocketKey(pocketKey);
            long ck = Library.sectionToChunkLong(sck);
            ChunkRef cr = chunkRefs.get(ck);
            if (cr == null) return;

            int yz = Library.getSectionY(pocketKey);
            int sy = (yz >>> 4) & 15;
            int pi = yz & 15;

            cr.clearActiveBit(sy, pi);
        }

        ChunkRef onChunkLoaded(Chunk chunk) {
            int x = chunk.x, z = chunk.z;
            ChunkRef cr = chunkRefs.computeIfAbsent(ChunkPos.asLong(x, z), ChunkRef::new);
            cr.mcChunk = chunk;

            ChunkRef n = chunkRefs.get(ChunkPos.asLong(x, z - 1));
            if (n != null) {
                cr.north = n;
                n.south = cr;
            }
            ChunkRef s = chunkRefs.get(ChunkPos.asLong(x, z + 1));
            if (s != null) {
                cr.south = s;
                s.north = cr;
            }
            ChunkRef w = chunkRefs.get(ChunkPos.asLong(x - 1, z));
            if (w != null) {
                cr.west = w;
                w.east = cr;
            }
            ChunkRef e = chunkRefs.get(ChunkPos.asLong(x + 1, z));
            if (e != null) {
                cr.east = e;
                e.west = cr;
            }
            return cr;
        }

        @Nullable SectionRef getSection(long sck) {
            int sy = Library.getSectionY(sck);
            if (isInvalidSectionY(sy)) return null;
            long ck = Library.sectionToChunkLong(sck);
            ChunkRef cr = chunkRefs.get(ck);
            if (cr == null) return null;
            return cr.sec[sy];
        }

        ChunkRef getOrCreateChunkRef(long ck) {
            ChunkRef cr = chunkRefs.get(ck);
            if (cr != null) return cr;

            int cx = Library.getChunkPosX(ck);
            int cz = Library.getChunkPosZ(ck);
            cr = chunkRefs.computeIfAbsent(ck, ChunkRef::new);
            ChunkRef n = chunkRefs.get(ChunkPos.asLong(cx, cz - 1));
            if (n != null) {
                cr.north = n;
                n.south = cr;
            }
            ChunkRef s = chunkRefs.get(ChunkPos.asLong(cx, cz + 1));
            if (s != null) {
                cr.south = s;
                s.north = cr;
            }
            ChunkRef w = chunkRefs.get(ChunkPos.asLong(cx - 1, cz));
            if (w != null) {
                cr.west = w;
                w.east = cr;
            }
            ChunkRef e = chunkRefs.get(ChunkPos.asLong(cx + 1, cz));
            if (e != null) {
                cr.east = e;
                e.west = cr;
            }

            return cr;
        }

        LongBag getWokenBag(int cap) {
            if (wokenBag.capacity < cap) wokenBag = new LongBag(cap);
            return wokenBag;
        }

        void spawnFog(SectionRef sc, int pocketIndex, ThreadLocalRandom rnd) {
            Chunk chunk = sc.owner.getChunk(world.getChunkProvider().loadedChunks);
            if (chunk == null) return;
            int bx = chunk.x << 4;
            int by = sc.sy << 4;
            int bz = chunk.z << 4;
            BlockPos.MutableBlockPos mp = new BlockPos.MutableBlockPos();
            ExtendedBlockStorage[] stor = chunk.getBlockStorageArray();
            ExtendedBlockStorage storage = stor[sc.sy];

            for (int k = 0; k < 10; k++) {
                int i = rnd.nextInt(4096);
                int lx = Library.getLocalX(i);
                int lz = Library.getLocalZ(i);
                int ly = Library.getLocalY(i);
                int x = bx + lx;
                int y = by + ly;
                int z = bz + lz;
                long posLong = Library.blockPosToLong(x, y, z);
                if (sc.getPocketIndex(posLong) != pocketIndex) continue;
                IBlockState state = (storage == null || storage.isEmpty()) ? Blocks.AIR.getDefaultState() : storage.data.get(i);

                mp.setPos(x, y, z);
                if (state.getMaterial() != Material.AIR) continue;

                boolean nearGround = false;
                for (int d = 1; d <= 6; d++) {
                    int yy = y - d;
                    if (yy < 0) break;
                    int sy = yy >>> 4;
                    ExtendedBlockStorage e = stor[sy];
                    IBlockState below = (e == null || e.isEmpty()) ? Blocks.AIR.getDefaultState() : e.get(lx, yy & 15, lz);
                    mp.setPos(x, yy, z);
                    if (below.getMaterial() != Material.AIR) {
                        nearGround = true;
                        break;
                    }
                }
                if (!nearGround) continue;

                float fx = x + 0.5F, fy = y + 0.5F, fz = z + 0.5F;
                NBTTagCompound tag = new NBTTagCompound();
                tag.setString("type", "radiationfog");
                PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(tag, fx, fy, fz),
                        new TargetPoint(world.provider.getDimension(), fx, fy, fz, 100));
                break;
            }
        }

        void markDirty(long sck) {
            if (sck == Long.MIN_VALUE || !dirtySections.add(sck)) return;
            dirtyQueue.offer(sck);
        }

        void clearDirtyAll() {
            dirtySections.clear(true);
            dirtyQueue.clear(true);
        }

        void ensureDirtyBatchCapacity(int need) {
            if (dirtyChunkKeysScratch.length >= need && dirtyChunkMasksScratchArr.length >= need) return;
            int n = Math.max(dirtyChunkKeysScratch.length, 16);
            while (n < need) n = n + (n >>> 1) + 16;
            dirtyChunkKeysScratch = Arrays.copyOf(dirtyChunkKeysScratch, n);
            dirtyChunkMasksScratchArr = Arrays.copyOf(dirtyChunkMasksScratchArr, n);
        }

        void retireIfNeeded(SectionRef sc) {
            if (sc == null) return;
            if (sc instanceof MultiSectionRef || sc instanceof SingleMaskedSectionRef) {
                retiredSections.add(sc);
            }
        }

        void clearPendingAll() {
            pendingPocketRadBits.clear(true);
        }

        int nextWorkEpoch() {
            return ++workEpoch == 0 ? ++workEpoch : workEpoch;
        }

        void unloadChunk(int cx, int cz) {
            final long ck = ChunkPos.asLong(cx, cz);
            final ChunkRef cr = chunkRefs.remove(ck);

            if (cr == null) {
                for (int sy = 0; sy < 16; sy++) {
                    final long sck = Library.sectionToLong(cx, sy, cz);
                    dirtySections.remove(sck);
                    for (int p = 0; p <= NO_POCKET; p++) pendingPocketRadBits.remove(pocketKey(sck, p));
                }
                return;
            }

            ChunkRef n = cr.north;
            if (n == null) n = chunkRefs.get(ChunkPos.asLong(cx, cz - 1));
            if (n != null && n.south == cr) n.south = null;

            ChunkRef s = cr.south;
            if (s == null) s = chunkRefs.get(ChunkPos.asLong(cx, cz + 1));
            if (s != null && s.north == cr) s.north = null;

            ChunkRef w = cr.west;
            if (w == null) w = chunkRefs.get(ChunkPos.asLong(cx - 1, cz));
            if (w != null && w.east == cr) w.east = null;

            ChunkRef e = cr.east;
            if (e == null) e = chunkRefs.get(ChunkPos.asLong(cx + 1, cz));
            if (e != null && e.west == cr) e.west = null;

            cr.north = cr.south = cr.west = cr.east = null;
            cr.mcChunk = null;

            for (int sy = 0; sy < 16; sy++) {
                final long sck = Library.sectionToLong(cx, sy, cz);

                final SectionRef old = cr.sec[sy];
                if (old != null) {
                    retireIfNeeded(old);
                    markNeighborsMissing(sck);
                }

                cr.sec[sy] = null;
                cr.clearActiveBitMask(sy);

                dirtySections.remove(sck);
                for (int p = 0; p <= NO_POCKET; p++) pendingPocketRadBits.remove(pocketKey(sck, p));
            }
        }

        void cleanupPools() {
            destructionQueue.clear(true);
            retiredSections.drainAndRecycle(pocketDataPool);
        }

        void readPayload(int cx, int cz, byte[] raw) throws DecodeException {
            long ck = ChunkPos.asLong(cx, cz);
            ChunkRef owner = getOrCreateChunkRef(ck);

            for (int sy = 0; sy < 16; sy++) {
                final long sck = Library.sectionToLong(cx, sy, cz);
                final SectionRef prev = owner.sec[sy];
                if (prev != null) retireIfNeeded(prev);
                owner.clearActiveBitMask(sy);
                owner.sec[sy] = null;

                dirtySections.remove(sck);
                markDirty(sck);
            }

            for (int sy = 0; sy < 16; sy++) {
                long sck = Library.sectionToLong(cx, sy, cz);
                for (int p = 0; p <= NO_POCKET; p++) pendingPocketRadBits.remove(pocketKey(sck, p));
            }

            if (raw == null || raw.length == 0) return;

            final ByteBuffer b = ByteBuffer.wrap(raw, 4, raw.length - 4);
            if (b.remaining() < 2) throw new DecodeException("truncated v6 header");
            final int entryCount = b.getShort() & 0xFFFF;

            final int need = entryCount * (1 + 8);
            if (b.remaining() < need) throw new DecodeException("truncated v6 payload: need=" + need + " rem=" + b.remaining());

            for (int i = 0; i < entryCount; i++) {
                int yz = b.get() & 0xFF;
                int sy = (yz >>> 4) & 15;
                int pi = yz & 15;

                double rad = b.getDouble();
                if (!Double.isFinite(rad) || nearZero(rad)) continue;

                long sck = Library.sectionToLong(cx, sy, cz);
                long pk = pocketKey(sck, pi);
                pendingPocketRadBits.put(pk, Double.doubleToRawLongBits(rad));
            }
        }

        void rebuildChunkPocketsLoaded(long sectionKey, @Nullable ExtendedBlockStorage ebs) {
            final int sy = Library.getSectionY(sectionKey);
            final long ck = Library.sectionToChunkLong(sectionKey);
            final ChunkRef owner = getOrCreateChunkRef(ck);
            owner.clearActiveBitMask(sy);

            byte[] pocketData;
            int pocketCount;
            int[] vols = TL_VOL_COUNTS.get();
            long[] sumX = TL_SUM_X.get();
            long[] sumY = TL_SUM_Y.get();
            long[] sumZ = TL_SUM_Z.get();

            int singleVolume0 = SECTION_BLOCK_COUNT;
            short[] singleFaceCounts = null;

            final SectionMask resistant = scanResistantMask(world, sectionKey, ebs);

            if (resistant == null || resistant.isEmpty()) {
                pocketCount = 1;
                pocketData = null;
            } else {
                byte[] scratch = pocketDataPool.borrow();
                Arrays.fill(scratch, (byte) 0xFF);
                int[] queue = TL_FF_QUEUE.get();
                Arrays.fill(vols, 0);
                Arrays.fill(sumX, 0);
                Arrays.fill(sumY, 0);
                Arrays.fill(sumZ, 0);

                int pc = 0;

                for (int blockIndex = 0; blockIndex < SECTION_BLOCK_COUNT; blockIndex++) {
                    if (readNibble(scratch, blockIndex) != NO_POCKET) continue;
                    if (resistant.get(blockIndex)) continue;

                    final int currentPaletteIndex = (pc >= NO_POCKET) ? 0 : pc++;
                    int head = 0, tail = 0;
                    queue[tail++] = blockIndex;
                    writeNibble(scratch, blockIndex, currentPaletteIndex);

                    vols[currentPaletteIndex]++;
                    sumX[currentPaletteIndex] += Library.getLocalX(blockIndex);
                    sumY[currentPaletteIndex] += Library.getLocalY(blockIndex);
                    sumZ[currentPaletteIndex] += Library.getLocalZ(blockIndex);

                    while (head != tail) {
                        int currentIndex = queue[head++];
                        for (int i = 0; i < 6; i++) {
                            int neighborIndex = currentIndex + LINEAR_OFFSETS[i];
                            if (((neighborIndex & 0xF000) | ((currentIndex ^ neighborIndex) & BOUNDARY_MASKS[i])) != 0) continue;
                            if (readNibble(scratch, neighborIndex) != NO_POCKET) continue;
                            if (resistant.get(neighborIndex)) continue;
                            writeNibble(scratch, neighborIndex, currentPaletteIndex);
                            queue[tail++] = neighborIndex;

                            vols[currentPaletteIndex]++;
                            sumX[currentPaletteIndex] += Library.getLocalX(neighborIndex);
                            sumY[currentPaletteIndex] += Library.getLocalY(neighborIndex);
                            sumZ[currentPaletteIndex] += Library.getLocalZ(neighborIndex);
                        }
                    }
                }

                pocketCount = pc;
                if (pocketCount > 0) {
                    pocketData = scratch;
                    if (pocketCount == 1) {
                        singleVolume0 = Math.max(1, vols[0]);
                        singleFaceCounts = new short[6];
                        for (int face = 0; face < 6; face++) {
                            int base = face << 8;
                            int count = 0;
                            for (int t = 0; t < 256; t++) {
                                int idx = FACE_PLANE[base + t];
                                if (readNibble(pocketData, idx) == 0) count++;
                            }
                            singleFaceCounts[face] = (short) count;
                        }
                    }
                } else {
                    pocketDataPool.recycle(scratch);
                    pocketData = null;
                }
            }

            final SectionRef old = owner.sec[sy];
            if (pocketCount <= 0) {
                if (old != null) retireIfNeeded(old);
                owner.sec[sy] = null;
                for (int p = 0; p <= NO_POCKET; p++) pendingPocketRadBits.remove(pocketKey(sectionKey, p));
                return;
            }
            final double[] newPocketMasses = TL_NEW_MASS.get();
            Arrays.fill(newPocketMasses, 0, pocketCount, 0.0d);

            if (old != null && (old.pocketCount & 0xFF) > 0) {
                final int oldCnt = Math.min(old.pocketCount & 0xFF, NO_POCKET);

                if (pocketCount == 1 && pocketData == null) {
                    double totalMass = 0.0d;
                    if (oldCnt == 1) {
                        int v = (old instanceof SingleMaskedSectionRef) ? Math.max(1, ((SingleMaskedSectionRef) old).volume) : SECTION_BLOCK_COUNT;
                        double d = old.getRad(0);
                        if (Double.isFinite(d) && !nearZero(d)) totalMass = d * (double) v;
                    } else if (old instanceof MultiSectionRef mob) {
                        for (int i = 0; i < oldCnt; i++) {
                            int v = Math.max(1, mob.volume[i]);
                            double d = mob.getRad(i);
                            if (Double.isFinite(d) && !nearZero(d)) totalMass += d * (double) v;
                        }
                    }
                    if (Double.isFinite(totalMass) && !nearZero(totalMass)) newPocketMasses[0] = totalMass;
                } else if (oldCnt == 1 && (old instanceof UniformSectionRef)) {
                    double d = old.getRad(0);
                    if (Double.isFinite(d) && !nearZero(d)) {
                        double oldMass = d * (double) SECTION_BLOCK_COUNT;
                        long totalNewAir = 0L;
                        if (pocketCount == 1) {
                            totalNewAir = singleVolume0;
                        } else {
                            for (int i = 0; i < pocketCount; i++) totalNewAir += Math.max(1, vols[i]);
                        }
                        if (totalNewAir > 0L) {
                            double massPerBlock = oldMass / (double) totalNewAir;
                            if (pocketCount == 1) {
                                newPocketMasses[0] = massPerBlock * (double) singleVolume0;
                            } else {
                                for (int i = 0; i < pocketCount; i++) {
                                    int v = Math.max(1, vols[i]);
                                    newPocketMasses[i] = massPerBlock * (double) v;
                                }
                            }
                        }
                    }
                } else {
                    final double[] oldTotalMass = TL_OLD_MASS.get();
                    Arrays.fill(oldTotalMass, 0, oldCnt, 0.0d);

                    if (oldCnt == 1) {
                        int v = (old instanceof SingleMaskedSectionRef) ? Math.max(1, ((SingleMaskedSectionRef) old).volume) : SECTION_BLOCK_COUNT;
                        double d = old.getRad(0);
                        if (!Double.isFinite(d) || nearZero(d)) d = 0.0d;
                        oldTotalMass[0] = d * (double) v;
                    } else if (old instanceof MultiSectionRef mob) {
                        for (int i = 0; i < oldCnt; i++) {
                            int v = Math.max(1, mob.volume[i]);
                            double d = mob.getRad(i);
                            if (!Double.isFinite(d) || nearZero(d)) d = 0.0d;
                            oldTotalMass[i] = d * (double) v;
                        }
                    }

                    final int[] overlaps = TL_OVERLAPS.get();
                    Arrays.fill(overlaps, 0, oldCnt * pocketCount, 0);
                    final byte[] oldPocketData = old.getPocketData();
                    for (int i = 0; i < SECTION_BLOCK_COUNT; i++) {
                        int nIdx = readNibble(pocketData, i);
                        if (nIdx >= pocketCount) continue;
                        final int oIdx;
                        if (oldPocketData == null) {
                            oIdx = 0;
                        } else {
                            oIdx = readNibble(oldPocketData, i);
                            if (oIdx >= oldCnt) continue;
                        }
                        overlaps[oIdx * pocketCount + nIdx]++;
                    }

                    for (int o = 0; o < oldCnt; o++) {
                        final double mass = oldTotalMass[o];
                        if (!Double.isFinite(mass) || nearZero(mass)) continue;
                        int totalRemainingBlocks = 0;
                        final int row = o * pocketCount;
                        for (int n = 0; n < pocketCount; n++) totalRemainingBlocks += overlaps[row + n];
                        if (totalRemainingBlocks != 0) {
                            final double massPerBlock = mass / (double) totalRemainingBlocks;
                            for (int n = 0; n < pocketCount; n++) {
                                int count = overlaps[row + n];
                                if (count != 0) newPocketMasses[n] = Math.max(Math.min(newPocketMasses[n] + (double) count * massPerBlock, Double.MAX_VALUE), -Double.MAX_VALUE);
                            }
                        }
                        // else: pocket fully filled with blocks -> mass lost
                    }
                }
            }

            if (old != null) retireIfNeeded(old);
            final double minB = this.minBound;

            if (pocketCount == 1) {
                final long pk = pocketKey(sectionKey, 0);
                final int vol = pocketData == null ? SECTION_BLOCK_COUNT : singleVolume0;
                double density = newPocketMasses[0] / (double) vol;
                long bits = pendingPocketRadBits.remove(pk);
                if (bits != 0L) {
                    double v = Double.longBitsToDouble(bits);
                    density = (Double.isFinite(v) && !nearZero(v)) ? v : 0.0d;
                }
                for (int i = 1; i <= NO_POCKET; i++) pendingPocketRadBits.remove(pocketKey(sectionKey, i));
                if (!Double.isFinite(density) || nearZero(density)) density = 0.0d;
                if (density < minB) density = minB;

                SectionRef sc;
                if (pocketData == null) {
                    UniformSectionRef uni = new UniformSectionRef(owner, sy);
                    uni.rad = density;
                    sc = uni;
                } else {
                    double inv = 1.0d / singleVolume0;
                    double cx = sumX[0] * inv;
                    double cy = sumY[0] * inv;
                    double cz = sumZ[0] * inv;

                    SingleMaskedSectionRef masked = new SingleMaskedSectionRef(owner, sy, pocketData, singleVolume0, singleFaceCounts, cx, cy, cz);
                    masked.rad = density;
                    sc = masked;
                }

                owner.sec[sy] = sc;
                if (!nearZero(density)) {
                    if (owner.setActiveBit(sy, 0)) enqueueActiveNext(pk);
                }
                return;
            }

            double[] faceDists = new double[pocketCount * 6];
            for (int i = 0; i < pocketCount; i++) {
                int v = Math.max(1, vols[i]);
                double inv = 1.0d / v;
                double cx = sumX[i] * inv;
                double cy = sumY[i] * inv;
                double cz = sumZ[i] * inv;
                int base = i * 6;
                faceDists[base] = cy + 0.5d;
                faceDists[base + 1] = 15.5d - cy;
                faceDists[base + 2] = cz + 0.5d;
                faceDists[base + 3] = 15.5d - cz;
                faceDists[base + 4] = cx + 0.5d;
                faceDists[base + 5] = 15.5d - cx;
            }

            MultiSectionRef sc = new MultiSectionRef(owner, sy, (byte) pocketCount, pocketData, faceDists);
            for (int i = 0; i < pocketCount; i++) {
                final long pk = pocketKey(sectionKey, i);
                final int vol = Math.max(1, vols[i]);
                double density = newPocketMasses[i] / (double) vol;
                long bits = pendingPocketRadBits.remove(pk);
                if (bits != 0L) {
                    double v = Double.longBitsToDouble(bits);
                    density = (Double.isFinite(v) && !nearZero(v)) ? v : 0.0d;
                }
                if (!Double.isFinite(density) || nearZero(density)) density = 0.0d;
                if (density < minB) density = minB;
                sc.rad[i] = density;
                sc.volume[i] = vol;
                sc.invVolume[i] = 1.0d / vol;
            }
            for (int i = pocketCount; i <= NO_POCKET; i++) pendingPocketRadBits.remove(pocketKey(sectionKey, i));

            owner.sec[sy] = sc;

            for (int i = 0; i < pocketCount; i++) {
                double rad = sc.getRad(i);
                if (!nearZero(rad)) {
                    if (owner.setActiveBit(sy, i)) enqueueActiveNext(pocketKey(sectionKey, i));
                }
            }
        }

        void relinkKeys(long[] keys, int hi) {
            if (hi <= 0) return;
            ensureLinkScratch(hi << 1);
            int eN = buildLinkKeys(keys, hi, EnumFacing.WEST.ordinal(), linkScratch);
            if (eN > 0) new LinkDirTask(this, linkScratch, 0, eN, EnumFacing.EAST.ordinal()).invoke();
            int uN = buildLinkKeys(keys, hi, EnumFacing.DOWN.ordinal(), linkScratch);
            if (uN > 0) new LinkDirTask(this, linkScratch, 0, uN, EnumFacing.UP.ordinal()).invoke();
            int sN = buildLinkKeys(keys, hi, EnumFacing.NORTH.ordinal(), linkScratch);
            if (sN > 0) new LinkDirTask(this, linkScratch, 0, sN, EnumFacing.SOUTH.ordinal()).invoke();
        }

        private void ensureLinkScratch(int need) {
            long[] a = linkScratch;
            if (a.length >= need) return;
            int n = a.length;
            while (n < need) n = n + (n >>> 1) + 16;
            linkScratch = Arrays.copyOf(a, n);
        }

        void linkCanonical(long aKey, int faceA) {
            int ay = Library.getSectionY(aKey);
            if (isInvalidSectionY(ay)) return;

            SectionRef a = getSection(aKey);
            int by = ay + FACE_DY[faceA];

            if (isInvalidSectionY(by)) {
                if (a != null) a.clearFaceAllPockets(faceA);
                return;
            }

            long bKey = Library.sectionToLong(Library.getSectionX(aKey) + FACE_DX[faceA], by, Library.getSectionZ(aKey) + FACE_DZ[faceA]);
            SectionRef b = getSection(bKey);

            if (a == null || a.pocketCount <= 0) {
                if (b != null) {
                    int faceB = faceA ^ 1;
                    b.clearFaceAllPockets(faceB);
                    markSentinelOnBoundary(bKey, b, faceB);
                }
                return;
            }

            if (b == null || b.pocketCount <= 0) {
                a.clearFaceAllPockets(faceA);
                markSentinelOnBoundary(aKey, a, faceA);
                return;
            }
            boolean aMulti = a.isMultiPocket();
            boolean bMulti = b.isMultiPocket();
            if (aMulti) {
                if (bMulti) {
                    a.linkFaceTo(b, faceA);
                } else {
                    a.linkFaceToSingle(b, faceA);
                }
            } else {
                if (bMulti) {
                    b.linkFaceToSingle(a, faceA ^ 1);
                } else {
                    if (a instanceof SingleMaskedSectionRef sa) {
                        sa.linkFaceToSingle(b, faceA);
                    } else if (b instanceof SingleMaskedSectionRef sb) {
                        sb.linkFaceToSingle(a, faceA ^ 1);
                    } // implicitly 256
                }
            }
        }

        void markSentinelOnBoundary(long sck, SectionRef sc, int faceOrdinal) {
            if (sc.markSentinelPlane16x16(faceOrdinal)) markDirty(sck);
        }

        void markNeighborsMissing(long removedKey) {
            int rx = Library.getSectionX(removedKey);
            int ry = Library.getSectionY(removedKey);
            int rz = Library.getSectionZ(removedKey);
            for (int i = 0; i < 6; i++) {
                int ny = ry + FACE_DY[i];
                if (isInvalidSectionY(ny)) continue;
                long nKey = Library.sectionToLong(rx + FACE_DX[i], ny, rz + FACE_DZ[i]);
                SectionRef n = getSection(nKey);
                if (n != null) {
                    int nFace = i ^ 1;
                    n.clearFaceAllPockets(nFace);
                    markSentinelOnBoundary(nKey, n, nFace);
                }
            }
        }
    }

    private static final class LongBag {
        static final int CHUNK_SHIFT = 10;
        static final int CHUNK_SIZE = 1 << CHUNK_SHIFT;
        static final int CHUNK_MASK = CHUNK_SIZE - 1;
        static final long JAA_BASE = U.arrayBaseOffset(long[][].class);
        static final int JAA_SHIFT = Integer.numberOfTrailingZeros(U.arrayIndexScale(long[][].class));
        static final long SIZE_OFF = UnsafeHolder.fieldOffset(LongBag.class, "size");

        final long[][] chunks;
        final int capacity;
        volatile int size;

        LongBag(int cap) {
            int chunkCount = (cap + CHUNK_SIZE - 1) >>> CHUNK_SHIFT;
            chunks = new long[chunkCount][];
            capacity = chunkCount * CHUNK_SIZE;
        }

        void clear() {
            size = 0;
        }

        boolean tryAdd(long v) {
            int i = U.getAndAddInt(this, SIZE_OFF, 1);
            if (i >= capacity) {
                while (true) {
                    int s = size;
                    if (s <= capacity) break;
                    if (U.compareAndSetInt(this, SIZE_OFF, s, capacity)) break;
                }
                return false;
            }
            int c = i >>> CHUNK_SHIFT;
            int o = i & CHUNK_MASK;
            long[] chunk = chunks[c];
            if (chunk == null) {
                long chunkAddr = JAA_BASE + ((long) c << JAA_SHIFT);
                chunk = (long[]) U.getReferenceVolatile(chunks, chunkAddr);
                if (chunk == null) {
                    long[] newChunk = new long[CHUNK_SIZE];
                    if (U.compareAndSetReference(chunks, chunkAddr, null, newChunk)) chunk = newChunk;
                    else chunk = (long[]) U.getReferenceVolatile(chunks, chunkAddr);
                }
            }
            chunk[o] = v;
            return true;
        }

        int size() {
            return Math.min(size, capacity);
        }
    }

    private static final class SectionRetireBag {
        static final int CHUNK_SHIFT = 10;
        static final int CHUNK_SIZE = 1 << CHUNK_SHIFT;
        static final int CHUNK_MASK = CHUNK_SIZE - 1;
        static final long SRAA_BASE = U.arrayBaseOffset(SectionRef[][].class);
        static final int SRAA_SHIFT = Integer.numberOfTrailingZeros(U.arrayIndexScale(SectionRef[][].class));
        static final long SIZE_OFF = UnsafeHolder.fieldOffset(SectionRetireBag.class, "size");

        final SectionRef[][] chunks;
        final int capacity;
        volatile int size;

        SectionRetireBag(int cap) {
            int chunkCount = (cap + CHUNK_SIZE - 1) >>> CHUNK_SHIFT;
            this.chunks = new SectionRef[chunkCount][];
            this.capacity = chunkCount * CHUNK_SIZE;
        }

        void add(SectionRef v) {
            int i = U.getAndAddInt(this, SIZE_OFF, 1);
            if (i >= capacity) {
                while (true) {
                    int s = size;
                    if (s <= capacity) break;
                    if (U.compareAndSetInt(this, SIZE_OFF, s, capacity)) break;
                }
                return;
            }

            int c = i >>> CHUNK_SHIFT;
            int o = i & CHUNK_MASK;

            SectionRef[] chunk = chunks[c];
            if (chunk == null) {
                long addr = SRAA_BASE + ((long) c << SRAA_SHIFT);
                chunk = (SectionRef[]) U.getReferenceVolatile(chunks, addr);
                if (chunk == null) {
                    SectionRef[] newChunk = new SectionRef[CHUNK_SIZE];
                    if (U.compareAndSetReference(chunks, addr, null, newChunk)) chunk = newChunk;
                    else chunk = (SectionRef[]) U.getReferenceVolatile(chunks, addr);
                }
            }
            chunk[o] = v;
        }

        void drainAndRecycle(TLPool<byte[]> pp) {
            int sz = size;
            if (sz > capacity) sz = capacity;
            for (int i = 0; i < sz; i++) {
                int c = i >>> CHUNK_SHIFT;
                int o = i & CHUNK_MASK;

                SectionRef[] chunk = chunks[c];
                if (chunk == null) continue;

                SectionRef sc = chunk[o];
                if (sc != null) {
                    byte[] data = sc.getPocketData();
                    if (data != null) pp.recycle(data);
                    chunk[o] = null;
                }
            }
            size = 0;
        }
    }
}
