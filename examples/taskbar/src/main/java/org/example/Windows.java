//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package org.example;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.Arena;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

import static java.lang.foreign.MemorySegment.NULL;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static windows.win32.foundation.Apis.LocalFree;
import static windows.win32.foundation.Constants.S_OK;
import static windows.win32.system.diagnostics.debug.Apis.FormatMessageW;
import static windows.win32.system.diagnostics.debug.FORMAT_MESSAGE_OPTIONS.FORMAT_MESSAGE_ALLOCATE_BUFFER;
import static windows.win32.system.diagnostics.debug.FORMAT_MESSAGE_OPTIONS.FORMAT_MESSAGE_FROM_HMODULE;
import static windows.win32.system.diagnostics.debug.FORMAT_MESSAGE_OPTIONS.FORMAT_MESSAGE_FROM_SYSTEM;
import static windows.win32.system.diagnostics.debug.FORMAT_MESSAGE_OPTIONS.FORMAT_MESSAGE_IGNORE_INSERTS;
import static windows.win32.system.libraryloader.Apis.GetModuleHandleW;

public class Windows {
    // address layout pointing to an unbounded memory segment
    private static final AddressLayout ADDRESS_UNBOUNDED = ADDRESS.withTargetLayout(
            MemoryLayout.sequenceLayout(Long.MAX_VALUE, JAVA_BYTE));

    private static final MemoryLayout errorStateLayout = Linker.Option.captureStateLayout();

    private static final MemorySegment ntModuleHandle;

    static {
        var arena = Arena.ofAuto();
        var ntModuleName = arena.allocateFrom("NTDLL.DLL", UTF_16LE);
        var errorState = arena.allocate(errorStateLayout);
        ntModuleHandle = GetModuleHandleW(errorState, ntModuleName);
    }

    /**
     * Checks the HRESULT code.
     * <p>
     * If the code is different from S_OK, throws an exception with the message for the code.
     * </p>
     * @param hresult the HRESULT code
     */
    public static void checkHResult(int hresult) {
        if (hresult != S_OK)
            throw new IllegalStateException(getErrorMessage(hresult));
    }

    /**
     * Gets the error message for the specified Windows error code.
     *
     * @param errorCode error code
     */
    public static String getErrorMessage(int errorCode) {
        try (var arena = Arena.ofConfined()) {
            var errorState = arena.allocate(errorStateLayout);
            var messagePointerHolder = arena.allocate(ADDRESS);

            // First try: Win32 error code
            var res = FormatMessageW(
                    errorState,
                    FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS,
                    NULL,
                    errorCode,
                    0,
                    messagePointerHolder,
                    0,
                    NULL
            );

            // Second try: NTSTATUS error code
            if (res == 0) {
                res = FormatMessageW(
                        errorState,
                        FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_HMODULE | FORMAT_MESSAGE_IGNORE_INSERTS,
                        ntModuleHandle,
                        errorCode,
                        0,
                        messagePointerHolder,
                        0,
                        NULL
                );
            }

            // Fallback
            if (res == 0)
                return "unspecified error";

            var messagePointer = messagePointerHolder.get(ADDRESS_UNBOUNDED, 0);
            var message = messagePointer.getString(0, UTF_16LE);
            LocalFree(errorState, messagePointer);
            return message.trim();
        }
    }

    private Windows() {
    }
}
