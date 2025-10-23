package com.llib.math.range;

import java.util.Iterator;

public class RangeLong extends RangeBase implements Iterable<Long> {
    public long min;
    public long max;
    public RangeLong(long min,long max) {
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
    public Iterator<Long> iterator() {
        return new Iterator<Long>() {
            long cursor = min;
            @Override
            public boolean hasNext() {
                return cursor <= max;
            }
            @Override
            public Long next() {
                return cursor++;
            }
        };
    }

    @Override
    public RangeLong clone() {
        try {
            RangeLong clone = (RangeLong) super.clone();
            clone.min = this.min;
            clone.max = this.max;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
