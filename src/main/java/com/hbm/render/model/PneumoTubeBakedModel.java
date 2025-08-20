package com.hbm.render.model;

import com.hbm.lib.ForgeDirection;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.hbm.blocks.network.PneumoTube.*;

@SuppressWarnings("deprecation")
@MethodsReturnNonnullByDefault
@SideOnly(Side.CLIENT)
public class PneumoTubeBakedModel implements IBakedModel {
    public PneumoTubeBakedModel() {}

    private TextureAtlasSprite getIcon(EnumFacing face, boolean pX, boolean nX, boolean pY, boolean nY, boolean pZ, boolean nZ, byte type) {
        if (type == 1) return iconIn;
        if (type == 2) return iconOut;
        if (type == 3) return iconConnector;

        int mask = (pX ? 32 : 0) + (nX ? 16 : 0) + (pY ? 8 : 0) + (nY ? 4 : 0) + (pZ ? 2 : 0) + (nZ ? 1 : 0);

        if (mask == 0b110000) {
            return (face == EnumFacing.WEST || face == EnumFacing.EAST) ? iconConnector : iconStraight;
        }
        else if (mask == 0b000011) {
            return (face == EnumFacing.NORTH || face == EnumFacing.SOUTH) ? iconConnector : iconStraight;
        }
        else if (mask == 0b001100) {
            return (face == EnumFacing.UP || face == EnumFacing.DOWN) ? iconConnector : iconStraight;
        }

        return iconBase;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (side != null) return Collections.emptyList();

        List<BakedQuad> quads = new ArrayList<>();

        boolean pX, nX, pY, nY, pZ, nZ;
        ForgeDirection outDir, inDir, connectorDir;

        if (state == null) {
            pX = true; nX = true; pY = false; nY = false; pZ = false; nZ = false;
            outDir = ForgeDirection.UNKNOWN; inDir = ForgeDirection.UNKNOWN; connectorDir = ForgeDirection.UNKNOWN;
        } else {
            IExtendedBlockState ext = (IExtendedBlockState) state;
            outDir = ext.getValue(OUT_DIR);
            inDir = ext.getValue(IN_DIR);
            connectorDir = ext.getValue(CONNECTOR_DIR);
            nZ = Boolean.TRUE.equals(ext.getValue(CONN_NORTH));
            pZ = Boolean.TRUE.equals(ext.getValue(CONN_SOUTH));
            nX = Boolean.TRUE.equals(ext.getValue(CONN_WEST));
            pX = Boolean.TRUE.equals(ext.getValue(CONN_EAST));
            nY = Boolean.TRUE.equals(ext.getValue(CONN_DOWN));
            pY = Boolean.TRUE.equals(ext.getValue(CONN_UP));
        }

        float lower = 5f / 16f;
        float upper = 11f / 16f;

        int mask = (pX ? 32 : 0) + (nX ? 16 : 0) + (pY ? 8 : 0) + (nY ? 4 : 0) + (pZ ? 2 : 0) + (nZ ? 1 : 0);
        boolean straightX = (mask & 0b001111) == 0 && mask > 0;
        boolean straightY = (mask & 0b110011) == 0 && mask > 0;
        boolean straightZ = (mask & 0b111100) == 0 && mask > 0;

        List<float[]> boundsList = new ArrayList<>();
        boolean hasConnections = outDir != ForgeDirection.UNKNOWN || inDir != ForgeDirection.UNKNOWN || connectorDir != ForgeDirection.UNKNOWN;
        int count = Integer.bitCount(mask);

        if (straightX && !hasConnections && count > 1) {
            boundsList.add(new float[]{0, lower, lower, 1, upper, upper, 0});
        } else if (straightZ && !hasConnections && count > 1) {
            boundsList.add(new float[]{lower, lower, 0, upper, upper, 1, 0});
        } else if (straightY && !hasConnections && count > 1) {
            boundsList.add(new float[]{lower, 0, lower, upper, 1, upper, 0});
        } else {
            boundsList.add(new float[]{lower, lower, lower, upper, upper, upper, 0});

            if (nY) boundsList.add(new float[]{lower, 0, lower, upper, lower, upper, 0});
            if (pY) boundsList.add(new float[]{lower, upper, lower, upper, 1, upper, 0});
            if (nX) boundsList.add(new float[]{0, lower, lower, lower, upper, upper, 0});
            if (pX) boundsList.add(new float[]{upper, lower, lower, 1, upper, upper, 0});
            if (nZ) boundsList.add(new float[]{lower, lower, 0, upper, upper, lower, 0});
            if (pZ) boundsList.add(new float[]{lower, lower, upper, upper, upper, 1, 0});
        }

        if (outDir != ForgeDirection.UNKNOWN) addConnectorBounds(boundsList, outDir, 1);
        if (inDir != ForgeDirection.UNKNOWN) addConnectorBounds(boundsList, inDir, 2);
        if (connectorDir != ForgeDirection.UNKNOWN) addConnectorBounds(boundsList, connectorDir, 3);

        FaceBakery faceBakery = new FaceBakery();

        for (float[] b : boundsList) {
            float minX = b[0] * 16f, minY = b[1] * 16f, minZ = b[2] * 16f;
            float maxX = b[3] * 16f, maxY = b[4] * 16f, maxZ = b[5] * 16f;
            if (minX == maxX || minY == maxY || minZ == maxZ) continue;

            for (EnumFacing face : EnumFacing.VALUES) {
                TextureAtlasSprite sprite = getIcon(face, pX, nX, pY, nY, pZ, nZ, (byte) Math.round(b[6]));

                float uMin, uMax, vMin, vMax;
                int uvRotate = 0;

                switch (face) {
                    case UP:
                        uMin = straightZ ? minZ : minX; uMax = straightZ ? maxZ : maxX;
                        vMin = straightZ ? minX : minZ; vMax = straightZ ? maxX : maxZ;
                        if (straightZ) uvRotate = 90;
                        break;
                    case DOWN:
                        uMin = straightZ ? minZ : minX; uMax = straightZ ? maxZ : maxX;
                        vMin = straightZ ? minX : minZ; vMax = straightZ ? maxX : maxZ;
                        if (straightZ) uvRotate = 270;
                        break;
                    case NORTH:
                        uMin = 16f - (straightY ? maxY : maxX); uMax = 16f - (straightY ? minY : minX);
                        vMin = 16f - (straightY ? maxX : maxY); vMax = 16f - (straightY ? minX : minY);
                        if (straightY) uvRotate = 90;
                        break;
                    case SOUTH:
                        uMin = straightY ? maxY : maxX; uMax = straightY ? minY : minX;
                        vMin = 16f - (straightY ? maxX : maxY); vMax = 16f - (straightY ? minX : minY);
                        if (straightY) uvRotate = 90;
                        break;
                    case EAST:
                        uMin = 16f - (straightY ? maxY : maxZ); uMax = 16f - (straightY ? minY : minZ);
                        vMin = 16f - (straightY ? maxZ : maxY); vMax = 16f - (straightY ? minZ : minY);
                        if (straightY) uvRotate = 90;
                        break;
                    case WEST:
                    default:
                        uMin = straightY ? maxY : maxZ; uMax = straightY ? minY : minZ;
                        vMin = 16f - (straightY ? maxZ : maxY); vMax = 16f - (straightY ? minZ : minY);
                        if (straightY) uvRotate = 90;
                        break;
                }

                if (uMin > uMax) { float temp = uMin; uMin = uMax; uMax = temp; }
                if (vMin > vMax) { float temp = vMin; vMin = vMax; vMax = temp; }

                float[] uvs = new float[]{uMin, vMin, uMax, vMax};
                BlockPartFace bpf = new BlockPartFace(null, 0, "", new BlockFaceUV(uvs, uvRotate));

                Vector3f from = new Vector3f(), to = new Vector3f();
                switch (face) {
                    case DOWN: from.set(minX, minY, minZ); to.set(maxX, minY, maxZ); break;
                    case UP: from.set(minX, maxY, minZ); to.set(maxX, maxY, maxZ); break;
                    case NORTH: from.set(minX, minY, minZ); to.set(maxX, maxY, minZ); break;
                    case SOUTH: from.set(minX, minY, maxZ); to.set(maxX, maxY, maxZ); break;
                    case WEST: from.set(minX, minY, minZ); to.set(minX, maxY, maxZ); break;
                    case EAST: from.set(maxX, minY, minZ); to.set(maxX, maxY, maxZ); break;
                }

                BakedQuad quad = faceBakery.makeBakedQuad(from, to, bpf, sprite, face, ModelRotation.X0_Y0, null, false, true);
                quads.add(quad);
            }
        }

        return quads;
    }

    private void addConnectorBounds(List<float[]> boundsList, ForgeDirection dir, float type) {
        float tLower = 5f / 16f;
        float tUpper = 11f / 16f;
        float cLower = 4f / 16f;
        float cUpper = 12f / 16f;
        float nLower = 4f / 16f;
        float nUpper = 12f / 16f;

        switch(dir) {
            case EAST:
                boundsList.add(new float[]{tUpper, tLower, tLower, cUpper, tUpper, tUpper, type});
                boundsList.add(new float[]{cUpper, nLower, nLower, 1, nUpper, nUpper, type});
                break;
            case WEST:
                boundsList.add(new float[]{cLower, tLower, tLower, tLower, tUpper, tUpper, type});
                boundsList.add(new float[]{0, nLower, nLower, cLower, nUpper, nUpper, type});
                break;
            case UP:
                boundsList.add(new float[]{tLower, tUpper, tLower, tUpper, cUpper, tUpper, type});
                boundsList.add(new float[]{nLower, cUpper, nLower, nUpper, 1, nUpper, type});
                break;
            case DOWN:
                boundsList.add(new float[]{tLower, cLower, tLower, tUpper, tLower, tUpper, type});
                boundsList.add(new float[]{nLower, 0, nLower, nUpper, cLower, nUpper, type});
                break;
            case SOUTH:
                boundsList.add(new float[]{tLower, tLower, tUpper, tUpper, tUpper, cUpper, type});
                boundsList.add(new float[]{nLower, nLower, cUpper, nUpper, nUpper, 1, type});
                break;
            case NORTH:
                boundsList.add(new float[]{tLower, tLower, cLower, tUpper, tUpper, tLower, type});
                boundsList.add(new float[]{nLower, nLower, 0, nUpper, nUpper, cLower, type});
                break;
        }
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
        return iconBase;
    }

    private static final ItemCameraTransforms CUSTOM_TRANSFORMS = createCustomTransforms();
    private static ItemCameraTransforms createCustomTransforms() {
        ItemTransformVec3f gui = new ItemTransformVec3f(
                new Vector3f(30, -135, 0),
                new Vector3f(0, 0, 0),
                new Vector3f(0.625f, 0.625f, 0.625f)
        );

        ItemTransformVec3f thirdPerson = new ItemTransformVec3f(
                new Vector3f(75, 45, 0),
                new Vector3f(0, 1.5f / 16, -2.5f / 16),
                new Vector3f(0.5f, 0.5f, 0.5f)
        );

        ItemTransformVec3f firstPerson = new ItemTransformVec3f(
                new Vector3f(0, 45, 0),
                new Vector3f(0, 0, 0),
                new Vector3f(0.5f, 0.5f, 0.5f)
        );

        ItemTransformVec3f ground = new ItemTransformVec3f(
                new Vector3f(0, 0, 0),
                new Vector3f(0, 2f / 16, 0),
                new Vector3f(0.5f, 0.5f, 0.5f)
        );

        ItemTransformVec3f head = new ItemTransformVec3f(
                new Vector3f(0, 0, 0),
                new Vector3f(0, 13f / 16, 7f / 16),
                new Vector3f(1, 1, 1)
        );

        ItemTransformVec3f fixed = new ItemTransformVec3f(
                new Vector3f(0, 180, 0),
                new Vector3f(0, 0, 0),
                new Vector3f(0.75f, 0.75f, 0.75f)
        );

        return new ItemCameraTransforms(thirdPerson, thirdPerson, firstPerson, firstPerson, head, gui, ground, fixed);
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return CUSTOM_TRANSFORMS;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}
