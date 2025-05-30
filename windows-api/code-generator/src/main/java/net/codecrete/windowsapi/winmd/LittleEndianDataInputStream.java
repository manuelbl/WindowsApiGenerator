//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * A data input stream to read primitive Java data types
 * in little-endian organization from an underlying input stream.
 * <p>
 * Similar to {@link java.io.DataInputStream} but uses little-endian
 * instead of big-endian byte organization.
 * </p>
 */
public class LittleEndianDataInputStream {

    private final InputStream in;
    private int offset = 0;

    /**
     * Creates an instance using the specified underlying InputStream.
     *
     * @param in the specified input stream
     */
    public LittleEndianDataInputStream(InputStream in) {
        this.in = in;
    }

    /**
     * Returns the current offset into the stream.
     *
     * @return offset, in bytes
     */
    public long getOffset() {
        return offset;
    }

    /**
     * Reads data from the input.
     *
     * @param b the buffer into which the data is read.
     * @throws EOFException if this input stream reaches the end before reading all the bytes.
     * @throws IOException  if another error occurs during reading
     */
    public final void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    /**
     * Reads data from the input.
     *
     * @param b   the buffer into which the data is read.
     * @param off the start offset in the data array {@code b}.
     * @param len the number of bytes to read.
     * @throws EOFException if this input stream reaches the end before reading all the bytes.
     * @throws IOException  if another error occurs during reading
     */
    public final void readFully(byte[] b, int off, int len) throws IOException {
        Objects.checkFromIndexSize(off, len, b.length);
        int n = 0;
        while (n < len) {
            int count = in.read(b, off + n, len - n);
            if (count < 0)
                throw new EOFException();
            offset += count;
            n += count;
        }
    }

    public void skipNBytes(int n) throws IOException {
        if (n < 0)
            throw new IOException("Cannot skip backwards");

        while (n > 0) {
            long count = in.skip(n);
            if (count == 0)
                throw new EOFException();
            n -= (int) count;
            offset += (int) count;
        }
    }

    /**
     * Skips to the specified offset.
     * </p>
     *
     * @param offset number of bytes into the steam
     * @throws EOFException if the offset is after the end of the stream
     * @throws IOException  if an I/O error occurs
     */
    public void skipTo(int offset) throws IOException {
        skipNBytes(offset - this.offset);
    }

    /**
     * Reads a byte
     *
     * @return the next byte of this input stream as a signed 8-bit {@code byte}.
     * @throws EOFException if this input stream has reached the end.
     * @throws IOException  if any other I/O error occurs.
     */
    public final byte readByte() throws IOException {
        int ch = in.read();
        if (ch < 0)
            throw new EOFException();
        offset += 1;
        return (byte) (ch);
    }

    /**
     * Reads an unsigned 16-bit integer number.
     *
     * @return the next two bytes of this input stream, interpreted as an unsigned 16-bit integer.
     * @throws EOFException if this input stream has reached the end.
     * @throws IOException  if any other I/O error occurs.
     */
    public final int readUnsignedShort() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        offset += 2;
        return ch1 + (ch2 << 8);
    }

    /**
     * Reads a signed 32-bit integer number.
     *
     * @return the next four bytes of this input stream, interpreted as an {@code int}.
     * @throws EOFException if this input stream has reached the end.
     * @throws IOException  if any other I/O error occurs.
     */
    public final int readInt() throws IOException {
        var bytes = new byte[4];
        readFully(bytes);
        int ch1 = bytes[0] & 0xff;
        int ch2 = bytes[1] & 0xff;
        int ch3 = bytes[2] & 0xff;
        int ch4 = bytes[3] & 0xff;
        return (ch1 + (ch2 << 8) + (ch3 << 16) + (ch4 << 24));
    }

    /**
     * Reads a signed 64-bit integer number.
     *
     * @return the next 8 bytes of this input stream, interpreted as an {@code int}.
     * @throws EOFException if this input stream has reached the end.
     * @throws IOException  if any other I/O error occurs.
     */
    public final long readLong() throws IOException {
        var bytes = new byte[8];
        readFully(bytes);

        long result = 0;
        for (int i = 0; i < 8; i++)
            result += (long) (bytes[i] & 0xff) << (8 * i);
        return result;
    }
}
