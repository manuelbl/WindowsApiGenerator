//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd.tables;

import static net.codecrete.windowsapi.winmd.tables.CodedIndexes.RESOLUTION_SCOPE_TABLES;

/**
 * Row of the "TypeRef" table.
 * <p>
 * See ECMA-335, II.22.38 TypeRef
 * </p>
 *
 * @param resolutionScope scope for resolving reference (ResolutionScope coded index)
 * @param typeName        type name (string index)
 * @param typeNamespace   type namespace (string index)
 */
public record TypeRef(int resolutionScope, int typeName, int typeNamespace) {

    /**
     * Gets the resolution scoped as a coded index.
     *
     * @return coded index instance
     */
    public CodedIndex resolutionScopeIndex() {
        return CodedIndex.decode(resolutionScope, RESOLUTION_SCOPE_TABLES);
    }
}
