package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.registry.NamedRegistry;
import com.hbm.hazard.HazardData;
import com.hbm.hazard.HazardEntry;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardSystem;
import com.hbm.hazard.modifier.*;
import com.hbm.hazard.type.HazardTypeBase;
import groovy.lang.Closure;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;
import java.util.function.Function;

@SuppressWarnings("MethodMayBeStatic")
public final class Hazards extends NamedRegistry {

    private final HazardTypeFacade types = new HazardTypeFacade();
    private final HazardModifierFacade modifiers = new HazardModifierFacade();

    public Hazards() {
        super(Collections.singletonList("hazards"));
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public HazardTypeFacade getTypes() {
        return types;
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public HazardModifierFacade getModifiers() {
        return modifiers;
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public HazardDataBuilder data() {
        return new HazardDataBuilder();
    }

    @MethodDescription()
    public HazardData register(Object target, HazardData data) {
        if (data == null) {
            GroovyLog.get().warn("HBM hazard register: data is null for target {}", target);
            return null;
        }
        int count = registerTargets(target, data);
        refreshIfNeeded(count, "register", target);
        return data;
    }

    @MethodDescription()
    public HazardData register(Object target, HazardDataBuilder builder) {
        if (builder == null) {
            GroovyLog.get().warn("HBM hazard register: builder is null for target {}", target);
            return null;
        }
        return register(target, builder.build());
    }

    @MethodDescription()
    public HazardData register(Object target, Closure<?> definition) {
        if (definition == null) {
            GroovyLog.get().warn("HBM hazard register: closure is null for target {}", target);
            return null;
        }
        HazardDataBuilder builder = data();
        Closure<?> callable = (Closure<?>) definition.clone();
        callable.setDelegate(builder);
        callable.setResolveStrategy(Closure.DELEGATE_FIRST);
        callable.call(builder);
        return register(target, builder.build());
    }

    @MethodDescription()
    public boolean unregister(Object target) {
        int count = forEachTarget(target, HazardSystem::unregister, HazardSystem::unregister);
        refreshIfNeeded(count, "unregister", target);
        return count > 0;
    }

    @MethodDescription()
    public void blacklist(Object target) {
        int count = forEachTarget(target, o -> {
            HazardSystem.blacklist(o);
            return true;
        }, stack -> {
            HazardSystem.blacklist(stack);
            return true;
        });
        refreshIfNeeded(count, "blacklist", target);
    }

    @MethodDescription()
    public boolean unblacklist(Object target) {
        int count = forEachTarget(target, HazardSystem::unblacklist, HazardSystem::unblacklist);
        refreshIfNeeded(count, "unblacklist", target);
        return count > 0;
    }

    @MethodDescription()
    public void clearCaches() {
        HazardSystem.clearCaches();
    }

    @MethodDescription()
    public void refresh() {
        HazardSystem.clearCaches();
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server != null) {
            for (EntityPlayer player : server.getPlayerList().getPlayers()) {
                HazardSystem.schedulePlayerUpdate(player);
            }
        }
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public float radiationLevel(ItemStack stack) {
        return hazardLevel(stack, HazardRegistry.RADIATION);
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public float hazardLevel(ItemStack stack, HazardTypeBase type) {
        if (stack == null || stack.isEmpty() || type == null) return 0F;
        return (float) HazardSystem.getHazardLevelFromStack(stack, type);
    }

    private int registerTargets(Object target, HazardData data) {
        return forEachTarget(target, o -> {
            HazardSystem.register(o, data);
            return true;
        }, stack -> {
            HazardSystem.register(stack, data);
            return true;
        });
    }

    private int forEachTarget(Object target, Function<Object, Boolean> objectFunction, Function<ItemStack, Boolean> stackFunction) {
        if (target == null) {
            return 0;
        }
        if (target instanceof HazardDataBuilder) {
            GroovyLog.get().warn("Received HazardDataBuilder where a target was expected. Use hazards.register(target, builder) instead.");
            return 0;
        }
        if (target instanceof HazardData) {
            GroovyLog.get().warn("Expected hazard target but received HazardData. Did you mean to call hazards.register(target, data)?");
            return 0;
        }
        if (target instanceof IIngredient ingredient) {
            int count = 0;
            for (ItemStack stack : ingredient.getMatchingStacks()) {
                if (stack != null && !stack.isEmpty()) {
                    ItemStack copy = stack.copy();
                    copy.setCount(1);
                    Boolean applied = stackFunction.apply(copy);
                    if (Boolean.TRUE.equals(applied)) count++;
                }
            }
            return count;
        }
        if (target instanceof ItemStack stack) {
            ItemStack copy = stack.copy();
            copy.setCount(1);
            Boolean applied = stackFunction.apply(copy);
            return Boolean.TRUE.equals(applied) ? 1 : 0;
        }
        if (target instanceof Collection<?>) {
            int count = 0;
            for (Object element : (Collection<?>) target) {
                count += forEachTarget(element, objectFunction, stackFunction);
            }
            return count;
        }
        if (target.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(target);
            int count = 0;
            for (int i = 0; i < length; i++) {
                Object element = java.lang.reflect.Array.get(target, i);
                count += forEachTarget(element, objectFunction, stackFunction);
            }
            return count;
        }
        if (target instanceof String value) {
            if (value.contains(":")) {
                try {
                    return forEachTarget(new ResourceLocation(value), objectFunction, stackFunction);
                } catch (IllegalArgumentException ex) {
                    GroovyLog.get().error("Invalid resource location '{}': {}", value, ex.getMessage());
                    return 0;
                }
            }
        }
        Boolean result = objectFunction.apply(target);
        return result != null && result ? 1 : 0;
    }

    private void refreshIfNeeded(int count, String action, Object target) {
        if (count > 0) {
            refresh();
        } else {
            GroovyLog.get().warn("HBM hazard {}: no targets matched for {}", action, target);
        }
    }

    @SuppressWarnings("MethodMayBeStatic")
    public static final class HazardTypeFacade {
        private final Map<String, HazardTypeBase> lookup;

        HazardTypeFacade() {
            lookup = new LinkedHashMap<>();
            register("radiation", HazardRegistry.RADIATION, "rad", "rads");
            register("contaminating", HazardRegistry.CONTAMINATING, "contam");
            register("digamma", HazardRegistry.DIGAMMA, "dg");
            register("hot", HazardRegistry.HOT);
            register("blinding", HazardRegistry.BLINDING, "blind");
            register("asbestos", HazardRegistry.ASBESTOS);
            register("coal", HazardRegistry.COAL);
            register("hydroactive", HazardRegistry.HYDROACTIVE, "hydro");
            register("explosive", HazardRegistry.EXPLOSIVE, "boom");
            register("toxic", HazardRegistry.TOXIC);
            register("cold", HazardRegistry.COLD);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardTypeBase get(String name) {
            if (name == null) return null;
            HazardTypeBase type = lookup.get(name.toLowerCase(Locale.ENGLISH));
            if (type == null) {
                GroovyLog.get().warn("Unknown HBM hazard type '{}'.", name);
            }
            return type;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardTypeBase radiation() {
            return HazardRegistry.RADIATION;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardTypeBase contaminating() {
            return HazardRegistry.CONTAMINATING;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardTypeBase digamma() {
            return HazardRegistry.DIGAMMA;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardTypeBase hot() {
            return HazardRegistry.HOT;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardTypeBase blinding() {
            return HazardRegistry.BLINDING;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardTypeBase asbestos() {
            return HazardRegistry.ASBESTOS;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardTypeBase coal() {
            return HazardRegistry.COAL;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardTypeBase hydroactive() {
            return HazardRegistry.HYDROACTIVE;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardTypeBase explosive() {
            return HazardRegistry.EXPLOSIVE;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardTypeBase toxic() {
            return HazardRegistry.TOXIC;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardTypeBase cold() {
            return HazardRegistry.COLD;
        }

        private void register(String name, HazardTypeBase type, String... aliases) {
            lookup.put(name, type);
            for (String alias : aliases) {
                lookup.put(alias, type);
            }
        }
    }

    @SuppressWarnings("MethodMayBeStatic")
    public static final class HazardModifierFacade {
        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardModifierFuelRadiation fuelRadiation(float target) {
            return new HazardModifierFuelRadiation(target);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardModifierRTGRadiation rtgRadiation(float target) {
            return new HazardModifierRTGRadiation(target);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardModifierRBMKRadiation rbmkRadiation(float target) {
            return new HazardModifierRBMKRadiation(target, false);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardModifierRBMKRadiation rbmkRadiation(float target, boolean linear) {
            return new HazardModifierRBMKRadiation(target, linear);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardModifierRBMKHot rbmkHot() {
            return new HazardModifierRBMKHot();
        }
    }

    public static final class HazardDataBuilder {
        private final HazardData data = new HazardData();
        private boolean built;

        private void ensureMutable() {
            if (built) throw new IllegalStateException("HazardDataBuilder already built");
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardDataBuilder override(boolean override) {
            ensureMutable();
            data.setOverride(override);
            return this;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardDataBuilder mutex(int mutex) {
            ensureMutable();
            data.setMutex(mutex);
            return this;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardDataBuilder entry(HazardTypeBase type, float level, HazardModifier... modifiers) {
            ensureMutable();
            if (type == null) {
                GroovyLog.get().warn("Cannot add hazard entry for null type.");
                return this;
            }
            HazardEntry entry = new HazardEntry(type, level);
            if (modifiers != null) {
                for (HazardModifier modifier : modifiers) {
                    if (modifier != null) {
                        entry.addMod(modifier);
                    }
                }
            }
            data.addEntry(entry);
            return this;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardDataBuilder radiation(float level, HazardModifier... modifiers) {
            return entry(HazardRegistry.RADIATION, level, modifiers);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardDataBuilder contaminating(float level) {
            return entry(HazardRegistry.CONTAMINATING, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardDataBuilder digamma(float level) {
            return entry(HazardRegistry.DIGAMMA, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardDataBuilder hot(float level) {
            return entry(HazardRegistry.HOT, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardDataBuilder blinding(float level) {
            return entry(HazardRegistry.BLINDING, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardDataBuilder asbestos(float level) {
            return entry(HazardRegistry.ASBESTOS, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardDataBuilder coal(float level) {
            return entry(HazardRegistry.COAL, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardDataBuilder hydroactive(float level) {
            return entry(HazardRegistry.HYDROACTIVE, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardDataBuilder explosive(float level) {
            return entry(HazardRegistry.EXPLOSIVE, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardDataBuilder toxic(float level) {
            return entry(HazardRegistry.TOXIC, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardDataBuilder cold(float level) {
            return entry(HazardRegistry.COLD, level);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public HazardData build() {
            ensureMutable();
            built = true;
            return data;
        }
    }
}
