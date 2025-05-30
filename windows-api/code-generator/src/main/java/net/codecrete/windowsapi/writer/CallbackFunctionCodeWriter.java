//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.writer;

import net.codecrete.windowsapi.metadata.Delegate;
import net.codecrete.windowsapi.metadata.Method;

/**
 * Creates the Java code for a callback function.
 */
class CallbackFunctionCodeWriter extends FunctionCodeWriterBase<Delegate> {

    private final CommentWriter commentWriter = new CommentWriter();

    /**
     * Creates a new instance.
     *
     * @param generationContext the code generation context
     */
    CallbackFunctionCodeWriter(GenerationContext generationContext) {
        super(generationContext);
    }

    /**
     * Creates a new file and containing the Java code for the callback function.
     *
     * @param delegate the callback function
     */
    void writeCallbackFunction(Delegate delegate) {
        var className = toJavaClassName(delegate.name());
        withFile(delegate.namespace(), delegate, className, this::writeCallbackFunctionContent);
    }

    private void writeCallbackFunctionContent() {
        var signature = type.signature();
        writer.printf("""
                package %1$s;
                
                import java.lang.foreign.*;
                import java.lang.invoke.*;
                import static java.lang.foreign.ValueLayout.*;
                
                """, packageName);

        writeCallbackFunctionComment();

        writer.printf("""
                public class %1$s {
                """, className);

        // function interface
        writeComment("Callback function signature as a functional Java interface.");
        writer.print("""
                    public interface Function {
                """);
        writeCallbackFunctionInterfaceInvokeComment(signature);
        writer.print("        ");
        writeFunctionSignature(signature, "invoke");
        writer.println(";");
        writer.print("""
                    }
                
                """);

        // function descriptor accessor
        writeComment("Gets the function descriptor of the callback function.");
        writer.print("""
                    public static FunctionDescriptor descriptor() {
                        return $DESC;
                    }
                
                """);

        // allocate function pointer
        writeComment("Allocates an upcall stub that will call the given function.");
        writer.printf("""
                    public static MemorySegment allocate(Arena arena, %s.Function function) {
                        return Linker.nativeLinker().upcallStub(UPCALL$MH.bindTo(function), $DESC, arena);
                    }
                """, className);
        writer.println();

        // invoke the function pointer
        writeCallbackFunctionInvokeComment(signature);
        var optionalComma = signature.parameters().length > 0 ? ", " : "";
        writer.print("    public static ");
        writeFunctionSignatureIntro(signature, "invoke");
        writer.print("MemorySegment callbackFunction" + optionalComma);
        writeFunctionSignatureParameters(signature);
        writer.println(" {");
        writeInvoke(signature,
                "DOWNCALL$MH.invokeExact(callbackFunction" + optionalComma, 8);
        writer.println("    }");
        writer.println();

        // address layouts
        AddressLayout.requiredLayouts(signature).forEach(layoutType ->
                writeAddressLayoutInitialization(layoutType, "private static final "));
        writer.println();

        // function descriptor
        writer.print("    private static final FunctionDescriptor $DESC = ");
        writeFunctionDescriptor(signature, null);
        writer.println(";");
        writer.println();

        // method upcall handle lookup
        writer.printf("""
                    private static MethodHandle createUpcallHandle() {
                        try {
                            return MethodHandles.lookup().findVirtual(%1$s.Function.class, "invoke", $DESC.toMethodType());
                        } catch (ReflectiveOperationException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                
                    private static final MethodHandle UPCALL$MH = createUpcallHandle();
                
                """, className);

        // downcall handle
        writer.println("    private static final MethodHandle DOWNCALL$MH = Linker.nativeLinker().downcallHandle" +
                "($DESC);");
        writer.println();

        // private constructor
        writer.printf("""
                    private %1$s() {}
                }
                """, className);
    }

    private void writeCallbackFunctionComment() {
        writer.printf("""
                /**
                 * {@code %1$s} callback function
                """, type.nativeName());

        writeDocumentationUrl(type);

        writer.println(" */");
    }

    void writeCallbackFunctionInterfaceInvokeComment(Method method) {
        writer.printf("""
                        /**
                         * Invokes the callback function.
                         * <p>
                         * Implement this method to write the callback function in Java.
                         * </p>
                         * <p>
                         * {@snippet lang=c :
                """);

        commentWriter.writeFunctionSignatureIntro(writer, method, type.nativeName(), 8);
        commentWriter.writeFunctionSignatureParameters(writer, method, 8);

        writer.print("""
                         * );
                         * }
                         * </p>
                         */
                """);
    }

    void writeCallbackFunctionInvokeComment(Method method) {
        writer.printf("""
                    /**
                     * Invokes the callback function.
                     * <p>
                     * This method calls the given callback function, implemented in native code.
                     * </p>
                     * <p>
                     * {@snippet lang=c :
                """);

        commentWriter.writeFunctionSignatureIntro(writer, method, "CallbackFunction", 4);
        commentWriter.writeFunctionSignatureParameters(writer, method, 4);

        writer.print("""
                     * );
                     * }
                     * </p>
                     */
                """);
    }
}
