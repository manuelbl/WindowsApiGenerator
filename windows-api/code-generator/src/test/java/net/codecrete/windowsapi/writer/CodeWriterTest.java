//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.writer;

import net.codecrete.windowsapi.SimpleEventListener;
import net.codecrete.windowsapi.Testing;
import net.codecrete.windowsapi.events.Event;
import net.codecrete.windowsapi.events.EventListener;
import net.codecrete.windowsapi.metadata.Metadata;
import net.codecrete.windowsapi.winmd.MetadataBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CodeWriterTest {
    private final Metadata metadata = MetadataBuilder.load();

    @Test
    void writeAll_succeeds() {
        var eventListener = new FileNameListener();
        var codeWriter = new CodeWriter(metadata, Path.of("."), eventListener);
        codeWriter.setDryRun(true);
        assertDoesNotThrow(codeWriter::writeAll);
        var duplicateFiles = eventListener.filePaths.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey).toList();
        assertThat(duplicateFiles).isEmpty();
        // The following number must not change unless the metadata has been updated
        assertThat(eventListener.filePaths).hasSize(31367);
    }

    @Test
    void writeSingle_succeeds() throws IOException {
        var temporaryDirectory = Files.createTempDirectory("winapi");
        try {
            var eventListener = new FileCountEventListener();
            var scope = new Scope(metadata, new SimpleEventListener());
            scope.addStructs(Set.of("USB_NODE_CONNECTION_INFORMATION_EX"));
            scope.buildTransitiveScope();
            var codeWriter = new CodeWriter(metadata, temporaryDirectory, eventListener);
            codeWriter.setBasePackage("basepackage");
            codeWriter.setDryRun(false);
            codeWriter.write(scope);
            assertThat(temporaryDirectory.resolve("basepackage/windows/win32/devices/usb" +
                    "/USB_NODE_CONNECTION_INFORMATION_EX.java")).exists();
            assertThat(eventListener.fileCount).isEqualTo(5);

        } finally {
            Testing.deleteDirectory(temporaryDirectory);
        }
    }

    @Test
    void writeMultiples_succeeds() {
        var eventListener = new FileCountEventListener();
        var scope = new Scope(metadata, new SimpleEventListener());
        scope.addFunctions(Set.of(
                "FormatMessageW",
                "LocalFree",
                "GetModuleHandleW",
                "CLSIDFromString",
                "SetupDiCreateDeviceInfoList",
                "SetupDiDeleteDeviceInterfaceData",
                "SetupDiDestroyDeviceInfoList",
                "SetupDiEnumDeviceInfo",
                "SetupDiEnumDeviceInterfaces",
                "SetupDiGetClassDevsW",
                "SetupDiGetDeviceInterfaceDetailW",
                "SetupDiGetDevicePropertyW",
                "SetupDiOpenDevRegKey",
                "SetupDiOpenDeviceInfoW",
                "SetupDiOpenDeviceInterfaceW",
                "RegCloseKey",
                "RegQueryValueExW",
                "GetQueuedCompletionStatus",
                "CreateIoCompletionPort",
                "CreateFileW",
                "WinUsb_Initialize",
                "WinUsb_GetAssociatedInterface",
                "WinUsb_SetCurrentAlternateSetting",
                "WinUsb_WritePipe",
                "WinUsb_ReadPipe",
                "WinUsb_SetPipePolicy",
                "WinUsb_ResetPipe",
                "WinUsb_AbortPipe",
                "WinUsb_Free",
                "DeviceIoControl",
                "RegisterClassExW",
                "CreateWindowExW",
                "RegisterDeviceNotificationW",
                "GetMessageW",
                "DefWindowProcW"
        ));
        scope.addStructs(Set.of(
                "DEV_BROADCAST_DEVICEINTERFACE_W",
                "DEV_BROADCAST_HDR",
                "USB_DESCRIPTOR_REQUEST"
        ));
        scope.addEnums(Set.of(
                "FORMAT_MESSAGE_OPTIONS",
                "SETUP_DI_PROPERTY_CHANGE_SCOPE",
                "WIN32_ERROR",
                "REG_SAM_FLAGS",
                "GENERIC_ACCESS_RIGHTS",
                "DEV_BROADCAST_HDR_DEVICE_TYPE"
        ));
        scope.addConstants(Set.of(
                "USB_REQUEST_GET_DESCRIPTOR",
                "GUID_DEVINTERFACE_USB_DEVICE",
                "GUID_DEVINTERFACE_USB_HUB",
                "DEVPKEY_Device_Address",
                "DEVPKEY_Device_InstanceId",
                "DEVPKEY_Device_Parent",
                "DEVPKEY_Device_Service",
                "DEVPKEY_Device_Children",
                "DEVPKEY_Device_HardwareIds",
                "STATUS_UNSUCCESSFUL",
                "DIREG_DEV",
                "INFINITE",
                "IOCTL_USB_GET_NODE_CONNECTION_INFORMATION_EX",
                "IOCTL_USB_GET_DESCRIPTOR_FROM_NODE_CONNECTION",
                "HWND_MESSAGE",
                "WM_DEVICECHANGE",
                "DBT_DEVICEARRIVAL",
                "DBT_DEVICEREMOVECOMPLETE"
        ));
        var codeWriter = new CodeWriter(metadata, Path.of("."), eventListener);
        codeWriter.setDryRun(true);
        assertDoesNotThrow(() -> codeWriter.write(scope));
        assertThat(eventListener.fileCount).isEqualTo(16);
    }

    static class FileCountEventListener implements EventListener {

        int fileCount = 0;

        @Override
        public void onEvent(Event event) {
            if (event instanceof Event.JavaSourceGenerated)
                fileCount += 1;
        }
    }

    static class FileNameListener implements EventListener {

        List<Path> filePaths = new ArrayList<>();

        @Override
        public void onEvent(Event event) {
            if (event instanceof Event.JavaSourceGenerated(Path path))
                filePaths.add(path);
        }
    }
}
