package com.hbm.render.model;

import com.hbm.blocks.network.energy.PowerCableBox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CableBoxBakedModel extends AbstractBakedModel {

    private final int meta;

    public CableBoxBakedModel(int meta) {
        super(BakedModelTransforms.standardBlock());
        this.meta = meta;
    }

    public static TextureAtlasSprite getCableIcon(
            int meta, int side,
            boolean pX, boolean nX, boolean pY, boolean nY, boolean pZ, boolean nZ
    ) {
        int m = meta % 5;
        int mask = (pX ? 32 : 0) + (nX ? 16 : 0) + (pY ? 8 : 0) + (nY ? 4 : 0) + (pZ ? 2 : 0) + (nZ ? 1 : 0);
        int count = Integer.bitCount(mask);

        TextureAtlasSprite[] straight = PowerCableBox.iconStraight;
        TextureAtlasSprite[] end = PowerCableBox.iconEnd;
        TextureAtlasSprite[] curveTL = PowerCableBox.iconCurveTL;
        TextureAtlasSprite[] curveTR = PowerCableBox.iconCurveTR;
        TextureAtlasSprite[] curveBL = PowerCableBox.iconCurveBL;
        TextureAtlasSprite[] curveBR = PowerCableBox.iconCurveBR;
        TextureAtlasSprite[] junction = PowerCableBox.iconJunction;


        if ((mask & 0b001111) == 0 && mask > 0) return (side == 4 || side == 5) ? end[m] : straight[m];
        if ((mask & 0b111100) == 0 && mask > 0) return (side == 2 || side == 3) ? end[m] : straight[m];
        if ((mask & 0b110011) == 0 && mask > 0) return (side == 0 || side == 1) ? end[m] : straight[m];

        if (count == 2) {
            if ((nY && pZ) || (pY && nZ)) return side == 4 ? curveTR[m] : curveTL[m];
            if ((nY && nZ) || (pY && pZ)) return side == 5 ? curveTR[m] : curveTL[m];
            if ((nY && pX) || (pY && nX)) return side == 3 ? curveBR[m] : curveBL[m];
            if ((nX && nZ) || (pX && pZ)) return side == 2 ? curveBR[m] : curveBL[m];

            return straight[m];
        }

        return junction[Math.min(m, junction.length - 1)];
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (side != null) return Collections.emptyList();

        List<BakedQuad> quads = new ArrayList<>();

        boolean pX, nX, pY, nY, pZ, nZ;
        int useMeta = this.meta;

        if (state == null) {
            pX = nX = true;
            pY = nY = false;
            pZ = nZ = false;
        } else {
            IExtendedBlockState ext = (IExtendedBlockState) state;
            nZ = Boolean.TRUE.equals(ext.getValue(PowerCableBox.CONN_NORTH));
            pZ = Boolean.TRUE.equals(ext.getValue(PowerCableBox.CONN_SOUTH));
            nX = Boolean.TRUE.equals(ext.getValue(PowerCableBox.CONN_WEST));
            pX = Boolean.TRUE.equals(ext.getValue(PowerCableBox.CONN_EAST));
            nY = Boolean.TRUE.equals(ext.getValue(PowerCableBox.CONN_DOWN));
            pY = Boolean.TRUE.equals(ext.getValue(PowerCableBox.CONN_UP));
            useMeta = ext.getValue(PowerCableBox.META);
        }

        int sizeLevel = Math.min(useMeta, 4);
        float lower = 0.125f + sizeLevel * 0.0625f;
        float upper = 0.875f - sizeLevel * 0.0625f;
        float jLower = 0.0625f + sizeLevel * 0.0625f;
        float jUpper = 0.9375f - sizeLevel * 0.0625f;

        List<float[]> boundsList = new ArrayList<>();


        if (pX || nX) boundsList.add(new float[]{0, lower, lower, 1, upper, upper});
        if (pY || nY) boundsList.add(new float[]{lower, 0, lower, upper, 1, upper});
        if (pZ || nZ) boundsList.add(new float[]{lower, lower, 0, upper, upper, 1});

        int connectionCount = (pX ? 1 : 0) + (nX ? 1 : 0) + (pY ? 1 : 0) + (nY ? 1 : 0) + (pZ ? 1 : 0) + (nZ ? 1 : 0);
        if (connectionCount > 1) boundsList.add(new float[]{jLower, jLower, jLower, jUpper, jUpper, jUpper});

        FaceBakery faceBakery = new FaceBakery();

        for (float[] b : boundsList) {
            float minX = b[0] * 16f, minY = b[1] * 16f, minZ = b[2] * 16f;
            float maxX = b[3] * 16f, maxY = b[4] * 16f, maxZ = b[5] * 16f;
            if (minX == maxX || minY == maxY || minZ == maxZ) continue;

            for (EnumFacing faceEnum : EnumFacing.VALUES) {
                TextureAtlasSprite sprite = getCableIcon(useMeta, faceEnum.getIndex(), pX, nX, pY, nY, pZ, nZ);
                if (sprite == null) continue;

                BlockPartFace bpf = new BlockPartFace(null, 0, "", new BlockFaceUV(new float[]{0, 0, 16, 16}, 0));
                Vector3f from = new Vector3f(minX, minY, minZ);
                Vector3f to = new Vector3f(maxX, maxY, maxZ);

                BakedQuad quad = faceBakery.makeBakedQuad(from, to, bpf, sprite, faceEnum, ModelRotation.X0_Y0, null, false, true);
                quads.add(quad);
            }
        }

        return quads;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return getCableIcon(meta, EnumFacing.UP.getIndex(), false, false, false, false, false, false);
    }
}
