package com.hbm.world.dungeon;

import com.hbm.world.phased.AbstractPhasedStructure;
import com.hbm.world.phased.PhasedStructureGenerator;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class AncientTombStructure extends AbstractPhasedStructure {
    public static final AncientTombStructure INSTANCE = new AncientTombStructure();
    private AncientTombStructure() {}

    @Override
    protected boolean isCacheable() {
        return false;
    }

    @Override
    public List<@NotNull BlockPos> getValidationPoints(@NotNull BlockPos origin) {
        final int r = 32;
        final int inner = 16;
        return Arrays.asList(origin,
                origin.add( r, 0,  0),
                origin.add(-r, 0,  0),
                origin.add( 0, 0,  r),
                origin.add( 0, 0, -r),
                origin.add( r, 0,  r),
                origin.add( r, 0, -r),
                origin.add(-r, 0,  r),
                origin.add(-r, 0, -r),
                origin.add( inner, 0,  inner),
                origin.add( inner, 0, -inner),
                origin.add(-inner, 0,  inner),
                origin.add(-inner, 0, -inner)
        );
    }
    @Override
    protected void buildStructure(@NotNull LegacyBuilder builder, @NotNull Random rand) {
        new AncientTomb().buildChamber(builder, rand, 0, 0, 0);
    }

    @Override
    @NotNull
    public Optional<PhasedStructureGenerator.ReadyToGenerateStructure> validate(@NotNull World world, @NotNull PhasedStructureGenerator.PendingValidationStructure pending) {
        BlockPos origin = pending.origin;
        int surfaceY = world.getHeight(origin.getX(), origin.getZ());
        if (surfaceY > 35) {
            BlockPos finalOrigin = new BlockPos(origin.getX(), 20, origin.getZ());
            return Optional.of(new PhasedStructureGenerator.ReadyToGenerateStructure(pending, finalOrigin));
        }
        return Optional.empty();
    }

    @Override
    public void postGenerate(@NotNull World world, @NotNull Random rand, @NotNull BlockPos finalOrigin) {
        new AncientTomb().buildSurfaceFeatures(world, rand, finalOrigin.getX(), finalOrigin.getZ());
    }

    @Override
    public LongArrayList getWatchedChunkOffsets(@NotNull BlockPos origin) {
        int radiusChunks = 2;
        LongArrayList offsets = new LongArrayList();
        for (int dx = -radiusChunks; dx <= radiusChunks; dx++) {
            for (int dz = -radiusChunks; dz <= radiusChunks; dz++) {
                offsets.add(ChunkPos.asLong(dx, dz));
            }
        }
        return offsets;
    }
}
