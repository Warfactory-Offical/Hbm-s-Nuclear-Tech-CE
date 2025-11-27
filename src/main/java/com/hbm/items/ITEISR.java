package com.hbm.items;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hbm.render.item.TEISRBase;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.BakedItemModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import javax.vecmath.Matrix4f;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public interface ITEISR extends IDynamicModels {


    @SideOnly(Side.CLIENT)
    default void bakeModel(ModelBakeEvent event) {
        IRegistry<ModelResourceLocation, IBakedModel> reg = event.getModelRegistry();
        ResourceLocation res = null;
        TileEntityItemStackRenderer render = null;
        if (this instanceof Item item) {
            res = item.getRegistryName();
            render = item.getTileEntityItemStackRenderer();
        }
        if (this instanceof Block block) {
            res = block.getRegistryName();
            render = Item.getItemFromBlock(block).getTileEntityItemStackRenderer();
        }

        if (res == null)
            return;

        var modelLoc = new ModelResourceLocation(res, "inventory");
        if(render instanceof TEISRBase base)
            base.itemModel = isGui3D() ? new BakedItemModel(ImmutableList.of(),
                    Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/stone"),
                    ImmutableMap.of(), ItemOverrideList.NONE  ){

            } : getInvModel(event, modelLoc);
        else return;

        reg.putObject(modelLoc, new BakedModelDummy((TEISRBase) render, isGui3D()));

    }

    @Nullable
    default IBakedModel getInvModel(ModelBakeEvent event, ModelResourceLocation modelResourceLocation) {
        return event.getModelRegistry().getObject(modelResourceLocation);
    }


    default boolean isGui3D() {
        return true;
    }
    ;

    @SideOnly(Side.CLIENT)
    default void registerSprite(TextureMap map) {
    }

    ;

    class BakedModelDummy implements IBakedModel {
        TEISRBase renderer;
        boolean gui3D = true;

        public BakedModelDummy(TEISRBase renderer, boolean gui3D) {
            this.renderer = renderer;
            this.gui3D = gui3D;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            if (gui3D)
                return renderer.type != ItemCameraTransforms.TransformType.GUI ? List.of() : renderer.itemModel.getQuads(state, side, rand);
            return List.of();
        }

        @Override
        public boolean isAmbientOcclusion() {
            if (gui3D) return false;
            return renderer.type == ItemCameraTransforms.TransformType.GUI && renderer.itemModel.isAmbientOcclusion();
        }

        @Override
        public boolean isGui3d() {
            if (gui3D) return false;
            return renderer.type == ItemCameraTransforms.TransformType.GUI && renderer.itemModel.isGui3d();
        }

        @Override
        public boolean isBuiltInRenderer() {
            if (gui3D) return true; // tells Minecraft to use the TEISR
            return renderer.type != ItemCameraTransforms.TransformType.GUI || renderer.itemModel.isBuiltInRenderer();
        }

        @Override
        public TextureAtlasSprite getParticleTexture() {
            if (gui3D) return renderer.itemModel.getParticleTexture();
            else
                return null;
        }

        @Override
        public ItemCameraTransforms getItemCameraTransforms() {
            return ItemCameraTransforms.DEFAULT; // can be ignored if using TEISR
        }

        @Override
        public ItemOverrideList getOverrides() {

            return renderer.type != ItemCameraTransforms.TransformType.GUI ? new ItemOverrideList(Collections.emptyList()){
                @Override
                public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
                    renderer.entity = entity;
                    renderer.world = world;
                    return super.handleItemState(originalModel, stack, world, entity);
                }
            } : renderer.itemModel.getOverrides();
        }


        public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
            renderer.type = cameraTransformType;

            if (gui3D)
                return Pair.of(this, null);


            Pair<? extends IBakedModel, Matrix4f> par = renderer.itemModel.handlePerspective(cameraTransformType);
            return Pair.of(this, renderer.doNullTransform() && cameraTransformType == ItemCameraTransforms.TransformType.GUI ? null : par.getRight());
        }
    }


}
