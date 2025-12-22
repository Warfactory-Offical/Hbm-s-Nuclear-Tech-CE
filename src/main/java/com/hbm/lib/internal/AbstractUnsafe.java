package com.hbm.lib.internal;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.nio.ByteOrder;

public abstract sealed class AbstractUnsafe permits InternalUnsafeWrapper, SunUnsafeWrapper {
    static final boolean BIG_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
    static final Unsafe sunUnsafe;
    static final MethodHandles.Lookup IMPL_LOOKUP;
    static final int MAJOR_VERSION;
    static final boolean JPMS;

    static {
        MAJOR_VERSION = Runtime.version().feature(); // JVMDG downgrade
        JPMS = MAJOR_VERSION >= 9;
        sunUnsafe = getSunUnsafe();
        IMPL_LOOKUP = getImplLookupUnsafe(sunUnsafe);
    }

    public static AbstractUnsafe getUnsafe() {
        return JPMS ? new InternalUnsafeWrapper() : new SunUnsafeWrapper();
    }

    @SuppressWarnings("removal")
    private static MethodHandles.Lookup getImplLookupUnsafe(Unsafe unsafe) {
        try {
            Field lookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            Object base = unsafe.staticFieldBase(lookupField);
            long offset = unsafe.staticFieldOffset(lookupField);
            return (MethodHandles.Lookup) unsafe.getObject(base, offset);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private static Unsafe getSunUnsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    // ================================================================================================================
    // 1. Offsets, Info & Instantiation
    // ================================================================================================================

    public abstract <T extends Throwable> long objectFieldOffset(Field f) throws T;

    public abstract <T extends Throwable> Object staticFieldBase(Field f) throws T;

    public abstract <T extends Throwable> long staticFieldOffset(Field f) throws T;

    public abstract <T extends Throwable> Object allocateInstance(Class<?> cls) throws InstantiationException, T;

    public abstract <T extends Throwable> Object allocateUninitializedArray(Class<?> componentType, int length) throws T;

    public abstract <T extends Throwable> long arrayBaseOffset(Class<?> cls) throws T;

    public abstract <T extends Throwable> int arrayIndexScale(Class<?> cls) throws T;

    public abstract <T extends Throwable> int addressSize() throws T;

    public abstract <T extends Throwable> int pageSize() throws T;


    // ================================================================================================================
    // 2. References (Object)
    // ================================================================================================================

    public abstract <T extends Throwable> Object getReference(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putReference(Object o, long offset, Object x) throws T;

    public abstract <T extends Throwable> Object getReferenceVolatile(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putReferenceVolatile(Object o, long offset, Object x) throws T;

    public abstract <T extends Throwable> Object getReferenceAcquire(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putReferenceRelease(Object o, long offset, Object x) throws T;

    public abstract <T extends Throwable> Object getReferenceOpaque(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putReferenceOpaque(Object o, long offset, Object x) throws T;

    // CAS & Atomic - Reference
    public abstract <T extends Throwable> boolean compareAndSetReference(Object o, long offset, Object expected, Object x) throws T;

    public abstract <T extends Throwable> Object getAndSetReference(Object o, long offset, Object x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetReference(Object o, long offset, Object expected, Object x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetReferenceAcquire(Object o, long offset, Object expected, Object x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetReferenceRelease(Object o, long offset, Object expected, Object x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetReferencePlain(Object o, long offset, Object expected, Object x) throws T;

    public abstract <T extends Throwable> Object compareAndExchangeReference(Object o, long offset, Object expected, Object x) throws T;

    public abstract <T extends Throwable> Object compareAndExchangeReferenceAcquire(Object o, long offset, Object expected, Object x) throws T;

    public abstract <T extends Throwable> Object compareAndExchangeReferenceRelease(Object o, long offset, Object expected, Object x) throws T;

    public abstract <T extends Throwable> Object getAndSetReferenceAcquire(Object o, long offset, Object x) throws T;

    public abstract <T extends Throwable> Object getAndSetReferenceRelease(Object o, long offset, Object x) throws T;


    // ================================================================================================================
    // 3. Primitives: Int
    // ================================================================================================================

    public abstract <T extends Throwable> int getInt(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putInt(Object o, long offset, int x) throws T;

    public abstract <T extends Throwable> int getIntVolatile(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putIntVolatile(Object o, long offset, int x) throws T;

    public abstract <T extends Throwable> int getIntAcquire(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putIntRelease(Object o, long offset, int x) throws T;

    public abstract <T extends Throwable> int getIntOpaque(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putIntOpaque(Object o, long offset, int x) throws T;

    public abstract <T extends Throwable> boolean compareAndSetInt(Object o, long offset, int expected, int x) throws T;

    public abstract <T extends Throwable> int getAndAddInt(Object o, long offset, int delta) throws T;

    public abstract <T extends Throwable> int getAndSetInt(Object o, long offset, int x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetInt(Object o, long offset, int expected, int x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetIntAcquire(Object o, long offset, int expected, int x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetIntRelease(Object o, long offset, int expected, int x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetIntPlain(Object o, long offset, int expected, int x) throws T;

    public abstract <T extends Throwable> int compareAndExchangeInt(Object o, long offset, int expected, int x) throws T;

    public abstract <T extends Throwable> int compareAndExchangeIntAcquire(Object o, long offset, int expected, int x) throws T;

    public abstract <T extends Throwable> int compareAndExchangeIntRelease(Object o, long offset, int expected, int x) throws T;

    public abstract <T extends Throwable> int getAndSetIntAcquire(Object o, long offset, int x) throws T;

    public abstract <T extends Throwable> int getAndSetIntRelease(Object o, long offset, int x) throws T;


    // ================================================================================================================
    // 4. Primitives: Long
    // ================================================================================================================

    public abstract <T extends Throwable> long getLong(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putLong(Object o, long offset, long x) throws T;

    public abstract <T extends Throwable> long getLongVolatile(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putLongVolatile(Object o, long offset, long x) throws T;

    public abstract <T extends Throwable> long getLongAcquire(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putLongRelease(Object o, long offset, long x) throws T;

    public abstract <T extends Throwable> long getLongOpaque(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putLongOpaque(Object o, long offset, long x) throws T;

    public abstract <T extends Throwable> boolean compareAndSetLong(Object o, long offset, long expected, long x) throws T;

    public abstract <T extends Throwable> long getAndAddLong(Object o, long offset, long delta) throws T;

    public abstract <T extends Throwable> long getAndSetLong(Object o, long offset, long x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetLong(Object o, long offset, long expected, long x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetLongAcquire(Object o, long offset, long expected, long x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetLongRelease(Object o, long offset, long expected, long x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetLongPlain(Object o, long offset, long expected, long x) throws T;

    public abstract <T extends Throwable> long compareAndExchangeLong(Object o, long offset, long expected, long x) throws T;

    public abstract <T extends Throwable> long compareAndExchangeLongAcquire(Object o, long offset, long expected, long x) throws T;

    public abstract <T extends Throwable> long compareAndExchangeLongRelease(Object o, long offset, long expected, long x) throws T;

    public abstract <T extends Throwable> long getAndSetLongAcquire(Object o, long offset, long x) throws T;

    public abstract <T extends Throwable> long getAndSetLongRelease(Object o, long offset, long x) throws T;


    // ================================================================================================================
    // 5. Primitives: Boolean
    // ================================================================================================================

    public abstract <T extends Throwable> boolean getBoolean(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putBoolean(Object o, long offset, boolean x) throws T;

    public abstract <T extends Throwable> boolean getBooleanVolatile(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putBooleanVolatile(Object o, long offset, boolean x) throws T;

    public abstract <T extends Throwable> boolean getBooleanAcquire(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putBooleanRelease(Object o, long offset, boolean x) throws T;

    public abstract <T extends Throwable> boolean getBooleanOpaque(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putBooleanOpaque(Object o, long offset, boolean x) throws T;

    public abstract <T extends Throwable> boolean compareAndSetBoolean(Object o, long offset, boolean expected, boolean x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetBoolean(Object o, long offset, boolean expected, boolean x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetBooleanAcquire(Object o, long offset, boolean expected, boolean x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetBooleanRelease(Object o, long offset, boolean expected, boolean x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetBooleanPlain(Object o, long offset, boolean expected, boolean x) throws T;

    public abstract <T extends Throwable> boolean compareAndExchangeBoolean(Object o, long offset, boolean expected, boolean x) throws T;

    public abstract <T extends Throwable> boolean compareAndExchangeBooleanAcquire(Object o, long offset, boolean expected, boolean x) throws T;

    public abstract <T extends Throwable> boolean compareAndExchangeBooleanRelease(Object o, long offset, boolean expected, boolean x) throws T;


    // ================================================================================================================
    // 6. Primitives: Byte
    // ================================================================================================================

    public abstract <T extends Throwable> byte getByte(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putByte(Object o, long offset, byte x) throws T;

    public abstract <T extends Throwable> byte getByteVolatile(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putByteVolatile(Object o, long offset, byte x) throws T;

    public abstract <T extends Throwable> byte getByteAcquire(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putByteRelease(Object o, long offset, byte x) throws T;

    public abstract <T extends Throwable> byte getByteOpaque(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putByteOpaque(Object o, long offset, byte x) throws T;

    public abstract <T extends Throwable> boolean compareAndSetByte(Object o, long offset, byte expected, byte x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetByte(Object o, long offset, byte expected, byte x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetByteAcquire(Object o, long offset, byte expected, byte x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetByteRelease(Object o, long offset, byte expected, byte x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetBytePlain(Object o, long offset, byte expected, byte x) throws T;

    public abstract <T extends Throwable> byte compareAndExchangeByte(Object o, long offset, byte expected, byte x) throws T;

    public abstract <T extends Throwable> byte compareAndExchangeByteAcquire(Object o, long offset, byte expected, byte x) throws T;

    public abstract <T extends Throwable> byte compareAndExchangeByteRelease(Object o, long offset, byte expected, byte x) throws T;


    // ================================================================================================================
    // 7. Primitives: Short
    // ================================================================================================================

    public abstract <T extends Throwable> short getShort(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putShort(Object o, long offset, short x) throws T;

    public abstract <T extends Throwable> short getShortVolatile(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putShortVolatile(Object o, long offset, short x) throws T;

    public abstract <T extends Throwable> short getShortAcquire(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putShortRelease(Object o, long offset, short x) throws T;

    public abstract <T extends Throwable> short getShortOpaque(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putShortOpaque(Object o, long offset, short x) throws T;

    public abstract <T extends Throwable> boolean compareAndSetShort(Object o, long offset, short expected, short x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetShort(Object o, long offset, short expected, short x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetShortAcquire(Object o, long offset, short expected, short x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetShortRelease(Object o, long offset, short expected, short x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetShortPlain(Object o, long offset, short expected, short x) throws T;

    public abstract <T extends Throwable> short compareAndExchangeShort(Object o, long offset, short expected, short x) throws T;

    public abstract <T extends Throwable> short compareAndExchangeShortAcquire(Object o, long offset, short expected, short x) throws T;

    public abstract <T extends Throwable> short compareAndExchangeShortRelease(Object o, long offset, short expected, short x) throws T;


    // ================================================================================================================
    // 8. Primitives: Char
    // ================================================================================================================

    public abstract <T extends Throwable> char getChar(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putChar(Object o, long offset, char x) throws T;

    public abstract <T extends Throwable> char getCharVolatile(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putCharVolatile(Object o, long offset, char x) throws T;

    public abstract <T extends Throwable> char getCharAcquire(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putCharRelease(Object o, long offset, char x) throws T;

    public abstract <T extends Throwable> char getCharOpaque(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putCharOpaque(Object o, long offset, char x) throws T;

    public abstract <T extends Throwable> boolean compareAndSetChar(Object o, long offset, char expected, char x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetChar(Object o, long offset, char expected, char x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetCharAcquire(Object o, long offset, char expected, char x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetCharRelease(Object o, long offset, char expected, char x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetCharPlain(Object o, long offset, char expected, char x) throws T;

    public abstract <T extends Throwable> char compareAndExchangeChar(Object o, long offset, char expected, char x) throws T;

    public abstract <T extends Throwable> char compareAndExchangeCharAcquire(Object o, long offset, char expected, char x) throws T;

    public abstract <T extends Throwable> char compareAndExchangeCharRelease(Object o, long offset, char expected, char x) throws T;


    // ================================================================================================================
    // 9. Primitives: Float
    // ================================================================================================================

    public abstract <T extends Throwable> float getFloat(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putFloat(Object o, long offset, float x) throws T;

    public abstract <T extends Throwable> float getFloatVolatile(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putFloatVolatile(Object o, long offset, float x) throws T;

    public abstract <T extends Throwable> float getFloatAcquire(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putFloatRelease(Object o, long offset, float x) throws T;

    public abstract <T extends Throwable> float getFloatOpaque(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putFloatOpaque(Object o, long offset, float x) throws T;

    public abstract <T extends Throwable> boolean compareAndSetFloat(Object o, long offset, float expected, float x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetFloat(Object o, long offset, float expected, float x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetFloatAcquire(Object o, long offset, float expected, float x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetFloatRelease(Object o, long offset, float expected, float x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetFloatPlain(Object o, long offset, float expected, float x) throws T;

    public abstract <T extends Throwable> float compareAndExchangeFloat(Object o, long offset, float expected, float x) throws T;

    public abstract <T extends Throwable> float compareAndExchangeFloatAcquire(Object o, long offset, float expected, float x) throws T;

    public abstract <T extends Throwable> float compareAndExchangeFloatRelease(Object o, long offset, float expected, float x) throws T;


    // ================================================================================================================
    // 10. Primitives: Double
    // ================================================================================================================

    public abstract <T extends Throwable> double getDouble(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putDouble(Object o, long offset, double x) throws T;

    public abstract <T extends Throwable> double getDoubleVolatile(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putDoubleVolatile(Object o, long offset, double x) throws T;

    public abstract <T extends Throwable> double getDoubleAcquire(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putDoubleRelease(Object o, long offset, double x) throws T;

    public abstract <T extends Throwable> double getDoubleOpaque(Object o, long offset) throws T;

    public abstract <T extends Throwable> void putDoubleOpaque(Object o, long offset, double x) throws T;

    public abstract <T extends Throwable> boolean compareAndSetDouble(Object o, long offset, double expected, double x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetDouble(Object o, long offset, double expected, double x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetDoubleAcquire(Object o, long offset, double expected, double x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetDoubleRelease(Object o, long offset, double expected, double x) throws T;

    public abstract <T extends Throwable> boolean weakCompareAndSetDoublePlain(Object o, long offset, double expected, double x) throws T;

    public abstract <T extends Throwable> double compareAndExchangeDouble(Object o, long offset, double expected, double x) throws T;

    public abstract <T extends Throwable> double compareAndExchangeDoubleAcquire(Object o, long offset, double expected, double x) throws T;

    public abstract <T extends Throwable> double compareAndExchangeDoubleRelease(Object o, long offset, double expected, double x) throws T;


    // ================================================================================================================
    // 11. Raw Memory Access
    // ================================================================================================================

    public abstract <T extends Throwable> long allocateMemory(long bytes) throws T;

    public abstract <T extends Throwable> void freeMemory(long address) throws T;

    public abstract <T extends Throwable> long reallocateMemory(long address, long bytes) throws T;

    public abstract <T extends Throwable> void setMemory(long address, long bytes, byte value) throws T;

    public abstract <T extends Throwable> void copyMemory(Object srcBase, long srcOffset, Object destBase, long destOffset, long bytes) throws T;

    public abstract <T extends Throwable> long getLong(long address) throws T;

    public abstract <T extends Throwable> void putLong(long address, long value) throws T;


    // ================================================================================================================
    // 12. Fences & Synchronization
    // ================================================================================================================

    public abstract <T extends Throwable> void loadFence() throws T;

    public abstract <T extends Throwable> void storeFence() throws T;

    public abstract <T extends Throwable> void fullFence() throws T;

    public abstract <T extends Throwable> void park(boolean isAbsolute, long time) throws T;

    public abstract <T extends Throwable> void unpark(Object thread) throws T;

    /**
     * @deprecated use {@link #getReference(Object, long)} whenever possible
     */
    @Deprecated
    public final Object getObject(Object o, long offset) {
        return getReference(o, offset);
    }

    /**
     * @deprecated use {@link #putReference(Object, long, Object)} whenever possible
     */
    @Deprecated
    public final void putObject(Object o, long offset, Object x) {
        putReference(o, offset, x);
    }

    /**
     * @deprecated use {@link #getReferenceVolatile(Object, long)} whenever possible
     */
    @Deprecated
    public final Object getObjectVolatile(Object o, long offset) {
        return getReferenceVolatile(o, offset);
    }

    /**
     * @deprecated use {@link #putReferenceVolatile(Object, long, Object)} whenever possible
     */
    @Deprecated
    public final void putObjectVolatile(Object o, long offset, Object x) {
        putReferenceVolatile(o, offset, x);
    }

    /**
     * @deprecated use {@link #compareAndSetReference(Object, long, Object, Object)} whenever possible
     */
    @Deprecated
    public final boolean compareAndSwapObject(Object o, long offset, Object expected, Object x) {
        return compareAndSetReference(o, offset, expected, x);
    }

    /**
     * @deprecated use {@link #compareAndSetInt(Object, long, int, int)} whenever possible
     */
    @Deprecated
    public final boolean compareAndSwapInt(Object o, long offset, int expected, int x) {
        return compareAndSetInt(o, offset, expected, x);
    }

    /**
     * @deprecated use {@link #compareAndSetLong(Object, long, long, long)} whenever possible
     */
    @Deprecated
    public final boolean compareAndSwapLong(Object o, long offset, long expected, long x) {
        return compareAndSetLong(o, offset, expected, x);
    }

    /**
     * @deprecated use {@link #getAndSetReference(Object, long, Object)} whenever possible
     */
    @Deprecated
    public final Object getAndSetObject(Object o, long offset, Object x) {
        return getAndSetReference(o, offset, x);
    }

    /**
     * @deprecated use {@link #putIntRelease(Object, long, int)} whenever possible
     */
    @Deprecated
    public final void putOrderedInt(Object o, long offset, int x) {
        putIntRelease(o, offset, x);
    }

    /**
     * @deprecated use {@link #putLongRelease(Object, long, long)} whenever possible
     */
    @Deprecated
    public final void putOrderedLong(Object o, long offset, long x) {
        putLongRelease(o, offset, x);
    }

    /**
     * @deprecated use {@link #putReferenceRelease(Object, long, Object)} whenever possible
     */
    @Deprecated
    public final void putOrderedObject(Object o, long offset, Object x) {
        putReferenceRelease(o, offset, x);
    }
}
