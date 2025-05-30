//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.tests;

import org.junit.jupiter.api.Test;
import windows.win32.system.com.IUri;
import windows.win32.system.com.Uri_PROPERTY;

import java.lang.foreign.Arena;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static net.codecrete.windowsapi.tests.Windows.ADDRESS_UNBOUNDED;
import static org.assertj.core.api.Assertions.assertThat;
import static windows.win32.foundation.Apis.SysFreeString;
import static windows.win32.system.com.Apis.CreateUri;
import static windows.win32.system.com.URI_CREATE_FLAGS.Uri_CREATE_CANONICALIZE;

class ComTest {
    @Test
    void IURI_works() {
        try (var arena = Arena.ofConfined()) {
            var uriHolder = arena.allocate(ADDRESS);
            var uriString = arena.allocateFrom("https://github.com/manuelbl/WindowsApiGenerator", UTF_16LE);
            var result = CreateUri(uriString, Uri_CREATE_CANONICALIZE, 0, uriHolder);
            WindowsErrorAssert.assertThat(result).isSuccessful();

            var uri = IUri.wrap(uriHolder.get(IUri.addressLayout(), 0));

            var propertyHolder = arena.allocate(ADDRESS);
            result = uri.GetPropertyBSTR(Uri_PROPERTY.DOMAIN, propertyHolder, 0);
            WindowsErrorAssert.assertThat(result).isSuccessful();

            var property = propertyHolder.get(ADDRESS_UNBOUNDED, 0);
            var propertyText = property.getString(0, UTF_16LE);
            assertThat(propertyText).isEqualTo("github.com");

            SysFreeString(property);

            var boolHolder = arena.allocate(JAVA_INT);
            result = uri.HasProperty(Uri_PROPERTY.USER_INFO, boolHolder);
            WindowsErrorAssert.assertThat(result).isSuccessful();
            assertThat(boolHolder.get(JAVA_INT, 0)).isZero();

            uri.Release();
        }
    }
}
