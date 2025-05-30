//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.tests;

import org.assertj.core.api.AbstractIntegerAssert;

import java.lang.foreign.MemorySegment;

public class WindowsResultAssert extends AbstractIntegerAssert<WindowsResultAssert> {

    protected WindowsResultAssert(int actual) {
        super(actual, WindowsResultAssert.class);
    }

    public static WindowsResultAssert assertThat(int actual) {
        return new WindowsResultAssert(actual);
    }

    public WindowsResultAssert isSuccessful(MemorySegment errorState) {
        if (actual == 0) {
            var lastError = Windows.getLastError(errorState);
            failWithMessage("Expected successful result but got 0x%08x (%s)",
                    lastError, Windows.getErrorMessage(lastError));
        }

        return this;
    }
}
