//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd.tables;

/**
 * Row of the "NestedClass" table.
 * <p>
 * See ECMA-335, II.22.32 NestedClass
 * </p>
 *
 * @param nestedClass    nested class (TypeAlias index)
 * @param enclosingClass enclosing class (TypeAlias index)
 */
public record NestedClass(int nestedClass, int enclosingClass) {
}
