package com.leafia.unsorted;

import com.hbm.render.NTMRenderHelper;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.render.misc.BeamPronter;
import com.hbm.render.misc.BeamPronter.EnumBeamType;
import com.hbm.render.misc.BeamPronter.EnumWaveType;
import com.leafia.transformer.LeafiaGls;
import com.leafia.dev.math.FiaMatrix;
import com.leafia.dev.math.FiaMatrix.RotationOrder;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class ParticleSpark extends Particle {
	final Vec3d[] positions;
	final int length;
	int curIndex = 0;
	int color;
	public float thickness = 0.02f;
	public float segmentsPerTick = 3;
	long startTick;
	double lastTick = -65535;
	public ParticleSpark(World world,FiaMatrix mat,int color,int length,double speed,double spread) {
		super(world,mat.getX(),mat.getY(),mat.getZ());
		mat = mat.rotate(RotationOrder.XYZ,(world.rand.nextDouble()*2-1)*spread,(world.rand.nextDouble()*2-1)*spread,(world.rand.nextDouble()*2-1)*spread);
		this.motionX = mat.frontVector.x*speed;
		this.motionY = mat.frontVector.y*speed;
		this.motionZ = mat.frontVector.z*speed;
		//this.particleRed = (color>>>16&0xFF)/255f;
		//this.particleGreen = (color>>>8&0xFF)/255f;
		//this.particleBlue = (color&0xFF)/255f;
		this.color = color;
		this.particleMaxAge = (int)(16.0D / (Math.random() * 0.8D + 0.2D));
		this.canCollide = true;
		this.length = length;
		startTick = world.getTotalWorldTime();
		positions = new Vec3d[length];
	}
	
	@Override
	public int getFXLayer(){
		return 3;
	}

	public void onUpdate()
	{
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if (this.particleAge++ >= this.particleMaxAge) {
			this.setExpired();
		}

		float f = (float)this.particleAge / (float)this.particleMaxAge;

		this.motionY -= 0.03D*2;
		this.move(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.9990000128746033D;
		this.motionY *= 0.9990000128746033D;
		this.motionZ *= 0.9990000128746033D;

		if (this.onGround)
		{
			this.motionX *= 0.699999988079071D;
			this.motionZ *= 0.699999988079071D;
		}
	}
	
	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ){
		NTMRenderHelper.resetParticleInterpPos(entityIn, partialTicks);

		float f = ((float)this.particleAge + partialTicks) / (float)this.particleMaxAge;

		double curTime = (world.getTotalWorldTime()-startTick)+partialTicks;
		double targetTime = lastTick+20/segmentsPerTick;
		if (curTime >= targetTime) {
			lastTick = targetTime;
			positions[curIndex] = new Vec3d(
					prevPosX+(posX-prevPosX)*partialTicks,
					prevPosY+(posY-prevPosY)*partialTicks,
					prevPosZ+(posZ-prevPosZ)*partialTicks
			);
			curIndex = Math.floorMod(curIndex+1,length);
		}

		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
		GlStateManager.depthMask(false);
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		Vec3d prevPos = null;
		for (int z = 0; z < length; z++) {
			int i = Math.floorMod(curIndex-z,length);
			Vec3d curPos = positions[i];
			if (prevPos != null && curPos != null) {
				LeafiaGls.pushMatrix();
				if (z == length-1) {
					Vec3d relative = curPos.subtract(prevPos);
					prevPos = prevPos.add(relative.scale(partialTicks));
				}
				LeafiaGls.translate(prevPos.subtract(interpPosX,interpPosY,interpPosZ));
				Vec3d vec = curPos.subtract(prevPos);
				if (z <= 1)
					vec = vec.scale(partialTicks);
				BeamPronter.prontBeam(
						vec,
						EnumWaveType.STRAIGHT,
						EnumBeamType.SOLID,
						color,color,
						0,1,0,
						1,thickness*(1-z/(float)length)
				);
				LeafiaGls.popMatrix();
			}
			prevPos = curPos;
		}

		GlStateManager.enableLighting();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
	}

}
