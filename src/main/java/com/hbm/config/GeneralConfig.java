package com.hbm.config;

import com.hbm.main.MainRegistry;
import com.hbm.render.GLCompat;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GLContext;

public class GeneralConfig {

	public static double conversionRateHeToRF = 1.0F;
	public static boolean enablePacketThreading = true;
	public static int packetThreadingCoreCount = 1;
	public static int packetThreadingMaxCount = 2;
	public static boolean packetThreadingErrorBypass = false;
	public static boolean enableTickBasedWorldGenerator = false;
	public static boolean enableDebugMode = false;
	public static boolean enableDebugWorldGen = false;
	public static boolean enableSkybox = true;
	public static boolean enableWelcomeMessage = true;
	public static boolean enableKeybindOverlap = true;
	public static boolean enableFluidContainerCompat = true;
	public static boolean enableMycelium = false;
	public static boolean enablePlutoniumOre = false;
	public static boolean enableDungeons = true;
	public static boolean enableMDOres = true;
	public static boolean enableMines = true;
	public static boolean enableRad = true;
	public static boolean enableNITAN = true;
	public static boolean enableAutoCleanup = false;
	public static boolean enableMeteorStrikes = true;
	public static boolean enableMeteorShowers = true;
	public static boolean enableMeteorTails = true;
	public static boolean enableSpecialMeteors = true;
	public static boolean enableBomberShortMode = false;
	public static boolean enableVaults = true;
	public static boolean enableRads = true;
	public static boolean enableCoal = true;
	public static boolean enableAsbestos = true;
	public static boolean advancedRadiation = true;
	public static boolean enableCataclysm = false;
	public static boolean enableExtendedLogging = false;
	public static boolean enableHardcoreTaint = false;
	public static boolean enableGuns = true;
	public static boolean ssgAnim = true;
	public static boolean enableVirus = true;
	public static boolean enableCrosshairs = true;
	public static boolean instancedParticles = true;
	public static boolean callListModels = true;
	public static boolean useShaders = false;
	public static boolean useShaders2 = false;
	public static boolean bloom = true;
	public static boolean heatDistortion = true;
	public static boolean recipes = true;
	public static boolean shapeless = true;
	public static boolean oredict = true;
	public static boolean shaped = true;
	public static boolean nonoredict = true;
	public static boolean jei = true;
	public static boolean changelog = true;
	public static boolean registerTanks = true;
	public static boolean duckButton = true;
	public static boolean depthEffects = true;
	public static boolean flashlight = true;
	public static boolean flashlightVolumetric = true;
	public static boolean bulletHoleNormalMapping = true;
	public static int flowingDecalAmountMax = 20;
	public static boolean bloodFX = true;
	public static int hintPos = 0;
	public static int decoToIngotRate = 25;
	public static int crucibleMaxCharges = 16;
	public static boolean enableReEval = true;
	public static boolean enableSteamParticles = true;
	public static boolean enableServerRecipeSync = true;
	public static boolean enableExpensiveMode = false;
	
	public static boolean enable528 = false;
	public static boolean enable528ReasimBoilers = true;
	public static boolean enable528ColtanDeposit = true;
	public static boolean enable528ColtanSpawn = false;
	public static boolean enable528BedrockDeposit = true;
	public static boolean enable528BedrockSpawn = false;
	public static boolean enableReflectorCompat = false;
	public static int coltanRate = 2;
	public static int bedrockRate = 50;
	public static boolean enableThreadedAtmospheres = true;
	public static boolean enableHardcoreDarkness = false;

	public static boolean enableLBSM = false;
	public static boolean enableLBSMFullSchrab = true;
	public static boolean enableLBSMShorterDecay = true;
	public static boolean enableLBSMSimpleArmorRecipes = true;
	public static boolean enableLBSMSimpleToolRecipes = true;
	public static boolean enableLBSMSimpleAlloy = true;
	public static boolean enableLBSMSimpleChemsitry = true;
	public static boolean enableLBSMSimpleCentrifuge = true;
	public static boolean enableLBSMUnlockAnvil = true;
	public static boolean enableLBSMSimpleCrafting = true;
	public static boolean enableLBSMSimpleMedicineRecipes = true;
	public static boolean enableLBSMSafeCrates = true;
	public static boolean enableLBSMSafeMEDrives = true;
	public static boolean enableLBSMIGen = true;

	public static boolean disableAsbestos = false;
	public static boolean disableBlinding = false;
	public static boolean disableCoal = false;
	public static boolean disableExplosive = false;
	public static boolean disableHydro = false;
	public static boolean disableHot = false;
	public static boolean disableCold = false;

	public static void loadFromConfig(Configuration config){
		final String CATEGORY_GENERAL = "01_general";
		enablePacketThreading = config.get(CATEGORY_GENERAL, "0.01_enablePacketThreading", true, "Enables creation of a separate thread to increase packet processing speed on servers. Disable this if you are having anomalous crashes related to memory connections.").getBoolean(true);
		packetThreadingCoreCount = config.get(CATEGORY_GENERAL, "0.02_packetThreadingCoreCount", 1, "Number of core threads to create for packets (recommended 1).").getInt(1);
		packetThreadingMaxCount = config.get(CATEGORY_GENERAL, "0.03_packetThreadingMaxCount", 2, "Maximum number of threads to create for packet threading. Must be greater than or equal to 0.02_packetThreadingCoreCount.").getInt(2);
		packetThreadingErrorBypass = config.get(CATEGORY_GENERAL, "0.04_packetThreadingErrorBypass", false, "Forces the bypassing of most packet threading errors, only enable this if directed to or if you know what you're doing.").getBoolean(false);
		enableServerRecipeSync = config.get(CATEGORY_GENERAL, "0.05_enableServerRecipeSync", true, "Syncs any recipes customised via JSON to clients connecting to the server.").getBoolean(true);
		enableTickBasedWorldGenerator = config.get(CATEGORY_GENERAL, "0.06_enableTickBasedWorldGenerator", false, "Use tick-based phased world generator. This eliminates cascading worldgen, but is incompatible with most chunk pre-generators.\n Do not set to true unless you know what you are doing.").getBoolean(false);
		enableDebugMode = config.get(CATEGORY_GENERAL, "1.00_enableDebugMode", false).getBoolean(false);
		enableDebugWorldGen = config.get(CATEGORY_GENERAL, "1.00_enableDebugWorldGen", false).getBoolean(false);
		enableSkybox = config.get(CATEGORY_GENERAL, "1.00_enableSkybox", true, "Do not set it to false unless you know what you are doing.").getBoolean(true);
		enableMycelium = config.get(CATEGORY_GENERAL, "1.01_enableMyceliumSpread", false).getBoolean(false);
		enablePlutoniumOre = config.get(CATEGORY_GENERAL, "1.02_enablePlutoniumNetherOre", false).getBoolean(false);
		enableDungeons = config.get(CATEGORY_GENERAL, "1.03_enableDungeonSpawn", true).getBoolean(true);
		enableMDOres = config.get(CATEGORY_GENERAL, "1.04_enableOresInModdedDimensions", true).getBoolean(true);
		enableMines = config.get(CATEGORY_GENERAL, "1.05_enableLandmineSpawn", true).getBoolean(true);
		enableRad = config.get(CATEGORY_GENERAL, "1.06_enableRadHotspotSpawn", true).getBoolean(true);
		enableNITAN = config.get(CATEGORY_GENERAL, "1.07_enableNITANChestSpawn", true).getBoolean(true);
		enableAutoCleanup = config.get(CATEGORY_GENERAL, "1.09_enableAutomaticRadCleanup", false).getBoolean(false);
		enableMeteorStrikes = config.get(CATEGORY_GENERAL, "1.10_enableMeteorStrikes", true).getBoolean(true);
		enableMeteorShowers = config.get(CATEGORY_GENERAL, "1.11_enableMeteorShowers", true).getBoolean(true);
		enableMeteorTails = config.get(CATEGORY_GENERAL, "1.12_enableMeteorTails", true).getBoolean(true);
		enableSpecialMeteors = config.get(CATEGORY_GENERAL, "1.13_enableSpecialMeteors", false).getBoolean(false);
		enableBomberShortMode = config.get(CATEGORY_GENERAL, "1.14_enableBomberShortMode", false).getBoolean(false);
		enableVaults = config.get(CATEGORY_GENERAL, "1.15_enableVaultSpawn", true).getBoolean(true);
		enableRads = CommonConfig.createConfigBool(config, CATEGORY_GENERAL, "1.16_enableNewRadiation", "Toggles the world radiation system (primarly chunk radiation system)", true);
		enableCataclysm = config.get(CATEGORY_GENERAL, "1.17_enableCataclysm", false).getBoolean(false);
		enableExtendedLogging = config.get(CATEGORY_GENERAL, "1.18_enableExtendedLogging", false).getBoolean(false);
		enableHardcoreTaint = config.get(CATEGORY_GENERAL, "1.19_enableHardcoreTaint", false).getBoolean(false);
		enableGuns = config.get(CATEGORY_GENERAL, "1.20_enableGuns", true).getBoolean(true);
		enableVirus = config.get(CATEGORY_GENERAL, "1.21_enableVirus", false).getBoolean(false);
        enableCrosshairs = config.get(CATEGORY_GENERAL, "1.22_enableCrosshairs", true).getBoolean(true);
		Property shaders = config.get(CATEGORY_GENERAL, "1.23_enableShaders", false);
		shaders.setComment("Experimental, don't use");
		useShaders = shaders.getBoolean(false);
		if(FMLCommonHandler.instance().getSide() == Side.CLIENT)
			if(!OpenGlHelper.shadersSupported) {
				MainRegistry.logger.log(Level.WARN, "GLSL shaders are not supported; not using shaders");
				useShaders = false;
			} else if(!GLContext.getCapabilities().OpenGL30) {
				MainRegistry.logger.log(Level.WARN, "OpenGL 3.0 is not supported; not using shaders");
				useShaders = false;
			}
		useShaders = false;
		useShaders2 = config.get(CATEGORY_GENERAL, "1.23_enableShaders2", false).getBoolean(false);
		Property ssg_anim = config.get(CATEGORY_GENERAL, "1.24_ssgAnimType", true);
		ssg_anim.setComment("Which supershotgun reload animation to use. True is Drillgon's animation, false is Bob's animation");
		ssgAnim = ssg_anim.getBoolean();
		instancedParticles = CommonConfig.createConfigBool(config, CATEGORY_GENERAL, "1.25_instancedParticles", "Enables instanced particle rendering for some particles, which makes them render several times faster. May not work on all computers, and will break with shaders.", true);
		depthEffects = CommonConfig.createConfigBool(config, CATEGORY_GENERAL, "1.25_depthBufferEffects", "Enables effects that make use of reading from the depth buffer", true);
		flashlight = CommonConfig.createConfigBool(config, CATEGORY_GENERAL, "1.25_flashlights", "Enables dynamic directional lights", true);
		flashlightVolumetric = CommonConfig.createConfigBool(config, CATEGORY_GENERAL, "1.25_flashlight_volumetrics", "Enables volumetric lighting for directional lights", true);
		bulletHoleNormalMapping = CommonConfig.createConfigBool(config, CATEGORY_GENERAL, "1.25_bullet_hole_normal_mapping", "Enables normal mapping on bullet holes, which can improve visuals", true);
		flowingDecalAmountMax = CommonConfig.createConfigInt(config, CATEGORY_GENERAL, "1.25_flowing_decal_max", "The maximum number of 'flowing' decals that can exist at once (eg blood that can flow down walls)", 20);
		
		callListModels = CommonConfig.createConfigBool(config, CATEGORY_GENERAL, "1.26_callListModels", "Enables call lists for a few models, making them render extremely fast", true);
		enableReflectorCompat = config.get(CATEGORY_GENERAL, "1.24_enableReflectorCompat", false).getBoolean(false);
		
		enableCoal = config.get(CATEGORY_GENERAL, "1.26_enableCoalDust", true).getBoolean(true);
		enableAsbestos = config.get(CATEGORY_GENERAL, "1.26_enableAsbestosDust", true).getBoolean(true);
		
		enableReEval = config.get(CATEGORY_GENERAL, "1.27_enableReEval", true, "Allows re-evaluating power networks on link remove instead of destroying and recreating").getBoolean(true);
		enableSteamParticles = config.get(CATEGORY_GENERAL, "1.27.1_enableSteamParticles", true, "If disabled, auxiliary cooling towers and large cooling towers will not emit steam particles when in use.").getBoolean(true);
		
		recipes = config.get(CATEGORY_GENERAL, "1.28_enableRecipes", true).getBoolean(true);
		shapeless = config.get(CATEGORY_GENERAL, "1.28_enableShapeless", true).getBoolean(true);
		oredict = config.get(CATEGORY_GENERAL, "1.28_enableOreDict", true).getBoolean(true);
		shaped = config.get(CATEGORY_GENERAL, "1.28_enableShaped", true).getBoolean(true);
		nonoredict = config.get(CATEGORY_GENERAL, "1.28_enableNonOreDict", true).getBoolean(true);
		registerTanks = config.get(CATEGORY_GENERAL, "1.28_registerTanks", true).getBoolean(true);
		
		jei = config.get(CATEGORY_GENERAL, "1.28_enableJei", true).getBoolean(true);
		changelog = config.get(CATEGORY_GENERAL, "1.28_enableChangelog", true).getBoolean(true);
		duckButton = config.get(CATEGORY_GENERAL, "1.28_enableDuckButton", true).getBoolean(true);
		bloom = config.get(CATEGORY_GENERAL, "1.30_enableBloom", true).getBoolean(true);
		heatDistortion = config.get(CATEGORY_GENERAL, "1.30_enableHeatDistortion", true).getBoolean(true);
		
		Property adv_rads = config.get(CATEGORY_GENERAL, "1.31_enableAdvancedRadiation", true);
		adv_rads.setComment("Enables a 3 dimensional version of the radiation system that also allows some blocks (like concrete bricks) to stop it from spreading");
		advancedRadiation = adv_rads.getBoolean(true);
		
		bloodFX = CommonConfig.createConfigBool(config, CATEGORY_GENERAL, "1.32_enable_blood_effects", "Enables the over-the-top blood visual effects for some weapons", true);
	
		if((instancedParticles || depthEffects || flowingDecalAmountMax > 0 || bloodFX || bloom || heatDistortion) && (!GLCompat.error.isEmpty() || !useShaders2)){
			MainRegistry.logger.error("Warning - Open GL 3.3 not supported! Disabling 3.3 effects...");
			if(!useShaders2){
				MainRegistry.logger.error("Shader effects manually disabled");
			}
			instancedParticles = false;
			depthEffects = false;
			flowingDecalAmountMax = 0;
			bloodFX = false;
			useShaders2 = false;
			bloom = false;
			heatDistortion = false;
		}
		if(!depthEffects){
			flashlight = false;
			bulletHoleNormalMapping = false;
		}
		if(!flashlight){
			flashlightVolumetric = false;
		}
		
		crucibleMaxCharges = CommonConfig.createConfigInt(config, CATEGORY_GENERAL, "1.33_crucible_max_charges", "How many times you can use the crucible before recharge", 16);
		
		if(crucibleMaxCharges <= 0){
			crucibleMaxCharges = 16;
		}

		enableWelcomeMessage = CommonConfig.createConfigBool(config, CATEGORY_GENERAL, "1.34_enableWelcomeMessage", "Enables the welcome message which appears in the chat when you load into the game", true);

		conversionRateHeToRF = CommonConfig.createConfigDouble(config, CATEGORY_GENERAL, "1.35_conversionRateHeToRF", "One HE is (insert number) RF - <number> (double)", 1.0D);

		hintPos = CommonConfig.createConfigInt(config, CATEGORY_GENERAL, "1.36_infoOverlayPosition", "Positions where the info overlay will appear (from 0 to 3). 0: Top left\n1: Top right\n2: Center right\n3: Center Left", 0);
		enableFluidContainerCompat = config.get(CATEGORY_GENERAL, "1.37_enableFluidContainerCompat", true, "If enabled, fluid containers will be oredicted and interchangable in recipes with other mods' containers. Should probably work with things like IE's/GC oil properly.").getBoolean(true);
		decoToIngotRate = CommonConfig.createConfigInt(config, CATEGORY_GENERAL, "1.38_decoToIngotConversionRate", "Chance of successful turning a deco block into an ingot. Default is 25%", 25);
		enableThreadedAtmospheres = CommonConfig.createConfigBool(config, CATEGORY_GENERAL, "1.39_threadedAtmospheres", "If enabled, will run atmosphere blobbing in a separate thread for performance", true);
		enableHardcoreDarkness = CommonConfig.createConfigBool(config, CATEGORY_GENERAL, "1.40_hardcoreDarkness", "If enabled, sets night-time minimum fog to zero, to complement hardcore darkness mods", false);
		enableKeybindOverlap = config.get(CATEGORY_GENERAL, "1.41_enableKeybindOverlap", true, "If enabled, will handle keybinds that would otherwise be ignored due to overlapping.").getBoolean(true);
		enableExpensiveMode = config.get(CATEGORY_GENERAL, "1.99_enableExpensiveMode", false, "It does what the name implies.").getBoolean(false);

		final String CATEGORY_528 = "528";

		config.addCustomCategoryComment(CATEGORY_528, "CAUTION\n"
				+ "528 Mode: Please proceed with caution!\n"
				+ "528-Modus: Lassen Sie Vorsicht walten!\n"
				+ "способ-528: действовать с осторожностью!");
		
		enable528 = CommonConfig.createConfigBool(config, CATEGORY_528, "enable528Mode", "The central toggle for 528 mode.", false);
		enable528ReasimBoilers = CommonConfig.createConfigBool(config, CATEGORY_528, "X528_forceReasimBoilers", "Keeps the RBMK dial for ReaSim boilers on, preventing use of non-ReaSim boiler columns and forcing the use of steam in-/outlets", true);
		enable528ColtanDeposit = CommonConfig.createConfigBool(config, CATEGORY_528, "X528_enableColtanDepsoit", "Enables the coltan deposit. A large amount of coltan will spawn around a single random location in the world.", true);
		enable528ColtanSpawn = CommonConfig.createConfigBool(config, CATEGORY_528, "X528_enableColtanSpawning", "Enables coltan ore as a random spawn in the world. Unlike the deposit option, coltan will not just spawn in one central location.", false);
		enable528BedrockDeposit = CommonConfig.createConfigBool(config, CATEGORY_528, "X528_enableBedrockDepsoit", "Enables bedrock coltan ores in the coltan deposit. These ores can be drilled to extract infinite coltan, albeit slowly.", true);
		enable528BedrockSpawn = CommonConfig.createConfigBool(config, CATEGORY_528, "X528_enableBedrockSpawning", "Enables the bedrock coltan ores as a rare spawn. These will be rarely found anywhere in the world.", false);
		coltanRate = CommonConfig.createConfigInt(config, CATEGORY_528, "X528_oreColtanFrequency", "Determines how many coltan ore veins are to be expected in a chunk. These values do not affect the frequency in deposits, and only apply if random coltan spanwing is enabled.", 2);
		bedrockRate = CommonConfig.createConfigInt(config, CATEGORY_528, "X528_bedrockColtanFrequency", "Determines how often (1 in X) bedrock coltan ores spawn. Applies for both the bedrock ores in the coltan deposit (if applicable) and the random bedrock ores (if applicable)", 50);

		final String CATEGORY_LBSM = CommonConfig.CATEGORY_LBSM;

		config.addCustomCategoryComment(CATEGORY_LBSM,
				"Will most likely break standard progression!\n"
						+ "However, the game gets generally easier and more enjoyable for casual players.\n"
						+ "Progression-braking recipes are usually not too severe, so the mode is generally server-friendly!");

		enableLBSM = CommonConfig.createConfigBool(config, CATEGORY_LBSM, "enableLessBullshitMode", "The central toggle for LBS mode. Forced OFF when 528 is enabled!", false);
		enableLBSMFullSchrab = CommonConfig.createConfigBool(config, CATEGORY_LBSM, "LBSM_fullSchrab", "When enabled, this will replace schraranium with full schrabidium ingots in the transmutator's output", true);
		enableLBSMShorterDecay = CommonConfig.createConfigBool(config, CATEGORY_LBSM, "LBSM_shortDecay", "When enabled, this will highly accelerate the speed at which nuclear waste disposal drums decay their contents. 60x faster than 528 mode and 5-12x faster than on normal mode.", true);
		enableLBSMSimpleArmorRecipes = CommonConfig.createConfigBool(config, CATEGORY_LBSM, "LBSM_recipeSimpleArmor", "When enabled, simplifies the recipe for armor sets like starmetal or schrabidium.", true);
		enableLBSMSimpleToolRecipes = CommonConfig.createConfigBool(config, CATEGORY_LBSM, "LBSM_recipeSimpleTool", "When enabled, simplifies the recipe for tool sets like starmetal or scrhabidium", true);
		enableLBSMSimpleAlloy = CommonConfig.createConfigBool(config, CATEGORY_LBSM, "LBSM_recipeSimpleAlloy", "When enabled, adds some blast furnace recipes to make certain things cheaper", true);
		enableLBSMSimpleChemsitry = CommonConfig.createConfigBool(config, CATEGORY_LBSM, "LBSM_recipeSimpleChemistry", "When enabled, simplifies some chemical plant recipes", true);
		enableLBSMSimpleCentrifuge = CommonConfig.createConfigBool(config, CATEGORY_LBSM, "LBSM_recipeSimpleCentrifuge", "When enabled, enhances centrifuge outputs to make rare materials more common", true);
		enableLBSMUnlockAnvil = CommonConfig.createConfigBool(config, CATEGORY_LBSM, "LBSM_recipeUnlockAnvil", "When enabled, all anvil recipes are available at tier 1", true);
		enableLBSMSimpleCrafting = CommonConfig.createConfigBool(config, CATEGORY_LBSM, "LBSM_recipeSimpleCrafting", "When enabled, some uncraftable or more expansive items get simple crafting recipes. Scorched uranium also becomes washable", true);
		enableLBSMSimpleMedicineRecipes = CommonConfig.createConfigBool(config, CATEGORY_LBSM, "LBSM_recipeSimpleMedicine", "When enabled, makes some medicine recipes (like ones that require bismuth) much more affordable", true);
		enableLBSMSafeCrates = CommonConfig.createConfigBool(config, CATEGORY_LBSM, "LBSM_safeCrates", "When enabled, prevents crates from becoming radioactive", true);
		enableLBSMSafeMEDrives = CommonConfig.createConfigBool(config, CATEGORY_LBSM, "LBSM_safeMEDrives", "When enabled, prevents ME Drives and Portable Cells from becoming radioactive", true);
		enableLBSMIGen = CommonConfig.createConfigBool(config, CATEGORY_LBSM, "LBSM_iGen", "When enabled, restores the industrial generator to pre-nerf power", true);

		if(enable528) enableLBSM = false;
	}
}
