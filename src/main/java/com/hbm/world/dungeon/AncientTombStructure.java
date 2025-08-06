package com.hbm.world.dungeon;

import com.hbm.world.phased.AbstractPhasedStructure;
import com.hbm.world.phased.PhasedStructureGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
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
        return Collections.singletonList(origin);
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
}
