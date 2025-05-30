//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi;

import net.codecrete.windowsapi.events.Event;
import net.codecrete.windowsapi.events.EventListener;

public class SimpleEventListener implements EventListener {

    @Override
    public void onEvent(Event event) {
        switch (event) {
            case Event.JavaSourceGenerated(var path) -> System.out.println("File generated: " + path);
            case Event.InvalidArgument(var ignored1, var ignored2, var reason) ->
                    System.out.println("Error: " + reason);
            default -> System.out.println("Unknown event: " + event);
        }
    }
}
