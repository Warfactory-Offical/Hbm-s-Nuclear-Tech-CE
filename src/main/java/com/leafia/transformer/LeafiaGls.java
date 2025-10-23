package com.leafia.transformer;

//import com.hbm.core.leafia.TransformerCoreLeafia;
import com.hbm.render.amlfrom1710.CompositeBrush;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Yeah uh this is bullsh*t i could have just used GL11.getState which i didn't know exis
public class LeafiaGls extends GlStateManager {
	public static class LeafiaGlStack {
		// Those are intended only for reading, do not override them.
		// If you really have to, don't forget that you have to call LeafiaGlStack.commit(); in order for them to take effect.
		public boolean blend; public boolean alpha; public boolean fog; public boolean lighting; public boolean colormat; public boolean tex2d;
		// shader.preinit
		public boolean colorlogic; public boolean cull; public boolean norm; public boolean polyoffset; public boolean rescNorm; public boolean depthtest;
		// lttle stuff i found

		public boolean depthmask;
		public int depthfunc;
		public int[] blendfuncs = new int[4];
		public int alphafuncF;
		public float alphafuncR;

		public int shademdl;

		Map<Integer,Boolean> capstate = new HashMap<>();

		public boolean[] lightState = new boolean[8];

		public FogMode fogMode = FogMode.EXP;
		public float fogDensity = 1.0f;
		public float fogStart = 0.0f;
		public float fogEnd = 1.0f;
		//public float fogIndex = 0.0f;
		//public FloatBuffer fogColor = r,g,b,a: 0,0,0,0; just for in case we wanted it but then forgor

		/*int[] attribs = new int[0];

		public void tryAddAttrib(int attrib) {
			for (int i : attribs) {
				if (i == attrib) return;
			}
			int[] newAttribs = new int[attribs.length+1];
			System.arraycopy(attribs,0,newAttribs,0,attribs.length);
			newAttribs[newAttribs.length-1] = attrib;
			attribs = newAttribs;
		}*/ // fuck off what the fuck is this

		public void commit() {
			LeafiaGls.preventStackSyncing = true;
			{
				//this\.(\w+) != record\.tex2d\) \{ if \(this\.tex2d\)
				//this.$1 != record.$1) { if (this.$1)
				if (this.blend != record.blend) { if (this.blend) GlStateManager.enableBlend(); else GlStateManager.disableBlend(); }
				if (this.alpha != record.alpha) { if (this.alpha) GlStateManager.enableAlpha(); else GlStateManager.disableAlpha(); }
				if (this.fog != record.fog) { if (this.fog) GlStateManager.enableFog(); else GlStateManager.disableFog(); }
				if (this.lighting != record.lighting) { if (this.lighting) GlStateManager.enableLighting(); else GlStateManager.disableLighting(); }
				if (this.colormat != record.colormat) { if (this.colormat) GlStateManager.enableColorMaterial(); else GlStateManager.disableColorMaterial(); }
				if (this.tex2d != record.tex2d) { if (this.tex2d) GlStateManager.enableTexture2D(); else GlStateManager.disableTexture2D(); }

				if (this.colorlogic != record.colorlogic) { if (this.colorlogic) GlStateManager.enableColorLogic(); else GlStateManager.disableColorLogic(); }
				if (this.cull != record.cull) { if (this.cull) GlStateManager.enableCull(); else GlStateManager.disableCull(); }
				if (this.norm != record.norm) { if (this.norm) GlStateManager.enableNormalize(); else GlStateManager.disableNormalize(); }
				//if (this.tex2d != record.tex2d) { if (this.tex2d) GlStateManager.(); else GlStateManager.disableOutlineMode(); }
				if (this.polyoffset != record.polyoffset) { if (this.polyoffset) GlStateManager.enablePolygonOffset(); else GlStateManager.disablePolygonOffset(); }
				if (this.rescNorm != record.rescNorm) { if (this.rescNorm) GlStateManager.enableRescaleNormal(); else GlStateManager.disableRescaleNormal(); }
				if (this.depthtest != record.depthtest) { if (this.depthtest) GlStateManager.enableDepth(); else GlStateManager.disableDepth(); }
			}
			if (this.depthmask != record.depthmask) GlStateManager.depthMask(this.depthmask);
			if (this.depthfunc != record.depthfunc) GlStateManager.depthFunc(this.depthfunc);
			for (int i = 0; i < 4; i++) {
				if (this.blendfuncs[i] != record.blendfuncs[i]) {
					if ((this.blendfuncs[0] == this.blendfuncs[2]) && (this.blendfuncs[1] == this.blendfuncs[3]))
						GlStateManager.blendFunc(this.blendfuncs[0],this.blendfuncs[1]);
					else
						GlStateManager.tryBlendFuncSeparate(this.blendfuncs[0],this.blendfuncs[1],this.blendfuncs[2],this.blendfuncs[3]);
					break;
				}
			}
			if ((this.alphafuncF != record.alphafuncF) || (this.alphafuncR != record.alphafuncR)) GlStateManager.alphaFunc(this.alphafuncF,this.alphafuncR);
			for (int i = 0; i < 8; i++) {
				if (this.lightState[i] != record.lightState[i]) {
					if (this.lightState[i])
						GlStateManager.enableLight(i);
					else
						GlStateManager.disableLight(i);
				}
			}
			if (this.shademdl != record.shademdl)
				GlStateManager.shadeModel(this.shademdl);
			for (Integer cap : this.capstate.keySet()) {
				boolean enabled = this.capstate.get(cap);
				if ((!record.capstate.containsKey(cap)) || (enabled != record.capstate.get(cap))) {
					if (enabled)
						GlStateManager.glEnableClientState(cap);
					else
						GlStateManager.glDisableClientState(cap);
				}
			}
			{
				if (this.fogMode != record.fogMode)
					GlStateManager.setFog(this.fogMode);
				if (this.fogDensity != record.fogDensity)
					GlStateManager.setFogDensity(this.fogDensity);
				if (this.fogStart != record.fogStart)
					GlStateManager.setFogStart(this.fogStart);
				if (this.fogEnd != record.fogEnd)
					GlStateManager.setFogEnd(this.fogEnd);
			}
			LeafiaGls.preventStackSyncing = false;
		}
		protected LeafiaGlStack reflect(LeafiaGlStack stack) {
			//this.(\w+) = stack.(\w+)
			//this.$1 = stack.$1
			{
				{
					this.blend = stack.blend;
					this.alpha = stack.alpha;
					this.fog = stack.fog;
					this.lighting = stack.lighting;
					this.colormat = stack.colormat;
					this.tex2d = stack.tex2d;
				}
				{
					this.colorlogic = stack.colorlogic;
					this.cull = stack.cull;
					this.norm = stack.norm;
					this.polyoffset = stack.polyoffset;
					this.rescNorm = stack.rescNorm;
					this.depthtest = stack.depthtest;
				}
			}
			this.depthmask = stack.depthmask;
			this.depthfunc = stack.depthfunc;
			for (int i = 0; i < 4; i++)
				this.blendfuncs[i] = stack.blendfuncs[i];
			this.alphafuncF = stack.alphafuncF; this.alphafuncR = stack.alphafuncR;
			for (int i = 0; i < 8; i++)
				this.lightState[i] = stack.lightState[i];
			this.shademdl = stack.shademdl;
			this.capstate.clear();
			for (Integer cap : stack.capstate.keySet()) {
				this.capstate.put(cap,stack.capstate.get(cap));
			}
			{
				this.fogMode = stack.fogMode;
				this.fogDensity = stack.fogDensity;
				this.fogStart = stack.fogStart;
				this.fogEnd = stack.fogEnd;
			}
			return this;
		}
	}
	static LeafiaGlStack record = new LeafiaGlStack();
	protected static boolean preventStackSyncing = false;
	public static class Handler {
		public Handler() {
			// Fuck you Minecraft library and all the private variables. Fuck you.
		}
		public static void updateLastStack() {
			if (preventStackSyncing) return;
			getLastStack().reflect(record);
		}

		//enable|disable series
		public static void recBlend(boolean state) { record.blend = state; }
		public static void recAlpha(boolean state) { record.alpha = state; }
		public static void recFog(boolean state) { record.fog = state; }
		public static void recLighting(boolean state) { record.lighting = state; }
		public static void recColorMaterial(boolean state) { record.colormat = state; }
		public static void recTexture2D(boolean state) { record.tex2d = state; }

		public static void recColorLogic(boolean state) { record.colorlogic = state; }
		public static void recCull(boolean state) { record.cull = state; }
		public static void recNormalize(boolean state) { record.norm = state; }
		public static void recPolygonOffset(boolean state) { record.polyoffset = state; }
		public static void recRescaleNormal(boolean state) { record.rescNorm = state; }
		public static void recDepth(boolean state) { record.depthtest = state; }

		public static void depthMask(boolean flag) { record.depthmask = flag; }
		public static void depthFunc(int func) { record.depthfunc = func; }
		public static void blendFunc(int srcFactor, int dstFactor) {
			record.blendfuncs[0] = srcFactor;
			record.blendfuncs[1] = dstFactor;
			record.blendfuncs[2] = srcFactor;
			record.blendfuncs[3] = dstFactor;
		}
		public static void tryBlendFuncSeparate(int srcFactor, int dstFactor, int srcFactorAlpha, int dstFactorAlpha) {
			record.blendfuncs[0] = srcFactor;
			record.blendfuncs[1] = dstFactor;
			record.blendfuncs[2] = srcFactorAlpha;
			record.blendfuncs[3] = dstFactorAlpha;
		}
		public static void alphaFunc(int func, float ref) {
			record.alphafuncF = func;
			record.alphafuncR = ref;
		}
		public static void recLight(int light,boolean enabled)
		{
			record.lightState[light] = enabled;
		}
		public static void shadeModel(int mode) {
			record.shademdl = mode;
		}
		public static void recClientState(int cap,boolean enabled) {
			record.capstate.put(cap,enabled);
		}

		public static void setFog(FogMode mode) { record.fogMode = mode; }
		public static void glFogi(int param,int value) {
			switch(param) {
				case GL11.GL_FOG_MODE:
					switch(value) {
						case GL11.GL_EXP: record.fogMode = FogMode.EXP; break;
						case GL11.GL_EXP2: record.fogMode = FogMode.EXP2; break;
						case GL11.GL_LINEAR: record.fogMode = FogMode.LINEAR; break;
					}
					break;
				case GL11.GL_FOG_INDEX:
					break;
			}
		}
		public static void glFog(int param,FloatBuffer buffer) { } // forget it
		public static void setFogStart(float value) { record.fogStart = value; }
		public static void setFogEnd(float value) { record.fogEnd = value; }
		public static void setFogDensity(float value) { record.fogDensity = value; }
	}
	static final List<LeafiaGlStack> stacks = new ArrayList<>();
	static { stacks.add(new LeafiaGlStack()); }

	public static LeafiaGlStack getLastStack() {
		return stacks.get(stacks.size()-1);
	}
	public static void resetStacks() {
		preventStackSyncing = true;
		stacks.clear();
		stacks.add(new LeafiaGlStack().reflect(record));
		preventStackSyncing = false;
	}
	public static void _push() {
		//GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS); let me see if this is the proper way
		stacks.add(new LeafiaGlStack().reflect(record));
	}
	public static void _pop() {
		preventStackSyncing = true;
		//if (stacks.size() <= 1)
			//throw new TransformerCoreLeafia.LeafiaDevErrorGls("Attempted to pop the first Gls stack. Something definitely went wrong here.");
		stacks.remove(stacks.size()-1);
		getLastStack().commit();
		//GL11.glPopAttrib(); nooo! it made it WORSE
		preventStackSyncing = false;
	}
	public static void resetEffects() {
		LeafiaGls.color(1,1,1,1);
		LeafiaGls.disableDepth();
		LeafiaGls.disableLighting();
		LeafiaGls.enableAlpha();
		LeafiaGls.alphaFunc(GL11.GL_ALWAYS,0);
		LeafiaGls.enableBlend();
		LeafiaGls.blendFunc(SourceFactor.SRC_ALPHA,DestFactor.ONE_MINUS_SRC_ALPHA);
	}
	public static void translate(Vec3d vector) {
		LeafiaGls.translate(vector.x,vector.y,vector.z);
	}
	public static void rotate(double angle,Vec3d normal) {
		GL11.glRotated(angle,normal.x,normal.y,normal.z);
	}
	public static void scale(double factor) {
		LeafiaGls.scale(factor,factor,factor);
	}
	public static void scale(double x,double y) {
		LeafiaGls.scale(x,y,1);
	}
	public static void inLocalSpace(Runnable callback) {
		LeafiaGls.pushMatrix();
		callback.run();
		LeafiaGls.popMatrix();
	}
	public static void inStack(Runnable callback) {
		LeafiaGls._push();
		callback.run();
		LeafiaGls._pop();
	}
	public static void setGradient(boolean smooth) {
		LeafiaGls.shadeModel(smooth ? GL11.GL_SMOOTH : GL11.GL_FLAT);
	}
	public static class Tools {
		public static Tessellator getTessellator() {
			return Tessellator.getInstance();
		}
		public static BufferBuilder getBufferBuilder() {
			return getTessellator().getBuffer();
		}
		public static CompositeBrush getBrush() {
			return CompositeBrush.instance;
		}
		public static FontRenderer getFontRenderer() {
			return Minecraft.getMinecraft().fontRenderer;
		}
	}
	public static class Util3D {
		public static void rotateToCamera() {
			LeafiaGls.rotate(-Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
			LeafiaGls.rotate(Minecraft.getMinecraft().getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
		}
	}
}
