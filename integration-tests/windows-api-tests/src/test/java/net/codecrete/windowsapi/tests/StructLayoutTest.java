//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.tests;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import windows.win32.devices.usb.USB_DESCRIPTOR_REQUEST;
import windows.win32.devices.usb.WINUSB_SETUP_PACKET;
import windows.win32.globalization.UConverterToUnicodeArgs;
import windows.win32.networking.clustering.SR_RESOURCE_TYPE_REPLICATED_PARTITION_ARRAY;
import windows.win32.networking.wininet.INTERNET_CACHE_CONFIG_INFOW;
import windows.win32.networkmanagement.dhcp.DHCP_ALL_OPTIONS;
import windows.win32.storage.filesystem.FILE_ID_BOTH_DIR_INFO;
import windows.win32.ui.textservices.TF_LANGBARITEMINFO;
import windows.win32.ui.windowsandmessaging._DEV_BROADCAST_USERDEFINED;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class StructLayoutTest {

    private static final Map<String, Class<?>> LAYOUTS = Map.of(
            "TF_LANGBARITEMINFO", TF_LANGBARITEMINFO.class,
            "SR_RESOURCE_TYPE_REPLICATED_PARTITION_ARRAY", SR_RESOURCE_TYPE_REPLICATED_PARTITION_ARRAY.class,
            "_DEV_BROADCAST_USERDEFINED", _DEV_BROADCAST_USERDEFINED.class,
            "FILE_ID_BOTH_DIR_INFO", FILE_ID_BOTH_DIR_INFO.class,
            "DHCP_ALL_OPTIONS", DHCP_ALL_OPTIONS.class,
            "UConverterToUnicodeArgs", UConverterToUnicodeArgs.class,
            "WINUSB_SETUP_PACKET", WINUSB_SETUP_PACKET.class,
            "USB_DESCRIPTOR_REQUEST", USB_DESCRIPTOR_REQUEST.class,
            "INTERNET_CACHE_CONFIG_INFOW", INTERNET_CACHE_CONFIG_INFOW.class
    );

    @ParameterizedTest
    @MethodSource("provideLayoutData")
    void validateLayout(String type, long size, long alignment, int elementCount, List<MemberOffset> members) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        try (var arena = Arena.ofConfined()) {
            var layoutClass = LAYOUTS.get(type);
            assertThat(layoutClass).isNotNull();
            var layout = getLayout(layoutClass);
            var instance = allocate(layoutClass, arena, elementCount);

            assertThat(instance.byteSize())
                    .as("Layout %s: expected size %d bytes, actual size %d bytes",
                            type, size, instance.byteSize())
                    .isEqualTo(size);
            assertThat(layout.byteAlignment())
                    .as("Layout %s: expected alignment %d bytes, actual alignment %d bytes",
                            type, alignment, layout.byteAlignment())
                    .isEqualTo(alignment);

            for (var member : members) {
                var offset = layout.byteOffset(toPathElements(member.member));
                assertThat(offset)
                        .as("Layout %s, member %s: expected offset %d bytes, actual offset %d bytes",
                                type, member, member.offset, offset)
                        .isEqualTo(member.offset);
            }
        }
    }

    @SuppressWarnings("DataFlowIssue")
    private static Stream<Arguments> provideLayoutData() throws IOException {
        var arguments = new ArrayList<Arguments>();

        String type = null;
        long size = 0;
        long alignment = 0;
        int numElements = -1;
        List<MemberOffset> members = new ArrayList<>();

        try (var reader = new BufferedReader(new InputStreamReader(
                StructLayoutTest.class.getResourceAsStream("/struct_layout.txt"), StandardCharsets.UTF_8))) {
            var line = reader.readLine();
            while (line != null) {
                if (line.startsWith("T,")) {
                    if (type != null)
                        arguments.add(Arguments.of(type, size, alignment, numElements, members));
                    type = line.substring(2);
                    size = 0;
                    alignment = 0;
                    numElements = -1;
                    members = new ArrayList<>();
                } else if (line.startsWith("S,")) {
                    size = Long.parseLong(line.substring(2));
                } else if (line.startsWith("A,")) {
                    alignment = Long.parseLong(line.substring(2));
                } else if (line.startsWith("F,")) {
                    numElements = Integer.parseInt(line.substring(2));
                } else if (line.startsWith("O,")) {
                    var pieces = line.substring(2).split(",");
                    members.add(new MemberOffset(pieces[0], Long.parseLong(pieces[1])));
                }
                line = reader.readLine();
            }

            if (type != null)
                arguments.add(Arguments.of(type, size, alignment, numElements, members));
        }

        return arguments.stream();
    }

    private static MemoryLayout getLayout(Class<?> layoutClass) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        var method = layoutClass.getMethod("layout");
        return (MemoryLayout) method.invoke(null);
    }

    private static MemorySegment allocate(Class<?> layoutClass, Arena arena, int elementCount) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        var hasVariableSize = elementCount >= 0;
        if (hasVariableSize) {
            var method = layoutClass.getMethod("allocate", SegmentAllocator.class, int.class);
            return (MemorySegment) method.invoke(null, arena, elementCount);
        } else {
            var method = layoutClass.getMethod("allocate", SegmentAllocator.class);
            return (MemorySegment) method.invoke(null, arena);
        }
    }

    private static PathElement[] toPathElements(String path) {
        return Arrays.stream(path.split("\\."))
                .map(StructLayoutTest::toPathElement)
                .toArray(PathElement[]::new);
    }

    private static PathElement toPathElement(String member) {
        try {
            return PathElement.groupElement(Integer.parseInt(member));
        } catch (NumberFormatException e) {
            return PathElement.groupElement(member);
        }
    }


    record MemberOffset(String member, long offset) {}
}
