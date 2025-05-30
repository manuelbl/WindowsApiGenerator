//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd.tables;

import static net.codecrete.windowsapi.winmd.tables.MetadataTables.ASSEMBLY;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.ASSEMBLY_REF;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.EVENT;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.EXPORTED_TYPE;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.FIELD;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.FILE;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.GENERIC_PARAM;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.GENERIC_PARAM_CONSTRAINT;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.INTERFACE_IMPL;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.MANIFEST_RESOURCE;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.MEMBER_REF;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.METHOD_DEF;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.METHOD_SPEC;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.MODULE;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.MODULE_REF;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.PARAM;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.PROPERTY;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.STAND_ALONE_SIG;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.TYPE_DEF;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.TYPE_REF;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.TYPE_SPEC;

/**
 * Tables included in coded indexes.
 * <p>
 * See ECMA-335, II.24.2.6 "#~ stream"
 * </p>
 */
@SuppressWarnings("java:S2386")
public class CodedIndexes {
    private CodedIndexes() {
    }

    private static final int UNUSED_TABLE = 63;

    /**
     * Tables for in {@code TypeDefOrRef} coded index.
     */
    public static final int[] TYPE_DEF_OR_REF_TABLES = new int[]{
            TYPE_DEF,
            TYPE_REF,
            TYPE_SPEC
    };
    /**
     * Tables for in {@code HasConstant} coded index.
     */
    public static final int[] HAS_CONSTANT_TABLES = new int[]{
            FIELD,
            PARAM,
            PROPERTY
    };
    /**
     * Tables for in {@code HasCustomAttribute} coded index.
     */
    public static final int[] HAS_CUSTOM_ATTRIBUTE_TABLES = new int[]{
            METHOD_DEF,
            FIELD,
            TYPE_REF,
            TYPE_DEF,
            PARAM,
            INTERFACE_IMPL,
            MEMBER_REF,
            MODULE,
            UNUSED_TABLE, // MetadataTables.PERMISSION,
            PROPERTY,
            EVENT,
            STAND_ALONE_SIG,
            MODULE_REF,
            TYPE_SPEC,
            ASSEMBLY,
            ASSEMBLY_REF,
            FILE,
            EXPORTED_TYPE,
            MANIFEST_RESOURCE,
            GENERIC_PARAM,
            GENERIC_PARAM_CONSTRAINT,
            METHOD_SPEC
    };
    /**
     * Tables for in {@code FieldMarshal} coded index.
     */
    public static final int[] HAS_FIELD_MARSHAL_TABLES = new int[]{
            FIELD,
            PARAM
    };
    /**
     * Tables for in {@code HasDeclSecurity} coded index.
     */
    public static final int[] HAS_DECL_SECURITY_TABLES = new int[]{
            TYPE_DEF,
            METHOD_DEF,
            ASSEMBLY
    };
    /**
     * Tables for in {@code MemberRefParent} coded index.
     */
    public static final int[] MEMBER_REF_PARENT_TABLES = new int[]{
            TYPE_DEF,
            TYPE_REF,
            MODULE_REF,
            METHOD_DEF,
            TYPE_SPEC
    };
    /**
     * Tables for in {@code HasSemantics} coded index.
     */
    public static final int[] HAS_SEMANTICS_TABLES = new int[]{
            EVENT,
            PROPERTY
    };
    /**
     * Tables for in {@code MethodDefOrRef} coded index.
     */
    public static final int[] METHOD_DEF_OR_REF_TABLES = new int[]{
            METHOD_DEF,
            MEMBER_REF
    };
    /**
     * Tables for in {@code MemberForwarded} coded index.
     */
    public static final int[] MEMBER_FORWARDED_TABLES = new int[]{
            FIELD,
            METHOD_DEF
    };
    /**
     * Tables for in {@code Implementation} coded index.
     */
    public static final int[] IMPLEMENTATION_TABLES = new int[]{
            FILE,
            ASSEMBLY_REF,
            EXPORTED_TYPE
    };
    /**
     * Tables for in {@code CustomAttributeType} coded index.
     */
    public static final int[] CUSTOM_ATTRIBUTE_TYPE_TABLES = new int[]{
            UNUSED_TABLE,
            UNUSED_TABLE,
            METHOD_DEF,
            MEMBER_REF,
            UNUSED_TABLE
    };
    /**
     * Tables for in {@code ResolutionScope} coded index.
     */
    public static final int[] RESOLUTION_SCOPE_TABLES = new int[]{
            MODULE,
            MODULE_REF,
            ASSEMBLY_REF,
            TYPE_REF
    };
    /**
     * Tables for in {@code TypeOrMethodDef} coded index.
     */
    public static final int[] TYPE_OR_METHOD_DEF_TABLES = new int[]{
            TYPE_DEF,
            METHOD_DEF
    };
}
