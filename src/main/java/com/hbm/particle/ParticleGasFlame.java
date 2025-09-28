package com.hbm.particle;

import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleSmokeNormal;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;

@SideOnly(Side.CLIENT)
public class ParticleGasFlame extends ParticleSmokeNormal {

    private final float colorMod;

    public ParticleGasFlame(World world, double x, double y, double z,
                            double mX, double mY, double mZ, float scale) {
        super(world, x, y, z, mX, mY * 1.5, mZ, scale);

        this.colorMod = 0.8F + this.rand.nextFloat() * 0.2F;
        this.canCollide = true;
        this.particleMaxAge = 30 + this.rand.nextInt(13);
        this.particleScale = scale;

        updateColor();
    }

    @Override
    public void onUpdate() {
        double prevMo = this.motionY;
        super.onUpdate();
        updateColor();

        this.motionY = prevMo;
        this.motionX *= 0.75D;
        this.motionY += 0.005D;
        this.motionZ *= 0.75D;
    }

    private void updateColor() {
        float time = (float) this.particleAge / (float) this.particleMaxAge;

        Color color = Color.getHSBColor(
                Math.max((60 - time * 100) / 360F, 0.0F),
                1 - time * 0.25F,
                1 - time * 0.5F
        );

        this.particleRed   = (color.getRed()   / 255F) * colorMod;
        this.particleGreen = (color.getGreen() / 255F) * colorMod;
        this.particleBlue  = (color.getBlue()  / 255F) * colorMod;
    }

    @Override
    public int getBrightnessForRender(float partialTicks) {
        return 15728880;
    }

    public static class Factory implements IParticleFactory {
        @Override
        public Particle createParticle(int particleID, World world,
                                       double x, double y, double z,
                                       double mX, double mY, double mZ,
                                       int... args) {
            float scale = args.length > 0 ? (float) args[0] : 1.0F;
            return new ParticleGasFlame(world, x, y, z, mX, mY, mZ, scale);
        }
    }
}

