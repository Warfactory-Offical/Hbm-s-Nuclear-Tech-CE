package com.hbm.particle;

import com.hbm.main.client.NTMClientRegistry;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class ParticleRadiationFog extends Particle {

	private int maxAge;
	
	public ParticleRadiationFog(World worldIn, double posXIn, double posYIn, double posZIn) {
		super(worldIn, posXIn, posYIn, posZIn);
		maxAge = 100 + rand.nextInt(40);
		
        this.particleRed = 0.8F;
        this.particleGreen = 0.8F;
        this.particleBlue = 0.8F;
        this.particleScale = 7.5F;
        this.particleTexture = NTMClientRegistry.fog;
	}
	
	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        
        if(maxAge < 400)
        {
        	maxAge = 400;
        }

        this.particleAge++;
        
        if (this.particleAge >= maxAge)
        {
            this.setExpired();
        }

        this.motionX *= 0.9599999785423279D;
        this.motionY *= 0.9599999785423279D;
        this.motionZ *= 0.9599999785423279D;

        if (this.onGround)
        {
            this.motionX *= 0.699999988079071D;
            this.motionZ *= 0.699999988079071D;
        }
	}
	
	@Override
	public int getFXLayer() {
		return 1;
	}
	
	@Override
	public boolean shouldDisableDepth() {
		return true;
	}
	
	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		float alpha = 0;
		
		alpha = (float) Math.sin(particleAge * Math.PI / (400F)) * 0.25F;

        this.particleAlpha = MathHelper.clamp(alpha, 0, 1);
		
		Random rand = new Random(50);
		
		for(int i = 0; i < 25; i++) {

			double dX = (rand.nextGaussian() - 1D) * 2.5D;
			double dY = (rand.nextGaussian() - 1D) * 0.15D;
			double dZ = (rand.nextGaussian() - 1D) * 2.5D;
			double size = (0.75D+rand.nextDouble() * 0.5D) * particleScale;
			
			int j = this.getBrightnessForRender(partialTicks);
			int k = j >> 16 & 65535;
			int l = j & 65535;
	        
	        float pX = (float) ((this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX) + rand.nextGaussian() * 0.5);
	        float pY = (float) ((this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY) + rand.nextGaussian() * 0.5);
	        float pZ = (float) ((this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ) + rand.nextGaussian() * 0.5);
			
	        buffer.pos((double)(pX - rotationX * size - rotationXY * size) + dX, (double)(pY - rotationZ * size) + dY, (double)(pZ - rotationYZ * size - rotationXZ * size) + dZ).tex(particleTexture.getMaxU(), particleTexture.getMaxV()).color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(k, l).endVertex();
	        buffer.pos((double)(pX - rotationX * size + rotationXY * size) + dX, (double)(pY + rotationZ * size) + dY, (double)(pZ - rotationYZ * size + rotationXZ * size) + dZ).tex(particleTexture.getMaxU(), particleTexture.getMinV()).color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(k, l).endVertex();
	        buffer.pos((double)(pX + rotationX * size + rotationXY * size) + dX, (double)(pY + rotationZ * size) + dY, (double)(pZ + rotationYZ * size + rotationXZ * size) + dZ).tex(particleTexture.getMinU(), particleTexture.getMinV()).color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(k, l).endVertex();
	        buffer.pos((double)(pX + rotationX * size - rotationXY * size) + dX, (double)(pY - rotationZ * size) + dY, (double)(pZ + rotationYZ * size - rotationXZ * size) + dZ).tex(particleTexture.getMinU(), particleTexture.getMaxV()).color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(k, l).endVertex();
		}
		
	}
	
	@Override
	public int getBrightnessForRender(float p_189214_1_) {
		return 70;
	}
}
