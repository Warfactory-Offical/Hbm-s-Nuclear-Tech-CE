package com.hbm.world.phased;

import com.hbm.main.MainRegistry;
import com.hbm.util.ChunkUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Minimal dispatcher for post-generate-only structures that would otherwise overwhelm
 * {@link PhasedStructureGenerator} with marker tasks.
 */
public class DynamicStructureDispatcher {

    public static final DynamicStructureDispatcher INSTANCE = new DynamicStructureDispatcher();

    private static final LongArrayList ORIGIN_ONLY = LongArrayList.wrap(new long[]{ChunkPos.asLong(0, 0)});
    private static final Long2ObjectOpenHashMap<List<AbstractPhasedStructure.BlockInfo>> EMPTY_LAYOUT = new Long2ObjectOpenHashMap<>(0);

    private final Int2ObjectOpenHashMap<WorldState> states = new Int2ObjectOpenHashMap<>();

    private DynamicStructureDispatcher() {
    }

    public void schedule(@NotNull World world, @NotNull BlockPos origin, @NotNull AbstractPhasedStructure structure, long layoutSeed) {
        if (!(world instanceof WorldServer server) || world.isRemote) return;

        PhasedStructureGenerator.PendingValidationStructure pending = new PhasedStructureGenerator.PendingValidationStructure(origin, structure, EMPTY_LAYOUT, server.getSeed(), layoutSeed);
        PhasedStructureGenerator.ReadyToGenerateStructure ready = structure.validate(world, pending).orElse(null);
        if (ready == null) return;
        WorldState state = states.computeIfAbsent(server.provider.getDimension(), dim -> new WorldState());
        LongList watchedOffsets = resolveOffsets(structure, ready.finalOrigin);
        PendingDynamicStructure job = PendingDynamicStructure.obtain(structure, ready, watchedOffsets);

        ChunkProviderServer provider = server.getChunkProvider();
        if (job.evaluate(provider)) {
            job.run(server);
        } else {
            job.registerWaiting(state.waitingByChunk);
        }
    }

    public void forceGenerate(@NotNull World world, @NotNull Random rand, @NotNull BlockPos origin, @NotNull AbstractPhasedStructure structure) {
        if (world.isRemote) return;
        structure.postGenerate(world, rand, origin);
    }

    private LongList resolveOffsets(AbstractPhasedStructure structure, BlockPos origin) {
        LongArrayList offsets = structure.getWatchedChunkOffsets(origin);
        if (offsets == null || offsets.isEmpty()) {
            return ORIGIN_ONLY;
        }
        return offsets;
    }

    @SubscribeEvent
    public void onChunkPopulated(PopulateChunkEvent.Post event) {
        World world = event.getWorld();
        if (world.isRemote || !(world instanceof WorldServer server)) return;
        handleChunkAvailable(server, ChunkPos.asLong(event.getChunkX(), event.getChunkZ()));
    }

    @SubscribeEvent
    public void onChunkLoaded(ChunkEvent.Load event) {
        World world = event.getWorld();
        if (world.isRemote || !(world instanceof WorldServer server)) return;
        Chunk chunk = event.getChunk();
        if (!chunk.isPopulated() && !chunk.isTerrainPopulated()) return;
        handleChunkAvailable(server, ChunkPos.asLong(chunk.x, chunk.z));
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        World world = event.getWorld();
        if (world.isRemote || !(world instanceof WorldServer server)) return;
        states.remove(server.provider.getDimension());
    }

    private void handleChunkAvailable(WorldServer world, long chunkKey) {
        WorldState state = states.get(world.provider.getDimension());
        if (state == null) return;

        ArrayList<PendingDynamicStructure> waiters = state.waitingByChunk.remove(chunkKey);
        if (waiters == null || waiters.isEmpty()) return;

        for (int i = waiters.size() - 1; i >= 0; i--) {
            PendingDynamicStructure job = waiters.get(i);
            job.waitingOn.remove(chunkKey);
            if (job.isReady()) {
                job.run(world);
            }
        }
    }

    private static class WorldState {
        final Long2ObjectOpenHashMap<ArrayList<PendingDynamicStructure>> waitingByChunk = new Long2ObjectOpenHashMap<>();
    }

    private static class PendingDynamicStructure {
        private static final ArrayList<PendingDynamicStructure> POOL = new ArrayList<>(256);
        private static final int MAX_POOL_SIZE = 4096;
        private final LongOpenHashSet waitingOn = new LongOpenHashSet(32);
        private AbstractPhasedStructure structure;
        private BlockPos origin;
        private int baseChunkX;
        private int baseChunkZ;
        private long worldSeed;
        private LongList watchedOffsets;

        private PendingDynamicStructure() {
        }

        static PendingDynamicStructure obtain(AbstractPhasedStructure structure, PhasedStructureGenerator.ReadyToGenerateStructure ready,
                                              LongList watchedOffsets) {
            PendingDynamicStructure job;
            int size = POOL.size();
            if (size != 0) {
                job = POOL.remove(size - 1);
            } else {
                job = new PendingDynamicStructure();
            }

            job.structure = structure;
            job.origin = ready.finalOrigin;
            job.baseChunkX = job.origin.getX() >> 4;
            job.baseChunkZ = job.origin.getZ() >> 4;
            job.worldSeed = ready.pending.worldSeed;
            job.watchedOffsets = watchedOffsets;

            job.waitingOn.clear();
            return job;
        }

        private void recycle() {
            this.structure = null;
            this.origin = null;
            this.watchedOffsets = null;
            this.worldSeed = 0L;
            this.baseChunkX = 0;
            this.baseChunkZ = 0;
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

            for (int i = 0; i < watchedOffsets.size(); i++) {
                long rel = watchedOffsets.getLong(i);
                int offsetX = ChunkUtil.getChunkPosX(rel);
                int offsetZ = ChunkUtil.getChunkPosZ(rel);
                long absKey = ChunkPos.asLong(baseChunkX + offsetX, baseChunkZ + offsetZ);
                Chunk chunk = provider.loadedChunks.get(absKey);
                if (chunk == null || (!chunk.isPopulated() && !chunk.isTerrainPopulated())) {
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
                recycle();
            }
        }
    }
}
