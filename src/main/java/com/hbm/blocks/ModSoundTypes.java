package com.hbm.blocks;

import com.hbm.lib.HBMSoundHandler;
import net.minecraft.block.SoundType;

import java.util.Random;

public class ModSoundTypes {

    public static final ModSoundType grate = ModSoundType.customStep(SoundType.STONE, HBMSoundHandler.metalBlock, 0.5F, 1.0F);
    public static final ModSoundType pipe = ModSoundType.customDig(SoundType.METAL, HBMSoundHandler.pipePlaced, 0.85F, 0.85F).enveloped(new Random()).pitchFunction((in, rand, type) -> {
        if(type == ModSoundType.SubType.BREAK) in -= 0.15F;
        return in + rand.nextFloat() * 0.2F;
    });
}
