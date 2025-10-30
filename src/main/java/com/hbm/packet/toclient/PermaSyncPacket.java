package com.hbm.packet.toclient;

import com.hbm.main.MainRegistry;
import com.hbm.packet.PermaSyncHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PermaSyncPacket implements IMessage {

    EntityPlayerMP player;	//server only, for writing
    ByteBuf out;			//client only, for reading

    public PermaSyncPacket() { }

    public PermaSyncPacket(EntityPlayerMP player) {
        this.player = player;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PermaSyncHandler.writePacket(buf, player.world, player);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.out = buf;
    }

    public static class Handler implements IMessageHandler<PermaSyncPacket, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PermaSyncPacket m, MessageContext ctx) {

            try {

                EntityPlayer player = Minecraft.getMinecraft().player;
                if(player != null) PermaSyncHandler.readPacket(m.out, player.world, player);

            } catch(Exception x) {
                MainRegistry.logger.catching(x);
            } finally {
                m.out.release();
            }

            return null;
        }
    }
}
