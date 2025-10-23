package com.leafia.dev.container_utility;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.List;

public interface LeafiaPacketReceiver {
    public String getPacketIdentifier();
    @SideOnly(Side.CLIENT)
    public void onReceivePacketLocal(byte key,Object value);
    public void onReceivePacketServer(byte key,Object value,EntityPlayer plr);

    /**
     * <tt>[SERVER]</tt> This value times 1.3 will be the packet radius when __sendToAffectedClients() is used.
     * <p><tt>[CLIENT]</tt> Used for automatically sending ._validate() packet
     * @return The range
     */
    public default double affectionRange() { return 32; }
    public default List<EntityPlayer> getListeners() { return Collections.emptyList(); };

    /**
     * <tt>[SERVER]</tt> Called by client when they get into range specified by affectionRange().
     * <p><tt>[CLIENT]</tt> Never called.
     * @param plr The player, intended to for using .__sendToClient() to update the client.
     */
    public void onPlayerValidate(EntityPlayer plr); // little attempt to make this mod less internet expensive..
}
