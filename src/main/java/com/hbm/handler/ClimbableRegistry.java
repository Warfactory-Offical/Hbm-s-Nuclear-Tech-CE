package com.hbm.handler;

import com.hbm.interfaces.IClimbable;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

public final class ClimbableRegistry {
    private static final Int2ObjectMap<Long2ObjectOpenHashMap<ArrayList<IClimbable>>> CLIENT_BY_DIM = new Int2ObjectOpenHashMap<>();
    private static final IdentityHashMap<IClimbable, Entry> CLIENT_REVERSE = new IdentityHashMap<>();
    private static final Int2ObjectMap<Long2ObjectOpenHashMap<ArrayList<IClimbable>>> SERVER_BY_DIM = new Int2ObjectOpenHashMap<>();
    private static final IdentityHashMap<IClimbable, Entry> SERVER_REVERSE = new IdentityHashMap<>();
    private ClimbableRegistry() {
    }

    private static Int2ObjectMap<Long2ObjectOpenHashMap<ArrayList<IClimbable>>> byDim(@NotNull World w) {
        return w.isRemote ? CLIENT_BY_DIM : SERVER_BY_DIM;
    }

    private static IdentityHashMap<IClimbable, Entry> reverse(@NotNull World w) {
        return w.isRemote ? CLIENT_REVERSE : SERVER_REVERSE;
    }

    /**
     * Register a climbable across all chunks overlapped by its climb AABB
     */
    public static void register(@NotNull IClimbable c) {
        final World w = c.world();
        final int dim = w.provider.getDimension();

        final IdentityHashMap<IClimbable, Entry> rev = reverse(w);
        if (rev.containsKey(c)) {
            unregister(c);
        }

        final AxisAlignedBB aabb = c.getClimbAABBForIndexing();
        final Entry entry = new Entry(dim);

        if (aabb == null) {
            final long key = chunkKey(c.pos());
            addToChunk(w, dim, key, c);
            entry.keys.add(key);
        } else {
            final int minCX = MathHelper.floor(aabb.minX) >> 4;
            final int maxCX = MathHelper.floor(aabb.maxX) >> 4;
            final int minCZ = MathHelper.floor(aabb.minZ) >> 4;
            final int maxCZ = MathHelper.floor(aabb.maxZ) >> 4;

            for (int cx = minCX; cx <= maxCX; cx++) {
                for (int cz = minCZ; cz <= maxCZ; cz++) {
                    final long key = ChunkPos.asLong(cx, cz);
                    addToChunk(w, dim, key, c);
                    entry.keys.add(key);
                }
            }
        }

        rev.put(c, entry);
    }

    /**
     * Remove a climbable from every chunk it was registered to (safe if not present).
     */
    public static void unregister(@NotNull IClimbable c) {
        final World w = c.world();
        final IdentityHashMap<IClimbable, Entry> rev = reverse(w);
        final Entry e = rev.remove(c);
        if (e == null) return;

        final Long2ObjectOpenHashMap<ArrayList<IClimbable>> byChunk = byDim(w).get(e.dim);
        if (byChunk == null) return;

        for (long key : e.keys) {
            final ArrayList<IClimbable> list = byChunk.get(key);
            if (list == null) continue;

            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) == c) {
                    list.remove(i);
                    break;
                }
            }
            if (list.isEmpty()) {
                byChunk.remove(key);
            }
        }

        if (byChunk.isEmpty()) {
            byDim(w).remove(e.dim);
        }
    }

    /**
     * If a climbable's AABB or anchor changed, call this to rebuild its index.
     */
    public static void refresh(@NotNull IClimbable c) {
        unregister(c);
        register(c);
    }

    /**
     * Hot-path query: is the entity intersecting any climbable in nearby chunks?
     */
    public static boolean isEntityOnAny(@NotNull World w, @NotNull EntityLivingBase e) {
        final int dim = w.provider.getDimension();
        final Long2ObjectOpenHashMap<ArrayList<IClimbable>> byChunk = byDim(w).get(dim);
        if (byChunk == null || byChunk.isEmpty()) return false;

        final AxisAlignedBB bb = e.getEntityBoundingBox();

        final int minCX = MathHelper.floor(bb.minX) >> 4;
        final int maxCX = MathHelper.floor(bb.maxX) >> 4;
        final int minCZ = MathHelper.floor(bb.minZ) >> 4;
        final int maxCZ = MathHelper.floor(bb.maxZ) >> 4;

        for (int cx = minCX; cx <= maxCX; cx++) {
            for (int cz = minCZ; cz <= maxCZ; cz++) {
                final long key = ChunkPos.asLong(cx, cz);
                final ArrayList<IClimbable> list = byChunk.get(key);
                if (list == null || list.isEmpty()) continue;

                for (IClimbable c : list) {
                    if (c == null) continue;
                    if (c.world() != w) continue; // side/world guard
                    if (c.isEntityInClimbAABB(e)) return true;
                }
            }
        }
        return false;
    }

    /**
     * Clear all climbables for a dimension on the given side
     */
    public static void clearDimension(@NotNull World w) {
        final int dim = w.provider.getDimension();
        byDim(w).remove(dim);
        reverse(w).values().removeIf(entry -> entry.dim == dim);
    }

    /**
     * Clear everything on both sides.
     */
    public static void clearAll() {
        CLIENT_BY_DIM.clear();
        CLIENT_REVERSE.clear();
        SERVER_BY_DIM.clear();
        SERVER_REVERSE.clear();
    }

    /**
     * Count climbables registered in a dimension on the given side.
     */
    public static int countClimbablesInDim(@NotNull World w, int dim) {
        final Long2ObjectOpenHashMap<ArrayList<IClimbable>> byChunk = byDim(w).get(dim);
        if (byChunk == null) return 0;
        int total = 0;
        for (ArrayList<IClimbable> list : byChunk.values()) {
            total += (list == null) ? 0 : list.size();
        }
        return total;
    }

    public static List<IClimbable> getClimbablesInAABB(@NotNull World w, AxisAlignedBB aabb) {
        final ArrayList<IClimbable> out = new ArrayList<>();
        if (aabb == null) return out;

        final int dim = w.provider.getDimension();
        final Long2ObjectOpenHashMap<ArrayList<IClimbable>> byChunk = byDim(w).get(dim);
        if (byChunk == null) return out;

        final int minCX = MathHelper.floor(aabb.minX) >> 4;
        final int maxCX = MathHelper.floor(aabb.maxX) >> 4;
        final int minCZ = MathHelper.floor(aabb.minZ) >> 4;
        final int maxCZ = MathHelper.floor(aabb.maxZ) >> 4;

        for (int cx = minCX; cx <= maxCX; cx++) {
            for (int cz = minCZ; cz <= maxCZ; cz++) {
                final long key = ChunkPos.asLong(cx, cz);
                final ArrayList<IClimbable> list = byChunk.get(key);
                if (list != null) out.addAll(list);
            }
        }
        final AxisAlignedBB q = aabb.grow(1.0e-6);
        out.removeIf(c -> {
            AxisAlignedBB idx = c.getClimbAABBForIndexing();
            if (idx != null) {
                return !idx.intersects(q);
            }
            BlockPos p = c.pos();
            AxisAlignedBB anchor = new AxisAlignedBB(p);
            return !anchor.intersects(q);
        });
        return out;
    }

    private static void addToChunk(@NotNull World w, int dim, long key, @NotNull IClimbable c) {
        Int2ObjectMap<Long2ObjectOpenHashMap<ArrayList<IClimbable>>> side = byDim(w);
        Long2ObjectOpenHashMap<ArrayList<IClimbable>> byChunk = side.get(dim);
        if (byChunk == null) {
            byChunk = new Long2ObjectOpenHashMap<>();
            side.put(dim, byChunk);
        }
        ArrayList<IClimbable> list = byChunk.get(key);
        if (list == null) {
            list = new ArrayList<>(1);
            byChunk.put(key, list);
        }
        for (IClimbable existing : list) {
            if (existing == c) return;
        }
        list.add(c);
    }

    private static long chunkKey(@NotNull BlockPos p) {
        return ChunkPos.asLong(p.getX() >> 4, p.getZ() >> 4);
    }

    private static final class Entry {
        final int dim;
        final LongOpenHashSet keys = new LongOpenHashSet();

        Entry(int dim) {
            this.dim = dim;
        }
    }
}
