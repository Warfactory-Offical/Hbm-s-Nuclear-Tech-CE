package com.hbm.integration.groovy;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.compat.mods.GroovyPropertyContainer;
import com.cleanroommc.groovyscript.registry.NamedRegistry;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.google.gson.JsonElement;
import com.hbm.hazard.HazardData;
import com.hbm.hazard.HazardEntry;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardSystem;
import com.hbm.hazard.modifier.*;
import com.hbm.hazard.type.HazardTypeBase;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.SerializableRecipePacket;
import groovy.lang.Closure;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;

public class HbmGroovyPropertyContainer extends GroovyPropertyContainer {

    private static RecipeOverrideManager activeRecipeOverrides;

    private final RecipeOverrideManager recipeOverrides;
    private final HazardBinding hazards;

    public HbmGroovyPropertyContainer() {
        ensureRecipeHandlersRegistered();
        this.recipeOverrides = new RecipeOverrideManager(SerializableRecipe.recipeHandlers);
        activeRecipeOverrides = this.recipeOverrides;
        addProperty(this.recipeOverrides);
        for (RecipeFileBinding binding : this.recipeOverrides.createBindings()) {
            addProperty(binding);
        }
        this.hazards = new HazardBinding();
        addProperty(this.hazards);
    }

    public static void sendRecipeOverridesToPlayer(EntityPlayerMP player) {
        RecipeOverrideManager manager = activeRecipeOverrides;
        if (manager != null && player != null) {
            manager.sendToPlayer(player);
        }
    }

    private static void ensureRecipeHandlersRegistered() {
        if (SerializableRecipe.recipeHandlers.isEmpty()) {
            SerializableRecipe.registerAllHandlers();
        }
    }

    @SuppressWarnings({"MethodMayBeStatic", "ConstantValue"})
    static final class RecipeOverrideManager extends VirtualizedRegistry<OverrideData> {

        private final Map<SerializableRecipe, OverrideData> overrides = new LinkedHashMap<>();
        private final Map<SerializableRecipe, List<String>> aliasMap = new LinkedHashMap<>();
        private final Map<String, SerializableRecipe> aliasLookup = new LinkedHashMap<>();
        private boolean needsApply;

        RecipeOverrideManager(Collection<SerializableRecipe> handlers) {
            super(Arrays.asList("recipes", "recipeFiles", "recipeOverrides"));
            for (SerializableRecipe handler : handlers) {
                List<String> aliases = computeAliases(handler);
                if (aliases.isEmpty()) {
                    continue;
                }
                aliasMap.put(handler, aliases);
                for (String alias : aliases) {
                    aliasLookup.put(normalizeKey(alias), handler);
                }
            }
        }

        private static List<String> computeAliases(SerializableRecipe handler) {
            Set<String> aliases = new LinkedHashSet<>();
            String className = handler.getClass().getSimpleName();
            String classBase = stripSuffix(className, "Recipes");
            String fileName = handler.getFileName();
            String fileBase = stripSuffix(fileName, ".json");
            addAlias(aliases, className);
            addAlias(aliases, classBase);
            addAlias(aliases, classBase.toLowerCase(Locale.ENGLISH));
            addAlias(aliases, camelToSnake(classBase));
            addAlias(aliases, fileName);
            addAlias(aliases, fileBase);
            addAlias(aliases, camelToSnake(fileBase));
            addAlias(aliases, fileBase.toLowerCase(Locale.ENGLISH));
            List<String> list = new ArrayList<>(aliases);
            return list.isEmpty() ? Collections.singletonList(className.toLowerCase(Locale.ENGLISH)) : Collections.unmodifiableList(list);
        }

        private static void addAlias(Set<String> aliases, @Nullable String alias) {
            if (alias == null) return;
            String trimmed = alias.trim();
            if (!trimmed.isEmpty()) {
                aliases.add(trimmed);
            }
        }

        private static String camelToSnake(String value) {
            if (value == null || value.isEmpty()) return value;
            StringBuilder builder = new StringBuilder(value.length() + 4);
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                if (Character.isUpperCase(c)) {
                    if (builder.length() > 0) builder.append('_');
                    builder.append(Character.toLowerCase(c));
                } else if (c == ' ') {
                    builder.append('_');
                } else {
                    builder.append(Character.toLowerCase(c));
                }
            }
            return builder.toString();
        }

        private static String stripSuffix(String value, String suffix) {
            if (value != null && value.endsWith(suffix)) {
                return value.substring(0, value.length() - suffix.length());
            }
            return value;
        }

        private static String normalizeKey(String key) {
            StringBuilder builder = new StringBuilder(key.length());
            for (int i = 0; i < key.length(); i++) {
                char c = Character.toLowerCase(key.charAt(i));
                if (Character.isLetterOrDigit(c)) {
                    builder.append(c);
                }
            }
            return builder.toString();
        }

        List<RecipeFileBinding> createBindings() {
            List<RecipeFileBinding> result = new ArrayList<>();
            for (Map.Entry<SerializableRecipe, List<String>> entry : aliasMap.entrySet()) {
                result.add(new RecipeFileBinding(this, entry.getKey(), entry.getValue()));
            }
            return result;
        }

        @MethodDescription()
        public void override(@NotNull String target, @NotNull String json) {
            GroovyLog.Msg msg = GroovyLog.msg("Error overriding HBM recipe file").error();
            boolean empty = json == null || json.trim().isEmpty();
            msg.add(empty, "json data must not be empty");
            SerializableRecipe handler = resolve(target, msg);
            if (msg.postIfNotEmpty()) return;
            applyOverride(handler, json.getBytes(StandardCharsets.UTF_8));
        }

        @MethodDescription()
        public void override(@NotNull String target, @NotNull JsonElement json) {
            GroovyLog.Msg msg = GroovyLog.msg("Error overriding HBM recipe file").error();
            msg.add(json == null, "json data must not be null");
            SerializableRecipe handler = resolve(target, msg);
            if (msg.postIfNotEmpty()) return;
            applyOverride(handler, SerializableRecipe.gson.toJson(json).getBytes(StandardCharsets.UTF_8));
        }

        @MethodDescription()
        public void override(@NotNull String target, @NotNull File file) {
            GroovyLog.Msg msg = GroovyLog.msg("Error overriding HBM recipe file from disk").error();
            msg.add(file == null, "file must not be null");
            msg.add(file != null && !file.isFile(), () -> "file '" + file + "' does not exist or is not a regular file");
            SerializableRecipe handler = resolve(target, msg);
            if (msg.postIfNotEmpty()) return;
            try {
                applyOverride(handler, Files.readAllBytes(file.toPath()));
            } catch (IOException ex) {
                GroovyLog.get().error("Failed to read override file {}: {}", file, ex.getMessage());
                GroovyLog.get().exception(ex);
            }
        }

        @MethodDescription()
        public void clear(@NotNull String target) {
            GroovyLog.Msg msg = GroovyLog.msg("Error clearing HBM recipe override").error();
            SerializableRecipe handler = resolve(target, msg);
            if (msg.postIfNotEmpty()) return;
            if (!clearOverride(handler)) {
                GroovyLog.get().info("No override registered for {}", handler.getFileName());
            }
        }

        @MethodDescription()
        public void clearAll() {
            if (!overrides.isEmpty()) {
                overrides.clear();
                needsApply = true;
            }
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public boolean has(@NotNull String target) {
            SerializableRecipe handler = aliasLookup.get(normalizeKey(target));
            return handler != null && overrides.containsKey(handler);
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public List<String> listTargets() {
            List<String> targets = new ArrayList<>();
            for (List<String> aliases : aliasMap.values()) {
                if (!aliases.isEmpty()) {
                    targets.add(aliases.get(0));
                }
            }
            return Collections.unmodifiableList(targets);
        }

        @Override
        public void onReload() {
            overrides.clear();
            removeScripted();
            needsApply = false;
            SerializableRecipe.clearReceivedRecipes();
        }

        @Override
        public void afterScriptLoad() {
            if (!needsApply) return;
            needsApply = false;
            if (!isServerSide()) return;
            SerializableRecipe.clearReceivedRecipes();
            if (overrides.isEmpty()) {
                PacketDispatcher.wrapper.sendToAll(new SerializableRecipePacket(true));
                return;
            }
            for (OverrideData data : overrides.values()) {
                SerializableRecipe.receiveRecipes(data.fileName, data.data.clone());
            }
            SerializableRecipe.initialize();
            broadcastToClients(overrides.values());
        }

        private void broadcastToClients(Collection<OverrideData> entries) {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server == null) return;
            for (OverrideData data : entries) {
                PacketDispatcher.wrapper.sendToAll(new SerializableRecipePacket(data.fileName, data.data));
            }
            PacketDispatcher.wrapper.sendToAll(new SerializableRecipePacket(true));
        }

        void sendToPlayer(EntityPlayerMP player) {
            if (player == null || overrides.isEmpty()) return;
            for (OverrideData data : overrides.values()) {
                PacketDispatcher.wrapper.sendTo(new SerializableRecipePacket(data.fileName, data.data), player);
            }
            PacketDispatcher.wrapper.sendTo(new SerializableRecipePacket(true), player);
        }

        private boolean isServerSide() {
            return FMLCommonHandler.instance().getEffectiveSide().isServer();
        }

        void applyOverride(@Nullable SerializableRecipe handler, byte[] data) {
            if (handler == null || data == null || data.length == 0) return;
            OverrideData override = new OverrideData(handler, data.clone());
            overrides.put(handler, override);
            doAddScripted(override);
            needsApply = true;
        }

        boolean clearOverride(@Nullable SerializableRecipe handler) {
            if (handler == null) return false;
            OverrideData removed = overrides.remove(handler);
            if (removed != null) {
                needsApply = true;
                return true;
            }
            return false;
        }

        boolean hasOverride(@Nullable SerializableRecipe handler) {
            return handler != null && overrides.containsKey(handler);
        }

        private SerializableRecipe resolve(String target, GroovyLog.Msg msg) {
            if (target == null || target.trim().isEmpty()) {
                msg.add(true, "target must not be empty");
                return null;
            }
            SerializableRecipe handler = aliasLookup.get(normalizeKey(target));
            if (handler == null) {
                msg.add(true, () -> "Unknown recipe set '" + target + "'");
            }
            return handler;
        }
    }

    @SuppressWarnings("ConstantValue")
    static final class RecipeFileBinding extends NamedRegistry {

        private final RecipeOverrideManager manager;
        private final SerializableRecipe handler;

        RecipeFileBinding(RecipeOverrideManager manager, SerializableRecipe handler, Collection<String> aliases) {
            super(aliases);
            this.manager = manager;
            this.handler = handler;
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public String fileName() {
            return handler.getFileName();
        }

        @MethodDescription(type = MethodDescription.Type.QUERY)
        public boolean hasOverride() {
            return manager.hasOverride(handler);
        }

        @MethodDescription()
        public void override(@NotNull String json) {
            GroovyLog.Msg msg = GroovyLog.msg("Error overriding HBM recipe file {0}", handler.getFileName()).error();
            boolean empty = json == null || json.trim().isEmpty();
            msg.add(empty, "json data must not be empty");
            if (msg.postIfNotEmpty()) return;
            manager.applyOverride(handler, json.getBytes(StandardCharsets.UTF_8));
        }

        @MethodDescription()
        public void override(@NotNull JsonElement json) {
            GroovyLog.Msg msg = GroovyLog.msg("Error overriding HBM recipe file {0}", handler.getFileName()).error();
            msg.add(json == null, "json data must not be null");
            if (msg.postIfNotEmpty()) return;
            manager.applyOverride(handler, SerializableRecipe.gson.toJson(json).getBytes(StandardCharsets.UTF_8));
        }

        @MethodDescription()
        public void override(@NotNull File file) {
            GroovyLog.Msg msg = GroovyLog.msg("Error overriding HBM recipe file {0}", handler.getFileName()).error();
            msg.add(file == null, "file must not be null");
            msg.add(file != null && !file.isFile(), () -> "file '" + file + "' does not exist or is not a regular file");
            if (msg.postIfNotEmpty()) return;
            try {
                manager.applyOverride(handler, Files.readAllBytes(file.toPath()));
            } catch (IOException ex) {
                GroovyLog.get().error("Failed to read override file {}: {}", file, ex.getMessage());
                GroovyLog.get().exception(ex);
            }
        }

        @MethodDescription()
        public void clear() {
            if (!manager.clearOverride(handler)) {
                GroovyLog.get().info("No override registered for {}", handler.getFileName());
            }
        }
    }

    @SuppressWarnings("MethodMayBeStatic")
    static final class HazardBinding extends NamedRegistry {

        private final HazardTypeFacade types = new HazardTypeFacade();
        private final HazardModifierFacade modifiers = new HazardModifierFacade();

        HazardBinding() {
            super(Arrays.asList("hazards", "hazard", "radiation"));
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
            return HazardSystem.getHazardLevelFromStack(stack, type);
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
        static final class HazardTypeFacade {
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
        static final class HazardModifierFacade {
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

        static final class HazardDataBuilder {
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

    static final class OverrideData {
        final SerializableRecipe handler;
        final String fileName;
        final byte[] data;

        OverrideData(SerializableRecipe handler, byte[] data) {
            this.handler = handler;
            this.fileName = handler.getFileName();
            this.data = data;
        }
    }
}
