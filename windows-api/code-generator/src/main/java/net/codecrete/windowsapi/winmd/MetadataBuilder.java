//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd;

import net.codecrete.windowsapi.metadata.Array;
import net.codecrete.windowsapi.metadata.ComInterface;
import net.codecrete.windowsapi.metadata.ConstantValue;
import net.codecrete.windowsapi.metadata.Delegate;
import net.codecrete.windowsapi.metadata.EnumType;
import net.codecrete.windowsapi.metadata.Member;
import net.codecrete.windowsapi.metadata.Metadata;
import net.codecrete.windowsapi.metadata.Method;
import net.codecrete.windowsapi.metadata.Namespace;
import net.codecrete.windowsapi.metadata.Parameter;
import net.codecrete.windowsapi.metadata.Pointer;
import net.codecrete.windowsapi.metadata.Primitive;
import net.codecrete.windowsapi.metadata.PrimitiveKind;
import net.codecrete.windowsapi.metadata.QualifiedName;
import net.codecrete.windowsapi.metadata.Struct;
import net.codecrete.windowsapi.metadata.Type;
import net.codecrete.windowsapi.metadata.TypeAlias;
import net.codecrete.windowsapi.metadata.TypeKind;
import net.codecrete.windowsapi.winmd.tables.CodedIndex;
import net.codecrete.windowsapi.winmd.tables.CodedIndexes;
import net.codecrete.windowsapi.winmd.tables.Field;
import net.codecrete.windowsapi.winmd.tables.TypeDef;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static net.codecrete.windowsapi.metadata.Architecture.ALL;
import static net.codecrete.windowsapi.winmd.tables.ImplMap.SUPPORTS_LAST_ERROR;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.ASSEMBLY_REF;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.FIELD;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.METHOD_DEF;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.MODULE;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.TYPE_REF;
import static net.codecrete.windowsapi.winmd.tables.TypeDef.ABSTRACT;
import static net.codecrete.windowsapi.winmd.tables.TypeDef.CLASS_SEMANTICS_INTERFACE;
import static net.codecrete.windowsapi.winmd.tables.TypeDef.LAYOUT_EXPLICIT;
import static net.codecrete.windowsapi.winmd.tables.TypeDef.LAYOUT_MASK;
import static net.codecrete.windowsapi.winmd.tables.TypeDef.VISIBILITY_MASK;
import static net.codecrete.windowsapi.winmd.tables.TypeDef.VISIBILITY_NESTED_PUBLIC;
import static net.codecrete.windowsapi.winmd.tables.TypeDef.VISIBILITY_PUBLIC;

/**
 * Processes a .winmd file and creates the connected data structure described the types, methods, etc.
 */
@SuppressWarnings("java:S4274")
public class MetadataBuilder implements TypeLookup {
    private static final String APIS = "Apis";

    private final MetadataFile metadataFile;
    private final Metadata metadata;
    private final VariantTransformation variantTransformation;
    private final Primitive[] primitiveTypes = new Primitive[15];
    private final CustomAttributeDecoder customAttributeDecoder;
    private final Map<Integer, Namespace> apiTypes = new HashMap<>();
    private final SignatureDecoder signatureDecoder;
    private final Primitive intPtrType;
    private final Primitive uintPtrType;
    private final Pointer voidPointerType;
    private final Struct systemGuidType;

    /**
     * Loads the metadata from the built-in .winmd file.
     *
     * @return the loaded metadata
     */
    public static Metadata load() {
        try (var stream = MetadataBuilder.class.getClassLoader().getResourceAsStream("Windows.Win32.winmd")) {
            var builder = new MetadataBuilder(new MetadataFile(stream));
            return builder.build();
        } catch (IOException e) {
            throw new WinmdException("Cannot open resource 'Windows.Win32.winmd'", e);
        }
    }

    private MetadataBuilder(MetadataFile metadataFile) {
        this.metadataFile = metadataFile;
        metadata = new Metadata();
        variantTransformation = new VariantTransformation(metadata);

        primitiveTypes[ElementTypes.VOID] = metadata.getPrimitive(PrimitiveKind.VOID);
        primitiveTypes[ElementTypes.BOOLEAN] = metadata.getPrimitive(PrimitiveKind.BOOL);
        primitiveTypes[ElementTypes.CHAR] = metadata.getPrimitive(PrimitiveKind.CHAR);
        primitiveTypes[ElementTypes.I1] = metadata.getPrimitive(PrimitiveKind.SBYTE);
        primitiveTypes[ElementTypes.U1] = metadata.getPrimitive(PrimitiveKind.BYTE);
        primitiveTypes[ElementTypes.I2] = metadata.getPrimitive(PrimitiveKind.INT16);
        primitiveTypes[ElementTypes.U2] = metadata.getPrimitive(PrimitiveKind.UINT16);
        primitiveTypes[ElementTypes.I4] = metadata.getPrimitive(PrimitiveKind.INT32);
        primitiveTypes[ElementTypes.U4] = metadata.getPrimitive(PrimitiveKind.UINT32);
        primitiveTypes[ElementTypes.I8] = metadata.getPrimitive(PrimitiveKind.INT64);
        primitiveTypes[ElementTypes.U8] = metadata.getPrimitive(PrimitiveKind.UINT64);
        primitiveTypes[ElementTypes.R4] = metadata.getPrimitive(PrimitiveKind.SINGLE);
        primitiveTypes[ElementTypes.R8] = metadata.getPrimitive(PrimitiveKind.DOUBLE);
        primitiveTypes[ElementTypes.STRING] = metadata.getPrimitive(PrimitiveKind.STRING);
        intPtrType = metadata.getPrimitive(PrimitiveKind.INT_PTR);
        uintPtrType = metadata.getPrimitive(PrimitiveKind.UINT_PTR);
        voidPointerType = metadata.makePointerFor(primitiveTypes[ElementTypes.VOID]);

        signatureDecoder = new SignatureDecoder(this);
        customAttributeDecoder = new CustomAttributeDecoder(this, metadataFile);
        systemGuidType = ((Struct) metadata.getType("System", "Guid"));
    }

    private Metadata build() {
        buildTypes();
        buildMethodsAndConstants();
        buildTypeFields();
        convertGuidConstants();
        buildMethodParameters();
        buildDelegateSignatures();
        variantTransformation.splitCombinedVariants();
        calculateTypeLayout();
        return metadata;
    }

    /**
     * Builds all types (without the fields)
     */
    private void buildTypes() {
        // create types without fields and functions without parameters
        for (int typeDefIndex = 2; typeDefIndex <= metadataFile.getTypeDefinitionCount(); typeDefIndex += 1) {
            buildType(typeDefIndex);
        }
    }

    @SuppressWarnings("java:S3776")
    private void buildType(int typeDefIndex) {
        assert metadata.getTypeByTypeDefIndex(typeDefIndex) == null;

        var typeDef = metadataFile.getTypeDef(typeDefIndex);
        var typeName = metadataFile.getString(typeDef.typeName());
        var namespaceName = metadataFile.getString(typeDef.typeNamespace());

        int visibility = typeDef.typeAttributes() & VISIBILITY_MASK;
        assert visibility == VISIBILITY_PUBLIC || visibility == VISIBILITY_NESTED_PUBLIC;
        boolean isNested = visibility == VISIBILITY_NESTED_PUBLIC;

        Namespace namespace = null;
        if (!isNested)
            namespace = metadata.getOrCreateNamespace(namespaceName);

        if (typeName.equals(APIS)) {
            apiTypes.put(typeDefIndex, namespace);
            return; // pseudo type for functions
        }

        var typeKind = getTypeKind(typeDef);
        var customAttributesData = customAttributeDecoder.getTypeDefAttributes(typeDefIndex);
        if (customAttributesData.isTypedef)
            typeKind = TypeKind.ALIAS;

        var classLayout = (typeDef.typeAttributes() & LAYOUT_MASK) != 0 ? metadataFile.getClassLayout(typeDefIndex) :
                null;
        var isUnion = (typeDef.typeAttributes() & LAYOUT_MASK) == LAYOUT_EXPLICIT;
        var packageSize = classLayout != null ? classLayout.packingSize() : 0;
        var classSize = classLayout != null ? classLayout.classSize() : 0;

        Struct enclosingType = null;
        if (isNested) {
            var nestedClass = metadataFile.getNestedClass(typeDefIndex);
            assert nestedClass != null;
            // The spec guarantees that the enclosing type precedes the nested type.
            // So the type must already exist.
            if (variantTransformation.isUnsupportedVariant(nestedClass.enclosingClass()))
                return;
            var enclosingTypeCandidate = metadata.getTypeByTypeDefIndex(nestedClass.enclosingClass());
            assert enclosingTypeCandidate instanceof Struct;
            enclosingType = (Struct) enclosingTypeCandidate;
        }

        assert enclosingType == null || typeKind == TypeKind.STRUCT;

        assert typeKind != null;
        var type = switch (typeKind) {
            case TypeKind.STRUCT -> new Struct(
                    typeName, namespace, typeDefIndex,
                    isUnion, packageSize, classSize,
                    enclosingType, customAttributesData.structSizeField, customAttributesData.guidConstant
            );
            case TypeKind.ENUM -> new EnumType(typeName, namespace, typeDefIndex, customAttributesData.isEnumFlags);
            case TypeKind.DELEGATE -> new Delegate(typeName, namespace, typeDefIndex);
            case TypeKind.COM_INTERFACE ->
                    new ComInterface(typeName, namespace, typeDefIndex, customAttributesData.guidConstant);
            case TypeKind.ATTRIBUTE -> null;
            case TypeKind.ALIAS -> metadata.makeAliasFor(typeDefIndex, typeName, namespace);

            default -> throw new AssertionError("Unsupported type: " + typeKind);
        };

        if (type == null)
            return;

        if (variantTransformation.preprocessType(type, customAttributesData.supportedArchitecture))
            return;

        type.setDocumentationUrl(customAttributesData.documentationUrl);

        metadata.addType(type, customAttributesData.supportedArchitecture == ALL);

        if (type.namespace() != null && type.name().equals("Architecture") && type.namespace().name().equals("Windows" +
                ".Win32.Foundation.Metadata"))
            buildFields(type);
    }

    /**
     * Adds the field details to each type.
     */
    private void buildTypeFields() {
        metadata.types().forEach(this::buildFields);
    }

    /**
     * Adds the field details to the given type.
     *
     * @param type the type
     */
    private void buildFields(Type type) {
        if (type instanceof Struct struct && struct.members() != null)
            return;
        if (type instanceof EnumType enumType && enumType.members() != null)
            return;

        var fields = getFields(type.typeDefIndex(), type instanceof Struct struct ? struct : null);

        switch (type) {
            case Struct struct -> {
                struct.setMembers(fields);
                if (struct.hasNestedTypes()) {
                    for (var nestedType : struct.nestedTypes())
                        buildFields(nestedType);
                }
            }
            case EnumType enumType -> {
                assert !fields.isEmpty() && fields.getFirst().name().equals("value__");
                var enumBaseType = fields.getFirst().type();
                assert enumBaseType instanceof Primitive;
                enumType.setBaseType((Primitive) enumBaseType);
                fields.removeFirst();
                enumType.setMembers(fields);
            }
            case TypeAlias typeAlias -> {
                assert !fields.isEmpty() && fields.getFirst().name().equals("Value");
                var member = fields.getFirst();
                typeAlias.setAliasedType(member.type());
            }
            default -> {
                assert fields.isEmpty();
            }
        }
    }

    /**
     * Gets the fields of the specified type definition.
     *
     * @param typeDefIndex the type definition index
     * @param parentType   the parent type
     */
    private List<Member> getFields(int typeDefIndex, Struct parentType) {
        var fields = new ArrayList<Member>();
        for (var field : metadataFile.getFields(typeDefIndex)) {
            var name = metadataFile.getString(field.name());
            var fieldType = signatureDecoder.decodeFieldSignature(metadataFile.getBlob(field.signature()), parentType);
            Object value = null;
            if (field.flags() == (Field.PUBLIC | Field.STATIC | Field.LITERAL | Field.HAS_DEFAULT)) {
                var parentIndex = CodedIndex.encode(FIELD, field.index(), CodedIndexes.HAS_CONSTANT_TABLES);
                var constant = metadataFile.getConstant(parentIndex);
                assert constant.type() != ElementTypes.CLASS;
                var valueBlob = metadataFile.getBlob(constant.value());
                value = Decoder.readConstantVal(valueBlob, constant.type());
                assert valueBlob.isAtEnd();
            } else if (fieldType instanceof Array array) {
                var customAttributesData = customAttributeDecoder.getFieldAttributes(field.index());
                if (customAttributesData != null && customAttributesData.isFlexibleArray) {
                    array.setFlexible(true);
                    adjustArraySizes(array, name, parentType);
                }
            }
            var member = new Member(name, field.index(), fieldType, value);
            fields.add(member);
        }
        return fields;
    }

    private void adjustArraySizes(Array array, String memberName, Struct parentType) {
        if (memberName.equals("CachePaths") && parentType.enclosingType() != null
                && parentType.enclosingType().name().startsWith("INTERNET_CACHE_CONFIG_INFO")) {
            // These are actually overlays of an array with a fixed length of 260 characters
            array.setFlexible(false);
        }
    }

    /**
     * Convert structs with just a GUID constant into a constant.
     */
    private void convertGuidConstants() {
        // Some structs have no fields but a GUID value.
        // They are converted to GUID constants.
        var guidConstants = metadata.types()
                .filter(this::isGuidConstant)
                .map(Struct.class::cast)
                .toList();
        for (var struct : guidConstants) {
            metadata.removeType(struct, true);
            var constant = new ConstantValue(struct.name(), struct.namespace(), systemGuidType, struct.guid(), false);
            struct.namespace().addConstant(constant);
        }
    }

    private boolean isGuidConstant(Type type) {
        return type instanceof Struct struct
                && struct.members().isEmpty()
                && struct.guid() != null;
    }

    private List<ComInterface> getInterfaces(int typeDefIndex) {
        return StreamSupport.stream(metadataFile.getInterfaceImpl(typeDefIndex).spliterator(), false)
                .map(interfaceImpl -> {
                    var typeDefOrRef = interfaceImpl.interfaceTypeDefOrRef();
                    assert typeDefOrRef.table() == TYPE_REF;
                    var interfaceType = getTypeByTypeRef(typeDefOrRef.index(), null, false);
                    return (ComInterface) interfaceType;
                })
                .toList();
    }

    /**
     * Calculates the layouts of all types
     */
    private void calculateTypeLayout() {
        var calculator = new StructLayouter(metadataFile);
        metadata.types().forEach(type -> {
            if (type instanceof Struct struct)
                calculator.layout(struct);
        });
    }

    /**
     * Builds the method metadata.
     */
    private void buildMethodsAndConstants() {
        for (var entry : apiTypes.entrySet()) {
            buildMethods(entry.getKey(), entry.getValue());
            buildConstants(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Builds the method metadata for the specified type and namespace.
     *
     * @param typeDefIndex    type definition index
     * @param parentNamespace namespace
     */
    private void buildMethods(int typeDefIndex, Namespace parentNamespace) {
        createMethods(typeDefIndex, parentNamespace)
                .forEach(method -> {
                    var customAttributesData = customAttributeDecoder.getMethodDefAttributes(method.methodDefIndex());
                    method.setConstantValue(customAttributesData.constantValue);
                    method.setDocumentationUrl(customAttributesData.documentationUrl);

                    if (!variantTransformation.preprocessMethod(method, customAttributesData.supportedArchitecture))
                        metadata.addMethod(method);
                });
    }

    private static final Set<String> STATIC_INITIALIZER_CONSTANT_TYPES = Set.of(
            "PROPERTYKEY",
            "DEVPROPKEY",
            "SID_IDENTIFIER_AUTHORITY",
            "CONDITION_VARIABLE",
            "SRWLOCK",
            "INIT_ONCE"
    );

    private void buildConstants(int typeDefIndex, Namespace namespace) {
        var fields = getFields(typeDefIndex, null);
        for (var field : fields) {
            var name = field.name();
            var value = field.value();
            var type = field.type();
            var typeName = type.name();

            ConstantValue constant;
            if (value instanceof Number) {
                constant = new ConstantValue(name, namespace, type, value, false);
            } else {
                var customAttributesData = customAttributeDecoder.getFieldAttributes(field.fieldIndex());
                if (value instanceof String) {
                    constant = new ConstantValue(name, namespace, type, value, customAttributesData.isAnsiEncoding);
                } else if (type == systemGuidType) {
                    constant = new ConstantValue(name, namespace, type, customAttributesData.guidConstant, false);
                } else if (STATIC_INITIALIZER_CONSTANT_TYPES.contains(typeName)) {
                    constant = new ConstantValue(name, namespace, type, customAttributesData.constantValue, false);
                } else {
                    throw new AssertionError("Unsupported constant type: " + typeName + " / " + customAttributesData.constantValue);
                }
            }

            namespace.addConstant(constant);
        }
    }

    private Stream<Method> createMethods(int typeDefIndex, Namespace parentNamespace) {
        return StreamSupport.stream(metadataFile.getMethodDefs(typeDefIndex).spliterator(), false)
                .map(methodDef -> {
                    var methodName = metadataFile.getString(methodDef.name());
                    return new Method(methodName, parentNamespace, methodDef.index());
                });
    }

    /**
     * Adds the method parameters to the methods.
     */
    private void buildMethodParameters() {
        metadata.methods().forEach(this::buildMethodParameters);
    }

    /**
     * Adds the method parameters to the given method.
     *
     * @param method the method
     */
    private void buildMethodParameters(Method method) {
        assert method.methodDefIndex() != 0;
        var memberForwarded = CodedIndex.encode(METHOD_DEF, method.methodDefIndex(),
                CodedIndexes.MEMBER_FORWARDED_TABLES);
        var implMap = metadataFile.getImplMap(memberForwarded);
        if (implMap != null) {
            var nameIndex = metadataFile.getModuleRefName(implMap.importScope());
            method.setDll(metadataFile.getString(nameIndex));
            if (method.dll().equals("FORCEINLINE")) {
                assert method.constantValue() != null;
                method.setDll(null);
            }
            method.setSupportsLastError((implMap.flags() & SUPPORTS_LAST_ERROR) != 0);
        }

        var methodDef = metadataFile.getMethodDef(method.methodDefIndex());
        var methodSignature =
                signatureDecoder.decodeMethodDefSignature(metadataFile.getBlob(methodDef.signature()));
        method.setReturnType(methodSignature.returnType());

        var parameters = new Parameter[methodSignature.paramTypes().length];
        int index = 0;
        for (var param : metadataFile.getParameters(method.methodDefIndex())) {
            String paramName = metadataFile.getString(param.name());
            if (param.sequence() < 1) {
                // return type
                assert paramName == null;
            } else {
                // positional parameter
                assert paramName != null;
                parameters[index] = new Parameter(paramName, methodSignature.paramTypes()[index]);
                index += 1;
            }
        }
        assert index == parameters.length;
        method.setParameters(parameters);
    }

    private void buildDelegateSignatures() {
        metadata.types().forEach(type -> {
            if (type instanceof Delegate delegate)
                buildDelegateSignature(delegate);
            if (type instanceof ComInterface comInterface)
                buildComInterfaceMethods(comInterface);
        });
    }

    private void buildDelegateSignature(Delegate delegate) {
        var invoke = createMethods(delegate.typeDefIndex(), delegate.namespace())
                .filter(method -> method.name().equals("Invoke"))
                .findFirst();
        invoke.ifPresentOrElse(method -> {
            buildMethodParameters(method);
            delegate.setSignature(method);
        }, () -> {
            throw new AssertionError("Delegate has no method called 'Invoke'");
        });
    }

    private void buildComInterfaceMethods(ComInterface comInterface) {
        comInterface.setImplementedInterfaces(getInterfaces(comInterface.typeDefIndex()));
        comInterface.setMethods(createMethods(comInterface.typeDefIndex(), comInterface.namespace()).toList());
        comInterface.methods().forEach(this::buildMethodParameters);
    }

    private TypeKind getTypeKind(TypeDef typeDef) {

        var baseClassTypeDefOrRef = typeDef.extendsTypeIndex();
        if (baseClassTypeDefOrRef.isNull()) {
            if (typeDef.typeAttributes() == (VISIBILITY_PUBLIC | CLASS_SEMANTICS_INTERFACE | ABSTRACT))
                return TypeKind.COM_INTERFACE;

            assert false : "Unexpected attributes for type definition without base type";
            return null;
        }

        var baseClass = getTypeName(baseClassTypeDefOrRef);

        if (baseClass.namespace().equals("System")) {
            return switch (baseClass.name()) {
                case "Enum" -> TypeKind.ENUM;
                case "ValueType" -> TypeKind.STRUCT;
                case "Attribute" -> TypeKind.ATTRIBUTE;
                case "MulticastDelegate" -> TypeKind.DELEGATE;
                default -> throw new AssertionError("Unknown base type " + baseClass.name());
            };
        }
        assert false : "Unexpected base type";
        return null;
    }

    private QualifiedName getTypeName(CodedIndex typeDefOrRefIndex) {
        String namespace = "";
        String name = "";
        if (typeDefOrRefIndex.table() == TYPE_REF) {
            var typeRef = metadataFile.getTypeRef(typeDefOrRefIndex.index());
            var resolutionScopeIndex = typeRef.resolutionScopeIndex();
            if (resolutionScopeIndex.table() == ASSEMBLY_REF && resolutionScopeIndex.index() == 1) {
                namespace = metadataFile.getString(typeRef.typeNamespace());
                name = metadataFile.getString(typeRef.typeName());
            } else {
                assert false : "Unexpected resolution scope for base type reference";
            }
        } else {
            assert false : "Unexpected table for base type";
        }

        return new QualifiedName(namespace, name);
    }

    @Override
    public Primitive getPrimitiveType(int elementType) {
        if (elementType < primitiveTypes.length)
            return primitiveTypes[elementType];
        if (elementType == ElementTypes.I)
            return intPtrType;
        if (elementType == ElementTypes.U)
            return uintPtrType;
        return null;
    }

    @Override
    public Type getTypeByTypeDef(int typeDefIndex) {
        return metadata.getTypeByTypeDefIndex(typeDefIndex);
    }

    @Override
    public Type getTypeByTypeRef(int typeRefIndex, Struct parentType, boolean externalTypeAllowed) {
        var typeRef = metadataFile.getTypeRef(typeRefIndex);
        var namespace = metadataFile.getString(typeRef.typeNamespace());
        var name = metadataFile.getString(typeRef.typeName());
        var resolutionScopeIndex = typeRef.resolutionScopeIndex();
        return switch (resolutionScopeIndex.table()) {
            case TYPE_REF -> parentType.getNestedType(name);
            case MODULE -> metadata.getType(namespace, name);
            case ASSEMBLY_REF -> {
                if (name.equals(systemGuidType.name()) && namespace.equals(systemGuidType.namespace().name())) {
                    yield systemGuidType;
                } else {
                    assert externalTypeAllowed;
                    yield voidPointerType;
                }
            }
            default -> throw new AssertionError("Resolution scope MODULE is not supported");
        };
    }

    @Override
    public int getElementType(Primitive primitiveType) {
        assert primitiveType != null;
        for (int i = 1; i < primitiveTypes.length; i += 1) {
            if (primitiveTypes[i] == primitiveType)
                return i;
        }
        throw new AssertionError("Unexpected primitive type: " + primitiveType);
    }

    @Override
    public Pointer makePointerFor(Type type) {
        return metadata.makePointerFor(type);
    }
}
