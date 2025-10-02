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
import com.hbm.lib.maps.NonBlockingHashMapLong;
import com.hbm.util.ChunkUtil;
import com.hbm.world.WorldUtil;
import com.hbm.world.biome.BiomeGenCraterBase;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.jctools.queues.atomic.MpscLinkedAtomicQueue;
import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
        for (int i = 0; i + 1 < data.length; i += 2) {
            out.add(ChunkPos.asLong(data[i], data[i + 1]));
        }
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

        // Seed queues
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
            if (waitingRoom.putIfAbsentLong(cpLong, clampToRadius) == null) {
                chunkLoadQueue.add(cpLong);
            }
            return;
        }

        final int chunkX = ChunkUtil.getChunkPosX(cpLong);
        final int chunkZ = ChunkUtil.getChunkPosZ(cpLong);
        final int minX = (chunkX << 4);
        final int minZ = (chunkZ << 4);

        final Long2ObjectOpenHashMap<IBlockState> updates = new Long2ObjectOpenHashMap<>();
        final Long2IntOpenHashMap biomeChanges = new Long2IntOpenHashMap();
        final NonBlockingHashMapLong<Entity> spawnFalling = new NonBlockingHashMapLong<>(32);

        final Random rand = TL_RAND.get();

        final double cx = this.posX;
        final double cz = this.posZ;

        for (int lx = 0; lx < 16; lx++) {
            int x = minX + lx;
            for (int lz = 0; lz < 16; lz++) {
                int z = minZ + lz;
                final double distance = Math.hypot(x - cx, z - cz);
                if (clampToRadius && distance > (double) scale) continue;
                final double percent = (double) scale <= 0 ? 100.0 : (distance * 100.0 / (double) scale);
                Biome target = getBiomeChange(percent, scale, world.getBiome(TL_POS.get().setPos(x, 0, z)));
                if (target != null) {
                    biomeChanges.put(ChunkPos.asLong(x, z), Biome.getIdForBiome(target));
                }
                stompColumnToUpdates(ebs, x, z, percent, updates, spawnFalling, rand);
            }
        }

        final Chunk chunk = ChunkUtil.getLoadedChunk((WorldServer) world, cpLong);
        if (chunk == null) {
            if (waitingRoom.putIfAbsentLong(cpLong, clampToRadius) == null) chunkLoadQueue.add(cpLong);
            return;
        }

        final Long2ObjectOpenHashMap<IBlockState> changed = new Long2ObjectOpenHashMap<>();
        if (!updates.isEmpty()) {
            ChunkUtil.applyAndSwap(chunk, c -> updates, changed);
        }
        if (changed.isEmpty() && biomeChanges.isEmpty() && spawnFalling.isEmpty()) {
            if (pendingChunks.decrementAndGet() == 0) maybeFinish();
            return;
        }

        int mask = 0;
        ObjectIterator<Long2ObjectMap.Entry<IBlockState>> it = changed.long2ObjectEntrySet().fastIterator();
        while (it.hasNext()) {
            Long2ObjectMap.Entry<IBlockState> stateEntry = it.next();
            long p = stateEntry.getLongKey();
            int y = Library.getBlockPosY(p);
            mask |= 1 << (y >>> 4);
        }

        notifyMainThread(cpLong, changed, mask, biomeChanges, spawnFalling);
        if (pendingChunks.decrementAndGet() == 0) maybeFinish();
    }

    private void notifyMainThread(long cpLong,  Long2ObjectOpenHashMap<IBlockState> changed, int mask, Long2IntOpenHashMap biomeChanges,
                                  NonBlockingHashMapLong<Entity> spawnFalling) {
        pendingMainThreadNotifies.incrementAndGet();
        ((WorldServer) world).addScheduledTask(() -> {
            try {
                Chunk loadedChunk = ChunkUtil.getLoadedChunk((WorldServer) world, cpLong);
                sectionMaskByChunk.put(cpLong, sectionMaskByChunk.get(cpLong) | mask);
                final MutableBlockPos mutableBlockPos = TL_POS.get();
                if (loadedChunk != null) {
                    ObjectIterator<Long2ObjectMap.Entry<IBlockState>> iterator = changed.long2ObjectEntrySet().fastIterator();
                    while (iterator.hasNext()) {
                        Long2ObjectMap.Entry<IBlockState> stateEntry = iterator.next();
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

                if (!biomeChanges.isEmpty()) {
                    int cx = ChunkUtil.getChunkPosX(cpLong);
                    int cz = ChunkUtil.getChunkPosZ(cpLong);
                    ObjectIterator<Long2IntMap.Entry> iterator = biomeChanges.long2IntEntrySet().fastIterator();
                    while (iterator.hasNext()) {
                        Long2IntMap.Entry be = iterator.next();
                        long packed = be.getLongKey();
                        int x = ChunkUtil.getChunkPosX(packed);
                        int z = ChunkUtil.getChunkPosZ(packed);
                        Biome target = Biome.getBiome(be.getIntValue());
                        if (target != null) {
                            WorldUtil.setBiome(world, x, z, target);
                        }
                    }
                    WorldUtil.syncBiomeChange(world, cx, cz);
                }

                if (!spawnFalling.isEmpty()) {
                    for (Entity entity : spawnFalling.values()) {
                        if (entity == null) continue;
                        world.spawnEntity(entity);
                    }
                    spawnFalling.clear();
                }

                if (loadedChunk != null) loadedChunk.markDirty();
            } finally {
                if (pendingMainThreadNotifies.decrementAndGet() == 0) maybeFinish();
            }
        });
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
//            int cx = ChunkUtil.getChunkPosX(cp);
//            int cz = ChunkUtil.getChunkPosZ(cp);
            int changedMask = e.getIntValue();
            if (changedMask == 0) continue;
            Chunk chunk = ChunkUtil.getLoadedChunk((WorldServer) world, cp);
            if (chunk == null) continue;
//
//            ExtendedBlockStorage[] storages = chunk.getBlockStorageArray();
//            boolean groundUp = false;
//            for (int subY = 0; subY < storages.length; subY++) {
//                if (((changedMask >>> subY) & 1) == 0) continue;
//                ExtendedBlockStorage s = storages[subY];
//                if (s == Chunk.NULL_BLOCK_STORAGE) groundUp = true;
//                else if (s.isEmpty()) {
//                    storages[subY] = Chunk.NULL_BLOCK_STORAGE;
//                    groundUp = true;
//                }
//            }
            chunk.generateSkylightMap();
            chunk.resetRelightChecks();

//            PlayerChunkMapEntry entry = ((WorldServer) world).getPlayerChunkMap().getEntry(cx, cz);
//            if (entry != null) entry.sendPacket(new SPacketChunkData(chunk, groundUp ? 0xFFFF : changedMask));
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

    private void stompColumnToUpdates(ExtendedBlockStorage[] ebs, int x, int z, double distPercent, Long2ObjectOpenHashMap<IBlockState> updates,
                                      NonBlockingHashMapLong<Entity> spawnFalling, Random rand) {
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
            if (state.getMaterial() == Material.AIR || block == ModBlocks.fallout) continue;

            // Place fallout just above topmost solid
            final int upY = y + 1;
            if (solidDepth == 0 && upY < 256) {
                final int upSub = upY >>> 4;
                ExtendedBlockStorage su = ebs[upSub];
                final IBlockState stateUp = su == Chunk.NULL_BLOCK_STORAGE || su.isEmpty() ? Blocks.AIR.getDefaultState() : su.get(lx, upY & 15, lz);
                if (stateUp.getBlock() == Blocks.AIR) {
                    double d = distPercent / 100.0;
                    double chance = 0.1 - Math.pow(d - 0.7, 2.0);
                    long seed = (((long) x) * 341873128712L) ^ (((long) z) * 132897987541L) ^ (((long) upY) * 31L) ^ this.getEntityId();
                    rand.setSeed(seed);
                    if (chance >= rand.nextDouble()) {
                        updates.put(Library.blockPosToLong(x, upY, z), ModBlocks.fallout.getDefaultState());
                    }
                }
            }

            if (distPercent < 65 && block.isFlammable(world, pos.setPos(x, y, z), EnumFacing.UP)) {
                if (y + 1 < 256) {
                    final int upSub = (y + 1) >>> 4;
                    ExtendedBlockStorage su = ebs[upSub];
                    final IBlockState stateUp =
                            (su == Chunk.NULL_BLOCK_STORAGE || su.isEmpty()) ? Blocks.AIR.getDefaultState() : su.get(lx, (y + 1) & 15, lz);
                    if (stateUp.getBlock() == Blocks.AIR) {
                        if ((rand.nextInt(5)) == 0) {
                            updates.put(Library.blockPosToLong(x, y + 1, z), Blocks.FIRE.getDefaultState());
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
                final int belowSub = (y - 1) >>> 4;
                ExtendedBlockStorage sb = ebs[belowSub];
                final IBlockState below =
                        (sb == Chunk.NULL_BLOCK_STORAGE || sb.isEmpty()) ? Blocks.AIR.getDefaultState() : sb.get(lx, (y - 1) & 15, lz);
                if (below.getBlock() == Blocks.AIR) {
                    float hardnessHere = state.getBlockHardness(world, pos.setPos(x, y, z));
                    if (hardnessHere >= 0.0F && hardnessHere <= stonebrickRes) {
                        for (int i = 0; i <= solidDepth; i++) {
                            int yy = y + i;
                            if (yy >= 256) break;
                            final int sub = yy >>> 4;
                            ExtendedBlockStorage ss = ebs[sub];
                            final IBlockState sAt =
                                    (ss == Chunk.NULL_BLOCK_STORAGE || ss.isEmpty()) ? Blocks.AIR.getDefaultState() : ss.get(lx, yy & 15, lz);
                            if (sAt.getMaterial() == Material.AIR) continue; // nothing solid to drop at this offset
                            float h = sAt.getBlockHardness(world, pos.setPos(x, yy, z));
                            if (h >= 0.0F && h <= stonebrickRes) {
                                EntityFallingBlock falling = new EntityFallingBlock(world, x + 0.5D, yy + 0.5D, z + 0.5D, sAt);
                                falling.shouldDropItem = false;
                                long key = Library.blockPosToLong(x, yy, z);
                                spawnFalling.putIfAbsent(key, falling);
                            }
                        }
                    }
                }
            }

            if (!transformed && state.isNormalCube()) {
                solidDepth++;
            }
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
        for (int i = innerList.size() - 1; i >= 0; i--) chunksToProcess.add(innerList.getLong(i));
        for (int i = outerList.size() - 1; i >= 0; i--) outerChunksToProcess.add(outerList.getLong(i));
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
