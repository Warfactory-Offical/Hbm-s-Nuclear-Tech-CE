package com.hbm.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.api.entity.IResistanceProvider;
import com.hbm.items.ModItems.ArmorSets;
import com.hbm.main.MainRegistry;
import com.hbm.util.Tuple.Quartet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * Basic handling/registry class for our custom resistance stats.
 * Handles resistances for individual armor pieces, full sets as well as entity classes for innate damage resistance
 *
 * @author hbm
 */
public class DamageResistanceHandler {

    public static final Gson gson = new Gson();
    private static final String CATEGORY_EXPLOSION = "EXPL";
    private static final String CATEGORY_FIRE = "FIRE";
    private static final String CATEGORY_PHYSICAL = "PHYS";
    private static final String CATEGORY_ENERGY = "EN";
    private static final HashMap<Item, ResistanceStats> itemStats = new HashMap<>();
    private static final HashMap<Quartet<Item, Item, Item, Item>, ResistanceStats> setStats = new HashMap<>();
    private static final HashMap<Class<? extends Entity>, ResistanceStats> entityStats = new HashMap<>();
    private static final HashMap<Item, List<Quartet<Item, Item, Item, Item>>> itemInfoSet = new HashMap<>();
    private static final EntityEquipmentSlot[] armorSlots = {EntityEquipmentSlot.FEET, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.CHEST,
            EntityEquipmentSlot.HEAD};
    /**
     * Currently cached DT reduction
     */
    public static float currentPDT = 0F;
    /**
     * Currently cached armor piercing %
     */
    public static float currentPDR = 0F; //Norwood: I'll need that elsewhere

    public static void init() {
        File folder = MainRegistry.configHbmDir;

        File config = new File(folder.getAbsolutePath() + File.separatorChar + "hbmArmor.json");
        File template = new File(folder.getAbsolutePath() + File.separatorChar + "_hbmArmor.json");

        clearSystem();

        if (!config.exists()) {
            initDefaults();
            writeDefault(template);
        } else {
            readConfig(config);
        }
    }

    private static void clearSystem() {
        itemStats.clear();
        setStats.clear();
        entityStats.clear();
        itemInfoSet.clear();
    }

    private static void writeDefault(File file) {

        MainRegistry.logger.info("No armor file found, registering defaults for " + file.getName());

        try {
            JsonWriter writer = new JsonWriter(new FileWriter(file));
            writer.setIndent("  ");
            writer.beginObject();
            writer.name("comment").value("Template file, remove the underscore ('_') from the name to enable the config.");

            serialize(writer);

            writer.endObject();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readConfig(File file) {

        MainRegistry.logger.info("Reading armor file " + file.getName());

        try {
            JsonObject json = gson.fromJson(new FileReader(file), JsonObject.class);
            deserialize(json);

        } catch (FileNotFoundException ex) {
            clearSystem();
            initDefaults();
            ex.printStackTrace();
        }
    }

    private static void initDefaults() {

        entityStats.put(EntityCreeper.class, new ResistanceStats().addCategory(CATEGORY_EXPLOSION, 2F, 0.25F));

        itemStats.put(ArmorSets.jackt, new ResistanceStats().addCategory(CATEGORY_PHYSICAL, 5F, 0.5F));
        itemStats.put(ArmorSets.jackt2, new ResistanceStats().addCategory(CATEGORY_PHYSICAL, 5F, 0.5F));

        registerSet(ArmorSets.steel_helmet, ArmorSets.steel_plate, ArmorSets.steel_legs, ArmorSets.steel_boots, new ResistanceStats());
        registerSet(ArmorSets.titanium_helmet, ArmorSets.titanium_plate, ArmorSets.titanium_legs, ArmorSets.titanium_boots, new ResistanceStats());
        registerSet(ArmorSets.alloy_helmet, ArmorSets.alloy_plate, ArmorSets.alloy_legs, ArmorSets.alloy_boots,
                new ResistanceStats().addCategory(CATEGORY_PHYSICAL, 2F, 0.1F));
        registerSet(ArmorSets.cobalt_helmet, ArmorSets.cobalt_plate, ArmorSets.cobalt_legs, ArmorSets.cobalt_boots,
                new ResistanceStats().addCategory(CATEGORY_PHYSICAL, 2F, 0.1F));
        registerSet(ArmorSets.starmetal_helmet, ArmorSets.starmetal_plate, ArmorSets.starmetal_legs, ArmorSets.starmetal_boots,
                new ResistanceStats().addCategory(CATEGORY_PHYSICAL, 3F, 0.25F).setOther(1F, 0.1F));
        registerSet(null, null, ArmorSets.zirconium_legs, null, new ResistanceStats()
                .setOther(0F, 1F)); //What?
                         //MetalloloM: *Yes*
        registerSet(ArmorSets.dnt_helmet, ArmorSets.dnt_plate, ArmorSets.dnt_legs, ArmorSets.dnt_boots, new ResistanceStats());
        registerSet(ArmorSets.cmb_helmet, ArmorSets.cmb_plate, ArmorSets.cmb_legs, ArmorSets.cmb_boots,
                new ResistanceStats().addCategory(CATEGORY_PHYSICAL, 5F, 0.5F).setOther(5F, 0.25F));
        registerSet(ArmorSets.schrabidium_helmet, ArmorSets.schrabidium_plate, ArmorSets.schrabidium_legs, ArmorSets.schrabidium_boots,
                new ResistanceStats().addCategory(CATEGORY_PHYSICAL, 10F, 0.65F).setOther(5F, 0.5F));
        registerSet(ArmorSets.robes_helmet, ArmorSets.robes_plate, ArmorSets.robes_legs, ArmorSets.robes_boots, new ResistanceStats());

        registerSet(ArmorSets.security_helmet, ArmorSets.security_plate, ArmorSets.security_legs, ArmorSets.security_boots,
                new ResistanceStats().addCategory(CATEGORY_PHYSICAL, 5F, 0.5F).addCategory(CATEGORY_EXPLOSION, 2F, 0.25F));
        registerSet(ArmorSets.steamsuit_helmet, ArmorSets.steamsuit_plate, ArmorSets.steamsuit_legs, ArmorSets.steamsuit_boots,
                new ResistanceStats().addCategory(CATEGORY_PHYSICAL, 2F, 0.15F).addCategory(CATEGORY_FIRE, 0.5F, 0.25F).addExact(DamageSource.FALL.getDamageType(), 5F, 0.25F).setOther(0F, 0.1F));
        registerSet(ArmorSets.dieselsuit_helmet, ArmorSets.dieselsuit_plate, ArmorSets.dieselsuit_legs, ArmorSets.dieselsuit_boots, new
        ResistanceStats()
                .addCategory(CATEGORY_PHYSICAL, 1F, 0.15F)
                .addCategory(CATEGORY_FIRE, 0.5F, 0.5F)
                .addCategory(CATEGORY_EXPLOSION, 2F, 0.15F)
                .setOther(0F, 0.1F));
        registerSet(ArmorSets.ajr_helmet, ArmorSets.ajr_plate, ArmorSets.ajr_legs, ArmorSets.ajr_boots,
                new ResistanceStats().addCategory(CATEGORY_PHYSICAL, 4F, 0.15F).addCategory(CATEGORY_FIRE, 0.5F, 0.35F).addCategory(CATEGORY_EXPLOSION, 7.5F, 0.25F).addExact(DamageSource.FALL.getDamageType(), 0F, 1F).setOther(0F, 0.15F));
        registerSet(ArmorSets.ajro_helmet, ArmorSets.ajro_plate, ArmorSets.ajro_legs, ArmorSets.ajro_boots,
                new ResistanceStats().addCategory(CATEGORY_PHYSICAL, 4F, 0.15F).addCategory(CATEGORY_FIRE, 0.5F, 0.35F).addCategory(CATEGORY_EXPLOSION, 7.5F, 0.25F).addExact(DamageSource.FALL.getDamageType(), 0F, 1F).setOther(0F, 0.15F));
        registerSet(ArmorSets.rpa_helmet, ArmorSets.rpa_plate, ArmorSets.rpa_legs, ArmorSets.rpa_boots,
                new ResistanceStats().addCategory(CATEGORY_PHYSICAL, 25F, 0.65F).addCategory(CATEGORY_FIRE, 10F, 0.9F).addCategory(CATEGORY_EXPLOSION, 15F, 0.25F).addCategory(CATEGORY_ENERGY, 25F, 0.75F).addExact(DamageSource.FALL.getDamageType(), 0F, 1F).addExact(DamageClass.LASER.name(), 10F, 0.75F).setOther(15F, 0.3F));
        ResistanceStats bj =
                new ResistanceStats().addCategory(CATEGORY_PHYSICAL, 5F, 0.5F).addCategory(CATEGORY_FIRE, 2.5F, 0.5F).addCategory(CATEGORY_EXPLOSION, 10F, 0.25F).addExact(DamageSource.FALL.getDamageType(), 0F, 1F).setOther(2F, 0.15F);
        registerSet(ArmorSets.bj_helmet, ArmorSets.bj_plate, ArmorSets.bj_legs, ArmorSets.bj_boots, bj);
        registerSet(ArmorSets.bj_helmet, ArmorSets.bj_plate_jetpack, ArmorSets.bj_legs, ArmorSets.bj_boots, bj);
        registerSet(ArmorSets.envsuit_helmet, ArmorSets.envsuit_plate, ArmorSets.envsuit_legs, ArmorSets.envsuit_boots, new ResistanceStats()
                .addCategory(CATEGORY_FIRE, 2F, 0.75F)
                .addExact(DamageSource.DROWN.getDamageType(), 0F, 1F)
                .addExact(DamageSource.FALL.getDamageType(), 5F, 0.75F)
                .setOther(0F, 0.1F));
        registerSet(ArmorSets.hev_helmet, ArmorSets.hev_plate, ArmorSets.hev_legs, ArmorSets.hev_boots,
                new ResistanceStats().addCategory(CATEGORY_PHYSICAL, 2F, 0.25F).addCategory(CATEGORY_FIRE, 0.5F, 0.5F).addCategory(CATEGORY_EXPLOSION, 5F, 0.25F).addExact(DamageSource.ON_FIRE.getDamageType(), 0F, 1F).addExact(DamageSource.FALL.getDamageType(), 10F, 0F).setOther(2F, 0.25F));
        registerSet(ArmorSets.bismuth_helmet, ArmorSets.bismuth_plate, ArmorSets.bismuth_legs, ArmorSets.bismuth_boots, new ResistanceStats()
                .addCategory(CATEGORY_PHYSICAL, 2F, 0.15F)
                .addCategory(CATEGORY_FIRE, 5F, 0.5F)
                .addCategory(CATEGORY_EXPLOSION, 5F, 0.25F)
                .addExact(DamageSource.FALL.getDamageType(), 0F, 1F)
                .setOther(2F, 0.25F));
        registerSet(ArmorSets.fau_helmet, ArmorSets.fau_plate, ArmorSets.fau_legs, ArmorSets.fau_boots,
                new ResistanceStats().addCategory(CATEGORY_EXPLOSION, 50F, 0.95F).addCategory(CATEGORY_FIRE, 0F, 1F).addExact(DamageClass.LASER.name(), 25F, 0.95F).addExact(DamageSource.FALL.getDamageType(), 0F, 1F).setOther(100F, 0.99F));
        registerSet(ArmorSets.dns_helmet, ArmorSets.dns_plate, ArmorSets.dns_legs, ArmorSets.dns_boots,
                new ResistanceStats().addCategory(CATEGORY_EXPLOSION, 100F, 0.99F).addCategory(CATEGORY_FIRE, 0F, 1F).setOther(1000F, 1F));
        registerSet(ArmorSets.taurun_helmet, ArmorSets.taurun_plate, ArmorSets.taurun_legs, ArmorSets.taurun_boots, new ResistanceStats()
                .addCategory(CATEGORY_PHYSICAL, 2F, 0.15F)
                .addCategory(CATEGORY_FIRE, 0F, 0.25F)
                .addCategory(CATEGORY_EXPLOSION, 0F, 0.25F)
                .addExact(DamageSource.FALL.getDamageType(), 4F, 0.5F)
                .setOther(2F, 0.1F));
        registerSet(ArmorSets.t51_helmet, ArmorSets.t51_plate, ArmorSets.t51_legs, ArmorSets.t51_boots, new ResistanceStats()
                .addCategory(CATEGORY_PHYSICAL, 2F, 0.15F)
                .addCategory(CATEGORY_FIRE, 0.5F, 0.35F)
                .addCategory(CATEGORY_EXPLOSION, 5F, 0.25F)
                .addExact(DamageSource.FALL.damageType, 0F, 1F)
                .setOther(0F, 0.1F));
        registerSet(ArmorSets.trenchmaster_helmet, ArmorSets.trenchmaster_plate, ArmorSets.trenchmaster_legs, ArmorSets.trenchmaster_boots, new
        ResistanceStats()
                .addCategory(CATEGORY_PHYSICAL, 5F, 0.5F)
                .addCategory(CATEGORY_FIRE, 5F, 0.5F)
                .addCategory(CATEGORY_EXPLOSION, 5F, 0.25F)
                .addExact(DamageClass.LASER.name(), 15F, 0.9F)
                .addExact(DamageSource.FALL.getDamageType(), 10F, 0.5F)
                .setOther(5F, 0.25F));

        registerSet(ArmorSets.euphemium_helmet, ArmorSets.euphemium_plate, ArmorSets.euphemium_legs, ArmorSets.euphemium_boots,
                new ResistanceStats().setOther(1_000_000F, 1F));

        registerSet(ArmorSets.hazmat_helmet, ArmorSets.hazmat_plate, ArmorSets.hazmat_legs, ArmorSets.hazmat_boots, new ResistanceStats());
        registerSet(ArmorSets.hazmat_helmet_red, ArmorSets.hazmat_plate_red, ArmorSets.hazmat_legs_red, ArmorSets.hazmat_boots_red,
                new ResistanceStats());
        registerSet(ArmorSets.hazmat_helmet_grey, ArmorSets.hazmat_plate_grey, ArmorSets.hazmat_legs_grey, ArmorSets.hazmat_boots_grey,
                new ResistanceStats());
        registerSet(ArmorSets.liquidator_helmet, ArmorSets.liquidator_plate, ArmorSets.liquidator_legs, ArmorSets.liquidator_boots,
                new ResistanceStats());
        registerSet(ArmorSets.hazmat_paa_helmet, ArmorSets.hazmat_paa_plate, ArmorSets.hazmat_paa_legs, ArmorSets.hazmat_paa_boots,
                new ResistanceStats());
        registerSet(ArmorSets.asbestos_helmet, ArmorSets.asbestos_plate, ArmorSets.asbestos_legs, ArmorSets.asbestos_boots,
                new ResistanceStats().addCategory(CATEGORY_FIRE, 10F, 0.9F));
    }

    private static void registerSet(Item helmet, Item plate, Item legs, Item boots, ResistanceStats stats) {
        Quartet set = new Quartet(helmet, plate, legs, boots);
        setStats.put(set, stats);
        if (helmet != null) addToListInHashMap(helmet, itemInfoSet, set);
        if (plate != null) addToListInHashMap(plate, itemInfoSet, set);
        if (legs != null) addToListInHashMap(legs, itemInfoSet, set);
        if (boots != null) addToListInHashMap(boots, itemInfoSet, set);
    }

    private static void addToListInHashMap(Object key, HashMap map, Object listElement) {
        List list = (List) map.get(key);
        if (list == null) {
            list = new ArrayList();
            map.put(key, list);
        }
        list.add(listElement);
    }

    public static void addInfo(ItemStack stack, List desc) {
        if (stack == null || stack.getItem() == null) return;

        if (itemInfoSet.containsKey(stack.getItem())) {
            List<Quartet<Item, Item, Item, Item>> sets = itemInfoSet.get(stack.getItem());

            for (Quartet<Item, Item, Item, Item> set : sets) {

                ResistanceStats stats = setStats.get(set);
                if (stats == null) continue;

                List toAdd = new ArrayList();

                for (Entry<String, Resistance> entry : stats.categoryResistances.entrySet()) {
                    toAdd.add(I18nUtil.resolveKey("damage.category." + entry.getKey()) + ": " + entry.getValue().threshold + "/" + ((int) (entry.getValue().resistance * 100)) + "%");
                }
                for (Entry<String, Resistance> entry : stats.exactResistances.entrySet()) {
                    toAdd.add(I18nUtil.resolveKey("damage.exact." + entry.getKey()) + ": " + entry.getValue().threshold + "/" + ((int) (entry.getValue().resistance * 100)) + "%");
                }
                if (stats.otherResistance != null)
                    toAdd.add(I18nUtil.resolveKey("damage.other") + ": " + stats.otherResistance.threshold + "/" + ((int) (stats.otherResistance.resistance * 100)) + "%");

                if (!toAdd.isEmpty()) {
                    desc.add(TextFormatting.DARK_PURPLE + I18nUtil.resolveKey("damage.inset"));
                    //this sucks ass!
                    if (set.getW() != null) desc.add(TextFormatting.DARK_PURPLE + "  " + new ItemStack(set.getW()).getDisplayName());
                    if (set.getX() != null) desc.add(TextFormatting.DARK_PURPLE + "  " + new ItemStack(set.getX()).getDisplayName());
                    if (set.getY() != null) desc.add(TextFormatting.DARK_PURPLE + "  " + new ItemStack(set.getY()).getDisplayName());
                    if (set.getZ() != null) desc.add(TextFormatting.DARK_PURPLE + "  " + new ItemStack(set.getZ()).getDisplayName());
                    desc.addAll(toAdd);
                }

                break; //TEMP, only show one set for now
            }
        }

        if (itemStats.containsKey(stack.getItem())) {
            ResistanceStats stats = itemStats.get(stack.getItem());

            List toAdd = new ArrayList();

            for (Entry<String, Resistance> entry : stats.categoryResistances.entrySet()) {
                toAdd.add(I18nUtil.resolveKey("damage.category." + entry.getKey()) + ": " + entry.getValue().threshold + "/" + ((int) (entry.getValue().resistance * 100)) + "%");
            }
            for (Entry<String, Resistance> entry : stats.exactResistances.entrySet()) {
                toAdd.add(I18nUtil.resolveKey("damage.exact." + entry.getKey()) + ": " + entry.getValue().threshold + "/" + ((int) (entry.getValue().resistance * 100)) + "%");
            }
            if (stats.otherResistance != null)
                toAdd.add(I18nUtil.resolveKey("damage.other") + ": " + stats.otherResistance.threshold + "/" + ((int) (stats.otherResistance.resistance * 100)) + "%");

            if (!toAdd.isEmpty()) {
                desc.add(TextFormatting.DARK_PURPLE + I18nUtil.resolveKey("damage.item"));
                desc.addAll(toAdd);
            }
        }
    }

    public static void serialize(JsonWriter writer) throws IOException {
        /// ITEMS ///
        writer.name("itemStats").beginArray();
        for (Entry<Item, ResistanceStats> entry : itemStats.entrySet()) {
            writer.beginArray().setIndent("");
            writer.value(Item.REGISTRY.getNameForObject(entry.getKey()).toString()).setIndent("  ");
            writer.beginObject();
            entry.getValue().serialize(writer);
            writer.setIndent("");
            writer.endObject().endArray().setIndent("  ");
        }
        writer.endArray();

        /// SETS ///
        writer.name("setStats").beginArray();
        for (Entry<Quartet<Item, Item, Item, Item>, ResistanceStats> entry : setStats.entrySet()) {
            writer.beginArray().setIndent("");

            Quartet<Item, Item, Item, Item> key = entry.getKey();
            Item helmet = key.getW();
            Item plate = key.getX();
            Item legs = key.getY();
            Item boots = key.getZ();

            writer.value(helmet == null ? null : Item.REGISTRY.getNameForObject(helmet).toString());
            writer.value(plate == null ? null : Item.REGISTRY.getNameForObject(plate).toString());
            writer.value(legs == null ? null : Item.REGISTRY.getNameForObject(legs).toString());
            writer.value(boots == null ? null : Item.REGISTRY.getNameForObject(boots).toString());

            writer.setIndent("  ");
            writer.beginObject();
            entry.getValue().serialize(writer);
            writer.setIndent("");
            writer.endObject().endArray().setIndent("  ");
        }
        writer.endArray();

        /// ENTITIES ///
        writer.name("entityStats").beginArray();
        for (Entry<Class<? extends Entity>, ResistanceStats> entry : entityStats.entrySet()) {
            writer.beginArray().setIndent("");
            writer.value(entry.getKey().getName()).setIndent("  ");
            writer.beginObject();
            entry.getValue().serialize(writer);
            writer.setIndent("");
            writer.endObject().endArray().setIndent("  ");
        }
        writer.endArray();
    }

    public static void deserialize(JsonObject json) {
        /// ITEMS ///
        JsonArray itemStatsArray = json.get("itemStats").getAsJsonArray();
        for (JsonElement element : itemStatsArray) {
            JsonArray statArray = element.getAsJsonArray();
            Item item = Item.REGISTRY.getObject(new ResourceLocation(statArray.get(0).getAsString()));
            JsonObject stats = statArray.get(1).getAsJsonObject();
            itemStats.put(item, ResistanceStats.deserialize(stats));
        }

        /// SETS ///
        JsonArray setStatsArray = json.get("setStats").getAsJsonArray();
        for (JsonElement element : setStatsArray) {
            JsonArray statArray = element.getAsJsonArray();
            Item helmet = statArray.get(0).isJsonNull() ? null : Item.REGISTRY.getObject(new ResourceLocation(statArray.get(0).getAsString()));
            Item plate = statArray.get(1).isJsonNull() ? null : Item.REGISTRY.getObject(new ResourceLocation(statArray.get(1).getAsString()));
            Item legs = statArray.get(2).isJsonNull() ? null : Item.REGISTRY.getObject(new ResourceLocation(statArray.get(2).getAsString()));
            Item boots = statArray.get(3).isJsonNull() ? null : Item.REGISTRY.getObject(new ResourceLocation(statArray.get(3).getAsString()));
            JsonObject stats = statArray.get(4).getAsJsonObject();
            registerSet(helmet, plate, legs, boots, ResistanceStats.deserialize(stats));
        }

        /// ENTITIES ///
        JsonArray entityStatsArray = json.get("entityStats").getAsJsonArray();
        for (JsonElement element : entityStatsArray) {
            JsonArray statArray = element.getAsJsonArray();
            try {
                Class clazz = Class.forName(statArray.get(0).getAsString());
                JsonObject stats = statArray.get(1).getAsJsonObject();
                entityStats.put(clazz, ResistanceStats.deserialize(stats));
            } catch (ClassNotFoundException e) {
            }
        }
    }

    public static void setup(float dt, float dr) {
        currentPDT = dt;
        currentPDR = dr;
    }

    public static void reset() {
        currentPDT = 0;
        currentPDR = 0;
    }

    @SubscribeEvent
    public void onEntityAttacked(LivingAttackEvent event) {
        if (event.getSource().isDamageAbsolute()) return;

        EntityLivingBase e = event.getEntityLiving();
        float amount = event.getAmount();

        float[] vals = getDTDR(e, event.getSource(), amount, currentPDT, currentPDR);
        float dt = vals[0] - currentPDT;
        float dr = vals[1] - currentPDR;

        if ((dt > 0 && dt >= event.getAmount()) || dr >= 1F) {
            event.setCanceled(true);
            EntityDamageUtil.damageArmorNT(e, amount);
        }
    }

    @SubscribeEvent
    public void onEntityHurt(LivingHurtEvent event) {
        event.setAmount(calculateDamage(event.getEntityLiving(), event.getSource(), event.getAmount(), currentPDT, currentPDR));
        if (event.getEntityLiving() instanceof IResistanceProvider irp) {
            irp.onDamageDealt(event.getSource(), event.getAmount());
        }
    }

    private static String typeToCategory(DamageSource source) {
        if (source.isExplosion()) return CATEGORY_EXPLOSION;
        if (source.isFireDamage()) return CATEGORY_FIRE;
        if (source.isProjectile()) return CATEGORY_PHYSICAL;
        if (source.getDamageType().equals(DamageClass.LASER.name())) return CATEGORY_ENERGY;
        if (source.getDamageType().equals(DamageClass.MICROWAVE.name())) return CATEGORY_ENERGY;
        if (source.getDamageType().equals(DamageClass.SUBATOMIC.name())) return CATEGORY_ENERGY;
        if (source.getDamageType().equals(DamageClass.ELECTRIC.name())) return CATEGORY_ENERGY;
        if (source == DamageSource.CACTUS) return CATEGORY_PHYSICAL;
        if (source instanceof EntityDamageSource) return CATEGORY_PHYSICAL;
        return source.getDamageType();
    }

    private static float calculateDamage(EntityLivingBase entity, DamageSource damage, float amount, float pierceDT, float pierce) {
        if (damage.isDamageAbsolute()) return amount;

        float[] vals = getDTDR(entity, damage, amount, pierceDT, pierce);
        float dt = vals[0];
        float dr = vals[1];

        dt = Math.max(0F, dt - pierceDT);
        if (dt >= amount) return 0F;
        amount -= dt;
        dr *= MathHelper.clamp(1F - pierce, 0F, 2F /* we allow up to -1 armor piercing, which can double effective armor values */);

        return amount *= (1F - dr);
    }

    private static float[] getDTDR(EntityLivingBase entity, DamageSource damage, float amount, float pierceDT, float pierce) {

        float dt = 0;
        float dr = 0;

        if (entity instanceof IResistanceProvider irp) {
            float[] res = irp.getCurrentDTDR(damage, amount, pierceDT, pierce);
            dt += res[0];
            dr += res[1];
        }

        /// SET HANDLING ///
        Quartet<Item, Item, Item, Item> wornSet = new Quartet<>(entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty() ? null :
                entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem(), entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty() ?
                null : entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem(),
                entity.getItemStackFromSlot(EntityEquipmentSlot.LEGS).isEmpty() ? null :
                        entity.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem(),
                entity.getItemStackFromSlot(EntityEquipmentSlot.FEET).isEmpty() ? null :
                        entity.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem());

        ResistanceStats setResistance = setStats.get(wornSet);
        if (setResistance != null) {
            Resistance res = setResistance.getResistance(damage);
            if (res != null) {
                dt += res.threshold;
                dr += res.resistance;
            }
        }

        /// ARMOR ///
        for (EntityEquipmentSlot slot : armorSlots) {
            ItemStack armor = entity.getItemStackFromSlot(slot);
            if (armor.isEmpty()) continue;
            ResistanceStats stats = itemStats.get(armor.getItem());
            if (stats == null) continue;
            Resistance res = stats.getResistance(damage);
            if (res == null) continue;
            dt += res.threshold;
            dr += res.resistance;
        }

        /// ENTITY CLASS HANDLING ///
        ResistanceStats innateResistance = entityStats.get(entity.getClass());
        if (innateResistance != null) {
            Resistance res = innateResistance.getResistance(damage);
            if (res != null) {
                dt += res.threshold;
                dr += res.resistance;
            }
        }

        return new float[]{dt, dr};
    }

    public enum DamageClass {
        PHYSICAL, FIRE, EXPLOSIVE, ELECTRIC, LASER, MICROWAVE, SUBATOMIC, OTHER
    }

    public static class ResistanceStats {

        HashMap<String, Resistance> exactResistances = new HashMap<>();
        HashMap<String, Resistance> categoryResistances = new HashMap<>();
        Resistance otherResistance;

        public static ResistanceStats deserialize(JsonObject json) {
            ResistanceStats stats = new ResistanceStats();

            if (json.has("exact")) {
                JsonArray exact = json.get("exact").getAsJsonArray();
                for (JsonElement element : exact) {
                    JsonArray array = element.getAsJsonArray();
                    stats.exactResistances.put(array.get(0).getAsString(), new Resistance(array.get(1).getAsFloat(), array.get(2).getAsFloat()));
                }
            }

            if (json.has("category")) {
                JsonArray category = json.get("category").getAsJsonArray();
                for (JsonElement element : category) {
                    JsonArray array = element.getAsJsonArray();
                    stats.categoryResistances.put(array.get(0).getAsString(), new Resistance(array.get(1).getAsFloat(), array.get(2).getAsFloat()));
                }
            }

            if (json.has("other")) {
                JsonArray other = json.get("other").getAsJsonArray();
                stats.otherResistance = new Resistance(other.get(0).getAsFloat(), other.get(1).getAsFloat());
            }

            return stats;
        }

        Resistance getResistance(DamageSource source) {
            Resistance exact = exactResistances.get(source.getDamageType());
            if (exact != null) return exact;
            Resistance category = categoryResistances.get(typeToCategory(source));
            if (category != null) return category;
            return source.isUnblockable() ? null : otherResistance;
        }

        ResistanceStats addExact(String type, float threshold, float resistance) {
            exactResistances.put(type, new Resistance(threshold, resistance));
            return this;
        }

        ResistanceStats addCategory(String type, float threshold, float resistance) {
            categoryResistances.put(type, new Resistance(threshold, resistance));
            return this;
        }

        ResistanceStats setOther(float threshold, float resistance) {
            otherResistance = new Resistance(threshold, resistance);
            return this;
        }

        public void serialize(JsonWriter writer) throws IOException {

            if (!exactResistances.isEmpty()) {
                writer.name("exact").beginArray();
                for (Entry<String, Resistance> entry : exactResistances.entrySet()) {
                    writer.beginArray().setIndent("");
                    writer.value(entry.getKey()).value(entry.getValue().threshold).value(entry.getValue().resistance).endArray().setIndent("  ");
                }
                writer.endArray();
            }

            if (!categoryResistances.isEmpty()) {
                writer.name("category").beginArray();
                for (Entry<String, Resistance> entry : categoryResistances.entrySet()) {
                    writer.beginArray().setIndent("");
                    writer.value(entry.getKey()).value(entry.getValue().threshold).value(entry.getValue().resistance).endArray().setIndent("  ");
                }
                writer.endArray();
            }

            if (otherResistance != null) {
                writer.name("other").beginArray().setIndent("");
                writer.value(otherResistance.threshold).value(otherResistance.resistance).endArray().setIndent("  ");
            }
        }
    }

    public static class Resistance {

        public float threshold;
        public float resistance;

        public Resistance(float threshold, float resistance) {
            this.threshold = threshold;
            this.resistance = resistance;
        }
    }
}
