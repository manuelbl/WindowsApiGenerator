//
// Windows API Generator for Java
// Copyright (c) 2026 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.examples.graalvm;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.Arena;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.MemorySegment.NULL;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.lang.foreign.ValueLayout.JAVA_CHAR;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static windows.win32.foundation.Apis.LocalFree;
import static windows.win32.foundation.WIN32_ERROR.ERROR_SUCCESS;
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
     * Checks the result.
     * <p>
     * If the result is 0, an exception with the message for the last error is thrown.
     * </p>
     * @param result the result to check
     * @param errorState the error state.
     */
    public static int checkResult(int result, MemorySegment errorState) {
        if (result == 0)
            throwError(errorState);
        return result;
    }

    /**
     * Checks the HRESULT code.
     * <p>
     * If the code indicates an error, an exception with the message for the result code is raised.
     * </p>
     * @param hresult the HRESULT code to check
     */
    public static void checkHResult(int hresult) {
        if (hresult < 0)
            throwError(hresult);
    }

    /**
     * Checks the error code.
     * <p>
     * If the code is different from ERROR_SUCCESS, an exception with the message for the result code.
     * </p>
     * @param errorCode the error code to check
     */
    public static void checkErrorCode(int errorCode) {
        if (errorCode != ERROR_SUCCESS)
            throwError(errorCode);
    }

    /**
     * Throws an exception with the error message for the given error state.
     * @param errorState the error state
     */
    public static void throwError(MemorySegment errorState) {
        var lastError = getLastError(errorState);
        throw new IllegalStateException(getErrorMessage(lastError));
    }

    /**
     * Throws an exception with the error message for the given error code.
     * @param errorCode the error code
     */
    public static void throwError(int errorCode) {
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
            return message.trim();
        }
    }

    /**
     * Gets a Java String from UTF-16 data.
     * <p>
     * If the UTF-16 data ends with a terminating null character,
     * it is removed.
     * </p>
     *
     * @param data buffer with UTF-16 code units.
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

    private Windows() {
    }
}
