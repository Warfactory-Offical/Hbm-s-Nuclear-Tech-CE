package com.hbm.lib.internal;

import com.hbm.core.HbmCorePlugin;

/**
 * Use sun.misc.Unsafe on Java 8, jdk.internal.misc.Unsafe on Java 9+.
 *
 * @author mlbv
 */
public final class UnsafeHolder {
    public static final AbstractUnsafe U = AbstractUnsafe.getUnsafe();

    public static final long IA_BASE = U.arrayBaseOffset(int[].class);
    public static final long IA_SHIFT = Integer.numberOfTrailingZeros(U.arrayIndexScale(int[].class));
    public static final long JA_BASE = U.arrayBaseOffset(long[].class);
    public static final long JA_SHIFT = Integer.numberOfTrailingZeros(U.arrayIndexScale(long[].class));
    public static final long BA_BASE = U.arrayBaseOffset(byte[].class);
    public static final long BA_SHIFT = Integer.numberOfTrailingZeros(U.arrayIndexScale(byte[].class));
    public static final long ZA_BASE = U.arrayBaseOffset(boolean[].class);
    public static final long ZA_SHIFT = Integer.numberOfTrailingZeros(U.arrayIndexScale(boolean[].class));
    public static final long SA_BASE = U.arrayBaseOffset(short[].class);
    public static final long SA_SHIFT = Integer.numberOfTrailingZeros(U.arrayIndexScale(short[].class));
    public static final long CA_BASE = U.arrayBaseOffset(char[].class);
    public static final long CA_SHIFT = Integer.numberOfTrailingZeros(U.arrayIndexScale(char[].class));
    public static final long FA_BASE = U.arrayBaseOffset(float[].class);
    public static final long FA_SHIFT = Integer.numberOfTrailingZeros(U.arrayIndexScale(float[].class));
    public static final long DA_BASE = U.arrayBaseOffset(double[].class);
    public static final long DA_SHIFT = Integer.numberOfTrailingZeros(U.arrayIndexScale(double[].class));
    public static final long RA_BASE = U.arrayBaseOffset(Object[].class);
    public static final long RA_SHIFT = Integer.numberOfTrailingZeros(U.arrayIndexScale(Object[].class));

    private UnsafeHolder() {
    }

    public static long offInt(int i) {
        return ((long) i << IA_SHIFT) + IA_BASE;
    }

    public static long offLong(int i) {
        return ((long) i << JA_SHIFT) + JA_BASE;
    }

    public static long offByte(int i) {
        return ((long) i << BA_SHIFT) + BA_BASE;
    }

    public static long offBoolean(int i) {
        return ((long) i << ZA_SHIFT) + ZA_BASE;
    }

    public static long offShort(int i) {
        return ((long) i << SA_SHIFT) + SA_BASE;
    }

    public static long offChar(int i) {
        return ((long) i << CA_SHIFT) + CA_BASE;
    }

    public static long offFloat(int i) {
        return ((long) i << FA_SHIFT) + FA_BASE;
    }

    public static long offDouble(int i) {
        return ((long) i << DA_SHIFT) + DA_BASE;
    }

    public static long offReference(int i) {
        return ((long) i << RA_SHIFT) + RA_BASE;
    }

    public static long fieldOffset(Class<?> clz, String fieldName) {
        try {
            return U.objectFieldOffset(clz.getDeclaredField(fieldName));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T allocateInstance(Class<? extends T> clz) {
        try {
            //noinspection unchecked
            return (T) U.allocateInstance(clz);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public static long fieldOffset(Class<?> clz, String mcp, String srg) {
        try {
            return U.objectFieldOffset(clz.getDeclaredField(HbmCorePlugin.chooseName(mcp, srg)));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
