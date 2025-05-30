//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.tests;

import org.junit.jupiter.api.Test;
import windows.win32.foundation.DEVPROPKEY;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static org.assertj.core.api.Assertions.assertThat;
import static windows.win32.devices.deviceanddriverinstallation.Constants.GUID_DEVCLASS_BLUETOOTH;
import static windows.win32.devices.properties.Constants.DEVPKEY_Device_FriendlyName;
import static windows.win32.system.com.Apis.StringFromGUID2;
import static windows.win32.system.diagnostics.etw.Constants.KERNEL_LOGGER_NAMEA;
import static windows.win32.system.diagnostics.etw.Constants.KERNEL_LOGGER_NAMEW;

class ConstantTest extends TestBase {

    @Test
    void stringConstant_hasExpectedValue() {
        var value = KERNEL_LOGGER_NAMEW().getString(0, UTF_16LE);
        assertThat(value).isEqualTo("NT Kernel Logger");
    }

    @Test
    void ansiStringConstant_hasExpectedValue() {
        // it should actually be windows-1252 instead of ASCII
        var value = KERNEL_LOGGER_NAMEA().getString(0, US_ASCII);
        assertThat(value).isEqualTo("NT Kernel Logger");
    }

    @Test
    void guidConstant_hasExpectedValue() {
        var buffer = arena.allocate(2 * 40);
        StringFromGUID2(GUID_DEVCLASS_BLUETOOTH(), buffer, 40);
        var formattedGuid = buffer.getString(0, UTF_16LE);
        assertThat(formattedGuid).isEqualTo("{E0CBF06C-CD8B-4647-BB8A-263B43F0F974}");
    }

    @Test
    void devPkeyConstant_hasExpectedValue() {
        var devpkey = DEVPKEY_Device_FriendlyName();
        var guid = DEVPROPKEY.fmtid(devpkey);
        var buffer = arena.allocate(2 * 40);
        StringFromGUID2(guid, buffer, 40);
        var formattedGuid = buffer.getString(0, UTF_16LE);
        assertThat(formattedGuid).isEqualTo("{A45C254E-DF1C-4EFD-8020-67D146A850E0}");
        assertThat(DEVPROPKEY.pid(devpkey)).isEqualTo(14);
    }
}
