package com.leafia;

import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommandLeaf extends CommandBase {
	@Override
	public String getName() {
		return "hbmleaf";
	}
	@Override
	public String getUsage(ICommandSender sender) {
		return "myaaaaa";
	}

	@Override
	public void execute(MinecraftServer server,ICommandSender sender,String[] args) throws CommandException {

	}

	@Override
	public int getRequiredPermissionLevel() {
		//Level 2 ops can do commands like setblock, gamemode, and give. They can't kick/ban or stop the server.
		return 2;
	}
	String[] shiftArgs(String[] args,int n) {
		if (n > args.length) return new String[0];
		String[] argsOut = new String[args.length-n];
		for (int i = 0; i < args.length-n; i++)
			argsOut[i] = args[i+n];
		return argsOut;
	}
	boolean darkRow = false;
	ITextComponent genSuggestion(String c) {
		TextComponentString compo = new TextComponentString("  "+c);
		Style style = new Style()
				.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new TextComponentString("Click to try out")))
				.setClickEvent(new ClickEvent(Action.SUGGEST_COMMAND,c))
				.setColor(darkRow ? TextFormatting.DARK_GRAY : TextFormatting.GRAY);
		darkRow = !darkRow;
		return compo.setStyle(style);
	}
}
