package com.hbm.itempool;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.WeightedRandomChestContentFrom1710;
import com.hbm.items.ItemEnums;
import com.hbm.items.ModItems;
import com.hbm.items.ModItems.*;
import com.hbm.items.ModItems.Materials.Crystals;
import com.hbm.items.ModItems.Materials.Ingots;
import com.hbm.items.ModItems.Materials.Powders;
import com.hbm.items.weapon.sedna.factory.GunFactory;
import net.minecraft.init.Items;

import static com.hbm.lib.HbmChestContents.weighted;

public class ItemPoolsSingle {

    public static final String POOL_POWDER = "POOL_POWDER";
    public static final String POOL_VAULT_RUSTY = "POOL_VAULT_RUSTY";
    public static final String POOL_VAULT_STANDARD = "POOL_VAULT_STANDARD";
    public static final String POOL_VAULT_REINFORCED = "POOL_VAULT_REINFORCED";
    public static final String POOL_VAULT_UNBREAKABLE = "POOL_VAULT_UNBREAKABLE";
    public static final String POOL_METEORITE_TREASURE = "POOL_METEORITE_TREASURE";

    public static void init() {

        //powder boxes
        new ItemPool(POOL_POWDER) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(Powders.powder_neptunium, 0, 1, 32, 1),
                    weighted(Powders.powder_iodine, 0, 1, 32, 1),
                    weighted(Powders.powder_thorium, 0, 1, 32, 1),
                    weighted(Powders.powder_astatine, 0, 1, 32, 1),
                    weighted(Powders.powder_neodymium, 0, 1, 32, 1),
                    weighted(Powders.powder_caesium, 0, 1, 32, 1),
                    weighted(Powders.powder_strontium, 0, 1, 32, 1),
                    weighted(Powders.powder_cobalt, 0, 1, 32, 1),
                    weighted(Powders.powder_bromine, 0, 1, 32, 1),
                    weighted(Powders.powder_niobium, 0, 1, 32, 1),
                    weighted(Powders.powder_tennessine, 0, 1, 32, 1),
                    weighted(Powders.powder_cerium, 0, 1, 32, 1)
            };
        }};

        new ItemPool(POOL_VAULT_RUSTY) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(Items.GOLD_INGOT, 0, 3, 14, 1),
                    weighted(Armory.gun_heavy_revolver, 0, 1, 1, 2),
                    weighted(ModItems.pin, 0, 8, 8, 1),
                    weighted(Armory.gun_am180, 0, 1, 1, 1),
                    weighted(Foods.bottle_quantum, 0, 1, 3, 1),
                    weighted(Ingots.ingot_advanced_alloy, 0, 4, 12, 1),
                    weighted(Armory.ammo_standard, GunFactory.EnumAmmo.BMG50_FMJ.ordinal(), 24, 48, 1),
                    weighted(Armory.ammo_standard, GunFactory.EnumAmmo.P9_JHP.ordinal(), 48, 64, 2),
                    weighted(ModItems.circuit, ItemEnums.EnumCircuitType.CHIP.ordinal(), 3, 6, 1),
                    weighted(ModItems.gas_mask_m65, 0, 1, 1, 1),
                    weighted(Armory.grenade_if_he, 0, 1, 1, 1),
                    weighted(Armory.grenade_if_incendiary, 0, 1, 1, 1),
                    weighted(Items.DIAMOND, 0, 1, 2, 1)
            };
        }};

        new ItemPool(POOL_VAULT_STANDARD) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(Ingots.ingot_desh, 0, 2, 6, 1),
                    weighted(Batteries.battery_advanced_cell_4, 0, 1, 1, 1),
                    weighted(Powders.powder_desh_mix, 0, 1, 5, 1),
                    weighted(Items.DIAMOND, 0, 3, 6, 1),
                    weighted(Armory.ammo_standard, GunFactory.EnumAmmo.NUKE_STANDARD.ordinal(), 1, 1, 1),
                    weighted(Armory.ammo_container, 0, 1, 1, 1),
                    weighted(Armory.grenade_nuclear, 0, 1, 1, 1),
                    weighted(Armory.grenade_smart, 0, 1, 6, 1),
                    weighted(Powders.powder_yellowcake, 0, 16, 24, 1),
                    weighted(Armory.gun_uzi, 0, 1, 1, 1),
                    weighted(ModItems.circuit, ItemEnums.EnumCircuitType.VACUUM_TUBE.ordinal(), 12, 16, 1),
                    weighted(ModItems.circuit, ItemEnums.EnumCircuitType.CHIP.ordinal(), 2, 6, 1)
            };
        }};

        new ItemPool(POOL_VAULT_REINFORCED) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(Ingots.ingot_desh, 0, 6, 16, 1),
                    weighted(Batteries.battery_lithium, 0, 1, 1, 1),
                    weighted(Powders.powder_power, 0, 1, 5, 1),
                    weighted(ModItems.sat_chip, 0, 1, 1, 1),
                    weighted(Items.DIAMOND, 0, 5, 9, 1),
                    weighted(Armory.ammo_standard, GunFactory.EnumAmmo.NUKE_STANDARD.ordinal(), 1, 3, 1),
                    weighted(Armory.ammo_container, 0, 1, 4, 1),
                    weighted(Armory.grenade_nuclear, 0, 1, 2, 1),
                    weighted(Armory.grenade_mirv, 0, 1, 1, 1),
                    weighted(Powders.powder_yellowcake, 0, 26, 42, 1),
                    weighted(Armory.gun_heavy_revolver, 0, 1, 1, 1),
                    weighted(ModItems.circuit, ItemEnums.EnumCircuitType.CHIP.ordinal(), 18, 32, 1),
                    weighted(ModItems.circuit, ItemEnums.EnumCircuitType.BASIC.ordinal(), 6, 12, 1)
            };
        }};

        new ItemPool(POOL_VAULT_UNBREAKABLE) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(Armory.ammo_container, 0, 3, 6, 1),
                    weighted(Armory.ammo_standard, GunFactory.EnumAmmo.NUKE_DEMO.ordinal(), 2, 3, 1),
                    weighted(Armory.gun_carbine, 0, 1, 1, 1),
                    weighted(Armory.ammo_standard, GunFactory.EnumAmmo.R762_DU.ordinal(), 16, 32, 1),
                    weighted(Armory.gun_congolake, 0, 1, 1, 1),
                    weighted(Batteries.battery_schrabidium_cell, 0, 1, 1, 1),
                    weighted(ModItems.circuit, ItemEnums.EnumCircuitType.ADVANCED.ordinal(), 6, 12, 1)
            };
        }};

        new ItemPool(POOL_METEORITE_TREASURE) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(ToolSets.cobalt_pickaxe, 0, 1, 1, 10),
                    weighted(Ingots.ingot_zirconium, 0, 1, 16, 10),
                    weighted(Ingots.ingot_niobium, 0, 1, 16, 10),
                    weighted(Ingots.ingot_cobalt, 0, 1, 16, 10),
                    weighted(Ingots.ingot_boron, 0, 1, 16, 10),
                    weighted(Ingots.ingot_starmetal, 0, 1, 1, 5),
                    weighted(Crystals.crystal_gold, 0, 1, 4, 10),
                    weighted(ModItems.circuit, ItemEnums.EnumCircuitType.VACUUM_TUBE.ordinal(), 4, 8, 10),
                    weighted(ModItems.circuit, ItemEnums.EnumCircuitType.CHIP.ordinal(), 2, 4, 10),
                    weighted(Foods.definitelyfood, 0, 16, 32, 25),
                    weighted(ModBlocks.crate_can, 0, 1, 3, 10),
                    weighted(ModItems.pill_herbal, 0, 1, 2, 10),
                    weighted(Inserts.serum, 0, 1, 1, 5),
                    weighted(Inserts.heart_piece, 0, 1, 1, 5),
                    weighted(Inserts.scrumpy, 0, 1, 1, 5),
                    weighted(ModItems.launch_code_piece, 0, 1, 1, 5),
                    weighted(Foods.egg_glyphid, 0, 1, 1, 5),
                    weighted(ModItems.gem_alexandrite, 0, 1, 1, 1),
            };
        }};
    }
    
}
