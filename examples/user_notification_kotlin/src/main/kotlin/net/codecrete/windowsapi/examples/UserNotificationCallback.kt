//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.examples

import windows.win32.foundation.Constants.S_OK
import windows.win32.foundation.POINT
import windows.win32.system.com.IUnknown
import windows.win32.ui.shell.IUserNotificationCallback
import java.lang.foreign.MemorySegment

private val implementedIIDs = arrayOf(IUnknown.iid(), IUserNotificationCallback.iid())

/**
 * Implementation of COM interface `IUserNotificationCallback`
 */
class UserNotificationCallback : ComObject(implementedIIDs), IUserNotificationCallback {
    override fun OnBalloonUserClick(pt: MemorySegment): Int {
        printAction("clicked balloon", pt)
        return S_OK
    }

    override fun OnLeftClick(pt: MemorySegment): Int {
        printAction("clicked icon", pt)
        return S_OK
    }

    override fun OnContextMenu(pt: MemorySegment): Int {
        printAction("triggered context menu", pt)
        return S_OK
    }

    private fun printAction(action: String, point: MemorySegment) {
        val x = POINT.x(point)
        val y = POINT.y(point)
        println("$action at ($x, $y)")
    }
}