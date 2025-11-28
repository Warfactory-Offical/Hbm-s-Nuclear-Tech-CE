package com.hbm.util;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.hbm.lib.Library;
import com.hbm.lib.internal.UnsafeHolder;

import java.io.Serializable;
import java.util.function.DoubleUnaryOperator;

/**
 * Basically com.google.common.util.concurrent.AtomicDouble with additional getAndUpdate and updateAndGet.
 *
 * @author mlbv
 */
public final class AtomicDouble extends Number implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final long VALUE_OFFSET = UnsafeHolder.fieldOffset(AtomicDouble.class, "value");
    private volatile long value;

    public AtomicDouble() {
        // assert doubleToRawLongBits(0.0) == 0L;
    }

    public AtomicDouble(double initialValue) {
        set(initialValue);
    }

    /**
     * Returns the current value (volatile read).
     */
    public double get() {
        long bits = this.value;
        return Double.longBitsToDouble(bits);
    }

    /**
     * Sets to the given value (volatile write).
     */
    public void set(double newValue) {
        this.value = Double.doubleToRawLongBits(newValue);
    }

    /**
     * Eventually sets to the given value.
     */
    public void lazySet(double newValue) {
        UnsafeHolder.U.putOrderedLong(this, VALUE_OFFSET, Double.doubleToRawLongBits(newValue));
    }

    /**
     * Atomically sets to the given value and returns the old value.
     */
    @CanIgnoreReturnValue
    public double getAndSet(double newValue) {
        long next = Double.doubleToRawLongBits(newValue);
        while (true) {
            long cur = this.value;
            if (UnsafeHolder.U.compareAndSwapLong(this, VALUE_OFFSET, cur, next)) {
                return Double.longBitsToDouble(cur);
            }
            Library.onSpinWait();
        }
    }

    /**
     * Atomically adds the given delta and returns the previous value.
     */
    @CanIgnoreReturnValue
    public double getAndAdd(double delta) {
        while (true) {
            long curBits = this.value;
            double cur = Double.longBitsToDouble(curBits);
            double next = cur + delta;
            long nextBits = Double.doubleToRawLongBits(next);
            if (UnsafeHolder.U.compareAndSwapLong(this, VALUE_OFFSET, curBits, nextBits)) {
                return cur;
            }
            Library.onSpinWait();
        }
    }

    /**
     * Atomically adds the given delta and returns the updated value.
     */
    @CanIgnoreReturnValue
    public double addAndGet(double delta) {
        while (true) {
            long curBits = this.value;
            double cur = Double.longBitsToDouble(curBits);
            double next = cur + delta;
            long nextBits = Double.doubleToRawLongBits(next);
            if (UnsafeHolder.U.compareAndSwapLong(this, VALUE_OFFSET, curBits, nextBits)) {
                return next;
            }
            Library.onSpinWait();
        }
    }

    /**
     * Atomically sets the value to {@code update} if the current value is exactly {@code expect} (bitwise).
     */
    public boolean compareAndSet(double expect, double update) {
        long e = Double.doubleToRawLongBits(expect);
        long u = Double.doubleToRawLongBits(update);
        return UnsafeHolder.U.compareAndSwapLong(this, VALUE_OFFSET, e, u);
    }

    /**
     * Applies {@code updateFunction} with the current value, atomically sets to result, and returns the old value.
     */
    @CanIgnoreReturnValue
    public double getAndUpdate(DoubleUnaryOperator updateFunction) {
        while (true) {
            long curBits = this.value;
            double cur = Double.longBitsToDouble(curBits);
            double next = updateFunction.applyAsDouble(cur);
            long nextBits = Double.doubleToRawLongBits(next);
            if (UnsafeHolder.U.compareAndSwapLong(this, VALUE_OFFSET, curBits, nextBits)) {
                return cur;
            }
            Library.onSpinWait();
        }
    }

    /**
     * Applies {@code updateFunction} with the current value, atomically sets to result, and returns the new value.
     */
    @CanIgnoreReturnValue
    public double updateAndGet(DoubleUnaryOperator updateFunction) {
        while (true) {
            long curBits = this.value;
            double cur = Double.longBitsToDouble(curBits);
            double next = updateFunction.applyAsDouble(cur);
            long nextBits = Double.doubleToRawLongBits(next);
            if (UnsafeHolder.U.compareAndSwapLong(this, VALUE_OFFSET, curBits, nextBits)) {
                return next;
            }
            Library.onSpinWait();
        }
    }

    @Override
    public int intValue() {
        return (int) get();
    }

    @Override
    public long longValue() {
        return (long) get();
    }

    @Override
    public float floatValue() {
        return (float) get();
    }

    @Override
    public double doubleValue() {
        return get();
    }

    @Override
    public String toString() {
        return Double.toString(get());
    }
}
