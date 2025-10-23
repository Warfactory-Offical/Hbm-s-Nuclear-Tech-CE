package com.llib.math;

import java.util.function.BiFunction;

// SI Unit prefixes utility I made on Lua for use in OpenComputers translated to Java.
// Here's original code if you also play OpenComputers
/*
local sipfx = {}
sipfx.siPrefix = {"k","M","G","T","P","E","Z","Y","R","Q"}
sipfx.siPrefixFull = {"Kilo","Mega","Giga","Terra","Peta","Exa","Zetta","Yotta","Ronna","Quetta"}
function sipfx.round(num,decimals)
  local pow2 = 10^decimals
  return math.floor(num*pow2+0.5)/pow2
end
function sipfx.auto(x)
  local pfxLv = math.floor(math.log(x,1000))
  return (pfxLv == 0 and tostring(x).." ") or tostring(sipfx.round(x/1000^pfxLv,2)).." "..sipfx.siPrefix[pfxLv]
end
return sipfx
 */
public class SIPfx { // Now its both uppercase!
	public static final String[] siPrefix = new String[]{"k","M","G","T","P","E","Z","Y","R","Q"};
	public static final String[] siPrefixFull = new String[]{"Kilo","Mega","Giga","Terra","Peta","Exa","Zetta","Yotta","Ronna","Quetta"};
	public static double round(double num,int decimals) {
		double pow2 = Math.pow(10,decimals);
		return Math.floor(num * pow2 + 0.5) / pow2;
	}
	public static byte getExponent(double x) {
		return (byte)Math.floor(Math.log(x)/Math.log(1000));
	}
	public static double scale(double x,double exponent) {
		return x/Math.pow(1000,exponent);
	}
	public static String auto(double x,boolean full) {
		byte pfxLv = getExponent(x);
		return (pfxLv <= 0 ? String.valueOf(x)+" " : String.valueOf(round(scale(x,pfxLv),2))+" "+(full ? siPrefixFull : siPrefix)[pfxLv-1]);
	}
	public static String auto(double x) {
		return auto(x,false);
	}
	public static String format(String format,double x,boolean full) {
		byte exponent = getExponent(x);
		return String.format(format,scale(x,exponent))+(exponent <= 0 ? " " : " "+(full ? siPrefixFull : siPrefix)[exponent-1]);
	}
	public static String formatNoSpace(String format,double x,boolean full) {
		byte exponent = getExponent(x);
		return String.format(format,scale(x,exponent))+(exponent <= 0 ? "" : (full ? siPrefixFull : siPrefix)[exponent-1]);
	}
	public static String custom(BiFunction<Double,String,String> callback,double x,boolean full) {
		byte exponent = getExponent(x);
		return callback.apply(scale(x,exponent),(exponent <= 0 ? " " : " "+(full ? siPrefixFull : siPrefix)[exponent-1]));
	}
	public static double parse(String s) throws NumberFormatException {
		try {
			return Double.parseDouble(s);
		} catch (NumberFormatException e) {
			String suffix = s.substring(s.length()-1);
			for (int z = 0; z < siPrefix.length; z++) {
				if (suffix.equals(siPrefix[z]))
					return Double.parseDouble(s.substring(0,s.length()-1).trim())*Math.pow(1000,z+1);
			}
			throw new RuntimeException(e);
		}
	}
}
