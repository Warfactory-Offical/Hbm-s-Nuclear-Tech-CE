package com.llib.math.range;

public class RangeFloat extends RangeBase {
    public float min;
    public float max;
    public RangeFloat(float min,float max) {
        this.min = min;
        this.max = max;
    }
    public boolean isInRange(int x) {
        if (x > max) return false;
        if (x < min) return false;
        return true;
    }
    public boolean isInRange(long x) {
        if (x > max) return false;
        if (x < min) return false;
        return true;
    }
    public boolean isInRange(float x) {
        if (x > max) return false;
        if (x < min) return false;
        return true;
    }
    public boolean isInRange(double x) {
        if (x > max) return false;
        if (x < min) return false;
        return true;
    }
    public double lerp(double t) {
        return min + (max - min) * t;
    }
    public float lerp(float t) {
        return min + (max - min) * t;
    }
    public double ratio(double x) { return (x-min)/(max-min); }
    public float ratio(float x) { return (float)((x-min)/(max-min)); }

    @Override
    public double doubleMin() {
        return this.min;
    }
    @Override
    public double doubleMax() {
        return this.max;
    }

    @Override
    public RangeFloat clone() {
        try {
            RangeFloat clone = (RangeFloat) super.clone();
            clone.min = this.min;
            clone.max = this.max;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
