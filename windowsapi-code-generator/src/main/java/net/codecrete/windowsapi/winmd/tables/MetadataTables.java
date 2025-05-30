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

    /**
     * Table {@code Module}.
     */
    public static final int MODULE = 0x00;
    /**
     * Table {@code TypeRef}.
     */
    public static final int TYPE_REF = 0x01;
    /**
     * Table {@code TypeDef}.
     */
    public static final int TYPE_DEF = 0x02;
    /**
     * Table {@code Field}.
     */
    public static final int FIELD = 0x04;
    /**
     * Table {@code MethodDef}.
     */
    public static final int METHOD_DEF = 0x06;
    /**
     * Table {@code Param}.
     */
    public static final int PARAM = 0x08;
    /**
     * Table {@code InterfaceImpl}.
     */
    public static final int INTERFACE_IMPL = 0x09;
    /**
     * Table {@code MemberRef}.
     */
    public static final int MEMBER_REF = 0x0A;
    /**
     * Table {@code Constant}.
     */
    public static final int CONSTANT = 0x0B;
    /**
     * Table {@code CustomAttribute}.
     */
    public static final int CUSTOM_ATTRIBUTE = 0x0C;
    /**
     * Table {@code FieldMarshal}.
     */
    public static final int FIELD_MARSHAL = 0x0D;
    /**
     * Table {@code DeclSecurity}.
     */
    public static final int DECL_SECURITY = 0x0E;
    /**
     * Table {@code ClassLayout}.
     */
    public static final int CLASS_LAYOUT = 0x0F;
    /**
     * Table {@code FieldLayout}.
     */
    public static final int FIELD_LAYOUT = 0x10;
    /**
     * Table {@code StandAloneSig}.
     */
    public static final int STAND_ALONE_SIG = 0x11;
    /**
     * Table {@code EventMap}.
     */
    public static final int EVENT_MAP = 0x12;
    /**
     * Table {@code Event}.
     */
    public static final int EVENT = 0x14;
    /**
     * Table {@code PropertyMap}.
     */
    public static final int PROPERTY_MAP = 0x15;
    /**
     * Table {@code Property}.
     */
    public static final int PROPERTY = 0x17;
    /**
     * Table {@code MethodSemantics}.
     */
    public static final int METHOD_SEMANTICS = 0x18;
    /**
     * Table {@code MethodImpl}.
     */
    public static final int METHOD_IMPL = 0x19;
    /**
     * Table {@code ModuleRef}.
     */
    public static final int MODULE_REF = 0x1A;
    /**
     * Table {@code TypeSpec}.
     */
    public static final int TYPE_SPEC = 0x1B;
    /**
     * Table {@code ImplMap}.
     */
    public static final int IMPL_MAP = 0x1C;
    /**
     * Table {@code FieldRVA}.
     */
    public static final int FIELD_RVA = 0x1D;
    /**
     * Table {@code Assembly}.
     */
    public static final int ASSEMBLY = 0x20;
    /**
     * Table {@code AssemblyProcessor}.
     */
    public static final int ASSEMBLY_PROCESSOR = 0x21;
    /**
     * Table {@code AssemblyOS}.
     */
    public static final int ASSEMBLY_OS = 0x22;
    /**
     * Table {@code AssemblyRef}.
     */
    public static final int ASSEMBLY_REF = 0x23;
    /**
     * Table {@code AssemblyRefProcessor}.
     */
    public static final int ASSEMBLY_REF_PROCESSOR = 0x24;
    /**
     * Table {@code AssemblyRefOS}.
     */
    public static final int ASSEMBLY_REF_OS = 0x25;
    /**
     * Table {@code File}.
     */
    public static final int FILE = 0x26;
    /**
     * Table {@code ExportedType}.
     */
    public static final int EXPORTED_TYPE = 0x27;
    /**
     * Table {@code ManifestResource}.
     */
    public static final int MANIFEST_RESOURCE = 0x28;
    /**
     * Table {@code NestedClass}.
     */
    public static final int NESTED_CLASS = 0x29;
    /**
     * Table {@code GenericParam}.
     */
    public static final int GENERIC_PARAM = 0x2A;
    /**
     * Table {@code MethodSpec}.
     */
    public static final int METHOD_SPEC = 0x2B;
    /**
     * Table {@code GenericParamConstraint}.
     */
    public static final int GENERIC_PARAM_CONSTRAINT = 0x2C;
}
