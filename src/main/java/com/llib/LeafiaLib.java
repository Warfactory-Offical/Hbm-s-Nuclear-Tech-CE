package com.llib;

import java.util.function.Function;

public class LeafiaLib {
	/**
	 * <s>Replaces all occurrences of <tt>a</tt> and <tt>b</tt> within <tt>s</tt> with the other.</s>
	 * <p>Too painful to make.
	 * @param s The string to replace texts within
	 * @param a Pattern A
	 * @param b Pattern B
	 * @return Resulting string
	 */
	public static String stringSwap(String s,String a,String b) {
		/*
		StringBuilder builder = new StringBuilder(s.length());
		StringBuilder patternA = new StringBuilder();
		StringBuilder patternB = new StringBuilder();
		for (char c : s.toCharArray()) {
			if (a.charAt(patternA.length()) == c) {
				patternA.append(c);
				if (patternA.toString().equals(a)) {
					builder.append(patternA);
					patternA = new StringBuilder();
				}
			} else if (b.charAt(patternB.length()) == c) {
				patternB.append(c);
				if (patternB.toString().equals(b)) {
					builder.append(patternB);
					patternB = new StringBuilder();
				}
			} else {

			}
			if (patternA.length() >= a.length()) {
				builder.append(patternA);
				patternA = new StringBuilder();
			} else if (patternB.length() >= b.length()) {
				builder.append(patternB);
				patternB = new StringBuilder();
			}
		}
		return builder.toString();*/
		throw new UnsupportedOperationException("Too painful to make :/");
	}
	/**
	 * Replaces all occurrences of <tt>a</tt> and <tt>b</tt> within <tt>s</tt> with the other.
	 * @param s The string to replace texts within
	 * @param a Char A
	 * @param b Char B
	 * @return Resulting string
	 */
	public static String stringSwap(String s,char a,char b) {
		StringBuilder builder = new StringBuilder(s.length());
		for (char c : s.toCharArray()) {
			if (c == a)
				builder.append(b);
			else if (c == b)
				builder.append(a);
			else
				builder.append(c);
		}
		return builder.toString();
	}
	public static class NumScale {
		static public final int KILO = 1_000;
		static public final int MEGA = 1_000_000;
		static public final int GIGA = 1_000_000_000;
		static public final long TERRA = 1_000_000_000_000L;
		static public final long PETA = 1_000_000_000_000_000L;
		static public final long EXA = 1_000_000_000_000_000_000L;
		//,ZETTA,YOTTA,RONNA,QUETTA;
	}
	static int brailleStart = 0x2800;
	static int[] brailleMapping = new int[]{1<<6,1<<2,1<<1,1,1<<7,1<<5,1<<4,1<<3};
	public static String[] drawGraph(int width,int height,int lfSpacing,double minX,double maxX,double minY,double maxY,Function<Double,Double> callback) {
		double increment = (maxX-minX)/width/2;
		String[] output = new String[height];
		int dotHeight = height*4 + lfSpacing*(height-1);
		for (int i = 0; i < height; i++) output[i] = "";
		Integer prevY = null;
		for (int xb = 0; xb < width*2; xb+=2) {
			int[] bits = new int[height];
			int column = 0;
			for (int xf = xb; xf <= xb+1; xf++) {
				double xcal = xf*increment;
				double ycal = callback.apply(xcal);
				int yf = (int)((ycal-minY)/(maxY-minY)*dotHeight+0.5);
				int yTgt = yf;
				if (prevY != null) yTgt = prevY;
				if (Math.abs(yf-yTgt) >= 1) {
					int difference = yf-yTgt;
					yTgt += (int)Math.signum(difference);
				}
				int drawY0 = Math.min(yf,yTgt);
				int drawY1 = Math.max(yf,yTgt);
				for (int row = 0; row < height; row++) {
					int y0 = row*(4+lfSpacing+1);
					int y1 = y0+3;
					if (drawY0 < y0 && drawY1 < y0) continue;
					if (drawY0 > y1 && drawY1 > y1) continue;
					for (int yc = Math.max(drawY0,y0); yc <= Math.min(drawY1,y1); yc++) {
						int offset = yc-y0;
						bits[row] = bits[row] | brailleMapping[offset+column*4];
					}
				}
				column++;
				prevY = yf;
			}
			for (int i = 0; i < height; i++) {
				output[height-1-i] = output[height-1-i] + (char)(brailleStart+bits[i]);
			}
		}
		return output;
	}
}
