//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.tests;

import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;

import static java.lang.foreign.MemorySegment.NULL;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static windows.win32.system.registry.Apis.RegCloseKey;
import static windows.win32.system.registry.Apis.RegOpenKeyExW;
import static windows.win32.system.registry.Apis.RegQueryValueExW;
import static windows.win32.system.registry.Constants.HKEY_LOCAL_MACHINE;
import static windows.win32.system.registry.REG_SAM_FLAGS.KEY_QUERY_VALUE;

class RegistryTest {

    @Test
    void readingRegistry_succeeds() {
        try (var arena = Arena.ofConfined()) {
            var keyHandleHolder = arena.allocate(ADDRESS);
            var errorCode = RegOpenKeyExW(
                    HKEY_LOCAL_MACHINE,
                    arena.allocateFrom("SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion", UTF_16LE),
                    0,
                    KEY_QUERY_VALUE,
                    keyHandleHolder
            );
            WindowsErrorAssert.assertThat(errorCode).isSuccessful();

            var keyHandle = keyHandleHolder.get(ADDRESS, 0);

            var typeHolder = arena.allocate(JAVA_INT);
            var sizeHolder = arena.allocate(JAVA_INT);
            var data = arena.allocate(JAVA_LONG, 200);
            sizeHolder.set(JAVA_INT, 0L, (int) data.byteSize());
            errorCode = RegQueryValueExW(
                    keyHandle,
                    arena.allocateFrom("ProductName", UTF_16LE),
                    NULL,
                    typeHolder,
                    data,
                    sizeHolder
            );
            WindowsErrorAssert.assertThat(errorCode).isSuccessful();

            var value = Windows.getUtf16String(data, 0, sizeHolder.get(JAVA_INT, 0));
            System.out.println("Windows version: " + value);

            errorCode = RegCloseKey(keyHandle);
            WindowsErrorAssert.assertThat(errorCode).isSuccessful();
        }
    }
}
