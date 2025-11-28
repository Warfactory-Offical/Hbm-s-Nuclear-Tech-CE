package com.hbm.world.phased;

import com.hbm.config.GeneralConfig;
import com.hbm.main.MainRegistry;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
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

import java.util.*;

/**
 * Base class for all phased structures.
 */
public abstract class AbstractPhasedStructure extends WorldGenerator implements IPhasedStructure {
    private static final Map<Class<? extends AbstractPhasedStructure>, Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<List<BlockInfo>>>> STRUCTURE_CACHE = new IdentityHashMap<>();

    protected AbstractPhasedStructure() {
        super();
        PhasedStructureGenerator.INSTANCE.registerStructure(this);
    }

    private static long anchorKey(int anchorX, int anchorZ) {
        int ax = anchorX & 15;
        int az = anchorZ & 15;
        return (((long) ax) << 32) | (az & 0xFFFF_FFFFL);
    }

    /**
     * Static part. Leave this empty if the whole structure is dynamic.
     */
    protected abstract void buildStructure(@NotNull LegacyBuilder builder, @NotNull Random rand);

    /**
     * @return false if the structure is not completely static.
     */
    protected boolean isCacheable() {
        return true;
    }

    protected int getGenerationHeightOffset() {
        return 0;
    }

    @Override
    public final boolean generate(@NotNull World world, @NotNull Random rand, @NotNull BlockPos pos) {
        return this.generate(world, rand, pos, false);
    }

    public final boolean generate(@NotNull World world, @NotNull Random rand, @NotNull BlockPos pos, boolean force) {
        BlockPos origin = pos.add(0, getGenerationHeightOffset(), 0);
        long layoutSeed = rand.nextLong();
        Long2ObjectOpenHashMap<List<BlockInfo>> layout = buildLayout(origin, layoutSeed);

        if (force) {
            if (GeneralConfig.enableDebugWorldGen) MainRegistry.logger.info("Forcing {} generation at {}", this.getClass().getSimpleName(), origin);
            PhasedStructureGenerator.forceGenerateStructure(world, rand, origin, this, layout);
        } else {
            if (GeneralConfig.enableDebugWorldGen) MainRegistry.logger.info("Proposing {} generation at {}", this.getClass().getSimpleName(), origin);
            PhasedStructureGenerator.INSTANCE.scheduleStructureForValidation(world, origin, this, layout, layoutSeed);
        }
        return true;
    }

    protected Long2ObjectOpenHashMap<List<BlockInfo>> buildLayout(BlockPos origin, long layoutSeed) {
        int anchorX = origin.getX() & 15;
        int anchorZ = origin.getZ() & 15;
        long aKey = anchorKey(anchorX, anchorZ);

        if (this.isCacheable()) {
            Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<List<BlockInfo>>> byAnchor =
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

    private static Long2ObjectOpenHashMap<List<BlockInfo>> chunkTheLayout(Map<BlockPos, BlockInfo> blocks, int anchorX, int anchorZ) {
        Long2ObjectOpenHashMap<List<BlockInfo>> chunkedMap = new Long2ObjectOpenHashMap<>();
        for (BlockInfo info : blocks.values()) {
            int localX = anchorX + info.relativePos.getX();
            int localZ = anchorZ + info.relativePos.getZ();

            int relChunkX = localX >> 4;
            int relChunkZ = localZ >> 4;

            long relChunkKey = ChunkPos.asLong(relChunkX, relChunkZ);
            List<BlockInfo> list = chunkedMap.get(relChunkKey);
            if (list == null) {
                list = new ArrayList<>();
                chunkedMap.put(relChunkKey, list);
            }
            list.add(info);
        }
        return chunkedMap;
    }

    @Override
    public final void generateForChunk(@NotNull World world, @NotNull Random rand, @NotNull BlockPos structureOrigin,
                                       @NotNull ChunkPos chunkToGenerate, @Nullable List<BlockInfo> blocksForThisChunk) {
        if (blocksForThisChunk == null || blocksForThisChunk.isEmpty()) return;

        List<BlockInfo> teInfos = new ArrayList<>();
        for (BlockInfo info : blocksForThisChunk) {
            BlockPos worldPos = structureOrigin.add(info.relativePos);
            world.setBlockState(worldPos, info.state, 2 | 16);

            if (info.tePopulator != null) {
                teInfos.add(info);
            }
        }

        for (BlockInfo info : teInfos) {
            BlockPos worldPos = structureOrigin.add(info.relativePos);
            TileEntity te = world.getTileEntity(worldPos);
            if (te != null) {
                try {
                    info.tePopulator.populate(world, rand, worldPos, te);
                } catch (ClassCastException e) { // mlbv: just in case, I used force cast several times
                    MainRegistry.logger.error("WorldGen found incompatible TileEntity type in dimension {} at {}, this is a bug!", world.provider.getDimension(), worldPos, e);
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
    public static class BlockInfo {
        @NotNull
        final BlockPos relativePos;
        @NotNull
        final IBlockState state;
        @Nullable
        final TileEntityPopulator tePopulator;

        BlockInfo(@NotNull BlockPos relativePos, @NotNull IBlockState state, @Nullable TileEntityPopulator tePopulator) {
            this.relativePos = relativePos;
            this.state = state;
            this.tePopulator = tePopulator;
        }
    }

    public class LegacyBuilder {
        public final Random rand;
        private final Map<BlockPos, BlockInfo> blocks = new HashMap<>();

        public LegacyBuilder(Random rand) {
            this.rand = rand;
        }

        public void setBlockState(@NotNull BlockPos pos, @NotNull IBlockState state, int ignored) {
            setBlockState(pos, state, null);
        }

        public void setBlockState(@NotNull BlockPos pos, @NotNull IBlockState state) {
            setBlockState(pos, state, null);
        }

        public void setBlockState(@NotNull BlockPos pos, @NotNull IBlockState state, @Nullable TileEntityPopulator populator) {
            BlockPos immutable = pos.toImmutable();
            blocks.put(immutable, new BlockInfo(immutable, state, populator));
        }

        @NotNull
        @ApiStatus.Experimental
        public IBlockState getBlockState(@NotNull BlockPos pos) {
            BlockInfo info = blocks.get(pos);
            if (info != null) return info.state;
            if (GeneralConfig.enableDebugWorldGen) {
                MainRegistry.logger.warn("Structure {} tried to retrieve non-existent BlockState at relative {}",
                        AbstractPhasedStructure.this.getClass().getSimpleName(), pos);
            }
            return Blocks.AIR.getDefaultState();
        }

        public void setBlockToAir(@NotNull BlockPos pos) {
            this.setBlockState(pos, Blocks.AIR.getDefaultState());
        }

        @NotNull
        public Random getRandom() {
            return this.rand;
        }

        @NotNull
        private Map<BlockPos, BlockInfo> getBlocks() {
            return blocks;
        }

        public void placeDoorWithoutCheck(@NotNull BlockPos pos, @NotNull EnumFacing facing, @NotNull Block door, boolean isRightHinge,
                                          boolean isOpen) {
            BlockDoor.EnumHingePosition hinge = isRightHinge ? BlockDoor.EnumHingePosition.RIGHT : BlockDoor.EnumHingePosition.LEFT;
            IBlockState baseState = door.getDefaultState().withProperty(BlockDoor.FACING, facing).withProperty(BlockDoor.HINGE, hinge)
                                        .withProperty(BlockDoor.POWERED, Boolean.FALSE).withProperty(BlockDoor.OPEN, isOpen);
            BlockPos immutable = pos.toImmutable();
            this.setBlockState(immutable, baseState.withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.LOWER));
            this.setBlockState(immutable.up(), baseState.withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER));
        }

        public void placeDoorWithoutCheck(@NotNull BlockPos pos, @NotNull EnumFacing facing, @NotNull Block door, boolean isRightHinge) {
            placeDoorWithoutCheck(pos, facing, door, isRightHinge, false);
        }
    }
}
