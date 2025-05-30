//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.metadata;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * Delegate type (aka as function pointer or callback functions).
 * <p>
 * Defines the signature of a function pointer.
 * </p>
 */
public final class Delegate extends Type {

    private Method signature;

    /**
     * Creates a new instance.
     *
     * @param name         the delegate name
     * @param namespace    the delegate's namespace
     * @param typeDefIndex the {@code TypeDef} index
     */
    public Delegate(String name, Namespace namespace, int typeDefIndex) {
        super(name, namespace, typeDefIndex);
    }

    /**
     * Returns the delegate's signature.
     * <p>
     * The return {@link Method} instance defines the return type and parameters.
     * Most of the remaining method attributes can be ignored.
     * </p>
     *
     * @return the signature (as a method)
     */
    public Method signature() {
        return signature;
    }

    /**
     * Sets the delegate signature.
     *
     * @param signature the signature (as a method)
     */
    public void setSignature(Method signature) {
        this.signature = signature;
    }

    @Override
    public Stream<Type> referencedTypes() {
        return signature.referencedTypes();
    }

    @Override
    public void replaceTypes(UnaryOperator<Type> typeReplacements) {
        signature.replaceTypes(typeReplacements);
    }
}
