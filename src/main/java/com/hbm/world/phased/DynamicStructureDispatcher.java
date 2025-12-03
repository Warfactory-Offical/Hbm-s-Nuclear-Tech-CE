package com.hbm.world.phased;

import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.util.ChunkUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Random;

/**
 * Minimal dispatcher for post-generate-only structures that would otherwise overwhelm
 * {@link PhasedStructureGenerator} with marker tasks.
 */
@SuppressWarnings("ForLoopReplaceableByForEach")//iterator allocation
public class DynamicStructureDispatcher {

    public static final DynamicStructureDispatcher INSTANCE = new DynamicStructureDispatcher();

    private static final LongArrayList ORIGIN_ONLY = LongArrayList.wrap(new long[]{ChunkPos.asLong(0, 0)});
    private static final Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<AbstractPhasedStructure.BlockInfo>> EMPTY_LAYOUT = new Long2ObjectOpenHashMap<>(0);

    private final Int2ObjectOpenHashMap<WorldState> states = new Int2ObjectOpenHashMap<>();

    private DynamicStructureDispatcher() {
    }

    public void schedule(@NotNull World world, long originSerialized, @NotNull AbstractPhasedStructure structure, long layoutSeed) {
        if (world.isRemote) return;
        WorldServer server = (WorldServer) world;
        PhasedStructureGenerator.PendingValidationStructure pending = new PhasedStructureGenerator.PendingValidationStructure(originSerialized, structure, EMPTY_LAYOUT, server.getSeed(), layoutSeed);
        PhasedStructureGenerator.ReadyToGenerateStructure ready = structure.validate(world, pending);
        if (ready == null) return;

        WorldState state = states.computeIfAbsent(server.provider.getDimension(), dim -> new WorldState());
        LongList watchedOffsets = resolveOffsets(structure, ready.finalOrigin);
        PendingDynamicStructure job = PendingDynamicStructure.obtain(structure, ready, watchedOffsets);

        int originChunkX = Library.getBlockPosX(ready.finalOrigin) >> 4;
        int originChunkZ = Library.getBlockPosZ(ready.finalOrigin) >> 4;
        long originChunkKey = ChunkPos.asLong(originChunkX, originChunkZ);

        ChunkProviderServer provider = server.getChunkProvider();
        if (job.evaluate(provider)) {
            job.run(server);
        } else {
            job.registerWaiting(state.waitingByChunk);
            addJobForOrigin(state, originChunkKey, job);
        }
    }

    public static void forceGenerate(@NotNull World world, @NotNull Random rand, long originSerialized, @NotNull AbstractPhasedStructure structure) {
        if (world.isRemote) return;
        structure.postGenerate(world, rand, originSerialized);
    }

    private static LongList resolveOffsets(AbstractPhasedStructure structure, long origin) {
        LongArrayList offsets = structure.getWatchedChunkOffsets(origin);
        if (offsets == null || offsets.isEmpty()) {
            return ORIGIN_ONLY;
        }
        return offsets;
    }

    private static void addJobForOrigin(@NotNull WorldState state, long originChunkKey, @NotNull PendingDynamicStructure job) {
        ArrayList<PendingDynamicStructure> list = state.jobsByOriginChunk.get(originChunkKey);
        if (list == null) {
            list = new ArrayList<>(4);
            state.jobsByOriginChunk.put(originChunkKey, list);
        }
        list.add(job);
    }

    private void onJobFinished(@NotNull WorldServer world, @NotNull PendingDynamicStructure job) {
        WorldState state = states.get(world.provider.getDimension());
        if (state == null) return;

        int originChunkX = Library.getBlockPosX(job.origin) >> 4;
        int originChunkZ = Library.getBlockPosZ(job.origin) >> 4;
        long originChunkKey = ChunkPos.asLong(originChunkX, originChunkZ);

        ArrayList<PendingDynamicStructure> list = state.jobsByOriginChunk.get(originChunkKey);
        if (list != null) {
            list.remove(job);
            if (list.isEmpty()) {
                state.jobsByOriginChunk.remove(originChunkKey);
            }
        }
    }

    @SubscribeEvent
    public void onChunkPopulated(PopulateChunkEvent.Post event) {
        World world = event.getWorld();
        if (world.isRemote) return;
        handleChunkAvailable((WorldServer) world, ChunkPos.asLong(event.getChunkX(), event.getChunkZ()));
    }

    @SubscribeEvent
    public void onChunkLoaded(ChunkEvent.Load event) {
        World world = event.getWorld();
        if (world.isRemote) return;
        Chunk chunk = event.getChunk();
        if (!chunk.isTerrainPopulated()) return;
        handleChunkAvailable((WorldServer) world, ChunkPos.asLong(chunk.x, chunk.z));
    }

    @SubscribeEvent
    public void onChunkDataSave(ChunkDataEvent.Save event) {
        World world = event.getWorld();
        if (world.isRemote) return;

        int dim = world.provider.getDimension();
        WorldState state = states.get(dim);
        if (state == null) return;

        Chunk chunk = event.getChunk();
        long chunkKey = ChunkPos.asLong(chunk.x, chunk.z);

        ArrayList<PendingDynamicStructure> jobs = state.jobsByOriginChunk.get(chunkKey);
        NBTTagCompound data = event.getData();

        if (jobs == null || jobs.isEmpty()) {
            data.removeTag("HbmDynamicJobs");
            return;
        }

        NBTTagList list = new NBTTagList();
        for (int i = 0; i < jobs.size(); i++) {
            PendingDynamicStructure job = jobs.get(i);
            if (job.structure == null || job.watchedOffsets == null) continue;

            short typeId = PhasedStructureRegistry.getId(job.structure);
            Class<? extends IPhasedStructure> type = PhasedStructureRegistry.byId(typeId);
            if (type == null || type != job.structure.getClass()) {
                MainRegistry.logger.warn("Skipping dynamic job for unregistered structure type {} at origin {}", job.structure.getClass()
                                                                                                                              .getName(), job.origin);
                continue;
            }

            NBTTagCompound structTag = new NBTTagCompound();
            job.structure.writeToNBT(structTag);

            NBTTagCompound entry = new NBTTagCompound();
            entry.setShort("Type", typeId);
            entry.setLong("Origin", job.origin);
            entry.setLong("Seed", job.worldSeed);
            entry.setTag("Data", structTag);
            entry.setTag("Offsets", new NBTTagLongArray(job.watchedOffsets.toLongArray()));
            list.appendTag(entry);
        }

        if (!list.isEmpty()) {
            data.setTag("HbmDynamicJobs", list);
        } else {
            data.removeTag("HbmDynamicJobs");
        }
    }

    @SubscribeEvent
    public void onChunkDataLoad(ChunkDataEvent.Load event) {
        World world = event.getWorld();
        if (world.isRemote) return;
        WorldServer server = (WorldServer) world;
        NBTTagCompound data = event.getData();
        if (!data.hasKey("HbmDynamicJobs", 9)) { // 9 = TAG_LIST
            return;
        }

        int dim = server.provider.getDimension();
        WorldState state = states.computeIfAbsent(dim, d -> new WorldState());

        Chunk chunk = event.getChunk();
        long chunkKey = ChunkPos.asLong(chunk.x, chunk.z);

        NBTTagList list = data.getTagList("HbmDynamicJobs", 10); // 10 = TAG_COMPOUND
        if (list.tagCount() == 0) return;

        ChunkProviderServer provider = server.getChunkProvider();

        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            short typeId = entry.getShort("Type");

            NBTTagCompound structTag = entry.getCompoundTag("Data");
            IPhasedStructure struct;
            try {
                struct = PhasedStructureRegistry.deserialize(typeId, structTag);
            } catch (Exception ex) {
                MainRegistry.logger.warn("Failed to deserialize phased structure {} with nbt {}", typeId, structTag);
                continue;
            }
            if (!(struct instanceof AbstractPhasedStructure structure)) {
                MainRegistry.logger.warn("Skipping dynamic job with non-AbstractPhasedStructure type id {} at origin {}", typeId, entry.getLong("Origin"));
                continue;
            }

            long origin = entry.getLong("Origin");
            long seed = entry.getLong("Seed");
            long[] offsetsArr = ((NBTTagLongArray) entry.getTag("Offsets")).data;
            LongArrayList offsets = LongArrayList.wrap(offsetsArr);
            PendingDynamicStructure job = PendingDynamicStructure.obtain(structure, origin, seed, offsets);

            if (job.evaluate(provider)) {
                job.run(server);
            } else {
                job.registerWaiting(state.waitingByChunk);
                addJobForOrigin(state, chunkKey, job);
            }
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        World world = event.getWorld();
        if (world.isRemote) return;
        states.remove(world.provider.getDimension());
    }

    private void handleChunkAvailable(WorldServer world, long chunkKey) {
        WorldState state = states.get(world.provider.getDimension());
        if (state == null) return;

        ArrayList<PendingDynamicStructure> waiters = state.waitingByChunk.remove(chunkKey);
        if (waiters == null || waiters.isEmpty()) return;

        ChunkProviderServer provider = world.getChunkProvider();
        for (int i = waiters.size() - 1; i >= 0; i--) {
            PendingDynamicStructure job = waiters.get(i);
            job.waitingOn.remove(chunkKey);
            if (!job.isReady()) continue;
            if (!job.ensureAllWatchedChunksLoaded(provider)) {
                job.registerWaiting(state.waitingByChunk);
                continue;
            }
            job.run(world);
        }
    }

    private static class WorldState {
        final Long2ObjectOpenHashMap<ArrayList<PendingDynamicStructure>> waitingByChunk = new Long2ObjectOpenHashMap<>();
        final Long2ObjectOpenHashMap<ArrayList<PendingDynamicStructure>> jobsByOriginChunk = new Long2ObjectOpenHashMap<>();
    }

    private static class PendingDynamicStructure {
        private static final ArrayList<PendingDynamicStructure> POOL = new ArrayList<>(256);
        private static final int MAX_POOL_SIZE = 4096;

        private final LongOpenHashSet waitingOn = new LongOpenHashSet(32);
        private AbstractPhasedStructure structure;
        private long origin;
        private long worldSeed;
        private LongList watchedOffsets;

        private PendingDynamicStructure() {
        }

        boolean ensureAllWatchedChunksLoaded(ChunkProviderServer provider) {
            if (watchedOffsets == null || watchedOffsets.isEmpty()) return true;
            waitingOn.clear();
            final int originChunkX = Library.getBlockPosX(origin) >> 4;
            final int originChunkZ = Library.getBlockPosZ(origin) >> 4;
            for (int i = 0, len = watchedOffsets.size(); i < len; i++) {
                long rel = watchedOffsets.getLong(i);
                int offsetX = ChunkUtil.getChunkPosX(rel);
                int offsetZ = ChunkUtil.getChunkPosZ(rel);
                long absKey = ChunkPos.asLong(originChunkX + offsetX, originChunkZ + offsetZ);
                if (!provider.loadedChunks.containsKey(absKey)) waitingOn.add(absKey);
            }
            return waitingOn.isEmpty();
        }

        static PendingDynamicStructure obtain(AbstractPhasedStructure structure, PhasedStructureGenerator.ReadyToGenerateStructure ready,
                                              LongList watchedOffsets) {
            PendingDynamicStructure job = obtain();
            job.structure = structure;
            job.origin = ready.finalOrigin;
            job.worldSeed = ready.pending.worldSeed;
            job.watchedOffsets = watchedOffsets;
            return job;
        }

        static PendingDynamicStructure obtain(AbstractPhasedStructure structure, long origin, long worldSeed, LongList watchedOffsets) {
            PendingDynamicStructure job = obtain();
            job.structure = structure;
            job.origin = origin;
            job.worldSeed = worldSeed;
            job.watchedOffsets = watchedOffsets;
            return job;
        }

        private static PendingDynamicStructure obtain() {
            int size = POOL.size();
            PendingDynamicStructure job;
            if (size != 0) {
                job = POOL.remove(size - 1);
            } else {
                job = new PendingDynamicStructure();
            }
            job.waitingOn.clear();
            return job;
        }

        private void recycle() {
            this.structure = null;
            this.origin = 0L;
            this.watchedOffsets = null;
            this.worldSeed = 0L;
            this.waitingOn.clear();

            if (POOL.size() < MAX_POOL_SIZE) {
                POOL.add(this);
            }
        }

        /**
         * @return true if ready to run immediately, false if we need to wait for chunks to populate/load.
         */
        boolean evaluate(ChunkProviderServer provider) {
            waitingOn.clear();
            if (watchedOffsets == null || watchedOffsets.isEmpty()) return true;

            final int originChunkX = Library.getBlockPosX(origin) >> 4;
            final int originChunkZ = Library.getBlockPosZ(origin) >> 4;

            for (int i = 0; i < watchedOffsets.size(); i++) {
                long rel = watchedOffsets.getLong(i);
                int offsetX = ChunkUtil.getChunkPosX(rel);
                int offsetZ = ChunkUtil.getChunkPosZ(rel);

                long absKey = ChunkPos.asLong(originChunkX + offsetX, originChunkZ + offsetZ);
                Chunk chunk = provider.loadedChunks.get(absKey);
                if (chunk == null || !chunk.isTerrainPopulated()) {
                    waitingOn.add(absKey);
                }
            }
            return waitingOn.isEmpty();
        }

        void registerWaiting(Long2ObjectOpenHashMap<ArrayList<PendingDynamicStructure>> waitingByChunk) {
            long[] keys = waitingOn.toLongArray();
            for (long key : keys) {
                ArrayList<PendingDynamicStructure> list = waitingByChunk.get(key);
                if (list == null) {
                    list = new ArrayList<>(4);
                    waitingByChunk.put(key, list);
                }
                list.add(this);
            }
        }

        boolean isReady() {
            return waitingOn.isEmpty();
        }

        void run(WorldServer world) {
            Random rand = PhasedStructureGenerator.PendingValidationStructure.createRandom(this.worldSeed, this.origin);
            try {
                structure.postGenerate(world, rand, origin);
            } catch (Exception e) {
                MainRegistry.logger.error("Error generating dynamic structure {} at {}", structure != null ? structure.getClass()
                                                                                                                      .getSimpleName() : "null", origin, e);
            } finally {
                DynamicStructureDispatcher.INSTANCE.onJobFinished(world, this);
                recycle();
            }
        }
    }
}
