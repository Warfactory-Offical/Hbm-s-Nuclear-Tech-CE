package com.leafia.passive.rendering;

import com.hbm.render.amlfrom1710.CompositeBrush;
import com.leafia.transformer.LeafiaGls;
import com.llib.group.LeafiaSet;
import com.llib.math.MathLeafia;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

public class TopRender {
	public static void main(RenderWorldLastEvent evt) {
		Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
		float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
		double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
		double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
		double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;
		Highlight.render(new Vec3d(x,y,z));
	}
	public static class Highlight {
		public static final LeafiaSet<Highlight> instances = new LeafiaSet<>();
		static int t0 = 0;
		static void cleanup() {
			int t1 = MathLeafia.getTime32s();
			int dT = MathLeafia.getTimeDifference32s(t0,t1);
			t0 = t1;
			for (Highlight instance : instances) {
				if (instance.lifetime >= 0) {
					instance.lifetime -= dT/1000f;
					if (instance.lifetime <= 0)
						instances.remove(instance);
				}
			}
		}
		static void render(Vec3d pos) {
			cleanup();
			if (instances.size() <= 0) return;
			LeafiaGls.inStack(()->{
				LeafiaGls.resetEffects();
				LeafiaGls.blendFunc(SourceFactor.SRC_ALPHA,DestFactor.ONE);
				LeafiaGls.disableTexture2D();
				CompositeBrush brush = LeafiaGls.Tools.getBrush();
				FontRenderer font = LeafiaGls.Tools.getFontRenderer();
				LeafiaGls.setGradient(true);
				brush.startDrawing(GL11.GL_LINES,DefaultVertexFormats.POSITION_COLOR);
				for (Highlight highlight : instances) {
					highlight.draw(pos,new Vec3d(0,0,-1),new Vec3d(0,1,0));
					highlight.draw(pos,new Vec3d(1,0,0),new Vec3d(0,1,0));
					highlight.draw(pos,new Vec3d(0,-1,0),new Vec3d(0,0,-1));
					if (highlight.ray.length() <= highlight.size.length()) {
						brush.setColorHex(highlight.colorTop);
						brush.addVertex(highlight.center.subtract(pos));
						brush.addVertex(highlight.center.subtract(pos).add(highlight.ray));
					}
				}
				brush.draw();
				LeafiaGls.setGradient(false);
				brush.setColorHex(0xFFFFFF);
				LeafiaGls.enableTexture2D();
				try {
					for (Highlight highlight : instances) {
						if (highlight.label.length > 0) {
							LeafiaGls.inLocalSpace(() -> {
								LeafiaGls.translate(highlight.center.add(highlight.ray.scale(0.41421)).subtract(pos));
								LeafiaGls.Util3D.rotateToCamera();
								LeafiaGls.scale(-1,-1,1);
								LeafiaGls.scale(1 / 16d / 2 * highlight.textSize);
								int lineHeight = 9;
								for (int i = 0; i < highlight.label.length; i++) {
									LeafiaGls.pushMatrix();
									double offset = (i - (highlight.label.length - 1) / 2d) * lineHeight;
									LeafiaGls.translate(-font.getStringWidth(highlight.label[i].toString()) / 2d,-7 / 2d + offset,0);
									font.drawString(highlight.label[i].toString(),0,0,highlight.colorTop & 0xFFFFFF);
									LeafiaGls.popMatrix();
								}
							});
						}
					}
				} catch (NullPointerException ex) {
					//LeafiaDebug.debugLog(Minecraft.getMinecraft().world,ex.getMessage());
				}
				brush.setColorHex(0xFFFFFF);
				brush.startDrawingQuadsColor(); // reset the goofy ahh drawing modes
				brush.draw();
			});
			LeafiaGls.enableTexture2D();
		}
		public Vec3d center;
		public Vec3d size = new Vec3d(1,1,1);;
		public Vec3d ray = new Vec3d(0,0,0);
		public float lifetime = -1;
		public int color = 0x00_FFFFFF;
		public int colorTop = 0x00_FFFFFF;
		public Object[] label = new Object[0];
		public double textSize = 1;
		public Highlight() { center = new Vec3d(0,0,0); }
		public Highlight(BlockPos pos) { center = new Vec3d(pos).add(0.5,0.5,0.5); }
		public Highlight(Vec3d vec) { center = vec; }
		public Highlight setBlock(BlockPos pos) {
			center = new Vec3d(pos).add(0.5,0.5,0.5);
			size = new Vec3d(1,1,1);
			return this;
		}
		public Highlight setArea(Vec3d pointA,Vec3d pointB) {
			Vec3d min = new Vec3d(Math.min(pointA.x,pointB.x),Math.min(pointA.y,pointB.y),Math.min(pointA.z,pointB.z));
			Vec3d max = new Vec3d(Math.max(pointA.x,pointB.x),Math.max(pointA.y,pointB.y),Math.max(pointA.z,pointB.z));
			size = max.subtract(min);
			center = min.add(size.scale(0.5));
			return this;
		}
		public Highlight setArea(BlockPos posA,BlockPos posB) {
			setArea(new Vec3d(posA),new Vec3d(posB));
			size = size.add(1,1,1);
			center = center.add(0.5,0.5,0.5);
			return this;
		}
		public Highlight setLabel(Object... newLabel) {
			label = newLabel;
			return this;
		}
		public Highlight setColor(int color) {
			this.color = color;
			this.colorTop = color;
			return this;
		}
		public Highlight setLifetime(float lifetime) {
			this.lifetime = lifetime;
			return this;
		}
		public Highlight show() { instances.add(this); return this; }
		public Highlight hide() { instances.remove(this); return this; }
		public Highlight setVisibility(boolean visibility) { if (visibility) show(); else hide(); return this; }
		public boolean getVisibility() { return instances.contains(this); }
		void draw(Vec3d viewPos,Vec3d lookVector,Vec3d upVector) { // Front as *negative Z*
			Vec3d rightVector = lookVector.crossProduct(upVector);
			CompositeBrush brush = CompositeBrush.instance;
			for (int bits = 0b00; bits <= 0b11; bits++) {
				int dx = (bits>>1)*2-1;
				int dy = (bits&1)*2-1;
				for (int dz = -1; dz <= 1; dz+=2) {
					Vec3d vector = (rightVector.scale(dx)).add(upVector.scale(dy)).add(lookVector.scale(dz));
					brush.setColorHex((vector.y > 0) ? colorTop : color);
					brush.addVertex(center.add(vector.x*size.x/2,vector.y*size.y/2,vector.z*size.z/2).subtract(viewPos));
				}
				if (ray.length() > size.length()) {
					for (int dz = -1; dz <= 1; dz+=2) {
						Vec3d vector = (rightVector.scale(dx)).add(upVector.scale(dy)).add(lookVector.scale(dz));
						brush.setColorHex(colorTop);
						brush.addVertex(center.add(vector.x*size.x/2,vector.y*size.y/2,vector.z*size.z/2).subtract(viewPos));
						brush.addVertex(center.subtract(viewPos).add(ray));
					}
				}
			}
		}
	}
}
