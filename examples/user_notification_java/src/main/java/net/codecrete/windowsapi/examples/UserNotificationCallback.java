//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.examples;

import windows.win32.foundation.POINT;
import windows.win32.system.com.IUnknown;
import windows.win32.ui.shell.IUserNotificationCallback;

import java.lang.foreign.MemorySegment;

import static windows.win32.foundation.Constants.S_OK;

/**
 * Implementation of COM interface `IUserNotificationCallback`
 */
public class UserNotificationCallback extends ComObject implements IUserNotificationCallback {
    private static final MemorySegment[] implementedIIDs
            = new MemorySegment[]{IUnknown.iid(), IUserNotificationCallback.iid()};

    public UserNotificationCallback() {
        super(implementedIIDs);
    }

    @Override
    public int OnBalloonUserClick(MemorySegment pt) {
        printAction("clicked balloon", pt);
        return S_OK;
    }

    @Override
    public int OnLeftClick(MemorySegment pt) {
        printAction("clicked icon", pt);
        return S_OK;
    }

    @Override
    public int OnContextMenu(MemorySegment pt) {
        printAction("triggered context menu", pt);
        return S_OK;
    }

    @SuppressWarnings("java:S106")
    private void printAction(String action, MemorySegment point) {
        var x = POINT.x(point);
        var y = POINT.y(point);
        System.out.printf("%s at (%d, %d)%n", action, x, y);
    }
}
