package com.hbm.world.phased;

import com.hbm.config.GeneralConfig;
import com.hbm.main.MainRegistry;
import com.hbm.util.ChunkUtil;
import com.hbm.world.phased.AbstractPhasedStructure.BlockInfo;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Lightweight phased structure manager with minimal allocations and opt-in persistence.
 */
public class PhasedStructureGenerator implements IWorldGenerator {
    public static final PhasedStructureGenerator INSTANCE = new PhasedStructureGenerator();//singleton, only used for overworld
    private final Map<String, IPhasedStructure> structureRegistry = new HashMap<>();
    private final Long2ObjectOpenHashMap<ArrayList<PhasedChunkTask>> componentsByChunk = new Long2ObjectOpenHashMap<>(4096);
    private final Long2ObjectOpenHashMap<PhasedStructureStart> structureMap = new Long2ObjectOpenHashMap<>(4096);
    private final ArrayList<PhasedChunkTask> chunkTaskPool = new ArrayList<>();
    private final ArrayList<PhasedStructureStart> structureStartPool = new ArrayList<>();
    private final ArrayList<ArrayList<PhasedChunkTask>> chunkTaskListPool = new ArrayList<>();
    private final ArrayList<ArrayList<PhasedChunkTask>> recycleQueue = new ArrayList<>();
    private final ArrayList<PhasedStructureStart> completedStarts = new ArrayList<>();
    private final ArrayList<LongArrayList> additionalChunkPool = new ArrayList<>();
    private long currentlyProcessingChunk = Long.MIN_VALUE;
    private boolean processingTasks;
    private boolean pendingLoaded;
    private World world;

    private PhasedStructureGenerator() {
    }

    @Nullable
    private static ReadyToGenerateStructure validate(World world, PendingValidationStructure pending) {
        return pending.structure.validate(world, pending).orElse(null);
    }

    static void forceGenerateStructure(World world, Random rand, BlockPos origin, IPhasedStructure structure,
                                       Long2ObjectOpenHashMap<List<BlockInfo>> layout) {
        int originChunkX = origin.getX() >> 4;
        int originChunkZ = origin.getZ() >> 4;

        ObjectIterator<Long2ObjectMap.Entry<List<BlockInfo>>> iterator = layout.long2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Long2ObjectMap.Entry<List<BlockInfo>> entry = iterator.next();
            long relKey = entry.getLongKey();
            List<BlockInfo> blocksForThisChunk = entry.getValue();

            int relChunkX = ChunkUtil.getChunkPosX(relKey);
            int relChunkZ = ChunkUtil.getChunkPosZ(relKey);
            int absChunkX = originChunkX + relChunkX;
            int absChunkZ = originChunkZ + relChunkZ;

            structure.generateForChunk(world, rand, origin, absChunkX, absChunkZ, blocksForThisChunk);
        }
        structure.postGenerate(world, rand, origin);
    }

    void registerStructure(IPhasedStructure structure) {
        structureRegistry.put(structure.getId(), structure);
    }

    @Nullable
    private IPhasedStructure resolveStructure(String id) {
        return structureRegistry.get(id);
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world.isRemote) return;
        this.world = (WorldServer) world;
        ensurePendingLoaded(world);
        generateForChunkFast(world, chunkX, chunkZ);
    }

    void scheduleStructureForValidation(World world, BlockPos origin, IPhasedStructure structure, Long2ObjectOpenHashMap<List<BlockInfo>> layout,
                                        long layoutSeed) {
        this.world = world;
        ensurePendingLoaded(world);
        registerStructure(structure);

        boolean allowEmptyLayout = false;
        if (structure instanceof AbstractPhasedStructure phased) {
//            allowEmptyLayout = !structure.getValidationPoints(BlockPos.ORIGIN).isEmpty() && !phased.isCacheable();
            List<BlockPos> validationPoints = structure.getValidationPoints(origin);
            LongArrayList watchedOffsets = structure.getWatchedChunkOffsets(origin);
            LongArrayList additionalChunks = watchedOffsets == null ? null : AbstractPhasedStructure.translateOffsets(origin, watchedOffsets);
            boolean hasValidation = !validationPoints.isEmpty();
            boolean hasAdditionalChunks = additionalChunks != null && !additionalChunks.isEmpty();
            allowEmptyLayout = !phased.isCacheable() && (hasValidation || hasAdditionalChunks);
            recycleAdditionalChunkList(additionalChunks);
        }

        if (layout.isEmpty() && !allowEmptyLayout) {
            if (GeneralConfig.enableDebugWorldGen) {
                MainRegistry.logger.warn("Skipping structure {} generation at {} due to empty layout.", structure.getClass().getSimpleName(), origin);
            }
            return;
        }

        PendingValidationStructure pending = new PendingValidationStructure(origin, structure, layout, world.getSeed(), layoutSeed);
        ReadyToGenerateStructure ready = validate(world, pending);

        if (ready == null) {
            if (GeneralConfig.enableDebugWorldGen) {
                MainRegistry.logger.info("Structure {} at {} failed to validate on fast path.", pending.structure.getClass()
                                                                                                                 .getSimpleName(), pending.origin);
            }
            return;
        }

        PhasedStructureStart start = borrowStart(ready);
        long key = ChunkPos.asLong(start.chunkPosX, start.chunkPosZ);
        this.structureMap.put(key, start);
    }

    private void ensurePendingLoaded(World world) {
        if (pendingLoaded || world == null || world.isRemote) return;
        pendingLoaded = true;
        if (!(world instanceof WorldServer server)) return;
        PhasedStructurePendingData data = PhasedStructurePendingData.forWorld(server);
        NBTTagList pending = data.getPending();
        if (pending == null || pending.isEmpty()) return;
        for (int i = 0; i < pending.tagCount(); i++) {
            NBTTagCompound nbt = pending.getCompoundTagAt(i);
            PhasedStructureStart start = borrowStart();
            if (!start.readPending(nbt)) {
                recycleStart(start);
                continue;
            }
            long key = ChunkPos.asLong(start.chunkPosX, start.chunkPosZ);
            this.structureMap.put(key, start);
            start.registerTasksForRemaining();
        }
    }

    private void registerComponent(long key, PhasedChunkTask component) {
        if (processingTasks && key == currentlyProcessingChunk) return;
        ArrayList<PhasedChunkTask> list = componentsByChunk.get(key);
        if (list == null) {
            list = borrowTaskList();
            componentsByChunk.put(key, list);
        }
        list.add(component);
    }

    private void onChunkProcessed(long key) {
        ArrayList<PhasedChunkTask> list = componentsByChunk.remove(key);
        if (list == null) return;
        if (key == currentlyProcessingChunk) {
            recycleQueue.add(list);
        } else {
            recycleTaskList(list);
        }
    }

    private boolean generateForChunkFast(World world, int chunkX, int chunkZ) {
        long key = ChunkPos.asLong(chunkX, chunkZ);
        currentlyProcessingChunk = key;
        processingTasks = true;
        try {
            ArrayList<PhasedChunkTask> list = componentsByChunk.get(key);
            if (list == null || list.isEmpty()) {
                return false;
            }
            boolean generated = false;
            for (int i = 0, listSize = list.size(); i < listSize; i++) {
                PhasedChunkTask task = list.get(i);
                if (task == null) continue;
                PhasedStructureStart parent = task.parent;
                if (parent == null) continue;
                if (!parent.isValidForPostProcess(key)) {
                    continue;
                }
                task.generate(world);
                generated = true;
            }
            return generated;
        } finally {
            currentlyProcessingChunk = Long.MIN_VALUE;
            processingTasks = false;
            drainRecycleQueue();
            drainCompletedStarts();
        }
    }

    public static class ReadyToGenerateStructure {
        final PendingValidationStructure pending;
        final BlockPos finalOrigin;
        final Random structureRand;

        public ReadyToGenerateStructure(PendingValidationStructure pending, BlockPos finalOrigin) {
            this.pending = pending;
            this.finalOrigin = finalOrigin;
            this.structureRand = PendingValidationStructure.createRandom(pending.worldSeed, pending.origin);
        }
    }

    public static class PendingValidationStructure {
        public final BlockPos origin;
        final IPhasedStructure structure;
        final Long2ObjectOpenHashMap<List<BlockInfo>> layout;
        final long worldSeed;
        final long layoutSeed;

        PendingValidationStructure(BlockPos origin, IPhasedStructure structure, Long2ObjectOpenHashMap<List<BlockInfo>> layout, long worldSeed,
                                   long layoutSeed) {
            this.origin = origin;
            this.structure = structure;
            this.layout = layout;
            this.worldSeed = worldSeed;
            this.layoutSeed = layoutSeed;
        }

        static Random createRandom(long seed, BlockPos origin) {
            Random rand = new Random(seed);
            long x = rand.nextLong() ^ origin.getX();
            long z = rand.nextLong() ^ origin.getZ();
            rand.setSeed(x * origin.getX() + z * origin.getZ() ^ seed);
            return rand;
        }
    }

    public static class PhasedStructureStart {
        private final LongOpenHashSet remainingChunks = new LongOpenHashSet(32);
        private final LongOpenHashSet processedChunks = new LongOpenHashSet(32);
        private IPhasedStructure structure;
        private String structureId;
        private BlockPos finalOrigin = BlockPos.ORIGIN;
        private long worldSeed;
        private long layoutSeed;
        private Random structureRand = new Random();
        private Long2ObjectOpenHashMap<List<BlockInfo>> layout;
        private boolean postGenerated;
        private boolean completionQueued;
        private World cachedWorld;
        int chunkPosX;
        int chunkPosZ;
        private int minX;
        private int maxX;
        private int minZ;
        private int maxZ;
        private ArrayList<PhasedChunkTask> components = new ArrayList<>();

        @SuppressWarnings("WeakerAccess")
        public PhasedStructureStart() {
        }

        PhasedStructureStart(ReadyToGenerateStructure ready) {
            init(ready);
        }

        void resetState() {
            this.remainingChunks.clear();
            this.processedChunks.clear();
            this.structure = null;
            this.structureId = null;
            this.finalOrigin = BlockPos.ORIGIN;
            this.worldSeed = 0L;
            this.layoutSeed = 0L;
            if (this.structureRand == null) {
                this.structureRand = new Random();
            } else {
                this.structureRand.setSeed(0L);
            }
            this.layout = null;
            this.postGenerated = false;
            this.cachedWorld = null;
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

        void init(ReadyToGenerateStructure ready) {
            resetState();
            this.chunkPosX = ready.finalOrigin.getX() >> 4;
            this.chunkPosZ = ready.finalOrigin.getZ() >> 4;
            this.structure = ready.pending.structure;
            this.structureId = structure.getId();
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

        NBTTagCompound writePending() {
            if (this.structureId == null || this.remainingChunks.isEmpty()) return null;
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("StructureId", this.structureId);
            nbt.setLong("WorldSeed", this.worldSeed);
            nbt.setLong("LayoutSeed", this.layoutSeed);
            nbt.setInteger("OriginX", finalOrigin.getX());
            nbt.setInteger("OriginY", finalOrigin.getY());
            nbt.setInteger("OriginZ", finalOrigin.getZ());
            nbt.setInteger("ChunkX", this.chunkPosX);
            nbt.setInteger("ChunkZ", this.chunkPosZ);
            nbt.setTag("Remaining", new NBTTagLongArray(remainingChunks.toLongArray()));
            return nbt;
        }

        boolean readPending(NBTTagCompound nbt) {
            resetState();
            this.structureId = nbt.getString("StructureId");
            this.worldSeed = nbt.getLong("WorldSeed");
            this.layoutSeed = nbt.getLong("LayoutSeed");
            this.finalOrigin = new BlockPos(nbt.getInteger("OriginX"), nbt.getInteger("OriginY"), nbt.getInteger("OriginZ"));
            this.structureRand = PendingValidationStructure.createRandom(this.worldSeed, this.finalOrigin);
            this.structure = PhasedStructureGenerator.INSTANCE.resolveStructure(this.structureId);
            this.chunkPosX = nbt.hasKey("ChunkX") ? nbt.getInteger("ChunkX") : (this.finalOrigin.getX() >> 4);
            this.chunkPosZ = nbt.hasKey("ChunkZ") ? nbt.getInteger("ChunkZ") : (this.finalOrigin.getZ() >> 4);
            this.remainingChunks.clear();
            if (nbt.hasKey("Remaining")) {
                long[] remaining = ((NBTTagLongArray) nbt.getTag("Remaining")).data;
                for (int i = 0, remainingLength = remaining.length; i < remainingLength; i++) {
                    long key = remaining[i];
                    this.remainingChunks.add(key);
                }
            }
            this.processedChunks.clear();
            this.postGenerated = false;
            this.layout = null; // force rebuild from structure
            return this.structure != null && !this.remainingChunks.isEmpty();
        }

        void registerTasksForRemaining() {
            ensureLayout();
            if (this.remainingChunks.isEmpty()) return;
            this.components.clear();
            this.components.ensureCapacity(this.remainingChunks.size());

            int originChunkX = this.finalOrigin.getX() >> 4;
            int originChunkZ = this.finalOrigin.getZ() >> 4;
            int[] heightBounds = new int[]{finalOrigin.getY(), finalOrigin.getY()};
            minX = originChunkX;
            maxX = originChunkX;
            minZ = originChunkZ;
            maxZ = originChunkZ;

            ObjectIterator<Long2ObjectMap.Entry<List<BlockInfo>>> iterator = this.layout.long2ObjectEntrySet().fastIterator();
            while (iterator.hasNext()) {
                Long2ObjectMap.Entry<List<BlockInfo>> entry = iterator.next();
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
                updateHeightBounds(heightBounds, entry.getValue());

                PhasedChunkTask component = PhasedStructureGenerator.INSTANCE.borrowTask(this, relChunkX, relChunkZ, entry.getValue(), false);
                this.components.add(component);
                PhasedStructureGenerator.INSTANCE.registerComponent(absKey, component);
            }

            if (this.structure != null) {
                LongArrayList watched = this.structure.getWatchedChunkOffsets(this.finalOrigin);
                LongArrayList extras = watched == null ? null : AbstractPhasedStructure.translateOffsets(this.finalOrigin, watched);
                if (extras != null && !extras.isEmpty()) {
                    LongListIterator iter = extras.iterator();
                    while (iter.hasNext()) {
                        long extra = iter.nextLong();
                        if (!this.remainingChunks.contains(extra)) continue;
                        int relX = ChunkUtil.getChunkPosX(extra) - originChunkX;
                        int relZ = ChunkUtil.getChunkPosZ(extra) - originChunkZ;
                        int absX = ChunkUtil.getChunkPosX(extra);
                        int absZ = ChunkUtil.getChunkPosZ(extra);
                        minX = Math.min(minX, absX);
                        maxX = Math.max(maxX, absX);
                        minZ = Math.min(minZ, absZ);
                        maxZ = Math.max(maxZ, absZ);
                        PhasedChunkTask marker = PhasedStructureGenerator.INSTANCE.borrowTask(this, relX, relZ, null, true);
                        this.components.add(marker);
                        PhasedStructureGenerator.INSTANCE.registerComponent(extra, marker);
                    }
                }
                PhasedStructureGenerator.INSTANCE.recycleAdditionalChunkList(extras);
            }
            minX = (minX << 4);
            minZ = (minZ << 4);
            maxX = (maxX << 4) + 15;
            maxZ = (maxZ << 4) + 15;
        }

        private void buildComponentsFromLayout() {
            ensureLayout();
            remainingChunks.clear();
            this.components.clear();
            if (this.layout != null) {
                this.components.ensureCapacity(this.layout.size());
            }

            int originChunkX = this.finalOrigin.getX() >> 4;
            int originChunkZ = this.finalOrigin.getZ() >> 4;
            minX = originChunkX;
            maxX = originChunkX;
            minZ = originChunkZ;
            maxZ = originChunkZ;

            ObjectIterator<Long2ObjectMap.Entry<List<BlockInfo>>> iterator = this.layout.long2ObjectEntrySet().fastIterator();

            while (iterator.hasNext()) {
                Long2ObjectMap.Entry<List<BlockInfo>> entry = iterator.next();
                long relKey = entry.getLongKey();
                List<BlockInfo> blocksForThisChunk = entry.getValue();

                int relChunkX = ChunkUtil.getChunkPosX(relKey);
                int relChunkZ = ChunkUtil.getChunkPosZ(relKey);
                int absChunkX = originChunkX + relChunkX;
                int absChunkZ = originChunkZ + relChunkZ;

                long chunkKey = ChunkPos.asLong(absChunkX, absChunkZ);
                PhasedChunkTask component = PhasedStructureGenerator.INSTANCE.borrowTask(this, relChunkX, relChunkZ, blocksForThisChunk, false);
                this.components.add(component);
                this.remainingChunks.add(chunkKey);
                PhasedStructureGenerator.INSTANCE.registerComponent(chunkKey, component);

                minX = Math.min(minX, absChunkX);
                maxX = Math.max(maxX, absChunkX);
                minZ = Math.min(minZ, absChunkZ);
                maxZ = Math.max(maxZ, absChunkZ);
            }

            if (this.structure != null) {
                LongArrayList watched = this.structure.getWatchedChunkOffsets(this.finalOrigin);
                LongArrayList extras = watched == null ? null : AbstractPhasedStructure.translateOffsets(this.finalOrigin, watched);
                if (extras != null && !extras.isEmpty()) {
                    LongListIterator iter = extras.iterator();
                    while (iter.hasNext()) {
                        long extra = iter.nextLong();
                        if (this.remainingChunks.contains(extra)) continue;
                        int relX = ChunkUtil.getChunkPosX(extra) - originChunkX;
                        int relZ = ChunkUtil.getChunkPosZ(extra) - originChunkZ;
                        int absX = ChunkUtil.getChunkPosX(extra);
                        int absZ = ChunkUtil.getChunkPosZ(extra);
                        PhasedChunkTask marker = PhasedStructureGenerator.INSTANCE.borrowTask(this, relX, relZ, null, true);
                        this.components.add(marker);
                        this.remainingChunks.add(extra);
                        PhasedStructureGenerator.INSTANCE.registerComponent(extra, marker);
                        minX = Math.min(minX, absX);
                        maxX = Math.max(maxX, absX);
                        minZ = Math.min(minZ, absZ);
                        maxZ = Math.max(maxZ, absZ);
                    }
                }
                PhasedStructureGenerator.INSTANCE.recycleAdditionalChunkList(extras);
            }
            minX = (minX << 4);
            minZ = (minZ << 4);
            maxX = (maxX << 4) + 15;
            maxZ = (maxZ << 4) + 15;
        }

        private void updateHeightBounds(int[] minMax, List<BlockInfo> blocks) {
            if (blocks == null || blocks.isEmpty()) return;
            for (int i = 0, blocksSize = blocks.size(); i < blocksSize; i++) {
                BlockInfo info = blocks.get(i);
                int y = this.finalOrigin.getY() + info.relativePos.getY();
                minMax[0] = Math.min(minMax[0], y);
                minMax[1] = Math.max(minMax[1], y);
            }
        }

        private void ensureLayout() {
            if (this.layout != null) return;
            if (this.structure instanceof AbstractPhasedStructure abstractPhasedStructure) {
                this.layout = abstractPhasedStructure.buildLayout(this.finalOrigin, this.layoutSeed);
            } else {
                this.layout = new Long2ObjectOpenHashMap<>();
            }
        }

        @Nullable
        List<BlockInfo> getBlocksFor(int relChunkX, int relChunkZ) {
            ensureLayout();
            long key = ChunkPos.asLong(relChunkX, relChunkZ);
            return this.layout.get(key);
        }

        boolean isValidForPostProcess(long pos) {
            return !processedChunks.contains(pos);
        }

        void notifyPostProcessAt(long key) {
            processedChunks.add(key);
            remainingChunks.remove(key);
            PhasedStructureGenerator.INSTANCE.onChunkProcessed(key);

            if (remainingChunks.isEmpty() && !postGenerated && structure != null) {
                postGenerated = true;
                try {
                    World world = resolveWorld();
                    if (world != null) {
                        structure.postGenerate(world, structureRand, finalOrigin);
                    }
                } catch (Exception e) {
                    MainRegistry.logger.error("Error running postGenerate for {}", structureId, e);
                }
            }
            if (remainingChunks.isEmpty()) {
                if (completionQueued) return;
                completionQueued = true;
                PhasedStructureGenerator.INSTANCE.onStructureComplete(this);
            }
        }

        void markGenerated(long chunkKey) {
            notifyPostProcessAt(chunkKey);
        }

        public void writeToNBT(NBTTagCompound nbt) {
            nbt.setString("StructureId", structureId == null ? "" : structureId);
            nbt.setLong("WorldSeed", worldSeed);
            nbt.setLong("LayoutSeed", layoutSeed);
            nbt.setInteger("OriginX", finalOrigin.getX());
            nbt.setInteger("OriginY", finalOrigin.getY());
            nbt.setInteger("OriginZ", finalOrigin.getZ());
            if (!remainingChunks.isEmpty()) {
                nbt.setTag("Remaining", new NBTTagLongArray(remainingChunks.toLongArray()));
            }
            if (!processedChunks.isEmpty()) {
                nbt.setTag("Processed", new NBTTagLongArray(processedChunks.toLongArray()));
            }
            nbt.setBoolean("PostGenerated", postGenerated);
        }

        public void readFromNBT(NBTTagCompound nbt) {
            resetState();
            this.structureId = nbt.getString("StructureId");
            this.worldSeed = nbt.getLong("WorldSeed");
            this.layoutSeed = nbt.getLong("LayoutSeed");
            this.finalOrigin = new BlockPos(nbt.getInteger("OriginX"), nbt.getInteger("OriginY"), nbt.getInteger("OriginZ"));
            this.structureRand = PendingValidationStructure.createRandom(this.worldSeed, this.finalOrigin);
            this.structure = PhasedStructureGenerator.INSTANCE.resolveStructure(this.structureId);
            if (this.structure == null && GeneralConfig.enableDebugWorldGen) {
                MainRegistry.logger.warn("Missing phased structure for id {} while loading saved data.", this.structureId);
            }

            this.remainingChunks.clear();
            if (nbt.hasKey("Remaining")) {
                long[] remaining = ((NBTTagLongArray) nbt.getTag("Remaining")).data;
                for (int i = 0, remainingLength = remaining.length; i < remainingLength; i++) {
                    long key = remaining[i];
                    this.remainingChunks.add(key);
                }
            }

            this.processedChunks.clear();
            if (nbt.hasKey("Processed")) {
                long[] processed = ((NBTTagLongArray) nbt.getTag("Processed")).data;
                for (int i = 0, processedLength = processed.length; i < processedLength; i++) {
                    long key = processed[i];
                    this.processedChunks.add(key);
                }
            }
            this.postGenerated = nbt.getBoolean("PostGenerated");

            if (this.components != null) {
                PhasedChunkTask[] snapshot = this.components.toArray(new PhasedChunkTask[0]);
                for (int i = 0, snapshotLength = snapshot.length; i < snapshotLength; i++) {
                    PhasedChunkTask phased = snapshot[i];
                    if (phased == null) continue;
                    long key = phased.getChunkKey();

                    if (this.remainingChunks.contains(key)) {
                        PhasedStructureGenerator.INSTANCE.registerComponent(key, phased);
                    }
                }
            }
        }

        @Nullable
        private World resolveWorld() {
            if (cachedWorld != null) return cachedWorld;
            return PhasedStructureGenerator.INSTANCE.world;
        }

        void generateExistingChunks() {
            World world = resolveWorld();
            if (world == null) return;
            ChunkProviderServer provider = (ChunkProviderServer) world.getChunkProvider();
            if (this.components == null) return;
            PhasedStructureGenerator.INSTANCE.processingTasks = true;
            try {
                PhasedChunkTask[] snapshot = this.components.toArray(new PhasedChunkTask[0]);
                for (int i = 0, snapshotLength = snapshot.length; i < snapshotLength; i++) {
                    PhasedChunkTask task = snapshot[i];
                    if (task == null) continue;
                    long chunkKey = task.getChunkKey();
                    if (provider.loadedChunks.containsKey(chunkKey)) {
                        task.generate(world);
                    }
                }
            } finally {
                PhasedStructureGenerator.INSTANCE.processingTasks = false;
                PhasedStructureGenerator.INSTANCE.drainCompletedStarts();
            }
        }

        boolean isSizeableStructure() {
            return maxX - minX > 0 && maxZ - minZ > 0;
        }

        boolean intersectsChunk(int minX, int minZ, int maxX, int maxZ) {
            return this.maxX >= minX && this.minX <= maxX && this.maxZ >= minZ && this.minZ <= maxZ;
        }
    }

    public static class PhasedChunkTask {
        PhasedStructureStart parent;
        private int relChunkX;
        private int relChunkZ;
        private List<BlockInfo> blocks;
        private boolean markerOnly;
        private boolean generated;

        PhasedChunkTask() {
        }

        PhasedChunkTask(PhasedStructureStart parent, int relChunkX, int relChunkZ, List<BlockInfo> blocks, boolean markerOnly) {
            reset(parent, relChunkX, relChunkZ, blocks, markerOnly);
        }

        void reset(PhasedStructureStart parent, int relChunkX, int relChunkZ, List<BlockInfo> blocks, boolean markerOnly) {
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

        void generate(World worldIn) {
            if (generated) return;
            if (parent == null || parent.structure == null) return;
            long chunkKey = getChunkKey();
            if (markerOnly) {
                generated = true;
                parent.markGenerated(chunkKey);
                return;
            }
            if (this.blocks == null || this.blocks.isEmpty()) {
                List<BlockInfo> rebuilt = parent.getBlocksFor(relChunkX, relChunkZ);
                if (rebuilt != null) this.blocks = rebuilt;
            }
            if (this.blocks == null || this.blocks.isEmpty()) return;

            int absChunkX = parent.chunkPosX + relChunkX;
            int absChunkZ = parent.chunkPosZ + relChunkZ;
            try {
                parent.structure.generateForChunk(worldIn, parent.structureRand, parent.finalOrigin, absChunkX, absChunkZ, this.blocks);
                generated = true;
                parent.markGenerated(chunkKey);
            } catch (Exception e) {
                MainRegistry.logger.error("Error generating phased structure part at {} {}", absChunkX, absChunkZ, e);
            }
        }
    }

    @SubscribeEvent
    public void onWorldSave(WorldEvent.Save event) {
        World world = event.getWorld();
        if (world.isRemote || world.provider.getDimension() != 0) return;
        savePending(world);
        recycleAllComponents();
        recycleAllStarts();
        this.componentsByChunk.clear();
        this.structureMap.clear();
        this.chunkTaskPool.clear();
        this.structureStartPool.clear();
        this.chunkTaskListPool.clear();
        this.recycleQueue.clear();
        this.completedStarts.clear();
        this.additionalChunkPool.clear();
        this.world = null;
        this.currentlyProcessingChunk = Long.MIN_VALUE;
        this.processingTasks = false;
        this.pendingLoaded = false;
    }

    private void savePending(World world) {
        if (!(world instanceof WorldServer server)) return;
        NBTTagList pendingList = new NBTTagList();
        ObjectIterator<Long2ObjectMap.Entry<PhasedStructureStart>> iterator = this.structureMap.long2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Long2ObjectMap.Entry<PhasedStructureStart> entry = iterator.next();
            PhasedStructureStart start = entry.getValue();
            if (start == null) continue;
            if (start.remainingChunks.isEmpty()) continue;
            NBTTagCompound nbt = start.writePending();
            if (nbt != null) {
                pendingList.appendTag(nbt);
            }
        }

        PhasedStructurePendingData data = PhasedStructurePendingData.forWorld(server);
        data.setPending(pendingList);
    }

    PhasedChunkTask borrowTask(PhasedStructureStart parent, int relChunkX, int relChunkZ, List<BlockInfo> blocks, boolean markerOnly) {
        PhasedChunkTask task;
        int poolSize = chunkTaskPool.size();
        if (poolSize > 0) {
            task = chunkTaskPool.remove(poolSize - 1);
        } else {
            task = new PhasedChunkTask();
        }
        task.reset(parent, relChunkX, relChunkZ, blocks, markerOnly);
        return task;
    }

    LongArrayList borrowAdditionalChunkList() {
        int poolSize = additionalChunkPool.size();
        LongArrayList list = poolSize > 0 ? additionalChunkPool.remove(poolSize - 1) : new LongArrayList();
        list.clear();
        return list;
    }

    void recycleAdditionalChunkList(LongArrayList list) {
        if (list == null) return;
        list.clear();
        additionalChunkPool.add(list);
    }

    private ArrayList<PhasedChunkTask> borrowTaskList() {
        int poolSize = chunkTaskListPool.size();
        ArrayList<PhasedChunkTask> list = poolSize > 0 ? chunkTaskListPool.remove(poolSize - 1) : new ArrayList<>(1);
        list.clear();
        return list;
    }

    private void recycleTaskList(ArrayList<PhasedChunkTask> list) {
        if (list == null) return;
        for (int i = 0, listSize = list.size(); i < listSize; i++) {
            PhasedChunkTask task = list.get(i);
            if (task != null && task.parent != null && task.parent.components != null) {
                task.parent.components.remove(task);
            }
            recycleTask(task);
        }
        list.clear();
        chunkTaskListPool.add(list);
    }

    private void recycleTask(PhasedChunkTask task) {
        if (task == null) return;
        task.release();
        chunkTaskPool.add(task);
    }

    private void recycleAllComponents() {
        ObjectIterator<Long2ObjectMap.Entry<ArrayList<PhasedChunkTask>>> iterator = componentsByChunk.long2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Long2ObjectMap.Entry<ArrayList<PhasedChunkTask>> entry = iterator.next();
            recycleTaskList(entry.getValue());
        }
        drainRecycleQueue();
    }

    private PhasedStructureStart borrowStart() {
        int size = structureStartPool.size();
        PhasedStructureStart start = size > 0 ? structureStartPool.remove(size - 1) : new PhasedStructureStart();
        start.resetState();
        return start;
    }

    private PhasedStructureStart borrowStart(ReadyToGenerateStructure ready) {
        PhasedStructureStart start = borrowStart();
        start.init(ready);
        return start;
    }

    void recycleStart(PhasedStructureStart start) {
        if (start == null) return;
        start.release();
        structureStartPool.add(start);
    }

    private void onStructureComplete(PhasedStructureStart start) {
        if (start == null) return;
        if (processingTasks) {
            completedStarts.add(start);
            return;
        }
        finalizeStart(start);
    }

    private void finalizeStart(PhasedStructureStart start) {
        long key = ChunkPos.asLong(start.chunkPosX, start.chunkPosZ);
        this.structureMap.remove(key);
        recycleStart(start);
    }

    private void recycleAllStarts() {
        drainCompletedStarts();
        ObjectIterator<Long2ObjectMap.Entry<PhasedStructureStart>> iterator = this.structureMap.long2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            PhasedStructureStart start = iterator.next().getValue();
            recycleStart(start);
        }
    }

    private void drainRecycleQueue() {
        for (int i = recycleQueue.size() - 1; i >= 0; i--) {
            ArrayList<PhasedChunkTask> list = recycleQueue.remove(i);
            recycleTaskList(list);
        }
    }

    private void drainCompletedStarts() {
        for (int i = completedStarts.size() - 1; i >= 0; i--) {
            PhasedStructureStart start = completedStarts.remove(i);
            finalizeStart(start);
        }
    }

    public static class PhasedStructurePendingData extends WorldSavedData {
        private static final String ID = "PhasedStructurePending";
        private NBTTagList pending = new NBTTagList();

        @SuppressWarnings("WeakerAccess")
        public PhasedStructurePendingData() {
            super(ID);
        }

        public PhasedStructurePendingData(String name) {
            super(name);
        }

        static PhasedStructurePendingData forWorld(WorldServer world) {
            MapStorage storage = world.getPerWorldStorage();
            PhasedStructurePendingData data = (PhasedStructurePendingData) storage.getOrLoadData(PhasedStructurePendingData.class, ID);
            if (data == null) {
                data = new PhasedStructurePendingData();
                storage.setData(ID, data);
            }
            return data;
        }

        NBTTagList getPending() {
            return pending;
        }

        void setPending(NBTTagList list) {
            this.pending = list;
            this.markDirty();
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            this.pending = nbt.hasKey("Pending") ? nbt.getTagList("Pending", 10) : new NBTTagList();
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            nbt.setTag("Pending", pending);
            return nbt;
        }
    }
}
