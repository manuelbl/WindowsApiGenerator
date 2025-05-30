//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.metadata;

/**
 * Argument value for constructing a custom attribute.
 * <p>
 * It is a value passed to one of the arguments of the
 * custom attribute's constructor.
 * </p>
 *
 * @param type  the argument type
 * @param name  the argument name
 * @param value the argument value
 */
public record ArgumentValue(Type type, String name, Object value) {
}
