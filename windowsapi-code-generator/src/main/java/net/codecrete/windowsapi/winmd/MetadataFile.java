//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd;

import net.codecrete.windowsapi.winmd.tables.ClassLayout;
import net.codecrete.windowsapi.winmd.tables.Constant;
import net.codecrete.windowsapi.winmd.tables.CustomAttribute;
import net.codecrete.windowsapi.winmd.tables.Field;
import net.codecrete.windowsapi.winmd.tables.FieldLayout;
import net.codecrete.windowsapi.winmd.tables.ImplMap;
import net.codecrete.windowsapi.winmd.tables.InterfaceImpl;
import net.codecrete.windowsapi.winmd.tables.MemberRef;
import net.codecrete.windowsapi.winmd.tables.MethodDef;
import net.codecrete.windowsapi.winmd.tables.NestedClass;
import net.codecrete.windowsapi.winmd.tables.Param;
import net.codecrete.windowsapi.winmd.tables.RowKeyTableIterable;
import net.codecrete.windowsapi.winmd.tables.Table;
import net.codecrete.windowsapi.winmd.tables.TableRangeIterable;
import net.codecrete.windowsapi.winmd.tables.TypeDef;
import net.codecrete.windowsapi.winmd.tables.TypeRef;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

import static net.codecrete.windowsapi.winmd.tables.CodedIndexes.CUSTOM_ATTRIBUTE_TYPE_TABLES;
import static net.codecrete.windowsapi.winmd.tables.CodedIndexes.HAS_CONSTANT_TABLES;
import static net.codecrete.windowsapi.winmd.tables.CodedIndexes.HAS_CUSTOM_ATTRIBUTE_TABLES;
import static net.codecrete.windowsapi.winmd.tables.CodedIndexes.HAS_DECL_SECURITY_TABLES;
import static net.codecrete.windowsapi.winmd.tables.CodedIndexes.HAS_FIELD_MARSHAL_TABLES;
import static net.codecrete.windowsapi.winmd.tables.CodedIndexes.HAS_SEMATICS_TABLES;
import static net.codecrete.windowsapi.winmd.tables.CodedIndexes.IMPLEMENTATION_TABLES;
import static net.codecrete.windowsapi.winmd.tables.CodedIndexes.MEMBER_FORWARDED_TABLES;
import static net.codecrete.windowsapi.winmd.tables.CodedIndexes.MEMBER_REF_PARENT_TABLES;
import static net.codecrete.windowsapi.winmd.tables.CodedIndexes.METHOD_DEF_OR_REF_TABLES;
import static net.codecrete.windowsapi.winmd.tables.CodedIndexes.RESOLUTION_SCOPE_TABLES;
import static net.codecrete.windowsapi.winmd.tables.CodedIndexes.TYPE_DEF_OR_REF_TABLES;
import static net.codecrete.windowsapi.winmd.tables.CodedIndexes.TYPE_OR_METHOD_DEF_TABLES;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.ASSEMBLY;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.ASSEMBLY_OS;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.ASSEMBLY_PROCESSOR;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.ASSEMBLY_REF;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.ASSEMBLY_REF_OS;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.ASSEMBLY_REF_PROCESSOR;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.CLASS_LAYOUT;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.CONSTANT;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.CUSTOM_ATTRIBUTE;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.DECL_SECURITY;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.EVENT;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.EVENT_MAP;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.EXPORTED_TYPE;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.FIELD;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.FIELD_LAYOUT;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.FIELD_MARSHAL;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.FIELD_RVA;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.FILE;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.GENERIC_PARAM;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.GENERIC_PARAM_CONSTRAINT;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.IMPL_MAP;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.INTERFACE_IMPL;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.MANIFEST_RESOURCE;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.MEMBER_REF;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.METHOD_DEF;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.METHOD_IMPL;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.METHOD_SEMANTICS;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.METHOD_SPEC;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.MODULE;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.MODULE_REF;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.NESTED_CLASS;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.PARAM;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.PROPERTY;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.PROPERTY_MAP;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.STAND_ALONE_SIG;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.TYPE_DEF;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.TYPE_REF;
import static net.codecrete.windowsapi.winmd.tables.MetadataTables.TYPE_SPEC;

/**
 * Metadata file.
 * <p>
 * Metadata files are .winmd files in the Portable Executable (PE) format
 * containing metadata in one of the sections.
 * </p>
 * <p>
 * This class reads .winmd files and provides access to the different components
 * such as tables, blob and strings.
 * </p>
 */
public class MetadataFile {
    private final LittleEndianDataInputStream inputStream;
    private String version;
    private MetadataStream[] streams;
    private byte[] blobHeap;
    private byte[] stringHeap;
    private final Table[] tables = new Table[64];
    private Table classLayouts;
    private Table constants;
    private Table customAttributes;
    private Table fields;
    private Table fieldLayouts;
    private Table implMaps;
    private Table interfaceImpls;
    private Table memberRefs;
    private Table methodDefs;
    private Table moduleRefs;
    private Table nestedClasses;
    private Table params;
    private Table typeDefs;
    private Table typeRefs;
    private int hasCustomAttributeIndexWidth;
    private int hasConstantIndexWidth;
    private int memberForwardedIndexWidth;

    private static final Set<Integer> USED_TABLES = Set.of(
            CLASS_LAYOUT,
            CONSTANT,
            CUSTOM_ATTRIBUTE,
            FIELD,
            FIELD_LAYOUT,
            IMPL_MAP,
            INTERFACE_IMPL,
            MEMBER_REF,
            METHOD_DEF,
            MODULE_REF,
            NESTED_CLASS,
            PARAM,
            TYPE_DEF,
            TYPE_REF
    );

    /**
     * Creates a new instance reading the .winmd contents from the provided input stream.
     *
     * @param inputStream input stream providing the .winmd contents
     */
    public MetadataFile(InputStream inputStream) {
        this.inputStream = new LittleEndianDataInputStream(inputStream);
        try {
            read();
        } catch (IOException e) {
            throw new WinmdException("Failed to read .winmd file", e);
        }
    }

    /**
     * Gets the version string of the .winmd file.
     *
     * @return version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the metadata streams.
     *
     * @return array of metadata streams
     */
    MetadataStream[] getStreams() {
        return streams;
    }

    /**
     * Gets the "ClassLayout" row for the specified parent.
     *
     * @param parent parent (TypeDef index)
     * @return class layout
     */
    public ClassLayout getClassLayout(int parent) {
        var index = classLayouts.indexByPrimaryKey(parent, simpleIndexWidth(TYPE_DEF), 6);
        if (index == 0)
            return null;

        int[] values = new int[3];
        classLayouts.getRow(index, values);
        return new ClassLayout(values[0], values[1], values[2]);
    }

    /**
     * Gets the "Constant" row for the specified parent.
     *
     * @param parent parent (HasConstant coded index)
     * @return constant
     */
    public Constant getConstant(int parent) {
        var index = constants.indexByPrimaryKey(parent, hasConstantIndexWidth, 2);
        assert index != 0;

        int[] values = new int[3];
        constants.getRow(index, values);
        return new Constant(values[0], values[1], values[2]);
    }

    /**
     * Gets the "CustomAttribute" rows for the specified parent
     *
     * @param parent (HasCustomAttribute coded index)
     * @return iterable for iterating the "CustomAttribute" rows
     */
    public Iterable<CustomAttribute> getCustomAttributes(int parent) {
        return new RowKeyTableIterable<>(customAttributes, parent, hasCustomAttributeIndexWidth, index -> {
            int[] values = new int[3];
            customAttributes.getRow(index, values);
            return new CustomAttribute(values[0], values[1], values[2]);
        });
    }

    /**
     * Gets the "Field" rows for the specified type definition.
     *
     * @param typeDefIndex typeDef (index into TypeDef table)
     * @return iterable for iterating the "Field" rows
     */
    public Iterable<Field> getFields(int typeDefIndex) {
        int firstField = typeDefs.getValue(typeDefIndex, 4);
        int lastField;
        if (typeDefIndex + 1 <= typeDefs.numRows())
            lastField = typeDefs.getValue(typeDefIndex + 1, 4) - 1;
        else
            lastField = fields.numRows();
        assert firstField <= lastField + 1;

        return new TableRangeIterable<>(firstField, lastField, index -> {
            int[] values = new int[3];
            fields.getRow(index, values);
            return new Field(
                    index,
                    values[0],
                    values[1],
                    values[2]
            );
        });
    }

    /**
     * Gets the "FieldLayout" row for the specified field.
     *
     * @param field field (Field index)
     * @return field layout
     */
    public FieldLayout getFieldLayout(int field) {
        var index = fieldLayouts.indexByPrimaryKey(field, simpleIndexWidth(FIELD), 4);
        if (index == 0)
            return null;

        int[] values = new int[2];
        fieldLayouts.getRow(index, values);
        return new FieldLayout(values[0], values[1]);
    }

    /**
     * Gets the "ImplMap" row for the specified member.
     *
     * @param memberForwarded field or method definition (MemberForwarded coded index)
     * @return implementation map, or {@code null} if none is found
     */
    public ImplMap getImplMap(int memberForwarded) {
        var index = implMaps.indexByPrimaryKey(memberForwarded, memberForwardedIndexWidth, 2);
        if (index == 0)
            return null;

        int[] values = new int[4];
        implMaps.getRow(index, values);
        return new ImplMap(values[0], values[1], values[2], values[3]);
    }

    /**
     * Gets the "InterfaceImpl" rows for the specified class.
     *
     * @param classIndex type definition (index into TypeDef table)
     * @return iterable for iterating the "InterfaceImpl" rows
     */
    public Iterable<InterfaceImpl> getInterfaceImpl(int classIndex) {
        return new RowKeyTableIterable<>(interfaceImpls, classIndex, simpleIndexWidth(TYPE_DEF), index -> {
            int[] values = new int[2];
            interfaceImpls.getRow(index, values);
            return new InterfaceImpl(values[0], values[1]);
        });
    }

    /**
     * Gets the "MemberRef" row for the specified index.
     *
     * @param index row index
     * @return member reference
     */
    public MemberRef getMemberRef(int index) {
        int[] values = new int[3];
        memberRefs.getRow(index, values);
        return new MemberRef(
                values[0],
                values[1],
                values[2]
        );
    }

    /**
     * Gets the "MethodDef" row with the specified index.
     *
     * @param index (MethodDef index)
     * @return method definition entry
     */
    public MethodDef getMethodDef(int index) {
        int[] values = new int[6];
        methodDefs.getRow(index, values);
        return new MethodDef(
                index,
                values[0],
                values[1],
                values[2],
                values[3],
                values[4],
                values[5]
        );
    }

    /**
     * Gets the "MethodDef" rows for the specified type definition.
     *
     * @param typeDefIndex typeDef (index into TypeDef table)
     * @return iterable for iterating the "MethodDef" rows
     */
    public Iterable<MethodDef> getMethodDefs(int typeDefIndex) {
        int firstMethod = typeDefs.getValue(typeDefIndex, 5);
        int lastMethod;
        if (typeDefIndex + 1 <= typeDefs.numRows())
            lastMethod = typeDefs.getValue(typeDefIndex + 1, 5) - 1;
        else
            lastMethod = methodDefs.numRows();
        assert firstMethod <= lastMethod + 1;

        return new TableRangeIterable<>(firstMethod, lastMethod, this::getMethodDef);
    }

    /**
     * Gets the ModuleRef name for the specified index.
     *
     * @param moduleRef (ModuleRef index)
     * @return string index
     */
    public int getModuleRefName(int moduleRef) {
        int[] values = new int[1];
        moduleRefs.getRow(moduleRef, values);
        return values[0];
    }

    /**
     * Gets the "NestedClass" row for the specified class.
     *
     * @param nestedClass nested class (TypeAlias index)
     * @return nested class entry (consisting of nested and enclosing class), or {@code null} if none is found
     */
    public NestedClass getNestedClass(int nestedClass) {
        var index = nestedClasses.indexByPrimaryKey(nestedClass, simpleIndexWidth(TYPE_DEF), 0);
        if (index == 0)
            return null;

        int[] values = new int[2];
        nestedClasses.getRow(index, values);
        return new NestedClass(values[0], values[1]);
    }

    /**
     * Gets the "Param" rows for the specified method definition.
     *
     * @param methodDefIndex methodDef (index into MethodDef table)
     * @return iterable for iterating the "Param" rows
     */
    public Iterable<Param> getParameters(int methodDefIndex) {
        int firstParam = methodDefs.getValue(methodDefIndex, 5);
        int lastParam;
        if (methodDefIndex + 1 <= methodDefs.numRows())
            lastParam = methodDefs.getValue(methodDefIndex + 1, 5) - 1;
        else
            lastParam = params.numRows();
        assert firstParam <= lastParam + 1;

        return new TableRangeIterable<>(firstParam, lastParam, index -> {
            int[] values = new int[3];
            params.getRow(index, values);
            return new Param(
                    index,
                    values[0],
                    values[1],
                    values[2]
            );
        });
    }

    /**
     * Gets the "TypeDef" row for the specified index.
     *
     * @param typeDefIndex typeDef (index into TypeDef table)
     * @return the type definition
     */
    public TypeDef getTypeDef(int typeDefIndex) {
        int[] values = new int[6];
        typeDefs.getRow(typeDefIndex, values);
        return new TypeDef(
                values[0],
                values[1],
                values[2],
                values[3],
                values[4],
                values[5]
        );
    }

    /**
     * Gets an iterator over the TypeDef table.
     *
     * @return the iterator
     */
    public Iterable<TypeDef> getTypeDefs() {
        return new TableRangeIterable<>(1, typeDefs.numRows(), this::getTypeDef);
    }

    /**
     * Gets the number of rows in the TypeDef table.
     *
     * @return the number of rows
     */
    public int getTypeDefinitionCount() {
        return typeDefs.numRows();
    }

    /**
     * Gets the "TypeRef" row for the specified index.
     *
     * @param index row index
     * @return type reference
     */
    public TypeRef getTypeRef(int index) {
        int[] values = new int[3];
        typeRefs.getRow(index, values);
        return new TypeRef(values[0], values[1], values[2]);
    }

    /**
     * Gets the string with the specified index from the 'string' heap.
     *
     * @param index string index
     * @return string
     */
    public String getString(int index) {
        if (index == 0)
            return null;
        int end = index;
        while (stringHeap[end] != 0)
            end += 1;
        return new String(stringHeap, index, end - index, StandardCharsets.UTF_8);
    }

    /**
     * Gets the Blob with the specified index from the 'blob' heap.
     *
     * @param index blob index
     * @return blob handle
     */
    public Blob getBlob(int index) {
        int b1 = blobHeap[index] & 0xff;
        int length;
        if ((b1 & 0x80) == 0x00) {
            length = b1;
            index += 1;
        } else if ((b1 & 0xc0) == 0x80) {
            length = ((b1 & 0x3f) << 8) + (blobHeap[index + 1] & 0xff);
            index += 2;
        } else if ((b1 & 0xe0) == 0xc0) {
            length = ((b1 & 0x1f) << 24) + ((blobHeap[index + 1] & 0xff) << 16)
                    + ((blobHeap[index + 2] & 0xff) << 8) + (blobHeap[index + 3] & 0xff);
            index += 4;
        } else {
            throw new WinmdException("Invalid data in blob");
        }

        return new Blob(blobHeap, index, length);
    }

    /**
     * Reads the .winmd file
     */
    private void read() throws IOException {
        readPEHeaders();
        readMetadataHeader();
        readStreams();
    }

    /**
     * Reads the PE headers up to the start of the metadata header
     */
    private void readPEHeaders() throws IOException {
        // .winmd files are Portable Executable (PE) files

        // read magic bytes
        var magicBytes = new byte[2];
        inputStream.readFully(magicBytes);
        if (magicBytes[0] != 'M' || magicBytes[1] != 'Z')
            throw new WinmdException("Invalid data (expected magic bytes \"MZ\")");

        // read signature offset
        inputStream.skipTo(0x3c);
        var signatureOffset = inputStream.readInt();

        // read signature
        inputStream.skipTo(signatureOffset);
        magicBytes = new byte[4];
        inputStream.readFully(magicBytes);
        if (magicBytes[0] != 'P' || magicBytes[1] != 'E' || magicBytes[2] != 0 || magicBytes[3] != 0)
            throw new WinmdException("Invalid data (expected magic bytes \"PZ\\0\\0\")");

        // read COFF file header
        inputStream.readUnsignedShort(); // machine
        var numSections = inputStream.readUnsignedShort();
        inputStream.readInt(); // time date stamp
        inputStream.readInt(); // pointer to symbol table
        inputStream.readInt(); // number of symbols
        var optionalHeaderSize = inputStream.readUnsignedShort();
        inputStream.readUnsignedShort(); // characteristics
        if (optionalHeaderSize < 2)
            throw new WinmdException("Invalid data (expected optional header)");

        // read optional header
        var magicNumber = inputStream.readUnsignedShort();
        if (magicNumber != 0x10b)
            throw new WinmdException("Invalid data (expected magic number 0x10b)");
        inputStream.skipTo(signatureOffset + 24 + 96);
        // skip to entry 14 (referring to .cormeta section aka CLR runtime header aka metadata section)
        inputStream.skipNBytes((14 * 8));
        var clrRuntimeHeaderAddress = inputStream.readInt();

        // read sections
        inputStream.skipTo(signatureOffset + 24 + 96 + 16 * 8);
        var sections = new Section[numSections];
        for (int i = 0; i < numSections; i++) {
            inputStream.skipNBytes(8);
            var virtualSize = inputStream.readInt();
            var virtualAddress = inputStream.readInt();
            inputStream.skipNBytes(4);
            var pointerToRawData = inputStream.readInt();
            inputStream.skipNBytes((40 - 24));
            sections[i] = new Section(virtualSize, virtualAddress, pointerToRawData);
        }

        var clrRuntimeHeaderOffset = getOffset(sections, clrRuntimeHeaderAddress);
        inputStream.skipTo(clrRuntimeHeaderOffset);

        // read CLR runtime header
        int size = inputStream.readInt();
        if (size != 72)
            throw new WinmdException("Invalid data (unexpected size in CLR runtime header)");
        inputStream.skipNBytes(4);
        var metaDataAddress = inputStream.readInt();

        // skip to start of metadata
        var metaDataOffset = getOffset(sections, metaDataAddress);
        inputStream.skipTo(metaDataOffset);
    }

    /**
     * Reads the metadata header
     * <p>
     * The metadata header includes the version and the stream information.
     * </p>
     *
     * @throws IOException if an I/O error occurs
     */
    private void readMetadataHeader() throws IOException {
        // Also see ECMA-335, 6th ed., II.24

        // read metadata root
        var metadataRootOffset = inputStream.getOffset();
        var magicBytes = inputStream.readInt();
        if (magicBytes != 0x424A5342)
            throw new WinmdException("Invalid data (invalid magic bytes in metadata header)");

        inputStream.skipNBytes(8);
        var versionLength = inputStream.readInt();
        var versionBytes = new byte[versionLength];
        inputStream.readFully(versionBytes);
        version = createString(versionBytes);

        // read stream information
        inputStream.skipNBytes(2);
        var numStreams = inputStream.readUnsignedShort();
        streams = new MetadataStream[numStreams];
        for (int i = 0; i < numStreams; i++) {
            int offset = inputStream.readInt() + (int) metadataRootOffset;
            int size = inputStream.readInt();
            var name = readUtf8String();
            streams[i] = new MetadataStream(offset, size, name);
        }

        // sort in ascending order of offsets
        Arrays.sort(streams, Comparator.comparingInt(s -> s.offset));
    }

    private void readStreams() throws IOException {
        for (var stream : streams) {
            inputStream.skipTo(stream.offset);

            switch (stream.name) {
                case "#~":
                    readTablesHeader();
                    readTables();
                    break;
                case "#Strings":
                    stringHeap = new byte[stream.size];
                    inputStream.readFully(stringHeap);
                    break;
                case "#Blob":
                    blobHeap = new byte[stream.size];
                    inputStream.readFully(blobHeap);
                    break;
                default:
                    // skip
            }
        }
    }

    private void readTablesHeader() throws IOException {
        var headerOffset = (int) inputStream.getOffset();
        inputStream.skipTo(headerOffset + 6);
        var heapSizes = inputStream.readByte();
        int stringIndexWidth = (heapSizes & 1) != 0 ? 4 : 2;
        int guidIndexWidth = (heapSizes & 2) != 0 ? 4 : 2;
        int blobIndexWidth = (heapSizes & 4) != 0 ? 4 : 2;

        // available tables in the file
        inputStream.skipTo(headerOffset + 8);
        var availableTables = inputStream.readLong();

        // number of rows per table
        inputStream.skipTo(headerOffset + 24);
        for (int i = 0; i < 64; i++) {
            if ((availableTables & (1L << i)) != 0)
                tables[i] = new Table(inputStream.readInt());
        }

        // compute the length of coded indexes
        // (see ECMA-335, II.24.2.6 #~ stream)
        var typeDefOrRefIndexWidth = codedIndexWidth(TYPE_DEF_OR_REF_TABLES);
        hasConstantIndexWidth = codedIndexWidth(HAS_CONSTANT_TABLES);
        hasCustomAttributeIndexWidth = codedIndexWidth(HAS_CUSTOM_ATTRIBUTE_TABLES);
        var hasFieldMarshalIndexWidth = codedIndexWidth(HAS_FIELD_MARSHAL_TABLES);
        var hasDeclSecurityIndexWidth = codedIndexWidth(HAS_DECL_SECURITY_TABLES);
        var memberRefParentIndexWidth = codedIndexWidth(MEMBER_REF_PARENT_TABLES);
        var hasSemanticsIndexWidth = codedIndexWidth(HAS_SEMATICS_TABLES);
        var methodDefOrRefIndexWidth = codedIndexWidth(METHOD_DEF_OR_REF_TABLES);
        memberForwardedIndexWidth = codedIndexWidth(MEMBER_FORWARDED_TABLES);
        var implementationIndexWidth = codedIndexWidth(IMPLEMENTATION_TABLES);
        var customAttributeTypeIndexWidth = codedIndexWidth(CUSTOM_ATTRIBUTE_TYPE_TABLES);
        var resolutionScopeIndexWidth = codedIndexWidth(RESOLUTION_SCOPE_TABLES);
        var typeOrMethodDefIndexWidth = codedIndexWidth(TYPE_OR_METHOD_DEF_TABLES);

        // compute row lengths
        // (see ECMA-335, II.22 Metadata logical format: tables)
        setColumnWidths(ASSEMBLY, 4, 8, 4, blobIndexWidth, stringIndexWidth, stringIndexWidth);
        setColumnWidths(ASSEMBLY_OS, 4, 4, 4);
        setColumnWidths(ASSEMBLY_PROCESSOR, 4);
        setColumnWidths(ASSEMBLY_REF, 8, 4, blobIndexWidth, stringIndexWidth, stringIndexWidth,
                blobIndexWidth);
        setColumnWidths(ASSEMBLY_REF_OS, 4, 4, 4, simpleIndexWidth(ASSEMBLY_REF));
        setColumnWidths(ASSEMBLY_REF_PROCESSOR, 4, simpleIndexWidth(ASSEMBLY_REF));
        setColumnWidths(CLASS_LAYOUT, 2, 4, simpleIndexWidth(TYPE_DEF));
        setColumnWidths(CONSTANT, 2, hasConstantIndexWidth, blobIndexWidth);
        setColumnWidths(CUSTOM_ATTRIBUTE, hasCustomAttributeIndexWidth,
                customAttributeTypeIndexWidth, blobIndexWidth);
        setColumnWidths(DECL_SECURITY, 2, hasDeclSecurityIndexWidth, blobIndexWidth);
        setColumnWidths(EVENT, 2, stringIndexWidth, typeDefOrRefIndexWidth);
        setColumnWidths(EVENT_MAP, simpleIndexWidth(TYPE_DEF), simpleIndexWidth(EVENT));
        setColumnWidths(EXPORTED_TYPE, 4, 4, stringIndexWidth, stringIndexWidth,
                implementationIndexWidth);
        setColumnWidths(FIELD, 2, stringIndexWidth, blobIndexWidth);
        setColumnWidths(FIELD_LAYOUT, 4, simpleIndexWidth(FIELD));
        setColumnWidths(FIELD_MARSHAL, hasFieldMarshalIndexWidth, blobIndexWidth);
        setColumnWidths(FIELD_RVA, 4, simpleIndexWidth(FIELD));
        setColumnWidths(FILE, 4, stringIndexWidth, blobIndexWidth);
        setColumnWidths(GENERIC_PARAM, 2, 2, typeOrMethodDefIndexWidth, stringIndexWidth);
        setColumnWidths(GENERIC_PARAM_CONSTRAINT, simpleIndexWidth(GENERIC_PARAM),
                typeDefOrRefIndexWidth);
        setColumnWidths(IMPL_MAP, 2, memberForwardedIndexWidth, stringIndexWidth,
                simpleIndexWidth(MODULE_REF));
        setColumnWidths(INTERFACE_IMPL, simpleIndexWidth(TYPE_DEF), typeDefOrRefIndexWidth);
        setColumnWidths(MANIFEST_RESOURCE, 4, 4, stringIndexWidth, implementationIndexWidth);
        setColumnWidths(MEMBER_REF, memberRefParentIndexWidth, stringIndexWidth, blobIndexWidth);
        setColumnWidths(METHOD_DEF, 4, 2, 2, stringIndexWidth, blobIndexWidth,
                simpleIndexWidth(PARAM));
        setColumnWidths(METHOD_IMPL, simpleIndexWidth(TYPE_DEF), methodDefOrRefIndexWidth,
                methodDefOrRefIndexWidth);
        setColumnWidths(METHOD_SEMANTICS, 2, simpleIndexWidth(METHOD_DEF), hasSemanticsIndexWidth);
        setColumnWidths(METHOD_SPEC, methodDefOrRefIndexWidth, blobIndexWidth);
        setColumnWidths(MODULE, 2, stringIndexWidth, guidIndexWidth, guidIndexWidth,
                guidIndexWidth);
        setColumnWidths(MODULE_REF, stringIndexWidth);
        setColumnWidths(NESTED_CLASS, simpleIndexWidth(TYPE_DEF), simpleIndexWidth(TYPE_DEF));
        setColumnWidths(PARAM, 2, 2, stringIndexWidth);
        setColumnWidths(PROPERTY, 2, stringIndexWidth, blobIndexWidth);
        setColumnWidths(PROPERTY_MAP, simpleIndexWidth(TYPE_DEF), simpleIndexWidth(PROPERTY));
        setColumnWidths(STAND_ALONE_SIG, blobIndexWidth);
        setColumnWidths(TYPE_DEF, 4, stringIndexWidth, stringIndexWidth, typeDefOrRefIndexWidth,
                simpleIndexWidth(FIELD), simpleIndexWidth(METHOD_DEF));
        setColumnWidths(TYPE_REF, resolutionScopeIndexWidth, stringIndexWidth, stringIndexWidth);
        setColumnWidths(TYPE_SPEC, blobIndexWidth);
    }

    private void setColumnWidths(int tableIndex, int... widths) {
        if (tables[tableIndex] != null)
            tables[tableIndex].setColumnWidths(widths);
    }

    private void readTables() throws IOException {
        for (int i = 0; i < 63; i++) {
            if (tables[i] == null || tables[i].numRows() == 0)
                continue;
            int tableLength = tables[i].numRows() * tables[i].width();
            if (USED_TABLES.contains(i)) {
                var data = new byte[tableLength];
                inputStream.readFully(data);
                tables[i].setData(data);
            } else {
                inputStream.skipNBytes(tableLength);
            }
        }

        classLayouts = tables[CLASS_LAYOUT];
        constants = tables[CONSTANT];
        customAttributes = tables[CUSTOM_ATTRIBUTE];
        fields = tables[FIELD];
        fieldLayouts = tables[FIELD_LAYOUT];
        implMaps = tables[IMPL_MAP];
        interfaceImpls = tables[INTERFACE_IMPL];
        memberRefs = tables[MEMBER_REF];
        methodDefs = tables[METHOD_DEF];
        moduleRefs = tables[MODULE_REF];
        nestedClasses = tables[NESTED_CLASS];
        params = tables[PARAM];
        typeDefs = tables[TYPE_DEF];
        typeRefs = tables[TYPE_REF];
    }

    private int simpleIndexWidth(int table) {
        return tables[table] != null ? tables[table].indexWidth() : 2;
    }

    private int codedIndexWidth(int... tableIndexes) {
        int numBitsTable = 32 - Integer.numberOfLeadingZeros(tableIndexes.length - 1);
        int max16BitIndex = 1 << (16 - numBitsTable);
        for (var index : tableIndexes) {
            if (tables[index] != null && tables[index].numRows() >= max16BitIndex)
                return 4;
        }
        return 2;
    }

    /**
     * Reads a variable length, null-terminated string, encoded in UTF-8.
     * <p>
     * The actual allocation in the stream is a multiple of 4 bytes.
     * </p>
     *
     * @return the string
     * @throws IOException if an I/O error occurs
     */
    private String readUtf8String() throws IOException {
        var utf8Buffer = new ByteArrayOutputStream();
        var fourBytes = new byte[4];
        int length;
        do {
            inputStream.readFully(fourBytes);
            length = 0;
            while (length < 4 && fourBytes[length] != 0)
                length += 1;
            utf8Buffer.write(fourBytes, 0, length);
        } while (length == 4);
        return utf8Buffer.toString(StandardCharsets.UTF_8);
    }

    private static int getOffset(Section[] sections, int virtualAddress) {
        for (var section : sections) {
            if (section.virtualAddress <= virtualAddress && virtualAddress < section.virtualAddress + section.virtualSize)
                return virtualAddress - section.virtualAddress + section.pointerToRawData;
        }
        throw new WinmdException("Invalid data (virtual address outside sections)");
    }

    /**
     * Creates a new string instance from the provided null-terminated UTF-8 code units.
     *
     * @param codeUnits UTF-8 code units, null-terminated
     * @return string
     */
    private static String createString(byte[] codeUnits) {
        int length = 0;
        while (codeUnits[length] != 0)
            length += 1;
        return new String(codeUnits, 0, length, StandardCharsets.UTF_8);
    }

    record MetadataStream(int offset, int size, String name) {
    }

    private record Section(int virtualSize, int virtualAddress, int pointerToRawData) {
    }
}
