//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd.tables;

/**
 * Index of metadata tables.
 * <p>
 * See ECMA-335, II.22 Metadata logical format: tables
 * </p>
 */
public class MetadataTables {

    private MetadataTables() {
    }

    public static final int MODULE = 0x00;
    public static final int TYPE_REF = 0x01;
    public static final int TYPE_DEF = 0x02;
    public static final int FIELD = 0x04;
    public static final int METHOD_DEF = 0x06;
    public static final int PARAM = 0x08;
    public static final int INTERFACE_IMPL = 0x09;
    public static final int MEMBER_REF = 0x0A;
    public static final int CONSTANT = 0x0B;
    public static final int CUSTOM_ATTRIBUTE = 0x0C;
    public static final int FIELD_MARSHAL = 0x0D;
    public static final int DECL_SECURITY = 0x0E;
    public static final int CLASS_LAYOUT = 0x0F;
    public static final int FIELD_LAYOUT = 0x10;
    public static final int STAND_ALONE_SIG = 0x11;
    public static final int EVENT_MAP = 0x12;
    public static final int EVENT = 0x14;
    public static final int PROPERTY_MAP = 0x15;
    public static final int PROPERTY = 0x17;
    public static final int METHOD_SEMANTICS = 0x18;
    public static final int METHOD_IMPL = 0x19;
    public static final int MODULE_REF = 0x1A;
    public static final int TYPE_SPEC = 0x1B;
    public static final int IMPL_MAP = 0x1C;
    public static final int FIELD_RVA = 0x1D;
    public static final int ASSEMBLY = 0x20;
    public static final int ASSEMBLY_PROCESSOR = 0x21;
    public static final int ASSEMBLY_OS = 0x22;
    public static final int ASSEMBLY_REF = 0x23;
    public static final int ASSEMBLY_REF_PROCESSOR = 0x24;
    public static final int ASSEMBLY_REF_OS = 0x25;
    public static final int FILE = 0x26;
    public static final int EXPORTED_TYPE = 0x27;
    public static final int MANIFEST_RESOURCE = 0x28;
    public static final int NESTED_CLASS = 0x29;
    public static final int GENERIC_PARAM = 0x2A;
    public static final int METHOD_SPEC = 0x2B;
    public static final int GENERIC_PARAM_CONSTRAINT = 0x2C;
}
