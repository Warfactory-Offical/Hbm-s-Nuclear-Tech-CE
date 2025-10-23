package com.hbm.render.amlfrom1710;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;

import java.nio.*;
import java.util.PriorityQueue;

@SideOnly(Side.CLIENT)
// Norwood: DO NOT FUCKING USE THIS. It doesn't work well, its incompatible with 1.12 render and provides no value in the longterm.
// We should forgo any of this stuff. Base minecraft uses VBO with its vertex buffers nowdays, and uses renderlists when VBOs are disabled.
// And no, you can't just copy-paste stuff either as it breaks. If you struggle I propose a wrapper to make our time easier.
// Please take your time to port stuff properly and address issues using minecraft renderer, not this frankestein  abomination
//On top of that I have depricated 90% of stuff that uses it.
@Deprecated()
public class Tessellator
{
    private static int nativeBufferSize = 0x200000;
    private static int trivertsInBuffer = (nativeBufferSize / 48) * 6;
    public static boolean renderingWorldRenderer = false;
    public boolean defaultTexture = false;
    private int rawBufferSize = 0;
    public int textureID = 0;

    /** The byte buffer used for GL allocation. */
    private static ByteBuffer byteBuffer = GLAllocation.createDirectByteBuffer(nativeBufferSize * 4);
    /** The same memory as byteBuffer, but referenced as an integer buffer. */
    private static IntBuffer intBuffer = byteBuffer.asIntBuffer();
    /** The same memory as byteBuffer, but referenced as an float buffer. */
    private static FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
    /** The same memory as byteBuffer, but referenced as an short buffer. */
    private static ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
    /** Raw integer array. */
    private int[] rawBuffer;
    /** The number of vertices to be drawn in the next draw call. Reset to 0 between draw calls. */
    private int vertexCount;
    /** The first coordinate to be used for the texture. */
    private double textureU;
    /** The second coordinate to be used for the texture. */
    private double textureV;
    private int brightness;
    /** The color (RGBA) value to be used for the following draw call. */
    private int color;
    /** Whether the current draw object for this tessellator has color values. */
    public boolean hasColor;
    /** Whether the current draw object for this tessellator has texture coordinates. */
    private boolean hasTexture;
    private boolean hasBrightness;
    /** Whether the current draw object for this tessellator has normal values. */
    public boolean hasNormals;
    /** The index into the raw buffer to be used for the next data. */
    private int rawBufferIndex;
    /**
     * The number of vertices manually added to the given draw call. This differs from vertexCount because it adds extra
     * vertices when converting quads to triangles.
     */
    private int addedVertices;
    /** Disables all color information for the following draw call. */
    private boolean isColorDisabled;
    /** The draw mode currently being used by the tessellator. */
    private int drawMode;
    /** An offset to be applied along the x-axis for all vertices in this draw call. */
    private double xOffset;
    /** An offset to be applied along the y-axis for all vertices in this draw call. */
    private double yOffset;
    /** An offset to be applied along the z-axis for all vertices in this draw call. */
    private double zOffset;
    /** The normal to be applied to the face being drawn. */
    private int normal;
    /** The static instance of the Tessellator. */
    public static final Tessellator instance = new Tessellator(2097152);
    /** Whether this tessellator is currently in draw mode. */
    private boolean isDrawing;
    /** The size of the buffers used (in integers). */
    private int bufferSize;

    private Tessellator(int p_i1250_1_) {
    }

    public Tessellator(){
    }

    static
    {
        instance.defaultTexture = true;
    }

    /**
     * Draws the data set up in this tessellator and resets the state to prepare for new drawing.
     */
    public int draw()
    {
        hasColor = false;
        hasNormals = false;
        this.xOffset = 0;
        this.yOffset = 0;
        this.zOffset = 0;
        net.minecraft.client.renderer.Tessellator.getInstance().draw();
        return 1;
    }

    public TesselatorVertexState getVertexState(float p_147564_1_, float p_147564_2_, float p_147564_3_)
    {
        int[] aint = new int[this.rawBufferIndex];
        PriorityQueue priorityqueue = new PriorityQueue(this.rawBufferIndex, new QuadComparator(this.rawBuffer, p_147564_1_ + (float)this.xOffset, p_147564_2_ + (float)this.yOffset, p_147564_3_ + (float)this.zOffset));
        byte b0 = 32;
        int i;

        for (i = 0; i < this.rawBufferIndex; i += b0)
        {
            priorityqueue.add(Integer.valueOf(i));
        }

        for (i = 0; !priorityqueue.isEmpty(); i += b0)
        {
            int j = ((Integer)priorityqueue.remove()).intValue();

            for (int k = 0; k < b0; ++k)
            {
                aint[i + k] = this.rawBuffer[j + k];
            }
        }

        System.arraycopy(aint, 0, this.rawBuffer, 0, aint.length);
        return new TesselatorVertexState(aint, this.rawBufferIndex, this.vertexCount, this.hasTexture, this.hasBrightness, this.hasNormals, this.hasColor);
    }

    public void setVertexState(TesselatorVertexState p_147565_1_)
    {
        while (p_147565_1_.getRawBuffer().length > rawBufferSize && rawBufferSize > 0)
        {
            rawBufferSize <<= 1;
        }
        if (rawBufferSize > rawBuffer.length)
        {
            rawBuffer = new int[rawBufferSize];
        }
        System.arraycopy(p_147565_1_.getRawBuffer(), 0, this.rawBuffer, 0, p_147565_1_.getRawBuffer().length);
        this.rawBufferIndex = p_147565_1_.getRawBufferIndex();
        this.vertexCount = p_147565_1_.getVertexCount();
        this.hasTexture = p_147565_1_.getHasTexture();
        this.hasBrightness = p_147565_1_.getHasBrightness();
        this.hasColor = p_147565_1_.getHasColor();
        this.hasNormals = p_147565_1_.getHasNormals();
    }

    /**
     * Clears the tessellator state in preparation for new drawing.
     */
    private void reset()
    {
        this.vertexCount = 0;
        this.byteBuffer.clear();
        this.rawBufferIndex = 0;
        this.addedVertices = 0;
    }

    /**
     * Sets draw mode in the tessellator to draw quads.
     */
    public void startDrawingQuads()
    {
        this.startDrawing(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
    }

    public void startDrawingQuadsColor(){
        startDrawing(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
    }
    
    public void startDrawing(int mode){
        startDrawing(mode, DefaultVertexFormats.POSITION_TEX_NORMAL);
    }
    
    /**
     * Resets tessellator state and prepares for drawing (with the specified draw mode).
     */
    public void startDrawing(int glMode, VertexFormat format)
    {
       /* if (this.isDrawing)
        {
            throw new IllegalStateException("Already tesselating!");
        }
        else
        {
            this.isDrawing = true;
            this.reset();
            this.drawMode = p_78371_1_;
            this.hasNormals = false;
            this.hasColor = false;
            this.hasTexture = false;
            this.hasBrightness = false;
        }*/
        this.isColorDisabled = false;
        net.minecraft.client.renderer.Tessellator.getInstance().getBuffer().begin(glMode, format);
    }

    /**
     * Sets the texture coordinates.
     */
    public void setTextureUV(double p_78385_1_, double p_78385_3_)
    {
        this.hasTexture = true;
        this.textureU = p_78385_1_;
        this.textureV = p_78385_3_;
    }

    public void setBrightness(int p_78380_1_)
    {
        this.hasBrightness = true;
        this.brightness = p_78380_1_;
    }

    /**
     * Sets the RGB values as specified, converting from floats between 0 and 1 to integers from 0-255.
     */
    public void setColorOpaque_F(float p_78386_1_, float p_78386_2_, float p_78386_3_)
    {
        this.setColorOpaque((int)(p_78386_1_ * 255.0F), (int)(p_78386_2_ * 255.0F), (int)(p_78386_3_ * 255.0F));
    }

    /**
     * Sets the RGBA values for the color, converting from floats between 0 and 1 to integers from 0-255.
     */
    public void setColorRGBA_F(float p_78369_1_, float p_78369_2_, float p_78369_3_, float p_78369_4_)
    {
        this.setColorRGBA((int)(p_78369_1_ * 255.0F), (int)(p_78369_2_ * 255.0F), (int)(p_78369_3_ * 255.0F), (int)(p_78369_4_ * 255.0F));
    }

    /**
     * Sets the RGB values as specified, and sets alpha to opaque.
     */
    public void setColorOpaque(int p_78376_1_, int p_78376_2_, int p_78376_3_)
    {
        this.setColorRGBA(p_78376_1_, p_78376_2_, p_78376_3_, 255);
    }

    private int r, g, b, a;
    
    /**
     * Sets the RGBA values for the color. Also clamps them to 0-255.
     */
    public void setColorRGBA(int p_78370_1_, int p_78370_2_, int p_78370_3_, int p_78370_4_)
    {
        if (!this.isColorDisabled)
        {
            if (p_78370_1_ > 255)
            {
                p_78370_1_ = 255;
            }

            if (p_78370_2_ > 255)
            {
                p_78370_2_ = 255;
            }

            if (p_78370_3_ > 255)
            {
                p_78370_3_ = 255;
            }

            if (p_78370_4_ > 255)
            {
                p_78370_4_ = 255;
            }

            if (p_78370_1_ < 0)
            {
                p_78370_1_ = 0;
            }

            if (p_78370_2_ < 0)
            {
                p_78370_2_ = 0;
            }

            if (p_78370_3_ < 0)
            {
                p_78370_3_ = 0;
            }

            if (p_78370_4_ < 0)
            {
                p_78370_4_ = 0;
            }

            this.hasColor = true;
            
            r = p_78370_1_;
            g = p_78370_2_;
            b = p_78370_3_;
            a = p_78370_4_;

            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN)
            {
                this.color = p_78370_4_ << 24 | p_78370_3_ << 16 | p_78370_2_ << 8 | p_78370_1_;
            }
            else
            {
                this.color = p_78370_1_ << 24 | p_78370_2_ << 16 | p_78370_3_ << 8 | p_78370_4_;
            }
        }
    }

    public void func_154352_a(byte p_154352_1_, byte p_154352_2_, byte p_154352_3_)
    {
        this.setColorOpaque(p_154352_1_ & 255, p_154352_2_ & 255, p_154352_3_ & 255);
    }

    /**
     * Adds a vertex specifying both x,y,z and the texture u,v for it.
     */
    public void addVertexWithUV(double x, double y, double z, double u, double v)
    {
        BufferBuilder buf = net.minecraft.client.renderer.Tessellator.getInstance().getBuffer();
        if (buf == null) {
            System.out.println("BufferBuilder is null");
            return;
        }
        buf.pos(x+xOffset, y+yOffset, z+zOffset).tex(u, v);
        if(hasColor)
            buf.color(r, g, b, a);
        if(hasNormals)
            buf.normal(normalTestX, normalTestY, normalTestZ);
        buf.endVertex();
        /*this.setTextureUV(p_78374_7_, p_78374_9_);
        this.addVertex(p_78374_1_, p_78374_3_, p_78374_5_);*/
    }

    /**
     * Adds a vertex with the specified x,y,z to the current draw call. It will trigger a draw() if the buffer gets
     * full.
     */
    public void addVertex(double x, double y, double z)
    {
      BufferBuilder buf = net.minecraft.client.renderer.Tessellator.getInstance().getBuffer();
      buf.pos(x+xOffset, y+yOffset, z+zOffset);
       if(hasColor)
               buf.color(r, g, b, a);
  
       if(hasNormals)
           buf.normal(normalTestX, normalTestY, normalTestZ);
           
          buf.endVertex();
       
       /* if (rawBufferIndex >= rawBufferSize - 32) 
        {
            if (rawBufferSize == 0)
            {
                rawBufferSize = 0x10000;
                rawBuffer = new int[rawBufferSize];
            }
            else
            {
                rawBufferSize *= 2;
                rawBuffer = Arrays.copyOf(rawBuffer, rawBufferSize);
            }
        }
        ++this.addedVertices;
        if (this.hasTexture)
        {
            this.rawBuffer[this.rawBufferIndex + 3] = Float.floatToRawIntBits((float)this.textureU);
            this.rawBuffer[this.rawBufferIndex + 4] = Float.floatToRawIntBits((float)this.textureV);
        }
        if (this.hasBrightness)
        {
            this.rawBuffer[this.rawBufferIndex + 7] = this.brightness;
        }
        if (this.hasColor)
        {
            this.rawBuffer[this.rawBufferIndex + 5] = this.color;
        }
        if (this.hasNormals)
        {
            this.rawBuffer[this.rawBufferIndex + 6] = this.normal;
        }
        this.rawBuffer[this.rawBufferIndex + 0] = Float.floatToRawIntBits((float)(p_78377_1_ + this.xOffset));
        this.rawBuffer[this.rawBufferIndex + 1] = Float.floatToRawIntBits((float)(p_78377_3_ + this.yOffset));
        this.rawBuffer[this.rawBufferIndex + 2] = Float.floatToRawIntBits((float)(p_78377_5_ + this.zOffset));
        this.rawBufferIndex += 8;
        ++this.vertexCount;*/
    }

    /**
     * Sets the color to the given opaque value (stored as byte values packed in an integer).
     */
    public void setColorOpaque_I(int p_78378_1_)
    {
        int j = p_78378_1_ >> 16 & 255;
        int k = p_78378_1_ >> 8 & 255;
        int l = p_78378_1_ & 255;
        this.setColorOpaque(j, k, l);
    }

    /**
     * Sets the color to the given color (packed as bytes in integer) and alpha values.
     */
    public void setColorRGBA_I(int p_78384_1_, int p_78384_2_)
    {
        int k = p_78384_1_ >> 16 & 255;
        int l = p_78384_1_ >> 8 & 255;
        int i1 = p_78384_1_ & 255;
        this.setColorRGBA(k, l, i1, p_78384_2_);
    }

    /**
     * Disables colors for the current draw call.
     */
    public void disableColor()
    {
        this.isColorDisabled = true;
    }

    private float normalTestX, normalTestY, normalTestZ;
    
    /**
     * Sets the normal for the current draw call.
     */
    public void setNormal(float x, float y, float z)
    {
        this.hasNormals = true;
      //  byte b0 = (byte)((int)(p_78375_1_ * 127.0F));
       // byte b1 = (byte)((int)(p_78375_2_ * 127.0F));
       // byte b2 = (byte)((int)(p_78375_3_ * 127.0F));
       // this.normal = b0 & 255 | (b1 & 255) << 8 | (b2 & 255) << 16;
        normalTestX = x;
        normalTestY = y;
        normalTestZ = z;
    }

    /**
     * Sets the translation for all vertices in the current draw call.
     */
    public void setTranslation(double p_78373_1_, double p_78373_3_, double p_78373_5_)
    {
        this.xOffset = p_78373_1_;
        this.yOffset = p_78373_3_;
        this.zOffset = p_78373_5_;
    }

    /**
     * Offsets the translation for all vertices in the current draw call.
     */
    public void addTranslation(float p_78372_1_, float p_78372_2_, float p_78372_3_)
    {
        this.xOffset += (double)p_78372_1_;
        this.yOffset += (double)p_78372_2_;
        this.zOffset += (double)p_78372_3_;
    }
}
