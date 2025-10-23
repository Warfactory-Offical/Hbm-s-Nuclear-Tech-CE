package com.hbm.inventory.control_panel.controls;

import com.hbm.inventory.control_panel.*;
import com.hbm.main.ClientProxy;
import com.hbm.main.ResourceManager;
import com.hbm.render.amlfrom1710.IModelCustom;
import com.hbm.render.amlfrom1710.Tessellator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.util.List;
import java.util.Map;

public class DialSquare extends Control {

    private String label = "POWER    (RS/10)";

    public DialSquare(String name, ControlPanel panel) {
        super(name, panel);
        vars.put("value", new DataValueFloat(0));
        configMap.put("label", new DataValueString(label));
    }

    @Override
    public ControlType getControlType() {
        return ControlType.DIAL;
    }

    @Override
    public float[] getSize() {
        return new float[] {2.25F, 2.25F, .4F};
    }

    @Override
    public void applyConfigs(Map<String, DataValue> configs) {
        super.applyConfigs(configs);

        for (Map.Entry<String, DataValue> e : configMap.entrySet()) {
            switch (e.getKey()) {
                case "label": {
                    label = e.getValue().toString();
                    break;
                }
            }
        }
    }

    @Override
    public void render() {
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.ctrl_dial_square_tex);
        Tessellator tes = Tessellator.instance;
        IModelCustom model = getModel();

        int value = (int) getVar("value").getNumber();

        tes.startDrawing(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        tes.setTranslation(posX, 0, posY);
        tes.setColorRGBA_F(1, 1, 1, 1);
        model.tessellatePart(tes, "base");
        tes.draw();

        GlStateManager.pushMatrix();
            Matrix4f rot_mat = new Matrix4f().rotate((float) -MathHelper.clamp((value*((Math.PI/2)/100F)), 0, Math.PI/2), new Vector3f(0, 1, 0));
            Matrix4f.mul(new Matrix4f().translate(new Vector3f(posX+.77F, 0, posY+.77F)), rot_mat, new Matrix4f()).store(ClientProxy.AUX_GL_BUFFER);
            ClientProxy.AUX_GL_BUFFER.rewind();
            GlStateManager.multMatrix(ClientProxy.AUX_GL_BUFFER);
            tes.startDrawing(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
            tes.setColorRGBA_F(1, 1, 1, 1);
            model.tessellatePart(tes, "dial");
            tes.draw();
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
            GlStateManager.translate(posX, .07F, posY);
            GlStateManager.scale(.023F, .023F, .023F);
            GL11.glNormal3f(0.0F, 0.0F, -1.0F);
            GlStateManager.rotate(90, 1, 0, 0);

            FontRenderer font = Minecraft.getMinecraft().fontRenderer;

            for (int i=0; i<11; i++) {
                double angle = (Math.PI/1.8)/11F * i;
                float r = 68;
                double x = r * Math.cos(angle-Math.PI);
                double y = r * Math.sin(angle-Math.PI);
                font.drawString((i%2 != 0)? "·" : Integer.toString(i), (float) (28+x), (float) (29.5F+y), 0x303030, false);
            }

            font.drawSplitString(label, -8, -5, 50, 0x303030);

        GlStateManager.popMatrix();
    }

    @Override
    public IModelCustom getModel() {
        return ResourceManager.ctrl_dial_square;
    }

    @Override
    public ResourceLocation getGuiTexture() {
        return ResourceManager.ctrl_dial_square_gui_tex;
    }

    @Override
    public Control newControl(ControlPanel panel) {
        return new DialSquare(name, panel);
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return null;
    }

    @Override
    public void populateDefaultNodes(List<ControlEvent> receiveEvents) {

    }
}
