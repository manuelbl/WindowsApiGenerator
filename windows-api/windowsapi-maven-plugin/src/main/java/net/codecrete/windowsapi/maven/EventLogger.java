//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.maven;

import net.codecrete.windowsapi.events.Event;
import net.codecrete.windowsapi.events.Event.DirectoryCleaned;
import net.codecrete.windowsapi.events.Event.DirectoryCreated;
import net.codecrete.windowsapi.events.Event.JavaSourceGenerated;
import net.codecrete.windowsapi.events.EventListener;
import org.apache.maven.plugin.logging.Log;

/**
 * Event listener writing to messages to the Maven logger.
 */
public class EventLogger implements EventListener {
    private final Log logger;

    /**
     * Creates a new instance
     *
     * @param logger Maven plugin logger instance
     */
    public EventLogger(Log logger) {
        this.logger = logger;
    }

    @Override
    public void onEvent(Event event) {
        switch (event) {
            case JavaSourceGenerated(var path) -> {
                if (logger.isDebugEnabled())
                    logger.debug("Generated Java file " + path);
            }
            case DirectoryCreated(var path) -> {
                if (logger.isDebugEnabled())
                    logger.debug("Created source directory " + path);
            }
            case DirectoryCleaned(var path) -> {
                if (logger.isDebugEnabled())
                    logger.debug("Deleted all files and directories in output directory " + path);
            }
            case Event.InvalidArgument(var argument, var value, var reason)
                    -> logger.error(String.format("Invalid value '%s' for argument %s: %s", value, argument, reason));
        }
    }
}
