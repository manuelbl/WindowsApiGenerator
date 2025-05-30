//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd.tables;

/**
 * Row of the "Param" table.
 * <p>
 * See ECMA-335, II.22.33 Param: 0x08
 * </p>
 *
 * @param index    row index
 * @param flags    parameter attributes (ParamAttributes)
 * @param sequence sequence number
 * @param name     member name (string index)
 */
@SuppressWarnings("unused")
public record Param(int index, int flags, int sequence, int name) {

    public static final int IN = 0x0001; // Param is [In]
    public static final int OUT = 0x0002; // Param is [out]
    public static final int OPTIONAL = 0x0010; // Param is optional
    public static final int HAS_DEFAULT = 0x1000; // Param has default value
    public static final int HAS_FIELD_MARSHAL = 0x2000; // Param has FieldMarshal
}
