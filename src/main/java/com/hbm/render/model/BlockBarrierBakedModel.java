package com.hbm.render.model;

import com.hbm.blocks.generic.BlockBarrier;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class BlockBarrierBakedModel extends AbstractBakedModel {

    private final TextureAtlasSprite sprite;
    private final boolean isInventory;

    public BlockBarrierBakedModel(TextureAtlasSprite sprite, boolean isInventory) {
        super(BakedModelTransforms.standardBlock());
        this.sprite = sprite;
        this.isInventory = isInventory;
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (side != null) return Collections.emptyList();

        List<BakedQuad> quads = new ArrayList<>();

        if (isInventory) {
            // Inventory representation: center post + two cross bars
            addBox(quads, 0.4375f, 0.0f, 0.4375f, 0.5625f, 1.0f, 0.5625f, sprite);
            addBox(quads, 0.5f, 0.0625f, 0.0f, 0.5625f, 0.4375f, 1.0f, sprite);
            addBox(quads, 0.5f, 0.5625f, 0.0f, 0.5625f, 0.9375f, 1.0f, sprite);
            return quads;
        }

        boolean negX = false;
        boolean posX = false;
        boolean negZ = false;
        boolean posZ = false;
        boolean posY = false;

        if (state instanceof IExtendedBlockState ext) {
            Boolean b;

            b = ext.getValue(BlockBarrier.CONN_NEG_X);
            negX = b != null && b;

            b = ext.getValue(BlockBarrier.CONN_POS_X);
            posX = b != null && b;

            b = ext.getValue(BlockBarrier.CONN_NEG_Z);
            negZ = b != null && b;

            b = ext.getValue(BlockBarrier.CONN_POS_Z);
            posZ = b != null && b;

            b = ext.getValue(BlockBarrier.CONN_POS_Y);
            posY = b != null && b;
        }

        // Replicate old TESR boxes logic:

        if (negX) {
            addBox(quads, 0.0f, 0.0f, 0.4375f, 0.125f, 1.0f, 0.5625f, sprite);
            addBox(quads, 0.0f, 0.0625f, negZ ? 0.125f : 0.0f, 0.0625f, 0.4375f, posZ ? 0.875f : 1.0f, sprite);
            addBox(quads, 0.0f, 0.5625f, negZ ? 0.125f : 0.0f, 0.0625f, 0.9375f, posZ ? 0.875f : 1.0f, sprite);
        }

        if (negZ) {
            addBox(quads, 0.4375f, 0.0f, 0.0f, 0.5625f, 1.0f, 0.125f, sprite);
            addBox(quads, negX ? 0.125f : 0.0f, 0.0625f, 0.0f, posX ? 0.875f : 1.0f, 0.4375f, 0.0625f, sprite);
            addBox(quads, negX ? 0.125f : 0.0f, 0.5625f, 0.0f, posX ? 0.875f : 1.0f, 0.9375f, 0.0625f, sprite);
        }

        if (posX) {
            addBox(quads, 0.875f, 0.0f, 0.4375f, 1.0f, 1.0f, 0.5625f, sprite);
            addBox(quads, 0.9375f, 0.0625f, negZ ? 0.125f : 0.0f, 1.0f, 0.4375f, posZ ? 0.875f : 1.0f, sprite);
            addBox(quads, 0.9375f, 0.5625f, negZ ? 0.125f : 0.0f, 1.0f, 0.9375f, posZ ? 0.875f : 1.0f, sprite);
        }

        if (posZ) {
            addBox(quads, 0.4375f, 0.0f, 0.875f, 0.5625f, 1.0f, 1.0f, sprite);
            addBox(quads, negX ? 0.125f : 0.0f, 0.0625f, 0.9375f, posX ? 0.875f : 1.0f, 0.4375f, 1.0f, sprite);
            addBox(quads, negX ? 0.125f : 0.0f, 0.5625f, 0.9375f, posX ? 0.875f : 1.0f, 0.9375f, 1.0f, sprite);
        }

        if (posY) {
            addBox(quads, 0.0f, 0.875f, 0.0f, 0.125f, 0.9375f, 1.0f, sprite);
            addBox(quads, 0.875f, 0.875f, 0.0f, 1.0f, 0.9375f, 1.0f, sprite);
            addBox(quads, 0.0f, 0.9375f, 0.0625f, 1.0f, 1.0f, 0.4375f, sprite);
            addBox(quads, 0.0f, 0.9375f, 0.5625f, 1.0f, 1.0f, 0.9375f, sprite);
        }

        return quads;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return sprite;
    }

    private static void addBox(List<BakedQuad> quads, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, TextureAtlasSprite sprite) {
        FaceBakery bakery = new FaceBakery();

        Vector3f from = new Vector3f(minX * 16.0f, minY * 16.0f, minZ * 16.0f);
        Vector3f to   = new Vector3f(maxX * 16.0f, maxY * 16.0f, maxZ * 16.0f);

        for (EnumFacing face : EnumFacing.values()) {
            BlockFaceUV uv = makeFaceUV(face, from, to);
            BlockPartFace partFace = new BlockPartFace(face, -1, "", uv);
            BakedQuad quad = bakery.makeBakedQuad(from, to, partFace, sprite, face, TRSRTransformation.identity(), null, true, true);
            quads.add(quad);
        }
    }

    private static BlockFaceUV makeFaceUV(EnumFacing face, Vector3f from, Vector3f to) {
        float u1, v1, u2, v2;
        switch (face) {
            case DOWN -> {
                u1 = from.x;
                v1 = 16f - to.z;
                u2 = to.x;
                v2 = 16f - from.z;
            }
            case UP -> {
                u1 = from.x;
                v1 = from.z;
                u2 = to.x;
                v2 = to.z;
            }
            case NORTH -> { // Z-
                u1 = 16f - to.x;
                v1 = 16f - to.y;
                u2 = 16f - from.x;
                v2 = 16f - from.y;
            }
            case SOUTH -> { // Z+
                u1 = from.x;
                v1 = 16f - to.y;
                u2 = to.x;
                v2 = 16f - from.y;
            }
            case WEST -> {  // X-
                u1 = from.z;
                v1 = 16f - to.y;
                u2 = to.z;
                v2 = 16f - from.y;
            }
            case EAST -> {  // X+
                u1 = 16f - to.z;
                v1 = 16f - to.y;
                u2 = 16f - from.z;
                v2 = 16f - from.y;
            }
            default -> {
                u1 = 0f;
                v1 = 0f;
                u2 = 16f;
                v2 = 16f;
            }
        }
        return new BlockFaceUV(new float[] {u1, v1, u2, v2}, 0);
    }
}
