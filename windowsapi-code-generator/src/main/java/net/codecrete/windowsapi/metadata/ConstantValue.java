//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.metadata;

/**
 * Value of a constant.
 *
 * @param name           the constant name
 * @param namespace      the constant's namespace
 * @param type           the constant type
 * @param value          the constant value
 * @param isAnsiEncoding flag indicating if a string value is encoded as ANSI (Windows-1252) or Unicode (UTF-16).
 */
public record ConstantValue(String name, Namespace namespace, Type type, Object value, boolean isAnsiEncoding) {
}
