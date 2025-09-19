package com.hbm.render.block;

import com.google.common.collect.ImmutableMap;
import com.hbm.lib.RefStrings;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

import static com.hbm.render.block.BlockBakeFrame.BlockForm.*;


/**
 * Flexible system for baking Block models, supporting all possible configurations (that matter)
 * All you need to do is provide its form (consult the enum on the bottom of the class), and string names of textures
 * from there it will be handled for you.
 *
 * @author MrNorwood
 */
public class BlockBakeFrame {


    public static final String ROOT_PATH = "blocks/";
    public final String[] textureArray;
    public final BlockForm blockForm;

    //Quick method for making an array of single texture ALL form blocks
    public BlockBakeFrame(String texture) {
        this.textureArray = new String[]{texture};
        this.blockForm = ALL;
    }

    public BlockBakeFrame(String topTexture, String sideTexture) {
        this.textureArray = new String[]{topTexture, sideTexture};
        this.blockForm = PILLAR;
    }


    public BlockBakeFrame(String topTexture, String sideTexture, String bottomTexture) {
        this.textureArray = new String[]{topTexture, sideTexture, bottomTexture};
        this.blockForm = PILLAR_BOTTOM;
    }

    public BlockBakeFrame(BlockForm form, @NotNull String... textures) {
        this.textureArray = textures;
        switch (textures.length) {
            case 1:
                if (form == ALL || form == CROSS || form == CROP || form == LAYER) break;
            case 2:
                if (form == PILLAR) break;
            case 3:
                if (form == PILLAR_BOTTOM) break;
            case 6:
                if (form == FULL_CUSTOM) break;
            default:
                throw new IllegalArgumentException("Amount of textures provided is invalid: " + textures.length
                        + ". The amount should be 1, 2, 3 or 6");
        }
        this.blockForm = form;
    }

    public static BlockBakeFrame[] simpleModelArray(String... texture) {
        BlockBakeFrame[] frames = new BlockBakeFrame[texture.length];
        for (int i = 0; i < texture.length; i++) {
            frames[i] = new BlockBakeFrame(texture[i]);
        }
        return frames;
    }

    public static BlockBakeFrame simpleSouthRotatable(String sides, String front) {
        return new BlockBakeFrame(FULL_CUSTOM, sides, sides, sides, front, sides, sides);
    }

    public static int getYRotationForFacing(EnumFacing facing) {
        return switch (facing) {
            case SOUTH -> 0;
            case WEST -> 90;
            case NORTH -> 180;
            case EAST -> 270;
            default -> 0;
        };
    }

    public static int getXRotationForFacing(EnumFacing facing) {
        return switch (facing) {
            case UP -> 90;
            case DOWN -> 270;
            default -> 0;
        };
    }

    public void registerBlockTextures(TextureMap map) {
        for (String texture : this.textureArray) {
            ResourceLocation spriteLoc = new ResourceLocation(RefStrings.MODID, ROOT_PATH + texture);
            map.registerSprite(spriteLoc);
        }
    }

    public ResourceLocation getSpriteLoc(int index) {
        return new
                ResourceLocation(RefStrings.MODID, ROOT_PATH + textureArray[index]);
    }

    public static BlockBakeFrame bottomTop(String side, String top, String bottom) {
        return new BlockBakeFrame(FULL_CUSTOM, top, bottom, side, side, side, side);
    }

    public String getBaseModel() {
        return this.blockForm.baseBakedModel;
    }

    public void putTextures(ImmutableMap.Builder<String, String> textureMap) {
        String[] wraps = this.blockForm.textureWrap;
        AtomicInteger counter = new AtomicInteger(0);
        for (String face : wraps) {
            textureMap.put(face, getSpriteLoc(counter.getAndIncrement()).toString());
        }
        textureMap.put("particle", getSpriteLoc(0).toString());
    }

    public enum BlockForm {
        ALL("minecraft:block/cube_all", 1, new String[]{"all"}),
        CROP("minecraft:block/crop", 1, new String[]{"crop"}),
        LAYER("hbm:block/block_layering", 1, new String[]{"texture"}),
        CROSS("minecraft:block/cross", 1, new String[]{"cross"}),
        PILLAR("minecraft:block/cube_column", 2, new String[]{"end", "side"}),
        PILLAR_BOTTOM("minecraft:block/cube_column", 3, new String[]{"end", "side", "bottom"}),
        FULL_CUSTOM("minecraft:block/cube", 6, new String[]{
                "up",
                "down",
                "north",
                "south",
                "west",
                "east"
        });

        public final String baseBakedModel;
        public final int textureNum;
        public final String[] textureWrap;

        BlockForm(String baseBakedModel, int textureNum, String[] textureWrap) {
            this.baseBakedModel = baseBakedModel;
            this.textureNum = textureNum;
            this.textureWrap = textureWrap;
        }

    }
}
