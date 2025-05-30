//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.tests;

import org.junit.jupiter.api.Test;
import windows.win32.system.processstatus.PROCESS_MEMORY_COUNTERS;

import static org.assertj.core.api.Assertions.assertThat;

import static windows.win32.system.processstatus.Apis.GetProcessMemoryInfo;
import static windows.win32.system.threading.Apis.GetCurrentProcess;

class QueryMemoryTest extends TestBase {

    @Test
    void GetProcessMemoryInfo_succeeds() {
        var counters = arena.allocate(PROCESS_MEMORY_COUNTERS.layout());

        var result = GetProcessMemoryInfo(
                errorState,
                GetCurrentProcess(),
                counters,
                (int) counters.byteSize());

        WindowsResultAssert.assertThat(result).isSuccessful(errorState);
        assertThat(PROCESS_MEMORY_COUNTERS.cb(counters)).isEqualTo(72);
        assertThat(PROCESS_MEMORY_COUNTERS.WorkingSetSize(counters)).isGreaterThan(100000);
        assertThat(PROCESS_MEMORY_COUNTERS.PageFaultCount(counters)).isGreaterThan(1000);
    }
}
