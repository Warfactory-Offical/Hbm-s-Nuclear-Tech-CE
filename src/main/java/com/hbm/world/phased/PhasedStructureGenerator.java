package com.hbm.world.phased;

import com.hbm.config.GeneralConfig;
import com.hbm.lib.internal.MethodHandleHelper;
import com.hbm.main.MainRegistry;
import com.hbm.util.ChunkUtil;
import com.hbm.world.phased.AbstractPhasedStructure.BlockInfo;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fml.common.IWorldGenerator;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.*;

/**
 * After doing so much i realized that i could have just used the existing mapgen structure system
 *
 * @author mlbv
 */
public class PhasedStructureGenerator extends MapGenStructure implements IWorldGenerator {
    public static final PhasedStructureGenerator INSTANCE = new PhasedStructureGenerator();
    private static final String STRUCTURE_NAME = "PhasedStructures";
    private static final MethodHandle MH_SET_STRUCTURE_START = MethodHandleHelper.findSpecial(MapGenStructure.class, MapGenStructure.class, "setStructureStart", "func_143026_a", MethodType.methodType(void.class, int.class, int.class, StructureStart.class));
    private final Map<String, IPhasedStructure> structureRegistry = new HashMap<>();
    private final Long2ObjectOpenHashMap<ObjectArrayList<PhasedStructureComponent>> componentsByChunk = new Long2ObjectOpenHashMap<>();

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

            ChunkPos absoluteChunkPos = new ChunkPos(absChunkX, absChunkZ);
            structure.generateForChunk(world, rand, origin, absoluteChunkPos, blocksForThisChunk);
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
        this.world = world;
        this.initializeStructureData(world);
        generateForChunkFast(world, new ChunkPos(chunkX, chunkZ));
    }

    @Override
    public String getStructureName() {
        return STRUCTURE_NAME;
    }

    @Nullable
    @Override
    public BlockPos getNearestStructurePos(World worldIn, BlockPos pos, boolean findUnexplored) {
        return null;
    }

    @Override
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
        return false;
    }

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ) {
        return new PhasedStructureStart();
    }

    @Override
    public boolean isInsideStructure(BlockPos pos) {
//        if (this.world == null) {
//            return false;
//        }
//        this.initializeStructureData(this.world);
//        return this.getStructureAt(pos) != null;
        return false;
    }

    @Override
    @Nullable
    protected StructureStart getStructureAt(BlockPos pos) {
//        int cx = pos.getX() >> 4;
//        int cz = pos.getZ() >> 4;
//        long key = ChunkPos.asLong(cx, cz);
//
//        ObjectArrayList<PhasedStructureComponent> list = componentsByChunk.get(key);
//        if (list == null || list.isEmpty()) {
//            return null;
//        }
//
//        for (PhasedStructureComponent comp : list) {
//            if (comp == null) continue;
//            PhasedStructureStart parent = comp.parent;
//            if (parent == null) continue;
//            if (!parent.isSizeableStructure()) continue;
//            if (!parent.getBoundingBox().isVecInside(pos)) continue;
//            if (comp.getBoundingBox().isVecInside(pos)) {
//                return parent;
//            }
//        }

        return null;
    }

    @Override
    public boolean isPositionInStructure(World worldIn, BlockPos pos) {
//        this.world = worldIn;
//        this.initializeStructureData(worldIn);
//        int cx = pos.getX() >> 4;
//        int cz = pos.getZ() >> 4;
//        long key = ChunkPos.asLong(cx, cz);
//
//        ObjectArrayList<PhasedStructureComponent> list = componentsByChunk.get(key);
//        if (list == null || list.isEmpty()) return false;
//
//        for (PhasedStructureComponent comp : list) {
//            if (comp == null) continue;
//            PhasedStructureStart parent = comp.parent;
//            if (parent == null) continue;
//
//            if (parent.isSizeableStructure() && parent.getBoundingBox().isVecInside(pos)) {
//                return true;
//            }
//        }

        return false;
    }

    void scheduleStructureForValidation(World world, BlockPos origin, IPhasedStructure structure, Long2ObjectOpenHashMap<List<BlockInfo>> layout,
                                        long layoutSeed) {
        this.world = world;
        registerStructure(structure);

        boolean allowEmptyLayout = false;
        if (structure instanceof AbstractPhasedStructure phased) {
            List<BlockPos> validationPoints = structure.getValidationPoints(origin);
            List<ChunkPos> additionalChunks = structure.getAdditionalChunks(origin);
            boolean hasValidation = !validationPoints.isEmpty();
            boolean hasAdditionalChunks = additionalChunks != null && !additionalChunks.isEmpty();
            allowEmptyLayout = !phased.isCacheable() && (hasValidation || hasAdditionalChunks);
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

        PhasedStructureStart start = new PhasedStructureStart(ready);
        long key = ChunkPos.asLong(start.getChunkPosX(), start.getChunkPosZ());
        this.structureMap.put(key, start);

        this.initializeStructureData(world);
        try {
            MH_SET_STRUCTURE_START.invokeExact((MapGenStructure) this, start.getChunkPosX(), start.getChunkPosZ(), (StructureStart) start);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to persist phased structure start", t);
        }
    }

    private void registerComponent(ChunkPos chunkPos, PhasedStructureComponent component) {
        long key = ChunkPos.asLong(chunkPos.x, chunkPos.z);
        ObjectArrayList<PhasedStructureComponent> list = componentsByChunk.get(key);
        if (list == null) {
            list = new ObjectArrayList<>(1);
            componentsByChunk.put(key, list);
        }
        list.add(component);
    }

    private void onChunkProcessed(PhasedStructureStart start, ChunkPos chunkPos) {
        long key = ChunkPos.asLong(chunkPos.x, chunkPos.z);
//        ObjectArrayList<PhasedStructureComponent> list = componentsByChunk.get(key);
//        if (list == null || list.isEmpty()) {
//            return;
//        }
//
//        for (int i = 0; i < list.size(); ) {
//            PhasedStructureComponent comp = list.get(i);
//            if (comp == null || comp.parent != start) {
//                i++;
//            } else {
//                list.remove(i);
//            }
//        }
//
//        if (list.isEmpty()) {
            componentsByChunk.remove(key);
//        }
    }

    private void generateForChunkFast(World world, ChunkPos chunkPos) {
        long key = ChunkPos.asLong(chunkPos.x, chunkPos.z);
        ObjectArrayList<PhasedStructureComponent> list = componentsByChunk.get(key);
        if (list == null || list.isEmpty()) return;
        list = list.clone();
        for (PhasedStructureComponent comp : list) {
            if (comp == null) continue;
            PhasedStructureStart parent = comp.parent;
            if (parent == null) continue;
            if (!parent.isValidForPostProcess(chunkPos)) {
                continue;
            }
            comp.generateNow(world);
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

    public static class PhasedStructureStart extends StructureStart {
        private final LongOpenHashSet remainingChunks = new LongOpenHashSet();
        private final LongOpenHashSet processedChunks = new LongOpenHashSet();
        private IPhasedStructure structure;
        private String structureId;
        private BlockPos finalOrigin = BlockPos.ORIGIN;
        private long worldSeed;
        private long layoutSeed;
        private Random structureRand = new Random();
        private Long2ObjectOpenHashMap<List<BlockInfo>> layout;
        private boolean postGenerated;
        private World cachedWorld;

        @SuppressWarnings("WeakerAccess")
        public PhasedStructureStart() {
            super();
        }

        PhasedStructureStart(ReadyToGenerateStructure ready) {
            super(ready.finalOrigin.getX() >> 4, ready.finalOrigin.getZ() >> 4);
            this.structure = ready.pending.structure;
            this.structureId = structure.getId();
            this.finalOrigin = ready.finalOrigin;
            this.worldSeed = ready.pending.worldSeed;
            this.layoutSeed = ready.pending.layoutSeed;
            this.structureRand = ready.structureRand;
            this.layout = ready.pending.layout;
            this.postGenerated = false;

            buildComponentsFromLayout();
            generateExistingChunks();
        }

        private void buildComponentsFromLayout() {
            ensureLayout();
            remainingChunks.clear();
            this.components = new ArrayList<>();

            int originChunkX = this.finalOrigin.getX() >> 4;
            int originChunkZ = this.finalOrigin.getZ() >> 4;

            ObjectIterator<Long2ObjectMap.Entry<List<BlockInfo>>> iterator = this.layout.long2ObjectEntrySet().fastIterator();
            while (iterator.hasNext()) {
                Long2ObjectMap.Entry<List<BlockInfo>> entry = iterator.next();
                long relKey = entry.getLongKey();
                List<BlockInfo> blocksForThisChunk = entry.getValue();

                int relChunkX = ChunkUtil.getChunkPosX(relKey);
                int relChunkZ = ChunkUtil.getChunkPosZ(relKey);
                int absChunkX = originChunkX + relChunkX;
                int absChunkZ = originChunkZ + relChunkZ;

                ChunkPos chunkPos = new ChunkPos(absChunkX, absChunkZ);
                int[] minMax = computeHeightBounds(blocksForThisChunk);
                PhasedStructureComponent component = new PhasedStructureComponent(this, relChunkX, relChunkZ, chunkPos, minMax[0], minMax[1], blocksForThisChunk);
                this.components.add(component);
                this.remainingChunks.add(ChunkPos.asLong(absChunkX, absChunkZ));
                PhasedStructureGenerator.INSTANCE.registerComponent(chunkPos, component);
            }

            if (this.structure != null) {
                List<ChunkPos> extras = this.structure.getAdditionalChunks(this.finalOrigin);
                if (extras != null && !extras.isEmpty()) {
                    for (ChunkPos extra : extras) {
                        long key = ChunkPos.asLong(extra.x, extra.z);
                        if (this.remainingChunks.contains(key)) continue;
                        int relX = extra.x - originChunkX;
                        int relZ = extra.z - originChunkZ;
                        PhasedStructureComponent marker = new PhasedStructureComponent(this, relX, relZ, extra, this.finalOrigin.getY(), this.finalOrigin.getY(), true);
                        this.components.add(marker);
                        this.remainingChunks.add(key);
                        PhasedStructureGenerator.INSTANCE.registerComponent(extra, marker);
                    }
                }
            }
            this.updateBoundingBox();
        }

        private int[] computeHeightBounds(List<BlockInfo> blocks) {
            int minY = this.finalOrigin.getY();
            int maxY = this.finalOrigin.getY();
            if (blocks != null) {
                for (BlockInfo info : blocks) {
                    int y = this.finalOrigin.getY() + info.relativePos.getY();
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                }
            }
            return new int[]{minY, maxY};
        }

        private void ensureLayout() {
            if (this.layout != null) return;
            if (this.structure instanceof AbstractPhasedStructure abstractPhasedStructure) {
                this.layout = abstractPhasedStructure.buildLayout(this.finalOrigin, this.layoutSeed);
            } else {
                this.layout = new Long2ObjectOpenHashMap<>();
            }
        }

        @Nullable List<BlockInfo> getBlocksFor(int relChunkX, int relChunkZ) {
            ensureLayout();
            long key = ChunkPos.asLong(relChunkX, relChunkZ);
            return this.layout.get(key);
        }

        @Override
        public void generateStructure(World worldIn, Random rand, StructureBoundingBox structurebb) {
            this.cachedWorld = worldIn;
            ensureLayout();
            super.generateStructure(worldIn, this.structureRand, structurebb);
        }

        @Override
        public boolean isValidForPostProcess(ChunkPos pair) {
            return !processedChunks.contains(ChunkPos.asLong(pair.x, pair.z));
        }

        @Override
        public void notifyPostProcessAt(ChunkPos pair) {
            long key = ChunkPos.asLong(pair.x, pair.z);
            processedChunks.add(key);
            remainingChunks.remove(key);
            PhasedStructureGenerator.INSTANCE.onChunkProcessed(this, pair);
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
        }

        void markGenerated(ChunkPos pos) {
            notifyPostProcessAt(pos);
        }

        @Override
        public void writeToNBT(NBTTagCompound nbt) {
            super.writeToNBT(nbt);
            nbt.setString("StructureId", structureId == null ? "" : structureId);
            nbt.setLong("WorldSeed", worldSeed);
            nbt.setLong("LayoutSeed", layoutSeed);
            nbt.setInteger("OriginX", finalOrigin.getX());
            nbt.setInteger("OriginY", finalOrigin.getY());
            nbt.setInteger("OriginZ", finalOrigin.getZ());
            nbt.setTag("Remaining", new NBTTagLongArray(remainingChunks.toLongArray()));
            nbt.setTag("Processed", new NBTTagLongArray(processedChunks.toLongArray()));
            nbt.setBoolean("PostGenerated", postGenerated);
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            super.readFromNBT(nbt);
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
                for (long key : remaining) {
                    this.remainingChunks.add(key);
                }
            }

            this.processedChunks.clear();
            if (nbt.hasKey("Processed")) {
                long[] processed = ((NBTTagLongArray) nbt.getTag("Processed")).data;
                for (long key : processed) {
                    this.processedChunks.add(key);
                }
            }
            this.postGenerated = nbt.getBoolean("PostGenerated");

            ensureLayout();

            for (StructureComponent component : this.components) {
                if (component instanceof PhasedStructureComponent phased) {
                    phased.attachParent(this);
                    ChunkPos pos = phased.getChunkPos();
                    long key = ChunkPos.asLong(pos.x, pos.z);

                    if (this.remainingChunks.contains(key)) {
                        PhasedStructureGenerator.INSTANCE.registerComponent(pos, phased);
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
            IChunkProvider provider = world.getChunkProvider();
            for (StructureComponent component : this.components) {
                if (!(component instanceof PhasedStructureComponent phased)) continue;
                ChunkPos chunkPos = phased.getChunkPos();
                if (provider.isChunkGeneratedAt(chunkPos.x, chunkPos.z)) {
                    phased.generateNow(world);
                }
            }
        }
    }

    public static class PhasedStructureComponent extends StructureComponent {
        PhasedStructureStart parent;
        private int relChunkX;
        private int relChunkZ;
        private List<BlockInfo> blocks;
        private boolean generated;
        private boolean markerOnly;

        @SuppressWarnings("unused")
        public PhasedStructureComponent() {
            super(0);
        }

        PhasedStructureComponent(PhasedStructureStart parent, int relChunkX, int relChunkZ, ChunkPos chunkPos, int minY, int maxY,
                                 List<BlockInfo> blocks) {
            super(0);
            this.parent = parent;
            this.relChunkX = relChunkX;
            this.relChunkZ = relChunkZ;
            this.blocks = blocks;
            this.markerOnly = false;
            this.boundingBox = new StructureBoundingBox(chunkPos.x << 4, minY, chunkPos.z << 4, (chunkPos.x << 4) + 15, maxY, (chunkPos.z << 4) + 15);
        }

        PhasedStructureComponent(PhasedStructureStart parent, int relChunkX, int relChunkZ, ChunkPos chunkPos, int minY, int maxY,
                                 boolean markerOnly) {
            super(0);
            this.parent = parent;
            this.relChunkX = relChunkX;
            this.relChunkZ = relChunkZ;
            this.markerOnly = markerOnly;
            this.boundingBox = new StructureBoundingBox(chunkPos.x << 4, minY, chunkPos.z << 4, (chunkPos.x << 4) + 15, maxY, (chunkPos.z << 4) + 15);
        }

        void attachParent(PhasedStructureStart start) {
            this.parent = start;
            if (!markerOnly && (this.blocks == null || this.blocks.isEmpty())) {
                List<BlockInfo> rebuilt = start.getBlocksFor(relChunkX, relChunkZ);
                if (rebuilt != null) {
                    this.blocks = rebuilt;
                }
            }
            ChunkPos chunkPos = new ChunkPos(start.getChunkPosX() + relChunkX, start.getChunkPosZ() + relChunkZ);
            int minY = start.finalOrigin.getY();
            int maxY = start.finalOrigin.getY();
            if (!markerOnly && this.blocks != null && !this.blocks.isEmpty()) {
                int[] minMax = start.computeHeightBounds(this.blocks);
                minY = minMax[0];
                maxY = minMax[1];
            } else if (this.boundingBox != null) {
                minY = this.boundingBox.minY;
                maxY = this.boundingBox.maxY;
            }
            this.boundingBox = new StructureBoundingBox(chunkPos.x << 4, minY, chunkPos.z << 4, (chunkPos.x << 4) + 15, maxY, (chunkPos.z << 4) + 15);

            long key = ChunkPos.asLong(chunkPos.x, chunkPos.z);
            if (start.remainingChunks.contains(key)) {
                PhasedStructureGenerator.INSTANCE.registerComponent(chunkPos, this);
            }
        }

        ChunkPos getChunkPos() {
            int absX = (parent != null ? parent.getChunkPosX() : 0) + relChunkX;
            int absZ = (parent != null ? parent.getChunkPosZ() : 0) + relChunkZ;
            return new ChunkPos(absX, absZ);
        }

        @Override
        protected void writeStructureToNBT(NBTTagCompound nbt) {
            nbt.setInteger("RelX", relChunkX);
            nbt.setInteger("RelZ", relChunkZ);
            int minY = this.boundingBox != null ? this.boundingBox.minY : (parent != null ? parent.finalOrigin.getY() : 0);
            int maxY = this.boundingBox != null ? this.boundingBox.maxY : minY;
            nbt.setInteger("MinY", minY);
            nbt.setInteger("MaxY", maxY);
            nbt.setBoolean("Marker", markerOnly);
        }

        @Override
        protected void readStructureFromNBT(NBTTagCompound nbt, TemplateManager templateManager) {
            this.relChunkX = nbt.getInteger("RelX");
            this.relChunkZ = nbt.getInteger("RelZ");
            int minY = nbt.getInteger("MinY");
            int maxY = nbt.getInteger("MaxY");
            this.boundingBox = new StructureBoundingBox(0, minY, 0, 0, maxY, 0);
            this.markerOnly = nbt.getBoolean("Marker");
        }

        void generateNow(World worldIn) {
            if (generated) return;

            if (parent != null) {
                ChunkPos chunkPos = getChunkPos();
                if (!parent.isValidForPostProcess(chunkPos)) {
                    generated = true;
                    return;
                }
            }

            generateInternal(worldIn);
        }

        private void generateInternal(World worldIn) {
            if (parent == null || parent.structure == null) return;
            if (markerOnly) {
                generated = true;
                parent.markGenerated(getChunkPos());
                return;
            }
            if (this.blocks == null || this.blocks.isEmpty()) {
                List<BlockInfo> rebuilt = parent.getBlocksFor(relChunkX, relChunkZ);
                if (rebuilt != null) this.blocks = rebuilt;
            }
            if (this.blocks == null || this.blocks.isEmpty()) return;

            ChunkPos chunkPos = getChunkPos();
            try {
                parent.structure.generateForChunk(worldIn, parent.structureRand, parent.finalOrigin, chunkPos, this.blocks);
                generated = true;
                parent.markGenerated(chunkPos);
            } catch (Exception e) {
                MainRegistry.logger.error("Error generating phased structure part at {}", chunkPos, e);
            }
        }

        @Override
        public boolean addComponentParts(World worldIn, Random rand, StructureBoundingBox box) {
            if (generated) return true;
            generateInternal(worldIn);
            return true;
        }
    }
}
