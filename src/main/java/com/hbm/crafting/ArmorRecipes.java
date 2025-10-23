package com.hbm.crafting;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ItemEnums;
import com.hbm.items.ItemEnums.EnumCircuitType;
import com.hbm.items.ModItems;
import com.hbm.items.ModItems.*;
import com.hbm.items.ModItems.Materials.Billets;
import com.hbm.items.ModItems.Materials.Crystals;
import com.hbm.items.ModItems.Materials.Ingots;
import com.hbm.main.CraftingManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import static com.hbm.inventory.OreDictManager.*;

/**
 * For player armor
 * @author hbm
 */
public class ArmorRecipes {

    public static void register() {

        //Armor mod table
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.machine_armor_table, 1),"PPP", "TCT", "TST", 'P', STEEL.plate(), 'T', W.ingot(), 'C', Blocks.CRAFTING_TABLE, 'S', STEEL.block() );

        //Regular armor
        addHelmet(STEEL.ingot(), ArmorSets.steel_helmet);
        addChest(STEEL.ingot(), ArmorSets.steel_plate);
        addLegs(STEEL.ingot(), ArmorSets.steel_legs);
        addBoots(STEEL.ingot(), ArmorSets.steel_boots);
        addHelmet(TI.ingot(), ArmorSets.titanium_helmet);
        addChest(TI.ingot(), ArmorSets.titanium_plate);
        addLegs(TI.ingot(), ArmorSets.titanium_legs);
        addBoots(TI.ingot(), ArmorSets.titanium_boots);
        addHelmet(ALLOY.ingot(), ArmorSets.alloy_helmet);
        addChest(ALLOY.ingot(), ArmorSets.alloy_plate);
        addLegs(ALLOY.ingot(), ArmorSets.alloy_legs);
        addBoots(ALLOY.ingot(), ArmorSets.alloy_boots);
        addHelmet(CMB.ingot(), ArmorSets.cmb_helmet);
        addChest(CMB.ingot(), ArmorSets.cmb_plate);
        addLegs(CMB.ingot(), ArmorSets.cmb_legs);
        addBoots(CMB.ingot(), ArmorSets.cmb_boots);
        addHelmet(ModItems.rag, ArmorSets.robes_helmet);
        addChest(ModItems.rag, ArmorSets.robes_plate);
        addLegs(ModItems.rag, ArmorSets.robes_legs);
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.robes_boots, 1),"R R", "P P", 'R', ModItems.rag, 'P', ANY_RUBBER.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.cobalt_helmet, 1),"ECE", 'E', CO.billet(), 'C', ArmorSets.steel_helmet );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.cobalt_plate, 1)," E ", "ECE"," E ", 'E', CO.billet(), 'C', ArmorSets.steel_plate );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.cobalt_legs, 1),"ECE", "E E", 'E', CO.billet(), 'C', ArmorSets.steel_legs );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.cobalt_boots, 1),"ECE", 'E', CO.billet(), 'C', ArmorSets.steel_boots );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.security_helmet, 1),"SSS", "IGI", 'S', STEEL.plate(), 'I', ANY_RUBBER.ingot(), 'G', KEY_ANYPANE );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.security_plate, 1),"KWK", "IKI", "WKW", 'K', ModItems.plate_kevlar, 'I', ANY_PLASTIC.ingot(), 'W', new ItemStack(Blocks.WOOL, 1, OreDictionary.WILDCARD_VALUE) );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.security_legs, 1),"IWI", "K K", "W W", 'K', ModItems.plate_kevlar, 'I', ANY_PLASTIC.ingot(), 'W', new ItemStack(Blocks.WOOL, 1, OreDictionary.WILDCARD_VALUE) );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.security_boots, 1),"P P", "I I", 'P', STEEL.plate(), 'I', ANY_RUBBER.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.dnt_helmet, 1),"EEE", "EE ", 'E', DNT.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.dnt_plate, 1),"EE ", "EEE", "EEE", 'E', DNT.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.dnt_legs, 1),"EE ", "EEE", "E E", 'E', DNT.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.dnt_boots, 1),"  E", "E  ", "E E", 'E', DNT.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.zirconium_legs, 1),"EEE", "E E", "E E", 'E', ZR.ingot() );

        //Power armor
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.t51_helmet, 1),"PPC", "PBP", "IXI", 'P', ModItems.plate_armor_titanium, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC), 'I', ANY_RUBBER.ingot(), 'X', ModItems.gas_mask_m65, 'B', ArmorSets.titanium_helmet );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.t51_plate, 1),"MPM", "TBT", "PPP", 'M', ModItems.motor, 'P', ModItems.plate_armor_titanium, 'T', ModItems.gas_empty, 'B', ArmorSets.titanium_plate );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.t51_legs, 1),"MPM", "PBP", "P P", 'M', ModItems.motor, 'P', ModItems.plate_armor_titanium, 'B', ArmorSets.titanium_legs );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.t51_boots, 1),"P P", "PBP", 'P', ModItems.plate_armor_titanium, 'B', ArmorSets.titanium_boots );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.ajr_helmet, 1),"PPC", "PBP", "IXI", 'P', ModItems.plate_armor_ajr, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC), 'I', ANY_PLASTIC.ingot(), 'X', ModItems.gas_mask_m65, 'B', ArmorSets.alloy_helmet );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.ajr_plate, 1),"MPM", "TBT", "PPP", 'M', ModItems.motor_desh, 'P', ModItems.plate_armor_ajr, 'T', ModItems.gas_empty, 'B', ArmorSets.alloy_plate );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.ajr_legs, 1),"MPM", "PBP", "P P", 'M', ModItems.motor_desh, 'P', ModItems.plate_armor_ajr, 'B', ArmorSets.alloy_legs );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.ajr_boots, 1),"P P", "PBP", 'P', ModItems.plate_armor_ajr, 'B', ArmorSets.alloy_boots );
        CraftingManager.addShapelessAuto(new ItemStack(ArmorSets.ajro_helmet, 1),ArmorSets.ajr_helmet, KEY_RED, KEY_BLACK );
        CraftingManager.addShapelessAuto(new ItemStack(ArmorSets.ajro_plate, 1),ArmorSets.ajr_plate, KEY_RED, KEY_BLACK );
        CraftingManager.addShapelessAuto(new ItemStack(ArmorSets.ajro_legs, 1),ArmorSets.ajr_legs, KEY_RED, KEY_BLACK );
        CraftingManager.addShapelessAuto(new ItemStack(ArmorSets.ajro_boots, 1),ArmorSets.ajr_boots, KEY_RED, KEY_BLACK );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.bj_helmet, 1),"SBS", " C ", " I ", 'S', Items.STRING, 'B', new ItemStack(Blocks.WOOL, 1, 15), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED), 'I', STAR.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.bj_plate, 1),"N N", "MSM", "NCN", 'N', ModItems.plate_armor_lunar, 'M', ModItems.motor_desh, 'S', ArmorSets.starmetal_plate, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED) );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.bj_plate_jetpack, 1),"NFN", "TPT", "ICI", 'N', ModItems.plate_armor_lunar, 'F', ModItems.fins_quad_titanium, 'T', new ItemStack(ModItems.fluid_tank_full, 1, Fluids.XENON.getID()), 'P', ArmorSets.bj_plate, 'I', MissileParts.mp_thruster_10_xenon, 'C', Crystals.crystal_phosphorus );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.bj_legs, 1),"MBM", "NSN", "N N", 'N', ModItems.plate_armor_lunar, 'M', ModItems.motor_desh, 'S', ArmorSets.starmetal_legs, 'B', ModBlocks.block_starmetal );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.bj_boots, 1),"N N", "BSB", 'N', ModItems.plate_armor_lunar, 'S', ArmorSets.starmetal_boots, 'B', ModBlocks.block_starmetal );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.hev_helmet, 1),"PPC", "PBP", "IFI", 'P', ModItems.plate_armor_hev, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC), 'B', ArmorSets.titanium_helmet, 'I', ANY_PLASTIC.ingot(), 'F', ModItems.gas_mask_filter );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.hev_plate, 1),"MPM", "IBI", "PPP", 'P', ModItems.plate_armor_hev, 'B', ArmorSets.titanium_plate, 'I', ANY_PLASTIC.ingot(), 'M', ModItems.motor_desh );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.hev_legs, 1),"MPM", "IBI", "P P", 'P', ModItems.plate_armor_hev, 'B', ArmorSets.titanium_legs, 'I', ANY_PLASTIC.ingot(), 'M', ModItems.motor_desh );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.hev_boots, 1),"P P", "PBP", 'P', ModItems.plate_armor_hev, 'B', ArmorSets.titanium_boots );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.fau_helmet, 1),"PWP", "PBP", "FSF", 'P', ModItems.plate_armor_fau, 'W', new ItemStack(Blocks.WOOL, 1, 14), 'B', ArmorSets.starmetal_helmet, 'F', ModItems.gas_mask_filter, 'S', STEEL.pipe() );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.fau_plate, 1),"MCM", "PBP", "PSP", 'M', ModItems.motor_desh, 'C', ModItems.demon_core_closed, 'P', ModItems.plate_armor_fau, 'B', ArmorSets.starmetal_plate, 'S', ModBlocks.ancient_scrap );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.fau_legs, 1),"MPM", "PBP", "PDP", 'M', ModItems.motor_desh, 'P', ModItems.plate_armor_fau, 'B', ArmorSets.starmetal_legs, 'D', Billets.billet_polonium );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.fau_boots, 1),"PDP", "PBP", 'P', ModItems.plate_armor_fau, 'D', Billets.billet_polonium, 'B', ArmorSets.starmetal_boots );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.dns_helmet, 1),"PCP", "PBP", "PSP", 'P', ModItems.plate_armor_dnt, 'S', Ingots.ingot_chainsteel, 'B', ArmorSets.bj_helmet, 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.QUANTUM) );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.dns_plate, 1),"PCP", "PBP", "PSP", 'P', ModItems.plate_armor_dnt, 'S', Ingots.ingot_chainsteel, 'B', ArmorSets.bj_plate_jetpack, 'C', ModItems.singularity_spark );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.dns_legs, 1),"PCP", "PBP", "PSP", 'P', ModItems.plate_armor_dnt, 'S', Ingots.ingot_chainsteel, 'B', ArmorSets.bj_legs, 'C', Foods.coin_worm );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.dns_boots, 1),"PCP", "PBP", "PSP", 'P', ModItems.plate_armor_dnt, 'S', Ingots.ingot_chainsteel, 'B', ArmorSets.bj_boots, 'C', ModItems.demon_core_closed );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.rpa_helmet, 1),"KPK", "PLP", " F ", 'L', DictFrame.fromOne(ModItems.parts_legendary, ItemEnums.EnumLegendaryType.TIER2), 'K', ModItems.plate_kevlar, 'P', ModItems.plate_armor_ajr, 'F', ModItems.gas_mask_filter_combo );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.rpa_plate, 1),"P P", "MLM", "PKP", 'L', DictFrame.fromOne(ModItems.parts_legendary, ItemEnums.EnumLegendaryType.TIER2), 'K', ModItems.plate_kevlar, 'P', ModItems.plate_armor_ajr, 'M', ModItems.motor_desh );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.rpa_legs, 1),"MPM", "KLK", "P P", 'L', DictFrame.fromOne(ModItems.parts_legendary, ItemEnums.EnumLegendaryType.TIER2), 'K', ModItems.plate_kevlar, 'P', ModItems.plate_armor_ajr, 'M', ModItems.motor_desh );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.rpa_boots, 1),"KLK", "P P", 'L', DictFrame.fromOne(ModItems.parts_legendary, ItemEnums.EnumLegendaryType.TIER2), 'K', ModItems.plate_kevlar, 'P', ModItems.plate_armor_ajr );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.steamsuit_helmet, 1),"DCD", "CXC", " F ", 'D', DESH.ingot(), 'C', CU.plate(), 'X', ArmorSets.steel_helmet, 'F', ModItems.gas_mask_filter );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.steamsuit_plate, 1),"C C", "DXD", "CFC", 'D', DESH.ingot(), 'C', CU.plate(), 'X', ArmorSets.steel_plate, 'F', ModItems.tank_steel );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.steamsuit_legs, 1),"CCC", "DXD", "C C", 'D', DESH.ingot(), 'C', CU.plate(), 'X', ArmorSets.steel_legs );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.steamsuit_boots, 1),"C C", "DXD", 'D', DESH.ingot(), 'C', CU.plate(), 'X', ArmorSets.steel_boots );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.dieselsuit_helmet, 1),"W W", "W W", "SCS", 'W', new ItemStack(Blocks.WOOL, 1, 14), 'S', STEEL.ingot(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ANALOG) );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.dieselsuit_plate, 1),"W W", "CDC", "SWS", 'W', new ItemStack(Blocks.WOOL, 1, 14), 'S', STEEL.ingot(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ANALOG), 'D', ModBlocks.machine_diesel );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.dieselsuit_legs, 1),"M M", "S S", "W W", 'W', new ItemStack(Blocks.WOOL, 1, 14), 'S', STEEL.ingot(), 'M', ModItems.motor );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.dieselsuit_boots, 1),"W W", "S S", 'W', new ItemStack(Blocks.WOOL, 1, 14), 'S', STEEL.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.envsuit_helmet, 1),"TCT", "TGT", "RRR", 'T', TI.plate(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.CHIP), 'G', KEY_ANYPANE, 'R', RUBBER.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.envsuit_plate, 1),"T T", "TCT", "RRR", 'T', TI.plate(), 'C', TI.plateCast(), 'R', RUBBER.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.envsuit_legs, 1),"TCT", "R R", "T T", 'T', TI.plate(), 'C', TI.plateCast(), 'R', RUBBER.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.envsuit_boots, 1),"R R", "T T", 'T', TI.plate(), 'R', RUBBER.ingot() );

        //Bismuth fursui- I mean armor
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.bismuth_helmet, 1),"GPP", "P  ", "FPP", 'G', Items.GOLD_INGOT, 'P', ModItems.plate_bismuth, 'F', ModItems.rag );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.bismuth_plate, 1),"RWR", "PCP", "SFS", 'R', Crystals.crystal_rare, 'W', GOLD.wireFine(), 'P', ModItems.plate_bismuth, 'C', ModItems.laser_crystal_bismuth, 'S', ModItems.ring_starmetal, 'F', ModItems.rag );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.bismuth_legs, 1),"FSF", "   ", "FSF", 'F', ModItems.rag, 'S', ModItems.ring_starmetal );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.bismuth_boots, 1),"W W", "P P", 'W', GOLD.wireFine(), 'P', ModItems.plate_bismuth );

        //Euphemium armor
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.euphemium_helmet, 1),"EEE", "E E", 'E', ModItems.plate_euphemium );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.euphemium_plate, 1),"EWE", "EEE", "EEE", 'E', ModItems.plate_euphemium, 'W', ModItems.watch );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.euphemium_legs, 1),"EEE", "E E", "E E", 'E', ModItems.plate_euphemium );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.euphemium_boots, 1),"E E", "E E", 'E', ModItems.plate_euphemium );

        //Jetpacks
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.jetpack_fly, 1),"ACA", "TLT", "D D", 'A', AL.plate(), 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC), 'T', ModItems.tank_steel, 'L', Items.LEATHER, 'D', ModItems.thruster_small );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.jetpack_break, 1),"ICI", "TJT", "I I", 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.BASIC), 'T', Ingots.ingot_dura_steel, 'J', ModItems.jetpack_fly, 'I', ModItems.plate_polymer );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.jetpack_vector, 1),"TCT", "MJM", "B B", 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED), 'T', ModItems.tank_steel, 'J', ModItems.jetpack_break, 'M', ModItems.motor, 'B', DURA.bolt() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.jetpack_boost, 1),"PCP", "DJD", "PAP", 'C', DictFrame.fromOne(ModItems.circuit, EnumCircuitType.ADVANCED), 'P', BIGMT.plate(), 'D', DESH.ingot(), 'J', ModItems.jetpack_vector, 'A', CU.plateCast() );

        //Hazmat
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.hazmat_helmet, 1),"EEE", "EIE", " P ", 'E', ModItems.hazmat_cloth, 'I', KEY_ANYPANE, 'P', IRON.plate() );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.hazmat_plate, 1),"E E", "EEE", "EEE", 'E', ModItems.hazmat_cloth );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.hazmat_legs, 1),"EEE", "E E", "E E", 'E', ModItems.hazmat_cloth );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.hazmat_boots, 1),"E E", "E E", 'E', ModItems.hazmat_cloth );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.hazmat_helmet_red, 1),"EEE", "IEI", "EFE", 'E', ModItems.hazmat_cloth_red, 'I', KEY_ANYPANE, 'F', IRON.plate() );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.hazmat_plate_red, 1),"E E", "EEE", "EEE", 'E', ModItems.hazmat_cloth_red );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.hazmat_legs_red, 1),"EEE", "E E", "E E", 'E', ModItems.hazmat_cloth_red );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.hazmat_boots_red, 1),"E E", "E E", 'E', ModItems.hazmat_cloth_red );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.hazmat_helmet_grey, 1),"EEE", "IEI", "EFE", 'E', ModItems.hazmat_cloth_grey, 'I', KEY_ANYPANE, 'F', IRON.plate() );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.hazmat_plate_grey, 1),"E E", "EEE", "EEE", 'E', ModItems.hazmat_cloth_grey );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.hazmat_legs_grey, 1),"EEE", "E E", "E E", 'E', ModItems.hazmat_cloth_grey );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.hazmat_boots_grey, 1),"E E", "E E", 'E', ModItems.hazmat_cloth_grey );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.asbestos_helmet, 1),"EEE", "EIE", 'E', ModItems.asbestos_cloth, 'I', "plateGold" );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.asbestos_plate, 1),"E E", "EEE", "EEE", 'E', ModItems.asbestos_cloth );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.asbestos_legs, 1),"EEE", "E E", "E E", 'E', ModItems.asbestos_cloth );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.asbestos_boots, 1),"E E", "E E", 'E', ModItems.asbestos_cloth );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.hazmat_paa_helmet, 1),"EEE", "IEI", " P ", 'E', ModItems.plate_paa, 'I', KEY_ANYPANE, 'P', IRON.plate() );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.hazmat_paa_plate, 1),"E E", "EEE", "EEE", 'E', ModItems.plate_paa );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.hazmat_paa_legs, 1),"EEE", "E E", "E E", 'E', ModItems.plate_paa );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.hazmat_paa_boots, 1),"E E", "E E", 'E', ModItems.plate_paa );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.paa_plate, 1),"E E", "NEN", "ENE", 'E', ModItems.plate_paa, 'N', OreDictManager.getReflector() );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.paa_legs, 1),"EEE", "N N", "E E", 'E', ModItems.plate_paa, 'N', OreDictManager.getReflector() );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.paa_boots, 1),"E E", "N N", 'E', ModItems.plate_paa, 'N', OreDictManager.getReflector() );

        //Liquidator Suit
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.liquidator_helmet, 1),"III", "CBC", "III", 'I', ANY_RUBBER.ingot(), 'C', Inserts.cladding_lead, 'B', ArmorSets.hazmat_helmet_grey );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.liquidator_plate, 1),"ICI", "TBT", "ICI", 'I', ANY_RUBBER.ingot(), 'C', Inserts.cladding_lead, 'B', ArmorSets.hazmat_plate_grey, 'T', ModItems.gas_empty );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.liquidator_legs, 1),"III", "CBC", "I I", 'I', ANY_RUBBER.ingot(), 'C', Inserts.cladding_lead, 'B', ArmorSets.hazmat_legs_grey );
        CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.liquidator_boots, 1),"ICI", "IBI", 'I', ANY_RUBBER.ingot(), 'C', Inserts.cladding_lead, 'B', ArmorSets.hazmat_boots_grey );

        //Masks
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.goggles, 1),"P P", "GPG", 'G', KEY_ANYPANE, 'P', STEEL.plate() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gas_mask, 1),"PPP", "GPG", " F ", 'G', KEY_ANYPANE, 'P', STEEL.plate(), 'F', IRON.plate() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gas_mask_m65, 1),"PPP", "GPG", " F ", 'G', KEY_ANYPANE, 'P', ANY_RUBBER.ingot(), 'F', IRON.plate() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gas_mask_olde, 1),"PPP", "GPG", " F ", 'G', KEY_ANYPANE, 'P', Items.LEATHER, 'F', IRON.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.gas_mask_mono, 1)," P ", "PPP", " F ", 'P', ANY_RUBBER.ingot(), 'F', IRON.plate() );
        CraftingManager.addRecipeAuto(new ItemStack(ToolSets.mask_of_infamy, 1),"III", "III", " I ", 'I', IRON.plate() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.ashglasses, 1),"I I", "GPG", 'I', ANY_RUBBER.ingot(), 'G', ModBlocks.glass_ash, 'P', ANY_PLASTIC.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.mask_rag, 1),"RRR", 'R', ModItems.rag_damp );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.mask_piss, 1),"RRR", 'R', ModItems.rag_piss );

        //Capes
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.cape_radiation, 1),"W W", "WIW", "WDW", 'W', new ItemStack(Blocks.WOOL, 1, 11), 'D', KEY_YELLOW, 'I', ModItems.nuclear_waste );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.cape_gasmask, 1),"W W", "WIW", "WDW", 'W', new ItemStack(Blocks.WOOL, 1, 4), 'D', KEY_BLACK, 'I', ModItems.gas_mask );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.cape_schrabidium, 1),"W W", "WIW", "WDW", 'W', SA326.ingot(), 'D', KEY_BLACK, 'I', DictFrame.fromOne(ModItems.circuit, ItemEnums.EnumCircuitType.CHIP) );

        //Configged
        if(GeneralConfig.enableLBSM && GeneralConfig.enableLBSMSimpleArmorRecipes) {
            addHelmet(	STAR.ingot(), ArmorSets.starmetal_helmet);
            addChest(	STAR.ingot(), ArmorSets.starmetal_plate);
            addLegs(	STAR.ingot(), ArmorSets.starmetal_legs);
            addBoots(	STAR.ingot(), ArmorSets.starmetal_boots);
            addHelmet(	SA326.ingot(), ArmorSets.schrabidium_helmet);
            addChest(	SA326.ingot(), ArmorSets.schrabidium_plate);
            addLegs(	SA326.ingot(), ArmorSets.schrabidium_legs);
            addBoots(	SA326.ingot(), ArmorSets.schrabidium_boots);
        } else {
            CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.starmetal_helmet, 1),"EEE", "ECE", 'E', STAR.ingot(), 'C', ArmorSets.cobalt_helmet );
            CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.starmetal_plate, 1),"ECE", "EEE", "EEE", 'E', STAR.ingot(), 'C', ArmorSets.cobalt_plate );
            CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.starmetal_legs, 1),"EEE", "ECE", "E E", 'E', STAR.ingot(), 'C', ArmorSets.cobalt_legs );
            CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.starmetal_boots, 1),"E E", "ECE", 'E', STAR.ingot(), 'C', ArmorSets.cobalt_boots );
            CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.schrabidium_helmet, 1),"EEE", "ESE", " P ", 'E', SA326.ingot(), 'S', ArmorSets.starmetal_helmet, 'P', ModItems.pellet_charged );
            CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.schrabidium_plate, 1),"ESE", "EPE", "EEE", 'E', SA326.ingot(), 'S', ArmorSets.starmetal_plate, 'P', ModItems.pellet_charged );
            CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.schrabidium_legs, 1),"EEE", "ESE", "EPE", 'E', SA326.ingot(), 'S', ArmorSets.starmetal_legs, 'P', ModItems.pellet_charged );
            CraftingManager.addRecipeAuto(new ItemStack(ArmorSets.schrabidium_boots, 1),"EPE", "ESE", 'E', SA326.ingot(), 'S', ArmorSets.starmetal_boots, 'P', ModItems.pellet_charged );
        }
    }

    public static void addHelmet(Object ingot, Item pick) {
        addArmor(ingot, pick, patternHelmet);
    }
    public static void addChest(Object ingot, Item axe) {
        addArmor(ingot, axe, patternChetplate);
    }
    public static void addLegs(Object ingot, Item shovel) {
        addArmor(ingot, shovel, patternLeggings);
    }
    public static void addBoots(Object ingot, Item hoe) {
        addArmor(ingot, hoe, patternBoots);
    }

    public static void addArmor(Object ingot, Item armor, String[] pattern) {
        CraftingManager.addRecipeAuto(new ItemStack(armor),pattern, 'X', ingot );
    }

    public static final String[] patternHelmet = new String[] {"XXX", "X X"};
    public static final String[] patternChetplate = new String[] {"X X", "XXX", "XXX"};
    public static final String[] patternLeggings = new String[] {"XXX", "X X", "X X"};
    public static final String[] patternBoots = new String[] {"X X", "X X"};
}
