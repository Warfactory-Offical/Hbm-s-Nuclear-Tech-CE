package com.hbm.blocks;

import com.hbm.items.ModItems;
import com.hbm.items.ModItems.Foods;
import com.hbm.items.ModItems.Materials.Ingots;
import com.hbm.items.ModItems.Materials.Powders;
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

    public enum DecoCRTEnum {
        CLEAN,
        BROKEN,
        BLINKING,
        BSOD
    }

    public enum DecoToasterEnum {
        IRON,
        STEEL,
        WOOD
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
		SULFUR (new ItemStack(Powders.sulfur)),
		FLUORITE(new ItemStack(Ingots.fluorite)),
		ASBESTOS(new ItemStack(Ingots.ingot_asbestos)),
		GEM(new ItemStack(ModItems.gem_volcanic)),
		MOLYSITE(new ItemStack(Powders.powder_molysite));

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
		NUKA (new ItemStack(Foods.cap_nuka)),
		QUANTUM (new ItemStack(Foods.cap_quantum)),
		RAD (new ItemStack(Foods.cap_rad)),
		SPARKLE (new ItemStack(Foods.cap_sparkle)),
		KORL (new ItemStack(Foods.cap_korl)),
		FRITZ (new ItemStack(Foods.cap_fritz)),
		SUNSET (new ItemStack(Foods.cap_sunset)),
		STAR (new ItemStack(Foods.cap_star));

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


