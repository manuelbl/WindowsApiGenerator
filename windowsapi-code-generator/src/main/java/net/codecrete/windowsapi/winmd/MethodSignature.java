//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd;

import net.codecrete.windowsapi.metadata.Type;

/**
 * Method signature.
 *
 * @param returnType  the return type
 * @param paramTypes  the parameters
 */
@SuppressWarnings("java:S6218")
public record MethodSignature(
        Type returnType,
        Type[] paramTypes
) {
}