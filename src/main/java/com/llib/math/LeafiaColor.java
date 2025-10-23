package com.llib.math;

public class LeafiaColor { // Color utility for doing complex color mixing
	public double red = 1;
	public double green = 1;
	public double blue = 1;
	public double alpha = 1;
	public LeafiaColor() {}
	public LeafiaColor(double red,double green,double blue,double alpha) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
	}
	public LeafiaColor(double red,double green,double blue) {
		this(red,green,blue,1);
	}
	/**
	 * Creates LeafiaColor from color code, in <b>Inverse-ARGB</b> format.
	 * <br>An alpha value of 00 would be opaque, whereas FF would be invisible.
	 * <p>In other words, <b>Alpha here basically indicates transparency, instead of opacity.</b>
	 * @param code Color code [0xAA_RRGGBB]
	 */
	public LeafiaColor(int code) {
		red = toDouble(code>>>0_20&0xFF);
		green = toDouble(code>>>0_10&0xFF);
		blue = toDouble(code&0xFF);
		alpha = toDouble(0xFF-(code>>>0_30));
	}
	public LeafiaColor lerp(LeafiaColor other,double ratio) {
		LeafiaColor color = new LeafiaColor();
		color.red = red+(other.red-red)*ratio;
		color.green = green+(other.green-green)*ratio;
		color.blue = blue+(other.blue-blue)*ratio;
		color.alpha = alpha+(other.alpha-alpha)*ratio;
		return color;
	}
	public LeafiaColor(LeafiaColor other) {
		red = other.red;
		green = other.green;
		blue = other.blue;
		alpha = other.alpha;
	}
	public LeafiaColor(double brightness,double alpha) {
		this(brightness,brightness,brightness,alpha);
	}
	public LeafiaColor(double brightness) {
		this(brightness,1);
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LeafiaColor) {
			LeafiaColor color = (LeafiaColor)obj;
			return color.red == this.red && color.green == this.green && color.blue == this.blue && color.alpha == this.alpha;
		}
		return super.equals(obj);
	}

	/**
	 * Similar to <tt>LeafiaColor.equals</tt> but does not take alpha channel into account.
	 * @param obj
	 * @return
	 */
	public boolean equalsNoAlpha(Object obj) {
		if (obj instanceof LeafiaColor) {
			LeafiaColor color = (LeafiaColor)obj;
			return color.red == this.red && color.green == this.green && color.blue == this.blue;
		}
		return super.equals(obj);
	}
	public double toDouble(int integer) {
		return integer/255d;
	}
	/**
	 * Deep-fries the color lmao
	 * More specifically, <tt>x</tt> as the value of each colors and <tt>max</tt> as the maximum among RGB,
	 * each colors will be the value of
	 * <pre>{@code Math.pow(x/max,double exponent)*max}</pre>
	 * Also should be noted that if <tt>max</tt> will be capped to minimum of 0.001 if it's smaller than that.
	 * <p>Alpha channel is not affected.
	 * @param exponent The exponent. Higher for higher contrast, lower for more dull coloring.
	 * @return Resulting color
	 */
	public LeafiaColor fry(double exponent) {
		LeafiaColor color = new LeafiaColor();
		double max = Math.max(Math.max(red,green),Math.max(blue,0.001));
		color.red = Math.pow(red/max,exponent)*max;
		color.green = Math.pow(green/max,exponent)*max;
		color.blue = Math.pow(blue/max,exponent)*max;
		color.alpha = alpha;
		return color;
	}
	public LeafiaColor multiply(double mul) {
		return new LeafiaColor(red*mul,green*mul,blue*mul,alpha);
	}
	public LeafiaColor multiply(double mulRed,double mulGreen,double mulBlue) {
		return new LeafiaColor(red*mulRed,green*mulGreen,blue*mulBlue,alpha);
	}
	public LeafiaColor multiply(double mulRed,double mulGreen,double mulBlue,double mulAlpha) {
		return new LeafiaColor(red*mulRed,green*mulGreen,blue*mulBlue,alpha*mulAlpha);
	}
	public LeafiaColor multiplyAlpha(double mul) {
		return new LeafiaColor(red,green,blue,alpha*mul);
	}
	/**
	 * Generates color code in <b>Inverse-ARGB</b> format.
	 * <br>An alpha value of 00 would be opaque, whereas FF would be invisible.
	 * <p>In other words, <b>Alpha here basically indicates transparency, instead of opacity.</b>
	 * <h2>WARNING: May break if this LeafiaColor has values outside the range of 0~1!
	 * @return Color code in Inverse-ARGB format
	 */
	public int toInARGB() {
		return (int)(Math.round(red*255)<<0_20|Math.round(green*255)<<0_10|Math.round(blue*255)|Math.round(255-alpha*255)<<0_30);
	}
	public int toARGB() {
		return (int)(Math.round(red*255)<<0_20|Math.round(green*255)<<0_10|Math.round(blue*255)|Math.round(alpha*255)<<0_30);
	}

	/**
	 * @return The <tt>red</tt> field in float
	 */
	public float getRed() { return (float)red; }
	/**
	 * @return The <tt>green</tt> field in float
	 */
	public float getGreen() { return (float)green; }
	/**
	 * @return The <tt>blue</tt> field in float
	 */
	public float getBlue() { return (float)blue; }
	/**
	 * @return The <tt>alpha</tt> field in float
	 */
	public float getAlpha() { return (float)alpha; }
}
