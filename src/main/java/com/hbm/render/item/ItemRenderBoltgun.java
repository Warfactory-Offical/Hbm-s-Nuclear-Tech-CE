package com.hbm.render.item;

import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.item.weapon.sedna.ItemRenderWeaponBase;
import com.hbm.render.util.ViewModelPositonDebugger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL12;

@AutoRegister(item = "boltgun")
public class ItemRenderBoltgun extends ItemRenderWeaponBase {

//    ViewModelPositonDebugger offsets = new ViewModelPositonDebugger()
//            .get(TransformType.GUI)
//            .setScale(0.11f).setPosition(-4.15, 3.30, -3.35).setRotation(0, 135, -90)
//            .getHelper()
//            .get(TransformType.FIRST_PERSON_RIGHT_HAND)
//            .setPosition(-6.75, 0.55, 2.25).setRotation(80, 5, -180)
//            .getHelper()
//            .get(TransformType.FIRST_PERSON_LEFT_HAND)
//            .setPosition(-10.5, -1, 0).setRotation(180, 165, -180)
//            .getHelper()
//            .get(TransformType.THIRD_PERSON_RIGHT_HAND)
//            .setScale(0.1f).setPosition(-4.25, 5, -5.5).setRotation(-5, 90, 0)
//            .getHelper()
//            .get(TransformType.THIRD_PERSON_LEFT_HAND)
//            .setScale(1.03f).setPosition(-0.75, -0.25, 0.25).setRotation(5, 0, 0)
//            .getHelper()
//            .get(TransformType.GROUND)
//            .setPosition(-10, 10, -10).setRotation(0, 0, 0).setScale(0.05f)
//            .getHelper();
//    //Norwood: This is great and all but eulerian angles' order of rotation is important. You should probably use quaternions instead but I'm too lazy to do that.
//    //For now, just queue multiple rotations in the correct order. //TODO: Make angles use quaternions
//    ViewModelPositonDebugger.offset corrections = new ViewModelPositonDebugger.offset(offsets)
//            .setRotation(0, 5, 0);


    @Override
    public void renderByItem(ItemStack itemStackIn) {

        GlStateManager.pushMatrix();

        EntityPlayer player = Minecraft.getMinecraft().player;

        GlStateManager.enableCull();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.boltgun_tex);
        switch (type) {
            case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
                GlStateManager.pushMatrix();
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                GL11.glTranslatef(0.0F, -0.3F, 0.0F);
                GL11.glScalef(1.5F, 1.5F, 1.5F);
                GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
                GL11.glTranslatef(-0.9375F, -0.0625F, 0.0F);

                double s0 = 0.15D;
                GlStateManager.translate(0.5F, 0.35F, -0.25F);
                GlStateManager.rotate(15F, 0F, 0F, 1F);
                GlStateManager.rotate(80F, 0F, 1F, 0F);
                GlStateManager.scale((float) s0, (float) s0, (float) s0);

                double[] anim = HbmAnimations.getRelevantTransformation("RECOIL",EnumHand.MAIN_HAND);
                GlStateManager.translate(0F, 0F, (float) -anim[0]);
                if (anim[0] != 0) player.isSwingInProgress = false;
                ResourceManager.boltgun.renderPart("Barrel");
                GL11.glDisable(GL12.GL_RESCALE_NORMAL);
                GlStateManager.popMatrix();

            }
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {

                double scale = 0.25D;
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
            case GUI,FIXED -> {

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

        GlStateManager.popMatrix();
    }

    @Override
    public void renderFirstPerson(ItemStack stack) {

    }
}
