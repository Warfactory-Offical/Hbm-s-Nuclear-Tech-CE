package com.hbm.blocks.network;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hbm.api.block.IToolable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ModSoundTypes;
import com.hbm.blocks.generic.BlockBakeBase;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.ICopiable;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.BlockBakeFrame;
import com.hbm.render.model.BakedModelTransforms;
import com.hbm.tileentity.network.TileEntityPipeBaseNT;
import com.hbm.util.I18nUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FluidDuctPaintable extends BlockBakeBase implements IToolable, ILookOverlay {

    public static final IUnlistedProperty<IBlockState> DISGUISED_STATE = new SimpleUnlistedProperty<>("disguised_state", IBlockState.class);

    @SideOnly(Side.CLIENT)
    private static TextureAtlasSprite baseSprite;
    @SideOnly(Side.CLIENT)
    private static TextureAtlasSprite overlaySprite;
    @SideOnly(Side.CLIENT)
    private static TextureAtlasSprite overlayColorSprite;

    public FluidDuctPaintable(String name) {
        super(Material.IRON, name, new BlockBakeFrame("fluid_duct_paintable"));
        this.setDefaultState(this.blockState.getBaseState());
        this.setSoundType(ModSoundTypes.pipe);
        this.useNeighborBrightness = true;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[0], new IUnlistedProperty[]{DISGUISED_STATE});
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityPipePaintable();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean causesSuffocation(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.isEmpty()) {
            return super.onBlockActivated(worldIn, pos, state, player, hand, facing, hitX, hitY, hitZ);
        }

        if (!(stack.getItem() instanceof ItemBlock ib)) {
            return super.onBlockActivated(worldIn, pos, state, player, hand, facing, hitX, hitY, hitZ);
        }

        Block disguise = ib.getBlock();

        if (disguise == this) {
            return super.onBlockActivated(worldIn, pos, state, player, hand, facing, hitX, hitY, hitZ);
        }

        IBlockState disguiseState = disguise.getStateFromMeta(stack.getMetadata());
        if (!disguiseState.isFullCube() || !disguiseState.isOpaqueCube()) {
            return super.onBlockActivated(worldIn, pos, state, player, hand, facing, hitX, hitY, hitZ);
        }

        TileEntity tile = worldIn.getTileEntity(pos);
        if (!(tile instanceof TileEntityPipePaintable pipe)) {
            return super.onBlockActivated(worldIn, pos, state, player, hand, facing, hitX, hitY, hitZ);
        }

        if (pipe.block != null) {
            return super.onBlockActivated(worldIn, pos, state, player, hand, facing, hitX, hitY, hitZ);
        }

        if (!worldIn.isRemote) {
            pipe.block = disguise;
            pipe.meta = stack.getMetadata() & 15;
            pipe.markDirty();
            worldIn.markChunkDirty(pos, pipe);
            worldIn.notifyBlockUpdate(pos, state, state, 3);
        }

        return true;
    }

    @Override
    public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool) {
        BlockPos pos = new BlockPos(x, y, z);
        if (tool != ToolType.SCREWDRIVER) {
            return false;
        }

        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileEntityPipePaintable pipe)) {
            return false;
        }

        if (pipe.block == null) {
            return false;
        }

        if (!world.isRemote) {
            pipe.block = null;
            pipe.meta = 0;
            pipe.markDirty();
            world.markChunkDirty(pos, pipe);
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }

        return true;
    }

    @Override
    public void printHook(RenderGameOverlayEvent.Pre event, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        if (!(te instanceof TileEntityPipeBaseNT pipe)) {
            return;
        }

        List<String> text = new ArrayList<>();
        text.add("&[" + pipe.getType().getColor() + "&]" + pipe.getType().getLocalizedName());
        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(this.getTranslationKey() + ".name"), 0xFFFF00, 0x404000, text);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        super.registerSprite(map);
        baseSprite = map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/fluid_duct_paintable"));
        overlaySprite = map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/fluid_duct_paintable_overlay"));
        overlayColorSprite = map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/fluid_duct_paintable_color"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        TextureAtlasSprite base = baseSprite != null ? baseSprite : Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(new ResourceLocation(RefStrings.MODID, "blocks/fluid_duct_paintable").toString());
        TextureAtlasSprite overlay = overlaySprite != null ? overlaySprite : Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(new ResourceLocation(RefStrings.MODID, "blocks/fluid_duct_paintable_overlay").toString());
        TextureAtlasSprite overlayColor = overlayColorSprite != null ? overlayColorSprite : Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(new ResourceLocation(RefStrings.MODID, "blocks/fluid_duct_paintable_color").toString());

        FluidDuctPaintableModel model = new FluidDuctPaintableModel(base, overlay, overlayColor);
        ModelResourceLocation inventory = new ModelResourceLocation(Objects.requireNonNull(getRegistryName()), "inventory");
        ModelResourceLocation normal = new ModelResourceLocation(Objects.requireNonNull(getRegistryName()), "normal");

        event.getModelRegistry().putObject(inventory, model);
        event.getModelRegistry().putObject(normal, model);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IBlockColor getBlockColorHandler() {
        return (state, world, pos, tintIndex) -> {
            if (tintIndex != 1) {
                return 0xFFFFFF;
            }

            if (world == null || pos == null) {
                return 0xFFFFFF;
            }

            TileEntity tile = world.getTileEntity(pos);
            if (!(tile instanceof TileEntityPipePaintable pipe)) {
                return 0xFFFFFF;
            }

            if (pipe.block != null) {
                return 0xFFFFFF;
            }

            FluidType type = pipe.getType();
            return type != null ? type.getColor() : 0xFFFFFF;
        };
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (!(state instanceof IExtendedBlockState extState)) {
            return state;
        }

        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityPipePaintable pipe && pipe.block != null) {
            IBlockState disguiseState = pipe.block.getStateFromMeta(pipe.meta);
            return extState.withProperty(DISGUISED_STATE, disguiseState);
        }

        return extState.withProperty(DISGUISED_STATE, null);
    }
    @AutoRegister
    public static class TileEntityPipePaintable extends TileEntityPipeBaseNT implements ICopiable {

        public Block block;
        public int meta;
        private Block lastBlock;
        private int lastMeta;

        @Override
        public void update() {
            super.update();

            if (world != null && world.isRemote) {
                if (block != lastBlock || meta != lastMeta) {
                    world.markBlockRangeForRenderUpdate(pos, pos);
                    lastBlock = block;
                    lastMeta = meta;
                }
            }
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            super.readFromNBT(nbt);

            if (nbt.hasKey("block", Constants.NBT.TAG_STRING)) {
                ResourceLocation loc = new ResourceLocation(nbt.getString("block"));
                Block stored = ForgeRegistries.BLOCKS.getValue(loc);
                this.block = stored;
            } else {
                this.block = null;
            }

            this.meta = nbt.getInteger("meta");
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            super.writeToNBT(nbt);

            if (block != null) {
                ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block);
                if (key != null) {
                    nbt.setString("block", key.toString());
                }
            }

            nbt.setInteger("meta", meta);
            return nbt;
        }

        @Override
        public SPacketUpdateTileEntity getUpdatePacket() {
            NBTTagCompound nbt = new NBTTagCompound();
            this.writeToNBT(nbt);
            return new SPacketUpdateTileEntity(this.pos, 0, nbt);
        }

        @Override
        public NBTTagCompound getUpdateTag() {
            NBTTagCompound nbt = super.getUpdateTag();
            this.writeToNBT(nbt);
            return nbt;
        }

        @Override
        public NBTTagCompound getSettings(World world, int x, int y, int z) {
            NBTTagCompound nbt = new NBTTagCompound();
            if (block != null) {
                ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block);
                if (key != null) {
                    nbt.setString("paintblock", key.toString());
                    nbt.setInteger("paintmeta", meta);
                }
            }
            return nbt;
        }

        @Override
        public void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, int x, int y, int z) {
            if (nbt.hasKey("paintblock", Constants.NBT.TAG_STRING)) {
                ResourceLocation key = new ResourceLocation(nbt.getString("paintblock"));
                Block stored = ForgeRegistries.BLOCKS.getValue(key);
                this.block = stored;
                this.meta = nbt.getInteger("paintmeta");
                if (world != null && world.isRemote) {
                    this.lastBlock = null;
                }
                this.markDirty();
                if (world != null) {
                    world.markChunkDirty(pos, this);
                    IBlockState state = world.getBlockState(pos);
                    world.notifyBlockUpdate(pos, state, state, 3);
                }
            }
        }
    }
    // Th3_Sl1ze: mov, you're free to make this extend AbstractBakedModel as well as other paintable blocks, my goal is to make it work
    // I bet it can be done like 5x more elegant than that
    @SideOnly(Side.CLIENT)
    public static class FluidDuctPaintableModel implements IBakedModel {

        private static final FaceBakery FACE_BAKERY = new FaceBakery();
        private final TextureAtlasSprite particle;
        private final ImmutableMap<EnumFacing, ImmutableList<BakedQuad>> baseFaces;
        private final ImmutableMap<EnumFacing, ImmutableList<BakedQuad>> overlayFaces;
        private final ImmutableMap<EnumFacing, ImmutableList<BakedQuad>> overlayTintFaces;
        private final ImmutableList<BakedQuad> baseGeneral;
        private final ImmutableList<BakedQuad> overlayGeneral;
        private final ImmutableList<BakedQuad> overlayTintGeneral;

        public FluidDuctPaintableModel(TextureAtlasSprite base, TextureAtlasSprite overlay, TextureAtlasSprite overlayTint) {
            this.particle = base;
            this.baseFaces = buildFaceMap(base, -1, false);
            this.overlayFaces = buildFaceMap(overlay, -1, true);
            this.overlayTintFaces = buildFaceMap(overlayTint, 1, true);
            this.baseGeneral = flatten(this.baseFaces);
            this.overlayGeneral = flatten(this.overlayFaces);
            this.overlayTintGeneral = flatten(this.overlayTintFaces);
        }

        @Override
        public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
            List<BakedQuad> quads = new ArrayList<>();
            IBlockState disguiseState = null;

            if (state instanceof IExtendedBlockState ext) {
                disguiseState = ext.getValue(DISGUISED_STATE);
            }

            boolean disguised = disguiseState != null;

            if (disguised) {
                IBakedModel disguiseModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(disguiseState);
                quads.addAll(disguiseModel.getQuads(disguiseState, side, rand));
            } else {
                if (side == null) {
                    quads.addAll(baseGeneral);
                } else {
                    quads.addAll(baseFaces.get(side));
                }
            }

            if (disguised) {
                if (side == null) {
                    quads.addAll(overlayGeneral);
                } else {
                    quads.addAll(overlayFaces.get(side));
                }
            } else {
                if (side == null) {
                    quads.addAll(overlayTintGeneral);
                } else {
                    quads.addAll(overlayTintFaces.get(side));
                }
            }

            return quads;
        }

        @Override
        public boolean isAmbientOcclusion() {
            return true;
        }

        @Override
        public boolean isGui3d() {
            return true;
        }

        @Override
        public boolean isBuiltInRenderer() {
            return false;
        }

        @Override
        public TextureAtlasSprite getParticleTexture() {
            return particle;
        }

        @Override
        public ItemCameraTransforms getItemCameraTransforms() {
            return BakedModelTransforms.standardBlock();
        }

        @Override
        public ItemOverrideList getOverrides() {
            return ItemOverrideList.NONE;
        }

        private static ImmutableMap<EnumFacing, ImmutableList<BakedQuad>> buildFaceMap(TextureAtlasSprite sprite, int tintIndex, boolean offset) {
            ImmutableMap.Builder<EnumFacing, ImmutableList<BakedQuad>> builder = ImmutableMap.builder();
            for (EnumFacing face : EnumFacing.values()) {
                builder.put(face, ImmutableList.of(createQuad(face, sprite, tintIndex, offset)));
            }
            return builder.build();
        }

        private static ImmutableList<BakedQuad> flatten(Map<EnumFacing, ImmutableList<BakedQuad>> map) {
            ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
            for (EnumFacing face : EnumFacing.values()) {
                builder.addAll(map.get(face));
            }
            return builder.build();
        }

        private static BakedQuad createQuad(EnumFacing face, TextureAtlasSprite sprite, int tintIndex, boolean offset) {
            float eps = 0.001F;
            Vector3f from = new Vector3f(0F, 0F, 0F);
            Vector3f to = new Vector3f(16F, 16F, 16F);

            if (offset) {
                switch (face) {
                    case DOWN -> from.setY(-eps);
                    case UP -> to.setY(16F + eps);
                    case NORTH -> from.setZ(-eps);
                    case SOUTH -> to.setZ(16F + eps);
                    case WEST -> from.setX(-eps);
                    case EAST -> to.setX(16F + eps);
                }
            }

            BlockFaceUV uv = new BlockFaceUV(new float[]{0F, 0F, 16F, 16F}, 0);
            BlockPartFace partFace = new BlockPartFace(null, tintIndex, "", uv);
            return FACE_BAKERY.makeBakedQuad(from, to, partFace, sprite, face, ModelRotation.X0_Y0, null, false, true);
        }
    }
}
