package com.hbm.entity.effect;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.BombConfig;
import com.hbm.config.CompatibilityConfig;
import com.hbm.config.FalloutConfigJSON;
import com.hbm.config.FalloutConfigJSON.FalloutEntry;
import com.hbm.config.WorldConfig;
import com.hbm.entity.logic.EntityExplosionChunkloading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.Library;
import com.hbm.lib.maps.NonBlockingLong2LongHashMap;
import com.hbm.lib.queues.MpmcUnboundedXaddArrayLongQueue;
import com.hbm.lib.queues.MpscUnboundedXaddArrayLongQueue;
import com.hbm.util.ChunkUtil;
import com.hbm.world.WorldUtil;
import com.hbm.world.biome.BiomeGenCraterBase;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.jctools.queues.MpscUnboundedXaddArrayQueue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import static com.hbm.config.BombConfig.safeCommit;
import static com.hbm.lib.internal.UnsafeHolder.U;
import static com.hbm.lib.internal.UnsafeHolder.fieldOffset;

@AutoRegister(name = "entity_fallout_rain", trackingRange = 1000)
public class EntityFalloutRain extends EntityExplosionChunkloading {

    private static final DataParameter<Integer> SCALE = EntityDataManager.createKey(EntityFalloutRain.class, DataSerializers.VARINT);
    private static final int MAX_SOLID_DEPTH = 3;
    private static final int MIN_ANGLE_STEPS = 18;
    private static final int SPOKE_STEP_BLOCKS = 8;

    private static final ThreadLocal<MutableBlockPos> TL_POS = ThreadLocal.withInitial(MutableBlockPos::new);
    private static final ThreadLocal<WorkerScratch> TL_WORKER = ThreadLocal.withInitial(WorkerScratch::new);

    private static final long EMPTY_LONG = Long.MIN_VALUE;
    private static final long WAIT_INNER = 1L;
    private static final long WAIT_OUTER = 2L;

    private static final long OFF_PENDING_CHUNKS = fieldOffset(EntityFalloutRain.class, "pendingChunks");
    private static final long OFF_PENDING_MAIN = fieldOffset(EntityFalloutRain.class, "pendingMainThreadNotifies");
    private static final long OFF_MAP_ACQUIRED = fieldOffset(EntityFalloutRain.class, "mapAcquired");
    private static final long OFF_FINISH_QUEUED = fieldOffset(EntityFalloutRain.class, "finishQueued");
    private static final long OFF_FINISHED = fieldOffset(EntityFalloutRain.class, "finished");
    private static final long OFF_INNER_CURSOR = fieldOffset(EntityFalloutRain.class, "innerCursor");
    private static final long OFF_OUTER_CURSOR = fieldOffset(EntityFalloutRain.class, "outerCursor");
    public UUID detonator;
    private LongArrayList chunksToProcess;
    private LongArrayList outerChunksToProcess;
    private MpmcUnboundedXaddArrayLongQueue qInner;
    private MpmcUnboundedXaddArrayLongQueue qOuter;
    private MpscUnboundedXaddArrayLongQueue chunkLoadQueue;
    private MpscUnboundedXaddArrayQueue<Runnable> mainTasks;
    private NonBlockingLong2LongHashMap waitingRoom;
    private Long2IntOpenHashMap sectionMaskByChunk;
    private MainScratch mainScratch;
    @SuppressWarnings("unused")
    private volatile int pendingChunks, pendingMainThreadNotifies, finished, mapAcquired, finishQueued;
    @SuppressWarnings("unused")
    private volatile int innerCursor, outerCursor;

    private ForkJoinPool pool;
    private final Runnable finishRunnable = this::secondPassAndFinish;

    private int tickDelay = BombConfig.falloutDelay;
    private boolean biomeChange = true;

    public EntityFalloutRain(World worldIn) {
        super(worldIn);
        this.setSize(4.0F, 20.0F);
        this.ignoreFrustumCheck = true;
        this.isImmuneToFire = true;
    }

    public EntityFalloutRain(World worldIn, int ignored) {
        this(worldIn);
    }

    private static int ceilPow2(int x) {
        if (x <= 1) return 1;
        int hb = Integer.highestOneBit(x - 1);
        int r = hb << 1;
        return r > 0 ? r : (1 << 30);
    }

    private static int chooseChunkSizeForSegments(int peakDepth, int targetSegments, int minPow2, int maxPow2) {
        if (peakDepth <= 0) return minPow2;
        int want = (peakDepth + targetSegments - 1) / targetSegments;
        int cs = ceilPow2(want);
        if (cs < minPow2) cs = minPow2;
        if (cs > maxPow2) cs = maxPow2;
        return cs;
    }

    private static Biome getBiomeChange(double distPercent, int scale, Biome original) {
        if (!WorldConfig.enableCraterBiomes) return null;
        if (scale >= 150 && distPercent < 15) return BiomeGenCraterBase.craterInnerBiome;
        if (scale >= 100 && distPercent < 55 && original != BiomeGenCraterBase.craterInnerBiome) return BiomeGenCraterBase.craterBiome;
        if (scale >= 25 && original != BiomeGenCraterBase.craterInnerBiome && original != BiomeGenCraterBase.craterBiome)
            return BiomeGenCraterBase.craterOuterBiome;
        return null;
    }

    private static void addAllFromPairs(LongList out, int[] data) {
        if (data == null || data.length == 0) return;
        for (int i = 0; i + 1 < data.length; i += 2) out.add(ChunkPos.asLong(data[i], data[i + 1]));
    }

    private static int[] toPairsArray(LongList coords) {
        if (coords == null || coords.isEmpty()) return new int[0];
        int[] data = new int[coords.size() * 2];
        int i = 0;
        LongIterator it = coords.iterator();
        while (it.hasNext()) {
            long packed = it.nextLong();
            data[i++] = Library.getChunkPosX(packed);
            data[i++] = Library.getChunkPosZ(packed);
        }
        return data;
    }

    private static boolean isEmpty(LongArrayList l) {
        return l == null || l.isEmpty();
    }

    private void initWorkStructuresIfNeeded(int innerSize, int outerSize) {
        if (qInner != null) return;

        final int total = innerSize + outerSize;
        final int csRetryInner = chooseChunkSizeForSegments(Math.max(innerSize, 1024), 32, 1024, 8192);
        final int csRetryOuter = chooseChunkSizeForSegments(Math.max(outerSize, 1024), 32, 1024, 4096);
        final int csLoad = chooseChunkSizeForSegments(Math.max(total, 1024), 32, 1024, 8192);
        final int csTasks = chooseChunkSizeForSegments(Math.max(total, 1024), 64, 1024, 4096);
        final int pooledLong = 4;
        qInner = new MpmcUnboundedXaddArrayLongQueue(csRetryInner, pooledLong);
        qOuter = new MpmcUnboundedXaddArrayLongQueue(csRetryOuter, pooledLong);
        chunkLoadQueue = new MpscUnboundedXaddArrayLongQueue(csLoad, pooledLong);
        mainTasks = new MpscUnboundedXaddArrayQueue<>(csTasks);
        waitingRoom = new NonBlockingLong2LongHashMap(ceilPow2(Math.max(1024, total * 2)));
        sectionMaskByChunk = new Long2IntOpenHashMap(Math.max(1024, total));
        sectionMaskByChunk.defaultReturnValue(0);
        mainScratch = new MainScratch();
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote) {
            if (!CompatibilityConfig.isWarDim(world)) {
                this.setDead();
            } else {
                if (firstUpdate) {
                    if (isEmpty(chunksToProcess) && isEmpty(outerChunksToProcess)) {
                        gatherChunks();
                    }
                    startWorkersIfNeeded();
                }

                runServerThreadBudget(BombConfig.mk5);

                if (tickDelay > 0) tickDelay--;
                else tickDelay = BombConfig.falloutDelay;
            }
        }
        super.onUpdate();
    }

    private void runServerThreadBudget(int timeBudgetMs) {
        if (mainTasks == null || chunkLoadQueue == null) return;

        final long deadline = System.nanoTime() + (long) timeBudgetMs * 1_000_000L;
        while (System.nanoTime() < deadline) {
            Runnable r = mainTasks.relaxedPoll();
            if (r == null) break;
            r.run();
        }
        loadMissingChunksUntil(deadline);
    }

    private void startWorkersIfNeeded() {
        if (this.finished != 0) return;
        final LongArrayList innerList = this.chunksToProcess;
        final LongArrayList outerList = this.outerChunksToProcess;
        final int innerSize = innerList == null ? 0 : innerList.size();
        final int outerSize = outerList == null ? 0 : outerList.size();
        final int total = innerSize + outerSize;
        if (total == 0) {
            this.finished = 1;
            clearChunkLoader();
            setDead();
            return;
        }

        initWorkStructuresIfNeeded(innerSize, outerSize);
        this.innerCursor = 0;
        this.outerCursor = 0;
        this.pendingChunks = total;
        ChunkUtil.acquireMirrorMap((WorldServer) world);
        this.mapAcquired = 1;
        int processors = Runtime.getRuntime().availableProcessors();
        int workers = BombConfig.maxThreads <= 0 ? Math.max(1, processors + BombConfig.maxThreads) : Math.min(BombConfig.maxThreads, processors);
        pool = new ForkJoinPool(Math.max(1, workers));
        final int scale = getScale();
        final MpmcUnboundedXaddArrayLongQueue retryInner = this.qInner;
        final MpmcUnboundedXaddArrayLongQueue retryOuter = this.qOuter;

        for (int i = 0; i < workers; i++) {
            pool.submit(() -> {
                int spins = 0;

                while (!Thread.currentThread().isInterrupted()) {
                    if (this.finished != 0) return;

                    long cp = retryInner.poll();
                    boolean clamp = false;

                    if (cp == EMPTY_LONG) {
                        int idx = U.getAndAddInt(this, OFF_INNER_CURSOR, 1);
                        if (idx < innerSize) {
                            cp = innerList.getLong(idx);
                        } else {
                            cp = retryOuter.poll();
                            clamp = true;
                            if (cp == EMPTY_LONG) {
                                int oidx = U.getAndAddInt(this, OFF_OUTER_CURSOR, 1);
                                if (oidx < outerSize) {
                                    cp = outerList.getLong(oidx);
                                } else {
                                    cp = EMPTY_LONG;
                                }
                            }
                        }
                    }

                    if (cp != EMPTY_LONG) {
                        spins = 0;
                        processChunkOffThread(cp, scale, clamp);
                        continue;
                    }

                    if (this.pendingChunks == 0) return;

                    if (++spins < 256) {
                        Library.onSpinWait();
                    } else {
                        spins = 0;
                        LockSupport.parkNanos(1_000_000L);
                    }
                }
            });
        }
    }

    private void enqueueWork(long cp, boolean clamp) {
        if (clamp) qOuter.offer(cp);
        else qInner.offer(cp);
    }

    private void loadMissingChunksUntil(long deadlineNanos) {
        while (System.nanoTime() < deadlineNanos) {
            long ck = chunkLoadQueue.relaxedPoll();
            if (ck == EMPTY_LONG) break;

            int cx = Library.getChunkPosX(ck);
            int cz = Library.getChunkPosZ(ck);
            world.getChunk(cx, cz);

            long wait = waitingRoom.remove(ck);
            if (wait > 0) enqueueWork(ck, wait == WAIT_OUTER);
        }
    }

    private void processChunkOffThread(long cpLong, int scale, boolean clampToRadius) {
        if (this.finished != 0) return;

        ExtendedBlockStorage[] ebs = ChunkUtil.getLoadedEBS((WorldServer) world, cpLong);
        if (ebs == null) {
            long v = clampToRadius ? WAIT_OUTER : WAIT_INNER;
            long prev = waitingRoom.putIfAbsent(cpLong, v);
            if (prev <= 0) chunkLoadQueue.offer(cpLong);
            return;
        }

        final int chunkX = Library.getChunkPosX(cpLong);
        final int chunkZ = Library.getChunkPosZ(cpLong);
        final int minX = (chunkX << 4);
        final int minZ = (chunkZ << 4);

        final WorkerScratch s = TL_WORKER.get();
        s.clearWorkerPhase();

        final Long2ObjectOpenHashMap<IBlockState> updates = s.updates;
        final Long2IntOpenHashMap biomeChanges = s.biomeChanges;
        final Long2ObjectOpenHashMap<IBlockState> spawnFalling = s.spawnFalling;

        final ThreadLocalRandom rand = ThreadLocalRandom.current();
        final double cx = this.posX, cz = this.posZ;

        for (int lx = 0; lx < 16; lx++) {
            int x = minX + lx;
            for (int lz = 0; lz < 16; lz++) {
                int z = minZ + lz;
                final double distance = Math.hypot(x - cx, z - cz);
                if (clampToRadius && distance > (double) scale) continue;

                final double percent = (double) scale <= 0 ? 100.0 : (distance * 100.0 / (double) scale);

                Biome target = getBiomeChange(percent, scale, world.getBiome(TL_POS.get().setPos(x, 0, z)));
                if (biomeChange && target != null) biomeChanges.put(ChunkPos.asLong(x, z), Biome.getIdForBiome(target));

                stompColumnToUpdates(ebs, x, z, percent, updates, spawnFalling, rand);
            }
        }

        if (updates.isEmpty() && biomeChanges.isEmpty() && spawnFalling.isEmpty()) {
            if (U.getAndAddInt(this, OFF_PENDING_CHUNKS, -1) - 1 == 0) maybeFinish();
            return;
        }

        if (!safeCommit) {
            Chunk chunk = ChunkUtil.getLoadedChunk((WorldServer) world, cpLong);
            if (chunk == null) {
                long v = clampToRadius ? WAIT_OUTER : WAIT_INNER;
                long prev = waitingRoom.putIfAbsent(cpLong, v);
                if (prev <= 0) chunkLoadQueue.offer(cpLong);
                return;
            }

            final Long2ObjectOpenHashMap<IBlockState> changed = new Long2ObjectOpenHashMap<>();

            Chunk old;
            do {
                old = chunk;
                ChunkUtil.applyAndSwap(chunk, c -> updates, changed);
                chunk = ChunkUtil.getLoadedChunk((WorldServer) world, cpLong);
                if (chunk == null) {
                    long v = clampToRadius ? WAIT_OUTER : WAIT_INNER;
                    long prev = waitingRoom.putIfAbsent(cpLong, v);
                    if (prev <= 0) chunkLoadQueue.offer(cpLong);
                    return;
                }
            } while (old != chunk);

            if (changed.isEmpty() && biomeChanges.isEmpty() && spawnFalling.isEmpty()) {
                if (U.getAndAddInt(this, OFF_PENDING_CHUNKS, -1) - 1 == 0) maybeFinish();
                return;
            }

            int mask = 0;
            ObjectIterator<Long2ObjectMap.Entry<IBlockState>> it = changed.long2ObjectEntrySet().fastIterator();
            while (it.hasNext()) {
                Long2ObjectMap.Entry<IBlockState> e = it.next();
                int y = Library.getBlockPosY(e.getLongKey());
                mask |= 1 << (y >>> 4);
            }

            final Long2IntOpenHashMap biomeCopy = biomeChanges.isEmpty() ? null : new Long2IntOpenHashMap(biomeChanges);
            final Long2ObjectOpenHashMap<IBlockState> fallingCopy = spawnFalling.isEmpty() ? null : new Long2ObjectOpenHashMap<>(spawnFalling);
            final int finalMask = mask;

            U.getAndAddInt(this, OFF_PENDING_MAIN, 1);
            mainTasks.offer(() -> {
                try {
                    doNotifyOnMain(cpLong, changed, finalMask, biomeCopy, fallingCopy);
                } finally {
                    if (U.getAndAddInt(this, OFF_PENDING_MAIN, -1) - 1 == 0) maybeFinish();
                }
            });

            if (U.getAndAddInt(this, OFF_PENDING_CHUNKS, -1) - 1 == 0) maybeFinish();
            return;
        }

        ObjectIterator<Long2ObjectMap.Entry<IBlockState>> itUp = updates.long2ObjectEntrySet().fastIterator();
        while (itUp.hasNext()) {
            Long2ObjectMap.Entry<IBlockState> e = itUp.next();
            final long p = e.getLongKey();
            final int x = Library.getBlockPosX(p);
            final int y = Library.getBlockPosY(p);
            final int z = Library.getBlockPosZ(p);
            if ((x >> 4) != chunkX || (z >> 4) != chunkZ || y < 0 || y >= 256) continue;
            s.bucketBySub(y >>> 4).put(Library.blockPosToLocal(x, y, z), e.getValue());
        }

        SubUpdate[] tasks = null;
        int taskCount = 0;

        final boolean hasSky = world.provider.hasSkyLight();

        for (int subY = 0; subY < 16; subY++) {
            final Int2ObjectOpenHashMap<IBlockState> bucketScratch = s.bucketBySub[subY];
            if (bucketScratch == null || bucketScratch.isEmpty()) continue;

            final Int2ObjectOpenHashMap<IBlockState> bucket = new Int2ObjectOpenHashMap<>(bucketScratch.size());
            bucket.putAll(bucketScratch);

            final ExtendedBlockStorage expected = ebs[subY];

            final Long2ObjectOpenHashMap<IBlockState> oldSub = new Long2ObjectOpenHashMap<>();
            Optional<ExtendedBlockStorage> mod = ChunkUtil.copyAndModify(chunkX, chunkZ, subY, hasSky, expected, bucket, oldSub);
            //noinspection OptionalAssignedToNull
            if (mod == null) continue;

            if (tasks == null) tasks = new SubUpdate[16];
            tasks[taskCount++] = new SubUpdate(subY, bucket, expected, mod.orElse(null), oldSub);
        }

        if (taskCount == 0 && biomeChanges.isEmpty() && spawnFalling.isEmpty()) {
            if (U.getAndAddInt(this, OFF_PENDING_CHUNKS, -1) - 1 == 0) maybeFinish();
            return;
        }

        final Long2IntOpenHashMap biomeCopy = biomeChanges.isEmpty() ? null : new Long2IntOpenHashMap(biomeChanges);
        final Long2ObjectOpenHashMap<IBlockState> fallingCopy = spawnFalling.isEmpty() ? null : new Long2ObjectOpenHashMap<>(spawnFalling);
        final SubUpdate[] finalTasks = tasks;
        final int finalTaskCount = taskCount;

        U.getAndAddInt(this, OFF_PENDING_MAIN, 1);
        mainTasks.offer(() -> {
            try {
                final int mask = applyPreparedOnMainInto(chunkX, chunkZ, finalTasks, finalTaskCount);
                final Long2ObjectOpenHashMap<IBlockState> oldStates = mainScratch.oldMerged.isEmpty() ? null : mainScratch.oldMerged;
                doNotifyOnMain(cpLong, oldStates, mask, biomeCopy, fallingCopy);
            } finally {
                mainScratch.oldMerged.clear();
                if (U.getAndAddInt(this, OFF_PENDING_MAIN, -1) - 1 == 0) maybeFinish();
            }
        });

        if (U.getAndAddInt(this, OFF_PENDING_CHUNKS, -1) - 1 == 0) maybeFinish();
    }

    private void doNotifyOnMain(long cpLong, Long2ObjectOpenHashMap<IBlockState> oldStates, int mask, Long2IntOpenHashMap biomeChanges,
                                Long2ObjectOpenHashMap<IBlockState> spawnFalling) {

        int cx = Library.getChunkPosX(cpLong);
        int cz = Library.getChunkPosZ(cpLong);
        Chunk loadedChunk = world.getChunk(cx, cz);

        if (mask != 0) sectionMaskByChunk.put(cpLong, sectionMaskByChunk.get(cpLong) | mask);

        final MutableBlockPos mutableBlockPos = TL_POS.get();

        if (oldStates != null && !oldStates.isEmpty()) {
            ObjectIterator<Long2ObjectMap.Entry<IBlockState>> iterator1 = oldStates.long2ObjectEntrySet().fastIterator();
            while (iterator1.hasNext()) {
                Long2ObjectMap.Entry<IBlockState> stateEntry = iterator1.next();
                long lp = stateEntry.getLongKey();
                IBlockState oldState = stateEntry.getValue();
                Library.fromLong(mutableBlockPos, lp);
                IBlockState newState = world.getBlockState(mutableBlockPos);

                if (newState.getBlock() == ModBlocks.fallout && !ModBlocks.fallout.canPlaceBlockAt(world, mutableBlockPos)) {
                    world.setBlockState(mutableBlockPos, oldState, 3);
                    continue;
                }
                if (oldState != newState) world.notifyBlockUpdate(mutableBlockPos, oldState, newState, 3);
                ChunkUtil.flushTileEntity(loadedChunk, mutableBlockPos, oldState, newState);
                world.notifyNeighborsOfStateChange(mutableBlockPos, newState.getBlock(), true);
            }
        }

        if (biomeChanges != null && !biomeChanges.isEmpty()) {
            ObjectIterator<Long2IntMap.Entry> iterator2 = biomeChanges.long2IntEntrySet().fastIterator();
            while (iterator2.hasNext()) {
                Long2IntMap.Entry be = iterator2.next();
                long packed = be.getLongKey();
                int x = Library.getChunkPosX(packed);
                int z = Library.getChunkPosZ(packed);
                Biome target = Biome.getBiome(be.getIntValue());
                if (target != null) WorldUtil.setBiome(world, x, z, target);
            }
            WorldUtil.syncBiomeChange(world, cx, cz);
        }

        if (spawnFalling != null && !spawnFalling.isEmpty()) {
            ObjectIterator<Long2ObjectMap.Entry<IBlockState>> iterator = spawnFalling.long2ObjectEntrySet().fastIterator();
            while (iterator.hasNext()) {
                Long2ObjectMap.Entry<IBlockState> entry = iterator.next();
                long pos = entry.getLongKey();
                IBlockState state = entry.getValue();
                EntityFallingBlock falling = new EntityFallingBlock(world, Library.getBlockPosX(pos) + 0.5, Library.getBlockPosY(pos) + 0.5,
                        Library.getBlockPosZ(pos) + 0.5, state);
                falling.shouldDropItem = false;
                world.spawnEntity(falling);
            }
        }

        loadedChunk.markDirty();
    }

    private int applyPreparedOnMainInto(int chunkX, int chunkZ, SubUpdate[] tasks, int taskCount) {
        if (taskCount == 0 || tasks == null) return 0;

        final WorldServer ws = (WorldServer) world;
        final boolean hasSky = ws.provider.hasSkyLight();

        Chunk chunk = world.getChunk(chunkX, chunkZ);
        ExtendedBlockStorage[] storages = chunk.getBlockStorageArray();

        final MainScratch ms = this.mainScratch;
        ms.oldMerged.clear();

        int mask = 0;

        for (int i = 0; i < taskCount; i++) {
            SubUpdate t = tasks[i];
            if (t == null) continue;

            final int subY = t.subY;
            ExtendedBlockStorage cur = storages[subY];

            if (cur == t.expected) {
                if (cur != t.prepared) {
                    storages[subY] = t.prepared;
                    mask |= 1 << subY;
                    if (t.oldStates != null && !t.oldStates.isEmpty()) ms.oldMerged.putAll(t.oldStates);
                }
            } else {
                ms.oldSub.clear();
                Optional<ExtendedBlockStorage> rebuilt = ChunkUtil.copyAndModify(chunkX, chunkZ, subY, hasSky, cur, t.toUpdate, ms.oldSub);
                //noinspection OptionalAssignedToNull
                if (rebuilt != null) {
                    ExtendedBlockStorage upd = rebuilt.orElse(null);
                    if (cur != upd) {
                        storages[subY] = upd;
                        mask |= 1 << subY;
                        if (!ms.oldSub.isEmpty()) ms.oldMerged.putAll(ms.oldSub);
                    }
                }
            }
        }

        if (mask != 0) chunk.markDirty();
        return mask;
    }

    private void maybeFinish() {
        if (this.finished != 0) return;
        boolean waitingEmpty = (waitingRoom == null) || waitingRoom.isEmpty();
        if (this.pendingChunks == 0 && waitingEmpty && this.pendingMainThreadNotifies == 0) {
            if (U.compareAndSetInt(this, OFF_FINISH_QUEUED, 0, 1)) {
                mainTasks.offer(finishRunnable);
            }
        }
    }

    private void secondPassAndFinish() {
        this.finishQueued = 0;

        if (this.finished != 0) return;
        if (this.pendingChunks != 0) return;
        if (waitingRoom != null && !waitingRoom.isEmpty()) return;
        if (this.pendingMainThreadNotifies != 0) return;

        if (sectionMaskByChunk != null) {
            ObjectIterator<Long2IntMap.Entry> iterator = sectionMaskByChunk.long2IntEntrySet().fastIterator();
            while (iterator.hasNext()) {
                Long2IntMap.Entry e = iterator.next();
                long cp = e.getLongKey();
                int changedMask = e.getIntValue();
                if (changedMask == 0) continue;
                Chunk chunk = ChunkUtil.getLoadedChunk((WorldServer) world, cp);
                if (chunk == null) continue;
                chunk.generateSkylightMap();
                chunk.resetRelightChecks();
            }
            sectionMaskByChunk.clear();
        }

        this.finished = 1;

        if (pool != null) {
            pool.shutdown();
            try {
                pool.awaitTermination(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignored) {
            }
        }

        if (U.getAndSetInt(this, OFF_MAP_ACQUIRED, 0) != 0) ChunkUtil.releaseMirrorMap((WorldServer) world);
        clearChunkLoader();
        setDead();
    }

    private void stompColumnToUpdates(ExtendedBlockStorage[] ebs, int x, int z, double distPercent, Long2ObjectOpenHashMap<IBlockState> updates,
                                      Long2ObjectOpenHashMap<IBlockState> spawnFalling, ThreadLocalRandom rand) {

        int solidDepth = 0;
        final int lx = x & 15;
        final int lz = z & 15;
        final MutableBlockPos pos = TL_POS.get();
        final float stonebrickRes = Blocks.STONEBRICK.getExplosionResistance(null);

        for (int y = 255; y >= 0; y--) {
            if (solidDepth >= MAX_SOLID_DEPTH) return;

            final int subY = y >>> 4;
            ExtendedBlockStorage s = ebs[subY];
            final IBlockState state = s == Chunk.NULL_BLOCK_STORAGE || s.isEmpty() ? Blocks.AIR.getDefaultState() : s.get(lx, y & 15, lz);
            final Block block = state.getBlock();
            if (block.isAir(state, world, pos.setPos(x, y, z)) || block == ModBlocks.fallout) continue;

            IBlockState stateUp = null;
            final int upY = y + 1;
            if (solidDepth == 0 && upY < 256) {
                final int upSub = upY >>> 4;
                ExtendedBlockStorage su = ebs[upSub];
                stateUp = su == Chunk.NULL_BLOCK_STORAGE || su.isEmpty() ? Blocks.AIR.getDefaultState() : su.get(lx, upY & 15, lz);
                pos.setPos(x, upY, z);
                boolean airOrReplaceable = stateUp.getBlock().isAir(stateUp, world, pos) || stateUp.getBlock().isReplaceable(world,
                        pos) && !stateUp.getMaterial().isLiquid();
                if (airOrReplaceable) {
                    double d = distPercent / 100.0;
                    double chance = 0.1 - Math.pow(d - 0.7, 2.0);
                    if (chance >= rand.nextDouble()) {
                        updates.put(Library.blockPosToLong(x, upY, z), ModBlocks.fallout.getDefaultState());
                    }
                }
            }

            if (distPercent < 65 && block.isFlammable(world, pos.setPos(x, y, z), EnumFacing.UP)) {
                if (upY < 256) {
                    final int upSub = upY >>> 4;
                    if (stateUp == null) {
                        ExtendedBlockStorage su = ebs[upSub];
                        stateUp = su == Chunk.NULL_BLOCK_STORAGE || su.isEmpty() ? Blocks.AIR.getDefaultState() : su.get(lx, upY & 15, lz);
                    }
                    if (stateUp.getBlock().isAir(stateUp, world, pos.setPos(x, upY, z))) {
                        if ((rand.nextInt(5)) == 0) {
                            updates.put(Library.blockPosToLong(x, upY, z), Blocks.FIRE.getDefaultState());
                        }
                    }
                }
            }

            boolean transformed = false;
            List<FalloutEntry> entries = FalloutConfigJSON.entries;
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0, entriesSize = entries.size(); i < entriesSize; i++) {
                FalloutEntry entry = entries.get(i);
                IBlockState result = entry.eval(y, state, distPercent, rand);
                if (result != null) {
                    updates.put(Library.blockPosToLong(x, y, z), result);
                    if (entry.isSolid()) solidDepth++;
                    transformed = true;
                    break;
                }
            }

            if (!transformed && distPercent < 65 && y > 0) {
                final int yBelow = y - 1;
                ExtendedBlockStorage sb = ebs[yBelow >>> 4];
                final IBlockState below = (sb == Chunk.NULL_BLOCK_STORAGE || sb.isEmpty()) ? Blocks.AIR.getDefaultState() : sb.get(lx, yBelow & 15,
                        lz);
                if (below.getBlock().isAir(below, world, pos.setPos(x, yBelow, z))) {
                    float hardnessHere = state.getBlockHardness(world, pos.setPos(x, y, z));
                    if (hardnessHere >= 0.0F && hardnessHere <= stonebrickRes) {
                        for (int i = 0; i <= solidDepth; i++) {
                            int yy = y + i;
                            if (yy >= 256) break;
                            final int sub = yy >>> 4;
                            ExtendedBlockStorage ss = ebs[sub];
                            final IBlockState sAt = ss == Chunk.NULL_BLOCK_STORAGE || ss.isEmpty() ? Blocks.AIR.getDefaultState() : ss.get(lx,
                                    yy & 15, lz);
                            if (sAt.getBlock().isAir(sAt, world, pos.setPos(x, yy, z))) continue;
                            float h = sAt.getBlockHardness(world, pos);
                            if (h >= 0.0F && h <= stonebrickRes) {
                                long key = Library.blockPosToLong(x, yy, z);
                                spawnFalling.putIfAbsent(key, sAt);
                            }
                        }
                    }
                }
            }

            if (!transformed && state.isNormalCube()) solidDepth++;
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(SCALE, 1);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        setScale(tag.getInteger("scale"));

        LongArrayList in = new LongArrayList();
        LongArrayList out = new LongArrayList();

        addAllFromPairs(in, tag.getIntArray("chunks"));
        addAllFromPairs(out, tag.getIntArray("outerChunks"));

        this.chunksToProcess = in;
        this.outerChunksToProcess = out;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        tag.setInteger("scale", getScale());
        tag.setIntArray("chunks", toPairsArray(chunksToProcess));
        tag.setIntArray("outerChunks", toPairsArray(outerChunksToProcess));
    }

    @Override
    public void setDead() {
        try {
            U.putIntRelease(this, OFF_FINISHED, 1);
            if (pool != null) pool.shutdownNow();
            if (U.getAndSetInt(this, OFF_MAP_ACQUIRED, 0) != 0) {
                ChunkUtil.releaseMirrorMap((WorldServer) world);
            }
        } finally {
            super.setDead();
        }
    }

    public int getScale() {
        Integer scale = this.dataManager.get(SCALE);
        return scale <= 0 ? 1 : scale;
    }

    public void setScale(int i) {
        this.dataManager.set(SCALE, i);
    }

    public void setScale(int i, int ignored) {
        this.dataManager.set(SCALE, i);
    }

    public void noBiomeChange() {
        this.biomeChange = false;
    }

    private void gatherChunks() {
        final int radius = getScale();
        int angleSteps = 20 * radius / 32;
        if (angleSteps < MIN_ANGLE_STEPS) angleSteps = MIN_ANGLE_STEPS;

        final int steps = angleSteps + 1;
        final LongOpenHashSet outer = new LongOpenHashSet(steps);
        final int radialSteps = radius / SPOKE_STEP_BLOCKS + 1;
        final int candidateUpper = radialSteps * steps;
        final double rc = (radius + 16.0) / 16.0;
        final int diskUpper = (int) Math.ceil(Math.PI * rc * rc + 2.0 * Math.PI * rc + 32.0);
        final int innerExpected = Math.min(candidateUpper, diskUpper);
        final LongOpenHashSet inner = new LongOpenHashSet(innerExpected);

        final double px = this.posX;
        final double pz = this.posZ;

        final double[] cos = new double[steps];
        final double[] sin = new double[steps];
        for (int step = 0; step <= angleSteps; step++) {
            final double theta = step * (2.0 * Math.PI) / angleSteps;
            cos[step] = Math.cos(theta);
            sin[step] = Math.sin(theta);
        }

        for (int step = 0; step <= angleSteps; step++) {
            final double dx = radius * cos[step];
            final double dz = -radius * sin[step];
            int cx = ((int) Math.floor(px + dx)) >> 4;
            int cz = ((int) Math.floor(pz + dz)) >> 4;
            outer.add(ChunkPos.asLong(cx, cz));
        }

        for (int d = 0; d <= radius; d += SPOKE_STEP_BLOCKS) {
            for (int step = 0; step <= angleSteps; step++) {
                final double dx = d * cos[step];
                final double dz = -d * sin[step];
                int cx = ((int) Math.floor(px + dx)) >> 4;
                int cz = ((int) Math.floor(pz + dz)) >> 4;
                long packed = ChunkPos.asLong(cx, cz);
                if (!outer.contains(packed)) inner.add(packed);
            }
        }
        LongArrayList in = new LongArrayList(inner.size());
        for (LongIterator it = inner.iterator(); it.hasNext(); ) in.add(it.nextLong());
        LongArrayList out = new LongArrayList(outer.size());
        for (LongIterator it = outer.iterator(); it.hasNext(); ) out.add(it.nextLong());
        this.chunksToProcess = in;
        this.outerChunksToProcess = out;
    }

    private record SubUpdate(int subY, Int2ObjectOpenHashMap<IBlockState> toUpdate, ExtendedBlockStorage expected, ExtendedBlockStorage prepared,
                             Long2ObjectOpenHashMap<IBlockState> oldStates) {
    }

    private static final class WorkerScratch {
        final Long2ObjectOpenHashMap<IBlockState> updates = new Long2ObjectOpenHashMap<>(1024);
        final Long2IntOpenHashMap biomeChanges = new Long2IntOpenHashMap(256);
        final Long2ObjectOpenHashMap<IBlockState> spawnFalling = new Long2ObjectOpenHashMap<>(512);
        @SuppressWarnings("unchecked")
        final Int2ObjectOpenHashMap<IBlockState>[] bucketBySub = new Int2ObjectOpenHashMap[16];

        Int2ObjectOpenHashMap<IBlockState> bucketBySub(int subY) {
            Int2ObjectOpenHashMap<IBlockState> m = bucketBySub[subY];
            if (m == null) bucketBySub[subY] = m = new Int2ObjectOpenHashMap<>(256);
            return m;
        }

        void clearWorkerPhase() {
            updates.clear();
            biomeChanges.clear();
            spawnFalling.clear();
            for (int i = 0; i < 16; i++) {
                Int2ObjectOpenHashMap<IBlockState> b = bucketBySub[i];
                if (b != null) b.clear();
            }
        }
    }

    private static final class MainScratch {
        final Long2ObjectOpenHashMap<IBlockState> oldMerged = new Long2ObjectOpenHashMap<>(1024);
        final Long2ObjectOpenHashMap<IBlockState> oldSub = new Long2ObjectOpenHashMap<>(256);
    }
}
