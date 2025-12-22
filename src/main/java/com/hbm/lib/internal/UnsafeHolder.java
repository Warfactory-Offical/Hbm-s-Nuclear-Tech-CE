package com.hbm.lib.internal;

import com.hbm.core.HbmCorePlugin;

/**
 * Use sun.misc.Unsafe on Java 8, jdk.internal.misc.Unsafe on Java 9+.
 *
 * @author mlbv
 */
public final class UnsafeHolder {
    public static final AbstractUnsafe U = AbstractUnsafe.getUnsafe();

    private UnsafeHolder() {
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
