//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.metadata;

/**
 * Primitive type
 * <p>
 * There is a single instance of this class for each primitive kind.
 * </p>
 *
 * @see PrimitiveKind
 */
public final class Primitive extends Type {
    private final PrimitiveKind kind;

    Primitive(PrimitiveKind primitiveKind, Namespace namespace) {
        super(primitiveKind.typeName(), namespace, 0);
        kind = primitiveKind;
    }

    /**
     * Gets the primitive kind.
     *
     * @return the primitive kind
     */
    public PrimitiveKind kind() {
        return kind;
    }
}
