package com.leafia.dev;

import com.hbm.util.I18nUtil;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class MachineTooltip {
    public static void append(List<String> tooltip,String s) {
        tooltip.set(tooltip.size()-1,tooltip.get(tooltip.size()-1)+s);
    }
    public static void addGenerator(List<String> tooltip) {
        tooltip.add(TextFormatting.AQUA+"< "+ I18nUtil.resolveKey("trait._machine.power") +" >");
    }
    public static void addBoiler(List<String> tooltip) {
        tooltip.add(TextFormatting.GOLD+"< "+ I18nUtil.resolveKey("trait._machine.steam") +" >");
    }
    public static void addCondenser(List<String> tooltip) {
        tooltip.add(TextFormatting.LIGHT_PURPLE+"< "+ I18nUtil.resolveKey("trait._machine.condenser") +" >");
    }
    public static void addNuclear(List<String> tooltip) {
        append(tooltip,TextFormatting.DARK_GREEN+" -"+ I18nUtil.resolveKey("trait._machine.nuclear"));
    }
    public static void addMultiblock(List<String> tooltip) {
        tooltip.add(TextFormatting.GRAY+"< "+ I18nUtil.resolveKey("trait._machine.multiblock") +" >");
    }
    public static void addModular(List<String> tooltip) {
        append(tooltip,TextFormatting.GREEN+" -"+ I18nUtil.resolveKey("trait._machine.modular"));
    }
    public static void addCore(List<String> tooltip) {
        append(tooltip,TextFormatting.RED+" -"+ I18nUtil.resolveKey("trait._machine.multiblock.core"));
    }
    public static void addShit(List<String> tooltip) {
        tooltip.add(TextFormatting.DARK_RED+"< Obsolete >");
    }

    public static void addWIP(List<String> tooltip) {
        tooltip.add(TextFormatting.DARK_RED+"< Unfinished / Extremely buggy >");
    }

    public static void addUpdate(List<String> tooltip,String... oldKeys) {
        String buffer = I18nUtil.resolveKey(oldKeys[0]);
        for (int i = 1; i < oldKeys.length; i++) {
            if (i >= oldKeys.length-1)
                buffer = buffer+" and ";
            else
                buffer = buffer+ ", ";
            buffer = buffer + I18nUtil.resolveKey(oldKeys[i]);
        }
        tooltip.add(TextFormatting.DARK_PURPLE+"(Formerly "+buffer+")");
    }
}
