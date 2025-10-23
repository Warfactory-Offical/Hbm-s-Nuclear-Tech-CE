package com.hbm.util;

import com.hbm.entity.mob.ai.EntityAIFireGun;
import com.hbm.handler.ArmorUtil;
import com.hbm.items.ModItems;
import com.hbm.items.ModItems.ArmorSets;
import com.hbm.items.ModItems.Armory;
import com.hbm.items.ModItems.ToolSets;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandom;

import java.util.*;

public class MobUtil {
    public static Map<Integer, List<WeightedRandomObject>> slotPoolCommon = new HashMap<>();
    public static Map<Integer, List<WeightedRandomObject>> slotPoolRanged = new HashMap<>();

    public static Map<Integer, List<WeightedRandomObject>> slotPoolAdv = new HashMap<>();
    public static Map<Integer, List<WeightedRandomObject>> slotPoolAdvRanged;
    /**Unlike the above two, the Double is interpreted as minimum soot level, instead of armor slot **/
    public static HashMap<Double, List<WeightedRandomObject>> slotPoolGuns = new HashMap<>();

    //slop pools
    public static Map<Integer, List<WeightedRandomObject>> slotPoolGunsTier1 = new HashMap<>();
    public static Map<Integer, List<WeightedRandomObject>> slotPoolGunsTier2 = new HashMap<>();
    public static Map<Integer, List<WeightedRandomObject>> slotPoolGunsTier3 = new HashMap<>();
    public static Map<Integer, List<WeightedRandomObject>> slotPoolMasks = new HashMap<>();
    public static Map<Integer, List<WeightedRandomObject>> slotPoolHelms = new HashMap<>();
    public static Map<Integer, List<WeightedRandomObject>> slotPoolTierArmor = new HashMap<>();
    public static Map<Integer, List<WeightedRandomObject>> slotPoolMelee = new HashMap<>();


    public static void intializeMobPools(){
        slotPoolCommon.put(4, createSlotPool(8000, new Object[][]{ //new slots, smooth, brushed, no wrinkles // old slots, wrinkled, rusty, not smooth
                {ModItems.gas_mask_m65, 16}, {ModItems.gas_mask_olde, 12}, {ToolSets.mask_of_infamy, 8},
                {ModItems.gas_mask_mono, 8}, {ArmorSets.robes_helmet, 32}, {ModItems.no9, 16},
                {ArmorSets.cobalt_helmet, 2}, {ModItems.rag_piss, 1}, {ModItems.hat, 1}, {ArmorSets.alloy_helmet, 2},
                {ArmorSets.titanium_helmet, 4}, {ArmorSets.steel_helmet, 8}
        }));
        slotPoolCommon.put(3, createSlotPool(7000, new Object[][]{
                {ArmorSets.starmetal_plate, 1}, {ArmorSets.cobalt_plate, 2}, {ArmorSets.robes_plate, 32},
                {ArmorSets.jackt, 32}, {ArmorSets.jackt2, 32}, {ArmorSets.alloy_plate, 2},
                {ArmorSets.steel_plate, 2}
        }));
        slotPoolCommon.put(2, createSlotPool(7000, new Object[][]{
                {ArmorSets.zirconium_legs, 1}, {ArmorSets.cobalt_legs, 2}, {ArmorSets.steel_legs, 16},
                {ArmorSets.titanium_legs, 8}, {ArmorSets.robes_legs, 32}, {ArmorSets.alloy_legs, 2}
        }));
        slotPoolCommon.put(1, createSlotPool(7000, new Object[][]{
                {ArmorSets.robes_boots, 32}, {ArmorSets.steel_boots, 16}, {ArmorSets.cobalt_boots, 2}, {ArmorSets.alloy_boots, 2}
        }));
        slotPoolCommon.put(0, createSlotPool(10000, new Object[][]{
                {ToolSets.pipe_lead, 30}, {ToolSets.crowbar, 25}, {ModItems.geiger_counter, 20},
                {ToolSets.reer_graar, 16}, {ToolSets.steel_pickaxe, 12}, {ToolSets.stopsign, 10},
                {ToolSets.sopsign, 8}, {ToolSets.chernobylsign, 6}, {ToolSets.steel_sword, 15},
                {ToolSets.titanium_sword, 8}, {ModItems.lead_gavel, 4}, {ModItems.wrench_flipped, 2},
                {ModItems.wrench, 20}
        }));

        slotPoolRanged.put(4, createSlotPool(12000, new Object[][]{
                {ModItems.gas_mask_m65, 16}, {ModItems.gas_mask_olde, 12}, {ToolSets.mask_of_infamy, 8},
                {ModItems.gas_mask_mono, 8}, {ArmorSets.robes_helmet, 32}, {ModItems.no9, 16},
                {ModItems.rag_piss, 1}, {ModItems.goggles, 1}, {ArmorSets.alloy_helmet, 2},
                {ArmorSets.titanium_helmet, 4}, {ArmorSets.steel_helmet, 8}
        }));
        slotPoolRanged.put(3, createSlotPool(10000, new Object[][]{
                {ArmorSets.starmetal_plate, 1}, {ArmorSets.cobalt_plate, 2}, {ArmorSets.alloy_plate, 2}, //sadly they cant wear jackets bc it breaks it
                {ArmorSets.steel_plate, 8}, {ArmorSets.titanium_plate, 4}
        }));
        slotPoolRanged.put(2, createSlotPool(10000, new Object[][]{
                {ArmorSets.zirconium_legs, 1}, {ArmorSets.cobalt_legs, 2}, {ArmorSets.steel_legs, 16},
                {ArmorSets.titanium_legs, 8}, {ArmorSets.robes_legs, 32}, {ArmorSets.alloy_legs, 2},
        }));
        slotPoolRanged.put(1, createSlotPool(10000, new Object[][]{
                {ArmorSets.robes_boots, 32}, {ArmorSets.steel_boots, 16}, {ArmorSets.cobalt_boots, 2}, {ArmorSets.alloy_boots, 2},
                {ArmorSets.titanium_boots, 6}
        }));

        slotPoolGuns.put(0.3, createSlotPool(new Object[][]{
                {Armory.gun_light_revolver, 16}, {Armory.gun_greasegun, 8}, {Armory.gun_maresleg, 2}
        }));
        slotPoolGuns.put(1D, createSlotPool(new Object[][]{
                {Armory.gun_light_revolver, 6}, {Armory.gun_greasegun, 8}, {Armory.gun_maresleg, 4}, {Armory.gun_henry, 6}
        }));
        slotPoolGuns.put(3D, createSlotPool(new Object[][]{
                {Armory.gun_uzi, 10}, {Armory.gun_maresleg, 8}, {Armory.gun_henry, 12}, {Armory.gun_heavy_revolver, 4}, {Armory.gun_flaregun, 2}
        }));
        slotPoolGuns.put(5D, createSlotPool(new Object[][]{
                {Armory.gun_am180, 6}, {Armory.gun_uzi, 10}, {Armory.gun_spas12, 8}, {Armory.gun_henry_lincoln, 2}, {Armory.gun_heavy_revolver, 12}, {Armory.gun_flaregun, 4}, {Armory.gun_flamer, 2}
        }));

        slotPoolAdv.put(4, createSlotPool(new Object[][]{
                {ArmorSets.security_helmet, 10}, {ArmorSets.t51_helmet, 4}, {ArmorSets.asbestos_helmet, 12},
                {ArmorSets.liquidator_helmet, 4}, {ModItems.no9, 12},
                {ArmorSets.hazmat_helmet, 6}
        }));
        slotPoolAdv.put(3, createSlotPool(new Object[][]{
                {ArmorSets.liquidator_plate, 4}, {ArmorSets.security_plate, 8}, {ArmorSets.asbestos_plate, 12},
                {ArmorSets.t51_plate, 4}, {ArmorSets.hazmat_plate, 6},
                {ArmorSets.steel_plate, 8}
        }));
        slotPoolAdv.put(2, createSlotPool(new Object[][]{
                {ArmorSets.liquidator_legs, 4}, {ArmorSets.security_legs, 8}, {ArmorSets.asbestos_legs, 12},
                {ArmorSets.t51_legs, 4}, {ArmorSets.hazmat_legs, 6},
                {ArmorSets.steel_legs, 8}
        }));
        slotPoolAdv.put(1, createSlotPool(new Object[][]{
                {ArmorSets.liquidator_boots, 4}, {ArmorSets.security_boots, 8}, {ArmorSets.asbestos_boots, 12},
                {ArmorSets.t51_boots, 4}, {ArmorSets.hazmat_boots, 6},
                {ArmorSets.robes_boots, 8}
        }));
        slotPoolAdv.put(0, createSlotPool(new Object[][]{
                {ToolSets.pipe_lead, 20}, {ToolSets.crowbar, 30}, {ModItems.geiger_counter, 20},
                {ToolSets.reer_graar, 20}, {ModItems.wrench_flipped, 12}, {ToolSets.stopsign, 16},
                {ToolSets.sopsign, 4}, {ToolSets.chernobylsign, 16},
                {ToolSets.titanium_sword, 18}, {ModItems.lead_gavel, 8},
                {ModItems.wrench, 20}
        }));

        //For action block
        slotPoolGunsTier1.put(0, createSlotPool(0, new Object[][]{
                {Armory.gun_light_revolver, 16}, {Armory.gun_greasegun, 8}, {Armory.gun_maresleg, 2}
        }));

        slotPoolGunsTier2.put(0, createSlotPool(0, new Object[][]{
                {Armory.gun_uzi, 10}, {Armory.gun_maresleg, 8}, {Armory.gun_henry, 12}, {Armory.gun_heavy_revolver, 4}, {Armory.gun_flaregun, 4}, {Armory.gun_carbine, 4}
        }));

        slotPoolGunsTier3.put(0, createSlotPool(0, new Object[][]{
                {Armory.gun_uzi, 25}, {Armory.gun_spas12, 20}, {Armory.gun_carbine, 20}, {Armory.gun_g3, 10}, {Armory.gun_am180, 5}, {Armory.gun_stg77, 5}
        }));

        slotPoolMasks.put(4, createSlotPool(0, new Object[][]{
                {ModItems.gas_mask_m65, 16}, {ModItems.gas_mask_mono, 8}, {ArmorSets.robes_helmet, 32}, {ModItems.no9, 16},
                {ModItems.rag_piss, 4}, {ModItems.goggles, 12}
        }));

        slotPoolHelms.put(4, createSlotPool(0, new Object[][]{
                {ModItems.gas_mask_m65, 16}, {ModItems.gas_mask_olde, 12}, {ToolSets.mask_of_infamy, 8},
                {ModItems.gas_mask_mono, 8}, {ArmorSets.robes_helmet, 32}, {ModItems.no9, 16},
                {ArmorSets.cobalt_helmet, 2}, {ModItems.hat, 1}, {ArmorSets.alloy_helmet, 2},
                {ArmorSets.titanium_helmet, 4}, {ArmorSets.steel_helmet, 8}
        }));

        slotPoolTierArmor.put(4, createSlotPool(new Object[][]{
                {ModItems.gas_mask_m65, 20},
                {ModItems.gas_mask_olde, 15},
                {ArmorSets.steel_helmet, 25},
                {ArmorSets.titanium_helmet, 15},
                {ArmorSets.alloy_helmet, 10},
        }));

        slotPoolTierArmor.put(3, createSlotPool(new Object[][]{
                {ArmorSets.steel_plate, 30},
                {ArmorSets.titanium_plate, 20},
                {ArmorSets.alloy_plate, 15},
                {ArmorSets.cobalt_plate, 5},
                {ArmorSets.starmetal_plate, 5}
        }));

        slotPoolTierArmor.put(2, createSlotPool(new Object[][]{
                {ArmorSets.steel_legs, 30},
                {ArmorSets.titanium_legs, 20},
                {ArmorSets.alloy_legs, 15},
                {ArmorSets.cobalt_legs, 5},
                {ArmorSets.zirconium_legs, 5}
        }));

        slotPoolTierArmor.put(1, createSlotPool(new Object[][]{
                {ArmorSets.steel_boots, 30},
                {ArmorSets.robes_boots, 25},
                {ArmorSets.titanium_boots, 20},
                {ArmorSets.alloy_boots, 15},
                {ArmorSets.hazmat_boots, 10},
                {ArmorSets.cobalt_boots, 5},
        }));

        slotPoolMelee.put(0, createSlotPool(2000, new Object[][]{
                {ToolSets.pipe_lead, 40}, {ToolSets.crowbar, 35}, {ModItems.wrench, 30},
                {ToolSets.steel_sword, 25}, {ToolSets.titanium_sword, 20},
                {ToolSets.reer_graar, 20}, {ToolSets.stopsign, 15},
                {ModItems.lead_gavel, 12}, {ModItems.wrench_flipped, 10},
                {ToolSets.sopsign, 8}, {ToolSets.chernobylsign, 8}
        }));

        slotPoolAdvRanged = new HashMap<>(slotPoolAdv);
        slotPoolAdvRanged.remove(0);

    }

    public static List<WeightedRandomObject> createSlotPool(int nullWeight, Object[][] items) {
        List<WeightedRandomObject> pool = new ArrayList<>();
        pool.add(new WeightedRandomObject(null, nullWeight));
        for (Object[] item : items) {
            Object obj = item[0];
            int weight = (int) item[1];

            if (obj instanceof Item) {
                pool.add(new WeightedRandomObject(new ItemStack((Item) obj), weight));
            } else if (obj instanceof ItemStack) {		//lol just make it pass ItemStack aswell
                pool.add(new WeightedRandomObject(obj, weight));
            }
        }
        return pool;
    }
    public static List<WeightedRandomObject> createSlotPool(Object[][] items) {
        List<WeightedRandomObject> pool = new ArrayList<>();
        for (Object[] item : items) {
            Object obj = item[0];
            int weight = (int) item[1];

            if (obj instanceof Item) {
                pool.add(new WeightedRandomObject(new ItemStack((Item) obj), weight));
            } else if (obj instanceof ItemStack) {		//lol just make it pass ItemStack aswell
                pool.add(new WeightedRandomObject(obj, weight));
            }
        }
        return pool;
    }

    public static void equipFullSet(EntityLivingBase entity, Item helmet, Item chest, Item legs, Item boots) {
        entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(helmet));
        entity.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(chest));
        entity.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(legs));
        entity.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(boots));
    }

    public static void assignItemsToEntity(EntityLivingBase entity, Map<Integer, List<WeightedRandomObject>> slotPools, Random rand) {
        for (Map.Entry<Integer, List<WeightedRandomObject>> entry : slotPools.entrySet()) {
            int slot = entry.getKey();
            List<WeightedRandomObject> pool = entry.getValue();

            WeightedRandomObject choice = WeightedRandom.getRandomItem(rand, pool);
            if (choice == null) {
                continue;
            }

            ItemStack stack = choice.asStack();
            if (stack == null || stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() == ModItems.gas_mask_m65
                    || stack.getItem() == ModItems.gas_mask_olde
                    || stack.getItem() == ModItems.gas_mask_mono) {
                ArmorUtil.installGasMaskFilter(stack, new ItemStack(ModItems.gas_mask_filter));
            }

            entity.setItemStackToSlot(mapLegacySlot(slot), stack);

            if (slot == 0 && entity instanceof EntitySkeleton && pool == slotPools.get(0)) {
                addFireTask((EntityLiving) entity);
            }
        }
    }

    public static void addFireTask(EntityLiving entity) {
        entity.setDropChance(EntityEquipmentSlot.MAINHAND, 0.0F); // Prevent dropping guns

        for (EntityAITasks.EntityAITaskEntry entry : entity.tasks.taskEntries) {
            if (entry.action instanceof EntityAIFireGun) return;
        }

        entity.tasks.addTask(3, new EntityAIFireGun(entity));
    }

    private static EntityEquipmentSlot mapLegacySlot(int slot) {
        switch (slot) {
            case 4: return EntityEquipmentSlot.HEAD;
            case 3: return EntityEquipmentSlot.CHEST;
            case 2: return EntityEquipmentSlot.LEGS;
            case 1: return EntityEquipmentSlot.FEET;
            case 0:
            default: return EntityEquipmentSlot.MAINHAND;
        }
    }
}
