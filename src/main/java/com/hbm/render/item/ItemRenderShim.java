package com.hbm.render.item;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;
import com.hbm.items.ModItems.ToolSets;
import com.hbm.main.ResourceManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;
@AutoRegister(item = "shimmer_sledge")
@AutoRegister(item = "shimmer_axe")
@AutoRegister(item = "stopsign")
@AutoRegister(item = "sopsign")
@AutoRegister(item = "chernobylsign")
public class ItemRenderShim extends TEISRBase {
	// Drillgon200: I hope I never have to look at this code again.

	public void renderByItem(ItemStack stack) {
		switch(type) {
		case FIRST_PERSON_LEFT_HAND:
		case FIRST_PERSON_RIGHT_HAND:
			if(stack.getItem() == ModItems.shimmer_sledge)
				Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.shimmer_sledge_tex);
			if(stack.getItem() == ModItems.shimmer_axe)
				Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.shimmer_axe_tex);
			if(stack.getItem() == ToolSets.stopsign)
				Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.stopsign_tex);
			if(stack.getItem() == ToolSets.sopsign)
				Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.sopsign_tex);
			if(stack.getItem() == ToolSets.chernobylsign)
				Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.chernobylsign_tex);
			if(stack.getItem() == ModItems.shimmer_sledge || stack.getItem() == ModItems.shimmer_axe) {
				GlStateManager.scale(1.5F, 1.5F, 1.5F);
				if(type == TransformType.FIRST_PERSON_RIGHT_HAND) {
					GlStateManager.translate(0.2F, 0.1F, 0.5F);
					GL11.glRotated(-45, 0, 0, 1);
					GL11.glRotated(180, 0, 1, 0);
				} else {
					GlStateManager.translate(0.5F, 0.1F, 0.5F);
					GL11.glRotated(45, 0, 0, 1);
				}
			} else {
				GlStateManager.scale(0.35F, 0.35F, 0.35F);
				if(type == TransformType.FIRST_PERSON_RIGHT_HAND) {
					GlStateManager.translate(-1.5, 0, 1);
					GL11.glRotated(90, 0, 1, 0);
					GL11.glRotated(45, 1, 0, 0);
				} else {
					GlStateManager.translate(4, 0, 1);
					GL11.glRotated(90, 0, 1, 0);
					GL11.glRotated(-45, 1, 0, 0);
					GL11.glRotated(5, 0, 0, 1);
				}
			}
			if(stack.getItem() == ModItems.shimmer_sledge)
				ResourceManager.shimmer_sledge.renderAll();
			if(stack.getItem() == ModItems.shimmer_axe)
				ResourceManager.shimmer_axe.renderAll();
			if(stack.getItem() == ToolSets.stopsign || stack.getItem() == ToolSets.sopsign || stack.getItem() == ToolSets.chernobylsign)
				ResourceManager.stopsign.renderAll();
			break;
		case THIRD_PERSON_LEFT_HAND:
		case THIRD_PERSON_RIGHT_HAND:
		case GROUND:
			GlStateManager.translate(0.3, 0, 0.3);
		case FIXED:
		case HEAD:
			if(stack.getItem() == ModItems.shimmer_sledge)
				Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.shimmer_sledge_tex);
			if(stack.getItem() == ModItems.shimmer_axe)
				Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.shimmer_axe_tex);
			if(stack.getItem() == ToolSets.stopsign)
				Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.stopsign_tex);
			if(stack.getItem() == ToolSets.sopsign)
				Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.sopsign_tex);
			if(stack.getItem() == ToolSets.chernobylsign)
				Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.chernobylsign_tex);
			if(stack.getItem() == ModItems.shimmer_sledge || stack.getItem() == ModItems.shimmer_axe) {
				GL11.glScaled(2.5, 2.5, 2.5);
				GlStateManager.translate(0.1, -0.1, 0.15);
				GL11.glRotated(90, 0, 1, 0);
			} else {
				GlStateManager.scale(0.35F, 0.35F, 0.35F);
				GlStateManager.translate(0.5, 0.5, 0.5);
			}
			if(stack.getItem() == ModItems.shimmer_sledge)
				ResourceManager.shimmer_sledge.renderAll();
			if(stack.getItem() == ModItems.shimmer_axe)
				ResourceManager.shimmer_axe.renderAll();
			if(stack.getItem() == ToolSets.stopsign || stack.getItem() == ToolSets.sopsign || stack.getItem() == ToolSets.chernobylsign)
				ResourceManager.stopsign.renderAll();
			break;
		default:
			break;
		}
	}
}
