//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd;

import net.codecrete.windowsapi.metadata.Architecture;
import net.codecrete.windowsapi.metadata.LazyString;

import java.util.UUID;

/**
 * Custom attributes data.
 * <p>
 * The extracted values from custom attributes relevant for this software.
 * </p>
 */
class CustomAttributeData {
    /**
     * Indicates that the type is a TypeDef (alias).
     */
    boolean isTypedef = false;
    /**
     * Indicates that the type is marked as obsolete.
     */
    boolean isObsolete = false;
    /**
     * GUID associated with this type.
     */
    UUID guidConstant = null;
    /**
     * Indicates that an enumeration uses values that can be combined as a bitmask to encode multiple flags.
     */
    boolean isEnumFlags = false;
    /**
     * Supported processor architectures.
     */
    int supportedArchitecture = Architecture.ALL;
    /**
     * URL to Microsoft's documentation.
     */
    LazyString documentationUrl = null;
    /**
     * Value of a constant.
     */
    Object constantValue = null;
    /**
     * Indicates that a string value is encoded as ANSI (Windows-1252).
     */
    boolean isAnsiEncoding = false;
    /**
     * Indicates that an array has a flexible size.
     */
    boolean isFlexibleArray = false;
    /**
     * Name of field use to set the struct size.
     */
    String structSizeField = null;
}
