package com.hbm.blocks;

import com.hbm.blocks.generic.BlockMeta;
import com.hbm.render.block.BlockBakeFrame;
import com.hbm.util.EnumUtil;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;


//DUDE a third implementation of fucking metablocks?? Yea no, we're extending BlockMeta
// Th3_Sl1ze: name me one, ONE fucking reason it should've been abstract this whole time
public class BlockEnumMeta extends BlockMeta {


    final public boolean multiName;
    final private boolean multiTexture;
    public Class<? extends Enum> blockEnum;

    public static final <E extends Enum<E>> IBlockState stateFromEnum(Block block, E anEnum) {
        return block.getDefaultState().withProperty(META, anEnum.ordinal());
    }

    public BlockEnumMeta(Material mat, SoundType type, String registryName, Class<? extends Enum> blockEnum, boolean multiName, boolean multiTexture) {
        super(mat, type, registryName, (short) blockEnum.getEnumConstants().length);
        this.blockEnum = blockEnum;
        this.multiName = multiName;
        this.multiTexture = multiTexture;
        this.blockFrames = generateBlockFrames(registryName);
    }

    protected BlockBakeFrame[] generateBlockFrames(String registryName) {
        return Arrays.stream(blockEnum.getEnumConstants())
                .sorted(Comparator.comparing(Enum::ordinal))
                .map(Enum::name)
                .map(name -> registryName + "." + name.toLowerCase(Locale.US))
                .map(BlockBakeFrame::new)
                .toArray(BlockBakeFrame[]::new);
    }


    @Override
    public void registerItem() {
        ItemBlock itemBlock = new EnumMetaBlockItem(this);
        itemBlock.setRegistryName(this.getRegistryName());
        itemBlock.setCreativeTab(this.getCreativeTab());
        ForgeRegistries.ITEMS.register(itemBlock);
    }

    public String enumToTranslationKey(Enum value) {
        return this.getTranslationKey() + "." + value.name().toLowerCase(Locale.US);
    }

    public Enum getEnumFromState(IBlockState state)
    {
        return this.blockEnum.getEnumConstants()[getMetaFromState(state)];
    }

    public class EnumMetaBlockItem extends MetaBlockItem {

        public EnumMetaBlockItem(Block block) {
            super(block);
        }

        public String getTranslationKey(ItemStack stack) {
            if (multiName) {
                Enum num = EnumUtil.grabEnumSafely(blockEnum, stack.getMetadata());
                return enumToTranslationKey(num);
            } else
                return this.block.getTranslationKey();
        }
    }
}
