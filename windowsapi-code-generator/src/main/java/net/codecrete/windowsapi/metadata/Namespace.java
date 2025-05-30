//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.metadata;

import java.util.HashMap;
import java.util.Map;

/**
 * Namespace of a type, method or constant.
 * <p>
 * Windows metadata is organized by namespaces.
 * </p>
 */
@SuppressWarnings("java:S4274")
public class Namespace {
    private final String name;
    private final Map<String, Type> types = new HashMap<>();
    private final Map<String, Method> methods = new HashMap<>();
    private final Map<String, ConstantValue> constants = new HashMap<>();

    /**
     * Creates a new instance.
     * <p>
     * Namespaces follow the naming of C# namespaces, i.e., they have a hierarchical structure
     * resulting in a namespace name with components separated by periods, and each component
     * has a name in camel casing starting with an upper-case letter,
     * e.g., {@code Windows.Win32.Devices.Display}.
     * </p>
     *
     * @param name the namespace name
     */
    Namespace(String name) {
        this.name = name;
    }

    /**
     * Gets the namespace name.
     *
     * @return the name
     */
    public String name() {
        return name;
    }

    /**
     * Gets all types in this namespace.
     *
     * @return the types, as a map indexed by type name
     */
    public Map<String, Type> types() {
        return types;
    }

    /**
     * Gets all methods in this namespace.
     *
     * @return the methods, as a map indexed by method name
     */
    public Map<String, Method> methods() {
        return methods;
    }

    /**
     * Gets all constants in this namespace.
     *
     * @return the constants, as a map indexed by the constants' name
     */
    public Map<String, ConstantValue> constants() {
        return constants;
    }

    void addType(Type type) {
        assert type.namespace() == this;
        types.put(type.name(), type);
    }

    void removeType(Type type) {
        assert type.namespace() == this;
        var removed = types.remove(type.name());
        assert removed != null;
    }

    void addMethod(Method method) {
        assert method.namespace() == this;
        methods.put(method.name(), method);
    }

    /**
     * Adds a constant to the namespace.
     *
     * @param constant the constant
     */
    public void addConstant(ConstantValue constant) {
        assert !constants.containsKey(constant.name());
        constants.put(constant.name(), constant);
    }

    @Override
    public String toString() {
        return name;
    }
}
