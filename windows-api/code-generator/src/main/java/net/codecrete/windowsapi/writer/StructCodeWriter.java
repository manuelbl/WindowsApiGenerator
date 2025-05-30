//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.writer;

import net.codecrete.windowsapi.metadata.Array;
import net.codecrete.windowsapi.metadata.EnumType;
import net.codecrete.windowsapi.metadata.Member;
import net.codecrete.windowsapi.metadata.Pointer;
import net.codecrete.windowsapi.metadata.Primitive;
import net.codecrete.windowsapi.metadata.Struct;
import net.codecrete.windowsapi.metadata.Type;
import net.codecrete.windowsapi.metadata.TypeAlias;
import net.codecrete.windowsapi.winmd.LayoutRequirement;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Creates the Java code for a C structs and unions.
 */
class StructCodeWriter extends JavaCodeWriter<Struct> {

    private static final String STRUCT = "struct";
    private static final String UNION = "union";

    private final CommentWriter commentWriter = new CommentWriter();

    /**
     * Creates a new instance.
     *
     * @param generationContext the code generation context
     */
    StructCodeWriter(GenerationContext generationContext) {
        super(generationContext);
    }

    /**
     * Creates a new Java file containing the Java code for a struct or union.
     *
     * @param struct struct or union type
     */
    void writeStructOrUnion(Struct struct) {
        var className = toJavaClassName(struct.name());
        withFile(struct.namespace(), struct, className, this::writeStructOrUnionContent);
    }

    void writeStructOrUnionContent() {
        writer.printf("""
                package %s;
                
                import java.lang.foreign.*;
                import static java.lang.foreign.ValueLayout.*;
                
                """, packageName);

        writeStructComment();

        writer.printf("""
                public class %s {
                """, className);

        // add required primitive address layouts
        AddressLayout.requiredLayouts(type).forEach(layoutType ->
                writeAddressLayoutInitialization(layoutType, "private static final "));
        writer.println();

        writeUnalignedStructLayouts();
        writeLayout();
        writeFieldAccessors(type, 0, "", "");
        writeAllocationMethods();
        writeArrayAccessMethods();

        // private constructor
        writer.printf("""
                    private %s () {}
                """, className);

        writer.println("}");
    }

    private void writeLayout() {
        writer.print("    private static final GroupLayout $LAYOUT = ");
        writeStructLayout(type.packageSize());
        writer.println(";");
        writer.println();

        var elementType = type.isUnion() ? UNION : STRUCT;
        var comment = String.format("Gets the layout of the %s {@code %s}.", elementType, type.nativeName());
        var note = !type.hasFixedSize()
                ? String.format(
                "Note: The variable size of this %s cannot be represented. "
                        + "The field {@code %s} is set to a fixed size of %d elements.",
                elementType, type.flexibleArrayMember().name(),
                ((Array) type.flexibleArrayMember().type()).arrayLength()
        )
                : null;
        writeCommentWithNotes(comment, note);
        writer.print("""
                    public static GroupLayout layout() {
                        return $LAYOUT;
                    }
                
                """);

        comment = String.format("Gets the size of the %s {@code %s} (in bytes).", elementType, type.nativeName());
        note = !type.hasFixedSize()
                ? "Note: Since this " + elementType
                + " has a variable size, the returned size is a fixed value for the minimal size."
                : null;
        writeCommentWithNotes(comment, note);
        writer.printf("""
                    public static long sizeof() {
                        return %dL;
                    }
                
                """, type.structSize());
    }

    private void writeAllocationMethods() {
        var elementType = type.isUnion() ? UNION : STRUCT;
        var allocationComment = String.format("Allocates a new memory segment for the %s {@code %s}.", elementType,
                type.nativeName());
        var cbSizeNote = type.structSizeMember() != null
                ? String.format("The field {@code %s} is set to the size of the allocated %s.",
                type.structSizeMember(), elementType)
                : null;
        var flexibleArrayNote = !type.hasFixedSize()
                ? String.format(
                "The allocation is sized such that the flexible array {@code %s} can hold {@code elementCount} " +
                        "elements.",
                type.flexibleArrayMember().name())
                : null;

        if (type.hasFixedSize()) {
            if (type.structSizeMember() == null) {
                writeComment(allocationComment);
                writer.printf("""
                            public static MemorySegment allocate(SegmentAllocator allocator) {
                                return allocator.allocate(%dL, %dL);
                            }
                        
                        """, type.structSize(), type.packageSize());
            } else {
                writeCommentWithNotes(allocationComment, cbSizeNote);
                writer.printf("""
                                    public static MemorySegment allocate(SegmentAllocator allocator) {
                                        var segment = allocator.allocate(%dL, %dL);
                                        %s(segment, %s);
                                        return segment;
                                    }
                                
                                """, type.structSize(), type.packageSize(), type.structSizeMember(),
                        getStructSizeExpression(type));
            }
        } else {
            var flexibleMemberArray = (Array) type.flexibleArrayMember().type();
            var elementSize = LayoutRequirement.forType(flexibleMemberArray.itemType()).size();
            var packageSize = type.packageSize();
            var offset = flexibleMemberOffset(type, type.flexibleArrayMember(), type);
            var fixedSize = offset + type.packageSize() - 1;
            String sizeExpression = getSizeExpression(fixedSize, elementSize, packageSize);

            if (type.structSizeMember() == null) {
                writeCommentWithNotes(allocationComment, flexibleArrayNote);
                writer.printf("""
                            public static MemorySegment allocate(SegmentAllocator allocator, int elementCount) {
                                return allocator.allocate(%s, %dL);
                            }
                        
                        """, sizeExpression, type.packageSize());
            } else {
                writeCommentWithNotes(allocationComment, flexibleArrayNote, cbSizeNote);
                writer.printf("""
                                    public static MemorySegment allocate(SegmentAllocator allocator, int elementCount) {
                                        var segment = allocator.allocate(%s, %dL);
                                        %s(segment, %s);
                                        return segment;
                                    }
                                
                                """, sizeExpression, type.packageSize(), type.structSizeMember(),
                        getStructSizeExpression(type));
            }
        }
    }

    private void writeArrayAccessMethods() {
        if (type.hasFixedSize()) {
            var comment = String.format(
                    "Gets the element with index {@code index} of the specified {@code %s} array.",
                    type.nativeName());
            var note = "The returned memory segment is a slice of {@code array} and shares the backing memory.";
            writeCommentWithNotes(comment, note);
            writer.printf("""
                        public static MemorySegment elementAsSlice(MemorySegment array, long index) {
                            return array.asSlice(%dL * index, %1$dL);
                        }
                    
                    """, type.structSize());

            var elementType = type.isUnion() ? UNION : STRUCT;
            writeComment("Allocates an array of {@code elementCount} elements of this %s.", elementType);
            writer.printf("""
                        public static MemorySegment allocateArray(long elementCount, SegmentAllocator allocator) {
                            return allocator.allocate(%dL * elementCount, %dL);
                        }
                    
                    """, type.structSize(), type.packageSize());
        }
    }

    private static String getSizeExpression(long fixedSize, int elementSize, int packageSize) {
        String sizeExpression;
        if (elementSize != 1) {
            if (packageSize != 1) {
                sizeExpression = String.format("(%dL + elementCount * %dL) & -%dL", fixedSize, elementSize,
                        packageSize);
            } else {
                sizeExpression = String.format("%dL + elementCount * %dL", fixedSize, elementSize);
            }
        } else {
            if (packageSize != 1) {
                sizeExpression = String.format("(%dL + elementCount) & -%dL", fixedSize, packageSize);
            } else {
                sizeExpression = String.format("%dL + elementCount", fixedSize);
            }
        }
        return sizeExpression;
    }

    private static String getStructSizeExpression(Struct struct) {
        assert struct.structSizeMember() != null;
        var structSizeMember = struct.members().stream()
                .filter(it -> it.name().equals(struct.structSizeMember()))
                .findFirst().orElseThrow();
        return getJavaIntegerConstant(getPrimitiveJavaType((Primitive) structSizeMember.type()), struct.structSize());
    }

    private void writeStructLayout(int packageSize) {
        writer.println(type.isUnion() ? "MemoryLayout.unionLayout(" : "MemoryLayout.structLayout(");
        writeFieldsLayout(8, type.members(), packageSize);
        writeIndent(4);
        writer.print(")");
    }

    private void writeFieldAccessors(Struct type, long offset, String prefix, String nativePrefix) {
        assert type.members() != null;

        for (var field : type.members()) {
            var fieldType = field.type();
            var fieldOffset = type.isUnion() ? offset : offset + field.offset();

            if (fieldType instanceof Struct struct && struct.isNested()) {
                var nestedPrefix = fieldType.isAnonymous() ? prefix : prefix + field.name() + "_";
                var nestedNativePrefix = fieldType.isAnonymous() ? nativePrefix : nativePrefix + field.name() + ".";
                writeFieldAccessors(struct, fieldOffset, nestedPrefix, nestedNativePrefix);
            } else {
                writeFieldAccessors(field, fieldOffset, prefix, nativePrefix);
            }
        }
    }

    @SuppressWarnings("java:S125")
    private void writeFieldAccessors(Member field, long offset, String prefix, String nativePrefix) {
        var fieldName = field.name();
        var nativeFieldName = field.name();
        var fieldType = field.type();
        if (field.isBitField()) {
            int bitFieldNum = consumeBitFieldNumber();
            fieldName = "_bitfield" + bitFieldNum;
        }
        if (fieldName.equals("boolean"))
            fieldName = "boolean_";
        var dataType = getJavaType(fieldType);

        // offset constant
        writeComment("Gets the offset of the field {@code %s%s} (in bytes).", nativePrefix, nativeFieldName);
        writer.printf("""
                    public static long %s%s$offset() {
                        return %dL;
                    }
                
                """, prefix, fieldName, offset);

        // getter and setter
        if (fieldType instanceof Struct || fieldType instanceof Array) {
            var requirement = LayoutRequirement.forType(fieldType);
            var alignment = Math.min(requirement.alignment(), type.packageSize());
            if (fieldType instanceof Array array && array.isFlexible()) {
                var comment = String.format("Gets the flexible array field {@code %s%s} of {@code segment}.", nativePrefix,
                        nativeFieldName);
                var note1 = "The returned memory segment is a slice of {@code segment} and shares the backing memory.";
                var note2 = "The length of the returned slice is derived from the size of {@code segment}: " +
                        "It extends from the start of the flexible array to the end the segment.";
                writeCommentWithNotes(comment, note1, note2);
                writer.printf("""
                            public static %s %s%s(MemorySegment segment) {
                                return segment.asSlice(%dL, segment.byteSize() - %dL, %dL);
                            }
                        
                        """, dataType, prefix, fieldName, offset, offset, alignment);

                comment = String.format("Copies {@code value} into the flexible array field {@code %s%s} in {@code segment}.",
                        nativePrefix, nativeFieldName);
                note1 = "The number of bytes copied is the smaller of the size of {@code value} and the available " +
                        "space in {@code segment} (from the flexible array start to the end of the segment).";
                writeCommentWithNotes(comment, note1);
                writer.printf("""
                            public static void %1$s%2$s(MemorySegment segment, MemorySegment value) {
                                MemorySegment.copy(value, 0L, segment, %3$dL, Math.min(value.byteSize(), segment.byteSize() - %3$dL));
                            }
                        
                        """, prefix, fieldName, offset);
            } else {
                var comment = String.format("Gets the field {@code %s%s} of {@code segment}.", nativePrefix,
                        nativeFieldName);
                var note = "The returned memory segment is a slice of {@code segment} and shares the backing memory.";
                writeCommentWithNotes(comment, note);
                writer.printf("""
                            public static %s %s%s(MemorySegment segment) {
                                return segment.asSlice(%dL, %dL, %dL);
                            }
                        
                        """, dataType, prefix, fieldName, offset, requirement.size(), alignment);

                writeComment("Copies {@code value} into the field {@code %s%s} in {@code segment}.", nativePrefix,
                        nativeFieldName);
                writer.printf("""
                            public static void %s%s(MemorySegment segment, MemorySegment value) {
                                MemorySegment.copy(value, 0L, segment, %dL, %dL);
                            }
                        
                        """, prefix, fieldName, offset, requirement.size());
            }
        } else {
            writeComment("Gets the value of field {@code %s%s} in {@code segment}.", nativePrefix, nativeFieldName);
            var layoutName = getLayoutName(fieldType, type.packageSize(), namespace);
            writer.printf("""
                        public static %s %s%s(MemorySegment segment) {
                            return segment.get(%s, %dL);
                        }
                    
                    """, dataType, prefix, fieldName, layoutName, offset);

            writeComment("Sets the field {@code %s%s} in {@code segment} to {@code value}.", nativePrefix,
                    nativeFieldName);
            writer.printf("""
                        public static void %s%s(MemorySegment segment, %s value) {
                            segment.set(%s, %dL, value);
                        }
                    
                    """, prefix, fieldName, dataType, layoutName, offset);
        }
    }

    private void writeFieldsLayout(int indenting, List<Member> fields, int packageSize) {
        int numFields = fields.size();
        for (int i = 0; i < numFields; i += 1) {
            var field = fields.get(i);
            writeField(indenting, field, packageSize, i == numFields - 1 && field.paddingAfter() == 0);
            writePadding(indenting, field, i == numFields - 1);
        }
    }

    private void writeField(int indenting, Member field, int packageSize, boolean isLast) {
        writeIndent(indenting);
        writeFfmTypeLayout(indenting, field.type(), packageSize);
        if (!isAnonymous(field)) {
            writer.print(".withName(\"");
            writer.print(field.name());
            writer.print("\")");
        }
        if (!isLast)
            writer.print(",");
        writer.println();
    }

    private void writeFfmTypeLayout(int indenting, Type type, int packageSize) {
        switch (type) {
            case Primitive primitive -> writer.print(getPrimitiveLayoutName(primitive, packageSize));
            case Array arrayType -> {
                writer.printf("MemoryLayout.sequenceLayout(%dL, ", arrayType.arrayLength());
                writeFfmTypeLayout(indenting, arrayType.itemType(), packageSize);
                writer.print(")");
            }
            case EnumType enumType -> writeFfmTypeLayout(indenting, enumType.baseType(), packageSize);
            case TypeAlias typeAlias -> writeFfmTypeLayout(indenting, typeAlias.aliasedType(), packageSize);
            case Pointer pointer -> writer.print(getLayoutName(pointer, packageSize, null));
            case Struct struct -> {
                if (type.namespace() == null) {
                    writer.println(struct.isUnion() ? "MemoryLayout.unionLayout(" : "MemoryLayout.structLayout(");
                    writeFieldsLayout(indenting + 4, struct.members(), packageSize);
                    writeIndent(indenting);
                    writer.print(")");
                } else {
                    writeStructLayoutName(struct, packageSize, namespace);
                }
            }
            default -> writer.print(packageSize >= 8 ? "ADDRESS" : "ADDRESS_UNALIGNED");
        }
    }

    private void writePadding(int indenting, Member field, boolean isLast) {
        if (field.paddingAfter() == 0)
            return;

        writeIndent(indenting);
        writer.printf("MemoryLayout.paddingLayout(%d)", field.paddingAfter());

        if (!isLast)
            writer.print(",");
        writer.println();
    }

    private static final Pattern ANONYMOUS_NAME_PATTERN = Pattern.compile("^Anonymous\\d?$");

    private static boolean isAnonymous(Member field) {
        return ANONYMOUS_NAME_PATTERN.matcher(field.name()).find();
    }

    private static long flexibleMemberOffset(Struct struct, Member flexibleMember, Struct rootStruct) {
        var lastMember = struct.members().getLast();
        if (flexibleMember == lastMember)
            return flexibleMember.offset();

        if (lastMember.type() instanceof Struct lastMemberStruct)
            return lastMember.offset() + flexibleMemberOffset(lastMemberStruct, flexibleMember, rootStruct);

        throw new AssertionError("Flexible member " + flexibleMember.name() + " not found in struct " + rootStruct.name());
    }

    private void writeUnalignedStructLayouts() {
        var unalignedStructs = getUnalignedMemberStructs(type, type.packageSize());
        if (!unalignedStructs.isEmpty()) {
            var unalignedStructList = unalignedStructs.stream().sorted(Comparator.comparing(Struct::name)).toList();
            for (var struct : unalignedStructList) {
                writer.printf("""
                                    private static final MemoryLayout %s$LAYOUT_UNALIGNED = align1(%s);
                                
                                """,
                        struct.name(),
                        getLayoutName(struct, namespace));

                writeComment("Gets a layout for {@code %s} with relaxed alignment constraints", struct.name());
                writer.printf("""
                            public static MemoryLayout %1$s$unalignedLayout() {
                                return %1$s$LAYOUT_UNALIGNED;
                            }
                        
                        """, struct.name());
            }
            writeUnalignFunction();
        }
    }

    private void writeUnalignFunction() {
        writer.printf("""
                    private static MemoryLayout align1(MemoryLayout layout) {
                        return switch (layout) {
                            case PaddingLayout p -> p;
                            case ValueLayout v -> v.withByteAlignment(1);
                            case GroupLayout g -> {
                                var members = g.memberLayouts().stream().map(%s::align1).toArray(MemoryLayout[]::new);
                                yield g instanceof StructLayout ? MemoryLayout.structLayout(members) : MemoryLayout.unionLayout(members);
                            }
                            case SequenceLayout s -> MemoryLayout.sequenceLayout(s.elementCount(), align1(s.elementLayout()));
                        };
                    }
                
                """, className);
    }

    private void writeStructComment() {
        var dataStructure = type.isUnion() ? UNION : STRUCT;
        var postfix = "";
        if (type.isUnion())
            postfix = " (union)";
        if (!type.name().equals(type.nativeName())) {
            if (type.name().endsWith("_X64"))
                postfix += " (X64 only)";
            if (type.name().endsWith("_ARM64"))
                postfix += " (ARM64 only)";
        }

        writer.printf("""
                        /**
                         * {@code %1$s} structure%2$s
                        """,
                type.nativeName(),
                postfix);

        commentWriter.writeStructSnippet(writer, type);

        writer.print("""
                 * </p>
                """);

        if (!type.hasFixedSize()) {
            writer.printf("""
                     * <p>
                     * This struct contains the flexible array {@code %s}. It has a variable number of elements.
                     * Thus, the size of this %s is variable.
                     * </p>
                    """, type.flexibleArrayMember().name(), dataStructure);
        }

        writeDocumentationUrl(type);

        writer.println(" */");
    }

    private static Set<Struct> getUnalignedMemberStructs(Struct struct, int packageSize) {
        var unalignedStructs = new HashSet<Struct>();
        collectUnalignedMemberStructs(struct, packageSize, unalignedStructs);
        return unalignedStructs;
    }

    private static void collectUnalignedMemberStructs(Type type, int packageSize, Set<Struct> unalignedStructs) {
        if (type instanceof Struct struct) {
            if (struct.packageSize() > packageSize && !struct.isNested())
                unalignedStructs.add(struct);

            for (var member : struct.members())
                collectUnalignedMemberStructs(member.type(), packageSize, unalignedStructs);
        } else if (type instanceof Array arrayType) {
            collectUnalignedMemberStructs(arrayType.itemType(), packageSize, unalignedStructs);
        }
    }
}
