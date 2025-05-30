//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd.tables;

import static net.codecrete.windowsapi.winmd.tables.CodedIndexes.TYPE_DEF_OR_REF_TABLES;

/**
 * Row of the "InterfaceImpl" table.
 * <p>
 * See ECMA-335, II.22.23 InterfaceImpl: 0x09
 * </p>
 *
 * @param classIndex     type definition (index into TypeDef table)
 * @param interfaceIndex implemented interface (TypeDefOrRef coded index)
 */
public record InterfaceImpl(int classIndex, int interfaceIndex) {
    /**
     * Gets the interfaces as a TypDefOrRef coded index
     *
     * @return CodedIndex instance
     */
    public CodedIndex interfaceTypeDefOrRef() {
        return CodedIndex.decode(interfaceIndex, TYPE_DEF_OR_REF_TABLES);
    }
}
