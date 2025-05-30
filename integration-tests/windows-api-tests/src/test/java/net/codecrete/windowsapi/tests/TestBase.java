//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.lang.foreign.Arena;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

class TestBase {
    protected Arena arena;
    protected MemoryLayout errorStateLayout = Linker.Option.captureStateLayout();
    protected MemorySegment errorState;

    @BeforeEach
    void setUp() {
        arena = Arena.ofConfined();
        errorState = arena.allocate(errorStateLayout);
    }

    @AfterEach
    void tearDown() {
        arena.close();
    }
}
