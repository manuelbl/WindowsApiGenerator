//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.metadata;

import java.util.function.UnaryOperator;

/**
 * Member (field) of a struct or enumeration type.
 */
public class Member {
    private final String name;
    private final int fieldIndex;
    private final Type type;
    private final Object value;
    private int offset;
    private int paddingAfter;

    /**
     * Creates a new member instance.
     *
     * @param name       member name
     * @param fieldIndex index within struct/enumeration
     * @param type       member type
     * @param value      member value
     */
    public Member(String name, int fieldIndex, Type type, Object value) {
        this.name = name;
        this.fieldIndex = fieldIndex;
        this.type = type;
        this.value = value;
    }

    /**
     * Gets the member name.
     *
     * @return the name
     */
    public String name() {
        return name;
    }

    /**
     * Gets the field index.
     *
     * @return the index
     */
    public int fieldIndex() {
        return fieldIndex;
    }

    /**
     * Gets the member type.
     *
     * @return the type
     */
    public Type type() {
        return type;
    }

    /**
     * Gets the member value.
     *
     * @return the value
     */
    public Object value() {
        return value;
    }

    /**
     * Gets the member offset within the struct.
     *
     * @return the offset (in bytes)
     */
    public int offset() {
        return offset;
    }

    /**
     * Sets the member offset within the struct.
     *
     * @param offset the offset (in bytes)
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Sets the padding length after this member.
     *
     * @return the padding length (in bytes)
     */
    public int paddingAfter() {
        return paddingAfter;
    }

    /**
     * Sets the padding length after this member.
     *
     * @param paddingAfter the padding length (in bytes)
     */
    public void setPaddingAfter(int paddingAfter) {
        this.paddingAfter = paddingAfter;
    }

    /**
     * Indicates if this member is part of a C bitfield.
     *
     * @return {@code true} if it is part of a bitfield, {@code false} otherwise
     */
    public boolean isBitField() {
        return name.equals("_bitfield");
    }

    /**
     * Creates a copy of this member, replacing its type.
     *
     * @param typeReplacer lambda providing the target type for the current type
     * @return the member copy
     */
    public Member duplicate(UnaryOperator<Type> typeReplacer) {
        return new Member(name, fieldIndex, typeReplacer.apply(type), value);
    }
}
