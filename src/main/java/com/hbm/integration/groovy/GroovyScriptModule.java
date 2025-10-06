package com.hbm.integration.groovy;

import com.cleanroommc.groovyscript.api.GroovyPlugin;
import com.cleanroommc.groovyscript.compat.mods.GroovyContainer;
import com.cleanroommc.groovyscript.compat.mods.GroovyPropertyContainer;
import com.cleanroommc.groovyscript.documentation.linkgenerator.LinkGeneratorHooks;
import com.hbm.integration.groovy.script.*;
import com.hbm.lib.RefStrings;
import com.hbm.util.Compat;
import net.minecraftforge.fml.common.Optional;
import org.jetbrains.annotations.NotNull;

@Optional.Interface(iface = "com.cleanroommc.groovyscript.api.GroovyPlugin", modid = Compat.ModIds.GROOVY_SCRIPT)
public class GroovyScriptModule implements GroovyPlugin {
    public static final AnvilSmithing ANVILSMITHING = new AnvilSmithing();
    public static final AnvilConstruction ANVILCONSTRUCTION = new AnvilConstruction();
    public static final Assembler ASSEMBLER = new Assembler();
    public static final Press PRESS = new Press();
    public static final BlastFurnaceFuel BLASTFURNACEFUEL = new BlastFurnaceFuel();
    public static final BlastFurnace BLASTFURNACE = new BlastFurnace();
    public static final Shredder SHREDDER = new Shredder();
    public static final Bobmazon BOBMAZON = new Bobmazon();
    public static final BreedingReactor BREEDINGREACTOR = new BreedingReactor();
    public static final Centrifuge CENTRIFUGE = new Centrifuge();
    public static final DFC DFC = new DFC();
    public static final SILEX SILEX = new SILEX();
    public static final IrradiationChannel IRRADIATIONCHANNEL = new IrradiationChannel();
    public static final WasteDrum WASTEDRUM = new WasteDrum();

    @Override
    public @NotNull String getModId() {
        return RefStrings.MODID;
    }

    @Override
    public @NotNull String getContainerName() {
        return RefStrings.NAME;
    }

    @Override
    public GroovyPropertyContainer createGroovyPropertyContainer() {
        return new HbmGroovyPropertyContainer();
    }

    @Override
    public void onCompatLoaded(GroovyContainer<?> groovyContainer) {
        groovyContainer.addPropertiesOfFields(this, true);
        LinkGeneratorHooks.registerLinkGenerator(new NTMLinkGenerator());
    }
}
