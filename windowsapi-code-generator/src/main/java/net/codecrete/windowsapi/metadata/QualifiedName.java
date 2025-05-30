//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.metadata;

/**
 * Name consisting of namespace and local name.
 *
 * @param namespace the namespace name
 * @param name      the name within the namespace
 */
public record QualifiedName(String namespace, String name) {

    @Override
    public String toString() {
        return namespace != null ? namespace + "." + name : name;
    }
}
