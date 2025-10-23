package com.hbm.tileentity.machine;

import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.TileEntityLoadedBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

@AutoRegister
public class TileEntityBroadcaster extends TileEntityLoadedBase implements ITickable {

    private AudioWrapper audio;

    @Override
    public void update() {
        List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos.getX() + 0.5 - 25, pos.getY() + 0.5 - 25,
                pos.getZ() + 0.5 - 25, pos.getX() + 0.5 + 25, pos.getY() + 0.5 + 25, pos.getZ() + 0.5 + 25));

        for (Entity entity : list) {
            if (entity instanceof EntityLivingBase e) {
                double d =
                        Math.sqrt(Math.pow(e.posX - (pos.getX() + 0.5), 2) + Math.pow(e.posY - (pos.getY() + 0.5), 2) + Math.pow(e.posZ - (pos.getZ() + 0.5), 2));

                if (d <= 25) {
                    double t = (25 - d) / 25 * 10;
                    e.attackEntityFrom(ModDamageSource.broadcast, (float) t);
                    if (!(e instanceof EntityPlayer player && (player.capabilities.isCreativeMode || player.isSpectator())))
                        if (e.getActivePotionEffect(MobEffects.NAUSEA) == null || e.getActivePotionEffect(MobEffects.NAUSEA).getDuration() < 100)
                            e.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 300, 0));
                }

                if (d <= 15) {
                    double t = (15 - d) / 15 * 10;
                    e.attackEntityFrom(ModDamageSource.broadcast, (float) t);
                }
            }
        }
        if (world.isRemote) {
            if(audio == null) {
                audio = createAudioLoop();
                audio.startSound();
            } else if(!audio.isPlaying()) {
                audio = rebootAudio(audio);
            }

            audio.keepAlive();
        }
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        if(audio != null) {
            audio.stopSound();
            audio = null;
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if(audio != null) {
            audio.stopSound();
            audio = null;
        }
    }

    @Override
    public AudioWrapper createAudioLoop() {
        int xCoord = pos.getX(), yCoord = pos.getY(), zCoord = pos.getZ();
        Random rand = new Random(xCoord + yCoord + zCoord);
        SoundEvent event = switch (rand.nextInt(3)){
            case 1 -> HBMSoundHandler.broadcast2;
            case 2 -> HBMSoundHandler.broadcast3;
            default -> HBMSoundHandler.broadcast1;
        };
        return MainRegistry.proxy.getLoopedSound(event, SoundCategory.BLOCKS, xCoord, yCoord, zCoord, 25F, 25F, 1.0F, 20);
    }

    AxisAlignedBB bb = null;

    @Override
    public @NotNull AxisAlignedBB getRenderBoundingBox() {
        if(bb == null) {
            int xCoord = pos.getX(), yCoord = pos.getY(), zCoord = pos.getZ();
            bb = new AxisAlignedBB(
                    xCoord,
                    yCoord,
                    zCoord,
                    xCoord + 1,
                    yCoord + 2,
                    zCoord + 1
            );
        }

        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }
}
