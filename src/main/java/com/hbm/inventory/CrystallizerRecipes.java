package com.hbm.inventory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ItemEnums;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFluidIcon;
import com.hbm.util.Tuple;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import static com.hbm.inventory.OreDictManager.*;

//This time we're doing this right
//...right?

//yes this time i will

//.. hold my beer
public class CrystallizerRecipes extends SerializableRecipe {

	//'Object' is either a ComparableStack or the key for the ore dict
	private static HashMap<Tuple.Pair<Object, FluidType>, CrystallizerRecipe> recipes = new HashMap();
	private static HashMap<Object, Integer> amounts = new HashMap(); // for use in the partitioner
	private static List<CrystallizerRecipe> jeiCrystalRecipes = null;
	@Override
	public void registerDefaults() {
		final int baseTime = 600;
		final int utilityTime = 100;
		final int mixingTime = 20;
		FluidStack sulfur = new FluidStack(Fluids.SULFURIC_ACID, 500);

		registerRecipe(COAL.ore(),		new CrystallizerRecipe(ModItems.crystal_coal, baseTime));
		registerRecipe(IRON.ore(),		new CrystallizerRecipe(ModItems.crystal_iron, baseTime));
		registerRecipe(GOLD.ore(),		new CrystallizerRecipe(ModItems.crystal_gold, baseTime));
		registerRecipe(REDSTONE.ore(),	new CrystallizerRecipe(ModItems.crystal_redstone, baseTime));
		registerRecipe(LAPIS.ore(),		new CrystallizerRecipe(ModItems.crystal_lapis, baseTime));
		registerRecipe(DIAMOND.ore(),	new CrystallizerRecipe(ModItems.crystal_diamond, baseTime));
		registerRecipe(U.ore(),			new CrystallizerRecipe(ModItems.crystal_uranium, baseTime), sulfur);
		registerRecipe(TH232.ore(),		new CrystallizerRecipe(ModItems.crystal_thorium, baseTime), sulfur);
		registerRecipe(PU.ore(),		new CrystallizerRecipe(ModItems.crystal_plutonium, baseTime), sulfur);
		registerRecipe(TI.ore(),		new CrystallizerRecipe(ModItems.crystal_titanium, baseTime), sulfur);
		registerRecipe(S.ore(),			new CrystallizerRecipe(ModItems.crystal_sulfur, baseTime));
		registerRecipe(KNO.ore(),		new CrystallizerRecipe(ModItems.crystal_niter, baseTime));
		registerRecipe(CU.ore(),		new CrystallizerRecipe(ModItems.crystal_copper, baseTime));
		registerRecipe(W.ore(),			new CrystallizerRecipe(ModItems.crystal_tungsten, baseTime), sulfur);
		registerRecipe(AL.ore(),		new CrystallizerRecipe(ModItems.crystal_aluminium, baseTime));
		registerRecipe(F.ore(),			new CrystallizerRecipe(ModItems.crystal_fluorite, baseTime));
		registerRecipe(BE.ore(),		new CrystallizerRecipe(ModItems.crystal_beryllium, baseTime));
		registerRecipe(PB.ore(),		new CrystallizerRecipe(ModItems.crystal_lead, baseTime));
		registerRecipe(SA326.ore(),		new CrystallizerRecipe(ModItems.crystal_schrabidium, baseTime), sulfur);
		registerRecipe(LI.ore(),		new CrystallizerRecipe(ModItems.crystal_lithium, baseTime), sulfur);
		//registerRecipe(STAR.ore(),		new CrystallizerRecipe(ModItems.crystal_starmetal, baseTime), sulfur);
		registerRecipe(CO.ore(),		new CrystallizerRecipe(ModItems.crystal_cobalt, baseTime), sulfur);

		registerRecipe("oreRareEarth",	new CrystallizerRecipe(ModItems.crystal_rare, baseTime), sulfur);
		registerRecipe("oreCinnabar",	new CrystallizerRecipe(ModItems.crystal_cinnabar, baseTime));

		registerRecipe(new ComparableStack(ModBlocks.ore_nether_fire),	new CrystallizerRecipe(ModItems.crystal_phosphorus, baseTime));
		registerRecipe(new ComparableStack(ModBlocks.ore_tikite),		new CrystallizerRecipe(ModItems.crystal_trixite, baseTime), sulfur);
		registerRecipe(new ComparableStack(ModBlocks.gravel_diamond),	new CrystallizerRecipe(ModItems.crystal_diamond, baseTime));
		registerRecipe(SRN.ingot(),										new CrystallizerRecipe(ModItems.crystal_schraranium, baseTime));

		registerRecipe(KEY_SAND,			new CrystallizerRecipe(ModItems.ingot_fiberglass, utilityTime));
		registerRecipe(BORAX.dust(),		new CrystallizerRecipe(new ItemStack(ModItems.powder_boron_tiny, 3), baseTime), sulfur);
		registerRecipe(COAL.block(),		new CrystallizerRecipe(ModBlocks.block_graphite, baseTime));

		registerRecipe(new ComparableStack(Blocks.COBBLESTONE),			new CrystallizerRecipe(ModBlocks.reinforced_stone, utilityTime));
		registerRecipe(new ComparableStack(ModBlocks.gravel_obsidian),	new CrystallizerRecipe(ModBlocks.brick_obsidian, utilityTime));
		registerRecipe(new ComparableStack(Items.ROTTEN_FLESH),			new CrystallizerRecipe(Items.LEATHER, utilityTime));
		registerRecipe(new ComparableStack(ModItems.coal_infernal),		new CrystallizerRecipe(ModItems.solid_fuel, utilityTime));
		registerRecipe(new ComparableStack(ModBlocks.stone_gneiss),		new CrystallizerRecipe(ModItems.powder_lithium, utilityTime));
		registerRecipe(new ComparableStack(Items.DYE, 1, 15),			new CrystallizerRecipe(new ItemStack(Items.SLIME_BALL, 4), mixingTime), new FluidStack(Fluids.SULFURIC_ACID, 250));
		registerRecipe(new ComparableStack(Items.BONE),					new CrystallizerRecipe(new ItemStack(Items.SLIME_BALL, 16), mixingTime), new FluidStack(Fluids.SULFURIC_ACID, 1_000));
		registerRecipe(new ComparableStack(ModItems.scrap_oil),			new CrystallizerRecipe(new ItemStack(ModItems.nugget_arsenic), 100).setReq(16), new FluidStack(Fluids.RADIOSOLVENT, 100));

		registerRecipe(DIAMOND.dust(), 									new CrystallizerRecipe(Items.DIAMOND, utilityTime));
		registerRecipe(EMERALD.dust(), 									new CrystallizerRecipe(Items.EMERALD, utilityTime));
		registerRecipe(LAPIS.dust(),									new CrystallizerRecipe(new ItemStack(Items.DYE, 1, 4), utilityTime));
		registerRecipe(new ComparableStack(ModItems.powder_semtex_mix),	new CrystallizerRecipe(ModItems.ingot_semtex, baseTime));
		registerRecipe(new ComparableStack(ModItems.powder_desh_ready),	new CrystallizerRecipe(ModItems.ingot_desh, baseTime));
		registerRecipe(new ComparableStack(ModItems.powder_meteorite),	new CrystallizerRecipe(ModItems.fragment_meteorite, utilityTime));
		registerRecipe(CD.dust(),										new CrystallizerRecipe(ModItems.ingot_rubber, utilityTime), new FluidStack(Fluids.FISHOIL, 250));
		registerRecipe(LATEX.ingot(),									new CrystallizerRecipe(ModItems.ingot_rubber, mixingTime), new FluidStack(Fluids.SOURGAS, 25));

		registerRecipe(new ComparableStack(ModItems.meteorite_sword_treated),	new CrystallizerRecipe(ModItems.meteorite_sword_etched, baseTime));
		registerRecipe(new ComparableStack(ModItems.powder_impure_osmiridium),	new CrystallizerRecipe(ModItems.crystal_osmiridium, baseTime), new FluidStack(Fluids.SCHRABIDIC, 1_000));

		FluidStack nitric = new FluidStack(Fluids.NITRIC_ACID, 500);
		FluidStack organic = new FluidStack(Fluids.SOLVENT, 500);
		FluidStack hiperf = new FluidStack(Fluids.RADIOSOLVENT, 500);

		int oreTime = 200;

		for(Integer i : BedrockOreRegistry.oreIndexes.keySet()) {

			registerRecipe(new ComparableStack(ModItems.ore_bedrock_centrifuged, 1, i),			new CrystallizerRecipe(new ItemStack(ModItems.ore_bedrock_cleaned, 1, i), oreTime));
			registerRecipe(new ComparableStack(ModItems.ore_bedrock_separated, 1, i),			new CrystallizerRecipe(new ItemStack(ModItems.ore_bedrock_purified, 1, i), oreTime), sulfur);
			registerRecipe(new ComparableStack(ModItems.ore_bedrock_separated, 1, i),			new CrystallizerRecipe(new ItemStack(ModItems.ore_bedrock_nitrated, 1, i), oreTime), nitric);
			registerRecipe(new ComparableStack(ModItems.ore_bedrock_nitrocrystalline, 1, i),	new CrystallizerRecipe(new ItemStack(ModItems.ore_bedrock_deepcleaned, 1, i), oreTime), organic);
			registerRecipe(new ComparableStack(ModItems.ore_bedrock_nitrocrystalline, 1, i),	new CrystallizerRecipe(new ItemStack(ModItems.ore_bedrock_seared, 1, i), oreTime), hiperf);
		}

		registerRecipe(new ComparableStack(DictFrame.fromOne(ModItems.oil_tar, ItemEnums.EnumTarType.CRUDE)),		new CrystallizerRecipe(DictFrame.fromOne(ModItems.oil_tar, ItemEnums.EnumTarType.WAX), 20),	new FluidStack(Fluids.CHLORINE, 250));
		registerRecipe(new ComparableStack(DictFrame.fromOne(ModItems.oil_tar, ItemEnums.EnumTarType.CRACK)),		new CrystallizerRecipe(DictFrame.fromOne(ModItems.oil_tar, ItemEnums.EnumTarType.WAX), 20),	new FluidStack(Fluids.CHLORINE, 100));
		registerRecipe(new ComparableStack(DictFrame.fromOne(ModItems.oil_tar, ItemEnums.EnumTarType.PARAFFIN)),	new CrystallizerRecipe(DictFrame.fromOne(ModItems.oil_tar, ItemEnums.EnumTarType.WAX), 20),	new FluidStack(Fluids.CHLORINE, 100));
		registerRecipe(new ComparableStack(DictFrame.fromOne(ModItems.oil_tar, ItemEnums.EnumTarType.WAX)), 		new CrystallizerRecipe(new ItemStack(ModItems.pellet_charged), 200), 				new FluidStack(Fluids.IONGEL, 500));

		registerRecipe(KEY_SAND, new CrystallizerRecipe(Blocks.CLAY, 20), new FluidStack(Fluids.COLLOID, 1_000));
		registerRecipe(new ComparableStack(ModBlocks.sand_quartz), new CrystallizerRecipe(new ItemStack(ModItems.ball_dynamite, 16), 20), new FluidStack(Fluids.NITROGLYCERIN, 1_000));
		registerRecipe(NETHERQUARTZ.dust(), new CrystallizerRecipe(new ItemStack(ModItems.ball_dynamite, 4), 20), new FluidStack(Fluids.NITROGLYCERIN, 250));

		/// COMPAT CERTUS QUARTZ ///
		List<ItemStack> quartz = OreDictionary.getOres("crystalCertusQuartz");
		if(quartz != null && !quartz.isEmpty()) {
			ItemStack qItem = quartz.get(0).copy();
			qItem.setCount(12);
			registerRecipe("oreCertusQuartz", new CrystallizerRecipe(qItem, baseTime));
		}

		/// COMPAT WHITE PHOSPHORUS DUST ///
		List<ItemStack> dustWhitePhosphorus = OreDictionary.getOres(P_WHITE.dust());
		if(dustWhitePhosphorus != null && !dustWhitePhosphorus.isEmpty()) {
			registerRecipe(P_WHITE.dust(), new CrystallizerRecipe(new ItemStack(ModItems.ingot_phosphorus), utilityTime), new FluidStack(Fluids.AROMATICS, 50));
		}
	}

	public static CrystallizerRecipe getOutput(ItemStack stack, FluidType type) {

		if(stack == null || stack.getItem() == null)
			return null;

		ComparableStack comp = new ComparableStack(stack.getItem(), 1, stack.getItemDamage());
		Tuple.Pair compKey = new Tuple.Pair(comp, type);

		if(recipes.containsKey(compKey)) return recipes.get(compKey);

		String[] dictKeys = comp.getDictKeys();

		for(String key : dictKeys) {
			Tuple.Pair dictKey = new Tuple.Pair(key, type);
			if(recipes.containsKey(dictKey)) return recipes.get(dictKey);
		}

		comp.meta = OreDictionary.WILDCARD_VALUE;
		if(recipes.containsKey(compKey)) return recipes.get(compKey);

		return null;
	}

	public static int getAmount(ItemStack stack) {

		if(stack == null || stack.getItem() == null)
			return 0;

		ComparableStack comp = new ComparableStack(stack.getItem(), 1, stack.getItemDamage());
		if(amounts.containsKey(comp)) return amounts.get(comp);

		String[] dictKeys = comp.getDictKeys();

		for(String key : dictKeys) {
			if(amounts.containsKey(key)) return amounts.get(key);
		}

		comp.meta = OreDictionary.WILDCARD_VALUE;
		if(amounts.containsKey(comp)) return amounts.get(comp);

		return 0;
	}

	public static List<CrystallizerRecipe> getRecipes() {
		if(jeiCrystalRecipes != null)
			return jeiCrystalRecipes;
		jeiCrystalRecipes = new ArrayList<CrystallizerRecipe>();

		for(Entry<Tuple.Pair<Object, FluidType>, CrystallizerRecipe> entry : CrystallizerRecipes.recipes.entrySet()) {
			List<ItemStack> ingredients;
			CrystallizerRecipe recipe = entry.getValue();
			Object key = entry.getKey().getKey();
			if (key instanceof String oreKey) {
                ingredients = OreDictionary.getOres(oreKey);
				if (ingredients.isEmpty()) continue;
			} else {
				ItemStack stack = ((ComparableStack) key).toStack();
				ingredients = new ArrayList<>();
				ingredients.add(stack);
			}
			for(ItemStack stack : ingredients) stack.setCount(recipe.itemAmount);
			ItemStack inputFluid = ItemFluidIcon.make(new FluidStack(entry.getKey().getValue(), recipe.acidAmount));
			ItemStack outputItem = recipe.output;

			CrystallizerRecipe jeiRecipe = new CrystallizerRecipe(outputItem, recipe.duration);

			jeiRecipe.jeiItemInputs = ingredients;
			jeiRecipe.jeiFluidInput = inputFluid;

			jeiCrystalRecipes.add(jeiRecipe);
		}

		return jeiCrystalRecipes;
	}

	public static void registerRecipe(Object input, CrystallizerRecipe recipe) {
		registerRecipe(input, recipe, new FluidStack(Fluids.PEROXIDE, 500));
	}

	public static void registerRecipe(Object input, CrystallizerRecipe recipe, FluidStack stack) {
		recipe.acidAmount = stack.fill;
		recipes.put(new Tuple.Pair(input, stack.type), recipe);
		amounts.put(input, recipe.itemAmount);
	}

	public static class CrystallizerRecipe implements IRecipeWrapper {
		public int acidAmount;
		public int itemAmount = 1;
		public int duration;
		public ItemStack output;
		private List<ItemStack> jeiItemInputs;
		private ItemStack jeiFluidInput;

		public CrystallizerRecipe(Block output, int duration) { this(new ItemStack(output), duration); }
		public CrystallizerRecipe(Item output, int duration) { this(new ItemStack(output), duration); }

		public CrystallizerRecipe setReq(int amount) {
			this.itemAmount = amount;
			return this;
		}

		public CrystallizerRecipe(ItemStack output, int duration) {
			this.output = output;
			this.duration = duration;
			this.acidAmount = 500;
		}

		@Override
		public void getIngredients(IIngredients ingredients) {
			List<List<ItemStack>> allInputs = new ArrayList<>();
			allInputs.add(this.jeiItemInputs);
			allInputs.add(Collections.singletonList(this.jeiFluidInput));
			ingredients.setInputLists(VanillaTypes.ITEM, allInputs);
			ingredients.setOutput(VanillaTypes.ITEM, this.output);
		}
	}

	@Override
	public String getFileName() {
		return "hbmCrystallizer.json";
	}

	@Override
	public Object getRecipeObject() {
		return recipes;
	}

	@Override
	public void readRecipe(JsonElement recipe) {
		JsonObject obj = (JsonObject) recipe;

		ItemStack output = this.readItemStack(obj.get("output").getAsJsonArray());
		RecipesCommon.AStack input = this.readAStack(obj.get("input").getAsJsonArray());
		FluidStack fluid = this.readFluidStack(obj.get("fluid").getAsJsonArray());
		int duration = obj.get("duration").getAsInt();

		CrystallizerRecipe cRecipe = new CrystallizerRecipe(output, duration).setReq(input.stacksize);
		input.stacksize = 1;
		cRecipe.acidAmount = fluid.fill;
		if(input instanceof ComparableStack) {
			recipes.put(new Tuple.Pair(((ComparableStack) input), fluid.type), cRecipe);
		} else if(input instanceof RecipesCommon.OreDictStack) {
			recipes.put(new Tuple.Pair(((RecipesCommon.OreDictStack) input).name, fluid.type), cRecipe);
		}
	}

	@Override
	public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
		Entry<Tuple.Pair, CrystallizerRecipe> rec = (Entry<Tuple.Pair, CrystallizerRecipe>) recipe;
		CrystallizerRecipe cRecipe = rec.getValue();
		Tuple.Pair<Object, FluidType> pair = rec.getKey();
		RecipesCommon.AStack input = pair.getKey() instanceof String ? new RecipesCommon.OreDictStack((String )pair.getKey()) : ((ComparableStack) pair.getKey()).copy();
		input.stacksize = cRecipe.itemAmount;
		FluidStack fluid = new FluidStack(pair.getValue(), cRecipe.acidAmount);

		writer.name("duration").value(cRecipe.duration);
		writer.name("fluid");
		this.writeFluidStack(fluid, writer);
		writer.name("input");
		this.writeAStack(input, writer);
		writer.name("output");
		this.writeItemStack(cRecipe.output, writer);
	}

	@Override
	public void deleteRecipes() {
		recipes.clear();
		amounts.clear();
	}

	@Override
	public String getComment() {
		return "The acidizer also supports stack size requirements for input items, eg. the cadmium recipe requires 10 willow leaves.";
	}
}