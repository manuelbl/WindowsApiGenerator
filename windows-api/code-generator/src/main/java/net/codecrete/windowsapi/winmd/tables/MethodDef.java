//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd.tables;

/**
 * Row of the "MethodDef" table.
 * <p>
 * See ECMA-335, II.22.26 MethodDef: 0x06
 * </p>
 *
 * @param index      row index (MethodDef index)
 * @param rva        rva
 * @param implFlags  method implementation attributes (MethodImplAttributes)
 * @param flags      method attributes (MethodAttributes)
 * @param name       member name (string index)
 * @param signature  member signature (blob index)
 * @param firstParam first parameter of parameter list (Param index)
 */
public record MethodDef(int index, int rva, int implFlags, int flags, int name, int signature, int firstParam) {
}
