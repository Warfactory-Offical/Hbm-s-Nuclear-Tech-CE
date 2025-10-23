package com.hbm.itempool;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.WeightedRandomChestContentFrom1710;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.items.ModItems.Armory;
import com.hbm.items.ModItems.Foods;
import com.hbm.items.weapon.sedna.factory.GunFactory;

import static com.hbm.lib.HbmChestContents.weighted;

public class ItemPoolsC130 {
    public static final String POOL_SUPPLIES = "POOL_SUPPLIES";
    public static final String POOL_WEAPONS = "POOL_WEAPONS";
    public static final String POOL_AMMO = "POOL_AMMO";


    public static void init() {

        new ItemPool(POOL_SUPPLIES) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(Foods.definitelyfood, 0, 3, 10, 25),
                    weighted(ModItems.syringe_metal_stimpak, 0, 1, 3, 10),
                    weighted(ModItems.pill_iodine, 0, 1, 2, 2),
                    weighted(ModItems.canister_full, Fluids.DIESEL.getID(), 1, 4, 5),
                    weighted(ModBlocks.machine_diesel, 0, 1, 1, 1),
                    weighted(ModItems.geiger_counter, 0, 1, 1, 2),
                    weighted(ModItems.med_bag, 0, 1, 1, 3),
                    weighted(ModItems.radaway, 0, 1, 5, 10),
            };
        }};

        new ItemPool(POOL_WEAPONS) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(Armory.gun_light_revolver, 0, 1, 1, 10),
                    weighted(Armory.gun_henry, 0, 1, 1, 10),
                    weighted(Armory.gun_maresleg, 0, 1, 1, 10),
                    weighted(Armory.gun_greasegun, 0, 1, 1, 10),
                    weighted(Armory.gun_carbine, 0, 1, 1, 5),
                    weighted(Armory.gun_heavy_revolver, 0, 1, 1, 5),
                    weighted(Armory.gun_panzerschreck, 0, 1, 1, 2),
                    weighted(Armory.gun_double_barrel, 0, 1, 1, 1),
            };
        }};

        new ItemPool(POOL_AMMO) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(Armory.ammo_standard, GunFactory.EnumAmmo.M357_SP.ordinal(), 12, 12, 10),
                    weighted(Armory.ammo_standard, GunFactory.EnumAmmo.M357_FMJ.ordinal(), 6, 6, 10),
                    weighted(Armory.ammo_standard, GunFactory.EnumAmmo.M44_SP.ordinal(), 12, 12, 5),
                    weighted(Armory.ammo_standard, GunFactory.EnumAmmo.M44_FMJ.ordinal(), 6, 6, 5),
                    weighted(Armory.ammo_standard, GunFactory.EnumAmmo.P9_SP.ordinal(), 12, 12, 10),
                    weighted(Armory.ammo_standard, GunFactory.EnumAmmo.P9_FMJ.ordinal(), 6, 6, 10),
                    weighted(Armory.ammo_standard, GunFactory.EnumAmmo.R762_SP.ordinal(), 6, 6, 5),
                    weighted(Armory.ammo_standard, GunFactory.EnumAmmo.G12_BP.ordinal(), 6, 6, 10),
                    weighted(Armory.ammo_standard, GunFactory.EnumAmmo.ROCKET_HE.ordinal(), 1, 1, 3),
            };
        }};
    }
}
