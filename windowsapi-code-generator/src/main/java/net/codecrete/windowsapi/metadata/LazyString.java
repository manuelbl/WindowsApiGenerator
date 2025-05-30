//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.metadata;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A string that is only decoded if needed.
 *
 * @param blob   BLOB containing the UTF-8 decoded string
 * @param offset offset to the start of the string (in bytes)
 * @param length length of the string (in number of UTF-8 code points, i.e., in bytes)
 */
@SuppressWarnings("java:S6218")
public record LazyString(byte[] blob, int offset, int length) {
    public String toString() {
        return new String(blob, offset, length, UTF_8);
    }
}
