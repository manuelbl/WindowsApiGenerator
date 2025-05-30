//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.metadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * Windows API metadata.
 * <p>
 * An instance of this class contains the entire Windows API metadata.
 * </p>
 */
@SuppressWarnings({"java:S4274", "java:S1192"})
public class Metadata {
    private final Map<String, Namespace> namespaces = new HashMap<>();
    private final Namespace unnamedNamespace = new Namespace(null);
    private final Map<Integer, Type> typesByDefinitionIndex = new HashMap<>();
    private final Map<Integer, Method> methodsByMethodDefIndex = new HashMap<>();
    private final Map<PrimitiveKind, Primitive> primitivesByKind = buildPrimitiveTypes(unnamedNamespace);
    private final Map<Type, Pointer> pointersByType = new HashMap<>();
    private final Map<Integer, TypeAlias> aliasesByTypeDefIndex = new HashMap<>();

    /**
     * Creates a new instance.
     */
    public Metadata() {
        addSystemGuid();
    }

    /**
     * Gets a map of all namespaces.
     * <p>
     * In the map, namespaces are indexed by their name.
     * </p>
     *
     * @return the namespace map
     */
    public Map<String, Namespace> namespaces() {
        return namespaces;
    }

    /**
     * Creates a new namespace with the given name.
     *
     * @param name the namespace name
     * @return the created namespace
     */
    public Namespace createNamespace(String name) {
        assert name != null;
        assert !namespaces.containsKey(name);
        var namespace = new Namespace(name);
        namespaces.put(name, namespace);
        return namespace;
    }

    /**
     * Gets the namespace with the given name or creates it if it does not yet exist.
     *
     * @param name the namespace name
     * @return the existing or new namespace
     */
    public Namespace getOrCreateNamespace(String name) {
        assert name != null;
        var namespace = namespaces.get(name);
        if (namespace == null)
            namespace = createNamespace(name);
        return namespace;
    }

    /**
     * Gets all types.
     *
     * @return the types as a stream
     */
    public Stream<Type> types() {
        return typesByDefinitionIndex.values().stream();
    }

    /**
     * Gets the type with the specified name.
     * <p>
     * If different architecture variants exist for this type,
     * one of the variants is returned.
     * </p>
     *
     * @param namespace the namespace name
     * @param name      the type name
     * @return type, or {@code null} if it is not found
     */
    public Type getType(String namespace, String name) {
        Namespace ns;
        if (namespace != null) {
            ns = namespaces.get(namespace);
            if (ns == null)
                return null;
        } else {
            ns = unnamedNamespace;
        }
        return ns.types().get(name);
    }

    /**
     * Gets the primitive type for the given kind.
     *
     * @param kind the primitive type kind
     * @return the primitive
     */
    public Primitive getPrimitive(PrimitiveKind kind) {
        return primitivesByKind.get(kind);
    }

    /**
     * Gets the type with the specified {@code TypeDef} index.
     *
     * @param typeDefIndex the {@code TypeDef} index
     * @return the type, or {@code null} if it is not found
     */
    public Type getTypeByTypeDefIndex(int typeDefIndex) {
        return typesByDefinitionIndex.get(typeDefIndex);
    }

    /**
     * Adds a type to this metadata.
     *
     * @param type         the type
     * @param nameIsUnique if {@code true}, checks that the type does not exist yet
     */
    public void addType(Type type, boolean nameIsUnique) {
        assert type.typeDefIndex() != 0;
        assert !typesByDefinitionIndex.containsKey(type.typeDefIndex());
        typesByDefinitionIndex.put(type.typeDefIndex(), type);

        if (type instanceof Struct struct && struct.enclosingType() != null) {
            struct.enclosingType().addNestedType(type);
        } else {
            assert !nameIsUnique || !type.namespace().types().containsKey(type.name());
            type.namespace().addType(type);
        }
    }

    /**
     * Removes a type from this metadata.
     *
     * @param type       the type
     * @param nameExists if {@code true}, checks that the type actually exists
     */
    public void removeType(Type type, boolean nameExists) {
        assert type.typeDefIndex() != 0;
        assert typesByDefinitionIndex.containsKey(type.typeDefIndex());
        var removed = typesByDefinitionIndex.remove(type.typeDefIndex());
        assert removed != null;

        assert !(type instanceof Struct struct && struct.enclosingType() != null);

        if (nameExists || type.namespace().types().containsKey(type.name()))
            type.namespace().removeType(type);
    }

    /**
     * Gets all methods.
     *
     * @return the methods as a stream
     */
    public Stream<Method> methods() {
        return methodsByMethodDefIndex.values().stream();
    }

    /**
     * Adds a method to this metadata.
     *
     * @param method the method
     */
    public void addMethod(Method method) {
        assert !method.namespace().methods().containsKey(method.name());
        method.namespace().addMethod(method);
        assert method.methodDefIndex() != 0;
        assert !methodsByMethodDefIndex.containsKey(method.methodDefIndex());
        methodsByMethodDefIndex.put(method.methodDefIndex(), method);
    }

    /**
     * Gets all constants.
     *
     * @return the constants as a stream
     */
    public Stream<ConstantValue> constants() {
        return namespaces.values().stream().flatMap(namespace -> namespace.constants().values().stream());
    }

    /**
     * Creates a pointer referencing the specified type.
     * <p>
     * It will reuse an exising pointer type if available.
     * </p>
     *
     * @param type the type to reference
     * @return the pointer
     */
    public Pointer makePointerFor(Type type) {
        return pointersByType.computeIfAbsent(type, it -> new Pointer(it.name() + "*", it));
    }

    /**
     * Creates an alias for the specified {@code TypeDef} index.
     * <p>
     * It will reuse an exising alias type if available.
     * </p>
     *
     * @param typeDefIndex the {@code TypeDef} index of the alias
     * @param name         the alias name
     * @param namespace    the alias namespace
     * @return the alias
     */
    public TypeAlias makeAliasFor(int typeDefIndex, String name, Namespace namespace) {
        return aliasesByTypeDefIndex.computeIfAbsent(typeDefIndex, it -> new TypeAlias(name, namespace, it));
    }

    /**
     * Finds the structs with the given names.
     * <p>
     * The names are without the namespace, and they are case-sensitive.
     * If a name is not found, it is ignored.
     * </p>
     *
     * @param names the set of names
     * @return the found structs (as a list)
     */
    public List<Type> findStructs(Set<String> names) {
        return types().filter(type -> type instanceof Struct && names.contains(type.nativeName())).toList();
    }

    /**
     * Finds the enums with the given names.
     * <p>
     * The names are without the namespace, and they are case-sensitive.
     * If a name is not found, it is ignored.
     * </p>
     *
     * @param names the set of names
     * @return the found enumerations (as a list)
     */
    public List<Type> findEnums(Set<String> names) {
        return types().filter(type -> type instanceof EnumType && names.contains(type.nativeName())).toList();
    }

    /**
     * Finds enumerations containing a member with the given name.
     *
     * @param memberName the member name
     * @return the enumerations
     */
    public List<EnumType> findEnumWithMember(String memberName) {
        return types().filter(type -> type instanceof EnumType enumType && enumType.getMember(memberName) != null)
                .map(EnumType.class::cast).toList();
    }

    /**
     * Finds the delegates with the given names.
     * <p>
     * The names are without the namespace, and they are case-sensitive.
     * If a name is not found, it is ignored.
     * </p>
     *
     * @param names set of names
     * @return the found delegates (as a list)
     */
    public List<Type> findDelegates(Set<String> names) {
        return types().filter(type -> type instanceof Delegate && names.contains(type.nativeName())).toList();
    }

    /**
     * Finds the COM interfaces with the given names.
     * <p>
     * The names are without the namespace, and they are case-sensitive.
     * If a name is not found, it is ignored.
     * </p>
     *
     * @param names set of names
     * @return the found COM interfaces (as a list)
     */
    public List<Type> findComInterfaces(Set<String> names) {
        return types().filter(type -> type instanceof ComInterface && names.contains(type.nativeName())).toList();
    }

    /**
     * Finds the functions with the given names.
     * <p>
     * The names are without the namespace, they are case-sensitive, and they are compared to
     * the native Windows name.
     * If a name is not found, it is ignored.
     * </p>
     *
     * @param names the set of names
     * @return the found functions (as a list)
     */
    public List<Method> findFunctions(Set<String> names) {
        return methods().filter(method -> names.contains(method.nativeName())).toList();
    }

    /**
     * Finds the constants with the given names.
     * <p>
     * The names are without the namespace, and they are case-sensitive.
     * If a name is not found, it is ignored.
     * </p>
     *
     * @param names set of names
     * @return the found constants (as a list)
     */
    public List<ConstantValue> findConstants(Set<String> names) {
        return constants().filter(constant -> names.contains(constant.name())).toList();
    }

    private void addSystemGuid() {
        var systemNamespace = createNamespace("System");

        var guidType = new Struct("GUID", systemNamespace, 9999999, false, 0, 0, null, null, null);
        var members = new ArrayList<Member>();
        members.add(new Member("Data1", 0, getPrimitive(PrimitiveKind.UINT32), null));
        members.add(new Member("Data2", 0, getPrimitive(PrimitiveKind.UINT16), null));
        members.add(new Member("Data3", 0, getPrimitive(PrimitiveKind.UINT16), null));
        members.add(new Member("Data4", 0, new Array("Data4[]", null, 0, getPrimitive(PrimitiveKind.BYTE), 8), null));
        guidType.setMembers(members);
        guidType.setName("Guid");
        addType(guidType, true);
    }

    private static Map<PrimitiveKind, Primitive> buildPrimitiveTypes(Namespace namespace) {
        var primitives = Arrays.stream(PrimitiveKind.values())
                .map(kind -> new Primitive(kind, namespace))
                .toList();
        primitives.forEach(namespace::addType);
        return primitives.stream().collect(toMap(Primitive::kind, Function.identity()));
    }
}
