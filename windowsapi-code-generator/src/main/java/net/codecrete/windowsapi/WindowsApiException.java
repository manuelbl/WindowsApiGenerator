//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi;

/**
 * Thrown when a validation fails before generating code.
 */
public class WindowsApiException extends RuntimeException {
    /**
     * Creates a new instance.
     *
     * @param message exception message
     */
    public WindowsApiException(String message) {
        super(message);
    }
}
