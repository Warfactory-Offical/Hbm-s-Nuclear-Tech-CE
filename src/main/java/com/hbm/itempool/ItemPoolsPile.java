package com.hbm.itempool;

import com.hbm.handler.WeightedRandomChestContentFrom1710;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ModItems;
import com.hbm.items.ModItems.ArmorSets;
import com.hbm.items.ModItems.Armory;
import com.hbm.items.ModItems.Foods;
import com.hbm.items.ModItems.Materials.Ingots;
import com.hbm.items.ModItems.Materials.Nuggies;
import com.hbm.items.ModItems.ToolSets;
import com.hbm.items.weapon.sedna.factory.GunFactory;
import net.minecraft.init.Items;

import static com.hbm.lib.HbmChestContents.weighted;

public class ItemPoolsPile {
    public static final String POOL_PILE_HIVE = "POOL_PILE_HIVE";
    public static final String POOL_PILE_BONES = "POOL_PILE_BONES";
    public static final String POOL_PILE_CAPS = "POOL_PILE_CAPS";
    public static final String POOL_PILE_MED_SYRINGE = "POOL_PILE_MED_SYRINGE";
    public static final String POOL_PILE_MED_PILLS = "POOL_PILE_MED_PILLS";
    public static final String POOL_PILE_MAKESHIFT_GUN = "POOL_PILE_MAKESHIFT_GUN";
    public static final String POOL_PILE_MAKESHIFT_WRENCH = "POOL_PILE_MAKESHIFT_WRENCH";
    public static final String POOL_PILE_MAKESHIFT_PLATES = "POOL_PILE_MAKESHIFT_PLATES";
    public static final String POOL_PILE_MAKESHIFT_WIRE = "POOL_PILE_MAKESHIFT_WIRE";
    public static final String POOL_PILE_NUKE_STORAGE = "POOL_PILE_NUKE_STORAGE";
    public static final String POOL_PILE_OF_GARBAGE = "POOL_PILE_OF_GARBAGE";

    public static void init() {

        //items found in glyphid hives
        new ItemPool(POOL_PILE_HIVE) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    //Materials
                    weighted(Items.IRON_INGOT, 0, 1, 3, 10),
                    weighted(Ingots.ingot_steel, 0, 1, 2, 10),
                    weighted(Ingots.ingot_aluminium, 0, 1, 2, 10),
                    weighted(ModItems.scrap, 0, 3, 6, 10),
                    //Armor
                    weighted(ModItems.gas_mask_m65, 0, 1, 1, 10),
                    weighted(ArmorSets.steel_plate, 0, 1, 1, 5),
                    weighted(ArmorSets.steel_legs, 0, 1, 1, 5),
                    //Gear
                    weighted(ToolSets.steel_pickaxe, 0, 1, 1, 5),
                    weighted(ToolSets.steel_shovel, 0, 1, 1, 5),
                    //Weapons
                    weighted(Armory.gun_maresleg, 0, 1, 1, 5),
                    weighted(Armory.gun_light_revolver, 0, 1, 1, 1),
                    weighted(Armory.grenade_if_generic, 0, 1, 2, 5),
                    weighted(Armory.ammo_standard, GunFactory.EnumAmmo.G12.ordinal(), 4, 4, 10),
                    weighted(Armory.ammo_standard, GunFactory.EnumAmmo.M357_SP.ordinal(), 6, 12, 10),
                    weighted(Armory.ammo_standard, GunFactory.EnumAmmo.G40_HE.ordinal(), 1, 1, 2),
                    //Consumables
                    weighted(Foods.bottle_nuka, 0, 1, 2, 20),
                    weighted(Foods.bottle_quantum, 0, 1, 2, 1),
                    weighted(Foods.definitelyfood, 0, 5, 12, 20),
                    weighted(Foods.egg_glyphid, 0, 1, 3, 30),
                    weighted(ModItems.syringe_metal_stimpak, 0, 1, 1, 5),
                    weighted(ModItems.iv_blood, 0, 1, 1, 10),
                    weighted(Items.EXPERIENCE_BOTTLE, 0, 1, 3, 5),
            };
        }};

        //items found in glyphid bone piles
        new ItemPool(POOL_PILE_BONES) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(Items.BONE, 0, 1, 1, 10),
                    weighted(Items.ROTTEN_FLESH, 0, 1, 1, 5),
                    weighted(ModItems.biomass, 0, 1, 1, 2)
            };
        }};

        //bottlecap stashess
        new ItemPool(POOL_PILE_CAPS) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(Foods.cap_nuka, 0, 4, 4, 20),
                    weighted(Foods.cap_quantum, 0, 4, 4, 3),
                    weighted(Foods.cap_sparkle, 0, 4, 4, 1),
            };
        }};

        //medicine stashes
        new ItemPool(POOL_PILE_MED_SYRINGE) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(ModItems.syringe_metal_stimpak, 0, 1, 1, 10),
                    weighted(ModItems.syringe_metal_medx, 0, 1, 1, 5),
                    weighted(ModItems.syringe_metal_psycho, 0, 1, 1, 5),
            };
        }};
        new ItemPool(POOL_PILE_MED_PILLS) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(ModItems.radaway, 0, 1, 1, 10),
                    weighted(ModItems.radx, 0, 1, 1, 10),
                    weighted(ModItems.iv_blood, 0, 1, 1, 15),
                    weighted(ModItems.siox, 0, 1, 1, 5),
            };
        }};

        //makeshift gun
        new ItemPool(POOL_PILE_MAKESHIFT_GUN) {{ this.pool = new WeightedRandomChestContentFrom1710[] { weighted(Armory.gun_maresleg, 0, 1, 1, 10) }; }};
        new ItemPool(POOL_PILE_MAKESHIFT_WRENCH) {{ this.pool = new WeightedRandomChestContentFrom1710[] { weighted(ModItems.wrench, 0, 1, 1, 10) }; }};
        new ItemPool(POOL_PILE_MAKESHIFT_PLATES) {{ this.pool = new WeightedRandomChestContentFrom1710[] { weighted(ModItems.plate_steel, 0, 1, 1, 10) }; }};
        new ItemPool(POOL_PILE_MAKESHIFT_WIRE) {{ this.pool = new WeightedRandomChestContentFrom1710[] { weighted(ModItems.wire_fine, Mats.MAT_ALUMINIUM.id, 1, 1, 10) }; }};

        new ItemPool(POOL_PILE_NUKE_STORAGE) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(Armory.ammo_standard, GunFactory.EnumAmmo.NUKE_STANDARD.ordinal(), 1, 1, 50),
                    weighted(Armory.ammo_standard, GunFactory.EnumAmmo.NUKE_HIGH.ordinal(), 1, 1, 10),
                    weighted(Armory.ammo_standard, GunFactory.EnumAmmo.NUKE_TOTS.ordinal(), 1, 1, 10),

            };
        }};

        new ItemPool(POOL_PILE_OF_GARBAGE) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(ModItems.pipe, 2600, 0, 2, 20),
                    weighted(ModItems.scrap, 0, 1, 5, 20),
                    weighted(ModItems.wire_fine, 8200, 1, 2, 20),
                    weighted(ModItems.dust, 0, 1, 3, 40),
                    weighted(ModItems.dust_tiny, 0, 1, 7, 40),
                    weighted(ModItems.powder_cement, 0, 1, 6, 40),
                    weighted(Nuggies.nugget_lead, 0, 0, 3, 20),
                    weighted(ModItems.wire_fine, 30, 0, 3, 20),
                    weighted(ModItems.powder_ash, 0, 0, 1, 15),
                    weighted(ModItems.plate_lead, 0, 0, 1, 15),
                    weighted(Items.STRING, 0, 0, 1, 15),
                    weighted(ModItems.bolt, 8200, 0, 2, 15),
                    weighted(ModItems.pin, 0, 0, 2, 15),
                    weighted(Foods.cap_nuka, 0, 0, 8, 15),
                    weighted(ModItems.plate_iron, 0, 0, 2, 15),
                    weighted(ModItems.fallout, 0, 0, 2, 15),
                    weighted(ModItems.coil_tungsten, 0, 0, 2, 15),
                    weighted(Foods.can_empty, 0, 0, 1, 15),
                    weighted(Ingots.ingot_asbestos, 0, 0, 1, 15),
                    weighted(ModItems.syringe_metal_empty, 0, 0, 1, 15),
                    weighted(ModItems.syringe_empty, 0, 0, 1, 15),
                    weighted(ToolSets.pipe_lead, 0, 0, 1, 5),
                    weighted(ModItems.motor, 0, 0, 1, 5),
                    weighted(Foods.canned_conserve, 2, 0, 1, 5),
            };
        }};
    }
}
