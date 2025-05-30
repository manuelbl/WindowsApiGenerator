//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.examples;

import windows.win32.foundation.WIN32_ERROR;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.Arena;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.MemorySegment.NULL;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static windows.win32.foundation.Apis.LocalFree;
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
    private static final VarHandle callStateGetLastErrorVarHandle =
            errorStateLayout.varHandle(MemoryLayout.PathElement.groupElement("GetLastError"));

    private static final MemorySegment ntModuleHandle;

    static {
        var arena = Arena.ofAuto();
        var ntModuleName = arena.allocateFrom("NTDLL.DLL", UTF_16LE);
        var errorState = arena.allocate(errorStateLayout);
        ntModuleHandle = GetModuleHandleW(errorState, ntModuleName);
    }

    /**
     * Returns the error code captured using the call state.
     *
     * @param callState the call state
     * @return the error code
     */
    public static int getLastError(MemorySegment callState) {
        return (int) callStateGetLastErrorVarHandle.get(callState, 0);
    }

    /**
     * Checks that the previous Windows API call was successful.
     * <p>
     * Throws an exception with the error message otherwise.
     * </p>
     */
    public static void checkSuccessful(MemorySegment errorState) {
        var lastError = getLastError(errorState);
        if (lastError != WIN32_ERROR.ERROR_SUCCESS)
            throw new IllegalStateException(getErrorMessage(lastError));
    }

    /**
     * Checks that {@code errorCode} is a successful code.
     * <p>
     * Throws an exception with the error message otherwise.
     * </p>
     */
    public static void checkSuccessful(int errorCode) {
        if (errorCode < 0)
            throw new IllegalStateException(getErrorMessage(errorCode));
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
            return message.replaceAll("\\s", "");
        }
    }

    private Windows() {
    }
}
