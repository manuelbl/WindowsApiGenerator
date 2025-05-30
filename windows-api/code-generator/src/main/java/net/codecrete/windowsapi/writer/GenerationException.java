//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.writer;

/**
 * Exception thrown if an error occurs during code generation.
 */
public class GenerationException extends RuntimeException {

    /**
     * Creates a new instance.
     *
     * @param message the exception message
     */
    public GenerationException(String message) {
        super(message);
    }
}
