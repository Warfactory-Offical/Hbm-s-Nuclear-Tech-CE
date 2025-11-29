package com.hbm.lib.internal;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.ByteOrder;

/**
 * Switch between different versions of Unsafe automatically depending on the Java version.
 * Mirrors the API of jdk.internal.misc.Unsafe while supporting JDK 8 and onwards.
 *
 * @author mlbv
 */
@SuppressWarnings({"MethodMayBeStatic", "unchecked"})
public final class UnsafeWrapper {
    private static final UnsafeWrapper theUnsafe = new UnsafeWrapper();
    private static final boolean BIG_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;

    private static final MethodHandle OBJECT_FIELD_OFFSET, STATIC_FIELD_BASE, STATIC_FIELD_OFFSET, ALLOCATE_INSTANCE, ARRAY_BASE_OFFSET, ARRAY_INDEX_SCALE, ADDRESS_SIZE, PAGE_SIZE;

    private static final MethodHandle GET_REFERENCE, PUT_REFERENCE, GET_REFERENCE_VOLATILE, PUT_REFERENCE_VOLATILE, GET_REFERENCE_ACQUIRE, PUT_REFERENCE_RELEASE, GET_REFERENCE_OPAQUE, PUT_REFERENCE_OPAQUE, COMPARE_AND_SET_REFERENCE, GET_AND_SET_REFERENCE;
    private static final MethodHandle WEAK_COMPARE_AND_SET_REFERENCE, WEAK_COMPARE_AND_SET_REFERENCE_ACQUIRE, WEAK_COMPARE_AND_SET_REFERENCE_RELEASE, WEAK_COMPARE_AND_SET_REFERENCE_PLAIN;
    private static final MethodHandle COMPARE_AND_EXCHANGE_REFERENCE, COMPARE_AND_EXCHANGE_REFERENCE_ACQUIRE, COMPARE_AND_EXCHANGE_REFERENCE_RELEASE, GET_AND_SET_REFERENCE_ACQUIRE, GET_AND_SET_REFERENCE_RELEASE;

    private static final MethodHandle GET_INT, PUT_INT, GET_INT_VOLATILE, PUT_INT_VOLATILE, GET_INT_ACQUIRE, PUT_INT_RELEASE, GET_INT_OPAQUE, PUT_INT_OPAQUE, GET_AND_ADD_INT, GET_AND_SET_INT, COMPARE_AND_SET_INT;
    private static final MethodHandle WEAK_COMPARE_AND_SET_INT, WEAK_COMPARE_AND_SET_INT_ACQUIRE, WEAK_COMPARE_AND_SET_INT_RELEASE, WEAK_COMPARE_AND_SET_INT_PLAIN;
    private static final MethodHandle COMPARE_AND_EXCHANGE_INT, COMPARE_AND_EXCHANGE_INT_ACQUIRE, COMPARE_AND_EXCHANGE_INT_RELEASE, GET_AND_SET_INT_ACQUIRE, GET_AND_SET_INT_RELEASE;

    private static final MethodHandle GET_LONG, PUT_LONG, GET_LONG_VOLATILE, PUT_LONG_VOLATILE, GET_LONG_ACQUIRE, PUT_LONG_RELEASE, GET_LONG_OPAQUE, PUT_LONG_OPAQUE, GET_AND_ADD_LONG, GET_AND_SET_LONG, COMPARE_AND_SET_LONG;
    private static final MethodHandle WEAK_COMPARE_AND_SET_LONG, WEAK_COMPARE_AND_SET_LONG_ACQUIRE, WEAK_COMPARE_AND_SET_LONG_RELEASE, WEAK_COMPARE_AND_SET_LONG_PLAIN;
    private static final MethodHandle COMPARE_AND_EXCHANGE_LONG, COMPARE_AND_EXCHANGE_LONG_ACQUIRE, COMPARE_AND_EXCHANGE_LONG_RELEASE, GET_AND_SET_LONG_ACQUIRE, GET_AND_SET_LONG_RELEASE;

    private static final MethodHandle GET_BOOLEAN, PUT_BOOLEAN, GET_BOOLEAN_VOLATILE, PUT_BOOLEAN_VOLATILE, GET_BOOLEAN_ACQUIRE, PUT_BOOLEAN_RELEASE, GET_BOOLEAN_OPAQUE, PUT_BOOLEAN_OPAQUE, COMPARE_AND_SET_BOOLEAN;
    private static final MethodHandle WEAK_COMPARE_AND_SET_BOOLEAN, WEAK_COMPARE_AND_SET_BOOLEAN_ACQUIRE, WEAK_COMPARE_AND_SET_BOOLEAN_RELEASE, WEAK_COMPARE_AND_SET_BOOLEAN_PLAIN;
    private static final MethodHandle COMPARE_AND_EXCHANGE_BOOLEAN, COMPARE_AND_EXCHANGE_BOOLEAN_ACQUIRE, COMPARE_AND_EXCHANGE_BOOLEAN_RELEASE;

    private static final MethodHandle GET_BYTE, PUT_BYTE, GET_BYTE_VOLATILE, PUT_BYTE_VOLATILE, GET_BYTE_ACQUIRE, PUT_BYTE_RELEASE, GET_BYTE_OPAQUE, PUT_BYTE_OPAQUE, COMPARE_AND_SET_BYTE;
    private static final MethodHandle WEAK_COMPARE_AND_SET_BYTE, WEAK_COMPARE_AND_SET_BYTE_ACQUIRE, WEAK_COMPARE_AND_SET_BYTE_RELEASE, WEAK_COMPARE_AND_SET_BYTE_PLAIN;
    private static final MethodHandle COMPARE_AND_EXCHANGE_BYTE, COMPARE_AND_EXCHANGE_BYTE_ACQUIRE, COMPARE_AND_EXCHANGE_BYTE_RELEASE;

    private static final MethodHandle GET_SHORT, PUT_SHORT, GET_SHORT_VOLATILE, PUT_SHORT_VOLATILE, GET_SHORT_ACQUIRE, PUT_SHORT_RELEASE, GET_SHORT_OPAQUE, PUT_SHORT_OPAQUE, COMPARE_AND_SET_SHORT;
    private static final MethodHandle WEAK_COMPARE_AND_SET_SHORT, WEAK_COMPARE_AND_SET_SHORT_ACQUIRE, WEAK_COMPARE_AND_SET_SHORT_RELEASE, WEAK_COMPARE_AND_SET_SHORT_PLAIN;
    private static final MethodHandle COMPARE_AND_EXCHANGE_SHORT, COMPARE_AND_EXCHANGE_SHORT_ACQUIRE, COMPARE_AND_EXCHANGE_SHORT_RELEASE;

    private static final MethodHandle GET_CHAR, PUT_CHAR, GET_CHAR_VOLATILE, PUT_CHAR_VOLATILE, GET_CHAR_ACQUIRE, PUT_CHAR_RELEASE, GET_CHAR_OPAQUE, PUT_CHAR_OPAQUE, COMPARE_AND_SET_CHAR;
    private static final MethodHandle WEAK_COMPARE_AND_SET_CHAR, WEAK_COMPARE_AND_SET_CHAR_ACQUIRE, WEAK_COMPARE_AND_SET_CHAR_RELEASE, WEAK_COMPARE_AND_SET_CHAR_PLAIN;
    private static final MethodHandle COMPARE_AND_EXCHANGE_CHAR, COMPARE_AND_EXCHANGE_CHAR_ACQUIRE, COMPARE_AND_EXCHANGE_CHAR_RELEASE;

    private static final MethodHandle GET_FLOAT, PUT_FLOAT, GET_FLOAT_VOLATILE, PUT_FLOAT_VOLATILE, GET_FLOAT_ACQUIRE, PUT_FLOAT_RELEASE, GET_FLOAT_OPAQUE, PUT_FLOAT_OPAQUE, COMPARE_AND_SET_FLOAT;
    private static final MethodHandle WEAK_COMPARE_AND_SET_FLOAT, WEAK_COMPARE_AND_SET_FLOAT_ACQUIRE, WEAK_COMPARE_AND_SET_FLOAT_RELEASE, WEAK_COMPARE_AND_SET_FLOAT_PLAIN;
    private static final MethodHandle COMPARE_AND_EXCHANGE_FLOAT, COMPARE_AND_EXCHANGE_FLOAT_ACQUIRE, COMPARE_AND_EXCHANGE_FLOAT_RELEASE;

    private static final MethodHandle GET_DOUBLE, PUT_DOUBLE, GET_DOUBLE_VOLATILE, PUT_DOUBLE_VOLATILE, GET_DOUBLE_ACQUIRE, PUT_DOUBLE_RELEASE, GET_DOUBLE_OPAQUE, PUT_DOUBLE_OPAQUE, COMPARE_AND_SET_DOUBLE;
    private static final MethodHandle WEAK_COMPARE_AND_SET_DOUBLE, WEAK_COMPARE_AND_SET_DOUBLE_ACQUIRE, WEAK_COMPARE_AND_SET_DOUBLE_RELEASE, WEAK_COMPARE_AND_SET_DOUBLE_PLAIN;
    private static final MethodHandle COMPARE_AND_EXCHANGE_DOUBLE, COMPARE_AND_EXCHANGE_DOUBLE_ACQUIRE, COMPARE_AND_EXCHANGE_DOUBLE_RELEASE;

    private static final MethodHandle ALLOCATE_MEMORY, FREE_MEMORY, REALLOCATE_MEMORY, SET_MEMORY, COPY_MEMORY, GET_LONG_ADDRESS, PUT_LONG_ADDRESS, ALLOCATE_UNINITIALIZED_ARRAY;
    private static final MethodHandle LOAD_FENCE, STORE_FENCE, FULL_FENCE, PARK, UNPARK;

    static final MethodHandles.Lookup IMPL_LOOKUP;
    static final int MAJOR_VERSION;
    static final boolean JPMS;

    static {
        try {
            MAJOR_VERSION = Runtime.version().feature(); // JVMDG downgrade
            // JDK-8159995 did the internal Unsafe compare* renaming in JDK 10 and JDK-8181292 backported them to JDK 9.
            // I doubt if anyone would use java 9 to run the mod though...
            // Specifically:
            // - compareAndExchange*Volatile to compareAndExchange*
            // - compareAndSwap* to compareAndSet*
            // - weakCompareAndSwap*Volatile -> weakCompareAndSwap
            // - weakCompareAndSwap* -> weakCompareAndSwap*Plain
            JPMS = MAJOR_VERSION >= 9;
            boolean JDK_8207146 = MAJOR_VERSION >= 12; // Rename xxxObject -> xxxReference
            boolean JDK_8344168 = MAJOR_VERSION >= 25; // arrayBaseOffset returns long

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

            UnsafeBinder binder = new UnsafeBinder(IMPL_LOOKUP, unsafeClass, unsafeInstance);

            // --- 1. Offsets & Basics ---
            OBJECT_FIELD_OFFSET = binder.bind("objectFieldOffset", long.class, Field.class);
            STATIC_FIELD_BASE = binder.bind("staticFieldBase", Object.class, Field.class);
            STATIC_FIELD_OFFSET = binder.bind("staticFieldOffset", long.class, Field.class);
            ALLOCATE_INSTANCE = binder.bind("allocateInstance", Object.class, Class.class);
            if (JPMS) {
                ALLOCATE_UNINITIALIZED_ARRAY = binder.bind("allocateUninitializedArray", Object.class, Class.class, int.class);
            } else {
                ALLOCATE_UNINITIALIZED_ARRAY = IMPL_LOOKUP.findStatic(Array.class, "newInstance", MethodType.methodType(Object.class, Class.class, int.class));
            }

            MethodHandle arrayBase = binder.bind("arrayBaseOffset", JDK_8344168 ? long.class : int.class, Class.class);
            if (!JDK_8344168) {
                arrayBase = MethodHandles.explicitCastArguments(arrayBase, MethodType.methodType(long.class, Class.class));
            }
            ARRAY_BASE_OFFSET = arrayBase;
            ARRAY_INDEX_SCALE = binder.bind("arrayIndexScale", int.class, Class.class);
            ADDRESS_SIZE = binder.bind("addressSize", int.class);
            PAGE_SIZE = binder.bind("pageSize", int.class);

            // --- 2. References ---
            String getRefName = JDK_8207146 ? "getReference" : "getObject";
            String putRefName = JDK_8207146 ? "putReference" : "putObject";
            String getRefVolName = JDK_8207146 ? "getReferenceVolatile" : "getObjectVolatile";
            String putRefVolName = JDK_8207146 ? "putReferenceVolatile" : "putObjectVolatile";
            String casRefName = JDK_8207146 ? "compareAndSetReference" : "compareAndSwapObject";
            String getAndSetRefName = JDK_8207146 ? "getAndSetReference" : "getAndSetObject";

            GET_REFERENCE = binder.bind(getRefName, Object.class, Object.class, long.class);
            PUT_REFERENCE = binder.bind(putRefName, void.class, Object.class, long.class, Object.class);
            GET_REFERENCE_VOLATILE = binder.bind(getRefVolName, Object.class, Object.class, long.class);
            PUT_REFERENCE_VOLATILE = binder.bind(putRefVolName, void.class, Object.class, long.class, Object.class);
            COMPARE_AND_SET_REFERENCE = binder.bind(casRefName, boolean.class, Object.class, long.class, Object.class, Object.class);
            GET_AND_SET_REFERENCE = binder.bind(getAndSetRefName, Object.class, Object.class, long.class, Object.class);

            if (JPMS) {
                String getRefAcqName = JDK_8207146 ? "getReferenceAcquire" : "getObjectAcquire";
                String putRefRelName = JDK_8207146 ? "putReferenceRelease" : "putObjectRelease";
                String getRefOpaName = JDK_8207146 ? "getReferenceOpaque" : "getObjectOpaque";
                String putRefOpaName = JDK_8207146 ? "putReferenceOpaque" : "putObjectOpaque";

                String weakRefBaseName = JDK_8207146 ? "weakCompareAndSetReference" : "weakCompareAndSetObject";
                String weakRefAcqName = weakRefBaseName + "Acquire";
                String weakRefRelName = weakRefBaseName + "Release";
                String weakRefPlainName = weakRefBaseName + "Plain";

                String caeRefBaseName = JDK_8207146 ? "compareAndExchangeReference" : "compareAndExchangeObject";
                String caeRefAcqName = caeRefBaseName + "Acquire";
                String caeRefRelName = caeRefBaseName + "Release";

                String getAndSetRefAcqName = JDK_8207146 ? "getAndSetReferenceAcquire" : "getAndSetObjectAcquire";
                String getAndSetRefRelName = JDK_8207146 ? "getAndSetReferenceRelease" : "getAndSetObjectRelease";

                GET_REFERENCE_ACQUIRE = binder.bind(getRefAcqName, Object.class, Object.class, long.class);
                PUT_REFERENCE_RELEASE = binder.bind(putRefRelName, void.class, Object.class, long.class, Object.class);
                GET_REFERENCE_OPAQUE = binder.bind(getRefOpaName, Object.class, Object.class, long.class);
                PUT_REFERENCE_OPAQUE = binder.bind(putRefOpaName, void.class, Object.class, long.class, Object.class);

                WEAK_COMPARE_AND_SET_REFERENCE = binder.bind(weakRefBaseName, boolean.class, Object.class, long.class, Object.class, Object.class);
                WEAK_COMPARE_AND_SET_REFERENCE_ACQUIRE = binder.bind(weakRefAcqName, boolean.class, Object.class, long.class, Object.class, Object.class);
                WEAK_COMPARE_AND_SET_REFERENCE_RELEASE = binder.bind(weakRefRelName, boolean.class, Object.class, long.class, Object.class, Object.class);
                WEAK_COMPARE_AND_SET_REFERENCE_PLAIN = binder.bind(weakRefPlainName, boolean.class, Object.class, long.class, Object.class, Object.class);

                COMPARE_AND_EXCHANGE_REFERENCE = binder.bind(caeRefBaseName, Object.class, Object.class, long.class, Object.class, Object.class);
                COMPARE_AND_EXCHANGE_REFERENCE_ACQUIRE = binder.bind(caeRefAcqName, Object.class, Object.class, long.class, Object.class, Object.class);
                COMPARE_AND_EXCHANGE_REFERENCE_RELEASE = binder.bind(caeRefRelName, Object.class, Object.class, long.class, Object.class, Object.class);

                GET_AND_SET_REFERENCE_ACQUIRE = binder.bind(getAndSetRefAcqName, Object.class, Object.class, long.class, Object.class);
                GET_AND_SET_REFERENCE_RELEASE = binder.bind(getAndSetRefRelName, Object.class, Object.class, long.class, Object.class);
            } else {
                GET_REFERENCE_ACQUIRE = GET_REFERENCE_VOLATILE;
                PUT_REFERENCE_RELEASE = binder.bind("putOrderedObject", void.class, Object.class, long.class, Object.class);
                GET_REFERENCE_OPAQUE = GET_REFERENCE_VOLATILE;
                PUT_REFERENCE_OPAQUE = PUT_REFERENCE_VOLATILE;

                WEAK_COMPARE_AND_SET_REFERENCE = COMPARE_AND_SET_REFERENCE;
                WEAK_COMPARE_AND_SET_REFERENCE_ACQUIRE = COMPARE_AND_SET_REFERENCE;
                WEAK_COMPARE_AND_SET_REFERENCE_RELEASE = COMPARE_AND_SET_REFERENCE;
                WEAK_COMPARE_AND_SET_REFERENCE_PLAIN = COMPARE_AND_SET_REFERENCE;

                COMPARE_AND_EXCHANGE_REFERENCE = binder.bindCompat("compatCompareAndExchangeReference", Object.class, Object.class, long.class, Object.class, Object.class);
                COMPARE_AND_EXCHANGE_REFERENCE_ACQUIRE = COMPARE_AND_EXCHANGE_REFERENCE;
                COMPARE_AND_EXCHANGE_REFERENCE_RELEASE = COMPARE_AND_EXCHANGE_REFERENCE;

                GET_AND_SET_REFERENCE_ACQUIRE = GET_AND_SET_REFERENCE;
                GET_AND_SET_REFERENCE_RELEASE = GET_AND_SET_REFERENCE;
            }

            // --- 3. Int ---
            GET_INT = binder.bind("getInt", int.class, Object.class, long.class);
            PUT_INT = binder.bind("putInt", void.class, Object.class, long.class, int.class);
            GET_INT_VOLATILE = binder.bind("getIntVolatile", int.class, Object.class, long.class);
            PUT_INT_VOLATILE = binder.bind("putIntVolatile", void.class, Object.class, long.class, int.class);
            String casIntName = JPMS ? "compareAndSetInt" : "compareAndSwapInt";
            COMPARE_AND_SET_INT = binder.bind(casIntName, boolean.class, Object.class, long.class, int.class, int.class);
            GET_AND_ADD_INT = binder.bind("getAndAddInt", int.class, Object.class, long.class, int.class);
            GET_AND_SET_INT = binder.bind("getAndSetInt", int.class, Object.class, long.class, int.class);

            if (JPMS) {
                GET_INT_ACQUIRE = binder.bind("getIntAcquire", int.class, Object.class, long.class);
                PUT_INT_RELEASE = binder.bind("putIntRelease", void.class, Object.class, long.class, int.class);
                GET_INT_OPAQUE = binder.bind("getIntOpaque", int.class, Object.class, long.class);
                PUT_INT_OPAQUE = binder.bind("putIntOpaque", void.class, Object.class, long.class, int.class);

                WEAK_COMPARE_AND_SET_INT = binder.bind("weakCompareAndSetInt", boolean.class, Object.class, long.class, int.class, int.class);
                WEAK_COMPARE_AND_SET_INT_ACQUIRE = binder.bind("weakCompareAndSetIntAcquire", boolean.class, Object.class, long.class, int.class, int.class);
                WEAK_COMPARE_AND_SET_INT_RELEASE = binder.bind("weakCompareAndSetIntRelease", boolean.class, Object.class, long.class, int.class, int.class);
                WEAK_COMPARE_AND_SET_INT_PLAIN = binder.bind("weakCompareAndSetIntPlain", boolean.class, Object.class, long.class, int.class, int.class);

                COMPARE_AND_EXCHANGE_INT = binder.bind("compareAndExchangeInt", int.class, Object.class, long.class, int.class, int.class);
                COMPARE_AND_EXCHANGE_INT_ACQUIRE = binder.bind("compareAndExchangeIntAcquire", int.class, Object.class, long.class, int.class, int.class);
                COMPARE_AND_EXCHANGE_INT_RELEASE = binder.bind("compareAndExchangeIntRelease", int.class, Object.class, long.class, int.class, int.class);

                GET_AND_SET_INT_ACQUIRE = binder.bind("getAndSetIntAcquire", int.class, Object.class, long.class, int.class);
                GET_AND_SET_INT_RELEASE = binder.bind("getAndSetIntRelease", int.class, Object.class, long.class, int.class);
            } else {
                GET_INT_ACQUIRE = GET_INT_VOLATILE;
                PUT_INT_RELEASE = binder.bind("putOrderedInt", void.class, Object.class, long.class, int.class);
                GET_INT_OPAQUE = GET_INT_VOLATILE;
                PUT_INT_OPAQUE = PUT_INT_VOLATILE;

                WEAK_COMPARE_AND_SET_INT = COMPARE_AND_SET_INT;
                WEAK_COMPARE_AND_SET_INT_ACQUIRE = COMPARE_AND_SET_INT;
                WEAK_COMPARE_AND_SET_INT_RELEASE = COMPARE_AND_SET_INT;
                WEAK_COMPARE_AND_SET_INT_PLAIN = COMPARE_AND_SET_INT;

                COMPARE_AND_EXCHANGE_INT = binder.bindCompat("compatCompareAndExchangeInt", int.class, Object.class, long.class, int.class, int.class);
                COMPARE_AND_EXCHANGE_INT_ACQUIRE = COMPARE_AND_EXCHANGE_INT;
                COMPARE_AND_EXCHANGE_INT_RELEASE = COMPARE_AND_EXCHANGE_INT;

                GET_AND_SET_INT_ACQUIRE = GET_AND_SET_INT;
                GET_AND_SET_INT_RELEASE = GET_AND_SET_INT;
            }

            // --- 4. Long ---
            GET_LONG = binder.bind("getLong", long.class, Object.class, long.class);
            PUT_LONG = binder.bind("putLong", void.class, Object.class, long.class, long.class);
            GET_LONG_VOLATILE = binder.bind("getLongVolatile", long.class, Object.class, long.class);
            PUT_LONG_VOLATILE = binder.bind("putLongVolatile", void.class, Object.class, long.class, long.class);
            String casLongName = JPMS ? "compareAndSetLong" : "compareAndSwapLong";
            COMPARE_AND_SET_LONG = binder.bind(casLongName, boolean.class, Object.class, long.class, long.class, long.class);
            GET_AND_ADD_LONG = binder.bind("getAndAddLong", long.class, Object.class, long.class, long.class);
            GET_AND_SET_LONG = binder.bind("getAndSetLong", long.class, Object.class, long.class, long.class);

            if (JPMS) {
                GET_LONG_ACQUIRE = binder.bind("getLongAcquire", long.class, Object.class, long.class);
                PUT_LONG_RELEASE = binder.bind("putLongRelease", void.class, Object.class, long.class, long.class);
                GET_LONG_OPAQUE = binder.bind("getLongOpaque", long.class, Object.class, long.class);
                PUT_LONG_OPAQUE = binder.bind("putLongOpaque", void.class, Object.class, long.class, long.class);

                WEAK_COMPARE_AND_SET_LONG = binder.bind("weakCompareAndSetLong", boolean.class, Object.class, long.class, long.class, long.class);
                WEAK_COMPARE_AND_SET_LONG_ACQUIRE = binder.bind("weakCompareAndSetLongAcquire", boolean.class, Object.class, long.class, long.class, long.class);
                WEAK_COMPARE_AND_SET_LONG_RELEASE = binder.bind("weakCompareAndSetLongRelease", boolean.class, Object.class, long.class, long.class, long.class);
                WEAK_COMPARE_AND_SET_LONG_PLAIN = binder.bind("weakCompareAndSetLongPlain", boolean.class, Object.class, long.class, long.class, long.class);

                COMPARE_AND_EXCHANGE_LONG = binder.bind("compareAndExchangeLong", long.class, Object.class, long.class, long.class, long.class);
                COMPARE_AND_EXCHANGE_LONG_ACQUIRE = binder.bind("compareAndExchangeLongAcquire", long.class, Object.class, long.class, long.class, long.class);
                COMPARE_AND_EXCHANGE_LONG_RELEASE = binder.bind("compareAndExchangeLongRelease", long.class, Object.class, long.class, long.class, long.class);

                GET_AND_SET_LONG_ACQUIRE = binder.bind("getAndSetLongAcquire", long.class, Object.class, long.class, long.class);
                GET_AND_SET_LONG_RELEASE = binder.bind("getAndSetLongRelease", long.class, Object.class, long.class, long.class);
            } else {
                GET_LONG_ACQUIRE = GET_LONG_VOLATILE;
                PUT_LONG_RELEASE = binder.bind("putOrderedLong", void.class, Object.class, long.class, long.class);
                GET_LONG_OPAQUE = GET_LONG_VOLATILE;
                PUT_LONG_OPAQUE = PUT_LONG_VOLATILE;

                WEAK_COMPARE_AND_SET_LONG = COMPARE_AND_SET_LONG;
                WEAK_COMPARE_AND_SET_LONG_ACQUIRE = COMPARE_AND_SET_LONG;
                WEAK_COMPARE_AND_SET_LONG_RELEASE = COMPARE_AND_SET_LONG;
                WEAK_COMPARE_AND_SET_LONG_PLAIN = COMPARE_AND_SET_LONG;

                COMPARE_AND_EXCHANGE_LONG = binder.bindCompat("compatCompareAndExchangeLong", long.class, Object.class, long.class, long.class, long.class);
                COMPARE_AND_EXCHANGE_LONG_ACQUIRE = COMPARE_AND_EXCHANGE_LONG;
                COMPARE_AND_EXCHANGE_LONG_RELEASE = COMPARE_AND_EXCHANGE_LONG;

                GET_AND_SET_LONG_ACQUIRE = GET_AND_SET_LONG;
                GET_AND_SET_LONG_RELEASE = GET_AND_SET_LONG;
            }

            // --- 5. Boolean ---
            GET_BOOLEAN = binder.bind("getBoolean", boolean.class, Object.class, long.class);
            PUT_BOOLEAN = binder.bind("putBoolean", void.class, Object.class, long.class, boolean.class);
            GET_BOOLEAN_VOLATILE = binder.bind("getBooleanVolatile", boolean.class, Object.class, long.class);
            PUT_BOOLEAN_VOLATILE = binder.bind("putBooleanVolatile", void.class, Object.class, long.class, boolean.class);

            if (JPMS) {
                GET_BOOLEAN_ACQUIRE = binder.bind("getBooleanAcquire", boolean.class, Object.class, long.class);
                PUT_BOOLEAN_RELEASE = binder.bind("putBooleanRelease", void.class, Object.class, long.class, boolean.class);
                GET_BOOLEAN_OPAQUE = binder.bind("getBooleanOpaque", boolean.class, Object.class, long.class);
                PUT_BOOLEAN_OPAQUE = binder.bind("putBooleanOpaque", void.class, Object.class, long.class, boolean.class);
                COMPARE_AND_SET_BOOLEAN = binder.bind("compareAndSetBoolean", boolean.class, Object.class, long.class, boolean.class, boolean.class);

                WEAK_COMPARE_AND_SET_BOOLEAN = binder.bind("weakCompareAndSetBoolean", boolean.class, Object.class, long.class, boolean.class, boolean.class);
                WEAK_COMPARE_AND_SET_BOOLEAN_ACQUIRE = binder.bind("weakCompareAndSetBooleanAcquire", boolean.class, Object.class, long.class, boolean.class, boolean.class);
                WEAK_COMPARE_AND_SET_BOOLEAN_RELEASE = binder.bind("weakCompareAndSetBooleanRelease", boolean.class, Object.class, long.class, boolean.class, boolean.class);
                WEAK_COMPARE_AND_SET_BOOLEAN_PLAIN = binder.bind("weakCompareAndSetBooleanPlain", boolean.class, Object.class, long.class, boolean.class, boolean.class);

                COMPARE_AND_EXCHANGE_BOOLEAN = binder.bind("compareAndExchangeBoolean", boolean.class, Object.class, long.class, boolean.class, boolean.class);
                COMPARE_AND_EXCHANGE_BOOLEAN_ACQUIRE = binder.bind("compareAndExchangeBooleanAcquire", boolean.class, Object.class, long.class, boolean.class, boolean.class);
                COMPARE_AND_EXCHANGE_BOOLEAN_RELEASE = binder.bind("compareAndExchangeBooleanRelease", boolean.class, Object.class, long.class, boolean.class, boolean.class);
            } else {
                GET_BOOLEAN_ACQUIRE = GET_BOOLEAN_VOLATILE;
                PUT_BOOLEAN_RELEASE = binder.bindCompat("compatPutBooleanRelease", void.class, Object.class, long.class, boolean.class);
                GET_BOOLEAN_OPAQUE = GET_BOOLEAN_VOLATILE;
                PUT_BOOLEAN_OPAQUE = PUT_BOOLEAN_VOLATILE;
                COMPARE_AND_SET_BOOLEAN = binder.bindCompat("compatCompareAndSetBoolean", boolean.class, Object.class, long.class, boolean.class, boolean.class);

                WEAK_COMPARE_AND_SET_BOOLEAN = COMPARE_AND_SET_BOOLEAN;
                WEAK_COMPARE_AND_SET_BOOLEAN_ACQUIRE = COMPARE_AND_SET_BOOLEAN;
                WEAK_COMPARE_AND_SET_BOOLEAN_RELEASE = COMPARE_AND_SET_BOOLEAN;
                WEAK_COMPARE_AND_SET_BOOLEAN_PLAIN = COMPARE_AND_SET_BOOLEAN;

                COMPARE_AND_EXCHANGE_BOOLEAN = binder.bindCompat("compatCompareAndExchangeBoolean", boolean.class, Object.class, long.class, boolean.class, boolean.class);
                COMPARE_AND_EXCHANGE_BOOLEAN_ACQUIRE = COMPARE_AND_EXCHANGE_BOOLEAN;
                COMPARE_AND_EXCHANGE_BOOLEAN_RELEASE = COMPARE_AND_EXCHANGE_BOOLEAN;
            }

            // --- 6. Byte ---
            GET_BYTE = binder.bind("getByte", byte.class, Object.class, long.class);
            PUT_BYTE = binder.bind("putByte", void.class, Object.class, long.class, byte.class);
            GET_BYTE_VOLATILE = binder.bind("getByteVolatile", byte.class, Object.class, long.class);
            PUT_BYTE_VOLATILE = binder.bind("putByteVolatile", void.class, Object.class, long.class, byte.class);

            if (JPMS) {
                GET_BYTE_ACQUIRE = binder.bind("getByteAcquire", byte.class, Object.class, long.class);
                PUT_BYTE_RELEASE = binder.bind("putByteRelease", void.class, Object.class, long.class, byte.class);
                GET_BYTE_OPAQUE = binder.bind("getByteOpaque", byte.class, Object.class, long.class);
                PUT_BYTE_OPAQUE = binder.bind("putByteOpaque", void.class, Object.class, long.class, byte.class);
                COMPARE_AND_SET_BYTE = binder.bind("compareAndSetByte", boolean.class, Object.class, long.class, byte.class, byte.class);

                WEAK_COMPARE_AND_SET_BYTE = binder.bind("weakCompareAndSetByte", boolean.class, Object.class, long.class, byte.class, byte.class);
                WEAK_COMPARE_AND_SET_BYTE_ACQUIRE = binder.bind("weakCompareAndSetByteAcquire", boolean.class, Object.class, long.class, byte.class, byte.class);
                WEAK_COMPARE_AND_SET_BYTE_RELEASE = binder.bind("weakCompareAndSetByteRelease", boolean.class, Object.class, long.class, byte.class, byte.class);
                WEAK_COMPARE_AND_SET_BYTE_PLAIN = binder.bind("weakCompareAndSetBytePlain", boolean.class, Object.class, long.class, byte.class, byte.class);

                COMPARE_AND_EXCHANGE_BYTE = binder.bind("compareAndExchangeByte", byte.class, Object.class, long.class, byte.class, byte.class);
                COMPARE_AND_EXCHANGE_BYTE_ACQUIRE = binder.bind("compareAndExchangeByteAcquire", byte.class, Object.class, long.class, byte.class, byte.class);
                COMPARE_AND_EXCHANGE_BYTE_RELEASE = binder.bind("compareAndExchangeByteRelease", byte.class, Object.class, long.class, byte.class, byte.class);
            } else {
                GET_BYTE_ACQUIRE = GET_BYTE_VOLATILE;
                PUT_BYTE_RELEASE = binder.bindCompat("compatPutByteRelease", void.class, Object.class, long.class, byte.class);
                GET_BYTE_OPAQUE = GET_BYTE_VOLATILE;
                PUT_BYTE_OPAQUE = PUT_BYTE_VOLATILE;
                COMPARE_AND_SET_BYTE = binder.bindCompat("compatCompareAndSetByte", boolean.class, Object.class, long.class, byte.class, byte.class);

                WEAK_COMPARE_AND_SET_BYTE = COMPARE_AND_SET_BYTE;
                WEAK_COMPARE_AND_SET_BYTE_ACQUIRE = COMPARE_AND_SET_BYTE;
                WEAK_COMPARE_AND_SET_BYTE_RELEASE = COMPARE_AND_SET_BYTE;
                WEAK_COMPARE_AND_SET_BYTE_PLAIN = COMPARE_AND_SET_BYTE;

                COMPARE_AND_EXCHANGE_BYTE = binder.bindCompat("compatCompareAndExchangeByte", byte.class, Object.class, long.class, byte.class, byte.class);
                COMPARE_AND_EXCHANGE_BYTE_ACQUIRE = COMPARE_AND_EXCHANGE_BYTE;
                COMPARE_AND_EXCHANGE_BYTE_RELEASE = COMPARE_AND_EXCHANGE_BYTE;
            }

            // --- 7. Short ---
            GET_SHORT = binder.bind("getShort", short.class, Object.class, long.class);
            PUT_SHORT = binder.bind("putShort", void.class, Object.class, long.class, short.class);
            GET_SHORT_VOLATILE = binder.bind("getShortVolatile", short.class, Object.class, long.class);
            PUT_SHORT_VOLATILE = binder.bind("putShortVolatile", void.class, Object.class, long.class, short.class);

            if (JPMS) {
                GET_SHORT_ACQUIRE = binder.bind("getShortAcquire", short.class, Object.class, long.class);
                PUT_SHORT_RELEASE = binder.bind("putShortRelease", void.class, Object.class, long.class, short.class);
                GET_SHORT_OPAQUE = binder.bind("getShortOpaque", short.class, Object.class, long.class);
                PUT_SHORT_OPAQUE = binder.bind("putShortOpaque", void.class, Object.class, long.class, short.class);
                COMPARE_AND_SET_SHORT = binder.bind("compareAndSetShort", boolean.class, Object.class, long.class, short.class, short.class);

                WEAK_COMPARE_AND_SET_SHORT = binder.bind("weakCompareAndSetShort", boolean.class, Object.class, long.class, short.class, short.class);
                WEAK_COMPARE_AND_SET_SHORT_ACQUIRE = binder.bind("weakCompareAndSetShortAcquire", boolean.class, Object.class, long.class, short.class, short.class);
                WEAK_COMPARE_AND_SET_SHORT_RELEASE = binder.bind("weakCompareAndSetShortRelease", boolean.class, Object.class, long.class, short.class, short.class);
                WEAK_COMPARE_AND_SET_SHORT_PLAIN = binder.bind("weakCompareAndSetShortPlain", boolean.class, Object.class, long.class, short.class, short.class);

                COMPARE_AND_EXCHANGE_SHORT = binder.bind("compareAndExchangeShort", short.class, Object.class, long.class, short.class, short.class);
                COMPARE_AND_EXCHANGE_SHORT_ACQUIRE = binder.bind("compareAndExchangeShortAcquire", short.class, Object.class, long.class, short.class, short.class);
                COMPARE_AND_EXCHANGE_SHORT_RELEASE = binder.bind("compareAndExchangeShortRelease", short.class, Object.class, long.class, short.class, short.class);
            } else {
                GET_SHORT_ACQUIRE = GET_SHORT_VOLATILE;
                PUT_SHORT_RELEASE = binder.bindCompat("compatPutShortRelease", void.class, Object.class, long.class, short.class);
                GET_SHORT_OPAQUE = GET_SHORT_VOLATILE;
                PUT_SHORT_OPAQUE = PUT_SHORT_VOLATILE;
                COMPARE_AND_SET_SHORT = binder.bindCompat("compatCompareAndSetShort", boolean.class, Object.class, long.class, short.class, short.class);

                WEAK_COMPARE_AND_SET_SHORT = COMPARE_AND_SET_SHORT;
                WEAK_COMPARE_AND_SET_SHORT_ACQUIRE = COMPARE_AND_SET_SHORT;
                WEAK_COMPARE_AND_SET_SHORT_RELEASE = COMPARE_AND_SET_SHORT;
                WEAK_COMPARE_AND_SET_SHORT_PLAIN = COMPARE_AND_SET_SHORT;

                COMPARE_AND_EXCHANGE_SHORT = binder.bindCompat("compatCompareAndExchangeShort", short.class, Object.class, long.class, short.class, short.class);
                COMPARE_AND_EXCHANGE_SHORT_ACQUIRE = COMPARE_AND_EXCHANGE_SHORT;
                COMPARE_AND_EXCHANGE_SHORT_RELEASE = COMPARE_AND_EXCHANGE_SHORT;
            }

            // --- 8. Char ---
            GET_CHAR = binder.bind("getChar", char.class, Object.class, long.class);
            PUT_CHAR = binder.bind("putChar", void.class, Object.class, long.class, char.class);
            GET_CHAR_VOLATILE = binder.bind("getCharVolatile", char.class, Object.class, long.class);
            PUT_CHAR_VOLATILE = binder.bind("putCharVolatile", void.class, Object.class, long.class, char.class);

            if (JPMS) {
                GET_CHAR_ACQUIRE = binder.bind("getCharAcquire", char.class, Object.class, long.class);
                PUT_CHAR_RELEASE = binder.bind("putCharRelease", void.class, Object.class, long.class, char.class);
                GET_CHAR_OPAQUE = binder.bind("getCharOpaque", char.class, Object.class, long.class);
                PUT_CHAR_OPAQUE = binder.bind("putCharOpaque", void.class, Object.class, long.class, char.class);
                COMPARE_AND_SET_CHAR = binder.bind("compareAndSetChar", boolean.class, Object.class, long.class, char.class, char.class);

                WEAK_COMPARE_AND_SET_CHAR = binder.bind("weakCompareAndSetChar", boolean.class, Object.class, long.class, char.class, char.class);
                WEAK_COMPARE_AND_SET_CHAR_ACQUIRE = binder.bind("weakCompareAndSetCharAcquire", boolean.class, Object.class, long.class, char.class, char.class);
                WEAK_COMPARE_AND_SET_CHAR_RELEASE = binder.bind("weakCompareAndSetCharRelease", boolean.class, Object.class, long.class, char.class, char.class);
                WEAK_COMPARE_AND_SET_CHAR_PLAIN = binder.bind("weakCompareAndSetCharPlain", boolean.class, Object.class, long.class, char.class, char.class);

                COMPARE_AND_EXCHANGE_CHAR = binder.bind("compareAndExchangeChar", char.class, Object.class, long.class, char.class, char.class);
                COMPARE_AND_EXCHANGE_CHAR_ACQUIRE = binder.bind("compareAndExchangeCharAcquire", char.class, Object.class, long.class, char.class, char.class);
                COMPARE_AND_EXCHANGE_CHAR_RELEASE = binder.bind("compareAndExchangeCharRelease", char.class, Object.class, long.class, char.class, char.class);
            } else {
                GET_CHAR_ACQUIRE = GET_CHAR_VOLATILE;
                PUT_CHAR_RELEASE = binder.bindCompat("compatPutCharRelease", void.class, Object.class, long.class, char.class);
                GET_CHAR_OPAQUE = GET_CHAR_VOLATILE;
                PUT_CHAR_OPAQUE = PUT_CHAR_VOLATILE;
                COMPARE_AND_SET_CHAR = binder.bindCompat("compatCompareAndSetChar", boolean.class, Object.class, long.class, char.class, char.class);

                WEAK_COMPARE_AND_SET_CHAR = COMPARE_AND_SET_CHAR;
                WEAK_COMPARE_AND_SET_CHAR_ACQUIRE = COMPARE_AND_SET_CHAR;
                WEAK_COMPARE_AND_SET_CHAR_RELEASE = COMPARE_AND_SET_CHAR;
                WEAK_COMPARE_AND_SET_CHAR_PLAIN = COMPARE_AND_SET_CHAR;

                COMPARE_AND_EXCHANGE_CHAR = binder.bindCompat("compatCompareAndExchangeChar", char.class, Object.class, long.class, char.class, char.class);
                COMPARE_AND_EXCHANGE_CHAR_ACQUIRE = COMPARE_AND_EXCHANGE_CHAR;
                COMPARE_AND_EXCHANGE_CHAR_RELEASE = COMPARE_AND_EXCHANGE_CHAR;
            }

            // --- 9. Float ---
            GET_FLOAT = binder.bind("getFloat", float.class, Object.class, long.class);
            PUT_FLOAT = binder.bind("putFloat", void.class, Object.class, long.class, float.class);
            GET_FLOAT_VOLATILE = binder.bind("getFloatVolatile", float.class, Object.class, long.class);
            PUT_FLOAT_VOLATILE = binder.bind("putFloatVolatile", void.class, Object.class, long.class, float.class);

            if (JPMS) {
                GET_FLOAT_ACQUIRE = binder.bind("getFloatAcquire", float.class, Object.class, long.class);
                PUT_FLOAT_RELEASE = binder.bind("putFloatRelease", void.class, Object.class, long.class, float.class);
                GET_FLOAT_OPAQUE = binder.bind("getFloatOpaque", float.class, Object.class, long.class);
                PUT_FLOAT_OPAQUE = binder.bind("putFloatOpaque", void.class, Object.class, long.class, float.class);
                COMPARE_AND_SET_FLOAT = binder.bind("compareAndSetFloat", boolean.class, Object.class, long.class, float.class, float.class);

                WEAK_COMPARE_AND_SET_FLOAT = binder.bind("weakCompareAndSetFloat", boolean.class, Object.class, long.class, float.class, float.class);
                WEAK_COMPARE_AND_SET_FLOAT_ACQUIRE = binder.bind("weakCompareAndSetFloatAcquire", boolean.class, Object.class, long.class, float.class, float.class);
                WEAK_COMPARE_AND_SET_FLOAT_RELEASE = binder.bind("weakCompareAndSetFloatRelease", boolean.class, Object.class, long.class, float.class, float.class);
                WEAK_COMPARE_AND_SET_FLOAT_PLAIN = binder.bind("weakCompareAndSetFloatPlain", boolean.class, Object.class, long.class, float.class, float.class);

                COMPARE_AND_EXCHANGE_FLOAT = binder.bind("compareAndExchangeFloat", float.class, Object.class, long.class, float.class, float.class);
                COMPARE_AND_EXCHANGE_FLOAT_ACQUIRE = binder.bind("compareAndExchangeFloatAcquire", float.class, Object.class, long.class, float.class, float.class);
                COMPARE_AND_EXCHANGE_FLOAT_RELEASE = binder.bind("compareAndExchangeFloatRelease", float.class, Object.class, long.class, float.class, float.class);
            } else {
                GET_FLOAT_ACQUIRE = GET_FLOAT_VOLATILE;
                PUT_FLOAT_RELEASE = binder.bindCompat("compatPutFloatRelease", void.class, Object.class, long.class, float.class);
                GET_FLOAT_OPAQUE = GET_FLOAT_VOLATILE;
                PUT_FLOAT_OPAQUE = PUT_FLOAT_VOLATILE;
                COMPARE_AND_SET_FLOAT = binder.bindCompat("compatCompareAndSetFloat", boolean.class, Object.class, long.class, float.class, float.class);

                WEAK_COMPARE_AND_SET_FLOAT = COMPARE_AND_SET_FLOAT;
                WEAK_COMPARE_AND_SET_FLOAT_ACQUIRE = COMPARE_AND_SET_FLOAT;
                WEAK_COMPARE_AND_SET_FLOAT_RELEASE = COMPARE_AND_SET_FLOAT;
                WEAK_COMPARE_AND_SET_FLOAT_PLAIN = COMPARE_AND_SET_FLOAT;

                COMPARE_AND_EXCHANGE_FLOAT = binder.bindCompat("compatCompareAndExchangeFloat", float.class, Object.class, long.class, float.class, float.class);
                COMPARE_AND_EXCHANGE_FLOAT_ACQUIRE = COMPARE_AND_EXCHANGE_FLOAT;
                COMPARE_AND_EXCHANGE_FLOAT_RELEASE = COMPARE_AND_EXCHANGE_FLOAT;
            }

            // --- 10. Double ---
            GET_DOUBLE = binder.bind("getDouble", double.class, Object.class, long.class);
            PUT_DOUBLE = binder.bind("putDouble", void.class, Object.class, long.class, double.class);
            GET_DOUBLE_VOLATILE = binder.bind("getDoubleVolatile", double.class, Object.class, long.class);
            PUT_DOUBLE_VOLATILE = binder.bind("putDoubleVolatile", void.class, Object.class, long.class, double.class);

            if (JPMS) {
                GET_DOUBLE_ACQUIRE = binder.bind("getDoubleAcquire", double.class, Object.class, long.class);
                PUT_DOUBLE_RELEASE = binder.bind("putDoubleRelease", void.class, Object.class, long.class, double.class);
                GET_DOUBLE_OPAQUE = binder.bind("getDoubleOpaque", double.class, Object.class, long.class);
                PUT_DOUBLE_OPAQUE = binder.bind("putDoubleOpaque", void.class, Object.class, long.class, double.class);
                COMPARE_AND_SET_DOUBLE = binder.bind("compareAndSetDouble", boolean.class, Object.class, long.class, double.class, double.class);

                WEAK_COMPARE_AND_SET_DOUBLE = binder.bind("weakCompareAndSetDouble", boolean.class, Object.class, long.class, double.class, double.class);
                WEAK_COMPARE_AND_SET_DOUBLE_ACQUIRE = binder.bind("weakCompareAndSetDoubleAcquire", boolean.class, Object.class, long.class, double.class, double.class);
                WEAK_COMPARE_AND_SET_DOUBLE_RELEASE = binder.bind("weakCompareAndSetDoubleRelease", boolean.class, Object.class, long.class, double.class, double.class);
                WEAK_COMPARE_AND_SET_DOUBLE_PLAIN = binder.bind("weakCompareAndSetDoublePlain", boolean.class, Object.class, long.class, double.class, double.class);

                COMPARE_AND_EXCHANGE_DOUBLE = binder.bind("compareAndExchangeDouble", double.class, Object.class, long.class, double.class, double.class);
                COMPARE_AND_EXCHANGE_DOUBLE_ACQUIRE = binder.bind("compareAndExchangeDoubleAcquire", double.class, Object.class, long.class, double.class, double.class);
                COMPARE_AND_EXCHANGE_DOUBLE_RELEASE = binder.bind("compareAndExchangeDoubleRelease", double.class, Object.class, long.class, double.class, double.class);
            } else {
                GET_DOUBLE_ACQUIRE = GET_DOUBLE_VOLATILE;
                PUT_DOUBLE_RELEASE = binder.bindCompat("compatPutDoubleRelease", void.class, Object.class, long.class, double.class);
                GET_DOUBLE_OPAQUE = GET_DOUBLE_VOLATILE;
                PUT_DOUBLE_OPAQUE = PUT_DOUBLE_VOLATILE;
                COMPARE_AND_SET_DOUBLE = binder.bindCompat("compatCompareAndSetDouble", boolean.class, Object.class, long.class, double.class, double.class);

                WEAK_COMPARE_AND_SET_DOUBLE = COMPARE_AND_SET_DOUBLE;
                WEAK_COMPARE_AND_SET_DOUBLE_ACQUIRE = COMPARE_AND_SET_DOUBLE;
                WEAK_COMPARE_AND_SET_DOUBLE_RELEASE = COMPARE_AND_SET_DOUBLE;
                WEAK_COMPARE_AND_SET_DOUBLE_PLAIN = COMPARE_AND_SET_DOUBLE;

                COMPARE_AND_EXCHANGE_DOUBLE = binder.bindCompat("compatCompareAndExchangeDouble", double.class, Object.class, long.class, double.class, double.class);
                COMPARE_AND_EXCHANGE_DOUBLE_ACQUIRE = COMPARE_AND_EXCHANGE_DOUBLE;
                COMPARE_AND_EXCHANGE_DOUBLE_RELEASE = COMPARE_AND_EXCHANGE_DOUBLE;
            }

            // --- 11. Memory ---
            ALLOCATE_MEMORY = binder.bind("allocateMemory", long.class, long.class);
            FREE_MEMORY = binder.bind("freeMemory", void.class, long.class);
            REALLOCATE_MEMORY = binder.bind("reallocateMemory", long.class, long.class, long.class);
            SET_MEMORY = binder.bind("setMemory", void.class, long.class, long.class, byte.class);
            COPY_MEMORY = binder.bind("copyMemory", void.class, Object.class, long.class, Object.class, long.class, long.class);
            GET_LONG_ADDRESS = binder.bind("getLong", long.class, long.class);
            PUT_LONG_ADDRESS = binder.bind("putLong", void.class, long.class, long.class);

            // --- 12. Fences & Misc ---
            LOAD_FENCE = binder.bind("loadFence", void.class);
            STORE_FENCE = binder.bind("storeFence", void.class);
            FULL_FENCE = binder.bind("fullFence", void.class);
            PARK = binder.bind("park", void.class, boolean.class, long.class);
            UNPARK = binder.bind("unpark", void.class, Object.class);

        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

    private UnsafeWrapper() {
    }

    public static UnsafeWrapper getUnsafe() {
        return theUnsafe;
    }

    public <T extends Throwable> long objectFieldOffset(Field f) throws T {
        try {
            return (long) OBJECT_FIELD_OFFSET.invokeExact(f);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> Object staticFieldBase(Field f) throws T {
        try {
            return (Object) STATIC_FIELD_BASE.invokeExact(f);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> long staticFieldOffset(Field f) throws T {
        try {
            return (long) STATIC_FIELD_OFFSET.invokeExact(f);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> Object allocateInstance(Class<?> cls) throws InstantiationException, T {
        try {
            return ALLOCATE_INSTANCE.invokeExact(cls);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> Object allocateUninitializedArray(Class<?> componentType, int length) throws T {
        try {
            return ALLOCATE_UNINITIALIZED_ARRAY.invokeExact(componentType, length);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> long arrayBaseOffset(Class<?> cls) throws T {
        try {
            return (long) ARRAY_BASE_OFFSET.invokeExact(cls);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> int arrayIndexScale(Class<?> cls) throws T {
        try {
            return (int) ARRAY_INDEX_SCALE.invokeExact(cls);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> int addressSize() throws T {
        try {
            return (int) ADDRESS_SIZE.invokeExact();
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> int pageSize() throws T {
        try {
            return (int) PAGE_SIZE.invokeExact();
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> Object getReference(Object o, long offset) throws T {
        try {
            return GET_REFERENCE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putReference(Object o, long offset, Object x) throws T {
        try {
            PUT_REFERENCE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> Object getReferenceVolatile(Object o, long offset) throws T {
        try {
            return GET_REFERENCE_VOLATILE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putReferenceVolatile(Object o, long offset, Object x) throws T {
        try {
            PUT_REFERENCE_VOLATILE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> Object getReferenceAcquire(Object o, long offset) throws T {
        try {
            return GET_REFERENCE_ACQUIRE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putReferenceRelease(Object o, long offset, Object x) throws T {
        try {
            PUT_REFERENCE_RELEASE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> Object getReferenceOpaque(Object o, long offset) throws T {
        try {
            return GET_REFERENCE_OPAQUE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putReferenceOpaque(Object o, long offset, Object x) throws T {
        try {
            PUT_REFERENCE_OPAQUE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean compareAndSetReference(Object o, long offset, Object expected, Object x) throws T {
        try {
            return (boolean) COMPARE_AND_SET_REFERENCE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> Object getAndSetReference(Object o, long offset, Object x) throws T {
        try {
            return GET_AND_SET_REFERENCE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetReference(Object o, long offset, Object expected, Object x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_REFERENCE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetReferenceAcquire(Object o, long offset, Object expected, Object x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_REFERENCE_ACQUIRE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetReferenceRelease(Object o, long offset, Object expected, Object x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_REFERENCE_RELEASE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetReferencePlain(Object o, long offset, Object expected, Object x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_REFERENCE_PLAIN.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> Object compareAndExchangeReference(Object o, long offset, Object expected, Object x) throws T {
        try {
            return COMPARE_AND_EXCHANGE_REFERENCE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> Object compareAndExchangeReferenceAcquire(Object o, long offset, Object expected, Object x) throws T {
        try {
            return COMPARE_AND_EXCHANGE_REFERENCE_ACQUIRE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> Object compareAndExchangeReferenceRelease(Object o, long offset, Object expected, Object x) throws T {
        try {
            return COMPARE_AND_EXCHANGE_REFERENCE_RELEASE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> Object getAndSetReferenceAcquire(Object o, long offset, Object x) throws T {
        try {
            return GET_AND_SET_REFERENCE_ACQUIRE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> Object getAndSetReferenceRelease(Object o, long offset, Object x) throws T {
        try {
            return GET_AND_SET_REFERENCE_RELEASE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> int getInt(Object o, long offset) throws T {
        try {
            return (int) GET_INT.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putInt(Object o, long offset, int x) throws T {
        try {
            PUT_INT.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> int getIntVolatile(Object o, long offset) throws T {
        try {
            return (int) GET_INT_VOLATILE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putIntVolatile(Object o, long offset, int x) throws T {
        try {
            PUT_INT_VOLATILE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> int getIntAcquire(Object o, long offset) throws T {
        try {
            return (int) GET_INT_ACQUIRE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putIntRelease(Object o, long offset, int x) throws T {
        try {
            PUT_INT_RELEASE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> int getIntOpaque(Object o, long offset) throws T {
        try {
            return (int) GET_INT_OPAQUE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putIntOpaque(Object o, long offset, int x) throws T {
        try {
            PUT_INT_OPAQUE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean compareAndSetInt(Object o, long offset, int expected, int x) throws T {
        try {
            return (boolean) COMPARE_AND_SET_INT.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> int getAndAddInt(Object o, long offset, int delta) throws T {
        try {
            return (int) GET_AND_ADD_INT.invokeExact(o, offset, delta);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> int getAndSetInt(Object o, long offset, int x) throws T {
        try {
            return (int) GET_AND_SET_INT.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetInt(Object o, long offset, int expected, int x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_INT.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetIntAcquire(Object o, long offset, int expected, int x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_INT_ACQUIRE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetIntRelease(Object o, long offset, int expected, int x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_INT_RELEASE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetIntPlain(Object o, long offset, int expected, int x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_INT_PLAIN.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> int compareAndExchangeInt(Object o, long offset, int expected, int x) throws T {
        try {
            return (int) COMPARE_AND_EXCHANGE_INT.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> int compareAndExchangeIntAcquire(Object o, long offset, int expected, int x) throws T {
        try {
            return (int) COMPARE_AND_EXCHANGE_INT_ACQUIRE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> int compareAndExchangeIntRelease(Object o, long offset, int expected, int x) throws T {
        try {
            return (int) COMPARE_AND_EXCHANGE_INT_RELEASE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> int getAndSetIntAcquire(Object o, long offset, int x) throws T {
        try {
            return (int) GET_AND_SET_INT_ACQUIRE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> int getAndSetIntRelease(Object o, long offset, int x) throws T {
        try {
            return (int) GET_AND_SET_INT_RELEASE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> long getLong(Object o, long offset) throws T {
        try {
            return (long) GET_LONG.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putLong(Object o, long offset, long x) throws T {
        try {
            PUT_LONG.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> long getLongVolatile(Object o, long offset) throws T {
        try {
            return (long) GET_LONG_VOLATILE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putLongVolatile(Object o, long offset, long x) throws T {
        try {
            PUT_LONG_VOLATILE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> long getLongAcquire(Object o, long offset) throws T {
        try {
            return (long) GET_LONG_ACQUIRE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putLongRelease(Object o, long offset, long x) throws T {
        try {
            PUT_LONG_RELEASE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> long getLongOpaque(Object o, long offset) throws T {
        try {
            return (long) GET_LONG_OPAQUE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putLongOpaque(Object o, long offset, long x) throws T {
        try {
            PUT_LONG_OPAQUE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean compareAndSetLong(Object o, long offset, long expected, long x) throws T {
        try {
            return (boolean) COMPARE_AND_SET_LONG.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> long getAndAddLong(Object o, long offset, long delta) throws T {
        try {
            return (long) GET_AND_ADD_LONG.invokeExact(o, offset, delta);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> long getAndSetLong(Object o, long offset, long x) throws T {
        try {
            return (long) GET_AND_SET_LONG.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetLong(Object o, long offset, long expected, long x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_LONG.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetLongAcquire(Object o, long offset, long expected, long x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_LONG_ACQUIRE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetLongRelease(Object o, long offset, long expected, long x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_LONG_RELEASE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetLongPlain(Object o, long offset, long expected, long x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_LONG_PLAIN.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> long compareAndExchangeLong(Object o, long offset, long expected, long x) throws T {
        try {
            return (long) COMPARE_AND_EXCHANGE_LONG.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> long compareAndExchangeLongAcquire(Object o, long offset, long expected, long x) throws T {
        try {
            return (long) COMPARE_AND_EXCHANGE_LONG_ACQUIRE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> long compareAndExchangeLongRelease(Object o, long offset, long expected, long x) throws T {
        try {
            return (long) COMPARE_AND_EXCHANGE_LONG_RELEASE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> long getAndSetLongAcquire(Object o, long offset, long x) throws T {
        try {
            return (long) GET_AND_SET_LONG_ACQUIRE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> long getAndSetLongRelease(Object o, long offset, long x) throws T {
        try {
            return (long) GET_AND_SET_LONG_RELEASE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean getBoolean(Object o, long offset) throws T {
        try {
            return (boolean) GET_BOOLEAN.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putBoolean(Object o, long offset, boolean x) throws T {
        try {
            PUT_BOOLEAN.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean getBooleanVolatile(Object o, long offset) throws T {
        try {
            return (boolean) GET_BOOLEAN_VOLATILE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putBooleanVolatile(Object o, long offset, boolean x) throws T {
        try {
            PUT_BOOLEAN_VOLATILE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean getBooleanAcquire(Object o, long offset) throws T {
        try {
            return (boolean) GET_BOOLEAN_ACQUIRE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putBooleanRelease(Object o, long offset, boolean x) throws T {
        try {
            PUT_BOOLEAN_RELEASE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean getBooleanOpaque(Object o, long offset) throws T {
        try {
            return (boolean) GET_BOOLEAN_OPAQUE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putBooleanOpaque(Object o, long offset, boolean x) throws T {
        try {
            PUT_BOOLEAN_OPAQUE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean compareAndSetBoolean(Object o, long offset, boolean expected, boolean x) throws T {
        try {
            return (boolean) COMPARE_AND_SET_BOOLEAN.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetBoolean(Object o, long offset, boolean expected, boolean x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_BOOLEAN.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetBooleanAcquire(Object o, long offset, boolean expected, boolean x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_BOOLEAN_ACQUIRE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetBooleanRelease(Object o, long offset, boolean expected, boolean x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_BOOLEAN_RELEASE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetBooleanPlain(Object o, long offset, boolean expected, boolean x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_BOOLEAN_PLAIN.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean compareAndExchangeBoolean(Object o, long offset, boolean expected, boolean x) throws T {
        try {
            return (boolean) COMPARE_AND_EXCHANGE_BOOLEAN.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean compareAndExchangeBooleanAcquire(Object o, long offset, boolean expected, boolean x) throws T {
        try {
            return (boolean) COMPARE_AND_EXCHANGE_BOOLEAN_ACQUIRE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean compareAndExchangeBooleanRelease(Object o, long offset, boolean expected, boolean x) throws T {
        try {
            return (boolean) COMPARE_AND_EXCHANGE_BOOLEAN_RELEASE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> byte getByte(Object o, long offset) throws T {
        try {
            return (byte) GET_BYTE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putByte(Object o, long offset, byte x) throws T {
        try {
            PUT_BYTE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> byte getByteVolatile(Object o, long offset) throws T {
        try {
            return (byte) GET_BYTE_VOLATILE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putByteVolatile(Object o, long offset, byte x) throws T {
        try {
            PUT_BYTE_VOLATILE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> byte getByteAcquire(Object o, long offset) throws T {
        try {
            return (byte) GET_BYTE_ACQUIRE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putByteRelease(Object o, long offset, byte x) throws T {
        try {
            PUT_BYTE_RELEASE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> byte getByteOpaque(Object o, long offset) throws T {
        try {
            return (byte) GET_BYTE_OPAQUE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putByteOpaque(Object o, long offset, byte x) throws T {
        try {
            PUT_BYTE_OPAQUE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean compareAndSetByte(Object o, long offset, byte expected, byte x) throws T {
        try {
            return (boolean) COMPARE_AND_SET_BYTE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetByte(Object o, long offset, byte expected, byte x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_BYTE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetByteAcquire(Object o, long offset, byte expected, byte x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_BYTE_ACQUIRE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetByteRelease(Object o, long offset, byte expected, byte x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_BYTE_RELEASE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetBytePlain(Object o, long offset, byte expected, byte x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_BYTE_PLAIN.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> byte compareAndExchangeByte(Object o, long offset, byte expected, byte x) throws T {
        try {
            return (byte) COMPARE_AND_EXCHANGE_BYTE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> byte compareAndExchangeByteAcquire(Object o, long offset, byte expected, byte x) throws T {
        try {
            return (byte) COMPARE_AND_EXCHANGE_BYTE_ACQUIRE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> byte compareAndExchangeByteRelease(Object o, long offset, byte expected, byte x) throws T {
        try {
            return (byte) COMPARE_AND_EXCHANGE_BYTE_RELEASE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> short getShort(Object o, long offset) throws T {
        try {
            return (short) GET_SHORT.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putShort(Object o, long offset, short x) throws T {
        try {
            PUT_SHORT.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> short getShortVolatile(Object o, long offset) throws T {
        try {
            return (short) GET_SHORT_VOLATILE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putShortVolatile(Object o, long offset, short x) throws T {
        try {
            PUT_SHORT_VOLATILE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> short getShortAcquire(Object o, long offset) throws T {
        try {
            return (short) GET_SHORT_ACQUIRE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putShortRelease(Object o, long offset, short x) throws T {
        try {
            PUT_SHORT_RELEASE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> short getShortOpaque(Object o, long offset) throws T {
        try {
            return (short) GET_SHORT_OPAQUE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putShortOpaque(Object o, long offset, short x) throws T {
        try {
            PUT_SHORT_OPAQUE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean compareAndSetShort(Object o, long offset, short expected, short x) throws T {
        try {
            return (boolean) COMPARE_AND_SET_SHORT.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetShort(Object o, long offset, short expected, short x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_SHORT.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetShortAcquire(Object o, long offset, short expected, short x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_SHORT_ACQUIRE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetShortRelease(Object o, long offset, short expected, short x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_SHORT_RELEASE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetShortPlain(Object o, long offset, short expected, short x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_SHORT_PLAIN.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> short compareAndExchangeShort(Object o, long offset, short expected, short x) throws T {
        try {
            return (short) COMPARE_AND_EXCHANGE_SHORT.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> short compareAndExchangeShortAcquire(Object o, long offset, short expected, short x) throws T {
        try {
            return (short) COMPARE_AND_EXCHANGE_SHORT_ACQUIRE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> short compareAndExchangeShortRelease(Object o, long offset, short expected, short x) throws T {
        try {
            return (short) COMPARE_AND_EXCHANGE_SHORT_RELEASE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> char getChar(Object o, long offset) throws T {
        try {
            return (char) GET_CHAR.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putChar(Object o, long offset, char x) throws T {
        try {
            PUT_CHAR.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> char getCharVolatile(Object o, long offset) throws T {
        try {
            return (char) GET_CHAR_VOLATILE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putCharVolatile(Object o, long offset, char x) throws T {
        try {
            PUT_CHAR_VOLATILE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> char getCharAcquire(Object o, long offset) throws T {
        try {
            return (char) GET_CHAR_ACQUIRE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putCharRelease(Object o, long offset, char x) throws T {
        try {
            PUT_CHAR_RELEASE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> char getCharOpaque(Object o, long offset) throws T {
        try {
            return (char) GET_CHAR_OPAQUE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putCharOpaque(Object o, long offset, char x) throws T {
        try {
            PUT_CHAR_OPAQUE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean compareAndSetChar(Object o, long offset, char expected, char x) throws T {
        try {
            return (boolean) COMPARE_AND_SET_CHAR.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetChar(Object o, long offset, char expected, char x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_CHAR.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetCharAcquire(Object o, long offset, char expected, char x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_CHAR_ACQUIRE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetCharRelease(Object o, long offset, char expected, char x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_CHAR_RELEASE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetCharPlain(Object o, long offset, char expected, char x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_CHAR_PLAIN.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> char compareAndExchangeChar(Object o, long offset, char expected, char x) throws T {
        try {
            return (char) COMPARE_AND_EXCHANGE_CHAR.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> char compareAndExchangeCharAcquire(Object o, long offset, char expected, char x) throws T {
        try {
            return (char) COMPARE_AND_EXCHANGE_CHAR_ACQUIRE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> char compareAndExchangeCharRelease(Object o, long offset, char expected, char x) throws T {
        try {
            return (char) COMPARE_AND_EXCHANGE_CHAR_RELEASE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> float getFloat(Object o, long offset) throws T {
        try {
            return (float) GET_FLOAT.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putFloat(Object o, long offset, float x) throws T {
        try {
            PUT_FLOAT.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> float getFloatVolatile(Object o, long offset) throws T {
        try {
            return (float) GET_FLOAT_VOLATILE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putFloatVolatile(Object o, long offset, float x) throws T {
        try {
            PUT_FLOAT_VOLATILE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> float getFloatAcquire(Object o, long offset) throws T {
        try {
            return (float) GET_FLOAT_ACQUIRE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putFloatRelease(Object o, long offset, float x) throws T {
        try {
            PUT_FLOAT_RELEASE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> float getFloatOpaque(Object o, long offset) throws T {
        try {
            return (float) GET_FLOAT_OPAQUE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putFloatOpaque(Object o, long offset, float x) throws T {
        try {
            PUT_FLOAT_OPAQUE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean compareAndSetFloat(Object o, long offset, float expected, float x) throws T {
        try {
            return (boolean) COMPARE_AND_SET_FLOAT.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetFloat(Object o, long offset, float expected, float x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_FLOAT.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetFloatAcquire(Object o, long offset, float expected, float x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_FLOAT_ACQUIRE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetFloatRelease(Object o, long offset, float expected, float x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_FLOAT_RELEASE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetFloatPlain(Object o, long offset, float expected, float x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_FLOAT_PLAIN.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> float compareAndExchangeFloat(Object o, long offset, float expected, float x) throws T {
        try {
            return (float) COMPARE_AND_EXCHANGE_FLOAT.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> float compareAndExchangeFloatAcquire(Object o, long offset, float expected, float x) throws T {
        try {
            return (float) COMPARE_AND_EXCHANGE_FLOAT_ACQUIRE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> float compareAndExchangeFloatRelease(Object o, long offset, float expected, float x) throws T {
        try {
            return (float) COMPARE_AND_EXCHANGE_FLOAT_RELEASE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> double getDouble(Object o, long offset) throws T {
        try {
            return (double) GET_DOUBLE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putDouble(Object o, long offset, double x) throws T {
        try {
            PUT_DOUBLE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> double getDoubleVolatile(Object o, long offset) throws T {
        try {
            return (double) GET_DOUBLE_VOLATILE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putDoubleVolatile(Object o, long offset, double x) throws T {
        try {
            PUT_DOUBLE_VOLATILE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> double getDoubleAcquire(Object o, long offset) throws T {
        try {
            return (double) GET_DOUBLE_ACQUIRE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putDoubleRelease(Object o, long offset, double x) throws T {
        try {
            PUT_DOUBLE_RELEASE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> double getDoubleOpaque(Object o, long offset) throws T {
        try {
            return (double) GET_DOUBLE_OPAQUE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putDoubleOpaque(Object o, long offset, double x) throws T {
        try {
            PUT_DOUBLE_OPAQUE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean compareAndSetDouble(Object o, long offset, double expected, double x) throws T {
        try {
            return (boolean) COMPARE_AND_SET_DOUBLE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetDouble(Object o, long offset, double expected, double x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_DOUBLE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetDoubleAcquire(Object o, long offset, double expected, double x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_DOUBLE_ACQUIRE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetDoubleRelease(Object o, long offset, double expected, double x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_DOUBLE_RELEASE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> boolean weakCompareAndSetDoublePlain(Object o, long offset, double expected, double x) throws T {
        try {
            return (boolean) WEAK_COMPARE_AND_SET_DOUBLE_PLAIN.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> double compareAndExchangeDouble(Object o, long offset, double expected, double x) throws T {
        try {
            return (double) COMPARE_AND_EXCHANGE_DOUBLE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> double compareAndExchangeDoubleAcquire(Object o, long offset, double expected, double x) throws T {
        try {
            return (double) COMPARE_AND_EXCHANGE_DOUBLE_ACQUIRE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> double compareAndExchangeDoubleRelease(Object o, long offset, double expected, double x) throws T {
        try {
            return (double) COMPARE_AND_EXCHANGE_DOUBLE_RELEASE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> long allocateMemory(long bytes) throws T {
        try {
            return (long) ALLOCATE_MEMORY.invokeExact(bytes);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void freeMemory(long address) throws T {
        try {
            FREE_MEMORY.invokeExact(address);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> long reallocateMemory(long address, long bytes) throws T {
        try {
            return (long) REALLOCATE_MEMORY.invokeExact(address, bytes);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void setMemory(long address, long bytes, byte value) throws T {
        try {
            SET_MEMORY.invokeExact(address, bytes, value);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void copyMemory(Object srcBase, long srcOffset, Object destBase, long destOffset, long bytes) throws T {
        try {
            COPY_MEMORY.invokeExact(srcBase, srcOffset, destBase, destOffset, bytes);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> long getLong(long address) throws T {
        try {
            return (long) GET_LONG_ADDRESS.invokeExact(address);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void putLong(long address, long value) throws T {
        try {
            PUT_LONG_ADDRESS.invokeExact(address, value);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void loadFence() throws T {
        try {
            LOAD_FENCE.invokeExact();
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void storeFence() throws T {
        try {
            STORE_FENCE.invokeExact();
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void fullFence() throws T {
        try {
            FULL_FENCE.invokeExact();
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void park(boolean isAbsolute, long time) throws T {
        try {
            PARK.invokeExact(isAbsolute, time);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public <T extends Throwable> void unpark(Object thread) throws T {
        try {
            UNPARK.invokeExact(thread);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    @Deprecated
    public <T extends Throwable> Object getObject(Object o, long offset) throws T {
        try {
            return GET_REFERENCE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    @Deprecated
    public <T extends Throwable> void putObject(Object o, long offset, Object x) throws T {
        try {
            PUT_REFERENCE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    @Deprecated
    public <T extends Throwable> Object getObjectVolatile(Object o, long offset) throws T {
        try {
            return GET_REFERENCE_VOLATILE.invokeExact(o, offset);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    @Deprecated
    public <T extends Throwable> void putObjectVolatile(Object o, long offset, Object x) throws T {
        try {
            PUT_REFERENCE_VOLATILE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    @Deprecated
    public <T extends Throwable> boolean compareAndSwapObject(Object o, long offset, Object expected, Object x) throws T {
        try {
            return (boolean) COMPARE_AND_SET_REFERENCE.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    @Deprecated
    public <T extends Throwable> boolean compareAndSwapInt(Object o, long offset, int expected, int x) throws T {
        try {
            return (boolean) COMPARE_AND_SET_INT.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    @Deprecated
    public <T extends Throwable> boolean compareAndSwapLong(Object o, long offset, long expected, long x) throws T {
        try {
            return (boolean) COMPARE_AND_SET_LONG.invokeExact(o, offset, expected, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    @Deprecated
    public <T extends Throwable> Object getAndSetObject(Object o, long offset, Object x) throws T {
        try {
            return GET_AND_SET_REFERENCE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    @Deprecated
    public <T extends Throwable> void putOrderedInt(Object o, long offset, int x) throws T {
        try {
            PUT_INT_RELEASE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    @Deprecated
    public <T extends Throwable> void putOrderedLong(Object o, long offset, long x) throws T {
        try {
            PUT_LONG_RELEASE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    @Deprecated
    public <T extends Throwable> void putOrderedObject(Object o, long offset, Object x) throws T {
        try {
            PUT_REFERENCE_RELEASE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    @SuppressWarnings("unused")
    private static Object compatCompareAndExchangeReference(Object o, long offset, Object expected, Object x) {
        while (true) {
            Object witness;
            try {
                witness = GET_REFERENCE_VOLATILE.invokeExact(o, offset);
                if (witness != expected) {
                    return witness;
                }
                if ((boolean) COMPARE_AND_SET_REFERENCE.invokeExact(o, offset, expected, x)) {
                    return expected;
                }
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    private static int compatCompareAndExchangeInt(Object o, long offset, int expected, int x) {
        while (true) {
            int witness;
            try {
                witness = (int) GET_INT_VOLATILE.invokeExact(o, offset);
                if (witness != expected) {
                    return witness;
                }
                if ((boolean) COMPARE_AND_SET_INT.invokeExact(o, offset, expected, x)) {
                    return expected;
                }
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    private static long compatCompareAndExchangeLong(Object o, long offset, long expected, long x) {
        while (true) {
            long witness;
            try {
                witness = (long) GET_LONG_VOLATILE.invokeExact(o, offset);
                if (witness != expected) {
                    return witness;
                }
                if ((boolean) COMPARE_AND_SET_LONG.invokeExact(o, offset, expected, x)) {
                    return expected;
                }
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    private static byte compatCompareAndExchangeByte(Object o, long offset, byte expected, byte x) {
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
            final int fullWord;
            try {
                fullWord = (int) GET_INT_VOLATILE.invokeExact(o, wordOffset);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }

            final int currentBits = fullWord & mask;
            if (currentBits != expBits) {
                return (byte) (currentBits >>> shift);
            }
            final int updatedWord = (fullWord & ~mask) | newBits;
            final boolean success;
            try {
                success = (boolean) COMPARE_AND_SET_INT.invokeExact(o, wordOffset, fullWord, updatedWord);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
            if (success) {
                return expected;
            }
        }
    }

    private static short compatCompareAndExchangeShort(Object o, long offset, short expected, short x) {
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
            final int fullWord;
            try {
                fullWord = (int) GET_INT_VOLATILE.invokeExact(o, wordOffset);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }

            final int currentBits = fullWord & mask;
            if (currentBits != expBits) {
                return (short) (currentBits >>> shift);
            }

            final int updatedWord = (fullWord & ~mask) | newBits;

            final boolean success;
            try {
                success = (boolean) COMPARE_AND_SET_INT.invokeExact(o, wordOffset, fullWord, updatedWord);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
            if (success) {
                return expected;
            }
        }
    }

    @SuppressWarnings("unused")
    private static boolean compatCompareAndExchangeBoolean(Object o, long offset, boolean expected, boolean x) {
        return compatCompareAndExchangeByte(o, offset, (byte) (expected ? 1 : 0), (byte) (x ? 1 : 0)) != 0;
    }

    @SuppressWarnings("unused")
    private static char compatCompareAndExchangeChar(Object o, long offset, char expected, char x) {
        return (char) compatCompareAndExchangeShort(o, offset, (short) expected, (short) x);
    }

    @SuppressWarnings("unused")
    private static float compatCompareAndExchangeFloat(Object o, long offset, float expected, float x) {
        return Float.intBitsToFloat(compatCompareAndExchangeInt(o, offset, Float.floatToRawIntBits(expected), Float.floatToRawIntBits(x)));
    }

    @SuppressWarnings("unused")
    private static double compatCompareAndExchangeDouble(Object o, long offset, double expected, double x) {
        return Double.longBitsToDouble(compatCompareAndExchangeLong(o, offset, Double.doubleToRawLongBits(expected), Double.doubleToRawLongBits(x)));
    }

    @SuppressWarnings("unused")
    private static boolean compatCompareAndSetBoolean(Object o, long offset, boolean expected, boolean x) {
        return compatCompareAndSetByte(o, offset, (byte) (expected ? 1 : 0), (byte) (x ? 1 : 0));
    }

    private static boolean compatCompareAndSetByte(Object o, long offset, byte expected, byte x) {
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
            final int fullWord;
            try {
                fullWord = (int) GET_INT_VOLATILE.invokeExact(o, wordOffset);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
            final int currentBits = fullWord & mask;
            if (currentBits != expBits) {
                return false;
            }
            final int updatedWord = (fullWord & ~mask) | newBits;
            final boolean success;
            try {
                success = (boolean) COMPARE_AND_SET_INT.invokeExact(o, wordOffset, fullWord, updatedWord);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
            if (success) {
                return true;
            }
        }
    }

    private static boolean compatCompareAndSetShort(Object o, long offset, short expected, short x) {
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
            final int fullWord;
            try {
                fullWord = (int) GET_INT_VOLATILE.invokeExact(o, wordOffset);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
            final int currentBits = fullWord & mask;
            if (currentBits != expBits) {
                return false;
            }
            final int updatedWord = (fullWord & ~mask) | newBits;

            final boolean success;
            try {
                success = (boolean) COMPARE_AND_SET_INT.invokeExact(o, wordOffset, fullWord, updatedWord);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
            if (success) {
                return true;
            }
        }
    }

    @SuppressWarnings("unused")
    private static boolean compatCompareAndSetChar(Object o, long offset, char expected, char x) {
        return compatCompareAndSetShort(o, offset, (short) expected, (short) x);
    }

    @SuppressWarnings("unused")
    private static boolean compatCompareAndSetFloat(Object o, long offset, float expected, float x) {
        try {
            return (boolean) COMPARE_AND_SET_INT.invokeExact(o, offset, Float.floatToRawIntBits(expected), Float.floatToRawIntBits(x));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unused")
    private static boolean compatCompareAndSetDouble(Object o, long offset, double expected, double x) {
        try {
            return (boolean) COMPARE_AND_SET_LONG.invokeExact(o, offset, Double.doubleToRawLongBits(expected), Double.doubleToRawLongBits(x));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unused")
    private static void compatPutByteRelease(Object o, long offset, byte x) {
        try {
            STORE_FENCE.invokeExact();
            PUT_BYTE.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unused")
    private static void compatPutShortRelease(Object o, long offset, short x) {
        try {
            STORE_FENCE.invokeExact();
            PUT_SHORT.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unused")
    private static void compatPutBooleanRelease(Object o, long offset, boolean x) {
        try {
            STORE_FENCE.invokeExact();
            PUT_BOOLEAN.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unused")
    private static void compatPutCharRelease(Object o, long offset, char x) {
        try {
            STORE_FENCE.invokeExact();
            PUT_CHAR.invokeExact(o, offset, x);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unused")
    private static void compatPutFloatRelease(Object o, long offset, float x) {
        try {
            PUT_INT_RELEASE.invokeExact(o, offset, Float.floatToRawIntBits(x));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unused")
    private static void compatPutDoubleRelease(Object o, long offset, double x) {
        try {
            PUT_LONG_RELEASE.invokeExact(o, offset, Double.doubleToRawLongBits(x));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private record UnsafeBinder(MethodHandles.Lookup lookup, Class<?> unsafeClass, Object unsafeInstance) {
        MethodHandle bind(String name, Class<?> rtype, Class<?>... ptypes) throws NoSuchMethodException, IllegalAccessException {
            return lookup.findVirtual(unsafeClass, name, MethodType.methodType(rtype, ptypes)).bindTo(unsafeInstance);
        }

        MethodHandle bindCompat(String compatName, Class<?> rtype, Class<?>... ptypes) throws NoSuchMethodException, IllegalAccessException {
            return lookup.findStatic(UnsafeWrapper.class, compatName, MethodType.methodType(rtype, ptypes));
        }
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
