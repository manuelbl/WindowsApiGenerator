//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.writer;

import net.codecrete.windowsapi.metadata.Array;
import net.codecrete.windowsapi.metadata.ComInterface;
import net.codecrete.windowsapi.metadata.Delegate;
import net.codecrete.windowsapi.metadata.EnumType;
import net.codecrete.windowsapi.metadata.Method;
import net.codecrete.windowsapi.metadata.Pointer;
import net.codecrete.windowsapi.metadata.Primitive;
import net.codecrete.windowsapi.metadata.PrimitiveKind;
import net.codecrete.windowsapi.metadata.Struct;
import net.codecrete.windowsapi.metadata.Type;
import net.codecrete.windowsapi.metadata.TypeAlias;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Describes an address layout.
 * <p>
 * An address can either point to a memory segment of known size and alignment, to a segment of size 0,
 * to a segment of unknown size or to another address.
 * </p>
 * <p>
 * The address itself can be aligned (to 8 bytes) or unaligned.
 * </p>
 *
 * @param aligned     {@code true} if the address is aligned, {@code false} if it is unaligned
 * @param structSize  the size of the target memory segment
 * @param packageSize the alignment of the target memory segment
 * @param fixedName   a fixed name
 */
record AddressLayout(boolean aligned,
                     int structSize,
                     int packageSize,
                     String fixedName
) implements Comparable<AddressLayout> {

    private static final AddressLayout TO_ADDRESS = new AddressLayout(true, 8, 0, "ADDRESS$ADDRESS");
    private static final AddressLayout TO_ADDRESS_UNALIGNED = new AddressLayout(false, 8, 0,
            "ADDRESS_UNALIGNED$ADDRESS");
    private static final AddressLayout TO_UNKNOWN = new AddressLayout(true, Integer.MIN_VALUE, 0,
            "ADDRESS$UNKNOWN_SIZE");
    private static final AddressLayout TO_UNKNOWN_UNALIGNED = new AddressLayout(false, Integer.MIN_VALUE, 0,
            "ADDRESS_UNALIGNED$UNKNOWN_SIZE");
    private static final AddressLayout TO_VOID = new AddressLayout(true, 0, 0, "ADDRESS");
    private static final AddressLayout TO_VOID_UNALIGNED = new AddressLayout(false, 0, 0, "ADDRESS_UNALIGNED");

    /**
     * Address layout for a memory segment containing an address (8 bytes).
     *
     * @param aligned indicates if the address is aligned
     * @return address layout
     */
    static AddressLayout pointerToAddress(boolean aligned) {
        return aligned ? TO_ADDRESS : TO_ADDRESS_UNALIGNED;
    }

    /**
     * Address layout for a memory segment of unknown size.
     *
     * @param aligned indicates if the address is aligned
     * @return address layout
     */
    static AddressLayout pointerToUnknown(boolean aligned) {
        return aligned ? TO_UNKNOWN : TO_UNKNOWN_UNALIGNED;
    }

    /**
     * Address layout for a memory segment of size 0.
     *
     * @param aligned indicates if the address is aligned
     * @return address layout
     */
    static AddressLayout pointerToVoid(boolean aligned) {
        return aligned ? TO_VOID : TO_VOID_UNALIGNED;
    }

    /**
     * Gets the name of the specified address layout.
     *
     * @return the address layout name
     */
    String name() {
        if (fixedName != null)
            return fixedName;

        return String.format("ADDRESS%s$STRUCT_%d_%d", aligned ? "" : "_UNALIGNED", structSize, packageSize);
    }

    @Override
    public int compareTo(AddressLayout other) {
        if ((this.packageSize == 0) != (other.packageSize == 0))
            return this.packageSize - other.packageSize;
        if (this.packageSize == 0)
            return this.fixedName.compareTo(other.fixedName);

        int cmp = this.structSize - other.structSize;
        if (cmp != 0)
            return cmp;

        cmp = this.packageSize - other.packageSize;
        if (cmp != 0)
            return cmp;

        if (this.aligned == other.aligned)
            return 0;

        return this.aligned ? 1 : -1;
    }

    boolean isForStruct() {
        return packageSize > 0;
    }

    /**
     * Gets the required address layouts for the given struct.
     *
     * @param struct the struct
     * @return the required address layouts
     */
    static List<AddressLayout> requiredLayouts(Struct struct) {
        var addressLayouts = new HashSet<AddressLayout>();
        addLayoutsRecursively(struct, struct.packageSize(), addressLayouts);
        return filteredAndSorted(addressLayouts);
    }

    /**
     * Gets the required address layouts for the given functions.
     *
     * @param functions the functions
     * @return the required address layouts
     */
    static List<AddressLayout> requiredLayouts(Collection<Method> functions) {
        var addressLayouts = new HashSet<AddressLayout>();
        functions.stream().flatMap(Method::referencedTypes).forEach(it -> addLayout(it, addressLayouts));
        return filteredAndSorted(addressLayouts);
    }

    private static List<AddressLayout> filteredAndSorted(Collection<AddressLayout> addressLayouts) {
        return addressLayouts.stream()
                .filter(it -> it != pointerToVoid(true) && it != pointerToVoid(false))
                .distinct()
                .sorted()
                .toList();
    }

    /**
     * Gets the required address layouts for the given function.
     *
     * @param function the function
     * @return the required address layouts
     */
    static List<AddressLayout> requiredLayouts(Method function) {
        return requiredLayouts(List.of(function));
    }

    /**
     * Gets the required address layouts for the given COM interface.
     *
     * @param comInterface the COM interface
     * @return the required address layouts
     */
    static List<AddressLayout> requiredLayouts(ComInterface comInterface) {
        var allInterfaces = new ArrayList<ComInterface>();
        var intf = comInterface;
        while (intf != null) {
            allInterfaces.add(intf);
            intf = intf.implementedInterface();
        }

        var methods = allInterfaces.stream().flatMap(it -> it.methods().stream()).toList();
        return requiredLayouts(methods);
    }

    private static void addLayout(Type type, Set<AddressLayout> addressLayouts) {
        switch (type) {
            case TypeAlias typeAlias -> addLayout(typeAlias.aliasedType(), addressLayouts);
            case Pointer pointer -> addressLayouts.add(getAddressLayout(pointer.referencedType(), true));
            case Delegate ignored -> addressLayouts.add(pointerToAddress(true));
            case ComInterface ignored -> addressLayouts.add(pointerToAddress(true));
            default -> { /* no address layout required */ }
        }
    }

    private static void addLayoutsRecursively(Type type, int packageSize, Set<AddressLayout> addressLayouts) {
        var aligned = packageSize >= 8;
        switch (type) {
            case Struct struct -> {
                for (var member : struct.members()) {
                    var memberType = member.type();
                    if (memberType instanceof Struct structMember) {
                        if (structMember.isNested())
                            addLayoutsRecursively(memberType, packageSize, addressLayouts);
                    } else {
                        addLayoutsRecursively(memberType, packageSize, addressLayouts);
                    }
                }
            }
            case Array arrayType -> addLayoutsRecursively(arrayType.itemType(), packageSize, addressLayouts);
            case TypeAlias typeAlias -> addLayoutsRecursively(typeAlias.aliasedType(), packageSize, addressLayouts);
            case Pointer pointer -> addressLayouts.add(getAddressLayout(pointer.referencedType(), aligned));
            case Delegate ignored -> addressLayouts.add(pointerToAddress(aligned));
            case ComInterface ignored -> addressLayouts.add(pointerToAddress(aligned));
            default -> { /* no address layout required */ }
        }
    }

    /**
     * Gets the address layout for the specified target type.
     *
     * @param targetType the target type
     * @param aligned    {@code true} if the address should be aligned, {@code false} if it should be unaligned
     * @return the address layout
     */
    static AddressLayout getAddressLayout(Type targetType, boolean aligned) {
        return switch (targetType) {
            case Primitive primitive -> {
                if (primitive.kind() == PrimitiveKind.VOID) {
                    yield pointerToVoid(aligned);
                } else {
                    yield pointerToUnknown(aligned);
                }
            }
            case EnumType enumType -> getAddressLayout(enumType.baseType(), aligned);
            case TypeAlias typeAlias -> getAddressLayout(typeAlias.aliasedType(), aligned);
            case Struct struct -> {
                if (struct.namespace() != null && !struct.isArchitectureSpecific()) {
                    yield new AddressLayout(aligned, struct.structSize(), struct.packageSize(), null);
                } else {
                    yield pointerToUnknown(aligned);
                }
            }
            case Pointer ignored -> pointerToAddress(aligned);
            case Delegate ignored -> pointerToAddress(aligned);
            case ComInterface ignored -> pointerToAddress(aligned);
            default -> throw new AssertionError("Unexpected type: " + targetType);
        };
    }
}
