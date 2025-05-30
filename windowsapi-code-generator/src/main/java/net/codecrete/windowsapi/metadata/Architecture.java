//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.metadata;

/**
 * Constants describing the supported architecture of functions, data structures, etc.
 * <p>
 * The constants can be combined as a bitmask. They must use the same values as the Windows metadata format.
 * </p>
 */
public class Architecture {
    private Architecture() {
    }

    /**
     * X86 (32-bit Intel/AMD)
     */
    public static final int X86 = 1;
    /**
     * X64 (64-bit Intel/AMD, aka X86_64)
     */
    public static final int X64 = 2;
    /**
     * ARM64 (64-bit ARM, aka AArch64)
     */
    public static final int ARM64 = 4;
    /**
     * Combination of X86, X64 and ARM64
     */
    public static final int ALL = 7;
}
