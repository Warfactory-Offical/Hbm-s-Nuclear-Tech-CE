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

public class DialLarge extends Control {

    private String label = "    POWER            (RS)";

    public DialLarge(String name, ControlPanel panel) {
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
        return new float[] {4.5F, 2.25F, .4F};
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
        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.ctrl_dial_large_tex);
        Tessellator tes = Tessellator.instance;
        IModelCustom model = getModel();

        int value = (int) getVar("value").getNumber();

        tes.startDrawing(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        tes.setTranslation(posX, 0, posY);
        tes.setColorRGBA_F(1, 1, 1, 1);
        model.tessellatePart(tes, "base");
        tes.draw();

        GlStateManager.pushMatrix();
        Matrix4f rot_mat = new Matrix4f().rotate((float) -MathHelper.clamp((value*(Math.PI/100F)), 0, Math.PI), new Vector3f(0, 1, 0));
        Matrix4f.mul(new Matrix4f().translate(new Vector3f(posX, 0, posY+.77F)), rot_mat, new Matrix4f()).store(ClientProxy.AUX_GL_BUFFER);
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
            double angle = (Math.PI*1.1F)/11F * i;
            float r = 68;
            double x = r * Math.cos(angle-Math.PI)+i;
            double y = r * Math.sin(angle-Math.PI);
            font.drawString(Integer.toString(i*10), (float) (x - ((i==10)? 14 : 10)), (float) (29.5F+y), 0x303030, false);
        }

        font.drawSplitString(label, -31, -10, 70, 0x303030);

        GlStateManager.popMatrix();
    }

    @Override
    public IModelCustom getModel() {
        return ResourceManager.ctrl_dial_large;
    }

    @Override
    public ResourceLocation getGuiTexture() {
        return ResourceManager.ctrl_dial_large_gui_tex;
    }

    @Override
    public Control newControl(ControlPanel panel) {
        return new DialLarge(name, panel);
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return null;
    }

    @Override
    public void populateDefaultNodes(List<ControlEvent> receiveEvents) {

    }
}
