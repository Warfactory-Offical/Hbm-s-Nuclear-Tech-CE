package com.hbm.explosion;

import com.github.bsideup.jabel.Desugar;
import com.hbm.config.BombConfig;
import com.hbm.config.CompatibilityConfig;
import com.hbm.interfaces.BitMask;
import com.hbm.interfaces.IExplosionRay;
import com.hbm.interfaces.ServerThread;
import com.hbm.lib.Library;
import com.hbm.lib.TLPool;
import com.hbm.lib.maps.NonBlockingHashMapLong;
import com.hbm.main.MainRegistry;
import com.hbm.util.ChunkUtil;
import com.hbm.util.ConcurrentBitSet;
import com.hbm.util.MpscIntArrayListCollector;
import com.hbm.util.OffHeapBitSet;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.util.Constants;
import org.jctools.queues.atomic.MpscLinkedAtomicQueue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static com.hbm.config.BombConfig.safeCommit;

/**
 * Threaded DDA raytracer for mk5 explosion.
 *
 * @author mlbv
 */
public class ExplosionNukeRayParallelized implements IExplosionRay {
    private static final int WORLD_HEIGHT = 256;
    private static final int BITSET_SIZE = 16 * WORLD_HEIGHT * 16;
    private static final int SUB_MASK_SIZE = 16 * 16 * 16;
    private static final int SUBCHUNK_PER_CHUNK = WORLD_HEIGHT >> 4;
    private static final float NUKE_RESISTANCE_CUTOFF = 2_000_000F;
    private static final float INITIAL_ENERGY_FACTOR = 0.3F; // Scales crater, no impact on performance
    private static final double RESOLUTION_FACTOR = 1.0;  // Scales ray density, no impact on crater radius
    private static final int LUT_RESISTANCE_BINS = 256;
    private static final int LUT_DISTANCE_BINS = 256;
    private static final float LUT_MAX_RESISTANCE = 100.0F;
    private static final float[][] ENERGY_LOSS_LUT = new float[LUT_RESISTANCE_BINS][LUT_DISTANCE_BINS];
    private static final float DAMAGE_PER_BLOCK = 0.50F; // fallback for low resistance blocks
    private static final float DAMAGE_THRESHOLD_MULT = 1.00F;
    private static final float LOW_R_BOUND = 0.25F;
    private static final float LOW_R_PASS_LENGTH_BREAK = 0.75F;
    private static final double GOLDEN_ANGLE = Math.PI * (3.0 - Math.sqrt(5.0));

    private static final double RAY_DIRECTION_EPSILON = 1e-6;
    private static final double PROCESSING_EPSILON = 1e-9;
    private static final float MIN_EFFECTIVE_DIST_FOR_ENERGY_CALC = 0.01f;

    private static final ThreadLocal<MutableBlockPos> TL_POS = ThreadLocal.withInitial(MutableBlockPos::new);
    private static final ThreadLocal<LocalAgg> TL_LOCAL_AGG = ThreadLocal.withInitial(LocalAgg::new);
    private static final ThreadLocal<double[]> TL_DIR = ThreadLocal.withInitial(() -> new double[3]);
    private static final TLPool<IntDoubleAccumulator> ACC_POOL = new TLPool<>(IntDoubleAccumulator::new, IntDoubleAccumulator::clear, 16, 4096);
    private static final ThreadLocal<LongArrayList> TL_TE = ThreadLocal.withInitial(() -> new LongArrayList(16));
    private static final ThreadLocal<LongOpenHashSet> TL_EDGES = ThreadLocal.withInitial(() -> new LongOpenHashSet(64));
    private static final TLPool<LongArrayList> LONG_LIST_POOL = new TLPool<>(() -> new LongArrayList(64), LongArrayList::clear, 8, 512);
    private static final TLPool<Long2IntOpenHashMap> LONG2INT_POOL = new TLPool<>(Long2IntOpenHashMap::new, Long2IntOpenHashMap::clear, 4, 256);
    private static final TLPool<IntArrayList> INT_LIST_POOL = new TLPool<>(() -> new IntArrayList(64), IntArrayList::clear, 16, 512);

    static {
        for (int r = 0; r < LUT_RESISTANCE_BINS; r++) {
            float resistance = (r / (float) (LUT_RESISTANCE_BINS - 1)) * LUT_MAX_RESISTANCE;
            for (int d = 0; d < LUT_DISTANCE_BINS; d++) {
                float distFrac = d / (float) (LUT_DISTANCE_BINS - 1);
                ENERGY_LOSS_LUT[r][d] = (float) (Math.pow(resistance + 1.0, 3.0 * distFrac) - 1.0);
            }
        }
    }

    private final WorldServer world;
    private final double explosionX, explosionY, explosionZ;
    private final double invRadius, invRayIndexScale;
    private final int originX, originY, originZ;
    private final int radius;
    private final int strength;
    private final NonBlockingHashMapLong<ConcurrentBitSet> destructionMap;
    private final NonBlockingHashMapLong<ChunkAgg> aggMap;
    private final NonBlockingHashMapLong<MpscIntArrayListCollector> waitingRoom;
    private final NonBlockingHashMapLong<MpscLinkedAtomicQueue<ResumeItem>> postLoadQueues;
    private final MpscLinkedAtomicQueue<Long> chunkLoadQueue;
    private final Long2IntOpenHashMap sectionMaskByChunk;
    private final MpscLinkedAtomicQueue<PendingCarve> pendingCarves = new MpscLinkedAtomicQueue<>();
    private final AtomicBoolean mapAcquired = new AtomicBoolean(false);
    private final AtomicBoolean consolidationStarted = new AtomicBoolean(false);
    private final AtomicInteger pendingRays;
    private final AtomicInteger pendingCarveNotifies = new AtomicInteger(0);
    private final CarveApplier applier;
    private int algorithm;
    private int rayCount;
    private ForkJoinPool pool;
    private volatile UUID detonator;
    private volatile boolean collectFinished = false;
    private volatile boolean consolidationFinished = false;
    private volatile boolean destroyFinished = false;
    private volatile boolean isContained = true;

    public ExplosionNukeRayParallelized(World world, double x, double y, double z, int strength, int radius, int algorithm) {
        this(world, x, y, z, strength, radius);
        this.algorithm = algorithm;
        initializeAndStartWorkers();
    }

    public ExplosionNukeRayParallelized(World world, double x, double y, double z, int strength, int radius) {
        this.world = (WorldServer) world; // Casted here
        this.explosionX = x;
        this.explosionY = y;
        this.explosionZ = z;
        this.originX = (int) Math.floor(x);
        this.originY = (int) Math.floor(y);
        this.originZ = (int) Math.floor(z);
        this.strength = strength;
        this.radius = radius;
        this.invRadius = radius > 0 ? 1.0 / radius : 0.0;
        this.applier = safeCommit ? new SafeApplier() : new FastApplier();

        if (!CompatibilityConfig.isWarDim(world)) {
            this.collectFinished = true;
            this.consolidationFinished = true;
            this.destroyFinished = true;
            this.isContained = true;

            this.chunkLoadQueue = new MpscLinkedAtomicQueue<>();
            this.destructionMap = new NonBlockingHashMapLong<>(16);
            this.aggMap = new NonBlockingHashMapLong<>(16);
            this.waitingRoom = new NonBlockingHashMapLong<>(16);
            this.postLoadQueues = new NonBlockingHashMapLong<>(16);
            this.sectionMaskByChunk = new Long2IntOpenHashMap(0);
            this.sectionMaskByChunk.defaultReturnValue(0);
            this.pendingRays = new AtomicInteger(0);
            this.invRayIndexScale = 0.0;
            return;
        }

        this.rayCount = Math.max(0, (int) (2.5 * Math.PI * strength * strength * RESOLUTION_FACTOR));
        this.invRayIndexScale = rayCount > 1 ? 1.0 / (rayCount - 1) : 0.0;
        this.pendingRays = new AtomicInteger(this.rayCount);
        this.chunkLoadQueue = new MpscLinkedAtomicQueue<>();

        int estimatedChunkCount = Math.max(16, count(false));
        int chunkCap = capFor(estimatedChunkCount * 2);
        int subChunkCap = capFor(Math.max(16, count(true)));

        this.destructionMap = new NonBlockingHashMapLong<>(chunkCap);
        this.aggMap = new NonBlockingHashMapLong<>(chunkCap);
        this.waitingRoom = new NonBlockingHashMapLong<>(subChunkCap);
        this.postLoadQueues = new NonBlockingHashMapLong<>(subChunkCap);
        this.sectionMaskByChunk = new Long2IntOpenHashMap(estimatedChunkCount);
        this.sectionMaskByChunk.defaultReturnValue(0);
    }

    private static int capFor(int n) {
        int c = 1;
        while (c < n) c <<= 1;
        return Math.max(16, c);
    }

    @Contract(pure = true)
    private int count(boolean perSubchunk) {
        final int cr = (radius + 15) >> 4;
        final int minCX = (originX >> 4) - cr;
        final int maxCX = (originX >> 4) + cr;
        final int minCZ = (originZ >> 4) - cr;
        final int maxCZ = (originZ >> 4) + cr;
        final int minSubY = Math.max(0, (originY - radius) >> 4);
        final int maxSubY = Math.min(SUBCHUNK_PER_CHUNK - 1, (originY + radius) >> 4);
        if (minSubY > maxSubY) return 0;
        final double R2 = (radius + 14) * (radius + 14);
        int total = 0;
        for (int cx = minCX; cx <= maxCX; cx++) {
            final double dx = ((cx << 4) + 8) - explosionX;
            final double dx2 = dx * dx;

            for (int cz = minCZ; cz <= maxCZ; cz++) {
                final double dz = ((cz << 4) + 8) - explosionZ;
                final double base = dx2 + dz * dz;
                if (base > R2) continue;
                final double ry = Math.sqrt(R2 - base);
                int firstY = (int) Math.ceil((explosionY - ry - 8.0) / 16.0);
                int lastY = (int) Math.floor((explosionY + ry - 8.0) / 16.0);
                if (firstY < minSubY) firstY = minSubY;
                if (lastY > maxSubY) lastY = maxSubY;
                if (perSubchunk) {
                    final int countY = lastY - firstY + 1;
                    if (countY > 0) total += countY;
                } else {
                    if (lastY >= firstY) total++;
                }
            }
        }
        return total;
    }

    @SuppressWarnings({"DataFlowIssue", "deprecation"})
    private static float getNukeResistance(IBlockState state) {
        if (state.getMaterial().isLiquid()) return 0.1F;
        Block block = state.getBlock();
        if (block == Blocks.SANDSTONE) return 4.0F;
        if (block == Blocks.OBSIDIAN) return 18.0F;
        return block.getExplosionResistance(null);
    }

    private static double getEnergyLossFactor(float resistance, double distFrac) {
        if (resistance >= NUKE_RESISTANCE_CUTOFF) return resistance;
        if (resistance <= 0) return 0.0;
        if (resistance > LUT_MAX_RESISTANCE) return Math.pow(resistance + 1.0, 3.0 * distFrac) - 1.0;
        int rBin = (int) (resistance * (LUT_RESISTANCE_BINS - 1) / LUT_MAX_RESISTANCE);
        if (rBin == 0 && resistance > 0) return Math.pow(resistance + 1.0, 3.0 * distFrac) - 1.0;
        int dBin = (int) (distFrac * (LUT_DISTANCE_BINS - 1));
        return ENERGY_LOSS_LUT[rBin][dBin];
    }

    @Contract(mutates = "param2")
    private void directionFromIndex(int i, double[] out3) {
        final double y = 1.0 - (i * invRayIndexScale) * 2.0;
        final double r = Math.sqrt(Math.max(0.0, 1.0 - y * y));
        final double t = GOLDEN_ANGLE * i;
        out3[0] = Math.cos(t) * r;
        out3[1] = y;
        out3[2] = Math.sin(t) * r;
    }

    private void initializeAndStartWorkers() {
        if (rayCount <= 0) {
            this.collectFinished = true;
            this.consolidationFinished = true;
            return;
        }
        ChunkUtil.acquireMirrorMap(world);
        mapAcquired.set(true);
        int processors = Runtime.getRuntime().availableProcessors();
        int workers = BombConfig.maxThreads <= 0 ? Math.max(1, processors + BombConfig.maxThreads) : Math.min(BombConfig.maxThreads, processors);
        this.pool = new ForkJoinPool(workers, ForkJoinPool.defaultForkJoinWorkerThreadFactory, (t, ex) -> {
            MainRegistry.logger.error("Nuke ray-tracing crashed in {}", t.getName(), ex);
            onAllRaysFinished();
        }, false);
        pool.submit(new RayTracerTask(0, rayCount, computeTaskGrain(rayCount, 16)));
    }

    private int computeTaskGrain(int totalRays, int perWorker) {
        int desiredTasks = Math.max(1, pool.getParallelism() * perWorker);
        int batch = (int) Math.ceil((double) totalRays / desiredTasks);
        batch = Math.max(batch, 512);
        return Integer.highestOneBit(batch - 1) << 1;
    }

    private void onAllRaysFinished() {
        collectFinished = true;
        if (consolidationStarted.compareAndSet(false, true)) {
            pool.submit(this::runConsolidation);
        }
    }

    @ServerThread
    private void loadMissingChunks(int timeBudgetMs) {
        final long deadline = System.nanoTime() + (timeBudgetMs * 1_000_000L);
        while (System.nanoTime() < deadline) {
            Long ck = chunkLoadQueue.poll();
            if (ck == null) break;
            processChunkLoadRequest(ck);
        }
    }

    @ServerThread
    private void processChunkLoadRequest(long chunkPos) {
        Chunk chunk = world.getChunk(ChunkUtil.getChunkPosX(chunkPos), ChunkUtil.getChunkPosZ(chunkPos));
        MpscIntArrayListCollector waiters = waitingRoom.remove(chunkPos);
        if (waiters != null && pool != null && !pool.isShutdown()) {
            IntArrayList batch = waiters.drain();
            if (!batch.isEmpty()) {
                pool.submit(new ResumeBatchTask(batch, 0, batch.size(), computeTaskGrain(batch.size(), 16)));
            }
        }

        MpscLinkedAtomicQueue<ResumeItem> q = postLoadQueues.remove(chunkPos);
        if (q != null && pool != null && !pool.isShutdown()) {
            List<CarveSubTask> rebuilt = new ArrayList<>();
            ExtendedBlockStorage[] ebs = chunk.getBlockStorageArray();
            int cx = ChunkUtil.getChunkPosX(chunkPos);
            int cz = ChunkUtil.getChunkPosZ(chunkPos);
            ResumeItem item;
            while ((item = q.poll()) != null) {
                switch (item.kind) {
                    case ResumeItem.CARVE:
                        CarveSubTask t = prepareOneSub(cx, cz, item.subY, item.mask, ebs);
                        if (t != null) rebuilt.add(t);
                        else item.mask.free();
                        break;
                    case ResumeItem.APPLY_MASKS:
                        Int2ObjectOpenHashMap<BitMask> masks = item.masks;
                        pool.submit(() -> {
                            applier.apply(chunkPos, ebs, masks);
                            maybeFinish();
                        });
                        break;
                    case ResumeItem.APPLY_AGG:
                        ChunkAgg agg = item.agg;
                        pool.submit(() -> {
                            applier.apply(chunkPos, ebs, buildMasksFromAgg(agg, ebs));
                            agg.clear();
                            maybeFinish();
                        });
                        break;
                }
            }
            if (!rebuilt.isEmpty()) pendingCarves.add(new PendingCarve(chunkPos, rebuilt));
        }
    }

    @ServerThread
    private void secondPass() {
        ObjectIterator<Long2IntMap.Entry> iterator = sectionMaskByChunk.long2IntEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Long2IntMap.Entry e = iterator.next();
            long cp = e.getLongKey();
            int cx = ChunkUtil.getChunkPosX(cp);
            int cz = ChunkUtil.getChunkPosZ(cp);
            int changedMask = e.getIntValue();
            if (changedMask == 0) continue;
            Chunk chunk = ChunkUtil.getLoadedChunk(world, cp);
            if (chunk == null) continue;
            ExtendedBlockStorage[] storages = chunk.getBlockStorageArray();

            boolean groundUp = false;
            for (int subY = 0; subY < storages.length; subY++) {
                if (((changedMask >>> subY) & 1) == 0) continue;

                ExtendedBlockStorage s = storages[subY];
                if (s == Chunk.NULL_BLOCK_STORAGE) {
                    groundUp = true;
                } else if (s.isEmpty()) {
                    storages[subY] = Chunk.NULL_BLOCK_STORAGE;
                    groundUp = true;
                }
            }
            chunk.generateSkylightMap();
            chunk.resetRelightChecks();

            PlayerChunkMapEntry entry = world.getPlayerChunkMap().getEntry(cx, cz);
            if (entry != null) {
                entry.sendPacket(new SPacketChunkData(chunk, groundUp ? 0xFFFF : changedMask));
            }
        }
        sectionMaskByChunk.clear();
    }

    @Override
    public boolean isComplete() {
        return collectFinished && consolidationFinished && destroyFinished;
    }

    @Override
    public boolean isContained() {
        return isContained;
    }

    private static Int2ObjectOpenHashMap<BitMask> splitBySubchunk(@NotNull ConcurrentBitSet chunkMask) {
        Int2ObjectOpenHashMap<BitMask> m = new Int2ObjectOpenHashMap<>();
        int bit = chunkMask.nextSetBit(0);
        while (bit >= 0) {
            int yGlobal = WORLD_HEIGHT - 1 - (bit >>> 8);
            int subY = yGlobal >>> 4;
            int xLocal = (bit >>> 4) & 0xF;
            int zLocal = bit & 0xF;
            int yLocal = yGlobal & 0xF;
            int localIndex = xLocal | (zLocal << 4) | (yLocal << 8);
            m.computeIfAbsent(subY, k -> new OffHeapBitSet(SUB_MASK_SIZE)).set(localIndex);
            bit = chunkMask.nextSetBit(bit + 1);
        }
        return m;
    }

    private static Int2ObjectOpenHashMap<BitMask> buildMasksFromAgg(@NotNull ChunkAgg agg, @NotNull ExtendedBlockStorage[] storages) {
        Int2ObjectOpenHashMap<BitMask> subChunkBitSets = new Int2ObjectOpenHashMap<>();
        Int2DoubleOpenHashMap dmg = agg.damage;
        Int2DoubleOpenHashMap len = agg.passLen;

        if (!dmg.isEmpty()) {
            ObjectIterator<Int2DoubleMap.Entry> iterator = dmg.int2DoubleEntrySet().fastIterator();
            while (iterator.hasNext()) {
                Int2DoubleOpenHashMap.Entry e = iterator.next();
                int bitIndex = e.getIntKey(); // global index
                int yGlobal = WORLD_HEIGHT - 1 - (bitIndex >>> 8);
                int subY = yGlobal >>> 4;
                int xLocal = (bitIndex >>> 4) & 0xF;
                int zLocal = bitIndex & 0xF;
                int yLocal = yGlobal & 0xF;
                int localIndex = xLocal | (zLocal << 4) | (yLocal << 8);
                if (shouldDestroy(bitIndex, storages, e.getDoubleValue(), len.get(bitIndex))) {
                    subChunkBitSets.computeIfAbsent(subY, k -> new OffHeapBitSet(SUB_MASK_SIZE)).set(localIndex);
                }
            }
        }

        if (!len.isEmpty()) {
            ObjectIterator<Int2DoubleMap.Entry> iterator = len.int2DoubleEntrySet().fastIterator();
            while (iterator.hasNext()) {
                Int2DoubleOpenHashMap.Entry e = iterator.next();
                int bitIndex = e.getIntKey(); // global index
                if (dmg.containsKey(bitIndex)) continue;
                int yGlobal = WORLD_HEIGHT - 1 - (bitIndex >>> 8);
                int subY = yGlobal >>> 4;
                int xLocal = (bitIndex >>> 4) & 0xF;
                int zLocal = bitIndex & 0xF;
                int yLocal = yGlobal & 0xF;
                int localIndex = xLocal | (zLocal << 4) | (yLocal << 8);
                if (shouldDestroy(bitIndex, storages, 0.0, e.getDoubleValue())) {
                    subChunkBitSets.computeIfAbsent(subY, k -> new OffHeapBitSet(SUB_MASK_SIZE)).set(localIndex);
                }
            }
        }
        return subChunkBitSets;
    }

    private static boolean shouldDestroy(int bitIndex, ExtendedBlockStorage[] storages, double accumulatedDamage, double passLen) {
        final int yGlobal = WORLD_HEIGHT - 1 - (bitIndex >>> 8);
        final int subY = yGlobal >>> 4;
        final ExtendedBlockStorage s = storages[subY];
        if (s == Chunk.NULL_BLOCK_STORAGE || s.isEmpty()) return false;
        final int xLocal = (bitIndex >>> 4) & 0xF;
        final int zLocal = bitIndex & 0xF;
        final int yLocal = yGlobal & 0xF;
        final IBlockState st = s.get(xLocal, yLocal, zLocal);
        if (st.getBlock() == Blocks.AIR) return false;
        final float resistance = getNukeResistance(st);
        if (accumulatedDamage >= (resistance * DAMAGE_THRESHOLD_MULT)) return true;
        return resistance <= LOW_R_BOUND && passLen >= LOW_R_PASS_LENGTH_BREAK;
    }

    @Override
    public void setDetonator(UUID detonator) {
        this.detonator = detonator;
    }

    @Override
    public void update(int processTimeMs) {
        if (safeCommit) {
            final long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(processTimeMs);
            loadMissingChunks(processTimeMs / 2);
            while (System.nanoTime() < deadline) {
                PendingCarve job = pendingCarves.poll();
                if (job == null) break;
                applyCarveJobOnMain(job);
            }
            maybeFinish();
        } else {
            loadMissingChunks(processTimeMs);
        }
    }

    @Override
    public void cancel() {
        this.collectFinished = true;
        this.consolidationFinished = true;
        this.destroyFinished = true;

        if (this.waitingRoom != null) this.waitingRoom.clear();
        if (this.sectionMaskByChunk != null) this.sectionMaskByChunk.clear();
        PendingCarve job;
        while ((job = this.pendingCarves.poll()) != null) {
            for (CarveSubTask t : job.tasks()) {
                if (t != null && t.mask != null) t.mask.free();
            }
        }
        if (this.postLoadQueues != null) {
            long[] keys = this.postLoadQueues.keySetLong();
            for (long cp : keys) {
                MpscLinkedAtomicQueue<ResumeItem> q = this.postLoadQueues.remove(cp);
                if (q != null) {
                    ResumeItem item;
                    while ((item = q.poll()) != null) {
                        switch (item.kind) {
                            case ResumeItem.CARVE:
                                if (item.mask != null) item.mask.free();
                                break;
                            case ResumeItem.APPLY_MASKS:
                                if (item.masks != null) {
                                    ObjectIterator<Int2ObjectMap.Entry<BitMask>> it = item.masks.int2ObjectEntrySet().fastIterator();
                                    while (it.hasNext()) it.next().getValue().free();
                                    item.masks.clear();
                                }
                                break;
                            case ResumeItem.APPLY_AGG:
                                if (item.agg != null) item.agg.clear();
                                break;
                        }
                    }
                }
            }
            this.postLoadQueues.clear();
        }

        if (this.pool != null && !this.pool.isShutdown()) {
            this.pool.shutdownNow();
            try {
                if (!this.pool.awaitTermination(100, TimeUnit.MILLISECONDS))
                    MainRegistry.logger.error("ExplosionNukeRayParallelized ForkJoinPool did not terminate promptly on cancel.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (!this.pool.isShutdown()) this.pool.shutdownNow();
            }
            if (mapAcquired.getAndSet(false)) ChunkUtil.releaseMirrorMap(world);
        }
        if (this.destructionMap != null) this.destructionMap.clear();
        if (this.aggMap != null) this.aggMap.clear();
        if (this.chunkLoadQueue != null) this.chunkLoadQueue.clear();
    }

    /**
     * Run once all rays finish. Converts sources → masks → applies via applier.
     */
    private void runConsolidation() {
        if (algorithm == 2) {
            Arrays.stream(this.aggMap.keySetLong()).parallel().forEach(cpLong -> {
                final ChunkAgg agg = this.aggMap.get(cpLong);
                if (agg == null) return;
                agg.drainUnlocked();

                ExtendedBlockStorage[] storages = ChunkUtil.getLoadedEBS(world, cpLong);
                if (storages == null) {
                    enqueueForMissingChunk(cpLong, new ResumeItem(agg));
                } else {
                    applier.apply(cpLong, storages, buildMasksFromAgg(agg, storages));
                    agg.clear();
                    aggMap.remove(cpLong);
                }
            });
            aggMap.clear();
        } else {
            Arrays.stream(this.destructionMap.keySetLong()).parallel().forEach(cpLong -> {
                final ConcurrentBitSet chunkBitSet = destructionMap.get(cpLong);
                if (chunkBitSet == null || chunkBitSet.isEmpty()) {
                    destructionMap.remove(cpLong);
                    return;
                }
                ExtendedBlockStorage[] storages = ChunkUtil.getLoadedEBS(world, cpLong);
                Int2ObjectOpenHashMap<BitMask> masks = splitBySubchunk(chunkBitSet);
                if (storages == null) {
                    enqueueForMissingChunk(cpLong, new ResumeItem(masks));
                } else {
                    applier.apply(cpLong, storages, masks);
                }
                destructionMap.remove(cpLong);
            });
        }
        consolidationFinished = true;
        maybeFinish();
    }

    private void maybeFinish() {
        if (collectFinished && consolidationFinished && !destroyFinished && pendingCarveNotifies.get() == 0 && (!safeCommit || pendingCarves.isEmpty()) && waitingRoom.isEmpty() && postLoadQueues.isEmpty()) {
            world.addScheduledTask(() -> {
                secondPass();
                destroyFinished = true;
                if (pool != null && !pool.isShutdown()) pool.shutdown();
                if (mapAcquired.getAndSet(false)) ChunkUtil.releaseMirrorMap(world);
            });
        }
    }

    private int carveSubchunkAndSwap(BitMask subMask, long cpLong, ExtendedBlockStorage[] storages, LongArrayList teRemovals, LongArrayList neighborNotifies, int selfMask, Long2IntOpenHashMap neighborMask, int subY) {
        final int cx = ChunkUtil.getChunkPosX(cpLong);
        final int cz = ChunkUtil.getChunkPosZ(cpLong);

        ExtendedBlockStorage expected = storages[subY];
        if (expected == Chunk.NULL_BLOCK_STORAGE || expected.isEmpty()) {
            return selfMask | (1 << subY);
        }

        final LongArrayList te = TL_TE.get();
        final LongOpenHashSet edges = TL_EDGES.get();

        te.clear();
        edges.clear();

        while (true) {
            final ExtendedBlockStorage carved = ChunkUtil.copyAndCarveLocal(world, cx, cz, subY, storages, subMask, te, edges);
            if (ChunkUtil.casEbsAt(expected, carved, storages, subY)) {
                selfMask |= (1 << subY);
                for (int i = 0, n = te.size(); i < n; i++) teRemovals.add(te.getLong(i));
                if (!edges.isEmpty()) {
                    MutableBlockPos pos = TL_POS.get();
                    LongIterator it = edges.iterator();
                    while (it.hasNext()) {
                        long lp = it.nextLong();
                        neighborNotifies.add(lp);
                        Library.fromLong(pos, lp);
                        long nck = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
                        int m = neighborMask.get(nck);
                        m |= 1 << (pos.getY() >>> 4);
                        neighborMask.put(nck, m);
                    }
                }
                break;
            }
            expected = storages[subY];
            if (expected == Chunk.NULL_BLOCK_STORAGE || expected.isEmpty()) {
                selfMask |= (1 << subY);
                break;
            }
            te.clear();
            edges.clear();
        }
        return selfMask;
    }

    private void notifyMainThread(long cpLong, LongArrayList teRemovals, LongArrayList neighborNotifies, int selfMask, Long2IntOpenHashMap neighborMask) {
        pendingCarveNotifies.incrementAndGet();
        world.addScheduledTask(() -> {
            try {
                chunkFixup(cpLong, teRemovals, neighborNotifies, selfMask, neighborMask);
                Chunk chunk = ChunkUtil.getLoadedChunk(world, cpLong);
                if (chunk != null) chunk.markDirty();
            } finally {
                LONG_LIST_POOL.recycle(teRemovals);
                LONG_LIST_POOL.recycle(neighborNotifies);
                LONG2INT_POOL.recycle(neighborMask);
                if (pendingCarveNotifies.decrementAndGet() == 0) {
                    maybeFinish();
                }
            }
        });
    }

    private void prepareAndEnqueue(long cpLong, Int2ObjectOpenHashMap<BitMask> masks, ExtendedBlockStorage[] storages) {
        if (masks == null || masks.isEmpty()) return;

        final int cx = ChunkUtil.getChunkPosX(cpLong);
        final int cz = ChunkUtil.getChunkPosZ(cpLong);
        List<CarveSubTask> tasks = new ArrayList<>(masks.size());
        ObjectIterator<Int2ObjectMap.Entry<BitMask>> it = masks.int2ObjectEntrySet().fastIterator();
        while (it.hasNext()) {
            Int2ObjectMap.Entry<BitMask> e = it.next();
            int subY = e.getIntKey();
            BitMask bitset = e.getValue();
            CarveSubTask t = prepareOneSub(cx, cz, subY, bitset, storages);
            if (t != null) tasks.add(t);
            else bitset.free();
        }
        if (!tasks.isEmpty()) pendingCarves.add(new PendingCarve(cpLong, tasks));
    }

    private CarveSubTask prepareOneSub(int cx, int cz, int subY, BitMask subBitset, ExtendedBlockStorage[] storages) {
        ExtendedBlockStorage expected = storages[subY];
        if (expected == Chunk.NULL_BLOCK_STORAGE || expected.isEmpty()) return null;

        final LongArrayList te = TL_TE.get();
        final LongOpenHashSet edges = TL_EDGES.get();
        te.clear();
        edges.clear();
        final ExtendedBlockStorage carved = ChunkUtil.copyAndCarveLocal(world, cx, cz, subY, storages, subBitset, te, edges);
        CarveSubTask task = new CarveSubTask(subY, subBitset);
        task.expected = expected;
        task.carved = carved;
        for (int i = 0, n = te.size(); i < n; i++) task.teRemovals.add(te.getLong(i));
        if (!edges.isEmpty()) {
            MutableBlockPos pos = TL_POS.get();
            LongIterator it = edges.iterator();
            while (it.hasNext()) {
                long lp = it.nextLong();
                task.neighborNotifies.add(lp);
                Library.fromLong(pos, lp);
                long nck = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
                int m = task.neighborMask.get(nck);
                m |= 1 << (pos.getY() >>> 4);
                task.neighborMask.put(nck, m);
            }
        }
        return task;
    }

    private void applyCarveJobOnMain(@NotNull PendingCarve job) {
        final int cx = ChunkUtil.getChunkPosX(job.chunkPos());
        final int cz = ChunkUtil.getChunkPosZ(job.chunkPos());
        Chunk chunk = ChunkUtil.getLoadedChunk(world, job.chunkPos());
        if (chunk == null) {
            for (CarveSubTask t : job.tasks()) {
                enqueueForMissingChunk(job.chunkPos(), new ResumeItem(t.subY, t.mask));
            }
            maybeFinish();
            return;
        }
        ExtendedBlockStorage[] storages = chunk.getBlockStorageArray();
        final LongArrayList teRemovals = LONG_LIST_POOL.borrow();
        final LongArrayList neighborNotifies = LONG_LIST_POOL.borrow();
        final Long2IntOpenHashMap neighborMask = LONG2INT_POOL.borrow();
        teRemovals.clear();
        neighborNotifies.clear();
        neighborMask.clear();
        int selfMask = 0;

        for (int i = 0, n = job.tasks().size(); i < n; i++) {
            CarveSubTask t = job.tasks().get(i);
            final int subY = t.subY;
            ExtendedBlockStorage cur = storages[subY];
            if (cur == Chunk.NULL_BLOCK_STORAGE || cur.isEmpty()) continue;
            if (cur == t.expected) {
                storages[subY] = t.carved;
                selfMask |= (1 << subY);
                for (int j = 0, m = t.teRemovals.size(); j < m; j++) teRemovals.add(t.teRemovals.getLong(j));
                for (int j = 0, m = t.neighborNotifies.size(); j < m; j++)
                    neighborNotifies.add(t.neighborNotifies.getLong(j));
                if (!t.neighborMask.isEmpty()) {
                    ObjectIterator<Long2IntMap.Entry> it = t.neighborMask.long2IntEntrySet().fastIterator();
                    while (it.hasNext()) {
                        Long2IntMap.Entry e = it.next();
                        neighborMask.put(e.getLongKey(), neighborMask.get(e.getLongKey()) | e.getIntValue());
                    }
                }
                t.mask.free();
            } else {
                final BitMask mask = t.mask;
                pool.submit(() -> {
                    ExtendedBlockStorage[] ebs = ChunkUtil.getLoadedEBS(world, job.chunkPos());
                    if (ebs == null) {
                        enqueueForMissingChunk(job.chunkPos(), new ResumeItem(subY, mask));
                    } else {
                        CarveSubTask rebuilt = prepareOneSub(cx, cz, subY, mask, ebs);
                        if (rebuilt != null)
                            pendingCarves.add(new PendingCarve(job.chunkPos(), Collections.singletonList(rebuilt)));
                        else mask.free();
                        maybeFinish();
                    }
                });
            }
        }

        if (selfMask != 0) {
            applyMasks(chunk, job.chunkPos(), teRemovals, neighborNotifies, selfMask, neighborMask);
        } else {
            LONG_LIST_POOL.recycle(teRemovals);
            LONG_LIST_POOL.recycle(neighborNotifies);
            LONG2INT_POOL.recycle(neighborMask);
        }
    }

    private void applyMasks(Chunk chunk, long cpLong, LongArrayList teRemovals, LongArrayList neighborNotifies, int selfMask, Long2IntOpenHashMap neighborMask) {
        pendingCarveNotifies.incrementAndGet();
        try {
            chunkFixup(cpLong, teRemovals, neighborNotifies, selfMask, neighborMask);
            chunk.markDirty();
        } finally {
            LONG_LIST_POOL.recycle(teRemovals);
            LONG_LIST_POOL.recycle(neighborNotifies);
            LONG2INT_POOL.recycle(neighborMask);
            if (pendingCarveNotifies.decrementAndGet() == 0) {
                maybeFinish();
            }
        }
    }

    private void chunkFixup(long cpLong, LongArrayList teRemovals, LongArrayList neighborNotifies, int selfMask, Long2IntOpenHashMap neighborMask) {
        sectionMaskByChunk.put(cpLong, sectionMaskByChunk.get(cpLong) | selfMask);
        ObjectIterator<Long2IntMap.Entry> iterator = neighborMask.long2IntEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Long2IntMap.Entry e = iterator.next();
            long cpk = e.getLongKey();
            int m = e.getIntValue();
            sectionMaskByChunk.put(cpk, sectionMaskByChunk.get(cpk) | m);
        }
        MutableBlockPos p = TL_POS.get();
        for (int i = 0, n = teRemovals.size(); i < n; i++) {
            long lp = teRemovals.getLong(i);
            Library.fromLong(p, lp);
            world.removeTileEntity(p);
        }
        for (int i = 0, n = neighborNotifies.size(); i < n; i++) {
            long lp = neighborNotifies.getLong(i);
            Library.fromLong(p, lp);
            world.notifyNeighborsOfStateChange(p, Blocks.AIR, true);
        }
    }

    private interface CarveApplier {
        void apply(long cpLong, ExtendedBlockStorage[] storages, Int2ObjectOpenHashMap<BitMask> masks);
    }

    private static final class CarveSubTask {
        final int subY;
        final BitMask mask;
        final LongArrayList teRemovals = new LongArrayList(64);
        final LongArrayList neighborNotifies = new LongArrayList(128);
        final Long2IntOpenHashMap neighborMask = new Long2IntOpenHashMap();
        ExtendedBlockStorage expected;
        ExtendedBlockStorage carved;

        CarveSubTask(int subY, BitMask mask) {
            this.subY = subY;
            this.mask = mask;
        }
    }

    private static final class ResumeItem {
        static final int CARVE = 0;
        static final int APPLY_MASKS = 1;
        static final int APPLY_AGG = 2;
        private final int kind;
        private final int subY;
        private final BitMask mask;
        private final Int2ObjectOpenHashMap<BitMask> masks;
        private final ChunkAgg agg;

        private ResumeItem(int kind, int subY, BitMask mask, Int2ObjectOpenHashMap<BitMask> masks, ChunkAgg agg) {
            this.kind = kind;
            this.subY = subY;
            this.mask = mask;
            this.masks = masks;
            this.agg = agg;
        }

        ResumeItem(int subY, BitMask mask) {
            this(CARVE, subY, mask, null, null);
        }

        ResumeItem(Int2ObjectOpenHashMap<BitMask> masks) {
            this(APPLY_MASKS, 0, null, masks, null);
        }

        ResumeItem(ChunkAgg agg) {
            this(APPLY_AGG, 0, null, null, agg);
        }
    }

    @Desugar
    private record PendingCarve(long chunkPos, List<CarveSubTask> tasks) {
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        if (!CompatibilityConfig.isWarDim(world)) {
            this.collectFinished = this.consolidationFinished = this.destroyFinished = true;
            return;
        }
        this.algorithm = nbt.getInteger("algorithm");
        this.rayCount = nbt.getInteger("rayCount");
        this.isContained = nbt.getBoolean("isContained");
        this.collectFinished = nbt.getBoolean("collectDone");
        this.consolidationFinished = nbt.getBoolean("consolidateDone");
        this.destroyFinished = nbt.getBoolean("destroyDone");
        if (nbt.hasKey("sectionMask", Constants.NBT.TAG_LIST)) {
            NBTTagList list = nbt.getTagList("sectionMask", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound t = list.getCompoundTagAt(i);
                long ck = ChunkPos.asLong(t.getInteger("cX"), t.getInteger("cZ"));
                sectionMaskByChunk.put(ck, t.getInteger("mask"));
            }
        }
        if (nbt.hasKey("destructionMap", Constants.NBT.TAG_LIST)) {
            NBTTagList list = nbt.getTagList("destructionMap", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound tag = list.getCompoundTagAt(i);
                long ck = ChunkPos.asLong(tag.getInteger("cX"), tag.getInteger("cZ"));
                long[] bitsetData = ((NBTTagLongArray) tag.getTag("bitset")).data;
                ConcurrentBitSet bs = ConcurrentBitSet.fromLongArray(bitsetData, BITSET_SIZE);
                destructionMap.put(ck, bs);
            }
        }
        if (collectFinished && consolidationFinished && !destroyFinished) {
            world.addScheduledTask(() -> {
                secondPass();
                destroyFinished = true;
            });
        } else if (!collectFinished || !consolidationFinished) {
            if (!CompatibilityConfig.isWarDim(world)) {
                this.collectFinished = this.consolidationFinished = this.destroyFinished = true;
                return;
            }
            this.pendingRays.set(this.rayCount);
            initializeAndStartWorkers();
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        if (!BombConfig.enableNukeNBTSaving) return;
        nbt.setInteger("algorithm", this.algorithm);
        nbt.setInteger("rayCount", this.rayCount);
        nbt.setBoolean("isContained", this.isContained);
        nbt.setBoolean("collectDone", this.collectFinished);
        nbt.setBoolean("consolidateDone", this.consolidationFinished);
        nbt.setBoolean("destroyDone", this.destroyFinished);
        if (collectFinished && consolidationFinished && !destroyFinished && !sectionMaskByChunk.isEmpty()) {
            NBTTagList list = new NBTTagList();
            ObjectIterator<Long2IntMap.Entry> iterator = sectionMaskByChunk.long2IntEntrySet().fastIterator();
            while (iterator.hasNext()) {
                Long2IntMap.Entry e = iterator.next();
                long ck = e.getLongKey();
                int mask = e.getIntValue();
                NBTTagCompound t = new NBTTagCompound();
                t.setInteger("cX", ChunkUtil.getChunkPosX(ck));
                t.setInteger("cZ", ChunkUtil.getChunkPosZ(ck));
                t.setInteger("mask", mask);
                list.appendTag(t);
            }
            nbt.setTag("sectionMask", list);
        }
        if (!this.collectFinished && !this.destructionMap.isEmpty()) {
            NBTTagList list = new NBTTagList();
            this.destructionMap.forEach((long ck, ConcurrentBitSet bitset) -> {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setInteger("cX", ChunkUtil.getChunkPosX(ck));
                tag.setInteger("cZ", ChunkUtil.getChunkPosZ(ck));
                tag.setTag("bitset", new NBTTagLongArray(bitset.toLongArray()));
                list.appendTag(tag);
            });
            nbt.setTag("destructionMap", list);
        }
    }

    private void handleMissingChunk(LocalAgg agg, long chunkPos, int dirIndex) {
        MpscIntArrayListCollector group = waitingRoom.get(chunkPos);
        if (group == null) {
            MpscIntArrayListCollector created = new MpscIntArrayListCollector();
            MpscIntArrayListCollector prev = waitingRoom.putIfAbsent(chunkPos, created);
            group = (prev != null) ? prev : created;
            if (prev == null) {
                chunkLoadQueue.add(chunkPos);
                group.push(dirIndex);
                return;
            }
        }
        agg.deferMissing(chunkPos, dirIndex);
    }

    private void flushDeferredMissing(LocalAgg agg) {
        if (!agg.hasDeferredMissing()) return;
        Long2ObjectOpenHashMap<IntArrayList> map = agg.missingChunks();
        ObjectIterator<Long2ObjectMap.Entry<IntArrayList>> iterator = map.long2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Long2ObjectMap.Entry<IntArrayList> entry = iterator.next();
            IntArrayList list = entry.getValue();
            if (list == null || list.isEmpty()) continue;
            long chunkPos = entry.getLongKey();
            MpscIntArrayListCollector collector = waitingRoom.get(chunkPos);
            boolean amFirst = false;
            if (collector == null) {
                MpscIntArrayListCollector created = new MpscIntArrayListCollector();
                MpscIntArrayListCollector prev = waitingRoom.putIfAbsent(chunkPos, created);
                if (prev == null) {
                    collector = created;
                    amFirst = true;
                } else {
                    collector = prev;
                }
            }
            collector.pushBatch(list);
            boolean requeued = ensureCollectorRegistered(chunkPos, collector);
            if (amFirst || requeued) chunkLoadQueue.add(chunkPos);
            list.clear();
            INT_LIST_POOL.recycle(list);
        }
        map.clear();
    }

    private boolean ensureCollectorRegistered(long chunkPos, MpscIntArrayListCollector collector) {
        while (true) {
            MpscIntArrayListCollector current = waitingRoom.get(chunkPos);
            if (current == collector) return false;
            if (current == null) {
                if (waitingRoom.putIfAbsent(chunkPos, collector) == null) return true;
                continue;
            }
            IntArrayList drained = collector.drain();
            if (!drained.isEmpty()) {
                for (int i = drained.size() - 1; i >= 0; i--) {
                    current.push(drained.getInt(i));
                }
            }
            collector = current;
        }
    }

    private void enqueueForMissingChunk(long chunkPos, ResumeItem item) {
        MpscLinkedAtomicQueue<ResumeItem> q = postLoadQueues.get(chunkPos);
        if (q == null) {
            MpscLinkedAtomicQueue<ResumeItem> created = new MpscLinkedAtomicQueue<>();
            MpscLinkedAtomicQueue<ResumeItem> prev = postLoadQueues.putIfAbsent(chunkPos, created);
            q = prev == null ? created : prev;
            if (prev == null) chunkLoadQueue.add(chunkPos);
        }
        q.add(item);
    }

    private boolean traceSingle(int dirIndex, LocalAgg agg) {
        double energy = strength * INITIAL_ENERGY_FACTOR;
        final double px = explosionX, py = explosionY, pz = explosionZ;
        int x = originX, y = originY, z = originZ;

        double currentRayPosition = 0.0;
        final double[] d = TL_DIR.get();
        directionFromIndex(dirIndex, d);
        final double dirX = d[0], dirY = d[1], dirZ = d[2];

        final double absDirX = Math.abs(dirX);
        final int stepX = (absDirX < RAY_DIRECTION_EPSILON) ? 0 : (dirX > 0 ? 1 : -1);
        final double tDeltaX = (stepX == 0) ? Double.POSITIVE_INFINITY : 1.0 / absDirX;

        final double absDirY = Math.abs(dirY);
        final int stepY = (absDirY < RAY_DIRECTION_EPSILON) ? 0 : (dirY > 0 ? 1 : -1);
        final double tDeltaY = (stepY == 0) ? Double.POSITIVE_INFINITY : 1.0 / absDirY;

        final double absDirZ = Math.abs(dirZ);
        final int stepZ = (absDirZ < RAY_DIRECTION_EPSILON) ? 0 : (dirZ > 0 ? 1 : -1);
        final double tDeltaZ = (stepZ == 0) ? Double.POSITIVE_INFINITY : 1.0 / absDirZ;

        final double minDeltaT = Math.min(tDeltaX, Math.min(tDeltaY, tDeltaZ));
        final double radiusLimit = radius - PROCESSING_EPSILON;
        final boolean useAggDamage = (algorithm == 2);

        double tMaxX = (stepX == 0) ? Double.POSITIVE_INFINITY : ((stepX > 0 ? (x + 1 - px) : (px - x)) * tDeltaX);
        double tMaxY = (stepY == 0) ? Double.POSITIVE_INFINITY : ((stepY > 0 ? (y + 1 - py) : (py - y)) * tDeltaY);
        double tMaxZ = (stepZ == 0) ? Double.POSITIVE_INFINITY : ((stepZ > 0 ? (z + 1 - pz) : (pz - z)) * tDeltaZ);

        long cachedCPLong = 0L;
        ExtendedBlockStorage[] storages = null;
        int lastCX = Integer.MIN_VALUE, lastCZ = Integer.MIN_VALUE;
        int chunkMinX = 0, chunkMaxX = 0, chunkMinZ = 0, chunkMaxZ = 0;
        int emptySubMask = 0;
        long bitsetCPLong = Long.MIN_VALUE;
        ConcurrentBitSet currentBits = null;
        int loopCount = 0;

        try {
            if (energy <= 0) return true;
            while (energy > 0) {
                if ((loopCount++ & 0x3FF) == 0) {
                    if (y < 0 || y >= WORLD_HEIGHT || Thread.currentThread().isInterrupted()) break;
                    if (currentRayPosition >= radiusLimit) break;
                } else {
                    if (currentRayPosition >= radiusLimit) break;
                    if (y < 0 || y >= WORLD_HEIGHT) break;
                }
                final int cx = x >> 4;
                final int cz = z >> 4;

                if (cx != lastCX || cz != lastCZ) {
                    cachedCPLong = ChunkPos.asLong(cx, cz);
                    storages = ChunkUtil.getLoadedEBS(world, cachedCPLong);
                    if (storages == null) {
                        handleMissingChunk(agg, cachedCPLong, dirIndex);
                        return false;
                    }
                    lastCX = cx;
                    lastCZ = cz;
                    chunkMinX = cx << 4;
                    chunkMaxX = chunkMinX + 16;
                    chunkMinZ = cz << 4;
                    chunkMaxZ = chunkMinZ + 16;
                    int mask = 0;
                    for (int i = 0; i < storages.length; i++) {
                        final ExtendedBlockStorage s = storages[i];
                        if (s == Chunk.NULL_BLOCK_STORAGE || s.isEmpty()) {
                            mask |= (1 << i);
                        }
                    }
                    emptySubMask = mask;
                    if (!useAggDamage) {
                        if (bitsetCPLong != cachedCPLong) {
                            ConcurrentBitSet bs = destructionMap.get(cachedCPLong);
                            if (bs == null) {
                                ConcurrentBitSet created = new ConcurrentBitSet(BITSET_SIZE);
                                ConcurrentBitSet prev = destructionMap.putIfAbsent(cachedCPLong, created);
                                bs = (prev != null) ? prev : created;
                            }
                            currentBits = bs;
                            bitsetCPLong = cachedCPLong;
                        }
                    }
                }

                final int subY = y >> 4;
                final boolean subIsEmpty = ((emptySubMask >>> subY) & 1) != 0;
                if (subIsEmpty) {
                    final double minY = (subY << 4);
                    final double maxY = minY + 16.0;

                    double tExitAbs = Double.POSITIVE_INFINITY;
                    if (stepX > 0) tExitAbs = Math.min(tExitAbs, (chunkMaxX - px) * tDeltaX);
                    else if (stepX < 0) tExitAbs = Math.min(tExitAbs, (px - chunkMinX) * tDeltaX);
                    if (stepY > 0) tExitAbs = Math.min(tExitAbs, (maxY - py) * tDeltaY);
                    else if (stepY < 0) tExitAbs = Math.min(tExitAbs, (py - minY) * tDeltaY);
                    if (stepZ > 0) tExitAbs = Math.min(tExitAbs, (chunkMaxZ - pz) * tDeltaZ);
                    else if (stepZ < 0) tExitAbs = Math.min(tExitAbs, (pz - chunkMinZ) * tDeltaZ);

                    double deltaT = Math.min(tExitAbs, radius) - currentRayPosition;
                    if (deltaT <= PROCESSING_EPSILON) deltaT = minDeltaT;
                    currentRayPosition += deltaT;
                    if (currentRayPosition >= radiusLimit) break;

                    final double bias = 1e-9;
                    x = (int) Math.floor(px + dirX * (currentRayPosition - bias));
                    y = (int) Math.floor(py + dirY * (currentRayPosition - bias));
                    z = (int) Math.floor(pz + dirZ * (currentRayPosition - bias));

                    tMaxX = (stepX == 0) ? Double.POSITIVE_INFINITY : ((stepX > 0 ? (x + 1 - px) : (px - x)) * tDeltaX);
                    tMaxY = (stepY == 0) ? Double.POSITIVE_INFINITY : ((stepY > 0 ? (y + 1 - py) : (py - y)) * tDeltaY);
                    tMaxZ = (stepZ == 0) ? Double.POSITIVE_INFINITY : ((stepZ > 0 ? (z + 1 - pz) : (pz - z)) * tDeltaZ);
                    continue;
                }
                final ExtendedBlockStorage storage = storages[subY];

                final int xLocal = x & 0xF, yLocal = y & 0xF, zLocal = z & 0xF;
                final IBlockState state = storage.get(xLocal, yLocal, zLocal);

                final double tExitVoxel = Math.min(tMaxX, Math.min(tMaxY, tMaxZ));
                double segLen = tExitVoxel - currentRayPosition;
                double remaining = radius - currentRayPosition;
                if (remaining <= PROCESSING_EPSILON) break;
                final boolean clipAtRadius = segLen > remaining - 1e-12;
                if (clipAtRadius) segLen = remaining;

                if (state.getBlock() != Blocks.AIR && segLen > PROCESSING_EPSILON) {
                    final float resistance = getNukeResistance(state);
                    if (resistance >= NUKE_RESISTANCE_CUTOFF) {
                        energy = 0;
                    } else {
                        final double distFrac = Math.max(currentRayPosition, MIN_EFFECTIVE_DIST_FOR_ENERGY_CALC) * invRadius;
                        final double energyLoss = getEnergyLossFactor(resistance, distFrac) * segLen;
                        energy -= energyLoss;

                        if (useAggDamage) {
                            final int bitIndex = ((WORLD_HEIGHT - 1 - y) << 8) | ((x & 0xF) << 4) | (z & 0xF);
                            final double damageInc = Math.max(DAMAGE_PER_BLOCK * segLen, energyLoss) * INITIAL_ENERGY_FACTOR;
                            if (damageInc > 0) agg.dmg(cachedCPLong).add(bitIndex, damageInc);
                            agg.len(cachedCPLong).add(bitIndex, segLen);
                        } else if (energy > 0) {
                            if (energyLoss > 0) {
                                final int bitIndex = ((WORLD_HEIGHT - 1 - y) << 8) | ((x & 0xF) << 4) | (z & 0xF);
                                currentBits.set(bitIndex);
                            }
                        }
                    }
                }

                currentRayPosition = tExitVoxel;
                if (energy <= 0.0 || clipAtRadius) break;
                if (tMaxX < tMaxY) {
                    if (tMaxX < tMaxZ) {
                        x += stepX;
                        tMaxX += tDeltaX;
                    } else {
                        z += stepZ;
                        tMaxZ += tDeltaZ;
                    }
                } else {
                    if (tMaxY < tMaxZ) {
                        y += stepY;
                        tMaxY += tDeltaY;
                    } else {
                        z += stepZ;
                        tMaxZ += tDeltaZ;
                    }
                }
            }

            if (energy > 0) this.isContained = false;
            return true;
        } catch (Exception e) {
            MainRegistry.logger.error("Ray {} finished exceptionally", dirIndex, e);
            return true;
        }
    }

    private void mergeLocalAgg(LocalAgg agg) {
        if (algorithm != 2) return;
        ObjectIterator<Long2ObjectMap.Entry<IntDoubleAccumulator>> damageIter = agg.localDamage.long2ObjectEntrySet().fastIterator();
        while (damageIter.hasNext()) {
            Long2ObjectMap.Entry<IntDoubleAccumulator> entry = damageIter.next();
            final long ck = entry.getLongKey();
            final IntDoubleAccumulator accumulator = entry.getValue();
            aggMap.computeIfAbsent(ck, k -> new ChunkAgg()).merge(accumulator, true);
        }
        ObjectIterator<Long2ObjectMap.Entry<IntDoubleAccumulator>> lenIter = agg.localLen.long2ObjectEntrySet().fastIterator();
        while (lenIter.hasNext()) {
            Long2ObjectMap.Entry<IntDoubleAccumulator> entry = lenIter.next();
            final long ck = entry.getLongKey();
            final IntDoubleAccumulator accumulator = entry.getValue();
            aggMap.computeIfAbsent(ck, k -> new ChunkAgg()).merge(accumulator, false);
        }
        agg.clear();
    }

    private static final class IntDoubleAccumulator {
        private static final int EMPTY = Integer.MIN_VALUE;
        private static final int BASE_CAPACITY = 64;
        private static final int MAX_RETAINED_CAPACITY = 4096;

        private int[] keys;
        private double[] vals;
        private int mask;
        private int size;
        private int resizeThreshold;

        IntDoubleAccumulator() {
            this(BASE_CAPACITY);
        }

        IntDoubleAccumulator(int expected) {
            init(capFor(expected));
        }

        private void init(int capacity) {
            keys = new int[capacity];
            Arrays.fill(keys, EMPTY);
            vals = new double[capacity];
            mask = capacity - 1;
            size = 0;
            resizeThreshold = (int) (capacity * 0.67);
        }

        void clear() {
            if (keys.length > MAX_RETAINED_CAPACITY) {
                init(BASE_CAPACITY);
                return;
            }
            Arrays.fill(keys, EMPTY);
            Arrays.fill(vals, 0.0);
            size = 0;
        }

        void add(int key, double delta) {
            int idx = key & mask;
            while (true) {
                int k = keys[idx];
                if (k == EMPTY) {
                    keys[idx] = key;
                    vals[idx] = delta;
                    if (++size >= resizeThreshold) rehash();
                    return;
                } else if (k == key) {
                    vals[idx] += delta;
                    return;
                }
                idx = (idx + 1) & mask;
            }
        }

        int size() {
            return size;
        }

        void accumulateTo(Int2DoubleOpenHashMap map) {
            for (int i = 0; i < keys.length; i++) {
                int k = keys[i];
                if (k != EMPTY) map.addTo(k, vals[i]);
            }
        }

        private void rehash() {
            int[] oldK = keys;
            double[] oldV = vals;
            init(oldK.length << 1);
            for (int i = 0; i < oldK.length; i++) {
                int k = oldK[i];
                if (k != EMPTY) add(k, oldV[i]);
            }
        }
    }

    private static final class LocalAgg {
        final Long2ObjectOpenHashMap<IntDoubleAccumulator> localDamage = new Long2ObjectOpenHashMap<>(16);
        final Long2ObjectOpenHashMap<IntDoubleAccumulator> localLen = new Long2ObjectOpenHashMap<>(16);
        final Long2ObjectOpenHashMap<IntArrayList> deferredMissing = new Long2ObjectOpenHashMap<>(4);

        void clear() {
            localDamage.clear();
            localLen.clear();
            if (!deferredMissing.isEmpty()) {
                for (IntArrayList list : deferredMissing.values()) {
                    list.clear();
                    INT_LIST_POOL.recycle(list);
                }
                deferredMissing.clear();
            }
        }

        void deferMissing(long chunkPos, int dirIndex) {
            IntArrayList list = deferredMissing.get(chunkPos);
            if (list == null) {
                list = INT_LIST_POOL.borrow();
                deferredMissing.put(chunkPos, list);
            }
            list.add(dirIndex);
        }

        boolean hasDeferredMissing() {
            return !deferredMissing.isEmpty();
        }

        Long2ObjectOpenHashMap<IntArrayList> missingChunks() {
            return deferredMissing;
        }

        IntDoubleAccumulator dmg(long cp) {
            IntDoubleAccumulator acc = localDamage.get(cp);
            if (acc == null) {
                acc = ACC_POOL.borrow();
                localDamage.put(cp, acc);
            }
            return acc;
        }

        IntDoubleAccumulator len(long cp) {
            IntDoubleAccumulator acc = localLen.get(cp);
            if (acc == null) {
                acc = ACC_POOL.borrow();
                localLen.put(cp, acc);
            }
            return acc;
        }
    }

    private static final class ChunkAgg {
        final ReentrantLock lock = new ReentrantLock(false);
        final Int2DoubleOpenHashMap damage = new Int2DoubleOpenHashMap();
        final Int2DoubleOpenHashMap passLen = new Int2DoubleOpenHashMap();
        final MpscLinkedAtomicQueue<IntDoubleAccumulator> qDmg = new MpscLinkedAtomicQueue<>();
        final MpscLinkedAtomicQueue<IntDoubleAccumulator> qLen = new MpscLinkedAtomicQueue<>();

        void merge(@NotNull IntDoubleAccumulator accumulator, boolean isDamage) {
            if (lock.tryLock()) {
                try {
                    drainUnlocked();
                    if (accumulator.size() > 0) {
                        if (isDamage) accumulator.accumulateTo(damage);
                        else accumulator.accumulateTo(passLen);
                    }
                    ACC_POOL.recycle(accumulator);
                } finally {
                    lock.unlock();
                }
            } else {
                if (isDamage) qDmg.add(accumulator);
                else qLen.add(accumulator);
            }
        }

        void drainUnlocked() {
            IntDoubleAccumulator a;
            while ((a = qDmg.poll()) != null) {
                if (a.size() > 0) a.accumulateTo(damage);
                ACC_POOL.recycle(a);
            }
            while ((a = qLen.poll()) != null) {
                if (a.size() > 0) a.accumulateTo(passLen);
                ACC_POOL.recycle(a);
            }
        }

        void clear() {
            lock.lock();
            try {
                damage.clear();
                passLen.clear();
                qDmg.clear();
                qLen.clear();
            } finally {
                lock.unlock();
            }
        }
    }

    private final class FastApplier implements CarveApplier {
        @Override
        public void apply(long cpLong, ExtendedBlockStorage[] storages, Int2ObjectOpenHashMap<BitMask> masks) {
            if (masks == null || masks.isEmpty()) return;
            final LongArrayList teRemovals = LONG_LIST_POOL.borrow();
            final LongArrayList neighborNotifies = LONG_LIST_POOL.borrow();
            teRemovals.clear();
            neighborNotifies.clear();
            int selfMask = 0;
            final Long2IntOpenHashMap neighborMask = LONG2INT_POOL.borrow();
            neighborMask.clear();

            ObjectIterator<Int2ObjectMap.Entry<BitMask>> it = masks.int2ObjectEntrySet().fastIterator();
            while (it.hasNext()) {
                Int2ObjectMap.Entry<BitMask> e = it.next();
                BitMask bs = e.getValue();
                selfMask = carveSubchunkAndSwap(bs, cpLong, storages, teRemovals, neighborNotifies, selfMask, neighborMask, e.getIntKey());
                bs.free();
            }
            if (selfMask != 0) {
                notifyMainThread(cpLong, teRemovals, neighborNotifies, selfMask, neighborMask);
            } else {
                LONG_LIST_POOL.recycle(teRemovals);
                LONG_LIST_POOL.recycle(neighborNotifies);
                LONG2INT_POOL.recycle(neighborMask);
            }
        }
    }

    private final class SafeApplier implements CarveApplier {
        @Override
        public void apply(long cpLong, ExtendedBlockStorage[] storages, Int2ObjectOpenHashMap<BitMask> masks) {
            prepareAndEnqueue(cpLong, masks, storages);
        }
    }

    private class RayTracerTask extends RecursiveAction {
        private final int start, end, threshold;

        RayTracerTask(int start, int end, int threshold) {
            this.start = start;
            this.end = end;
            this.threshold = Math.max(1, threshold);
        }

        @Override
        protected void compute() {
            int len = end - start;
            if (len <= threshold) {
                LocalAgg agg = TL_LOCAL_AGG.get();
                agg.clear();
                int completed = 0;
                for (int i = start; i < end; i++) {
                    if (Thread.currentThread().isInterrupted()) break;
                    if (traceSingle(i, agg)) completed++;
                }
                flushDeferredMissing(agg);
                mergeLocalAgg(agg);
                if (completed > 0) {
                    if (pendingRays.addAndGet(-completed) == 0) onAllRaysFinished();
                }
            } else {
                int mid = start + (len >>> 1);
                invokeAll(new RayTracerTask(start, mid, threshold), new RayTracerTask(mid, end, threshold));
            }
        }
    }

    private class ResumeBatchTask extends RecursiveAction {
        private final IntArrayList indices;
        private final int start, end, threshold;

        ResumeBatchTask(IntArrayList indices, int start, int end, int threshold) {
            this.indices = indices;
            this.start = start;
            this.end = end;
            this.threshold = Math.max(1, threshold);
        }

        @Override
        protected void compute() {
            int len = end - start;
            if (len <= threshold) {
                LocalAgg agg = TL_LOCAL_AGG.get();
                agg.clear();
                int completed = 0;
                for (int i = start; i < end; i++) {
                    int dirIndex = indices.getInt(i);
                    if (Thread.currentThread().isInterrupted()) break;
                    if (traceSingle(dirIndex, agg)) completed++;
                }
                flushDeferredMissing(agg);
                mergeLocalAgg(agg);
                if (completed > 0) {
                    if (pendingRays.addAndGet(-completed) == 0) onAllRaysFinished();
                }
            } else {
                int mid = start + (len >>> 1);
                invokeAll(new ResumeBatchTask(indices, start, mid, threshold), new ResumeBatchTask(indices, mid, end, threshold));
            }
        }
    }
}
