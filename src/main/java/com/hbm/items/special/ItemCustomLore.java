package com.hbm.items.special;

import com.hbm.items.ItemBakedBase;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.util.I18nUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ItemCustomLore extends ItemBakedBase {

	EnumRarity rarity;
	public ItemCustomLore(String s) {
		super(s);
		this.setCreativeTab(MainRegistry.controlTab);
	}

	public ItemCustomLore(String s, String texture) {
		super(s, texture);
		this.setCreativeTab(MainRegistry.controlTab);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flagIn) {
		boolean p11 = !I18nUtil.resolveKey(this.getTranslationKey() + ".desc.P11").equals(this.getTranslationKey() + ".desc.P11");

		if(MainRegistry.polaroidID == 11 && p11) {
			String unlocP11 = this.getTranslationKey() + ".desc.P11";
			String locP11 = I18nUtil.resolveKey(unlocP11);

			if(!unlocP11.equals(locP11)) {
				String[] locsP11 = locP11.split("\\$");

				list.addAll(Arrays.asList(locsP11));
			}
		} else {
			String unloc = this.getTranslationKey() + ".desc";
			String loc = I18nUtil.resolveKey(unloc);

			if(!unloc.equals(loc)) {
				String[] locs = loc.split("\\$");

				list.addAll(Arrays.asList(locs));
			}
		}

		if(this == ModItems.undefined) {

			try {
				if(world.rand.nextInt(10) == 0) {
					list.add(TextFormatting.DARK_RED + "UNDEFINED");
				} else {
					Random rand = new Random(System.currentTimeMillis() / 500);

					if(setSize == 0)
						setSize = Item.REGISTRY.getKeys().size();

					int r = rand.nextInt(setSize);

					Item item = Item.getItemById(r);

					list.add(new ItemStack(item).getDisplayName());
				}
			} catch(Exception ex) {
				list.add(TextFormatting.DARK_RED + "UNDEFINED");
			}
		}
	}
	
	static int setSize = 0;

	@Override
	public EnumRarity getRarity(ItemStack stack) {
		return this.rarity != null ? rarity : super.getRarity(stack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack) {
		if(this == ModItems.rune_isa ||
    			this == ModItems.rune_dagaz ||
    			this == ModItems.rune_hagalaz ||
    			this == ModItems.rune_jera ||
    			this == ModItems.rune_thurisaz ||
    			this == ModItems.egg_balefire_shard ||
    			this == ModItems.egg_balefire ||
    			this == ModItems.coin_maskman || 
    			this == ModItems.coin_radiation || 
    			this == ModItems.coin_worm || 
    			this == ModItems.coin_ufo || 
    			this == ModItems.coin_creeper) 
		{
    		return true;
    	}
		return super.hasEffect(stack);
	}
	
	public ItemCustomLore setRarity(EnumRarity rarity) {
    	this.rarity = rarity;
		return this;
    }

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		if(stack.getItem() != ModItems.undefined || stack.getItemDamage() != 99) return ("" + I18n.format(this.getUnlocalizedNameInefficiently(stack) + ".name")).trim();

		return name.getResult();
	}

	public static String[] names = new String[] {
			"THE DEFAULT", "NEXT ONE", "ANOTHER ONE", "NON-STANDARD NAME", "AMBIGUOUS TITLE", "SHORT"
	};

	public static Random rand = new Random();
	public static ScramblingName name = new ScramblingName(names[0]);

	public static void updateSystem() {
		name.updateTick(names);
	}

	/**
	 * A surprise tool we need for later
	 * @author hbm
	 */
	public static class ScramblingName {

		public String previous;
		public String next;
		public String[] previousFrags;
		public String[] nextFrags;
		public String[] frags;
		public int[] mask;
		public int age = 0;

		public ScramblingName(String init) {
			previous = next = init;
			frags = init.split("");
			mask = new int[frags.length];
			previousFrags = chop(previous, frags.length);
			nextFrags = chop(next, frags.length);
		}

		public String getResult() {
			return String.join("", frags);
		}

		public void updateTick(String[] nextNames) {
			age++;
			try {
				//pick new name
				if(age % 200 == 0) nextName(nextNames);
				//run substitution
				if(age % 5 == 0) scramble();
			} catch(Exception ignored) { }
		}

		public void nextName(String[] nextNames) {
			if(nextNames.length < 2) return;

			this.previous = this.next;

			String initial = next;
			//keep choosing new names until it's different
			while(initial.equals(next)) {
				next = nextNames[rand.nextInt(nextNames.length)];
			}

			//frag setup
			int length = Math.min(previous.length(), next.length());
			this.previousFrags = chop(previous, length);
			this.frags = chop(previous, length);
			this.nextFrags = chop(next, length);
			mask = new int[length];
		}

		public void scramble() {

			//all fragments that haven't been substituted
			List<Integer> indices = new ArrayList<>();

			for(int i = 0; i < mask.length; i++) {
				int m = mask[i];
				//mask 0 means not yet processed
				if(m == 0) indices.add(i);
				//mask 1-5 means obfuscated
				if(m > 0 && m <= 5) mask[i]++;
				//mask >5 means replaced
				if(m > 5) frags[i] = nextFrags[i];
			}

			//if there's at least one index listed, start processing
			if(!indices.isEmpty()) {
				int toSwitch = indices.get(rand.nextInt(indices.size()));
				mask[toSwitch] = 1;
				frags[toSwitch] = TextFormatting.OBFUSCATED + previousFrags[toSwitch] + TextFormatting.RESET;
			}
		}

		public String[] chop(String name, int parts) {
			if(parts == name.length()) return name.split("");

			double index = 0;
			double incrementPerStep = (double) name.length() / (double) parts;
			List<String> slices = new ArrayList<>();

			for(int i = 0; i < parts; i++) {
				int end = (i == parts - 1) ? name.length() : (int) (index + incrementPerStep);
				slices.add(name.substring((int) index, end));
				index += incrementPerStep;
			}

			//System.out.println("Chopped " + name + " into " + parts + " pieces: " + chop);

			return slices.toArray(new String[parts]);
		}
	}

}
