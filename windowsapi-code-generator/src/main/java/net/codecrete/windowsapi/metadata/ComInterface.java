//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.metadata;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * COM interface type.
 *
 * <p>
 * A COM interface is a mostly opaque object-oriented data structures.
 * It can only be accessed and manipulated through functions.
 * </p>
 * <p>
 * At the start of the data structure, there is a pointer to an array
 * of function pointers, often called "vtable" after the similar C++ construct.
 * Thees function pointers are the function to access and manipulate the
 * COM object instance.
 * </p>
 * <p>
 * This class defines the method/function signatures, the COM interface ID
 * and the implemented super interface type.
 * </p>
 */
public final class ComInterface extends Type {

    private final UUID iid;
    private List<Method> methods;
    private ComInterface implementedInterface;

    /**
     * Creates a new instance.
     *
     * @param name         the COM interface name
     * @param namespace    the COM interface's namespace
     * @param typeDefIndex the {@code TypeDef} index
     * @param iid          the COM interface ID
     */
    public ComInterface(String name, Namespace namespace, int typeDefIndex, UUID iid) {
        super(name, namespace, typeDefIndex);
        this.iid = iid;
    }

    /**
     * Gets the COM interface ID (IID).
     *
     * @return the IID
     */
    public UUID getIid() {
        return iid;
    }

    /**
     * Gets the implemented super interface.
     * <p>
     * All COM interfaces implement the {@code IUnknown} interface, either directly
     * or indirectly by implementing a super COM interface that does.
     * The only exception is {@code IUnknown} itself.
     * </p>
     *
     * @return the super interface, or {@code null} if no super interface is implemented.
     */
    public ComInterface implementedInterface() {
        return implementedInterface;
    }

    /**
     * Sets the implemented COM interfaces.
     * <p>
     * Even though this method accepts a list, it assumes that at most one interface is implemented.
     * </p>
     *
     * @param implementedInterfaces list of COM interface types.
     */
    @SuppressWarnings("java:S4274")
    public void setImplementedInterfaces(List<ComInterface> implementedInterfaces) {
        assert implementedInterfaces.size() <= 1;
        implementedInterface = !implementedInterfaces.isEmpty() ? implementedInterfaces.getFirst() : null;
    }

    /**
     * Gets the methods that make up the interface of this COM interface.
     * <p>
     * The list of methods does not include the methods inherited from implemented super interfaces.
     * </p>
     *
     * @return the methods
     */
    public List<Method> methods() {
        return methods;
    }

    /**
     * Sets the methods that make up the interface of this COM interface.
     * <p>
     * The list of methods may not include the methods inherited from implemented super interfaces.
     * </p>
     *
     * @param methods the methods
     */
    public void setMethods(List<Method> methods) {
        this.methods = methods;
    }

    @Override
    public Stream<Type> referencedTypes() {
        return Stream.concat(
                methods.stream().flatMap(Method::referencedTypes),
                implementedInterface != null ? Stream.of(implementedInterface) : Stream.empty()
        );
    }
}
