//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd;

import net.codecrete.windowsapi.metadata.Pointer;
import net.codecrete.windowsapi.metadata.Primitive;
import net.codecrete.windowsapi.metadata.Struct;
import net.codecrete.windowsapi.metadata.Type;

/**
 * Interface for looking up types.
 * <p>
 * Used by decoder classes, implemented by {@link MetadataBuilder}.
 * </p>
 */
interface TypeLookup {
    /**
     * Gets the primitive type for the specified element type.
     * <p>
     * For the list of element types, see ECMA-335 II.23.1.16 Element types used in signatures.
     * </p>
     *
     * @param elementType element type
     * @return type instance, or {@code null} if the element type is not a primitive type
     */
    Primitive getPrimitiveType(int elementType);

    /**
     * Gets the type for the specified TypeAlias index.
     *
     * @param typeDefIndex TypeAlias index
     * @return type instance
     */
    Type getTypeByTypeDef(int typeDefIndex);

    /**
     * Gets the type for the specified TypeRef index.
     *
     * @param typeRefIndex        TypeRef index
     * @param parentType          parent type to resolve nested types
     * @param externalTypeAllowed indicates if a type from another assembly is allowed
     * @return type instance
     */
    Type getTypeByTypeRef(int typeRefIndex, Struct parentType, boolean externalTypeAllowed);

    /**
     * Gets the element type for the specified type instance.
     * <p>
     * The type instance must refer to a primitive type.
     * </p>
     *
     * @param primitiveType type instance
     * @return element type
     */
    int getElementType(Primitive primitiveType);

    /**
     * Creates a pointer referencing the specified type.
     *
     * @param type type to reference
     * @return pointer
     */
    Pointer makePointerFor(Type type);
}
