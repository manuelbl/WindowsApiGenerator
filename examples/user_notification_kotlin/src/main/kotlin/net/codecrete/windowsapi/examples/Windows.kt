//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.examples

import windows.win32.foundation.Apis.LocalFree
import windows.win32.system.diagnostics.debug.Apis.FormatMessageW
import windows.win32.system.diagnostics.debug.FORMAT_MESSAGE_OPTIONS
import windows.win32.system.libraryloader.Apis.GetModuleHandleW
import java.lang.foreign.*
import java.lang.foreign.MemorySegment.NULL
import java.lang.foreign.ValueLayout.ADDRESS
import java.lang.foreign.ValueLayout.JAVA_BYTE
import java.nio.charset.StandardCharsets.UTF_16LE

object Windows {
    // address layout pointing to an unbounded memory segment
    private val ADDRESS_UNBOUNDED: AddressLayout? = ADDRESS.withTargetLayout(
        MemoryLayout.sequenceLayout(
            Long.MAX_VALUE,
            JAVA_BYTE
        )
    )

    private val errorStateLayout: MemoryLayout = Linker.Option.captureStateLayout()

    val ntModuleHandle: MemorySegment

    init {
        Arena.ofConfined().use { arena ->
            val ntModuleName = arena.allocateFrom("NTDLL.DLL", UTF_16LE)
            val errorState = arena.allocate(errorStateLayout)
            ntModuleHandle = GetModuleHandleW(errorState, ntModuleName)
        }
    }

    /**
     * Checks the HRESULT value.
     *
     * If the value indicates an error, an exception is raised.
     *
     * @param hresult the HRESULT value
     * @return the HRESULT value
     */
    fun checkHResult(hresult: Int): Int {
        check(hresult >= 0) { getErrorMessage(hresult) }
        return hresult
    }

    /**
     * Gets the error message for the specified Windows error code.
     *
     * @param errorCode error code
     */
    fun getErrorMessage(errorCode: Int): String {
        Arena.ofConfined().use { arena ->
            val errorState = arena.allocate(errorStateLayout)
            val messagePointerHolder = arena.allocate(ADDRESS)

            // First try: Win32 error code
            var res = FormatMessageW(
                errorState,
                FORMAT_MESSAGE_OPTIONS.FORMAT_MESSAGE_ALLOCATE_BUFFER or FORMAT_MESSAGE_OPTIONS.FORMAT_MESSAGE_FROM_SYSTEM or FORMAT_MESSAGE_OPTIONS.FORMAT_MESSAGE_IGNORE_INSERTS,
                NULL,
                errorCode,
                0,
                messagePointerHolder,
                0,
                NULL
            )

            // Second try: NTSTATUS error code
            if (res == 0) {
                res = FormatMessageW(
                    errorState,
                    FORMAT_MESSAGE_OPTIONS.FORMAT_MESSAGE_ALLOCATE_BUFFER or FORMAT_MESSAGE_OPTIONS.FORMAT_MESSAGE_FROM_HMODULE or FORMAT_MESSAGE_OPTIONS.FORMAT_MESSAGE_IGNORE_INSERTS,
                    ntModuleHandle,
                    errorCode,
                    0,
                    messagePointerHolder,
                    0,
                    NULL
                )
            }

            // Fallback
            if (res == 0) return "unspecified error"

            val messagePointer = messagePointerHolder.get(ADDRESS_UNBOUNDED, 0)
            val message = messagePointer.getString(0, UTF_16LE)
            LocalFree(errorState, messagePointer)
            return message.trim { it <= ' ' }
        }
    }
}