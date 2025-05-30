//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd;

import net.codecrete.windowsapi.metadata.Array;
import net.codecrete.windowsapi.metadata.Member;
import net.codecrete.windowsapi.metadata.Struct;
import net.codecrete.windowsapi.metadata.Type;

import java.util.Objects;
import java.util.Set;

/**
 * Determines the layout of structs and unions.
 * <p>
 * The layout consists of the size of the struct/union, its alignment requirement,
 * the offset of all fields and the required padding between the fields.
 * </p>
 */
@SuppressWarnings("java:S4274")
class StructLayouter {

    /**
     * Set of structs that contain a flexible array and thus have a variable size
     * but are not consistent enough to be treated as a regular variable size struct.
     */
    private static final Set<String> VARIABLE_SIZE_EXEMPTIONS = Set.of(
            "IMAGEHLP_SYMBOL64_PACKAGE",
            "IMAGEHLP_SYMBOLW64_PACKAGE",
            "SYMBOL_INFO_PACKAGE",
            "SYMBOL_INFO_PACKAGEW",
            "KSSTREAMALLOCATOR_STATUS_EX"
    );

    private final MetadataFile metadataFile;

    /**
     * Creates a new instance.
     *
     * @param metadataFile the metadata file to get additional information from
     */
    StructLayouter(MetadataFile metadataFile) {
        this.metadataFile = metadataFile;
    }

    /**
     * Calculates the size and offset of this struct and its fields.
     * <p>
     * It will also trigger the calculation of all embedded types.
     * </p>
     *
     * @param struct the struct
     */
    void layout(Struct struct) {
        if (struct.isLayoutDone())
            return;

        if (struct.isUnion()) {
            layoutUnion(struct);
        } else {
            layoutSequential(struct);
        }

        struct.setFlexibleArrayMember(findFlexibleMember(struct));

        struct.setLayoutDone();
    }

    private void layoutSequential(Struct struct) {
        var state = new LayoutState(struct.packageSize());
        Member previousMember = null;

        for (var member : struct.members()) {
            var previousEndOffset = state.endOffset;
            ensureLayoutDone(member.type());
            state.advance(member.type());

            if (previousMember != null)
                previousMember.setPaddingAfter(state.startOffset - previousEndOffset);

            member.setOffset(state.startOffset);
            previousMember = member;
        }

        finishStructLayout(struct, state, previousMember);
    }

    private void layoutUnion(Struct struct) {
        var state = new LayoutState(struct.packageSize());
        Member previousMember = null;

        for (var member : struct.members()) {
            var fieldLayout = metadataFile.getFieldLayout(member.fieldIndex());
            assert fieldLayout.offset() == 0;
            ensureLayoutDone(member.type());
            state.overlay(member.type());
            previousMember = member;
        }

        finishStructLayout(struct, state, previousMember);
    }

    private void finishStructLayout(Struct struct, LayoutState state, Member lastMember) {
        var previousEndOffset = state.endOffset;
        state.advance(0, state.packageSize);
        assert struct.structSize() == 0 || struct.structSize() == state.startOffset;
        struct.setStructSize(state.startOffset);
        assert struct.packageSize() == 0 || struct.packageSize() == state.packageSize;
        struct.setPackageSize(state.packageSize);
        if (lastMember != null)
            lastMember.setPaddingAfter(state.startOffset - previousEndOffset);
    }

    private void ensureLayoutDone(Type type) {
        switch (type) {
            case Struct struct -> layout(struct);
            case Array array -> ensureLayoutDone(array.itemType());
            default -> { /* nothing to do */ }
        }
    }

    private static Member findFlexibleMember(Struct struct) {
        if (struct.isUnion())
            return null;

        if (VARIABLE_SIZE_EXEMPTIONS.contains(struct.name()))
            return null;

        return struct.members().stream()
                .map(StructLayouter::findFlexible)
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
    }

    private static Member findFlexible(Member member) {
        return switch (member.type()) {
            case Array array -> array.isFlexible() ? member : null;
            case Struct struct -> findFlexibleMember(struct);
            default -> null;
        };
    }


    /**
     * Holds intermediate state when calculating the struct/union layout.
     */
    private static class LayoutState {
        private final boolean forcedPackageSize;
        private int startOffset;
        private int endOffset;
        private int packageSize;

        private LayoutState(int packageSize) {
            forcedPackageSize = packageSize != 0;
            this.packageSize = forcedPackageSize ? packageSize : 1;
        }

        /**
         * Advances the offset by the size and alignment required by the specified type.
         *
         * @param type the type
         */
        private void advance(Type type) {
            var requirement = LayoutRequirement.forType(type);
            advance(requirement.size(), requirement.alignment());
        }

        /**
         * Advances the offset by the specified size and alignment.
         *
         * @param size      the size to advance (in bytes)
         * @param alignment the alignment (in bytes)
         */
        private void advance(int size, int alignment) {
            if (forcedPackageSize) {
                alignment = Math.min(alignment, packageSize);
                startOffset = (endOffset + alignment - 1) & -alignment;
            } else {
                startOffset = (endOffset + alignment - 1) & -alignment;
                packageSize = Math.max(packageSize, alignment);
            }
            endOffset = startOffset + size;
        }

        /**
         * Overlays the layout with another union member and modifies size and alignment accordingly.
         *
         * @param type the union member type
         */
        private void overlay(Type type) {
            var requirement = LayoutRequirement.forType(type);
            endOffset = Math.max(endOffset, requirement.size());
            if (!forcedPackageSize)
                packageSize = Math.max(packageSize, requirement.alignment());
        }
    }
}
