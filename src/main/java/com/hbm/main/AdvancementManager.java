package com.hbm.main;

import com.hbm.lib.RefStrings;
import net.minecraft.advancements.Advancement;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;

public class AdvancementManager {

    public static Advancement achSacrifice;
    public static Advancement achImpossible;
    public static Advancement achTOB;
    public static Advancement achPotato;
    public static Advancement achC20_5;
    public static Advancement achFiend;
    public static Advancement achFiend2;
    public static Advancement achRadPoison;
    public static Advancement achRadDeath;
    public static Advancement achStratum;
    public static Advancement achOmega12;
    public static Advancement achSomeWounds;
    public static Advancement achSlimeball;
    public static Advancement achSulfuric;
    public static Advancement achGoFish;
    public static Advancement achNo9;
    public static Advancement achInferno;
    public static Advancement achRedRoom;
    public static Advancement bobHidden;
    public static Advancement horizonsStart;
    public static Advancement horizonsEnd;
    public static Advancement horizonsBonus;
    public static Advancement bossCreeper;
    public static Advancement bossMeltdown;
    public static Advancement bossMaskman;
    public static Advancement bossWorm;
    public static Advancement bossUFO;
    public static Advancement digammaSee;
    public static Advancement digammaFeel;
    public static Advancement digammaKnow;
    public static Advancement digammaKauaiMoho;
    public static Advancement digammaUpOnTop;

    public static Advancement achBurnerPress;
    public static Advancement achBlastFurnace;
    public static Advancement achAssembly;
    public static Advancement achSelenium;
    public static Advancement achChemplant;
    public static Advancement achConcrete;
    public static Advancement achPolymer;
    public static Advancement achDesh;
    public static Advancement achTantalum;
    public static Advancement achRedBalloons;
    public static Advancement achManhattan;
    public static Advancement achGasCent;
    public static Advancement achCentrifuge;
    public static Advancement achFOEQ;
    public static Advancement achSoyuz;
    public static Advancement achSpace;
    public static Advancement achSchrab;
    public static Advancement achAcidizer;
    public static Advancement achRadium;
    public static Advancement achTechnetium;
    public static Advancement achZIRNOXBoom;
    public static Advancement achChicagoPile;
    public static Advancement achSILEX;
    public static Advancement achWatz;
    public static Advancement achWatzBoom;
    public static Advancement achRBMK;
    public static Advancement achRBMKBoom;
    public static Advancement achBismuth;
    public static Advancement achBreeding;
    public static Advancement achFusion;
    public static Advancement achMeltdown;

    public static Advancement achDriveFail;
    public static Advancement progress_dfc;
    public static Advancement progress_rbmk_boom;
    public static Advancement root;

    public static void init(MinecraftServer serv) {
        net.minecraft.advancements.AdvancementManager adv = serv.getAdvancementManager();

        achSacrifice   = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achsacrifice"));
        achImpossible  = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achimpossible"));
        achTOB         = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achtob"));
        achGoFish      = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achgofish"));
        achPotato      = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achpotato"));
        achC20_5       = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achc20_5"));
        achFiend       = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achfiend"));
        achFiend2      = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achfiend2"));
        achStratum     = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achstratum"));
        achOmega12     = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achomega12"));

        achNo9         = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achno9")); //TODO mov, make another .json
        achSlimeball   = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achslimeball"));
        achSulfuric    = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achsulfuric"));
        achInferno     = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achinferno"));
        achRedRoom     = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achredroom"));

        bobHidden      = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "bobhidden"));

        horizonsStart  = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "horizonsstart"));
        horizonsEnd    = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "horizonsend"));
        horizonsBonus  = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "horizonsbonus"));

        bossCreeper    = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "bosscreeper"));
        bossMeltdown   = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "bossmeltdown")); //TODO
        bossMaskman    = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "bossmaskman"));
        bossWorm       = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "bossworm"));
        bossUFO        = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "bossufo"));

        achRadPoison   = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achradpoison"));
        achRadDeath    = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achraddeath"));

        achSomeWounds  = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achsomewounds"));

        digammaSee       = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "digammasee"));
        digammaFeel      = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "digammafeel"));
        digammaKnow      = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "digammaknow"));
        digammaKauaiMoho = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "digammakauaimoho"));
        digammaUpOnTop   = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "digammaupontop"));

        // Progression
        achBurnerPress  = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achburnerpress"));
        achBlastFurnace = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achblastfurnace"));
        achAssembly     = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achassembly"));
        achSelenium     = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achselenium"));
        achChemplant    = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achchemplant"));
        achConcrete     = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achconcrete"));
        achPolymer      = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achpolymer"));
        achDesh         = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achdesh"));
        achTantalum     = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achtantalum"));
        achGasCent      = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achgascent"));
        achCentrifuge   = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achcentrifuge"));
        achFOEQ         = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achfoeq"));
        achSoyuz        = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achsoyuz"));
        achSpace        = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achspace"));
        achSchrab       = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achschrab"));
        achAcidizer     = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achacidizer"));
        achRadium       = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achradium"));
        achTechnetium   = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achtechnetium"));
        achZIRNOXBoom   = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achzirnoxboom"));
        achChicagoPile  = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achchicagopile"));
        achSILEX        = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achsilex"));
        achWatz         = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achwatz"));
        achWatzBoom     = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achwatzboom"));
        achRBMK         = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achrbmk"));
        achRBMKBoom     = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achrbmkboom"));
        achBismuth      = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achbismuth"));
        achBreeding     = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achbreeding"));
        achFusion       = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achfusion"));
        achMeltdown     = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achmeltdown"));
        achRedBalloons  = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achredballoons"));
        achManhattan    = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achmanhattan"));

        achDriveFail      = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "achdrivefail")); // NTM SPACE
        progress_dfc      = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "progress_dfc")); // 1.12.2 exclusive, kept because why not?
        // TODO: Maybe add an achievement for SAFE
        // not really, it got removed
        root              = adv.getAdvancement(new ResourceLocation(RefStrings.MODID, "root")); // 1.12.2 Root advancement
    }

    public static void grantAchievement(EntityPlayerMP player, Advancement a) {
        if (a == null) {
            MainRegistry.logger.log(Level.ERROR, "Failed to grant null advancement! This should never happen.");
            return;
        }
        for (String s : player.getAdvancements().getProgress(a).getRemaningCriteria()) {
            player.getAdvancements().grantCriterion(a, s);
        }
    }

    @Deprecated
    public static void grantAchievement(EntityPlayer player, Advancement a) {
        if (player instanceof EntityPlayerMP) grantAchievement((EntityPlayerMP) player, a);
    }

    public static boolean hasAdvancement(EntityPlayer player, Advancement a) {
        if (a == null) {
            MainRegistry.logger.log(Level.ERROR, "Failed to test null advancement! This should never happen.");
            return false;
        }
        if (player instanceof EntityPlayerMP) {
            return ((EntityPlayerMP) player).getAdvancements().getProgress(a).isDone();
        }
        return false;
    }
}
