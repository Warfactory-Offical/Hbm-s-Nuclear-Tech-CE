package com.hbm.itempool;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.WeightedRandomChestContentFrom1710;
import com.hbm.items.ItemEnums;
import com.hbm.items.ModItems;
import com.hbm.items.ModItems.Armory;
import com.hbm.items.ModItems.Foods;
import com.hbm.items.ModItems.Inserts;
import com.hbm.items.ModItems.ToolSets;

import static com.hbm.lib.HbmChestContents.weighted;

public class ItemPoolsRedRoom {
    public static final String POOL_RED_PEDESTAL = "POOL_RED_PEDESTAL";
    public static final String POOL_BLACK_SLAB = "POOL_BLACK_SLAB";
    public static final String POOL_BLACK_PART = "POOL_BLACK_PART";

    public static void init() {

        //pedestal items
        new ItemPool(POOL_RED_PEDESTAL) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(Inserts.armor_polish, 0, 1, 1, 10),
                    weighted(Inserts.bandaid, 0, 1, 1, 10),
                    weighted(Inserts.serum, 0, 1, 1, 10),
                    weighted(Inserts.quartz_plutonium, 0, 1, 1, 10),
                    weighted(Inserts.morning_glory, 0, 1, 1, 10),
                    weighted(Inserts.spider_milk, 0, 1, 1, 10),
                    weighted(Inserts.ink, 0, 1, 1, 10),
                    weighted(Inserts.heart_container, 0, 1, 1, 10),
                    weighted(Inserts.black_diamond, 0, 1, 1, 10),
                    weighted(Inserts.scrumpy, 0, 1, 1, 10),

                    weighted(Inserts.wild_p, 0, 1, 1, 5),
                    weighted(Inserts.ballistic_gauntlet, 0, 1, 1, 10),
                    weighted(Inserts.card_aos, 0, 1, 1, 5),
                    weighted(Inserts.card_qos, 0, 1, 1, 5),
                    weighted(ToolSets.starmetal_sword, 0, 1, 1, 5),
                    weighted(ModItems.gem_alexandrite, 0, 1, 1, 5),
                    weighted(Inserts.crackpipe, 0, 1, 1, 5),
                    weighted(Foods.flask_infusion, 0, 1, 1, 5),
                    weighted(ModBlocks.boxcar, 0, 1, 1, 5),
                    weighted(ModItems.book_of_, 0, 1, 1, 5),

                    weighted(Armory.gun_hangman, 0, 1, 1, 1),
                    weighted(Armory.gun_mas36, 0, 1, 1, 1),
            };
        }};

        //pedestal weapons
        new ItemPool(POOL_BLACK_SLAB) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(ModItems.clay_tablet, 0, 1, 1, 10)
            };
        }};

        //pedestal weapons
        new ItemPool(POOL_BLACK_PART) {{
            this.pool = new WeightedRandomChestContentFrom1710[] {
                    weighted(ModItems.item_secret, ItemEnums.EnumSecretType.SELENIUM_STEEL.ordinal(), 4, 4, 10),
                    weighted(ModItems.item_secret, ItemEnums.EnumSecretType.CONTROLLER.ordinal(), 1, 1, 10),
                    weighted(ModItems.item_secret, ItemEnums.EnumSecretType.CANISTER.ordinal(), 1, 1, 10),
            };
        }};
    }
}
