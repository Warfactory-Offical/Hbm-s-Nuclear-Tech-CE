package com.leafia.unsorted.recipe_book.system;

import com.hbm.main.CraftingManager;
import com.hbm.main.ModEventHandlerClient;
import com.hbm.packet.PacketDispatcher;
import com.hbm.util.Tuple.Triplet;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class LeafiaRecipeBookServer {
    public static boolean unlockRecipe(EntityPlayer player,String resource) {
        try {
            player.unlockRecipes(new ResourceLocation[]{new ResourceLocation(resource)});
        } catch (NullPointerException e) {
            // do nothing about it, fuckass
        }
        NBTTagCompound nbt = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        if (!nbt.hasKey("leafiaRecipeBook"))
            nbt.setTag("leafiaRecipeBook",new NBTTagList());
        if (!nbt.hasKey("leafiaRecipeBookNew"))
            nbt.setTag("leafiaRecipeBookNew",new NBTTagList());
        NBTTagList list = (NBTTagList)nbt.getTag("leafiaRecipeBook");
        NBTTagList listNew = (NBTTagList)nbt.getTag("leafiaRecipeBookNew");
        for (NBTBase nbtBase : list) {
            if (nbtBase instanceof NBTTagString) {
                NBTTagString str = (NBTTagString)nbtBase;
                if (str.getString().equals(resource))
                    return false;
            }
        }
        list.appendTag(new NBTTagString(resource));
        listNew.appendTag(new NBTTagString(resource));
        /*LeafiaRecipePacket packet = new LeafiaRecipePacket((byte)0);
        packet.resources.add(resource);
        if (player instanceof EntityPlayerMP)
            PacketDispatcher.wrapper.sendTo(packet,(EntityPlayerMP)player);
        else
            PacketDispatcher.wrapper.sendToAll(packet);*/
        return true;
    }
    public static boolean unlockRecipe(EntityPlayer player,Item item) {
        try {
            player.unlockRecipes(new ResourceLocation[]{CraftingManager.getRecipeName(new ItemStack(item))});
        } catch (NullPointerException e) {
            // do nothing about it, fuckass
        }
        return unlockRecipe(player,item.getRegistryName().toString());
    }
    public static void loadRecipe(EntityPlayerMP player) {
        NBTTagCompound nbt = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        if (!nbt.hasKey("leafiaRecipeBook"))
            nbt.setTag("leafiaRecipeBook",new NBTTagList());
        if (!nbt.hasKey("leafiaRecipeBookNew"))
            nbt.setTag("leafiaRecipeBookNew",new NBTTagList());
        NBTTagList list = (NBTTagList)nbt.getTag("leafiaRecipeBook");
        NBTTagList listNew = (NBTTagList)nbt.getTag("leafiaRecipeBookNew");
        List<String> items = new ArrayList<>();
        List<String> itemsNew = new ArrayList<>();
        for (NBTBase nbtBase : list) {
            if (nbtBase instanceof NBTTagString) {
                NBTTagString str = (NBTTagString)nbtBase;
                items.add(str.getString());
            }
        }
        for (NBTBase nbtBase : listNew) {
            if (nbtBase instanceof NBTTagString) {
                NBTTagString str = (NBTTagString)nbtBase;
                itemsNew.add(str.getString());
            }
        }
        /*if (items.size() > 0) {
            LeafiaRecipePacket packet = new LeafiaRecipePacket((byte)1);
            packet.resources = items;
            PacketDispatcher.wrapper.sendTo(packet,player);
        }
        if (itemsNew.size() > 0) {
            LeafiaRecipePacket packet = new LeafiaRecipePacket((byte)2);
            packet.resources = itemsNew;
            PacketDispatcher.wrapper.sendTo(packet,player);
        }*/
    }
    /*
    public static class LeafiaRecipePacket extends RecordablePacket {
        public byte type;
        public List<String> resources = new ArrayList<>(); // i know this is very expensive but it's also the most stable soo
        public LeafiaRecipePacket() {
        }
        public LeafiaRecipePacket(byte type) {//,Item[] items) {
            this.type = type;
        }
        @Override
        public void fromBits(LeafiaBuf buf) {
            type = buf.readByte();
            while (buf.isReadable()) {
                resources.add(buf.readUTF8String());
            }
        }
        @Override
        public void toBits(LeafiaBuf buf) {
            buf.writeByte(type);
            for (String res : resources) {
                buf.writeUTF8String(res);
            }
        }
        public static class Handler implements IMessageHandler<LeafiaRecipePacket, IMessage> {
            @Override
            @SideOnly(Side.CLIENT)
            public IMessage onMessage(LeafiaRecipePacket message,MessageContext ctx) {
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    for (String resource : message.resources) {
                        if (message.type >= 1) {
                            ModEventHandlerClient.loadRecipeFromString(resource,message.type == 2);
                        } else
                            ModEventHandlerClient.unlockRecipeFromString(resource);
                    }
                });
                return null;
            }
        }
    }*/
    /*public static class LeafiaTransferItemPacket extends RecordablePacket {
        List<Triplet<Integer,Integer,Integer>> transfer = new ArrayList<>();
        public LeafiaTransferItemPacket() {
        }
        @Override
        public void fromBits(LeafiaBuf buf) {
            while (buf.readableBits() >= 32*3) {
                transfer.add(new Triplet<>(buf.readInt(),buf.readInt(),buf.readInt()));
            }
        }
        @Override
        public void toBits(LeafiaBuf buf) {
            for (Triplet<Integer,Integer,Integer> triplet : transfer) {
                buf.writeInt(triplet.getA());
                buf.writeInt(triplet.getB());
                buf.writeInt(triplet.getC());
            }
        }
        public static class Handler implements IMessageHandler<LeafiaTransferItemPacket, IMessage> {
            @Override
            public IMessage onMessage(LeafiaTransferItemPacket message,MessageContext ctx) {
                EntityPlayer player = ctx.getServerHandler().player;
                player.getServer().addScheduledTask(() -> {
                    //player.sendMessage(new TextComponentString("Packet received"));
                    Container container = player.openContainer;
                    if (container != null) {
                        //player.sendMessage(new TextComponentString("Container found"));
                        //player.sendMessage(new TextComponentString("Container size: "+container.inventorySlots.size()));
                        //player.sendMessage(new TextComponentString("Transfer count: "+message.transfer.size()));
                        for (Triplet<Integer,Integer,Integer> triplet : message.transfer) {
                            if (container.inventorySlots.size() > triplet.getA()) {
                                if (container.inventorySlots.size() > triplet.getC()) {
                                    if (container.getSlot(triplet.getA()).getHasStack()) {
                                        ItemStack copyStack = container.getSlot(triplet.getA()).getStack().copy();
                                        //player.sendMessage(new TextComponentString("Stack: "+copyStack.getTranslationKey()));
                                        if (copyStack.getCount() < triplet.getB()) continue;
                                        copyStack.setCount(triplet.getB());
                                        if (container.getSlot(triplet.getC()).getHasStack()) {
                                            if (!Container.canAddItemToSlot(container.getSlot(triplet.getC()),copyStack,true))
                                                continue;
                                        }
                                        //player.sendMessage(new TextComponentString("Transfering"));
                                        if (container.getSlot(triplet.getC()).getHasStack())
                                            container.getSlot(triplet.getC()).getStack().grow(triplet.getB());
                                        else
                                            container.getSlot(triplet.getC()).putStack(copyStack);
                                        container.getSlot(triplet.getA()).decrStackSize(triplet.getB());
                                    }
                                }
                            }
                        }
                        container.detectAndSendChanges();
                    }
                });
                return null;
            }
        }
    }*/
}
