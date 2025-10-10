package com.hbm.lib;

import com.hbm.core.HbmCorePlugin;
import com.hbm.main.MainRegistry;
import org.jetbrains.annotations.ApiStatus;
import sun.misc.Unsafe;

import java.lang.invoke.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

@ApiStatus.Internal
public class UnsafeHolder {
    public static final Unsafe U;
    private static final Runnable SPIN_WAITER;

    static {
        MainRegistry.logger.info("Loading UnsafeHolder");
        U = getUnsafe();
        SPIN_WAITER = initOnSpinWait();
    }

    private static Unsafe getUnsafe() {
        Unsafe instance;
        try {
            final Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            instance = (Unsafe) field.get(null);
        } catch (Exception e) {
            MainRegistry.logger.error("Failed to get theUnsafe in Unsafe.class, trying UnsafeHolder", e);
            try { // Fallback for cleanroom loader but should never happen
                Class<?> holderClass = Class.forName("top.outlands.foundation.boot.UnsafeHolder");
                Field unsafeField = holderClass.getField("UNSAFE");
                instance = (Unsafe) unsafeField.get(null);
            } catch (Exception ignored) {
                try {
                    Constructor<Unsafe> c = Unsafe.class.getDeclaredConstructor();
                    c.setAccessible(true);
                    instance = c.newInstance();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        return instance;
    }

    private static Runnable initOnSpinWait() {
        try {
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            MethodHandle mh = lookup.findStatic(Thread.class, "onSpinWait", MethodType.methodType(void.class));
            CallSite cs = LambdaMetafactory.metafactory(
                    lookup,
                    "run",
                    MethodType.methodType(Runnable.class),
                    MethodType.methodType(void.class),
                    mh,
                    MethodType.methodType(void.class)
            );

            Runnable r = (Runnable) cs.getTarget().invokeExact();
            MainRegistry.logger.info("Java 9+ detected, using Thread.onSpinWait()");
            return r;
        } catch (Throwable t) {
            return () -> {
            };
        }
    }

    // mlbv: remove and replace it with Thread.onSpinWait() if we ever migrate to Java 9+
    public static void onSpinWait() {
        SPIN_WAITER.run();
    }

    public static long fieldOffset(Class<?> clz, String fieldName) throws RuntimeException {
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

    public static long fieldOffset(Class<?> clz, String mcp, String srg) throws RuntimeException {
        try {
            return U.objectFieldOffset(clz.getDeclaredField(HbmCorePlugin.runtimeDeobfEnabled() ? srg : mcp));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
