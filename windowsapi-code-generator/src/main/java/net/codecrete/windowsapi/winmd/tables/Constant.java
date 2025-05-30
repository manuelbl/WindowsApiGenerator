//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd.tables;

/**
 * Row of the "Constant" table.
 * <p>
 * See ECMA-335, II.22.9 Constant: 0x0B
 * </p>
 *
 * @param type   data type (ELEMENT_TYPE_XX)
 * @param parent parent (HasConstant coded index)
 * @param value  constant value (blob index)
 */
public record Constant(int type, int parent, int value) {
}
