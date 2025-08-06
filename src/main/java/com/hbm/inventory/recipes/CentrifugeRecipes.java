package com.hbm.inventory.recipes;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.items.ModItems;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import static com.hbm.inventory.OreDictManager.*;

public class CentrifugeRecipes {

	private static LinkedHashMap<Object, ItemStack[]> recipes = new LinkedHashMap<Object, ItemStack[]>();
	private static List<CentrifugeRecipe> centrifugeRecipes = null;
	
	public static void register() {
		recipes.put(new ComparableStack(ModItems.waste_natural_uranium), new ItemStack[] {
				new ItemStack(ModItems.nugget_u238, 1),
				new ItemStack(ModItems.nugget_pu_mix, 2),
				new ItemStack(ModItems.nugget_pu239, 1),
				new ItemStack(ModItems.nuclear_waste_tiny, 2) });

		recipes.put(new ComparableStack(ModItems.waste_uranium), new ItemStack[] {
				new ItemStack(ModItems.nugget_pu_mix, 2),
				new ItemStack(ModItems.nugget_plutonium, 1),
				new ItemStack(ModItems.nugget_technetium, 1),
				new ItemStack(ModItems.nuclear_waste_tiny, 2) });

		recipes.put(new ComparableStack(ModItems.waste_thorium), new ItemStack[] {
				new ItemStack(ModItems.nugget_u238, 1),
				new ItemStack(ModItems.nugget_th232, 1),
				new ItemStack(ModItems.nugget_u233, 2),
				new ItemStack(ModItems.nuclear_waste_tiny, 2) });

		recipes.put(new ComparableStack(ModItems.waste_mox), new ItemStack[] {
				new ItemStack(ModItems.nugget_pu_mix, 1),
				new ItemStack(ModItems.nugget_technetium, 1),
				new ItemStack(ModItems.nugget_u238, 1),
				new ItemStack(ModItems.nuclear_waste_tiny, 3) });

		recipes.put(new ComparableStack(ModItems.waste_plutonium), new ItemStack[] {
				new ItemStack(ModItems.nugget_pu_mix, 1),
				new ItemStack(ModItems.nugget_pu_mix, 1),
				new ItemStack(ModItems.nugget_technetium, 1),
				new ItemStack(ModItems.nuclear_waste_tiny, 3) });

		recipes.put(new ComparableStack(ModItems.waste_u233), new ItemStack[] {
				new ItemStack(ModItems.nugget_u235, 1),
				new ItemStack(ModItems.nugget_neptunium, 1),
				new ItemStack(ModItems.nugget_technetium, 1),
				new ItemStack(ModItems.nuclear_waste_tiny, 3) });

		recipes.put(new ComparableStack(ModItems.waste_u235), new ItemStack[] {
				new ItemStack(ModItems.nugget_pu238, 1),
				new ItemStack(ModItems.nugget_neptunium, 1),
				new ItemStack(ModItems.nugget_technetium, 1),
				new ItemStack(ModItems.nuclear_waste_tiny, 3) });

		recipes.put(new ComparableStack(ModItems.waste_schrabidium), new ItemStack[] {
				new ItemStack(ModItems.nugget_beryllium, 2),
				new ItemStack(ModItems.nugget_pu239, 1),
				new ItemStack(ModItems.nuclear_waste_tiny, 1),
				new ItemStack(ModItems.nuclear_waste_tiny, 2) });

		recipes.put(new ComparableStack(ModItems.waste_zfb_mox), new ItemStack[] {
				new ItemStack(ModItems.nugget_zirconium, 3),
				new ItemStack(ModItems.nugget_technetium, 1),
				new ItemStack(ModItems.nugget_pu_mix, 1),
				new ItemStack(ModItems.nuclear_waste_tiny, 1) });

		//TO REMOVE
		recipes.put(new ComparableStack(ModItems.waste_uranium_legacy), new ItemStack[] {
				new ItemStack(ModItems.nugget_pu_mix, 2),
				new ItemStack(ModItems.nugget_plutonium, 1),
				new ItemStack(ModItems.nugget_technetium, 1),
				new ItemStack(ModItems.nuclear_waste_tiny, 2) });
		
		recipes.put(new ComparableStack(ModItems.waste_plutonium_legacy), new ItemStack[] {
				new ItemStack(ModItems.nugget_pu_mix, 1),
				new ItemStack(ModItems.nugget_pu_mix, 1),
				new ItemStack(ModItems.nugget_technetium, 1),
				new ItemStack(ModItems.nuclear_waste_tiny, 3) });
		
		recipes.put(new ComparableStack(ModItems.waste_mox_legacy), new ItemStack[] {
				new ItemStack(ModItems.nugget_pu_mix, 1),
				new ItemStack(ModItems.nugget_technetium, 1),
				new ItemStack(ModItems.nugget_u238, 1),
				new ItemStack(ModItems.nuclear_waste_tiny, 3) });
		
		recipes.put(new ComparableStack(ModItems.waste_schrabidium_legacy), new ItemStack[] {
				new ItemStack(ModItems.nugget_beryllium, 2),
				new ItemStack(ModItems.nugget_lead, 1),
				new ItemStack(ModItems.nugget_solinium, 1),
				new ItemStack(ModItems.nuclear_waste_tiny, 2) });
		
		recipes.put(new ComparableStack(ModItems.waste_thorium_legacy), new ItemStack[] {
				new ItemStack(ModItems.nugget_u238, 1),
				new ItemStack(ModItems.nugget_th232, 1),
				new ItemStack(ModItems.nugget_u233, 2),
				new ItemStack(ModItems.nuclear_waste_tiny, 2) });
		//TO REMOVE
		
		recipes.put(new ComparableStack(ModItems.powder_cloud), new ItemStack[] {
				new ItemStack(ModItems.powder_copper, 1),
				new ItemStack(ModItems.sulfur, 1),
				new ItemStack(ModItems.dust, 1),
				new ItemStack(ModItems.dust, 1) });

		recipes.put(COAL.ore(), new ItemStack[] {
				new ItemStack(ModItems.powder_coal, 2),
				new ItemStack(ModItems.powder_coal, 2),
				new ItemStack(ModItems.powder_coal, 2),
				new ItemStack(Blocks.GRAVEL, 1) });
		
		recipes.put(LIGNITE.ore(), new ItemStack[] {
				new ItemStack(ModItems.powder_lignite, 2),
				new ItemStack(ModItems.powder_lignite, 2),
				new ItemStack(ModItems.powder_lignite, 2),
				new ItemStack(Blocks.GRAVEL, 1) });
		
		recipes.put(IRON.ore(), new ItemStack[] {
				new ItemStack(ModItems.powder_iron, 1),
				new ItemStack(ModItems.powder_iron, 1),
				new ItemStack(ModItems.powder_iron, 1),
				new ItemStack(Blocks.GRAVEL, 1) });
		
		recipes.put(GOLD.ore(), new ItemStack[] {
				new ItemStack(ModItems.powder_gold, 1),
				new ItemStack(ModItems.powder_gold, 1),
				new ItemStack(ModItems.powder_gold, 1),
				new ItemStack(Blocks.GRAVEL, 1) });
		
		recipes.put(DIAMOND.ore(), new ItemStack[] {
				new ItemStack(ModItems.powder_diamond, 1),
				new ItemStack(ModItems.powder_diamond, 1),
				new ItemStack(ModItems.powder_diamond, 1),
				new ItemStack(Blocks.GRAVEL, 1) });
		
		recipes.put(EMERALD.ore(), new ItemStack[] {
				new ItemStack(ModItems.powder_emerald, 1),
				new ItemStack(ModItems.powder_emerald, 1),
				new ItemStack(ModItems.powder_emerald, 1),
				new ItemStack(Blocks.GRAVEL, 1) });
		
		recipes.put(TI.ore(), new ItemStack[] {
				new ItemStack(ModItems.powder_titanium, 1),
				new ItemStack(ModItems.powder_titanium, 1),
				new ItemStack(ModItems.powder_iron, 1),
				new ItemStack(Blocks.GRAVEL, 1) });
		
		recipes.put("oreQuartz", new ItemStack[] {
				new ItemStack(ModItems.powder_quartz, 1),
				new ItemStack(ModItems.powder_quartz, 1),
				new ItemStack(ModItems.powder_lithium_tiny, 1),
				new ItemStack(Blocks.NETHERRACK, 1) });
		
		recipes.put(W.ore(), new ItemStack[] {
				new ItemStack(ModItems.powder_tungsten, 1),
				new ItemStack(ModItems.powder_tungsten, 1),
				new ItemStack(ModItems.powder_iron, 1),
				new ItemStack(Blocks.GRAVEL, 1) });
		
		recipes.put(CU.ore(), new ItemStack[] {
				new ItemStack(ModItems.powder_copper, 1),
				new ItemStack(ModItems.powder_copper, 1),
				new ItemStack(ModItems.powder_gold, 1),
				new ItemStack(Blocks.GRAVEL, 1) });
		
		recipes.put(AL.ore(), new ItemStack[] {
				new ItemStack(ModItems.powder_aluminium, 1),
				new ItemStack(ModItems.powder_aluminium, 1),
				new ItemStack(ModItems.powder_iron, 1),
				new ItemStack(Blocks.GRAVEL, 1) });
		
		recipes.put(PB.ore(), new ItemStack[] {
				new ItemStack(ModItems.powder_lead, 1),
				new ItemStack(ModItems.powder_lead, 1),
				new ItemStack(ModItems.powder_gold, 1),
				new ItemStack(Blocks.GRAVEL, 1) });

		recipes.put(ASBESTOS.ore(), new ItemStack[] {
				new ItemStack(ModItems.powder_asbestos, 1),
				new ItemStack(ModItems.powder_asbestos, 1),
				new ItemStack(ModItems.powder_boron_tiny, 1),
				new ItemStack(Blocks.GRAVEL, 1) });
		
		recipes.put(SA326.ore(), new ItemStack[] {
				new ItemStack(ModItems.powder_schrabidium, 1),
				new ItemStack(ModItems.powder_schrabidium, 1),
				new ItemStack(ModItems.nugget_solinium, 1),
				new ItemStack(Blocks.GRAVEL, 1) });
		
		recipes.put(RAREEARTH.ore(), new ItemStack[] {
				new ItemStack(ModItems.powder_desh_mix, 1),
				new ItemStack(ModItems.nugget_zirconium, 1),
				new ItemStack(ModItems.nugget_zirconium, 1),
				new ItemStack(Blocks.GRAVEL, 1) });

		recipes.put(CINNABAR.ore(), new ItemStack[] {
				new ItemStack(ModItems.cinnabar, 2),
				new ItemStack(ModItems.cinnabar, 2),
				new ItemStack(Items.REDSTONE, 1),
				new ItemStack(Blocks.GRAVEL, 1) });
		
		recipes.put(PU.ore(), new ItemStack[] {
				new ItemStack(ModItems.powder_plutonium, 1),
				new ItemStack(ModItems.powder_plutonium, 1),
				new ItemStack(ModItems.nugget_polonium, 3),
				new ItemStack(Blocks.GRAVEL, 1) });
		
		recipes.put(U.ore(), new ItemStack[] {
				new ItemStack(ModItems.powder_uranium, 1),
				new ItemStack(ModItems.powder_uranium, 1),
				new ItemStack(ModItems.nugget_ra226, 1),
				new ItemStack(Blocks.GRAVEL, 1) });
		
		recipes.put(TH232.ore(), new ItemStack[] {
				new ItemStack(ModItems.powder_thorium, 1),
				new ItemStack(ModItems.powder_thorium, 1),
				new ItemStack(ModItems.powder_uranium, 1),
				new ItemStack(Blocks.GRAVEL, 1) });
		
		recipes.put(BE.ore(), new ItemStack[] {
				new ItemStack(ModItems.powder_beryllium, 1),
				new ItemStack(ModItems.powder_beryllium, 1),
				new ItemStack(ModItems.powder_emerald, 1),
				new ItemStack(Blocks.GRAVEL, 1) });
		
		recipes.put(REDSTONE.ore(), new ItemStack[] {
				new ItemStack(Items.REDSTONE, 3),
				new ItemStack(Items.REDSTONE, 3),
				new ItemStack(ModItems.nugget_mercury, 1),
				new ItemStack(Blocks.GRAVEL, 1) });
		
		recipes.put(new ComparableStack(ModBlocks.ore_tikite), new ItemStack[] {
				new ItemStack(ModItems.powder_plutonium, 2),
				new ItemStack(ModItems.powder_cobalt, 2),
				new ItemStack(ModItems.powder_nitan_mix, 1),
				new ItemStack(Blocks.END_STONE, 1) });
		
		recipes.put(LAPIS.ore(), new ItemStack[] {
				new ItemStack(ModItems.powder_lapis, 3),
				new ItemStack(ModItems.powder_lapis, 3),
				new ItemStack(ModItems.powder_cobalt_tiny, 1),
				new ItemStack(Blocks.GRAVEL, 1) });
		
		recipes.put(STAR.ore(), new ItemStack[] {
				new ItemStack(ModItems.powder_dura_steel, 3),
				new ItemStack(ModItems.powder_astatine, 1),
				new ItemStack(ModItems.powder_cobalt, 2),
				new ItemStack(Blocks.GRAVEL, 1) });

		recipes.put(new ComparableStack(ModItems.powder_tektite), new ItemStack[] {
				new ItemStack(ModItems.powder_paleogenite_tiny, 2),
				new ItemStack(ModItems.powder_meteorite_tiny, 1),
				new ItemStack(ModItems.powder_meteorite_tiny, 1),
				new ItemStack(ModItems.dust, 6) });
		
		recipes.put(new ComparableStack(ModBlocks.block_euphemium_cluster), new ItemStack[] {
				new ItemStack(ModItems.nugget_euphemium, 7),
				new ItemStack(ModItems.powder_schrabidium, 4),
				new ItemStack(ModItems.ingot_starmetal, 2),
				new ItemStack(ModItems.nugget_solinium, 2) });
		
		recipes.put(P_RED.ore(), new ItemStack[] {
				new ItemStack(Items.BLAZE_POWDER, 2),
				new ItemStack(ModItems.powder_fire, 2),
				new ItemStack(ModItems.ingot_phosphorus),
				new ItemStack(Blocks.NETHERRACK) });
		
		recipes.put(new ComparableStack(Items.BLAZE_ROD), new ItemStack[] {new ItemStack(Items.BLAZE_POWDER, 1), new ItemStack(Items.BLAZE_POWDER, 1), new ItemStack(ModItems.powder_fire, 1), new ItemStack(ModItems.powder_fire, 1) });
		
		recipes.put(SRN.ingot(), new ItemStack[] { new ItemStack(ModItems.nugget_schrabidium, 2), new ItemStack(ModItems.nugget_schrabidium, 1), new ItemStack(ModItems.nugget_uranium, 3), new ItemStack(ModItems.nugget_plutonium, 2) });
		
		recipes.put(COAL.crystal(), new ItemStack[] { new ItemStack(ModItems.powder_coal, 3), new ItemStack(ModItems.powder_coal, 3), new ItemStack(ModItems.powder_coal, 3), new ItemStack(ModItems.powder_lithium_tiny, 1) });
		recipes.put(IRON.crystal(), new ItemStack[] { new ItemStack(ModItems.powder_iron, 2), new ItemStack(ModItems.powder_iron, 2), new ItemStack(ModItems.powder_titanium, 1), new ItemStack(ModItems.powder_lithium_tiny, 1) });
		recipes.put(GOLD.crystal(), new ItemStack[] { new ItemStack(ModItems.powder_gold, 2), new ItemStack(ModItems.powder_gold, 2), new ItemStack(ModItems.nugget_mercury, 1), new ItemStack(ModItems.powder_lithium_tiny, 1) });
		recipes.put(REDSTONE.crystal(), new ItemStack[] { new ItemStack(Items.REDSTONE, 3), new ItemStack(Items.REDSTONE, 3), new ItemStack(Items.REDSTONE, 3), new ItemStack(ModItems.nugget_mercury, 3) });
		recipes.put(LAPIS.crystal(), new ItemStack[] { new ItemStack(ModItems.powder_lapis, 8), new ItemStack(ModItems.powder_lapis, 8), new ItemStack(ModItems.powder_cobalt, 1), new ItemStack(Blocks.GRAVEL, 1) });
		recipes.put(DIAMOND.crystal(), new ItemStack[] { new ItemStack(ModItems.powder_diamond, 1), new ItemStack(ModItems.powder_diamond, 1), new ItemStack(ModItems.powder_diamond, 1), new ItemStack(ModItems.powder_diamond, 1) });
		recipes.put(U.crystal(), new ItemStack[] { new ItemStack(ModItems.powder_uranium, 2), new ItemStack(ModItems.powder_uranium, 2), new ItemStack(ModItems.nugget_ra226, 2), new ItemStack(ModItems.powder_lithium_tiny, 1) });
		recipes.put(TH232.crystal(), new ItemStack[] { new ItemStack(ModItems.powder_thorium, 2), new ItemStack(ModItems.powder_thorium, 2), new ItemStack(ModItems.powder_uranium, 1), new ItemStack(ModItems.nugget_ra226, 1) });
		recipes.put(PU.crystal(), new ItemStack[] { new ItemStack(ModItems.powder_plutonium, 2), new ItemStack(ModItems.powder_plutonium, 2), new ItemStack(ModItems.powder_polonium, 1), new ItemStack(ModItems.powder_lithium_tiny, 1) });
		recipes.put(TI.crystal(), new ItemStack[] { new ItemStack(ModItems.powder_titanium, 2), new ItemStack(ModItems.powder_titanium, 2), new ItemStack(ModItems.powder_iron, 1), new ItemStack(ModItems.powder_lithium_tiny, 1) });
		recipes.put(S.crystal(), new ItemStack[] { new ItemStack(ModItems.sulfur, 4), new ItemStack(ModItems.sulfur, 4), new ItemStack(ModItems.powder_iron, 1), new ItemStack(ModItems.nugget_mercury, 1) });
		recipes.put(KNO.crystal(), new ItemStack[] { new ItemStack(ModItems.niter, 3), new ItemStack(ModItems.niter, 3), new ItemStack(ModItems.niter, 3), new ItemStack(ModItems.powder_lithium_tiny, 1) });
		recipes.put(CU.crystal(), new ItemStack[] { new ItemStack(ModItems.powder_copper, 2), new ItemStack(ModItems.powder_copper, 2), new ItemStack(ModItems.sulfur, 1), new ItemStack(ModItems.powder_lithium_tiny, 1) });
		recipes.put(W.crystal(), new ItemStack[] { new ItemStack(ModItems.powder_tungsten, 2), new ItemStack(ModItems.powder_tungsten, 2), new ItemStack(ModItems.powder_iron, 1), new ItemStack(ModItems.powder_lithium_tiny, 1) });
		recipes.put(AL.crystal(), new ItemStack[] { new ItemStack(ModItems.powder_aluminium, 2), new ItemStack(ModItems.powder_aluminium, 2), new ItemStack(ModItems.powder_iron, 1), new ItemStack(ModItems.powder_lithium_tiny, 1) });
		recipes.put(F.crystal(), new ItemStack[] { new ItemStack(ModItems.fluorite, 3), new ItemStack(ModItems.fluorite, 3), new ItemStack(ModItems.fluorite, 3), new ItemStack(ModItems.powder_lithium_tiny, 1) });
		recipes.put(BE.crystal(), new ItemStack[] { new ItemStack(ModItems.powder_beryllium, 2), new ItemStack(ModItems.powder_beryllium, 2), new ItemStack(ModItems.powder_quartz, 1), new ItemStack(ModItems.powder_lithium_tiny, 1) });
		recipes.put(PB.crystal(), new ItemStack[] { new ItemStack(ModItems.powder_lead, 2), new ItemStack(ModItems.powder_lead, 2), new ItemStack(ModItems.powder_gold, 1), new ItemStack(ModItems.powder_lithium_tiny, 1) });
		recipes.put(SRN.crystal(), new ItemStack[] { new ItemStack(ModItems.nugget_schrabidium, 2), new ItemStack(ModItems.nugget_schrabidium, 2), new ItemStack(ModItems.nugget_uranium, 2), new ItemStack(ModItems.nugget_plutonium, 2) });
		recipes.put(SA326.crystal(), new ItemStack[] { new ItemStack(ModItems.powder_schrabidium, 2), new ItemStack(ModItems.powder_schrabidium, 2), new ItemStack(ModItems.powder_plutonium, 1), new ItemStack(ModItems.powder_lithium_tiny, 1) });
		recipes.put(RAREEARTH.crystal(), new ItemStack[] { new ItemStack(ModItems.powder_desh_mix, 1), new ItemStack(ModItems.powder_desh_mix, 1), new ItemStack(ModItems.nugget_zirconium, 2), new ItemStack(ModItems.nugget_zirconium, 2) });
		recipes.put(CINNABAR.crystal(), new ItemStack[] { new ItemStack(ModItems.cinnabar, 3), new ItemStack(ModItems.cinnabar, 3), new ItemStack(Items.REDSTONE, 2), new ItemStack(ModItems.powder_lithium_tiny, 1) });
		recipes.put(P_RED.crystal(), new ItemStack[] { new ItemStack(ModItems.powder_fire, 3), new ItemStack(ModItems.powder_fire, 3), new ItemStack(ModItems.ingot_phosphorus, 2), new ItemStack(Items.BLAZE_POWDER, 2) });
		recipes.put(new ComparableStack(ModItems.crystal_trixite), new ItemStack[] { new ItemStack(ModItems.powder_plutonium, 3), new ItemStack(ModItems.powder_cobalt, 3), new ItemStack(ModItems.powder_spark_mix, 1), new ItemStack(ModItems.powder_nitan_mix, 2) });
		recipes.put(LI.crystal(), new ItemStack[] { new ItemStack(ModItems.powder_lithium, 2), new ItemStack(ModItems.powder_lithium, 2), new ItemStack(ModItems.powder_quartz, 1), new ItemStack(ModItems.fluorite, 1) });
		recipes.put(STAR.crystal(), new ItemStack[] { new ItemStack(ModItems.powder_dura_steel, 3), new ItemStack(ModItems.powder_cobalt, 3), new ItemStack(ModItems.powder_astatine, 2), new ItemStack(ModItems.nugget_mercury, 5) });
		recipes.put(CO.crystal(), new ItemStack[] { new ItemStack(ModItems.powder_cobalt, 2), new ItemStack(ModItems.powder_iron, 3), new ItemStack(ModItems.powder_copper, 3), new ItemStack(ModItems.powder_lithium_tiny, 1) });
		recipes.put(ASBESTOS.crystal(), new ItemStack[] { new ItemStack(ModItems.powder_asbestos, 2), new ItemStack(ModItems.powder_asbestos, 2), new ItemStack(ModItems.powder_boron_tiny, 1), new ItemStack(Blocks.GRAVEL, 1) });
	}

	public static ItemStack getNugget(String oreName){
		if(oreName.equals("oreLead") || oreName.equals("oreCopper")) return new ItemStack(ModItems.nugget_cadmium, 1);
		if(oreName.equals("oreGold") || oreName.equals("oreTungsten")) return new ItemStack(ModItems.nugget_bismuth, 1);
		if(oreName.equals("oreUranium")) return new ItemStack(ModItems.nugget_ra226, 1);
		if(oreName.equals("oreThorium")) return new ItemStack(ModItems.nugget_technetium, 1);
		if(oreName.equals("oreStarmetal")) return new ItemStack(ModItems.powder_meteorite_tiny, 1);
		if(oreName.equals("oreRedstone")) return new ItemStack(ModItems.nugget_mercury, 1);
		if(oreName.equals("oreRedPhosphorus")) return new ItemStack(ModItems.nugget_arsenic, 1);
		if(oreName.equals("oreNeodymium")) return new ItemStack(ModItems.nugget_tantalium, 1);
		return new ItemStack(Blocks.GRAVEL, 1);
	}

	public static void addRecipe(ItemStack in, ItemStack[] outputs){
		recipes.put(new ComparableStack(in), outputs);
	}

	public static void removeRecipe(ItemStack in){
		recipes.remove(new ComparableStack(in));
	}
	
	public static ItemStack[] getOutput(ItemStack stack) {
		
		if(stack == null || stack.getItem() == null)
			return null;
	
		ComparableStack comp = new ComparableStack(stack.getItem(), 1, stack.getItemDamage());
		if(recipes.containsKey(comp))
			return RecipesCommon.copyStackArray(recipes.get(comp));
		
		String[] dictKeys = comp.getDictKeys();
		
		for(String key : dictKeys) {

			if(recipes.containsKey(key))
				return RecipesCommon.copyStackArray(recipes.get(key));
		}
		
		return null;
	}

	public static List<CentrifugeRecipe> getCentrifugeRecipes() {
		if(centrifugeRecipes != null)
			return centrifugeRecipes;
		centrifugeRecipes = new ArrayList<CentrifugeRecipe>();
		
		for(Entry<Object, ItemStack[]> entry : CentrifugeRecipes.recipes.entrySet()) {
			
			if(entry.getKey() instanceof String) {
				List<ItemStack> ingredients = OreDictionary.getOres((String)entry.getKey());
				centrifugeRecipes.add(new CentrifugeRecipe(ingredients, Arrays.asList(entry.getValue())));
			} else {
				centrifugeRecipes.add(new CentrifugeRecipe(((ComparableStack)entry.getKey()).toStack(), Arrays.asList(entry.getValue())));
			}
		}
		
		return centrifugeRecipes;
	}
	
	public static class CentrifugeRecipe implements IRecipeWrapper {
		
		private final ItemStack input;
		private final List<ItemStack> outputs;
		public final List<ItemStack> inputs;
		
		public CentrifugeRecipe(ItemStack input, List<ItemStack> outputs) {
			this.input = input;
			this.inputs = null;
			this.outputs = outputs; 
		}
		
		public CentrifugeRecipe(List<ItemStack> inputs, List<ItemStack> outputs) {
			this.inputs = inputs;
			this.input = ItemStack.EMPTY;
			this.outputs = outputs; 
		}
		
		@Override
		public void getIngredients(IIngredients ingredients) {
			if(inputs != null){
				ingredients.setInputLists(VanillaTypes.ITEM, Arrays.asList(inputs));
			} else {
				ingredients.setInput(VanillaTypes.ITEM, input);
			}
			ingredients.setOutputs(VanillaTypes.ITEM, outputs);
		}
		
	}
}