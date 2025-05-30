//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.examples;

import windows.win32.system.com.IUnknown;
import windows.win32.ui.shell.IQueryContinue;

import java.lang.foreign.MemorySegment;

import static windows.win32.foundation.Constants.S_FALSE;

/**
 * Implementation of COM interface `IQueryContinue`
 */
public class QueryContinueObject extends ComObject implements IQueryContinue {
    private static final MemorySegment[] implementedIIDs = new MemorySegment[]{IUnknown.iid(), IQueryContinue.iid()};

    public QueryContinueObject() {
        super(implementedIIDs);
    }

    @Override
    public int QueryContinue() {
        // indicate that the notification should stop
        return S_FALSE;
    }
}
