package com.hbm.util;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * Drop-in replacement for {@code WorldServer#pendingTickListEntriesTreeSet} that additionally keeps
 * every entry bucketed by chunk.
 * <p>
 * Vanilla's {@code WorldServer#getPendingBlockUpdates(StructureBoundingBox, boolean)} linearly scans the
 * whole set and bounds-checks each entry, and {@code AnvilChunkLoader#writeChunkToNBT} calls it once for
 * every chunk that gets saved. Saving is therefore O(chunks * pending ticks) - on a world with a large
 * scheduled-tick backlog that is by far the most expensive thing the server thread does, both during the
 * 900-tick autosave and during the up-to-100 unload saves {@code ChunkProviderServer#tick} does every tick.
 * <p>
 * Keeping the index inside the collection itself rather than redirecting individual call sites means any
 * add/remove path - vanilla's or another mod's - stays in sync automatically, with the exception of removals
 * made through a {@link TreeSet} sub-view ({@code headSet}/{@code tailSet}/{@code subSet}/{@code descendingSet}),
 * which nothing in vanilla uses. The buckets hold entries by identity rather than by equality, because
 * {@code NextTickListEntry} equality is only position plus block while the set itself orders by a unique id:
 * vanilla's two add paths both guard against duplicate positions, but the index stays a faithful mirror even
 * if something bypasses that. {@link #collect} verifies the index in O(1) before every query and rebuilds it
 * if it ever drifts, falling back to the plain scan for good if the rebuild does not resolve it, so a desync
 * degrades to vanilla behaviour instead of corrupting the tick list.
 */
public class ChunkIndexedTickSet extends TreeSet<NextTickListEntry> {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger("HbmTickIndex");

    private final Long2ObjectOpenHashMap<ReferenceOpenHashSet<NextTickListEntry>> byChunk = new Long2ObjectOpenHashMap<>();
    private int indexedSize;
    private boolean degraded;

    private static long chunkKey(NextTickListEntry entry) {
        BlockPos pos = entry.position;
        return ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
    }

    private void index(NextTickListEntry entry) {
        if (degraded) return;
        long key = chunkKey(entry);
        ReferenceOpenHashSet<NextTickListEntry> bucket = byChunk.get(key);
        if (bucket == null) {
            bucket = new ReferenceOpenHashSet<>(4);
            byChunk.put(key, bucket);
        }
        if (bucket.add(entry)) indexedSize++;
    }

    private void unindex(NextTickListEntry entry) {
        if (degraded) return;
        long key = chunkKey(entry);
        ReferenceOpenHashSet<NextTickListEntry> bucket = byChunk.get(key);
        if (bucket == null) return;
        if (bucket.remove(entry)) indexedSize--;
        if (bucket.isEmpty()) byChunk.remove(key);
    }

    /**
     * Cheap O(1) consistency check; only pays the O(n) rebuild if the index actually drifted, and gives up on
     * the index entirely rather than rebuilding on every query if a rebuild does not resolve the drift.
     */
    private void validate() {
        if (degraded || indexedSize == size()) return;

        LOGGER.warn("Pending block update chunk index drifted ({} indexed vs {} scheduled), rebuilding", indexedSize, size());
        byChunk.clear();
        indexedSize = 0;
        for (Iterator<NextTickListEntry> it = superIterator(); it.hasNext(); ) index(it.next());

        if (indexedSize != size()) {
            degraded = true;
            byChunk.clear();
            indexedSize = 0;
            LOGGER.error("Pending block update chunk index could not be rebuilt; falling back to a linear scan for this world");
        }
    }

    private Iterator<NextTickListEntry> superIterator() {
        return super.iterator();
    }

    @Override
    public boolean add(NextTickListEntry entry) {
        if (!super.add(entry)) return false;
        index(entry);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        if (!super.remove(o)) return false;
        // TreeSet removes by compareTo, which includes the unique tick entry id, so o is the stored entry.
        if (o instanceof NextTickListEntry) unindex((NextTickListEntry) o);
        return true;
    }

    @Override
    public void clear() {
        super.clear();
        byChunk.clear();
        indexedSize = 0;
    }

    @Override
    public NextTickListEntry pollFirst() {
        NextTickListEntry entry = super.pollFirst();
        if (entry != null) unindex(entry);
        return entry;
    }

    @Override
    public NextTickListEntry pollLast() {
        NextTickListEntry entry = super.pollLast();
        if (entry != null) unindex(entry);
        return entry;
    }

    @Override
    public Iterator<NextTickListEntry> iterator() {
        return wrap(super.iterator());
    }

    @Override
    public Iterator<NextTickListEntry> descendingIterator() {
        return wrap(super.descendingIterator());
    }

    private Iterator<NextTickListEntry> wrap(final Iterator<NextTickListEntry> delegate) {
        return new Iterator<NextTickListEntry>() {

            private NextTickListEntry last;

            @Override
            public boolean hasNext() {
                return delegate.hasNext();
            }

            @Override
            public NextTickListEntry next() {
                last = delegate.next();
                return last;
            }

            @Override
            public void remove() {
                delegate.remove();
                if (last != null) {
                    unindex(last);
                    last = null;
                }
            }
        };
    }

    /**
     * Gathers every scheduled tick inside the given box, optionally removing them, in exactly the order
     * and with exactly the semantics of vanilla's linear scan of this set.
     *
     * @return the matching entries in scheduling order, or null when there are none (vanilla returns null)
     */
    public List<NextTickListEntry> collect(StructureBoundingBox box, boolean remove) {
        if (isEmpty()) return null;
        validate();
        if (degraded) return collectByScan(box, remove);

        int minChunkX = box.minX >> 4;
        int maxChunkX = (box.maxX - 1) >> 4;
        int minChunkZ = box.minZ >> 4;
        int maxChunkZ = (box.maxZ - 1) >> 4;
        if (maxChunkX < minChunkX || maxChunkZ < minChunkZ) return null;

        long chunkArea = (long) (maxChunkX - minChunkX + 1) * (long) (maxChunkZ - minChunkZ + 1);
        // Walking the box is only a win while it covers fewer chunks than the index holds; for a box that
        // large fall through to the vanilla scan so this is never worse than what it replaces.
        if (chunkArea > byChunk.size()) return collectByScan(box, remove);

        List<NextTickListEntry> list = null;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                long key = ChunkPos.asLong(chunkX, chunkZ);
                ReferenceOpenHashSet<NextTickListEntry> bucket = byChunk.get(key);
                if (bucket == null) continue;

                Iterator<NextTickListEntry> it = bucket.iterator();
                while (it.hasNext()) {
                    NextTickListEntry entry = it.next();
                    BlockPos pos = entry.position;
                    if (pos.getX() < box.minX || pos.getX() >= box.maxX) continue;
                    if (pos.getZ() < box.minZ || pos.getZ() >= box.maxZ) continue;

                    if (list == null) list = new ArrayList<>();
                    list.add(entry);

                    if (remove) {
                        it.remove();
                        indexedSize--;
                        super.remove(entry);
                    }
                }

                if (bucket.isEmpty()) byChunk.remove(key);
            }
        }

        // Bucket order is arbitrary; vanilla hands these back in scheduling order and chunk NBT preserves
        // that order across a save/load, so restore it.
        if (list != null) Collections.sort(list);
        return list;
    }

    private List<NextTickListEntry> collectByScan(StructureBoundingBox box, boolean remove) {
        List<NextTickListEntry> list = null;
        Iterator<NextTickListEntry> it = iterator();

        while (it.hasNext()) {
            NextTickListEntry entry = it.next();
            BlockPos pos = entry.position;
            if (pos.getX() < box.minX || pos.getX() >= box.maxX) continue;
            if (pos.getZ() < box.minZ || pos.getZ() >= box.maxZ) continue;

            if (list == null) list = new ArrayList<>();
            list.add(entry);
            if (remove) it.remove();
        }

        return list;
    }
}
