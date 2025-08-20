package com.hbm.items;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

/**
 * Used in items that require model baking;
 * Will automatically bake once correct methods are supplied
 */
public interface IDynamicModels {

    /**
     * Should be populated by implementors in constructors.
     */
    List<IDynamicModels> INSTANCES = new ArrayList<>();

    @SideOnly(Side.CLIENT)
    static void bakeModels(ModelBakeEvent event) {
        INSTANCES.forEach(blockMeta -> blockMeta.bakeModel(event));
    }

    @SideOnly(Side.CLIENT)
    static void registerModels() {
        INSTANCES.forEach(IDynamicModels::registerModel);
    }

    @SideOnly(Side.CLIENT)
    static void registerSprites(TextureMap map) {
        INSTANCES.forEach(dynamicSprite -> dynamicSprite.registerSprite(map));
    }

    @SideOnly(Side.CLIENT)
    public static void registerCustomStateMappers() {
        for (IDynamicModels model : INSTANCES) {
            if (model.getSelf() == null || !(model.getSelf() instanceof Block block)) continue;
            StateMapperBase mapper = model.getStateMapper(block.getRegistryName());
            if (mapper != null)
                ModelLoader.setCustomStateMapper(
                        block,
                        mapper
                );
        }

    }

    @SideOnly(Side.CLIENT)
    static void registerColorHandlers(ColorHandlerEvent.Item evt) {
        for (IDynamicModels model : INSTANCES) {
            IItemColor colorHandler = model.getColorHandler();
            Object self = model.getSelf();

            if (colorHandler == null || !(self instanceof Item item)) continue;

            evt.getItemColors().registerItemColorHandler(colorHandler, item);
        }
    }


    @SideOnly(Side.CLIENT)
    default IItemColor getColorHandler() {
        return null;
    }


    @SideOnly(Side.CLIENT)
    default StateMapperBase getStateMapper(ResourceLocation loc) {
        return null;
    }

    @SideOnly(Side.CLIENT)
    void bakeModel(ModelBakeEvent event);

    default Object getSelf() {
        return this;
    }

    @SideOnly(Side.CLIENT)
    void registerModel();

    @SideOnly(Side.CLIENT)
    void registerSprite(TextureMap map);

}