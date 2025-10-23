package com.llib.math;

public class MathLeafia {
	public static short getTime32s() {
		return (short)(Math.floorMod(System.currentTimeMillis(),32_767));
	}
	public static short getTimeDifference32s(int t1,int t2) {
		return (short)(Math.floorMod(t2-t1,32_767));
	}

	/**
	 * Trims both ends of a linear function with a sine function in a way they connect seamlessly.
	 * @param x Input value, range 0 ~ 1
	 * @param scale Scale of sine functions, where 1 = completely sine, 0 = completely linear
	 * @return Smoothed value
	 */
	public static double smoothLinear(double x,double scale) {
		if (x > 1) x = 1; else if (x < 0) x = 0;
		if (scale < 1e-6) return x; // Skip the calculation if the scale is too small to avoid errors
		double smoothingStartOffset = 0.5-scale/2;
		double initialInclination = Math.PI/(scale*2);
		double peak = smoothingStartOffset*2 * initialInclination + 1;
		double waveHeight = 0.5/peak;
		if (x <= 0.5-smoothingStartOffset)
			return (1-Math.cos(2*x*initialInclination))*waveHeight;
		else if (x >= 0.5+smoothingStartOffset)
			return Math.sin(2*( x - (0.5+smoothingStartOffset) )*initialInclination)*waveHeight + (1-waveHeight);
		else
			return (x-0.5)*initialInclination/peak + 0.5;
	}
}
