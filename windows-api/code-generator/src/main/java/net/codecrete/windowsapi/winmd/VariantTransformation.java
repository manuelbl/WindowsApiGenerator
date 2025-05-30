//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd;

import net.codecrete.windowsapi.metadata.ComInterface;
import net.codecrete.windowsapi.metadata.Delegate;
import net.codecrete.windowsapi.metadata.EnumType;
import net.codecrete.windowsapi.metadata.Metadata;
import net.codecrete.windowsapi.metadata.Method;
import net.codecrete.windowsapi.metadata.Pointer;
import net.codecrete.windowsapi.metadata.Primitive;
import net.codecrete.windowsapi.metadata.Struct;
import net.codecrete.windowsapi.metadata.Type;
import net.codecrete.windowsapi.metadata.TypeAlias;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static net.codecrete.windowsapi.metadata.Architecture.ARM64;
import static net.codecrete.windowsapi.metadata.Architecture.X64;

/**
 * Deals with processor-architecture-specific types and methods.
 * <p>
 * 32-bit variants are removed. Architecture-specific variants are renamed
 * to have a unique name. Types that are indirectly architecture-specific are
 * duplicated and renamed.
 * </p>
 */
@SuppressWarnings("java:S2447")
class VariantTransformation {
    private static final String X64_SUFFIX = "_X64";
    private static final String ARM64_SUFFIX = "_ARM64";
    private static final int X64_OFFSET = 0;
    private static final int ARM64_OFFSET = 1000000;

    private final Metadata metadata;
    private final HashMap<String, HashMap<Integer, Type>> typeVariants = new HashMap<>();
    private final Set<Integer> unsupportedVariants = new HashSet<>();

    VariantTransformation(Metadata metadata) {
        this.metadata = metadata;
    }

    /**
     * Preprocess the architecture-specific aspects of the given type.
     * <p>
     * If this method returns {@code true}, the type is not relevant and should
     * be ignored. 32-bit types are not relevant.
     * </p>
     *
     * @param type         the type
     * @param architecture the architectures
     * @return {@code true} if the type should be ignored, {@code false} otherwise
     */
    boolean preprocessType(Type type, int architecture) {
        architecture = architecture & (X64 | ARM64);
        if (architecture == 0) {
            unsupportedVariants.add(type.typeDefIndex());
            return true;
        }

        if (type.namespace() == null)
            return false;

        if (architecture == (X64 | ARM64))
            return false;

        assert type instanceof Struct || type instanceof Delegate;
        var variants = typeVariants.computeIfAbsent(type.name(), k -> new HashMap<>());
        variants.put(architecture, type);
        if (type instanceof Struct struct)
            struct.setArchitectureSpecific(true);
        return false;
    }

    boolean isUnsupportedVariant(int typeDefIndex) {
        return unsupportedVariants.contains(typeDefIndex);
    }

    /**
     * Preprocesses a method for processor-architecture-specific aspects.
     * <p>
     * If this method returns {@code true}, the method is not relevant for
     * further processing and should be ignored.
     * </p>
     *
     * @param method       the method
     * @param architecture the processor architecture
     * @return {@code true} if the method should be ignored, {@code false} otherwise
     */
    boolean preprocessMethod(Method method, int architecture) {
        architecture = architecture & (X64 | ARM64);
        if (architecture == 0)
            return true;

        if (architecture == (X64 | ARM64))
            return false;

        method.setName(method.nativeName() + (architecture == X64 ? X64_SUFFIX : ARM64_SUFFIX));
        return false;
    }

    /**
     * Splits processor-architecture-dependent types into separate variants for X64 and ARM64.
     * <p>
     * While some types have two separate variants to start with, other types are indirectly
     * architecture-specific because they contain an architecture-specific member.
     * </p>
     * <p>
     * In the separated variants, the member types need to be replaced with the architecture-specific
     * variant if there is one.
     * </p>
     * <p>
     * Pointers to architecture-specific types are not considered architecture-specific.
     * </p>
     */
    void splitCombinedVariants() {
        var architectureSpecificCache = new HashMap<Type, Boolean>();

        // identify types that are indirectly architecture-specific
        var indirectlySpecificTypes = metadata.types()
                .filter(type -> type.namespace() != null)
                .filter(type -> !typeVariants.containsKey(type.name()))
                .filter(type -> isArchitectureSpecific(type, architectureSpecificCache))
                .map(Struct.class::cast)
                .collect(Collectors.toSet());

        var x64Replacements = new HashMap<Type, Type>();
        var arm64Replacements = new HashMap<Type, Type>();

        // create two separate variants for each indirectly architecture-specific type
        for (var type : indirectlySpecificTypes) {

            type.setArchitectureSpecific(true);
            metadata.removeType(type, true);

            var x64Type = duplicateType(type, X64_OFFSET);
            x64Type.setName(type.name() + X64_SUFFIX);
            metadata.addType(x64Type, true);
            x64Replacements.put(type, x64Type);

            var arm64Type = duplicateType(type, ARM64_OFFSET);
            arm64Type.setName(type.name() + ARM64_SUFFIX);
            metadata.addType(arm64Type, true);
            arm64Replacements.put(type, arm64Type);
        }

        // rename X64 variants
        typeVariants.values().stream()
                .map(it -> it.get(X64))
                .filter(Objects::nonNull)
                .forEach(it -> renameType(it, X64_SUFFIX));

        // rename ARM64 variants
        typeVariants.values().stream()
                .map(it -> it.get(ARM64))
                .filter(Objects::nonNull)
                .forEach(it -> renameType(it, ARM64_SUFFIX));

        // replace used types with the architecture-specific variant
        for (var type : x64Replacements.values())
            type.replaceTypes(it -> replaceType(it, x64Replacements));
        for (var type : arm64Replacements.values())
            type.replaceTypes(it -> replaceType(it, arm64Replacements));
        typeVariants.values().stream()
                .map(it -> it.get(X64))
                .filter(Objects::nonNull)
                .forEach(type -> type.replaceTypes(it -> replaceType(it, x64Replacements)));
        typeVariants.values().stream()
                .map(it -> it.get(ARM64))
                .filter(Objects::nonNull)
                .forEach(type -> type.replaceTypes(it -> replaceType(it, arm64Replacements)));

        metadata.methods()
                .filter(method -> method.name().endsWith(X64_SUFFIX))
                .forEach(method -> method.replaceTypes(it -> replaceType(it, x64Replacements)));
        metadata.methods()
                .filter(method -> method.name().endsWith(ARM64_SUFFIX))
                .forEach(method -> method.replaceTypes(it -> replaceType(it, arm64Replacements)));
    }

    private void renameType(Type type, String suffix) {
        metadata.removeType(type, false);
        type.setName(type.name() + suffix);
        metadata.addType(type, true);
    }

    private Type duplicateType(Type type, int offset) {
        assert type instanceof Struct;
        var struct = (Struct) type;
        return struct.duplicate(struct.typeDefIndex() + offset);
    }

    private Type replaceType(Type type, Map<Type, Type> replacements) {
        var replacement = replacements.get(type);
        if (replacement != null)
            return replacement;

        var replacedType = type;

        if (type instanceof Pointer pointer) {
            var referencedType = pointer.referencedType();
            var replacedRefType = replaceType(referencedType, replacements);
            if (replacedRefType != referencedType)
                replacedType = metadata.makePointerFor(replacedRefType);
        }

        return replacedType;
    }

    private boolean isArchitectureSpecific(Type type, Map<Type, Boolean> visited) {
        var isSpecific = visited.get(type);
        if (isSpecific != null)
            return isSpecific;

        isSpecific = isArchitectureSpecificNonRecursive(type);
        if (isSpecific != null)
            return isSpecific;

        return isArchitectureSpecificRecursive(type, visited);
    }

    private Boolean isArchitectureSpecificNonRecursive(Type type) {
        if ((type instanceof Primitive) || (type instanceof EnumType)) {
            return FALSE;
        } else if ((type instanceof Struct) || (type instanceof Delegate)) {
            return (type.namespace() != null && typeVariants.containsKey(type.name())) ? TRUE : null;
        } else if (type instanceof Pointer) {
            return FALSE;
        } else if (type instanceof TypeAlias typeAlias) {
            return isArchitectureSpecificNonRecursive(typeAlias.aliasedType());
        }

        return null;
    }

    /**
     * Determine recursively if a type is architecture-specific.
     *
     * @param type    the type
     * @param visited visited types and the result of analysis
     * @return {@code true} if the type is architecture-specific, {@code false} otherwise
     */
    @SuppressWarnings("java:S3776")
    private boolean isArchitectureSpecificRecursive(Type type, Map<Type, Boolean> visited) {
        assert !visited.containsKey(type);

        if (type instanceof Struct || type instanceof ComInterface)
            visited.put(type, null); // temporary entry during the visit
        var iterator = type.referencedTypes().iterator();
        while (iterator.hasNext()) {
            var memberType = iterator.next();
            var isSpecific = visited.get(memberType);
            if (isSpecific == null && visited.containsKey(memberType))
                continue;
            if (isSpecific == null)
                isSpecific = isArchitectureSpecificNonRecursive(memberType);
            if (isSpecific == null)
                isSpecific = isArchitectureSpecificRecursive(memberType, visited);
            if (isSpecific) {
                if (type instanceof Struct || type instanceof ComInterface)
                    visited.put(type, TRUE);
                return true;
            }
        }

        if (type instanceof Struct || type instanceof ComInterface)
            visited.put(type, FALSE);
        return false;
    }
}
