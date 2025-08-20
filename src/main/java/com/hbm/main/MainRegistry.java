package com.hbm.main;

//FIXME This may have gotten mangled in a merge

import com.google.common.collect.ImmutableList;
import com.hbm.blocks.BlockEnums;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockCrate;
import com.hbm.blocks.generic.BlockResourceStone;
import com.hbm.capability.HbmCapability;
import com.hbm.capability.HbmLivingCapability;
import com.hbm.capability.NTMBatteryCapabilityHandler;
import com.hbm.command.CommandHbm;
import com.hbm.command.CommandPacketInfo;
import com.hbm.command.CommandRadiation;
import com.hbm.config.*;
import com.hbm.creativetabs.*;
import com.hbm.dim.CommandSpaceTP;
import com.hbm.dim.SolarSystem;
import com.hbm.entity.logic.IChunkLoader;
import com.hbm.entity.siege.SiegeTier;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.forgefluid.FFPipeNetwork;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.handler.*;
import com.hbm.handler.imc.IMCHandler;
import com.hbm.handler.neutron.NeutronHandler;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.radiation.RadiationSystemNT;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.hazard.HazardRegistry;
import com.hbm.interfaces.Spaghetti;
import com.hbm.inventory.BedrockOreRegistry;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.control_panel.ControlEvent;
import com.hbm.inventory.control_panel.ControlRegistry;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.*;
import com.hbm.inventory.recipes.SerializableRecipe;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.GrenadeDispenserRegistry;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.HbmWorld;
import com.hbm.lib.Library;
import com.hbm.lib.RefStrings;
import com.hbm.packet.PacketDispatcher;
import com.hbm.potion.HbmDetox;
import com.hbm.potion.HbmPotion;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.tileentity.bomb.TileEntityLaunchPadBase;
import com.hbm.tileentity.bomb.TileEntityNukeCustom;
import com.hbm.tileentity.machine.rbmk.RBMKDials;
import com.hbm.util.CrashHelper;
import com.hbm.util.DamageResistanceHandler;
import com.hbm.world.ModBiomes;
import com.hbm.world.PlanetGen;
import com.hbm.world.feature.SchistStratum;
import com.hbm.world.generator.CellularDungeonFactory;
import com.hbm.world.phased.PhasedStructureGenerator;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatBasic;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mod(modid = RefStrings.MODID, version = RefStrings.VERSION, name = RefStrings.NAME)
@Spaghetti("Total cluserfuck")
public class MainRegistry {

    @SidedProxy(clientSide = RefStrings.CLIENTSIDE, serverSide = RefStrings.SERVERSIDE)
    public static ServerProxy proxy;
    @Mod.Instance(RefStrings.MODID)
    public static MainRegistry instance;
    public static Logger logger;
    public static List<FFPipeNetwork> allPipeNetworks = new ArrayList<>();
    // Creative Tabs
    // ingots, nuggets, wires, machine parts
    public static CreativeTabs partsTab = new PartsTab(CreativeTabs.getNextID(), "tabParts");
    // items that belong in machines, fuels, etc
    public static CreativeTabs controlTab = new ControlTab(CreativeTabs.getNextID(), "tabControl");
    // templates, siren tracks
    public static CreativeTabs templateTab = new TemplateTab(CreativeTabs.getNextID(), "tabTemplate");
    // ore and mineral blocks
    public static CreativeTabs resourceTab = new ResourceTab(CreativeTabs.getNextID(), "tabResource");
    // construction blocks
    public static CreativeTabs blockTab = new BlockTab(CreativeTabs.getNextID(), "tabBlocks");
    // machines, structure parts
    public static CreativeTabs machineTab = new MachineTab(CreativeTabs.getNextID(), "tabMachine");
    // bombs
    public static CreativeTabs nukeTab = new NukeTab(CreativeTabs.getNextID(), "tabNuke");
    // missiles, satellites
    public static CreativeTabs missileTab = new MissileTab(CreativeTabs.getNextID(), "tabMissile");
    // turrets, weapons, ammo
    public static CreativeTabs weaponTab = new WeaponTab(CreativeTabs.getNextID(), "tabWeapon");
    // drinks, kits, tools
    public static CreativeTabs consumableTab = new ConsumableTab(CreativeTabs.getNextID(), "tabConsumable");

    public static StatBase statMines;
    public static StatBase statBullets;
    public static int generalOverride = 0;
    public static int polaroidID = 1;
    public static int x;
    public static int y;
    public static int z;
    public static long time;
    public static File configDir;
    public static File configHbmDir;
    // Armor Materials
    // Drillgon200: I have no idea what the two strings and the number at the
    // end are.
    public static ArmorMaterial enumArmorMaterialT45 = EnumHelper.addArmorMaterial(RefStrings.MODID + ":T45", RefStrings.MODID + ":T45", 150, new int[]{3, 6, 8, 3}, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial aMatBJ = EnumHelper.addArmorMaterial(RefStrings.MODID + ":BLACKJACK", RefStrings.MODID + ":HBM_BLACKJACK", 150, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial aMatAJR = EnumHelper.addArmorMaterial(RefStrings.MODID + ":T45AJR", RefStrings.MODID + ":T45AJR", 150, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial aMatSteamsuit = EnumHelper.addArmorMaterial(RefStrings.MODID + ":Steamsuit", RefStrings.MODID + ":Steamsuit", 150, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial aMatDieselsuit = EnumHelper.addArmorMaterial(RefStrings.MODID + ":Dieselsuit", RefStrings.MODID + ":Dieselsuit", 150, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial aMatTrench = EnumHelper.addArmorMaterial(RefStrings.MODID + ":Trenchmaster", RefStrings.MODID + ":Trenchmaster", 150, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial aMatTaurun = EnumHelper.addArmorMaterial(RefStrings.MODID + ":Taurun", RefStrings.MODID + ":Taurun", 150, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial aMatBismuth = EnumHelper.addArmorMaterial(RefStrings.MODID + ":Bismuth", RefStrings.MODID + ":Bismuth", 150, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial aMatZirconium = EnumHelper.addArmorMaterial(RefStrings.MODID + ":Zirconium", RefStrings.MODID + ":Zirconium", 150, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial aMatDNT = EnumHelper.addArmorMaterial(RefStrings.MODID + ":DNT", RefStrings.MODID + ":DNT", 3, new int[]{1, 1, 1, 1}, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
    public static ArmorMaterial aMatEnvsuit = EnumHelper.addArmorMaterial(RefStrings.MODID + ":Envsuit", RefStrings.MODID + ":Envsuit", 150, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial aMatRPA = EnumHelper.addArmorMaterial(RefStrings.MODID + ":RPA", RefStrings.MODID + ":RPA", 150, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial aMatHEV = EnumHelper.addArmorMaterial(RefStrings.MODID + ":HEV", RefStrings.MODID + ":HEV", 150, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial enumArmorMaterialHazmat = EnumHelper.addArmorMaterial(RefStrings.MODID + ":HAZMAT", RefStrings.MODID + ":HAZMAT", 60, new int[]{1, 4, 5, 2}, 5, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
    public static ArmorMaterial enumArmorMaterialHazmat2 = EnumHelper.addArmorMaterial(RefStrings.MODID + ":HAZMAT2", RefStrings.MODID + ":HAZMAT2", 60, new int[]{1, 4, 5, 2}, 5, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
    public static ArmorMaterial enumArmorMaterialHazmat3 = EnumHelper.addArmorMaterial(RefStrings.MODID + ":HAZMAT3", RefStrings.MODID + ":HAZMAT3", 60, new int[]{1, 4, 5, 2}, 5, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
    public static ArmorMaterial enumArmorMaterialPaa = EnumHelper.addArmorMaterial(RefStrings.MODID + ":PAA", RefStrings.MODID + ":PAA", 75, new int[]{3, 6, 8, 3}, 25, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial enumArmorMaterialSchrabidium = EnumHelper.addArmorMaterial(RefStrings.MODID + ":SCHRABIDIUM", RefStrings.MODID + ":SCHRABIDIUM", 100, new int[]{3, 6, 8, 3}, 50, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial enumArmorMaterialEuphemium = EnumHelper.addArmorMaterial(RefStrings.MODID + ":EUPHEMIUM", RefStrings.MODID + ":EUPHEMIUM", 15000000, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial enumArmorMaterialSteel = EnumHelper.addArmorMaterial(RefStrings.MODID + ":STEEL", RefStrings.MODID + ":STEEL", 20, new int[]{2, 5, 6, 2}, 5, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
    public static ArmorMaterial enumArmorMaterialAlloy = EnumHelper.addArmorMaterial(RefStrings.MODID + ":ALLOY", RefStrings.MODID + ":ALLOY", 40, new int[]{3, 6, 8, 3}, 12, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
    public static ArmorMaterial enumArmorMaterialAusIII = EnumHelper.addArmorMaterial(RefStrings.MODID + ":AUSIII", RefStrings.MODID + ":AUSIII", 375, new int[]{2, 5, 6, 2}, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
    public static ArmorMaterial enumArmorMaterialTitanium = EnumHelper.addArmorMaterial(RefStrings.MODID + ":TITANIUM", RefStrings.MODID + ":TITANIUM", 25, new int[]{3, 6, 8, 3}, 9, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial enumArmorMaterialCmb = EnumHelper.addArmorMaterial(RefStrings.MODID + ":CMB", RefStrings.MODID + ":CMB", 60, new int[]{3, 6, 8, 3}, 50, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial enumArmorMaterialSecurity = EnumHelper.addArmorMaterial(RefStrings.MODID + ":SECURITY", RefStrings.MODID + ":SECURITY", 100, new int[]{3, 6, 8, 3}, 15, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial enumArmorMaterialAsbestos = EnumHelper.addArmorMaterial(RefStrings.MODID + ":ASBESTOS", RefStrings.MODID + ":ASBESTOS", 20, new int[]{1, 3, 4, 1}, 5, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
    public static ArmorMaterial aMatCobalt = EnumHelper.addArmorMaterial(RefStrings.MODID + ":COBALT", RefStrings.MODID + ":COBALT", 70, new int[]{3, 6, 8, 3}, 25, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial aMatStarmetal = EnumHelper.addArmorMaterial(RefStrings.MODID + ":STARMETAL", RefStrings.MODID + ":STARMETAL", 150, new int[]{3, 6, 8, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial aMatLiquidator = EnumHelper.addArmorMaterial(RefStrings.MODID + ":LIQUIDATOR", RefStrings.MODID + ":LIQUIDATOR", 750, new int[]{3, 6, 8, 3}, 10, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial aMatFau = EnumHelper.addArmorMaterial(RefStrings.MODID + ":DIGAMMA", RefStrings.MODID + ":DIGAMMA", 150, new int[]{3, 8, 6, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    public static ArmorMaterial aMatDNS = EnumHelper.addArmorMaterial(RefStrings.MODID + ":DNT_NANO", RefStrings.MODID + ":DNT_NANO", 150, new int[]{3, 8, 6, 3}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 2.0F);
    // Tool Materials
    public static ToolMaterial enumToolMaterialSchrabidium = EnumHelper.addToolMaterial(RefStrings.MODID + ":SCHRABIDIUM", 4, 10000, 50.0F, 100.0F, 200);
    public static ToolMaterial enumToolMaterialHammer = EnumHelper.addToolMaterial(RefStrings.MODID + ":SCHRABIDIUMHAMMER", 3, 0, 50.0F, 999999996F, 200);
    public static ToolMaterial enumToolMaterialChainsaw = EnumHelper.addToolMaterial(RefStrings.MODID + ":CHAINSAW", 3, 1500, 50.0F, 22.0F, 0);
    public static ToolMaterial enumToolMaterialSteel = EnumHelper.addToolMaterial(RefStrings.MODID + ":STEEL", 2, 500, 7.5F, 2.0F, 10);
    public static ToolMaterial enumToolMaterialTitanium = EnumHelper.addToolMaterial(RefStrings.MODID + ":TITANIUM", 2, 750, 9.0F, 2.5F, 15);
    public static ToolMaterial enumToolMaterialAlloy = EnumHelper.addToolMaterial(RefStrings.MODID + ":ALLOY", 3, 2000, 15.0F, 5.0F, 5);
    public static ToolMaterial enumToolMaterialCmb = EnumHelper.addToolMaterial(RefStrings.MODID + ":CMB", 4, 8500, 40.0F, 55F, 100);
    public static ToolMaterial enumToolMaterialElec = EnumHelper.addToolMaterial(RefStrings.MODID + ":ELEC", 2, 0, 30.0F, 12.0F, 2);
    public static ToolMaterial enumToolMaterialDesh = EnumHelper.addToolMaterial(RefStrings.MODID + ":DESH", 2, 0, 7.5F, 2.0F, 10);
    public static ToolMaterial enumToolMaterialCobalt = EnumHelper.addToolMaterial(RefStrings.MODID + ":COBALT", 4, 750, 9.0F, 2.5F, 15);
    public static ToolMaterial enumToolMaterialSaw = EnumHelper.addToolMaterial(RefStrings.MODID + ":SAW", 2, 750, 2.0F, 3.5F, 25);
    public static ToolMaterial enumToolMaterialBat = EnumHelper.addToolMaterial(RefStrings.MODID + ":BAT", 0, 500, 1.5F, 3F, 25);
    public static ToolMaterial enumToolMaterialBatNail = EnumHelper.addToolMaterial(RefStrings.MODID + ":BATNAIL", 0, 450, 1.0F, 4F, 25);
    public static ToolMaterial enumToolMaterialGolfClub = EnumHelper.addToolMaterial(RefStrings.MODID + ":GOLFCLUB", 1, 1000, 2.0F, 5F, 25);
    public static ToolMaterial enumToolMaterialPipeRusty = EnumHelper.addToolMaterial(RefStrings.MODID + ":PIPERUSTY", 1, 350, 1.5F, 4.5F, 25);
    public static ToolMaterial enumToolMaterialPipeLead = EnumHelper.addToolMaterial(RefStrings.MODID + ":PIPELEAD", 1, 250, 1.5F, 5.5F, 25);
    public static ToolMaterial enumToolMaterialBottleOpener = EnumHelper.addToolMaterial(RefStrings.MODID + ":OPENER", 1, 250, 1.5F, 0.5F, 200);
    public static ToolMaterial enumToolMaterialSledge = EnumHelper.addToolMaterial(RefStrings.MODID + ":SHIMMERSLEDGE", 1, 0, 25.0F, 26F, 200);
    public static ToolMaterial enumToolMaterialMultitool = EnumHelper.addToolMaterial(RefStrings.MODID + ":MULTITOOL", 3, 5000, 25F, 5.5F, 25);
    public static ToolMaterial matMeteorite = EnumHelper.addToolMaterial("HBM_METEORITE", 4, 0, 50F, 0.0F, 200);
    public static ToolMaterial matCrucible = EnumHelper.addToolMaterial("CRUCIBLE", 3, 10000, 50.0F, 100.0F, 200);
    public static ToolMaterial matHS = EnumHelper.addToolMaterial("CRUCIBLE", 3, 10000, 50.0F, 100.0F, 200);
    public static ToolMaterial matHF = EnumHelper.addToolMaterial("CRUCIBLE", 3, 10000, 50.0F, 100.0F, 200);

    static {
        HBMSoundHandler.init();
        FluidRegistry.enableUniversalBucket();
    }

    Random rand = new Random();

    public static void reloadConfig() {
        Configuration config = new Configuration(new File(proxy.getDataDir().getPath() + "/config/hbm/hbm.cfg"));
        config.load();

        GeneralConfig.loadFromConfig(config);
        MachineConfig.loadFromConfig(config);
        BombConfig.loadFromConfig(config);
        RadiationConfig.loadFromConfig(config);
        PotionConfig.loadFromConfig(config);
        ToolConfig.loadFromConfig(config);
        WeaponConfig.loadFromConfig(config);
        MobConfig.loadFromConfig(config);
        SpaceConfig.loadFromConfig(config);
        reloadCompatConfig();
        BedrockOreJsonConfig.init();
        config.save();
    }

    public static void reloadCompatConfig() {
        Configuration config = new Configuration(new File(proxy.getDataDir().getPath() + "/config/hbm/hbm_dimensions.cfg"));
        config.load();
        CompatibilityConfig.loadFromConfig(config);
        config.save();
    }

    @EventHandler //Apparently this is "legacy", well I am not making my own protocol
    public static void initIMC(FMLInterModComms.IMCEvent event) {

        ImmutableList<FMLInterModComms.IMCMessage> inbox = event.getMessages();

        for (FMLInterModComms.IMCMessage message : inbox) {
            IMCHandler handler = IMCHandler.getHandler(message.key);

            if (handler != null) {
                MainRegistry.logger.info("Received IMC of type >" + message.key + "< from " + message.getSender() + "!");
                handler.process(message);
            } else {
                MainRegistry.logger.error("Could not process unknown IMC type \"" + message.key + "\"");
            }
        }
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        CrashHelper.init();

        if (logger == null)
            logger = event.getModLog();

        if (generalOverride > 0 && generalOverride < 19) {
            polaroidID = generalOverride;
        } else {
            polaroidID = rand.nextInt(18) + 1;
            while (polaroidID == 4 || polaroidID == 9)
                polaroidID = rand.nextInt(18) + 1;
        }

        configDir = event.getModConfigurationDirectory();
        configHbmDir = new File(configDir.getAbsolutePath() + File.separatorChar + "hbmConfig");

        if (!configHbmDir.exists()) configHbmDir.mkdir();

        if (SharedMonsterAttributes.MAX_HEALTH.clampValue(Integer.MAX_VALUE) <= 2000) {
            ((RangedAttribute) SharedMonsterAttributes.MAX_HEALTH).maximumValue = Integer.MAX_VALUE;
        }
        proxy.checkGLCaps();
        reloadConfig();

        OreDictManager.registerGroups();
        OreDictManager oreMan = new OreDictManager();

        MinecraftForge.EVENT_BUS.register(oreMan); //OreRegisterEvent

        MinecraftForge.EVENT_BUS.register(new ModEventHandler());
        MinecraftForge.TERRAIN_GEN_BUS.register(new ModEventHandler());
        MinecraftForge.ORE_GEN_BUS.register(new ModEventHandler());
        MinecraftForge.EVENT_BUS.register(new ModEventHandlerImpact());
        MinecraftForge.TERRAIN_GEN_BUS.register(new ModEventHandlerImpact());
        MinecraftForge.EVENT_BUS.register(new PollutionHandler());
        MinecraftForge.EVENT_BUS.register(new NeutronHandler());
        MinecraftForge.EVENT_BUS.register(new DamageResistanceHandler());

        if (event.getSide() == Side.CLIENT) {
            HbmKeybinds keyHandler = new HbmKeybinds();
            MinecraftForge.EVENT_BUS.register(keyHandler);
        }

        PacketDispatcher.registerPackets();

        HbmPotion.init();

        CapabilityManager.INSTANCE.register(HbmLivingCapability.IEntityHbmProps.class, new HbmLivingCapability.EntityHbmPropsStorage(), HbmLivingCapability.EntityHbmProps.FACTORY);
        CapabilityManager.INSTANCE.register(HbmCapability.IHBMData.class, new HbmCapability.HBMDataStorage(), HbmCapability.HBMData.FACTORY);
        Fluids.init();
        ModForgeFluids.init();
        ModItems.preInit();
        ModBlocks.preInit();
        BulletConfigSyncingUtil.loadConfigsForSync();
        CellularDungeonFactory.init();
        Satellite.register();
        HTTPHandler.loadStats();
        MultiblockBBHandler.init();
        ControlEvent.init();
        SiegeTier.registerTiers();
        HazardRegistry.registerItems();
        PotionRecipes.registerPotionRecipes();

        proxy.registerRenderInfo();
        HbmWorld.mainRegistry();
        proxy.preInit(event);
        Library.initSuperusers();

        enumArmorMaterialSchrabidium.setRepairItem(new ItemStack(ModItems.ingot_schrabidium));
        enumArmorMaterialHazmat.setRepairItem(new ItemStack(ModItems.hazmat_cloth));
        enumArmorMaterialHazmat2.setRepairItem(new ItemStack(ModItems.hazmat_cloth_red));
        enumArmorMaterialHazmat3.setRepairItem(new ItemStack(ModItems.hazmat_cloth_grey));
        enumArmorMaterialT45.setRepairItem(new ItemStack(ModItems.plate_titanium));
        aMatBJ.setRepairItem(new ItemStack(ModItems.plate_armor_lunar));
        aMatAJR.setRepairItem(new ItemStack(ModItems.plate_armor_ajr));
        aMatHEV.setRepairItem(new ItemStack(ModItems.plate_armor_hev));
        enumArmorMaterialTitanium.setRepairItem(new ItemStack(ModItems.ingot_titanium));
        enumArmorMaterialSteel.setRepairItem(new ItemStack(ModItems.ingot_steel));
        enumArmorMaterialAlloy.setRepairItem(new ItemStack(ModItems.ingot_advanced_alloy));
        enumArmorMaterialPaa.setRepairItem(new ItemStack(ModItems.plate_paa));
        enumArmorMaterialCmb.setRepairItem(new ItemStack(ModItems.ingot_combine_steel));
        enumArmorMaterialAusIII.setRepairItem(new ItemStack(ModItems.ingot_australium));
        enumArmorMaterialSecurity.setRepairItem(new ItemStack(ModItems.plate_kevlar));
        enumToolMaterialSchrabidium.setRepairItem(new ItemStack(ModItems.ingot_schrabidium));
        enumToolMaterialHammer.setRepairItem(new ItemStack(Item.getItemFromBlock(ModBlocks.block_schrabidium)));
        enumToolMaterialChainsaw.setRepairItem(new ItemStack(ModItems.ingot_steel));
        enumToolMaterialTitanium.setRepairItem(new ItemStack(ModItems.ingot_titanium));
        enumToolMaterialSteel.setRepairItem(new ItemStack(ModItems.ingot_steel));
        enumToolMaterialAlloy.setRepairItem(new ItemStack(ModItems.ingot_advanced_alloy));
        enumToolMaterialCmb.setRepairItem(new ItemStack(ModItems.ingot_combine_steel));
        enumToolMaterialBottleOpener.setRepairItem(new ItemStack(ModItems.plate_steel));
        enumToolMaterialDesh.setRepairItem(new ItemStack(ModItems.ingot_desh));
        enumArmorMaterialAsbestos.setRepairItem(new ItemStack(ModItems.asbestos_cloth));
        matMeteorite.setRepairItem(new ItemStack(ModItems.plate_paa));
        aMatLiquidator.setRepairItem(new ItemStack(ModItems.plate_lead));
        aMatFau.setRepairItem(new ItemStack(ModItems.plate_armor_fau));
        aMatDNS.setRepairItem(new ItemStack(ModItems.plate_armor_dnt));

        AutoRegistry.registerTileEntities();
        AutoRegistry.loadAuxiliaryData();
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());

        int i = 0;


        AutoRegistry.registerEntities(i);
        ForgeChunkManager.setForcedChunkLoadingCallback(this, new LoadingCallback() {

            @Override
            public void ticketsLoaded(List<Ticket> tickets, World world) {
                for (Ticket ticket : tickets) {

                    if (ticket.getEntity() instanceof IChunkLoader) {
                        ((IChunkLoader) ticket.getEntity()).init(ticket);
                    }
                }
            }
        });

        GrenadeDispenserRegistry.registerDispenserBehaviors();
        GrenadeDispenserRegistry.registerDispenserBehaviorFertilizer();
        TileEntityLaunchPadBase.registerLaunchables();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        //RodRecipes.registerInit();
        statMines = new StatBasic("stat.ntmMines", new TextComponentTranslation("stat.ntmMines")).registerStat();
        statBullets = new StatBasic("stat.ntmBullets", new TextComponentTranslation("stat.ntmBullets")).registerStat();
        ModItems.init();
        proxy.init(event);
        ModBlocks.init();
        HazmatRegistry.registerHazmats();
        ControlRegistry.init();
        OreDictManager.registerOres();
        Fluids.initForgeFluidCompat();
        PacketThreading.init();
        IMCHandler.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        ModItems.postInit();
        ModBlocks.postInit();
        ModBiomes.init();
        SolarSystem.init();
        PlanetGen.init();
        DamageResistanceHandler.init();
        BlockCrate.setDrops();
        BedrockOreRegistry.registerBedrockOres();
        ShredderRecipes.registerShredder();
        ShredderRecipes.registerOverrides();
        ExplosionNukeGeneric.loadSoliniumFromFile();
        CyclotronRecipes.register();
        HadronRecipes.register();
        MagicRecipes.register();
        SILEXRecipes.register();
        GasCentrifugeRecipes.register();
        NTMToolHandler.register();
        SerializableRecipe.registerAllHandlers();
        SerializableRecipe.initialize();
        AnvilRecipes.register();
        ClientConfig.initConfig();
        RefineryRecipes.registerRefinery();
        FluidContainerRegistry.register();
        TileEntityNukeCustom.registerBombItems();
        ArmorUtil.register();
        RBMKFuelRecipes.registerRecipes();
        DFCRecipes.register();
        SAFERecipes.registerRecipes();
        StorageDrumRecipes.registerRecipes();
        NuclearTransmutationRecipes.registerRecipes();
        EngineRecipes.registerEngineRecipes();
        FluidCombustionRecipes.registerFluidCombustionRecipes();
        HbmDetox.init();
        NTMBatteryCapabilityHandler.initialize();
        MinecraftForge.EVENT_BUS.register(PhasedStructureGenerator.INSTANCE);

        //has to register after cracking, and therefore after all serializable recipes
        RadiolysisRecipes.registerRadiolysis();

        ItemPoolConfigJSON.initialize();

        //Drillgon200: expand the max entity radius for the hunter chopper
        if (World.MAX_ENTITY_RADIUS < 5)
            World.MAX_ENTITY_RADIUS = 5;
        MinecraftForge.EVENT_BUS.register(new SchistStratum(ModBlocks.stone_gneiss.getDefaultState(), 0.01D, 5, 8, 30)); //DecorateBiomeEvent.Pre
        MinecraftForge.EVENT_BUS.register(new SchistStratum(ModBlocks.stone_resource.getDefaultState().withProperty(BlockResourceStone.META, BlockEnums.EnumStoneType.HEMATITE.ordinal()), 0.02D, 5.5, 5, 45)); //DecorateBiomeEvent.Pre

        if (event.getSide() == Side.CLIENT) {
            BedrockOreRegistry.registerOreColors();
            ModForgeFluids.registerFluidColors();
        }
        proxy.postInit(event);
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent evt) {
        RBMKDials.createDials(evt.getServer().getEntityWorld());
        evt.registerServerCommand(new CommandRadiation());
        evt.registerServerCommand(new CommandHbm());
        evt.registerServerCommand(new CommandSpaceTP());
        evt.registerServerCommand(new CommandPacketInfo());
        AdvancementManager.init(evt.getServer());
        //MUST be initialized AFTER achievements!!
        BobmazonOfferFactory.reset();
        BobmazonOfferFactory.init();
        RadiationSystemNT.onServerStarting(evt);
    }

    @EventHandler
    public void serverStopping(FMLServerStoppingEvent evt) {
        RadiationSystemNT.onServerStopping(evt);
    }
}
