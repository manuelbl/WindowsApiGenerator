//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.metadata;

import java.util.List;
import java.util.stream.Stream;

/**
 * Enumeration type.
 * <p>
 * An enumeration is a collection of related integer constants.
 * </p>
 */
public final class EnumType extends Type {
    private final boolean isEnumFlags;
    private Primitive baseType;
    private List<Member> members;

    /**
     * Creates a new instance.
     *
     * @param name         enumeration name
     * @param namespace    enumeration's namespace
     * @param typeDefIndex the {@code TypeDef} index
     * @param isEnumFlags  indicates if multiple enumeration values can be combined as a bitmask, representing
     *                     multiple flags.
     */
    public EnumType(String name, Namespace namespace, int typeDefIndex, boolean isEnumFlags) {
        super(name, namespace, typeDefIndex);
        this.isEnumFlags = isEnumFlags;
    }

    /**
     * Gets the type of the enumeration constants.
     * <p>
     * The base type is a primitive integer type.
     * </p>
     *
     * @return the base type
     */
    public Primitive baseType() {
        return baseType;
    }

    /**
     * Sets the type of the enumeration constants.
     * <p>
     * The base type must be a primitive integer type.
     * </p>
     *
     * @param baseType the base type
     */
    public void setBaseType(Primitive baseType) {
        this.baseType = baseType;
    }

    /**
     * Gets the enumeration constants.
     *
     * @return the constants
     */
    public List<Member> members() {
        return members;
    }

    /**
     * Sets the enumeration constants.
     *
     * @param members the constants
     */
    public void setMembers(List<Member> members) {
        this.members = members;
    }

    /**
     * Gets the member with the given name.
     *
     * @param memberName the member name
     * @return the member, or {@code null} if no matching member was found
     */
    public Member getMember(String memberName) {
        return members.stream().filter(member -> member.name().equals(memberName)).findFirst().orElse(null);
    }

    /**
     * Indicates if the enumeration constants can be combined as a bitmask, representing multiple flags.
     *
     * @return {@code true} if constants can be combined, {@code false} otherwise
     */
    public boolean isEnumFlags() {
        return isEnumFlags;
    }

    @Override
    public Stream<Type> referencedTypes() {
        return Stream.of(baseType);
    }
}
