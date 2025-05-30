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
 * Iterable allowing to iterate a range of rows within a metadata table.
 *
 * @param <T> table row type
 */
public class TableRangeIterable<T> implements Iterable<T> {
    private final Function<Integer, T> creator;
    private final int startIndex;
    private final int endIndex;

    /**
     * Creates a new instance.
     * <p>
     * If {@code startIndex} is greater than {@code endIndex},
     * the iterator does not provide any elements.
     * </p>
     *
     * @param startIndex the index of the range start (inclusive)
     * @param endIndex   the index of the range end (inclusive)
     * @param creator    a lambda creating a table row instance for a given index
     */
    public TableRangeIterable(int startIndex, int endIndex, Function<Integer, T> creator) {
        this.creator = creator;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    @Override
    public Iterator<T> iterator() {

        return new Iterator<>() {
            private int index = startIndex;

            @Override
            public boolean hasNext() {
                return index <= endIndex;
            }

            @Override
            public T next() {
                if (index > endIndex)
                    throw new NoSuchElementException();
                var row = creator.apply(index);
                index += 1;
                return row;
            }
        };
    }
}
