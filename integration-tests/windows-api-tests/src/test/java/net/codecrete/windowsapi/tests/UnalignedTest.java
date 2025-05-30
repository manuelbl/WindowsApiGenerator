//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.tests;

import org.junit.jupiter.api.Test;
import windows.win32.system.console.COORD;
import windows.win32.ui.controls.TASKDIALOGCONFIG;
import windows.win32.ui.shell.NT_CONSOLE_PROPS;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static java.lang.foreign.ValueLayout.JAVA_SHORT_UNALIGNED;
import static org.assertj.core.api.Assertions.assertThat;

class UnalignedTest {
    @Test
    void unalignedPrimitive_shouldGetAndSet() {
        try (var arena = Arena.ofConfined()) {
            var segment = arena.allocate(NT_CONSOLE_PROPS.layout().byteSize() + 1, 8);
            var unaligned = segment.asSlice(1, NT_CONSOLE_PROPS.layout().byteSize());

            NT_CONSOLE_PROPS.uFontWeight(unaligned, 400);
            assertThat(NT_CONSOLE_PROPS.uFontWeight(unaligned)).isEqualTo(400);
        }
    }

    @Test
    void unalignedStruct_shouldGetAndSet() {
        try (var arena = Arena.ofConfined()) {
            var segment = arena.allocate(NT_CONSOLE_PROPS.layout().byteSize() + 1, 8);
            var unaligned = segment.asSlice(1, NT_CONSOLE_PROPS.layout().byteSize());

            var coord = COORD.allocate(arena);
            COORD.X(coord, (short) 100);
            COORD.Y(coord, (short) 200);
            NT_CONSOLE_PROPS.dwWindowSize(unaligned, coord);
            coord = NT_CONSOLE_PROPS.dwWindowSize(unaligned);
            assertThat(coord.get(JAVA_SHORT_UNALIGNED, COORD.X$offset())).isEqualTo((short) 100);
            assertThat(coord.get(JAVA_SHORT_UNALIGNED, COORD.Y$offset())).isEqualTo((short) 200);
        }
    }

    @Test
    void unalignedAddress_shouldGetAndSet() {
        try (var arena = Arena.ofConfined()) {
            var segment = arena.allocate(TASKDIALOGCONFIG.layout().byteSize() + 1, 8);
            var unaligned = segment.asSlice(1, TASKDIALOGCONFIG.layout().byteSize());

            TASKDIALOGCONFIG.pszContent(unaligned, MemorySegment.ofAddress(0x4008000L));
            assertThat(TASKDIALOGCONFIG.pszContent(unaligned).address()).isEqualTo(0x4008000L);
        }
    }
}
