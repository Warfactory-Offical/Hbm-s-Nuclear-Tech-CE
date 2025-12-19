package com.hbm.handler.radiation;

import com.hbm.Tags;
import com.hbm.config.CompatibilityConfig;
import com.hbm.config.GeneralConfig;
import com.hbm.config.RadiationConfig;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.IRadResistantBlock;
import com.hbm.interfaces.ServerThread;
import com.hbm.lib.Library;
import com.hbm.lib.TLPool;
import com.hbm.lib.internal.UnsafeHolder;
import com.hbm.lib.maps.NonBlockingHashMapLong;
import com.hbm.lib.maps.NonBlockingHashSetLong;
import com.hbm.lib.maps.NonBlockingLong2LongHashMap;
import com.hbm.lib.queues.MpscUnboundedXaddArrayLongQueue;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.saveddata.AuxSavedData;
import com.hbm.util.DecodeException;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BitArray;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.*;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.*;

import static com.hbm.lib.internal.UnsafeHolder.U;

/**
 * A concurrent radiation propagation system utilizing the Finite Volume Method.
 * <p>
 * This system solves for radiation density (&rho;) using the transport equation:
 * <br>
 * <center>
 * &part;&rho;/&part;t = D&nabla;<sup>2</sup>&rho; &minus; &lambda;&rho; + S
 * </center>
 *
 * @author mlbv
 */
@Mod.EventBusSubscriber(modid = Tags.MODID)
public final class RadiationSystemNT {

    private static final int NO_POCKET = 15, NEI_SLOTS = 16, NEI_SHIFT = 1;
    private static final int[] BOUNDARY_MASKS = {0, 0, 0xF00, 0xF00, 0xFF0, 0xFF0}, LINEAR_OFFSETS = {-256, 256, -16, 16, -1, 1};
    private static final int[] FACE_DX = {0, 0, 0, 0, -1, 1}, FACE_DY = {-1, 1, 0, 0, 0, 0}, FACE_DZ = {0, 0, -1, 1, 0, 0};
    private static final int[] FACE_PLANE = new int[6 * 256];

    private static final int SECTION_BLOCK_COUNT = 4096;

    private static final long IA_BASE = U.arrayBaseOffset(int[].class);
    private static final int IA_SHIFT = Integer.numberOfTrailingZeros(U.arrayIndexScale(int[].class));
    private static final long SRA_BASE = U.arrayBaseOffset(SectionRef[].class);
    private static final int SRA_SHIFT = Integer.numberOfTrailingZeros(U.arrayIndexScale(SectionRef[].class));
    private static final String TAG_RAD = "hbmRadDataNT";
    private static final byte MAGIC_0 = (byte) 'N';
    private static final byte MAGIC_1 = (byte) 'T';
    private static final byte MAGIC_2 = (byte) 'X';
    private static final byte FMT = 6;
    private static final Object NOT_RES = new Object();
    private static final ForkJoinPool RAD_POOL = ForkJoinPool.commonPool(); // safe: we don't lock in sim path
    private static final ConcurrentMap<WorldServer, WorldRadiationData> worldMap = new ConcurrentHashMap<>(4);
    private static final ThreadLocal<int[]> TL_FF_QUEUE = ThreadLocal.withInitial(() -> new int[SECTION_BLOCK_COUNT]);
    private static final ThreadLocal<PalScratch> TL_PAL_SCRATCH = ThreadLocal.withInitial(PalScratch::new);
    private static final ThreadLocal<int[]> TL_VOL_COUNTS = ThreadLocal.withInitial(() -> new int[NO_POCKET]);
    private static final int ACTIVE_STRIPES = computeActiveStripes(2);
    private static final int ACTIVE_STRIPE_MASK = ACTIVE_STRIPES - 1;
    private static ByteBuffer BUF = ByteBuffer.allocate(524_288);
    private static long ticks;
    private static CompletableFuture<Void> radiationFuture = CompletableFuture.completedFuture(null);
    private static Object[] STATE_CLASS;
    private static int tickDelay = 1;
    private static double dT = tickDelay / 20.0D;
    private static double diffusionDt = (10.0D / 6.0D) * dT;
    private static double retentionDt = Math.pow(0.99424, dT); // 2min
    private static int fogChance = (int) (50.0 / dT);

    static {
        int[] rowShifts = {4, 4, 8, 8, 8, 8}, colShifts = {0, 0, 0, 0, 4, 4}, bases = {0, 15 << 8, 0, 15 << 4, 0, 15};
        for (int face = 0; face < 6; face++) {
            int base = face << 8;
            int rowShift = rowShifts[face];
            int colShift = colShifts[face];
            int fixedBits = bases[face];
            int t = 0;
            for (int r = 0; r < 16; r++) {
                int rBase = r << rowShift;
                for (int c = 0; c < 16; c++) {
                    FACE_PLANE[base + (t++)] = rBase | (c << colShift) | fixedBits;
                }
            }
        }
    }

    private RadiationSystemNT() {
    }

    private static int computeActiveStripes(int scale) {
        int p = RAD_POOL.getParallelism();
        if (p < 3) p = 1;
        int desired = p * scale;
        if (desired > 128) desired = 128;
        return HashCommon.nextPowerOfTwo(desired);
    }

    private static long offInt(int idx) {
        return IA_BASE + ((long) idx << IA_SHIFT);
    }

    private static long offSecRefArr(int idx) {
        return SRA_BASE + ((long) idx << SRA_SHIFT);
    }

    public static void onLoadComplete() {
        // noinspection deprecation
        STATE_CLASS = new Object[Block.BLOCK_STATE_IDS.size() + 1024];
        tickDelay = RadiationConfig.radTickRate;
        if (tickDelay <= 0) throw new IllegalStateException("Radiation tick rate must be positive");
        dT = tickDelay / 20.0D;
        diffusionDt = RadiationConfig.radDiffusivity * dT;
        if (diffusionDt <= 0.0D || !Double.isFinite(diffusionDt))
            throw new IllegalStateException("Radiation diffusivity must be positive and finite");
        double hl = RadiationConfig.radHalfLifeSeconds;
        if (hl <= 0.0D || !Double.isFinite(hl)) throw new IllegalStateException("Radiation HalfLife must be positive and finite");
        retentionDt = Math.exp(Math.log(0.5) * (dT / hl));
        if (diffusionDt > 1.0 / 6.0)
            MainRegistry.logger.warn("[RadiationSystemNT] Radiation diffusion rate per step = {} is higher than 1/6, which may lead to numerical instability", diffusionDt);
        int ch = RadiationConfig.fogCh;
        fogChance = ch <= 0 ? Integer.MAX_VALUE : (int) ((double) ch / dT);
    }

    public static void onServerStarting(FMLServerStartingEvent event) {
    }

    public static void onServerStopping(FMLServerStoppingEvent ignoredEvent) {
        try {
            if (radiationFuture != null) radiationFuture.join();
        } catch (Exception e) {
            MainRegistry.logger.error("Radiation system error during shutdown.", e);
        } finally {
            worldMap.clear();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onServerTickFirst(TickEvent.ServerTickEvent e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation || e.phase != Phase.START) return;
        if (radiationFuture != null && !radiationFuture.isDone()) {
            try {
                // this provides a quiescent server thread and sufficient happens-before for structural updates
                radiationFuture.join();
            } catch (Exception ex) {
                MainRegistry.logger.error("Radiation simulation failed", ex);
            }
        }
        if (ticks % 200 == 13) {
            for (WorldRadiationData wd : worldMap.values()) {
                wd.cleanupPools();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onServerTickLast(TickEvent.ServerTickEvent e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation || e.phase != Phase.END) return;
        ticks++;
        if ((ticks + 17) % tickDelay == 0) {
            radiationFuture = CompletableFuture.runAsync(RadiationSystemNT::runParallelSimulation, RAD_POOL);
        }
    }

    @SubscribeEvent
    public static void onWorldUpdate(TickEvent.WorldTickEvent e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation || e.world.isRemote) return;
        WorldServer worldServer = (WorldServer) e.world;

        if (e.phase == Phase.START) {
            RadiationWorldHandler.handleWorldDestruction(worldServer);
        }
        if (GeneralConfig.enableRads) {
            int thunder = AuxSavedData.getThunder(worldServer);
            if (thunder > 0) AuxSavedData.setThunder(worldServer, thunder - 1);
        }
    }

    @ServerThread
    public static void jettisonData(WorldServer world) {
        WorldRadiationData data = worldMap.get(world);
        if (data == null) return;
        data.clearAllSections();
        data.clearDirtyAll();
        data.clearQueues();
        data.destructionQueue.clear(true);
        data.clearPendingAll();
        data.cleanupPools();
    }

    @ServerThread
    public static void incrementRad(WorldServer world, BlockPos pos, double amount, double max) {
        if (isOutsideWorldY(pos.getY()) || !world.isBlockLoaded(pos)) return;
        if (amount <= 0 || max <= 0 || isResistantAt(world, pos)) return;

        WorldRadiationData data = getWorldRadData(world);
        long worldPosLong = pos.toLong();
        long sck = Library.blockPosToSectionLong(worldPosLong);

        SectionRef sc = ensureSectionForWrite(data, sck);
        if (sc.pocketCount <= 0) return;

        int pocketIndex = sc.getPocketIndex(worldPosLong);
        if (pocketIndex >= 0) {
            double current = sc.getRad(pocketIndex);
            if (current < max) {
                double next = Math.min(current + amount, max);
                sc.setRad(pocketIndex, next);

                long key = pocketKey(sck, pocketIndex);
                data.activatePocket(key);

                Chunk c = sc.owner.getChunk(world.getChunkProvider().loadedChunks);
                if (c != null) c.markDirty();
            }
        }
    }

    @ServerThread
    public static void decrementRad(WorldServer world, BlockPos pos, double amount) {
        if (isOutsideWorldY(pos.getY()) || !world.isBlockLoaded(pos)) return;
        if (amount <= 0 || isResistantAt(world, pos)) return;

        WorldRadiationData data = getWorldRadData(world);
        long worldPosLong = pos.toLong();
        long sck = Library.blockPosToSectionLong(worldPosLong);

        SectionRef sc = ensureSectionForWrite(data, sck);
        int pocketIndex = sc.getPocketIndex(worldPosLong);

        if (pocketIndex >= 0) {
            double current = sc.getRad(pocketIndex);
            double next = Math.max(data.minBound, current - amount);
            sc.setRad(pocketIndex, next);
            long key = pocketKey(sck, pocketIndex);

            if (!nearZero(next)) {
                data.activatePocket(key);
            } else {
                data.deactivatePocket(key);
            }

            Chunk c = sc.owner.getChunk(world.getChunkProvider().loadedChunks);
            if (c != null) c.markDirty();
        }
    }

    public static void setRadForCoord(WorldServer world, BlockPos pos, double amount) {
        if (isOutsideWorldY(pos.getY()) || !world.isBlockLoaded(pos) || isResistantAt(world, pos)) return;

        WorldRadiationData data = getWorldRadData(world);
        long worldPosLong = pos.toLong();
        long sck = Library.blockPosToSectionLong(worldPosLong);

        SectionRef sc = ensureSectionForWrite(data, sck);
        int pocketIndex = sc.getPocketIndex(worldPosLong);

        if (pocketIndex >= 0) {
            double v = Math.max(data.minBound, amount);
            sc.setRad(pocketIndex, v);
            long key = pocketKey(sck, pocketIndex);

            if (!nearZero(v)) {
                data.activatePocket(key);
            } else {
                data.deactivatePocket(key);
            }

            Chunk c = sc.owner.getChunk(world.getChunkProvider().loadedChunks);
            if (c != null) c.markDirty();
        }
    }

    @ServerThread
    public static double getRadForCoord(WorldServer world, BlockPos pos) {
        if (isOutsideWorldY(pos.getY()) || !world.isBlockLoaded(pos)) return 0D;
        WorldRadiationData data = worldMap.get(world);
        if (data == null || isResistantAt(world, pos)) return 0D;

        long posLong = pos.toLong();
        long sck = Library.blockPosToSectionLong(posLong);
        SectionRef sc = data.getSection(sck);
        if (sc == null || sc.pocketCount <= 0) {
            Chunk c = world.getChunkProvider().loadedChunks.get(Library.sectionToChunkLong(sck));
            if (c != null) data.markDirty(sck);
            return 0D;
        }

        int pocketIndex = sc.getPocketIndex(posLong);
        if (pocketIndex >= 0) return sc.getRad(pocketIndex);
        return 0D;
    }

    @ServerThread
    public static void markSectionForRebuild(World world, BlockPos pos) {
        if (world.isRemote || !GeneralConfig.advancedRadiation) return;
        if (isOutsideWorldY(pos.getY()) || !world.isBlockLoaded(pos)) return;

        WorldRadiationData data = getWorldRadData((WorldServer) world);
        long sck = Library.blockPosToSectionLong(pos);
        data.markDirty(sck);

        Chunk c = ((WorldServer) world).getChunkProvider().loadedChunks.get(Library.sectionToChunkLong(sck));
        if (c != null) c.markDirty();
    }

    @ServerThread
    static void handleWorldDestruction(WorldServer world) {
        WorldRadiationData data = worldMap.get(world);
        if (data == null) return;

        long pocketKey;
        if (tickDelay == 1) pocketKey = data.pocketToDestroy;
        else {
            if (data.destructionQueue.isEmpty()) return;
            pocketKey = data.destructionQueue.poll();
        }
        if (pocketKey == Long.MIN_VALUE) return;

        int cx = Library.getSectionX(pocketKey);
        int yz = Library.getSectionY(pocketKey);
        int cz = Library.getSectionZ(pocketKey);
        int cy = yz >>> 4;
        int targetPocketIndex = yz & 15;

        SectionRef sc = data.getSection(Library.sectionToLong(cx, cy, cz));
        if (sc == null) return;

        int baseX = cx << 4;
        int baseY = cy << 4;
        int baseZ = cz << 4;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        Chunk chunk = sc.owner.getChunk(world.getChunkProvider().loadedChunks);
        if (chunk == null) return;
        ExtendedBlockStorage storage = chunk.getBlockStorageArray()[cy];
        if (storage == null || storage.isEmpty()) return;
        BlockStateContainer container = storage.data;

        if (sc.pocketData == null && sc.pocketCount == 1) {
            if (targetPocketIndex != 0) return;
            for (int i = 0; i < SECTION_BLOCK_COUNT; i++) {
                if (world.rand.nextInt(3) != 0) continue;
                IBlockState state = container.get(i);
                pos.setPos(baseX + Library.getLocalX(i), baseY + Library.getLocalY(i), baseZ + Library.getLocalZ(i));
                if (state.getMaterial() == Material.AIR) continue;
                RadiationWorldHandler.decayBlock(world, pos, state, false);
            }
            return;
        }

        for (int i = 0; i < SECTION_BLOCK_COUNT; i++) {
            if (world.rand.nextInt(3) != 0) continue;
            int actualPocketIndex = sc.paletteIndexOrNeg(i);
            if (actualPocketIndex < 0) continue;
            if (actualPocketIndex != targetPocketIndex) continue;
            IBlockState state = container.get(i);
            if (state.getMaterial() == Material.AIR) continue;
            pos.setPos(baseX + Library.getLocalX(i), baseY + Library.getLocalY(i), baseZ + Library.getLocalZ(i));
            RadiationWorldHandler.decayBlock(world, pos, state, false);
        }
    }

    private static void runParallelSimulation() {
        long start = System.nanoTime();
        WorldRadiationData[] all = worldMap.values().toArray(new WorldRadiationData[0]);
        int n = all.length;
        if (n == 0) return;

        if (n == 1) {
            WorldRadiationData data = all[0];
            if (data.world.getMinecraftServer() == null) return;
            try {
                processWorldSimulation(data);
            } catch (Throwable t) {
                MainRegistry.logger.error("Error in async rad simulation for world {}", data.world.provider.getDimension(), t);
            }
        } else {
            ForkJoinTask<?>[] tasks = new ForkJoinTask<?>[n];
            for (int i = 0; i < n; i++) {
                final WorldRadiationData data = all[i];
                tasks[i] = ForkJoinTask.adapt(() -> {
                    if (data.world.getMinecraftServer() == null) return;
                    try {
                        processWorldSimulation(data);
                    } catch (Throwable t) {
                        MainRegistry.logger.error("Error in async rad simulation for world {}", data.world.provider.getDimension(), t);
                    }
                });
            }
            ForkJoinTask.invokeAll(tasks);
        }
        MainRegistry.logger.info((System.nanoTime() - start) / 1_000_000.0);
    }

    private static void processWorldSimulation(WorldRadiationData data) {
        rebuildDirtySections(data);
        data.swapQueues();

        final int epoch = data.nextWorkEpoch();

        // parallel drain each stripe into its local buffer
        final MpscUnboundedXaddArrayLongQueue[] qs = data.activeQueuesCurrent;

        if (ACTIVE_STRIPES == 1) {
            new DrainStripeTask(data, qs[0], 0, epoch).invoke();
        } else {
            ForkJoinTask<?>[] drainTasks = new ForkJoinTask<?>[ACTIVE_STRIPES];
            for (int s = 0; s < ACTIVE_STRIPES; s++) {
                drainTasks[s] = new DrainStripeTask(data, qs[s], s, epoch);
            }
            ForkJoinTask.invokeAll(drainTasks);
        }

        // flatten stripe buffers into data.activeBuf
        int activeCount = 0;
        for (int s = 0; s < ACTIVE_STRIPES; s++) activeCount += data.activeStripeCounts[s];

        long[] buf = data.activeBuf;
        if (buf.length < activeCount) {
            int n = buf.length;
            while (n < activeCount) n = n + (n >>> 1) + 16;
            buf = data.activeBuf = Arrays.copyOf(buf, n);
        }

        int pos = 0;
        for (int s = 0; s < ACTIVE_STRIPES; s++) {
            int c = data.activeStripeCounts[s];
            if (c == 0) continue;
            long[] sb = data.activeStripeBufs[s];
            System.arraycopy(sb, 0, buf, pos, c);
            pos += c;
        }

        final int maxWake = (activeCount <= (Integer.MAX_VALUE / 90)) ? (activeCount * 90) : Integer.MAX_VALUE;
        final LongBag wokenBag = data.getWokenBag(maxWake);
        wokenBag.clear();

        if (activeCount > 0) {
            new ComputeAndScanTask(data, data.activeBuf, 0, activeCount, epoch, wokenBag).invoke();
        }

        final int wokenCount = wokenBag.size();
        if (activeCount > 0 && wokenCount > 0) {
            ForkJoinTask.invokeAll(new ApplyTask(data, data.activeBuf, 0, activeCount), new ApplyBagTask(data, wokenBag));
        } else if (activeCount > 0) {
            new ApplyTask(data, data.activeBuf, 0, activeCount).invoke();
        } else if (wokenCount > 0) {
            new ApplyBagTask(data, wokenBag).invoke();
        }
    }

    private static void rebuildDirtySections(WorldRadiationData data) {
        if (data.dirtyQueue.isEmpty()) return;
        final LongArrayList toRebuild = data.dirtyToRebuildScratch;
        final Long2IntOpenHashMap chunkMasks = data.dirtyChunkMasksScratch;
        toRebuild.clear();
        chunkMasks.clear();

        while (true) {
            final long sck = data.dirtyQueue.poll();
            if (sck == Long.MIN_VALUE) break;
            if (!data.dirtySections.remove(sck)) continue;
            final int sy = Library.getSectionY(sck);
            if (isInvalidSectionY(sy)) continue;
            toRebuild.add(sck);
            final long ck = Library.sectionToChunkLong(sck);
            chunkMasks.put(ck, chunkMasks.get(ck) | (1 << sy));
        }

        final int size = toRebuild.size();
        if (size == 0) return;

        final int batchCount = chunkMasks.size();
        data.ensureDirtyBatchCapacity(batchCount);

        final long[] chunkKeys = data.dirtyChunkKeysScratch;
        final int[] masks = data.dirtyChunkMasksScratchArr;

        int bi = 0;
        ObjectIterator<Long2IntMap.Entry> iterator = chunkMasks.long2IntEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Long2IntMap.Entry e = iterator.next();
            chunkKeys[bi] = e.getLongKey();
            masks[bi] = e.getIntValue();
            bi++;
        }

        new RebuildChunkBatchTask(data, chunkKeys, masks, 0, batchCount).invoke();
        data.relinkKeys(toRebuild.elements(), size);
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation) return;
        if (e.getWorld().isRemote) return;

        WorldRadiationData data = getWorldRadData((WorldServer) e.getWorld());
        Chunk chunk = e.getChunk();
        data.onChunkLoaded(chunk);
        for (int sy = 0; sy < 16; sy++) {
            long sck = Library.sectionToLong(chunk.x, sy, chunk.z);
            if (data.getSection(sck) == null) data.markDirty(sck);
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation) return;
        if (e.getWorld().isRemote) return;

        WorldRadiationData data = getWorldRadData((WorldServer) e.getWorld());
        Chunk chunk = e.getChunk();
        data.unloadChunk(chunk.x, chunk.z);
    }

    @SubscribeEvent
    public static void onChunkDataLoad(ChunkDataEvent.Load e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation) return;
        if (e.getWorld().isRemote) return;
        WorldRadiationData data = getWorldRadData((WorldServer) e.getWorld());

        Chunk chunk = e.getChunk();
        try {
            byte[] payload = null;
            NBTTagCompound nbt = e.getData();
            int id = nbt.getTagId(TAG_RAD);
            if (id == Constants.NBT.TAG_COMPOUND) {
                WorldProvider provider = e.getWorld().provider;
                MainRegistry.logger.warn("[RadiationSystemNT] Skipped legacy radiation data for chunk {} in dimension {} ({})", chunk.getPos(), provider.getDimension(), provider.getDimensionType().getName());
            } else if (id == Constants.NBT.TAG_BYTE_ARRAY) {
                byte[] raw = nbt.getByteArray(TAG_RAD);
                payload = verifyPayload(raw);
            }
            data.readPayload(chunk.x, chunk.z, payload);
        } catch (BufferUnderflowException | DecodeException ex) {
            WorldProvider provider = e.getWorld().provider;
            MainRegistry.logger.error("[RadiationSystemNT] Failed to decode data for chunk {} in dimension {} ({})", chunk.getPos(), provider.getDimension(), provider.getDimensionType().getName(), ex);
        }
    }

    @SubscribeEvent
    public static void onChunkDataSave(ChunkDataEvent.Save e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation) return;
        if (e.getWorld().isRemote) return;
        WorldRadiationData data = getWorldRadData((WorldServer) e.getWorld());

        Chunk chunk = e.getChunk();
        byte[] payload = tryEncodePayload(data, chunk.x, chunk.z);

        if (payload != null && payload.length > 0) {
            e.getData().setByteArray(TAG_RAD, payload);
        } else if (e.getData().hasKey(TAG_RAD)) {
            e.getData().removeTag(TAG_RAD);
        }
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation) return;
        if (e.getWorld().isRemote) return;
        worldMap.computeIfAbsent((WorldServer) e.getWorld(), WorldRadiationData::new);
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload e) {
        if (e.getWorld().isRemote) return;
        worldMap.remove((WorldServer) e.getWorld());
    }

    private static byte[] verifyPayload(byte[] raw) throws DecodeException {
        if (raw.length == 0) return null;
        if (raw.length < 6) throw new DecodeException("Payload too short: " + raw.length);
        if (raw[0] != MAGIC_0 || raw[1] != MAGIC_1 || raw[2] != MAGIC_2) throw new DecodeException("Invalid magic");
        byte fmt = raw[3];
        if (fmt != FMT) throw new DecodeException("Unknown format: " + fmt);
        return raw;
    }

    private static @Nullable byte[] tryEncodePayload(WorldRadiationData data, int cx, int cz) {
        ByteBuffer buf = BUF; // by contract ChunkDataEvent.Save is never called off-thread
        buf.clear();
        buf = ensureCapacity(buf, 2);
        buf.putShort((short) 0);
        int count = 0;

        for (int sy = 0; sy < 16; sy++) {
            long sck = Library.sectionToLong(cx, sy, cz);
            SectionRef sc = data.getSection(sck);
            if (sc == null || sc.pocketCount <= 0) continue;

            int len = sc.pocketCount & 0xFF;
            if (len > NO_POCKET) len = NO_POCKET;
            if (sc.pocketData == null && sc.pocketCount == 1) len = 1;

            for (int p = 0; p < len; p++) {
                double v = sc.getRad(p);
                if (!Double.isFinite(v) || nearZero(v)) continue;
                buf = ensureCapacity(buf, 1 + 8);
                buf.put((byte) ((sy << 4) | (p & 15)));
                buf.putDouble(v);
                count++;
            }
        }

        BUF = buf;
        if (count == 0) return null;
        buf.putShort(0, (short) count);
        buf.flip();
        byte[] out = new byte[4 + buf.limit()];
        out[0] = MAGIC_0;
        out[1] = MAGIC_1;
        out[2] = MAGIC_2;
        out[3] = FMT;
        buf.get(out, 4, buf.limit());
        return out;
    }

    private static ByteBuffer ensureCapacity(ByteBuffer buf, int extra) {
        if (buf.remaining() >= extra) return buf;
        int need = buf.position() + extra;
        int ncap = buf.capacity();
        while (ncap < need) ncap = ncap + (ncap >>> 1) + 64;
        ByteBuffer nb = ByteBuffer.allocate(ncap);
        buf.flip();
        nb.put(buf);
        return nb;
    }

    @NotNull
    private static WorldRadiationData getWorldRadData(WorldServer world) {
        return worldMap.computeIfAbsent(world, WorldRadiationData::new);
    }

    private static boolean isResistantAt(WorldServer w, BlockPos pos) {
        if (!(w.getBlockState(pos).getBlock() instanceof IRadResistantBlock r)) return false;
        return r.isRadResistant(w, pos);
    }

    private static boolean isOutsideWorldY(int y) {
        return (y & ~255) != 0;
    }

    private static boolean isInvalidSectionY(int sy) {
        return (sy & ~15) != 0;
    }

    private static boolean nearZero(double v) {
        return Math.abs(v) < 1.0e-5d;
    }

    private static long pocketKey(long sectionKey, int pocketIndex) {
        int sy = Library.getSectionY(sectionKey);
        // sectionKey allows sy ∈ [-524_288, 524_287]
        return Library.setSectionY(sectionKey, (sy << 4) | (pocketIndex & 15));
    }

    private static int pocketIndexFromPocketKey(long pocketKey) {
        return Library.getSectionY(pocketKey) & 15;
    }

    private static long sectionKeyFromPocketKey(long pocketKey) {
        int yz = Library.getSectionY(pocketKey);
        return Library.setSectionY(pocketKey, (yz >>> 4) & 15);
    }

    private static long offsetKey(long key, int fo) {
        return Library.sectionToLong(Library.getSectionX(key) + FACE_DX[fo], Library.getSectionY(key) + FACE_DY[fo], Library.getSectionZ(key) + FACE_DZ[fo]);
    }

    private static void writeNibble(byte[] pocketData, int blockIndex, int paletteIndex) {
        int byteIndex = blockIndex >> 1;
        int b = pocketData[byteIndex] & 0xFF;
        if ((blockIndex & 1) == 0) {
            b = (b & 0x0F) | ((paletteIndex & 0x0F) << 4);
        } else {
            b = (b & 0xF0) | (paletteIndex & 0x0F);
        }
        pocketData[byteIndex] = (byte) b;
    }

    private static int readNibble(byte[] pocketData, int blockIndex) {
        int byteIndex = blockIndex >> 1;
        int b = pocketData[byteIndex] & 0xFF;
        return ((blockIndex & 1) == 0) ? ((b >> 4) & 0x0F) : (b & 0x0F);
    }

    private static SectionRef ensureSectionForWrite(WorldRadiationData data, long sectionKey) {
        SectionRef sc = data.getSection(sectionKey);
        if (sc != null && sc.pocketCount > 0) return sc;
        int sy = Library.getSectionY(sectionKey);
        long ck = Library.sectionToChunkLong(sectionKey);
        ChunkRef owner = data.getOrCreateChunkRef(ck);
        SectionRef stub = SectionRef.singleUniform(owner, sy);
        SectionRef prev = owner.getSec(sy);
        U.putIntRelease(owner.activePocketMasks, offInt(sy), 0);
        owner.setSec(sy, stub);
        owner.setUniformBit(sy, true);
        if (prev != null) {
            int prevLen = prev.pocketCount & 0xFF;
            for (int p = 0; p < prevLen; p++) data.deactivatePocket(pocketKey(sectionKey, p));
            data.retireIfNeeded(prev);
        }

        data.markDirty(sectionKey);
        return stub;
    }

    @SuppressWarnings("deprecation")
    private static SectionMask scanResistantMask(WorldServer world, long sectionKey, ExtendedBlockStorage ebs) {
        if (ebs == null || ebs.isEmpty()) return null;

        BlockStateContainer c = ebs.getData();
        BitArray storage = c.storage;
        IBlockStatePalette pal = c.palette;

        int baseX = Library.getSectionX(sectionKey) << 4;
        int baseY = Library.getSectionY(sectionKey) << 4;
        int baseZ = Library.getSectionZ(sectionKey) << 4;
        BlockPos.MutableBlockPos mp = new BlockPos.MutableBlockPos();

        long[] data = storage.getBackingLongArray();
        int bits = c.bits;
        long entryMask = (1L << bits) - 1L;
        int stateSize = Block.BLOCK_STATE_IDS.size();
        Object[] cache = STATE_CLASS;
        if (cache == null || cache.length < stateSize) cache = ensureStateClassCapacity(stateSize);

        if (pal == BlockStateContainer.REGISTRY_BASED_PALETTE) {
            SectionMask mask = null;
            int li = 0, bo = 0;

            for (int idx = 0; idx < SECTION_BLOCK_COUNT; idx++) {
                int globalId;
                if (bo + bits <= 64) {
                    globalId = (int) ((data[li] >>> bo) & entryMask);
                    bo += bits;
                    if (bo == 64) {
                        bo = 0;
                        li++;
                    }
                } else {
                    int spill = 64 - bo;
                    long v = (data[li] >>> bo) | (data[li + 1] << spill);
                    globalId = (int) (v & entryMask);
                    li++;
                    bo = bits - spill;
                }

                if (globalId < 0 || globalId >= stateSize) {
                    int newSize = Block.BLOCK_STATE_IDS.size();
                    if (globalId < 0 || globalId >= newSize) continue;
                    stateSize = newSize;
                    if (cache.length < stateSize) cache = ensureStateClassCapacity(stateSize);
                }

                Object cls = cache[globalId];
                if (cls == null) {
                    IBlockState s = Block.BLOCK_STATE_IDS.getByValue(globalId);
                    if (s == null) {
                        cache[globalId] = NOT_RES;
                        continue;
                    }
                    Block b = s.getBlock();
                    Object nv = (b instanceof IRadResistantBlock) ? b : NOT_RES;
                    cache[globalId] = nv;
                    cls = nv;
                }

                if (cls == NOT_RES) continue;

                int x = Library.getLocalX(idx);
                int y = Library.getLocalY(idx);
                int z = Library.getLocalZ(idx);
                mp.setPos(baseX + x, baseY + y, baseZ + z);

                if (((IRadResistantBlock) cls).isRadResistant(world, mp)) {
                    if (mask == null) mask = new SectionMask();
                    mask.set(idx);
                }
            }
            return mask;
        }

        PalScratch sc = TL_PAL_SCRATCH.get();
        int gen = sc.nextGen();
        Object[] lcls = sc.cls;
        int[] lstamp = sc.stamp;

        boolean anyCandidate = false;

        if (pal instanceof BlockStatePaletteLinear p) {
            IBlockState[] states = p.states;
            int n = p.arraySize;

            for (int i = 0; i < n; i++) {
                IBlockState s = states[i];
                if (s == null) continue;

                int gid = Block.BLOCK_STATE_IDS.get(s);
                Object cls;
                if (gid < 0) {
                    cls = NOT_RES;
                } else {
                    if (gid >= stateSize) {
                        int newSize = Block.BLOCK_STATE_IDS.size();
                        if (gid >= newSize) {
                            cls = NOT_RES;
                        } else {
                            stateSize = newSize;
                            if (cache.length < stateSize) cache = ensureStateClassCapacity(stateSize);
                            cls = cache[gid];
                        }
                    } else {
                        cls = cache[gid];
                    }

                    if (cls == null) {
                        Block b = s.getBlock();
                        Object nv = (b instanceof IRadResistantBlock) ? b : NOT_RES;
                        cache[gid] = nv;
                        cls = nv;
                    }
                }

                lcls[i] = cls;
                lstamp[i] = gen;
                if (cls != NOT_RES) anyCandidate = true;
            }

            if (!anyCandidate) return null;

            SectionMask mask = null;
            int li = 0, bo = 0;

            for (int idx = 0; idx < SECTION_BLOCK_COUNT; idx++) {
                int localId;
                if (bo + bits <= 64) {
                    localId = (int) ((data[li] >>> bo) & entryMask);
                    bo += bits;
                    if (bo == 64) {
                        bo = 0;
                        li++;
                    }
                } else {
                    int spill = 64 - bo;
                    long v = (data[li] >>> bo) | (data[li + 1] << spill);
                    localId = (int) (v & entryMask);
                    li++;
                    bo = bits - spill;
                }
                if ((localId & ~255) != 0) continue;
                if (lstamp[localId] != gen) continue;
                Object cls = lcls[localId];
                if (cls == NOT_RES) continue;

                int x = Library.getLocalX(idx);
                int y = Library.getLocalY(idx);
                int z = Library.getLocalZ(idx);
                mp.setPos(baseX + x, baseY + y, baseZ + z);

                if (((IRadResistantBlock) cls).isRadResistant(world, mp)) {
                    if (mask == null) mask = new SectionMask();
                    mask.set(idx);
                }
            }
            return mask;
        }

        if (pal instanceof BlockStatePaletteHashMap p) {
            IntIdentityHashBiMap<IBlockState> map = p.statePaletteMap;
            Object[] byId = map.byId; // erasure

            int cap = 1 << bits;
            int lim = Math.min(cap, byId.length);

            for (int i = 0; i < lim; i++) {
                IBlockState s = (IBlockState) byId[i];
                if (s == null) continue;

                int gid = Block.BLOCK_STATE_IDS.get(s);
                Object cls;
                if (gid < 0) {
                    cls = NOT_RES;
                } else {
                    if (gid >= stateSize) {
                        int newSize = Block.BLOCK_STATE_IDS.size();
                        if (gid >= newSize) {
                            cls = NOT_RES;
                        } else {
                            stateSize = newSize;
                            if (cache.length < stateSize) cache = ensureStateClassCapacity(stateSize);
                            cls = cache[gid];
                        }
                    } else {
                        cls = cache[gid];
                    }

                    if (cls == null) {
                        Block b = s.getBlock();
                        Object nv = (b instanceof IRadResistantBlock) ? b : NOT_RES;
                        cache[gid] = nv;
                        cls = nv;
                    }
                }

                lcls[i] = cls;
                lstamp[i] = gen;
                if (cls != NOT_RES) anyCandidate = true;
            }

            if (!anyCandidate) return null;

            SectionMask mask = null;
            int li2 = 0, bo2 = 0;

            for (int idx = 0; idx < SECTION_BLOCK_COUNT; idx++) {
                int localId;
                if (bo2 + bits <= 64) {
                    localId = (int) ((data[li2] >>> bo2) & entryMask);
                    bo2 += bits;
                    if (bo2 == 64) {
                        bo2 = 0;
                        li2++;
                    }
                } else {
                    int spill = 64 - bo2;
                    long v = (data[li2] >>> bo2) | (data[li2 + 1] << spill);
                    localId = (int) (v & entryMask);
                    li2++;
                    bo2 = bits - spill;
                }

                if ((localId & ~255) != 0) continue;
                if (localId >= lim) continue;
                if (lstamp[localId] != gen) continue;

                Object cls = lcls[localId];
                if (cls == NOT_RES) continue;

                int x = Library.getLocalX(idx);
                int y = Library.getLocalY(idx);
                int z = Library.getLocalZ(idx);
                mp.setPos(baseX + x, baseY + y, baseZ + z);

                if (((IRadResistantBlock) cls).isRadResistant(world, mp)) {
                    if (mask == null) mask = new SectionMask();
                    mask.set(idx);
                }
            }
            return mask;
        }

        throw new UnsupportedOperationException("Unexpected palette format: " + pal.getClass());
    }

    // it seems that the size of total blockstate id count can fucking grow after FMLLoadCompleteEvent, making STATE_CLASS throw AIOOBE
    // I can't explain it, either there are registration happening after that event, or that ObjectIntIdentityMap went out
    // of sync internally (it uses IdentityHashMap to map blockstate to id, with an ArrayList to map ids back)
    // Anyway, we introduce a manual resize here to address this weird growth issue.
    private static Object[] ensureStateClassCapacity(int minSize) {
        Object[] a = STATE_CLASS;
        if (a != null && a.length >= minSize) return a;
        synchronized (RadiationSystemNT.class) {
            a = STATE_CLASS;
            if (a != null && a.length >= minSize) return a;

            int newLen = (a == null) ? 256 : a.length;
            while (newLen < minSize) newLen = newLen + (newLen >>> 1) + 16;
            STATE_CLASS = (a == null) ? new Object[newLen] : Arrays.copyOf(a, newLen);
            return STATE_CLASS;
        }
    }

    private static int scanBoundaryAreaSingleToSingle(SectionRef aSingle, SectionRef bSingle, int faceA) {
        int faceB = faceA ^ 1; // EnumFacing opposite (0D<->1U, 2N<->3S, 4W<->5E)
        if (aSingle.pocketData == null && bSingle.pocketData == null) return 256;

        boolean aOpenAll = (aSingle.pocketData == null);
        boolean bOpenAll = (bSingle.pocketData == null);
        int baseA = faceA << 8;
        int baseB = faceB << 8;
        int area = 0;
        for (int t = 0; t < 256; t++) {
            if (!aOpenAll) {
                int pa = aSingle.paletteIndexOrNeg(FACE_PLANE[baseA + t]);
                if (pa < 0) continue;
            }
            if (!bOpenAll) {
                int pb = bSingle.paletteIndexOrNeg(FACE_PLANE[baseB + t]);
                if (pb < 0) continue;
            }
            area++;
        }
        return area;
    }

    private static void applyRange(WorldRadiationData data, long[] keys, int start, int end) {
        Long2ObjectMap<Chunk> loaded = data.world.getChunkProvider().loadedChunks;
        double minB = data.minBound;
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        for (int i = start; i < end; i++) {
            long pk = keys[i];
            long curKey = sectionKeyFromPocketKey(pk);
            int pi = pocketIndexFromPocketKey(pk);

            SectionRef sc = data.getSection(curKey);
            if (sc != null && sc.pocketCount > 0) {
                int len = sc.pocketCount & 0xFF;
                if (pi >= 0 && pi < len) {
                    double prev = sc.getRad(pi);
                    double delta = sc.getDelta(pi);

                    double next = (prev + delta) * retentionDt;
                    if (!Double.isFinite(next)) next = minB;
                    if (next < minB) next = minB;
                    if (nearZero(next)) next = 0.0d;

                    if (next != prev) {
                        sc.setRad(pi, next);
                        if (next == 0.0d) data.deactivatePocket(pk);
                        else data.enqueueActiveNext(pk);

                        Chunk c = sc.owner.getChunk(loaded);
                        if (c != null) c.markDirty();

                        if (prev >= 5.0 && rnd.nextDouble() < 0.01 && pk != Long.MIN_VALUE) {
                            if (tickDelay == 1) data.pocketToDestroy = pk;
                            else data.destructionQueue.offer(pk);
                        }
                    } else if (nearZero(next)) {
                        data.deactivatePocket(pk);
                    } else {
                        data.enqueueActiveNext(pk);
                    }

                    sc.setDelta(pi, 0.0d);
                    continue;
                }
            }

            data.deactivatePocket(pk);
        }
    }

    private static final class ChunkRef {
        final long ck;
        final SectionRef[] sec = new SectionRef[16];
        final int[] activePocketMasks = new int[16]; // [sy] -> bitmask of active pockets
        int uniformMask; // written by server thread, read by workers
        ChunkRef north, south, west, east; // written by server thread, read by workers
        @Deprecated// don't use directly
        volatile Chunk mcChunk;

        ChunkRef(long ck) {
            this.ck = ck;
        }

        boolean isActive(int sy, int pi) {
            int mask = U.getIntVolatile(activePocketMasks, offInt(sy));
            return (mask & (1 << pi)) != 0;
        }

        boolean setActiveBit(int sy, int pi) {
            long offset = offInt(sy);
            int bit = 1 << pi;
            while (true) {
                int cur = U.getIntVolatile(activePocketMasks, offset);
                if ((cur & bit) != 0) return false; // already set
                int next = cur | bit;
                if (U.compareAndSetInt(activePocketMasks, offset, cur, next)) return true; // transitioned 0->1
            }
        }

        void clearActiveBit(int sy, int pi) {
            long offset = offInt(sy);
            int bit = 1 << pi;
            while (true) {
                int cur = U.getIntVolatile(activePocketMasks, offset);
                if ((cur & bit) == 0) return; // already clear
                int next = cur & ~bit;
                if (U.compareAndSetInt(activePocketMasks, offset, cur, next)) return;
            }
        }

        Chunk getChunk(Long2ObjectMap<Chunk> loaded) {
            Chunk c = mcChunk;
            return c != null && c.loaded ? c : (mcChunk = loaded.get(ck));
        }

        @Nullable SectionRef getSec(int sy) {
            return (SectionRef) U.getReference(sec, offSecRefArr(sy));
        }

        void setSec(int sy, @Nullable SectionRef v) {
            U.putReference(sec, offSecRefArr(sy), v);
        }

        boolean isUniform(int sy) {
            return ((uniformMask >>> sy) & 1) != 0;
        }

        // single writer
        void setUniformBit(int sy, boolean on) {
            int mask = uniformMask;
            if (on) mask |= (1 << sy);
            else mask &= ~(1 << sy);
            uniformMask = mask;
        }

        @Nullable ChunkRef neighborByFace(int face) {
            return switch (face) {
                case 2 -> north;
                case 3 -> south;
                case 4 -> west;
                case 5 -> east;
                default -> null;
            };
        }
    }

    private static final class PalScratch {
        final Object[] cls = new Object[256]; // localId -> (IRadResistantBlock instance) or NOT_RES
        final int[] stamp = new int[256];     // localId -> generation
        int gen = 1;

        int nextGen() {
            int g = gen + 1;
            gen = g == 0 ? 1 : g;
            return gen;
        }
    }

    private static final class SectionMask {
        final long[] words = new long[64];

        boolean get(int bit) {
            int w = bit >>> 6;
            return (words[w] & (1L << (bit & 63))) != 0L;
        }

        void set(int bit) {
            int w = bit >>> 6;
            words[w] |= (1L << (bit & 63));
        }

        boolean isEmpty() {
            for (long w : words) if (w != 0L) return false;
            return true;
        }
    }

    private static abstract class PocketTask extends RecursiveAction {
        final int lo, hi;

        PocketTask(int lo, int hi) {
            this.lo = lo;
            this.hi = hi;
        }

        protected abstract void work(int start, int end);

        protected abstract PocketTask createSubtask(int start, int end);

        @Override
        protected void compute() {
            if (hi - lo <= /*heuristic*/ 128) {
                work(lo, hi);
            } else {
                int mid = (lo + hi) >>> 1;
                invokeAll(createSubtask(lo, mid), createSubtask(mid, hi));
            }
        }
    }

    private static abstract class BagTask extends RecursiveAction {
        final LongBag bag;
        final int lo, hi;

        BagTask(LongBag bag, int lo, int hi) {
            this.bag = bag;
            this.lo = lo;
            this.hi = hi;
        }

        protected abstract void work(long[] chunk, int start, int end);

        protected abstract BagTask createSubtask(int start, int end);

        @Override
        protected void compute() {
            int n = hi - lo;
            if (n <= 0) return;

            int c1 = lo >>> LongBag.CHUNK_SHIFT;
            int c2 = (hi - 1) >>> LongBag.CHUNK_SHIFT;

            if (c1 == c2) {
                long[] chunk = bag.chunks[c1];
                if (chunk == null) return;
                int startIn = lo & LongBag.CHUNK_MASK;
                int endIn = hi - (c1 << LongBag.CHUNK_SHIFT);
                work(chunk, startIn, endIn);
                return;
            }

            if (n <= LongBag.CHUNK_SIZE) {
                int split = (c1 + 1) << LongBag.CHUNK_SHIFT;
                invokeAll(createSubtask(lo, split), createSubtask(split, hi));
                return;
            }

            int mid = (lo + hi) >>> 1;
            int aligned = (mid >>> LongBag.CHUNK_SHIFT) << LongBag.CHUNK_SHIFT;
            if (aligned > lo && aligned < hi) mid = aligned;
            invokeAll(createSubtask(lo, mid), createSubtask(mid, hi));
        }
    }

    private static abstract class ChunkTask extends RecursiveAction {
        final int lo, hi;

        ChunkTask(int lo, int hi) {
            this.lo = lo;
            this.hi = hi;
        }

        protected abstract void work(int start, int end);

        protected abstract ChunkTask createSubtask(int start, int end);

        @Override
        protected void compute() {
            if (hi - lo <= /*heuristic*/ 8) {
                work(lo, hi);
            } else {
                int mid = (lo + hi) >>> 1;
                invokeAll(createSubtask(lo, mid), createSubtask(mid, hi));
            }
        }
    }

    private static final class RebuildChunkBatchTask extends ChunkTask {
        final WorldRadiationData data;
        final long[] chunkKeys;
        final int[] masks;

        RebuildChunkBatchTask(WorldRadiationData data, long[] chunkKeys, int[] masks, int lo, int hi) {
            super(lo, hi);
            this.data = data;
            this.chunkKeys = chunkKeys;
            this.masks = masks;
        }

        @Override
        protected void work(int start, int end) {
            Long2ObjectMap<Chunk> loadedChunks = data.world.getChunkProvider().loadedChunks;
            for (int i = start; i < end; i++) {
                long ck = chunkKeys[i];
                int mask = masks[i];
                int cx = Library.getChunkPosX(ck);
                int cz = Library.getChunkPosZ(ck);
                Chunk chunk = loadedChunks.get(ck);
                if (chunk == null) {
                    int m = mask;
                    while (m != 0) {
                        int sy = Integer.numberOfTrailingZeros(m);
                        m &= (m - 1);
                        data.markDirty(Library.sectionToLong(cx, sy, cz));
                    }
                    continue;
                }

                data.onChunkLoaded(chunk);

                ExtendedBlockStorage[] stor = chunk.getBlockStorageArray();
                int m = mask;
                while (m != 0) {
                    int sy = Integer.numberOfTrailingZeros(m);
                    m &= (m - 1);
                    long sck = Library.sectionToLong(cx, sy, cz);
                    data.rebuildChunkPocketsLoaded(sck, stor[sy]);
                }
            }
        }

        @Override
        protected ChunkTask createSubtask(int start, int end) {
            return new RebuildChunkBatchTask(data, chunkKeys, masks, start, end);
        }
    }

    private static final class DrainStripeTask extends RecursiveAction {
        final WorldRadiationData data;
        final MpscUnboundedXaddArrayLongQueue q;
        final int stripe;
        final int epoch;

        DrainStripeTask(WorldRadiationData data, MpscUnboundedXaddArrayLongQueue q, int stripe, int epoch) {
            this.data = data;
            this.q = q;
            this.stripe = stripe;
            this.epoch = epoch;
        }

        @Override
        protected void compute() {
            long[] out = data.activeStripeBufs[stripe];
            if (out == null) out = new long[256];

            int count = 0;
            long pk;

            while ((pk = q.relaxedPoll()) != Long.MIN_VALUE) {
                long sck = sectionKeyFromPocketKey(pk);
                long ck = Library.sectionToChunkLong(sck);
                ChunkRef cr = data.chunkRefs.get(ck);
                if (cr == null) continue;

                int yz = Library.getSectionY(pk);
                int sy = (yz >>> 4) & 15;
                int pi = yz & 15;

                if (!cr.isActive(sy, pi)) continue;

                SectionRef sc = cr.getSec(sy);
                if (sc == null || sc.pocketCount <= 0) {
                    cr.clearActiveBit(sy, pi);
                    continue;
                }

                int len = sc.pocketCount & 0xFF;
                if (pi >= len) {
                    cr.clearActiveBit(sy, pi);
                    continue;
                }

                // Stripe guarantees all duplicates for this pocket land here, so non-CAS epoch mark is OK.
                if (!sc.markEpochIfNot(pi, epoch)) continue;

                if (count == out.length) out = Arrays.copyOf(out, out.length << 1);
                out[count++] = pk;
            }

            data.activeStripeBufs[stripe] = out;
            data.activeStripeCounts[stripe] = count;
        }
    }

    private static final class ComputeAndScanTask extends PocketTask {
        final WorldRadiationData data;
        final long[] active;
        final int epoch;
        final LongBag wakeBag;

        ComputeAndScanTask(WorldRadiationData data, long[] active, int lo, int hi, int epoch, LongBag wakeBag) {
            super(lo, hi);
            this.data = data;
            this.active = active;
            this.epoch = epoch;
            this.wakeBag = wakeBag;
        }

        @Override
        protected void work(int start, int end) {
            final ThreadLocalRandom rnd = ThreadLocalRandom.current();
            for (int i = start; i < end; i++) {
                long pk = active[i];
                long curKey = sectionKeyFromPocketKey(pk);
                int pi = pocketIndexFromPocketKey(pk);
                SectionRef sc = data.getSection(curKey);
                if (sc == null || sc.pocketCount <= 0) continue;

                int len = sc.pocketCount & 0xFF;
                if (pi < 0 || pi >= len) {
                    data.deactivatePocket(pk);
                    continue;
                }

                if (sc.getRad(pi) > RadiationConfig.fogRad && rnd.nextInt(fogChance) == 0) {
                    data.spawnFog(sc, pi, rnd);
                }

                double delta = sc.calculateFluxAndScan(data, curKey, pi, epoch, wakeBag);
                sc.setDelta(pi, delta);
            }
        }

        @Override
        protected PocketTask createSubtask(int start, int end) {
            return new ComputeAndScanTask(data, active, start, end, epoch, wakeBag);
        }
    }

    private static final class ApplyTask extends PocketTask {
        final WorldRadiationData data;
        final long[] keys;

        ApplyTask(WorldRadiationData data, long[] keys, int lo, int hi) {
            super(lo, hi);
            this.data = data;
            this.keys = keys;
        }

        @Override
        protected void work(int start, int end) {
            applyRange(data, keys, start, end);
        }

        @Override
        protected PocketTask createSubtask(int start, int end) {
            return new ApplyTask(data, keys, start, end);
        }
    }

    private static final class ApplyBagTask extends BagTask {
        final WorldRadiationData data;

        ApplyBagTask(WorldRadiationData data, LongBag bag) {
            super(bag, 0, bag.size());
            this.data = data;
        }

        private ApplyBagTask(WorldRadiationData data, LongBag bag, int lo, int hi) {
            super(bag, lo, hi);
            this.data = data;
        }

        @Override
        protected void work(long[] chunk, int start, int end) {
            applyRange(data, chunk, start, end);
        }

        @Override
        protected BagTask createSubtask(int start, int end) {
            return new ApplyBagTask(data, bag, start, end);
        }
    }

    private static final class LinkDirTask extends RecursiveAction {
        final WorldRadiationData data;
        final long[] keys;
        final int lo, hi;
        final int canonicalFace;

        LinkDirTask(WorldRadiationData data, long[] keys, int lo, int hi, int canonicalFace) {
            this.data = data;
            this.keys = keys;
            this.lo = lo;
            this.hi = hi;
            this.canonicalFace = canonicalFace;
        }

        @Override
        protected void compute() {
            int n = hi - lo;
            if (n <= 64) {
                for (int i = lo; i < hi; i++) data.linkCanonical(keys[i], canonicalFace);
            } else {
                int mid = (lo + hi) >>> 1;
                invokeAll(new LinkDirTask(data, keys, lo, mid, canonicalFace), new LinkDirTask(data, keys, mid, hi, canonicalFace));
            }
        }
    }

    private static final class SectionBuffers {
        final char[] connectionArea = new char[NO_POCKET * 6 * NEI_SLOTS];
        final byte[] faceActive = new byte[NO_POCKET * 6];
        final long[] radBits = new long[NO_POCKET];
        final double[] deltas = new double[NO_POCKET]; // embedded delta buffer
        final int[] volume = new int[NO_POCKET];
        final double[] invVolume = new double[NO_POCKET];
        final int[] neighborMarkEpoch = new int[NO_POCKET];

        void reset() {
            Arrays.fill(connectionArea, (char) 0);
            Arrays.fill(faceActive, (byte) 0);
            Arrays.fill(radBits, 0L);
            Arrays.fill(deltas, 0.0d);
            Arrays.fill(volume, 0);
            Arrays.fill(invVolume, 0.0d);
            Arrays.fill(neighborMarkEpoch, 0);
        }
    }

    private static final class SectionRef {
        private static final long EPOCH0_OFF = UnsafeHolder.fieldOffset(SectionRef.class, "neighborMarkEpoch0");

        final ChunkRef owner;
        final int sy;
        final byte pocketCount;
        final byte @Nullable [] pocketData;
        final @Nullable SectionBuffers buffers;
        final short @Nullable [] singleFaceCounts;

        long radBits0;
        double delta0;
        int neighborMarkEpoch0;
        int volume0;
        double invVolume0;

        private SectionRef(ChunkRef owner, int sy, byte pocketCount, byte @Nullable [] pocketData, @Nullable SectionBuffers buffers,
                           short @Nullable [] singleFaceCounts, long radBits0, int neighborMarkEpoch0, int volume0, double invVolume0) {
            this.owner = owner;
            this.sy = sy;
            this.pocketCount = pocketCount;
            this.pocketData = pocketData;
            this.buffers = buffers;
            this.singleFaceCounts = singleFaceCounts;
            this.radBits0 = radBits0;
            this.neighborMarkEpoch0 = neighborMarkEpoch0;
            this.volume0 = volume0;
            this.invVolume0 = invVolume0;
        }

        static SectionRef singleUniform(ChunkRef owner, int sy) {
            return new SectionRef(owner, sy, (byte) 1, null, null, null, 0L, 0, SECTION_BLOCK_COUNT, 1.0d / SECTION_BLOCK_COUNT);
        }

        static SectionRef singleMasked(ChunkRef owner, int sy, byte[] pocketData, int volume0, short[] faceCounts) {
            int v = Math.max(1, volume0);
            return new SectionRef(owner, sy, (byte) 1, pocketData, null, faceCounts, 0L, 0, v, 1.0d / v);
        }

        static SectionRef multi(ChunkRef owner, int sy, byte pocketCount, byte[] pocketData, SectionBuffers buffers) {
            return new SectionRef(owner, sy, pocketCount, pocketData, buffers, null, 0L, 0, 0, 0.0d);
        }

        boolean isSinglePocket() {
            return (pocketCount & 0xFF) == 1;
        }

        boolean isMultiPocket() {
            return (pocketCount & 0xFF) > 1;
        }

        boolean isOpenUniformSingle() {
            return isSinglePocket() && pocketData == null;
        }

        double getRad(int idx) {
            if (isSinglePocket()) return Double.longBitsToDouble(radBits0);
            return Double.longBitsToDouble(buffers.radBits[idx]);
        }

        void setRad(int idx, double val) {
            long bits = Double.doubleToRawLongBits(val);
            if (isSinglePocket()) radBits0 = bits;
            else buffers.radBits[idx] = bits;
        }

        double getDelta(int idx) {
            if (isSinglePocket()) return delta0;
            return buffers.deltas[idx];
        }

        void setDelta(int idx, double val) {
            if (isSinglePocket()) delta0 = val;
            else buffers.deltas[idx] = val;
        }

        boolean markEpochIfNot(int idx, int epoch) {
            if (isSinglePocket()) {
                if (neighborMarkEpoch0 == epoch) return false;
                neighborMarkEpoch0 = epoch;
                return true;
            }
            int[] a = buffers.neighborMarkEpoch;
            if (a[idx] == epoch) return false;
            a[idx] = epoch;
            return true;
        }

        boolean tryMarkEpochCAS(int idx, int epoch) {
            if (isSinglePocket()) {
                int cur = neighborMarkEpoch0;
                if (cur == epoch) return false;
                return U.compareAndSetInt(this, EPOCH0_OFF, cur, epoch);
            }
            int[] arr = buffers.neighborMarkEpoch;
            long offset = offInt(idx);
            int cur = U.getInt(arr, offset);
            if (cur == epoch) return false;
            return U.compareAndSetInt(arr, offset, cur, epoch);
        }

        int getPocketIndex(long pos) {
            if (pocketCount <= 0) return -1;
            if (pocketData == null) return 0;
            int blockIndex = Library.blockPosToLocal(pos);
            int nibble = readNibble(pocketData, blockIndex);
            if (nibble == NO_POCKET) return -1;
            int len = pocketCount & 0xFF;
            if (nibble >= len) return -1;
            return nibble;
        }

        int paletteIndexOrNeg(int blockIndex) {
            int len = pocketCount & 0xFF;
            if (len == 0) return -1;
            if (pocketData == null) return 0;
            int nibble = readNibble(pocketData, blockIndex);
            if (nibble == NO_POCKET) return -1;
            if (nibble >= len) return -2;
            return nibble;
        }

        void clearFaceAllPockets(int faceOrdinal) {
            if (!isMultiPocket()) return;
            int len = pocketCount & 0xFF;
            int stride = 6 * NEI_SLOTS;
            int faceBase = faceOrdinal * NEI_SLOTS;
            for (int p = 0; p < len; p++) {
                int off = p * stride + faceBase;
                Arrays.fill(buffers.connectionArea, off, off + NEI_SLOTS, (char) 0);
                buffers.faceActive[p * 6 + faceOrdinal] = 0;
            }
        }

        boolean markSentinelPlane16x16(int ordinal) {
            if (!isMultiPocket()) return false;

            boolean dirty = false;
            int slotBase = ordinal * NEI_SLOTS;
            int planeBase = ordinal << 8;

            for (int t = 0; t < 256; t++) {
                int idx = FACE_PLANE[planeBase + t];
                int pi = paletteIndexOrNeg(idx);
                if (pi == -2) {
                    dirty = true;
                    continue;
                }
                if (pi >= 0) {
                    buffers.connectionArea[pi * 6 * NEI_SLOTS + slotBase] = 1; // slot 0 sentinel
                    buffers.faceActive[pi * 6 + ordinal] = 1;
                }
            }
            return dirty;
        }

        void linkFaceTo(SectionRef b, int faceA) {
            int faceB = faceA ^ 1;

            clearFaceAllPockets(faceA);
            b.clearFaceAllPockets(faceB);

            char[] aConn = buffers.connectionArea;
            byte[] aFace = buffers.faceActive;
            char[] bConn = b.buffers.connectionArea;
            byte[] bFace = b.buffers.faceActive;

            int aFaceBase0 = faceA * NEI_SLOTS;
            int bFaceBase0 = faceB * NEI_SLOTS;

            int planeA = faceA << 8;
            int planeB = faceB << 8;

            for (int t = 0; t < 256; t++) {
                int aIdx = FACE_PLANE[planeA + t];
                int bIdx = FACE_PLANE[planeB + t];

                int pa = paletteIndexOrNeg(aIdx);
                if (pa < 0) continue;

                int pb = b.paletteIndexOrNeg(bIdx);
                if (pb < 0) continue;

                int aOff = pa * 6 * NEI_SLOTS + aFaceBase0;
                aConn[aOff] = 1; // sentinel
                aConn[aOff + NEI_SHIFT + pb]++;

                aFace[pa * 6 + faceA] = 1;

                int bOff = pb * 6 * NEI_SLOTS + bFaceBase0;
                bConn[bOff] = 1; // sentinel
                bConn[bOff + NEI_SHIFT + pa]++;

                bFace[pb * 6 + faceB] = 1;
            }
        }

        void linkFaceToSingle(SectionRef single, int faceA) {
            if (!isMultiPocket()) return;

            int faceB = faceA ^ 1;

            clearFaceAllPockets(faceA);

            char[] aConn = buffers.connectionArea;
            byte[] aFace = buffers.faceActive;
            int aFaceBase0 = faceA * NEI_SLOTS;

            int planeA = faceA << 8;
            int planeB = faceB << 8;

            // If the single has no pocketData, its boundary is always open.
            boolean singleAlwaysOpen = (single.pocketData == null) && single.isSinglePocket();

            for (int t = 0; t < 256; t++) {
                int aIdx = FACE_PLANE[planeA + t];
                int pa = paletteIndexOrNeg(aIdx);
                if (pa < 0) continue;

                if (!singleAlwaysOpen) {
                    int bIdx = FACE_PLANE[planeB + t];
                    int pb = single.paletteIndexOrNeg(bIdx);
                    if (pb < 0) continue;
                }

                int aOff = pa * 6 * NEI_SLOTS + aFaceBase0;
                aConn[aOff] = 1;
                aConn[aOff + NEI_SHIFT]++; // neighbor pocket index 0
                aFace[pa * 6 + faceA] = 1;
            }
        }

        double calculateFlux(WorldRadiationData data, long myKey, int pi) {
            return calculateFluxInternal(data, myKey, pi, 0, null);
        }

        double calculateFluxAndScan(WorldRadiationData data, long myKey, int pi, int epoch, LongBag wakeBag) {
            return calculateFluxInternal(data, myKey, pi, epoch, wakeBag);
        }

        private double calculateFluxInternal(WorldRadiationData data, long myKey, int pi, int epoch, @Nullable LongBag wakeBag) {
            if (pocketCount <= 0) return 0.0d;

            int len = pocketCount & 0xFF;
            if (pi < 0 || pi >= len) return 0.0d;

            final boolean doWake = (wakeBag != null);

            double myRad = getRad(pi);
            double flux = 0.0d;

            final ChunkRef myChunk = this.owner;
            final int mySy = this.sy;
            final long myCk = myChunk.ck;
            final int myCx = Library.getChunkPosX(myCk);
            final int myCz = Library.getChunkPosZ(myCk);

            boolean markMeDirty = false;

            final double invVol = isSinglePocket() ? invVolume0 : buffers.invVolume[pi];

            Long2ObjectMap<Chunk> loaded = data.world.getChunkProvider().loadedChunks;
            if (!isSinglePocket()) {
                final int pocketFaceBase = pi * 6 * NEI_SLOTS;

                for (int face = 0; face < 6; face++) {
                    if (buffers.faceActive[pi * 6 + face] == 0) continue;

                    int nSy = mySy + FACE_DY[face];
                    if (isInvalidSectionY(nSy)) continue;

                    int base = pocketFaceBase + face * NEI_SLOTS;
                    if (buffers.connectionArea[base] == 0) continue;

                    ChunkRef nChunk;
                    SectionRef ns;
                    long nKey;

                    if (face <= 1) {
                        nChunk = myChunk;
                        ns = myChunk.getSec(nSy);
                        nKey = Library.sectionToLong(myCk, nSy);
                    } else {
                        nChunk = myChunk.neighborByFace(face);
                        if (nChunk == null) {
                            long nck = ChunkPos.asLong(myCx + FACE_DX[face], myCz + FACE_DZ[face]);
                            nChunk = data.chunkRefs.get(nck);
                        }
                        if (nChunk == null) continue;
                        ns = nChunk.getSec(mySy);
                        nKey = Library.sectionToLong(nChunk.ck, mySy);
                    }

                    if (ns == null || ns.pocketCount <= 0) {
                        Chunk mc = nChunk.getChunk(loaded);
                        if (mc != null) {
                            data.markDirty(nKey);
                            markMeDirty = true;
                        }
                        continue;
                    }

                    int nCount = ns.pocketCount & 0xFF;
                    int lim = Math.min(NO_POCKET, nCount);

                    for (int npi = 0; npi < lim; npi++) {
                        int area = buffers.connectionArea[base + NEI_SHIFT + npi];
                        if (area <= 0) continue;

                        double nRad = ns.getRad(npi);
                        flux += (double) area * (nRad - myRad);

                        if (doWake) {
                            if (ns.tryMarkEpochCAS(npi, epoch)) {
                                long nk = pocketKey(nKey, npi);
                                if (wakeBag.tryAdd(nk)) {
                                    int nSyBit = (face <= 1) ? nSy : mySy;
                                    nChunk.setActiveBit(nSyBit, npi);

                                    double nd = ns.calculateFlux(data, nKey, npi);
                                    ns.setDelta(npi, nd);
                                } else {
                                    data.activatePocket(nk);
                                }
                            }
                        }
                    }
                }

                if (markMeDirty) data.markDirty(myKey);
                return flux * diffusionDt * invVol;
            }

            // Single-pocket
            final boolean myUniform = (this.pocketData == null);

            for (int face = 0; face < 6; face++) {
                int nSy = mySy + FACE_DY[face];
                if (isInvalidSectionY(nSy)) continue;

                ChunkRef nChunk;
                SectionRef ns;
                long nKey;

                if (face <= 1) {
                    nChunk = myChunk;
                    ns = myChunk.getSec(nSy);
                    nKey = Library.sectionToLong(myCx, nSy, myCz);
                } else {
                    nChunk = myChunk.neighborByFace(face);
                    if (nChunk == null) {
                        long nck = ChunkPos.asLong(myCx + FACE_DX[face], myCz + FACE_DZ[face]);
                        nChunk = data.chunkRefs.get(nck);
                    }
                    if (nChunk == null) continue;
                    ns = nChunk.getSec(mySy);
                    nKey = Library.sectionToLong(nChunk.ck, mySy);
                }

                if (ns == null || ns.pocketCount <= 0) {
                    Chunk mc = nChunk.getChunk(loaded);
                    if (mc != null) {
                        data.markDirty(nKey);
                        markMeDirty = true;
                    }
                    continue;
                }

                // open-single-uniform fast path via uniformMask bit
                if (myUniform) {
                    boolean neighborUniform;
                    if (face <= 1) neighborUniform = myChunk.isUniform(nSy);
                    else neighborUniform = nChunk.isUniform(mySy);

                    if (neighborUniform) {
                        double nRad = Double.longBitsToDouble(ns.radBits0);
                        flux += 256.0d * (nRad - myRad);

                        if (doWake) {
                            if (ns.tryMarkEpochCAS(0, epoch)) {
                                long nk = pocketKey(nKey, 0);
                                if (wakeBag.tryAdd(nk)) {
                                    int nSyBit = (face <= 1) ? nSy : mySy;
                                    nChunk.setActiveBit(nSyBit, 0);

                                    double nd = ns.calculateFlux(data, nKey, 0);
                                    ns.setDelta(0, nd);
                                } else {
                                    data.activatePocket(nk);
                                }
                            }
                        }
                        continue;
                    }
                }

                if (ns.isSinglePocket()) {
                    final int area;

                    boolean nsUniform = (ns.pocketData == null);
                    if (myUniform && nsUniform) {
                        area = 256;
                    } else if (nsUniform) {
                        // I am masked, neighbor is air -> overlap is my face count
                        area = this.singleFaceCounts[face] & 0xFFFF;
                    } else if (myUniform) {
                        // I am air, neighbor is masked -> overlap is neighbor's face count (opposite face)
                        area = ns.singleFaceCounts[face ^ 1] & 0xFFFF;
                    } else {
                        // Both masked: must scan intersection
                        area = scanBoundaryAreaSingleToSingle(this, ns, face);
                    }

                    if (area <= 0) continue;

                    double nRad = ns.getRad(0);
                    flux += (double) area * (nRad - myRad);

                    if (doWake) {
                        if (ns.tryMarkEpochCAS(0, epoch)) {
                            long nk = pocketKey(nKey, 0);
                            if (wakeBag.tryAdd(nk)) {
                                int nSyBit = (face <= 1) ? nSy : mySy;
                                nChunk.setActiveBit(nSyBit, 0);

                                double nd = ns.calculateFlux(data, nKey, 0);
                                ns.setDelta(0, nd);
                            } else {
                                data.activatePocket(nk);
                            }
                        }
                    }
                } else {
                    int faceB = face ^ 1;
                    int nCount = ns.pocketCount & 0xFF;

                    char[] nConn = ns.buffers.connectionArea;
                    byte[] nFace = ns.buffers.faceActive;
                    int stride = 6 * NEI_SLOTS;
                    int base = faceB * NEI_SLOTS + NEI_SHIFT;

                    int lim = Math.min(NO_POCKET, nCount);
                    for (int p = 0; p < lim; p++) {
                        if (nFace[p * 6 + faceB] == 0) continue;
                        int area = nConn[p * stride + base];
                        if (area <= 0) continue;

                        double nRad = ns.getRad(p);
                        flux += (double) area * (nRad - myRad);

                        if (doWake) {
                            if (ns.tryMarkEpochCAS(p, epoch)) {
                                long nk = pocketKey(nKey, p);
                                if (wakeBag.tryAdd(nk)) {
                                    int nSyBit = (face <= 1) ? nSy : mySy;
                                    nChunk.setActiveBit(nSyBit, p);
                                    double nd = ns.calculateFlux(data, nKey, p);
                                    ns.setDelta(p, nd);
                                } else {
                                    data.activatePocket(nk);
                                }
                            }
                        }
                    }
                }
            }

            if (markMeDirty) data.markDirty(myKey);
            return flux * diffusionDt * invVol;
        }
    }

    private static final class WorldRadiationData {
        final WorldServer world;
        final NonBlockingHashMapLong<ChunkRef> chunkRefs = new NonBlockingHashMapLong<>(4096);

        final NonBlockingHashSetLong dirtySections = new NonBlockingHashSetLong(16384);
        // reserved value: Long.MIN_VALUE
        final MpscUnboundedXaddArrayLongQueue dirtyQueue = new MpscUnboundedXaddArrayLongQueue(16384);
        // only used when tickrate != 1.
        final MpscUnboundedXaddArrayLongQueue destructionQueue = new MpscUnboundedXaddArrayLongQueue(64);
        final TLPool<SectionBuffers> sectionBuffersPool = new TLPool<>(SectionBuffers::new, SectionBuffers::reset, 256, 4096);
        final TLPool<byte[]> pocketDataPool = new TLPool<>(() -> new byte[2048], _ -> /*@formatter:off*/{}/*@formatter:on*/, 256, 4096);
        final SectionRetireBag retiredSections = new SectionRetireBag(16384);
        final NonBlockingLong2LongHashMap pendingPocketRadBits = new NonBlockingLong2LongHashMap(16384);
        final LongArrayList dirtyToRebuildScratch = new LongArrayList(16384);
        final Long2IntOpenHashMap dirtyChunkMasksScratch = new Long2IntOpenHashMap(16384);
        final double minBound;
        final long[][] activeStripeBufs = new long[ACTIVE_STRIPES][];
        final int[] activeStripeCounts = new int[ACTIVE_STRIPES];
        volatile MpscUnboundedXaddArrayLongQueue[] activeQueuesCurrent, activeQueuesNext;
        LongBag wokenBag = new LongBag(32768);
        long[] activeBuf = new long[32768];
        long[] linkScratch = new long[512];
        long[] dirtyChunkKeysScratch = new long[4096];
        int[] dirtyChunkMasksScratchArr = new int[4096];
        // only used when tickrate == 1. races are tolerable.
        long pocketToDestroy = Long.MIN_VALUE;
        int workEpoch = 0;

        WorldRadiationData(WorldServer world) {
            this.world = world;
            Object v = CompatibilityConfig.dimensionRad.get(world.provider.getDimension());
            minBound = -((v instanceof Number n) ? n.doubleValue() : 0D);
            dirtyChunkMasksScratch.defaultReturnValue(0);
            int p = Math.max(256, 65536 / ACTIVE_STRIPES);
            MpscUnboundedXaddArrayLongQueue[] cur = new MpscUnboundedXaddArrayLongQueue[ACTIVE_STRIPES];
            MpscUnboundedXaddArrayLongQueue[] nxt = new MpscUnboundedXaddArrayLongQueue[ACTIVE_STRIPES];
            for (int i = 0; i < ACTIVE_STRIPES; i++) {
                cur[i] = new MpscUnboundedXaddArrayLongQueue(p);
                nxt[i] = new MpscUnboundedXaddArrayLongQueue(p);
                activeStripeBufs[i] = new long[p];
            }
            activeQueuesCurrent = cur;
            activeQueuesNext = nxt;
        }

        private static int buildLinkKeys(long[] keys, int hi, int negFace, long[] out) {
            int n = 0;
            for (int i = 0; i < hi; i++) {
                long k = keys[i];
                out[n++] = k;
                out[n++] = offsetKey(k, negFace);
            }
            if (n >= 8192) Arrays.parallelSort(out, 0, n);
            else Arrays.sort(out, 0, n);

            int u = 0;
            long prev = Long.MIN_VALUE;
            for (int i = 0; i < n; i++) {
                long v = out[i];
                if (i == 0 || v != prev) out[u++] = v;
                prev = v;
            }
            return u;
        }

        // tested to be uniformly distributed
        private static int stripeIndex(long pocketKey) {
            long x = pocketKey ^ (pocketKey >>> 33);
            x ^= (x >>> 17);
            return ((int) x) & ACTIVE_STRIPE_MASK;
        }

        void clearAllSections() {
            chunkRefs.clear(true);
        }

        void clearQueues() {
            MpscUnboundedXaddArrayLongQueue[] cur = activeQueuesCurrent;
            MpscUnboundedXaddArrayLongQueue[] nxt = activeQueuesNext;
            for (int i = 0; i < ACTIVE_STRIPES; i++) {
                cur[i].clear(true);
                nxt[i].clear(true);
            }
        }

        void swapQueues() {
            MpscUnboundedXaddArrayLongQueue[] cur = activeQueuesCurrent;
            for (int i = 0; i < ACTIVE_STRIPES; i++) cur[i].clear(true);
            activeQueuesCurrent = activeQueuesNext;
            activeQueuesNext = cur;
        }

        void enqueueActiveNext(long pocketKey) {
            if (pocketKey == Long.MIN_VALUE) return;
            activeQueuesNext[stripeIndex(pocketKey)].offer(pocketKey);
        }

        boolean activatePocket(long pocketKey) {
            if (pocketKey == Long.MIN_VALUE) return false;
            long sck = sectionKeyFromPocketKey(pocketKey);
            long ck = Library.sectionToChunkLong(sck);
            ChunkRef cr = chunkRefs.get(ck);
            if (cr == null) return false;

            int yz = Library.getSectionY(pocketKey);
            int sy = (yz >>> 4) & 15;
            int pi = yz & 15;

            if (cr.setActiveBit(sy, pi)) {
                enqueueActiveNext(pocketKey);
                return true;
            }
            return false;
        }

        void deactivatePocket(long pocketKey) {
            long sck = sectionKeyFromPocketKey(pocketKey);
            long ck = Library.sectionToChunkLong(sck);
            ChunkRef cr = chunkRefs.get(ck);
            if (cr == null) return;

            int yz = Library.getSectionY(pocketKey);
            int sy = (yz >>> 4) & 15;
            int pi = yz & 15;

            cr.clearActiveBit(sy, pi);
        }

        void onChunkLoaded(Chunk chunk) {
            int x = chunk.x;
            int z = chunk.z;
            long ck = ChunkPos.asLong(x, z);

            ChunkRef cr = chunkRefs.get(ck);
            if (cr == null) {
                ChunkRef fresh = new ChunkRef(ck);
                ChunkRef prev = chunkRefs.put(ck, fresh);
                cr = (prev != null) ? prev : fresh;
            }

            cr.mcChunk = chunk;

            ChunkRef n = chunkRefs.get(ChunkPos.asLong(x, z - 1));
            if (n != null) {
                cr.north = n;
                n.south = cr;
            }
            ChunkRef s = chunkRefs.get(ChunkPos.asLong(x, z + 1));
            if (s != null) {
                cr.south = s;
                s.north = cr;
            }
            ChunkRef w = chunkRefs.get(ChunkPos.asLong(x - 1, z));
            if (w != null) {
                cr.west = w;
                w.east = cr;
            }
            ChunkRef e = chunkRefs.get(ChunkPos.asLong(x + 1, z));
            if (e != null) {
                cr.east = e;
                e.west = cr;
            }
        }

        @Nullable SectionRef getSection(long sck) {
            int sy = Library.getSectionY(sck);
            if (isInvalidSectionY(sy)) return null;
            long ck = Library.sectionToChunkLong(sck);
            ChunkRef cr = chunkRefs.get(ck);
            if (cr == null) return null;
            return cr.getSec(sy);
        }

        ChunkRef getOrCreateChunkRef(long ck) {
            ChunkRef cr = chunkRefs.get(ck);
            if (cr != null) return cr;

            int cx = Library.getChunkPosX(ck);
            int cz = Library.getChunkPosZ(ck);
            ChunkRef fresh = new ChunkRef(ck);
            ChunkRef prev = chunkRefs.put(ck, fresh);
            cr = (prev != null) ? prev : fresh;
            ChunkRef n = chunkRefs.get(ChunkPos.asLong(cx, cz - 1));
            if (n != null) {
                cr.north = n;
                n.south = cr;
            }
            ChunkRef s = chunkRefs.get(ChunkPos.asLong(cx, cz + 1));
            if (s != null) {
                cr.south = s;
                s.north = cr;
            }
            ChunkRef w = chunkRefs.get(ChunkPos.asLong(cx - 1, cz));
            if (w != null) {
                cr.west = w;
                w.east = cr;
            }
            ChunkRef e = chunkRefs.get(ChunkPos.asLong(cx + 1, cz));
            if (e != null) {
                cr.east = e;
                e.west = cr;
            }

            return cr;
        }

        LongBag getWokenBag(int cap) {
            if (wokenBag.capacity < cap) wokenBag = new LongBag(cap);
            return wokenBag;
        }

        void spawnFog(SectionRef sc, int pocketIndex, ThreadLocalRandom rnd) {
            Chunk chunk = sc.owner.getChunk(world.getChunkProvider().loadedChunks);
            if (chunk == null) return;
            int bx = chunk.x << 4;
            int by = sc.sy << 4;
            int bz = chunk.z << 4;
            BlockPos.MutableBlockPos mp = new BlockPos.MutableBlockPos();
            ExtendedBlockStorage[] stor = chunk.getBlockStorageArray();
            ExtendedBlockStorage storage = stor[sc.sy];

            for (int k = 0; k < 10; k++) {
                int i = rnd.nextInt(4096);
                int lx = Library.getLocalX(i);
                int lz = Library.getLocalZ(i);
                int ly = Library.getLocalY(i);
                int x = bx + lx;
                int y = by + ly;
                int z = bz + lz;
                long posLong = Library.blockPosToLong(x, y, z);
                if (sc.getPocketIndex(posLong) != pocketIndex) continue;
                IBlockState state = (storage == null || storage.isEmpty()) ? Blocks.AIR.getDefaultState() : storage.data.get(i);

                mp.setPos(x, y, z);
                if (state.getMaterial() != Material.AIR) continue;

                boolean nearGround = false;
                for (int d = 1; d <= 6; d++) {
                    int yy = y - d;
                    if (yy < 0) break;
                    int sy = yy >>> 4;
                    ExtendedBlockStorage e = stor[sy];
                    IBlockState below = (e == null || e.isEmpty()) ? Blocks.AIR.getDefaultState() : e.get(lx, yy & 15, lz);
                    mp.setPos(x, yy, z);
                    if (below.getMaterial() != Material.AIR) {
                        nearGround = true;
                        break;
                    }
                }
                if (!nearGround) continue;

                float fx = x + 0.5F, fy = y + 0.5F, fz = z + 0.5F;
                NBTTagCompound tag = new NBTTagCompound();
                tag.setString("type", "radiationfog");
                PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(tag, fx, fy, fz), new TargetPoint(world.provider.getDimension(), fx, fy, fz, 100));
                break;
            }
        }

        void markDirty(long sck) {
            if (sck == Long.MIN_VALUE || !dirtySections.add(sck)) return;
            dirtyQueue.offer(sck);
        }

        void clearDirtyAll() {
            dirtySections.clear(true);
            dirtyQueue.clear(true);
        }

        void ensureDirtyBatchCapacity(int need) {
            if (dirtyChunkKeysScratch.length >= need && dirtyChunkMasksScratchArr.length >= need) return;
            int n = Math.max(dirtyChunkKeysScratch.length, 16);
            while (n < need) n = n + (n >>> 1) + 16;
            dirtyChunkKeysScratch = Arrays.copyOf(dirtyChunkKeysScratch, n);
            dirtyChunkMasksScratchArr = Arrays.copyOf(dirtyChunkMasksScratchArr, n);
        }

        void retireIfNeeded(SectionRef sc) {
            if (sc == null) return;
            if (sc.buffers != null || sc.pocketData != null) {
                retiredSections.add(sc);
            }
        }

        void clearPendingAll() {
            pendingPocketRadBits.clear(true);
        }

        int nextWorkEpoch() {
            return ++workEpoch == 0 ? ++workEpoch : workEpoch;
        }

        void unloadChunk(int cx, int cz) {
            final long ck = ChunkPos.asLong(cx, cz);
            final ChunkRef cr = chunkRefs.remove(ck);

            if (cr == null) {
                for (int sy = 0; sy < 16; sy++) {
                    final long sck = Library.sectionToLong(cx, sy, cz);
                    dirtySections.remove(sck);
                    for (int p = 0; p <= NO_POCKET; p++) pendingPocketRadBits.remove(pocketKey(sck, p));
                }
                return;
            }

            ChunkRef n = cr.north;
            if (n == null) n = chunkRefs.get(ChunkPos.asLong(cx, cz - 1));
            if (n != null && n.south == cr) n.south = null;

            ChunkRef s = cr.south;
            if (s == null) s = chunkRefs.get(ChunkPos.asLong(cx, cz + 1));
            if (s != null && s.north == cr) s.north = null;

            ChunkRef w = cr.west;
            if (w == null) w = chunkRefs.get(ChunkPos.asLong(cx - 1, cz));
            if (w != null && w.east == cr) w.east = null;

            ChunkRef e = cr.east;
            if (e == null) e = chunkRefs.get(ChunkPos.asLong(cx + 1, cz));
            if (e != null && e.west == cr) e.west = null;

            cr.north = cr.south = cr.west = cr.east = null;
            cr.mcChunk = null;

            for (int sy = 0; sy < 16; sy++) {
                final long sck = Library.sectionToLong(cx, sy, cz);

                final SectionRef old = cr.getSec(sy);
                if (old != null) {
                    retireIfNeeded(old);
                    markNeighborsMissing(sck);
                }

                cr.setSec(sy, null);
                U.putIntRelease(cr.activePocketMasks, offInt(sy), 0);

                dirtySections.remove(sck);
                for (int p = 0; p <= NO_POCKET; p++) pendingPocketRadBits.remove(pocketKey(sck, p));
            }
            cr.uniformMask = 0;
        }

        void cleanupPools() {
            destructionQueue.clear(true);
            retiredSections.drainAndRecycle(sectionBuffersPool, pocketDataPool);
        }

        void readPayload(int cx, int cz, byte[] raw) throws DecodeException {
            long ck = ChunkPos.asLong(cx, cz);
            ChunkRef owner = getOrCreateChunkRef(ck);

            for (int sy = 0; sy < 16; sy++) {
                final long sck = Library.sectionToLong(cx, sy, cz);
                final SectionRef prev = owner.getSec(sy);
                if (prev != null) {
                    final int prevLen = prev.pocketCount & 0xFF;
                    for (int p = 0; p < prevLen; p++) deactivatePocket(pocketKey(sck, p));
                    retireIfNeeded(prev);
                }

                U.putIntRelease(owner.activePocketMasks, offInt(sy), 0);
                owner.setSec(sy, null);
                owner.setUniformBit(sy, false);

                dirtySections.remove(sck);
                markDirty(sck);
            }

            // Clear stale pending entries for this chunk.
            for (int sy = 0; sy < 16; sy++) {
                long sck = Library.sectionToLong(cx, sy, cz);
                for (int p = 0; p <= NO_POCKET; p++) pendingPocketRadBits.remove(pocketKey(sck, p));
            }

            if (raw == null || raw.length == 0) return;

            final ByteBuffer b = ByteBuffer.wrap(raw, 4, raw.length - 4);
            if (b.remaining() < 2) throw new DecodeException("truncated v6 header");
            final int entryCount = b.getShort() & 0xFFFF;

            final int need = entryCount * (1 + 8);
            if (b.remaining() < need) throw new DecodeException("truncated v6 payload: need=" + need + " rem=" + b.remaining());

            for (int i = 0; i < entryCount; i++) {
                int yz = b.get() & 0xFF;
                int sy = (yz >>> 4) & 15;
                int pi = yz & 15;

                double rad = b.getDouble();
                if (!Double.isFinite(rad) || nearZero(rad)) continue;

                long sck = Library.sectionToLong(cx, sy, cz);
                long pk = pocketKey(sck, pi);
                pendingPocketRadBits.put(pk, Double.doubleToRawLongBits(rad));
            }
        }

        void rebuildChunkPocketsLoaded(long sectionKey,  @Nullable ExtendedBlockStorage ebs) {
            int sy = Library.getSectionY(sectionKey);
            long ck = Library.sectionToChunkLong(sectionKey);
            ChunkRef owner = getOrCreateChunkRef(ck);

            byte[] pocketData;
            int pocketCount;
            SectionBuffers buffers = null;
            int singleVolume0 = SECTION_BLOCK_COUNT;
            short[] singleFaceCounts = null;

            final SectionMask resistant = scanResistantMask(world, sectionKey, ebs);

            if (resistant == null || resistant.isEmpty()) {
                pocketCount = 1;
                pocketData = null;
            } else {
                byte[] scratch = pocketDataPool.borrow();
                Arrays.fill(scratch, (byte) 0xFF);
                int[] queue = TL_FF_QUEUE.get();
                int[] vols = TL_VOL_COUNTS.get();
                Arrays.fill(vols, 0);

                int pc = 0;

                for (int blockIndex = 0; blockIndex < SECTION_BLOCK_COUNT; blockIndex++) {
                    if (readNibble(scratch, blockIndex) != NO_POCKET) continue;
                    if (resistant.get(blockIndex)) continue;

                    final int currentPaletteIndex = (pc >= NO_POCKET) ? 0 : pc++;
                    int head = 0, tail = 0;
                    queue[tail++] = blockIndex;
                    writeNibble(scratch, blockIndex, currentPaletteIndex);
                    vols[currentPaletteIndex]++;
                    while (head != tail) {
                        int currentIndex = queue[head++];
                        for (int i = 0; i < 6; i++) {
                            int neighborIndex = currentIndex + LINEAR_OFFSETS[i];
                            if (((neighborIndex & 0xF000) | ((currentIndex ^ neighborIndex) & BOUNDARY_MASKS[i])) != 0) continue;
                            if (readNibble(scratch, neighborIndex) != NO_POCKET) continue;
                            if (resistant.get(neighborIndex)) continue;
                            writeNibble(scratch, neighborIndex, currentPaletteIndex);
                            queue[tail++] = neighborIndex;
                            vols[currentPaletteIndex]++;
                        }
                    }
                }

                pocketCount = pc;
                if (pocketCount > 0) {
                    pocketData = scratch;

                    if (pocketCount > 1) {
                        buffers = sectionBuffersPool.borrow();
                        buffers.reset();

                        for (int i = 0; i < pocketCount; i++) {
                            int v = Math.max(1, vols[i]);
                            buffers.volume[i] = v;
                            buffers.invVolume[i] = 1.0d / v;
                        }
                    } else {
                        singleVolume0 = Math.max(1, vols[0]);
                        singleFaceCounts = new short[6];
                        for (int face = 0; face < 6; face++) {
                            int base = face << 8;
                            int count = 0;
                            for (int t = 0; t < 256; t++) {
                                int idx = FACE_PLANE[base + t];
                                if (readNibble(pocketData, idx) == 0) count++;
                            }
                            singleFaceCounts[face] = (short) count;
                        }
                    }
                } else {
                    pocketDataPool.recycle(scratch);
                    pocketData = null;
                }
            }

            SectionRef old = owner.getSec(sy);
            double oldTotal = 0.0d;
            if (old != null) {
                int oldLen = old.pocketCount & 0xFF;
                for (int p = 0; p < oldLen; p++) deactivatePocket(pocketKey(sectionKey, p));
                for (int i = 0; i < oldLen; i++) oldTotal += old.getRad(i);
                retireIfNeeded(old);
            }

            if (pocketCount <= 0) {
                U.putIntRelease(owner.activePocketMasks, offInt(sy), 0);
                owner.setSec(sy, null);
                owner.setUniformBit(sy, false);
                return;
            }

            boolean anyPending = false;

            if (pocketCount == 1) {
                long pk = pocketKey(sectionKey, 0);
                long bits = pendingPocketRadBits.remove(pk);
                if (bits != 0L) {
                    double v = Double.longBitsToDouble(bits);
                    if (!Double.isFinite(v) || nearZero(v)) {
                        anyPending = true;
                        bits = 0L;
                    } else {
                        anyPending = true;
                    }
                }

                for (int i = 1; i <= NO_POCKET; i++) pendingPocketRadBits.remove(pocketKey(sectionKey, i));

                SectionRef sc = (pocketData == null) ? SectionRef.singleUniform(owner, sy ) : SectionRef.singleMasked(owner, sy,  pocketData, singleVolume0, singleFaceCounts);
                sc.radBits0 = bits;

                if (!anyPending && oldTotal != 0.0d) {
                    sc.radBits0 = Double.doubleToRawLongBits(oldTotal);
                } else if (anyPending && oldTotal != 0.0d) {
                    double base = Double.longBitsToDouble(sc.radBits0);
                    double v = base + oldTotal;
                    sc.radBits0 = Double.doubleToRawLongBits(Double.isFinite(v) ? v : 0.0d);
                }

                U.putIntRelease(owner.activePocketMasks, offInt(sy), 0);
                owner.setSec(sy, sc);
                owner.setUniformBit(sy, sc.isOpenUniformSingle());

                double rad = sc.getRad(0);
                if (!nearZero(rad)) activatePocket(pk);
                return;
            }

            for (int i = 0; i < pocketCount; i++) {
                long pk = pocketKey(sectionKey, i);
                long bits = pendingPocketRadBits.remove(pk);
                if (bits != 0L) {
                    double v = Double.longBitsToDouble(bits);
                    if (!Double.isFinite(v) || nearZero(v)) {
                        buffers.radBits[i] = 0L;
                    } else {
                        buffers.radBits[i] = Double.doubleToRawLongBits(v);
                        anyPending = true;
                    }
                } else {
                    buffers.radBits[i] = 0L;
                }
                buffers.deltas[i] = 0.0d;
            }
            for (int i = pocketCount; i <= NO_POCKET; i++) pendingPocketRadBits.remove(pocketKey(sectionKey, i));

            if (!anyPending && oldTotal != 0.0d) {
                long totalVol = 0;
                for (int i = 0; i < pocketCount; i++) totalVol += Math.max(1, buffers.volume[i]);
                if (totalVol <= 0) totalVol = 1;
                for (int i = 0; i < pocketCount; i++) {
                    double share = (double) Math.max(1, buffers.volume[i]) / (double) totalVol;
                    buffers.radBits[i] = Double.doubleToRawLongBits(oldTotal * share);
                }
            } else if (anyPending && oldTotal != 0.0d) {
                long totalVol = 0;
                for (int i = 0; i < pocketCount; i++) totalVol += Math.max(1, buffers.volume[i]);
                if (totalVol <= 0) totalVol = 1;
                for (int i = 0; i < pocketCount; i++) {
                    double base = Double.longBitsToDouble(buffers.radBits[i]);
                    double share = (double) Math.max(1, buffers.volume[i]) / (double) totalVol;
                    double v = base + oldTotal * share;
                    buffers.radBits[i] = Double.doubleToRawLongBits(Double.isFinite(v) ? v : 0.0d);
                }
            }

            SectionRef sc = SectionRef.multi(owner, sy,  (byte) pocketCount, pocketData, buffers);
            U.putIntRelease(owner.activePocketMasks, offInt(sy), 0);
            owner.setSec(sy, sc);
            owner.setUniformBit(sy, false);

            for (int i = 0; i < pocketCount; i++) {
                double rad = sc.getRad(i);
                if (!nearZero(rad)) activatePocket(pocketKey(sectionKey, i));
            }
        }

        void relinkKeys(long[] keys, int hi) {
            if (hi <= 0) return;
            ensureLinkScratch(hi << 1);
            int eN = buildLinkKeys(keys, hi, EnumFacing.WEST.ordinal(), linkScratch);
            if (eN > 0) new LinkDirTask(this, linkScratch, 0, eN, EnumFacing.EAST.ordinal()).invoke();
            int uN = buildLinkKeys(keys, hi, EnumFacing.DOWN.ordinal(), linkScratch);
            if (uN > 0) new LinkDirTask(this, linkScratch, 0, uN, EnumFacing.UP.ordinal()).invoke();
            int sN = buildLinkKeys(keys, hi, EnumFacing.NORTH.ordinal(), linkScratch);
            if (sN > 0) new LinkDirTask(this, linkScratch, 0, sN, EnumFacing.SOUTH.ordinal()).invoke();
        }

        private void ensureLinkScratch(int need) {
            long[] a = linkScratch;
            if (a.length >= need) return;
            int n = a.length;
            while (n < need) n = n + (n >>> 1) + 16;
            linkScratch = Arrays.copyOf(a, n);
        }

        void linkCanonical(long aKey, int faceA) {
            int ay = Library.getSectionY(aKey);
            if (isInvalidSectionY(ay)) return;

            SectionRef a = getSection(aKey);
            int by = ay + FACE_DY[faceA];

            if (isInvalidSectionY(by)) {
                if (a != null && a.isMultiPocket()) a.clearFaceAllPockets(faceA);
                return;
            }

            long bKey = Library.sectionToLong(Library.getSectionX(aKey) + FACE_DX[faceA], by, Library.getSectionZ(aKey) + FACE_DZ[faceA]);
            SectionRef b = getSection(bKey);

            if (a == null || a.pocketCount <= 0) {
                if (b != null && b.isMultiPocket()) {
                    int faceB = faceA ^ 1;
                    b.clearFaceAllPockets(faceB);
                    markSentinelOnBoundary(bKey, b, faceB);
                }
                return;
            }

            if (b == null || b.pocketCount <= 0) {
                if (a.isMultiPocket()) {
                    a.clearFaceAllPockets(faceA);
                    markSentinelOnBoundary(aKey, a, faceA);
                }
                return;
            }

            if (a.isMultiPocket() && b.isMultiPocket()) {
                a.linkFaceTo(b, faceA);
                return;
            }

            if (a.isMultiPocket() && b.isSinglePocket()) {
                a.linkFaceToSingle(b, faceA);
                return;
            }

            if (a.isSinglePocket() && b.isMultiPocket()) {
                b.linkFaceToSingle(a, faceA ^ 1);
            }
        }

        void markSentinelOnBoundary(long sck, SectionRef sc, int faceOrdinal) {
            if (!sc.isMultiPocket()) return;
            if (sc.markSentinelPlane16x16(faceOrdinal)) markDirty(sck);
        }

        void markNeighborsMissing(long removedKey) {
            int rx = Library.getSectionX(removedKey);
            int ry = Library.getSectionY(removedKey);
            int rz = Library.getSectionZ(removedKey);
            for (int i = 0; i < 6; i++) {
                int ny = ry + FACE_DY[i];
                if (isInvalidSectionY(ny)) continue;
                long nKey = Library.sectionToLong(rx + FACE_DX[i], ny, rz + FACE_DZ[i]);
                SectionRef n = getSection(nKey);
                if (n != null && n.isMultiPocket()) {
                    int nFace = i ^ 1;
                    n.clearFaceAllPockets(nFace);
                    markSentinelOnBoundary(nKey, n, nFace);
                }
            }
        }
    }

    private static final class LongBag {
        static final int CHUNK_SHIFT = 10;
        static final int CHUNK_SIZE = 1 << CHUNK_SHIFT;
        static final int CHUNK_MASK = CHUNK_SIZE - 1;
        static final long JAA_BASE = U.arrayBaseOffset(long[][].class);
        static final int JAA_SHIFT = Integer.numberOfTrailingZeros(U.arrayIndexScale(long[][].class));
        static final long SIZE_OFF = UnsafeHolder.fieldOffset(LongBag.class, "size");

        final long[][] chunks;
        final int capacity;
        volatile int size;

        LongBag(int cap) {
            int chunkCount = (cap + CHUNK_SIZE - 1) >>> CHUNK_SHIFT;
            chunks = new long[chunkCount][];
            capacity = chunkCount * CHUNK_SIZE;
        }

        void clear() {
            size = 0;
        }

        boolean tryAdd(long v) {
            int i = U.getAndAddInt(this, SIZE_OFF, 1);
            if (i >= capacity) {
                while (true) {
                    int s = size;
                    if (s <= capacity) break;
                    if (U.compareAndSetInt(this, SIZE_OFF, s, capacity)) break;
                }
                return false;
            }
            int c = i >>> CHUNK_SHIFT;
            int o = i & CHUNK_MASK;
            long[] chunk = chunks[c];
            if (chunk == null) {
                long chunkAddr = JAA_BASE + ((long) c << JAA_SHIFT);
                chunk = (long[]) U.getReferenceVolatile(chunks, chunkAddr);
                if (chunk == null) {
                    long[] newChunk = new long[CHUNK_SIZE];
                    if (U.compareAndSetReference(chunks, chunkAddr, null, newChunk)) chunk = newChunk;
                    else chunk = (long[]) U.getReferenceVolatile(chunks, chunkAddr);
                }
            }
            chunk[o] = v;
            return true;
        }

        int size() {
            return Math.min(size, capacity);
        }
    }

    private static final class SectionRetireBag {
        static final int CHUNK_SHIFT = 10;
        static final int CHUNK_SIZE = 1 << CHUNK_SHIFT;
        static final int CHUNK_MASK = CHUNK_SIZE - 1;
        static final long SRAA_BASE = U.arrayBaseOffset(SectionRef[][].class);
        static final int SRAA_SHIFT = Integer.numberOfTrailingZeros(U.arrayIndexScale(SectionRef[][].class));
        static final long SIZE_OFF = UnsafeHolder.fieldOffset(SectionRetireBag.class, "size");

        final SectionRef[][] chunks;
        final int capacity;
        volatile int size;

        SectionRetireBag(int cap) {
            int chunkCount = (cap + CHUNK_SIZE - 1) >>> CHUNK_SHIFT;
            this.chunks = new SectionRef[chunkCount][];
            this.capacity = chunkCount * CHUNK_SIZE;
        }

        void add(SectionRef v) {
            int i = U.getAndAddInt(this, SIZE_OFF, 1);
            if (i >= capacity) {
                while (true) {
                    int s = size;
                    if (s <= capacity) break;
                    if (U.compareAndSetInt(this, SIZE_OFF, s, capacity)) break;
                }
                return;
            }

            int c = i >>> CHUNK_SHIFT;
            int o = i & CHUNK_MASK;

            SectionRef[] chunk = chunks[c];
            if (chunk == null) {
                long addr = SRAA_BASE + ((long) c << SRAA_SHIFT);
                chunk = (SectionRef[]) U.getReferenceVolatile(chunks, addr);
                if (chunk == null) {
                    SectionRef[] newChunk = new SectionRef[CHUNK_SIZE];
                    if (U.compareAndSetReference(chunks, addr, null, newChunk)) chunk = newChunk;
                    else chunk = (SectionRef[]) U.getReferenceVolatile(chunks, addr);
                }
            }
            chunk[o] = v;
        }

        void drainAndRecycle(TLPool<SectionBuffers> bp, TLPool<byte[]> pp) {
            int sz = size;
            if (sz > capacity) sz = capacity;
            for (int i = 0; i < sz; i++) {
                int c = i >>> CHUNK_SHIFT;
                int o = i & CHUNK_MASK;

                SectionRef[] chunk = chunks[c];
                if (chunk == null) continue;

                SectionRef sc = chunk[o];
                if (sc != null) {
                    if (sc.buffers != null) bp.recycle(sc.buffers);
                    if (sc.pocketData != null) pp.recycle(sc.pocketData);
                    chunk[o] = null;
                }
            }
            size = 0;
        }
    }
}
