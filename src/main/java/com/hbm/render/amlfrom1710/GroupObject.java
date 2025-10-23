package com.hbm.render.amlfrom1710;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

public class GroupObject
{
    public String name;
    public ArrayList<Face> faces = new ArrayList<Face>();
    public int glDrawingMode;

    public GroupObject()
    {
        this("");
    }

    public GroupObject(String name)
    {
        this(name, -1);
    }

    public GroupObject(String name, int glDrawingMode)
    {
        this.name = name;
        this.glDrawingMode = glDrawingMode;
    }

    @SideOnly(Side.CLIENT)
    public void render()
    {
        if (faces.size() > 0)
        {
        	
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawing(glDrawingMode);
            render(tessellator);
            tessellator.draw();
        }
    }

    @SideOnly(Side.CLIENT)
    public void render(Tessellator tessellator)
    {
        if (faces.size() > 0)
        {
            for (Face face : faces)
            {
                face.addFaceForRender(tessellator);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void renderSplit(float splitHeight, float scale)
    {
        if (faces.size() > 0)
        {
            
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawing(glDrawingMode);
            renderSplit(tessellator, splitHeight, scale);
            tessellator.draw();
        }
    }

    @SideOnly(Side.CLIENT)
    public void renderSplit(Tessellator tessellator, float splitHeight, float scale)
    {
        if (faces.size() > 0)
        {
            for (Face face : faces)
            {
                face.addFaceForRenderSplit(tessellator, 0F, splitHeight, scale);
            }
        }
    }
}