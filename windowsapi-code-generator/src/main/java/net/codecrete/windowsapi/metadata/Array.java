//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.metadata;

import java.util.stream.Stream;

/**
 * Array type.
 */
public final class Array extends Type {

    private final Type itemType;
    private final int arrayLength;
    private boolean isFlexible;

    /**
     * Creates a new instance.
     *
     * @param name         the type name
     * @param namespace    the type's namespace
     * @param typeDefIndex the {@code TypeDef} index
     * @param itemType     the type of the array items
     * @param arrayLength  the array length (number of items)
     */
    public Array(String name, Namespace namespace, int typeDefIndex, Type itemType, int arrayLength) {
        super(name, namespace, typeDefIndex);
        this.itemType = itemType;
        this.arrayLength = arrayLength;
    }

    /**
     * Gets the type of the array items.
     *
     * @return the type
     */
    public Type itemType() {
        return itemType;
    }

    /**
     * Gets the array length.
     * <p>
     * For flexible length arrays, this method usually refers 0 or 1,
     * depending on how it is declared in the metadata.
     * </p>
     *
     * @return the number of items
     */
    public int arrayLength() {
        return arrayLength;
    }

    /**
     * Indicates if the array has a flexible length (as opposed to variable length).
     *
     * @return {@code true} if the array has flexible length, {@code false} is it has fixed length
     */
    public boolean isFlexible() {
        return isFlexible;
    }

    /**
     * Sets if the array has a flexible length (as opposed to variable length).
     *
     * @param isFlexible {@code true} if the array has flexible length, {@code false} is it has fixed length
     */
    public void setFlexible(boolean isFlexible) {
        this.isFlexible = isFlexible;
    }

    @Override
    public Stream<Type> referencedTypes() {
        return Stream.of(itemType);
    }
}
