package com.hbm.world.phased;

import com.hbm.config.GeneralConfig;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.util.ChunkUtil;
import com.hbm.util.DelayedTick;
import com.hbm.world.phased.AbstractPhasedStructure.BlockInfo;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Random;

/**
 * Lightweight phased structure manager with minimal allocations and per-chunk persistence.
 */
@SuppressWarnings("ForLoopReplaceableByForEach") // avoids allocating an iterator
public class PhasedStructureGenerator implements IWorldGenerator {

    public static final PhasedStructureGenerator INSTANCE = new PhasedStructureGenerator(); // entrypoint, state is per-dimension
    private static final Int2ObjectOpenHashMap<DimensionState> STATES = new Int2ObjectOpenHashMap<>();
    private static final int MAX_TASK_POOL_SIZE = 2048;
    private static final int MAX_START_POOL_SIZE = 1024;
    private static final int MAX_TASK_LIST_POOL_SIZE = 2048;
    private static final int MAX_ADDITIONAL_CHUNK_POOL_SIZE = 512;

    private PhasedStructureGenerator() {
    }

    private static DimensionState getState(@NotNull World world) {
        int dim = world.provider.getDimension();
        return getState(dim);
    }

    private static DimensionState getState(int dim) {
        DimensionState state = STATES.get(dim);
        if (state == null) {
            state = new DimensionState(dim);
            STATES.put(dim, state);
        }
        return state;
    }

    private static void clearState(DimensionState state) {
        recycleAllComponents(state);
        recycleAllStarts(state);
        state.componentsByChunk.clear();
        state.structureMap.clear();
        state.chunkTaskPool.clear();
        state.structureStartPool.clear();
        state.chunkTaskListPool.clear();
        state.recycleQueue.clear();
        state.completedStarts.clear();
        state.additionalChunkPool.clear();
        state.world = null;
        state.currentlyProcessingChunk = Long.MIN_VALUE;
        state.processingTasks = false;
    }

    static void forceGenerateStructure(World world, Random rand, long originSerialized, IPhasedStructure structure,
                                       Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<BlockInfo>> layout) {
        int originChunkX = Library.getBlockPosX(originSerialized) >> 4;
        int originChunkZ = Library.getBlockPosZ(originSerialized) >> 4;

        ObjectIterator<Long2ObjectMap.Entry<Long2ObjectOpenHashMap<BlockInfo>>> iterator = layout.long2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Long2ObjectMap.Entry<Long2ObjectOpenHashMap<BlockInfo>> entry = iterator.next();
            long relKey = entry.getLongKey();
            Long2ObjectOpenHashMap<BlockInfo> blocksForThisChunk = entry.getValue();

            int relChunkX = ChunkUtil.getChunkPosX(relKey);
            int relChunkZ = ChunkUtil.getChunkPosZ(relKey);
            int absChunkX = originChunkX + relChunkX;
            int absChunkZ = originChunkZ + relChunkZ;

            structure.generateForChunk(world, rand, originSerialized, absChunkX, absChunkZ, blocksForThisChunk);
        }
        structure.postGenerate(world, rand, originSerialized);
    }

    private static LongArrayList translateOffsets(@NotNull DimensionState state, @NotNull long originSerialized,
                                                  @NotNull LongArrayList relativeOffsets) {
        int baseChunkX = Library.getBlockPosX(originSerialized) >> 4;
        int baseChunkZ = Library.getBlockPosZ(originSerialized) >> 4;
        LongArrayList absolute = borrowAdditionalChunkList(state);
        absolute.ensureCapacity(relativeOffsets.size());
        for (int i = 0; i < relativeOffsets.size(); i++) {
            long rel = relativeOffsets.getLong(i);
            int relChunkX = ChunkUtil.getChunkPosX(rel);
            int relChunkZ = ChunkUtil.getChunkPosZ(rel);
            absolute.add(ChunkPos.asLong(baseChunkX + relChunkX, baseChunkZ + relChunkZ));
        }
        return absolute;
    }

    private static boolean generateForChunkFast(World world, DimensionState state, int chunkX, int chunkZ) {
        long key = ChunkPos.asLong(chunkX, chunkZ);
        long oldProcessingChunk = state.currentlyProcessingChunk;
        boolean oldProcessingFlag = state.processingTasks;

        state.currentlyProcessingChunk = key;
        state.processingTasks = true;
        ArrayList<PhasedChunkTask> list = state.componentsByChunk.remove(key);

        if (list == null || list.isEmpty()) {
            state.currentlyProcessingChunk = oldProcessingChunk;
            state.processingTasks = oldProcessingFlag;
            if (list != null) recycleTaskList(state, list);
            return false;
        }

        try {
            boolean generated = false;
            for (int i = 0, listSize = list.size(); i < listSize; i++) {
                PhasedChunkTask task = list.get(i);
                if (task == null) continue;
                PhasedStructureStart parent = task.parent;
                if (parent == null) continue;
                if (!parent.isValidForPostProcess(key)) {
                    continue;
                }
                task.generate(world, true); // true = full cleanup allowed (we are processing the whole chunk)
                generated = true;
            }
            return generated;
        } finally {
            recycleTaskList(state, list);
            state.currentlyProcessingChunk = oldProcessingChunk;
            state.processingTasks = oldProcessingFlag;
            drainRecycleQueue(state);
            drainCompletedStarts(state);
        }
    }

    private static PhasedChunkTask borrowTask(DimensionState state, PhasedStructureStart parent, int relChunkX, int relChunkZ,
                                              Long2ObjectOpenHashMap<BlockInfo> blocks, boolean markerOnly) {
        PhasedChunkTask task;
        int poolSize = state.chunkTaskPool.size();
        if (poolSize > 0) {
            task = state.chunkTaskPool.remove(poolSize - 1);
        } else {
            task = new PhasedChunkTask();
        }
        task.reset(parent, relChunkX, relChunkZ, blocks, markerOnly);
        return task;
    }

    private static LongArrayList borrowAdditionalChunkList(DimensionState state) {
        int poolSize = state.additionalChunkPool.size();
        LongArrayList list = poolSize > 0 ? state.additionalChunkPool.remove(poolSize - 1) : new LongArrayList();
        list.clear();
        return list;
    }

    private static void recycleAdditionalChunkList(DimensionState state, LongArrayList list) {
        if (list == null) return;
        list.clear();
        if (state.additionalChunkPool.size() < MAX_ADDITIONAL_CHUNK_POOL_SIZE) {
            state.additionalChunkPool.add(list);
        }
    }

    private static ArrayList<PhasedChunkTask> borrowTaskList(DimensionState state) {
        int poolSize = state.chunkTaskListPool.size();
        ArrayList<PhasedChunkTask> list = poolSize > 0 ? state.chunkTaskListPool.remove(poolSize - 1) : new ArrayList<>(1);
        list.clear();
        return list;
    }

    private static void recycleTaskList(DimensionState state, ArrayList<PhasedChunkTask> list) {
        if (list == null) return;
        for (int i = 0, listSize = list.size(); i < listSize; i++) {
            PhasedChunkTask task = list.get(i);
            recycleTask(state, task);
        }
        list.clear();
        if (state.chunkTaskListPool.size() < MAX_TASK_LIST_POOL_SIZE) {
            state.chunkTaskListPool.add(list);
        }
    }

    private static void recycleTask(DimensionState state, PhasedChunkTask task) {
        if (task == null) return;
        task.release();
        if (state.chunkTaskPool.size() < MAX_TASK_POOL_SIZE) {
            state.chunkTaskPool.add(task);
        }
    }

    private static void recycleAllComponents(DimensionState state) {
        ObjectIterator<Long2ObjectMap.Entry<ArrayList<PhasedChunkTask>>> iterator = state.componentsByChunk.long2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Long2ObjectMap.Entry<ArrayList<PhasedChunkTask>> entry = iterator.next();
            recycleTaskList(state, entry.getValue());
        }
        drainRecycleQueue(state);
    }

    private static PhasedStructureStart borrowStart(DimensionState state) {
        int size = state.structureStartPool.size();
        PhasedStructureStart start = size > 0 ? state.structureStartPool.remove(size - 1) : new PhasedStructureStart();
        start.dimension = state.dimension;
        start.resetState();
        return start;
    }

    private static PhasedStructureStart borrowStart(DimensionState state, ReadyToGenerateStructure ready) {
        PhasedStructureStart start = borrowStart(state);
        start.init(ready, state);
        return start;
    }

    private static void recycleStart(DimensionState state, PhasedStructureStart start) {
        if (start == null) return;
        start.release();
        if (state.structureStartPool.size() < MAX_START_POOL_SIZE) {
            state.structureStartPool.add(start);
        }
    }

    private static void onStructureComplete(DimensionState state, PhasedStructureStart start) {
        if (start == null) return;
        if (state.processingTasks) {
            state.completedStarts.add(start);
            return;
        }
        finalizeStart(state, start);
    }

    private static void finalizeStart(DimensionState state, PhasedStructureStart start) {
        long key = ChunkPos.asLong(start.chunkPosX, start.chunkPosZ);
        state.structureMap.remove(key);
        recycleStart(state, start);
    }

    private static void recycleAllStarts(DimensionState state) {
        drainCompletedStarts(state);
        ObjectIterator<Long2ObjectMap.Entry<PhasedStructureStart>> iterator = state.structureMap.long2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            PhasedStructureStart start = iterator.next().getValue();
            recycleStart(state, start);
        }
    }

    private static void registerComponent(DimensionState state, long key, PhasedChunkTask component) {
        if (state.processingTasks && key == state.currentlyProcessingChunk) return;
        ArrayList<PhasedChunkTask> list = state.componentsByChunk.get(key);
        if (list == null) {
            list = borrowTaskList(state);
            state.componentsByChunk.put(key, list);
        }
        list.add(component);
    }

    /**
     * Handles cleanup of tasks.
     *
     * @param fullCleanup If true, assumes the entire chunk is processed (nukes the list).
     *                    If false, removes only the specific task (preserves others).
     */
    private static void onChunkProcessed(DimensionState state, long key, PhasedChunkTask task, boolean fullCleanup) {
        ArrayList<PhasedChunkTask> list = state.componentsByChunk.get(key);
        if (list == null) return;

        if (fullCleanup) {
            state.componentsByChunk.remove(key);
            if (key == state.currentlyProcessingChunk) {
                state.recycleQueue.add(list);
            } else {
                recycleTaskList(state, list);
            }
        } else {
            list.remove(task);
            if (list.isEmpty()) {
                state.componentsByChunk.remove(key);
                recycleTaskList(state, list);
            }
            ArrayList<PhasedChunkTask> wrapper = borrowTaskList(state);
            wrapper.add(task);
            state.recycleQueue.add(wrapper);
        }
    }

    private static void drainRecycleQueue(DimensionState state) {
        for (int i = state.recycleQueue.size() - 1; i >= 0; i--) {
            ArrayList<PhasedChunkTask> list = state.recycleQueue.remove(i);
            recycleTaskList(state, list);
        }
    }

    private static void drainCompletedStarts(DimensionState state) {
        for (int i = state.completedStarts.size() - 1; i >= 0; i--) {
            PhasedStructureStart start = state.completedStarts.remove(i);
            finalizeStart(state, start);
        }
    }

    static void scheduleStructureForValidation(World world, long originSerialized, IPhasedStructure structure,
                                               Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<BlockInfo>> layout, long layoutSeed) {
        if (world.isRemote) return;
        DimensionState state = getState(world);
        state.world = world;

        if (layout.isEmpty()) {
            MainRegistry.logger.warn("Skipping structure {} generation at {} due to empty layout.", structure.getClass()
                                                                                                             .getSimpleName(), originSerialized);
            return;
        }

        PendingValidationStructure pending = new PendingValidationStructure(originSerialized, structure, layout, world.getSeed(), layoutSeed);
        ReadyToGenerateStructure ready = pending.structure.validate(world, pending);

        if (ready == null) {
            if (GeneralConfig.enableDebugWorldGen) {
                MainRegistry.logger.info("Structure {} at {} failed to validate on fast path.", pending.structure.getClass()
                                                                                                                 .getSimpleName(), pending.origin);
            }
            return;
        }

        PhasedStructureStart start = borrowStart(state, ready);
        long key = ChunkPos.asLong(start.chunkPosX, start.chunkPosZ);
        state.structureMap.put(key, start);
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world.isRemote) return;
        DimensionState state = getState(world);
        state.world = world;
        generateForChunkFast(world, state, chunkX, chunkZ);
    }

    @SubscribeEvent
    public void onChunkDataSave(ChunkDataEvent.Save event) {
        World world = event.getWorld();
        if (world.isRemote) return;
        DimensionState state = getState(world);
        state.world = world;

        Chunk chunk = event.getChunk();
        long chunkKey = ChunkPos.asLong(chunk.x, chunk.z);
        PhasedStructureStart start = state.structureMap.get(chunkKey);
        NBTTagCompound data = event.getData();

        if (start == null || start.remainingChunks.isEmpty()) {
            data.removeTag("HbmPhasedStructures");
            return;
        }

        NBTTagList list = new NBTTagList();
        list.appendTag(start.writeToNBT());
        data.setTag("HbmPhasedStructures", list);
    }

    @SubscribeEvent
    public void onChunkDataLoad(ChunkDataEvent.Load event) {
        World world = event.getWorld();
        if (world.isRemote) return;
        DimensionState state = getState(world);
        state.world = world;

        NBTTagCompound data = event.getData();
        if (!data.hasKey("HbmPhasedStructures", Constants.NBT.TAG_LIST)) return;
        NBTTagList list = data.getTagList("HbmPhasedStructures", Constants.NBT.TAG_COMPOUND);
        if (list.tagCount() == 0) return;
        final ArrayList<PhasedStructureStart> loaded = new ArrayList<>(list.tagCount());
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            PhasedStructureStart start = borrowStart(state);
            if (!start.readFromNBT(state, entry)) {
                recycleStart(state, start);
                continue;
            }
            long key = ChunkPos.asLong(start.chunkPosX, start.chunkPosZ);
            state.structureMap.put(key, start);
            start.registerTasksForRemaining();
            loaded.add(start);
        }
        if (!loaded.isEmpty()) {
            DelayedTick.nextWorldTickStart(world, w -> {
                DimensionState s = getState(w);
                s.world = w;
                for (PhasedStructureStart start : loaded) {
                    start.generateExistingChunks();
                }
            });
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        World world = event.getWorld();
        if (world.isRemote) return;
        int dim = world.provider.getDimension();
        DimensionState state = STATES.remove(dim);
        if (state != null) {
            clearState(state);
        }
    }

    private static final class DimensionState {
        final int dimension;
        final Long2ObjectOpenHashMap<ArrayList<PhasedChunkTask>> componentsByChunk = new Long2ObjectOpenHashMap<>(4096);
        final Long2ObjectOpenHashMap<PhasedStructureStart> structureMap = new Long2ObjectOpenHashMap<>(4096);
        final ArrayList<PhasedChunkTask> chunkTaskPool = new ArrayList<>(512);
        final ArrayList<PhasedStructureStart> structureStartPool = new ArrayList<>(512);
        final ArrayList<ArrayList<PhasedChunkTask>> chunkTaskListPool = new ArrayList<>(512);
        final ArrayList<ArrayList<PhasedChunkTask>> recycleQueue = new ArrayList<>(128);
        final ArrayList<PhasedStructureStart> completedStarts = new ArrayList<>(256);
        final ArrayList<LongArrayList> additionalChunkPool = new ArrayList<>(512);
        long currentlyProcessingChunk = Long.MIN_VALUE;
        boolean processingTasks;
        World world; // cached per-dimension world reference

        DimensionState(int dimension) {
            this.dimension = dimension;
        }
    }

    public static class ReadyToGenerateStructure {
        final PendingValidationStructure pending;
        final long finalOrigin; // y-adjusted origin if validation points are present
        final Random structureRand;

        public ReadyToGenerateStructure(PendingValidationStructure pending, long finalOrigin) {
            this.pending = pending;
            this.finalOrigin = finalOrigin;
            this.structureRand = PendingValidationStructure.createRandom(pending.worldSeed, pending.origin);
        }
    }

    public static class PendingValidationStructure {
        public final long origin;
        final IPhasedStructure structure;
        final Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<BlockInfo>> layout;
        final long worldSeed;
        final long layoutSeed;

        PendingValidationStructure(long origin, IPhasedStructure structure, Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<BlockInfo>> layout,
                                   long worldSeed, long layoutSeed) {
            this.origin = origin;
            this.structure = structure;
            this.layout = layout;
            this.worldSeed = worldSeed;
            this.layoutSeed = layoutSeed;
        }

        static Random createRandom(long seed, long origin) {
            Random rand = new Random(seed);
            long x = rand.nextLong() ^ Library.getBlockPosX(origin);
            long z = rand.nextLong() ^ Library.getBlockPosZ(origin);
            rand.setSeed(x * Library.getBlockPosX(origin) + z * Library.getBlockPosZ(origin) ^ seed);
            return rand;
        }
    }

    public static class PhasedStructureStart {
        private final LongOpenHashSet remainingChunks = new LongOpenHashSet(32);
        private final LongOpenHashSet processedChunks = new LongOpenHashSet(32);
        int chunkPosX;
        int chunkPosZ;
        private IPhasedStructure structure;
        private long finalOrigin = 0L;
        private long worldSeed;
        private long layoutSeed;
        private Random structureRand = new Random();
        private Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<BlockInfo>> layout;
        private boolean postGenerated;
        private boolean completionQueued;
        private int dimension;
        private int minX;
        private int maxX;
        private int minZ;
        private int maxZ;
        private ArrayList<PhasedChunkTask> components = new ArrayList<>();

        @SuppressWarnings("WeakerAccess")
        public PhasedStructureStart() {
        }

        private DimensionState state() {
            return PhasedStructureGenerator.getState(this.dimension);
        }

        void resetState() {
            this.remainingChunks.clear();
            this.processedChunks.clear();
            this.structure = null;
            this.finalOrigin = 0L;
            this.worldSeed = 0L;
            this.layoutSeed = 0L;
            if (this.structureRand == null) {
                this.structureRand = new Random();
            } else {
                this.structureRand.setSeed(0L);
            }
            this.layout = null;
            this.postGenerated = false;
            this.chunkPosX = 0;
            this.chunkPosZ = 0;
            this.minX = 0;
            this.maxX = 0;
            this.minZ = 0;
            this.maxZ = 0;
            this.completionQueued = false;
            if (this.components == null) {
                this.components = new ArrayList<>();
            } else {
                this.components.clear();
            }
        }

        void init(ReadyToGenerateStructure ready, DimensionState state) {
            resetState();
            this.dimension = state.dimension;
            this.chunkPosX = Library.getBlockPosX(ready.finalOrigin) >> 4;
            this.chunkPosZ = Library.getBlockPosZ(ready.finalOrigin) >> 4;
            this.structure = ready.pending.structure;
            this.finalOrigin = ready.finalOrigin;
            this.worldSeed = ready.pending.worldSeed;
            this.layoutSeed = ready.pending.layoutSeed;
            this.structureRand = ready.structureRand;
            this.layout = ready.pending.layout;
            buildComponentsFromLayout();
            generateExistingChunks();
        }

        void release() {
            resetState();
        }

        /**
         * Serialize this start to chunk NBT. Uses PhasedStructureRegistry type id + structure NBT.
         */
        NBTTagCompound writeToNBT() {
            NBTTagCompound nbt = new NBTTagCompound();
            if (this.structure == null) {
                return nbt;
            }

            short typeId = PhasedStructureRegistry.getId(this.structure);
            nbt.setShort("Type", typeId);
            nbt.setLong("WorldSeed", worldSeed);
            nbt.setLong("LayoutSeed", layoutSeed);
            nbt.setLong("Origin", finalOrigin);
            nbt.setInteger("ChunkX", this.chunkPosX);
            nbt.setInteger("ChunkZ", this.chunkPosZ);

            NBTTagCompound data = new NBTTagCompound();
            this.structure.writeToNBT(data);
            nbt.setTag("Data", data);

            if (!remainingChunks.isEmpty()) {
                nbt.setTag("Remaining", new NBTTagLongArray(remainingChunks.toLongArray()));
            }
            if (!processedChunks.isEmpty()) {
                nbt.setTag("Processed", new NBTTagLongArray(processedChunks.toLongArray()));
            }
            nbt.setBoolean("PostGenerated", postGenerated);
            return nbt;
        }

        /**
         * Deserialize from chunk NBT using PhasedStructureRegistry.
         *
         * @return true if successfully deserialized and has remaining chunks.
         */
        boolean readFromNBT(DimensionState state, NBTTagCompound nbt) {
            resetState();
            this.dimension = state.dimension;

            short typeId = nbt.getShort("Type");
            NBTTagCompound data = nbt.getCompoundTag("Data");
            IPhasedStructure struct;
            try {
                struct = PhasedStructureRegistry.deserialize(typeId, data);
            } catch (Exception e) {
                MainRegistry.logger.warn("Failed to deserialize phased structure type {} in dimension {}", typeId, state.dimension, e);
                return false;
            }

            this.structure = struct;
            this.worldSeed = nbt.getLong("WorldSeed");
            this.layoutSeed = nbt.getLong("LayoutSeed");
            this.finalOrigin = nbt.getLong("Origin");
            this.chunkPosX = nbt.getInteger("ChunkX");
            this.chunkPosZ = nbt.getInteger("ChunkZ");
            this.structureRand = PendingValidationStructure.createRandom(this.worldSeed, this.finalOrigin);

            this.remainingChunks.clear();
            if (nbt.hasKey("Remaining")) {
                long[] remaining = ((NBTTagLongArray) nbt.getTag("Remaining")).data;
                for (int i = 0; i < remaining.length; i++) {
                    this.remainingChunks.add(remaining[i]);
                }
            }

            this.processedChunks.clear();
            if (nbt.hasKey("Processed")) {
                long[] processed = ((NBTTagLongArray) nbt.getTag("Processed")).data;
                for (int i = 0; i < processed.length; i++) {
                    this.processedChunks.add(processed[i]);
                }
            }

            this.postGenerated = nbt.getBoolean("PostGenerated");
            this.layout = null; // force rebuild from structure

            return !this.remainingChunks.isEmpty();
        }

        void registerTasksForRemaining() {
            ensureLayout();
            if (this.remainingChunks.isEmpty()) return;
            if (this.components == null) {
                this.components = new ArrayList<>();
            } else {
                this.components.clear();
            }
            this.components.ensureCapacity(this.remainingChunks.size());

            DimensionState state = state();
            int originChunkX = Library.getBlockPosX(this.finalOrigin) >> 4;
            int originChunkZ = Library.getBlockPosZ(this.finalOrigin) >> 4;
            minX = originChunkX;
            maxX = originChunkX;
            minZ = originChunkZ;
            maxZ = originChunkZ;

            ObjectIterator<Long2ObjectMap.Entry<Long2ObjectOpenHashMap<BlockInfo>>> iterator = this.layout.long2ObjectEntrySet().fastIterator();
            while (iterator.hasNext()) {
                Long2ObjectMap.Entry<Long2ObjectOpenHashMap<BlockInfo>> entry = iterator.next();
                long relKey = entry.getLongKey();
                int relChunkX = ChunkUtil.getChunkPosX(relKey);
                int relChunkZ = ChunkUtil.getChunkPosZ(relKey);
                int absChunkX = originChunkX + relChunkX;
                int absChunkZ = originChunkZ + relChunkZ;
                long absKey = ChunkPos.asLong(absChunkX, absChunkZ);
                if (!remainingChunks.contains(absKey)) continue;

                minX = Math.min(minX, absChunkX);
                maxX = Math.max(maxX, absChunkX);
                minZ = Math.min(minZ, absChunkZ);
                maxZ = Math.max(maxZ, absChunkZ);

                PhasedChunkTask component = borrowTask(state, this, relChunkX, relChunkZ, entry.getValue(), false);
                this.components.add(component);
                registerComponent(state, absKey, component);
            }

            if (this.structure != null) {
                LongArrayList watched = this.structure.getWatchedChunkOffsets(finalOrigin);
                LongArrayList extras = watched == null ? null : translateOffsets(state, finalOrigin, watched);
                if (extras != null && !extras.isEmpty()) {
                    LongListIterator iter = extras.iterator();
                    while (iter.hasNext()) {
                        long extra = iter.nextLong();
                        if (!this.remainingChunks.contains(extra)) continue;
                        int absX = ChunkUtil.getChunkPosX(extra);
                        int absZ = ChunkUtil.getChunkPosZ(extra);
                        int relX = absX - originChunkX;
                        int relZ = absZ - originChunkZ;

                        minX = Math.min(minX, absX);
                        maxX = Math.max(maxX, absX);
                        minZ = Math.min(minZ, absZ);
                        maxZ = Math.max(maxZ, absZ);

                        PhasedChunkTask marker = borrowTask(state, this, relX, relZ, null, true);
                        this.components.add(marker);
                        registerComponent(state, extra, marker);
                    }
                }
                recycleAdditionalChunkList(state, extras);
            }
            minX = (minX << 4);
            minZ = (minZ << 4);
            maxX = (maxX << 4) + 15;
            maxZ = (maxZ << 4) + 15;
        }

        private void buildComponentsFromLayout() {
            ensureLayout();
            remainingChunks.clear();
            if (this.components == null) {
                this.components = new ArrayList<>();
            } else {
                this.components.clear();
            }
            if (this.layout != null) {
                this.components.ensureCapacity(this.layout.size());
            }

            DimensionState state = state();
            int originChunkX = Library.getBlockPosX(this.finalOrigin) >> 4;
            int originChunkZ = Library.getBlockPosZ(this.finalOrigin) >> 4;
            minX = originChunkX;
            maxX = originChunkX;
            minZ = originChunkZ;
            maxZ = originChunkZ;

            ObjectIterator<Long2ObjectMap.Entry<Long2ObjectOpenHashMap<BlockInfo>>> iterator = this.layout.long2ObjectEntrySet().fastIterator();

            while (iterator.hasNext()) {
                Long2ObjectMap.Entry<Long2ObjectOpenHashMap<BlockInfo>> entry = iterator.next();
                long relKey = entry.getLongKey();
                Long2ObjectOpenHashMap<BlockInfo> blocksForThisChunk = entry.getValue();

                int relChunkX = ChunkUtil.getChunkPosX(relKey);
                int relChunkZ = ChunkUtil.getChunkPosZ(relKey);
                int absChunkX = originChunkX + relChunkX;
                int absChunkZ = originChunkZ + relChunkZ;

                long chunkKey = ChunkPos.asLong(absChunkX, absChunkZ);
                PhasedChunkTask component = borrowTask(state, this, relChunkX, relChunkZ, blocksForThisChunk, false);
                this.components.add(component);
                this.remainingChunks.add(chunkKey);
                registerComponent(state, chunkKey, component);

                minX = Math.min(minX, absChunkX);
                maxX = Math.max(maxX, absChunkX);
                minZ = Math.min(minZ, absChunkZ);
                maxZ = Math.max(maxZ, absChunkZ);
            }

            if (this.structure != null) {
                LongArrayList watched = this.structure.getWatchedChunkOffsets(finalOrigin);
                LongArrayList extras = watched == null ? null : translateOffsets(state, finalOrigin, watched);
                if (extras != null && !extras.isEmpty()) {
                    LongListIterator iter = extras.iterator();
                    while (iter.hasNext()) {
                        long extra = iter.nextLong();
                        if (this.remainingChunks.contains(extra)) continue;
                        int absX = ChunkUtil.getChunkPosX(extra);
                        int absZ = ChunkUtil.getChunkPosZ(extra);
                        int relX = absX - originChunkX;
                        int relZ = absZ - originChunkZ;

                        PhasedChunkTask marker = borrowTask(state, this, relX, relZ, null, true);
                        this.components.add(marker);
                        this.remainingChunks.add(extra);
                        registerComponent(state, extra, marker);

                        minX = Math.min(minX, absX);
                        maxX = Math.max(maxX, absX);
                        minZ = Math.min(minZ, absZ);
                        maxZ = Math.max(maxZ, absZ);
                    }
                }
                recycleAdditionalChunkList(state, extras);
            }
            minX = (minX << 4);
            minZ = (minZ << 4);
            maxX = (maxX << 4) + 15;
            maxZ = (maxZ << 4) + 15;
        }

        private void ensureLayout() {
            if (this.layout != null) return;
            if (this.structure instanceof AbstractPhasedStructure abstractPhasedStructure) {
                this.layout = abstractPhasedStructure.buildLayout(this.finalOrigin, this.layoutSeed);
            } else {
                this.layout = new Long2ObjectOpenHashMap<>();
            }
        }

        @Nullable Long2ObjectOpenHashMap<BlockInfo> getBlocksFor(int relChunkX, int relChunkZ) {
            ensureLayout();
            long key = ChunkPos.asLong(relChunkX, relChunkZ);
            return this.layout.get(key);
        }

        boolean isValidForPostProcess(long pos) {
            return !processedChunks.contains(pos);
        }

        void notifyPostProcessAt(World world, long key, boolean fullCleanup, PhasedChunkTask task) {
            processedChunks.add(key);
            remainingChunks.remove(key);
            DimensionState state = state();
            onChunkProcessed(state, key, task, fullCleanup);

            if (remainingChunks.isEmpty() && !postGenerated && structure != null) {
                postGenerated = true;
                try {
                    structure.postGenerate(world, structureRand, finalOrigin);
                } catch (Exception e) {
                    MainRegistry.logger.error("Error running postGenerate for {}", structure != null ? structure.getClass()
                                                                                                                .getSimpleName() : "<null>", e);
                }
            }
            if (remainingChunks.isEmpty()) {
                if (completionQueued) return;
                completionQueued = true;
                onStructureComplete(state, this);
            }
        }

        void markGenerated(World world, long chunkKey, boolean fullCleanup, PhasedChunkTask task) {
            notifyPostProcessAt(world, chunkKey, fullCleanup, task);
        }

        void generateExistingChunks() {
            DimensionState state = state();
            WorldServer server = (WorldServer) state.world;
            ChunkProviderServer provider = server.getChunkProvider();
            if (this.components == null) return;

            state.processingTasks = true;
            try {
                // noinspection unchecked
                ArrayList<PhasedChunkTask> snapshot = (ArrayList<PhasedChunkTask>) components.clone();
                for (PhasedChunkTask task : snapshot) {
                    if (task == null) continue;
                    long chunkKey = task.getChunkKey();
                    if (provider.loadedChunks.containsKey(chunkKey)) {
                        task.generate(server, false);
                    }
                }
            } finally {
                state.processingTasks = false;
                drainRecycleQueue(state);
                drainCompletedStarts(state);
            }
        }
    }

    static class PhasedChunkTask {
        PhasedStructureStart parent;
        private int relChunkX;
        private int relChunkZ;
        private Long2ObjectOpenHashMap<BlockInfo> blocks;
        private boolean markerOnly;
        private boolean generated;

        PhasedChunkTask() {
        }

        void reset(PhasedStructureStart parent, int relChunkX, int relChunkZ, Long2ObjectOpenHashMap<BlockInfo> blocks, boolean markerOnly) {
            this.parent = parent;
            this.relChunkX = relChunkX;
            this.relChunkZ = relChunkZ;
            this.blocks = blocks;
            this.markerOnly = markerOnly;
            this.generated = false;
        }

        void release() {
            this.parent = null;
            this.blocks = null;
            this.markerOnly = false;
            this.generated = false;
            this.relChunkX = 0;
            this.relChunkZ = 0;
        }

        long getChunkKey() {
            if (parent == null) {
                return ChunkPos.asLong(relChunkX, relChunkZ);
            }
            int absX = parent.chunkPosX + relChunkX;
            int absZ = parent.chunkPosZ + relChunkZ;
            return ChunkPos.asLong(absX, absZ);
        }

        void generate(World worldIn, boolean fullCleanup) {
            if (generated) return;
            if (parent == null || parent.structure == null) return;

            // Capture parent locally to prevent NPE if 'this' is recycled during recursive generation
            PhasedStructureStart localParent = this.parent;
            long chunkKey = getChunkKey();

            if (markerOnly) {
                generated = true;
                localParent.markGenerated(worldIn, chunkKey, fullCleanup, this);
                return;
            }
            if (this.blocks == null || this.blocks.isEmpty()) {
                Long2ObjectOpenHashMap<BlockInfo> rebuilt = localParent.getBlocksFor(relChunkX, relChunkZ);
                if (rebuilt != null) this.blocks = rebuilt;
            }
            if (this.blocks == null || this.blocks.isEmpty()) return;

            int absChunkX = localParent.chunkPosX + relChunkX;
            int absChunkZ = localParent.chunkPosZ + relChunkZ;
            try {
                localParent.structure.generateForChunk(worldIn, localParent.structureRand, localParent.finalOrigin, absChunkX, absChunkZ, this.blocks);

                generated = true;
                localParent.markGenerated(worldIn, chunkKey, fullCleanup, this);

            } catch (Exception e) {
                MainRegistry.logger.error("Error generating phased structure part at {} {}", absChunkX, absChunkZ, e);
            }
        }
    }
}
