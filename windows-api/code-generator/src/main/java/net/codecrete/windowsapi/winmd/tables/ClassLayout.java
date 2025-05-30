//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd.tables;

/**
 * Row of the "ClassLayout" table.
 * <p>
 * See ECMA-335, II.22.8 ClassLayout: 0x0F
 * </p>
 *
 * @param packingSize packingSize
 * @param classSize   class size
 * @param parent      parent (TypeDef index)
 */
public record ClassLayout(int packingSize, int classSize, int parent) {
}
