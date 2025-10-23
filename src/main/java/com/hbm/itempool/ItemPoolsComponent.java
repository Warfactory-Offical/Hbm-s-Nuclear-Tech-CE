package com.hbm.itempool;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.WeightedRandomChestContentFrom1710;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ItemEnums;
import com.hbm.items.ItemEnums.EnumCircuitType;
import com.hbm.items.ModItems;
import com.hbm.items.ModItems.ArmorSets;
import com.hbm.items.ModItems.Batteries;
import com.hbm.items.ModItems.Foods;
import com.hbm.items.ModItems.Inserts;
import com.hbm.items.ModItems.Materials.Billets;
import com.hbm.items.ModItems.Materials.Ingots;
import com.hbm.items.ModItems.Materials.Nuggies;
import com.hbm.items.ModItems.Materials.Powders;
import com.hbm.items.tool.ItemBlowtorch;
import net.minecraft.init.Items;

import static com.hbm.lib.HbmChestContents.weighted;

public class ItemPoolsComponent {
    public static final String POOL_MACHINE_PARTS = "POOL_MACHINE_PARTS";
    public static final String POOL_NUKE_FUEL = "POOL_NUKE_FUEL";
    public static final String POOL_SILO = "POOL_SILO";
    public static final String POOL_OFFICE_TRASH = "POOL_OFFICE_TRASH";
    public static final String POOL_FILING_CABINET = "POOL_FILING_CABINET";
    public static final String POOL_SOLID_FUEL = "POOL_SOLID_FUEL";
    public static final String POOL_VAULT_LAB = "POOL_VAULT_LAB";
    public static final String POOL_VAULT_LOCKERS = "POOL_VAULT_LOCKERS";
    public static final String POOL_METEOR_SAFE = "POOL_METEOR_SAFE";

    public static void init() {

        //machine parts
        new ItemPool(POOL_MACHINE_PARTS) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(ModItems.plate_steel, 0, 1, 5, 5),
                    weighted(ModItems.shell, Mats.MAT_STEEL.id, 1, 3, 3),
                    weighted(ModItems.plate_polymer, 0, 1, 6, 5),
                    weighted(ModItems.bolt, Mats.MAT_STEEL.id, 4, 16, 3),
                    weighted(ModItems.bolt, Mats.MAT_TUNGSTEN.id, 4, 16, 3),
                    weighted(ModItems.coil_tungsten, 0, 1, 2, 5),
                    weighted(ModItems.motor, 0, 1, 2, 4),
                    weighted(ModItems.tank_steel, 0, 1, 2, 3),
                    weighted(ModItems.coil_copper, 0, 1, 3, 4),
                    weighted(ModItems.coil_copper_torus, 0, 1, 2, 3),
                    weighted(ModItems.wire_fine, Mats.MAT_MINGRADE.id, 1, 8, 5),
                    weighted(ModItems.piston_selenium, 0, 1, 1, 3),
                    weighted(Batteries.battery_advanced_cell, 0, 1, 1, 3),
                    weighted(ModItems.circuit, EnumCircuitType.VACUUM_TUBE.ordinal(), 1, 2, 4),
                    weighted(ModItems.circuit, EnumCircuitType.PCB.ordinal(), 1, 3, 5),
                    weighted(ModItems.circuit, EnumCircuitType.CAPACITOR.ordinal(), 1, 1, 3),
                    weighted(ModItems.blade_titanium, 0, 1, 8, 1)
            };
        }};

        //fuel isotopes found in bunkers and labs
        new ItemPool(POOL_NUKE_FUEL) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(Billets.billet_uranium, 0, 1, 4, 4),
                    weighted(Billets.billet_th232, 0, 1, 3, 3),
                    weighted(Billets.billet_uranium_fuel, 0, 1, 3, 5),
                    weighted(Billets.billet_mox_fuel, 0, 1, 3, 5),
                    weighted(Billets.billet_thorium_fuel, 0, 1, 3, 3),
                    weighted(Billets.billet_ra226be, 0, 1, 2, 2),
                    weighted(Billets.billet_beryllium, 0, 1, 1, 1),
                    weighted(Nuggies.nugget_u233, 0, 1, 1, 1),
                    weighted(Nuggies.nugget_uranium_fuel, 0, 1, 1, 1),
                    weighted(ModItems.rod_zirnox_empty, 0, 1, 3, 3),
                    weighted(Ingots.ingot_graphite, 0, 1, 4, 3),
                    weighted(ModItems.pile_rod_uranium, 0, 2, 5, 3),
                    weighted(ModItems.pile_rod_source, 0, 1, 2, 2),
                    weighted(ModItems.reacher, 0, 1, 1, 3),
                    weighted(ModItems.screwdriver, 0, 1, 1, 2)
            };
        }};

        //missile parts found in silos
        new ItemPool(POOL_SILO) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(ModItems.missile_generic, 0, 1, 1, 4),
                    weighted(ModItems.missile_incendiary, 0, 1, 1, 4),
                    weighted(ModItems.gas_mask_m65, 0, 1, 1, 5),
                    weighted(Batteries.battery_advanced, 0, 1, 1, 5),
                    weighted(ModItems.designator, 0, 1, 1, 5),
                    weighted(ModItems.thruster_small, 0, 1, 1, 5),
                    weighted(ModItems.thruster_medium, 0, 1, 1, 4),
                    weighted(ModItems.fuel_tank_small, 0, 1, 1, 5),
                    weighted(ModItems.fuel_tank_medium, 0, 1, 1, 4),
                    weighted(ModItems.bomb_caller, 0, 1, 1, 1),
                    weighted(ModItems.bomb_caller, 3, 1, 1, 1),
                    weighted(Foods.bottle_nuka, 0, 1, 3, 10)
            };
        }};

        //low quality items from offices in chests
        new ItemPool(POOL_OFFICE_TRASH) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(Items.PAPER, 0, 1, 12, 10),
                    weighted(Items.BOOK, 0, 1, 3, 4),
                    weighted(Foods.twinkie, 0, 1, 2, 6),
                    weighted(Foods.coffee, 0, 1, 1, 4),
                    weighted(ModBlocks.deco_computer, 0, 1, 1, 1),
                    weighted(ModItems.flame_politics, 0, 1, 1, 2),
                    weighted(Foods.ring_pull, 0, 1, 1, 4),
                    weighted(Foods.can_empty, 0, 1, 1, 2),
                    weighted(Foods.can_creature, 0, 1, 2, 2),
                    weighted(Foods.can_smart, 0, 1, 3, 2),
                    weighted(Foods.can_mrsugar, 0, 1, 2, 2),
                    weighted(Foods.cap_nuka, 0, 1, 16, 2),
                    weighted(ModItems.book_guide, 3, 1, 1, 1),
            };
        }};

        //things found in various filing cabinets, paper, books, etc
        new ItemPool(POOL_FILING_CABINET) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(Items.PAPER, 0, 1, 12, 240),
                    weighted(Items.BOOK, 0, 1, 3, 90),
                    weighted(Items.MAP, 0, 1, 1, 50),
                    weighted(Items.WRITABLE_BOOK, 0, 1, 1, 30),
                    weighted(Inserts.cigarette, 0, 1, 16, 20),
                    weighted(ModItems.toothpicks, 0, 1, 16, 10),
                    weighted(ModItems.dust, 0, 1, 1, 40),
                    weighted(ModItems.dust_tiny, 0, 1, 3, 75),
                    weighted(Inserts.ink, 0, 1, 1, 1)
            };
        }};

        //solid fuels from bunker power rooms
        new ItemPool(POOL_SOLID_FUEL) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(ModItems.solid_fuel, 0, 1, 5, 1),
                    weighted(ModItems.solid_fuel_presto, 0, 1, 2, 2),
                    weighted(ModItems.ball_dynamite, 0, 1, 4, 2),
                    weighted(ModItems.coke, ItemEnums.EnumCokeType.PETROLEUM.ordinal(), 1, 3, 1),
                    weighted(Items.REDSTONE, 0, 1, 3, 1),
                    weighted(Powders.niter, 0, 1, 3, 1)
            };
        }};

        //various lab related items from bunkers
        new ItemPool(POOL_VAULT_LAB) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(ItemBlowtorch.getEmptyTool(ModItems.blowtorch), 1, 1, 4),
                    weighted(ModItems.chemistry_set, 0, 1, 1, 15),
                    weighted(ModItems.screwdriver, 0, 1, 1, 10),
                    weighted(Nuggies.ingot_mercury, 0, 1, 1, 3),
                    weighted(Inserts.morning_glory, 0, 1, 1, 1),
                    weighted(ModItems.filter_coal, 0, 1, 1, 5),
                    weighted(ModItems.dust, 0, 1, 3, 25),
                    weighted(Items.PAPER, 0, 1, 2, 15),
                    weighted(ModItems.cell, 0, 1, 1, 5),
                    weighted(Items.GLASS_BOTTLE, 0, 1, 1, 5),
                    weighted(Powders.powder_iodine, 0, 1, 1, 1),
                    weighted(Powders.powder_bromine, 0, 1, 1, 1),
                    weighted(Powders.powder_cobalt, 0, 1, 1, 1),
                    weighted(Powders.powder_neodymium, 0, 1, 1, 1),
                    weighted(Powders.powder_boron, 0, 1, 1, 1)
            };
        }};

        //personal items and gear from vaults
        new ItemPool(POOL_VAULT_LOCKERS) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(ArmorSets.robes_helmet, 0, 1, 1, 1),
                    weighted(ArmorSets.robes_plate, 0, 1, 1, 1),
                    weighted(ArmorSets.robes_legs, 0, 1, 1, 1),
                    weighted(ArmorSets.robes_boots, 0, 1, 1, 1),
                    weighted(ArmorSets.jackt, 0, 1, 1, 1),
                    weighted(ArmorSets.jackt2, 0, 1, 1, 1),
                    weighted(ModItems.gas_mask_m65, 0, 1, 1, 2),
                    weighted(ModItems.gas_mask_mono, 0, 1, 1, 2),
                    weighted(ModItems.goggles, 0, 1, 1, 2),
                    weighted(ModItems.gas_mask_filter, 0, 1, 1, 4),
                    weighted(ModItems.flame_opinion, 0, 1, 3, 5),
                    weighted(ModItems.flame_conspiracy, 0, 1, 3, 5),
                    weighted(ModItems.flame_politics, 0, 1, 3, 5),
                    weighted(Foods.definitelyfood, 0, 2, 7, 5),
                    weighted(Inserts.cigarette, 0, 1, 8, 5),
                    weighted(Inserts.armor_polish, 0, 1, 1, 3),
                    weighted(ModItems.gun_kit_1, 0, 1, 1, 3),
                    weighted(ModItems.rag, 0, 1, 3, 5),
                    weighted(Items.PAPER, 0, 1, 6, 7),
                    weighted(Items.CLOCK, 0, 1, 1, 3),
                    weighted(Items.BOOK, 0, 1, 5, 10),
                    weighted(Items.EXPERIENCE_BOTTLE, 0, 1, 3, 1)
            };
        }};

        // Black Book safe in meteor dungeons
        new ItemPool(POOL_METEOR_SAFE) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(ModItems.book_of_, 0, 1, 1, 1),
                    weighted(ModItems.stamp_book, 0, 1, 1, 1),
                    weighted(ModItems.stamp_book, 1, 1, 1, 1),
                    weighted(ModItems.stamp_book, 2, 1, 1, 1),
                    weighted(ModItems.stamp_book, 3, 1, 1, 1),
                    weighted(ModItems.stamp_book, 4, 1, 1, 1),
                    weighted(ModItems.stamp_book, 5, 1, 1, 1),
                    weighted(ModItems.stamp_book, 6, 1, 1, 1),
                    weighted(ModItems.stamp_book, 7, 1, 1, 1),
            };
        }};
    }
}
