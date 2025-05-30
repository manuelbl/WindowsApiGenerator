//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd;

import java.nio.charset.StandardCharsets;

/**
 * A chunk of the BLOB heap.
 *
 * <p>
 * Instances of this class maintain an offset that
 * moves when data is read from the BLOB.
 * </p>
 */
public class Blob {

    private final byte[] data;
    private int offset;
    private final int end;

    /**
     * Creates a new instance.
     *
     * @param data   blob heap
     * @param offset offset into blob heap
     * @param length length.
     */
    Blob(byte[] data, int offset, int length) {
        this.data = data;
        this.offset = offset;
        this.end = offset + length;
    }

    /**
     * Gets the entire BLOB heap.
     *
     * @return the heap
     */
    byte[] data() {
        return data;
    }

    /**
     * Gets the offset of this chunk within the BLOB heap.
     *
     * @return the offset
     */
    int offset() {
        return offset;
    }

    /**
     * Checks if the offset is at the end of this chunk.
     *
     * @return {@code true} if it is at then, {@code false} otherwise
     */
    boolean isAtEnd() {
        return offset == end;
    }

    /**
     * Reads a byte from this chunk.
     *
     * @return the byte
     */
    int readByte() {
        assert offset < end;
        int result = data[offset] & 0xff;
        offset += 1;
        return result;
    }

    /**
     * Reads an unsigned 16-bit integer from this chunk.
     *
     * @return the integer
     */
    int readUInt16() {
        assert offset + 2 <= end;
        int byte1 = data[offset] & 0xff;
        int byte2 = data[offset + 1] & 0xff;
        offset += 2;
        return (byte2 << 8) + byte1;
    }

    /**
     * Reads a signed 32-bit integer from this chunk.
     *
     * @return the integer
     */
    int readInt32() {
        assert offset + 4 <= end;
        int byte1 = data[offset] & 0xff;
        int byte2 = data[offset + 1] & 0xff;
        int byte3 = data[offset + 2] & 0xff;
        int byte4 = data[offset + 3] & 0xff;
        offset += 4;
        return (byte4 << 24) + (byte3 << 16) + (byte2 << 8) + byte1;
    }

    /**
     * Reads a signed 64-bit integer from this chunk.
     *
     * @return the integer
     */
    long readInt64() {
        assert offset + 8 <= end;
        long result = 0;
        for (int i = 0; i < 8; i += 1)
            result |= (long) (data[offset + i] & 0xff) << i * 8;
        offset += 8;
        return result;
    }

    /**
     * Reads a UTF-8 encoded string from this chunk.
     *
     * @return the string
     */
    String readUtf8String() {
        int len = readCompressedUnsignedInt();
        assert offset + len <= end;
        offset += len;
        return new String(data, offset - len, len, StandardCharsets.UTF_8);
    }

    /**
     * Reads a UTF-16 encoded string from this chunk.
     *
     * @return the string
     */
    String readUtf16String() {
        int len = end - offset;
        assert (len & 1) == 0;
        offset = end;
        return new String(data, offset - len, len, StandardCharsets.UTF_16LE);
    }

    /**
     * Reads a compressed unsigned 32-bit integer from this chunk.
     *
     * @return the integer
     */
    int readCompressedUnsignedInt() {
        int byte1 = readByte();
        if (byte1 <= 127)
            return byte1;
        int byte2 = readByte();
        if ((byte1 & 0xc0) == 0x80)
            return ((byte1 & 0x3f) << 8) + byte2;

        if ((byte1 & 0xe0) == 0xc0) {
            int byte3 = readByte();
            int byte4 = readByte();
            return ((byte1 & 0x1f) << 24) + (byte2 << 16) + (byte3 << 8) + byte4;
        }

        assert false : "Invalid compressed integer";
        return 0;
    }

    /**
     * Skips several bytes in this chunk.
     *
     * @param bytes the number of bytes to skip
     */
    void skip(int bytes) {
        offset += bytes;
    }
}
