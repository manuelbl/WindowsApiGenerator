//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.tests;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.Arena;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.MemoryLayout.sequenceLayout;
import static java.lang.foreign.MemorySegment.NULL;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.lang.foreign.ValueLayout.JAVA_CHAR;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static windows.win32.foundation.Apis.LocalFree;
import static windows.win32.system.diagnostics.debug.Apis.FormatMessageW;
import static windows.win32.system.diagnostics.debug.FORMAT_MESSAGE_OPTIONS.FORMAT_MESSAGE_ALLOCATE_BUFFER;
import static windows.win32.system.diagnostics.debug.FORMAT_MESSAGE_OPTIONS.FORMAT_MESSAGE_FROM_HMODULE;
import static windows.win32.system.diagnostics.debug.FORMAT_MESSAGE_OPTIONS.FORMAT_MESSAGE_FROM_SYSTEM;
import static windows.win32.system.diagnostics.debug.FORMAT_MESSAGE_OPTIONS.FORMAT_MESSAGE_IGNORE_INSERTS;
import static windows.win32.system.libraryloader.Apis.GetModuleHandleW;

public class Windows {
    private static final MemoryLayout errorStateLayout = Linker.Option.captureStateLayout();
    private static final VarHandle callState_GetLastError$VH =
            errorStateLayout.varHandle(MemoryLayout.PathElement.groupElement("GetLastError"));

    public static final AddressLayout ADDRESS_UNBOUNDED
            = ADDRESS.withTargetLayout(sequenceLayout(Long.MAX_VALUE, JAVA_BYTE));

    /**
     * Returns the error code captured using the call state.
     *
     * @param callState the call state
     * @return the error code
     */
    public static int getLastError(MemorySegment callState) {
        return (int) callState_GetLastError$VH.get(callState, 0);
    }

    public static String getErrorMessage(int errorCode) {
        try (var arena = Arena.ofConfined()) {
            var errorStateLayout = Linker.Option.captureStateLayout();
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
                    NULL);

            // Second try: NTSTATUS error code
            if (res == 0) {
                var moduleName = arena.allocateFrom("NTDLL.DLL", UTF_16LE);
                var ntModuleHandle = GetModuleHandleW(errorState, moduleName);
                res = FormatMessageW(
                        errorState,
                        FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_HMODULE | FORMAT_MESSAGE_IGNORE_INSERTS,
                        ntModuleHandle,
                        errorCode,
                        0,
                        messagePointerHolder,
                        0,
                        NULL);
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

    /**
     * Gets a Java string from a memory segment with UTF-16 code units.
     * <p>
     * If the UTF-16 data ends with a terminating null character, it is removed.
     * </p>
     *
     * @param data memory segment with UTF-16 code units.
     * @param offset offset to start of relevant data (in bytes)
     * @param length length of relevant data (in bytes)
     * @return string
     */
    public static String getUtf16String(MemorySegment data, int offset, int length) {
        // test for terminating null
        if (length > 0 && data.get(JAVA_CHAR, (long) offset + length) == 0)
            length -= 2;

        var characters = data.asSlice(offset, length).toArray(JAVA_CHAR);
        return new String(characters);
    }
}
