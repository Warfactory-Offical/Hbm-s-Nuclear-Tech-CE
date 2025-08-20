package com.hbm.entity.mob;

import com.hbm.blocks.ModBlocks;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.*;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
@AutoRegister(name = "entity_mob_flesh_creeper")
public class EntityCreeperFlesh extends EntityCreeper {

    public EntityCreeperFlesh(World world) {
        super(world);
    }

    @Override
    public void explode() {
        if(!this.world.isRemote) {
            NBTTagCompound vdat = new NBTTagCompound();
            vdat.setString("type", "giblets");
            vdat.setInteger("ent", getEntityId());
            vdat.setInteger("cDiv", 2);
            PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(vdat, posX, posY + height * 0.5, posZ), new NetworkRegistry.TargetPoint(dimension, posX, posY + height * 0.5, posZ, 150));

            this.setDead();
            boolean mobGriefing = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this);
            ExplosionVNT vnt = new ExplosionVNT(world, posX, posY, posZ, this.getPowered() ? 7 : 4, this);
            if (mobGriefing) {
                vnt.setBlockAllocator(new BlockAllocatorBulkie(30, this.getPowered() ? 16 : 8));
                vnt.setBlockProcessor(new BlockProcessorStandard().withBlockEffect(new BlockMutatorBulkie(ModBlocks.tumor)));
            }
            vnt.setEntityProcessor(new EntityProcessorStandard().withRangeMod(0.25F));
            vnt.setPlayerProcessor(new PlayerProcessorStandard());
            vnt.setSFX(new ExplosionEffectStandard());
            vnt.explode();
        }
    }

}
