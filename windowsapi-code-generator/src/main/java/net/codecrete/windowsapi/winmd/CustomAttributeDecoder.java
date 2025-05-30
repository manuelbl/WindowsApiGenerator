//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd;

import net.codecrete.windowsapi.metadata.ArgumentValue;
import net.codecrete.windowsapi.metadata.Array;
import net.codecrete.windowsapi.metadata.EnumType;
import net.codecrete.windowsapi.metadata.LazyString;
import net.codecrete.windowsapi.metadata.Primitive;
import net.codecrete.windowsapi.metadata.QualifiedName;
import net.codecrete.windowsapi.metadata.Type;
import net.codecrete.windowsapi.winmd.tables.CodedIndex;
import net.codecrete.windowsapi.winmd.tables.CustomAttribute;
import net.codecrete.windowsapi.winmd.tables.MemberRef;

import java.util.Set;
import java.util.UUID;

import static net.codecrete.windowsapi.winmd.tables.CodedIndexes.HAS_CUSTOM_ATTRIBUTE_TABLES;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.FIELD;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.MEMBER_REF;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.METHOD_DEF;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.TYPE_DEF;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.TYPE_REF;

/**
 * Decodes custom attributes (similar to Java annotations).
 * <p>
 * Custom attributes are used to provide additional metadata about types, methods, etc.
 * </p>
 * <p>
 * Only custom attributes relevant for this software are decoded.
 * </p>
 */
@SuppressWarnings("java:S1192")
class CustomAttributeDecoder extends Decoder {
    private static final String SYSTEM = "System";
    private static final String METADATA = "Windows.Win32.Foundation.Metadata";
    private static final QualifiedName FLAGS_ATTRIBUTE = new QualifiedName(SYSTEM, "FlagsAttribute");
    private static final QualifiedName OBSOLETE_ATTRIBUTE = new QualifiedName(SYSTEM, "ObsoleteAttribute");
    private static final QualifiedName CONSTANT_ATTRIBUTE = new QualifiedName(METADATA, "ConstantAttribute");
    private static final QualifiedName DOCUMENTATION_ATTRIBUTE = new QualifiedName(METADATA, "DocumentationAttribute");
    private static final QualifiedName FLEXIBLE_ARRAY_ATTRIBUTE = new QualifiedName(METADATA, "FlexibleArrayAttribute");
    private static final QualifiedName GUID_ATTRIBUTE = new QualifiedName(METADATA, "GuidAttribute");
    private static final QualifiedName NATIVE_ENCODING_ATTRIBUTE = new QualifiedName(METADATA,
            "NativeEncodingAttribute");
    private static final QualifiedName NATIVE_TYPEDEF_ATTRIBUTE = new QualifiedName(METADATA, "NativeTypedefAttribute");
    private static final QualifiedName STRUCT_SIZE_FIELD_ATTRIBUTE = new QualifiedName(METADATA,
            "StructSizeFieldAttribute");
    private static final QualifiedName SUPPORTED_ARCHITECTURE_ATTRIBUTE = new QualifiedName(METADATA,
            "SupportedArchitectureAttribute");

    /**
     * Set of relevant custom attributes.
     */
    private static final Set<QualifiedName> relevantAttributes = Set.of(
            CONSTANT_ATTRIBUTE,
            FLAGS_ATTRIBUTE,
            OBSOLETE_ATTRIBUTE,
            DOCUMENTATION_ATTRIBUTE,
            FLEXIBLE_ARRAY_ATTRIBUTE,
            GUID_ATTRIBUTE,
            NATIVE_ENCODING_ATTRIBUTE,
            NATIVE_TYPEDEF_ATTRIBUTE,
            STRUCT_SIZE_FIELD_ATTRIBUTE,
            SUPPORTED_ARCHITECTURE_ATTRIBUTE
    );

    /**
     * Set of ignored attributes.
     */
    private static final Set<QualifiedName> ignoredAttributes = Set.of(
            new QualifiedName(SYSTEM, "AttributeUsageAttribute"),
            new QualifiedName("System.Diagnostics.CodeAnalysis", "DoesNotReturnAttribute"),
            new QualifiedName("System.Runtime.InteropServices", "ComVisibleAttribute"),
            new QualifiedName("System.Runtime.InteropServices", "UnmanagedFunctionPointerAttribute"),
            new QualifiedName(METADATA, "AgileAttribute"),
            new QualifiedName(METADATA, "AlsoUsableForAttribute"),
            new QualifiedName(METADATA, "AnsiAttribute"),
            new QualifiedName(METADATA, "AssociatedConstantAttribute"),
            new QualifiedName(METADATA, "AssociatedEnumAttribute"),
            new QualifiedName(METADATA, "CanReturnErrorsAsSuccessAttribute"),
            new QualifiedName(METADATA, "CanReturnMultipleSuccessValuesAttribute"),
            new QualifiedName(METADATA, "ConstAttribute"),
            new QualifiedName(METADATA, "InvalidHandleValueAttribute"),
            new QualifiedName(METADATA, "MetadataTypedefAttribute"),
            new QualifiedName(METADATA, "NativeArrayInfoAttribute"),
            new QualifiedName(METADATA, "NativeBitfieldAttribute"),
            new QualifiedName(METADATA, "NotNullTerminatedAttribute"),
            new QualifiedName(METADATA, "NullNullTerminatedAttribute"),
            new QualifiedName(METADATA, "RAIIFreeAttribute"),
            new QualifiedName(METADATA, "ScopedEnumAttribute"),
            new QualifiedName(METADATA, "SupportedOSPlatformAttribute"),
            new QualifiedName(METADATA, "UnicodeAttribute")
    );

    private final MetadataFile metadataFile;
    private final Primitive stringType;

    /**
     * Creates a new custom attribute decoder.
     *
     * @param typeLookup   an instance for providing type information
     * @param metadataFile the metadata file to retrieve additional information
     */
    CustomAttributeDecoder(TypeLookup typeLookup, MetadataFile metadataFile) {
        super(typeLookup);
        this.metadataFile = metadataFile;
        this.stringType = typeLookup.getPrimitiveType(ElementTypes.STRING);
    }

    /**
     * Gets the custom attribute data for the given {@code TypeDef} index.
     *
     * @param typeDef the {@code TypeDef} index
     * @return the custom attribute data
     */
    CustomAttributeData getTypeDefAttributes(int typeDef) {
        var typeDefIndex = CodedIndex.encode(TYPE_DEF, typeDef, HAS_CUSTOM_ATTRIBUTE_TABLES);
        return getAttributes(typeDefIndex);
    }

    /**
     * Gets the custom attribute data for the given {@code MethodDef} index.
     *
     * @param methodDef the {@code MethodDef} index
     * @return the custom attribute data
     */
    CustomAttributeData getMethodDefAttributes(int methodDef) {
        var methodDefIndex = CodedIndex.encode(METHOD_DEF, methodDef, HAS_CUSTOM_ATTRIBUTE_TABLES);
        return getAttributes(methodDefIndex);
    }

    /**
     * Gets the custom attribute data for the given {@code Field} index.
     *
     * @param field the {@code Field} index
     * @return the custom attribute data
     */
    CustomAttributeData getFieldAttributes(int field) {
        var fieldIndex = CodedIndex.encode(FIELD, field, HAS_CUSTOM_ATTRIBUTE_TABLES);
        return getAttributes(fieldIndex);
    }

    private CustomAttributeData getAttributes(int hasCustomAttributeIndex) {
        var data = new CustomAttributeData();

        for (var customAttribute : metadataFile.getCustomAttributes(hasCustomAttributeIndex)) {
            var constructor = customAttribute.constructorIndex();
            assert constructor.table() == MEMBER_REF;
            assert constructor.index() != 0;
            var memberRef = metadataFile.getMemberRef(constructor.index());
            var parent = memberRef.parentIndex();
            assert parent.table() == TYPE_REF;
            assert parent.index() != 0;
            var typeRef = metadataFile.getTypeRef(parent.index());
            var qualifiedName = new QualifiedName(
                    metadataFile.getString(typeRef.typeNamespace()),
                    metadataFile.getString(typeRef.typeName())
            );

            if (ignoredAttributes.contains(qualifiedName))
                continue;
            assert relevantAttributes.contains(qualifiedName);

            if (qualifiedName.equals(FLAGS_ATTRIBUTE)) {
                data.isEnumFlags = true;
            } else if (qualifiedName.equals(OBSOLETE_ATTRIBUTE)) {
                data.isObsolete = true;
            } else if (qualifiedName.equals(NATIVE_TYPEDEF_ATTRIBUTE)) {
                data.isTypedef = true;
            } else if (qualifiedName.equals(DOCUMENTATION_ATTRIBUTE)) {
                data.documentationUrl = getLazyString(customAttribute, memberRef);
            } else if (qualifiedName.equals(SUPPORTED_ARCHITECTURE_ATTRIBUTE)) {
                var value = getValue(customAttribute, memberRef);
                data.supportedArchitecture = ((Number) value.fixedArguments()[0].value()).intValue();
            } else if (qualifiedName.equals(GUID_ATTRIBUTE)) {
                var value = getValue(customAttribute, memberRef);
                data.guidConstant = createGuidConstant(value);
            } else if (qualifiedName.equals(CONSTANT_ATTRIBUTE)) {
                var value = getValue(customAttribute, memberRef);
                data.constantValue = value.fixedArguments()[0].value();
            } else if (qualifiedName.equals(NATIVE_ENCODING_ATTRIBUTE)) {
                var value = getValue(customAttribute, memberRef);
                data.isAnsiEncoding = value.fixedArguments()[0].value().equals("ansi");
            } else if (qualifiedName.equals(FLEXIBLE_ARRAY_ATTRIBUTE)) {
                data.isFlexibleArray = true;
            } else if (qualifiedName.equals(STRUCT_SIZE_FIELD_ATTRIBUTE)) {
                var value = getValue(customAttribute, memberRef);
                data.structSizeField = (String) value.fixedArguments()[0].value();
            }
        }

        return data;
    }

    private CustomAttributeValue getValue(CustomAttribute customAttribute, MemberRef memberRef) {
        var methodSignature = decodeMethodRefSignature(metadataFile.getBlob(memberRef.signature()));
        var valueBlob = metadataFile.getBlob(customAttribute.value());
        return decodeCustomAttributeValue(methodSignature, valueBlob);
    }

    /**
     * Decodes a {@code MethodRef} signature.
     *
     * @param signature the BLOB containing the signature
     * @return the decoded signature
     */
    private MethodSignature decodeMethodRefSignature(Blob signature) {
        // See ECMA-335, II.23.2.2
        signature.readByte();
        var paramCount = signature.readCompressedUnsignedInt();
        var returnType = decodeType(signature, null);
        var params = new Type[paramCount];
        for (int i = 0; i < paramCount; i++) {
            params[i] = decodeType(signature, null);
            assert params[i] != null; // no support for variable arguments
        }

        assert signature.isAtEnd();
        return new MethodSignature(returnType, params);
    }

    /**
     * Gets a lazily decoded string.
     * <p>
     * This method only works if this member has a single fixed attribute of type string.
     * </p>
     *
     * @param customAttribute custom attribute
     * @param memberRef       member reference (of value constructor)
     * @return lazily decoded string
     */
    private LazyString getLazyString(CustomAttribute customAttribute, MemberRef memberRef) {
        var methodSignature = decodeMethodRefSignature(metadataFile.getBlob(memberRef.signature()));
        assert methodSignature.paramTypes().length == 1;
        assert methodSignature.paramTypes()[0] == stringType;
        var valueBlob = metadataFile.getBlob(customAttribute.value());
        var value = decodeSingleStringValue(valueBlob);
        int numNamedArgs = valueBlob.readUInt16();
        assert numNamedArgs == 0;
        assert valueBlob.isAtEnd();
        return value;
    }

    CustomAttributeValue decodeCustomAttributeValue(MethodSignature methodSignature, Blob valueBlob) {
        // See ECMA-335, II.22.10 CustomAttribute: 0x0C and II.23.3 Custom attributes
        int prolog = valueBlob.readUInt16();
        assert prolog == 1;

        // fixed arguments
        var fixedParamTypes = methodSignature.paramTypes();
        int numFixedArgs = fixedParamTypes.length;
        ArgumentValue[] fixedArguments = new ArgumentValue[numFixedArgs];
        for (int i = 0; i < numFixedArgs; i += 1) {
            assert !(fixedParamTypes[i] instanceof Array);
            fixedArguments[i] = new ArgumentValue(fixedParamTypes[i], null, readElem(valueBlob, fixedParamTypes[i]));
        }

        // named arguments
        int numNamedArgs = valueBlob.readUInt16();
        assert numNamedArgs == 0;

        assert valueBlob.isAtEnd();
        return new CustomAttributeValue(fixedArguments);
    }

    private LazyString decodeSingleStringValue(Blob valueBlob) {
        int prolog = valueBlob.readUInt16();
        assert prolog == 1;
        int len = valueBlob.readCompressedUnsignedInt();
        int offset = valueBlob.offset();
        valueBlob.skip(len);
        return new LazyString(valueBlob.data(), offset, len);
    }

    private Object readElem(Blob blob, Type type) {
        // See ECMA-335, II.23.3 Custom attributes
        return switch (type) {
            case Primitive primitive -> readPrimitiveVal(blob, typeLookup.getElementType(primitive));
            case EnumType enumType -> readPrimitiveVal(blob, typeLookup.getElementType(enumType.baseType()));
            default ->
                    throw new IllegalArgumentException("Unsupported element type: " + type.getClass().getSimpleName());
        };
    }

    private static UUID createGuidConstant(CustomAttributeValue value) {
        // The GUID value is given as 11 fixed arguments:
        // a 32-bit integer, two 16-bit integers and 8 8-bit integers.
        var fixedArguments = value.fixedArguments();
        var a = 0xffffffffL & (Integer) fixedArguments[0].value();
        var b = 0xffffL & (Short) fixedArguments[1].value();
        var c = 0xffffL & (Short) fixedArguments[2].value();
        var mostSigBits = (a << 32) | (b << 16) | c;

        var leastSigBits = 0L;
        for (int i = 0; i < 8; i++) {
            var v = 0xffL & (Byte) fixedArguments[i + 3].value();
            leastSigBits = leastSigBits | (v << (56 - i * 8));
        }
        return new UUID(mostSigBits, leastSigBits);
    }
}
