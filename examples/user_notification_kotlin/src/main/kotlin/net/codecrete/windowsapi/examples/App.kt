//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.examples

import net.codecrete.windowsapi.examples.Windows.checkSuccessful
import windows.win32.foundation.Constants.S_FALSE
import windows.win32.foundation.Constants.S_OK
import windows.win32.foundation.WIN32_ERROR
import windows.win32.system.com.Apis.CoCreateInstance
import windows.win32.system.com.Apis.CoInitializeEx
import windows.win32.system.com.CLSCTX
import windows.win32.system.com.COINIT
import windows.win32.ui.shell.Constants.UserNotification
import windows.win32.ui.shell.IQueryContinue
import windows.win32.ui.shell.IUserNotification2
import windows.win32.ui.shell.IUserNotificationCallback
import windows.win32.ui.shell.NOTIFY_ICON_INFOTIP_FLAGS.NIIF_INFO
import windows.win32.ui.shell.NOTIFY_ICON_INFOTIP_FLAGS.NIIF_RESPECT_QUIET_TIME
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment.NULL
import java.lang.foreign.ValueLayout.ADDRESS
import kotlin.text.Charsets.UTF_16LE

fun main() {
    // Initialize COM with apartment-threaded object concurrency
    var result = CoInitializeEx(NULL, COINIT.APARTMENTTHREADED)
    check(result == WIN32_ERROR.ERROR_SUCCESS) { Windows.getErrorMessage(result) }

    Arena.ofConfined().use { arena ->
        // Create a user notification instance (implemented by Windows)
        val holder = arena.allocate(ADDRESS)
        result = CoCreateInstance(UserNotification(), NULL, CLSCTX.ALL, IUserNotification2.iid(), holder)
        checkSuccessful(result)

        // Wrap the COM instance in an easy-to-use Java/Kotlin object
        val notification = IUserNotification2.wrap(holder.get(IUserNotification2.addressLayout(), 0))

        // Configure a balloon info
        val title = arena.allocateFrom("Windows API", UTF_16LE)
        val text = arena.allocateFrom("Hello from Java", UTF_16LE)
        result = notification.SetBalloonInfo(title, text, NIIF_INFO and NIIF_RESPECT_QUIET_TIME)
        checkSuccessful(result)

        // Create an IQueryContinue instance (implemented in Kotlin)
        val queryContinue = QueryContinue()
        val queryContinueSegment = IQueryContinue.create(queryContinue, arena)
        queryContinue.thisPointer = queryContinueSegment

        // Create an IUserNotification instance (implemented in Kotlin)
        val callback = UserNotificationCallback()
        val callbackSegment = IUserNotificationCallback.create(callback, arena)
        callback.thisPointer = callbackSegment

        // Show the notification
        result = notification.Show(queryContinueSegment, 5000, callbackSegment)
        checkSuccessful(result)

        notification.Release()
    }
}
