//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd;

import net.codecrete.windowsapi.metadata.ArgumentValue;

/**
 * Value of a custom attribute.
 * <p>
 * The value consists of the arguments to a constructor.
 * </p>
 *
 * @param fixedArguments array of fixed/positional arguments
 */
@SuppressWarnings("java:S6218")
public record CustomAttributeValue(ArgumentValue[] fixedArguments) {
}
