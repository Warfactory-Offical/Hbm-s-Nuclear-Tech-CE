package com.hbm.mixin.vanilla;

import com.hbm.util.ChunkIndexedTickSet;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Makes collecting a chunk's scheduled block updates proportional to the ticks in that chunk instead of
 * to every scheduled tick in the world.
 * <p>
 * Vanilla scans the entire pending tick set once per chunk saved, so saving costs
 * O(chunks * pending ticks): with a large backlog that is over half of the server thread, split between
 * the 900-tick autosave and the up-to-100 unload saves {@code ChunkProviderServer#tick} performs every tick.
 * Swapping the set for {@link ChunkIndexedTickSet} keeps the same entries bucketed by chunk so the query
 * can go straight to the buckets the box covers.
 */
@Mixin(WorldServer.class)
public abstract class MixinWorldServer {

    @Shadow @Final @Mutable private TreeSet<NextTickListEntry> pendingTickListEntriesTreeSet;
    @Shadow @Final private Set<NextTickListEntry> pendingTickListEntriesHashSet;
    @Shadow @Final private List<NextTickListEntry> pendingTickListEntriesThisTick;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void hbm$installTickIndex(MinecraftServer server, ISaveHandler saveHandlerIn, WorldInfo info, int dimensionId, Profiler profilerIn, CallbackInfo ci) {
        ChunkIndexedTickSet indexed = new ChunkIndexedTickSet();
        // Empty at construction time, but copy through add() rather than addAll() so this stays correct if
        // something ever populates the set from a field initialiser (TreeSet#addAll bypasses add()).
        for (NextTickListEntry entry : this.pendingTickListEntriesTreeSet) indexed.add(entry);
        this.pendingTickListEntriesTreeSet = indexed;
    }

    @Inject(
            method = "getPendingBlockUpdates(Lnet/minecraft/world/gen/structure/StructureBoundingBox;Z)Ljava/util/List;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void hbm$getPendingBlockUpdatesIndexed(StructureBoundingBox structureBB, boolean remove, CallbackInfoReturnable<List<NextTickListEntry>> cir) {
        if (!(this.pendingTickListEntriesTreeSet instanceof ChunkIndexedTickSet)) return; // someone else owns the set; let vanilla run

        List<NextTickListEntry> list = ((ChunkIndexedTickSet) this.pendingTickListEntriesTreeSet).collect(structureBB, remove);

        if (remove && list != null) {
            for (int i = 0; i < list.size(); i++) this.pendingTickListEntriesHashSet.remove(list.get(i));
        }

        // Second pass over the entries already pulled for this tick, exactly as vanilla does. This list is
        // drained at the end of tickUpdates and every chunk save happens outside it, so it is normally
        // empty here; it is an ArrayList capped at 65536 either way, so scanning it is not worth indexing.
        if (!this.pendingTickListEntriesThisTick.isEmpty()) {
            Iterator<NextTickListEntry> iterator = this.pendingTickListEntriesThisTick.iterator();

            while (iterator.hasNext()) {
                NextTickListEntry entry = iterator.next();
                BlockPos pos = entry.position;

                if (pos.getX() >= structureBB.minX && pos.getX() < structureBB.maxX && pos.getZ() >= structureBB.minZ && pos.getZ() < structureBB.maxZ) {
                    if (remove) iterator.remove();
                    if (list == null) list = new ArrayList<>();
                    list.add(entry);
                }
            }
        }

        cir.setReturnValue(list);
    }
}
