package com.hbm.inventory.control_panel.controls;

import com.hbm.inventory.control_panel.*;
import com.hbm.inventory.control_panel.nodes.*;
import com.hbm.main.ClientProxy;
import com.hbm.main.ResourceManager;
import com.hbm.render.amlfrom1710.IModelCustom;
import com.hbm.render.amlfrom1710.Tessellator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.EnumDyeColor;
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


public class ButtonEncasedPush extends Control {

    public ButtonEncasedPush(String name, ControlPanel panel) {
        super(name, panel);
        vars.put("isPushed", new DataValueFloat(0));
        vars.put("isLit", new DataValueFloat(0));
        vars.put("isCoverOpen", new DataValueFloat(0));
    }

    @Override
    public ControlType getControlType() {
        return ControlType.BUTTON;
    }

    @Override
    public float[] getSize() {
        return new float[] {1.5F, 1.5F, 1F};
    }

    @Override
    public void render() {
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.ctrl_button_encased_push_tex);
        Tessellator tes = Tessellator.instance;
        IModelCustom model = getModel();

        boolean isPushed = getVar("isPushed").getBoolean();
        boolean isLit = getVar("isLit").getBoolean();
        boolean isCoverOpen = getVar("isCoverOpen").getBoolean();

        float lX = OpenGlHelper.lastBrightnessX;
        float lY = OpenGlHelper.lastBrightnessY;

        tes.startDrawing(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        tes.setTranslation(posX, 0, posY);
        tes.setColorRGBA_F(1, 1, 1, 1);
        model.tessellatePart(tes, "base");
        tes.draw();

        if (isLit) {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
        }

        float[] color = EnumDyeColor.RED.getColorComponentValues();
        float cMul = 0.6F;
        if (isLit) {
            cMul = 1;
        }

        tes.startDrawing(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        tes.setTranslation(posX, (isPushed)?-.1F:0, posY);
        tes.setColorRGBA_F(color[0]*cMul, color[1]*cMul, color[2]*cMul, 1F);
        model.tessellatePart(tes, "btn_top");
        tes.draw();

        tes.startDrawing(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        tes.setTranslation(posX, (isPushed)?-.1F:0, posY);
        tes.setColorRGBA_F(color[0]*cMul, color[1]*cMul, color[2]*cMul, 1F);
        model.tessellatePart(tes, "btn_top_top");
        tes.draw();

        if (isLit) {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lX, lY);
        }

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        Matrix4f rot_mat = new Matrix4f().rotate((float) ((isCoverOpen) ? Math.toRadians(-75) : 0), new Vector3f(1, 0, 0));
        Matrix4f trans_mat = new Matrix4f().translate(new Vector3f(posX, .625F, posY-.75F));
        Matrix4f transform_mat = new Matrix4f();
        Matrix4f.mul(trans_mat, rot_mat, transform_mat);
        transform_mat.store(ClientProxy.AUX_GL_BUFFER);
        ClientProxy.AUX_GL_BUFFER.rewind();
        GlStateManager.multMatrix(ClientProxy.AUX_GL_BUFFER);

        tes.startDrawing(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        tes.setColorRGBA_F(1, 1, 1, 1);
        model.tessellatePart(tes, "cover");
        tes.draw();

        tes.startDrawing(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        tes.setColorRGBA_F(1, 1, 1, 1);
        model.tessellatePart(tes, "cover2");
        tes.draw();

        GlStateManager.disableBlend();
        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IModelCustom getModel() {
        return ResourceManager.ctrl_button_encased_push;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ResourceLocation getGuiTexture() {
        return ResourceManager.ctrl_button_encased_push_gui_tex;
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
            NodeFunction node1 = new NodeFunction(230, 80);
            NodeSystem node1_subsystem = new NodeSystem(this);
            {
                NodeGetVar node1_0 = new NodeGetVar(170, 100, this).setData("isCoverOpen", false);
                node1_subsystem.addNode(node1_0);
                NodeBoolean node1_1 = new NodeBoolean(230, 120).setData(NodeBoolean.BoolOperation.NOT);
                node1_1.inputs.get(0).setData(node1_0, 0, true);
                node1_subsystem.addNode(node1_1);
                NodeSetVar node1_2 = new NodeSetVar(290, 140, this).setData("isCoverOpen", false);
                node1_2.inputs.get(0).setData(node1_1, 0, true);
                node1_subsystem.addNode(node1_2);
            }
            node1.inputs.get(0).setData(node0, 1, true);
            ctrl_press.subSystems.put(node1, node1_subsystem);
            ctrl_press.addNode(node1);
            NodeBoolean node2 = new NodeBoolean(230, 120).setData(NodeBoolean.BoolOperation.NOT);
            node2.inputs.get(0).setData(node0, 1, true);
            ctrl_press.addNode(node2);
            NodeGetVar node3 = new NodeGetVar(170, 160, this).setData("isCoverOpen", false);
            ctrl_press.addNode(node3);
            NodeBoolean node4 = new NodeBoolean(290, 130).setData(NodeBoolean.BoolOperation.AND);
            node4.inputs.get(0).setData(node2, 0, true);
            node4.inputs.get(1).setData(node3, 0, true);
            ctrl_press.addNode(node4);
            NodeFunction node5 = new NodeFunction(350, 140);
            NodeSystem node5_subsystem = new NodeSystem(this);
            {
                NodeGetVar node5_0 = new NodeGetVar(170, 100, this).setData("isPushed", false);
                node5_subsystem.addNode(node5_0);
                NodeBoolean node5_1 = new NodeBoolean(230, 120).setData(NodeBoolean.BoolOperation.NOT);
                node5_1.inputs.get(0).setData(node5_0, 0, true);
                node5_subsystem.addNode(node5_1);
                NodeSetVar node5_2 = new NodeSetVar(290, 140, this).setData("isPushed", false);
                node5_2.inputs.get(0).setData(node5_1, 0, true);
                node5_subsystem.addNode(node5_2);
                NodeSetVar node5_3 = new NodeSetVar(290, 100, this).setData("isLit", false);
                node5_3.inputs.get(0).setDefault(new DataValueFloat(1));
                node5_subsystem.addNode(node5_3);
            }
            node5.inputs.get(0).setData(node4, 0, true);
            ctrl_press.subSystems.put(node5, node5_subsystem);
            ctrl_press.addNode(node5);
        }
        receiveNodeMap.put("ctrl_press", ctrl_press);
        NodeSystem tick = new NodeSystem(this);
        {
            NodeGetVar node0 = new NodeGetVar(170, 100, this).setData("isPushed", false);
            tick.addNode(node0);
            NodeBuffer node1 = new NodeBuffer(230, 120);
            node1.inputs.get(0).setData(node0, 0, true);
            node1.inputs.get(1).setDefault(new DataValueFloat(15));
            tick.addNode(node1);
            NodeFunction node2 = new NodeFunction(290, 140);
            NodeSystem node2_subsystem = new NodeSystem(this);
            {
                NodeSetVar node2_0 = new NodeSetVar(290, 100, this).setData("isPushed", false);
                node2_subsystem.addNode(node2_0);
                NodeSetVar node2_1 = new NodeSetVar(290, 140, this).setData("isLit", false);
                node2_subsystem.addNode(node2_1);
            }
            node2.inputs.get(0).setData(node1, 0, true);
            tick.subSystems.put(node2, node2_subsystem);
            tick.addNode(node2);
        }
        receiveNodeMap.put("tick", tick);
    }

    @Override
    public Control newControl(ControlPanel panel) {
        return new ButtonEncasedPush(name, panel);
    }
}
