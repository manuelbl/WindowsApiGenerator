//
// Windows API Generator for Java
// Copyright (c) 2026 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//

package net.codecrete.windowsapi.graalvm.json;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Deque;

import static net.codecrete.windowsapi.graalvm.json.JsonWriter.ElementType.END_ARRAY;
import static net.codecrete.windowsapi.graalvm.json.JsonWriter.ElementType.OPEN_ARRAY;
import static net.codecrete.windowsapi.graalvm.json.JsonWriter.ElementType.MEMBER_NAME;
import static net.codecrete.windowsapi.graalvm.json.JsonWriter.ElementType.NONE;
import static net.codecrete.windowsapi.graalvm.json.JsonWriter.ElementType.END_OBJECT;
import static net.codecrete.windowsapi.graalvm.json.JsonWriter.ElementType.OPEN_OBJECT;
import static net.codecrete.windowsapi.graalvm.json.JsonWriter.ElementType.VALUE;

/**
 * Writes JSON.
 */
@SuppressWarnings("UnusedReturnValue")
public class JsonWriter {

    enum ElementType {
        NONE,
        OPEN_OBJECT,
        END_OBJECT,
        OPEN_ARRAY,
        END_ARRAY,
        MEMBER_NAME,
        VALUE,
    }

    enum ContainerType {
        OBJECT,
        ARRAY,
    }

    private final Writer writer;
    private final Deque<ContainerType> containers = new ArrayDeque<>();
    private ElementType lastElement = ElementType.NONE;

    /**
     * Creates a new instance writing to the specified output writer.
     *
     * @param writer {@code Writer} instance for JSON output.
     */
    public JsonWriter(Writer writer) {
        this.writer = writer;
    }

    /**
     * Flushes the writer.
     * <p>
     * Any buffered data is written to the underlying writer.
     * </p>
     * @throws IOException if there was a problem writing to the output
     */
    public void flush() throws IOException {
        writer.flush();
    }

    /**
     * Writes the opening of a JSON object.
     * @return this writer
     * @throws IOException if there was a problem writing to the output
     */
    public JsonWriter openObject() throws IOException {
        insertFormatting(OPEN_OBJECT);
        writer.write('{');
        return this;
    }

    /**
     * Writes the closing of a JSON object.
     * @return this writer
     * @throws IOException if there was a problem writing to the output
     */
    public JsonWriter closeObject() throws IOException {
        insertFormatting(END_OBJECT);
        writer.write('}');
        if (containers.isEmpty())
            writeNewline(NONE);
        return this;
    }

    /**
     * Writes the opening of a JSON array.
     * @return this writer
     * @throws IOException if there was a problem writing to the output
     */
    public JsonWriter openArray() throws IOException {
        insertFormatting(OPEN_ARRAY);
        writer.write('[');
        return this;
    }

    /**
     * Writes the closing of a JSON array.
     * @return this writer
     * @throws IOException if there was a problem writing to the output
     */
    public JsonWriter closeArray() throws IOException {
        insertFormatting(END_ARRAY);
        writer.write(']');
        if (containers.isEmpty())
            writeNewline(NONE);
        return this;
    }

    /**
     * Writes a member in a JSON object.
     * @param name the member name
     * @param value the member value (must a simple value)
     * @return this writer
     * @throws IOException if there was a problem writing to the output
     */
    public JsonWriter member(String name, Object value) throws IOException {
        if (value != null) {
            memberName(name);
            value(value);
        }
        return this;
    }

    /**
     * Writes the member name in a JSON object.
     * @param name the member name
     * @return this writer
     * @throws IOException if there was a problem writing to the output
     */
    public JsonWriter memberName(String name) throws IOException {
        insertFormatting(MEMBER_NAME);
        writeMemberName(name);
        return this;
    }

    /**
     * Writes a value in a JSON object or array.
     * @param value the member value (must a simple value)
     * @return this writer
     * @throws IOException if there was a problem writing to the output
     */
    public JsonWriter value(Object value) throws IOException {
        insertFormatting(VALUE);
        writeValue(value);
        return this;
    }

    private void writeMemberName(String name) throws IOException {
        writeString(name);
        writer.write(": ");
    }

    private void writeValue(Object value) throws IOException {
        switch (value) {
            case null -> writer.write("null");
            case String str -> writeString(str);
            case Integer number -> writer.write(Integer.toString(number));
            case Long number -> writer.write(Long.toString(number));
            case Float number -> writer.write(Float.toString(number));
            case Double number -> writer.write(Double.toString(number));
            case Boolean bool -> writer.write(bool.toString());
            default -> throw new IllegalArgumentException("invalid JSON value of type " + value.getClass().getName());
        }
    }

    private void writeString(String str) throws IOException {
        var escaped = str
                .replace("\"", "\\\"")
                .replace("\\", "\\\\")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        writer.write('"');
        writer.write(escaped);
        writer.write('"');
    }

    @SuppressWarnings("java:S3776")
    private void insertFormatting(ElementType nextElement) throws IOException {

        var currentContainerType = containers.peek();
        if (nextElement == END_OBJECT && currentContainerType != ContainerType.OBJECT) {
            throw new IllegalStateException("current container is not an object");
        } else if (nextElement == END_ARRAY && currentContainerType != ContainerType.ARRAY) {
            throw new IllegalStateException("current container is not an array");
        }

        switch (lastElement) {
            case NONE, MEMBER_NAME -> {
                switch (nextElement) {
                    case OPEN_OBJECT, OPEN_ARRAY, VALUE -> { /* no formatting needed */ }
                    default -> throw new IllegalStateException("start of object, start of array or value expected");
                }
            }
            case OPEN_OBJECT -> {
                switch (nextElement) {
                    case MEMBER_NAME, OPEN_ARRAY, END_OBJECT -> writeNewline(nextElement);
                    default -> throw new  IllegalStateException("member name or end of object expected");
                }
            }
            case OPEN_ARRAY -> {
                switch (nextElement) {
                    case VALUE, OPEN_OBJECT, END_ARRAY -> writeNewline(nextElement);
                    default -> throw new  IllegalStateException("member name or end of array expected");
                }
            }
            case END_OBJECT, END_ARRAY, VALUE -> {
                switch (nextElement) {
                    case OPEN_OBJECT, OPEN_ARRAY, MEMBER_NAME, VALUE -> {
                        if (nextElement == MEMBER_NAME && currentContainerType != ContainerType.OBJECT)
                            throw new  IllegalStateException("member name is only allowed in open object");
                        writeComma();
                        writeNewline(nextElement);
                    }
                    case END_OBJECT, END_ARRAY -> writeNewline(nextElement);
                    default -> throw new IllegalStateException("internal error");
                }
            }
        }

        switch (nextElement) {
            case OPEN_OBJECT -> containers.push(ContainerType.OBJECT);
            case OPEN_ARRAY -> containers.push(ContainerType.ARRAY);
            case END_OBJECT, END_ARRAY -> containers.pop();
            default -> { /* container update */ }
        }

        lastElement = nextElement;
    }

    private void writeNewline(ElementType nextState) throws IOException {
        writer.write('\n');

        var indenting = containers.size();
        if (nextState == END_OBJECT || nextState == END_ARRAY)
            indenting -= 1;
        if (indenting >= 1)
            writer.write("    ".repeat(indenting));
    }

    private void writeComma() throws IOException {
        writer.write(",");
    }
}
