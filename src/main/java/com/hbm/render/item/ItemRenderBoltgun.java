package com.hbm.render.item;

import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.util.ViewModelPositonDebugger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.KHRDebug;

import java.nio.FloatBuffer;

@AutoRegister(item = "boltgun")
public class ItemRenderBoltgun extends TEISRBase {




    @Override
    public void renderByItem(ItemStack itemStackIn) {

        GlStateManager.pushMatrix();
        // Begin debug group
        if (GLContext.getCapabilities().GL_KHR_debug) {
            KHRDebug.glPushDebugGroup(
                    KHRDebug.GL_DEBUG_SOURCE_APPLICATION,
                    1,
                    "Boltgun render; 1.12.2; type = " + type
            );
        }


        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.boltgun_tex);


        EntityPlayer player = Minecraft.getMinecraft().player;

        GlStateManager.enableCull();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.boltgun_tex);
        switch (type) {
            case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {

                double s0 = 0.15D;

                FloatBuffer buf = BufferUtils.createFloatBuffer(16);
                Matrix4f FPE17 = new Matrix4f(
                        -0.035f, -0.190f, -0.570f, 0.000f,
                        -0.015f, 0.575f, -0.169f, 0.000f,
                        0.599f, 0.005f, -0.034f, 0.000f,
                        0.612f, -0.982f, -0.328f, 1.000f
                );
                Matrix4f M112 = new Matrix4f(
                        1.000f,   0.000f,  -0.000f,  0.000f,
                        0.000f,   1.000f,   0.000f,  0.000f,
                        0.000f,  -0.000f,   1.000f,  0.000f,
                        0.060f,  -1.522f,  -1.220f,  1.000f
                ).invert();

                Matrix4f multMatrix = new Matrix4f(M112).mul(FPE17);




                multMatrix.translate(0.5F, 0.35F, -0.25F);
                multMatrix.rotate((float)Math.toRadians(15), 0f, 0f, 1f);
                multMatrix.rotate((float)Math.toRadians(80), 0f, 1f, 0f);
                multMatrix.scale((float) s0);
                multMatrix.get(buf);
//                GlStateManager.loadIdentity();
                GlStateManager.multMatrix(buf);


//
//                GlStateManager.translate(0.5F, 0.35F, -0.25F);
//                GlStateManager.rotate(15F, 0F, 0F, 1F);
//                GlStateManager.rotate(80F, 0F, 1F, 0F);
//                GlStateManager.scale((float) s0, (float) s0, (float) s0);

                GlStateManager.pushMatrix();
                double[] anim = HbmAnimations.getRelevantTransformation("RECOIL", type == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
                GlStateManager.translate(0F, 0F, (float) -anim[0]);
                if (anim[0] != 0)
                    player.isSwingInProgress = false;



                ResourceManager.boltgun.renderPart("Barrel");

                GlStateManager.popMatrix();
            }


            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
                double scale = 0.1D;
                GlStateManager.scale((float) scale, (float) scale, (float) scale);
                GlStateManager.rotate(10F, 0F, 1F, 0F);
                GlStateManager.rotate(10F, 0F, 0F, 1F);
                GlStateManager.rotate(10F, 1F, 0F, 0F);
                GlStateManager.translate(1.5F, -0.25F, 1F);

            }
            case GROUND -> {
                double s1 = 0.1D;
                GlStateManager.scale((float) s1, (float) s1, (float) s1);

            }
            case GUI, FIXED -> {
                GlStateManager.enableAlpha();
                GlStateManager.enableLighting();

                double s = 1.75D;
                GlStateManager.translate(7F, 10F, 0F);
                GlStateManager.rotate(-90F, 0F, 1F, 0F);
                GlStateManager.rotate(-135F, 1F, 0F, 0F);
                GlStateManager.scale((float) s, (float) s, (float) -s);



            }
            default -> {
            }
        }


        ResourceManager.boltgun.renderPart("Gun");
        if (type != type.FIRST_PERSON_RIGHT_HAND && type != type.FIRST_PERSON_LEFT_HAND) {
            ResourceManager.boltgun.renderPart("Barrel");
        }
        GlStateManager.shadeModel(GL11.GL_FLAT);
        if (GLContext.getCapabilities().GL_KHR_debug) {
            KHRDebug.glPopDebugGroup();
        }

        GlStateManager.popMatrix();
    }


}
