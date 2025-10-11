package com.hbm.config;

import com.google.common.base.Optional;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockGlyphid;
import com.hbm.blocks.generic.BlockGlyphidSpawner;
import com.hbm.inventory.RecipesCommon;
import com.hbm.main.MainRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.BlockSand;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@SuppressWarnings({"deprication", "unchecked"})
public class FalloutConfigJSON {

    public static final List<FalloutEntry> entries = new ArrayList();
    public static final Gson gson = new Gson();
    public static HashBiMap<String, Material> matNames = HashBiMap.create();

    static {
        matNames.put("grass", Material.GRASS);
        matNames.put("ground", Material.GROUND);
        matNames.put("wood", Material.WOOD);
        matNames.put("rock", Material.ROCK);
        matNames.put("iron", Material.IRON);
        matNames.put("anvil", Material.ANVIL);
        matNames.put("water", Material.WATER);
        matNames.put("lava", Material.LAVA);
        matNames.put("leaves", Material.LEAVES);
        matNames.put("plants", Material.PLANTS);
        matNames.put("vine", Material.VINE);
        matNames.put("sponge", Material.SPONGE);
        matNames.put("cloth", Material.CLOTH);
        matNames.put("fire", Material.FIRE);
        matNames.put("sand", Material.SAND);
        matNames.put("circuits", Material.CIRCUITS);
        matNames.put("carpet", Material.CARPET);
        matNames.put("redstoneLight", Material.REDSTONE_LIGHT);
        matNames.put("tnt", Material.TNT);
        matNames.put("coral", Material.CORAL);
        matNames.put("ice", Material.ICE);
        matNames.put("packedIce", Material.PACKED_ICE);
        matNames.put("snow", Material.SNOW);
        matNames.put("craftedSnow", Material.CRAFTED_SNOW);
        matNames.put("cactus", Material.CACTUS);
        matNames.put("clay", Material.CLAY);
        matNames.put("gourd", Material.GOURD);
        matNames.put("dragonEgg", Material.DRAGON_EGG);
        matNames.put("portal", Material.PORTAL);
        matNames.put("cake", Material.CAKE);
        matNames.put("web", Material.WEB);
        matNames.put("glass", Material.GLASS);
        matNames.put("piston", Material.PISTON);

    }

    public static void initialize() {
        File folder = MainRegistry.configHbmDir;

        File config = new File(folder.getAbsolutePath() + File.separatorChar + "hbmFallout.json");
        File template = new File(folder.getAbsolutePath() + File.separatorChar + "_hbmFallout.json");

        initDefault();

        if (!config.exists()) {
            writeDefault(template);
        } else {
            List<FalloutEntry> conf = readConfig(config);

            if (conf != null) {
                entries.clear();
                entries.addAll(conf);
            }
        }
    }

    private static void initDefault() {

        double woodEffectRange = 65D;

        /* petrify all wooden things possible */
        entries.add(new FalloutEntry()
                .setBlockState(Blocks.LOG)
                .withPreserveState(BlockRotatedPillar.AXIS)
                .primaryStates(new Tuple<>(ModBlocks.waste_log.getDefaultState(), 1))
                .setMax(woodEffectRange));

        entries.add(new FalloutEntry()
                .setBlockState(Blocks.LOG2)
                .withPreserveState(BlockRotatedPillar.AXIS)
                .primaryStates(new Tuple<>(ModBlocks.waste_log.getDefaultState(), 1))
                .setMax(woodEffectRange));

        entries.add(new FalloutEntry()
                .setBlockState(Blocks.RED_MUSHROOM_BLOCK.getStateFromMeta(10))
                .shouldMatchState(true)
                .primaryStates(new Tuple<>(ModBlocks.waste_log.getDefaultState().withProperty(BlockRotatedPillar.AXIS, EnumFacing.Axis.Y), 1))
                .setMax(woodEffectRange));

        entries.add(new FalloutEntry()
                .setBlockState(Blocks.BROWN_MUSHROOM_BLOCK.getStateFromMeta(10))
                .shouldMatchState(true)
                .primaryStates(new Tuple<>(ModBlocks.waste_log.getDefaultState().withProperty(BlockRotatedPillar.AXIS, EnumFacing.Axis.Y), 1))
                .setMax(woodEffectRange));

        entries.add(new FalloutEntry()
                .setBlockState(Blocks.RED_MUSHROOM_BLOCK)
                .primaryStates(new Tuple<>(Blocks.AIR.getDefaultState(), 1))
                .setMax(woodEffectRange));

        entries.add(new FalloutEntry()
                .setBlockState(Blocks.BROWN_MUSHROOM_BLOCK)
                .primaryStates(new Tuple<>(Blocks.AIR.getDefaultState(), 1))
                .setMax(woodEffectRange));

        entries.add(new FalloutEntry()
                .setBlockState(Blocks.SNOW)
                .primaryStates(new Tuple<>(Blocks.AIR.getDefaultState(), 1))
                .setMax(woodEffectRange));

        entries.add(new FalloutEntry()
                .setBlockState(Blocks.PLANKS)
                .primaryStates(new Tuple<>(ModBlocks.waste_planks.getDefaultState(), 1))
                .setMax(woodEffectRange));

        /* if it can't be petrified, destroy it */
        entries.add(new FalloutEntry()
                .setMatchingMaterial(Material.WOOD)
                .primaryStates(new Tuple<>(Blocks.AIR.getDefaultState(), 1))
                .setMax(woodEffectRange));

        /* destroy all leaves within the radius, kill all leaves outside of it */
        entries.add(new FalloutEntry()
                .setMatchingMaterial(Material.LEAVES)
                .primaryStates(new Tuple<>(Blocks.AIR.getDefaultState(), 1))
                .setMax(woodEffectRange));

        entries.add(new FalloutEntry()
                .setMatchingMaterial(Material.PLANTS)
                .primaryStates(new Tuple<>(Blocks.AIR.getDefaultState(), 1))
                .setMax(woodEffectRange));

        entries.add(new FalloutEntry()
                .setMatchingMaterial(Material.VINE)
                .primaryStates(new Tuple<>(Blocks.AIR.getDefaultState(), 1))
                .setMax(woodEffectRange));

        entries.add(new FalloutEntry()
                .setBlockState(ModBlocks.waste_leaves)
                .primaryStates(new Tuple<>(Blocks.AIR.getDefaultState(), 1))
                .setMax(woodEffectRange));

        entries.add(new FalloutEntry()
                .setBlockState(Blocks.LEAVES)
                .primaryStates(new Tuple<>(ModBlocks.waste_leaves.getDefaultState(), 1))
                .setMinDistance(woodEffectRange - 5D));

        entries.add(new FalloutEntry()
                .setBlockState(Blocks.LEAVES2)
                .primaryStates(new Tuple<>(ModBlocks.waste_leaves.getDefaultState(), 1))
                .setMinDistance(woodEffectRange - 5D));

        entries.add(new FalloutEntry()
                .setBlockState(Blocks.MOSSY_COBBLESTONE)
                .primaryStates(new Tuple<>(Blocks.COAL_ORE.getDefaultState(), 1)));

        entries.add(new FalloutEntry()
                .setBlockState(ModBlocks.ore_nether_uranium)
                .primaryStates(
                        new Tuple<>(ModBlocks.ore_nether_schrabidium.getDefaultState(), 1),
                        new Tuple<>(ModBlocks.ore_nether_uranium_scorched.getDefaultState(), 99)
                ));

        entries.add(new FalloutEntry()
                .setBlockState(ModBlocks.glyphid_base)
                .primaryStates(new Tuple<>(ModBlocks.glyphid_base.getDefaultState().withProperty(BlockGlyphid.TYPE, BlockGlyphid.Type.RAD), 1)));
        entries.add(new FalloutEntry()
                .setBlockState(ModBlocks.glyphid_spawner)
                .primaryStates(new Tuple<>(ModBlocks.glyphid_spawner.getDefaultState().withProperty(BlockGlyphidSpawner.TYPE, BlockGlyphidSpawner.Type.RAD), 1)));


        for (int i = 1; i <= 10; i++) {
            int m = 10 - i;
            entries.add(new FalloutEntry().primaryStates(new Tuple<>(ModBlocks.ore_sellafield_diamond.getStateFromMeta(m), 3), new Tuple<>(ModBlocks.ore_sellafield_emerald.getStateFromMeta(m), 2)).setPrimaryChance(0.5).setMax(i * 5).shouldBeOpaque(true).setBlockState(Blocks.COAL_ORE));
            entries.add(new FalloutEntry().primaryStates(new Tuple<>(ModBlocks.ore_sellafield_diamond.getStateFromMeta(m), 1)).setPrimaryChance(0.2).setMax(i * 5).shouldBeOpaque(true).setBlockState(ModBlocks.ore_lignite));
            entries.add(new FalloutEntry().primaryStates(new Tuple<>(ModBlocks.ore_sellafield_emerald.getStateFromMeta(m), 1)).setMax(i * 5).shouldBeOpaque(true).setBlockState(ModBlocks.ore_beryllium));
            if (m > 4)
                entries.add(new FalloutEntry().primaryStates(new Tuple<>(ModBlocks.ore_sellafield_schrabidium.getStateFromMeta(m), 1), new Tuple<>(ModBlocks.ore_sellafield_uranium_scorched.getStateFromMeta(m), 9)).setMax(i * 5).shouldBeOpaque(true).setBlockState(ModBlocks.ore_uranium));
            if (m > 4)
                entries.add(new FalloutEntry().primaryStates(new Tuple<>(ModBlocks.ore_sellafield_schrabidium.getStateFromMeta(m), 1), new Tuple<>(ModBlocks.ore_sellafield_uranium_scorched.getStateFromMeta(m), 9)).setMax(i * 5).shouldBeOpaque(true).setBlockState(ModBlocks.ore_gneiss_uranium));
            entries.add(new FalloutEntry().primaryStates(new Tuple<>(ModBlocks.ore_sellafield_radgem.getStateFromMeta(m), 1)).setMax(i * 5).shouldBeOpaque(true).setBlockState(Blocks.DIAMOND_ORE));
            entries.add(new FalloutEntry().primaryStates(new Tuple<>(ModBlocks.sellafield_bedrock.getStateFromMeta(m), 1)).setMax(i * 5).shouldBeOpaque(true).setBlockState(Blocks.BEDROCK));
            entries.add(new FalloutEntry().primaryStates(new Tuple<>(ModBlocks.sellafield_bedrock.getStateFromMeta(m), 1)).setMax(i * 5).shouldBeOpaque(true).setBlockState(ModBlocks.ore_bedrock_block));
            entries.add(new FalloutEntry().primaryStates(new Tuple<>(ModBlocks.sellafield_bedrock.getStateFromMeta(m), 1)).setMax(i * 5).shouldBeOpaque(true).setBlockState(ModBlocks.ore_bedrock_oil));
            entries.add(new FalloutEntry().primaryStates(new Tuple<>(ModBlocks.sellafield_bedrock.getStateFromMeta(m), 1)).setMax(i * 5).shouldBeOpaque(true).setBlockState(ModBlocks.sellafield_bedrock));
            entries.add(new FalloutEntry().primaryStates(new Tuple<>(ModBlocks.sellafield_slaked.getStateFromMeta(m), 1)).setMax(i * 5).shouldBeOpaque(true).setMatchingMaterial(Material.IRON));
            entries.add(new FalloutEntry().primaryStates(new Tuple<>(ModBlocks.sellafield_slaked.getStateFromMeta(m), 1)).setMax(i * 5).shouldBeOpaque(true).setMatchingMaterial(Material.ROCK));
            entries.add(new FalloutEntry().primaryStates(new Tuple<>(ModBlocks.sellafield_slaked.getStateFromMeta(m), 1)).setMax(i * 5).shouldBeOpaque(true).setMatchingMaterial(Material.SAND));
            entries.add(new FalloutEntry().primaryStates(new Tuple<>(ModBlocks.sellafield_slaked.getStateFromMeta(m), 1)).setMax(i * 5).shouldBeOpaque(true).setMatchingMaterial(Material.GROUND));
            if (i <= 9)
                entries.add(new FalloutEntry().primaryStates(new Tuple<>(ModBlocks.sellafield_slaked.getStateFromMeta(m), 1)).setMax(i * 5).shouldBeOpaque(true).setMatchingMaterial(Material.GRASS));
        }


        entries.add(new FalloutEntry()
                .setBlockState(Blocks.MYCELIUM)
                .primaryStates(new Tuple<>(ModBlocks.waste_mycelium.getDefaultState(), 1)));
        entries.add(new FalloutEntry()
                .setBlockState(Blocks.SAND.getDefaultState())
                .primaryStates(new Tuple<>(ModBlocks.waste_trinitite.getDefaultState(), 1))
                .setPrimaryChance(0.05));
        entries.add(new FalloutEntry()
                .setBlockState(Blocks.SAND.getDefaultState().withProperty(BlockSand.VARIANT, BlockSand.EnumType.RED_SAND))
                .primaryStates(new Tuple<>(ModBlocks.waste_trinitite_red.getDefaultState(), 1))
                .setPrimaryChance(0.05));
        entries.add(new FalloutEntry()
                .setBlockState(Blocks.CLAY)
                .primaryStates(new Tuple<>(Blocks.HARDENED_CLAY.getDefaultState(), 1)));
    }

    private static void writeDefault(File file) {

        try {
            JsonWriter writer = new JsonWriter(new FileWriter(file));
            writer.setIndent("  ");                    //pretty formatting
            writer.beginObject();                    //initial '{'
            writer.name("entries").beginArray();    //all recipes are stored in an array called "entries"

            for (FalloutEntry entry : entries) {
                writer.beginObject();                //begin object for a single recipe
                entry.write(writer);                //serialize here
                writer.endObject();                    //end recipe object
            }

            writer.endArray();                        //end recipe array
            writer.endObject();                        //final '}'
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<FalloutEntry> readConfig(File config) {

        try {
            JsonObject json = gson.fromJson(new FileReader(config), JsonObject.class);
            JsonArray recipes = json.get("entries").getAsJsonArray();
            List<FalloutEntry> conf = new ArrayList<>();

            for (JsonElement recipe : recipes) {
                conf.add(FalloutEntry.readEntry(recipe));
            }
            return conf;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static class FalloutEntry {
        private IBlockState blockState = null;
        private Material matchesMaterial = null;
        private boolean matchState = true;
        private boolean matchesOpaque = false;


        @SuppressWarnings("unchecked")
        private IProperty<? extends Comparable<?>>[] preservedProperties = null;

        //BlockState / Weight
        private Tuple<IBlockState, Integer>[] primaryBlocks = null;
        private Tuple<IBlockState, Integer>[] secondaryBlocks = null;
        private double primaryChance = 1.0D;
        private double minDist = 0.0D;
        private double maxDist = 100.0D;
        private double falloffStart = 0.9D;

        /**
         * Whether the depth value should be decremented when this block is converted
         */
        private boolean isSolid = false;

        private static <T extends Comparable<T>> IBlockState applyProperty(IBlockState state, IProperty<T> prop, Object value) {
            return state.withProperty(prop, (T) value);
        }

        public void setPreserveState(IProperty<?>... properties) {
            this.preservedProperties = properties;
        }

        public FalloutEntry withPreserveState(IProperty<?>... properties) {
            this.preservedProperties = properties;
            return this;
        }

        private static String stateToString(IBlockState state) {
            StringBuilder builder = new StringBuilder();
            Block block = state.getBlock();

            builder.append(ForgeRegistries.BLOCKS.getKey(block));
            builder.append("[");

            ImmutableMap<IProperty<?>, Comparable<?>> properties = state.getProperties();
            Iterator<Map.Entry<IProperty<?>, Comparable<?>>> iterator =
                    properties.entrySet().stream()
                            .sorted(Comparator.comparing(e -> e.getKey().getName()))
                            .iterator();

            while (iterator.hasNext()) {
                Map.Entry<IProperty<?>, Comparable<?>> entry = iterator.next();
                builder.append(entry.getKey().getName())
                        .append("=")
                        .append(entry.getValue().toString());
                if (iterator.hasNext()) {
                    builder.append(", ");
                }
            }

            builder.append("]");
            return builder.toString();
        }

        private static FalloutEntry readEntry(JsonElement recipe) {
            FalloutEntry entry = new FalloutEntry();
            if (!recipe.isJsonObject()) return null;

            JsonObject obj = recipe.getAsJsonObject();

            if (obj.has("matchesBlock"))
                entry.setBlockState(parseBlockState(obj.get("matchesBlock").getAsString()));
            if (obj.has("matchesState")) entry.shouldMatchState(obj.get("matchesState").getAsBoolean());
            if (obj.has("mustBeOpaque")) entry.shouldBeOpaque(obj.get("mustBeOpaque").getAsBoolean());
            if (obj.has("matchesMaterial"))
                entry.setMatchingMaterial(matNames.get(obj.get("mustBeOpaque").getAsString()));
            if (obj.has("restrictDepth")) entry.isSolid(obj.get("restrictDepth").getAsBoolean());
            if(obj.has("preserveState")) entry.setPreserveState(readPreserveStateArray( entry.blockState.getBlock(), obj.get("preserveState").getAsJsonArray()));

            if (obj.has("primarySubstitution")) entry.primaryStates(readMetaArray(obj.get("primarySubstitution")));
            if (obj.has("secondarySubstitutions"))
                entry.secondaryStates(readMetaArray(obj.get("secondarySubstitutions")));

            if (obj.has("chance")) entry.setPrimaryChance(obj.get("chance").getAsDouble());

            if (obj.has("minimumDistancePercent"))
                entry.setMinDistance(obj.get("minimumDistancePercent").getAsDouble());
            if (obj.has("maximumDistancePercent")) entry.setMax(obj.get("maximumDistancePercent").getAsDouble());
            if (obj.has("falloffStartFactor")) entry.setFallOffStart(obj.get("falloffStartFactor").getAsDouble());

            return entry;
        }

        private static IProperty<?>[] readPreserveStateArray(Block block, JsonElement element) {

            if (!element.isJsonArray()) return null;
            JsonArray array = element.getAsJsonArray();
            var outArray = new IProperty<?>[array.size()];
            for(int index = 0; index < array.size(); index++){
                String rawProperty = array.get(index).getAsString();
                IProperty property = getPropertyByName(block, rawProperty);
                if(property!=null)
                    outArray[index] = property;
            }

            return outArray;
        }

        private static String writePreserveStateArray(IProperty<?>[] properties){
            var sb = new StringBuilder();
            sb.append("[");
            for(int index = 0; index < properties.length; index++){
                sb.append(properties[index].getName());
                sb.append(index == properties.length - 1? "]":", ");
            }
            return sb.toString();
        }

        private static IProperty<?> getPropertyByName(Block block, String name) {
            for (IProperty<?> prop : block.getBlockState().getProperties()) {
                if (prop.getName().equals(name)) {
                    return prop;
                }
            }
            return null;
        }


        private static void writeStateArray(JsonWriter writer, Tuple<IBlockState, Integer>[] array) throws IOException {
            writer.beginArray();
            writer.setIndent("");

            for (Tuple<IBlockState, Integer> state : array) {
                writer.beginArray();
                writer.value(stateToString(state.getFirst()));
                writer.value(state.getSecond());
                writer.endArray();
            }

            writer.endArray();
            writer.setIndent("  ");
        }

        private static Tuple<IBlockState, Integer>[] readMetaArray(JsonElement jsonElement) {

            if (!jsonElement.isJsonArray()) return null;

            JsonArray array = jsonElement.getAsJsonArray();
            Tuple<IBlockState, Integer>[] stateArray = new Tuple[array.size()];

            for (int i = 0; i < stateArray.length; i++) {
                JsonElement metaBlock = array.get(i);

                if (!metaBlock.isJsonArray()) {
                    throw new IllegalStateException("Could not read meta block " + metaBlock);
                }

                JsonArray mBArray = metaBlock.getAsJsonArray();

                stateArray[i] = new Tuple<>(parseBlockState(mBArray.get(0).getAsString()), mBArray.get(1).getAsInt());
            }

            return stateArray;
        }

        private static IBlockState parseBlockState(String input) {
            String blockName;
            String propsPart = null;
            int idx = input.indexOf('[');
            if (idx != -1) {
                blockName = input.substring(0, idx);
                propsPart = input.substring(idx + 1, input.length() - 1);
            } else {
                //Assume no blockstate specified
                blockName = input;
            }

            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockName));
            if (block == null) {
                throw new IllegalArgumentException("Unknown block: " + blockName); //TODO:Graceful handing
            }
            IBlockState state = block.getDefaultState();

            if (propsPart == null || propsPart.isEmpty()) {
                return state;
            }
            String[] props = propsPart.split(",");
            for (String prop : props) {
                String[] kv = prop.split("=");
                if (kv.length != 2) continue;

                String key = kv[0];
                String val = kv[1];

                IProperty<?> property = block.getBlockState().getProperty(key);
                if (property == null) {
                    throw new IllegalArgumentException("Unknown property " + key + " for block " + blockName);
                }

                Optional<?> parsed = property.parseValue(val);
                if (!parsed.isPresent()) {
                    throw new IllegalArgumentException("Invalid value " + val + " for property " + key);
                }

                state = applyProperty(state, property, parsed.get());
            }
            return state;

        }

        public FalloutEntry clone() {
            FalloutEntry entry = new FalloutEntry();
            entry.setBlockState(blockState);
            entry.setMatchingMaterial(matchesMaterial);
            entry.shouldBeOpaque(matchesOpaque);
            entry.primaryStates(primaryBlocks);
            entry.secondaryStates(secondaryBlocks);
            entry.setMinDistance(minDist);
            entry.setMax(maxDist);
            entry.setFallOffStart(falloffStart);
            entry.isSolid(isSolid);

            return entry;
        }

        public FalloutEntry setBlockState(IBlockState block) {
            this.blockState = block;
            return this;
        }

        public FalloutEntry setBlockState(Block block) {
            this.blockState = block.getDefaultState();
            this.matchState = false;
            return this;
        }


        public FalloutEntry setMatchingMaterial(Material mat) {
            this.matchesMaterial = mat;
            return this;
        }

        public FalloutEntry shouldBeOpaque(boolean opaque) {
            this.matchesOpaque = opaque;
            return this;
        }

        public FalloutEntry primaryStates(Tuple<IBlockState, Integer>... blocks) {
            this.primaryBlocks = blocks;
            return this;
        }

        public FalloutEntry secondaryStates(Tuple<IBlockState, Integer>... blocks) {
            this.secondaryBlocks = blocks;
            return this;
        }

        public FalloutEntry setPrimaryChance(double chance) {
            this.primaryChance = chance;
            return this;
        }

        public FalloutEntry setMinDistance(double min) {
            this.minDist = min;
            return this;
        }

        public FalloutEntry setMax(double max) {
            this.maxDist = max;
            return this;
        }

        public FalloutEntry shouldMatchState(boolean matchState) {
            this.matchState = matchState;
            return this;
        }

        public FalloutEntry setFallOffStart(double falloffStart) {
            this.falloffStart = falloffStart;
            return this;
        }

        public FalloutEntry isSolid(boolean solid) {
            this.isSolid = solid;
            return this;
        }

        /**
         * Evaluates whether this {@link FalloutEntry} should convert the block at the given
         * position.
         *
         * @param yGlobal            the target block's y coordinate
         * @param blockState         the current block state at {@code pos} to test against this entry
         * @param dist               distance factor from the effect origin (same units as {@link #minDist}/{@link #maxDist}; typically a percentage)
         * @return the IBlockState if the block needs to be replaced, or null if no-op
         */
        @Nullable
        @Contract(mutates = "param4")
        public IBlockState eval(int yGlobal, IBlockState blockState, double dist, Random random) {
            if (dist > maxDist || dist < minDist) return null;
            Block originalBlock = blockState.getBlock();
            if (this.blockState != null && originalBlock != this.blockState.getBlock()) return null;
            if (matchesMaterial != null && blockState.getMaterial() != matchesMaterial) return null;
            if (matchState && blockState.equals(this.blockState)) return null;
            if (matchesOpaque && !blockState.isOpaqueCube()) return null;

            if (dist > maxDist * falloffStart) {
                double t = (dist - maxDist * falloffStart) / (maxDist - maxDist * falloffStart);
                if (Math.abs(random.nextGaussian()) < t * t * 3.0) {
                    return null;
                }
            }

            RecipesCommon.MetaBlock conversion =
                    chooseRandomOutcome((primaryChance == 1D || random.nextDouble() < primaryChance) ? primaryBlocks : secondaryBlocks, random);

            if (conversion == null) return null;

            int originalMeta = originalBlock.getMetaFromState(blockState);

            if (conversion.block == ModBlocks.sellafield_slaked &&
                    originalBlock == ModBlocks.sellafield_slaked &&
                    conversion.meta <= originalMeta) {
                return null;
            }

            if (conversion.block == ModBlocks.sellafield_bedrock &&
                    originalBlock == ModBlocks.sellafield_bedrock &&
                    conversion.meta <= originalMeta) {
                return null;
            }

            if (originalBlock == ModBlocks.sellafield_bedrock &&
                    conversion.block != ModBlocks.sellafield_bedrock) {
                return null;
            }

            if (yGlobal == 0 && conversion.block != ModBlocks.sellafield_bedrock) {
                return null;
            }

            IBlockState newState = conversion.block.getStateFromMeta(conversion.meta);
            if(preservedProperties != null) {
                for (IProperty<?> property : preservedProperties) {
                    newState = copyProperty(blockState, newState, property.getName());
                }
            }
            return newState;
        }

        private static IBlockState copyProperty(IBlockState from, IBlockState to, String propertyName) {
            final IProperty<?> fromProp = getPropertyByName(from.getBlock(), propertyName);
            final IProperty<?> toProp = getPropertyByName(to.getBlock(), propertyName);
            if (fromProp == null || toProp == null) return to;
            return copyPropertyGeneric(from, to, fromProp, toProp);
        }

        private static <F extends Comparable<F>, T extends Comparable<T>> IBlockState copyPropertyGeneric(IBlockState from, IBlockState to,
                                                                                                          IProperty<F> fromProp,
                                                                                                          IProperty<T> toProp) {
            final F oldValue = from.getValue(fromProp);
            final T mapped = coerceValue(oldValue, fromProp, toProp);
            return mapped == null ? to : to.withProperty(toProp, mapped);
        }

        private static <F extends Comparable<F>, T extends Comparable<T>> T coerceValue(F oldValue, IProperty<F> fromProp, IProperty<T> toProp) {
            final String srcName = fromProp.getName(oldValue);
            final Optional<T> parsed = toProp.parseValue(srcName);
            if (parsed.isPresent()) return parsed.get();

            final Collection<T> allowed = toProp.getAllowedValues();
            if (toProp.getValueClass().isInstance(oldValue)) {
                final T sameTyped = (T) oldValue;
                if (allowed.contains(sameTyped)) return sameTyped;
            }

            for (T a : allowed) {
                String aName;
                try {
                    aName = toProp.getName(a);
                } catch (Throwable t) {
                    aName = String.valueOf(a);
                }
                if (aName.equals(srcName) || aName.equalsIgnoreCase(srcName) || String.valueOf(a).equals(srcName) ||
                    String.valueOf(a).equalsIgnoreCase(srcName)) {
                    return a;
                }
            }

            if (oldValue instanceof Enum<?> oldEnum) {
                for (T a : allowed) {
                    if (a instanceof Enum<?> tgt && tgt.ordinal() == oldEnum.ordinal()) {
                        return a;
                    }
                }
            }

            if (oldValue instanceof Integer oldInt) {
                for (T a : allowed) {
                    if (a instanceof Integer ai && ai.equals(oldInt)) return a;
                }
            } else if (oldValue instanceof Boolean oldBool) {
                for (T a : allowed) {
                    if (a instanceof Boolean ab && ab.equals(oldBool)) return a;
                }
            }
            return null;
        }


        private static RecipesCommon.MetaBlock chooseRandomOutcome(Tuple<IBlockState, Integer>[] blocks, Random random) {
            if (blocks == null) return null;

            int weight = 0;

            for (Tuple<IBlockState, Integer> choice : blocks) {
                weight += choice.getSecond();
            }

            int r = random.nextInt(weight);

            for (Tuple<IBlockState, Integer> choice : blocks) {
                r -= choice.getSecond();

                if (r <= 0) {
                    return RecipesCommon.metaOf(choice.getFirst());
                }
            }

            return RecipesCommon.metaOf(blocks[0].getFirst());
        }

        public boolean isSolid() {
            return this.isSolid;
        }

        public void write(JsonWriter writer) throws IOException {
            if (blockState != null) {
                writer.name("matchesBlock").value(stateToString(blockState));
                writer.name("matchesState").value(matchState);
            }
            if (matchesOpaque) writer.name("mustBeOpaque").value(true);

            if (matchesMaterial != null) {
                String matName = matNames.inverse().get(matchesMaterial);
                if (matName != null) {
                    writer.name("matchesMaterial").value(matName);
                }
            }
            if (isSolid) writer.name("restrictDepth").value(true);
            if(preservedProperties != null) {
                writer.name("preserveState").value(
                        writePreserveStateArray(preservedProperties).toString()
                );
            }

            if (primaryBlocks != null) {
                writer.name("primarySubstitution");
                writeStateArray(writer, primaryBlocks);
            }
            if (secondaryBlocks != null) {
                writer.name("secondarySubstitutions");
                writeStateArray(writer, secondaryBlocks);
            }

            if (primaryChance != 1D) writer.name("chance").value(primaryChance);

            if (minDist != 0.0D) writer.name("minimumDistancePercent").value(minDist);
            if (maxDist != 100.0D) writer.name("maximumDistancePercent").value(maxDist);
            if (falloffStart != 0.9D) writer.name("falloffStartFactor").value(falloffStart);
        }
    }
}
