package com.hbm.world.generator;

import com.hbm.world.phased.AbstractPhasedStructure;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MeteorDungeonStructure extends AbstractPhasedStructure {
    public static final MeteorDungeonStructure INSTANCE = new MeteorDungeonStructure();
    private MeteorDungeonStructure() {}

    @Override
    protected boolean isCacheable() {
        return false;
    }

    @Override
    protected void buildStructure(@NotNull LegacyBuilder builder, @NotNull Random rand) {
        CellularDungeonFactory.meteor.generate(builder, 0, 0, 0, rand);
    }

    @Override
    public List<@NotNull BlockPos> getValidationPoints(@NotNull BlockPos origin) {
        return Collections.singletonList(origin);
    }
}
