package com.hbm.entity.effect;

import com.github.bsideup.jabel.Desugar;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.BombConfig;
import com.hbm.config.CompatibilityConfig;
import com.hbm.config.FalloutConfigJSON;
import com.hbm.config.FalloutConfigJSON.FalloutEntry;
import com.hbm.config.WorldConfig;
import com.hbm.entity.logic.EntityExplosionChunkloading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.Library;
import com.hbm.lib.maps.NonBlockingHashMapLong;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.jctools.queues.atomic.MpscLinkedAtomicQueue;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.hbm.config.BombConfig.safeCommit;

@AutoRegister(name = "entity_fallout_rain", trackingRange = 1000)
public class EntityFalloutRain extends EntityExplosionChunkloading {

    private static final DataParameter<Integer> SCALE = EntityDataManager.createKey(EntityFalloutRain.class, DataSerializers.VARINT);

    private static final int MAX_SOLID_DEPTH = 3;
    private static final int MIN_ANGLE_STEPS = 18;
    private static final int SPOKE_STEP_BLOCKS = 8;

    private static final ThreadLocal<MutableBlockPos> TL_POS = ThreadLocal.withInitial(MutableBlockPos::new);
    private final ThreadLocal<Random> TL_RAND;
    private final LongArrayList chunksToProcess = new LongArrayList();
    private final LongArrayList outerChunksToProcess = new LongArrayList();
    private final Queue<Long> qInner = new ConcurrentLinkedQueue<>();
    private final Queue<Long> qOuter = new ConcurrentLinkedQueue<>();
    private final Queue<Long> chunkLoadQueue = new MpscLinkedAtomicQueue<>();
    private final NonBlockingHashMapLong<Boolean> waitingRoom = new NonBlockingHashMapLong<>(64); // cpLong -> clampToRadius
    private final Long2IntOpenHashMap sectionMaskByChunk = new Long2IntOpenHashMap();
    private final AtomicInteger pendingChunks = new AtomicInteger(0);
    private final AtomicInteger pendingMainThreadNotifies = new AtomicInteger(0);

    private final AtomicBoolean finished = new AtomicBoolean(false);
    private final AtomicBoolean mapAcquired = new AtomicBoolean(false);
    public UUID detonator;
    private ForkJoinPool pool;
    private int tickDelay = BombConfig.falloutDelay;

    public EntityFalloutRain(World worldIn) {
        super(worldIn);
        this.setSize(4.0F, 20.0F);
        this.ignoreFrustumCheck = true;
        this.isImmuneToFire = true;
        TL_RAND = ThreadLocal.withInitial(() -> new Random(worldIn.getSeed()));
    }

    public EntityFalloutRain(World worldIn, int ignored) {
        this(worldIn);
    }

    public static Biome getBiomeChange(double distPercent, int scale, Biome original) {
        if (!WorldConfig.enableCraterBiomes) return null;

        if (scale >= 150 && distPercent < 15) {
            return BiomeGenCraterBase.craterInnerBiome;
        }
        if (scale >= 100 && distPercent < 55 && original != BiomeGenCraterBase.craterInnerBiome) {
            return BiomeGenCraterBase.craterBiome;
        }
        if (scale >= 25 && original != BiomeGenCraterBase.craterInnerBiome && original != BiomeGenCraterBase.craterBiome) {
            return BiomeGenCraterBase.craterOuterBiome;
        }
        return null;
    }

    private static void addAllFromPairs(LongList out, int[] data) {
        if (data == null || data.length == 0) return;
        for (int i = 0; i + 1 < data.length; i += 2) out.add(ChunkPos.asLong(data[i], data[i + 1]));
    }

    private static int[] toPairsArray(LongList coords) {
        int[] data = new int[coords.size() * 2];
        int i = 0;
        LongIterator it = coords.iterator();
        while (it.hasNext()) {
            long packed = it.nextLong();
            data[i++] = ChunkUtil.getChunkPosX(packed);
            data[i++] = ChunkUtil.getChunkPosZ(packed);
        }
        return data;
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote) {
            if (!CompatibilityConfig.isWarDim(world)) {
                this.setDead();
            } else {
                if (firstUpdate) {
                    // Initialize queues and workers lazily on first tick server-side
                    if (chunksToProcess.isEmpty() && outerChunksToProcess.isEmpty()) {
                        gatherChunks();
                    }
                    startWorkersIfNeeded();
                }

                // Keep chunk loads on the server thread and resume queued work
                loadMissingChunks(BombConfig.mk5);

                // Keep a small tick delay to avoid hogging
                if (tickDelay > 0) tickDelay--;
                else tickDelay = BombConfig.falloutDelay;
            }
        }
        super.onUpdate();
    }

    private void startWorkersIfNeeded() {
        if (finished.get()) return;

        for (int i = 0; i < chunksToProcess.size(); i++) qInner.add(chunksToProcess.getLong(i));
        for (int i = 0; i < outerChunksToProcess.size(); i++) qOuter.add(outerChunksToProcess.getLong(i));
        pendingChunks.set(chunksToProcess.size() + outerChunksToProcess.size());

        if (pendingChunks.get() == 0) {
            finished.set(true);
            clearChunkLoader();
            setDead();
            return;
        }

        ChunkUtil.acquireMirrorMap((WorldServer) world);
        mapAcquired.set(true);

        int processors = Runtime.getRuntime().availableProcessors();
        int workers = BombConfig.maxThreads <= 0 ? Math.max(1, processors + BombConfig.maxThreads) : Math.min(BombConfig.maxThreads, processors);
        pool = new ForkJoinPool(Math.max(1, workers));
        for (int i = 0; i < workers; i++) {
            pool.submit(new WorkerTask());
        }
    }

    private void loadMissingChunks(int timeBudgetMs) {
        final long deadline = System.nanoTime() + timeBudgetMs * 1_000_000L;
        while (System.nanoTime() < deadline) {
            Long cpBoxed = chunkLoadQueue.poll();
            if (cpBoxed == null) break;
            long ck = cpBoxed;
            int cx = ChunkUtil.getChunkPosX(ck);
            int cz = ChunkUtil.getChunkPosZ(ck);
            world.getChunk(cx, cz);
            Boolean clamp = waitingRoom.remove(ck);
            if (clamp != null) {
                if (clamp) qOuter.add(ck);
                else qInner.add(ck);
            }
        }
    }

    private void processChunkOffThread(long cpLong, int scale, boolean clampToRadius) {
        if (finished.get()) return;

        ExtendedBlockStorage[] ebs = ChunkUtil.getLoadedEBS((WorldServer) world, cpLong);
        if (ebs == null) {
            if (waitingRoom.putIfAbsent(cpLong, Boolean.valueOf(clampToRadius)) == null) chunkLoadQueue.add(cpLong);
            return;
        }

        final int chunkX = ChunkUtil.getChunkPosX(cpLong);
        final int chunkZ = ChunkUtil.getChunkPosZ(cpLong);
        final int minX = (chunkX << 4);
        final int minZ = (chunkZ << 4);

        final Long2ObjectOpenHashMap<IBlockState> updates = new Long2ObjectOpenHashMap<>();
        final Long2IntOpenHashMap biomeChanges = new Long2IntOpenHashMap();
        final Long2ObjectOpenHashMap<IBlockState> spawnFalling = new Long2ObjectOpenHashMap<>();
        final Random rand = TL_RAND.get();
        final double cx = this.posX, cz = this.posZ;

        for (int lx = 0; lx < 16; lx++) {
            int x = minX + lx;
            for (int lz = 0; lz < 16; lz++) {
                int z = minZ + lz;
                final double distance = Math.hypot(x - cx, z - cz);
                if (clampToRadius && distance > (double) scale) continue;
                final double percent = (double) scale <= 0 ? 100.0 : (distance * 100.0 / (double) scale);
                Biome target = getBiomeChange(percent, scale, world.getBiome(TL_POS.get().setPos(x, 0, z)));
                if (target != null) biomeChanges.put(ChunkPos.asLong(x, z), Biome.getIdForBiome(target));
                stompColumnToUpdates(ebs, x, z, percent, updates, spawnFalling, rand);
            }
        }

        if (!safeCommit) {
            Chunk chunk = ChunkUtil.getLoadedChunk((WorldServer) world, cpLong);
            if (chunk == null) {
                if (waitingRoom.putIfAbsent(cpLong, Boolean.valueOf(clampToRadius)) == null) chunkLoadQueue.add(cpLong);
                return;
            }

            final Long2ObjectOpenHashMap<IBlockState> changed = new Long2ObjectOpenHashMap<>();
            Chunk old;
            if (!updates.isEmpty()) {
                do {
                    old = chunk;
                    ChunkUtil.applyAndSwap(chunk, c -> updates, changed);
                    chunk = ChunkUtil.getLoadedChunk((WorldServer) world, cpLong);
                    if (chunk == null) {
                        if (waitingRoom.putIfAbsent(cpLong, Boolean.valueOf(clampToRadius)) == null) chunkLoadQueue.add(cpLong);
                        return;
                    }
                } while (old != chunk);
            }
            if (changed.isEmpty() && biomeChanges.isEmpty() && spawnFalling.isEmpty()) {
                if (pendingChunks.decrementAndGet() == 0) maybeFinish();
                return;
            }

            int mask = 0;
            ObjectIterator<Long2ObjectMap.Entry<IBlockState>> it = changed.long2ObjectEntrySet().fastIterator();
            while (it.hasNext()) {
                Long2ObjectMap.Entry<IBlockState> e = it.next();
                int y = Library.getBlockPosY(e.getLongKey());
                mask |= 1 << (y >>> 4);
            }

            notifyMainThread(cpLong, changed, mask, biomeChanges, spawnFalling);
            if (pendingChunks.decrementAndGet() == 0) maybeFinish();
            return;
        }

        if (updates.isEmpty() && biomeChanges.isEmpty() && spawnFalling.isEmpty()) {
            if (pendingChunks.decrementAndGet() == 0) maybeFinish();
            return;
        }

        final List<SubUpdate> tasks = new ArrayList<>(8);
        final boolean hasSky = world.provider.hasSkyLight();
        //noinspection unchecked
        final Int2ObjectOpenHashMap<IBlockState>[] bySub = new Int2ObjectOpenHashMap[16];
        for (int i = 0; i < 16; i++) bySub[i] = new Int2ObjectOpenHashMap<>();

        ObjectIterator<Long2ObjectMap.Entry<IBlockState>> itUp = updates.long2ObjectEntrySet().fastIterator();
        while (itUp.hasNext()) {
            Long2ObjectMap.Entry<IBlockState> e = itUp.next();
            final long p = e.getLongKey();
            final int x = Library.getBlockPosX(p);
            final int y = Library.getBlockPosY(p);
            final int z = Library.getBlockPosZ(p);
            if ((x >> 4) != chunkX || (z >> 4) != chunkZ || y < 0 || y >= 256) continue;
            bySub[y >> 4].put(Library.packLocal(x & 15, y & 15, z & 15), e.getValue());
        }

        for (int subY = 0; subY < 16; subY++) {
            final Int2ObjectOpenHashMap<IBlockState> bucket = bySub[subY];
            if (bucket == null || bucket.isEmpty()) continue;

            final ExtendedBlockStorage expected = ebs[subY];
            final Long2ObjectOpenHashMap<IBlockState> oldSub = new Long2ObjectOpenHashMap<>();
            Optional<ExtendedBlockStorage> mod = ChunkUtil.copyAndModify(chunkX, chunkZ, subY, hasSky, expected, bucket, oldSub);
            //noinspection OptionalAssignedToNull
            if (mod == null) continue; // nothing changed
            tasks.add(new SubUpdate(subY, bucket, expected, mod.orElse(null), oldSub));
        }

        if (tasks.isEmpty() && biomeChanges.isEmpty() && spawnFalling.isEmpty()) {
            if (pendingChunks.decrementAndGet() == 0) maybeFinish();
            return;
        }

        pendingMainThreadNotifies.incrementAndGet();
        ((WorldServer) world).addScheduledTask(() -> {
            try {
                ApplyOutcome out = applyPreparedOnMain(chunkX, chunkZ, tasks);
                doNotifyOnMain(cpLong, out.oldStates, out.mask, biomeChanges, spawnFalling);
            } finally {
                if (pendingMainThreadNotifies.decrementAndGet() == 0) maybeFinish();
            }
        });

        if (pendingChunks.decrementAndGet() == 0) maybeFinish();
    }

    private ApplyOutcome applyPreparedOnMain(int chunkX, int chunkZ, List<SubUpdate> tasks) {
        final WorldServer ws = (WorldServer) world;
        final boolean hasSky = ws.provider.hasSkyLight();
        Chunk chunk = world.getChunk(chunkX, chunkZ);
        ExtendedBlockStorage[] storages = chunk.getBlockStorageArray();

        int mask = 0;
        final Long2ObjectOpenHashMap<IBlockState> oldStatesMerged = new Long2ObjectOpenHashMap<>();

        for (int i = 0, n = tasks.size(); i < n; i++) {
            SubUpdate t = tasks.get(i);
            final int subY = t.subY;
            ExtendedBlockStorage cur = storages[subY];

            if (cur == t.expected) {
                if (cur != t.prepared) { // will change
                    storages[subY] = t.prepared;
                    mask |= 1 << subY;
                    if (!t.oldStates.isEmpty()) oldStatesMerged.putAll(t.oldStates);
                }
            } else {
                // pointer drift → rebuild against latest
                final Long2ObjectOpenHashMap<IBlockState> oldSub = new Long2ObjectOpenHashMap<>();
                Optional<ExtendedBlockStorage> rebuilt = ChunkUtil.copyAndModify(chunkX, chunkZ, subY, hasSky, cur, t.toUpdate, oldSub);
                //noinspection OptionalAssignedToNull
                if (rebuilt != null) {
                    ExtendedBlockStorage upd = rebuilt.orElse(null);
                    if (cur != upd) {
                        storages[subY] = upd;
                        mask |= 1 << subY;
                        if (!oldSub.isEmpty()) oldStatesMerged.putAll(oldSub);
                    }
                }
            }
        }

        chunk.markDirty();
        return new ApplyOutcome(mask, oldStatesMerged);
    }

    private void notifyMainThread(long cpLong, Long2ObjectOpenHashMap<IBlockState> oldStates, int mask, Long2IntOpenHashMap biomeChanges, Long2ObjectOpenHashMap<IBlockState> spawnFalling) {
        pendingMainThreadNotifies.incrementAndGet();
        ((WorldServer) world).addScheduledTask(() -> {
            try {
                doNotifyOnMain(cpLong, oldStates, mask, biomeChanges, spawnFalling);
            } finally {
                if (pendingMainThreadNotifies.decrementAndGet() == 0) maybeFinish();
            }
        });
    }

    private void doNotifyOnMain(long cpLong, Long2ObjectOpenHashMap<IBlockState> oldStates, int mask, Long2IntOpenHashMap biomeChanges, Long2ObjectOpenHashMap<IBlockState> spawnFalling) {
        int cx = ChunkUtil.getChunkPosX(cpLong);
        int cz = ChunkUtil.getChunkPosZ(cpLong);
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

                // This check can't be done in the workers as it reads the world instance, so instead we restore the state
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
                int x = ChunkUtil.getChunkPosX(packed);
                int z = ChunkUtil.getChunkPosZ(packed);
                Biome target = Biome.getBiome(be.getIntValue());
                if (target != null) WorldUtil.setBiome(world, x, z, target);
            }
            WorldUtil.syncBiomeChange(world, cx, cz);
        }

        if (spawnFalling != null && !spawnFalling.isEmpty()) {
            ObjectIterator<Long2ObjectMap.Entry<IBlockState>> iterator = spawnFalling.long2ObjectEntrySet().fastIterator();
            while (iterator.hasNext()) {
                Map.Entry<Long, IBlockState> entry = iterator.next();
                Long pos = entry.getKey();
                IBlockState state = entry.getValue();
                EntityFallingBlock falling = new EntityFallingBlock(world, Library.getBlockPosX(pos) + 0.5, Library.getBlockPosY(pos) + 0.5, Library.getBlockPosZ(pos) + 0.5, state);
                falling.shouldDropItem = false;
                world.spawnEntity(falling);
            }
            spawnFalling.clear();
        }

        loadedChunk.markDirty();
    }

    private void maybeFinish() {
        if (finished.get()) return;
        if (pendingChunks.get() == 0 && waitingRoom.isEmpty() && qInner.isEmpty() && qOuter.isEmpty() && pendingMainThreadNotifies.get() == 0) {
            ((WorldServer) world).addScheduledTask(this::secondPassAndFinish);
        }
    }

    private void secondPassAndFinish() {
        for (Long2IntMap.Entry e : sectionMaskByChunk.long2IntEntrySet()) {
            long cp = e.getLongKey();
            int changedMask = e.getIntValue();
            if (changedMask == 0) continue;
            Chunk chunk = ChunkUtil.getLoadedChunk((WorldServer) world, cp);
            if (chunk == null) continue;
            chunk.generateSkylightMap();
            chunk.resetRelightChecks();
        }
        sectionMaskByChunk.clear();

        finished.set(true);
        if (pool != null) {
            pool.shutdown();
            try {
                pool.awaitTermination(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignored) {
            }
        }
        if (mapAcquired.getAndSet(false)) ChunkUtil.releaseMirrorMap((WorldServer) world);
        clearChunkLoader();
        setDead();
    }

    private void stompColumnToUpdates(ExtendedBlockStorage[] ebs, int x, int z, double distPercent, Long2ObjectOpenHashMap<IBlockState> updates, Long2ObjectOpenHashMap<IBlockState> spawnFalling, Random rand) {
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

            //TODO: implement volcano_rad_core
//            if (block == ModBlocks.volcano_core) {
//                updates.put(Library.blockPosToLong(x, y, z), ModBlocks.volcano_rad_core.getDefaultState());
//                continue;
//            }

            // Place fallout just above topmost solid
            IBlockState stateUp = null;
            final int upY = y + 1;
            if (solidDepth == 0 && upY < 256) {
                final int upSub = upY >>> 4;
                ExtendedBlockStorage su = ebs[upSub];
                stateUp = su == Chunk.NULL_BLOCK_STORAGE || su.isEmpty() ? Blocks.AIR.getDefaultState() : su.get(lx, upY & 15, lz);
                pos.setPos(x, upY, z);
                boolean airOrReplaceable = stateUp.getBlock().isAir(stateUp, world, pos) || stateUp.getBlock().isReplaceable(world, pos) && !stateUp.getMaterial().isLiquid();
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
                final IBlockState below = (sb == Chunk.NULL_BLOCK_STORAGE || sb.isEmpty()) ? Blocks.AIR.getDefaultState() : sb.get(lx, yBelow & 15, lz);
                if (below.getBlock().isAir(below, world, pos.setPos(x, yBelow, z))) {
                    float hardnessHere = state.getBlockHardness(world, pos.setPos(x, y, z));
                    if (hardnessHere >= 0.0F && hardnessHere <= stonebrickRes) {
                        for (int i = 0; i <= solidDepth; i++) {
                            int yy = y + i;
                            if (yy >= 256) break;
                            final int sub = yy >>> 4;
                            ExtendedBlockStorage ss = ebs[sub];
                            final IBlockState sAt = ss == Chunk.NULL_BLOCK_STORAGE || ss.isEmpty() ? Blocks.AIR.getDefaultState() : ss.get(lx, yy & 15, lz);
                            if (sAt.getBlock().isAir(sAt, world, pos.setPos(x, yy, z)))
                                continue; // nothing solid to drop at this offset
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
        chunksToProcess.clear();
        outerChunksToProcess.clear();
        addAllFromPairs(chunksToProcess, tag.getIntArray("chunks"));
        addAllFromPairs(outerChunksToProcess, tag.getIntArray("outerChunks"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        tag.setInteger("scale", getScale());
        tag.setIntArray("chunks", toPairsArray(chunksToProcess));
        tag.setIntArray("outerChunks", toPairsArray(outerChunksToProcess));
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

    private void gatherChunks() {
        final LongLinkedOpenHashSet inner = new LongLinkedOpenHashSet();
        final LongLinkedOpenHashSet outer = new LongLinkedOpenHashSet();

        final int radius = getScale();
        int angleSteps = 20 * radius / 32;
        if (angleSteps < MIN_ANGLE_STEPS) angleSteps = MIN_ANGLE_STEPS;

        for (int step = 0; step <= angleSteps; step++) {
            final double theta = step * (2.0 * Math.PI) / angleSteps;
            Vec3d vec = new Vec3d(radius, 0, 0).rotateYaw((float) theta);
            int cx = ((int) Math.floor(this.posX + vec.x)) >> 4;
            int cz = ((int) Math.floor(this.posZ + vec.z)) >> 4;
            outer.add(ChunkPos.asLong(cx, cz));
        }

        for (int d = 0; d <= radius; d += SPOKE_STEP_BLOCKS) {
            for (int step = 0; step <= angleSteps; step++) {
                final double theta = step * (2.0 * Math.PI) / angleSteps;
                Vec3d vec = new Vec3d(d, 0, 0).rotateYaw((float) theta);
                int cx = ((int) Math.floor(this.posX + vec.x)) >> 4;
                int cz = ((int) Math.floor(this.posZ + vec.z)) >> 4;
                long packed = ChunkPos.asLong(cx, cz);
                if (!outer.contains(packed)) inner.add(packed);
            }
        }

        LongArrayList innerList = new LongArrayList(inner);
        LongArrayList outerList = new LongArrayList(outer);
        for (int i = 0; i < innerList.size(); i++) chunksToProcess.add(innerList.getLong(i));
        for (int i = 0; i < outerList.size(); i++) outerChunksToProcess.add(outerList.getLong(i));
    }

    @Desugar
    private record SubUpdate(int subY, Int2ObjectOpenHashMap<IBlockState> toUpdate, ExtendedBlockStorage expected,
                             ExtendedBlockStorage prepared, Long2ObjectOpenHashMap<IBlockState> oldStates) {
    }

    @Desugar
    private record ApplyOutcome(int mask, Long2ObjectOpenHashMap<IBlockState> oldStates) {
    }

    private final class WorkerTask extends RecursiveAction {
        @Override
        protected void compute() {
            while (!Thread.currentThread().isInterrupted()) {
                Long cp = qInner.poll();
                boolean clamp = false;
                if (cp == null) {
                    cp = qOuter.poll();
                    clamp = true;
                }
                if (cp == null) break;
                processChunkOffThread(cp, getScale(), clamp);
            }
        }
    }
}
