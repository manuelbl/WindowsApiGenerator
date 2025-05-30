//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd.tables;

/**
 * Row of the "FieldLayout" table.
 * <p>
 * See ECMA-335, II.22.16 FieldLayout: 0x10
 * </p>
 *
 * @param offset offset
 * @param field  field (Field index)
 */
public record FieldLayout(int offset, int field) {
}
