//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd.tables;

/**
 * Row of the "Field" table.
 * <p>
 * See ECMA-335, II.22.15 Field: 0x04
 * </p>
 *
 * @param index     row index
 * @param flags     field attributes (FieldAttributes)
 * @param name      member name (string index)
 * @param signature member signature (blob index)
 */
@SuppressWarnings("unused")
public record Field(int index, int flags, int name, int signature) {

    public static final int FIELD_ACCESS_MASK = 0x0007;
    public static final int COMPILER_CONTROLLED = 0x0000;
    public static final int PRIVATE = 0x0001;
    public static final int FAM_AND_ASSEM = 0x0002;
    public static final int ASSEMBLY = 0x0003;
    public static final int FAMILY = 0x0004;
    public static final int FAM_OR_ASSEM = 0x0005;
    public static final int PUBLIC = 0x0006;
    public static final int STATIC = 0x0010;
    public static final int INIT_ONLY = 0x0020;
    public static final int LITERAL = 0x0040;
    public static final int NOT_SERIALIZED = 0x0080;
    public static final int SPECIAL_NAME = 0x0200;

    public static final int P_INVOKE_IMPL = 0x2000;

    public static final int RT_SPECIAL_NAME = 0x0400;
    public static final int HAS_FIELD_MARSHAL = 0x1000;
    public static final int HAS_DEFAULT = 0x8000;
    public static final int HAS_FIELD_RVA = 0x0100;
}
