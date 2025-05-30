//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd;

/**
 * Exception thrown when inconsistent or invalid data is detected in a WinMD file.
 */
public class WinmdException extends RuntimeException {
    /**
     * Creates a new exception with the given message.
     *
     * @param message the message
     */
    public WinmdException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with the given message and cause.
     *
     * @param message the message
     * @param cause   the original cause
     */
    public WinmdException(String message, Throwable cause) {
        super(message, cause);
    }
}
