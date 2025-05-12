package com.hbm.inventory.fluid;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

public class ForgeFluidNTM extends Fluid {
    public ForgeFluidNTM(String ffName, ResourceLocation textureStill, ResourceLocation textureFlowing, int color) {
        super(ffName, textureStill, textureFlowing, color);
    }

    public String getUnlocalizedName() {
        return "hbmfluid." +this.getName();
    }
}

