//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd.tables;

/**
 * Row of the "ImplMap" table.
 * <p>
 * See ECMA-335, II.22.22 ImplMap: 0x1C
 * </p>
 *
 * @param flags           mapping flags (a 2-byte bitmask of type PInvokeAttributes)
 * @param memberForwarded parent class (MemberForwarded coded index)
 * @param importName      import name (string index)
 * @param importScope     import scope (ModuleRef index)
 */
@SuppressWarnings("unused")
public record ImplMap(int flags, int memberForwarded, int importName, int importScope) {

    public static final int SUPPORTS_LAST_ERROR = 0x0040;

    public static final int CALL_CONV_MASK = 0x0700;
    public static final int CALL_CONV_PLATFORMAPI = 0x0100;
    public static final int CALL_CONV_CDECL = 0x0200;
    public static final int CALL_CONV_STDCALL = 0x0300;
    public static final int CALL_CONV_THISCALL = 0x0400;
    public static final int CALL_CONV_FASTCALL = 0x0500;
}
