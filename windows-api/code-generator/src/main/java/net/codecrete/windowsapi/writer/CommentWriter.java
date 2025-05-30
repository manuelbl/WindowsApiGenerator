//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.writer;

import net.codecrete.windowsapi.metadata.Array;
import net.codecrete.windowsapi.metadata.Member;
import net.codecrete.windowsapi.metadata.Method;
import net.codecrete.windowsapi.metadata.Pointer;
import net.codecrete.windowsapi.metadata.Primitive;
import net.codecrete.windowsapi.metadata.PrimitiveKind;
import net.codecrete.windowsapi.metadata.Struct;
import net.codecrete.windowsapi.metadata.Type;

import java.io.PrintWriter;
import java.util.List;

/**
 * Writes C comments and C functions signature as Java comments.
 */
class CommentWriter {

    /**
     * Writes a C struct/union declaration as a Java comment snippet.
     *
     * @param writer the print writer
     * @param struct the struct
     */
    void writeStructSnippet(PrintWriter writer, Struct struct) {
        writer.printf("""
                         * {@snippet lang=c :
                         * %s %s {
                        """,
                struct.isUnion() ? "union" : "struct",
                struct.nativeName());

        writeStructMembers(writer, 4, struct.members());

        writer.print("""
                 * }
                 * }
                """);
    }

    private void writeStructMembers(PrintWriter writer, int indenting, List<Member> fields) {
        for (Member field : fields) {
            writeField(writer, indenting, field);
        }
    }

    private void writeField(PrintWriter writer, int indenting, Member field) {
        writeIndentAfterAsterisk(writer, indenting);
        writeCType(writer, indenting, field.type());
        writer.print(" ");
        writer.print(field.name());
        writer.println(";");
    }

    private void writeCType(PrintWriter writer, int indenting, Type type) {
        switch (type) {
            case Struct struct when struct.namespace() == null -> {
                writer.println(struct.isUnion() ? "union {" : "struct {");
                writeStructMembers(writer, indenting + 4, struct.members());
                writeIndentAfterAsterisk(writer, indenting);
                writer.print("}");
            }
            case Array array -> {
                writeShortCType(writer, array.itemType());
                writer.print("[");
                writer.print(array.arrayLength());
                writer.print("]");
            }
            default -> writeShortCType(writer, type);
        }
    }

    /**
     * Writes a Java comment for a function including the C signature as a snippet.
     *
     * @param writer   the writer
     * @param function the method
     * @param label    the label for the function or similar construct
     */
    void writeFunctionComment(PrintWriter writer, Method function, String label) {
        writer.printf("""
                    /**
                     * {@code %2$s} %1$s
                     * <p>
                     * {@snippet lang=c :
                """, label, function.nativeName());

        writeFunctionSignatureIntro(writer, function, function.nativeName(), 4);
        writeFunctionSignatureParameters(writer, function, 4);

        writer.print("""
                     * );
                     * }
                     * </p>
                """);

        if (function.supportsLastError())
            writer.print("""
                         * <p>
                         * The additional first parameter takes a memory segment to capture the call state (replacement for {@code GetLastError()}).
                         * </p>
                    """);

        writeDocumentationUrl(writer, function);

        writer.println("     */");
    }

    /**
     * Writes the C function intro (return type, function name, opening parenthesis=
     *
     * @param writer       the writer
     * @param function     the function (as a metadata method)
     * @param functionName the function name
     * @param indenting    the indenting before the comment asterisk (number of spaces)
     */
    void writeFunctionSignatureIntro(PrintWriter writer, Method function, String functionName, int indenting) {
        writeIndentBeforeAsterisk(writer, indenting);
        if (function.hasReturnType()) {
            writeShortCType(writer, function.returnType());
        } else {
            writer.print("void");
        }

        writer.print(" ");
        writer.print(functionName);
        writer.println("(");
    }

    /**
     * Writes the C function parameters (without parentheses)
     *
     * @param writer    the writer
     * @param function  the function
     * @param indenting the indenting before the comment asterisk (number of spaces)
     */
    void writeFunctionSignatureParameters(PrintWriter writer, Method function, int indenting) {
        for (int i = 0; i < function.parameters().length; i += 1) {
            var parameter = function.parameters()[i];
            writeIndentBeforeAsterisk(writer, indenting);
            writer.print("    ");
            writeShortCType(writer, parameter.type());
            writer.print(" ");
            writer.print(parameter.name());
            if (i != function.parameters().length - 1)
                writer.print(",");
            writer.println();
        }
    }

    private void writeDocumentationUrl(PrintWriter writer, Method function) {
        var documentationUrl = function.documentationUrl();
        if (documentationUrl != null) {
            writer.printf("""
                                 *
                                 * @see <a href="%1$s">%2$s (Microsoft)</a>
                            """,
                    documentationUrl,
                    function.nativeName()
            );
        }
    }

    static void writeShortCType(PrintWriter writer, Type type) {
        switch (type) {
            case Primitive primitive -> writer.print(getPrimitiveCType(primitive));
            case Pointer pointer -> {
                writeShortCType(writer, pointer.referencedType());
                writer.print("*");
            }
            default -> writer.print(type.nativeName());
        }
    }

    static String getPrimitiveCType(Primitive type) {
        return switch (type.kind()) {
            case PrimitiveKind.INT64 -> "LONGLONG";
            case PrimitiveKind.UINT64 -> "ULONGLONG";
            case PrimitiveKind.INT_PTR -> "LONG_PTR";
            case PrimitiveKind.UINT_PTR -> "ULONG_PTR";
            case PrimitiveKind.INT32 -> "LONG";
            case PrimitiveKind.UINT32 -> "DWORD";
            case PrimitiveKind.UINT16 -> "WORD";
            case PrimitiveKind.INT16 -> "SHORT";
            case PrimitiveKind.BYTE -> "BYTE";
            case PrimitiveKind.SBYTE -> "INT8";
            case PrimitiveKind.CHAR -> "WCHAR";
            case PrimitiveKind.SINGLE -> "FLOAT";
            case PrimitiveKind.DOUBLE -> "DOUBLE";
            case PrimitiveKind.BOOL -> "BOOL";
            case PrimitiveKind.VOID -> "void";
            default -> throw new AssertionError("Unexpected primitive type: " + type.name());
        };
    }

    private static final String SPACES_BEFORE_ASTERISK = "        ".repeat(10) + " * ";

    private void writeIndentBeforeAsterisk(PrintWriter writer, int indenting) {
        writer.write(SPACES_BEFORE_ASTERISK, SPACES_BEFORE_ASTERISK.length() - indenting - 3, indenting + 3);
    }

    private static final String SPACES_AFTER_ASTERISK = " *" + "        ".repeat(10);

    private void writeIndentAfterAsterisk(PrintWriter writer, int indenting) {
        writer.write(SPACES_AFTER_ASTERISK, 0, indenting + 2);
    }
}
