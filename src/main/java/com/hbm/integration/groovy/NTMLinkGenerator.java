package com.hbm.integration.groovy;

import com.cleanroommc.groovyscript.documentation.linkgenerator.BasicLinkGenerator;
import com.hbm.Tags;
import com.hbm.lib.RefStrings;

public class NTMLinkGenerator extends BasicLinkGenerator {
    @Override
    public String id() {
        return RefStrings.MODID;
    }

    @Override
    protected String version() {
        return RefStrings.VERSION;
    }

    @Override
    protected String domain() {
        return "https://github.com/MisterNorwood/Hbm-s-Nuclear-Tech-CE/";
    }
}
