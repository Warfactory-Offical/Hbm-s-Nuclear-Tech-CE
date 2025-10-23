package com.hbm.crafting;

import com.hbm.blocks.BlockEnums.EnumMeteorType;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.PlantEnums;
import com.hbm.inventory.OreDictManager;
import com.hbm.items.ItemEnums;
import com.hbm.items.ModItems;
import com.hbm.items.ModItems.Foods;
import com.hbm.items.ModItems.Inserts;
import com.hbm.items.ModItems.Materials.Crystals;
import com.hbm.items.ModItems.Materials.Ingots;
import com.hbm.items.ModItems.Materials.Nuggies;
import com.hbm.items.ModItems.Materials.Powders;
import com.hbm.items.ModItems.ToolSets;
import com.hbm.items.machine.ItemArcElectrode;
import com.hbm.items.special.ItemHot;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

public class SmeltingRecipes {

    public static void AddSmeltingRec()
    {
        GameRegistry.addSmelting(Foods.glyphid_meat, new ItemStack(Foods.glyphid_meat_grilled), 1.0F);

        GameRegistry.addSmelting(Item.getItemFromBlock(ModBlocks.ore_thorium), new ItemStack(Ingots.ingot_th232), 3.0F);
        GameRegistry.addSmelting(Item.getItemFromBlock(ModBlocks.ore_uranium), new ItemStack(Ingots.ingot_uranium), 6.0F);
        GameRegistry.addSmelting(Item.getItemFromBlock(ModBlocks.ore_uranium_scorched), new ItemStack(Ingots.ingot_uranium), 6.0F);
        GameRegistry.addSmelting(Item.getItemFromBlock(ModBlocks.ore_nether_uranium), new ItemStack(Ingots.ingot_uranium), 12.0F);
        GameRegistry.addSmelting(Item.getItemFromBlock(ModBlocks.ore_nether_uranium_scorched), new ItemStack(Ingots.ingot_uranium), 12.0F);
        GameRegistry.addSmelting(Item.getItemFromBlock(ModBlocks.ore_nether_plutonium), new ItemStack(Ingots.ingot_plutonium), 24.0F);
        GameRegistry.addSmelting(Item.getItemFromBlock(ModBlocks.ore_titanium), new ItemStack(Ingots.ingot_titanium), 3.0F);
        GameRegistry.addSmelting(Item.getItemFromBlock(ModBlocks.ore_copper), new ItemStack(Ingots.ingot_copper), 2.5F);
        GameRegistry.addSmelting(Item.getItemFromBlock(ModBlocks.ore_tungsten), new ItemStack(Ingots.ingot_tungsten), 6.0F);
        GameRegistry.addSmelting(Item.getItemFromBlock(ModBlocks.ore_nether_tungsten), new ItemStack(Ingots.ingot_tungsten), 12.0F);
        GameRegistry.addSmelting(Item.getItemFromBlock(ModBlocks.ore_aluminium), OreDictManager.DictFrame.fromOne(ModItems.chunk_ore, ItemEnums.EnumChunkType.CRYOLITE, 1), 2.5F);
        GameRegistry.addSmelting(Item.getItemFromBlock(ModBlocks.ore_lead), new ItemStack(Ingots.ingot_lead), 3.0F);
        GameRegistry.addSmelting(Item.getItemFromBlock(ModBlocks.ore_beryllium), new ItemStack(Ingots.ingot_beryllium), 2.0F);
        GameRegistry.addSmelting(Item.getItemFromBlock(ModBlocks.ore_schrabidium), new ItemStack(Ingots.ingot_schrabidium), 128.0F);
        GameRegistry.addSmelting(Item.getItemFromBlock(ModBlocks.ore_nether_schrabidium), new ItemStack(Ingots.ingot_schrabidium), 256.0F);
        GameRegistry.addSmelting(Item.getItemFromBlock(ModBlocks.ore_cobalt), new ItemStack(Ingots.ingot_cobalt), 2.0F);
        GameRegistry.addSmelting(Item.getItemFromBlock(ModBlocks.ore_nether_cobalt), new ItemStack(Ingots.ingot_cobalt), 2.0F);

        GameRegistry.addSmelting(OreDictManager.DictFrame.fromOne(ModBlocks.ore_meteor, EnumMeteorType.IRON), new ItemStack(Items.IRON_INGOT, 16), 10.0F);
        GameRegistry.addSmelting(OreDictManager.DictFrame.fromOne(ModBlocks.ore_meteor, EnumMeteorType.COPPER), new ItemStack(Ingots.ingot_copper, 16), 10.0F);
        GameRegistry.addSmelting(OreDictManager.DictFrame.fromOne(ModBlocks.ore_meteor, EnumMeteorType.ALUMINIUM), OreDictManager.DictFrame.fromOne(ModItems.chunk_ore, ItemEnums.EnumChunkType.CRYOLITE, 16), 10.0F);
        GameRegistry.addSmelting(OreDictManager.DictFrame.fromOne(ModBlocks.ore_meteor, EnumMeteorType.RAREEARTH), OreDictManager.DictFrame.fromOne(ModItems.chunk_ore, ItemEnums.EnumChunkType.RARE, 16), 10.0F);
        GameRegistry.addSmelting(OreDictManager.DictFrame.fromOne(ModBlocks.ore_meteor, EnumMeteorType.COBALT), new ItemStack(Ingots.ingot_cobalt, 4), 10.0F);

        GameRegistry.addSmelting(Item.getItemFromBlock(ModBlocks.ore_gneiss_iron), new ItemStack(Items.IRON_INGOT), 5.0F);
        GameRegistry.addSmelting(Item.getItemFromBlock(ModBlocks.ore_gneiss_gold), new ItemStack(Items.GOLD_INGOT), 5.0F);
        GameRegistry.addSmelting(Item.getItemFromBlock(ModBlocks.ore_gneiss_uranium), new ItemStack(Ingots.ingot_uranium), 12.0F);
        GameRegistry.addSmelting(Item.getItemFromBlock(ModBlocks.ore_gneiss_uranium_scorched), new ItemStack(Ingots.ingot_uranium), 12.0F);
        GameRegistry.addSmelting(Item.getItemFromBlock(ModBlocks.ore_gneiss_copper), new ItemStack(Ingots.ingot_copper), 5F);
        GameRegistry.addSmelting(Item.getItemFromBlock(ModBlocks.ore_gneiss_lithium), new ItemStack(Ingots.lithium), 10F);
        GameRegistry.addSmelting(Item.getItemFromBlock(ModBlocks.ore_gneiss_schrabidium), new ItemStack(Ingots.ingot_schrabidium), 256.0F);

        GameRegistry.addSmelting(Item.getItemFromBlock(ModBlocks.ore_australium), new ItemStack(Nuggies.nugget_australium), 2.5F);
        GameRegistry.addSmelting(Powders.powder_australium, new ItemStack(Ingots.ingot_australium), 5.0F);

        GameRegistry.addSmelting(OreDictManager.DictFrame.fromOne(ModItems.briquette, ItemEnums.EnumBriquetteType.COAL), OreDictManager.DictFrame.fromOne(ModItems.coke, ItemEnums.EnumCokeType.COAL), 1.0F);
        GameRegistry.addSmelting(OreDictManager.DictFrame.fromOne(ModItems.briquette, ItemEnums.EnumBriquetteType.LIGNITE), OreDictManager.DictFrame.fromOne(ModItems.coke, ItemEnums.EnumCokeType.LIGNITE), 1.0F);
        GameRegistry.addSmelting(OreDictManager.DictFrame.fromOne(ModItems.briquette, ItemEnums.EnumBriquetteType.WOOD), new ItemStack(Items.COAL, 1, 1), 1.0F);

        GameRegistry.addSmelting(Powders.powder_lead, new ItemStack(Ingots.ingot_lead), 1.0F);
        GameRegistry.addSmelting(Powders.powder_neptunium, new ItemStack(Ingots.ingot_neptunium), 1.0F);
        GameRegistry.addSmelting(Powders.powder_polonium, new ItemStack(Ingots.ingot_polonium), 1.0F);
        GameRegistry.addSmelting(Powders.powder_schrabidium, new ItemStack(Ingots.ingot_schrabidium), 5.0F);
        GameRegistry.addSmelting(Powders.powder_schrabidate, new ItemStack(Ingots.ingot_schrabidate), 5.0F);
        GameRegistry.addSmelting(Powders.powder_euphemium, new ItemStack(Ingots.ingot_euphemium), 10.0F);
        GameRegistry.addSmelting(Powders.powder_aluminium, new ItemStack(Ingots.ingot_aluminium), 1.0F);
        GameRegistry.addSmelting(Powders.powder_beryllium, new ItemStack(Ingots.ingot_beryllium), 1.0F);
        GameRegistry.addSmelting(Powders.powder_copper, new ItemStack(Ingots.ingot_copper), 1.0F);
        GameRegistry.addSmelting(Powders.powder_gold, new ItemStack(Items.GOLD_INGOT), 1.0F);
        GameRegistry.addSmelting(Powders.powder_iron, new ItemStack(Items.IRON_INGOT), 1.0F);
        GameRegistry.addSmelting(Powders.powder_titanium, new ItemStack(Ingots.ingot_titanium), 1.0F);
        GameRegistry.addSmelting(Powders.powder_cobalt, new ItemStack(Ingots.ingot_cobalt), 1.0F);
        GameRegistry.addSmelting(Powders.powder_tungsten, new ItemStack(Ingots.ingot_tungsten), 1.0F);
        GameRegistry.addSmelting(Powders.powder_uranium, new ItemStack(Ingots.ingot_uranium), 1.0F);
        GameRegistry.addSmelting(Powders.powder_thorium, new ItemStack(Ingots.ingot_th232), 1.0F);
        GameRegistry.addSmelting(Powders.powder_plutonium, new ItemStack(Ingots.ingot_plutonium), 1.0F);
        GameRegistry.addSmelting(Powders.powder_advanced_alloy, new ItemStack(Ingots.ingot_advanced_alloy), 1.0F);
        GameRegistry.addSmelting(Powders.powder_combine_steel, new ItemStack(Ingots.ingot_combine_steel), 1.0F);
        GameRegistry.addSmelting(Powders.powder_magnetized_tungsten, new ItemStack(Ingots.ingot_magnetized_tungsten), 1.0F);
        GameRegistry.addSmelting(Powders.powder_red_copper, new ItemStack(Ingots.ingot_red_copper), 1.0F);
        GameRegistry.addSmelting(Powders.powder_steel, new ItemStack(Ingots.ingot_steel), 1.0F);
        GameRegistry.addSmelting(Powders.powder_lithium, new ItemStack(Ingots.lithium), 1.0F);
        GameRegistry.addSmelting(Powders.powder_dura_steel, new ItemStack(Ingots.ingot_dura_steel), 1.0F);
        GameRegistry.addSmelting(Powders.powder_polymer, new ItemStack(Ingots.ingot_polymer), 1.0F);
        GameRegistry.addSmelting(Powders.powder_bakelite, new ItemStack(Ingots.ingot_bakelite), 1.0F);
        GameRegistry.addSmelting(Powders.powder_lanthanium, new ItemStack(Ingots.ingot_lanthanium), 1.0F);
        GameRegistry.addSmelting(Powders.powder_actinium, new ItemStack(Ingots.ingot_actinium), 1.0F);
        GameRegistry.addSmelting(Powders.powder_boron, new ItemStack(Ingots.ingot_boron), 1.0F);
        GameRegistry.addSmelting(Powders.powder_desh, new ItemStack(Ingots.ingot_desh), 1.0F);
        GameRegistry.addSmelting(Powders.powder_dineutronium, new ItemStack(Ingots.ingot_dineutronium), 5.0F);
        GameRegistry.addSmelting(Powders.powder_asbestos, new ItemStack(Ingots.ingot_asbestos), 1.0F);
        GameRegistry.addSmelting(Powders.powder_zirconium, new ItemStack(Ingots.ingot_zirconium), 1.0F);
        GameRegistry.addSmelting(Powders.powder_tcalloy, new ItemStack(Ingots.ingot_tcalloy), 1.0F);
        GameRegistry.addSmelting(Powders.powder_au198, new ItemStack(Ingots.ingot_au198), 1.0F);
        GameRegistry.addSmelting(Powders.powder_sr90, new ItemStack(Ingots.ingot_sr90), 1.0F);
        GameRegistry.addSmelting(Powders.powder_ra226, new ItemStack(Ingots.ingot_ra226), 1.0F);
        GameRegistry.addSmelting(Powders.powder_tantalium, new ItemStack(Ingots.ingot_tantalium), 1.0F);
        GameRegistry.addSmelting(Powders.powder_niobium, new ItemStack(Ingots.ingot_niobium), 1.0F);
        GameRegistry.addSmelting(Powders.powder_bismuth, new ItemStack(Ingots.ingot_bismuth), 1.0F);
        GameRegistry.addSmelting(Powders.powder_calcium, new ItemStack(Ingots.ingot_calcium), 1.0F);
        GameRegistry.addSmelting(Powders.powder_cadmium, new ItemStack(Ingots.ingot_cadmium), 1.0F);
        GameRegistry.addSmelting(ModItems.ball_resin, new ItemStack(Ingots.ingot_biorubber), 0.1F);

        GameRegistry.addSmelting(OreDictManager.DictFrame.fromOne(ModItems.arc_electrode_burnt, ItemArcElectrode.EnumElectrodeType.GRAPHITE), new ItemStack(Ingots.ingot_graphite), 3.0F);
        GameRegistry.addSmelting(OreDictManager.DictFrame.fromOne(ModItems.arc_electrode_burnt, ItemArcElectrode.EnumElectrodeType.LANTHANIUM), new ItemStack(Ingots.ingot_lanthanium), 3.0F);
        GameRegistry.addSmelting(OreDictManager.DictFrame.fromOne(ModItems.arc_electrode_burnt, ItemArcElectrode.EnumElectrodeType.DESH), new ItemStack(Ingots.ingot_desh), 3.0F);
        GameRegistry.addSmelting(OreDictManager.DictFrame.fromOne(ModItems.arc_electrode_burnt, ItemArcElectrode.EnumElectrodeType.SATURNITE), new ItemStack(Ingots.ingot_saturnite), 3.0F);

        GameRegistry.addSmelting(ModItems.combine_scrap, new ItemStack(Ingots.ingot_combine_steel), 1.0F);
        GameRegistry.addSmelting(ModItems.rag_damp, new ItemStack(ModItems.rag), 0.1F);
        GameRegistry.addSmelting(ModItems.rag_piss, new ItemStack(ModItems.rag), 0.1F);
        GameRegistry.addSmelting(OreDictManager.DictFrame.fromOne(ModBlocks.plant_flower, PlantEnums.EnumFlowerPlantType.TOBACCO), OreDictManager.DictFrame.fromOne(ModItems.plant_item, ItemEnums.EnumPlantType.TOBACCO), 0.1F);
        GameRegistry.addSmelting(ModItems.ball_fireclay, new ItemStack(Ingots.ingot_firebrick), 0.1F);

        //GameRegistry.addSmelting(Items.BONE, new ItemStack(Items.SLIME_BALL, 3), 0.0F);
        //GameRegistry.addSmelting(new ItemStack(Items.DYE, 1, 15), new ItemStack(Items.SLIME_BALL, 1), 0.0F);
        GameRegistry.addSmelting(new ItemStack(Blocks.GRAVEL, 1), new ItemStack(Blocks.COBBLESTONE, 1), 0.0F);
        GameRegistry.addSmelting(new ItemStack(ModBlocks.gravel_obsidian), new ItemStack(Blocks.OBSIDIAN), 0.0F);
        GameRegistry.addSmelting(new ItemStack(ModBlocks.gravel_diamond), new ItemStack(Items.DIAMOND), 3.0F);
        GameRegistry.addSmelting(new ItemStack(ModBlocks.sand_uranium), new ItemStack(ModBlocks.glass_uranium), 0.25F);
        GameRegistry.addSmelting(new ItemStack(ModBlocks.sand_polonium), new ItemStack(ModBlocks.glass_polonium), 0.75F);
        GameRegistry.addSmelting(new ItemStack(ModBlocks.waste_trinitite), new ItemStack(ModBlocks.glass_trinitite), 0.25F);
        GameRegistry.addSmelting(new ItemStack(ModBlocks.waste_trinitite_red), new ItemStack(ModBlocks.glass_trinitite), 0.25F);
        GameRegistry.addSmelting(new ItemStack(ModBlocks.sand_boron), new ItemStack(ModBlocks.glass_boron), 0.25F);
        GameRegistry.addSmelting(new ItemStack(ModBlocks.sand_lead), new ItemStack(ModBlocks.glass_lead), 0.25F);
        GameRegistry.addSmelting(new ItemStack(ModBlocks.ash_digamma), new ItemStack(ModBlocks.glass_ash), 10F);
        GameRegistry.addSmelting(new ItemStack(ModBlocks.basalt), new ItemStack(ModBlocks.basalt_smooth), 0.1F);

        GameRegistry.addSmelting(Ingots.ingot_schraranium, new ItemStack(Nuggies.nugget_schrabidium, 1), 2.0F);

        GameRegistry.addSmelting(Inserts.lodestone, new ItemStack(Crystals.crystal_iron, 1), 5.0F);
        GameRegistry.addSmelting(Crystals.crystal_iron, new ItemStack(Items.IRON_INGOT, 2), 2.0F);
        GameRegistry.addSmelting(Crystals.crystal_gold, new ItemStack(Items.GOLD_INGOT, 2), 2.0F);
        GameRegistry.addSmelting(Crystals.crystal_redstone, new ItemStack(Items.REDSTONE, 6), 2.0F);
        GameRegistry.addSmelting(Crystals.crystal_diamond, new ItemStack(Items.DIAMOND, 2), 2.0F);
        GameRegistry.addSmelting(Crystals.crystal_uranium, new ItemStack(Ingots.ingot_uranium, 2), 2.0F);
        GameRegistry.addSmelting(Crystals.crystal_thorium, new ItemStack(Ingots.ingot_th232, 2), 2.0F);
        GameRegistry.addSmelting(Crystals.crystal_plutonium, new ItemStack(Ingots.ingot_plutonium, 2), 2.0F);
        GameRegistry.addSmelting(Crystals.crystal_titanium, new ItemStack(Ingots.ingot_titanium, 2), 2.0F);
        GameRegistry.addSmelting(Crystals.crystal_sulfur, new ItemStack(Powders.sulfur, 6), 2.0F);
        GameRegistry.addSmelting(Crystals.crystal_niter, new ItemStack(Powders.niter, 6), 2.0F);
        GameRegistry.addSmelting(Crystals.crystal_copper, new ItemStack(Ingots.ingot_copper, 2), 2.0F);
        GameRegistry.addSmelting(Crystals.crystal_tungsten, new ItemStack(Ingots.ingot_tungsten, 2), 2.0F);
        GameRegistry.addSmelting(Crystals.crystal_aluminium, new ItemStack(Ingots.ingot_aluminium, 2), 2.0F);
        GameRegistry.addSmelting(Crystals.crystal_fluorite, new ItemStack(Ingots.fluorite, 6), 2.0F);
        GameRegistry.addSmelting(Crystals.crystal_beryllium, new ItemStack(Ingots.ingot_beryllium, 2), 2.0F);
        GameRegistry.addSmelting(Crystals.crystal_lead, new ItemStack(Ingots.ingot_lead, 2), 2.0F);
        GameRegistry.addSmelting(Crystals.crystal_schraranium, new ItemStack(Nuggies.nugget_schrabidium, 2), 2.0F);
        GameRegistry.addSmelting(Crystals.crystal_schrabidium, new ItemStack(Ingots.ingot_schrabidium, 2), 2.0F);
        GameRegistry.addSmelting(Crystals.crystal_rare, new ItemStack(Powders.powder_desh_mix, 1), 2.0F);
        GameRegistry.addSmelting(Crystals.crystal_phosphorus, new ItemStack(Powders.powder_fire, 6), 2.0F);
        GameRegistry.addSmelting(Crystals.crystal_lithium, new ItemStack(Ingots.lithium, 2), 2.0F);
        GameRegistry.addSmelting(Crystals.crystal_cobalt, new ItemStack(Ingots.ingot_cobalt, 2), 2.0F);
        GameRegistry.addSmelting(Crystals.crystal_starmetal, new ItemStack(Ingots.ingot_starmetal, 2), 2.0F);
        GameRegistry.addSmelting(Crystals.crystal_trixite, new ItemStack(Ingots.ingot_plutonium, 4), 2.0F);
        GameRegistry.addSmelting(Crystals.crystal_cinnabar, new ItemStack(ModItems.cinnabar, 4), 2.0F);
        GameRegistry.addSmelting(Crystals.crystal_osmiridium, new ItemStack(Ingots.ingot_osmiridium, 1), 2.0F);

        GameRegistry.addSmelting(Ingots.ingot_chainsteel, ItemHot.heatUp(new ItemStack(Ingots.ingot_chainsteel)), 0.0F);
        GameRegistry.addSmelting(Ingots.ingot_meteorite, ItemHot.heatUp(new ItemStack(Ingots.ingot_meteorite)), 0.0F);
        GameRegistry.addSmelting(Ingots.ingot_meteorite_forged, ItemHot.heatUp(new ItemStack(Ingots.ingot_meteorite_forged)), 0.0F);
        GameRegistry.addSmelting(Nuggies.blade_meteorite, ItemHot.heatUp(new ItemStack(Nuggies.blade_meteorite)), 0.0F);
        GameRegistry.addSmelting(ToolSets.meteorite_sword, ItemHot.heatUp(new ItemStack(ToolSets.meteorite_sword_seared)), 0.0F);

        GameRegistry.addSmelting(new ItemStack(ModItems.scrap_plastic, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(Ingots.ingot_polymer), 0.1F);

        for(int i = 0; i < 10; i++)
            GameRegistry.addSmelting(new ItemStack(Ingots.ingot_steel_dusted, 1, i), ItemHot.heatUp(new ItemStack(Ingots.ingot_steel_dusted, 1, i)), 1.0F);
    }

}
