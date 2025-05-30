//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.metadata;

import java.util.stream.Stream;

/**
 * Pointer type.
 */
public final class Pointer extends Type {

    private final Type referencedType;

    /**
     * Creates a new pointer instance.
     *
     * @param name           the pointer name
     * @param referencedType the type this pointer refers to
     */
    Pointer(String name, Type referencedType) {
        super(name, null, 0);
        this.referencedType = referencedType;
    }

    /**
     * Gets the type this pointer refers to.
     *
     * @return the type
     */
    public Type referencedType() {
        return referencedType;
    }

    @Override
    public Stream<Type> referencedTypes() {
        return Stream.of(referencedType);
    }
}
