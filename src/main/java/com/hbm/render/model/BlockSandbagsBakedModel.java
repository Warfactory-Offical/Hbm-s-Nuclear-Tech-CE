package com.hbm.render.model;

import com.hbm.blocks.generic.BlockSandbags;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class BlockSandbagsBakedModel extends AbstractBakedModel {
  private final TextureAtlasSprite sprite;
  private final boolean isInventory;

  public BlockSandbagsBakedModel(TextureAtlasSprite sprite, boolean isInventory) {
    super(BakedModelTransforms.standardBlock());
    this.sprite = sprite;
    this.isInventory = isInventory;
  }

  @Override
  public TextureAtlasSprite getParticleTexture() {
    return sprite;
  }

  @Override
  public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
    if (side != null) return Collections.emptyList();

    List<BakedQuad> quads = new ArrayList<>();

    if (isInventory) {
      addBox(quads, 0.1250F, 0F, 0.125F, 0.875F, 1.0F, 0.875F, sprite);
      return quads;
    }

    final float min = 0.25F;
    final float max = 0.75F;

    boolean nx = false;
    boolean px = false;
    boolean nz = false;
    boolean pz = false;

    if (state instanceof IExtendedBlockState ext) {
      Boolean b;

      b = ext.getValue(BlockSandbags.CONN_NEG_X);
      nx = b != null && b;

      b = ext.getValue(BlockSandbags.CONN_POS_X);
      px = b != null && b;

      b = ext.getValue(BlockSandbags.CONN_NEG_Z);
      nz = b != null && b;

      b = ext.getValue(BlockSandbags.CONN_POS_Z);
      pz = b != null && b;
    }

    addBox(quads, min, 0F, min, max, 1F, max, sprite);

    if (nx) addBox(quads, 0F, 0F, min, min, 1F, max, sprite);
    if (px) addBox(quads, max, 0F, min, 1F, 1F, max, sprite);
    if (nz) addBox(quads, min, 0F, 0F, max, 1F, min, sprite);
    if (pz) addBox(quads, min, 0F, max, max, 1F, 1F, sprite);

    return quads;
  }
}
