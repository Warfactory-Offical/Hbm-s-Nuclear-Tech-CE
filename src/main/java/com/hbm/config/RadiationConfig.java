package com.hbm.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class RadiationConfig {

	public static int rain = 0;
	public static int cont = 0;
	public static int fogRad = 100;
	public static int fogCh = 50;
	public static int worldRad = 10;
	public static int worldRadThreshold = 20;
	public static boolean worldRadEffects = true;
	public static boolean enableContamination = true;
	public static boolean enableContaminationOnGround = false;
	public static int blocksFallCh = 100;
	
	//Drillgon200: Not sure why I put these here, but oh well.
	public static int railgunDamage = 1000;
	public static int railgunBuffer = 500000000;
	public static int railgunUse = 250000000;
	public static int fireDuration = 4 * 20;
	public static boolean neutronActivation = false;
	public static int neutronActivationThreshold = 15;

	public static int geigerX = 16;
	public static int geigerY = 2;
	public static int digammaX = 16;
	public static int digammaY = 18;

	public static int hazardRate = 5;
	public static boolean disableAsbestos = false;
	public static boolean disableBlinding = false;
	public static boolean disableCoal = false;
	public static boolean disableExplosive = false;
	public static boolean disableHydro = false;
	public static boolean disableHot = false;
	public static boolean disableCold = false;
	public static boolean disableToxic = false;

	public static boolean enablePollution = true;
	public static boolean enableLeadFromBlocks = true;
	public static boolean enableLeadPoisoning = true;
	public static boolean enableSootFog = true;
	public static boolean enablePoison = true;
	public static double buffMobThreshold = 15D;
	public static double sootFogThreshold = 35D;
	public static double sootFogDivisor = 120D;
	public static double smokeStackSootMult = 0.8;
	
	public static void loadFromConfig(Configuration config) {
		final String CATEGORY_NUKE = "06_explosions";
		final String CATEGORY_RAD = "07_radiation";
		final String CATEGORY_POL = "16_pollution";
		final String CATEGORY_HAZ = "CE-03_hazard";
		// afterrain duration
		Property radRain = config.get(CATEGORY_NUKE, "6.12_falloutRainDuration", 2000);
		radRain.setComment("Duration of the thunderstorm after fallout in ticks (only large explosions)");
		rain = radRain.getInt();
		// afterrain radiation
		Property rainCont = config.get(CATEGORY_NUKE, "6.13_falloutRainRadiation", 1000);
		rainCont.setComment("Radiation in 100th RADs created by fallout rain");
		cont = rainCont.getInt();
		// fog threshold
		Property fogThresh = config.get(CATEGORY_NUKE, "6.14_fogThreshold", 100);
		fogThresh.setComment("Radiation in RADs required for fog to spawn");
		fogRad = fogThresh.getInt();
		// fog chance
		Property fogChance = config.get(CATEGORY_NUKE, "6.14_fogChance", 50);
		fogChance.setComment("1:n chance of fog spawning every second - default 1/50");
		fogCh = fogChance.getInt();
		worldRad = CommonConfig.createConfigInt(config, CATEGORY_NUKE, "6.15_worldRadCount", "How many block operations radiation can perform per tick", 10);
		worldRadThreshold = CommonConfig.createConfigInt(config, CATEGORY_NUKE, "6.16_worldRadThreshold", "The least amount of RADs required for block modification to happen", 40);
		worldRadEffects = CommonConfig.createConfigBool(config, CATEGORY_NUKE, "6.17_worldRadEffects", "Whether high radiation levels should perform changes in the world", true);
		enableContamination = CommonConfig.createConfigBool(config, CATEGORY_NUKE, "6.18_enableContamination", "Toggles player contamination (and negative effects from radiation poisoning)", true);
		enableContaminationOnGround = CommonConfig.createConfigBool(config, CATEGORY_NUKE, "6.18.1_enableContaminationOnGround", "Toggles contamination for items being on-ground", false);
		blocksFallCh = CommonConfig.createConfigInt(config, CATEGORY_NUKE, "6.19_blocksFallingChance", "The chance (in percentage form) that a block with low blast resistance will fall down. -1 Disables falling", 100);
		// railgun
		Property railDamage = config.get(CATEGORY_NUKE, "6.20_railgunDamage", 1000);
		railDamage.setComment("How much damage a railgun death blast does per tick");
		railgunDamage = railDamage.getInt();
		Property railBuffer = config.get(CATEGORY_NUKE, "6.21_railgunBuffer", 500000000);
		railBuffer.setComment("How much RF the railgun can store");
		railgunDamage = railBuffer.getInt();
		Property railUse = config.get(CATEGORY_NUKE, "6.22_railgunConsumption", 250000000);
		railUse.setComment("How much RF the railgun requires per shot");
		railgunDamage = railUse.getInt();
		Property fireDurationP = config.get(CATEGORY_NUKE, "6.23_fireDuration", 15 * 20);
		fireDurationP.setComment("How long the fire blast will last in ticks");
		fireDuration = fireDurationP.getInt();
		
		fogCh = CommonConfig.setDef(RadiationConfig.fogCh, 20);

		//TODO either completely depricate this or make it work
		neutronActivation = CommonConfig.createConfigBool(config, CATEGORY_RAD, "7.01_itemContamination", "Whether high radiation levels should radiate items in inventory. WARNING: extremely laggy and and buggy. Keep it off unless you know what you are doing", false);
		neutronActivationThreshold = CommonConfig.createConfigInt(config, CATEGORY_RAD, "7.01_itemContaminationThreshold", "Minimum recieved Rads/s threshold at which items get irradiated", 15);
		
		geigerX = CommonConfig.createConfigInt(config, CATEGORY_RAD, "7.02_geigerX", "X Coordinate of the geiger counter gui (x=0 is on the right)", 16);
		geigerY = CommonConfig.createConfigInt(config, CATEGORY_RAD, "7.03_geigerY", "Y Coordinate of the geiger counter gui (y=0 is on the bottom)", 2);
		digammaX = CommonConfig.createConfigInt(config, CATEGORY_RAD, "7.04_digammaX", "X Coordinate of the digamma diagnostic gui (x=0 is on the right)", 16);
		digammaY = CommonConfig.createConfigInt(config, CATEGORY_RAD, "7.05_digammaY", "Y Coordinate of the digamma diagnostic gui (y=0 is on the bottom)", 18);

		hazardRate = CommonConfig.createConfigInt(config, CATEGORY_HAZ, "CE-0.3_hazardRate", "Ticks between application of effects for the hazards", 5);
		disableAsbestos = CommonConfig.createConfigBool(config, CATEGORY_HAZ, "CE-0.3_disableAsbestos", "Setting it true makes Asbestos Hazard to do nothing", false);
		disableBlinding = CommonConfig.createConfigBool(config, CATEGORY_HAZ, "CE-0.3_disableBlinding", "Setting it true makes Blinding Hazard to do nothing", false);
		disableCoal = CommonConfig.createConfigBool(config, CATEGORY_HAZ, "CE-0.3_disableCoal", "Setting it true makes Coal Hazard to do nothing", false);;
		disableExplosive = CommonConfig.createConfigBool(config, CATEGORY_HAZ, "CE-0.3_disableExplosive", "Setting it true makes Explosive Hazard to do nothing", false);
		disableHydro = CommonConfig.createConfigBool(config, CATEGORY_HAZ, "CE-0.3_disableHydro", "Setting it true makes Hydro Hazard to do nothing", false);;
		disableHot = CommonConfig.createConfigBool(config, CATEGORY_HAZ, "CE-0.3_disableHot", "Setting it true makes Hot Hazard to do nothing", false);
		disableCold = CommonConfig.createConfigBool(config, CATEGORY_HAZ, "CE-0.3_disableCold", "Setting it true makes Cold Hazard to do nothing", false);
		disableToxic = CommonConfig.createConfigBool(config, CATEGORY_HAZ, "CE-0.3_disableToxic", "Setting it true makes Toxic Hazard to do nothing", false);

		enablePollution = CommonConfig.createConfigBool(config, CATEGORY_POL, "16.01_enablePollution", "If disabled, none of the polltuion related things will work", true);
		enableLeadFromBlocks = CommonConfig.createConfigBool(config, CATEGORY_POL, "16.02_enableLeadFromBlocks", "Whether breaking blocks in heavy metal polluted areas will poison the player", true);
		enableLeadPoisoning = CommonConfig.createConfigBool(config, CATEGORY_POL, "16.03_enableLeadPoisoning", "Whether being in a heavy metal polluted area will poison the player", true);
		enableSootFog = CommonConfig.createConfigBool(config, CATEGORY_POL, "16.04_enableSootFog", "Whether smog should be visible", true);
		enablePoison = CommonConfig.createConfigBool(config, CATEGORY_POL, "16.05_enablePoison", "Whether being in a poisoned area will affect the player", true);
		buffMobThreshold = CommonConfig.createConfigDouble(config, CATEGORY_POL, "16.06_buffMobThreshold", "The amount of soot required to buff naturally spawning mobs", 15D);
		sootFogThreshold = CommonConfig.createConfigDouble(config, CATEGORY_POL, "16.07_sootFogThreshold", "How much soot is required for smog to become visible", 35D);
		sootFogDivisor = CommonConfig.createConfigDouble(config, CATEGORY_POL, "16.08_sootFogDivisor", "The divisor for smog, higher numbers will require more soot for the same smog density", 120D);
		smokeStackSootMult = CommonConfig.createConfigDouble(config, CATEGORY_POL, "16.09_smokeStackSootMult", "How much does smokestack multiply soot by, with decimal values reducing the soot", 0.8);
	}

}
