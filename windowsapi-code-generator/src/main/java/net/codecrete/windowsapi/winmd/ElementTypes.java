//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd;

/**
 * Element type used in signatures.
 * <p>
 * See ECMA-335, II.23.1.16 Element types used in signatures
 * </p>
 */
@SuppressWarnings("unused")
public class ElementTypes {
    private ElementTypes() {
    }

    static final int END = 0x00; // Marks end of a list
    static final int VOID = 0x01; //
    static final int BOOLEAN = 0x02; //
    static final int CHAR = 0x03; //
    static final int I1 = 0x04; //
    static final int U1 = 0x05; //
    static final int I2 = 0x06; //
    static final int U2 = 0x07; //
    static final int I4 = 0x08; //
    static final int U4 = 0x09; //
    static final int I8 = 0x0a; //
    static final int U8 = 0x0b; //
    static final int R4 = 0x0c; //
    static final int R8 = 0x0d; //
    static final int STRING = 0x0e; //
    static final int PTR = 0x0f; // Followed by type
    static final int BYREF = 0x10; // Followed by type
    static final int VALUETYPE = 0x11; // Followed by TypeAlias or TypeRef token
    static final int CLASS = 0x12; // Followed by TypeAlias or TypeRef token
    static final int VAR = 0x13; // Generic parameter in a generic type definition, represented as number (compressed
    // unsigned integer)
    static final int ARRAY = 0x14; // type rank boundsCount bound1 … loCount lo1 …
    static final int GENERICINST = 0x15; // Generic type instantiation. Followed by type type-arg-count type-1 ...
    // type-n
    static final int TYPEDBYREF = 0x16; //
    // not used
    static final int I = 0x18; // System.IntPtr
    static final int U = 0x19; // System.UIntPtr
    // not used
    static final int FNPTR = 0x1b; // Followed by full method signature
    static final int OBJECT = 0x1c; // System.Object
    static final int SZARRAY = 0x1d; // Single-dim array with 0 lower bound
    static final int MVAR = 0x1e; // Generic parameter in a generic method definition, represented as number
    // (compressed unsigned integer)
    static final int CMOD_REQD = 0x1f; // Required modifier : followed by a TypeAlias or TypeRef token
    static final int CMOD_OPT = 0x20; // Optional modifier : followed by a TypeAlias or TypeRef token
    static final int INTERNAL = 0x21; // Implemented within the CLI

    static final int MODIFIER = 0x40; // Or’d with following element types
    static final int SENTINEL = 0x41; // Sentinel for vararg method signature
    static final int PINNED = 0x45; // Denotes a local variable that points at a pinned object

    static final int SYSTEM_TYPE = 0x50; // Indicates an argument of type System.Type.
    static final int BOXED_OBJECT = 0x51; // Used in custom attributes to specify a boxed object (§II.23.3).
    // 0x52 Reserved
    static final int FIELD = 0x53; // Used in custom attributes to indicate a FIELD (§II.22.10, II.23.3).
    static final int PROPERTY = 0x54; // Used in custom attributes to indicate a PROPERTY (§II.22.10, II.23.3).
    static final int ENUM = 0x55; // Used in custom attributes to specify an enum (§II.23.3).
}
