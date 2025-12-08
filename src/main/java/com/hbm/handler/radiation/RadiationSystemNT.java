package com.hbm.handler.radiation;

import com.hbm.Tags;
import com.hbm.capability.HbmLivingProps;
import com.hbm.config.CompatibilityConfig;
import com.hbm.config.GeneralConfig;
import com.hbm.config.RadiationConfig;
import com.hbm.entity.mob.EntityCreeperNuclear;
import com.hbm.entity.mob.EntityDuck;
import com.hbm.entity.mob.EntityQuackos;
import com.hbm.entity.mob.EntityRADBeast;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.IRadResistantBlock;
import com.hbm.lib.ModDamageSource;
import com.hbm.lib.maps.NonBlockingHashMapLong;
import com.hbm.main.AdvancementManager;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.saveddata.AuxSavedData;
import com.hbm.util.AtomicDouble;
import com.hbm.util.ChunkUtil;
import com.hbm.util.SubChunkKey;
import com.hbm.world.WorldUtil;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.Supplier;

/**
 * Refactored to be fully threaded. Do not aim for upstream parity.
 * This is actually way better than upstream.
 *
 * @author Drillgon, Zach2039, mlbv
 */
@Mod.EventBusSubscriber(modid = Tags.MODID)
public final class RadiationSystemNT {

    /**
     * Per world radiation storage data
     */
    private static final Map<WorldServer, WorldRadiationData> worldMap = new ConcurrentHashMap<>();
    private static final ThreadLocal<ByteBuffer> BUF = ThreadLocal.withInitial(() -> ByteBuffer.allocate(524_288)); // in case some mod threads it
    private static final double PRUNE_THRESHOLD = 0.1D;
    private static final double EXTRA_DECAY = 0.05D;
    private static final Object futureLock = new Object();
    /**
     * A tick counter so radiation only updates once every second.
     */
    private static int ticks;
    /**
     * A future that represents the completion of the entire current radiation update cycle (computation and application).
     * This is used to chain updates and prevent race conditions. It should only be modified via the synchronized
     * scheduleUpdate method.
     */
    private static volatile CompletableFuture<Void> radiationFuture = CompletableFuture.completedFuture(null);
    private static MinecraftServer serverInstance;

    public static void onServerStarting(FMLServerStartingEvent event) {
        serverInstance = event.getServer();
    }

    /**
     * Clear the server instance when it stops to prevent memory leaks.
     */
    public static void onServerStopping(FMLServerStoppingEvent event) {
        try {
            radiationFuture.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            MainRegistry.logger.error("Radiation system timed out or failed to complete tasks during server shutdown.", e);
        }
    }

    /**
     * Forces an asynchronous, non-blocking radiation update for a single world.
     * If an update is already in progress, this new update will be scheduled to run immediately after the current one completes.
     *
     * @param world The server world to force an update on.
     */
    public static void forceUpdate(WorldServer world) {
        if (world == null) return;
        scheduleUpdate(() -> scheduleWorldUpdate(world));
    }

    /**
     * Forces an asynchronous, non-blocking radiation update for all loaded worlds.
     * If an update is already in progress, this new update will be scheduled to run immediately after the current one completes.
     */
    public static void forceUpdateAll() {
        // Schedule a new update that will be chained to the end of the current update queue.
        scheduleUpdate(RadiationSystemNT::scheduleFullUpdate);
    }

    /**
     * Increments the radiation at the specified block position. Only increments if the current radiation stored is less than max
     *
     * @param world - the world to increment radiation in
     * @param pos    - the block position to increment radiation at
     * @param amount - the amount to increment by
     * @param max    - the maximum amount of radiation allowed before it doesn't increment
     */
    public static void incrementRad(WorldServer world, BlockPos pos, double amount, double max) {
        if (pos.getY() < WorldUtil.getMinWorldHeight(world) || pos.getY() >= WorldUtil.getMaxWorldHeight(world) || !world.isBlockLoaded(pos)) return;
        if (amount < 0 || max < 0) throw new IllegalArgumentException("Radiation amount and max must be positive.");
        if (!isSubChunkLoaded(world, pos)) rebuildChunkPockets(world, world.getChunk(pos), pos.getY() >> 4);
        RadPocket p = getPocket(world, pos);
        if (p == null) return;
        final double prev = p.radiation.getAndUpdate(cur -> (cur < max) ? cur + amount : cur);
        if (!nearZero(amount) && p.radiation.get() != prev) {
            getWorldRadData(world).addActivePocket(p);
        }
    }

    /**
     * Subtracts amount from the current radiation at pos.
     *
     * @param world  - the world to edit radiation in
     * @param pos    - the position to edit radiation at
     * @param amount - the amount to subtract from current rads
     */
    public static void decrementRad(WorldServer world, BlockPos pos, double amount) {
        if (pos.getY() < WorldUtil.getMinWorldHeight(world) || pos.getY() >= WorldUtil.getMaxWorldHeight(world) || !world.isBlockLoaded(pos)) return;
        if (amount < 0) throw new IllegalArgumentException("Radiation amount to decrement must be positive.");
        if (!isSubChunkLoaded(world, pos)) rebuildChunkPockets(world, world.getChunk(pos), pos.getY() >> 4);
        RadPocket p = getPocket(world, pos);
        if (p == null) return;
        final double minB = minBoundFor(world);
        p.radiation.updateAndGet(cur -> Math.max(minB, cur - amount));
        getWorldRadData(world).addActivePocket(p);
    }

    /**
     * Sets the radiation at pos to the specified amount
     *
     * @param world  - the world to set radiation in
     * @param pos    - the position to set radiation at
     * @param amount - the amount to set the radiation to
     */
    public static void setRadForCoord(WorldServer world, BlockPos pos, double amount) {
        if (pos.getY() < WorldUtil.getMinWorldHeight(world) || pos.getY() >= WorldUtil.getMaxWorldHeight(world) || !world.isBlockLoaded(pos)) return;
        if (!isSubChunkLoaded(world, pos)) rebuildChunkPockets(world, world.getChunk(pos), pos.getY() >> 4);
        RadPocket p = getPocket(world, pos);
        if (p == null) return;
        final double minB = minBoundFor(world);
        final double clamped = Math.max(minB, amount);
        final double prev = p.radiation.getAndSet(clamped);
        if (!nearZero(clamped - prev)) getWorldRadData(world).addActivePocket(p);
    }

    /**
     * Gets the radiation at the pos
     *
     * @param world - the world to get raadiation in
     * @param pos   - the position to get radiation at
     * @return - the radiation value at the specified position
     */
    public static double getRadForCoord(WorldServer world, BlockPos pos) {
        //If it's not loaded, assume there's no radiation. Makes sure to not keep a lot of chunks loaded
        if (!isSubChunkLoaded(world, pos)) return 0D;

        // If no pockets, assume no radiation
        RadPocket pocket = getPocket(world, pos);
        return pocket == null ? 0D : pocket.radiation.get();
    }

    /**
     * Removes all loaded radiation from a world
     *
     * @param world - the world from which to remove radiation
     */
    public static void jettisonData(WorldServer world) {
        WorldRadiationData data = getWorldRadData(world);
        data.sectionsStorage.clear();
        data.clearActivePockets();
    }

    /**
     * Gets the pocket at the position (pockets explained below)
     *
     * @param world - the world to get the pocket from
     * @param pos   - the position the pocket should contain
     * @return - the RadPocket at the specified position, or null if it's a radiation-resistant block
     */
    @Nullable
    public static RadPocket getPocket(WorldServer world, BlockPos pos) {
        SubChunkRadiationStorage storage = getSubChunkStorage(world, pos);
        return storage != null ? storage.getPocket(pos) : null;
    }

    /**
     * Gets the set view of RadiationPockets that have active radiation data
     *
     * @param world - the world to get radiation pockets from
     * @return - weakly-consistent set view of active rad pockets
     */
    public static Set<RadPocket> getActiveSetView(WorldServer world) {
        return getWorldRadData(world).getActivePocketsView();
    }

    /**
     * Gets whether the rad data of the section at the specified position exists
     *
     * @param world - the world to check in
     * @param pos   - ths position to check at
     * @return whether the specified position currently has an active sub chunk
     */
    public static boolean isSubChunkLoaded(WorldServer world, BlockPos pos) {
        //If the position is out of bounds, it isn't loaded
        if (pos.getY() < WorldUtil.getMinWorldHeight(world) || pos.getY() >= WorldUtil.getMaxWorldHeight(world)) return false;
        //If the world radiation data doesn't exist, nothing is loaded
        WorldRadiationData worldRadData = worldMap.get(world);
        if (worldRadData == null) return false;
        long key = SubChunkKey.asLong(pos.getX() >> 4, pos.getZ() >> 4, pos.getY() >> 4);
        return worldRadData.sectionsStorage.containsKey(key);
    }

    /**
     * Gets the sub chunk from the specified pos. Does not load it if it doesn't exist
     *
     * @param world - the world to get from
     * @param pos   - the position to get the sub chunk at
     * @return the sub chunk at the specified position or null if not loaded
     */
    @Nullable
    public static SubChunkRadiationStorage getSubChunkStorage(WorldServer world, BlockPos pos) {
        WorldRadiationData worldRadData = getWorldRadData(world);
        return worldRadData.sectionsStorage.get(SubChunkKey.asLong(pos.getX() >> 4, pos.getZ() >> 4, pos.getY() >> 4));
    }

    /**
     * Gets the world radiation data for the world
     *
     * @param world - the world to get the radiation data from
     * @return the radiation data for the world
     */
    @NotNull
    private static WorldRadiationData getWorldRadData(WorldServer world) {
        return worldMap.computeIfAbsent(world, WorldRadiationData::new);
    }

    /**
     * Atomically schedules a new update to run after the current one completes.
     *
     * @param updateSupplier A supplier that returns a CompletableFuture for the new update.
     */
    private static void scheduleUpdate(Supplier<CompletableFuture<Void>> updateSupplier) {
        synchronized (futureLock) {
            radiationFuture = radiationFuture.thenComposeAsync(v -> updateSupplier.get());
        }
    }

    /**
     * Creates a CompletableFuture that computes and applies radiation updates for all worlds.
     *
     * @return A future that completes when the update is fully applied.
     */
    private static CompletableFuture<Void> scheduleFullUpdate() {
        return CompletableFuture.supplyAsync(RadiationSystemNT::computeRadiationUpdates).thenAcceptAsync(updates -> {
            if (serverInstance != null) {
                serverInstance.addScheduledTask(() -> applyRadiationUpdates(updates));
            } else {
                MainRegistry.logger.error("Cannot apply radiation updates: MinecraftServer instance is null!");
            }
        });
    }

    /**
     * Creates a CompletableFuture that computes and applies radiation updates for a single world.
     * This is safe because the world object is guaranteed to exist when forceUpdate(world) is called.
     *
     * @param world The world to update.
     * @return A future that completes when the update is fully applied.
     */
    private static CompletableFuture<Void> scheduleWorldUpdate(WorldServer world) {
        WorldRadiationData worldData = getWorldRadData(world);
        return CompletableFuture.supplyAsync(() -> computeWorldRadiationUpdate(worldData)).thenAcceptAsync(update -> applyWorldRadiationUpdate(worldData, update), ((WorldServer) world)::addScheduledTask);
    }

    /**
     * Computes radiation changes for all worlds.
     */
    private static RadiationUpdates computeRadiationUpdates() {
        RadiationUpdates res = new RadiationUpdates();
        worldMap.values().forEach(w -> {
            RadiationUpdates.WorldUpdate wu = computeWorldRadiationUpdate(w);
            res.updates.put(w, wu);
        });
        return res;
    }

    @SubscribeEvent
    public static void onUpdate(TickEvent.ServerTickEvent e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation) return;

        if (e.phase == Phase.END) {
            ticks++;
            if (radiationFuture.isDone() && ticks % 20 == 17) {
                scheduleUpdate(RadiationSystemNT::scheduleFullUpdate);
            }
        }
        rebuildDirty();
    }

    @SubscribeEvent
    public static void onEntityUpdate(LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.world.isRemote) return;
        WorldServer world = (WorldServer) entity.world;
        if (!GeneralConfig.enableRads || entity.isEntityInvulnerable(ModDamageSource.radiation) || entity instanceof EntityPlayerMP player && player.isSpectator()) return;

        double eRad = HbmLivingProps.getRadiation(entity);

        if (eRad < 100) return;
        if (eRad >= 200 && entity.getHealth() > 0 && entity instanceof EntityCreeper) {
            if (world.rand.nextInt(3) == 0) {
                EntityCreeperNuclear creep = new EntityCreeperNuclear(world);
                creep.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
                if (!entity.isDead) world.spawnEntity(creep);
                entity.setDead();
            } else {
                entity.attackEntityFrom(ModDamageSource.radiation, 100F);
            }
            return;
        } else if (eRad >= 500 && entity instanceof EntityCow && !(entity instanceof EntityMooshroom)) {
            EntityMooshroom creep = new EntityMooshroom(world);
            creep.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
            if (!entity.isDead) world.spawnEntity(creep);
            entity.setDead();
            return;
        } else if (eRad >= 600 && entity instanceof EntityVillager vil) {
            EntityZombieVillager creep = new EntityZombieVillager(world);
            creep.setProfession(vil.getProfession());
            creep.setForgeProfession(vil.getProfessionForge());
            creep.setChild(vil.isChild());
            creep.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
            if (!entity.isDead) world.spawnEntity(creep);
            entity.setDead();
            return;
        } else if (eRad >= 700 && entity instanceof EntityBlaze) {
            EntityRADBeast creep = new EntityRADBeast(world);
            creep.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
            if (!entity.isDead) world.spawnEntity(creep);
            entity.setDead();
            return;
        } else if (eRad >= 800 && entity instanceof EntityHorse horsie) {
            EntityZombieHorse zomhorsie = new EntityZombieHorse(world);
            zomhorsie.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
            zomhorsie.setGrowingAge(horsie.getGrowingAge());
            zomhorsie.setTemper(horsie.getTemper());
            zomhorsie.setHorseSaddled(horsie.isHorseSaddled());
            zomhorsie.setHorseTamed(horsie.isTame());
            zomhorsie.setOwnerUniqueId(horsie.getOwnerUniqueId());
            zomhorsie.makeMad();
            if (!entity.isDead) world.spawnEntity(zomhorsie);
            entity.setDead();
            return;
        } else if (eRad >= 900 && entity instanceof EntityDuck) { // This is now safe since EntityQuackos is invulnerable
            EntityQuackos quacc = new EntityQuackos(world);
            quacc.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
            if (!entity.isDead) world.spawnEntity(quacc);
            entity.setDead();
            return;
        }

        if (eRad > 2500000) HbmLivingProps.setRadiation(entity, 2500000);

        if (eRad >= 1000) {
            entity.attackEntityFrom(ModDamageSource.radiation, 1000F);
            HbmLivingProps.setRadiation(entity, 0);

            if (entity.getHealth() > 0) {
                entity.setHealth(0);
                entity.onDeath(ModDamageSource.radiation);
            }

            if (entity instanceof EntityPlayerMP) AdvancementManager.grantAchievement((EntityPlayerMP) entity, AdvancementManager.achRadDeath);
        } else if (eRad >= 800) {
            if (world.rand.nextInt(300) == 0) entity.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 5 * 30, 0));
            if (world.rand.nextInt(300) == 0) entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 10 * 20, 2));
            if (world.rand.nextInt(300) == 0) entity.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 10 * 20, 2));
            if (world.rand.nextInt(500) == 0) entity.addPotionEffect(new PotionEffect(MobEffects.POISON, 3 * 20, 2));
            if (world.rand.nextInt(700) == 0) entity.addPotionEffect(new PotionEffect(MobEffects.WITHER, 3 * 20, 1));
            if (world.rand.nextInt(300) == 0) entity.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 5 * 20, 3));
            if (world.rand.nextInt(300) == 0) entity.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 5 * 20, 3));
        } else if (eRad >= 600) {
            if (world.rand.nextInt(300) == 0) entity.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 5 * 30, 0));
            if (world.rand.nextInt(300) == 0) entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 10 * 20, 2));
            if (world.rand.nextInt(300) == 0) entity.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 10 * 20, 2));
            if (world.rand.nextInt(500) == 0) entity.addPotionEffect(new PotionEffect(MobEffects.POISON, 3 * 20, 1));
            if (world.rand.nextInt(300) == 0) entity.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 3 * 20, 3));
            if (world.rand.nextInt(400) == 0) entity.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 6 * 20, 2));
        } else if (eRad >= 400) {
            if (world.rand.nextInt(300) == 0) entity.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 5 * 30, 0));
            if (world.rand.nextInt(500) == 0) entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 5 * 20, 0));
            if (world.rand.nextInt(300) == 0) entity.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 5 * 20, 1));
            if (world.rand.nextInt(500) == 0) entity.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 3 * 20, 2));
            if (world.rand.nextInt(600) == 0) entity.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 4 * 20, 1));
        } else if (eRad >= 200) {
            if (world.rand.nextInt(300) == 0) entity.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 5 * 20, 0));
            if (world.rand.nextInt(500) == 0) entity.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 5 * 20, 0));
            if (world.rand.nextInt(700) == 0) entity.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 3 * 20, 2));
            if (world.rand.nextInt(800) == 0) entity.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 4 * 20, 0));
        } else if (eRad >= 100) {
            if (world.rand.nextInt(800) == 0) entity.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 2 * 20, 0));
            if (world.rand.nextInt(1000) == 0) entity.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 20, 0));

            if (entity instanceof EntityPlayerMP) AdvancementManager.grantAchievement((EntityPlayerMP) entity, AdvancementManager.achRadPoison);
        }
    }

    /**
     * Marks a subchunk to be rebuilt. This is used when a radiation resistant block is added or removed
     *
     * @param world - the world to mark in
     * @param pos   - the position to mark at
     */
    public static void markChunkForRebuild(World world, BlockPos pos) {
        if (world.isRemote) return;
        if (!GeneralConfig.advancedRadiation) return;
        int subX = pos.getX() >> 4;
        int subZ = pos.getZ() >> 4;
        int subY = pos.getY() >> 4;
        long key = SubChunkKey.asLong(subX, subZ, subY);
        WorldRadiationData r = getWorldRadData((WorldServer) world);

        if (GeneralConfig.enableDebugMode) {
            MainRegistry.logger.info("[Debug] Marking chunk dirty at [{}, {}], section {}", subX, subZ, subY);
        }

        //Ensures we don't run into any problems with concurrent modification
        if (r.iteratingDirty) {
            r.dirtySections2.add(key);
        } else {
            r.dirtySections.add(key);
        }
    }

    public static void markSectionForRebuild(WorldServer world, long sck){
        if (!GeneralConfig.advancedRadiation) return;
        WorldRadiationData r = getWorldRadData(world);
        if (r.iteratingDirty) {
            r.dirtySections2.add(sck);
        } else {
            r.dirtySections.add(sck);
        }
    }

    /**
     * Rebuilds stored dirty chunks
     */
    private static void rebuildDirty() {
        worldMap.values().parallelStream().forEach(r -> {
            //Set the iteration flag to avoid concurrent modification
            r.iteratingDirty = true;

            //For each dirty sub chunk, rebuild it
            LongIterator iterator = r.dirtySections.iterator();
            while (iterator.hasNext()) {
                long dirtyKey = iterator.nextLong();
                int cx = SubChunkKey.getSubX(dirtyKey);
                int cz = SubChunkKey.getSubZ(dirtyKey);
                int subY = SubChunkKey.getSubY(dirtyKey);
                if (GeneralConfig.enableDebugMode) {
                    MainRegistry.logger.info("[Debug] Rebuilding chunk pockets for dirty chunk at [{}, {}] section {}", cx, cz, subY);
                }
                if (r.world.getChunkProvider().chunkExists(cx, cz)) {
                    rebuildChunkPockets(r.world, r.world.getChunk(cx, cz), subY);
                    iterator.remove();
                } else {
                    r.deferredRebuildSections.add(dirtyKey);
                    iterator.remove();
                }
            }
            r.iteratingDirty = false;
            //Clear the dirty chunks lists, and add any chunks that might have been marked while iterating to be dealt with next tick.
            LongIterator stagedIterator = r.dirtySections2.iterator();
            while (stagedIterator.hasNext()) {
                long dirtyKey = stagedIterator.nextLong();
                r.dirtySections.add(dirtyKey);
            }
            r.dirtySections2.clear();
        });
    }

    @SubscribeEvent
    public static void onWorldUpdate(TickEvent.WorldTickEvent e) {
        if (e.world.isRemote) return;
        WorldServer worldServer = (WorldServer) e.world;
        if (GeneralConfig.enableDebugMode) {
            MainRegistry.logger.info("[Debug] onWorldUpdate called for RadSys tick {}", ticks);
        }
        if (e.phase == Phase.START) {
            RadiationWorldHandler.handleWorldDestruction(worldServer);
        }
        if (GeneralConfig.enableRads) {
            int thunder = AuxSavedData.getThunder(worldServer);
            if (thunder > 0) AuxSavedData.setThunder(worldServer, thunder - 1);
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation) return;
        if (e.getWorld().isRemote) return;
        WorldServer world = (WorldServer) e.getWorld();
        WorldRadiationData data = getWorldRadData(world);
        ChunkPos chunkPos = e.getChunk().getPos();
        final int subChunkCount = WorldUtil.getSubChunkCount(world);

        for (int i = 0; i < subChunkCount; i++) {
            long key = SubChunkKey.asLong(chunkPos.x, chunkPos.z, i);
            SubChunkRadiationStorage storage = data.sectionsStorage.remove(key);
            if (storage != null) {
                storage.unload();
            }
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkDataEvent.Load e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation) return;
        if (e.getWorld().isRemote) return;
        if (!e.getData().hasKey("hbmRadDataNT")) return;
        //If this chunk had saved radiation in it, read it and add the persistent chunk data at this chunk position
        WorldRadiationData data = getWorldRadData((WorldServer) e.getWorld());
        readFromNBT(data, e.getChunk().getPos(), e.getData().getCompoundTag("hbmRadDataNT"));

        ChunkPos cpos = e.getChunk().getPos();
        final int subChunkCount = WorldUtil.getSubChunkCount(e.getWorld());
        for (int i = 0; i < subChunkCount; i++) {
            long key = SubChunkKey.asLong(cpos.x, cpos.z, i);
            if (data.deferredRebuildSections.remove(key)) {
                data.dirtySections.add(key);
            }
        }
    }

    @SubscribeEvent
    public static void onChunkSave(ChunkDataEvent.Save e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation) return;
        if (!e.getWorld().isRemote) {
            WorldRadiationData data = getWorldRadData((WorldServer) e.getWorld());
            NBTTagCompound tag = writeToNBT(data, e.getChunk().getPos());
            if (tag != null) {
                e.getData().setTag("hbmRadDataNT", tag);
            }
        }
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation) return;
        if (!e.getWorld().isRemote) {
            //Always make sure worlds have radiation data
            worldMap.put((WorldServer) e.getWorld(), new WorldRadiationData((WorldServer) e.getWorld()));
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation) return;
        if (!e.getWorld().isRemote) {
            //Remove the world data on unload
            worldMap.remove((WorldServer) e.getWorld());
        }
    }

    /**
     * Computes radiation changes for a single world.
     */
    private static RadiationUpdates.WorldUpdate computeWorldRadiationUpdate(WorldRadiationData worldData) {
        RadiationUpdates.WorldUpdate wu = new RadiationUpdates.WorldUpdate();
        final ThreadLocalRandom rand = ThreadLocalRandom.current();
        final double minB = minBoundFor(worldData.world);
        final int minY = WorldUtil.getMinWorldHeight(worldData.world);
        final int maxY = WorldUtil.getMaxWorldHeight(worldData.world);
        Set<RadPocket> setView = worldData.getActivePocketsView();
        setView.parallelStream().forEach(p -> {
            BlockPos pos = p.parent.subChunkPos;

            // Symmetric decay towards 0, with clamp to -background
            p.radiation.updateAndGet(current -> {
                final double decay = (current > 0d) ? -0.05d : (current < 0d ? +0.05d : 0d);
                final double next  = current * 0.999d + decay;
                return Math.max(minB, next);
            });

            wu.dirtyChunkPositions.add(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
            double currentRadiation = p.radiation.get();

            if (nearZero(currentRadiation)) {
                p.radiation.set(0.0d);
                p.accumulatedRads.reset();
                return;
            }

            // Near-zero pruning (non-sealed): nudge toward 0 symmetrically
            if (!p.isSealed() && Math.abs(currentRadiation) < PRUNE_THRESHOLD) {
                p.radiation.updateAndGet(cur -> {
                    double n = cur + (cur > 0d ? -EXTRA_DECAY : (cur < 0d ? +EXTRA_DECAY : 0d));
                    // keep between [-background, 0] if negative, or allow >0 if positive
                    if (n < 0d) n = Math.max(minB, n);
                    return n;
                });
                currentRadiation = p.radiation.get();
                if (nearZero(currentRadiation)) {
                    p.radiation.set(0.0d);
                    p.accumulatedRads.reset();
                    return;
                }
            }

            if (currentRadiation > RadiationConfig.fogRad && rand.nextInt(RadiationConfig.fogCh) == 0) {
                //Fog calculation works slightly differently here to account for the 3d nature of the system
                //We just try 10 random coordinates of the sub chunk
                //If the coordinate is inside this pocket and the block at the coordinate is air,
                //use it to spawn a rad particle at that block and break
                //Also only spawn it if it's close to the ground, otherwise you get a giant fart when nukes go off.
                for (int i = 0; i < 10; i++) {
                    BlockPos randPos = new BlockPos(rand.nextInt(16), rand.nextInt(16), rand.nextInt(16));
                    if (p.parent.getPocket(randPos) == p) {
                        final BlockPos worldPos = randPos.add(pos);
                        final IBlockState state = worldData.world.getBlockState(worldPos);
                        final Vec3d rPos = new Vec3d(worldPos.getX() + 0.5, worldPos.getY() + 0.5, worldPos.getZ() + 0.5);
                        final RayTraceResult trace = worldData.world.rayTraceBlocks(rPos, rPos.add(0, -6, 0));
                        if (state.getBlock().isAir(state, worldData.world, worldPos) && trace != null && trace.typeOfHit == Type.BLOCK) {
                            wu.fogPositions.add(worldPos);
                            break;
                        }
                    }
                }
            }

            double connectionCount = 0D;
            for (EnumFacing e : EnumFacing.VALUES) connectionCount += p.connectionIndices[e.ordinal()].size();
            final double amountPer = (connectionCount > 0D && Math.abs(currentRadiation) >= 1D) ? 0.7D / connectionCount : 0D;

            final double radForThisTick = p.radiation.get();
            // All pockets, even those not spreading, retain their own radiation value.
            p.accumulatedRads.add(radForThisTick);

            // Diffuse both positive and negative radiation
            if (amountPer > 0D && radForThisTick != 0D) {
                p.accumulatedRads.add(-radForThisTick * 0.7D); // export share
                for (EnumFacing e : EnumFacing.VALUES) {
                    BlockPos nPos = pos.offset(e, 16);
                    if (!worldData.world.isBlockLoaded(nPos) || nPos.getY() < minY || nPos.getY() >= maxY) continue;
                    List<Integer> cons = p.connectionIndices[e.ordinal()];
                    if (cons.contains(-1)) {
                        wu.sectionsForRebuild.add(SubChunkKey.asLong(nPos.getX() >> 4, nPos.getZ() >> 4, nPos.getY() >> 4));
                        continue;
                    }
                    SubChunkRadiationStorage sc2 = getSubChunkStorage(worldData.world, nPos);
                    if (sc2 == null) {
                        wu.sectionsForRebuild.add(SubChunkKey.asLong(nPos.getX() >> 4, nPos.getZ() >> 4, nPos.getY() >> 4));
                        continue;
                    }
                    for (int idx : cons) {
                        if (idx < 0 || idx >= sc2.pockets.length) continue;
                        RadPocket target = sc2.pockets[idx];
                        if (!target.isSealed()) {
                            target.accumulatedRads.add(radForThisTick * amountPer);
                            // Collect the newly irradiated pocket.
                            wu.toAdd.add(target);
                        }
                    }
                }
            }
        });

        // Apply accumulated totals & decide active set membership
        setView.addAll(wu.toAdd);
        setView.parallelStream().forEach(act -> {
            final double newRad = act.accumulatedRads.sumThenReset();
            final double bounded = Math.max(minB, newRad);
            if (nearZero(bounded)) {
                act.radiation.set(0D);
                wu.toRemove.add(act);
            } else {
                act.radiation.set(bounded);
            }
        });
        return wu;
    }

    /**
     * Applies computed radiation changes for all worlds. MUST be called from the main server thread.
     */
    private static void applyRadiationUpdates(RadiationUpdates updates) {
        for (Map.Entry<WorldRadiationData, RadiationUpdates.WorldUpdate> entry : updates.updates.entrySet()) {
            applyWorldRadiationUpdate(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Applies computed radiation changes for a single world. MUST be called from the main server thread.
     */
    private static void applyWorldRadiationUpdate(WorldRadiationData worldData, RadiationUpdates.WorldUpdate update) {
        for (RadPocket p : update.toAdd) worldData.addActivePocket(p);
        for (RadPocket p : update.toRemove) worldData.removeActivePocket(p);

        LongIterator iterator = update.dirtyChunkPositions.iterator();
        while (iterator.hasNext()) {
            long cp = iterator.nextLong();
            int cx = ChunkUtil.getChunkPosX(cp);
            int cz = ChunkUtil.getChunkPosZ(cp);
            if (worldData.world.getChunkProvider().chunkExists(cx, cz)) {
                worldData.world.getChunk(cx, cz).markDirty();
            }// else noop: chunk was unloaded before application. Nothing we can do to make up for this.
        }

        LongIterator iter = update.sectionsForRebuild.iterator();
        while (iter.hasNext()) {
            long d = iter.nextLong();
            markSectionForRebuild(worldData.world, d);
        }

        for (BlockPos f : update.fogPositions) {
            NBTTagCompound data = new NBTTagCompound();
            data.setString("type", "radiationfog");
            PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, f.getX() + 0.5F, f.getY() + 0.5F, f.getZ() + 0.5F),
                    new TargetPoint(worldData.world.provider.getDimension(), f.getX(), f.getY(), f.getZ(), 100));
        }
    }

    /**
     * Divides a 16x16x16 sub chunk into pockets that are separated by radiation resistant blocks.
     * These pockets are also linked to other pockets in neighboring chunks
     *
     * @param worldServer
     * @param chunk       - the chunk to rebuild
     * @param yIndex      - the Y index of the sub chunk to rebuild
     */
    private static void rebuildChunkPockets(WorldServer worldServer, Chunk chunk, int yIndex) {
        BlockPos subChunkPos = new BlockPos(chunk.getPos().x << 4, yIndex << 4, chunk.getPos().z << 4);
        if (GeneralConfig.enableDebugMode) {
            MainRegistry.logger.info("[Debug] Starting rebuild of chunk at {}", new BlockPos(chunk.getPos().x, yIndex, chunk.getPos().z));
        }

        //Initialize all the necessary variables. A list of pockets for the sub chunk, the block storage for this sub chunk,
        //an array of rad pockets for fast pocket lookup by blockpos, chunk radiation storage for this position
        //And finally a new sub chunk that will be added to the chunk radiation storage when it's filled with data
        List<RadPocket> pockets = new ArrayList<>();
        ExtendedBlockStorage blocks = chunk.getBlockStorageArray()[yIndex];
        WorldRadiationData data = getWorldRadData(worldServer);
        SubChunkRadiationStorage subChunk = new SubChunkRadiationStorage(data, subChunkPos);

        if (blocks != null) {
            byte[] pocketData = new byte[2048]; // 4096 blocks * 4 bits/block / 8 bits/byte
            Arrays.fill(pocketData, (byte) 0xFF);

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        // Check if this block has already been assigned to a pocket.
                        int blockIndex = (x << 8) | (y << 4) | z;
                        int byteIndex = blockIndex / 2;
                        int paletteIndex = (blockIndex % 2 == 0) ? (pocketData[byteIndex] >> 4) & 0x0F : pocketData[byteIndex] & 0x0F;

                        if (paletteIndex != SubChunkRadiationStorage.NO_POCKET_INDEX) {
                            continue; // Already processed
                        }

                        Block block = blocks.get(x, y, z).getBlock();
                        BlockPos localPos = new BlockPos(x, y, z);

                        if (!(block instanceof IRadResistantBlock && ((IRadResistantBlock) block).isRadResistant(worldServer,
                                localPos.add(subChunkPos)))) {
                            // This block is not resistant and has no pocket yet. Start a flood fill.
                            int currentPaletteIndex;
                            // If we have more than 14 pockets, merge new pockets into pocket 0 to prevent overflow.
                            // This is intended! having > 14 pockets in one single subchunk is extremely rare
                            if (pockets.size() >= 15) {
                                currentPaletteIndex = 0; // Merge with pocket 0
                            } else {
                                currentPaletteIndex = pockets.size();
                                pockets.add(new RadPocket(subChunk, currentPaletteIndex));
                            }
                            // Run the flood fill for this new pocket.
                            buildPocket(worldServer, localPos, subChunkPos, blocks, pocketData, pockets.get(currentPaletteIndex));
                        }
                    }
                }
            }
            // If there's only one pocket, we can save 2KB by using null, as the entire subchunk is uniform.
            subChunk.pocketData = pockets.size() <= 1 ? null : pocketData;
        } else {
            RadPocket pocket = new RadPocket(subChunk, 0);
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    doEmptyChunk(worldServer, chunk, subChunkPos, new BlockPos(x, 15, z), pocket, EnumFacing.UP);
                    doEmptyChunk(worldServer, chunk, subChunkPos, new BlockPos(x, 0, z), pocket, EnumFacing.DOWN);
                }
            }
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    doEmptyChunk(worldServer, chunk, subChunkPos, new BlockPos(x, y, 0), pocket, EnumFacing.NORTH);
                    doEmptyChunk(worldServer, chunk, subChunkPos, new BlockPos(x, y, 15), pocket, EnumFacing.SOUTH);
                }
            }
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    doEmptyChunk(worldServer, chunk, subChunkPos, new BlockPos(15, y, z), pocket, EnumFacing.EAST);
                    doEmptyChunk(worldServer, chunk, subChunkPos, new BlockPos(0, y, z), pocket, EnumFacing.WEST);
                }
            }
            pockets.add(pocket);
            subChunk.pocketData = null;
        }

        subChunk.pockets = pockets.toArray(new RadPocket[0]);

        //Finally, put the newly built sub chunk into the data
        long key = SubChunkKey.asLong(chunk.getPos().x, chunk.getPos().z, yIndex);
        SubChunkRadiationStorage old = data.sectionsStorage.put(key, subChunk);
        if (old != null) {
            subChunk.setRad(old);
        }
        subChunk.add(worldServer, subChunkPos);
        if (GeneralConfig.enableDebugMode) {
            MainRegistry.logger.info("[Debug] Finished rebuild of chunk at {} with {} pockets.", new BlockPos(chunk.getPos().x, yIndex,
                    chunk.getPos().z), pockets.size());
        }
    }

    private static void doEmptyChunk(WorldServer worldServer, Chunk chunk, BlockPos subChunkPos, BlockPos pos, RadPocket pocket, EnumFacing facing) {
        BlockPos newPos = pos.offset(facing);
        BlockPos outPos = newPos.add(subChunkPos);
        Block block = chunk.getWorld().getBlockState(outPos).getBlock();
        //If the block isn't radiation resistant...
        if (!(block instanceof IRadResistantBlock && ((IRadResistantBlock) block).isRadResistant(worldServer, outPos))) {
            if (!isSubChunkLoaded(worldServer, outPos)) {
                //if it's not loaded, mark it with a single -1 value. This will tell the update method that the
                //Chunk still needs to be loaded to propagate radiation into it
                if (!pocket.connectionIndices[facing.ordinal()].contains(-1)) {
                    pocket.connectionIndices[facing.ordinal()].add(-1);
                }
            } else {
                //If it is loaded, see if the pocket at that position is already connected to us. If not, add it as a connection.
                //Setting outPocket's connection will be handled in setForYLevel

                RadPocket outPocket = getPocket(worldServer, outPos);
                if (outPocket != null && !pocket.connectionIndices[facing.ordinal()].contains(outPocket.index))
                    pocket.connectionIndices[facing.ordinal()].add(outPocket.index);
            }
        }
    }

    /**
     * Builds a pocket using a flood fill.
     *
     * @param world            - world we're building in
     * @param start            - the block pos to flood fill from
     * @param subChunkWorldPos - the world position of the sub chunk
     * @param chunk            - the block storage to pull blocks from
     * @param pocketData       - the byte array to populate with palette indices
     * @param pocket           - the pocket object (containing the index) to assign
     */
    private static void buildPocket(WorldServer world, BlockPos start, BlockPos subChunkWorldPos,
                                    ExtendedBlockStorage chunk, byte[] pocketData, RadPocket pocket) {
        int paletteIndex = pocket.index;
        Queue<BlockPos> queue = new ArrayDeque<>(1024);
        queue.add(start);
        int minY = WorldUtil.getMinWorldHeight(world);
        int maxY = WorldUtil.getMaxWorldHeight(world);

        // Set the starting block's palette index
        setPaletteIndex(pocketData, start, paletteIndex);

        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();

            //For each direction...
            for (EnumFacing facing : EnumFacing.VALUES) {
                BlockPos newPos = pos.offset(facing);

                if (newPos.getX() < 0 || newPos.getX() > 15 || newPos.getY() < 0 || newPos.getY() > 15 || newPos.getZ() < 0 || newPos.getZ() > 15) {
                    //If we're outside the sub chunk bounds, try to connect to neighbor chunk pockets
                    BlockPos outPos = newPos.add(subChunkWorldPos);
                    //If this position is out of bounds, do nothing
                    if (outPos.getY() < minY || outPos.getY() >= maxY) continue;
                    //Will also attempt to load the chunk, which will cause neighbor data to be updated correctly if it's unloaded.
                    Block block = world.getBlockState(outPos).getBlock();
                    //If the block isn't radiation resistant...
                    if (!(block instanceof IRadResistantBlock resistantBlock && resistantBlock.isRadResistant(world, outPos))) {
                        if (!isSubChunkLoaded(world, outPos)) {
                            //if it's not loaded, mark it with a single -1 value. This will tell the update method that the
                            //Chunk still needs to be loaded to propagate radiation into it
                            if (!pocket.connectionIndices[facing.ordinal()].contains(-1)) {
                                pocket.connectionIndices[facing.ordinal()].add(-1);
                            }
                        } else {
                            //If it is loaded, see if the pocket at that position is already connected to us. If not, add it as a connection.
                            //Setting outPocket's connection will be handled in setForYLevel
                            RadPocket outPocket = getPocket(world, outPos);
                            if (outPocket != null && !pocket.connectionIndices[facing.ordinal()].contains(outPocket.index)) {
                                pocket.connectionIndices[facing.ordinal()].add(outPocket.index);
                            }
                        }
                    }
                } else {
                    // Inside the sub-chunk, check if we should continue the flood fill
                    int blockIndex = (newPos.getX() << 8) | (newPos.getY() << 4) | newPos.getZ();
                    int byteIndex = blockIndex / 2;
                    int existingPaletteIndex = (blockIndex % 2 == 0) ? (pocketData[byteIndex] >> 4) & 0x0F : pocketData[byteIndex] & 0x0F;

                    // Continue if the block is not yet processed
                    if (existingPaletteIndex == SubChunkRadiationStorage.NO_POCKET_INDEX) {
                        Block block = chunk.get(newPos.getX(), newPos.getY(), newPos.getZ()).getBlock();
                        if (!(block instanceof IRadResistantBlock && ((IRadResistantBlock) block).isRadResistant(world,
                                newPos.add(subChunkWorldPos)))) {
                            setPaletteIndex(pocketData, newPos, paletteIndex);
                            queue.add(newPos);
                        }
                    }
                }
            }
        }
    }

    private static void setPaletteIndex(byte[] pocketData, BlockPos pos, int paletteIndex) {
        int blockIndex = (pos.getX() << 8) | (pos.getY() << 4) | pos.getZ();
        int byteIndex = blockIndex / 2;
        byte existingByte = pocketData[byteIndex];
        if (blockIndex % 2 == 0) { // Even index, use high nibble
            pocketData[byteIndex] = (byte) ((existingByte & 0x0F) | (paletteIndex << 4));
        } else { // Odd index, use low nibble
            pocketData[byteIndex] = (byte) ((existingByte & 0xF0) | paletteIndex);
        }
    }

    private static double dimBackground(@NotNull WorldServer world) {
        Object v = CompatibilityConfig.dimensionRad.get(world.provider.getDimension());
        return (v instanceof Number) ? ((Number) v).doubleValue() : 0D;
    }

    private static double minBoundFor(@NotNull WorldServer world) {
        return -dimBackground(world); // -background
    }

    private static boolean nearZero(double v) {
        return Math.abs(v) < 1.0e-9d;
    }

    private static NBTTagCompound writeToNBT(WorldRadiationData data, ChunkPos chunkPos) {
        boolean hasData = false;
        ByteBuffer buf = BUF.get();
        buf.clear();
        final int subChunkCount = WorldUtil.getSubChunkCount(data.world);
        for (int i = 0; i < subChunkCount; i++) {
            long key = SubChunkKey.asLong(chunkPos.x, chunkPos.z, i);
            SubChunkRadiationStorage sc = data.sectionsStorage.get(key);
            if (sc == null) {
                buf.put((byte) 0);
            } else {
                hasData = true;
                buf.put((byte) 1);
                buf.putShort((short) sc.yLevel);
                buf.putShort((short) sc.pockets.length);
                for (RadPocket p : sc.pockets) {
                    writePocket(buf, p); // v1 uses doubles
                }
                if (sc.pocketData == null) {
                    buf.put((byte) 0);
                } else {
                    buf.put((byte) 1);
                    buf.put(sc.pocketData);
                }
            }
        }
        if (!hasData) return null;
        buf.flip();
        byte[] arr = new byte[buf.limit()];
        buf.get(arr);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("fmt", "v2");
        tag.setInteger("subChunkCount", subChunkCount);
        tag.setByteArray("chunkRadData", arr);
        return tag;
    }

    private static void readFromNBT(WorldRadiationData data, ChunkPos chunkPos, NBTTagCompound tag) {
        final String fmt = tag.hasKey("fmt") ? tag.getString("fmt") : "v0";
        final boolean useDouble = "v1".equals(fmt) || "v2".equals(fmt);
        final int runtimeSubChunks = WorldUtil.getSubChunkCount(data.world);
        final int storedSubChunks;
        if ("v2".equals(fmt) && tag.hasKey("subChunkCount")) {
            storedSubChunks = tag.getInteger("subChunkCount");
        } else {
            storedSubChunks = 16;
        }
        final int maxSubChunks = Math.min(runtimeSubChunks, storedSubChunks);

        ByteBuffer bdata = ByteBuffer.wrap(tag.getByteArray("chunkRadData"));
        for (int i = 0; i < maxSubChunks; i++) {
            try {
                if (bdata.remaining() == 0) break;
                byte has = bdata.get();
                if (has == 1) {
                    int yLevel = bdata.getShort();
                    if (yLevel >> 4 != i)
                        throw new IllegalStateException("Sub-chunk y-level mismatch. Expected index " + i + ", but data says " + (yLevel >> 4));
                    SubChunkRadiationStorage sc = new SubChunkRadiationStorage(data, new BlockPos(chunkPos.x << 4, yLevel, chunkPos.z << 4));
                    int len = bdata.getShort();
                    sc.pockets = new RadPocket[len];
                    for (int j = 0; j < len; j++) {
                        sc.pockets[j] = readPocket(bdata, sc, useDouble);
                        if (sc.pockets[j] != null && !nearZero(sc.pockets[j].radiation.get())) {
                            data.addActivePocket(sc.pockets[j]);
                        }
                    }
                    byte hasPalette = bdata.get();
                    if (hasPalette == 1) {
                        sc.pocketData = new byte[2048];
                        bdata.get(sc.pocketData);
                    } else {
                        sc.pocketData = null;
                    }
                    long key = SubChunkKey.asLong(chunkPos.x, chunkPos.z, yLevel >> 4);
                    data.sectionsStorage.put(key, sc);
                }
            } catch (BufferUnderflowException | IndexOutOfBoundsException | IllegalStateException ex) {
                MainRegistry.logger.error("Data corruption detected while reading radiation data for sub-chunk {} in chunk {}. ", i, chunkPos, ex);
                BlockPos subChunkPos = new BlockPos(chunkPos.x << 4, i << 4, chunkPos.z << 4);
                SubChunkRadiationStorage corruptedStorage = new SubChunkRadiationStorage(data, subChunkPos);
                corruptedStorage.pockets = new RadPocket[]{new RadPocket(corruptedStorage, 0)};
                corruptedStorage.pocketData = null;
                long key = SubChunkKey.asLong(chunkPos.x, chunkPos.z, i);
                data.sectionsStorage.put(key, corruptedStorage);
                break;
            }
        }
    }

    private static void writePocket(@SuppressWarnings("SameParameterValue") ByteBuffer buf, RadPocket p) {
        if (p == null) {
            buf.putInt(-1);
            buf.putDouble(0.0d); // v1: double
            for (EnumFacing ignored : EnumFacing.VALUES) {
                buf.putShort((short) 0);
            }
            return;
        }
        buf.putInt(p.index);
        buf.putDouble(p.radiation.get());
        for (EnumFacing e : EnumFacing.VALUES) {
            List<Integer> indc = p.connectionIndices[e.ordinal()];
            buf.putShort((short) indc.size());
            for (int idx : indc) {
                buf.putShort((short) idx);
            }
        }
    }

    private static RadPocket readPocket(ByteBuffer buf, SubChunkRadiationStorage parent, boolean isV1) {
        int index = buf.getInt();
        if (index == -1) return null;
        RadPocket p = new RadPocket(parent, index);
        if (isV1) {
            p.radiation.set(buf.getDouble());
        } else {
            // Legacy payload (no "fmt"), floats
            p.radiation.set(buf.getFloat());
        }
        for (EnumFacing e : EnumFacing.VALUES) {
            short size = buf.getShort();
            List<Integer> indc = p.connectionIndices[e.ordinal()];
            for (short k = 0; k < size; k++) {
                indc.add((int) buf.getShort());
            }
        }
        return p;
    }

    private static class RadiationUpdates {
        Map<WorldRadiationData, WorldUpdate> updates = new ConcurrentHashMap<>();

        // we get a new instance of WorldUpdate for each world on every radsys update
        static class WorldUpdate {
            final Set<RadPocket> toAdd = ConcurrentHashMap.newKeySet();
            final Set<RadPocket> toRemove = ConcurrentHashMap.newKeySet();
            final LongSet dirtyChunkPositions = NonBlockingHashMapLong.newKeySet();
            // key: subchunkkey
            final LongSet sectionsForRebuild = NonBlockingHashMapLong.newKeySet();
            final Set<BlockPos> fogPositions = ConcurrentHashMap.newKeySet();
        }
    }

    /*
     * And finally, the data structure part.
     * The hierarchy goes like this:
     * WorldRadiationData - Stores ChunkRadiationStorages, one per chunk. Also keeps dirty chunks that need to be rebuilt and a set of active rad
     * pockets
     * 		ChunkRadiationStorage - Stores an array of SubChunkRadiationStorage, one for each 16 tall section.
     * 			SubChunkRadiationStorage - Stores and array of RadPockets as well as a larger array representing the RadPocket in each position in
     * the sub chunk
     * 				RadPocket - Stores the actual radiation value as well as connections to neighboring RadPockets by indices
     */

    //A list of pockets completely closed off by radiation resistant blocks
    public static class RadPocket {
        public final AtomicDouble radiation = new AtomicDouble(0.0d);
        @SuppressWarnings("unchecked")
        public final List<Integer>[] connectionIndices = new CopyOnWriteArrayList[EnumFacing.VALUES.length];
        private final DoubleAdder accumulatedRads = new DoubleAdder();
        public SubChunkRadiationStorage parent;
        public int index;

        public RadPocket(SubChunkRadiationStorage parent, int index) {
            this.parent = parent;
            this.index = index;
            for (int i = 0; i < EnumFacing.VALUES.length; i++) {
                connectionIndices[i] = new CopyOnWriteArrayList<>();
            }
        }

        /**
         * Mainly just removes itself from the active pockets list
         */
        protected void remove() {
            for (EnumFacing e : EnumFacing.VALUES) {
                connectionIndices[e.ordinal()].clear();
            }
            parent.parent.removeActivePocket(this);
        }

        /**
         * @return the world position of the sub chunk this pocket is in
         */
        public BlockPos getSubChunkPos() {
            return parent.subChunkPos;
        }

        /**
         * Checks if a pocket is radiation shielded against other pockets or chunks
         *
         * @return if pocket is sealed
         */
        public boolean isSealed() {
            // Sealed pockets should have no connects to other chunks (-1) or other pockets
            double count = 0;
            for (EnumFacing e : EnumFacing.VALUES) {
                count += this.connectionIndices[e.ordinal()].size();
            }
            return (count == 0);
        }
    }

    //the smaller 16*16*16 chunk
    public static class SubChunkRadiationStorage {
        public static final int NO_POCKET_INDEX = 15; // The sentinel value for a resistant block (binary 1111)
        public WorldRadiationData parent;
        /**
         * the base position of the sub chunk. see {@link RadiationWorldHandler#handleAdvancedDestruction}
         */
        public BlockPos subChunkPos;
        public int yLevel;
        /**
         * A bit-packed array storing the palette index for each of the 4096 blocks.
         * Each byte holds two 4-bit indices.
         * If this is null, it signifies the entire sub-chunk is one single pocket (optimization).
         */
        public byte[] pocketData;
        /**
         * The palette of unique pockets in this sub-chunk.
         */
        public RadPocket[] pockets;

        public SubChunkRadiationStorage(WorldRadiationData parent, BlockPos subChunkPos) {
            this.parent = parent;
            this.subChunkPos = subChunkPos;
            this.yLevel = subChunkPos.getY();
        }

        /**
         * Gets the pocket at the position using the optimized palette encoding.
         *
         * @param pos - the position to get the pocket at
         * @return the pocket at the specified position, or null if the block is radiation-resistant.
         */
        @Nullable
        public RadPocket getPocket(BlockPos pos) {
            if (this.pocketData == null) {
                return (pockets != null && pockets.length > 0) ? pockets[0] : null;
            }
            int x = pos.getX() & 15;
            int y = pos.getY() & 15;
            int z = pos.getZ() & 15;

            int blockIndex = (x << 8) | (y << 4) | z;
            int byteIndex = blockIndex / 2;
            byte b = pocketData[byteIndex];

            // Extract the 4-bit palette index from the correct half of the byte (nibble).
            int paletteIndex = (blockIndex % 2 == 0) ? (b >> 4) & 0x0F : b & 0x0F;

            if (paletteIndex == NO_POCKET_INDEX || paletteIndex >= pockets.length) {
                return null; // This is a resistant block or invalid data.
            }
            return pockets[paletteIndex];
        }

        /**
         * Attempts to distribute radiation from another sub chunk into this one's pockets.
         *
         * @param other - the sub chunk to set from
         */
        public void setRad(SubChunkRadiationStorage other) {
            //Accumulate a total, and divide that evenly among our pockets
            double total = 0d;
            for (RadPocket p : other.pockets) {
                // Sealed pockets should not attribute to total rad count
                if (!p.isSealed()) {
                    total += p.radiation.get();
                }
            }

            if (pockets.length > 0) {
                double radPer = total / pockets.length;
                for (RadPocket p : pockets) {
                    p.radiation.set(radPer);
                    if (radPer != 0d) {
                        //If the pocket now has radiation or is sealed, mark it as active
                        parent.addActivePocket(p);
                    }
                }
            }
        }

        /**
         * Remove from the world
         *
         * @param world - the world to remove from
         * @param pos   - the pos to remove from
         */
        public void remove(WorldServer world, BlockPos pos) {
            for (RadPocket p : pockets) {
                //Call remove for each pocket
                p.remove();
            }
            for (EnumFacing e : EnumFacing.VALUES) {
                //Tries to load the chunk so it updates right.
                world.getBlockState(pos.offset(e, 16));
                if (isSubChunkLoaded(world, pos.offset(e, 16))) {
                    SubChunkRadiationStorage sc = getSubChunkStorage(world, pos.offset(e, 16));
                    //Clears any connections the neighboring chunk has to this sub chunk
                    if (sc != null) {
                        for (RadPocket p : sc.pockets) {
                            p.connectionIndices[e.getOpposite().ordinal()].clear();
                        }
                    }
                }
            }
        }

        /**
         * Adds to the world
         *
         * @param world - the world to add to
         * @param pos   - the position to add to
         */
        public void add(WorldServer world, BlockPos pos) {
            for (EnumFacing e : EnumFacing.VALUES) {
                // Force chunk loading by accessing block state
                world.getBlockState(pos.offset(e, 16));
                if (isSubChunkLoaded(world, pos.offset(e, 16))) {
                    SubChunkRadiationStorage sc = getSubChunkStorage(world, pos.offset(e, 16));
                    if (sc != null && sc.pockets != null) {
                        // Clear all the neighbor's references to this sub-chunk
                        for (RadPocket p : sc.pockets) {
                            p.connectionIndices[e.getOpposite().ordinal()].clear();
                        }
                        // Sync connections to the neighbor to make it two-way
                        for (RadPocket p : pockets) {
                            List<Integer> indc = p.connectionIndices[e.ordinal()];
                            for (int idx : indc) {
                                if (idx >= 0 && idx < sc.pockets.length) {
                                    List<Integer> oppList = sc.pockets[idx].connectionIndices[e.getOpposite().ordinal()];
                                    if (oppList.contains(-1)) {
                                        oppList.remove(Integer.valueOf(-1));
                                    }
                                    if (!oppList.contains(p.index)) {
                                        oppList.add(p.index);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        public void unload() {
            for (RadPocket p : pockets) {
                parent.removeActivePocket(p);
            }
        }
    }

    public static class WorldRadiationData {
        /**
         * <p>Primary set of subchunks awaiting to be rebuilt.</p>
         * <p>Key: packed subchunk</p>
         */
        private final LongSet dirtySections = NonBlockingHashMapLong.newKeySet();

        /**
         * <p>Secondary set of subchunks awaiting to be rebuilt.</p>
         * <p>Key: packed subchunk</p>
         */
        private final LongSet dirtySections2 = NonBlockingHashMapLong.newKeySet();
        private final Set<RadPocket> activePockets = ConcurrentHashMap.newKeySet();

        /**
         * <p>Set of subchunks that will be marked for rebuild on load. Those which are added to dirtyChunks but turns out to be unloaded during iteration
         * will end up here.</p>
         * <p>Key: packed subchunk</p>
         */
        private final LongSet deferredRebuildSections = NonBlockingHashMapLong.newKeySet();
        public final WorldServer world;
        /**
         * key: subchunk
         */
        public final NonBlockingHashMapLong<SubChunkRadiationStorage> sectionsStorage = new NonBlockingHashMapLong<>();
        private volatile boolean iteratingDirty = false;

        public WorldRadiationData(WorldServer world) {
            this.world = world;
        }

        public Set<RadPocket> getActivePocketsView() {
            if (GeneralConfig.enableDebugMode) {
                MainRegistry.logger.info("[Debug] Queried active pockets for world {}", world);
            }
            return this.activePockets;
        }

        public void addActivePocket(RadPocket radPocket) {
            this.activePockets.add(radPocket);
            if (GeneralConfig.enableDebugMode) {
                SubChunkKey chunkKey = new SubChunkKey(radPocket.getSubChunkPos().getX() >> 4, radPocket.getSubChunkPos().getZ() >> 4,
                        radPocket.getSubChunkPos().getY() >> 4);
                MainRegistry.logger.info("[Debug] Added active pocket {} (radiation: {}, accumulatedRads: {}, sealed: {}) at {} (Chunk:{}) for " +
                        "world {}", radPocket.index, radPocket.radiation.get(), radPocket.accumulatedRads.sum(), radPocket.isSealed(),
                        radPocket.getSubChunkPos(), chunkKey, world);
            }
        }

        public void removeActivePocket(RadPocket radPocket) {
            this.activePockets.remove(radPocket);
            if (GeneralConfig.enableDebugMode) {
                SubChunkKey chunkKey = new SubChunkKey(radPocket.getSubChunkPos().getX() >> 4, radPocket.getSubChunkPos().getZ() >> 4,
                        radPocket.getSubChunkPos().getY() >> 4);
                MainRegistry.logger.info("[Debug] Removed active pocket {} (radiation: {}, accumulatedRads: {}, sealed: {}) at {} (Chunk:{}) for " + "world {}", radPocket.index, radPocket.radiation.get(), radPocket.accumulatedRads.sum(), radPocket.isSealed(), radPocket.getSubChunkPos(), chunkKey, world);

            }
        }

        public void clearActivePockets() {
            this.activePockets.clear();
            if (GeneralConfig.enableDebugMode) {
                MainRegistry.logger.info("[Debug] Cleared active pockets for world {}", world);
            }
        }
    }
}
