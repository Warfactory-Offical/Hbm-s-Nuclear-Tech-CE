package com.hbm.blocks;

import com.hbm.items.ModItems;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

import static com.hbm.blocks.OreEnumUtil.OreEnum;

public class BlockEnums {

	public static enum EnumStoneType {
		SULFUR,
		ASBESTOS,
		HEMATITE,
		MALACHITE,
		LIMESTONE,
		BAUXITE
	}

	public static enum EnumMeteorType {
		IRON,
		COPPER,
		ALUMINIUM,
		RAREEARTH,
		COBALT
	}

	public static enum EnumStalagmiteType {
		SULFUR,
		ASBESTOS
	}
	
	/** DECO / STRUCTURE ENUMS */
	//i apologize in advance
	
	public static enum TileType {
		LARGE,
		SMALL
	}

	public static enum LightstoneType {
		UNREFINED,
		TILE,
		BRICKS,
		BRICKS_CHISELED,
		CHISELED
	}
	
	public static enum DecoComputerEnum {
		IBM_300PL
	}
	
	public static enum DecoCabinetEnum {
		GREEN,
		STEEL
	}

	public static enum OreType {
		EMERALD ("emerald",OreEnum.EMERALD),
		DIAMOND ("diamond", OreEnum.DIAMOND),
		RADGEM ("radgem",OreEnum.RAD_GEM),
		//URANIUM_SCORCEHD ("uranium_scorched", null),
		URANIUM ("uranium", null),
		SCHRABIDIUM ("schrabidium", null);

		public final String overlayTexture;
		public final OreEnum oreEnum;

		public String getName(){
			return overlayTexture;
		}

		OreType(String overlayTexture, @Nullable OreEnum oreEnum) {
			this.overlayTexture = overlayTexture;
			this.oreEnum = oreEnum;

		}
	}


	public static enum EnumBasaltOreType {
		SULFUR (new ItemStack(ModItems.sulfur)),
		FLUORITE(new ItemStack(ModItems.fluorite)),
		ASBESTOS(new ItemStack(ModItems.ingot_asbestos)),
		GEM(new ItemStack(ModItems.gem_volcanic)),
		MOLYSITE(new ItemStack(ModItems.powder_molysite));

		public final ItemStack drop;

		public ItemStack getDrop(){
			return drop;
		}
		public int getDropCount(int rand){
			return rand + 1;
		}

        EnumBasaltOreType(ItemStack drop) {
            this.drop = drop;
        }
    }

	public static enum EnumBlockCapType {
		NUKA (new ItemStack(ModItems.cap_nuka)),
		QUANTUM (new ItemStack(ModItems.cap_quantum)),
		RAD (new ItemStack(ModItems.cap_rad)),
		SPARKLE (new ItemStack(ModItems.cap_sparkle)),
		KORL (new ItemStack(ModItems.cap_korl)),
		FRITZ (new ItemStack(ModItems.cap_fritz)),
		SUNSET (new ItemStack(ModItems.cap_sunset)),
		STAR (new ItemStack(ModItems.cap_star));

		public final ItemStack drop;

		public ItemStack getDrop() {
			return drop;
		}

		public int getDropCount(int rand){
			return 128;
		}

		EnumBlockCapType(ItemStack drop) {
			this.drop = drop;
		}
	}
}