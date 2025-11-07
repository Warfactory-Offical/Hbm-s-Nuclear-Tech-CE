package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.HbmShaderManager2;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.Library;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.render.NTMRenderHelper;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.misc.BeamPronter;
import com.hbm.render.util.ModelRendererUtil;
import com.hbm.render.util.ModelRendererUtil.VertexData;
import com.hbm.render.util.RenderMiscEffects;
import com.hbm.tileentity.deco.TileEntityObjTester;
import com.hbm.util.Vec3dUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.util.List;
@AutoRegister
public class RenderObjTester extends TileEntitySpecialRenderer<TileEntityObjTester>
    implements IItemRendererProvider {

  @Override
  public boolean isGlobalRenderer(TileEntityObjTester te) {
    return true;
  }

  @Override
  public void render(
      TileEntityObjTester te,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {

    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5, y, z + 0.5);
    GlStateManager.disableLighting();
    /*GlStateManager.pushMatrix();
          GlStateManager.translate(x + 0.5D, y, z + 0.5D);

          //Drillgon200: Aha! I finally started using bob's tester block!
          Quaternion identity = new Quaternion();
          Quaternion q = new Quaternion(0, 0.7071068F, 0, 0.7071068F);
          Quaternion a = identity;
          Quaternion b = q;
          float t = 0.5F;

          Quaternion r = new Quaternion();
    float t_ = 1 - t;
    float Wa, Wb;
    float theta = (float) Math.acos(Quaternion.dot(a, b));

    float sn = (float) Math.sin(theta);
    Wa = (float) (Math.sin(t_*theta) / sn);
    Wb = (float) (Math.sin(t*theta) / sn);
    if(sn == 0)
    	Wa = Wb = 1;

    r.x = Wa*a.x + Wb*b.x;
    r.y = Wa*a.y + Wb*b.y;
    r.z = Wa*a.z + Wb*b.z;
    r.w = Wa*a.w + Wb*b.w;

    r.normalise(r);
    //System.out.println(r);

          //System.out.println(q);

          GlStateManager.popMatrix();*/

    GlStateManager.pushMatrix();
    GlStateManager.translate(0, 8, 0);
    Vec3d player = new Vec3d(x + 0.5, y + 8, z + 0.5);
    GlStateManager.disableCull();
    GlStateManager.enableBlend();
    GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
    bindTexture(ResourceManager.turbofan_blades_tex);
    /* LightningGenInfo l = new LightningGenInfo();
       l.randAmount = 0.7F;
       l.subdivisions = 3;
       l.forkChance = 0.6F;
    LightningNode n = LightningGenerator.generateLightning(new Vec3d(0, 10, 0), new Vec3d(0, 0, 0), l);
    ResourceManager.test_trail.use();
    GL20.glUniform4f(GL20.glGetUniformLocation(ResourceManager.test_trail.getShaderId(), "duck"), 1F, 1F, 1F, 1F);
    //TrailRenderer2.draw(player, Arrays.asList(new Vec3d(0, 0, 0), new Vec3d(0, 1, 0), new Vec3d(1, 2, 0)), 0.2F);
    int bruh3 = GL11.glGenLists(1);

    GL11.glNewList(bruh3, GL11.GL_COMPILE);

    LightningGenerator.render(n, player, 0.05F);

    GL11.glEndList();

    GL11.glCallList(bruh3);

    HbmShaderManager2.bloomData.bindFramebuffer(false);
    float mult = 1F;
    GL20.glUniform4f(GL20.glGetUniformLocation(ResourceManager.test_trail.getShaderId(), "duck"), 0.6F*mult, 0.8F*mult, 1F*mult, 1F);
    GL11.glCallList(bruh3);
    GL11.glCallList(bruh3);
    GL11.glCallList(bruh3);
    Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(false);
    HbmShaderManager2.releaseShader();
    GL11.glDeleteLists(bruh3, 1);*/

    GlStateManager.enableCull();
    GlStateManager.disableBlend();

    GlStateManager.popMatrix();

    GlStateManager.rotate(-90, 0, 1, 0);
    GlStateManager.translate(0, 3, 0);
    // Drillgon200: The thing is dead.
    bindTexture(ResourceManager.bobkotium_tex);
    ResourceManager.nikonium.renderAll();
    /*HbmShaderManager2.bloomData.bindFramebuffer(false);
     ResourceManager.bloom_test.use();
     ResourceManager.nikonium.renderAll();
     float aeug = (float) Math.sin((te.getWorld().getTotalWorldTime()+partialTicks)*0.15)*0.5F+0.65F;
     float aeug2 = (float) Math.sin((te.getWorld().getTotalWorldTime()+partialTicks)*0.15+0.2)*0.5F+0.65F;
     GL20.glUniform4f(GL20.glGetUniformLocation(ResourceManager.bloom_test.getShaderId(), "color"), 1F*aeug, 0.2F*aeug2, 0, 1);
     GL20.glUniform4f(GL20.glGetUniformLocation(ResourceManager.bloom_test.getShaderId(), "color"), 2F*aeug, 1.3F*aeug, 1*aeug, 1);
    // GL20.glUniform4f(GL20.glGetUniformLocation(ResourceManager.bloom_test.getShaderId(), "color"), 0F, 1F, 0, 1);
     HbmShaderManager2.releaseShader();
     Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(false);*/

    GlStateManager.translate(0, 4, 0);
    EntityCreeper creep = new EntityCreeper(Minecraft.getMinecraft().world);
    creep.setPosition(te.getPos().getX() + 0.5F, te.getPos().getY() + 7, te.getPos().getZ() + 0.5F);
    Minecraft.getMinecraft()
        .getTextureManager()
        .bindTexture(ModelRendererUtil.getEntityTexture(creep));
    List<Pair<Matrix4f, ModelRenderer>> boxes = ModelRendererUtil.getBoxesFromMob(creep);
    Vec3d nor = new Vec3d(0, 1, 1).normalize();
    float[] plane = new float[] {(float) nor.x, (float) nor.y, (float) nor.z, -0.5F};
    GlStateManager.disableDepth();
    HbmShaderManager2.distort(
        0.5F,
        () -> {
          for (Pair<Matrix4f, ModelRenderer> p : boxes) {
            GlStateManager.pushMatrix();
            BufferBuilder buf = Tessellator.getInstance().getBuffer();
            buf.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX);
            for (ModelBox b : p.getRight().cubeList) {
              VertexData[] dat = ModelRendererUtil.cutAndCapModelBox(b, plane, p.getLeft());
              // dat[0].tessellate(buf);
              dat[1].tessellate(buf, false);
              // dat[2].tessellate(buf, true);
            }
            Tessellator.getInstance().draw();
            GlStateManager.popMatrix();
          }
        });
    GlStateManager.enableDepth();
    GlStateManager.translate(0, -7, 0);
    GlStateManager.rotate(90, 0, 1, 0);

    GlStateManager.shadeModel(GL11.GL_SMOOTH);
    bindTexture(ResourceManager.fstbmb_tex);
    ResourceManager.fstbmb.renderPart("Body");
    ResourceManager.fstbmb.renderPart("Balefire");

    bindTexture(new ResourceLocation(RefStrings.MODID + ":textures/misc/glintBF.png"));
    RenderMiscEffects.renderClassicGlint(
        te.getWorld(), partialTicks, ResourceManager.fstbmb, "Balefire", 0.0F, 0.8F, 0.15F, 5, 2F);

    FontRenderer font = Minecraft.getMinecraft().fontRenderer;
    float f3 = 0.04F;
    GlStateManager.translate(0.815F, 0.9275F, 0.5F);
    GlStateManager.scale(f3, -f3, f3);
    GlStateManager.color(0.0F, 0.0F, -1.0F * f3);
    GlStateManager.rotate(90, 0, 1, 0);
    GlStateManager.depthMask(false);
    GlStateManager.translate(0, 1, 0);
    font.drawString("00:15", 0, 0, 0xff0000);
    NTMRenderHelper.resetColor();
    GlStateManager.depthMask(true);

    GlStateManager.shadeModel(GL11.GL_FLAT);

    GlStateManager.translate(0, 2, 0);
    bindTexture(ResourceManager.turbofan_blades_tex);

    GlStateManager.popMatrix();

    GlStateManager.pushMatrix();

    GlStateManager.translate(x + 0.5, y + 20, z + 0.5);
    bindTexture(ResourceManager.turbofan_blades_tex);

    ResourceManager.BFG10K.renderAll();

    GlStateManager.popMatrix();

    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5, y + 6, z + 0.5);

    Tessellator tes = Tessellator.getInstance();
    BufferBuilder buf = tes.getBuffer();

    GlStateManager.disableCull();
    GlStateManager.disableLighting();
    GlStateManager.enableBlend();
    GlStateManager.depthMask(false);
    GlStateManager.disableAlpha();
    GlStateManager.disableFog();
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
    GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
    GlStateManager.color(0.5F, 1F, 0.5F, 0.6F);
    GlStateManager.color(1, 1, 1);

    /*bindTexture(ResourceManager.bfg_beam2);
    GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL14.GL_MIRRORED_REPEAT);

    float time = te.getWorld().getTotalWorldTime() + partialTicks;
    time *= 0.25;

    GL11.glScaled(1, 2, 1);

    buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
    for(int i = 0; i < 5; i ++){
    	int offset = i*20;
    	int texFlip = i % 2 == 1 ? 1 : 0;
    	buf.pos(-25+offset, -1, 0).tex(0+texFlip+time, 0).endVertex();
        buf.pos(-25+offset, 12, 0).tex(0+texFlip+time, 1).endVertex();
        buf.pos(-5+offset, 12, 0).tex(1+texFlip+time, 1).endVertex();
        buf.pos(-5+offset, -1, 0).tex(1+texFlip+time, 0).endVertex();
    }
    tes.draw();
    GL11.glScaled(5, 1, 1);

    time *= 2;

    bindTexture(ResourceManager.bfg_beam1);
    HbmShaderManager.useShader2(HbmShaderManager.bfg_beam);
    GlStateManager.color(1F, 1F, 1F, 1F);

    buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
    buf.pos(-5, 4, 0).tex(0+time, 0).endVertex();
    buf.pos(-5, 6, 0).tex(0+time, 1).endVertex();
    buf.pos(15, 6, 0).tex(1+time, 1).endVertex();
    buf.pos(15, 4, 0).tex(1+time, 0).endVertex();
    tes.draw();

    HbmShaderManager.releaseShader2();*/

    int index = (int) ((System.currentTimeMillis() % 1000000) / 10F % 64);
    float size = 1 / 8F;
    float u = (index % 8) * size;
    float v = (index / 8) * size;

    bindTexture(ResourceManager.bfg_smoke);
    GL13.glActiveTexture(GL13.GL_TEXTURE1);
    int bruh = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
    GL13.glActiveTexture(GL13.GL_TEXTURE0);
    GlStateManager.bindTexture(bruh);
    // HbmShaderManager2.bloomBuffers[6].bindFramebufferTexture();

    // buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
    /* buf.pos(-1, -1, 0).tex(u+size, v+size).endVertex();
    buf.pos(-1, 1, 0).tex(u+size, v).endVertex();
    buf.pos(1, 1, 0).tex(u, v).endVertex();
    buf.pos(1, -1, 0).tex(u, v+size).endVertex();
    buf.pos(-1, -1, 0).tex(u+size, v+size).endVertex();
    buf.pos(-1, 1, 0).tex(u+size, v).endVertex();
    buf.pos(1, 1, 0).tex(u, v).endVertex();
    buf.pos(1, -1, 0).tex(u, v+size).endVertex();*/
    /*buf.pos(-1, -1, 0).tex(0, 0).endVertex();
    buf.pos(-1, 1, 0).tex(0, 1).endVertex();
    buf.pos(1, 1, 0).tex(1, 1).endVertex();
    buf.pos(1, -1, 0).tex(1, 0).endVertex();
    tes.draw();*/
    GlStateManager.disableBlend();
    ResourceManager.test.draw();
    GlStateManager.enableBlend();

    GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, HbmShaderManager2.AUX_GL_BUFFER);
    HbmShaderManager2.AUX_GL_BUFFER.rewind();
    Matrix4f mvMatrix = new Matrix4f();
    mvMatrix.load(HbmShaderManager2.AUX_GL_BUFFER);
    HbmShaderManager2.AUX_GL_BUFFER.rewind();

    GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, HbmShaderManager2.AUX_GL_BUFFER);
    HbmShaderManager2.AUX_GL_BUFFER.rewind();
    Matrix4f pMatrix = new Matrix4f();
    pMatrix.load(HbmShaderManager2.AUX_GL_BUFFER);
    HbmShaderManager2.AUX_GL_BUFFER.rewind();

    Matrix4f.mul(pMatrix, mvMatrix, mvMatrix);

    Vector4f bruh1 = new Vector4f(0, 0, 0, 1);
    Matrix4f.transform(mvMatrix, bruh1, bruh1);
    Vector3f bruh2 = new Vector3f(bruh1.x / bruh1.w, bruh1.y / bruh1.w, bruh1.z / bruh1.w);
    // System.out.println(bruh2);

    GlStateManager.translate(-0.5, -4, 0);
    RayTraceResult r =
        Library.rayTraceIncludeEntities(
            te.getWorld(),
            new Vec3d(te.getPos()).add(0, 2, 0.5),
            new Vec3d(te.getPos()).add(12, 2, 0.5),
            null);
    if (r != null && r.hitVec != null) {
      BeamPronter.gluonBeam(
          new Vec3d(0, 0, 0),
          new Vec3d(Vec3dUtil.convertToVec3i(r.hitVec.subtract(te.getPos().getX(), te.getPos().getY() + 2, te.getPos().getZ() + 0.5))), 0.8F);
    } else {
      BeamPronter.gluonBeam(
          new Vec3d(0, 0, 0), new Vec3d(11, 0, 0), 0.8F);
    }

    /*if(spikeV == -1){
    	FloatBuffer buffer = GLAllocation.createDirectFloatBuffer(16);
    	GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buffer);
    	spikeV = LensSpikeVisibilityHandler.generate(buffer);
    }

    FloatBuffer buffer = LensSpikeVisibilityHandler.getMatrixBuf(spikeV);
    buffer.rewind();
    GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buffer);*/
    /*GlStateManager.translate(0, 5, 0);
    AxisAlignedBB bb1 = new AxisAlignedBB(0, 0, 0, 1, 1, 1);
    AxisAlignedBB bb2 = new AxisAlignedBB(0, 0, 0, 1, 1, 1).offset(-0.2, 0.7, 0.1);
    Collider a = new AABBCollider(bb1, 1);
    Collider b = new AABBCollider(bb2, 1);
    GJKInfo info = GJK.colliding(null, null, a, b);
    boolean c = info.result == Result.COLLIDING;
    boolean doOffset = c && false;
    GlStateManager.disableTexture2D();
    GlStateManager.glLineWidth(4);
    RenderGlobal.drawSelectionBoundingBox(bb1, 1, c?0:1, c?0:1, 1);
    if(doOffset){
    	RenderGlobal.drawSelectionBoundingBox(bb2.offset(info.normal.toVec3d().scale(info.depth)), 1, c?0:1, c?0:1, 1);
    } else {
    	RenderGlobal.drawSelectionBoundingBox(bb2, 1, c?0:1, c?0:1, 1);
    }
    if(c){
    	buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
    	Vec3 normal = info.normal.mult(info.depth);
    	buf.pos(info.contactPointA.xCoord, info.contactPointA.yCoord, info.contactPointA.zCoord).color(c?0F:1F, c?0F:1F, 1F, 1F).endVertex();
    	buf.pos(info.contactPointA.xCoord-normal.xCoord, info.contactPointA.yCoord-normal.yCoord, info.contactPointA.zCoord-normal.xCoord).color(c?0F:1F, c?0F:1F, 1F, 1F).endVertex();

    	buf.pos(info.contactPointB.xCoord, info.contactPointB.yCoord, info.contactPointB.zCoord).color(c?0F:1F, 1F, c?0F:1F, 1F).endVertex();
    	buf.pos(info.contactPointB.xCoord+normal.xCoord, info.contactPointB.yCoord+normal.yCoord, info.contactPointB.zCoord+normal.xCoord).color(c?0F:1F, 1F, c?0F:1F, 1F).endVertex();
    	tes.draw();

    	GL11.glPointSize(16);
    	buf.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION_COLOR);
    	buf.pos(info.contactPointA.xCoord, info.contactPointA.yCoord, info.contactPointA.zCoord).color(c?0F:1F, c?0F:1F, 1F, 1F).endVertex();
    	buf.pos(info.contactPointB.xCoord, info.contactPointB.yCoord, info.contactPointB.zCoord).color(c?0F:1F, 1F, c?0F:1F, 1F).endVertex();
    	tes.draw();
    }*/
    // List<BakedQuad> list =
    // Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(ModBlocks.yellow_barrel.getDefaultState()).getQuads(ModBlocks.yellow_barrel.getDefaultState(), null, 0);
    // BakedQuad q = list.get(0);
    // System.out.println(q.getVertexData());
    // System.out.println(Float.intBitsToFloat(q.getVertexData()[5]));

    GlStateManager.enableTexture2D();

    GlStateManager.enableCull();
    GlStateManager.enableLighting();
    GlStateManager.disableBlend();
    GlStateManager.enableFog();
    GlStateManager.enableAlpha();
    GlStateManager.depthMask(true);

    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.obj_tester);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -4, 0);
        GlStateManager.scale(2, 2, 2);
      }

      public void renderCommon() {
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.soyuz_module_dome_tex);
        ResourceManager.soyuz_module.renderPart("Dome");
        bindTexture(ResourceManager.soyuz_module_lander_tex);
        ResourceManager.soyuz_module.renderPart("Capsule");
        bindTexture(ResourceManager.soyuz_module_propulsion_tex);
        ResourceManager.soyuz_module.renderPart("Propulsion");
        bindTexture(ResourceManager.soyuz_module_solar_tex);
        ResourceManager.soyuz_module.renderPart("Solar");
        GlStateManager.shadeModel(GL11.GL_FLAT);
      }
    };
  }

  // public int spikeV = -1;
}
