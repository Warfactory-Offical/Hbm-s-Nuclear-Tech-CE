package com.hbm.render.item;

import com.hbm.forgefluid.SpecialContainerFillLists.EnumCell;
import com.hbm.render.NTMRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import static com.hbm.items.special.ItemCell.getFluidType;

public class ItemRenderCell extends TEISRBase {

    @Override
    public void renderByItem(@NotNull ItemStack stack) {
        EnumCell cellType = EnumCell.getEnumFromFluid(getFluidType(stack));
        IBakedModel model = cellType.getRenderModel();
        if (model == null) {
            model = itemModel;
        }
        NTMRenderHelper.bindBlockTexture();

        GL11.glPushMatrix();
        GL11.glTranslated(0.5, 0.5, 0.5);
        Minecraft.getMinecraft().getRenderItem().renderItem(stack, model);
        GL11.glPopMatrix();
        super.renderByItem(stack);
    }
}