package com.hbm.items;

public class ItemEnums {

  public enum EnumCokeType {
    COAL,
    LIGNITE,
    PETROLEUM
  }

  public enum EnumTarType {
    CRUDE,
    CRACK,
    COAL,
    WOOD,
    WAX,
    PARAFFIN
  }

  public enum EnumBriquetteType {
    COAL,
    LIGNITE,
    WOOD
  }

  public enum SoyuzSkinType {
    NORMAL,
    LUNAR,
    POST_WAR
  }

  public enum EnumLegendaryType {
    TIER1,
    TIER2,
    TIER3
  }

  public enum EnumAshType {
    WOOD,
    COAL,
    MISC,
    FLY,
    SOOT,
    FULLERENE
  }

  public enum EnumPlantType {
    TOBACCO,
    ROPE,
    MUSTARDWILLOW
  }

  public enum EnumChunkType {
    RARE,
    MALACHITE,
    CRYOLITE
  }

  public enum EnumAchievementType {
    GOFISH,
    ACID,
    BALLS,
    DIGAMMASEE,
    DIGAMMAFEEL,
    DIGAMMAKNOW,
    DIGAMMAKAUAIMOHO,
    DIGAMMAUPONTOP,
    DIGAMMAFOROURRIGHT,
    QUESTIONMARK
  }

  public enum EnumFuelAdditive {
    ANTIKNOCK,
    DEICER
  }

  public enum EnumPages {
    PAGE1,
    PAGE2,
    PAGE3,
    PAGE4,
    PAGE5,
    PAGE6,
    PAGE7,
    PAGE8
  }

  public enum EnumSecretType {
    CANISTER,
    CONTROLLER,
    SELENIUM_STEEL
  }

  public enum EnumCasingType {
    SMALL, LARGE, SMALL_STEEL, LARGE_STEEL, SHOTSHELL, BUCKSHOT, BUCKSHOT_ADVANCED
  }

  public enum EnumCircuitType {
    VACUUM_TUBE,
    CAPACITOR,
    CAPACITOR_TANTALIUM,
    PCB,
    SILICON,
    CHIP,
    CHIP_BISMOID,
    ANALOG,
    BASIC,
    ADVANCED,
    CAPACITOR_BOARD,
    BISMOID,
    CONTROLLER_CHASSIS,
    CONTROLLER,
    CONTROLLER_ADVANCED,
    QUANTUM,
    CHIP_QUANTUM,
    CONTROLLER_QUANTUM,
    ATOMIC_CLOCK,
  }

  public enum EnumDepletedRTGMaterial {
    BISMUTH,
    MERCURY,
    NEPTUNIUM,
    LEAD,
    ZIRCONIUM,
    NICKEL;
  }

  public enum EnumPartType {
    PISTON_PNEUMATIC("piston_pneumatic"),
    PISTON_HYDRAULIC("piston_hydraulic"),
    PISTON_ELECTRIC("piston_electric"),
    LDE("low_density_element"),
    HDE("heavy_duty_element"),
    GLASS_POLARIZED("glass_polarized");

    final String texName;

    EnumPartType(String texName) {
      this.texName = texName;
    }
  }

  public static enum ScrapType {
    //GENERAL BOARD
    BOARD_BLANK,
    BOARD_TRANSISTOR,
    BOARD_CONVERTER,

    //CHIPSET
    BRIDGE_NORTH,
    BRIDGE_SOUTH,
    BRIDGE_IO,
    BRIDGE_BUS,
    BRIDGE_CHIPSET,
    BRIDGE_CMOS,
    BRIDGE_BIOS,

    //CPU
    CPU_REGISTER,
    CPU_CLOCK,
    CPU_LOGIC,
    CPU_CACHE,
    CPU_EXT,
    CPU_SOCKET,

    //RAM
    MEM_SOCKET,
    MEM_16K_A,
    MEM_16K_B,
    MEM_16K_C,
    MEM_16K_D,

    //EXTENSION CARD
    CARD_BOARD,
    CARD_PROCESSOR
  }

  public static enum CircuitComponentType {
    CHIPSET,
    CPU,
    RAM,
    CARD
  }
}
