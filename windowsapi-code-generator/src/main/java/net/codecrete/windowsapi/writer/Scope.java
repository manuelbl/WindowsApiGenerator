//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.writer;

import net.codecrete.windowsapi.events.Event;
import net.codecrete.windowsapi.events.EventListener;
import net.codecrete.windowsapi.metadata.Array;
import net.codecrete.windowsapi.metadata.ConstantValue;
import net.codecrete.windowsapi.metadata.Metadata;
import net.codecrete.windowsapi.metadata.Method;
import net.codecrete.windowsapi.metadata.Namespace;
import net.codecrete.windowsapi.metadata.Pointer;
import net.codecrete.windowsapi.metadata.Primitive;
import net.codecrete.windowsapi.metadata.Type;
import net.codecrete.windowsapi.metadata.TypeAlias;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Manages a scope types, functions, and constants to generate.
 * <p>
 * Computes the transitive scope by adding indirectly used metadata
 * to the initially specified scope.
 * </p>
 */
public class Scope {
    private static final String NOT_FOUND_TEMPLATE = "%s \"%s\" does not exist.";
    private static final String DID_YOU_MEAN_TEMPLATE = "%s Did you mean \"%s\"?.";
    private static final String ENUMERATION_MEMBER_SINGLE = "%s Enumeration \"%s\" contains a member with that name. " +
            "Specify the enumeration instead of the constant.";

    private final Set<Type> typeSet = new HashSet<>();
    private final Set<Method> methodSet = new HashSet<>();
    private final Set<ConstantValue> constantSet = new HashSet<>();
    private final Set<Type> transitiveScope = new HashSet<>();
    private final Metadata metadata;
    private final EventListener eventListener;
    private boolean hasInvalidArguments = false;

    /**
     * Creates a new scope.
     *
     * @param metadata      the metadata
     * @param eventListener an event listener to notify about events (in particular validation errors)
     */
    public Scope(Metadata metadata, EventListener eventListener) {
        this.metadata = metadata;
        this.eventListener = eventListener;
    }

    Set<Type> types() {
        return typeSet;
    }

    Set<ConstantValue> constants() {
        return constantSet;
    }

    Set<Method> methods() {
        return methodSet;
    }

    /**
     * Indicates if some of the arguments that were set are invalid.
     *
     * @return {@code true} if arguments are invalid, {@code false} otherwise
     */
    public boolean hasInvalidArguments() {
        return hasInvalidArguments;
    }

    /**
     * Adds structs and unions to this scope.
     * <p>
     * For invalid struct names, error events are emitted.
     * But no exception is thrown.
     * </p>
     *
     * @param structs names of structs and unions
     */
    public void addStructs(Set<String> structs) {
        var foundStructs = metadata.findStructs(structs);
        var foundStructNames = foundStructs.stream().map(Type::nativeName).collect(Collectors.toSet());
        var missingStructs =
                structs.stream().filter(name -> !foundStructNames.contains(name)).collect(Collectors.toSet());
        if (missingStructs.isEmpty()) {
            addTypes(foundStructs);
        } else {
            hasInvalidArguments = true;
            for (var struct : missingStructs) {
                var wideStringType = metadata.findStructs(Set.of(struct + "W"))
                        .stream().map(Type::name).findFirst();
                emitNotFoundError("structs", struct, "Struct/union", wideStringType.orElse(null));
            }
        }
    }

    /**
     * Adds enumerations to this scope.
     * <p>
     * For invalid enumeration names, error events are emitted.
     * But no exception is thrown.
     * </p>
     *
     * @param enumerations names of enumerations
     */
    public void addEnums(Set<String> enumerations) {
        var foundEnums = metadata.findEnums(enumerations);
        var foundEnumNames = foundEnums.stream().map(Type::name).collect(Collectors.toSet());
        var missingEnums =
                enumerations.stream().filter(name -> !foundEnumNames.contains(name)).collect(Collectors.toSet());
        if (missingEnums.isEmpty()) {
            addTypes(foundEnums);
        } else {
            hasInvalidArguments = true;
            for (var enumName : missingEnums)
                emitNotFoundError("enumerations", enumName, "Enumeration", null);
        }
    }

    /**
     * Adds callback functions to this scope.
     * <p>
     * For invalid callback function names, error events are emitted.
     * But no exception is thrown.
     * </p>
     *
     * @param callbackFunctions names of callback functions
     */
    public void addCallbackFunctions(Set<String> callbackFunctions) {
        var foundCallbackFunctions = metadata.findDelegates(callbackFunctions);
        var foundCallbackFunctionNames = foundCallbackFunctions.stream().map(Type::name).collect(Collectors.toSet());
        var missingCallbackFunctions =
                callbackFunctions.stream().filter(name -> !foundCallbackFunctionNames.contains(name)).collect(Collectors.toSet());

        if (missingCallbackFunctions.isEmpty()) {
            addTypes(foundCallbackFunctions);
        } else {
            hasInvalidArguments = true;
            for (var callbackFunction : missingCallbackFunctions) {
                var wideStringCallbackFunction = metadata.findDelegates(Set.of(callbackFunction + "W"))
                        .stream().map(Type::name).findFirst().orElse(null);
                emitNotFoundError("callbackFunctions", callbackFunction, "Callback function",
                        wideStringCallbackFunction);
            }
        }
    }

    /**
     * Adds COM interfaces to this scope.
     * <p>
     * For invalid COM interface names, error events are emitted.
     * But no exception is thrown.
     * </p>
     *
     * @param comInterfaces names of COM interfaces
     */
    public void addComInterfaces(Set<String> comInterfaces) {
        var foundComInterfaces = metadata.findComInterfaces(comInterfaces);
        var foundComInterfaceNames = foundComInterfaces.stream().map(Type::name).collect(Collectors.toSet());
        var missingComInterfaces =
                comInterfaces.stream().filter(name -> !foundComInterfaceNames.contains(name)).collect(Collectors.toSet());
        if (missingComInterfaces.isEmpty()) {
            addTypes(foundComInterfaces);
        } else {
            hasInvalidArguments = true;
            for (var comInterface : missingComInterfaces)
                emitNotFoundError("comInterfaces", comInterface, "COM interface", null);
        }
    }

    /**
     * Adds constants to this scope.
     * <p>
     * For invalid constant names, error events are emitted.
     * But no exception is thrown.
     * </p>
     *
     * @param constants names of constants
     */
    public void addConstants(Set<String> constants) {
        var foundConstants = metadata.findConstants(constants);
        var foundConstantNames = foundConstants.stream().map(ConstantValue::name).collect(Collectors.toSet());
        var missingConstants =
                constants.stream().filter(name -> !foundConstantNames.contains(name)).collect(Collectors.toSet());
        if (missingConstants.isEmpty()) {
            constantSet.addAll(foundConstants);
        } else {
            hasInvalidArguments = true;
            for (var constant : missingConstants) {
                var alternatives = metadata.findEnumWithMember(constant);
                var alternative = !alternatives.isEmpty() ? alternatives.getFirst().name() : null;
                emitNotFoundErrorForConstant(constant, alternative);
            }
        }
    }

    /**
     * Adds functions to this scope.
     * <p>
     * For invalid function names, error events are emitted.
     * But no exception is thrown.
     * </p>
     */
    public void addFunctions(Set<String> functions) {
        var foundFunctions = metadata.findFunctions(functions);
        var foundFunctionNames =
                foundFunctions.stream().map(Method::nativeName).collect(Collectors.toSet());
        var missingFunctions =
                functions.stream().filter(name -> !foundFunctionNames.contains(name)).collect(Collectors.toSet());

        if (missingFunctions.isEmpty()) {
            methodSet.addAll(foundFunctions);
        } else {
            hasInvalidArguments = true;
            for (var function : missingFunctions) {
                var alternative = metadata.findFunctions(Set.of(function + "W"))
                        .stream().map(Method::name).findFirst().orElse(null);
                emitNotFoundError("functions", function, "Function", alternative);
            }
        }
    }

    void addTypes(Collection<Type> types) {
        var newTypes = types.stream().filter(t -> !typeSet.contains(t)).toList();
        typeSet.addAll(newTypes);
    }

    private void emitNotFoundError(String argumentName, String argumentValue, String elementType, String alternative) {
        var reason = String.format(NOT_FOUND_TEMPLATE, elementType, argumentValue);
        if (alternative != null)
            reason = String.format(DID_YOU_MEAN_TEMPLATE, reason, alternative);
        eventListener.onEvent(new Event.InvalidArgument(argumentName, argumentValue, reason));
    }

    private void emitNotFoundErrorForConstant(String argumentValue, String alternative) {
        var reason = String.format(NOT_FOUND_TEMPLATE, "Constant", argumentValue);
        if (alternative != null)
            reason = String.format(ENUMERATION_MEMBER_SINGLE, reason, alternative);
        eventListener.onEvent(new Event.InvalidArgument("constants", argumentValue, reason));
    }

    /**
     * Builds the transitive scope from the registered types and functions.
     * <p>
     * Primitive types, pointers, type aliases, and arrays are excluded
     * as they will not result in a Java file.
     * </p>
     */
    public void buildTransitiveScope() {
        if (hasInvalidArguments)
            throw new IllegalStateException("The transitive scope cannot be built as invalid arguments were set.");

        transitiveScope.addAll(typeSet);
        typeSet.forEach(type -> addDependencies(type.referencedTypes()));
        methodSet.forEach(method -> addDependencies(method.referencedTypes()));
        addDependencies(constantSet.stream().map(ConstantValue::type));
        if (methodSet.stream().anyMatch(Method::supportsLastError))
            transitiveScope.add(metadata.getType("Windows.Win32.Foundation", "WIN32_ERROR"));
        transitiveScope.removeIf(type -> !generatesJavaFile(type));
    }

    private void addDependencies(Stream<Type> types) {
        var iterator = types.iterator();
        while (iterator.hasNext()) {
            var type = iterator.next();
            if (type instanceof Primitive || type instanceof TypeAlias)
                continue; // optimization as they will not extend the scope
            if (transitiveScope.add(type))
                addDependencies(type.referencedTypes());
        }
    }

    private boolean generatesJavaFile(Type type) {
        return !(type instanceof Primitive || type instanceof TypeAlias || type instanceof Pointer || type instanceof Array);
    }

    /**
     * Gets the types of this transitive scope.
     *
     * @return types
     */
    Set<Type> getTransitiveTypeScope() {
        return transitiveScope;
    }

    /**
     * Gets the functions grouped by namespace.
     *
     * @return lists of functions, index by namespace
     */
    Map<Namespace, List<Method>> getFunctions() {
        return methodSet.stream().collect(Collectors.groupingBy(Method::namespace));
    }

    /**
     * Gets the constants grouped by namespace.
     *
     * @return lists of constants, indexed by namespace
     */
    Map<Namespace, List<ConstantValue>> getConstants() {
        return constantSet.stream().collect(Collectors.groupingBy(ConstantValue::namespace));
    }
}
