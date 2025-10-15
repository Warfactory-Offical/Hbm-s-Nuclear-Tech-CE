package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.bomb.TileEntityLaunchPad;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderLaunchPadTier1 extends TileEntitySpecialRenderer<TileEntityLaunchPad>
    implements IItemRendererProvider {

  public static final float h_1 = 1F;
  public static final float h_2 = 1F;
  public static final float h_3 = 0.8F;
  public static final float w_2 = 1F;

  @Override
  public boolean isGlobalRenderer(TileEntityLaunchPad te) {
    return true;
  }

  @Override
  public void render(
      TileEntityLaunchPad te,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5D, y, z + 0.5D);
    GlStateManager.enableLighting();
    GlStateManager.disableCull();
    GlStateManager.shadeModel(GL11.GL_SMOOTH);
    bindTexture(ResourceManager.missile_pad_tex);
    ResourceManager.missile_pad_old.renderAll();

    if (te.clearingTimer == 0) {
      ItemStack missileStack = te.toRender;
      if (missileStack != null && !missileStack.isEmpty()) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 1, 0);
        renderMissileModel(missileStack.getItem());
        GlStateManager.popMatrix();
      }
    }
    GlStateManager.shadeModel(GL11.GL_FLAT);
    GlStateManager.enableCull();
    GlStateManager.popMatrix();
  }

  private void renderMissileModel(Item missileItem) {
    if (missileItem == ModItems.missile_micro) {
      GlStateManager.scale(2F, 2F, 2F);
      bindTexture(ResourceManager.missileMicro_tex);
      ResourceManager.missileTaint.renderAll();
    } else if (missileItem == ModItems.missile_schrabidium) {
      GlStateManager.scale(2F, 2F, 2F);
      bindTexture(ResourceManager.missileMicroSchrab_tex);
      ResourceManager.missileTaint.renderAll();
    } else if (missileItem == ModItems.missile_bhole) {
      GlStateManager.scale(2F, 2F, 2F);
      bindTexture(ResourceManager.missileMicroBHole_tex);
      ResourceManager.missileTaint.renderAll();
    } else if (missileItem == ModItems.missile_taint) {
      GlStateManager.scale(2F, 2F, 2F);
      bindTexture(ResourceManager.missileTaint_tex);
      ResourceManager.missileTaint.renderAll();
    } else if (missileItem == ModItems.missile_emp) {
      GlStateManager.scale(2F, 2F, 2F);
      bindTexture(ResourceManager.missileMicroEMP_tex);
      ResourceManager.missileTaint.renderAll();
    }
    // Tier 1
    else if (missileItem == ModItems.missile_generic) {
      GlStateManager.scale(1.0F, h_1, 1.0F);
      bindTexture(ResourceManager.missileV2_HE_tex);
      ResourceManager.missileV2.renderAll();
    } else if (missileItem == ModItems.missile_incendiary) {
      GlStateManager.scale(1.0F, h_1, 1.0F);
      bindTexture(ResourceManager.missileV2_IN_tex);
      ResourceManager.missileV2.renderAll();
    } else if (missileItem == ModItems.missile_cluster) {
      GlStateManager.scale(1.0F, h_1, 1.0F);
      bindTexture(ResourceManager.missileV2_CL_tex);
      ResourceManager.missileV2.renderAll();
    } else if (missileItem == ModItems.missile_buster) {
      GlStateManager.scale(1.0F, h_1, 1.0F);
      bindTexture(ResourceManager.missileV2_BU_tex);
      ResourceManager.missileV2.renderAll();
    }
    // Tier 2
    else if (missileItem == ModItems.missile_strong) {
      GlStateManager.scale(w_2, h_2, w_2);
      bindTexture(ResourceManager.missileStrong_HE_tex);
      ResourceManager.missileStrong.renderAll();
    } else if (missileItem == ModItems.missile_incendiary_strong) {
      GlStateManager.scale(w_2, h_2, w_2);
      bindTexture(ResourceManager.missileStrong_IN_tex);
      ResourceManager.missileStrong.renderAll();
    } else if (missileItem == ModItems.missile_cluster_strong) {
      GlStateManager.scale(w_2, h_2, w_2);
      bindTexture(ResourceManager.missileStrong_CL_tex);
      ResourceManager.missileStrong.renderAll();
    } else if (missileItem == ModItems.missile_buster_strong) {
      GlStateManager.scale(w_2, h_2, w_2);
      bindTexture(ResourceManager.missileStrong_BU_tex);
      ResourceManager.missileStrong.renderAll();
    } else if (missileItem == ModItems.missile_emp_strong) {
      GlStateManager.scale(w_2, h_2, w_2);
      bindTexture(ResourceManager.missileStrong_EMP_tex);
      ResourceManager.missileStrong.renderAll();
    }
    // Tier 3
    else if (missileItem == ModItems.missile_burst
        || missileItem == ModItems.missile_inferno
        || missileItem == ModItems.missile_rain
        || missileItem == ModItems.missile_drill) {
      GlStateManager.scale(h_3, h_3, h_3);
      if (missileItem == ModItems.missile_burst) bindTexture(ResourceManager.missileHuge_HE_tex);
      if (missileItem == ModItems.missile_inferno) bindTexture(ResourceManager.missileHuge_IN_tex);
      if (missileItem == ModItems.missile_rain) bindTexture(ResourceManager.missileHuge_CL_tex);
      if (missileItem == ModItems.missile_drill) bindTexture(ResourceManager.missileHuge_BU_tex);
      ResourceManager.missileHuge.renderAll();
    } else if (missileItem == ModItems.missile_endo) {
      GlStateManager.scale(1.5F, 1.5F, 1.5F);
      bindTexture(ResourceManager.missileEndo_tex);
      ResourceManager.missileThermo.renderAll();
    } else if (missileItem == ModItems.missile_exo) {
      GlStateManager.scale(1.5F, 1.5F, 1.5F);
      bindTexture(ResourceManager.missileExo_tex);
      ResourceManager.missileThermo.renderAll();
    }
    // Tier 4
    else if (missileItem == ModItems.missile_nuclear) {
      GlStateManager.scale(1.5F, 1.5F, 1.5F);
      bindTexture(ResourceManager.missileNuclear_tex);
      ResourceManager.missileNuclear.renderAll();
    } else if (missileItem == ModItems.missile_nuclear_cluster) {
      GlStateManager.scale(1.5F, 1.5F, 1.5F);
      bindTexture(ResourceManager.missileMIRV_tex);
      ResourceManager.missileNuclear.renderAll();
    } else if (missileItem == ModItems.missile_volcano) {
      GlStateManager.scale(1.5F, 1.5F, 1.5F);
      bindTexture(ResourceManager.missileVolcano_tex);
      ResourceManager.missileNuclear.renderAll();
    } else if (missileItem == ModItems.missile_doomsday) {
      GlStateManager.scale(2F, 2F, 2F);
      bindTexture(ResourceManager.missileDoomsday_tex);
      ResourceManager.missileDoomsday.renderAll();
    } else if (missileItem == ModItems.missile_n2) {
      GlStateManager.scale(1.5F, 1.5F, 1.5F);
      bindTexture(ResourceManager.missileN2_tex);
      ResourceManager.missileNuclear.renderAll();
    }
    // Special
    else if (missileItem == ModItems.missile_anti_ballistic) {
      bindTexture(ResourceManager.missileAA_tex);
      ResourceManager.missileABM.renderAll();
    } else if (missileItem == ModItems.missile_carrier) {
      GlStateManager.scale(2F, 2F, 2F);
      bindTexture(ResourceManager.missileCarrier_tex);
      ResourceManager.missileCarrier.renderAll();
      GlStateManager.pushMatrix();
      GlStateManager.translate(0.0D, 0.5D, 0.0D);
      bindTexture(ResourceManager.missileBooster_tex);
      GlStateManager.translate(1.25D, 0.0D, 0.0D);
      ResourceManager.missileBooster.renderAll();
      GlStateManager.translate(-2.5D, 0.0D, 0.0D);
      ResourceManager.missileBooster.renderAll();
      GlStateManager.translate(1.25D, 0.0D, 0.0D);
      GlStateManager.translate(0.0D, 0.0D, 1.25D);
      ResourceManager.missileBooster.renderAll();
      GlStateManager.translate(0.0D, 0.0D, -2.5D);
      ResourceManager.missileBooster.renderAll();
      GlStateManager.popMatrix();
    }
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.launch_pad);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -1, 0);
        GlStateManager.scale(3, 3, 3);
      }

      public void renderCommon() {
        bindTexture(ResourceManager.missile_pad_tex);
        ResourceManager.missile_pad_old.renderAll();
      }
    };
  }
}
