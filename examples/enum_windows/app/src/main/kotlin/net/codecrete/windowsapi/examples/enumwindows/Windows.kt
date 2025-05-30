//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.examples.enumwindows

import windows.win32.foundation.Apis.LocalFree
import windows.win32.foundation.WIN32_ERROR
import windows.win32.system.diagnostics.debug.Apis.FormatMessageW
import windows.win32.system.diagnostics.debug.FORMAT_MESSAGE_OPTIONS
import windows.win32.system.libraryloader.Apis.GetModuleHandleW
import java.lang.foreign.AddressLayout
import java.lang.foreign.Arena
import java.lang.foreign.Linker
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemorySegment
import java.lang.foreign.MemorySegment.NULL
import java.lang.foreign.ValueLayout.ADDRESS
import java.lang.foreign.ValueLayout.JAVA_BYTE
import java.lang.invoke.VarHandle
import java.nio.charset.StandardCharsets.UTF_16LE

object Windows {
    // address layout pointing to an unbounded memory segment
    private val ADDRESS_UNBOUNDED: AddressLayout? = ADDRESS.withTargetLayout(
        MemoryLayout.sequenceLayout(
            Long.Companion.MAX_VALUE,
            JAVA_BYTE
        )
    )

    private val errorStateLayout: MemoryLayout = Linker.Option.captureStateLayout()
    private val callStateGetLastErrorVarHandle: VarHandle =
        errorStateLayout.varHandle(MemoryLayout.PathElement.groupElement("GetLastError"))

    val ntModuleHandle: MemorySegment

    init {
        Arena.ofConfined().use { arena ->
            val ntModuleName = arena.allocateFrom("NTDLL.DLL", UTF_16LE)
            val errorState = arena.allocate(errorStateLayout)
            ntModuleHandle = GetModuleHandleW(errorState, ntModuleName)
        }
    }

    /**
     * Returns the error code captured using the call state.
     *
     * @param callState the call state
     * @return the error code
     */
    fun getLastError(callState: MemorySegment): Int {
        return callStateGetLastErrorVarHandle.get(callState, 0) as Int
    }

    /**
     * Checks that the previous Windows API call was successful.
     *
     * Throws an exception with the error message otherwise.
     */
    fun checkSuccessful(errorState: MemorySegment) {
        val lastError = getLastError(errorState)
        check(lastError == WIN32_ERROR.ERROR_SUCCESS) { getErrorMessage(lastError) }
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