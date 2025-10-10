package com.hbm.util;

import net.minecraft.client.renderer.GlStateManager;

public class RenderUtil {
    private RenderUtil() {
    }

    public static boolean isAlphaEnabled() {
        return GlStateManager.alphaState.alphaTest.currentState;
    }

    public static boolean isBlendEnabled() {
        return GlStateManager.blendState.blend.currentState;
    }

    public static int getBlendSrcFactor() {
        return GlStateManager.blendState.srcFactor;
    }

    public static int getBlendDstFactor() {
        return GlStateManager.blendState.dstFactor;
    }

    public static int getBlendSrcAlphaFactor() {
        return GlStateManager.blendState.srcFactorAlpha;
    }

    public static int getBlendDstAlphaFactor() {
        return GlStateManager.blendState.dstFactorAlpha;
    }

    public static boolean isCullEnabled() {
        return GlStateManager.cullState.cullFace.currentState;
    }

    public static boolean isDepthEnabled() {
        return GlStateManager.depthState.depthTest.currentState;
    }

    public static float getColorMaskRed() {
        return GlStateManager.colorState.red;
    }

    public static float getColorMaskGreen() {
        return GlStateManager.colorState.green;
    }

    public static float getColorMaskBlue() {
        return GlStateManager.colorState.blue;
    }

    public static float getColorMaskAlpha() {
        return GlStateManager.colorState.alpha;
    }

    public static boolean isDepthMaskEnabled() {
        return GlStateManager.depthState.maskEnabled;
    }

    public static int getDepthFunc() {
        return GlStateManager.depthState.depthFunc;
    }

    public static boolean isLightingEnabled() {
        return GlStateManager.lightingState.currentState;
    }

    public static int getActiveTextureUnitIndex() {
        return GlStateManager.activeTextureUnit;
    }

    public static boolean isTexture2DEnabled() {
        final int unit = GlStateManager.activeTextureUnit;
        return GlStateManager.textureState[unit].texture2DState.currentState;
    }

    public static boolean isTexture2DEnabled(int unit) {
        if (unit < 0 || unit >= GlStateManager.textureState.length) return false;
        return GlStateManager.textureState[unit].texture2DState.currentState;
    }

    public static int getShadeModel() {
        return GlStateManager.activeShadeModel;
    }
}
