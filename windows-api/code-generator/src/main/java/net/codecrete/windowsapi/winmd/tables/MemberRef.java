//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd.tables;

import static net.codecrete.windowsapi.winmd.tables.CodedIndexes.MEMBER_REF_PARENT_TABLES;

/**
 * Row of the "MemberRef" table.
 * <p>
 * See ECMA-335, II.22.25 MemberRef
 * </p>
 *
 * @param parent    parent class (MemberRefParent coded index)
 * @param name      member name (string index)
 * @param signature member signature (blob index)
 */
public record MemberRef(int parent, int name, int signature) {

    /**
     * Returns the class the member belongs to (MemberRefParent coded index)
     *
     * @return coded index
     */
    public CodedIndex parentIndex() {
        return CodedIndex.decode(parent, MEMBER_REF_PARENT_TABLES);
    }
}
