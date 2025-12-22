package com.hbm.lib.internal;

import java.lang.reflect.Field;

/**
 * Direct wrapper for sun.misc.Unsafe
 */
@SuppressWarnings("removal")
public final class SunUnsafeWrapper extends AbstractUnsafe {

    SunUnsafeWrapper() {
    }

    public <T extends Throwable> long objectFieldOffset(Field f) throws T {
        return sunUnsafe.objectFieldOffset(f);
    }

    public <T extends Throwable> Object staticFieldBase(Field f) throws T {
        return sunUnsafe.staticFieldBase(f);
    }

    public <T extends Throwable> long staticFieldOffset(Field f) throws T {
        return sunUnsafe.staticFieldOffset(f);
    }

    public <T extends Throwable> Object allocateInstance(Class<?> cls) throws InstantiationException {
        return sunUnsafe.allocateInstance(cls);
    }

    public <T extends Throwable> Object allocateUninitializedArray(Class<?> componentType, int length) throws T {
        if (componentType == null) throw new IllegalArgumentException("Component type is null");
        if (!componentType.isPrimitive()) throw new IllegalArgumentException("Component type is not primitive");
        if (length < 0) throw new IllegalArgumentException("Negative length");
        if (componentType == byte.class) return new byte[length];
        if (componentType == boolean.class) return new boolean[length];
        if (componentType == short.class) return new short[length];
        if (componentType == char.class) return new char[length];
        if (componentType == int.class) return new int[length];
        if (componentType == float.class) return new float[length];
        if (componentType == long.class) return new long[length];
        if (componentType == double.class) return new double[length];
        return null;
    }

    public <T extends Throwable> long arrayBaseOffset(Class<?> cls) throws T {
        return sunUnsafe.arrayBaseOffset(cls);
    }

    public <T extends Throwable> int arrayIndexScale(Class<?> cls) throws T {
        return sunUnsafe.arrayIndexScale(cls);
    }

    public <T extends Throwable> int addressSize() throws T {
        return sunUnsafe.addressSize();
    }

    public <T extends Throwable> int pageSize() throws T {
        return sunUnsafe.pageSize();
    }

    // --- 2. References ---

    public <T extends Throwable> Object getReference(Object o, long offset) throws T {
        return sunUnsafe.getObject(o, offset);
    }

    public <T extends Throwable> void putReference(Object o, long offset, Object x) throws T {
        sunUnsafe.putObject(o, offset, x);
    }

    public <T extends Throwable> Object getReferenceVolatile(Object o, long offset) throws T {
        return sunUnsafe.getObjectVolatile(o, offset);
    }

    public <T extends Throwable> void putReferenceVolatile(Object o, long offset, Object x) throws T {
        sunUnsafe.putObjectVolatile(o, offset, x);
    }

    public <T extends Throwable> Object getReferenceAcquire(Object o, long offset) throws T {
        Object v = sunUnsafe.getObject(o, offset);
        sunUnsafe.loadFence();
        return v;
    }

    public <T extends Throwable> void putReferenceRelease(Object o, long offset, Object x) throws T {
        sunUnsafe.putOrderedObject(o, offset, x);
    }

    public <T extends Throwable> Object getReferenceOpaque(Object o, long offset) throws T {
        return getReferenceAcquire(o, offset);
    }

    public <T extends Throwable> void putReferenceOpaque(Object o, long offset, Object x) throws T {
        putReferenceRelease(o, offset, x);
    }

    public <T extends Throwable> boolean compareAndSetReference(Object o, long offset, Object expected, Object x) throws T {
        return sunUnsafe.compareAndSwapObject(o, offset, expected, x);
    }

    public <T extends Throwable> Object getAndSetReference(Object o, long offset, Object x) throws T {
        return sunUnsafe.getAndSetObject(o, offset, x);
    }

    public <T extends Throwable> boolean weakCompareAndSetReference(Object o, long offset, Object expected, Object x) throws T {
        return sunUnsafe.compareAndSwapObject(o, offset, expected, x);
    }

    public <T extends Throwable> boolean weakCompareAndSetReferenceAcquire(Object o, long offset, Object expected, Object x) throws T {
        return sunUnsafe.compareAndSwapObject(o, offset, expected, x);
    }

    public <T extends Throwable> boolean weakCompareAndSetReferenceRelease(Object o, long offset, Object expected, Object x) throws T {
        return sunUnsafe.compareAndSwapObject(o, offset, expected, x);
    }

    public <T extends Throwable> boolean weakCompareAndSetReferencePlain(Object o, long offset, Object expected, Object x) throws T {
        return sunUnsafe.compareAndSwapObject(o, offset, expected, x);
    }

    public <T extends Throwable> Object compareAndExchangeReference(Object o, long offset, Object expected, Object x) throws T {
        while (true) {
            Object witness = sunUnsafe.getObjectVolatile(o, offset);
            if (witness != expected) {
                return witness;
            }
            if (sunUnsafe.compareAndSwapObject(o, offset, expected, x)) {
                return expected;
            }
        }
    }

    public <T extends Throwable> Object compareAndExchangeReferenceAcquire(Object o, long offset, Object expected, Object x) throws T {
        return compareAndExchangeReference(o, offset, expected, x);
    }

    public <T extends Throwable> Object compareAndExchangeReferenceRelease(Object o, long offset, Object expected, Object x) throws T {
        return compareAndExchangeReference(o, offset, expected, x);
    }

    public <T extends Throwable> Object getAndSetReferenceAcquire(Object o, long offset, Object x) throws T {
        return sunUnsafe.getAndSetObject(o, offset, x);
    }

    public <T extends Throwable> Object getAndSetReferenceRelease(Object o, long offset, Object x) throws T {
        return sunUnsafe.getAndSetObject(o, offset, x);
    }

    // --- 3. Int ---

    public <T extends Throwable> int getInt(Object o, long offset) throws T {
        return sunUnsafe.getInt(o, offset);
    }

    public <T extends Throwable> void putInt(Object o, long offset, int x) throws T {
        sunUnsafe.putInt(o, offset, x);
    }

    public <T extends Throwable> int getIntVolatile(Object o, long offset) throws T {
        return sunUnsafe.getIntVolatile(o, offset);
    }

    public <T extends Throwable> void putIntVolatile(Object o, long offset, int x) throws T {
        sunUnsafe.putIntVolatile(o, offset, x);
    }

    public <T extends Throwable> int getIntAcquire(Object o, long offset) throws T {
        int v = sunUnsafe.getInt(o, offset);
        sunUnsafe.loadFence();
        return v;
    }

    public <T extends Throwable> void putIntRelease(Object o, long offset, int x) throws T {
        sunUnsafe.putOrderedInt(o, offset, x);
    }

    public <T extends Throwable> int getIntOpaque(Object o, long offset) throws T {
        return getIntAcquire(o, offset);
    }

    public <T extends Throwable> void putIntOpaque(Object o, long offset, int x) throws T {
        putIntRelease(o, offset, x);
    }

    public <T extends Throwable> boolean compareAndSetInt(Object o, long offset, int expected, int x) throws T {
        return sunUnsafe.compareAndSwapInt(o, offset, expected, x);
    }

    public <T extends Throwable> int getAndAddInt(Object o, long offset, int delta) throws T {
        return sunUnsafe.getAndAddInt(o, offset, delta);
    }

    public <T extends Throwable> int getAndSetInt(Object o, long offset, int x) throws T {
        return sunUnsafe.getAndSetInt(o, offset, x);
    }

    public <T extends Throwable> boolean weakCompareAndSetInt(Object o, long offset, int expected, int x) throws T {
        return sunUnsafe.compareAndSwapInt(o, offset, expected, x);
    }

    public <T extends Throwable> boolean weakCompareAndSetIntAcquire(Object o, long offset, int expected, int x) throws T {
        return sunUnsafe.compareAndSwapInt(o, offset, expected, x);
    }

    public <T extends Throwable> boolean weakCompareAndSetIntRelease(Object o, long offset, int expected, int x) throws T {
        return sunUnsafe.compareAndSwapInt(o, offset, expected, x);
    }

    public <T extends Throwable> boolean weakCompareAndSetIntPlain(Object o, long offset, int expected, int x) throws T {
        return sunUnsafe.compareAndSwapInt(o, offset, expected, x);
    }

    public <T extends Throwable> int compareAndExchangeInt(Object o, long offset, int expected, int x) throws T {
        while (true) {
            int witness = sunUnsafe.getIntVolatile(o, offset);
            if (witness != expected) {
                return witness;
            }
            if (sunUnsafe.compareAndSwapInt(o, offset, expected, x)) {
                return expected;
            }
        }
    }

    public <T extends Throwable> int compareAndExchangeIntAcquire(Object o, long offset, int expected, int x) throws T {
        return compareAndExchangeInt(o, offset, expected, x);
    }

    public <T extends Throwable> int compareAndExchangeIntRelease(Object o, long offset, int expected, int x) throws T {
        return compareAndExchangeInt(o, offset, expected, x);
    }

    public <T extends Throwable> int getAndSetIntAcquire(Object o, long offset, int x) throws T {
        return sunUnsafe.getAndSetInt(o, offset, x);
    }

    public <T extends Throwable> int getAndSetIntRelease(Object o, long offset, int x) throws T {
        return sunUnsafe.getAndSetInt(o, offset, x);
    }

    // --- 4. Long ---

    public <T extends Throwable> long getLong(Object o, long offset) throws T {
        return sunUnsafe.getLong(o, offset);
    }

    public <T extends Throwable> void putLong(Object o, long offset, long x) throws T {
        sunUnsafe.putLong(o, offset, x);
    }

    public <T extends Throwable> long getLongVolatile(Object o, long offset) throws T {
        return sunUnsafe.getLongVolatile(o, offset);
    }

    public <T extends Throwable> void putLongVolatile(Object o, long offset, long x) throws T {
        sunUnsafe.putLongVolatile(o, offset, x);
    }

    public <T extends Throwable> long getLongAcquire(Object o, long offset) throws T {
        long v = sunUnsafe.getLong(o, offset);
        sunUnsafe.loadFence();
        return v;
    }

    public <T extends Throwable> void putLongRelease(Object o, long offset, long x) throws T {
        sunUnsafe.putOrderedLong(o, offset, x);
    }

    public <T extends Throwable> long getLongOpaque(Object o, long offset) throws T {
        return getLongAcquire(o, offset);
    }

    public <T extends Throwable> void putLongOpaque(Object o, long offset, long x) throws T {
        putLongRelease(o, offset, x);
    }

    public <T extends Throwable> boolean compareAndSetLong(Object o, long offset, long expected, long x) throws T {
        return sunUnsafe.compareAndSwapLong(o, offset, expected, x);
    }

    public <T extends Throwable> long getAndAddLong(Object o, long offset, long delta) throws T {
        return sunUnsafe.getAndAddLong(o, offset, delta);
    }

    public <T extends Throwable> long getAndSetLong(Object o, long offset, long x) throws T {
        return sunUnsafe.getAndSetLong(o, offset, x);
    }

    public <T extends Throwable> boolean weakCompareAndSetLong(Object o, long offset, long expected, long x) throws T {
        return sunUnsafe.compareAndSwapLong(o, offset, expected, x);
    }

    public <T extends Throwable> boolean weakCompareAndSetLongAcquire(Object o, long offset, long expected, long x) throws T {
        return sunUnsafe.compareAndSwapLong(o, offset, expected, x);
    }

    public <T extends Throwable> boolean weakCompareAndSetLongRelease(Object o, long offset, long expected, long x) throws T {
        return sunUnsafe.compareAndSwapLong(o, offset, expected, x);
    }

    public <T extends Throwable> boolean weakCompareAndSetLongPlain(Object o, long offset, long expected, long x) throws T {
        return sunUnsafe.compareAndSwapLong(o, offset, expected, x);
    }

    public <T extends Throwable> long compareAndExchangeLong(Object o, long offset, long expected, long x) throws T {
        while (true) {
            long witness = sunUnsafe.getLongVolatile(o, offset);
            if (witness != expected) {
                return witness;
            }
            if (sunUnsafe.compareAndSwapLong(o, offset, expected, x)) {
                return expected;
            }
        }
    }

    public <T extends Throwable> long compareAndExchangeLongAcquire(Object o, long offset, long expected, long x) throws T {
        return compareAndExchangeLong(o, offset, expected, x);
    }

    public <T extends Throwable> long compareAndExchangeLongRelease(Object o, long offset, long expected, long x) throws T {
        return compareAndExchangeLong(o, offset, expected, x);
    }

    public <T extends Throwable> long getAndSetLongAcquire(Object o, long offset, long x) throws T {
        return sunUnsafe.getAndSetLong(o, offset, x);
    }

    public <T extends Throwable> long getAndSetLongRelease(Object o, long offset, long x) throws T {
        return sunUnsafe.getAndSetLong(o, offset, x);
    }

    // --- 5. Boolean ---

    public <T extends Throwable> boolean getBoolean(Object o, long offset) throws T {
        return sunUnsafe.getBoolean(o, offset);
    }

    public <T extends Throwable> void putBoolean(Object o, long offset, boolean x) throws T {
        sunUnsafe.putBoolean(o, offset, x);
    }

    public <T extends Throwable> boolean getBooleanVolatile(Object o, long offset) throws T {
        return sunUnsafe.getBooleanVolatile(o, offset);
    }

    public <T extends Throwable> void putBooleanVolatile(Object o, long offset, boolean x) throws T {
        sunUnsafe.putBooleanVolatile(o, offset, x);
    }

    public <T extends Throwable> boolean getBooleanAcquire(Object o, long offset) throws T {
        boolean v = sunUnsafe.getBoolean(o, offset);
        sunUnsafe.loadFence();
        return v;
    }

    public <T extends Throwable> void putBooleanRelease(Object o, long offset, boolean x) throws T {
        sunUnsafe.storeFence();
        sunUnsafe.putBoolean(o, offset, x);
    }

    public <T extends Throwable> boolean getBooleanOpaque(Object o, long offset) throws T {
        return getBooleanAcquire(o, offset);
    }

    public <T extends Throwable> void putBooleanOpaque(Object o, long offset, boolean x) throws T {
        putBooleanRelease(o, offset, x);
    }

    public <T extends Throwable> boolean compareAndSetBoolean(Object o, long offset, boolean expected, boolean x) throws T {
        return compareAndSetByte(o, offset, (byte) (expected ? 1 : 0), (byte) (x ? 1 : 0));
    }

    public <T extends Throwable> boolean weakCompareAndSetBoolean(Object o, long offset, boolean expected, boolean x) throws T {
        return compareAndSetBoolean(o, offset, expected, x);
    }

    public <T extends Throwable> boolean weakCompareAndSetBooleanAcquire(Object o, long offset, boolean expected, boolean x) throws T {
        return compareAndSetBoolean(o, offset, expected, x);
    }

    public <T extends Throwable> boolean weakCompareAndSetBooleanRelease(Object o, long offset, boolean expected, boolean x) throws T {
        return compareAndSetBoolean(o, offset, expected, x);
    }

    public <T extends Throwable> boolean weakCompareAndSetBooleanPlain(Object o, long offset, boolean expected, boolean x) throws T {
        return compareAndSetBoolean(o, offset, expected, x);
    }

    public <T extends Throwable> boolean compareAndExchangeBoolean(Object o, long offset, boolean expected, boolean x) throws T {
        return compareAndExchangeByte(o, offset, (byte) (expected ? 1 : 0), (byte) (x ? 1 : 0)) != 0;
    }

    public <T extends Throwable> boolean compareAndExchangeBooleanAcquire(Object o, long offset, boolean expected, boolean x) throws T {
        return compareAndExchangeBoolean(o, offset, expected, x);
    }

    public <T extends Throwable> boolean compareAndExchangeBooleanRelease(Object o, long offset, boolean expected, boolean x) throws T {
        return compareAndExchangeBoolean(o, offset, expected, x);
    }

    // --- 6. Byte ---

    public <T extends Throwable> byte getByte(Object o, long offset) throws T {
        return sunUnsafe.getByte(o, offset);
    }

    public <T extends Throwable> void putByte(Object o, long offset, byte x) throws T {
        sunUnsafe.putByte(o, offset, x);
    }

    public <T extends Throwable> byte getByteVolatile(Object o, long offset) throws T {
        return sunUnsafe.getByteVolatile(o, offset);
    }

    public <T extends Throwable> void putByteVolatile(Object o, long offset, byte x) throws T {
        sunUnsafe.putByteVolatile(o, offset, x);
    }

    public <T extends Throwable> byte getByteAcquire(Object o, long offset) throws T {
        byte v = sunUnsafe.getByte(o, offset);
        sunUnsafe.loadFence();
        return v;
    }

    public <T extends Throwable> void putByteRelease(Object o, long offset, byte x) throws T {
        sunUnsafe.storeFence();
        sunUnsafe.putByte(o, offset, x);
    }

    public <T extends Throwable> byte getByteOpaque(Object o, long offset) throws T {
        return getByteAcquire(o, offset);
    }

    public <T extends Throwable> void putByteOpaque(Object o, long offset, byte x) throws T {
        putByteRelease(o, offset, x);
    }

    public <T extends Throwable> boolean compareAndSetByte(Object o, long offset, byte expected, byte x) throws T {
        final long wordOffset = offset & ~3L;
        int byteIndex = (int) offset & 3;
        if (BIG_ENDIAN) {
            byteIndex ^= 3;
        }
        final int shift = byteIndex << 3;
        final int mask = 0xFF << shift;
        final int expBits = (expected & 0xFF) << shift;
        final int newBits = (x & 0xFF) << shift;
        while (true) {
            final int fullWord = sunUnsafe.getIntVolatile(o, wordOffset);
            final int currentBits = fullWord & mask;
            if (currentBits != expBits) {
                return false;
            }
            final int updatedWord = (fullWord & ~mask) | newBits;
            if (sunUnsafe.compareAndSwapInt(o, wordOffset, fullWord, updatedWord)) {
                return true;
            }
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetByte(Object o, long offset, byte expected, byte x) throws T {
        return compareAndSetByte(o, offset, expected, x);
    }

    public <T extends Throwable> boolean weakCompareAndSetByteAcquire(Object o, long offset, byte expected, byte x) throws T {
        return compareAndSetByte(o, offset, expected, x);
    }

    public <T extends Throwable> boolean weakCompareAndSetByteRelease(Object o, long offset, byte expected, byte x) throws T {
        return compareAndSetByte(o, offset, expected, x);
    }

    public <T extends Throwable> boolean weakCompareAndSetBytePlain(Object o, long offset, byte expected, byte x) throws T {
        return compareAndSetByte(o, offset, expected, x);
    }

    public <T extends Throwable> byte compareAndExchangeByte(Object o, long offset, byte expected, byte x) throws T {
        final long wordOffset = offset & ~3L;
        int byteIndex = (int) offset & 3;
        if (BIG_ENDIAN) {
            byteIndex ^= 3;
        }

        final int shift = byteIndex << 3;
        final int mask = 0xFF << shift;
        final int expBits = (expected & 0xFF) << shift;
        final int newBits = (x & 0xFF) << shift;

        while (true) {
            final int fullWord = sunUnsafe.getIntVolatile(o, wordOffset);
            final int currentBits = fullWord & mask;
            if (currentBits != expBits) {
                return (byte) (currentBits >>> shift);
            }
            final int updatedWord = (fullWord & ~mask) | newBits;
            if (sunUnsafe.compareAndSwapInt(o, wordOffset, fullWord, updatedWord)) {
                return expected;
            }
        }
    }

    public <T extends Throwable> byte compareAndExchangeByteAcquire(Object o, long offset, byte expected, byte x) throws T {
        return compareAndExchangeByte(o, offset, expected, x);
    }

    public <T extends Throwable> byte compareAndExchangeByteRelease(Object o, long offset, byte expected, byte x) throws T {
        return compareAndExchangeByte(o, offset, expected, x);
    }

    // --- 7. Short ---

    public <T extends Throwable> short getShort(Object o, long offset) throws T {
        return sunUnsafe.getShort(o, offset);
    }

    public <T extends Throwable> void putShort(Object o, long offset, short x) throws T {
        sunUnsafe.putShort(o, offset, x);
    }

    public <T extends Throwable> short getShortVolatile(Object o, long offset) throws T {
        return sunUnsafe.getShortVolatile(o, offset);
    }

    public <T extends Throwable> void putShortVolatile(Object o, long offset, short x) throws T {
        sunUnsafe.putShortVolatile(o, offset, x);
    }

    public <T extends Throwable> short getShortAcquire(Object o, long offset) throws T {
        short v = sunUnsafe.getShort(o, offset);
        sunUnsafe.loadFence();
        return v;
    }

    public <T extends Throwable> void putShortRelease(Object o, long offset, short x) throws T {
        sunUnsafe.storeFence();
        sunUnsafe.putShort(o, offset, x);
    }

    public <T extends Throwable> short getShortOpaque(Object o, long offset) throws T {
        return getShortAcquire(o, offset);
    }

    public <T extends Throwable> void putShortOpaque(Object o, long offset, short x) throws T {
        putShortRelease(o, offset, x);
    }

    public <T extends Throwable> boolean compareAndSetShort(Object o, long offset, short expected, short x) throws T {
        int byteIndex = (int) offset & 3;
        if (byteIndex == 3) {
            throw new IllegalArgumentException("short CAS crosses word boundary");
        }
        final long wordOffset = offset & ~3L;
        final int shift;
        if (BIG_ENDIAN) {
            shift = (2 - byteIndex) << 3;
        } else {
            shift = byteIndex << 3;
        }
        final int mask = 0xFFFF << shift;
        final int expBits = (expected & 0xFFFF) << shift;
        final int newBits = (x & 0xFFFF) << shift;
        while (true) {
            final int fullWord = sunUnsafe.getIntVolatile(o, wordOffset);
            final int currentBits = fullWord & mask;
            if (currentBits != expBits) {
                return false;
            }
            final int updatedWord = (fullWord & ~mask) | newBits;
            if (sunUnsafe.compareAndSwapInt(o, wordOffset, fullWord, updatedWord)) {
                return true;
            }
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetShort(Object o, long offset, short expected, short x) throws T {
        return compareAndSetShort(o, offset, expected, x);
    }

    public <T extends Throwable> boolean weakCompareAndSetShortAcquire(Object o, long offset, short expected, short x) throws T {
        return compareAndSetShort(o, offset, expected, x);
    }

    public <T extends Throwable> boolean weakCompareAndSetShortRelease(Object o, long offset, short expected, short x) throws T {
        return compareAndSetShort(o, offset, expected, x);
    }

    public <T extends Throwable> boolean weakCompareAndSetShortPlain(Object o, long offset, short expected, short x) throws T {
        return compareAndSetShort(o, offset, expected, x);
    }

    public <T extends Throwable> short compareAndExchangeShort(Object o, long offset, short expected, short x) throws T {
        int byteIndex = (int) offset & 3;
        if (byteIndex == 3) {
            throw new IllegalArgumentException("short CAS crosses word boundary");
        }
        final long wordOffset = offset & ~3L;
        final int shift;
        if (BIG_ENDIAN) {
            shift = (2 - byteIndex) << 3;
        } else {
            shift = byteIndex << 3;
        }

        final int mask = 0xFFFF << shift;
        final int expBits = (expected & 0xFFFF) << shift;
        final int newBits = (x & 0xFFFF) << shift;

        while (true) {
            final int fullWord = sunUnsafe.getIntVolatile(o, wordOffset);
            final int currentBits = fullWord & mask;
            if (currentBits != expBits) {
                return (short) (currentBits >>> shift);
            }

            final int updatedWord = (fullWord & ~mask) | newBits;

            if (sunUnsafe.compareAndSwapInt(o, wordOffset, fullWord, updatedWord)) {
                return expected;
            }
        }
    }

    public <T extends Throwable> short compareAndExchangeShortAcquire(Object o, long offset, short expected, short x) throws T {
        return compareAndExchangeShort(o, offset, expected, x);
    }

    public <T extends Throwable> short compareAndExchangeShortRelease(Object o, long offset, short expected, short x) throws T {
        return compareAndExchangeShort(o, offset, expected, x);
    }

    // --- 8. Char ---

    public <T extends Throwable> char getChar(Object o, long offset) throws T {
        return sunUnsafe.getChar(o, offset);
    }

    public <T extends Throwable> void putChar(Object o, long offset, char x) throws T {
        sunUnsafe.putChar(o, offset, x);
    }

    public <T extends Throwable> char getCharVolatile(Object o, long offset) throws T {
        return sunUnsafe.getCharVolatile(o, offset);
    }

    public <T extends Throwable> void putCharVolatile(Object o, long offset, char x) throws T {
        sunUnsafe.putCharVolatile(o, offset, x);
    }

    public <T extends Throwable> char getCharAcquire(Object o, long offset) throws T {
        char v = sunUnsafe.getChar(o, offset);
        sunUnsafe.loadFence();
        return v;
    }

    public <T extends Throwable> void putCharRelease(Object o, long offset, char x) throws T {
        sunUnsafe.storeFence();
        sunUnsafe.putChar(o, offset, x);
    }

    public <T extends Throwable> char getCharOpaque(Object o, long offset) throws T {
        return getCharAcquire(o, offset);
    }

    public <T extends Throwable> void putCharOpaque(Object o, long offset, char x) throws T {
        putCharRelease(o, offset, x);
    }

    public <T extends Throwable> boolean compareAndSetChar(Object o, long offset, char expected, char x) throws T {
        return compareAndSetShort(o, offset, (short) expected, (short) x);
    }

    public <T extends Throwable> boolean weakCompareAndSetChar(Object o, long offset, char expected, char x) throws T {
        return compareAndSetChar(o, offset, expected, x);
    }

    public <T extends Throwable> boolean weakCompareAndSetCharAcquire(Object o, long offset, char expected, char x) throws T {
        return compareAndSetChar(o, offset, expected, x);
    }

    public <T extends Throwable> boolean weakCompareAndSetCharRelease(Object o, long offset, char expected, char x) throws T {
        return compareAndSetChar(o, offset, expected, x);
    }

    public <T extends Throwable> boolean weakCompareAndSetCharPlain(Object o, long offset, char expected, char x) throws T {
        return compareAndSetChar(o, offset, expected, x);
    }

    public <T extends Throwable> char compareAndExchangeChar(Object o, long offset, char expected, char x) throws T {
        return (char) compareAndExchangeShort(o, offset, (short) expected, (short) x);
    }

    public <T extends Throwable> char compareAndExchangeCharAcquire(Object o, long offset, char expected, char x) throws T {
        return compareAndExchangeChar(o, offset, expected, x);
    }

    public <T extends Throwable> char compareAndExchangeCharRelease(Object o, long offset, char expected, char x) throws T {
        return compareAndExchangeChar(o, offset, expected, x);
    }

    // --- 9. Float ---

    public <T extends Throwable> float getFloat(Object o, long offset) throws T {
        return sunUnsafe.getFloat(o, offset);
    }

    public <T extends Throwable> void putFloat(Object o, long offset, float x) throws T {
        sunUnsafe.putFloat(o, offset, x);
    }

    public <T extends Throwable> float getFloatVolatile(Object o, long offset) throws T {
        return sunUnsafe.getFloatVolatile(o, offset);
    }

    public <T extends Throwable> void putFloatVolatile(Object o, long offset, float x) throws T {
        sunUnsafe.putFloatVolatile(o, offset, x);
    }

    public <T extends Throwable> float getFloatAcquire(Object o, long offset) throws T {
        float v = sunUnsafe.getFloat(o, offset);
        sunUnsafe.loadFence();
        return v;
    }

    public <T extends Throwable> void putFloatRelease(Object o, long offset, float x) throws T {
        sunUnsafe.putOrderedInt(o, offset, Float.floatToRawIntBits(x));
    }

    public <T extends Throwable> float getFloatOpaque(Object o, long offset) throws T {
        return getFloatAcquire(o, offset);
    }

    public <T extends Throwable> void putFloatOpaque(Object o, long offset, float x) throws T {
        putFloatRelease(o, offset, x);
    }

    public <T extends Throwable> boolean compareAndSetFloat(Object o, long offset, float expected, float x) throws T {
        return sunUnsafe.compareAndSwapInt(o, offset, Float.floatToRawIntBits(expected), Float.floatToRawIntBits(x));
    }

    public <T extends Throwable> boolean weakCompareAndSetFloat(Object o, long offset, float expected, float x) throws T {
        return compareAndSetFloat(o, offset, expected, x);
    }

    public <T extends Throwable> boolean weakCompareAndSetFloatAcquire(Object o, long offset, float expected, float x) throws T {
        return compareAndSetFloat(o, offset, expected, x);
    }

    public <T extends Throwable> boolean weakCompareAndSetFloatRelease(Object o, long offset, float expected, float x) throws T {
        return compareAndSetFloat(o, offset, expected, x);
    }

    public <T extends Throwable> boolean weakCompareAndSetFloatPlain(Object o, long offset, float expected, float x) throws T {
        return compareAndSetFloat(o, offset, expected, x);
    }

    public <T extends Throwable> float compareAndExchangeFloat(Object o, long offset, float expected, float x) throws T {
        return Float.intBitsToFloat(compareAndExchangeInt(o, offset, Float.floatToRawIntBits(expected), Float.floatToRawIntBits(x)));
    }

    public <T extends Throwable> float compareAndExchangeFloatAcquire(Object o, long offset, float expected, float x) throws T {
        return compareAndExchangeFloat(o, offset, expected, x);
    }

    public <T extends Throwable> float compareAndExchangeFloatRelease(Object o, long offset, float expected, float x) throws T {
        return compareAndExchangeFloat(o, offset, expected, x);
    }

    // --- 10. Double ---

    public <T extends Throwable> double getDouble(Object o, long offset) throws T {
        return sunUnsafe.getDouble(o, offset);
    }

    public <T extends Throwable> void putDouble(Object o, long offset, double x) throws T {
        sunUnsafe.putDouble(o, offset, x);
    }

    public <T extends Throwable> double getDoubleVolatile(Object o, long offset) throws T {
        return sunUnsafe.getDoubleVolatile(o, offset);
    }

    public <T extends Throwable> void putDoubleVolatile(Object o, long offset, double x) throws T {
        sunUnsafe.putDoubleVolatile(o, offset, x);
    }

    public <T extends Throwable> double getDoubleAcquire(Object o, long offset) throws T {
        double v = sunUnsafe.getDouble(o, offset);
        sunUnsafe.loadFence();
        return v;
    }

    public <T extends Throwable> void putDoubleRelease(Object o, long offset, double x) throws T {
        sunUnsafe.putOrderedLong(o, offset, Double.doubleToRawLongBits(x));
    }

    public <T extends Throwable> double getDoubleOpaque(Object o, long offset) throws T {
        return getDoubleAcquire(o, offset);
    }

    public <T extends Throwable> void putDoubleOpaque(Object o, long offset, double x) throws T {
        putDoubleRelease(o, offset, x);
    }

    public <T extends Throwable> boolean compareAndSetDouble(Object o, long offset, double expected, double x) throws T {
        return sunUnsafe.compareAndSwapLong(o, offset, Double.doubleToRawLongBits(expected), Double.doubleToRawLongBits(x));
    }

    public <T extends Throwable> boolean weakCompareAndSetDouble(Object o, long offset, double expected, double x) throws T {
        return compareAndSetDouble(o, offset, expected, x);
    }

    public <T extends Throwable> boolean weakCompareAndSetDoubleAcquire(Object o, long offset, double expected, double x) throws T {
        return compareAndSetDouble(o, offset, expected, x);
    }

    public <T extends Throwable> boolean weakCompareAndSetDoubleRelease(Object o, long offset, double expected, double x) throws T {
        return compareAndSetDouble(o, offset, expected, x);
    }

    public <T extends Throwable> boolean weakCompareAndSetDoublePlain(Object o, long offset, double expected, double x) throws T {
        return compareAndSetDouble(o, offset, expected, x);
    }

    public <T extends Throwable> double compareAndExchangeDouble(Object o, long offset, double expected, double x) throws T {
        return Double.longBitsToDouble(compareAndExchangeLong(o, offset, Double.doubleToRawLongBits(expected), Double.doubleToRawLongBits(x)));
    }

    public <T extends Throwable> double compareAndExchangeDoubleAcquire(Object o, long offset, double expected, double x) throws T {
        return compareAndExchangeDouble(o, offset, expected, x);
    }

    public <T extends Throwable> double compareAndExchangeDoubleRelease(Object o, long offset, double expected, double x) throws T {
        return compareAndExchangeDouble(o, offset, expected, x);
    }

    // --- 11. Memory ---

    public <T extends Throwable> long allocateMemory(long bytes) throws T {
        return sunUnsafe.allocateMemory(bytes);
    }

    public <T extends Throwable> void freeMemory(long address) throws T {
        sunUnsafe.freeMemory(address);
    }

    public <T extends Throwable> long reallocateMemory(long address, long bytes) throws T {
        return sunUnsafe.reallocateMemory(address, bytes);
    }

    public <T extends Throwable> void setMemory(long address, long bytes, byte value) throws T {
        sunUnsafe.setMemory(address, bytes, value);
    }

    public <T extends Throwable> void copyMemory(Object srcBase, long srcOffset, Object destBase, long destOffset, long bytes) throws T {
        sunUnsafe.copyMemory(srcBase, srcOffset, destBase, destOffset, bytes);
    }

    public <T extends Throwable> long getLong(long address) throws T {
        return sunUnsafe.getLong(address);
    }

    public <T extends Throwable> void putLong(long address, long value) throws T {
        sunUnsafe.putLong(address, value);
    }

    public <T extends Throwable> void loadFence() throws T {
        sunUnsafe.loadFence();
    }

    public <T extends Throwable> void storeFence() throws T {
        sunUnsafe.storeFence();
    }

    public <T extends Throwable> void fullFence() throws T {
        sunUnsafe.fullFence();
    }

    public <T extends Throwable> void park(boolean isAbsolute, long time) throws T {
        sunUnsafe.park(isAbsolute, time);
    }

    public <T extends Throwable> void unpark(Object thread) throws T {
        sunUnsafe.unpark(thread);
    }
}
