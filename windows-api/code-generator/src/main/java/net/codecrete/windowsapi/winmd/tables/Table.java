//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd.tables;

import java.util.stream.IntStream;

/**
 * Metadata table.
 * <p>
 * Metadata tables consist of multiple integer columns that are either 2 or 4 bytes wide.
 * </p>
 * <p>
 * This class also serves as an accessor for these tables.
 * </p>
 */
@SuppressWarnings("java:S4274")
public class Table {

    private final int numRows;
    private int width; // in bytes
    private int[] columnWidths;
    private byte[] data;

    /**
     * Creates a new instance.
     *
     * @param numRows number of rows
     */
    public Table(int numRows) {
        this.numRows = numRows;
    }

    /**
     * Number of rows.
     *
     * @return number of rows
     */
    public int numRows() {
        return numRows;
    }

    /**
     * Width of the table (column length).
     *
     * @return width, in bytes
     */
    public int width() {
        return width;
    }

    /**
     * The width of the indexes for this table.
     *
     * @return width, in bytes
     */
    public int indexWidth() {
        return numRows < 65536 ? 2 : 4;
    }

    /**
     * Sets the width of the columns.
     *
     * @param widths width for each column, in bytes
     */
    public void setColumnWidths(int... widths) {
        columnWidths = widths;
        width = IntStream.of(widths).sum();
    }

    /**
     * Sets the table data.
     *
     * @param data table contents as a byte array
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * Loads the row data into the array of integers.
     *
     * @param index  row index
     * @param values array to be filled
     */
    public void getRow(int index, int[] values) {
        assert index > 0 && index <= numRows;
        int offset = (index - 1) * width;
        int numColumns = columnWidths.length;
        for (int i = 0; i < numColumns; i += 1) {
            int columnWidth = columnWidths[i];
            values[i] = getInt(offset, columnWidth);
            offset += columnWidth;
        }
    }

    /**
     * Gets the value of the specified row and column.
     *
     * @param rowIndex    row index
     * @param columnIndex column index
     * @return the value
     */
    public int getValue(int rowIndex, int columnIndex) {
        assert rowIndex > 0 && rowIndex <= numRows;
        assert columnIndex > 0 && columnIndex <= columnWidths.length;
        int columnOffset = 0;
        for (int i = 0; i < columnIndex; i += 1)
            columnOffset += columnWidths[i];
        int offset = (rowIndex - 1) * width + columnOffset;
        return getInt(offset, columnWidths[columnIndex]);
    }

    /**
     * Finds the first index for the specified (primary) key.
     * <p>
     * Only works for sorted tables. See ECMA-335, II.22 Metadata logical format: tables.
     * </p>
     *
     * @param key       the key to search for
     * @param keyWidth  the width of the key, in bytes
     * @param keyOffset offset of the key from the start of the row, in bytes
     * @return the row index, or 0 if the key was not found
     */
    public int indexByPrimaryKey(int key, int keyWidth, int keyOffset) {
        // binary search
        int left = 0;
        int right = numRows - 1;

        while (left < right) {
            int mid = (left + right) >>> 1;
            int midKey = getInt(mid * width + keyOffset, keyWidth);

            if (midKey < key)
                left = mid + 1;
            else
                right = mid;
        }

        if (left < numRows && getInt(left * width + keyOffset, keyWidth) == key)
            return left + 1;

        return 0;
    }

    /**
     * Tests if the row after the specified row has the given (primary) key.
     *
     * @param index    row index
     * @param key      the key to search for
     * @param keyWidth the width of the key, in bytes
     * @return {@code true} if the next row has the given key, {@code false} otherwise
     */
    public boolean hasNext(int index, int key, int keyWidth) {
        return index < numRows && getInt(index * width, keyWidth) == key;
    }

    private int getInt16(int offset) {
        int byte1 = data[offset] & 0xff;
        int byte2 = data[offset + 1] & 0xff;
        return byte1 + (byte2 << 8);
    }

    private int getInt32(int offset) {
        int byte1 = data[offset] & 0xff;
        int byte2 = data[offset + 1] & 0xff;
        int byte3 = data[offset + 2] & 0xff;
        int byte4 = data[offset + 3] & 0xff;
        return byte1 + (byte2 << 8) + (byte3 << 16) + (byte4 << 24);
    }

    private int getInt(int offset, int indexLength) {
        return (indexLength == 2) ? getInt16(offset) : getInt32(offset);
    }
}
