package com.hbm.util;

import com.hbm.config.GeneralConfig;
import com.hbm.interfaces.ServerThread;
import com.hbm.interfaces.ThreadSafeMethod;
import com.hbm.lib.Library;
import com.hbm.lib.RefStrings;
import com.hbm.lib.UnsafeHolder;
import com.hbm.lib.maps.NonBlockingHashMapLong;
import com.hbm.main.MainRegistry;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BitArray;
import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.*;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

import static com.hbm.lib.UnsafeHolder.U;

/**
 * <p>
 * High-performance, non-blocking utilities for working with Minecraft 1.12.2 {@link Chunk}
 * internals. This class provides a small set of low-level, allocation-conscious helpers around:
 * </p>
 *
 * <ul>
 *   <li>Maintaining a <strong>mirror map</strong> of currently loaded chunks per dimension for
 *       concurrent, read-mostly access (see {@link #acquireMirrorMap} and
 *       {@link #getLoadedChunk}).</li>
 *   <li>Copy-on-write mutation of sub-chunks ({@link ExtendedBlockStorage}) with a single
 *       <strong>CAS publish</strong> per sub-chunk (see {@link #applyAndSwap}
 *       and {@link #casEbsAt};{@link #copyAndModify} builds a modified copy without publishing).</li>
 *   <li>Selective carving of blocks inside a sub-chunk from a bitmask while collecting metadata
 *       such as tile-entity removals and edge contacts (see {@link #copyAndCarve}).</li>
 *   <li>TileEntity lifecycle fix-ups after block-state transitions (see {@link #flushTileEntity}).</li>
 * </ul>
 *
 * <h3>Concurrency & Memory Model</h3>
 * <ul>
 *   <li>For a given {@code Chunk} instance, the {@code ExtendedBlockStorage[]} array returned by
 *       {@link Chunk#getBlockStorageArray} is <em>stable</em> for the lifetime of that {@code Chunk}:
 *       it is created in the constructor and never replaced. Only the <em>elements</em>
 *       ({@code ExtendedBlockStorage} per sub-Y) can be swapped or mutated.</li>
 *   <li>Publishing a new sub-chunk is done via {@link #casEbsAt}; the CAS provides volatile write
 *       semantics for the slot. <strong>Readers must load the slot with a volatile read</strong>
 *       via {@link #getEbsVolatile} to reliably observe swaps.
 *       Direct array reads like {@code arr[subY]} may see stale values.</li>
 * </ul>
 *
 * <h3>Mirror Map</h3>
 * <ul>
 *   <li>The mirror map is an auxiliary, non-blocking concurrent structure mapping
 *       {@code dimension → (chunkPos → Chunk)} used to locate chunks from worker threads without
 *       touching the vanilla
 *       {@link net.minecraft.world.gen.ChunkProviderServer#loadedChunks loadedChunks}, which is a
 *       non-threadsafe {@link it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap}.</li>
 *   <li>Reference-counted: call {@link #acquireMirrorMap} before concurrent work and
 *       {@link #releaseMirrorMap} afterwards. When the reference count drops to zero the
 *       mirror map is cleared.</li>
 * </ul>
 *
 * <h3>Indexing conventions</h3>
 * <ul>
 *   <li>Local sub-chunk indices (0..4095) are laid out as:
 *       {@code index = x | (z << 4) | (y << 8)}. See {@link #indexToX(int, int)},
 *       {@link #indexToZ(int, int)}, and {@link #indexToY(int)}.</li>
 *   <li>When using a height-descending bitset (as in {@link #copyAndCarve}), the bit index for a
 *       global position {@code (x,y,z)} within a world of height {@code H} is
 *       {@code ((H - 1 - y) << 8) | (z << 4) | x}.</li>
 *   <li>"Packed local" integers used in maps follow the same layout and can be created/decomposed
 *       via helpers in {@link Library}.</li>
 * </ul>
 *
 * <h3>Safety notes</h3>
 * <ul>
 *   <li>The {@code Chunk}/{@code ExtendedBlockStorage[]} array instance passed to helpers must be current.
 *       If a stale reference is passed, the modification will fail <strong>silently</strong> because
 *       the actual modification happens on an orphaned instance that was supposed to be garbage collected.
 *       This can happen if the chunk was unloaded or reloaded. </li>
 *   <li>{@link #getLoadedChunk} clears the {@link Chunk#unloadQueued} flag on the retrieved chunk
 *       to reduce races with the provider's unload pass. This can keep the chunk alive slightly
 *       longer but doesn't ensure the chunk is always loaded. Use {@link net.minecraftforge.common.ForgeChunkManager.Ticket}
 *       to enforce this behavior.</li>
 * </ul>
 *
 * @author mlbv
 */
@Mod.EventBusSubscriber(modid = RefStrings.MODID)
public final class ChunkUtil {

    private static final long ARR_BASE = U.arrayBaseOffset(ExtendedBlockStorage[].class);
    private static final long ARR_SCALE = U.arrayIndexScale(ExtendedBlockStorage[].class);
    private static final long UNLOAD_QUEUED_OFFSET = UnsafeHolder.fieldOffset(Chunk.class, "unloadQueued", "field_189550_d");
    private static final long BSL_STATES_OFFSET = UnsafeHolder.fieldOffset(BlockStatePaletteLinear.class, "states", "field_186042_a");
    private static final long BSL_ARRAY_SIZE_OFFSET = UnsafeHolder.fieldOffset(BlockStatePaletteLinear.class, "arraySize", "field_186045_d");
    private static final long BSHM_MAP_OFFSET = UnsafeHolder.fieldOffset(BlockStatePaletteHashMap.class, "statePaletteMap", "field_186046_a");
    private static final long IIHBM_VALUES_OFFSET = UnsafeHolder.fieldOffset(IntIdentityHashBiMap.class, "values", "field_186818_b");
    private static final long IIHBM_INTKEYS_OFFSET = UnsafeHolder.fieldOffset(IntIdentityHashBiMap.class, "intKeys", "field_186819_c");
    private static final long IIHBM_BYID_OFFSET = UnsafeHolder.fieldOffset(IntIdentityHashBiMap.class, "byId", "field_186820_d");
    private static final long IIHBM_NEXTFREE_OFFSET = UnsafeHolder.fieldOffset(IntIdentityHashBiMap.class, "nextFreeIndex", "field_186821_e");
    private static final long IIHBM_MAPSIZE_OFFSET = UnsafeHolder.fieldOffset(IntIdentityHashBiMap.class, "mapSize", "field_186822_f");

    private static final ThreadLocal<Int2ObjectOpenHashMap<IBlockState>[]> TL_BUCKET = ThreadLocal.withInitial(() -> {
        // noinspection unchecked
        Int2ObjectOpenHashMap<IBlockState>[] maps = new Int2ObjectOpenHashMap[16];
        for (int i = 0; i < maps.length; i++) {
            maps[i] = new Int2ObjectOpenHashMap<>();
        }
        return maps;
    });
    private static final ThreadLocal<Long2ObjectOpenHashMap<IBlockState>> TL_SCRATCH = ThreadLocal.withInitial(Long2ObjectOpenHashMap::new);
    private static final ThreadLocal<IBlockState[]> TL_OVERRIDES = ThreadLocal.withInitial(() -> new IBlockState[4096]);
    private static final IBlockState AIR_DEFAULT_STATE = Blocks.AIR.getDefaultState();

    /**
     * Dimension → active task count (used to decide when to build/tear down the mirror map).
     */
    private static final Int2IntOpenHashMap activeTask = new Int2IntOpenHashMap();

    /**
     * Dimension → (ChunkPos long key → Chunk) mirror. Only present while there are active tasks.
     */
    private static final NonBlockingHashMapLong<NonBlockingHashMapLong<Chunk>> chunkMap = new NonBlockingHashMapLong<>();

    /**
     * Heuristic threshold: switch to dense path when sub-chunk updates exceed roughly 1/3 of 4096.
     */
    private static final int DENSE_THRESHOLD = 4096 / 3;

    /**
     * Global reference count across all dimensions indicating how many concurrent tasks are active.
     */
    private static int refCounter = 0;

    /**
     * Build (or reference) the mirror map for the given dimension and increment its task count.
     *
     * <p><strong>Threading:</strong> Must be called from the server thread. Typical usage is to
     * acquire once before kicking off parallel work that will rely on {@link #getLoadedChunk} or
     * {@link #getLoadedEBS}.</p>
     *
     * @param world the server world whose dimension will be mirrored
     */
    @ServerThread
    public static void acquireMirrorMap(@NotNull WorldServer world) {
        int key = world.provider.getDimension();
        if (activeTask.addTo(key, 1) == 0) {
            NonBlockingHashMapLong<Chunk> thisDim = new NonBlockingHashMapLong<>(4096); // half the initial capacity of loadedChunks
            // This parallel traversal assumes the server thread is quiescent for this world's provider
            world.getChunkProvider().loadedChunks.values().parallelStream().forEach(chunk -> thisDim.put(ChunkPos.asLong(chunk.x, chunk.z), chunk));
            chunkMap.put(key, thisDim);
        }
        refCounter++;
        if (GeneralConfig.enableExtendedLogging) {
            MainRegistry.logger.info(
                    "Acquired mirror map for dimension {}. Active tasks of this dim = {}, refCounter = {}.\nAll active dimensions: {}", key,
                    activeTask.get(key), refCounter, chunkMap.keySetLong());
        }
    }

    /**
     * Decrement the dimension task count and, if it hits zero, drop the mirror map for that
     * dimension. Also decrements the global reference counter.
     *
     * <p><strong>Threading:</strong> Must be called from the server thread and should be paired
     * with {@link #acquireMirrorMap(WorldServer)}.</p>
     *
     * @param world the server world whose dimension mirror should be released (if last user)
     */
    @ServerThread
    public static void releaseMirrorMap(@NotNull WorldServer world) {
        int key = world.provider.getDimension();
        if (activeTask.addTo(key, -1) == 1) chunkMap.remove(key);
        refCounter--;
        if (GeneralConfig.enableExtendedLogging) {
            MainRegistry.logger.info(
                    "Released mirror map for dimension {}. Active tasks of this dim = {}, refCounter = {}.\nAll active dimensions: {}", key,
                    activeTask.get(key), refCounter, chunkMap.keySetLong());
        }
    }

    /**
     * Lookup a loaded {@link Chunk} from the mirror map by packed chunk position.
     *
     * <p>
     * If found, this method also forcibly clears the chunk's {@code unloadQueued} flag to reduce
     * the chance that the provider unloads it while off-thread work is still in-flight. This reduces
     * but does not eliminate unload races; callers should tolerate {@code null} on subsequent lookups.</p>
     * <p>
     * It is strongly advised to keep the chunk loaded via {@link net.minecraftforge.common.ForgeChunkManager.Ticket ForgeChunkManager.Ticket}
     *
     * @param world    the world whose dimension mirror to query
     * @param chunkPos packed long chunk position (see {@link ChunkPos#asLong(int, int)})
     * @return the loaded {@link Chunk} reference, or {@code null} if absent in the mirror
     */
    @Nullable
    @ThreadSafeMethod
    public static Chunk getLoadedChunk(@NotNull WorldServer world, long chunkPos) {
        int key = world.provider.getDimension();
        NonBlockingHashMapLong<Chunk> dimMap = chunkMap.get(key);
        if (dimMap == null) return null;
        Chunk chunk = dimMap.get(chunkPos);
        if (chunk == null) return null;
        U.putBooleanVolatile(chunk, UNLOAD_QUEUED_OFFSET, false);
        return chunk;
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (refCounter == 0) return;
        Chunk chunk = event.getChunk();
        World world = chunk.getWorld();
        if (world.isRemote) return;
        int key = world.provider.getDimension();
        if (activeTask.get(key) == 0) return;
        NonBlockingHashMapLong<Chunk> dimMap = chunkMap.get(key);
        if (dimMap != null) dimMap.put(ChunkPos.asLong(chunk.x, chunk.z), chunk);
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (refCounter == 0) return;
        Chunk chunk = event.getChunk();
        World world = chunk.getWorld();
        if (world.isRemote) return;
        int key = world.provider.getDimension();
        NonBlockingHashMapLong<Chunk> dimMap = chunkMap.get(key);
        if (dimMap != null) dimMap.remove(ChunkPos.asLong(chunk.x, chunk.z));
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        World world = event.getWorld();
        if (world.isRemote) return;
        int key = world.provider.getDimension();
        if (GeneralConfig.enableExtendedLogging)
            MainRegistry.logger.info("Dimension {} unloaded with {} active tasks. refCounter = {}", key, activeTask.get(key), refCounter);
        activeTask.put(key, 0);
        chunkMap.remove(key);
    }

    public static void onServerStopping() {
        chunkMap.clear();
        activeTask.clear();
        if (GeneralConfig.enableExtendedLogging)
            MainRegistry.logger.info("Server stopping with {} active tasks, refCounter = {}", Arrays.stream(activeTask.values().toIntArray()).sum(),
                    refCounter);
        refCounter = 0;
    }

    /**
     * Make a deep copy of a {@link BlockStateContainer}, including its palette and backing bit-storage.
     *
     * @param srcData the source container
     * @return a new, independent {@link BlockStateContainer} with identical content
     */
    @NotNull
    @ThreadSafeMethod
    @Contract("_ -> new")
    public static BlockStateContainer copyOf(@NotNull BlockStateContainer srcData) {
        final int bits = srcData.bits;
        final IBlockStatePalette srcPalette = srcData.palette;
        final BlockStateContainer copied = UnsafeHolder.allocateInstance(BlockStateContainer.class);
        copied.bits = bits;

        if (bits <= 4) {
            copied.palette = new BlockStatePaletteLinear(bits, copied);
            final int arraySize = U.getInt(srcPalette, BSL_ARRAY_SIZE_OFFSET);
            U.putInt(copied.palette, BSL_ARRAY_SIZE_OFFSET, arraySize);
            final IBlockState[] srcStates = (IBlockState[]) U.getObject(srcPalette, BSL_STATES_OFFSET);
            final IBlockState[] dstStates = (IBlockState[]) U.getObject(copied.palette, BSL_STATES_OFFSET);
            System.arraycopy(srcStates, 0, dstStates, 0, arraySize);
        } else if (bits <= 8) {
            copied.palette = new BlockStatePaletteHashMap(bits, copied);
            final Object srcMap = U.getObject(srcPalette, BSHM_MAP_OFFSET);
            final Object dstMap = U.getObject(copied.palette, BSHM_MAP_OFFSET);

            final int nextFree = U.getInt(srcMap, IIHBM_NEXTFREE_OFFSET);
            final int mapSize = U.getInt(srcMap, IIHBM_MAPSIZE_OFFSET);
            U.putInt(dstMap, IIHBM_NEXTFREE_OFFSET, nextFree);
            U.putInt(dstMap, IIHBM_MAPSIZE_OFFSET, mapSize);

            final Object[] srcValues = (Object[]) U.getObject(srcMap, IIHBM_VALUES_OFFSET);
            final int[] srcIntKeys = (int[]) U.getObject(srcMap, IIHBM_INTKEYS_OFFSET);
            final Object[] srcById = (Object[]) U.getObject(srcMap, IIHBM_BYID_OFFSET);

            U.putObject(dstMap, IIHBM_VALUES_OFFSET, srcValues.clone());
            U.putObject(dstMap, IIHBM_INTKEYS_OFFSET, srcIntKeys.clone());
            U.putObject(dstMap, IIHBM_BYID_OFFSET, srcById.clone());
        } else {
            copied.palette = BlockStateContainer.REGISTRY_BASED_PALETTE;
        }

        final BitArray srcStorage = srcData.storage;
        copied.storage = new BitArray(bits, 4096);
        final long[] srcLongs = srcStorage.getBackingLongArray();
        final long[] dstLongs = copied.storage.getBackingLongArray();
        System.arraycopy(srcLongs, 0, dstLongs, 0, srcLongs.length);
        return copied;
    }

    /**
     * Produce a modified copy of the target sub-chunk by <em>carving out</em> positions marked in
     * {@code bs}. For every non-air block removed, tile-entity removals and edge contacts are
     * recorded.
     *
     * <p>Edge contact logic: if a removed block position is adjacent to a non-air block outside the
     * sub-chunk bounds (±X/±Z neighbor chunks or the subchunk above/below), the removed block's
     * <em>own</em> global packed position is added to {@code edgeOut}.
     *
     * <p>Only the bit range that corresponds to this {@code subY} is scanned; set bits for other
     * sub-chunks are skipped.</p>
     *
     * <p>Neighbor reads are done via {@link #getLoadedEBS(WorldServer, long)} using the mirror map.</p>
     *
     * @param world      the world (for height and skylight info)
     * @param chunkX     chunk X coordinate
     * @param chunkZ     chunk Z coordinate
     * @param subY       sub-chunk Y index (0..height/16-1)
     * @param srcs       the source chunk's {@code ExtendedBlockStorage[]} array
     * @param bs         bitset of positions to carve; bits are ordered by descending global Y
     * @param teRemovals sink of global packed positions whose TEs should be removed
     * @param edgeOut    sink of global packed positions that touch non-air outside the sub-chunk
     * @return a copied {@link ExtendedBlockStorage} with carved positions set to air, or
     * {@code null} if the source is empty.
     */
    @ThreadSafeMethod
    @Contract(mutates = "param7, param8") // teRemovals and edgeOut
    public static @Nullable ExtendedBlockStorage copyAndCarve(@NotNull WorldServer world, int chunkX, int chunkZ, int subY,
                                                              @Nullable ExtendedBlockStorage @NotNull [] srcs, @NotNull ConcurrentBitSet bs,
                                                              @NotNull LongCollection teRemovals, @NotNull LongCollection edgeOut) {
        ExtendedBlockStorage src = getEbsVolatile(srcs, subY);
        if (src == null || src.isEmpty()) return null;
        final int height = world.getHeight();
        final ExtendedBlockStorage dst = copyOf(src);
        ExtendedBlockStorage[] storagesNegX = null, storagesPosX = null, storagesNegZ = null, storagesPosZ = null;
        final int startBit = (height - 1 - ((subY << 4) + 15)) << 8;
        final int endBit = ((height - 1 - (subY << 4)) << 8) | 0xFF;
        int bit = bs.nextSetBit(startBit);
        while (bit >= 0 && bit <= endBit) {
            final int yGlobal = height - 1 - (bit >>> 8);
            final int xGlobal = (chunkX << 4) | ((bit >>> 4) & 0xF);
            final int zGlobal = (chunkZ << 4) | (bit & 0xF);

            final int xLocal = xGlobal & 0xF;
            final int yLocal = yGlobal & 0xF;
            final int zLocal = zGlobal & 0xF;

            final IBlockState old = dst.get(xLocal, yLocal, zLocal);
            final Block oldBlock = old.getBlock();
            if (oldBlock != Blocks.AIR) {
                final long packed = Library.blockPosToLong(xGlobal, yGlobal, zGlobal);
                if (oldBlock.hasTileEntity(old)) teRemovals.add(packed);
                boolean touchesOutsideNonAir = false;
                if (yLocal == 0 && subY > 0) {
                    ExtendedBlockStorage below = srcs[subY - 1];
                    if (below != null && !below.isEmpty()) {
                        IBlockState nb = below.get(xLocal, 15, zLocal);
                        if (nb.getBlock() != Blocks.AIR) touchesOutsideNonAir = true;
                    }
                }
                if (!touchesOutsideNonAir && yLocal == 15 && subY < (height >> 4) - 1) {
                    ExtendedBlockStorage above = srcs[subY + 1];
                    if (above != null && !above.isEmpty()) {
                        IBlockState nb = above.get(xLocal, 0, zLocal);
                        if (nb.getBlock() != Blocks.AIR) touchesOutsideNonAir = true;
                    }
                }
                if (!touchesOutsideNonAir && xLocal == 0) {
                    int nCX = chunkX - 1;
                    if (storagesNegX == null) storagesNegX = getLoadedEBS(world, ChunkPos.asLong(nCX, chunkZ));
                    if (storagesNegX != null) {
                        ExtendedBlockStorage n = storagesNegX[subY];
                        if (n != null && !n.isEmpty()) {
                            IBlockState nb = n.get(15, yLocal, zLocal);
                            if (nb.getBlock() != Blocks.AIR) touchesOutsideNonAir = true;
                        }
                    }
                }
                if (!touchesOutsideNonAir && xLocal == 15) {
                    int nCX = chunkX + 1;
                    if (storagesPosX == null) storagesPosX = getLoadedEBS(world, ChunkPos.asLong(nCX, chunkZ));
                    if (storagesPosX != null) {
                        ExtendedBlockStorage n = storagesPosX[subY];
                        if (n != null && !n.isEmpty()) {
                            IBlockState nb = n.get(0, yLocal, zLocal);
                            if (nb.getBlock() != Blocks.AIR) touchesOutsideNonAir = true;
                        }
                    }
                }
                if (!touchesOutsideNonAir && zLocal == 0) {
                    int nCZ = chunkZ - 1;
                    if (storagesNegZ == null) storagesNegZ = getLoadedEBS(world, ChunkPos.asLong(chunkX, nCZ));
                    if (storagesNegZ != null) {
                        ExtendedBlockStorage n = storagesNegZ[subY];
                        if (n != null && !n.isEmpty()) {
                            IBlockState nb = n.get(xLocal, yLocal, 15);
                            if (nb.getBlock() != Blocks.AIR) touchesOutsideNonAir = true;
                        }
                    }
                }
                if (!touchesOutsideNonAir && zLocal == 15) {
                    int nCZ = chunkZ + 1;
                    if (storagesPosZ == null) storagesPosZ = getLoadedEBS(world, ChunkPos.asLong(chunkX, nCZ));
                    if (storagesPosZ != null) {
                        ExtendedBlockStorage n = storagesPosZ[subY];
                        if (n != null && !n.isEmpty()) {
                            IBlockState nb = n.get(xLocal, yLocal, 0);
                            if (nb.getBlock() != Blocks.AIR) touchesOutsideNonAir = true;
                        }
                    }
                }
                if (touchesOutsideNonAir) {
                    edgeOut.add(packed);
                }
                dst.set(xLocal, yLocal, zLocal, AIR_DEFAULT_STATE); // updates ref counts
            }
            bit = bs.nextSetBit(bit + 1);
        }
        return dst;
    }

    /**
     * Atomically swap a sub-chunk slot using a single compare-and-swap (CAS).
     *
     * @param expect the expected current value in {@code arr[subY]}.
     *               The CAS is guaranteed to fail if the reference is stale.
     * @param update the value to publish on success (can be {@code null})
     * @param arr    the chunk's {@code ExtendedBlockStorage[]} array
     * @param subY   the sub-chunk Y index
     * @return {@code true} if the swap succeeded; {@code false} if the expected value did not match
     */
    @ThreadSafeMethod
    public static boolean casEbsAt(@Nullable ExtendedBlockStorage expect, @Nullable ExtendedBlockStorage update,
                                   @Nullable ExtendedBlockStorage @NotNull [] arr, int subY) {
        final long off = ARR_BASE + ((long) subY) * ARR_SCALE;
        return U.compareAndSwapObject(arr, off, expect, update);
    }

    /**
     * Load a sub-chunk slot using volatile reads, semantically equivalent to {@code arr[subY]}.
     * This must be used by concurrent readers that need to observe published swaps promptly.
     *
     * @param arr  the chunk's {@code ExtendedBlockStorage[]} array
     * @param subY the sub-chunk Y index
     * @return the {@link ExtendedBlockStorage} reference at {@code arr[subY]}, can be {@code null}
     */
    @ThreadSafeMethod
    private static @Nullable ExtendedBlockStorage getEbsVolatile(@Nullable ExtendedBlockStorage @NotNull [] arr, int subY) {
        final long off = ARR_BASE + ((long) subY) * ARR_SCALE;
        return (ExtendedBlockStorage) U.getObjectVolatile(arr, off);
    }

    /**
     * Fetch the sub-chunk array for a loaded chunk using the mirror map.
     *
     * @param world    the world whose mirror to query
     * @param chunkPos packed long chunk position
     * @return the sub-chunk array reference or {@code null} if the chunk is not present in the mirror
     */
    @ThreadSafeMethod
    public static @Nullable ExtendedBlockStorage @Nullable [] getLoadedEBS(@NotNull WorldServer world, long chunkPos) {
        Chunk chunk = getLoadedChunk(world, chunkPos);
        if (chunk == null) return null;
        return chunk.getBlockStorageArray();
    }

    /**
     * Copy block/skylight nibble arrays, palette+storage and ref counts from
     * {@code src} to {@code dst}. Does not copy {@link ExtendedBlockStorage#yBase}.
     * <p>
     * Intended for cross-dimension copying.
     */
    public static void copyEBS(boolean hasSky, @NotNull ExtendedBlockStorage src, @NotNull ExtendedBlockStorage dst) {
        dst.data = copyOf(src.getData());
        dst.blockLight = new NibbleArray(src.getBlockLight().getData().clone());
        dst.skyLight = hasSky ? src.skyLight != null ? new NibbleArray(src.skyLight.getData().clone()) : new NibbleArray() : null;
        dst.blockRefCount = src.blockRefCount;
        dst.tickRefCount = src.tickRefCount;
    }

    /**
     * @return a deep copy of {@code src}
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull ExtendedBlockStorage copyOf(@NotNull ExtendedBlockStorage src) {
        ExtendedBlockStorage dst = UnsafeHolder.allocateInstance(ExtendedBlockStorage.class);
        dst.yBase = src.yBase;
        dst.data = copyOf(src.getData());
        dst.blockLight = new NibbleArray(src.getBlockLight().getData().clone());
        dst.skyLight = src.skyLight != null ? new NibbleArray(src.skyLight.getData().clone()) : null;
        dst.blockRefCount = src.blockRefCount;
        dst.tickRefCount = src.tickRefCount;
        return dst;
    }

    /**
     * Apply block-state changes to a chunk <em>in place</em> using a copy-on-write strategy and a
     * per-subchunk CAS publish. This method retries when the CAS fails due to concurrent swaps.
     *
     * <p>The provided {@code function} must return a map of <strong>global packed block position</strong>
     * (see {@link Library#blockPosToLong(int, int, int)}) to <strong>new</strong> {@link IBlockState}
     * for positions that should change. Entries outside the target chunk or outside world height are
     * ignored.</p>
     *
     * <p>For each sub-chunk touched, this method builds a working copy, applies changes, and attempts
     * a CAS publish. If the CAS fails due to a concurrent swap, it retries by reading the latest
     * source again. Two paths are used:
     * <ul>
     *   <li><em>Sparse path</em> (updates &lt; {@link #DENSE_THRESHOLD}): iterate only the changed
     *       indices.</li>
     *   <li><em>Dense path</em>: prebuild a 4096-entry override array and sweep 0..4095.</li>
     * </ul>
     * </p>
     *
     * <p>If {@code oldStatesOut} is non-null, it is populated <em>only after</em> a successful CAS
     * with the <strong>old</strong> states of all positions changed in that sub-chunk, using global
     * packed positions. Only entries that actually changed (identity compare) are emitted, and the
     * old state may be {@link Blocks#AIR}'s default state when the sub-chunk was empty.</p>
     *
     * @param chunk        the chunk to mutate
     * @param function     producer of desired mutations; may return {@code null} or an empty map to signal no-op;
     *                     values inside the returned map must be non-null
     * @param oldStatesOut optional sink for old states (global packed positions)
     * @throws NullPointerException if {@code chunk} or {@code function} is {@code null}, or if any
     *                              value in the map returned by {@code function.apply(chunk)} is {@code null}
     */
    @ThreadSafeMethod
    public static void applyAndSwap(@NotNull Chunk chunk, @NotNull Function<Chunk, @Nullable Long2ObjectMap<@NotNull IBlockState>> function,
                                    @Nullable Long2ObjectMap<@NotNull IBlockState> oldStatesOut) {

        final Long2ObjectMap<IBlockState> newStates = function.apply(chunk);
        if (newStates == null || newStates.isEmpty()) return;

        final WorldServer world = (WorldServer) chunk.getWorld();
        final boolean hasSky = world.provider.hasSkyLight();
        final int height = world.getHeight();
        final int chunkX = chunk.x, chunkZ = chunk.z;
        final ExtendedBlockStorage[] arr = chunk.getBlockStorageArray();

        final Int2ObjectOpenHashMap<IBlockState>[] bySub = TL_BUCKET.get();
        for (Int2ObjectOpenHashMap<IBlockState> map : bySub) map.clear();

        // bucket updates per subY with local-packed indices
        for (Long2ObjectMap.Entry<IBlockState> e : newStates.long2ObjectEntrySet()) {
            final long p = e.getLongKey();
            final int x = Library.getBlockPosX(p);
            final int y = Library.getBlockPosY(p);
            final int z = Library.getBlockPosZ(p);
            if ((x >> 4) != chunkX || (z >> 4) != chunkZ) continue;
            if (y < 0 || y >= height) continue;

            final int subY = y >> 4;
            Int2ObjectOpenHashMap<IBlockState> b = bySub[subY];
            if (b == null) bySub[subY] = b = new Int2ObjectOpenHashMap<>();
            b.put(Library.packLocal(x & 15, y & 15, z & 15), e.getValue());
        }

        final Long2ObjectOpenHashMap<IBlockState> subScratch = oldStatesOut == null ? null : TL_SCRATCH.get();

        for (int subY = 0; subY < 16; subY++) {
            if (subScratch != null) subScratch.clear();
            final Int2ObjectOpenHashMap<IBlockState> bucket = bySub[subY];
            if (bucket == null || bucket.isEmpty()) continue;

            if (bucket.size() < DENSE_THRESHOLD) {
                // sparse path
                while (true) {
                    final ExtendedBlockStorage src = getEbsVolatile(arr, subY);
                    ExtendedBlockStorage working = null;
                    boolean any = false;

                    final int yBase = subY << 4;
                    final int xBase = chunkX << 4, zBase = chunkZ << 4;

                    for (Int2ObjectMap.Entry<IBlockState> e : bucket.int2ObjectEntrySet()) {
                        final int local = e.getIntKey();
                        final int lx = Library.unpackLocalX(local);
                        final int ly = Library.unpackLocalY(local);
                        final int lz = Library.unpackLocalZ(local);
                        final IBlockState ns = e.getValue();
                        if (ns == null) throw new NullPointerException("newState");

                        final IBlockState os = (src != null && !src.isEmpty()) ? src.get(lx, ly, lz) : AIR_DEFAULT_STATE;
                        if (os == ns) continue;

                        if (working == null) {
                            if (src != null && !src.isEmpty()) {
                                working = copyOf(src);
                            } else {
                                if (ns.getBlock() == Blocks.AIR) continue;
                                working = new ExtendedBlockStorage(yBase, hasSky);
                            }
                        }

                        working.set(lx, ly, lz, ns);
                        any = true;

                        if (subScratch != null) {
                            final long gpos = Library.blockPosToLong(xBase | lx, yBase | ly, zBase | lz);
                            subScratch.put(gpos, os);
                        }
                    }

                    if (!any) break;
                    final ExtendedBlockStorage update = working.isEmpty() ? null : working;
                    if (casEbsAt(src, update, arr, subY)) {
                        if (oldStatesOut != null) oldStatesOut.putAll(subScratch);
                        break;
                    } else if (subScratch != null) {
                        subScratch.clear();
                    }
                }
            } else {
                // Dense path: build a 4096 override array, then sweep 0..4095 using indexTo*
                final IBlockState[] overrides = TL_OVERRIDES.get();
                Arrays.fill(overrides, null);
                for (Int2ObjectMap.Entry<IBlockState> e : bucket.int2ObjectEntrySet()) {
                    overrides[e.getIntKey()] = e.getValue();
                }

                while (true) {
                    final ExtendedBlockStorage src = getEbsVolatile(arr, subY);
                    ExtendedBlockStorage working = null;
                    boolean any = false;

                    for (int idx = 0; idx < 4096; idx++) {
                        final IBlockState newState = overrides[idx];
                        if (newState == null) continue;

                        final int lx = idx & 15;
                        final int ly = idx >>> 8;
                        final int lz = (idx >>> 4) & 15;

                        final IBlockState oldState = src == null || src.isEmpty() ? AIR_DEFAULT_STATE : src.get(lx, ly, lz);
                        if (oldState == newState) continue;

                        if (working == null) {
                            if (src == null || src.isEmpty()) {
                                working = new ExtendedBlockStorage(subY << 4, hasSky);
                            } else {
                                working = copyOf(src);
                            }
                        }

                        working.set(lx, ly, lz, newState);
                        any = true;

                        if (subScratch != null) {
                            final int x = indexToX(idx, chunkX);
                            final int y = indexToY(idx) | (subY << 4);
                            final int z = indexToZ(idx, chunkZ);
                            subScratch.put(Library.blockPosToLong(x, y, z), oldState);
                        }
                    }

                    if (!any) break;
                    final ExtendedBlockStorage update = working.isEmpty() ? null : working;
                    if (casEbsAt(src, update, arr, subY)) {
                        if (oldStatesOut != null) oldStatesOut.putAll(subScratch);
                        break;
                    } else if (subScratch != null) {
                        subScratch.clear();
                    }
                }
            }
        }
    }

    /**
     * Create a modified <em>copy</em> of a sub-chunk by applying local (0..4095) overrides and,
     * optionally, recording the <strong>old</strong> global states into {@code oldStatesOut}.
     *
     * @param chunkX       chunk X coordinate
     * @param chunkZ       chunk Z coordinate
     * @param subY         sub-chunk Y index (0..15)
     * @param hasSky       whether the world has skylight
     * @param src          source sub-chunk; may be {@code null} or empty
     * @param toUpdate     map of <em>packed local</em> index ({@code x | (z << 4) | (y << 8)}) → new state
     * @param oldStatesOut optional sink of pre-change states keyed by <em>global packed</em> positions
     * @return {@code null} for no-op, {@code Optional.empty()} for became empty,
     * or {@code Optional<ExtendedBlockStorage>} for a non-empty modified copy
     * @throws NullPointerException if any value in {@code toUpdate} is {@code null}
     */
    @ThreadSafeMethod
    @SuppressWarnings("OptionalAssignedToNull")
    public static @Nullable Optional<ExtendedBlockStorage> copyAndModify(int chunkX, int chunkZ, int subY, boolean hasSky, @Nullable ExtendedBlockStorage src
            , @NotNull Int2ObjectMap<@NotNull IBlockState> toUpdate, @Nullable Long2ObjectMap<@NotNull IBlockState> oldStatesOut) {
        if (toUpdate.isEmpty()) return null;

        ExtendedBlockStorage dst = null;
        boolean anyChange = false;
        final int xBase = chunkX << 4;
        final int yBase = subY << 4;
        final int zBase = chunkZ << 4;

        for (Int2ObjectMap.Entry<IBlockState> e : toUpdate.int2ObjectEntrySet()) {
            final int packedLocal = e.getIntKey();
            final int lx = Library.unpackLocalX(packedLocal);
            final int ly = Library.unpackLocalY(packedLocal);
            final int lz = Library.unpackLocalZ(packedLocal);

            final IBlockState newState = e.getValue();
            if (newState == null) throw new NullPointerException("newState");

            final IBlockState oldState = (src != null && !src.isEmpty()) ? src.get(lx, ly, lz) : AIR_DEFAULT_STATE;
            if (oldState == newState) continue;

            if (dst == null) {
                if (src != null && !src.isEmpty()) {
                    dst = copyOf(src);
                } else {
                    if (newState.getBlock() == Blocks.AIR) continue;
                    dst = new ExtendedBlockStorage(yBase, hasSky);
                }
            }

            // record OLD state before change
            if (oldStatesOut != null) {
                final int xGlobal = xBase | lx;
                final int yGlobal = yBase | ly;
                final int zGlobal = zBase | lz;
                oldStatesOut.put(Library.blockPosToLong(xGlobal, yGlobal, zGlobal), oldState);
            }

            dst.set(lx, ly, lz, newState);
            anyChange = true;
        }

        if (!anyChange) return null;
        return dst.isEmpty() ? Optional.empty() : Optional.of(dst);
    }

    /**
     * Server-thread fix-up for TileEntity lifecycle around a block-state transition at {@code pos}.
     *
     * <p>Calls {@link Block#breakBlock(World, BlockPos, IBlockState)} on the old block if the block
     * type changes, removes the existing TE if {@link TileEntity#shouldRefresh(World, BlockPos, IBlockState, IBlockState)}
     * says so, and then creates/attaches a new TE if the new block has one. Finally calls
     * {@link TileEntity#updateContainingBlockInfo()} on the TE if present. This mirrors the TE
     * lifecycle parts of {@code setBlockState} without neighbor notifications and may trigger block
     * drops/cleanup via {@code breakBlock} when the type changes.</p>
     *
     * @param chunk    the chunk containing {@code pos}
     * @param pos      target position
     * @param oldState previous block state
     * @param newState new block state
     */
    @ServerThread
    public static void flushTileEntity(Chunk chunk, BlockPos pos, IBlockState oldState, IBlockState newState) {
        World world = chunk.getWorld();
        Block oldBlock = oldState.getBlock();
        Block newBlock = newState.getBlock();
        if (oldBlock != newBlock) oldBlock.breakBlock(world, pos, oldState);
        TileEntity te = chunk.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
        if (te != null && te.shouldRefresh(world, pos, oldState, newState)) world.removeTileEntity(pos);
        Block block = newState.getBlock();
        if (!block.hasTileEntity(newState)) return;
        TileEntity newTileEntity = chunk.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
        if (newTileEntity == null) {
            newTileEntity = block.createTileEntity(world, newState);
            if (newTileEntity != null) world.setTileEntity(pos, newTileEntity);
        }
        if (newTileEntity != null) newTileEntity.updateContainingBlockInfo();
    }

    /**
     * @return the X component of a packed chunk-key ({@code x | (z << 32)}).
     */
    @Contract(pure = true)
    public static int getChunkPosX(long chunkKey) {
        return (int) (chunkKey & 0xFFFFFFFFL);
    }

    /**
     * @return the Z component of a packed chunk-key ({@code x | (z << 32)}).
     */
    @Contract(pure = true)
    public static int getChunkPosZ(long chunkKey) {
        return (int) ((chunkKey >>> 32) & 0xFFFFFFFFL);
    }

    /**
     * Convert a local 0..4095 index to a global X coordinate for the given chunk X.
     */
    @Contract(pure = true)
    public static int indexToX(int index, int chunkX) {
        return (chunkX << 4) | (index & 15);
    }

    /**
     * Extract the local Y (0..15) from a 0..4095 local index.
     */
    @Contract(pure = true)
    public static int indexToY(int index) {
        return index >>> 8;
    }

    /**
     * Convert a local 0..4095 index to a global Z coordinate for the given chunk Z.
     */
    @Contract(pure = true)
    public static int indexToZ(int index, int chunkZ) {
        return (chunkZ << 4) | ((index >>> 4) & 15);
    }
}
