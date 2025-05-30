//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.metadata;

import java.util.Arrays;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * Method (function)
 * <p>
 * Describes the parameters, return type and further attributes of a
 * Windows API function, COM interface function or delegate.
 * </p>
 */
public class Method {
    private String name;
    private final String nativeName;
    private final Namespace namespace;
    private final int methodDefIndex;
    private Type returnType;
    private Parameter[] parameters;
    private String dll;
    private boolean supportsLastError;
    private Object constantValue;
    private LazyString documentationUrl;

    /**
     * Creates a new method.
     *
     * @param name           the method name
     * @param namespace      the method namespace
     * @param methodDefIndex the {@code MethodDef} index
     */
    public Method(String name, Namespace namespace, int methodDefIndex) {
        this.name = name;
        this.nativeName = name;
        this.namespace = namespace;
        this.methodDefIndex = methodDefIndex;
    }

    /**
     * Gets the method name.
     * <p>
     * For architecture-specific methods, this is the modified method name,
     * as used for Java code.
     * </p>
     *
     * @return the name
     */
    public String name() {
        return name;
    }

    /**
     * Sets the method name.
     * <p>
     * For architecture-specific methods, this is the modified method name,
     * as used for Java code.
     * </p>
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the native method name.
     * <p>
     * For architecture-specific types, this is the original function name used by Windows.
     * </p>
     *
     * @return native method name
     */
    public String nativeName() {
        return nativeName;
    }

    /**
     * Gets the method's namespace.
     *
     * @return the namespace
     */
    public Namespace namespace() {
        return namespace;
    }

    /**
     * Gets the {@code MethodDef} index.
     *
     * @return the index
     */
    public int methodDefIndex() {
        return methodDefIndex;
    }

    /**
     * Replaces the types of the parameters and return type with different types.
     * <p>
     * If the type should not be changed, the given lambda should return the received type.
     * </p>
     *
     * @param typeReplacer lambda providing the target type for the current type
     */
    public void replaceTypes(UnaryOperator<Type> typeReplacer) {
        returnType = typeReplacer.apply(returnType);
        var newParameters = new Parameter[parameters.length];
        for (int i = 0; i < newParameters.length; i++)
            newParameters[i] = new Parameter(parameters[i].name(), typeReplacer.apply(parameters[i].type()));
        parameters = newParameters;
    }

    /**
     * Gets this method's return type.
     * <p>
     * If the method does not return anything, the return type is
     * the primitive <i>void</i> type is returned.
     * </p>
     *
     * @return the return type
     */
    public Type returnType() {
        return returnType;
    }

    /**
     * Sets the return type.
     * <p>
     * If the method does not return anything, the type must be set to
     * the primitive <i>void</i> type.
     * </p>
     *
     * @param returnType the return type
     */
    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    /**
     * Gets the method parameters.
     *
     * @return the parameters (as an array)
     */
    public Parameter[] parameters() {
        assert parameters != null;
        return parameters;
    }

    /**
     * Sets the method parameters.
     *
     * @param parameters the parameters
     */
    public void setParameters(Parameter[] parameters) {
        this.parameters = parameters;
    }

    /**
     * Gets all types directly referenced by this method.
     *
     * @return the referenced types (as a stream)
     */
    public Stream<Type> referencedTypes() {
        return Stream.concat(Stream.of(returnType), Arrays.stream(parameters).map(Parameter::type));
    }

    /**
     * Gets the name of the DLL containing this Windows function.
     * <p>
     * Not applicable to COM interface methods and delegates.
     * </p>
     *
     * @return the DLL name
     */
    public String dll() {
        return dll;
    }

    /**
     * Sets the name of the DLL containing this Windows function.
     * <p>
     * Not applicable to COM interface methods and delegates.
     * </p>
     *
     * @param dll the DLL name
     */
    public void setDll(String dll) {
        this.dll = dll;
    }

    /**
     * Indicates if this method returns anything, i.e., it is not set to the <i>void</i> type.
     *
     * @return {@code true} if the returns anything, {@code false} otherwise
     */
    public boolean hasReturnType() {
        assert returnType != null;
        return !(returnType instanceof Primitive primitive && primitive.kind() == PrimitiveKind.VOID);
    }

    /**
     * Indicates if this method uses the {@code GetLastError} function to provide error details.
     *
     * @return {@code true} if {@code GetLastError} is supported
     */
    public boolean supportsLastError() {
        return supportsLastError;
    }

    /**
     * Sets if this method uses the {@code GetLastError} function to provide error details.
     *
     * @param supportsLastError {@code true} if {@code GetLastError} is supported, {@code false} otherwise
     */
    public void setSupportsLastError(boolean supportsLastError) {
        this.supportsLastError = supportsLastError;
    }

    /**
     * Returns the constant value returned by this method.
     * <p>
     * A few Windows functions do not exist but are C macros returning a constant value.
     * This is the constant value.
     * </p>
     *
     * @return the constant value
     */
    public Object constantValue() {
        return constantValue;
    }

    /**
     * Returns the constant value returned by this method.
     * <p>
     * A few Windows functions do not exist but are C macros returning a constant value.
     * This is the constant value.
     * </p>
     *
     * @param constantValue the constant value
     */
    public void setConstantValue(Object constantValue) {
        this.constantValue = constantValue;
    }

    /**
     * Returns the URL to Microsoft's documentation about this type.
     *
     * @return the URL
     */
    public final LazyString documentationUrl() {
        return documentationUrl;
    }

    /**
     * Sets the URL to Microsoft's documentation about this type.
     *
     * @param documentationUrl the URL
     */
    public final void setDocumentationUrl(LazyString documentationUrl) {
        this.documentationUrl = documentationUrl;
    }
}
