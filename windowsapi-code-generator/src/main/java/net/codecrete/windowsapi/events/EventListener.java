//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.events;

/**
 * Listener for events.
 * <p>
 * A listener is notified about events that happened during source code generation.
 * </p>
 */
public interface EventListener {
    /**
     * Called when an event has happened
     *
     * @param event the event
     */
    void onEvent(Event event);
}
