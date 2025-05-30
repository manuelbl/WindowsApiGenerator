//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd;

import net.codecrete.windowsapi.metadata.Array;
import net.codecrete.windowsapi.metadata.ComInterface;
import net.codecrete.windowsapi.metadata.Delegate;
import net.codecrete.windowsapi.metadata.EnumType;
import net.codecrete.windowsapi.metadata.Pointer;
import net.codecrete.windowsapi.metadata.Primitive;
import net.codecrete.windowsapi.metadata.PrimitiveKind;
import net.codecrete.windowsapi.metadata.Struct;
import net.codecrete.windowsapi.metadata.Type;
import net.codecrete.windowsapi.metadata.TypeAlias;

/**
 * Size and alignment of a type.
 * <p>
 * Instances of this record are used once the {@link StructLayouter} has been run.
 * </p>
 *
 * @param size      the size (in bytes)
 * @param alignment the required alignment (in bytes)
 */
public record LayoutRequirement(int size, int alignment) {

    /**
     * Gets the given type's layout requirements.
     *
     * @param type the type
     * @return the layout requirement
     */
    public static LayoutRequirement forType(Type type) {
        return switch (type) {
            case Primitive primitive -> forPrimitive(primitive);
            case Struct struct -> getStructRequirement(struct);
            case EnumType enumType -> forPrimitive(enumType.baseType());
            case Pointer ignored -> new LayoutRequirement(8, 8);
            case Delegate ignored -> new LayoutRequirement(8, 8);
            case ComInterface ignored -> new LayoutRequirement(8, 8);
            case Array arrayType -> getArrayRequirement(arrayType);
            case TypeAlias typeAlias -> forType(typeAlias.aliasedType());
        };
    }

    private static LayoutRequirement getStructRequirement(Struct type) {
        assert type.isLayoutDone();
        return new LayoutRequirement(type.structSize(), type.packageSize());
    }

    private static LayoutRequirement getArrayRequirement(Array type) {
        var requirement = forType(type.itemType());
        return new LayoutRequirement(type.arrayLength() * requirement.size(), requirement.alignment());
    }

    /**
     * Gets the size of a primitive type.
     *
     * @param type the primitive type
     * @return the size (in bytes)
     */
    public static int primitiveSize(Primitive type) {
        return switch (type.kind()) {
            case PrimitiveKind.INT64, PrimitiveKind.UINT64, PrimitiveKind.DOUBLE, PrimitiveKind.INT_PTR,
                 PrimitiveKind.UINT_PTR -> 8;
            case PrimitiveKind.INT32, PrimitiveKind.UINT32, PrimitiveKind.SINGLE -> 4;
            case PrimitiveKind.UINT16, PrimitiveKind.INT16, PrimitiveKind.CHAR -> 2;
            case PrimitiveKind.BYTE, PrimitiveKind.SBYTE, PrimitiveKind.BOOL -> 1;
            default -> throw new AssertionError("Unexpected type: " + type.name());
        };
    }

    /**
     * Gets the layout requirement for a primitive type.
     * @param type the primitive type
     * @return the layout requirement
     */
    public static LayoutRequirement forPrimitive(Primitive type) {
        var size = primitiveSize(type);
        return new LayoutRequirement(size, size);
    }
}
