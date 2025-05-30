//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd.tables;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

/**
 * Iterable allowing to iterate a range of rows of a metadata table.
 * <p>
 * The rows within the range have the same primary or foreign key.
 * The table must be sorted by the key field, and the key field
 * must be the first field within the row.
 * The
 * </p>
 *
 * @param <T> table row type
 */
public class RowKeyTableIterable<T> implements Iterable<T> {
    private final Table table;
    private final Function<Integer, T> creator;
    private final int keyValue;
    private final int keyWidth;

    /**
     * Creates a new instance for the given table and key.
     *
     * @param table    the metadata table
     * @param keyValue the value of the key
     * @param keyWidth the width of the key field (in bytes)
     * @param creator  a lambda creating a new table row instance for a given index
     */
    public RowKeyTableIterable(Table table, int keyValue, int keyWidth, Function<Integer, T> creator) {
        this.table = table;
        this.creator = creator;
        this.keyValue = keyValue;
        this.keyWidth = keyWidth;
    }

    @Override
    public Iterator<T> iterator() {
        int startIndex = table.indexByPrimaryKey(keyValue, keyWidth, 0);

        return new Iterator<>() {
            private int index = startIndex;
            private boolean hasElement = startIndex != 0;

            @Override
            public boolean hasNext() {
                return hasElement;
            }

            @Override
            public T next() {
                if (!hasElement)
                    throw new NoSuchElementException();
                var row = creator.apply(index);
                hasElement = table.hasNext(index, keyValue, keyWidth);
                if (hasElement)
                    index += 1;
                return row;
            }
        };
    }
}
