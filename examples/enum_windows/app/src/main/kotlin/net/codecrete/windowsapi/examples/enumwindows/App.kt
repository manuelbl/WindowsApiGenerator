//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.examples.enumwindows

import net.codecrete.windowsapi.examples.enumwindows.Windows.checkSuccessful
import windows.win32.foundation.RECT
import windows.win32.ui.windowsandmessaging.Apis.EnumWindows
import windows.win32.ui.windowsandmessaging.Apis.GetWindowInfo
import windows.win32.ui.windowsandmessaging.Apis.GetWindowTextW
import windows.win32.ui.windowsandmessaging.WINDOWINFO
import windows.win32.ui.windowsandmessaging.WINDOW_STYLE.WS_VISIBLE
import windows.win32.ui.windowsandmessaging.WNDENUMPROC
import java.lang.foreign.Arena
import java.lang.foreign.Linker
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout.JAVA_CHAR
import java.nio.charset.StandardCharsets.UTF_16LE

val errorStateLayout: MemoryLayout = Linker.Option.captureStateLayout()

/**
 * Lists the visible windows
 */
fun main() {
    Arena.ofConfined().use { arena ->
        val errorState = arena.allocate(errorStateLayout)
        val enumFuncUpcall = WNDENUMPROC.allocate(arena) { windowHandle, _ -> windowEnumerationFunction(windowHandle) }
        EnumWindows(errorState, enumFuncUpcall, 0)
        checkSuccessful(errorState)
    }
}

/**
 * Function called by `EnumWindows` for each window
 */
fun windowEnumerationFunction(windowHandle: MemorySegment): Int {
    Arena.ofConfined().use { arena ->
        val errorState = arena.allocate(errorStateLayout)

        val windowInfo = WINDOWINFO.allocate(arena)
        GetWindowInfo(errorState, windowHandle, windowInfo)
        checkSuccessful(errorState)

        val dwStyle = WINDOWINFO.dwStyle(windowInfo)
        if ((dwStyle and WS_VISIBLE) == 0)
            return@use // window is not visible

        val titleBarTextBuffer = arena.allocate(JAVA_CHAR, 300)
        GetWindowTextW(errorState, windowHandle, titleBarTextBuffer, 300)
        checkSuccessful(errorState)
        val titleBarText = titleBarTextBuffer.getString(0, UTF_16LE)
        if (titleBarText.isEmpty())
            return@use // window has no title bar text

        val size = WINDOWINFO.rcWindow(windowInfo)
        val left = RECT.left(size)
        val top = RECT.top(size)
        val right = RECT.right(size)
        val bottom = RECT.bottom(size)

        println("\"$titleBarText\" at ($left, $top), size ${right - left} x ${bottom - top}")
    }
    return 1
}
