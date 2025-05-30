//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.examples

import windows.win32.foundation.Constants.S_FALSE
import windows.win32.system.com.IUnknown
import windows.win32.ui.shell.IQueryContinue

private val implementedIIDs = arrayOf(IUnknown.iid(), IQueryContinue.iid())

/**
 * Implementation of COM interface `IQueryContinue`
 */
class QueryContinue : IQueryContinue, ComObject(implementedIIDs) {
    override fun QueryContinue(): Int {
        // indicate that the notification should stop
        return S_FALSE
    }
}