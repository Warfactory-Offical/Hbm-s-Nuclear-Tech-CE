package com.hbm.command;

import com.hbm.capability.HbmLivingCapability;
import com.hbm.handler.radiation.ChunkRadiationManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//Well this class is a big mess. Needs some helper methods, should add later
public class CommandRadiation extends CommandBase {

    @Override
    public String getName() {
        return "hbmrad";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Usage: /hbmrad <block x> <block y>  <block z> <new rad>\n" + " /hbmrad <clearall/reset>\n" +
               " or /hbmrad player <player> <newRad>\n" + " or /hbmrad player <player>";
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.add("set");
            list.add("clearall");
            list.add("reset");
            list.add("player");
            list.add("resetplayers");
        } else if (args.length == 2 && (args[0].equals("clearall") || args[0].equals("reset") || args[0].equals("resetplayers"))) {
        } else if (args.length == 2 && args[0].equals("player")) {
            Collections.addAll(list, server.getOnlinePlayerNames());
        } else if (args.length == 2 && args[0].equals("set")) {
            list.add(String.valueOf(sender.getPosition().getX()));
        } else if (args.length == 3 && args[0].equals("set")) {
            list.add(String.valueOf(sender.getPosition().getY()));
        } else if (args.length == 4 && args[0].equals("set")) {
            list.add(String.valueOf(sender.getPosition().getZ()));
        } else if (args.length == 5 && args[0].equals("set")) {
            list.add(String.valueOf(0));
        } else if (args.length == 3 && getPlayer(server, args[1]) != null) {
            list.add(String.valueOf(0));
        }
        return list;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 3;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) throw new CommandException(this.getUsage(sender));

        switch (args[0]) {
            case "set" -> {
                if (args.length != 5 || !isInteger(args[1]) || !isInteger(args[2]) || !isInteger(args[3]))
                    throw new CommandException(this.getUsage(sender));

                int blockX = parseCoord(args[1], sender.getPosition().getX());
                int blockY = parseCoord(args[2], sender.getPosition().getY());
                int blockZ = parseCoord(args[3], sender.getPosition().getZ());
                int amount = Integer.parseInt(args[4]);
                ChunkRadiationManager.proxy.setRadiation(sender.getEntityWorld(), new BlockPos(blockX, blockY, blockZ), amount);
                sender.sendMessage(
                        new TextComponentTranslation("Set radiation at coords (" + blockX + ", " + blockY + ", " + blockZ + ") to " + amount + "."));
            }
            case "clearall", "reset" -> {
                ChunkRadiationManager.proxy.clearSystem(sender.getEntityWorld());
                sender.sendMessage(new TextComponentTranslation("commands.hbmrad.removeall", sender.getEntityWorld().provider.getDimension()));
            }
            case "player" -> {
                if (args.length == 2) {
                    EntityPlayerMP player = getPlayer(server, args[1]);
                    if (player == null) throw new CommandException("commands.hbmrad.not_found_player", args[1]);

                    double rads = 0.0D;
                    if (player.hasCapability(HbmLivingCapability.EntityHbmPropsProvider.ENT_HBM_PROPS_CAP, null)) {
                        rads = player.getCapability(HbmLivingCapability.EntityHbmPropsProvider.ENT_HBM_PROPS_CAP, null).getRads();
                    }
                    sender.sendMessage(new TextComponentString(String.valueOf(rads)));
                } else if (args.length == 3) {
                    EntityPlayerMP player = getPlayer(server, args[1]);
                    if (player == null) throw new CommandException("commands.hbmrad.not_found_player", args[1]);
                    if (!isDouble(args[2])) throw new CommandException("commands.hbmrad.rad_not_int");

                    double newRads = Double.parseDouble(args[2]);
                    if (newRads < 0.0D) newRads = 0.0D;

                    if (player.hasCapability(HbmLivingCapability.EntityHbmPropsProvider.ENT_HBM_PROPS_CAP, null)) {
                        player.getCapability(HbmLivingCapability.EntityHbmPropsProvider.ENT_HBM_PROPS_CAP, null).setRads(newRads);
                    }
                    sender.sendMessage(new TextComponentTranslation("Set radiation for player " + player.getName() + " to " + newRads + "."));
                } else {
                    throw new CommandException(this.getUsage(sender));
                }
            }
            case "resetplayers" -> {
                if (args.length != 1) throw new CommandException(this.getUsage(sender));
                server.getPlayerList().getPlayers().forEach(player -> {
                    if (player.hasCapability(HbmLivingCapability.EntityHbmPropsProvider.ENT_HBM_PROPS_CAP, null)) {
                        player.getCapability(HbmLivingCapability.EntityHbmPropsProvider.ENT_HBM_PROPS_CAP, null).setRads(0.0D);
                    }
                });
                sender.sendMessage(new TextComponentTranslation("commands.hbmrad.player_success"));
            }
            default -> throw new CommandException(this.getUsage(sender));
        }
    }

    private int parseCoord(String s, int current) {
        return "~".equals(s) ? current : Integer.parseInt(s);
    }

    public boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return s.equals("~");
        }
    }

    public boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public EntityPlayerMP getPlayer(MinecraftServer server, String name) {
        return server.getPlayerList().getPlayerByUsername(name);
    }
}
