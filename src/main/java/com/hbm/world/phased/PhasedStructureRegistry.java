package com.hbm.world.phased;

import com.hbm.world.*;
import com.hbm.world.dungeon.AncientTombStructure;
import com.hbm.world.dungeon.ArcticVault;
import com.hbm.world.dungeon.LibraryDungeon;
import com.hbm.world.feature.*;
import com.hbm.world.generator.JungleDungeonStructure;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class PhasedStructureRegistry {
    private static final Short2ObjectOpenHashMap<Class<? extends IPhasedStructure>> registry = new Short2ObjectOpenHashMap<>(64);
    private static final Object2ShortOpenHashMap<Object> reverseRegistry = new Object2ShortOpenHashMap<>(64);
    private static final Short2ObjectOpenHashMap<Function<NBTTagCompound, IPhasedStructure>> factoryRegistry = new Short2ObjectOpenHashMap<>(64);

    static {
        reverseRegistry.defaultReturnValue((short) -1);
        short i = 0;
        register(AncientTombStructure.class, i++, AncientTombStructure.INSTANCE);
        register(Antenna.class, i++, Antenna.INSTANCE);
        register(ArcticVault.class, i++, ArcticVault.INSTANCE);
        register(Barrel.class, i++, Barrel.INSTANCE);
        register(BedrockOilDeposit.class, i++, BedrockOilDeposit.INSTANCE);
        register(BedrockOre.class, i++, BedrockOre.OVERWORLD);
        register(BedrockOre.class, i++, BedrockOre.COLTAN);
        register(BedrockOre.class, i++, BedrockOre.NETHER_GLOWSTONE);
        register(BedrockOre.class, i++, BedrockOre.NETHER_QUARTZ);
        register(BedrockOre.class, i++, BedrockOre.NETHER_POWDER_FIRE);
        register(Bunker.class, i++, Bunker.INSTANCE);
        register(DepthDeposit.class, i++, DepthDeposit::readFromNBT);
        register(DesertAtom001.class, i++, DesertAtom001.INSTANCE);
        register(Geyser.class, i++, Geyser.INSTANCE);
        register(GeyserLarge.class, i++, GeyserLarge.INSTANCE);
        register(GlyphidHive.class, i++, GlyphidHive.INFECTED);
        register(GlyphidHive.class, i++, GlyphidHive.INFECTED_NOLOOT);
        register(GlyphidHive.class, i++, GlyphidHive.NORMAL);
        register(GlyphidHive.class, i++, GlyphidHive.NORMAL_NOLOOT);
        register(JungleDungeonStructure.class, i++, JungleDungeonStructure.INSTANCE);
        register(LibraryDungeon.class, i++, LibraryDungeon.INSTANCE);
        register(NTMFlowers.class, i++, NTMFlowers.INSTANCE_FOXGLOVE);
        register(NTMFlowers.class, i++, NTMFlowers.INSTANCE_HEMP);
        register(NTMFlowers.class, i++, NTMFlowers.INSTANCE_TOBACCO);
        register(NTMFlowers.class, i++, NTMFlowers.INSTANCE_NIGHTSHADE);
        register(OilBubble.class, i++, OilBubble::readFromNBT);
        register(OilSandBubble.class, i++, OilSandBubble::readFromNBT);
        register(Radio01.class, i++, Radio01.INSTANCE);
        register(Relay.class, i++, Relay.INSTANCE);
        register(Satellite.class, i++, Satellite.INSTANCE);
        register(Sellafield.class, i++, Sellafield::readFromNBT);
        register(Spaceship.class, i++, Spaceship.INSTANCE);
        register(WorldGenMinableNonCascade.class, i++, WorldGenMinableNonCascade::readFromNBT);
    }

    public static void register(Class<? extends IPhasedStructure> structure, short id, IPhasedStructure instance) {
        registry.put(id, structure);
        factoryRegistry.put(id, _ -> instance);
        reverseRegistry.put(instance, id);
    }

    public static void register(Class<? extends IPhasedStructure> structure, short id, Function<NBTTagCompound, IPhasedStructure> factory) {
        registry.put(id, structure);
        factoryRegistry.put(id, factory);
        reverseRegistry.put(structure, id);
    }

    public static short getId(@NotNull IPhasedStructure structure) {
        short id = reverseRegistry.getShort(structure);
        if (id < 0) id = reverseRegistry.getShort(structure.getClass());
        if (id < 0) throw new IllegalArgumentException("Unknown phased structure: " + structure);
        return id;
    }

    public static @Nullable Class<? extends IPhasedStructure> byId(short id) {
        return registry.get(id);
    }

    public static @NotNull IPhasedStructure deserialize(short id, NBTTagCompound nbt) throws Exception {
        Function<NBTTagCompound, IPhasedStructure> factory = factoryRegistry.get(id);
        if (factory == null) throw new IllegalArgumentException("Unknown phased structure ID: " + id);
        IPhasedStructure result = factory.apply(nbt);
        if (result == null) throw new IllegalStateException("Factory for ID " + id + " returned null");
        return result;
    }
}
