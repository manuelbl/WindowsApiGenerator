//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.metadata;

/**
 * Type alias
 */
@SuppressWarnings("java:S4274")
public final class TypeAlias extends Type {

    private Type aliasedType;

    /**
     * Creates a new instance.
     *
     * @param name         the alias name
     * @param namespace    the alias namespace
     * @param typeDefIndex the {@code TypeDef} index
     */
    TypeAlias(String name, Namespace namespace, int typeDefIndex) {
        super(name, namespace, typeDefIndex);
    }

    /**
     * Gets the type this instance provides an alias for.
     *
     * @return the type.
     */
    public Type aliasedType() {
        return aliasedType;
    }

    /**
     * Sets the type this instance provides an alias for.
     *
     * @param aliasedType the type.
     */
    public void setAliasedType(Type aliasedType) {
        assert aliasedType instanceof Primitive
                || (aliasedType instanceof Pointer pointer && pointer.referencedType() instanceof Primitive);
        this.aliasedType = aliasedType;
    }
}
