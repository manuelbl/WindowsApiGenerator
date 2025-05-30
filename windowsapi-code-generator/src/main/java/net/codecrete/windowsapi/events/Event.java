//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.events;

import java.nio.file.Path;

/**
 * Describes an event that has happened during the source code generation.
 * <p>
 * Events can be progress information or errors.
 * </p>
 */
public sealed interface Event {
    /**
     * Event notifying that a Java source file has been generated.
     *
     * @param path path to the Java source file
     */
    record JavaSourceGenerated(Path path) implements Event {
    }

    /**
     * Event notifying that a directory has been created.
     *
     * @param path path to the directory
     */
    record DirectoryCreated(Path path) implements Event {
    }

    /**
     * Event notifying that a directory has been cleaned.
     *
     * @param path path to the directory
     */
    record DirectoryCleaned(Path path) implements Event {
    }

    /**
     * Event notifying that an argument passed to the code generation is invalid.
     * <p>
     * For arguments with a list type, this event might be raised multiple times.
     * </p>
     *
     * @param argument the argument name
     * @param value    the argument value
     * @param reason   the reason the value is invalid
     */
    record InvalidArgument(String argument, String value, String reason) implements Event {
    }
}
