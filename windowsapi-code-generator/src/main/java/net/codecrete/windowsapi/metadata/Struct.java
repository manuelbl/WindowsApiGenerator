//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.metadata;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * Struct or union.
 * <p>
 * Structs can either be independent or nested. An independent type has a namespace and
 * can be the enclosing type for nested types. Nested types have no namespace and must
 * be contained in an enclosing parent type. They often have artificial names like
 * {@code _Anonymous_e__Struct}. Nested types are typically used for unnamed structs or unions.
 * </p>
 */
@SuppressWarnings("java:S4274")
public final class Struct extends Type {
    private final boolean isUnion;
    private int packageSize;
    private int structSize;
    private final Struct enclosingType;
    private Map<String, Type> nestedTypes;
    private List<Member> members;
    private boolean isLayoutDone;
    private boolean isArchitectureSpecific;
    private Member flexibleArrayMember;
    private final String structSizeMember;
    private final UUID guid;

    /**
     * Creates a new instance.
     *
     * @param name             the struct name
     * @param namespace        the namespace
     * @param typeDefIndex     the {@code TypeDef} index
     * @param isUnion          indicates if the new instance is a union (instead of a struct)
     * @param packageSize      the package size (alignment) of this struct (in bytes)
     * @param structSize       the size of this struct (in bytes)
     * @param enclosingType    the enclosing type in case it is a nested type
     * @param structSizeMember the struct field holding the struct's size (usually a member called {code cbSize}).
     * @param guid             the GUID associated with the struct
     */
    @SuppressWarnings("java:S107")
    public Struct(String name, Namespace namespace, int typeDefIndex, boolean isUnion,
                  int packageSize, int structSize,
                  Struct enclosingType, String structSizeMember, UUID guid) {
        super(name, namespace, typeDefIndex);
        assert namespace != null || enclosingType != null;
        this.isUnion = isUnion;
        this.enclosingType = enclosingType;
        this.packageSize = packageSize;
        this.structSize = structSize;
        this.structSizeMember = structSizeMember;
        this.guid = guid;
    }

    /**
     * Indicates if this is a union.
     *
     * @return {@code true} if it is a union, {@code false} if it is a struct
     */
    public boolean isUnion() {
        return isUnion;
    }

    /**
     * Returns the package size, i.e., the alignment required by this struct.
     *
     * @return the package size (in bytes)
     */
    public int packageSize() {
        return packageSize;
    }

    /**
     * Sets the package size, i.e., the alignment required by this struct.
     *
     * @param packageSize the package size (in bytes)
     */
    public void setPackageSize(int packageSize) {
        this.packageSize = packageSize;
    }

    /**
     * Gets the struct size (length).
     *
     * @return the struct size (in bytes).
     */
    public int structSize() {
        return structSize;
    }

    /**
     * Sets the struct size (length).
     *
     * @param structSize the struct size (in bytes).
     */
    public void setStructSize(int structSize) {
        this.structSize = structSize;
    }

    /**
     * Indicates if the layout of this struct has been calculated.
     *
     * @return {code true} if it is done
     */
    public boolean isLayoutDone() {
        return isLayoutDone;
    }

    /**
     * Marks that the layout of this struct has been calculated.
     */
    public void setLayoutDone() {
        this.isLayoutDone = true;
    }

    /**
     * Gets the parent type enclosing this type.
     *
     * @return the enclosing type, or {@code null} if this type is not nested
     */
    public Struct enclosingType() {
        return enclosingType;
    }

    /**
     * Adds the given type as a nested type of this type.
     *
     * @param nestedType the nested type
     */
    public void addNestedType(Type nestedType) {
        if (nestedTypes == null)
            nestedTypes = new HashMap<>();
        assert !nestedTypes.containsKey(nestedType.name());
        nestedTypes.put(nestedType.name(), nestedType);
    }

    /**
     * Gets the nested type with the given name.
     *
     * @param name the type name
     * @return the nested type
     */
    public Type getNestedType(String name) {
        assert nestedTypes != null;
        var type = nestedTypes.get(name);
        assert type != null;
        return type;
    }

    /**
     * Indicates if this type contains nested types.
     *
     * @return {@code true} if this type has nested types
     */
    public boolean hasNestedTypes() {
        return nestedTypes != null;
    }

    /**
     * Gets the nested types.
     *
     * @return the types
     */
    public Collection<Type> nestedTypes() {
        assert nestedTypes != null;
        return nestedTypes.values();
    }

    /**
     * Gets this struct's members (fields).
     *
     * @return the members
     */
    public List<Member> members() {
        return members;
    }

    /**
     * Sets this struct's members (fields).
     *
     * @param members the members
     */
    public void setMembers(List<Member> members) {
        this.members = members;
    }

    /**
     * Indicates if this type is a nested type.
     *
     * @return {@code true} if this type is nested
     */
    public boolean isNested() {
        return namespace == null;
    }

    /**
     * Indicates if this struct has a fixed size.
     * <p>
     * A struct does not have a fixed size if it contains
     * a flexible array, i.e., an array with a variable number of elements.
     * </p>
     *
     * @return {@code true} if this struct has a fixed size
     */
    public boolean hasFixedSize() {
        return flexibleArrayMember == null;
    }

    /**
     * Gets the field within this struct that is a flexible array
     *
     * @return the flexible array member, or {@code null} if there is no such member
     */
    public Member flexibleArrayMember() {
        return flexibleArrayMember;
    }

    /**
     * Sets the field within this struct that is a flexible array
     *
     * @param flexibleArrayMember the flexible array member, or {@code null} if there is no such member
     */
    public void setFlexibleArrayMember(Member flexibleArrayMember) {
        this.flexibleArrayMember = flexibleArrayMember;
    }

    /**
     * Gets the field within this struct that indicates the struct size.
     * <p>
     * The Windows API can support multiple versions of the same struct
     * if the struct has a field indicating the size of the struct.
     * It must be set by the caller.
     * </p>
     * <p>
     * If the struct contains such a field, it is usually called {@code cbSize}.
     * </p>
     *
     * @return the field containing the struct size, or {@code null} if there is no such field
     */
    public String structSizeMember() {
        return structSizeMember;
    }

    /**
     * Indicates if this struct is only valid for certain processor architectures (X64, ARM64).
     * <p>
     * If it is architecture-specific, there might be separate versions of each architecture,
     * or it might not be supported on all architectures.
     * </p>
     *
     * @return {@code true} if it is architecture-specific
     */
    public boolean isArchitectureSpecific() {
        return isArchitectureSpecific;
    }

    /**
     * Sets if this struct is only valid for certain processor architectures (X64, ARM64).
     * <p>
     * If it is architecture-specific, there might be separate versions of each architecture,
     * or it might not be supported on all architectures.
     * </p>
     *
     * @param isArchitectureSpecific {@code true} if it is architecture-specific, {@code false} if it is valid for
     *                               all architecture
     */
    public void setArchitectureSpecific(boolean isArchitectureSpecific) {
        this.isArchitectureSpecific = isArchitectureSpecific;
    }

    /**
     * Gets the GUID associated with this struct.
     *
     * @return the GUID
     */
    public UUID guid() {
        return guid;
    }

    @Override
    public Stream<Type> referencedTypes() {
        return Stream.concat(
                members.stream().map(Member::type),
                nestedTypes != null ? nestedTypes.values().stream() : Stream.empty()
        );
    }

    /**
     * Creates a copy of this type and assigns it a different {@code TypeDef} index.
     *
     * @param newTypeDefIndex the new {@code TypeDef} index
     * @return the copied type
     */
    public Struct duplicate(int newTypeDefIndex) {
        var struct = new Struct(name, namespace, newTypeDefIndex, isUnion, packageSize, structSize, enclosingType,
                structSizeMember, guid);
        struct.nestedTypes = nestedTypes;
        struct.members = members;
        struct.flexibleArrayMember = flexibleArrayMember;
        return struct;
    }

    @Override
    public void replaceTypes(UnaryOperator<Type> typeReplacement) {
        if (nestedTypes != null)
            nestedTypes = nestedTypes.entrySet().stream()
                    .collect(toMap(Map.Entry::getKey, it -> typeReplacement.apply(it.getValue())));
        members = members.stream().map(member -> member.duplicate(typeReplacement)).toList();
    }
}
