package com.hbm.crafting;

import com.hbm.blocks.BlockEnums;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.items.ItemEnums;
import com.hbm.items.ItemEnums.EnumDepletedRTGMaterial;
import com.hbm.items.ModItems;
import com.hbm.items.ModItems.Materials.Billets;
import com.hbm.items.ModItems.Materials.Ingots;
import com.hbm.items.ModItems.Materials.Nuggies;
import com.hbm.items.ModItems.Materials.Powders;
import com.hbm.items.special.ItemWasteLong;
import com.hbm.items.special.ItemWasteShort;
import com.hbm.main.CraftingManager;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import static com.hbm.inventory.OreDictManager.*;

/**
 * Anything that deals exclusively with nuggets, ingots, blocks or compression in general
 * @author hbm
 */
public class MineralRecipes {

    public static void register() {

        add1To9Pair(ModItems.dust, ModItems.dust_tiny);
        add1To9Pair(Powders.powder_coal, Powders.powder_coal_tiny);
        add1To9Pair(Nuggies.ingot_mercury, Nuggies.nugget_mercury);

        add1To9Pair(ModBlocks.block_aluminium, Ingots.ingot_aluminium);
        add1To9Pair(ModBlocks.block_graphite, Ingots.ingot_graphite);
        add1To9Pair(ModBlocks.block_boron, Ingots.ingot_boron);
        add1To9Pair(ModBlocks.block_schraranium, Ingots.ingot_schraranium);
        add1To9Pair(ModBlocks.block_lanthanium, Ingots.ingot_lanthanium);
        add1To9Pair(ModBlocks.block_ra226, Ingots.ingot_ra226);
        add1To9Pair(ModBlocks.block_actinium, Ingots.ingot_actinium);
        add1To9Pair(ModBlocks.block_schrabidate, Ingots.ingot_schrabidate);
        add1To9Pair(ModBlocks.block_coltan, ModItems.fragment_coltan);
        add1To9Pair(ModBlocks.block_smore, Ingots.ingot_smore);
        add1To9Pair(ModBlocks.block_semtex, Ingots.ingot_semtex);
        add1To9Pair(ModBlocks.block_c4, Ingots.ingot_c4);
        add1To9Pair(ModBlocks.block_polymer, Ingots.ingot_polymer);
        add1To9Pair(ModBlocks.block_bakelite, Ingots.ingot_bakelite);
        add1To9Pair(ModBlocks.block_rubber, Ingots.ingot_rubber);
        add1To9Pair(ModBlocks.block_cadmium, Ingots.ingot_cadmium);
        add1To9Pair(ModBlocks.block_tcalloy, Ingots.ingot_tcalloy);
        add1To9Pair(ModBlocks.block_cdalloy, Ingots.ingot_cdalloy);

        for(int i = 0; i < ItemEnums.EnumCokeType.values().length; i++) {
            add1To9PairSameMeta(Item.getItemFromBlock(ModBlocks.block_coke), ModItems.coke, i);
        }

        addMineralSet(Nuggies.nugget_niobium, Ingots.ingot_niobium, ModBlocks.block_niobium);
        addMineralSet(Nuggies.nugget_bismuth, Ingots.ingot_bismuth, ModBlocks.block_bismuth);
        addMineralSet(Nuggies.nugget_tantalium, Ingots.ingot_tantalium, ModBlocks.block_tantalium);
        addMineralSet(Nuggies.nugget_zirconium, Ingots.ingot_zirconium, ModBlocks.block_zirconium);
        addMineralSet(Nuggies.nugget_dineutronium, Ingots.ingot_dineutronium, ModBlocks.block_dineutronium);
        addMineralSet(ModItems.nuclear_waste_vitrified_tiny, ModItems.nuclear_waste_vitrified, ModBlocks.block_waste_vitrified);

        add1To9Pair(Ingots.ingot_silicon, Nuggies.nugget_silicon);

        add1To9Pair(Powders.powder_boron, Powders.powder_boron_tiny);
        add1To9Pair(Powders.powder_sr90, Powders.powder_sr90_tiny);
        add1To9Pair(Powders.powder_xe135, Powders.powder_xe135_tiny);
        add1To9Pair(Powders.powder_cs137, Powders.powder_cs137_tiny);
        add1To9Pair(Powders.powder_i131, Powders.powder_i131_tiny);

        add1To9Pair(Ingots.ingot_technetium, Nuggies.nugget_technetium);
        add1To9Pair(Ingots.ingot_co60, Nuggies.nugget_co60);
        add1To9Pair(Ingots.ingot_sr90, Nuggies.nugget_sr90);
        add1To9Pair(Ingots.ingot_au198, Nuggies.nugget_au198);
        add1To9Pair(Ingots.ingot_pb209, Nuggies.nugget_pb209);
        add1To9Pair(Ingots.ingot_ra226, Nuggies.nugget_ra226);
        add1To9Pair(Ingots.ingot_actinium, Nuggies.nugget_actinium);
        add1To9Pair(Ingots.ingot_arsenic, Nuggies.nugget_arsenic);

        add1To9Pair(Ingots.ingot_pu241, Nuggies.nugget_pu241);
        add1To9Pair(Ingots.ingot_am241, Nuggies.nugget_am241);
        add1To9Pair(Ingots.ingot_am242, Nuggies.nugget_am242);
        add1To9Pair(Ingots.ingot_am_mix, Nuggies.nugget_am_mix);
        add1To9Pair(Ingots.ingot_americium_fuel, Nuggies.nugget_americium_fuel);

        add1To9Pair(Ingots.ingot_gh336, Nuggies.nugget_gh336);

        for(int i = 0; i < ItemWasteLong.WasteClass.values().length; i++) {
            add1To9PairSameMeta(ModItems.nuclear_waste_long, ModItems.nuclear_waste_long_tiny, i);
            add1To9PairSameMeta(ModItems.nuclear_waste_long_depleted, ModItems.nuclear_waste_long_depleted_tiny, i);
        }

        for(int i = 0; i < ItemWasteShort.WasteClass.values().length; i++) {
            add1To9PairSameMeta(ModItems.nuclear_waste_short, ModItems.nuclear_waste_short_tiny, i);
            add1To9PairSameMeta(ModItems.nuclear_waste_short_depleted, ModItems.nuclear_waste_short_depleted_tiny, i);
        }

        add1To9Pair(ModBlocks.block_fallout, ModItems.fallout);
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.fallout, 2), "##", '#', ModItems.fallout );

        addMineralSet(Nuggies.nugget_pu_mix, Ingots.ingot_pu_mix, ModBlocks.block_pu_mix);
        add1To9Pair(Ingots.ingot_neptunium_fuel, Nuggies.nugget_neptunium_fuel);

        addBillet(Billets.billet_cobalt,				Ingots.ingot_cobalt,				Nuggies.nugget_cobalt);
        addBillet(Billets.billet_co60,					Ingots.ingot_co60,				Nuggies.nugget_co60);
        addBillet(Billets.billet_sr90,					Ingots.ingot_sr90,				Nuggies.nugget_sr90, SR90.all(MaterialShapes.NUGGET));
        addBillet(Billets.billet_uranium,				Ingots.ingot_uranium,				Nuggies.nugget_uranium, U.all(MaterialShapes.NUGGET));
        addBillet(Billets.billet_u233,					Ingots.ingot_u233,				Nuggies.nugget_u233, U233.all(MaterialShapes.NUGGET));
        addBillet(Billets.billet_u235,					Ingots.ingot_u235,				Nuggies.nugget_u235, U235.all(MaterialShapes.NUGGET));
        addBillet(Billets.billet_u238,					Ingots.ingot_u238,				Nuggies.nugget_u238, U238.all(MaterialShapes.NUGGET));
        addBillet(Billets.billet_th232,				Ingots.ingot_th232,				Nuggies.nugget_th232, TH232.all(MaterialShapes.NUGGET));
        addBillet(Billets.billet_plutonium,			Ingots.ingot_plutonium,			Nuggies.nugget_plutonium, PU.all(MaterialShapes.NUGGET));
        addBillet(Billets.billet_pu238,				Ingots.ingot_pu238,				Nuggies.nugget_pu238, PU238.all(MaterialShapes.NUGGET));
        addBillet(Billets.billet_pu239,				Ingots.ingot_pu239,				Nuggies.nugget_pu239, PU239.all(MaterialShapes.NUGGET));
        addBillet(Billets.billet_pu240,				Ingots.ingot_pu240,				Nuggies.nugget_pu240, PU240.all(MaterialShapes.NUGGET));
        addBillet(Billets.billet_pu241,				Ingots.ingot_pu241,				Nuggies.nugget_pu241, PU241.all(MaterialShapes.NUGGET));
        addBillet(Billets.billet_pu_mix,				Ingots.ingot_pu_mix,				Nuggies.nugget_pu_mix);
        addBillet(Billets.billet_am241,				Ingots.ingot_am241,				Nuggies.nugget_am241, AM241.all(MaterialShapes.NUGGET));
        addBillet(Billets.billet_am242,				Ingots.ingot_am242,				Nuggies.nugget_am242, AM242.all(MaterialShapes.NUGGET));
        addBillet(Billets.billet_am_mix,				Ingots.ingot_am_mix,				Nuggies.nugget_am_mix);
        addBillet(Billets.billet_neptunium,			Ingots.ingot_neptunium,			Nuggies.nugget_neptunium, NP237.all(MaterialShapes.NUGGET));
        addBillet(Billets.billet_polonium,				Ingots.ingot_polonium,			Nuggies.nugget_polonium, PO210.all(MaterialShapes.NUGGET));
        addBillet(Billets.billet_technetium,			Ingots.ingot_technetium,			Nuggies.nugget_technetium, TC99.all(MaterialShapes.NUGGET));
        addBillet(Billets.billet_au198,				Ingots.ingot_au198,				Nuggies.nugget_au198, AU198.all(MaterialShapes.NUGGET));
        addBillet(Billets.billet_pb209,				Ingots.ingot_pb209,				Nuggies.nugget_pb209, PB209.all(MaterialShapes.NUGGET)); //and so forth
        addBillet(Billets.billet_ra226,				Ingots.ingot_ra226,				Nuggies.nugget_ra226, RA226.all(MaterialShapes.NUGGET));
        addBillet(Billets.billet_actinium,				Ingots.ingot_actinium,			Nuggies.nugget_actinium, AC227.all(MaterialShapes.NUGGET));
        addBillet(Billets.billet_schrabidium,			Ingots.ingot_schrabidium,			Nuggies.nugget_schrabidium, SA326.nugget());
        addBillet(Billets.billet_solinium,				Ingots.ingot_solinium,			Nuggies.nugget_solinium, SA327.nugget());
        addBillet(Billets.billet_gh336,				Ingots.ingot_gh336,				Nuggies.nugget_gh336, GH336.all(MaterialShapes.NUGGET));
        addBillet(Billets.billet_uranium_fuel,			Ingots.ingot_uranium_fuel,		Nuggies.nugget_uranium_fuel);
        addBillet(Billets.billet_thorium_fuel,			Ingots.ingot_thorium_fuel,		Nuggies.nugget_thorium_fuel);
        addBillet(Billets.billet_plutonium_fuel,		Ingots.ingot_plutonium_fuel,		Nuggies.nugget_plutonium_fuel);
        addBillet(Billets.billet_neptunium_fuel,		Ingots.ingot_neptunium_fuel,		Nuggies.nugget_neptunium_fuel);
        addBillet(Billets.billet_mox_fuel,				Ingots.ingot_mox_fuel,			Nuggies.nugget_mox_fuel);
        addBillet(Billets.billet_les,					Ingots.ingot_les,					Nuggies.nugget_les);
        addBillet(Billets.billet_schrabidium_fuel,		Ingots.ingot_schrabidium_fuel,	Nuggies.nugget_schrabidium_fuel);
        addBillet(Billets.billet_hes,					Ingots.ingot_hes,					Nuggies.nugget_hes);
        addBillet(Billets.billet_australium,			Ingots.ingot_australium,			Nuggies.nugget_australium, "nuggetAustralium");
        addBillet(Billets.billet_australium_greater,										Nuggies.nugget_australium_greater);
        addBillet(Billets.billet_australium_lesser,										Nuggies.nugget_australium_lesser);
        addBillet(Billets.billet_nuclear_waste,		ModItems.nuclear_waste,				ModItems.nuclear_waste_tiny);
        addBillet(Billets.billet_beryllium,			Ingots.ingot_beryllium,			Nuggies.nugget_beryllium, BE.nugget());
        addBillet(Billets.billet_zirconium,			Ingots.ingot_zirconium,			Nuggies.nugget_zirconium, ZR.nugget());
        addBillet(Billets.billet_bismuth,				Ingots.ingot_bismuth,				Nuggies.nugget_bismuth);
        addBillet(Billets.billet_silicon,				Ingots.ingot_silicon,				Nuggies.nugget_silicon, SI.nugget());

        CraftingManager.addShapelessAuto(new ItemStack(Billets.billet_thorium_fuel, 6), Billets.billet_th232, Billets.billet_th232, Billets.billet_th232, Billets.billet_th232, Billets.billet_th232, Billets.billet_u233 );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_thorium_fuel, 1), "nuggetThorium232", "nuggetThorium232", "nuggetThorium232", "nuggetThorium232", "nuggetThorium232", "nuggetUranium233" );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_thorium_fuel, 1), "tinyTh232", "tinyTh232", "tinyTh232", "tinyTh232", "tinyTh232", "tinyU233" );
        CraftingManager.addShapelessAuto(new ItemStack(Billets.billet_uranium_fuel, 6), Billets.billet_u238, Billets.billet_u238, Billets.billet_u238, Billets.billet_u238, Billets.billet_u238, Billets.billet_u235 );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_uranium_fuel, 1), "nuggetUranium238", "nuggetUranium238", "nuggetUranium238", "nuggetUranium238", "nuggetUranium238", "nuggetUranium235" );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_uranium_fuel, 1), "tinyU238", "tinyU238", "tinyU238", "tinyU238", "tinyU238", "tinyU235" );
        CraftingManager.addShapelessAuto(new ItemStack(Billets.billet_plutonium_fuel, 3), Billets.billet_u238, Billets.billet_u238, Billets.billet_pu_mix );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_plutonium_fuel, 1), Nuggies.nugget_pu_mix, Nuggies.nugget_pu_mix, "nuggetUranium238", "nuggetUranium238", "nuggetUranium238", "nuggetUranium238" );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_plutonium_fuel, 1), Nuggies.nugget_pu_mix, Nuggies.nugget_pu_mix, "tinyU238", "tinyU238", "tinyU238", "tinyU238" );
        CraftingManager.addShapelessAuto(new ItemStack(Billets.billet_pu_mix, 3), Billets.billet_pu239, Billets.billet_pu239, Billets.billet_pu240 );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_pu_mix, 1), "nuggetPlutonium239", "nuggetPlutonium239", "nuggetPlutonium239", "nuggetPlutonium239", "nuggetPlutonium240", "nuggetPlutonium240" );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_pu_mix, 1), "tinyPu239", "tinyPu239", "tinyPu239", "tinyPu239", "tinyPu240", "tinyPu240" );
        CraftingManager.addShapelessAuto(new ItemStack(Billets.billet_americium_fuel, 3), Billets.billet_u238, Billets.billet_u238, Billets.billet_am_mix );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_americium_fuel, 1), Nuggies.nugget_am_mix, Nuggies.nugget_am_mix, "nuggetUranium238", "nuggetUranium238", "nuggetUranium238", "nuggetUranium238" );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_americium_fuel, 1), Nuggies.nugget_am_mix, Nuggies.nugget_am_mix, "tinyU238", "tinyU238", "tinyU238", "tinyU238" );
        CraftingManager.addShapelessAuto(new ItemStack(Billets.billet_am_mix, 3), Billets.billet_am241, Billets.billet_am242, Billets.billet_am242 );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_am_mix, 1), "nuggetAmericium241", "nuggetAmericium241", "nuggetAmericium242", "nuggetAmericium242", "nuggetAmericium242", "nuggetAmericium242" );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_am_mix, 1), "tinyAm241", "tinyAm241", "tinyAm242", "tinyAm242", "tinyAm242", "tinyAm242" );
        CraftingManager.addShapelessAuto(new ItemStack(Billets.billet_neptunium_fuel, 3), Billets.billet_u238, Billets.billet_u238, Billets.billet_neptunium );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_neptunium_fuel, 1), "nuggetNeptunium237", "nuggetNeptunium237", "nuggetUranium238", "nuggetUranium238", "nuggetUranium238", "nuggetUranium238" );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_neptunium_fuel, 1), "tinyNp237", "tinyNp237", "tinyU238", "tinyU238", "tinyU238", "tinyU238" );
        CraftingManager.addShapelessAuto(new ItemStack(Billets.billet_mox_fuel, 3), Billets.billet_uranium_fuel, Billets.billet_uranium_fuel, Billets.billet_pu_mix );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_mox_fuel, 1), Nuggies.nugget_pu_mix, Nuggies.nugget_pu_mix, Nuggies.nugget_uranium_fuel, Nuggies.nugget_uranium_fuel, Nuggies.nugget_uranium_fuel, Nuggies.nugget_uranium_fuel );
        CraftingManager.addShapelessAuto(new ItemStack(Billets.billet_schrabidium_fuel, 3), Billets.billet_schrabidium, Billets.billet_neptunium, Billets.billet_beryllium );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_schrabidium_fuel, 1), Nuggies.nugget_schrabidium, Nuggies.nugget_schrabidium, "nuggetNeptunium237", "nuggetNeptunium237", Nuggies.nugget_beryllium, Nuggies.nugget_beryllium );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_schrabidium_fuel, 1), Nuggies.nugget_schrabidium, Nuggies.nugget_schrabidium, "tinyNp237", "tinyNp237", Nuggies.nugget_beryllium, Nuggies.nugget_beryllium );

        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_po210be, 1), "nuggetPolonium210", "nuggetPolonium210", "nuggetPolonium210", Nuggies.nugget_beryllium, Nuggies.nugget_beryllium, Nuggies.nugget_beryllium );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_pu238be, 1), "nuggetPlutonium238", "nuggetPlutonium238", "nuggetPlutonium238", Nuggies.nugget_beryllium, Nuggies.nugget_beryllium, Nuggies.nugget_beryllium );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_ra226be, 1), "nuggetRadium226", "nuggetRadium226", "nuggetRadium226", Nuggies.nugget_beryllium, Nuggies.nugget_beryllium, Nuggies.nugget_beryllium );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_po210be, 2), Billets.billet_polonium, Billets.billet_beryllium );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_pu238be, 2), Billets.billet_pu238, Billets.billet_beryllium );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_ra226be, 2), Billets.billet_ra226, Billets.billet_beryllium );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_po210be, 6), Billets.billet_polonium, Billets.billet_polonium, Billets.billet_polonium,  Billets.billet_beryllium, Billets.billet_beryllium, Billets.billet_beryllium );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_pu238be, 6), Billets.billet_pu238, Billets.billet_pu238, Billets.billet_pu238,  Billets.billet_beryllium, Billets.billet_beryllium, Billets.billet_beryllium );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_ra226be, 6), Billets.billet_ra226, Billets.billet_ra226, Billets.billet_ra226,  Billets.billet_beryllium, Billets.billet_beryllium, Billets.billet_beryllium );

        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_zfb_bismuth, 1), ZR.nugget(), ZR.nugget(), ZR.nugget(), U.nugget(), PU241.nugget(), BI.nugget() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_zfb_pu241, 1), ZR.nugget(), ZR.nugget(), ZR.nugget(), U235.nugget(), PU240.nugget(), PU241.nugget() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_zfb_am_mix, 1), ZR.nugget(), ZR.nugget(), ZR.nugget(), PU241.nugget(), PU241.nugget(), AMRG.nugget() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_zfb_bismuth, 6), ZR.billet(), ZR.billet(), ZR.billet(), U.billet(), PU241.billet(), BI.billet() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_zfb_pu241, 6), ZR.billet(), ZR.billet(), ZR.billet(), U235.billet(), PU240.billet(), PU241.billet() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_zfb_am_mix, 6), ZR.billet(), ZR.billet(), ZR.billet(), PU241.billet(), PU241.billet(), AMRG.billet() );


        CraftingManager.addShapelessAuto(new ItemStack(Billets.billet_uranium, 2), Billets.billet_uranium_fuel, Billets.billet_u238 );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_uranium, 2), Billets.billet_u238, "nuggetUranium238", "nuggetUranium238", "nuggetUranium238", "nuggetUranium238", "nuggetUranium238", "nuggetUranium235" );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Billets.billet_uranium, 2), Billets.billet_u238, "tinyU238", "tinyU238", "tinyU238", "tinyU238", "tinyU238", "tinyU235" );

        CraftingManager.addShapelessAuto(new ItemStack(Billets.billet_balefire_gold, 1), Billets.billet_au198, new ItemStack(ModItems.cell, 1, Fluids.AMAT.getID()), ModItems.pellet_charged );
        CraftingManager.addShapelessAuto(new ItemStack(Billets.billet_flashlead, 2), Billets.billet_balefire_gold, Billets.billet_pb209, new ItemStack(ModItems.cell, 1, Fluids.AMAT.getID()) );

        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.pellet_rtg), Billets.billet_pu238, Billets.billet_pu238, Billets.billet_pu238, IRON.plate() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.pellet_rtg_radium), Billets.billet_ra226, Billets.billet_ra226, Billets.billet_ra226, IRON.plate() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.pellet_rtg_weak), Billets.billet_u238, Billets.billet_u238, Billets.billet_pu238, IRON.plate() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.pellet_rtg_strontium), Billets.billet_sr90, Billets.billet_sr90, Billets.billet_sr90, IRON.plate() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.pellet_rtg_cobalt), Billets.billet_co60, Billets.billet_co60, Billets.billet_co60, IRON.plate() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.pellet_rtg_actinium), Billets.billet_actinium, Billets.billet_actinium, Billets.billet_actinium, IRON.plate() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.pellet_rtg_polonium), Billets.billet_polonium, Billets.billet_polonium, Billets.billet_polonium, IRON.plate() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.pellet_rtg_lead), Billets.billet_pb209, Billets.billet_pb209, Billets.billet_pb209, IRON.plate() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.pellet_rtg_gold), Billets.billet_au198, Billets.billet_au198, Billets.billet_au198, IRON.plate() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.pellet_rtg_americium), Billets.billet_am241, Billets.billet_am241, Billets.billet_am241, IRON.plate() );

        //There's no need for anvil recycling recipes if you simply set the container item
        CraftingManager.addShapelessAuto(new ItemStack(Billets.billet_bismuth, 3), new ItemStack(ModItems.pellet_rtg_depleted, 1, EnumDepletedRTGMaterial.BISMUTH.ordinal()) );
        CraftingManager.addShapelessAuto(new ItemStack(Ingots.ingot_lead, 2), new ItemStack(ModItems.pellet_rtg_depleted, 1, EnumDepletedRTGMaterial.LEAD.ordinal()) );
        CraftingManager.addShapelessAuto(new ItemStack(Nuggies.ingot_mercury, 2), new ItemStack(ModItems.pellet_rtg_depleted, 1, EnumDepletedRTGMaterial.MERCURY.ordinal()) );
        CraftingManager.addShapelessAuto(new ItemStack(Billets.billet_neptunium, 3), new ItemStack(ModItems.pellet_rtg_depleted, 1, EnumDepletedRTGMaterial.NEPTUNIUM.ordinal()) );
        CraftingManager.addShapelessAuto(new ItemStack(Billets.billet_zirconium, 3), new ItemStack(ModItems.pellet_rtg_depleted, 1, EnumDepletedRTGMaterial.ZIRCONIUM.ordinal()) );
        if (OreDictionary.doesOreNameExist("ingotNickel")) {
            NonNullList<ItemStack> ores = OreDictionary.getOres("ingotNickel");
            if (!ores.isEmpty()) {
                ItemStack out = ores.get(0).copy();
                out.setCount(2);
                CraftingManager.addShapelessAuto(out, new ItemStack(ModItems.pellet_rtg_depleted, 1, EnumDepletedRTGMaterial.NICKEL.ordinal()));
            }
        }

        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_copper), 1), "###", "###", "###", '#', Ingots.ingot_copper );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_fluorite), 1), "###", "###", "###", '#', Ingots.fluorite );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_niter), 1), "###", "###", "###", '#', Powders.niter );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_red_copper), 1), "###", "###", "###", '#', Ingots.ingot_red_copper );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_steel), 1), "###", "###", "###", '#', Ingots.ingot_steel );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_sulfur), 1), "###", "###", "###", '#', Powders.sulfur );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_titanium), 1), "###", "###", "###", '#', Ingots.ingot_titanium );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_tungsten), 1), "###", "###", "###", '#', Ingots.ingot_tungsten );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_uranium), 1), "###", "###", "###", '#', Ingots.ingot_uranium );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_thorium), 1), "###", "###", "###", '#', Ingots.ingot_th232 );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_lead), 1), "###", "###", "###", '#', Ingots.ingot_lead );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_trinitite), 1), "###", "###", "###", '#', ModItems.trinitite );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_waste), 1), "###", "###", "###", '#', ModItems.nuclear_waste );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_scrap), 1), "##", "##", '#', ModItems.scrap );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_scrap), 1), "###", "###", "###", '#', ModItems.dust );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_beryllium), 1), "###", "###", "###", '#', Ingots.ingot_beryllium );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_schrabidium), 1), "###", "###", "###", '#', Ingots.ingot_schrabidium );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_schrabidium_cluster, 1), "#S#", "SXS", "#S#", '#', Ingots.ingot_schrabidium, 'S', Ingots.ingot_starmetal, 'X', Ingots.ingot_schrabidate );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_euphemium), 1), "###", "###", "###", '#', Ingots.ingot_euphemium );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_advanced_alloy), 1), "###", "###", "###", '#', Ingots.ingot_advanced_alloy );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_magnetized_tungsten), 1), "###", "###", "###", '#', Ingots.ingot_magnetized_tungsten );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_combine_steel), 1), "###", "###", "###", '#', Ingots.ingot_combine_steel );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_australium), 1), "###", "###", "###", '#', Ingots.ingot_australium );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_desh), 1), "###", "###", "###", '#', Ingots.ingot_desh );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_dura_steel), 1), "###", "###", "###", '#', Ingots.ingot_dura_steel );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_meteor_cobble), 1), "##", "##", '#', ModItems.fragment_meteorite );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_meteor_broken), 1), "###", "###", "###", '#', ModItems.fragment_meteorite );
        CraftingManager.addRecipeAuto(new ItemStack(Item.getItemFromBlock(ModBlocks.block_yellowcake), 1), "###", "###", "###", '#', Powders.powder_yellowcake );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_starmetal, 1), "###", "###", "###", '#', Ingots.ingot_starmetal );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_u233, 1), "###", "###", "###", '#', Ingots.ingot_u233 );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_u235, 1), "###", "###", "###", '#', Ingots.ingot_u235 );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_u238, 1), "###", "###", "###", '#', Ingots.ingot_u238 );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_uranium_fuel, 1), "###", "###", "###", '#', Ingots.ingot_uranium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_neptunium, 1), "###", "###", "###", '#', Ingots.ingot_neptunium );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_polonium, 1), "###", "###", "###", '#', Ingots.ingot_polonium );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_plutonium, 1), "###", "###", "###", '#', Ingots.ingot_plutonium );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_pu238, 1), "###", "###", "###", '#', Ingots.ingot_pu238 );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_pu239, 1), "###", "###", "###", '#', Ingots.ingot_pu239 );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_pu240, 1), "###", "###", "###", '#', Ingots.ingot_pu240 );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_mox_fuel, 1), "###", "###", "###", '#', Ingots.ingot_mox_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_plutonium_fuel, 1), "###", "###", "###", '#', Ingots.ingot_plutonium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_thorium_fuel, 1), "###", "###", "###", '#', Ingots.ingot_thorium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_solinium, 1), "###", "###", "###", '#', Ingots.ingot_solinium );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_schrabidium_fuel, 1), "###", "###", "###", '#', Ingots.ingot_schrabidium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_lithium, 1), "###", "###", "###", '#', Ingots.lithium );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_white_phosphorus, 1), "###", "###", "###", '#', Ingots.ingot_phosphorus );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_red_phosphorus, 1), "###", "###", "###", '#', Powders.powder_fire );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_insulator, 1), "###", "###", "###", '#', ModItems.plate_polymer );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_asbestos, 1), "###", "###", "###", '#', Ingots.ingot_asbestos );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_fiberglass, 1), "###", "###", "###", '#', Ingots.ingot_fiberglass );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.block_cobalt, 1), "###", "###", "###", '#', Ingots.ingot_cobalt );

        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_copper, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_copper) );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.fluorite, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_fluorite) );
        CraftingManager.addRecipeAuto(new ItemStack(Powders.niter, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_niter) );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_red_copper, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_red_copper) );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_steel, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_steel) );
        CraftingManager.addRecipeAuto(new ItemStack(Powders.sulfur, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_sulfur) );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_titanium, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_titanium) );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_tungsten, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_tungsten) );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_uranium, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_uranium) );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_th232, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_thorium) );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_lead, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_lead) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.trinitite, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_trinitite) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nuclear_waste, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_waste) );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nuclear_waste, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_waste_painted) );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_beryllium, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_beryllium) );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_schrabidium, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_schrabidium) );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_euphemium, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_euphemium) );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_advanced_alloy, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_advanced_alloy) );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_magnetized_tungsten, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_magnetized_tungsten) );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_combine_steel, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_combine_steel) );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_australium, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_australium) );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_desh, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_desh) );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_dura_steel, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_dura_steel) );
        CraftingManager.addRecipeAuto(new ItemStack(Powders.powder_yellowcake, 9), "#", '#', Item.getItemFromBlock(ModBlocks.block_yellowcake) );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_starmetal, 9), "#", '#', ModBlocks.block_starmetal );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_u233, 9), "#", '#', ModBlocks.block_u233 );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_u235, 9), "#", '#', ModBlocks.block_u235 );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_u238, 9), "#", '#', ModBlocks.block_u238 );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_uranium_fuel, 9), "#", '#', ModBlocks.block_uranium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_neptunium, 9), "#", '#', ModBlocks.block_neptunium );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_polonium, 9), "#", '#', ModBlocks.block_polonium );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_plutonium, 9), "#", '#', ModBlocks.block_plutonium );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_pu238, 9), "#", '#', ModBlocks.block_pu238 );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_pu239, 9), "#", '#', ModBlocks.block_pu239 );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_pu240, 9), "#", '#', ModBlocks.block_pu240 );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_mox_fuel, 9), "#", '#', ModBlocks.block_mox_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_plutonium_fuel, 9), "#", '#', ModBlocks.block_plutonium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_thorium_fuel, 9), "#", '#', ModBlocks.block_thorium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_solinium, 9), "#", '#', ModBlocks.block_solinium );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_schrabidium_fuel, 9), "#", '#', ModBlocks.block_schrabidium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.lithium, 9), "#", '#', ModBlocks.block_lithium );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_phosphorus, 9), "#", '#', ModBlocks.block_white_phosphorus );
        CraftingManager.addRecipeAuto(new ItemStack(Powders.powder_fire, 9), "#", '#', ModBlocks.block_red_phosphorus );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.plate_polymer, 9), "#", '#', ModBlocks.block_insulator );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_asbestos, 9), "#", '#', ModBlocks.block_asbestos );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_fiberglass, 9), "#", '#', ModBlocks.block_fiberglass );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_cobalt, 9), "#", '#', ModBlocks.block_cobalt );

        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_plutonium, 1), "###", "###", "###", '#', Nuggies.nugget_plutonium );
        CraftingManager.addRecipeAuto(new ItemStack(Nuggies.nugget_plutonium, 9), "#", '#', Ingots.ingot_plutonium );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_pu238, 1), "###", "###", "###", '#', Nuggies.nugget_pu238 );
        CraftingManager.addRecipeAuto(new ItemStack(Nuggies.nugget_pu238, 9), "#", '#', Ingots.ingot_pu238 );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_pu239, 1), "###", "###", "###", '#', Nuggies.nugget_pu239 );
        CraftingManager.addRecipeAuto(new ItemStack(Nuggies.nugget_pu239, 9), "#", '#', Ingots.ingot_pu239 );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_pu240, 1), "###", "###", "###", '#', Nuggies.nugget_pu240 );
        CraftingManager.addRecipeAuto(new ItemStack(Nuggies.nugget_pu240, 9), "#", '#', Ingots.ingot_pu240 );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_th232, 1), "###", "###", "###", '#', Nuggies.nugget_th232 );
        CraftingManager.addRecipeAuto(new ItemStack(Nuggies.nugget_th232, 9), "#", '#', Ingots.ingot_th232 );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_uranium, 1), "###", "###", "###", '#', Nuggies.nugget_uranium );
        CraftingManager.addRecipeAuto(new ItemStack(Nuggies.nugget_uranium, 9), "#", '#', Ingots.ingot_uranium );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_u233, 1), "###", "###", "###", '#', Nuggies.nugget_u233 );
        CraftingManager.addRecipeAuto(new ItemStack(Nuggies.nugget_u233, 9), "#", '#', Ingots.ingot_u233 );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_u235, 1), "###", "###", "###", '#', Nuggies.nugget_u235 );
        CraftingManager.addRecipeAuto(new ItemStack(Nuggies.nugget_u235, 9), "#", '#', Ingots.ingot_u235 );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_u238, 1), "###", "###", "###", '#', Nuggies.nugget_u238 );
        CraftingManager.addRecipeAuto(new ItemStack(Nuggies.nugget_u238, 9), "#", '#', Ingots.ingot_u238 );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_neptunium, 1), "###", "###", "###", '#', Nuggies.nugget_neptunium );
        CraftingManager.addRecipeAuto(new ItemStack(Nuggies.nugget_neptunium, 9), "#", '#', Ingots.ingot_neptunium );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_polonium, 1), "###", "###", "###", '#', Nuggies.nugget_polonium );
        CraftingManager.addRecipeAuto(new ItemStack(Nuggies.nugget_polonium, 9), "#", '#', Ingots.ingot_polonium );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_lead, 1), "###", "###", "###", '#', Nuggies.nugget_lead );
        CraftingManager.addRecipeAuto(new ItemStack(Nuggies.nugget_lead, 9), "#", '#', Ingots.ingot_lead );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_beryllium, 1), "###", "###", "###", '#', Nuggies.nugget_beryllium );
        CraftingManager.addRecipeAuto(new ItemStack(Nuggies.nugget_beryllium, 9), "#", '#', Ingots.ingot_beryllium );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_schrabidium, 1), "###", "###", "###", '#', Nuggies.nugget_schrabidium );
        CraftingManager.addRecipeAuto(new ItemStack(Nuggies.nugget_schrabidium, 9), "#", '#', Ingots.ingot_schrabidium );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_uranium_fuel, 1), "###", "###", "###", '#', Nuggies.nugget_uranium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(Nuggies.nugget_uranium_fuel, 9), "#", '#', Ingots.ingot_uranium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_thorium_fuel, 1), "###", "###", "###", '#', Nuggies.nugget_thorium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(Nuggies.nugget_thorium_fuel, 9), "#", '#', Ingots.ingot_thorium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_plutonium_fuel, 1), "###", "###", "###", '#', Nuggies.nugget_plutonium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(Nuggies.nugget_plutonium_fuel, 9), "#", '#', Ingots.ingot_plutonium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_mox_fuel, 1), "###", "###", "###", '#', Nuggies.nugget_mox_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(Nuggies.nugget_mox_fuel, 9), "#", '#', Ingots.ingot_mox_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_schrabidium_fuel, 1), "###", "###", "###", '#', Nuggies.nugget_schrabidium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(Nuggies.nugget_schrabidium_fuel, 9), "#", '#', Ingots.ingot_schrabidium_fuel );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_hes, 1), "###", "###", "###", '#', Nuggies.nugget_hes );
        CraftingManager.addRecipeAuto(new ItemStack(Nuggies.nugget_hes, 9), "#", '#', Ingots.ingot_hes );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_les, 1), "###", "###", "###", '#', Nuggies.nugget_les );
        CraftingManager.addRecipeAuto(new ItemStack(Nuggies.nugget_les, 9), "#", '#', Ingots.ingot_les );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_australium, 1), "###", "###", "###", '#', Nuggies.nugget_australium );
        CraftingManager.addRecipeAuto(new ItemStack(Nuggies.nugget_australium, 9), "#", '#', Ingots.ingot_australium );
        CraftingManager.addRecipeAuto(new ItemStack(Powders.powder_steel, 1), "###", "###", "###", '#', Powders.powder_steel_tiny );
        CraftingManager.addRecipeAuto(new ItemStack(Powders.powder_steel_tiny, 9), "#", '#', Powders.powder_steel );
        CraftingManager.addRecipeAuto(new ItemStack(Powders.powder_lithium, 1), "###", "###", "###", '#', Powders.powder_lithium_tiny );
        CraftingManager.addRecipeAuto(new ItemStack(Powders.powder_lithium_tiny, 9), "#", '#', Powders.powder_lithium );
        CraftingManager.addRecipeAuto(new ItemStack(Powders.powder_cobalt, 1), "###", "###", "###", '#', Powders.powder_cobalt_tiny );
        CraftingManager.addRecipeAuto(new ItemStack(Powders.powder_cobalt_tiny, 9), "#", '#', Powders.powder_cobalt );
        CraftingManager.addRecipeAuto(new ItemStack(Powders.powder_neodymium, 1), "###", "###", "###", '#', Powders.powder_neodymium_tiny );
        CraftingManager.addRecipeAuto(new ItemStack(Powders.powder_neodymium_tiny, 9), "#", '#', Powders.powder_neodymium );
        CraftingManager.addRecipeAuto(new ItemStack(Powders.powder_niobium, 1), "###", "###", "###", '#', Powders.powder_niobium_tiny );
        CraftingManager.addRecipeAuto(new ItemStack(Powders.powder_niobium_tiny, 9), "#", '#', Powders.powder_niobium );
        CraftingManager.addRecipeAuto(new ItemStack(Powders.powder_cerium, 1), "###", "###", "###", '#', Powders.powder_cerium_tiny );
        CraftingManager.addRecipeAuto(new ItemStack(Powders.powder_cerium_tiny, 9), "#", '#', Powders.powder_cerium );
        CraftingManager.addRecipeAuto(new ItemStack(Powders.powder_lanthanium, 1), "###", "###", "###", '#', Powders.powder_lanthanium_tiny );
        CraftingManager.addRecipeAuto(new ItemStack(Powders.powder_lanthanium_tiny, 9), "#", '#', Powders.powder_lanthanium );
        CraftingManager.addRecipeAuto(new ItemStack(Powders.powder_actinium, 1), "###", "###", "###", '#', Powders.powder_actinium_tiny );
        CraftingManager.addRecipeAuto(new ItemStack(Powders.powder_actinium_tiny, 9), "#", '#', Powders.powder_actinium );
        CraftingManager.addRecipeAuto(new ItemStack(Powders.powder_meteorite, 1), "###", "###", "###", '#', Powders.powder_meteorite_tiny );
        CraftingManager.addRecipeAuto(new ItemStack(Powders.powder_meteorite_tiny, 9), "#", '#', Powders.powder_meteorite );
        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_solinium, 1), "###", "###", "###", '#', Nuggies.nugget_solinium );
        CraftingManager.addRecipeAuto(new ItemStack(Nuggies.nugget_solinium, 9), "#", '#', Ingots.ingot_solinium );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nuclear_waste, 1), "###", "###", "###", '#', ModItems.nuclear_waste_tiny );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.nuclear_waste_tiny, 9), "#", '#', ModItems.nuclear_waste );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.bottle_mercury, 1), "###", "#B#", "###", '#', Nuggies.ingot_mercury, 'B', Items.GLASS_BOTTLE );
        CraftingManager.addRecipeAuto(new ItemStack(Nuggies.ingot_mercury, 8), "#", '#', ModItems.bottle_mercury );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.egg_balefire, 1), "###", "###", "###", '#', ModItems.egg_balefire_shard );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.egg_balefire_shard, 9), "#", '#', ModItems.egg_balefire );
        CraftingManager.addRecipeAuto(new ItemStack(Powders.nitra, 1), "##", "##", '#', Powders.nitra_small );
        CraftingManager.addRecipeAuto(new ItemStack(Powders.nitra_small, 4), "#", '#', Powders.nitra );
        CraftingManager.addRecipeAuto(new ItemStack(ModBlocks.glass_polarized, 4), "##", "##", '#', DictFrame.fromOne(ModItems.part_generic, ItemEnums.EnumPartType.GLASS_POLARIZED) );
        add1To9Pair(Powders.powder_paleogenite, Powders.powder_paleogenite_tiny);
        add1To9Pair(Ingots.ingot_osmiridium, Nuggies.nugget_osmiridium);

        CraftingManager.addRecipeAuto(new ItemStack(ModItems.egg_balefire_shard, 1), "##", "##", '#', Powders.powder_balefire );
        add9To1(ModItems.cell_balefire, ModItems.egg_balefire_shard);

        CraftingManager.addRecipeAuto(new ItemStack(Ingots.ingot_euphemium, 1), "###", "###", "###", '#', Nuggies.nugget_euphemium );
        CraftingManager.addRecipeAuto(new ItemStack(Nuggies.nugget_euphemium, 9), "#", '#', Ingots.ingot_euphemium );

        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Ingots.ingot_schrabidium_fuel, 1), "nuggetSchrabidium", "nuggetSchrabidium", "nuggetSchrabidium", "nuggetNeptunium237", "nuggetNeptunium237", "nuggetNeptunium237", Nuggies.nugget_beryllium, Nuggies.nugget_beryllium, Nuggies.nugget_beryllium );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Ingots.ingot_hes, 1), "nuggetSchrabidium", "nuggetSchrabidium", "nuggetSchrabidium", "nuggetSchrabidium", "nuggetSchrabidium", "nuggetNeptunium237", "nuggetNeptunium237", Nuggies.nugget_beryllium, Nuggies.nugget_beryllium );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Ingots.ingot_les, 1), "nuggetSchrabidium", "nuggetNeptunium237", "nuggetNeptunium237", "nuggetNeptunium237", "nuggetNeptunium237", Nuggies.nugget_beryllium, Nuggies.nugget_beryllium, Nuggies.nugget_beryllium, Nuggies.nugget_beryllium );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Ingots.ingot_pu_mix, 1), "nuggetPlutonium239", "nuggetPlutonium239", "nuggetPlutonium239", "nuggetPlutonium239", "nuggetPlutonium239", "nuggetPlutonium239", "nuggetPluonium240", "nuggetPluonium240", "nuggetPluonium240" );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Ingots.ingot_pu_mix, 1), "tinyPu239", "tinyPu239", "tinyPu239", "tinyPu239", "tinyPu239", "tinyPu239", "tinyPu240", "tinyPu240", "tinyPu240" );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Ingots.ingot_am_mix, 1), "nuggetAmericium241", "nuggetAmericium241", "nuggetAmericium241", "nuggetAmericium242", "nuggetAmericium242", "nuggetAmericium242", "nuggetAmericium242", "nuggetAmericium242", "nuggetAmericium242" );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(Ingots.ingot_am_mix, 1), "tinyAm241", "tinyAm241", "tinyAm241", "tinyAm242", "tinyAm242", "tinyAm242", "tinyAm242", "tinyAm242", "tinyAm242" );

        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.ball_fireclay, 4), Items.CLAY_BALL, Items.CLAY_BALL, Items.CLAY_BALL, AL.dust() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.ball_fireclay, 4), Items.CLAY_BALL, Items.CLAY_BALL, Items.CLAY_BALL, AL.ore() );
        CraftingManager.addRecipeAutoOreShapeless(new ItemStack(ModItems.ball_fireclay, 4), Items.CLAY_BALL, Items.CLAY_BALL, DictFrame.fromOne(ModBlocks.stone_resource, BlockEnums.EnumStoneType.LIMESTONE), KEY_SAND );
    }

    //Bundled 1/9 recipes
    public static void add1To9Pair(Item one, Item nine) {
        add1To9(new ItemStack(one), new ItemStack(nine, 9));
        add9To1(new ItemStack(nine), new ItemStack(one));
    }

    public static void add1To9Pair(Block one, Item nine) {
        add1To9(new ItemStack(one), new ItemStack(nine, 9));
        add9To1(new ItemStack(nine), new ItemStack(one));
    }

    public static void add1To9PairSameMeta(Item one, Item nine, int meta) {
        add1To9SameMeta(one, nine, meta);
        add9To1SameMeta(nine, one, meta);
    }

    //Full set of nugget, ingot and block
    public static void addMineralSet(Item nugget, Item ingot, Block block) {
        add1To9(new ItemStack(ingot), new ItemStack(nugget, 9));
        add9To1(new ItemStack(nugget), new ItemStack(ingot));
        add1To9(new ItemStack(block), new ItemStack(ingot, 9));
        add9To1(new ItemStack(ingot), new ItemStack(block));
    }

    //Decompress one item into nine
    public static void add1To9(Block one, Item nine) {
        add1To9(new ItemStack(one), new ItemStack(nine, 9));
    }

    public static void add1To9(Item one, Item nine) {
        add1To9(new ItemStack(one), new ItemStack(nine, 9));
    }

    public static void add1To9SameMeta(Item one, Item nine, int meta) {
        add1To9(new ItemStack(one, 1, meta), new ItemStack(nine, 9, meta));
    }

    public static void add1To9(ItemStack one, ItemStack nine){
        CraftingManager.addShapelessAuto(nine, one );
    }

    //Compress nine items into one
    public static void add9To1(Item nine, Block one) {
        add9To1(new ItemStack(nine), new ItemStack(one));
    }

    public static void add9To1(Item nine, Item one) {
        add9To1(new ItemStack(nine), new ItemStack(one));
    }

    public static void add9To1SameMeta(Item nine, Item one, int meta) {
        add9To1(new ItemStack(nine, 1, meta), new ItemStack(one, 1, meta));
    }

    public static void add9To1(ItemStack nine, ItemStack one){
        CraftingManager.addRecipeAuto(one, "###", "###", "###", '#', nine );
    }

    public static void addBillet(Item billet, Item nugget, String... ore){
        for(String o : ore) CraftingManager.addRecipeAuto(new ItemStack(billet), "###", "###", '#', o );
        addBillet(billet, nugget);
    }

    public static void addBillet(Item billet, Item ingot, Item nugget, String... ore) {
        for(String o : ore) CraftingManager.addRecipeAuto(new ItemStack(billet), "###", "###", '#', o );
        addBillet(billet, ingot, nugget);
    }

    public static void addBilletFragment(ItemStack billet, ItemStack nugget) {
        CraftingManager.addRecipeAuto(billet.copy(), "###", "###", '#', nugget );
    }

    public static void addBillet(Item billet, Item nugget) {
        CraftingManager.addRecipeAuto(new ItemStack(billet), "###", "###", '#', nugget );
        CraftingManager.addShapelessAuto(new ItemStack(nugget, 6), billet );
    }

    public static void addBillet(Item billet, Item ingot, Item nugget) {
        CraftingManager.addRecipeAuto(new ItemStack(billet), "###", "###", '#', nugget );
        CraftingManager.addShapelessAuto(new ItemStack(nugget, 6), billet );
        addBilletToIngot(billet, ingot);
    }

    public static void addBilletToIngot(Item billet, Item ingot) {
        CraftingManager.addShapelessAuto(new ItemStack(ingot, 2), billet, billet, billet );
        CraftingManager.addRecipeAuto(new ItemStack(billet, 3), "##", '#', ingot );
    }
}
