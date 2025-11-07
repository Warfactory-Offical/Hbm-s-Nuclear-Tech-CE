package com.hbm.render.model;

import com.hbm.blocks.BlockEnumMeta;
import com.hbm.hfr.render.loader.HFRWavefrontObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

@SideOnly(Side.CLIENT)
public class BlockDecoBakedModel extends AbstractWavefrontBakedModel {

    private final TextureAtlasSprite sprite;
    private final boolean forBlock; // true: world block model, false: item/inventory model

    private final Map<EnumFacing, List<BakedQuad>> cacheByFacing = new EnumMap<>(EnumFacing.class);
    private List<BakedQuad> itemQuads;

    public BlockDecoBakedModel(HFRWavefrontObject model, TextureAtlasSprite sprite, boolean forBlock, float baseScale, float tx, float ty, float tz) {
        super(model, DefaultVertexFormats.ITEM, baseScale, tx, ty, tz, BakedModelTransforms.forDeco(BakedModelTransforms.standardBlock()));
        this.sprite = sprite;
        this.forBlock = forBlock;
    }

    public static BlockDecoBakedModel forBlock(HFRWavefrontObject model, TextureAtlasSprite sprite) {
        return new BlockDecoBakedModel(model, sprite, true, 1.0F, 0.0F, 0.0F, 0.0F);
    }

    public static BlockDecoBakedModel forBlock(HFRWavefrontObject model, TextureAtlasSprite sprite, float ty) {
        return new BlockDecoBakedModel(model, sprite, true, 1.0F, 0.0F, ty, 0.0F);
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (side != null) return Collections.emptyList();

        if (forBlock) {
            EnumFacing facing = EnumFacing.SOUTH;
            if (state != null && state.getPropertyKeys().contains(BlockEnumMeta.META)) {
                int meta = state.getValue(BlockEnumMeta.META);
                int orient = (meta >> 2) & 3;
                switch (orient) {
                    case 0 -> facing = EnumFacing.NORTH;
                    case 1 -> {}
                    case 2 -> facing = EnumFacing.WEST;
                    case 3 -> facing = EnumFacing.EAST;
                }
            }
            return cacheByFacing.computeIfAbsent(facing, this::buildQuadsForFacing);
        }

        if (itemQuads == null) {
            itemQuads = buildItemQuads();
        }
        return itemQuads;
    }

    private List<BakedQuad> buildQuadsForFacing(EnumFacing facing) {
        float yaw = switch (facing) {
            case NORTH -> (float) Math.PI;
            case WEST -> 1.5F * (float) Math.PI;
            case EAST -> 0.5F * (float) Math.PI;
            default -> 0.0F;
        };
        // World: shadow enabled, center model to block (+0.5)
        return super.bakeSimpleQuads(null, 0.0F, 0.0F, yaw, true, true, sprite);
    }

    private List<BakedQuad> buildItemQuads() {
        // Item: no shadow, no centering (+0.5), but apply base scale and translation
        return super.bakeSimpleQuads(null, 0.0F, 0.0F, 0.0F, false, false, sprite);
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return sprite;
    }
}
