//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.metadata;

/**
 * Enumeration for the type kind.
 */
public enum TypeKind {
    /**
     * Struct or union
     */
    STRUCT,
    /**
     * Enumeration
     */
    ENUM,
    /**
     * Attribute.
     * <p>
     * Attributes are similar to Java annotations.
     * </p>
     */
    ATTRIBUTE,
    /**
     * Delegate
     */
    DELEGATE,
    /**
     * COM interface
     */
    COM_INTERFACE,
    /**
     * Primitive type
     */
    PRIMITIVE,
    /**
     * Pointer
     */
    POINTER,
    /**
     * Array
     */
    ARRAY,
    /**
     * Type alias
     */
    ALIAS
}
