package com.hbm.world.phased;

import com.hbm.config.GeneralConfig;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Random;

/**
 * Base class for all phased structures.
 */
public abstract class AbstractPhasedStructure extends WorldGenerator implements IPhasedStructure {
    protected final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(); // use this, assuming no mod would ever thread worldgen
    private static final Map<Class<? extends AbstractPhasedStructure>, Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<BlockInfo>>>> STRUCTURE_CACHE = new IdentityHashMap<>();

    private static long anchorKey(int anchorX, int anchorZ) {
        int ax = anchorX & 15;
        int az = anchorZ & 15;
        return (((long) ax) << 32) | (az & 0xFFFF_FFFFL);
    }

    /**
     * Static part. Leave this empty and override {@link #useDynamicScheduler()} if the whole structure is completely dynamic.
     */
    protected void buildStructure(@NotNull LegacyBuilder builder, @NotNull Random rand) {
    }

    /**
     * @return false if the structure is not completely static, e.g. random is used.
     */
    protected boolean isCacheable() {
        return true;
    }

    /**
     * Override to route generation through {@link DynamicStructureDispatcher} instead of {@link PhasedStructureGenerator}.
     */
    protected boolean useDynamicScheduler() {
        return false;
    }

    protected int getGenerationHeightOffset() {
        return 0;
    }

    @Override
    public final boolean generate(@NotNull World world, @NotNull Random rand, @NotNull BlockPos pos) {
        return this.generate(world, rand, pos, false);
    }

    // common logic used by a lot of legacy structures
    protected boolean locationIsValidSpawn(World world, long serialized) {
        int x = Library.getBlockPosX(serialized);
        int y = Library.getBlockPosY(serialized);
        int z = Library.getBlockPosZ(serialized);
        BlockPos.MutableBlockPos pos = this.mutablePos;
        IBlockState checkBlockState = world.getBlockState(pos.setPos(x, y - 1, z));
        Block checkBlock = checkBlockState.getBlock();
        IBlockState stateAbove = world.getBlockState(pos.setPos(x, y, z));
        Block blockBelow = world.getBlockState(pos.setPos(x, y - 2, z)).getBlock();

        if (!stateAbove.getBlock().isAir(stateAbove, world, pos.setPos(x, y, z))) {
            return false;
        }
        if (isValidSpawnBlock(checkBlock)) {
            return true;
        } else if (checkBlock == Blocks.SNOW_LAYER && isValidSpawnBlock(blockBelow)) {
            return true;
        } else return checkBlockState.getMaterial() == Material.PLANTS && isValidSpawnBlock(blockBelow);
    }

    protected boolean isValidSpawnBlock(Block block) {
        return block == Blocks.GRASS || block == Blocks.DIRT || block == Blocks.STONE || block == Blocks.SAND;
    }

    //TODO: make it return false if generation failed
    public final boolean generate(@NotNull World world, @NotNull Random rand, @NotNull BlockPos pos, boolean force) {
        int ox = pos.getX();
        int oy = pos.getY() + getGenerationHeightOffset();
        int oz = pos.getZ();
        long originSerialized = Library.blockPosToLong(ox, oy, oz);
        long layoutSeed = rand.nextLong();

        if (useDynamicScheduler()) {
            if (force) {
                if (GeneralConfig.enableDebugWorldGen) MainRegistry.logger.info("Forcing dynamic {} generation at {}", this.getClass().getSimpleName(), originSerialized);
                DynamicStructureDispatcher.INSTANCE.forceGenerate(world, rand, originSerialized, this);
            } else {
                if (GeneralConfig.enableDebugWorldGen) MainRegistry.logger.info("Proposing dynamic {} generation at {}", this.getClass().getSimpleName(), originSerialized);
                DynamicStructureDispatcher.INSTANCE.schedule(world, originSerialized, this, layoutSeed);
            }
            return true;
        }

        Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<BlockInfo>> layout = buildLayout(originSerialized, layoutSeed);

        if (force) {
            if (GeneralConfig.enableDebugWorldGen) MainRegistry.logger.info("Forcing {} generation at {}", this.getClass().getSimpleName(), originSerialized);
            PhasedStructureGenerator.forceGenerateStructure(world, rand, originSerialized, this, layout);
        } else {
            if (GeneralConfig.enableDebugWorldGen) MainRegistry.logger.info("Proposing {} generation at {}", this.getClass().getSimpleName(), originSerialized);
            PhasedStructureGenerator.scheduleStructureForValidation(world, originSerialized, this, layout, layoutSeed);
        }
        return true;
    }

    final Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<BlockInfo>> buildLayout(long originSerialized, long layoutSeed) {
        int anchorX = Library.getBlockPosX(originSerialized) & 15;
        int anchorZ = Library.getBlockPosZ(originSerialized) & 15;
        long aKey = anchorKey(anchorX, anchorZ);

        if (this.isCacheable()) {
            Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<BlockInfo>>> byAnchor =
                    STRUCTURE_CACHE.computeIfAbsent(this.getClass(), k -> new Long2ObjectOpenHashMap<>());
            return byAnchor.computeIfAbsent(aKey, k -> {
                LegacyBuilder staticBuilder = new LegacyBuilder(new Random(this.getClass().getName().hashCode()));
                this.buildStructure(staticBuilder, staticBuilder.rand);
                return chunkTheLayout(staticBuilder.getBlocks(), anchorX, anchorZ);
            });
        }

        LegacyBuilder dynamicBuilder = new LegacyBuilder(new Random(layoutSeed));
        this.buildStructure(dynamicBuilder, dynamicBuilder.rand);
        return chunkTheLayout(dynamicBuilder.getBlocks(), anchorX, anchorZ);
    }

    private static Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<BlockInfo>> chunkTheLayout(Long2ObjectOpenHashMap<BlockInfo> blocks, int anchorX, int anchorZ) {
        Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<BlockInfo>> chunkedMap = new Long2ObjectOpenHashMap<>();
        ObjectIterator<Long2ObjectMap.Entry<BlockInfo>> iterator = blocks.long2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Long2ObjectMap.Entry<BlockInfo> entry = iterator.next();
            long key = entry.getLongKey();
            BlockInfo info = entry.getValue();
            int localX = anchorX + Library.getBlockPosX(key);
            int localZ = anchorZ + Library.getBlockPosZ(key);

            int relChunkX = localX >> 4;
            int relChunkZ = localZ >> 4;

            long relChunkKey = ChunkPos.asLong(relChunkX, relChunkZ);
            Long2ObjectOpenHashMap<BlockInfo> chunk = chunkedMap.get(relChunkKey);
            if (chunk == null) {
                chunk = new Long2ObjectOpenHashMap<>();
                chunkedMap.put(relChunkKey, chunk);
            }
            chunk.put(key, info);
        }
        return chunkedMap;
    }

    protected static LongArrayList collectChunkOffsetsByRadius(int horizontalRadius) {
        int chunkRadius = Math.max(0, Math.floorDiv(Math.max(0, horizontalRadius) + 15, 16));
        LongArrayList offsets = new LongArrayList((chunkRadius * 2 + 1) * (chunkRadius * 2 + 1));
        for (int cx = -chunkRadius; cx <= chunkRadius; cx++) {
            for (int cz = -chunkRadius; cz <= chunkRadius; cz++) {
                offsets.add(ChunkPos.asLong(cx, cz));
            }
        }
        return offsets;
    }

    @Override
    public final void generateForChunk(@NotNull World world, @NotNull Random rand, long structureOrigin,
                                       int chunkX, int chunkZ, @Nullable Long2ObjectOpenHashMap<BlockInfo> blocksForThisChunk) {
        if (blocksForThisChunk == null || blocksForThisChunk.isEmpty()) return;

        ObjectIterator<Long2ObjectMap.Entry<BlockInfo>> iterator = blocksForThisChunk.long2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Long2ObjectMap.Entry<BlockInfo> entry = iterator.next();
            long key = entry.getLongKey();
            BlockInfo info = entry.getValue();
            mutablePos.setPos(Library.getBlockPosX(structureOrigin) + Library.getBlockPosX(key),
                    Library.getBlockPosY(structureOrigin) + Library.getBlockPosY(key),
                    Library.getBlockPosZ(structureOrigin) + Library.getBlockPosZ(key));
            world.setBlockState(mutablePos, info.state, 2 | 16);

            if (info.tePopulator != null) {
                TileEntity te = world.getTileEntity(mutablePos);
                if (te != null) {
                    try {
                        info.tePopulator.populate(world, rand, mutablePos, te);
                    } catch (ClassCastException e) { // mlbv: just in case, I used force cast several times
                        MainRegistry.logger.error("WorldGen found incompatible TileEntity type in dimension {} at {}, this is a bug!", world.provider.getDimension(), mutablePos.toImmutable(), e);
                    }
                }
            }
        }
    }

    @FunctionalInterface
    public interface TileEntityPopulator {
        void populate(@NotNull World worldIn, @NotNull Random random, @NotNull BlockPos blockPos, @NotNull TileEntity chest);
    }

    /**
     * A custom block info class, containing all the information needed to generate a block
     */
    public record BlockInfo(@NotNull IBlockState state, @Nullable TileEntityPopulator tePopulator) {
    }

    public class LegacyBuilder {
        public final Random rand;
        private final Long2ObjectOpenHashMap<BlockInfo> blocks = new Long2ObjectOpenHashMap<>();

        public LegacyBuilder(Random rand) {
            this.rand = rand;
        }

        public void setBlockState(int x, int y, int z, @NotNull IBlockState state) {
            setBlockState(x, y, z, state, null);
        }

        public void setBlockState(int x, int y, int z, @NotNull IBlockState state, @Nullable TileEntityPopulator populator) {
            long key = Library.blockPosToLong(x, y, z);
            blocks.put(key, new BlockInfo(state, populator));
        }

        public void setBlockState(@NotNull BlockPos pos, @NotNull IBlockState state, int ignored) {
            setBlockState(pos, state, null);
        }

        public void setBlockState(@NotNull BlockPos pos, @NotNull IBlockState state) {
            setBlockState(pos, state, null);
        }

        public void setBlockState(@NotNull BlockPos pos, @NotNull IBlockState state, @Nullable TileEntityPopulator populator) {
            BlockPos immutable = pos.toImmutable();
            long key = Library.blockPosToLong(immutable.getX(), immutable.getY(), immutable.getZ());
            blocks.put(key, new BlockInfo(state, populator));
        }

        @NotNull
        @ApiStatus.Experimental
        public IBlockState getBlockState(int x, int y, int z) {
            long key = Library.blockPosToLong(x, y, z);
            BlockInfo info = blocks.get(key);
            if (info != null) return info.state;
            if (GeneralConfig.enableDebugWorldGen) {
                MainRegistry.logger.warn("Structure {} tried to retrieve non-existent BlockState at relative ({}, {}, {})",
                        AbstractPhasedStructure.this.getClass().getSimpleName(), x, y, z);
            }
            return Blocks.AIR.getDefaultState();
        }

        @NotNull
        @ApiStatus.Experimental
        public IBlockState getBlockState(@NotNull BlockPos pos) {
            long key = Library.blockPosToLong(pos.getX(), pos.getY(), pos.getZ());
            BlockInfo info = blocks.get(key);
            if (info != null) return info.state;
            if (GeneralConfig.enableDebugWorldGen) {
                MainRegistry.logger.warn("Structure {} tried to retrieve non-existent BlockState at relative {}",
                        AbstractPhasedStructure.this.getClass().getSimpleName(), pos);
            }
            return Blocks.AIR.getDefaultState();
        }

        public void setBlockToAir(int x, int y, int z) {
            this.setBlockState(x, y, z, Blocks.AIR.getDefaultState());
        }

        public void setBlockToAir(@NotNull BlockPos pos) {
            this.setBlockState(pos, Blocks.AIR.getDefaultState());
        }

        @NotNull
        public Random getRandom() {
            return this.rand;
        }

        @NotNull
        private Long2ObjectOpenHashMap<BlockInfo> getBlocks() {
            return blocks;
        }

        public void placeDoorWithoutCheck(int x, int y, int z, @NotNull EnumFacing facing, @NotNull Block door, boolean isRightHinge,
                                          boolean isOpen) {
            BlockDoor.EnumHingePosition hinge = isRightHinge ? BlockDoor.EnumHingePosition.RIGHT : BlockDoor.EnumHingePosition.LEFT;
            IBlockState baseState = door.getDefaultState().withProperty(BlockDoor.FACING, facing).withProperty(BlockDoor.HINGE, hinge)
                    .withProperty(BlockDoor.POWERED, Boolean.FALSE).withProperty(BlockDoor.OPEN, isOpen);
            this.setBlockState(x, y, z, baseState.withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.LOWER));
            this.setBlockState(x, y + 1, z, baseState.withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER));
        }

        public void placeDoorWithoutCheck(int x, int y, int z, @NotNull EnumFacing facing, @NotNull Block door, boolean isRightHinge) {
            placeDoorWithoutCheck(x, y, z, facing, door, isRightHinge, false);
        }

        public void placeDoorWithoutCheck(@NotNull BlockPos pos, @NotNull EnumFacing facing, @NotNull Block door, boolean isRightHinge,
                                          boolean isOpen) {
            placeDoorWithoutCheck(pos.getX(), pos.getY(), pos.getZ(), facing, door, isRightHinge, isOpen);
        }

        public void placeDoorWithoutCheck(@NotNull BlockPos pos, @NotNull EnumFacing facing, @NotNull Block door, boolean isRightHinge) {
            placeDoorWithoutCheck(pos, facing, door, isRightHinge, false);
        }
    }
}
