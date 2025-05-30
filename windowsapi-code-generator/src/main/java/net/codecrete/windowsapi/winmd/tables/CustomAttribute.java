//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd.tables;

import static net.codecrete.windowsapi.winmd.tables.CodedIndexes.CUSTOM_ATTRIBUTE_TYPE_TABLES;

/**
 * Row of the "CustomAttribute" table.
 * <p>
 * See ECMA-335, II.22.10 CustomAttribute
 * </p>
 *
 * @param parent      parent annotated with this attribute (HasCustomAttribute coded index)
 * @param constructor constructor method (CustomAttributeType coded index)
 * @param value       attribute value (blob index)
 */
public record CustomAttribute(int parent, int constructor, int value) {

    /**
     * Coded index of the constructor method (MethodDefOrRef).
     *
     * @return coded index
     */
    public CodedIndex constructorIndex() {
        return CodedIndex.decode(constructor, CUSTOM_ATTRIBUTE_TYPE_TABLES);
    }
}
