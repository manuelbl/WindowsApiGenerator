//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.writer;

import net.codecrete.windowsapi.metadata.ConstantValue;
import net.codecrete.windowsapi.metadata.Namespace;
import net.codecrete.windowsapi.metadata.Primitive;
import net.codecrete.windowsapi.metadata.Type;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/**
 * Creates the Java code for constants.
 */
class ConstantCodeWriter extends JavaCodeWriter<Type> {

    /**
     * Creates a new instance.
     *
     * @param generationContext the code generation context
     */
    ConstantCodeWriter(GenerationContext generationContext) {
        super(generationContext);
    }

    /**
     * Creates a new file with the Java code for the specified constants.
     *
     * @param namespace the metadata namespace
     * @param constants the constants
     */
    void writeConstants(Namespace namespace, Collection<ConstantValue> constants) {
        withFile(namespace, null, "Constants", () -> writeConstantsContent(constants));
    }

    void writeConstantsContent(Collection<ConstantValue> constants) {
        var needsArena = constants.stream().anyMatch(constant -> !(constant.value() instanceof Number));
        var hasGuids = constants.stream().anyMatch(constant -> constant.value() instanceof UUID);
        var hasPropertyKeys = constants.stream().anyMatch(ConstantCodeWriter::isPropertyKey);

        writer.printf("""
                package %s;
                
                import java.lang.foreign.*;
                
                /**
                 * Constants of namespace %s.
                 */
                public class Constants {
                """, packageName, namespace.name());

        if (needsArena)
            writer.print("""
                        private static final Arena ARENA = Arena.ofAuto();
                    
                    """);

        if (hasGuids)
            writeCreateGuidMethod(4);

        if (hasPropertyKeys)
            writer.print("""
                        private static MemorySegment createPropertyKey(long v1, long v2, int v3) {
                            var seg = ARENA.allocate(20, 4);
                            seg.set(ValueLayout.JAVA_LONG, 0, v1);
                            seg.set(ValueLayout.JAVA_LONG, 8, v2);
                            seg.set(ValueLayout.JAVA_INT, 16, v3);
                            return seg;
                        }
                    
                    """);

        for (var constant : constants)
            writeConstant(constant);

        writer.println("}");
    }

    private static final Set<String> POINTER_STRUCT_TYPES = Set.of("CONDITION_VARIABLE", "SRWLOCK", "INIT_ONCE");

    private void writeConstant(ConstantValue constant) {
        var typeName = constant.type().name();

        switch (constant.value()) {
            case String ignored -> {
                if (isPropertyKey(constant)) {
                    writePropertyKey(constant);
                } else if (typeName.equals("SID_IDENTIFIER_AUTHORITY")) {
                    writeByteArrayConstant(constant);
                } else if (POINTER_STRUCT_TYPES.contains(typeName)) {
                    writePointerStruct(constant);
                } else {
                    writeStringConstant(constant);
                }
            }
            case UUID ignored -> writeGuidConstant(constant);
            case Number ignored -> writeNumericConstant(constant);
            default -> throw new AssertionError("Unexpected constant type: " + constant.type());
        }
    }

    private void writeNumericConstant(ConstantValue constant) {
        String typeName;
        if (constant.type() instanceof Primitive primitive)
            typeName = CommentWriter.getPrimitiveCType(primitive);
        else
            typeName = constant.type().name();

        writer.printf("""
                    /**
                     * Numeric constant {@code %s} (%s).
                     */
                """, constant.name(), typeName);
        writer.printf("    public static final %s %s = ", getJavaType(constant.type()), constant.name());
        writeValue(constant.type(), constant.value());
        writer.println(";");
        writer.println();
    }

    private void writeStringConstant(ConstantValue constant) {
        var value = constant.value().toString();
        // FFM does not support Windows-1252 charset.
        // Ensure the string is the same in UTF-8.
        assert !constant.isAnsiEncoding()
                || Arrays.compare(
                value.getBytes(StandardCharsets.UTF_8),
                value.getBytes(Charset.forName("windows-1252"))
        ) == 0;

        var stringType = String.format("%s, null-terminated", constant.isAnsiEncoding() ? "ANSI" : "UTF-16");
        writer.printf("    private static final MemorySegment %s$SEG = ARENA.allocateFrom(\"%s\"%s);%n%n",
                constant.name(),
                value
                        .replace("\\", "\\\\")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r"),
                !constant.isAnsiEncoding() ? ", java.nio.charset.StandardCharsets.UTF_16LE" : ""
        );

        writer.printf("""
                    /**
                     * String constant {@code %s} (%s).
                     */
                """, constant.name(), stringType);
        writeMemorySegmentConstant(constant.name());
    }

    private void writeGuidConstant(ConstantValue constant) {
        writeGuidConstantMemorySegment(constant.name(), (UUID) constant.value(), 4);

        writer.printf("""
                    /**
                     * GUID constant {@code %s} ({@code {%s}}).
                     */
                """, constant.name(), constant.value());
        writeMemorySegmentConstant(constant.name());
    }

    private static boolean isPropertyKey(ConstantValue constant) {
        return constant.type().name().equals("PROPERTYKEY") || constant.type().name().equals("DEVPROPKEY");
    }

    private void writePropertyKey(ConstantValue constant) {
        assert constant.value() instanceof String;
        var numbers = parseNumbers((String) constant.value());
        assert numbers.length == 12;

        var data1 = (long) numbers[0];
        var data2 = numbers[1] << 32;
        var data3 = numbers[2] << 48;
        var v1 = data1 | data2 | data3;

        var v2 = 0L;
        for (int i = 10; i >= 3; i--)
            v2 = (v2 << 8) | numbers[i];

        var v3 = numbers[11].intValue();

        writer.printf("""
                    private static final MemorySegment %s$SEG = createPropertyKey(%dL, %dL, %d);
                
                """, constant.name(), v1, v2, v3);

        writer.printf("""
                    /**
                     * Property key constant {@code %s}.
                     */
                """, constant.name());
        writeMemorySegmentConstant(constant.name());
    }

    private void writeByteArrayConstant(ConstantValue constant) {
        var numbers = parseNumbers(constant.value().toString());

        writer.printf("    private static final MemorySegment %s$SEG = ARENA.allocateFrom(ValueLayout.JAVA_BYTE",
                constant.name());
        for (var number : numbers) {
            writer.print(", (byte) ");
            writer.print(number.intValue());
        }
        writer.println(");");
        writer.println();

        writer.printf("""
                    /**
                     * Binary constant {@code %s}.
                     */
                """, constant.name());
        writeMemorySegmentConstant(constant.name());
    }

    private void writePointerStruct(ConstantValue constant) {
        // The struct consists of a single pointer (address).
        writer.printf("""
                    private static final MemorySegment %s$SEG = ARENA.allocateFrom(ValueLayout.JAVA_LONG, %sL);
                
                """, constant.name(), constant.value());

        writer.printf("""
                    /**
                     * %s constant {@code %s}.
                     */
                """, constant.type().name(), constant.name());
        writeMemorySegmentConstant(constant.name());
    }

    private static Long[] parseNumbers(String value) {
        var numbers = value
                .replace('{', ' ')
                .replace('}', ' ')
                .replace(" ", "")
                .split(",");
        return Arrays.stream(numbers).map(Long::parseLong).toArray(Long[]::new);
    }

    private void writeMemorySegmentConstant(String name) {
        writer.printf("""
                    public static MemorySegment %1$s() {
                        return %1$s$SEG;
                    }
                
                """, name);
    }
}
