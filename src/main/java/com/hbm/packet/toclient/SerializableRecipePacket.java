package com.hbm.packet.toclient;

import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.util.BufferUtil;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class SerializableRecipePacket implements IMessage {

    private String filename;
    private byte[] fileBytes;

    private boolean reinit;

    public SerializableRecipePacket() {}

    public SerializableRecipePacket(File recipeFile) {
        try {
            filename = recipeFile.getName();
            fileBytes = Files.readAllBytes(recipeFile.toPath());
        } catch(IOException ignored) {}
    }

    public SerializableRecipePacket(String filename, byte[] fileBytes) {
        this.filename = filename;
        this.fileBytes = fileBytes == null ? new byte[0] : fileBytes.clone();
    }

    public SerializableRecipePacket(boolean reinit) {
        this.reinit = reinit;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        reinit = buf.readBoolean();
        if(reinit) return;

        filename = BufferUtil.readString(buf);
        fileBytes = new byte[buf.readInt()];
        buf.readBytes(fileBytes);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(reinit);
        if(reinit) return;

        BufferUtil.writeString(buf, filename);
        buf.writeInt(fileBytes.length);
        buf.writeBytes(fileBytes);
    }

    public static class Handler implements IMessageHandler<SerializableRecipePacket, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(SerializableRecipePacket m, MessageContext ctx) {
            try {
                if(m.reinit) {
                    SerializableRecipe.initialize();
                    return null;
                }
                SerializableRecipe.receiveRecipes(m.filename, m.fileBytes);
            } catch (Exception ignored) { }
            return null;
        }
    }

}
