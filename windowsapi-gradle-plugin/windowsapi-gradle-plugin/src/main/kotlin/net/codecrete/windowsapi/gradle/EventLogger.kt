//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.gradle

import net.codecrete.windowsapi.events.Event
import net.codecrete.windowsapi.events.EventListener
import org.gradle.api.logging.Logger

/**
 * Event listener writing to messages to the Gradle logger.
 */
class EventLogger(private val logger: Logger) : EventListener {
    override fun onEvent(event: Event) {
        when (event) {
            is Event.JavaSourceGenerated -> logger.info("Generated java file {}", event.path)
            is Event.DirectoryCleaned -> logger.info("Deleted all files and directories in output directory {}", event.path)
            is Event.DirectoryCreated -> logger.info("Created source directory {}", event.path)
            is Event.InvalidArgument -> logger.error("'{}' is invalid for argument {}: {}", event.value, event.argument, event.reason)
        }
    }
}