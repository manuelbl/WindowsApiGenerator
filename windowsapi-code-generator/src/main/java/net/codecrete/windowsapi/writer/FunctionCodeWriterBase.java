//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.writer;

import net.codecrete.windowsapi.metadata.Method;
import net.codecrete.windowsapi.metadata.Type;

/**
 * Base class for code writers generating function descriptors and calls.
 *
 * @param <T> the metadata type
 */
class FunctionCodeWriterBase<T extends Type> extends JavaCodeWriter<T> {

    /**
     * Creates a new instance.
     *
     * @param generationContext the code generation context
     */
    protected FunctionCodeWriterBase(GenerationContext generationContext) {
        super(generationContext);
    }

    /**
     * Writes the Java code for creating a function descriptor.
     *
     * @param method        the function
     * @param thisParameter the additional {@code this} parameter (or {@code null})
     */
    protected void writeFunctionDescriptor(Method method, String thisParameter) {
        writer.print("FunctionDescriptor.");
        if (method.hasReturnType()) {
            writer.print("of(");
            writer.print(getLayoutName(method.returnType(), null));
        } else {
            writer.print("ofVoid(");
        }

        if (thisParameter != null) {
            if (method.hasReturnType())
                writer.print(", ");
            writer.print(thisParameter);
        }

        var parameters = method.parameters();
        for (int i = 0; i < parameters.length; i += 1) {
            writer.print(i > 0 || thisParameter != null || method.hasReturnType() ? ", " : "");
            writer.print(getLayoutName(parameters[i].type(), null));
        }
        writer.print(")");
    }

    /**
     * Writes the Java method signature for the given function.
     *
     * @param function     the function
     * @param functionName the function name
     */
    protected void writeFunctionSignature(Method function, String functionName) {
        writeFunctionSignatureIntro(function, functionName);
        writeFunctionSignatureParameters(function);
    }

    /**
     * Writes the Java method signature intro (return type, function name, opening parenthesis)
     *
     * @param function     the function
     * @param functionName the function name
     */
    protected void writeFunctionSignatureIntro(Method function, String functionName) {
        writer.printf("%s %s(",
                function.hasReturnType() ? getJavaType(function.returnType()) : "void",
                functionName);
    }

    /**
     * Writes the parameters of the Java method signature (without opening and closing parentheses)
     *
     * @param function the function
     */
    protected void writeFunctionSignatureParameters(Method function) {
        var parameters = function.parameters();
        if (function.supportsLastError())
            writer.print("MemorySegment lastErrorState");

        for (int i = 0; i < parameters.length; i += 1) {
            writer.printf("%s%s %s",
                    i > 0 || function.supportsLastError() ? ", " : "",
                    getJavaType(parameters[i].type()),
                    getJavaSafeName(parameters[i].name()));
        }
        writer.print(")");
    }

    /**
     * Writes the Java code for invoking a native function through a method handle.
     *
     * @param function  the function
     * @param invoke    the name of the method handle to invoke
     * @param indenting the indenting (number of spaces)
     */
    protected void writeInvoke(Method function, String invoke, int indenting) {
        var indent = getIndent(indenting);
        var returnWithCast = function.hasReturnType()
                ? String.format("return (%s) ", getJavaType(function.returnType()))
                : "";

        writer.printf("""
                %1$stry {
                %1$s    %2$s%3$s""", indent, returnWithCast, invoke);

        var supportsLastError = function.supportsLastError();
        if (supportsLastError)
            writer.print("lastErrorState");

        var parameters = function.parameters();
        for (int i = 0; i < parameters.length; i += 1) {
            writer.print(i > 0 || supportsLastError ? ", " : "");
            writer.print(getJavaSafeName(parameters[i].name()));
        }
        writer.println(");");

        writer.printf("""
                %1$s} catch (Throwable ex) {
                %1$s    throw new RuntimeException(ex);
                %1$s}
                """, indent);
    }
}
