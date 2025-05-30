//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd.tables;

import static net.codecrete.windowsapi.winmd.tables.CodedIndexes.TYPE_DEF_OR_REF_TABLES;

/**
 * Row of the "TypeAlias" table.
 * <p>
 * See ECMA-335, II.22.37 TypeAlias
 * </p>
 *
 * @param typeAttributes type attributes
 * @param typeName       type name (string index)
 * @param typeNamespace  type namespace (string index)
 * @param extendsType    type extended by this instance (TypeDefOrRef coded index)
 * @param firstField     first field (Field index)
 * @param firstMethod    first method (Method index)
 */
@SuppressWarnings("unused")
public record TypeDef(
        int typeAttributes,
        int typeName,
        int typeNamespace,
        int extendsType,
        int firstField,
        int firstMethod
) {
    /**
     * Coded index of the extended type (TypeDefOrRef).
     *
     * @return coded index
     */
    public CodedIndex extendsTypeIndex() {
        return CodedIndex.decode(extendsType, TYPE_DEF_OR_REF_TABLES);
    }

    public static final int VISIBILITY_MASK = 0x00000007;
    public static final int VISIBILITY_PUBLIC = 0x00000001;
    public static final int VISIBILITY_NESTED_PUBLIC = 0x00000002;

    public static final int LAYOUT_MASK = 0x00000018;
    public static final int LAYOUT_SEQUENTIAL = 0x00000008;
    public static final int LAYOUT_EXPLICIT = 0x00000010;

    public static final int CLASS_SEMANTICS_MASK = 0x00000020;
    public static final int CLASS_SEMANTICS_CLASS = 0x00000000;
    public static final int CLASS_SEMANTICS_INTERFACE = 0x00000020;

    public static final int ABSTRACT = 0x00000080;
}
