package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.registry.NamedRegistry;
import com.hbm.hazard.HazardEntry;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.modifier.HazardModifier;
import com.hbm.hazard.transformer.HazardTransformerForgeFluid;
import com.hbm.hazard.type.HazardTypeBase;
import groovy.lang.Closure;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("MethodMayBeStatic")
public final class FluidHazards extends NamedRegistry {

    private final Hazards.HazardTypeFacade types = new Hazards.HazardTypeFacade();
    private final Hazards.HazardModifierFacade modifiers = new Hazards.HazardModifierFacade();

    public FluidHazards() {
        super(Collections.singletonList("fluidHazards"));
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public Hazards.HazardTypeFacade getTypes() {
        return types;
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public Hazards.HazardModifierFacade getModifiers() {
        return modifiers;
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public FluidHazardBuilder builder() {
        return new FluidHazardBuilder();
    }

    @Nullable
    private String normalizeFluidName(Object id) {
        if (id == null) return null;
        if (id instanceof String) {
            String s = ((String) id).trim();
            if (s.isEmpty()) return null;
            int colon = s.indexOf(':');
            return (colon >= 0 && colon < s.length() - 1)
                    ? s.substring(colon + 1).toLowerCase(Locale.ENGLISH)
                    : s.toLowerCase(Locale.ENGLISH);
        }
        if (id instanceof ResourceLocation) {
            return ((ResourceLocation) id).getPath().toLowerCase(Locale.ENGLISH);
        }
        if (id instanceof net.minecraftforge.fluids.Fluid) {
            return ((net.minecraftforge.fluids.Fluid) id).getName().toLowerCase(Locale.ENGLISH);
        }
        GroovyLog.get().warn("Unknown fluid identifier type '{}': {}", id.getClass().getName(), id);
        return null;
    }

    private void warnIfEmpty(@Nullable String fluidName, String action) {
        if (fluidName == null || fluidName.isEmpty()) {
            GroovyLog.get().warn("HBM fluid hazard {}: invalid or empty fluid id", action);
        }
    }

    @MethodDescription()
    public void add(Object fluidId, HazardEntry entry) {
        String name = normalizeFluidName(fluidId);
        warnIfEmpty(name, "add");
        if (name == null || entry == null) return;
        HazardTransformerForgeFluid.FLUID_HAZARDS
                .computeIfAbsent(name, k -> new it.unimi.dsi.fastutil.objects.ObjectArrayList<>())
                .add(entry);
    }

    @MethodDescription()
    public void add(Object fluidId, FluidHazardBuilder builder) {
        String name = normalizeFluidName(fluidId);
        warnIfEmpty(name, "add");
        if (name == null || builder == null) return;
        for (HazardEntry e : builder.entries()) {
            if (e != null) {
                HazardTransformerForgeFluid.FLUID_HAZARDS
                        .computeIfAbsent(name, k -> new it.unimi.dsi.fastutil.objects.ObjectArrayList<>())
                        .add(e);
            }
        }
    }

    @MethodDescription()
    public void add(Object fluidId, Closure<?> definition) {
        if (definition == null) {
            GroovyLog.get().warn("HBM fluid hazard add: closure is null for {}", fluidId);
            return;
        }
        FluidHazardBuilder b = builder();
        Closure<?> c = (Closure<?>) definition.clone();
        c.setDelegate(b);
        c.setResolveStrategy(Closure.DELEGATE_FIRST);
        c.call(b);
        add(fluidId, b);
    }

    @MethodDescription(description = "Replace all hazards for a fluid with the provided builder.")
    public void set(Object fluidId, FluidHazardBuilder builder) {
        clear(fluidId);
        add(fluidId, builder);
    }

    @MethodDescription()
    public void set(Object fluidId, Closure<?> definition) {
        clear(fluidId);
        add(fluidId, definition);
    }

    @MethodDescription()
    public boolean remove(Object fluidId, HazardEntry entry) {
        String name = normalizeFluidName(fluidId);
        warnIfEmpty(name, "remove");
        if (name == null || entry == null) return false;
        it.unimi.dsi.fastutil.objects.ObjectArrayList<HazardEntry> list =
                HazardTransformerForgeFluid.FLUID_HAZARDS.get(name);
        if (list == null) return false;
        boolean removed = list.remove(entry);
        if (removed && list.isEmpty()) {
            HazardTransformerForgeFluid.FLUID_HAZARDS.remove(name);
        }
        return removed;
    }

    @MethodDescription()
    public void clear(Object fluidId) {
        String name = normalizeFluidName(fluidId);
        warnIfEmpty(name, "clear");
        if (name == null) return;
        it.unimi.dsi.fastutil.objects.ObjectArrayList<HazardEntry> prev =
                HazardTransformerForgeFluid.FLUID_HAZARDS.remove(name);
        if (prev == null || prev.isEmpty()) {
            GroovyLog.get().info("No fluid hazards registered for '{}'", name);
        }
    }

    @MethodDescription()
    public void clearAll() {
        HazardTransformerForgeFluid.FLUID_HAZARDS.clear();
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public List<HazardEntry> list(Object fluidId) {
        String name = normalizeFluidName(fluidId);
        if (name == null) return Collections.emptyList();
        it.unimi.dsi.fastutil.objects.ObjectArrayList<HazardEntry> list =
                HazardTransformerForgeFluid.FLUID_HAZARDS.get(name);
        if (list == null) return Collections.emptyList();
        return Collections.unmodifiableList(new ArrayList<>(list));
    }

    @MethodDescription(description = "Set whether to apply fluid hazards to NTM containers. default: true")
    public boolean setApplyToNTMContainer(boolean value) {
        return (HazardTransformerForgeFluid.applyToNTMContainer = value);
    }

    public static final class FluidHazardBuilder {
        private final List<HazardEntry> entries = new ArrayList<>();
        private boolean built;

        private void ensureMutable() {
            if (built) throw new IllegalStateException("FluidHazardBuilder already built");
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public FluidHazardBuilder entry(HazardTypeBase type, float level, HazardModifier... mods) {
            ensureMutable();
            if (type == null) {
                GroovyLog.get().warn("Cannot add fluid hazard entry for null type.");
                return this;
            }
            HazardEntry e = new HazardEntry(type, level);
            if (mods != null) {
                for (HazardModifier m : mods) if (m != null) e.addMod(m);
            }
            entries.add(e);
            return this;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public FluidHazardBuilder radiation(float level, HazardModifier... mods) {
            return entry(HazardRegistry.RADIATION, level, mods);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public FluidHazardBuilder contaminating(float level) {
            return entry(HazardRegistry.CONTAMINATING, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public FluidHazardBuilder digamma(float level) {
            return entry(HazardRegistry.DIGAMMA, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public FluidHazardBuilder hot(float level) {
            return entry(HazardRegistry.HOT, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public FluidHazardBuilder blinding(float level) {
            return entry(HazardRegistry.BLINDING, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public FluidHazardBuilder asbestos(float level) {
            return entry(HazardRegistry.ASBESTOS, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public FluidHazardBuilder coal(float level) {
            return entry(HazardRegistry.COAL, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public FluidHazardBuilder hydroactive(float level) {
            return entry(HazardRegistry.HYDROACTIVE, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public FluidHazardBuilder explosive(float level) {
            return entry(HazardRegistry.EXPLOSIVE, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public FluidHazardBuilder toxic(float level) {
            return entry(HazardRegistry.TOXIC, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public FluidHazardBuilder cold(float level) {
            return entry(HazardRegistry.COLD, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public List<HazardEntry> entries() {
            built = true;
            return Collections.unmodifiableList(entries);
        }
    }
}
