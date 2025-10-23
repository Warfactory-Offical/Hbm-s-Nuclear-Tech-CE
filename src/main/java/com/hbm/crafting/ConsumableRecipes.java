package com.hbm.crafting;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ItemEnums;
import com.hbm.items.ItemEnums.*;
import com.hbm.items.ModItems;
import com.hbm.items.ModItems.*;
import com.hbm.items.ModItems.Materials.Billets;
import com.hbm.items.ModItems.Materials.Ingots;
import com.hbm.items.ModItems.Materials.Nuggies;
import com.hbm.items.ModItems.Materials.Powders;
import com.hbm.items.weapon.sedna.factory.GunFactory;
import com.hbm.main.CraftingManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import static com.hbm.inventory.OreDictManager.*;

/**
 * For foods, drinks or other consumables
 * @author hbm
 */
public class ConsumableRecipes {
    public static void register() {

        //Airstikes
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.bomb_caller, 1, 0),"TTT", "TRT", "TTT", 'T', Blocks.TNT, 'R', ModItems.rangefinder );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.bomb_caller, 1, 1),"TTT", "TRT", "TTT", 'T', Armory.grenade_gascan, 'R', ModItems.rangefinder );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.bomb_caller, 1, 2),"TTT", "TRT", "TTT", 'T', ModItems.pellet_gas, 'R', ModItems.rangefinder );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.bomb_caller, 1, 3),"TRT", 'T', Armory.grenade_cloud, 'R', ModItems.rangefinder );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.bomb_caller, 1, 4),"TRC", 'T', OreDictManager.DictFrame.fromOne(Armory.ammo_standard, GunFactory.EnumAmmo.NUKE_HIGH), 'R', ModItems.rangefinder, 'C', OreDictManager.DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CONTROLLER) );

        //Food
        CraftingManager.addRecipeAuto(new ItemStack(Foods.bomb_waffle, 1),"WEW", "MPM", "WEW", 'W', Items.WHEAT, 'E', Items.EGG, 'M', Items.MILK_BUCKET, 'P', ModItems.man_core );
        CraftingManager.addRecipeAuto(new ItemStack(Foods.schnitzel_vegan, 3),"RWR", "WPW", "RWR", 'W', ModItems.nuclear_waste, 'R', Items.REEDS, 'P', Items.PUMPKIN_SEEDS );
        CraftingManager.addRecipeAuto(new ItemStack(Foods.cotton_candy, 2)," S ", "SPS", " H ", 'P', PU239.nugget(), 'S', Items.SUGAR, 'H', Items.STICK );
        CraftingManager.addRecipeAuto(new ItemStack(Foods.apple_schrabidium, 1, 0),"SSS", "SAS", "SSS", 'S', SA326.nugget(), 'A', Items.APPLE );
        CraftingManager.addRecipeAuto(new ItemStack(Foods.apple_schrabidium, 1, 1),"SSS", "SAS", "SSS", 'S', SA326.ingot(), 'A', Items.APPLE );
        CraftingManager.addRecipeAuto(new ItemStack(Foods.apple_schrabidium, 1, 2),"SSS", "SAS", "SSS", 'S', SA326.block(), 'A', Items.APPLE );
        CraftingManager.addRecipeAuto(new ItemStack(Foods.apple_lead, 1, 0),"SSS", "SAS", "SSS", 'S', PB.nugget(), 'A', Items.APPLE );
        CraftingManager.addRecipeAuto(new ItemStack(Foods.apple_lead, 1, 1),"SSS", "SAS", "SSS", 'S', PB.ingot(), 'A', Items.APPLE );
        CraftingManager.addRecipeAuto(new ItemStack(Foods.apple_lead, 1, 2),"SSS", "SAS", "SSS", 'S', PB.block(), 'A', Items.APPLE );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.apple_euphemium, 1),"EEE", "EAE", "EEE", 'E', EUPH.nugget(), 'A', Items.APPLE );
        CraftingManager.addShapelessAuto(new ItemStack(Foods.tem_flakes, 1, 0),GOLD.nugget(), Items.PAPER );
        CraftingManager.addShapelessAuto(new ItemStack(Foods.tem_flakes, 1, 1),GOLD.nugget(), GOLD.nugget(), GOLD.nugget(), Items.PAPER );
        CraftingManager.addShapelessAuto(new ItemStack(Foods.tem_flakes, 1, 2),GOLD.ingot(), GOLD.ingot(), GOLD.nugget(), GOLD.nugget(), Items.PAPER );
        CraftingManager.addShapelessAuto(new ItemStack(Foods.glowing_stew, 1),Items.BOWL, Item.getItemFromBlock(ModBlocks.mush), Item.getItemFromBlock(ModBlocks.mush) );
        CraftingManager.addShapelessAuto(new ItemStack(Foods.balefire_scrambled, 1),Items.BOWL, ModItems.egg_balefire );
        CraftingManager.addShapelessAuto(new ItemStack(Foods.balefire_and_ham, 1),Foods.balefire_scrambled, Items.COOKED_BEEF );
        CraftingManager.addShapelessAuto(new ItemStack(Foods.med_ipecac, 1),Items.GLASS_BOTTLE, Items.NETHER_WART );
        CraftingManager.addShapelessAuto(new ItemStack(Foods.med_ptsd, 1),Foods.med_ipecac );
        CraftingManager.addShapelessAuto(new ItemStack(Foods.pancake, 1),REDSTONE.dust(), DIAMOND.dust(), Items.WHEAT, STEEL.bolt(), CU.wireFine(), STEEL.plate() );
        CraftingManager.addShapelessAuto(new ItemStack(Foods.pancake, 1),REDSTONE.dust(), EMERALD.dust(), Items.WHEAT, STEEL.bolt(), CU.wireFine(), STEEL.plate() );
        CraftingManager.addShapelessAuto(new ItemStack(Foods.chocolate_milk, 1),KEY_ANYPANE, new ItemStack(Items.DYE, 1, 3), Items.MILK_BUCKET, Fluids.NITROGLYCERIN.getDict(1_000) );
        CraftingManager.addShapelessAuto(new ItemStack(Foods.loops),ModItems.flame_pony, Items.WHEAT, Items.SUGAR );
        CraftingManager.addShapelessAuto(new ItemStack(Foods.loop_stew),Foods.loops, Foods.can_smart, Items.BOWL );
        CraftingManager.addShapelessAuto(new ItemStack(Foods.coffee),COAL.dust(), Items.MILK_BUCKET, Items.POTIONITEM, Items.SUGAR );
        CraftingManager.addShapelessAuto(new ItemStack(Foods.coffee_radium),Foods.coffee, RA226.nugget() );
        CraftingManager.addShapelessAuto(new ItemStack(Ingots.ingot_smore),Items.WHEAT, new ItemStack(Foods.marshmallow_roasted), new ItemStack(Items.DYE, 1, 3) );
        CraftingManager.addShapelessAuto(new ItemStack(Foods.marshmallow),Items.STICK, Items.SUGAR, Items.WHEAT_SEEDS );
        CraftingManager.addShapelessAuto(new ItemStack(Foods.quesadilla, 3),Foods.cheese, Foods.cheese, Items.BREAD );

        //Peas
        CraftingManager.addRecipeAuto(new ItemStack(Foods.peas)," S ", "SNS", " S ", 'S', Items.WHEAT_SEEDS, 'N', GOLD.nugget() );

        //Cans
        CraftingManager.addRecipeAuto(new ItemStack(Foods.can_empty, 1),"P", "P", 'P', AL.plate() );
        CraftingManager.addShapelessAuto(new ItemStack(Foods.can_smart, 1),Foods.can_empty, Items.POTIONITEM, Items.SUGAR, KNO.dust() );
        CraftingManager.addShapelessAuto(new ItemStack(Foods.can_creature, 1),Foods.can_empty, Items.POTIONITEM, Items.SUGAR, Fluids.DIESEL.getDict(1000) );
        CraftingManager.addShapelessAuto(new ItemStack(Foods.can_redbomb, 1),Foods.can_empty, Items.POTIONITEM, Items.SUGAR, ModItems.pellet_cluster );
        CraftingManager.addShapelessAuto(new ItemStack(Foods.can_mrsugar, 1),Foods.can_empty, Items.POTIONITEM, Items.SUGAR, F.dust() );
        CraftingManager.addShapelessAuto(new ItemStack(Foods.can_overcharge, 1),Foods.can_empty, Items.POTIONITEM, Items.SUGAR, S.dust() );
        CraftingManager.addShapelessAuto(new ItemStack(Foods.can_luna, 1),Foods.can_empty, Items.POTIONITEM, Items.SUGAR, Powders.powder_meteorite_tiny );
        CraftingManager.addShapelessAuto(new ItemStack(Foods.mucho_mango, 1),Items.POTIONITEM, Items.SUGAR, Items.SUGAR, KEY_ORANGE );

        //Canteens
        CraftingManager.addRecipeAuto(new ItemStack(Foods.canteen_vodka, 1),"O", "P", 'O', Items.POTATO, 'P', STEEL.plate() );

        //Soda
        CraftingManager.addRecipeAuto(new ItemStack(Foods.bottle_empty, 6)," G ", "G G", "GGG", 'G', KEY_ANYPANE );
        CraftingManager.addShapelessAuto(new ItemStack(Foods.bottle_nuka, 1),Foods.bottle_empty, Items.POTIONITEM, Items.SUGAR, COAL.dust() );
        CraftingManager.addShapelessAuto(new ItemStack(Foods.bottle_cherry, 1),Foods.bottle_empty, Items.POTIONITEM, Items.SUGAR, REDSTONE.dust() );
        CraftingManager.addShapelessAuto(new ItemStack(Foods.bottle_quantum, 1),Foods.bottle_empty, Items.POTIONITEM, Items.SUGAR, ModItems.trinitite );
        CraftingManager.addShapelessAuto(new ItemStack(Foods.bottle_sparkle),Foods.bottle_nuka, Items.CARROT, GOLD.nugget() );
        CraftingManager.addShapelessAuto(new ItemStack(Foods.bottle_rad),Foods.bottle_quantum, Items.CARROT, GOLD.nugget() );
        CraftingManager.addRecipeAuto(new ItemStack(Foods.bottle2_empty, 6)," G ", "G G", "G G", 'G', KEY_ANYPANE );
        CraftingManager.addShapelessAuto(new ItemStack(Foods.bottle2_korl, 1),Foods.bottle2_empty, Items.POTIONITEM, Items.SUGAR, CU.dust() );
        CraftingManager.addShapelessAuto(new ItemStack(Foods.bottle2_fritz, 1),Foods.bottle2_empty, Items.POTIONITEM, Items.SUGAR, W.dust() );

        //Syringes
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_empty, 6),"P", "C", "B", 'B', Item.getItemFromBlock(Blocks.IRON_BARS), 'C', new ItemStack(ModItems.cell), 'P', IRON.plate() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_antidote, 6),"SSS", "PMP", "SSS", 'S', ModItems.syringe_empty, 'P', Items.PUMPKIN_SEEDS, 'M', Items.MILK_BUCKET );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_antidote, 6),"SPS", "SMS", "SPS", 'S', ModItems.syringe_empty, 'P', Items.PUMPKIN_SEEDS, 'M', Items.MILK_BUCKET );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_antidote, 6),"SSS", "PMP", "SSS", 'S', ModItems.syringe_empty, 'P', Items.PUMPKIN_SEEDS, 'M', Items.REEDS );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_antidote, 6),"SPS", "SMS", "SPS", 'S', ModItems.syringe_empty, 'P', Items.PUMPKIN_SEEDS, 'M', Items.REEDS );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_poison, 1),"SLS", "LCL", "SLS", 'C', ModItems.syringe_empty, 'S', Items.SPIDER_EYE, 'L', PB.dust() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_poison, 1),"SLS", "LCL", "SLS", 'C', ModItems.syringe_empty, 'S', Items.SPIDER_EYE, 'L', Powders.powder_poison );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_awesome, 1),"SPS", "NCN", "SPS", 'C', ModItems.syringe_empty, 'S', S.dust(), 'P', PU239.nugget(), 'N', PU238.nugget() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_awesome, 1),"SNS", "PCP", "SNS", 'C', ModItems.syringe_empty, 'S', S.dust(), 'P', PU239.nugget(), 'N', PU238.nugget() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_metal_empty, 6),"P", "C", "B", 'B', Blocks.IRON_BARS, 'C', RetroRods.rod_empty, 'P', IRON.plate() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_metal_stimpak, 1)," N ", "NSN", " N ", 'N', Items.NETHER_WART, 'S', ModItems.syringe_metal_empty );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.syringe_metal_stimpak, 1),Powders.nitra_small, Powders.nitra_small, Powders.nitra_small, ModItems.syringe_metal_empty );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_metal_medx, 1)," N ", "NSN", " N ", 'N', Items.QUARTZ, 'S', ModItems.syringe_metal_empty );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_metal_psycho, 1)," N ", "NSN", " N ", 'N', Items.GLOWSTONE_DUST, 'S', ModItems.syringe_metal_empty );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_metal_super, 1)," N ", "PSP", "L L", 'N', Foods.bottle_nuka, 'P', STEEL.plate(), 'S', ModItems.syringe_metal_stimpak, 'L', Items.LEATHER );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_metal_super, 1)," N ", "PSP", "L L", 'N', Foods.bottle_nuka, 'P', STEEL.plate(), 'S', ModItems.syringe_metal_stimpak, 'L', ANY_RUBBER.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_metal_super, 1)," N ", "PSP", "L L", 'N', Foods.bottle_cherry, 'P', STEEL.plate(), 'S', ModItems.syringe_metal_stimpak, 'L', Items.LEATHER );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.syringe_metal_super, 1)," N ", "PSP", "L L", 'N', Foods.bottle_cherry, 'P', STEEL.plate(), 'S', ModItems.syringe_metal_stimpak, 'L', ANY_RUBBER.ingot() );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.syringe_taint),Foods.bottle2_empty, ModItems.syringe_metal_empty, ModItems.ducttape, Powders.powder_magic, SA326.nugget(), Items.POTIONITEM );

        //Medicine
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.pill_iodine, 8),"IF", 'I', I.dust(), 'F', F.dust() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.plan_c, 1),"PFP", 'P', Powders.powder_poison, 'F', F.dust() );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.radx, 1),COAL.dust(), COAL.dust(), F.dust() );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.fmn, 1),COAL.dust(), PO210.dust(), SR.dust() );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.five_htp, 1),COAL.dust(), EUPH.dust(), Foods.canteen_vodka );
        CraftingManager.addShapelessAuto(new ItemStack(Inserts.cigarette, 16),ASBESTOS.ingot(), ANY_TAR.any(), PO210.nugget(), DictFrame.fromOne(ModItems.plant_item, ItemEnums.EnumPlantType.TOBACCO) );
        CraftingManager.addShapelessAuto(new ItemStack(Inserts.crackpipe, 1),ModItems.catalytic_converter );

        if(GeneralConfig.enableLBSM && GeneralConfig.enableLBSMSimpleMedicineRecipes) {
            CraftingManager.addShapelessAuto(new ItemStack(ModItems.siox, 8),COAL.dust(), ASBESTOS.dust(), GOLD.nugget() );
            CraftingManager.addShapelessAuto(new ItemStack(ModItems.xanax, 1),COAL.dust(), KNO.dust(), NETHERQUARTZ.dust() );
        } else {
            CraftingManager.addShapelessAuto(new ItemStack(ModItems.siox, 8),COAL.dust(), ASBESTOS.dust(), Nuggies.nugget_bismuth );
            CraftingManager.addShapelessAuto(new ItemStack(ModItems.xanax, 1),COAL.dust(), KNO.dust(), BR.dust() );
        }

        //Med bags
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.med_bag, 1),"LLL", "SIS", "LLL", 'L', Items.LEATHER, 'S', ModItems.syringe_metal_stimpak, 'I', ModItems.syringe_antidote );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.med_bag, 1),"LLL", "SIS", "LLL", 'L', Items.LEATHER, 'S', ModItems.syringe_metal_stimpak, 'I', ModItems.pill_iodine );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.med_bag, 1),"LL", "SI", "LL", 'L', Items.LEATHER, 'S', ModItems.syringe_metal_super, 'I', ModItems.radaway );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.med_bag, 1),"LLL", "SIS", "LLL", 'L', ANY_RUBBER.ingot(), 'S', ModItems.syringe_metal_stimpak, 'I', ModItems.syringe_antidote );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.med_bag, 1),"LLL", "SIS", "LLL", 'L', ANY_RUBBER.ingot(), 'S', ModItems.syringe_metal_stimpak, 'I', ModItems.pill_iodine );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.med_bag, 1),"LL", "SI", "LL", 'L', ANY_RUBBER.ingot(), 'S', ModItems.syringe_metal_super, 'I', ModItems.radaway );

        //IV Bags
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.iv_empty, 4),"S", "I", "S", 'S', ANY_RUBBER.ingot(), 'I', IRON.plate() );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.iv_xp_empty, 1),ModItems.iv_empty, Powders.powder_magic );

        //Radaway
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.radaway, 1),ModItems.iv_blood, COAL.dust(), Items.PUMPKIN_SEEDS );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.radaway_strong, 1),ModItems.radaway, ModBlocks.mush );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.radaway_flush, 1),ModItems.radaway_strong, I.dust() );

        //Cladding
        CraftingManager.addShapelessAuto(new ItemStack(Inserts.cladding_paint, 1),PB.nugget(), PB.nugget(), PB.nugget(), PB.nugget(), Items.CLAY_BALL, Items.GLASS_BOTTLE );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.cladding_rubber, 1),"RCR", "CDC", "RCR", 'R', ANY_RUBBER.ingot(), 'C', COAL.dust(), 'D', ModItems.ducttape );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.cladding_lead, 1),"DPD", "PRP", "DPD", 'R', Inserts.cladding_rubber, 'P', PB.plate(), 'D', ModItems.ducttape );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.cladding_desh, 1),"DPD", "PRP", "DPD", 'R', Inserts.cladding_lead, 'P', ModItems.plate_desh, 'D', ModItems.ducttape );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.cladding_ghiorsium, 1),"DPD", "PRP", "DPD", 'R', Inserts.cladding_desh, 'P', Ingots.ingot_gh336, 'D', ModItems.ducttape );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.cladding_obsidian, 1),"OOO", "PDP", "OOO", 'O', Blocks.OBSIDIAN, 'P', STEEL.plate(), 'D', ModItems.ducttape );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.cladding_iron, 1),"OOO", "PDP", "OOO", 'O', IRON.plate(), 'P', ModItems.plate_polymer, 'D', ModItems.ducttape );

        //Inserts
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.insert_steel, 1),"DPD", "PSP", "DPD", 'D', ModItems.ducttape, 'P', IRON.plate(), 'S', STEEL.block() );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.insert_du, 1),"DPD", "PSP", "DPD", 'D', ModItems.ducttape, 'P', IRON.plate(), 'S', U238.block() );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.insert_ghiorsium, 1),"DPD", "PSP", "DPD", 'D', ModItems.ducttape, 'P', GH336.ingot(), 'S', U238.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.insert_polonium, 1),"DPD", "PSP", "DPD", 'D', ModItems.ducttape, 'P', IRON.plate(), 'S', PO210.block() );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.insert_era, 1),"DPD", "PSP", "DPD", 'D', ModItems.ducttape, 'P', IRON.plate(), 'S', Ingots.ingot_semtex );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.insert_kevlar, 1),"KIK", "IDI", "KIK", 'K', ModItems.plate_kevlar, 'I', ANY_RUBBER.ingot(), 'D', ModItems.ducttape );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.insert_sapi, 1),"PKP", "DPD", "PKP", 'P', ANY_PLASTIC.ingot(), 'K', Inserts.insert_kevlar, 'D', ModItems.ducttape );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.insert_esapi, 1),"PKP", "DSD", "PKP", 'P', ANY_PLASTIC.ingot(), 'K', Inserts.insert_sapi, 'D', ModItems.ducttape, 'S', WEAPONSTEEL.plate() );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.insert_xsapi, 1),"PKP", "DSD", "PKP", 'P', ASBESTOS.ingot(), 'K', Inserts.insert_esapi, 'D', ModItems.ducttape, 'S', BIGMT.plate() );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.insert_yharonite, 1),"YIY", "IYI", "YIY", 'Y', Billets.billet_yharonite, 'I', Inserts.insert_du );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.australium_iii, 1),"WSW", "PAP", "SPS", 'S', STEEL.plateWelded(), 'P', ANY_PLASTIC.ingot(), 'A', AUSTRALIUM.ingot(), 'W', GOLD.wireDense() );

        //Servos
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.servo_set, 1),"MBM", "PBP", "MBM", 'M', ModItems.motor, 'B', STEEL.bolt(), 'P', IRON.plate() );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.servo_set_desh, 1),"MBM", "PSP", "MBM", 'M', ModItems.motor_desh, 'B', DURA.bolt(), 'P', ALLOY.plate(), 'S', Inserts.servo_set );

        //Helmet Mods
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.attachment_mask, 1),"DID", "IGI", " F ", 'D', ModItems.ducttape, 'I', ANY_RUBBER.ingot(), 'G', KEY_ANYPANE, 'F', IRON.plate() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.attachment_mask_mono, 1)," D ", "DID", " F ", 'D', ModItems.ducttape, 'I', ANY_RUBBER.ingot(), 'F', IRON.plate() );

        //Boot Mods
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.pads_rubber, 1),"P P", "IDI", "P P", 'P', ANY_RUBBER.ingot(), 'I', IRON.plate(), 'D', ModItems.ducttape );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.pads_slime, 1),"SPS", "DSD", "SPS", 'S', KEY_SLIME, 'P', Inserts.pads_rubber, 'D', ModItems.ducttape );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.pads_static, 1),"CDC", "ISI", "CDC", 'C', CU.ingot(), 'D', ModItems.ducttape, 'I', ANY_RUBBER.ingot(), 'S', Inserts.pads_slime );

        //Batteries
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.armor_battery, 1),"PCP", "PCP", "PCP", 'P', STEEL.plate(), 'C', ModBlocks.capacitor_gold );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.armor_battery_mk2, 1),"PCP", "PCP", "PCP", 'P', ANY_PLASTIC.ingot(), 'C', ModBlocks.capacitor_niobium );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.armor_battery_mk3, 1),"PCP", "PCP", "PCP", 'P', GOLD.plate(), 'C', ModBlocks.capacitor_tantalium );

        //Special Mods
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.horseshoe_magnet, 1),"L L", "I I", "ILI", 'L', Inserts.lodestone, 'I', IRON.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.industrial_magnet, 1),"SMS", " B ", "SMS", 'S', STEEL.ingot(), 'M', Inserts.horseshoe_magnet, 'B', ModBlocks.fusion_conductor );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.heart_container, 1),"HAH", "ACA", "HAH", 'H', Inserts.heart_piece, 'A', AL.ingot(), 'C', Foods.coin_creeper );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.heart_booster, 1),"GHG", "MCM", "GHG", 'G', GOLD.ingot(), 'H', Inserts.heart_container, 'M', Inserts.morning_glory, 'C', Foods.coin_maskman );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.heart_fab, 1),"GHG", "MCM", "GHG", 'G', PO210.billet(), 'H', Inserts.heart_booster, 'M', ANY_COKE.gem(), 'C', Foods.coin_worm );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.ink, 1),"FPF", "PIP", "FPF", 'F', new ItemStack(Blocks.RED_FLOWER, 1, OreDictionary.WILDCARD_VALUE), 'P', Inserts.armor_polish, 'I', KEY_BLACK );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.bathwater_mk2, 1),"MWM", "WBW", "MWM", 'M', ModItems.bottle_mercury, 'W', ModItems.nuclear_waste, 'B', Inserts.bathwater );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.back_tesla, 1),"DGD", "GTG", "DGD", 'D', ModItems.ducttape, 'G', GOLD.wireFine(), 'T', ModBlocks.tesla );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.medal_liquidator, 1),"GBG", "BFB", "GBG", 'G', AU198.nugget(), 'B', B.ingot(), 'F', ModItems.debris_fuel );
        CraftingManager.addShapelessAuto(new ItemStack(Inserts.injector_5htp, 1),ModItems.five_htp, DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC), BIGMT.plate() );
        CraftingManager.addShapelessAuto(new ItemStack(Inserts.injector_knife, 1),Inserts.injector_5htp, Items.IRON_SWORD );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.shackles, 1),"CIC", "C C", "I I", 'I', Ingots.ingot_chainsteel, 'C', ModBlocks.chain );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.black_diamond, 1),"NIN", "IGI", "NIN", 'N', AU198.nugget(), 'I', Inserts.ink, 'G', VOLCANIC.gem() );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.protection_charm, 1)," M ", "MDM", " M ", 'M', ModItems.fragment_meteorite, 'D', DIAMOND.gem() );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.meteor_charm, 1)," M ", "MDM", " M ", 'M', ModItems.fragment_meteorite, 'D', VOLCANIC.gem() );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.neutrino_lens, 1),"PSP", "SCS", "PSP", 'P', ANY_PLASTIC.ingot(), 'S', STAR.ingot(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BISMOID) );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.gas_tester, 1),"G", "C", "I", 'G', GOLD.plate(), 'C', DictFrame.fromOne(ModItems.circuit, ItemEnums.EnumCircuitType.VACUUM_TUBE), 'I', IRON.plate() );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.defuser_gold, 1),"GPG", "PRP", "GPG", 'G', Items.GUNPOWDER, 'P', GOLD.plate(), 'R', "record" );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.ballistic_gauntlet, 1)," WS", "WRS", " RS", 'W', CU.wireFine(), 'R', ModItems.ring_starmetal, 'S', STEEL.plate() );
        CraftingManager.addRecipeAuto(new ItemStack(Inserts.night_vision, 1), "P P", "GCG", 'P', ANY_PLASTIC.ingot(), 'G', KEY_ANYGLASS, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC));
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.jetpack_glider), "CSC", "DJD", "T T", 'J', ModItems.jetpack_boost, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED), 'D', ModItems.plate_desh, 'T', ModItems.thruster_nuclear, 'S', ModItems.motor);

        //Stealth boy
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.stealth_boy, 1)," B", "LI", "LC", 'B', Blocks.STONE_BUTTON, 'L', Items.LEATHER, 'I', STEEL.ingot(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC) );

        //RD40 Filters
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gas_mask_filter, 1),"I", "F", 'F', ModItems.filter_coal, 'I', IRON.plate() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gas_mask_filter_mono, 1),"ZZZ", "ZCZ", "ZZZ", 'Z', ZR.nugget(), 'C', ModItems.catalyst_clay );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gas_mask_filter_combo, 1),"ZCZ", "CFC", "ZCZ", 'Z', ZR.ingot(), 'C', ModItems.catalyst_clay, 'F', ModItems.gas_mask_filter );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gas_mask_filter_rag, 1),"I", "F", 'F', ModItems.rag_damp, 'I', IRON.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gas_mask_filter_piss, 1),"I", "F", 'F', ModItems.rag_piss, 'I', IRON.ingot() );
    }
}
