package com.hbm.inventory.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.handler.imc.IMCCentrifuge;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ItemEnums;
import com.hbm.items.ModItems;
import com.hbm.items.ModItems.Materials.Crystals;
import com.hbm.items.ModItems.Materials.Ingots;
import com.hbm.items.ModItems.Materials.Nuggies;
import com.hbm.items.ModItems.Materials.Powders;
import com.hbm.items.special.ItemBedrockOreNew;
import com.hbm.main.MainRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import static com.hbm.inventory.OreDictManager.*;

public class CentrifugeRecipes extends SerializableRecipe {

	public static HashMap<RecipesCommon.AStack, ItemStack[]> recipes = new HashMap<>();

    public static void removeRecipe(RecipesCommon.AStack key) {
        recipes.remove(key);
    }

    public static void addRecipe(RecipesCommon.AStack key, ItemStack[] value) {
        recipes.put(key, value);
    }

    @Override
	public void registerDefaults() {

		boolean lbs = GeneralConfig.enableLBSM && GeneralConfig.enableLBSMSimpleCentrifuge;

		recipes.put(new ComparableStack(DictFrame.fromOne(ModItems.chunk_ore, ItemEnums.EnumChunkType.RARE)), new ItemStack[] {
				new ItemStack(Powders.powder_cobalt_tiny, 2),
				new ItemStack(Powders.powder_boron_tiny, 2),
				new ItemStack(Powders.powder_niobium_tiny, 2),
				new ItemStack(Nuggies.nugget_zirconium, 3) });

		recipes.put(new RecipesCommon.OreDictStack(COAL.ore()), new ItemStack[] {
				new ItemStack(Powders.powder_coal, 2),
				new ItemStack(Powders.powder_coal, 2),
				new ItemStack(Powders.powder_coal, 2),
				new ItemStack(Blocks.GRAVEL, 1) });

		recipes.put(new RecipesCommon.OreDictStack(LIGNITE.ore()), new ItemStack[] {
				new ItemStack(Powders.powder_lignite, 2),
				new ItemStack(Powders.powder_lignite, 2),
				new ItemStack(Powders.powder_lignite, 2),
				new ItemStack(Blocks.GRAVEL, 1) });

		recipes.put(new RecipesCommon.OreDictStack(IRON.ore()), new ItemStack[] {
				new ItemStack(Powders.powder_iron, 1),
				new ItemStack(Powders.powder_iron, 1),
				new ItemStack(Powders.powder_iron, 1),
				new ItemStack(Blocks.GRAVEL, 1) });

		recipes.put(new RecipesCommon.OreDictStack(GOLD.ore()), new ItemStack[] {
				lbs ? new ItemStack(Powders.powder_gold, 2) : new ItemStack(Powders.powder_gold, 1),
				new ItemStack(Powders.powder_gold, 1),
				lbs ? new ItemStack(Nuggies.nugget_bismuth, 1) : new ItemStack(Powders.powder_gold, 1),
				new ItemStack(Blocks.GRAVEL, 1) });

		recipes.put(new RecipesCommon.OreDictStack(DIAMOND.ore()), new ItemStack[] {
				new ItemStack(Powders.powder_diamond, 1),
				new ItemStack(Powders.powder_diamond, 1),
				new ItemStack(Powders.powder_diamond, 1),
				new ItemStack(Blocks.GRAVEL, 1) });

		recipes.put(new RecipesCommon.OreDictStack(EMERALD.ore()), new ItemStack[] {
				new ItemStack(Powders.powder_emerald, 1),
				new ItemStack(Powders.powder_emerald, 1),
				new ItemStack(Powders.powder_emerald, 1),
				new ItemStack(Blocks.GRAVEL, 1) });

		recipes.put(new RecipesCommon.OreDictStack(TI.ore()), new ItemStack[] {
				lbs ? new ItemStack(Powders.powder_titanium, 2) : new ItemStack(Powders.powder_titanium, 1),
				lbs ? new ItemStack(Powders.powder_titanium, 2) : new ItemStack(Powders.powder_titanium, 1),
				new ItemStack(Powders.powder_iron, 1),
				new ItemStack(Blocks.GRAVEL, 1) });

		recipes.put(new RecipesCommon.OreDictStack(NETHERQUARTZ.ore()), new ItemStack[] {
				new ItemStack(Powders.powder_quartz, 1),
				new ItemStack(Powders.powder_quartz, 1),
				new ItemStack(Powders.powder_lithium_tiny, 1),
				new ItemStack(Blocks.NETHERRACK, 1) });

		recipes.put(new RecipesCommon.OreDictStack(W.ore()), new ItemStack[] {
				lbs ? new ItemStack(Powders.powder_tungsten, 2) : new ItemStack(Powders.powder_tungsten, 1),
				new ItemStack(Powders.powder_tungsten, 1),
				new ItemStack(Powders.powder_iron, 1),
				new ItemStack(Blocks.GRAVEL, 1) });

		recipes.put(new RecipesCommon.OreDictStack(CU.ore()), new ItemStack[] {
				lbs ? new ItemStack(Powders.powder_copper, 2) : new ItemStack(Powders.powder_copper, 1),
				new ItemStack(Powders.powder_copper, 1),
				new ItemStack(Powders.powder_gold, 1),
				new ItemStack(Blocks.GRAVEL, 1) });

		recipes.put(new RecipesCommon.OreDictStack(AL.ore()), new ItemStack[] {
				new ItemStack(ModItems.chunk_ore, 2, ItemEnums.EnumChunkType.CRYOLITE.ordinal()),
				new ItemStack(Powders.powder_titanium, 1),
				new ItemStack(Powders.powder_iron, 1),
				new ItemStack(Blocks.GRAVEL, 1) });

		recipes.put(new RecipesCommon.OreDictStack(PB.ore()), new ItemStack[] {
				lbs ? new ItemStack(Powders.powder_lead, 2) : new ItemStack(Powders.powder_lead, 1),
				lbs ? new ItemStack(Nuggies.nugget_bismuth, 1) : new ItemStack(Powders.powder_lead, 1),
				new ItemStack(Powders.powder_gold, 1),
				new ItemStack(Blocks.GRAVEL, 1) });

		recipes.put(new RecipesCommon.OreDictStack(SA326.ore()), new ItemStack[] {
				new ItemStack(Powders.powder_schrabidium, 1),
				new ItemStack(Powders.powder_schrabidium, 1),
				new ItemStack(Nuggies.nugget_solinium, 1),
				new ItemStack(Blocks.GRAVEL, 1) });

		recipes.put(new RecipesCommon.OreDictStack("oreRareEarth"), new ItemStack[] {
				new ItemStack(Powders.powder_desh_mix, 1),
				new ItemStack(Nuggies.nugget_zirconium, 1),
				new ItemStack(Nuggies.nugget_zirconium, 1),
				new ItemStack(Blocks.GRAVEL, 1) });

		recipes.put(new RecipesCommon.OreDictStack(PU.ore()), new ItemStack[] {
				new ItemStack(Powders.powder_plutonium, 1),
				new ItemStack(Powders.powder_plutonium, 1),
				new ItemStack(Nuggies.nugget_polonium, 3),
				new ItemStack(Blocks.GRAVEL, 1) });

		recipes.put(new RecipesCommon.OreDictStack(U.ore()), new ItemStack[] {
				lbs ? new ItemStack(Powders.powder_uranium, 2) : new ItemStack(Powders.powder_uranium, 1),
				lbs ? new ItemStack(Nuggies.nugget_technetium, 2) : new ItemStack(Powders.powder_uranium, 1),
				lbs ? new ItemStack(Nuggies.nugget_ra226, 2) : new ItemStack(Nuggies.nugget_ra226, 1),
				new ItemStack(Blocks.GRAVEL, 1) });

		recipes.put(new RecipesCommon.OreDictStack(TH232.ore()), new ItemStack[] {
				new ItemStack(Powders.powder_thorium, 1),
				new ItemStack(Powders.powder_thorium, 1),
				new ItemStack(Powders.powder_uranium, 1),
				new ItemStack(Blocks.GRAVEL, 1) });

		recipes.put(new RecipesCommon.OreDictStack(BE.ore()), new ItemStack[] {
				new ItemStack(Powders.powder_beryllium, 1),
				new ItemStack(Powders.powder_beryllium, 1),
				new ItemStack(Powders.powder_emerald, 1),
				new ItemStack(Blocks.GRAVEL, 1) });

		recipes.put(new RecipesCommon.OreDictStack(F.ore()), new ItemStack[] {
				new ItemStack(Ingots.fluorite, 3),
				new ItemStack(Ingots.fluorite, 3),
				new ItemStack(ModItems.gem_sodalite, 1),
				new ItemStack(Blocks.GRAVEL, 1) });

		recipes.put(new RecipesCommon.OreDictStack(REDSTONE.ore()), new ItemStack[] {
				new ItemStack(Items.REDSTONE, 3),
				new ItemStack(Items.REDSTONE, 3),
				lbs ? new ItemStack(Nuggies.ingot_mercury, 3) : new ItemStack(Nuggies.ingot_mercury, 1),
				new ItemStack(Blocks.GRAVEL, 1) });

		recipes.put(new ComparableStack(ModBlocks.ore_tikite), new ItemStack[] {
				new ItemStack(Powders.powder_plutonium, 1),
				new ItemStack(Powders.powder_cobalt, 2),
				new ItemStack(Powders.powder_niobium, 2),
				new ItemStack(Blocks.END_STONE, 1) });

		recipes.put(new RecipesCommon.OreDictStack(LAPIS.ore()), new ItemStack[] {
				new ItemStack(Powders.powder_lapis, 6),
				new ItemStack(Powders.powder_cobalt_tiny, 1),
				new ItemStack(ModItems.gem_sodalite, 1),
				new ItemStack(Blocks.GRAVEL, 1) });

		recipes.put(new ComparableStack(ModBlocks.block_euphemium_cluster), new ItemStack[] {
				new ItemStack(Nuggies.nugget_euphemium, 7),
				new ItemStack(Powders.powder_schrabidium, 4),
				new ItemStack(Ingots.ingot_starmetal, 2),
				new ItemStack(Nuggies.nugget_solinium, 2) });

		recipes.put(new ComparableStack(ModBlocks.ore_nether_fire), new ItemStack[] {
				new ItemStack(Items.BLAZE_POWDER, 2),
				new ItemStack(Powders.powder_fire, 2),
				new ItemStack(Ingots.ingot_phosphorus),
				new ItemStack(Blocks.NETHERRACK) });

		recipes.put(new RecipesCommon.OreDictStack(CO.ore()), new ItemStack[] {
				new ItemStack(Powders.powder_cobalt, 2),
				new ItemStack(Powders.powder_iron, 1),
				new ItemStack(Powders.powder_copper, 1),
				new ItemStack(Blocks.GRAVEL, 1) });

		recipes.put(new ComparableStack(Powders.powder_tektite), new ItemStack[] {
				new ItemStack(Powders.powder_meteorite_tiny, 1),
				new ItemStack(Powders.powder_paleogenite_tiny, 1),
				new ItemStack(Powders.powder_meteorite_tiny, 1),
				new ItemStack(ModItems.dust, 6) });

		recipes.put(new ComparableStack(ModBlocks.block_slag), new ItemStack[] {
				new ItemStack(Blocks.GRAVEL, 1),
				new ItemStack(Powders.powder_fire, 1),
				new ItemStack(Powders.powder_calcium),
				new ItemStack(ModItems.dust) });

		recipes.put(new ComparableStack(ModItems.powder_ash, 1, ItemEnums.EnumAshType.COAL.ordinal()), new ItemStack[] {
				new ItemStack(Powders.powder_coal_tiny, 2),
				new ItemStack(Powders.powder_boron_tiny, 1),
				new ItemStack(ModItems.dust_tiny, 6)});

		for(ItemBedrockOreNew.BedrockOreType type : ItemBedrockOreNew.BedrockOreType.values()) {

			recipes.put(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.BASE, type)), new ItemStack[] {ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY, type), new ItemStack(Blocks.GRAVEL)});
			recipes.put(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.BASE_ROASTED, type)), new ItemStack[] {ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY, type), new ItemStack(Blocks.GRAVEL)});
			recipes.put(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.BASE_WASHED, type)), new ItemStack[] {ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY, type), ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY, type), new ItemStack(Blocks.GRAVEL)});

			recipes.put(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_SULFURIC, type)), new ItemStack[] {ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_NOSULFURIC, type, 2), ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SULFURIC_BYPRODUCT, type, 2)});
			recipes.put(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_SOLVENT, type)), new ItemStack[] {ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_NOSOLVENT, type, 2), ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SULFURIC_BYPRODUCT, type, 2), ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SOLVENT_BYPRODUCT, type, 2)});
			recipes.put(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_RAD, type)), new ItemStack[] {ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_NORAD, type, 2), ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SULFURIC_BYPRODUCT, type, 2), ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SOLVENT_BYPRODUCT, type, 2), ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.RAD_BYPRODUCT, type, 2)});

			recipes.put(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY, type)), new ItemStack[] {ItemBedrockOreNew.extract(type.primary1, 1), ItemBedrockOreNew.extract(type.primary2, 1)});
			recipes.put(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_ROASTED, type)), new ItemStack[] {ItemBedrockOreNew.extract(type.primary1, 1), ItemBedrockOreNew.extract(type.primary2, 1)});
			recipes.put(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_NOSULFURIC, type)), new ItemStack[] {ItemBedrockOreNew.extract(type.primary1, 1), ItemBedrockOreNew.extract(type.primary2, 1), ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.CRUMBS, type)});
			recipes.put(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_NOSOLVENT, type)), new ItemStack[] {ItemBedrockOreNew.extract(type.primary1, 1), ItemBedrockOreNew.extract(type.primary2, 1), ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.CRUMBS, type)});
			recipes.put(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_NORAD, type)), new ItemStack[] {ItemBedrockOreNew.extract(type.primary1, 1), ItemBedrockOreNew.extract(type.primary2, 1), ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.CRUMBS, type)});
			recipes.put(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_FIRST, type)), new ItemStack[] {ItemBedrockOreNew.extract(type.primary1, 1), ItemBedrockOreNew.extract(type.primary1, 1), ItemBedrockOreNew.extract(type.primary2, 1), ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.CRUMBS, type, 1)});
			recipes.put(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.PRIMARY_SECOND, type)), new ItemStack[] {ItemBedrockOreNew.extract(type.primary1, 1), ItemBedrockOreNew.extract(type.primary2, 1), ItemBedrockOreNew.extract(type.primary2, 1), ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.CRUMBS, type, 1)});

			recipes.put(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SULFURIC_WASHED, type)), new ItemStack[] {ItemBedrockOreNew.extract(type.byproductAcid1, 1), ItemBedrockOreNew.extract(type.byproductAcid2, 1), ItemBedrockOreNew.extract(type.byproductAcid3, 1), ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.CRUMBS, type)});
			recipes.put(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.SOLVENT_WASHED, type)), new ItemStack[] {ItemBedrockOreNew.extract(type.byproductSolvent1, 1), ItemBedrockOreNew.extract(type.byproductSolvent2, 1), ItemBedrockOreNew.extract(type.byproductSolvent3, 1), ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.CRUMBS, type)});
			recipes.put(new ComparableStack(ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.RAD_WASHED, type)), new ItemStack[] {ItemBedrockOreNew.extract(type.byproductRad1, 1), ItemBedrockOreNew.extract(type.byproductRad2, 1), ItemBedrockOreNew.extract(type.byproductRad3, 1), ItemBedrockOreNew.make(ItemBedrockOreNew.BedrockOreGrade.CRUMBS, type)});
		}

		List<ItemStack> quartz = OreDictionary.getOres("crystalCertusQuartz");

		if(quartz != null && !quartz.isEmpty()) {
			ItemStack qItem = quartz.get(0).copy();
			qItem.grow(2);

			recipes.put(new RecipesCommon.OreDictStack("oreCertusQuartz"), new ItemStack[] {
					qItem.copy(),
					qItem.copy(),
					qItem.copy(),
					qItem.copy() });
		}

		recipes.put(new ComparableStack(Items.BLAZE_ROD), new ItemStack[] {new ItemStack(Items.BLAZE_POWDER, 1), new ItemStack(Items.BLAZE_POWDER, 1), new ItemStack(Powders.powder_fire, 1), new ItemStack(Powders.powder_fire, 1) });

		recipes.put(new ComparableStack(Ingots.ingot_schraranium), new ItemStack[] { new ItemStack(Nuggies.nugget_schrabidium, 2), new ItemStack(Nuggies.nugget_schrabidium, 1), new ItemStack(Nuggies.nugget_uranium, 3), new ItemStack(Nuggies.nugget_neptunium, 2) });

		recipes.put(new ComparableStack(Crystals.crystal_coal), new ItemStack[] { new ItemStack(Powders.powder_coal, 3), new ItemStack(Powders.powder_coal, 3), new ItemStack(Powders.powder_coal, 3), new ItemStack(Powders.powder_lithium_tiny, 1) });
		recipes.put(new ComparableStack(Crystals.crystal_iron), new ItemStack[] { new ItemStack(Powders.powder_iron, 2), new ItemStack(Powders.powder_iron, 2), new ItemStack(Powders.powder_titanium, 1), new ItemStack(Powders.powder_lithium_tiny, 1) });
		recipes.put(new ComparableStack(Crystals.crystal_gold), new ItemStack[] { new ItemStack(Powders.powder_gold, 2), new ItemStack(Powders.powder_gold, 2), new ItemStack(Nuggies.ingot_mercury, 1), new ItemStack(Powders.powder_lithium_tiny, 1) });
		recipes.put(new ComparableStack(Crystals.crystal_redstone), new ItemStack[] { new ItemStack(Items.REDSTONE, 3), new ItemStack(Items.REDSTONE, 3), new ItemStack(Items.REDSTONE, 3), new ItemStack(Nuggies.ingot_mercury, 3) });
		recipes.put(new ComparableStack(Crystals.crystal_lapis), new ItemStack[] { new ItemStack(Powders.powder_lapis, 4), new ItemStack(Powders.powder_lapis, 4), new ItemStack(Powders.powder_cobalt, 1), new ItemStack(ModItems.gem_sodalite, 2) });
		recipes.put(new ComparableStack(Crystals.crystal_diamond), new ItemStack[] { new ItemStack(Powders.powder_diamond, 1), new ItemStack(Powders.powder_diamond, 1), new ItemStack(Powders.powder_diamond, 1), new ItemStack(Powders.powder_diamond, 1) });
		recipes.put(new ComparableStack(Crystals.crystal_uranium), new ItemStack[] { new ItemStack(Powders.powder_uranium, 2), new ItemStack(Powders.powder_uranium, 2), new ItemStack(Nuggies.nugget_ra226, 2), new ItemStack(Powders.powder_lithium_tiny, 1) });
		recipes.put(new ComparableStack(Crystals.crystal_thorium), new ItemStack[] { new ItemStack(Powders.powder_thorium, 2), new ItemStack(Powders.powder_thorium, 2), new ItemStack(Powders.powder_uranium, 1), new ItemStack(Nuggies.nugget_ra226, 1) });
		recipes.put(new ComparableStack(Crystals.crystal_plutonium), new ItemStack[] { new ItemStack(Powders.powder_plutonium, 2), new ItemStack(Powders.powder_plutonium, 2), new ItemStack(Powders.powder_polonium, 1), new ItemStack(Powders.powder_lithium_tiny, 1) });
		recipes.put(new ComparableStack(Crystals.crystal_titanium), new ItemStack[] { new ItemStack(Powders.powder_titanium, 2), new ItemStack(Powders.powder_titanium, 2), new ItemStack(Powders.powder_iron, 1), new ItemStack(Powders.powder_lithium_tiny, 1) });
		recipes.put(new ComparableStack(Crystals.crystal_sulfur), new ItemStack[] { new ItemStack(Powders.sulfur, 4), new ItemStack(Powders.sulfur, 4), new ItemStack(Powders.powder_iron, 1), new ItemStack(Nuggies.ingot_mercury, 1) });
		recipes.put(new ComparableStack(Crystals.crystal_niter), new ItemStack[] { new ItemStack(Powders.niter, 3), new ItemStack(Powders.niter, 3), new ItemStack(Powders.niter, 3), new ItemStack(Powders.powder_lithium_tiny, 1) });
		recipes.put(new ComparableStack(Crystals.crystal_copper), new ItemStack[] { new ItemStack(Powders.powder_copper, 2), new ItemStack(Powders.powder_copper, 2), new ItemStack(Powders.sulfur, 1), new ItemStack(Powders.powder_cobalt_tiny, 1) });
		recipes.put(new ComparableStack(Crystals.crystal_tungsten), new ItemStack[] { new ItemStack(Powders.powder_tungsten, 2), new ItemStack(Powders.powder_tungsten, 2), new ItemStack(Powders.powder_iron, 1), new ItemStack(Powders.powder_lithium_tiny, 1) });
		recipes.put(new ComparableStack(Crystals.crystal_aluminium), new ItemStack[] { new ItemStack(ModItems.chunk_ore, 3, ItemEnums.EnumChunkType.CRYOLITE.ordinal()), new ItemStack(Powders.powder_titanium, 1), new ItemStack(Powders.powder_iron, 1), new ItemStack(Powders.powder_lithium_tiny, 1) });
		recipes.put(new ComparableStack(Crystals.crystal_fluorite), new ItemStack[] { new ItemStack(Ingots.fluorite, 4), new ItemStack(Ingots.fluorite, 4), new ItemStack(ModItems.gem_sodalite, 2), new ItemStack(Powders.powder_lithium_tiny, 1) });
		recipes.put(new ComparableStack(Crystals.crystal_beryllium), new ItemStack[] { new ItemStack(Powders.powder_beryllium, 2), new ItemStack(Powders.powder_beryllium, 2), new ItemStack(Powders.powder_quartz, 1), new ItemStack(Powders.powder_lithium_tiny, 1) });
		recipes.put(new ComparableStack(Crystals.crystal_lead), new ItemStack[] { new ItemStack(Powders.powder_lead, 2), new ItemStack(Powders.powder_lead, 2), new ItemStack(Powders.powder_gold, 1), new ItemStack(Powders.powder_lithium_tiny, 1) });
		recipes.put(new ComparableStack(Crystals.crystal_schraranium), new ItemStack[] { new ItemStack(Nuggies.nugget_schrabidium, 2), new ItemStack(Nuggies.nugget_schrabidium, 2), new ItemStack(Nuggies.nugget_uranium, 2), new ItemStack(Nuggies.nugget_neptunium, 2) });
		recipes.put(new ComparableStack(Crystals.crystal_schrabidium), new ItemStack[] { new ItemStack(Powders.powder_schrabidium, 2), new ItemStack(Powders.powder_schrabidium, 2), new ItemStack(Powders.powder_plutonium, 1), new ItemStack(Powders.powder_lithium_tiny, 1) });
		recipes.put(new ComparableStack(Crystals.crystal_rare), new ItemStack[] { new ItemStack(Powders.powder_desh_mix, 1), new ItemStack(Powders.powder_desh_mix, 1), new ItemStack(Nuggies.nugget_zirconium, 2), new ItemStack(Nuggies.nugget_zirconium, 2) });
		recipes.put(new ComparableStack(Crystals.crystal_phosphorus), new ItemStack[] { new ItemStack(Powders.powder_fire, 3), new ItemStack(Powders.powder_fire, 3), new ItemStack(Ingots.ingot_phosphorus, 2), new ItemStack(Items.BLAZE_POWDER, 2) });
		recipes.put(new ComparableStack(Crystals.crystal_trixite), new ItemStack[] { new ItemStack(Powders.powder_plutonium, 2), new ItemStack(Powders.powder_cobalt, 3), new ItemStack(Powders.powder_niobium, 2), new ItemStack(Powders.powder_nitan_mix, 1) });
		recipes.put(new ComparableStack(Crystals.crystal_lithium), new ItemStack[] { new ItemStack(Powders.powder_lithium, 2), new ItemStack(Powders.powder_lithium, 2), new ItemStack(Powders.powder_quartz, 1), new ItemStack(Ingots.fluorite, 1) });
		recipes.put(new ComparableStack(Crystals.crystal_starmetal), new ItemStack[] { new ItemStack(Powders.powder_dura_steel, 3), new ItemStack(Powders.powder_cobalt, 3), new ItemStack(Powders.powder_astatine, 2), new ItemStack(Nuggies.ingot_mercury, 5) });
		recipes.put(new ComparableStack(Crystals.crystal_cobalt), new ItemStack[] { new ItemStack(Powders.powder_cobalt, 2), new ItemStack(Powders.powder_iron, 3), new ItemStack(Powders.powder_copper, 3), new ItemStack(Powders.powder_lithium_tiny, 1) });
	}

	@Override
	public void registerPost() {

		if(!IMCCentrifuge.buffer.isEmpty()) {
			recipes.putAll(IMCCentrifuge.buffer);
			MainRegistry.logger.info("Fetched " + IMCCentrifuge.buffer.size() + " IMC centrifuge recipes!");
			IMCCentrifuge.buffer.clear();
		}
	}

	@Nullable
	public static ItemStack[] getOutput(@Nullable ItemStack stack) {

		if(stack == null || stack.isEmpty())
			return null;

		ComparableStack comp = new ComparableStack(stack).makeSingular();

		if(recipes.containsKey(comp))
			return RecipesCommon.copyStackArray(recipes.get(comp));

		for(Entry<RecipesCommon.AStack, ItemStack[]> entry : recipes.entrySet()) {
			if(entry.getKey().isApplicable(stack)) {
				return RecipesCommon.copyStackArray(entry.getValue());
			}
		}

		return null;
	}

	public static HashMap<Object, Object[]> getRecipes() {

		HashMap<Object, Object[]> recipes = new HashMap<Object, Object[]>();

		for(Entry<RecipesCommon.AStack, ItemStack[]> entry : CentrifugeRecipes.recipes.entrySet()) {
			recipes.put(entry.getKey(), entry.getValue());
		}

		return recipes;
	}

	@Override
	public String getFileName() {
		return "hbmCentrifuge.json";
	}

	@Override
	public Object getRecipeObject() {
		return recipes;
	}

	@Override
	public void readRecipe(JsonElement recipe) {
		JsonObject obj = (JsonObject) recipe;
		RecipesCommon.AStack in = this.readAStack(obj.get("input").getAsJsonArray());
		ItemStack[] out = this.readItemStackArray((JsonArray) obj.get("output"));
		this.recipes.put(in, out);
	}

	@Override
	public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
		try {
			Entry<RecipesCommon.AStack, ItemStack[]> entry = (Entry<RecipesCommon.AStack, ItemStack[]>) recipe;
			writer.name("input");
			this.writeAStack(entry.getKey(), writer);
			writer.name("output").beginArray();
			for(ItemStack stack : entry.getValue()) {
				this.writeItemStack(stack, writer);
			}
			writer.endArray();
		} catch(Exception ex) {
			MainRegistry.logger.error(ex);
			ex.printStackTrace();
		}
	}

	@Override
	public void deleteRecipes() {
		recipes.clear();
	}

	@Override
	public String getComment() {
		return "Outputs have to be an array of up to four item stacks. Fewer aren't used by default recipes, but should work anyway.";
	}
}