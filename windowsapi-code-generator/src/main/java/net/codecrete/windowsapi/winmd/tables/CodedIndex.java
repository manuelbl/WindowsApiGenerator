//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd.tables;

/**
 * Structured coded index consisting of table and row index.
 * <p>
 * See ECMA-335, II.24.2.6 #~ stream
 * </p>
 *
 * @param table table number (see {@link MetadataTables})
 * @param index row index
 */
public record CodedIndex(int table, int index) {

    /**
     * Creates a new instance by decoding the index value for a coded index for the given list of tables.
     *
     * @param rawIndex the raw index value
     * @param tables   the tables participating in the coded index
     * @return a new instance
     */
    public static CodedIndex decode(int rawIndex, int[] tables) {
        int numBitsTable = 32 - Integer.numberOfLeadingZeros(tables.length - 1);
        int tableMask = (1 << numBitsTable) - 1;
        return new CodedIndex(tables[rawIndex & tableMask], rawIndex >> numBitsTable);
    }

    /**
     * Encodes the table and index into an integer number for the given list of tables.
     *
     * @param table  table index
     * @param index  row index
     * @param tables table indexes, in the order specified by the standard.
     * @return coded index (as a number)
     */
    public static int encode(int table, int index, int[] tables) {
        int numBitsTable = 32 - Integer.numberOfLeadingZeros(tables.length - 1);
        for (int i = 0; i < tables.length; i++) {
            if (tables[i] == table)
                return (index << numBitsTable) | i;
        }

        assert false : "Invalid table or table list";
        return 0;
    }

    /**
     * Indicates if the coded index represents a <i>null</i> value.
     *
     * @return {@code true} if the index is a <i>null</i> value, {@code false} otherwise
     */
    public boolean isNull() {
        return index == 0;
    }
}
