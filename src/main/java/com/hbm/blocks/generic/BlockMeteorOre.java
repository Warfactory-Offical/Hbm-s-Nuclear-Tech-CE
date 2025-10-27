package com.hbm.blocks.generic;

import com.hbm.blocks.BlockEnumMeta;
import com.hbm.blocks.BlockEnums;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class BlockMeteorOre extends BlockEnumMeta {

    public BlockMeteorOre() {
        super(Material.ROCK, SoundType.STONE, "ore_meteor", BlockEnums.EnumMeteorType.class, true, true);
    }
}
