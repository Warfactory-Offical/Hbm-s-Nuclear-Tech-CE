package com.hbm.hfr.render.loader;

import com.hbm.render.amlfrom1710.IModelCustom;
import com.hbm.render.amlfrom1710.TextureCoordinate;
import com.hbm.render.amlfrom1710.Vertex;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

public class WaveFrontObjectVAO implements IModelCustom {

    public static ArrayList<WaveFrontObjectVAO> allVBOs = new ArrayList<>();

    private static final int FLOAT_SIZE = 4;
    private static final int STRIDE = 9 * FLOAT_SIZE;
    static int VERTEX_SIZE = 3;
    List<VBOBufferData> groups = new ArrayList<>();
    public WaveFrontObjectVAO(HFRWavefrontObject obj) {
        for (S_GroupObject g : obj.groupObjects) {

            VBOBufferData data = new VBOBufferData();
            data.name = g.name;

            List<Float> vertexData = new ArrayList<>(g.faces.size() * 3 * VERTEX_SIZE);
            List<Float> uvwData = new ArrayList<>(g.faces.size() * 3 * VERTEX_SIZE);
            List<Float> normalData = new ArrayList<>(g.faces.size() * 3 * VERTEX_SIZE);


            for (S_Face face : g.faces) {
                for (int i = 0; i < face.vertices.length; i++) {
                    Vertex vert = face.vertices[i];
                    TextureCoordinate tex = new TextureCoordinate(0, 0);
                    Vertex normal = face.vertexNormals[i];

                    if (face.textureCoordinates != null && face.textureCoordinates.length > 0) {
                        tex = face.textureCoordinates[i];
                    }

                    data.vertices++;
                    vertexData.add(vert.x);
                    vertexData.add(vert.y);
                    vertexData.add(vert.z);

                    uvwData.add(tex.u);
                    uvwData.add(tex.v);
                    uvwData.add(tex.w);

                    normalData.add(normal.x);
                    normalData.add(normal.y);
                    normalData.add(normal.z);
                }
            }
            float[] combinedData = new float[data.vertices * 9];
            int dst = 0;
            for (int i = 0; i < data.vertices; i++) {
                combinedData[dst++] = vertexData.get(i * 3);
                combinedData[dst++] = vertexData.get(i * 3 + 1);
                combinedData[dst++] = vertexData.get(i * 3 + 2);

                combinedData[dst++] = uvwData.get(i * 3);
                combinedData[dst++] = uvwData.get(i * 3 + 1);
                combinedData[dst++] = uvwData.get(i * 3 + 2);

                combinedData[dst++] = normalData.get(i * 3);
                combinedData[dst++] = normalData.get(i * 3 + 1);
                combinedData[dst++] = normalData.get(i * 3 + 2);
            }


            FloatBuffer buffer = BufferUtils.createFloatBuffer(combinedData.length);
            buffer.put(combinedData);
            buffer.flip();


            data.vboHandle = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, data.vboHandle);
            glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER, 0);

            groups.add(data);
        }
        allVBOs.add(this);
    }

    public void generate_vaos(){
        for (VBOBufferData data : groups) {
            data.vaoHandle = GL30.glGenVertexArrays();
            GL30.glBindVertexArray(data.vaoHandle);
            glBindBuffer(GL_ARRAY_BUFFER, data.vboHandle);

            GL11.glVertexPointer(3, GL11.GL_FLOAT, STRIDE, 0L);
            glEnableClientState(GL_VERTEX_ARRAY);

            GL11.glTexCoordPointer(3, GL11.GL_FLOAT, STRIDE, 3L * Float.BYTES);
            glEnableClientState(GL_TEXTURE_COORD_ARRAY);

            GL11.glNormalPointer(GL11.GL_FLOAT, STRIDE, 6L * Float.BYTES);
            glEnableClientState(GL_NORMAL_ARRAY);

            GL30.glBindVertexArray(0);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
        }
    }

    @Override
    public String getType() {
        return "obj_vao";
    }

    private void renderVBO(VBOBufferData data) {
        GL30.glBindVertexArray(data.vaoHandle);
        GlStateManager.glDrawArrays(GL11.GL_TRIANGLES, 0, data.vertices);
        GL30.glBindVertexArray(0);
    }

    @Override
    public void renderAll() {
        for (VBOBufferData data : groups) {
            renderVBO(data);
        }
    }

    @Override
    public void renderOnly(String... groupNames) {
        for (VBOBufferData data : groups) {
            for (String name : groupNames) {
                if (data.name.equalsIgnoreCase(name)) {
                    renderVBO(data);
                }
            }
        }
    }

    @Override
    public void renderPart(String partName) {
        for (VBOBufferData data : groups) {
            if (data.name.equalsIgnoreCase(partName)) {
                renderVBO(data);
            }
        }
    }

    @Override
    public void renderAllExcept(String... excludedGroupNames) {
        for (VBOBufferData data : groups) {
            boolean skip = false;
            for (String name : excludedGroupNames) {
                if (data.name.equalsIgnoreCase(name)) {
                    skip = true;
                    break;
                }
            }
            if (!skip) {
                renderVBO(data);
            }
        }
    }



    class VBOBufferData {
        String name;
        int vertices = 0;
        int vboHandle;
        int vaoHandle;
    }

}
