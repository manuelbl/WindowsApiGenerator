//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.metadata;

/**
 * Enumeration of all primitive types.
 */
public enum PrimitiveKind {
    /**
     * Void.
     * <p>
     * Either used to represent the return type of functions not returning anything,
     * or for pointer types pointing to an unknown/opaque data structure.
     * </p>
     */
    VOID("void"),
    /**
     * Boolean.
     */
    BOOL("bool"),
    /**
     * Character.
     * <p>
     * A character is a UTF-16 code unit, i.e., it is 2 bytes long.
     * </p>
     */
    CHAR("Char"),
    /**
     * Signed byte.
     */
    SBYTE("SByte"),
    /**
     * Unsigned byte.
     */
    BYTE("Byte"),
    /**
     * Signed 16-bit integer.
     */
    INT16("Int16"),
    /**
     * Unsigned 16-bit integer.
     */
    UINT16("UInt16"),
    /**
     * Signed 32-bit integer.
     */
    INT32("Int32"),
    /**
     * Unsigned 32-bit integer.
     */
    UINT32("UInt32"),
    /**
     * Signed 64-bit integer.
     */
    INT64("Int64"),
    /**
     * Unsigned 64-bit integer.
     */
    UINT64("UInt64"),
    /**
     * Single-precision floating-point number (4 bytes long).
     */
    SINGLE("Single"),
    /**
     * Double-precision floating-point number (8 bytes long).
     */
    DOUBLE("Double"),
    /**
     * String.
     * <p>
     * Usually, the string is encoded in UTF-16.
     * Some string constants are indicated to be ANSI-encoded,
     * i.e., it uses a byte-wise encoding.
     * </p>
     */
    STRING("String"),
    /**
     * A signed integer sufficiently large to hold a pointer.
     */
    INT_PTR("IntPtr"),
    /**
     * An unsigned integer sufficiently large to hold a pointer.
     */
    UINT_PTR("UIntPtr");

    private final String name;

    PrimitiveKind(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the primitive.
     *
     * @return the name
     */
    public String typeName() {
        return name;
    }
}
