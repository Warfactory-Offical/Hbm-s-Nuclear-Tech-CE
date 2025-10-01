package com.hbm.render.model;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class AbstractBakedModel implements IBakedModel {

    private final boolean ambientOcclusion;
    private final boolean gui3d;
    private final boolean builtInRenderer;
    private final ItemCameraTransforms transforms;
    private final ItemOverrideList overrides;

    protected AbstractBakedModel(ItemCameraTransforms transforms) {
        this(true, true, false, transforms, ItemOverrideList.NONE);
    }

    protected AbstractBakedModel(boolean ambientOcclusion, boolean gui3d, boolean builtInRenderer, ItemCameraTransforms transforms) {
        this(ambientOcclusion, gui3d, builtInRenderer, transforms, ItemOverrideList.NONE);
    }

    protected AbstractBakedModel(boolean ambientOcclusion, boolean gui3d, boolean builtInRenderer, ItemCameraTransforms transforms, ItemOverrideList overrides) {
        this.ambientOcclusion = ambientOcclusion;
        this.gui3d = gui3d;
        this.builtInRenderer = builtInRenderer;
        this.transforms = transforms != null ? transforms : ItemCameraTransforms.DEFAULT;
        this.overrides = overrides != null ? overrides : ItemOverrideList.NONE;
    }

    @Override
    public final boolean isAmbientOcclusion() {
        return ambientOcclusion;
    }

    @Override
    public final boolean isGui3d() {
        return gui3d;
    }

    @Override
    public final boolean isBuiltInRenderer() {
        return builtInRenderer;
    }

    @Override
    public final ItemCameraTransforms getItemCameraTransforms() {
        return transforms;
    }

    @Override
    public final ItemOverrideList getOverrides() {
        return overrides;
    }
}
