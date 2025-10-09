package com.hbm.hazard;

import com.github.bsideup.jabel.Desugar;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hbm.capability.HbmLivingProps;
import com.hbm.config.GeneralConfig;
import com.hbm.config.RadiationConfig;
import com.hbm.hazard.modifier.HazardModifier;
import com.hbm.hazard.transformer.HazardTransformerBase;
import com.hbm.hazard.type.HazardTypeBase;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.main.MainRegistry;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ItemStackUtil;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.hbm.util.ContaminationUtil.NTM_NEUTRON_NBT_KEY;

/**
 * This logic was heavily refactored to be threaded and event-driven. Do not aim for upstream parity.
 *
 * @author drillgon200, Alcater, mlbv
 */
public class HazardSystem {

    /**
     * Map for OreDict entries, always evaluated first. Avoid registering HazardData with 'doesOverride', as internal order is based on the item's
     * ore dict keys.
     */
    public static final HashMap<String, HazardData> oreMap = new HashMap<>();
    /**
     * Map for items, either with wildcard meta or stuff that's expected to have a variety of damage values, like tools.
     */
    public static final HashMap<Item, HazardData> itemMap = new HashMap<>();
    /**
     * Very specific stacks with item and meta matching. ComparableStack does not support NBT matching, to scale hazards with NBT please use
     * HazardModifiers.
     */
    public static final HashMap<ComparableStack, HazardData> stackMap = new HashMap<>();
    /**
     * For items that should, for whichever reason, be completely exempt from the hazard system.
     */
    public static final HashSet<ComparableStack> stackBlacklist = new HashSet<>();
    public static final HashSet<String> dictBlacklist = new HashSet<>();


    /**
     * For items from outside of that mod that require registration right at end of fml loading
     */
    public static final List<Tuple<ResourceLocation,HazardData>> locationRateRegisterList = new CopyOnWriteArrayList<>();
    /**
     * List of hazard transformers, called in order before and after unrolling all the HazardEntries.
     */
    public static final List<HazardTransformerBase> trafos = new ArrayList<>();
    private static final int VOLATILITY_THRESHOLD = 16;
    private static final int VOLATILITY_WINDOW_SECONDS = 30;
    private static final int FINAL_HAZARD_CACHE_SIZE = 2048;
    private static final ConcurrentHashMap<ComparableStack, List<HazardData>> hazardDataChronologyCache = new ConcurrentHashMap<>();
    private static final Cache<NbtSensitiveCacheKey, List<HazardEntry>> finalHazardEntryCache =
            CacheBuilder.newBuilder().maximumSize(FINAL_HAZARD_CACHE_SIZE).build();
    private static final Cache<ComparableStack, AtomicInteger> volatilityTracker =
            CacheBuilder.newBuilder().expireAfterWrite(VOLATILITY_WINDOW_SECONDS, TimeUnit.SECONDS).build();
    private static final Set<ComparableStack> volatileItemsBlacklist = ConcurrentHashMap.newKeySet();
    private static final ConcurrentHashMap<UUID, PlayerHazardData> playerHazardDataMap = new ConcurrentHashMap<>();
    private static final ExecutorService hazardScanExecutor = Executors.newFixedThreadPool(Math.max(1,
            Runtime.getRuntime().availableProcessors() - 1),
            new ThreadFactoryBuilder().setNameFormat("HBM-Hazard-Scanner-%d").setDaemon(true).build());
    private static final Queue<InventoryDelta> inventoryDeltas = new ConcurrentLinkedQueue<>();
    private static final Set<UUID> playersToUpdate = ConcurrentHashMap.newKeySet();
    private static final float minRadRate = 0.000005F;
    private static CompletableFuture<Void> scanFuture = CompletableFuture.completedFuture(null);
    private static long tickCounter = 0;

    /**
     * Schedules a full rescan for a player.
     *
     * @param player The player whose inventory has changed.
     */
    public static void schedulePlayerUpdate(EntityPlayer player) {
        playersToUpdate.add(player.getUniqueID());
    }

    // note: oldStack isn't implemented
    public static void onInventoryDelta(EntityPlayer player, int serverSlotIndex, ItemStack oldStack, ItemStack newStack) {
        inventoryDeltas.add(new InventoryDelta(player.getUniqueID(), serverSlotIndex, oldStack.copy(), newStack.copy()));
    }

    /**
     * Main entry point, called from ServerTickEvent.
     */
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) return;

        tickCounter++;
        if (tickCounter % RadiationConfig.hazardRate == 0) {
            for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
                if (player.isDead) continue;
                PlayerHazardData phd = playerHazardDataMap.computeIfAbsent(player.getUniqueID(), uuid -> new PlayerHazardData(player));
                if (phd.player != player) {
                    if (GeneralConfig.enableExtendedLogging)
                        MainRegistry.logger.debug("Player {} entity instance changed, re-initializing.", player.getName());
                    phd.updatePlayerReference(player);
                }
                phd.applyActiveHazards();
            }
        }

        if (scanFuture.isDone() && (!playersToUpdate.isEmpty() || !inventoryDeltas.isEmpty())) {
            final List<EntityPlayer> playersForFullScan = new ArrayList<>();
            playersToUpdate.removeIf(uuid -> {
                EntityPlayer player = server.getPlayerList().getPlayerByUUID(uuid);
                playersForFullScan.add(player);
                return true;
            });

            final List<InventoryDelta> deltasForProcessing = new ArrayList<>();
            InventoryDelta delta;
            while ((delta = inventoryDeltas.poll()) != null) {
                deltasForProcessing.add(delta);
            }

            if (!playersForFullScan.isEmpty() || !deltasForProcessing.isEmpty()) {
                scanFuture =
                        CompletableFuture.supplyAsync(() -> processHazardsAsync(playersForFullScan, deltasForProcessing), hazardScanExecutor).thenAcceptAsync(results -> {
                    results.fullScanResults.forEach((uuid, result) -> {
                        PlayerHazardData phd = playerHazardDataMap.get(uuid);
                        if (phd != null) phd.setScanResult(result);
                    });
                    results.deltaResults.forEach((uuid, result) -> {
                        PlayerHazardData phd = playerHazardDataMap.get(uuid);
                        if (phd != null) phd.applyDeltaResult(result);
                    });
                }, server::addScheduledTask);
            }
        }
    }

    private static HazardUpdateResult processHazardsAsync(List<EntityPlayer> playersForFullScan, List<InventoryDelta> deltas) {
        Map<UUID, PlayerHazardData.HazardScanResult> fullScanResults =
                playersForFullScan.parallelStream().collect(Collectors.toConcurrentMap(EntityPlayer::getUniqueID,
                        PlayerHazardData::calculateHazardScanForPlayer));
        Map<UUID, List<InventoryDelta>> deltasByPlayer =
                deltas.stream().filter(d -> !fullScanResults.containsKey(d.playerUUID())).collect(Collectors.groupingBy(InventoryDelta::playerUUID));

        Map<UUID, PlayerDeltaResult> deltaResults = deltasByPlayer.entrySet().parallelStream().collect(Collectors.toConcurrentMap(Map.Entry::getKey
                , entry -> {
            float totalNeutronDelta = 0;
            Map<Integer, Optional<Consumer<EntityPlayer>>> finalApplicators = new HashMap<>();
            for (InventoryDelta delta : entry.getValue()) {
                DeltaUpdate update = calculateDeltaUpdate(delta);
                totalNeutronDelta += update.neutronRadsDelta();
                finalApplicators.put(delta.serverSlotIndex(), update.applicator());
            }
            return new PlayerDeltaResult(finalApplicators, totalNeutronDelta);
        }));

        return new HazardUpdateResult(fullScanResults, deltaResults);
    }

    /**
     * Calculates the change for a single slot. Runs on a background thread.
     */
    private static DeltaUpdate calculateDeltaUpdate(InventoryDelta delta) {
        ItemStack oldStack = delta.oldStack();
        ItemStack newStack = delta.newStack();
        boolean isOldStackHazardous = isStackHazardous(oldStack);
        boolean isNewStackHazardous = isStackHazardous(newStack);

        float neutronDelta = 0;
        if (RadiationConfig.neutronActivation) {
            if (!isOldStackHazardous) {
                neutronDelta -= ContaminationUtil.getNeutronRads(oldStack);
            }
            if (!isNewStackHazardous) {
                neutronDelta += ContaminationUtil.getNeutronRads(newStack);
            }
        }

        if (!isNewStackHazardous) {
            return new DeltaUpdate(Optional.empty(), neutronDelta);
        }

        final int slotIndex = delta.serverSlotIndex();
        Consumer<EntityPlayer> applicator = p -> {
            if (p.inventoryContainer == null || slotIndex >= p.inventoryContainer.inventorySlots.size()) return;
            ItemStack liveStack = p.inventoryContainer.getSlot(slotIndex).getStack();
            applyHazards(liveStack, p);
        };
        return new DeltaUpdate(Optional.of(applicator), neutronDelta);
    }

    public static void onPlayerLogout(EntityPlayer player) {
        UUID uuid = player.getUniqueID();
        playersToUpdate.remove(uuid);
        playerHazardDataMap.remove(uuid);
        inventoryDeltas.removeIf(delta -> delta.playerUUID().equals(uuid));
    }

    /**
     * Call when doing hot reload. Currently unused.
     */
    public static void clearCaches() {
        MainRegistry.logger.info("Clearing HBM hazard calculation caches.");
        hazardDataChronologyCache.clear();
        finalHazardEntryCache.invalidateAll();
        volatilityTracker.invalidateAll();
        volatileItemsBlacklist.clear();
    }

    public static boolean isStackHazardous(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return !getHazardsFromStack(stack).isEmpty();
    }

    public static void register(final Object o, final HazardData data) {
        if (o instanceof String) oreMap.put((String) o, data);
        if (o instanceof Item) itemMap.put((Item) o, data);
        if (o instanceof ResourceLocation) retriveAndRegister((ResourceLocation) o, data);
        if (o instanceof Block) itemMap.put(Item.getItemFromBlock((Block) o), data);
        if (o instanceof ItemStack) stackMap.put(ItemStackUtil.comparableStackFrom((ItemStack) o), data);
        if (o instanceof ComparableStack) stackMap.put((ComparableStack) o, data);
    }

    public static boolean unregister(final Object o) {
        if (o instanceof Collection<?>) {
            boolean removed = false;
            for (Object element : (Collection<?>) o) {
                removed |= unregister(element);
            }
            return removed;
        }
        if (o == null) return false;

        boolean removed = false;
        if (o instanceof String) {
            removed |= oreMap.remove(o) != null;
        } else if (o instanceof Item) {
            removed |= itemMap.remove(o) != null;
        } else if (o instanceof ResourceLocation) {
            removed |= removeResourceLocation((ResourceLocation) o);
        } else if (o instanceof Block) {
            Item item = Item.getItemFromBlock((Block) o);
            if (item != Items.AIR) {
                removed |= itemMap.remove(item) != null;
            }
        } else if (o instanceof ItemStack) {
            removed |= stackMap.remove(ItemStackUtil.comparableStackFrom((ItemStack) o)) != null;
        } else if (o instanceof ComparableStack) {
            removed |= stackMap.remove(o) != null;
        } else if (o.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(o);
            for (int i = 0; i < length; i++) {
                Object element = java.lang.reflect.Array.get(o, i);
                removed |= unregister(element);
            }
        }
        return removed;
    }


    /**
     * Attempts to retrive and append an item onto the map from resource location, helpful for groovy users
     * @param loc
     */
    private static void retriveAndRegister(ResourceLocation loc, HazardData data){
        IForgeRegistry<Item> registry = ForgeRegistries.ITEMS;
        if(registry.containsKey(loc))
            itemMap.put(registry.getValue(loc),data);
        else
           locationRateRegisterList.add(new Tuple<>(loc,data));

    }

    private static boolean removeResourceLocation(ResourceLocation loc) {
        boolean removed = false;
        IForgeRegistry<Item> registry = ForgeRegistries.ITEMS;
        if (registry.containsKey(loc)) {
            Item item = registry.getValue(loc);
            if (item != null) {
                removed |= itemMap.remove(item) != null;
            }
        }
        Iterator<Tuple<ResourceLocation, HazardData>> iterator = locationRateRegisterList.iterator();
        while (iterator.hasNext()) {
            Tuple<ResourceLocation, HazardData> tuple = iterator.next();
            if (loc.equals(tuple.getFirst())) {
                iterator.remove();
                removed = true;
            }
        }
        return removed;
    }

    /**
     * Prevents the stack from returning any HazardData
     */
    public static void blacklist(final Object o) {
        if (o instanceof ItemStack) stackBlacklist.add(ItemStackUtil.comparableStackFrom((ItemStack) o).makeSingular());
        else if (o instanceof String) dictBlacklist.add((String) o);
    }

    public static boolean unblacklist(final Object o) {
        if (o instanceof Collection<?>) {
            boolean removed = false;
            for (Object element : (Collection<?>) o) {
                removed |= unblacklist(element);
            }
            return removed;
        }
        if (o == null) return false;

        boolean removed = false;
        if (o instanceof ItemStack) {
            removed |= stackBlacklist.remove(ItemStackUtil.comparableStackFrom((ItemStack) o).makeSingular());
        } else if (o instanceof String) {
            removed |= dictBlacklist.remove(o);
        } else if (o instanceof ComparableStack) {
            removed |= stackBlacklist.remove(((ComparableStack) o).makeSingular());
        } else if (o.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(o);
            for (int i = 0; i < length; i++) {
                Object element = java.lang.reflect.Array.get(o, i);
                removed |= unblacklist(element);
            }
        }
        return removed;
    }

    public static boolean isItemBlacklisted(final ItemStack stack) {
        if (stackBlacklist.contains(ItemStackUtil.comparableStackFrom(stack).makeSingular())) return true;
        final int[] ids = OreDictionary.getOreIDs(stack);
        for (final int id : ids) {
            if (dictBlacklist.contains(OreDictionary.getOreName(id))) return true;
        }
        return false;
    }

    /**
     * Will return a full list of applicable HazardEntries for this stack.
     * <br><br>ORDER:
     * <ol>
     * <li>ore dict (if multiple keys, in order of the ore dict keys for this stack)
     * <li>item
     * <li>item stack
     * </ol>
     * <p>
     * "Applicable" means that entries that are overridden or excluded via mutex are not in this list.
     * Entries that are marked as "overriding" will delete all fetched entries that came before it.
     * Entries that use mutex will prevent subsequent entries from being considered, shall they collide. The mutex system already assumes that
     * two keys are the same in priority, so the flipped order doesn't matter.
     */
    private static List<HazardEntry> getHazardsFromStack(final ItemStack stack) {
        if (stack.isEmpty() || isItemBlacklisted(stack)) {
            return Collections.emptyList();
        }

        final ComparableStack compStack = ItemStackUtil.comparableStackFrom(stack).makeSingular();

        if (volatileItemsBlacklist.contains(compStack)) {
            return computeHazards(stack, compStack);
        }

        int nbtHash = 0;
        if (stack.hasTagCompound()) {
            NBTTagCompound sanitizedNbt = stack.getTagCompound().copy();
            sanitizedNbt.removeTag(NTM_NEUTRON_NBT_KEY);
            if (!sanitizedNbt.isEmpty()) {
                nbtHash = sanitizedNbt.hashCode();
            }
        }

        final NbtSensitiveCacheKey nbtKey = new NbtSensitiveCacheKey(compStack, nbtHash);

        try {
            return finalHazardEntryCache.get(nbtKey, () -> {
                AtomicInteger missCount = volatilityTracker.get(compStack, AtomicInteger::new);
                if (missCount.incrementAndGet() > VOLATILITY_THRESHOLD) {
                    volatileItemsBlacklist.add(compStack);
                    volatilityTracker.invalidate(compStack);
                }
                return computeHazards(stack, compStack);
            });
        } catch (ExecutionException e) {
            throw new RuntimeException("Error calculating hazard entries for stack: " + stack, e.getCause());
        }
    }

    private static List<HazardEntry> computeHazards(ItemStack stack, ComparableStack compStack) {
        // Get NBT-agnostic base data
        List<HazardData> chronological = hazardDataChronologyCache.computeIfAbsent(compStack, cs -> {
            final List<HazardData> data = new ArrayList<>();
            final int[] ids = OreDictionary.getOreIDs(new ItemStack(cs.item, 1, cs.meta));
            for (final int id : ids) {
                final String name = OreDictionary.getOreName(id);
                final HazardData hazardData = oreMap.get(name);
                if (hazardData != null) data.add(hazardData);
            }
            final HazardData itemHazardData = itemMap.get(cs.item);
            if (itemHazardData != null) data.add(itemHazardData);
            final HazardData stackHazardData = stackMap.get(cs);
            if (stackHazardData != null) data.add(stackHazardData);
            return Collections.unmodifiableList(data);
        });

        if (chronological.isEmpty() && trafos.isEmpty()) {
            return Collections.emptyList();
        }

        // Apply NBT-sensitive transformers and build the final list
        final List<HazardEntry> entries = new ArrayList<>();
        for (final HazardTransformerBase trafo : trafos) {
            trafo.transformPre(stack, entries);
        }

        int mutex = 0;
        for (final HazardData data : chronological) {
            if (data.doesOverride) entries.clear();
            if ((data.getMutex() & mutex) == 0) {
                entries.addAll(data.entries);
                mutex |= data.getMutex();
            }
        }

        for (final HazardTransformerBase trafo : trafos) {
            trafo.transformPost(stack, entries);
        }

        return Collections.unmodifiableList(entries);
    }

    public static float getHazardLevelFromStack(ItemStack stack, HazardTypeBase hazard) {
        return getHazardsFromStack(stack).stream().filter(entry -> entry.type == hazard).findFirst().map(entry -> HazardModifier.evalAllModifiers(stack, null, entry.baseLevel, entry.mods)).orElse(0F);
    }

    public static float getRawRadsFromBlock(Block b) {
        return getHazardLevelFromStack(new ItemStack(Item.getItemFromBlock(b)), HazardRegistry.RADIATION);
    }

    public static float getRawRadsFromStack(ItemStack stack) {
        return getHazardLevelFromStack(stack, HazardRegistry.RADIATION);
    }

    public static float getTotalRadsFromStack(ItemStack stack) {
        return getHazardLevelFromStack(stack, HazardRegistry.RADIATION) + ContaminationUtil.getNeutronRads(stack);
    }

    public static void applyHazards(Block b, EntityLivingBase entity) {
        applyHazards(new ItemStack(Item.getItemFromBlock(b)), entity);
    }

    /**
     * Will grab and iterate through all assigned hazards of the given stack and apply their effects to the holder.
     */
    public static void applyHazards(ItemStack stack, EntityLivingBase entity) {
        if (stack.isEmpty()) return;
        List<HazardEntry> hazards = getHazardsFromStack(stack);
        for (HazardEntry hazard : hazards) {
            hazard.applyHazard(stack, entity);
        }
    }

    public static void updateLivingInventory(EntityLivingBase entity) {

        for (EntityEquipmentSlot i : EntityEquipmentSlot.values()) {
            ItemStack stack = entity.getItemStackFromSlot(i);

            if (!stack.isEmpty()) {
                applyHazards(stack, entity);
            }
        }
    }

    public static void updateDroppedItem(EntityItem entity) {
        if (entity.isDead) return;
        ItemStack stack = entity.getItem();
        if (stack.isEmpty() || stack.getCount() <= 0) return;
        for (HazardEntry entry : getHazardsFromStack(stack)) {
            entry.type.updateEntity(entity, HazardModifier.evalAllModifiers(stack, null, entry.baseLevel, entry.mods));
        }
    }

    public static void addHazardInfo(ItemStack stack, EntityPlayer player, List<String> list, ITooltipFlag flagIn) {
        for (HazardEntry hazard : getHazardsFromStack(stack)) {
            hazard.type.addHazardInformation(player, list, hazard.baseLevel, stack, hazard.mods);
        }
    }

    private static class PlayerHazardData {
        private final Map<Integer, Consumer<EntityPlayer>> activeApplicators = new ConcurrentHashMap<>();
        private EntityPlayer player;
        private float totalNeutronRads = 0f;

        PlayerHazardData(EntityPlayer player) {
            this.player = player;
            schedulePlayerUpdate(player);
        }

        static HazardScanResult calculateHazardScanForPlayer(EntityPlayer player) {
            Map<Integer, Consumer<EntityPlayer>> applicators = new HashMap<>();
            float totalNeutronRads = 0f;

            if (player.inventoryContainer == null) {
                return new HazardScanResult(Collections.emptyMap(), 0f);
            }

            for (int i = 0; i < player.inventoryContainer.inventorySlots.size(); i++) {
                Slot slot = player.inventoryContainer.getSlot(i);
                if (slot.inventory != player.inventory) continue;

                ItemStack stack = slot.getStack();
                if (stack.isEmpty()) continue;

                List<HazardEntry> hazards = getHazardsFromStack(stack);
                if (!hazards.isEmpty()) {
                    final int slotIndex = i;
                    applicators.put(slotIndex, p -> {
                        if (p.inventoryContainer == null || slotIndex >= p.inventoryContainer.inventorySlots.size()) return;
                        ItemStack liveStack = p.inventoryContainer.getSlot(slotIndex).getStack();
                        applyHazards(liveStack, p);
                    });
                }
                if (RadiationConfig.neutronActivation && hazards.isEmpty()) {
                    totalNeutronRads += ContaminationUtil.getNeutronRads(stack);
                }
            }
            return new HazardScanResult(Collections.unmodifiableMap(applicators), totalNeutronRads);
        }

        void updatePlayerReference(EntityPlayer player) {
            this.player = player;
            schedulePlayerUpdate(player);
        }

        void setScanResult(HazardScanResult result) {
            this.activeApplicators.clear();
            this.activeApplicators.putAll(result.applicatorMap);
            this.totalNeutronRads = Math.max(0f, result.totalNeutronRads);
        }

        void applyDeltaResult(PlayerDeltaResult result) {
            for (Map.Entry<Integer, Optional<Consumer<EntityPlayer>>> entry : result.finalApplicators.entrySet()) {
                Optional<Consumer<EntityPlayer>> applicatorOptional = entry.getValue();
                Integer slotIndex = entry.getKey();
                if (applicatorOptional.isPresent()) {
                    activeApplicators.put(slotIndex, applicatorOptional.get());
                } else {
                    activeApplicators.remove(slotIndex);
                }
            }
            this.totalNeutronRads += result.totalNeutronDelta;
            if (this.totalNeutronRads < 0) this.totalNeutronRads = 0;
        }

        void applyActiveHazards() {
            if (player.isDead) return;

            if (!activeApplicators.isEmpty()) {
                activeApplicators.values().forEach(applier -> applier.accept(this.player));
            }
            HbmLivingProps.setNeutron(player, 0);

            // 1:1 moved from RadiationSystemNT, but now scales with RadiationConfig.hazardRate
            if (RadiationConfig.neutronActivation) {
                if (totalNeutronRads > 0) {
                    ContaminationUtil.contaminate(player, ContaminationUtil.HazardType.NEUTRON, ContaminationUtil.ContaminationType.CREATIVE,
                            totalNeutronRads * 0.05F * RadiationConfig.hazardRate);
                }
                if (!player.isCreative() && !player.isSpectator()) {
                    double activationRate =
                            ContaminationUtil.getNoNeutronPlayerRads(player) * 0.00004D - (0.00004D * RadiationConfig.neutronActivationThreshold);
                    if (activationRate > minRadRate) {
                        float totalActivationAmount = (float) activationRate * RadiationConfig.hazardRate;
                        if (ContaminationUtil.neutronActivateInventory(player, totalActivationAmount, 1.0F)) {
                            schedulePlayerUpdate(this.player);
                        }
                    }
                }
            }

            if (this.player.inventoryContainer != null) {
                this.player.inventoryContainer.detectAndSendChanges();
            }
        }

        @Desugar
        record HazardScanResult(Map<Integer, Consumer<EntityPlayer>> applicatorMap, float totalNeutronRads) {
        }
    }

    @Desugar
    private record NbtSensitiveCacheKey(ComparableStack stack, int nbtHash) {
    }

    @Desugar
    private record InventoryDelta(UUID playerUUID, int serverSlotIndex, ItemStack oldStack, ItemStack newStack) {
    }

    @Desugar
    private record DeltaUpdate(Optional<Consumer<EntityPlayer>> applicator, float neutronRadsDelta) {
    }

    @Desugar
    private record PlayerDeltaResult(Map<Integer, Optional<Consumer<EntityPlayer>>> finalApplicators, float totalNeutronDelta) {
    }

    @Desugar
    private record HazardUpdateResult(Map<UUID, PlayerHazardData.HazardScanResult> fullScanResults, Map<UUID, PlayerDeltaResult> deltaResults) {
    }
}
