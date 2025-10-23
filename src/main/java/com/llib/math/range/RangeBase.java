package com.llib.math.range;

public abstract class RangeBase implements Cloneable {
    public abstract double lerp(double t);
    public abstract float lerp(float t);
    public abstract double ratio(double x);
    public abstract float ratio(float x);
    public abstract double doubleMin();
    public abstract double doubleMax();
    public long lerpFloor(double t) {
        return (long)Math.floor(this.lerp(t));
    }
    public int lerpFloor(float t) {
        return (int)Math.floor(this.lerp(t));
    }
    public long lerpCeil(double t) {
        return (long)Math.ceil(this.lerp(t));
    }
    public int lerpCeil(float t) {
        return (int)Math.ceil(this.lerp(t));
    }
    public long lerpRound(double t) {
        return (long)Math.round(this.lerp(t));
    }
    public int lerpRound(float t) {
        return (int)Math.round(this.lerp(t));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RangeBase) {
            return (((RangeBase) obj).doubleMin() == this.doubleMin()) && (((RangeBase) obj).doubleMax() == this.doubleMax());
        }
        return super.equals(obj);
    }
}
