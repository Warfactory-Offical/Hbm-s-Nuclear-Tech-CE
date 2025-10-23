package com.llib.exceptions.messages;

import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class TextWarningLeafia extends TextComponentString {
	final String message;
	public TextWarningLeafia(String msg) {
		super("ERROR ");
		message = msg;
		this.appendSibling(new TextComponentString(">").setStyle(new Style().setColor(TextFormatting.RED)))
			.appendSibling(new TextComponentString(">").setStyle(new Style().setColor(TextFormatting.DARK_RED)))
			.appendSibling(new TextComponentString(" "+msg).setStyle(new Style().setColor(TextFormatting.RED)));
	}

	@Override
	public String toString() {
		return "ERROR "+TextFormatting.RED+">"+TextFormatting.DARK_RED+">"+TextFormatting.RED+" "+message;
	}
}
