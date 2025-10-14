package com.hbm.blocks.generic;

import com.hbm.blocks.BlockEnums;
import com.hbm.lib.RefStrings;
import com.hbm.render.amlfrom1710.WavefrontObject;
import com.hbm.render.model.BlockDecoBakedModel;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockDecoToaster extends BlockDecoModel {

    public BlockDecoToaster(Material mat, SoundType type, String registryName) {
        super(mat, type, registryName, BlockEnums.DecoToasterEnum.class, true, true,
                new ResourceLocation(RefStrings.MODID, "models/blocks/toaster.obj"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/toaster_iron"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/toaster_steel"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/toaster_wood"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        WavefrontObject wavefront = new WavefrontObject(new ResourceLocation(RefStrings.MODID, "models/blocks/toaster.obj"));
        TextureMap atlas = Minecraft.getMinecraft().getTextureMapBlocks();
        String[] variants = new String[]{"toaster_iron", "toaster_steel", "toaster_wood"};
        for (int m = 0; m < variants.length; m++) {
            TextureAtlasSprite sprite = atlas.getAtlasSprite(new ResourceLocation(RefStrings.MODID, "blocks/" + variants[m]).toString());
            // World/block model (centered, shaded)
            IBakedModel bakedWorld = BlockDecoBakedModel.forBlock(wavefront, sprite, -0.5F);
            ModelResourceLocation mrlWorld = new ModelResourceLocation(getRegistryName(), "meta=" + m);
            event.getModelRegistry().putObject(mrlWorld, bakedWorld);

            IBakedModel bakedItem = new BlockDecoBakedModel(wavefront, sprite, false, 1.0F, 0.0F, -0.5F, 0.0F);
            ModelResourceLocation mrlItem = new ModelResourceLocation(new ResourceLocation(RefStrings.MODID, getRegistryName().getPath() + "_item_" + m), "inventory");
            event.getModelRegistry().putObject(mrlItem, bakedItem);
        }
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, net.minecraft.util.math.RayTraceResult target, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.entity.player.EntityPlayer player) {
        return new ItemStack(Item.getItemFromBlock(this), 1, state.getValue(META) & 3);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel() {
        Item item = Item.getItemFromBlock(this);
        int count = 3; // IRON, STEEL, WOOD
        for (int m = 0; m < count; m++) {
            ModelResourceLocation inv = new ModelResourceLocation(new ResourceLocation(RefStrings.MODID, getRegistryName().getPath() + "_item_" + m), "inventory");
            ModelLoader.setCustomModelResourceLocation(item, m, inv);
        }
    }
}


