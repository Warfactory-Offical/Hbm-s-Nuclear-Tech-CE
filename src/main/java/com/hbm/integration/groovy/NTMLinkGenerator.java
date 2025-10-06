package com.hbm.integration.groovy;

import com.cleanroommc.groovyscript.documentation.linkgenerator.BasicLinkGenerator;
import com.hbm.Tags;

public class NTMLinkGenerator extends BasicLinkGenerator {
    @Override
    public String id() {
        return "hbm";
    }

    @Override
    protected String version() {
        return "v"+ Tags.MOD_ID;
    }

    @Override
    protected String domain() {
        return "https://github.com/MisterNorwood/Hbm-s-Nuclear-Tech-CE/";
    }
}
