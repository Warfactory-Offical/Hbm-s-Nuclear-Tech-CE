package com.hbm.blocks.generic;

import com.google.common.collect.ImmutableMap;
import com.hbm.blocks.ICustomBlockItem;
import com.hbm.items.IDynamicModels;
import com.hbm.items.IModelRegister;
import com.hbm.render.block.BlockBakeFrame;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

public class BlockStairsEnumMeta extends BlockGenericStairs implements ICustomBlockItem {

    public static final PropertyInteger META = PropertyInteger.create("meta", 0, 15);

    public final short META_COUNT;
    public final boolean multiName;
    public final boolean multiTexture;
    public final Class<? extends Enum<?>> blockEnum;

    protected BlockBakeFrame[] blockFrames;
    protected boolean showMetaInCreative = true;

    public BlockStairsEnumMeta(Block block, SoundType sound, String registryName, Class<? extends Enum<?>> blockEnum, boolean multiName, boolean multiTexture) {
        super(block, registryName);
        this.setSoundType(sound);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);

        this.blockEnum = blockEnum;
        this.multiName = multiName;
        this.multiTexture = multiTexture;
        this.META_COUNT = (short) blockEnum.getEnumConstants().length;

        this.blockFrames = generateBlockFrames(registryName);

        IDynamicModels.INSTANCES.add(this);
    }


    protected BlockBakeFrame[] generateBlockFrames(String registryName) {
        return Arrays.stream(blockEnum.getEnumConstants())
                .sorted(Comparator.comparing(Enum::ordinal))
                .map(Enum::name)
                .map(name -> {
                    final String path = registryName + "." + name.toLowerCase(Locale.US);
                    final String sprite = "hbm:blocks/" + path;
                    return new BlockBakeFrame(path) {
                        @Override
                        public void putTextures(ImmutableMap.Builder<String, String> builder) {
                            builder.put("bottom", sprite);
                            builder.put("top", sprite);
                            builder.put("side", sprite);
                            builder.put("particle", sprite);
                        }
                    };
                })
                .toArray(BlockBakeFrame[]::new);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        int meta = stack.getMetadata() % META_COUNT;
        world.setBlockState(pos, state.withProperty(META, meta), 2);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        state = super.getActualState(state, worldIn, pos);
        int meta = 0;
        if (state.getProperties().containsKey(META)) {
            meta = state.getValue(META);
        }
        return state.withProperty(META, meta);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, HALF, SHAPE, META);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(META);
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        IBlockState actual = this.getActualState(state, world, pos);
        return Collections.singletonList(new ItemStack(Item.getItemFromBlock(this), 1, actual.getValue(META)));
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        IBlockState actual = this.getActualState(state, world, pos);
        return new ItemStack(Item.getItemFromBlock(this), 1, actual.getValue(META));
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int meta,
                                            EntityLivingBase placer, EnumHand hand) {
        IBlockState base = super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer, hand);
        return base.withProperty(META, meta % META_COUNT);
    }

    public String enumToTranslationKey(Enum<?> value) {
        return this.getTranslationKey() + "." + value.name().toLowerCase(Locale.US);
    }

    @Override
    public void registerItem() {
        ItemBlock itemBlock = new EnumMetaBlockItem(this);
        itemBlock.setRegistryName(this.getRegistryName());
        if (showMetaInCreative) itemBlock.setCreativeTab(this.getCreativeTab());
        ForgeRegistries.ITEMS.register(itemBlock);
    }

    public class EnumMetaBlockItem extends ItemBlock implements IModelRegister {
        public EnumMetaBlockItem(Block block) {
            super(block);
            this.setHasSubtypes(true);
            this.canRepair = false;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {
            if (this.isInCreativeTab(tab)) {
                for (int i = 0; i < META_COUNT; i++) {
                    list.add(new ItemStack(this, 1, i));
                }
            }
        }

        @Override
        public String getTranslationKey(ItemStack stack) {
            if (multiName) {
                int meta = stack.getMetadata() % META_COUNT;
                Enum<?> val = blockEnum.getEnumConstants()[meta];
                return enumToTranslationKey(val);
            } else {
                return this.block.getTranslationKey();
            }
        }

        @Override
        public void registerModels() {
            for (int meta = 0; meta < META_COUNT; meta++) {
                ModelLoader.setCustomModelResourceLocation(this, meta,
                        new ModelResourceLocation(getRegistryName(), "meta=" + meta));
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel() {
        ModelLoader.setCustomStateMapper(this, getStateMapper(this.getRegistryName()));
        for (int meta = 0; meta < META_COUNT; meta++) {
            ModelLoader.setCustomModelResourceLocation(
                    Item.getItemFromBlock(this),
                    meta,
                    new ModelResourceLocation(this.getRegistryName(), "meta=" + meta)
            );
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        if (blockFrames == null || blockFrames.length == 0) {
            throw new RuntimeException("No block frames defined for " + getRegistryName());
        }
        for (BlockBakeFrame frame : blockFrames) {
            frame.registerBlockTextures(map);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        IModel baseStraight;
        IModel baseInner;
        IModel baseOuter;
        try {
            baseStraight = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft:block/stairs"));
            baseInner = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft:block/inner_stairs"));
            baseOuter = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft:block/outer_stairs"));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        for (int meta = 0; meta < META_COUNT; meta++) {
            BlockBakeFrame blockFrame = blockFrames[meta % blockFrames.length];
            bakeStairs(event, blockFrame, baseStraight, baseInner, baseOuter, meta, true);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public StateMapperBase getStateMapper(ResourceLocation loc) {
        return new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                int meta = state.getValue(META);
                if (meta >= META_COUNT) meta = 0;
                EnumFacing facing = state.getValue(FACING);
                EnumHalf half = state.getValue(HALF);
                EnumShape shape = state.getValue(SHAPE);
                return new ModelResourceLocation(loc,
                        "meta=" + meta + ",half=" + half.getName() + ",facing=" + facing.getName() + ",shape=" + shape.getName());
            }
        };
    }
}
