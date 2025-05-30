//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.metadata;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * Base class for types.
 */
public abstract sealed class Type
        permits Struct, Delegate, Primitive, Pointer, Array, TypeAlias, EnumType, ComInterface {

    /**
     * The type name.
     */
    protected String name;
    /**
     * The native type name.
     */
    protected final String nativeName;
    /**
     * The namespace this type belongs to.
     * <p>
     * Not set for nested types.
     * </p>
     */
    protected final Namespace namespace;
    /**
     * The {@code TypeDef} index in the metadata file.
     */
    protected final int typeDefIndex;
    /**
     * The documentation URL.
     */
    protected LazyString documentationUrl;

    /**
     * Creates a new instance.
     *
     * @param name         the type name
     * @param namespace    the type's namespace (or {@code null} if the type is an anonymous and/or nested type
     * @param typeDefIndex the {@code TypeDef} index
     */
    protected Type(String name, Namespace namespace, int typeDefIndex) {
        this.name = name;
        this.nativeName = name;
        this.namespace = namespace;
        this.typeDefIndex = typeDefIndex;
    }

    /**
     * Gets the type name.
     * <p>
     * For architecture-specific types, this is the modified type name,
     * as used for Java code.
     * </p>
     *
     * @return type name
     */
    public final String name() {
        return name;
    }

    /**
     * Sets the type name.
     *
     * @param name type name
     */
    public final void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the native type name.
     * <p>
     * For architecture-specific types, this is the original type name used by Windows.
     * </p>
     *
     * @return native type name
     */
    public final String nativeName() {
        return nativeName;
    }

    /**
     * Gets the type's namespace.
     * <p>
     * Types enclosed in other types do not have a namespace.
     * </p>
     *
     * @return namespace
     */
    public final Namespace namespace() {
        return namespace;
    }

    /**
     * Gets the {@code TypeDef} index
     *
     * @return the index
     */
    public final int typeDefIndex() {
        return typeDefIndex;
    }

    private static final Pattern ANONYMOUS_NAME_PATTERN = Pattern.compile("anonymous", CASE_INSENSITIVE);

    /**
     * Indicates if this type is anonymous, i.e., has an artificial name only.
     * <p>
     * Only types enclosed in other types can be anonymous.
     * </p>
     *
     * @return if the type is anonymous
     */
    public final boolean isAnonymous() {
        return namespace() == null && ANONYMOUS_NAME_PATTERN.matcher(name()).find();
    }

    /**
     * Returns the URL to Microsoft's documentation about this type.
     *
     * @return the URL
     */
    public final LazyString documentationUrl() {
        return documentationUrl;
    }

    /**
     * Sets the URL to Microsoft's documentation about this type.
     *
     * @param documentationUrl the URL
     */
    public final void setDocumentationUrl(LazyString documentationUrl) {
        this.documentationUrl = documentationUrl;
    }

    /**
     * Returns the types directly references by this type.
     *
     * @return Stream of types
     */
    public Stream<Type> referencedTypes() {
        return Stream.empty();
    }

    /**
     * Replace the types directly referenced by this type with the provided replacements.
     * <p>
     * The lambda for replacement lookup will return the same type if no replacement is needed.
     * </p>
     *
     * @param typeReplacement lambda for looking up the replacement type.
     */
    public void replaceTypes(UnaryOperator<Type> typeReplacement) {
        // default implementation: no replacement
    }
}
