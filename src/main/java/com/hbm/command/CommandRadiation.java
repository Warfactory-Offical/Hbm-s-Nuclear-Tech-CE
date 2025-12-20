package com.hbm.command;

import com.hbm.capability.HbmLivingCapability;
import com.hbm.handler.radiation.ChunkRadiationManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CommandRadiation extends CommandBase {

    private static double parseDoubleMinMax(String arg) throws CommandException {
        return switch (arg.toLowerCase()) {
            case "max" -> Double.MAX_VALUE;
            case "min" -> Double.MIN_VALUE;
            default -> parseDouble(arg);
        };
    }

    private static void handleClearAll(ICommandSender sender) {
        ChunkRadiationManager.proxy.clearSystem(sender.getEntityWorld());
        sender.sendMessage(new TextComponentTranslation("commands.hbmrad.removeall", sender.getEntityWorld().provider.getDimension()));
    }

    private static void handleResetPlayers(MinecraftServer server, ICommandSender sender) {
        server.getPlayerList().getPlayers().stream().map(CommandRadiation::getRadiationCap).filter(Objects::nonNull)
              .forEach(cap -> cap.setRads(0.0D));
        sender.sendMessage(new TextComponentTranslation("commands.hbmrad.player_success"));
    }

    private static HbmLivingCapability.IEntityHbmProps getRadiationCap(EntityPlayer player) {
        if (player.hasCapability(HbmLivingCapability.EntityHbmPropsProvider.ENT_HBM_PROPS_CAP, null)) {
            return player.getCapability(HbmLivingCapability.EntityHbmPropsProvider.ENT_HBM_PROPS_CAP, null);
        }
        return null;
    }

    @Override
    public String getName() {
        return "hbmrad";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return """
                Usage:
                 /hbmrad set <x> <y> <z> <value>
                 /hbmrad <clearall/reset>
                 /hbmrad player <player> [value]
                 /hbmrad resetplayers
                """;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 3;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
        if (args.length == 0) return Collections.emptyList();
        return switch (args.length) {
            case 1 -> getListOfStringsMatchingLastWord(args, "set", "clearall", "reset", "player", "resetplayers");
            case 2 -> switch (args[0]) {
                case "player" -> getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
                case "set" -> getTabCompletionCoordinate(args, 1, targetPos);
                default -> Collections.emptyList();
            };
            default -> {
                if ("set".equals(args[0]) && args.length <= 4) {
                    yield getTabCompletionCoordinate(args, 1, targetPos);
                }
                yield Collections.emptyList();
            }
        };
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) throw new CommandException(getUsage(sender));
        switch (args[0].toLowerCase()) {
            case "set" -> handleSet(sender, args);
            case "clearall", "reset" -> handleClearAll(sender);
            case "player" -> handlePlayer(server, sender, args);
            case "resetplayers" -> handleResetPlayers(server, sender);
            default -> throw new CommandException(getUsage(sender));
        }
    }

    private void handleSet(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 5) throw new CommandException(getUsage(sender));
        var pos = parseBlockPos(sender, args, 1, false);
        var amount = parseDoubleMinMax(args[4]);
        ChunkRadiationManager.proxy.setRadiation(sender.getEntityWorld(), pos, amount);
        sender.sendMessage(new TextComponentTranslation("Set radiation at coords (%s, %s, %s) to %s.", pos.getX(), pos.getY(), pos.getZ(), amount));
    }

    private void handlePlayer(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) throw new CommandException(getUsage(sender));
        var cap = getRadiationCap(getPlayer(server, sender, args[1]));
        if (cap == null) return;
        if (args.length == 2) {
            sender.sendMessage(new TextComponentString(String.valueOf(cap.getRads())));
        } else {
            var newRads = Math.max(0.0D, parseDoubleMinMax(args[2]));
            cap.setRads(newRads);
            sender.sendMessage(new TextComponentTranslation(
                    "Set radiation for player " + getPlayer(server, sender, args[1]).getName() + " to " + newRads + "."));
        }
    }
}
