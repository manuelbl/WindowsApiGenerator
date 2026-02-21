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

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static net.codecrete.windowsapi.winmd.tables.CodedIndexes.HAS_CUSTOM_ATTRIBUTE_TABLES;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.FIELD;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.MEMBER_REF;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.METHOD_DEF;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.PARAM;
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
    private static final QualifiedName ASSOCIATED_ENUM_ATTRIBUTE = new QualifiedName(METADATA,
            "AssociatedEnumAttribute");
    private static final QualifiedName CONSTANT_ATTRIBUTE = new QualifiedName(METADATA, "ConstantAttribute");
    private static final QualifiedName DOCUMENTATION_ATTRIBUTE = new QualifiedName(METADATA, "DocumentationAttribute");
    private static final QualifiedName FLEXIBLE_ARRAY_ATTRIBUTE = new QualifiedName(METADATA, "FlexibleArrayAttribute");
    private static final QualifiedName GUID_ATTRIBUTE = new QualifiedName(METADATA, "GuidAttribute");
    private static final QualifiedName NATIVE_ENCODING_ATTRIBUTE = new QualifiedName(METADATA,
            "NativeEncodingAttribute");
    private static final QualifiedName NATIVE_TYPEDEF_ATTRIBUTE = new QualifiedName(METADATA, "NativeTypedefAttribute");
    private static final QualifiedName METADATA_TYPEDEF_ATTRIBUTE = new QualifiedName(METADATA,
            "MetadataTypedefAttribute");
    private static final QualifiedName STRUCT_SIZE_FIELD_ATTRIBUTE = new QualifiedName(METADATA,
            "StructSizeFieldAttribute");
    private static final QualifiedName SUPPORTED_ARCHITECTURE_ATTRIBUTE = new QualifiedName(METADATA,
            "SupportedArchitectureAttribute");

    /**
     * Attribute extractors for types
     */
    private static final Map<QualifiedName, Extractor<TypeCustomAttributeData>> typeAttributeExtractors = Map.of(
            SUPPORTED_ARCHITECTURE_ATTRIBUTE,
            (context, data) -> data.supportedArchitecture =
                    ((Number) context.getValue().fixedArguments()[0].value()).intValue(),
            DOCUMENTATION_ATTRIBUTE,
            (context, data) -> data.documentationUrl = context.getLazyString(),
            FLAGS_ATTRIBUTE,
            (context, data) -> data.isEnumFlags = true,
            GUID_ATTRIBUTE,
            (context, data) -> data.guidConstant = createGuidConstant(context.getValue()),
            NATIVE_TYPEDEF_ATTRIBUTE,
            (context, data) -> data.isTypedef = true,
            METADATA_TYPEDEF_ATTRIBUTE,
            (context, data) -> data.isTypedef = true,
            STRUCT_SIZE_FIELD_ATTRIBUTE,
            (context, data) -> data.structSizeField = (String) context.getValue().fixedArguments()[0].value()
    );

    /**
     * Set of ignored custom attributes for types.
     */
    private static final Set<QualifiedName> ignoredTypesAttributes = Set.of(
            new QualifiedName(SYSTEM, "AttributeUsageAttribute"),
            new QualifiedName(SYSTEM, "ObsoleteAttribute"),
            new QualifiedName("System.Runtime.InteropServices", "ComVisibleAttribute"),
            new QualifiedName("System.Runtime.InteropServices", "UnmanagedFunctionPointerAttribute"),
            new QualifiedName(METADATA, "AgileAttribute"),
            new QualifiedName(METADATA, "AlsoUsableForAttribute"),
            new QualifiedName(METADATA, "AnsiAttribute"),
            new QualifiedName(METADATA, "AssociatedConstantAttribute"),
            new QualifiedName(METADATA, "InvalidHandleValueAttribute"),
            new QualifiedName(METADATA, "RAIIFreeAttribute"),
            new QualifiedName(METADATA, "ScopedEnumAttribute"),
            new QualifiedName(METADATA, "SupportedOSPlatformAttribute"),
            new QualifiedName(METADATA, "UnicodeAttribute")
    );

    /**
     * Attributes extractors for methods
     */
    private static final Map<QualifiedName, Extractor<MethodCustomAttributeData>> methodAttributeExtractors = Map.of(
            SUPPORTED_ARCHITECTURE_ATTRIBUTE,
            (context, data) -> data.supportedArchitecture =
                    ((Number) context.getValue().fixedArguments()[0].value()).intValue(),
            DOCUMENTATION_ATTRIBUTE,
            (context, data) -> data.documentationUrl = context.getLazyString(),
            CONSTANT_ATTRIBUTE,
            (context, data) -> data.constantValue = context.getValue().fixedArguments()[0].value()
    );

    /**
     * Set of ignored custom attributes for methods.
     */
    private static final Set<QualifiedName> ignoredMethodAttributes = Set.of(
            new QualifiedName(SYSTEM, "ObsoleteAttribute"),
            new QualifiedName(METADATA, "AnsiAttribute"),
            new QualifiedName(METADATA, "CanReturnErrorsAsSuccessAttribute"),
            new QualifiedName(METADATA, "CanReturnMultipleSuccessValuesAttribute"),
            new QualifiedName(METADATA, "SupportedOSPlatformAttribute"),
            new QualifiedName(METADATA, "UnicodeAttribute"),
            new QualifiedName("System.Diagnostics.CodeAnalysis", "DoesNotReturnAttribute")
    );

    /**
     * Attributes extractors for fields
     */
    private static final Map<QualifiedName, Extractor<FieldCustomAttributeData>> fieldAttributeExtractors = Map.of(
            DOCUMENTATION_ATTRIBUTE,
            (context, data) -> data.documentationUrl = context.getLazyString(),
            GUID_ATTRIBUTE,
            (context, data) -> data.guidConstant = createGuidConstant(context.getValue()),
            NATIVE_ENCODING_ATTRIBUTE,
            (context, data) -> data.isAnsiEncoding = true,
            FLEXIBLE_ARRAY_ATTRIBUTE,
            (context, data) -> data.isFlexibleArray = true,
            CONSTANT_ATTRIBUTE,
            (context, data) -> data.constantValue = context.getValue().fixedArguments()[0].value()
    );

    /**
     * Set of ignored custom attributes for fields.
     */
    private static final Set<QualifiedName> ignoredFieldAttributes = Set.of(
            new QualifiedName(METADATA, "ConstAttribute")
    );

    /**
     * Attributes extractors for parameters
     */
    private static final Map<QualifiedName, Extractor<ParamCustomAttributeData>> paramAttributeExtractors = Map.of(
            ASSOCIATED_ENUM_ATTRIBUTE,
            (context, data) -> data.associatedEnumType = (String) context.getValue().fixedArguments()[0].value()
    );

    /**
     * Set of ignored custom attributes for parameters.
     */
    private static final Set<QualifiedName> ignoredParamAttributes = Set.of(
            new QualifiedName(METADATA, "ComOutPtrAttribute"),
            new QualifiedName(METADATA, "ConstAttribute"),
            DOCUMENTATION_ATTRIBUTE,
            new QualifiedName(METADATA, "DoNotReleaseAttribute"),
            new QualifiedName(METADATA, "FreeWithAttribute"),
            new QualifiedName(METADATA, "IgnoreIfReturnAttribute"),
            new QualifiedName(METADATA, "MemorySizeAttribute"),
            new QualifiedName(METADATA, "NativeArrayInfoAttribute"),
            new QualifiedName(METADATA, "NotNullTerminatedAttribute"),
            new QualifiedName(METADATA, "NullNullTerminatedAttribute"),
            new QualifiedName(METADATA, "RAIIFreeAttribute"),
            new QualifiedName(METADATA, "ReservedAttribute"),
            new QualifiedName(METADATA, "RetainedAttribute"),
            new QualifiedName(METADATA, "RetValAttribute")
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
    TypeCustomAttributeData getTypeDefAttributes(int typeDef) {
        var typeDefIndex = CodedIndex.encode(TYPE_DEF, typeDef, HAS_CUSTOM_ATTRIBUTE_TABLES);
        var data = new TypeCustomAttributeData();
        extractAttributes(typeDefIndex, typeAttributeExtractors, ignoredTypesAttributes, data);
        return data;
    }

    /**
     * Gets the custom attribute data for the given {@code MethodDef} index.
     *
     * @param methodDef the {@code MethodDef} index
     * @return the custom attribute data
     */
    MethodCustomAttributeData getMethodDefAttributes(int methodDef) {
        var methodDefIndex = CodedIndex.encode(METHOD_DEF, methodDef, HAS_CUSTOM_ATTRIBUTE_TABLES);
        var data = new MethodCustomAttributeData();
        extractAttributes(methodDefIndex, methodAttributeExtractors, ignoredMethodAttributes, data);
        return data;
    }

    /**
     * Gets the custom attribute data for the given {@code Field} index.
     *
     * @param field the {@code Field} index
     * @return the custom attribute data
     */
    FieldCustomAttributeData getFieldAttributes(int field) {
        var fieldIndex = CodedIndex.encode(FIELD, field, HAS_CUSTOM_ATTRIBUTE_TABLES);
        var data = new FieldCustomAttributeData();
        extractAttributes(fieldIndex, fieldAttributeExtractors, ignoredFieldAttributes, data);
        return data;
    }

    /**
     * Gets the custom attribute data for the given {@code Param} index.
     *
     * @param param the {@code Param} index
     * @return the custom attribute data
     */
    ParamCustomAttributeData getParamAttributes(int param) {
        var paramIndex = CodedIndex.encode(PARAM, param, HAS_CUSTOM_ATTRIBUTE_TABLES);
        var data = new ParamCustomAttributeData();
        extractAttributes(paramIndex, paramAttributeExtractors, ignoredParamAttributes, data);
        return data;
    }

    private <T> void extractAttributes(int hasCustomAttributeIndex, Map<QualifiedName, Extractor<T>> extractors,
                                       Set<QualifiedName> ignoredAttributes, T data) {
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
            var extractor = extractors.get(qualifiedName);
            assert extractor != null;

            var context = new ExtractionContext(this, customAttribute, memberRef);
            extractor.extract(context, data);
        }
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
        var returnType = decodeType(signature, null, null);
        var params = new Type[paramCount];
        for (int i = 0; i < paramCount; i++) {
            params[i] = decodeType(signature, null, null);
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

    record ExtractionContext(CustomAttributeDecoder decoder, CustomAttribute attribute, MemberRef memberRef) {
        CustomAttributeValue getValue() {
            return decoder.getValue(attribute, memberRef);
        }

        LazyString getLazyString() {
            return decoder.getLazyString(attribute, memberRef);
        }
    }

    @FunctionalInterface
    interface Extractor<T> {
        void extract(ExtractionContext context, T data);
    }
}
