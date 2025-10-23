package com.hbm.inventory.control_panel.controls;

import com.hbm.inventory.control_panel.*;
import com.hbm.inventory.control_panel.nodes.*;
import com.hbm.main.ClientProxy;
import com.hbm.main.ResourceManager;
import com.hbm.render.amlfrom1710.IModelCustom;
import com.hbm.render.amlfrom1710.Tessellator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KnobControl extends Control {

    private int positions = 2;

    public KnobControl(String name, ControlPanel panel) {
        super(name, panel);
        vars.put("value", new DataValueFloat(0));
        configMap.put("positions", new DataValueFloat(positions));
    }

    @Override
    public ControlType getControlType() {
        return ControlType.KNOB;
    }

    @Override
    public float[] getSize() {
        return new float[]{2, 2, .4F};
    }

    @Override
    public void applyConfigs(Map<String, DataValue> configs) {
        super.applyConfigs(configs);

        for (Map.Entry<String, DataValue> e : configMap.entrySet()) {
            switch (e.getKey()) {
                case "positions": {
                    positions = (int) e.getValue().getNumber();
                    break;
                }
            }
        }
    }

    @Override
    public void render() {
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.ctrl_knob_control_tex);
        Tessellator tes = Tessellator.instance;
        IModelCustom model = getModel();

        int value = (int) getVar("value").getNumber();

        tes.startDrawing(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        tes.setTranslation(posX, 0, posY);
        tes.setColorRGBA_F(1, 1, 1, 1);
        model.tessellatePart(tes, "base");
        tes.draw();

        GlStateManager.pushMatrix();
            Matrix4f rot_mat = new Matrix4f().rotate((float) -(value*((2*Math.PI)/11F)), new Vector3f(0, 1, 0));
            Matrix4f.mul(new Matrix4f().translate(new Vector3f(posX, -.04F, posY)), rot_mat, new Matrix4f()).store(ClientProxy.AUX_GL_BUFFER);
            ClientProxy.AUX_GL_BUFFER.rewind();
            GlStateManager.multMatrix(ClientProxy.AUX_GL_BUFFER);
            tes.startDrawing(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
            tes.setColorRGBA_F(1, 1, 1, 1);
            model.tessellatePart(tes, "knob");
            tes.draw();
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
            GlStateManager.translate(posX, .07F, posY);
            GlStateManager.scale(.028F, .028F, .028F);
            GL11.glNormal3f(0.0F, 0.0F, -1.0F);
            GlStateManager.rotate(90, 1, 0, 0);

            FontRenderer font = Minecraft.getMinecraft().fontRenderer;

            for (int i=0; i<positions; i++) {
                double angle = (Math.PI*2)/11F * i;
                float r = 28;
                double x = r * Math.cos(angle-Math.PI/2);
                double y = r * Math.sin(angle-Math.PI/2);
                font.drawString(Integer.toString(i), (float) (((i==10)?-6.5 : -2.5F)+x), (float) (-3F+y), 0x282828, false);
            }
        GlStateManager.popMatrix();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IModelCustom getModel() {
        return ResourceManager.ctrl_knob_control;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ResourceLocation getGuiTexture() {
        return ResourceManager.ctrl_knob_control_gui_tex;
    }

    @Override
    public List<String> getOutEvents() {
        return Collections.singletonList("ctrl_press");
    }

    @Override
    public void populateDefaultNodes(List<ControlEvent> receiveEvents) {
        NodeSystem ctrl_press = new NodeSystem(this);
        {
            Map<String, DataValue> vars = new HashMap<>(receiveEvents.get(0).vars);
            vars.put("from index", new DataValueFloat(0));
            NodeInput node0 = new NodeInput(170, 100, "Event Data").setVars(vars);
            ctrl_press.addNode(node0);
            NodeConditional node1 = new NodeConditional(230, 120);
            node1.inputs.get(0).setData(node0, 1, true);
            node1.inputs.get(1).setDefault(new DataValueFloat(-1));
            node1.inputs.get(2).setDefault(new DataValueFloat(1));
            ctrl_press.addNode(node1);
            NodeGetVar node2 = new NodeGetVar(170, 160, this).setData("value", false);
            ctrl_press.addNode(node2);
            NodeMath node3 = new NodeMath(290, 140).setData(NodeMath.Operation.ADD);
            node3.inputs.get(0).setData(node1, 0, true);
            node3.inputs.get(1).setData(node2, 0, true);
            ctrl_press.addNode(node3);
            NodeMath node4 = new NodeMath(350, 140).setData(NodeMath.Operation.CLAMP);
            node4.inputs.get(0).setData(node3, 0, true);
            node4.inputs.get(1).setDefault(new DataValueFloat(0));
            node4.inputs.get(2).setDefault(new DataValueFloat(10));
            ctrl_press.addNode(node4);
            NodeSetVar node5 = new NodeSetVar(410, 145, this).setData("value", false);
            node5.inputs.get(0).setData(node4, 0, true);
            ctrl_press.addNode(node5);
        }
        receiveNodeMap.put("ctrl_press", ctrl_press);
    }

    @Override
    public Control newControl(ControlPanel panel) {
        return new KnobControl(name, panel);

    }

}
