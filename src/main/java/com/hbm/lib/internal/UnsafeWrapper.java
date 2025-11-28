package com.hbm.lib.internal;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

/**
 * Switch between different versions of Unsafe automatically depending on the Java version.
 *
 * @author mlbv
 */
@SuppressWarnings("MethodMayBeStatic")
public final class UnsafeWrapper {
    private static final UnsafeWrapper theUnsafe = new UnsafeWrapper();

    private static final MethodHandle OBJECT_FIELD_OFFSET;
    private static final MethodHandle STATIC_FIELD_BASE;
    private static final MethodHandle STATIC_FIELD_OFFSET;
    private static final MethodHandle ALLOCATE_INSTANCE;
    private static final MethodHandle ARRAY_BASE_OFFSET;
    private static final MethodHandle ARRAY_INDEX_SCALE;
    private static final MethodHandle COMPARE_AND_SET_REFERENCE;
    private static final MethodHandle COMPARE_AND_SET_LONG;
    private static final MethodHandle GET_REFERENCE;
    private static final MethodHandle GET_REFERENCE_VOLATILE;
    private static final MethodHandle GET_LONG_VOLATILE;
    private static final MethodHandle GET_INT;
    private static final MethodHandle PUT_REFERENCE;
    private static final MethodHandle PUT_DOUBLE;
    private static final MethodHandle PUT_INT;
    private static final MethodHandle PUT_BOOLEAN_VOLATILE;
    private static final MethodHandle GET_AND_SET_REFERENCE;
    private static final MethodHandle PUT_LONG_RELEASE;
    private static final MethodHandle ALLOCATE_MEMORY;
    private static final MethodHandle FREE_MEMORY;
    private static final MethodHandle SET_MEMORY;
    private static final MethodHandle COPY_MEMORY;
    private static final MethodHandle PUT_LONG_ADDRESS;
    private static final MethodHandle GET_LONG_ADDRESS;
    static final MethodHandles.Lookup IMPL_LOOKUP;
    static final int MAJOR_VERSION;
    static final boolean JPMS;

    static {
        try {
            String version = System.getProperty("java.version");
            if (version.startsWith("1.")) {
                version = version.substring(2, 3);
            } else {
                int dot = version.indexOf('.');
                if (dot != -1) {
                    version = version.substring(0, dot);
                }
            }
            MAJOR_VERSION = Integer.parseInt(version);
            JPMS = MAJOR_VERSION >= 9;
            MethodHandles.Lookup lookup;
            Unsafe sunUnsafe = null;
            if (MAJOR_VERSION >= 23) {
                lookup = TrustedLookupAccessor.lookup();
            } else {
                sunUnsafe = getSunUnsafe();
                lookup = getImplLookupUnsafe(sunUnsafe);
            }
            IMPL_LOOKUP = lookup;

            Class<?> unsafeClass;
            Object unsafeInstance;
            if (JPMS) {
                unsafeClass = Class.forName("jdk.internal.misc.Unsafe");
                unsafeInstance = lookup.findStatic(unsafeClass, "getUnsafe", MethodType.methodType(unsafeClass)).invoke();
            } else {
                unsafeClass = Unsafe.class;
                unsafeInstance = sunUnsafe;
            }

            String referenceGetter = JPMS ? "getReference" : "getObject";
            String referenceVolatileGetter = JPMS ? "getReferenceVolatile" : "getObjectVolatile";
            String referenceSetter = JPMS ? "putReference" : "putObject";
            String casReference = JPMS ? "compareAndSetReference" : "compareAndSwapObject";
            String casLong = JPMS ? "compareAndSetLong" : "compareAndSwapLong";
            String getAndSetReference = JPMS ? "getAndSetReference" : "getAndSetObject";
            String putLongRelease = JPMS ? "putLongRelease" : "putOrderedLong";

            OBJECT_FIELD_OFFSET = bindVirtual(lookup, unsafeClass, "objectFieldOffset", MethodType.methodType(long.class, Field.class), unsafeInstance);
            STATIC_FIELD_BASE = bindVirtual(lookup, unsafeClass, "staticFieldBase", MethodType.methodType(Object.class, Field.class), unsafeInstance);
            STATIC_FIELD_OFFSET = bindVirtual(lookup, unsafeClass, "staticFieldOffset", MethodType.methodType(long.class, Field.class), unsafeInstance);
            ALLOCATE_INSTANCE = bindVirtual(lookup, unsafeClass, "allocateInstance", MethodType.methodType(Object.class, Class.class), unsafeInstance);
            final boolean JDK_8344168 = MAJOR_VERSION >= 25;
            MethodHandle arrayBaseHandle = bindVirtual(lookup, unsafeClass, "arrayBaseOffset", MethodType.methodType(JDK_8344168 ? long.class : int.class, Class.class), unsafeInstance);
            if (!JDK_8344168) arrayBaseHandle = MethodHandles.explicitCastArguments(arrayBaseHandle, MethodType.methodType(long.class, Class.class));
            ARRAY_BASE_OFFSET = arrayBaseHandle;
            ARRAY_INDEX_SCALE = bindVirtual(lookup, unsafeClass, "arrayIndexScale", MethodType.methodType(int.class, Class.class), unsafeInstance);
            COMPARE_AND_SET_REFERENCE = bindVirtual(lookup, unsafeClass, casReference, MethodType.methodType(boolean.class, Object.class, long.class, Object.class, Object.class), unsafeInstance);
            COMPARE_AND_SET_LONG = bindVirtual(lookup, unsafeClass, casLong, MethodType.methodType(boolean.class, Object.class, long.class, long.class, long.class), unsafeInstance);
            GET_REFERENCE = bindVirtual(lookup, unsafeClass, referenceGetter, MethodType.methodType(Object.class, Object.class, long.class), unsafeInstance);
            GET_REFERENCE_VOLATILE = bindVirtual(lookup, unsafeClass, referenceVolatileGetter, MethodType.methodType(Object.class, Object.class, long.class), unsafeInstance);
            GET_LONG_VOLATILE = bindVirtual(lookup, unsafeClass, "getLongVolatile", MethodType.methodType(long.class, Object.class, long.class), unsafeInstance);
            GET_INT = bindVirtual(lookup, unsafeClass, "getInt", MethodType.methodType(int.class, Object.class, long.class), unsafeInstance);
            PUT_REFERENCE = bindVirtual(lookup, unsafeClass, referenceSetter, MethodType.methodType(void.class, Object.class, long.class, Object.class), unsafeInstance);
            PUT_DOUBLE = bindVirtual(lookup, unsafeClass, "putDouble", MethodType.methodType(void.class, Object.class, long.class, double.class), unsafeInstance);
            PUT_INT = bindVirtual(lookup, unsafeClass, "putInt", MethodType.methodType(void.class, Object.class, long.class, int.class), unsafeInstance);
            PUT_BOOLEAN_VOLATILE = bindVirtual(lookup, unsafeClass, "putBooleanVolatile", MethodType.methodType(void.class, Object.class, long.class, boolean.class), unsafeInstance);
            GET_AND_SET_REFERENCE = bindVirtual(lookup, unsafeClass, getAndSetReference, MethodType.methodType(Object.class, Object.class, long.class, Object.class), unsafeInstance);
            PUT_LONG_RELEASE = bindVirtual(lookup, unsafeClass, putLongRelease, MethodType.methodType(void.class, Object.class, long.class, long.class), unsafeInstance);
            ALLOCATE_MEMORY = bindVirtual(lookup, unsafeClass, "allocateMemory", MethodType.methodType(long.class, long.class), unsafeInstance);
            FREE_MEMORY = bindVirtual(lookup, unsafeClass, "freeMemory", MethodType.methodType(void.class, long.class), unsafeInstance);
            SET_MEMORY = bindVirtual(lookup, unsafeClass, "setMemory", MethodType.methodType(void.class, long.class, long.class, byte.class), unsafeInstance);
            COPY_MEMORY = bindVirtual(lookup, unsafeClass, "copyMemory", MethodType.methodType(void.class, Object.class, long.class, Object.class, long.class, long.class), unsafeInstance);
            PUT_LONG_ADDRESS = bindVirtual(lookup, unsafeClass, "putLong", MethodType.methodType(void.class, long.class, long.class), unsafeInstance);
            GET_LONG_ADDRESS = bindVirtual(lookup, unsafeClass, "getLong", MethodType.methodType(long.class, long.class), unsafeInstance);
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

    private UnsafeWrapper() {
    }

    static UnsafeWrapper getUnsafe() {
        return theUnsafe;
    }

    static MethodHandles.Lookup implLookup() {
        return IMPL_LOOKUP;
    }

    public <T extends Throwable> long objectFieldOffset(Field field) throws T {
        try {
            return (long) OBJECT_FIELD_OFFSET.invokeExact(field);
        } catch (Throwable t) {
            // noinspection unchecked
            throw (T) t;
        }
    }

    public <T extends Throwable> Object staticFieldBase(Field field) throws T {
        try {
            return STATIC_FIELD_BASE.invokeExact(field);
        } catch (Throwable t) {
            // noinspection unchecked
            throw (T) t;
        }
    }

    public <T extends Throwable> long staticFieldOffset(Field field) throws T {
        try {
            return (long) STATIC_FIELD_OFFSET.invokeExact(field);
        } catch (Throwable t) {
            // noinspection unchecked
            throw (T) t;
        }
    }

    public <T extends Throwable> Object allocateInstance(Class<?> clz) throws T, InstantiationException {
        try {
            return ALLOCATE_INSTANCE.invokeExact(clz);
        } catch (InstantiationException e) {
            throw e;
        } catch (Throwable t) {
            // noinspection unchecked
            throw (T) t;
        }
    }

    public <T extends Throwable> long arrayBaseOffset(Class<?> arrayClass) throws T {
        try {
            return (long) ARRAY_BASE_OFFSET.invokeExact(arrayClass);
        } catch (Throwable t) {
            // noinspection unchecked
            throw (T) t;
        }
    }

    public <T extends Throwable> int arrayIndexScale(Class<?> arrayClass) throws T {
        try {
            return (int) ARRAY_INDEX_SCALE.invokeExact(arrayClass);
        } catch (Throwable t) {
            // noinspection unchecked
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean compareAndSwapObject(Object base, long offset, Object expect, Object update) throws T {
        try {
            return (boolean) COMPARE_AND_SET_REFERENCE.invokeExact(base, offset, expect, update);
        } catch (Throwable t) {
            // noinspection unchecked
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean compareAndSwapLong(Object base, long offset, long expect, long update) throws T {
        try {
            return (boolean) COMPARE_AND_SET_LONG.invokeExact(base, offset, expect, update);
        } catch (Throwable t) {
            // noinspection unchecked
            throw (T) t;
        }
    }

    public <T extends Throwable> Object getObject(Object base, long offset) throws T {
        try {
            return GET_REFERENCE.invokeExact(base, offset);
        } catch (Throwable t) {
            // noinspection unchecked
            throw (T) t;
        }
    }

    public <T extends Throwable> Object getObjectVolatile(Object base, long offset) throws T {
        try {
            return GET_REFERENCE_VOLATILE.invokeExact(base, offset);
        } catch (Throwable t) {
            // noinspection unchecked
            throw (T) t;
        }
    }

    public <T extends Throwable> long getLongVolatile(Object base, long offset) throws T {
        try {
            return (long) GET_LONG_VOLATILE.invokeExact(base, offset);
        } catch (Throwable t) {
            // noinspection unchecked
            throw (T) t;
        }
    }

    public <T extends Throwable> int getInt(Object base, long offset) throws T {
        try {
            return (int) GET_INT.invokeExact(base, offset);
        } catch (Throwable t) {
            // noinspection unchecked
            throw (T) t;
        }
    }

    public <T extends Throwable> void putObject(Object base, long offset, Object value) throws T {
        try {
            PUT_REFERENCE.invokeExact(base, offset, value);
        } catch (Throwable t) {
            // noinspection unchecked
            throw (T) t;
        }
    }

    public <T extends Throwable> void putDouble(Object base, long offset, double value) throws T {
        try {
            PUT_DOUBLE.invokeExact(base, offset, value);
        } catch (Throwable t) {
            // noinspection unchecked
            throw (T) t;
        }
    }

    public <T extends Throwable> void putInt(Object base, long offset, int value) throws T {
        try {
            PUT_INT.invokeExact(base, offset, value);
        } catch (Throwable t) {
            // noinspection unchecked
            throw (T) t;
        }
    }

    public <T extends Throwable> void putBooleanVolatile(Object base, long offset, boolean value) throws T {
        try {
            PUT_BOOLEAN_VOLATILE.invokeExact(base, offset, value);
        } catch (Throwable t) {
            // noinspection unchecked
            throw (T) t;
        }
    }

    public <T extends Throwable> Object getAndSetObject(Object base, long offset, Object value) throws T {
        try {
            return GET_AND_SET_REFERENCE.invokeExact(base, offset, value);
        } catch (Throwable t) {
            // noinspection unchecked
            throw (T) t;
        }
    }

    public <T extends Throwable> void putOrderedLong(Object base, long offset, long value) throws T {
        try {
            PUT_LONG_RELEASE.invokeExact(base, offset, value);
        } catch (Throwable t) {
            // noinspection unchecked
            throw (T) t;
        }
    }

    public <T extends Throwable> long allocateMemory(long bytes) throws T {
        try {
            return (long) ALLOCATE_MEMORY.invokeExact(bytes);
        } catch (Throwable t) {
            // noinspection unchecked
            throw (T) t;
        }
    }

    public <T extends Throwable> void freeMemory(long address) throws T {
        try {
            FREE_MEMORY.invokeExact(address);
        } catch (Throwable t) {
            // noinspection unchecked
            throw (T) t;
        }
    }

    public <T extends Throwable> void setMemory(long address, long bytes, byte value) throws T {
        try {
            SET_MEMORY.invokeExact(address, bytes, value);
        } catch (Throwable t) {
            // noinspection unchecked
            throw (T) t;
        }
    }

    public <T extends Throwable> void copyMemory(Object srcBase, long srcOffset, Object destBase, long destOffset, long bytes) throws T {
        try {
            COPY_MEMORY.invokeExact(srcBase, srcOffset, destBase, destOffset, bytes);
        } catch (Throwable t) {
            // noinspection unchecked
            throw (T) t;
        }
    }

    public <T extends Throwable> long getLong(long address) throws T {
        try {
            return (long) GET_LONG_ADDRESS.invokeExact(address);
        } catch (Throwable t) {
            // noinspection unchecked
            throw (T) t;
        }
    }

    public <T extends Throwable> void putLong(long address, long value) throws T {
        try {
            PUT_LONG_ADDRESS.invokeExact(address, value);
        } catch (Throwable t) {
            // noinspection unchecked
            throw (T) t;
        }
    }

    private static MethodHandle bindVirtual(MethodHandles.Lookup lookup, Class<?> owner, String name, MethodType type, Object target) throws NoSuchMethodException, IllegalAccessException {
        return lookup.findVirtual(owner, name, type).bindTo(target);
    }

    @SuppressWarnings("removal")
    private static MethodHandles.Lookup getImplLookupUnsafe(Unsafe unsafe) throws NoSuchFieldException {
        Field lookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
        Object base = unsafe.staticFieldBase(lookupField);
        long offset = unsafe.staticFieldOffset(lookupField);
        return (MethodHandles.Lookup) unsafe.getObject(base, offset);
    }

    private static Unsafe getSunUnsafe() throws NoSuchFieldException, IllegalAccessException {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }
}
