//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.examples.messagebox;

import java.lang.foreign.Arena;
import java.lang.foreign.Linker;

import windows.win32.ui.windowsandmessaging.MESSAGEBOX_RESULT;
import windows.win32.ui.windowsandmessaging.MESSAGEBOX_STYLE;

import static java.lang.foreign.MemorySegment.NULL;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static windows.win32.ui.windowsandmessaging.Apis.MessageBoxW;

/**
 * Show a message box
 */
public class App
{
    public static void main( String[] args )
    {
        var errorStateLayout = Linker.Option.captureStateLayout();

        try (var arena = Arena.ofConfined()) {
            var errorState = arena.allocate(errorStateLayout);

            var result = MessageBoxW(
                    errorState,
                    NULL,
                    arena.allocateFrom("Hello, World!", UTF_16LE),
                    arena.allocateFrom("Windows API", UTF_16LE),
                    MESSAGEBOX_STYLE.MB_OKCANCEL
            );

            switch (result) {
                case MESSAGEBOX_RESULT.IDOK:
                    System.out.println("Clicked 'OK'");
                    break;
                case MESSAGEBOX_RESULT.IDCANCEL:
                    System.out.println("Clicked 'Cancel'");
                    break;
                default:
                    System.out.println("Closed with result " + result);
            }
        }
    }
}
