//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.tests;

import org.assertj.core.api.AbstractIntegerAssert;

import static windows.win32.foundation.WIN32_ERROR.ERROR_SUCCESS;

public class WindowsErrorAssert extends AbstractIntegerAssert<WindowsErrorAssert> {
    protected WindowsErrorAssert(int actual) {
        super(actual, WindowsErrorAssert.class);
    }

    public static WindowsErrorAssert assertThat(int actual) {
        return new WindowsErrorAssert(actual);
    }

    public WindowsErrorAssert isSuccessful() {
        if (actual != ERROR_SUCCESS)
            failWithMessage("Expected successful result but got 0x%08x (%s)",
                    actual, Windows.getErrorMessage(actual));
        return this;
    }}
