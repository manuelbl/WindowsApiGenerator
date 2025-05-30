package net.codecrete.windowsapi.winmd;

import net.codecrete.windowsapi.metadata.Array;
import net.codecrete.windowsapi.metadata.Struct;
import net.codecrete.windowsapi.metadata.Type;
import net.codecrete.windowsapi.winmd.tables.CodedIndex;
import net.codecrete.windowsapi.winmd.tables.CodedIndexes;

import static net.codecrete.windowsapi.winmd.tables.MetadataTables.TYPE_REF;

/**
 * Base class for decoders.
 */
class Decoder {
    protected final TypeLookup typeLookup;

    /**
     * Creates a new decoder.
     *
     * @param typeLookup an instance implementing the lookup of types
     */
    protected Decoder(TypeLookup typeLookup) {
        this.typeLookup = typeLookup;
    }

    /**
     * Decodes a type within a signature.
     *
     * @param signature  the signature
     * @param parentType the parent type
     * @return the decoded type
     */
    protected Type decodeType(Blob signature, Struct parentType) {
        // See ECMA-335, II.23.2.12 Type
        int elementType = signature.readByte();
        Type type = typeLookup.getPrimitiveType(elementType);
        if (type != null)
            return type;

        if (elementType == ElementTypes.PTR) {
            type = decodeType(signature, parentType);
            assert type != null;
            return typeLookup.makePointerFor(type);
        } else if (elementType == ElementTypes.VALUETYPE) {
            // TypeDefOrRefOrSpecEncoded
            var typeDefOrRefOrSpecEncoded = signature.readCompressedUnsignedInt();
            var typeDefOrRefIndex = CodedIndex.decode(typeDefOrRefOrSpecEncoded, CodedIndexes.TYPE_DEF_OR_REF_TABLES);
            Type valueType;
            if (typeDefOrRefIndex.table() == TYPE_REF) {
                valueType = typeLookup.getTypeByTypeRef(typeDefOrRefIndex.index(), parentType, false);
            } else {
                valueType = typeLookup.getTypeByTypeDef(typeDefOrRefIndex.index());
            }
            assert valueType != null;
            return valueType;
        } else if (elementType == ElementTypes.CLASS) {
            // TypeDefOrRefOrSpecEncoded
            var typeDefOrRefOrSpecEncoded = signature.readCompressedUnsignedInt();
            var typeDefOrRefIndex = CodedIndex.decode(typeDefOrRefOrSpecEncoded, CodedIndexes.TYPE_DEF_OR_REF_TABLES);
            Type classType;
            if (typeDefOrRefIndex.table() == TYPE_REF) {
                classType = typeLookup.getTypeByTypeRef(typeDefOrRefIndex.index(), parentType, true);
            } else {
                classType = typeLookup.getTypeByTypeDef(typeDefOrRefIndex.index());
            }
            assert classType != null;
            return classType;
        } else if (elementType == ElementTypes.ARRAY) {
            return decodeArray(signature, parentType);
        }

        assert false : "Unknown element type: " + elementType;
        return null;
    }

    /**
     * Decodes an array within a signature.
     *
     * @param signature  the signature
     * @param parentType the parent type
     * @return the decoded array type
     */
    private Array decodeArray(Blob signature, Struct parentType) {
        // See ECMA-335, II.23.2.13 ArrayShape
        var arrayType = decodeType(signature, parentType);
        var rank = signature.readCompressedUnsignedInt();
        assert rank == 1;
        var numSizes = signature.readCompressedUnsignedInt();
        assert numSizes == 1;
        var size = signature.readCompressedUnsignedInt();
        var numLoBounds = signature.readCompressedUnsignedInt();
        assert numLoBounds == 0 || numLoBounds == 1;
        if (numLoBounds == 1)
            signature.readCompressedUnsignedInt();

        return new Array(arrayType.name() + "[]", null, 0, arrayType, size);
    }

    protected static Object readPrimitiveVal(Blob blob, int elementType) {
        if (elementType == ElementTypes.STRING)
            return blob.readUtf8String();
        else
            return readValue(blob, elementType);
    }

    protected static Object readConstantVal(Blob blob, int elementType) {
        if (elementType == ElementTypes.STRING)
            return blob.readUtf16String();
        else
            return readValue(blob, elementType);
    }

    private static Object readValue(Blob blob, int elementType) {
        return switch (elementType) {
            case ElementTypes.I1, ElementTypes.U1 -> (byte) blob.readByte();
            case ElementTypes.I2, ElementTypes.U2 -> (short) blob.readUInt16();
            case ElementTypes.I4, ElementTypes.U4 -> blob.readInt32();
            case ElementTypes.R4 -> Float.intBitsToFloat(blob.readInt32());
            case ElementTypes.I8, ElementTypes.U8 -> blob.readInt64();
            case ElementTypes.R8 -> Double.longBitsToDouble(blob.readInt64());
            default -> throw new AssertionError("Invalid primitive type " + elementType);
        };
    }
}
