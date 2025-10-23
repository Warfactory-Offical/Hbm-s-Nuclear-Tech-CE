package com.hbm.itempool;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.WeightedRandomChestContentFrom1710;
import com.hbm.items.ModItems.Materials.Crystals;
import com.hbm.items.ModItems.Materials.Ingots;
import com.hbm.items.ModItems.Materials.Powders;
import net.minecraft.init.Items;

import static com.hbm.lib.HbmChestContents.weighted;

public class ItemPoolsSatellite {
    public static final String POOL_SAT_MINER = "POOL_SAT_MINER";
    public static final String POOL_SAT_LUNAR = "POOL_SAT_LUNAR"; //woona

    public static void init() {

        new ItemPool(POOL_SAT_MINER) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(Powders.powder_aluminium, 0, 3, 3, 10),
                    weighted(Powders.powder_iron, 0, 3, 3, 10),
                    weighted(Powders.powder_titanium, 0, 2, 2, 8),
                    weighted(Crystals.crystal_tungsten, 0, 2, 2, 7),
                    weighted(Powders.powder_coal, 0, 4, 4, 15),
                    weighted(Powders.powder_uranium, 0, 2, 2, 5),
                    weighted(Powders.powder_plutonium, 0, 1, 1, 5),
                    weighted(Powders.powder_thorium, 0, 2, 2, 7),
                    weighted(Powders.powder_desh_mix, 0, 3, 3, 5),
                    weighted(Powders.powder_diamond, 0, 2, 2, 7),
                    weighted(Items.REDSTONE, 0, 5, 5, 15),
                    weighted(Powders.powder_nitan_mix, 0, 2, 2, 5),
                    weighted(Powders.powder_power, 0, 2, 2, 5),
                    weighted(Powders.powder_copper, 0, 5, 5, 15),
                    weighted(Powders.powder_lead, 0, 3, 3, 10),
                    weighted(Ingots.fluorite, 0, 4, 4, 15),
                    weighted(Powders.powder_lapis, 0, 4, 4, 10),
                    weighted(Crystals.crystal_aluminium, 0, 1, 1, 5),
                    weighted(Crystals.crystal_gold, 0, 1, 1, 5),
                    weighted(Crystals.crystal_phosphorus, 0, 1, 1, 10),
                    weighted(ModBlocks.gravel_diamond, 0, 1, 1, 3),
                    weighted(Crystals.crystal_uranium, 0, 1, 1, 3),
                    weighted(Crystals.crystal_plutonium, 0, 1, 1, 3),
                    weighted(Crystals.crystal_trixite, 0, 1, 1, 1),
                    weighted(Crystals.crystal_starmetal, 0, 1, 1, 1),
                    weighted(Crystals.crystal_lithium, 0, 2 ,2, 4)
            };
        }};

        new ItemPool(POOL_SAT_LUNAR) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(ModBlocks.moon_turf, 0, 48, 48, 5),
                    weighted(ModBlocks.moon_turf, 0, 32, 32, 7),
                    weighted(ModBlocks.moon_turf, 0, 16, 16, 5),
                    weighted(Powders.powder_lithium, 0, 3, 3, 5),
                    weighted(Powders.powder_iron, 0, 3, 3, 5),
                    weighted(Crystals.crystal_iron, 0, 1, 1, 1),
                    weighted(Crystals.crystal_lithium, 0, 1, 1, 1)
            };
        }};
    }
}
