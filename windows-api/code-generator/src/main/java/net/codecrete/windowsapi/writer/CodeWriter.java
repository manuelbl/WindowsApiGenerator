//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.writer;

import net.codecrete.windowsapi.events.EventListener;
import net.codecrete.windowsapi.metadata.ComInterface;
import net.codecrete.windowsapi.metadata.Delegate;
import net.codecrete.windowsapi.metadata.EnumType;
import net.codecrete.windowsapi.metadata.Metadata;
import net.codecrete.windowsapi.metadata.Struct;
import net.codecrete.windowsapi.metadata.Type;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Generates Java code for a given scope of types, functions, and constants.
 */
public class CodeWriter extends JavaCodeWriter<Type> {

    private final Path outputDirectory;
    private final StructCodeWriter structCodeWriter;
    private final EnumCodeWriter enumCodeWriter;
    private final FunctionCodeWriter functionCodeWriter;
    private final CallbackFunctionCodeWriter callbackFunctionCodeWriter;
    private final ConstantCodeWriter constantCodeWriter;
    private final ComInterfaceWriter comInterfaceWriter;

    /**
     * Creates a new instance.
     *
     * @param metadata        the metadata
     * @param outputDirectory the output directory
     * @param eventListener   the event listener to notify about events
     */
    public CodeWriter(Metadata metadata, Path outputDirectory, EventListener eventListener) {
        super(new GenerationContext(metadata, eventListener));
        generationContext().setWriterFactory(this::createFileWriter);

        this.outputDirectory = outputDirectory;
        structCodeWriter = new StructCodeWriter(generationContext());
        enumCodeWriter = new EnumCodeWriter(generationContext());
        functionCodeWriter = new FunctionCodeWriter(generationContext());
        callbackFunctionCodeWriter = new CallbackFunctionCodeWriter(generationContext());
        constantCodeWriter = new ConstantCodeWriter(generationContext());
        comInterfaceWriter = new ComInterfaceWriter(generationContext());

        if (Files.notExists(outputDirectory))
            throw new IllegalArgumentException("Output directory does not exist: " + outputDirectory);
    }

    private PrintWriter createFileWriter(Path path) {
        var fullPath = outputDirectory.resolve(path);

        try {
            // create the directory if needed
            var directory = fullPath.getParent().toFile();
            if (!directory.exists()) {
                var success = directory.mkdirs();
                if (!success)
                    throw new GenerationException("Unable to create directory " + directory);
            }

            // create the file
            var file = fullPath.toFile();
            return new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8));

        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to write Java file " + path, exception);
        }
    }

    private static PrintWriter createNullWriter(Path path) {
        return new PrintWriter(OutputStream.nullOutputStream());
    }

    /**
     * Sets the base package.
     * <p>
     * The base package is prepended to all package names derived from Microsoft's namespace.
     * The default is an empty string, i.e., no further package name is added.
     * </p>
     *
     * @param basePackage the base package name
     */
    public void setBasePackage(String basePackage) {
        generationContext().setBasePackage(basePackage);
    }

    /**
     * Sets if this code writer should execute a dry run without creating files and directories.
     * <p>
     * Initially, it is set to {@code false}.
     * </p>
     *
     * @param isDryRun {@code true} for dry run, {@code false} for real run
     */
    public void setDryRun(boolean isDryRun) {
        generationContext.setWriterFactory(isDryRun ? CodeWriter::createNullWriter : this::createFileWriter);
    }

    /**
     * Writes the Java code for the specified scope of types, functions, and constants.
     *
     * @param scope the scope
     */
    public void write(Scope scope) {
        scope.getTransitiveTypeScope().forEach(this::writeType);
        scope.getFunctions().forEach(functionCodeWriter::writeFunctions);
        scope.getConstants().forEach(constantCodeWriter::writeConstants);
    }

    /**
     * Writes the Java code for all types, functions, and constants.
     * <p>
     * This method is used for tests.
     * </p>
     */
    public void writeAll() {
        var metadata = generationContext.metadata();
        metadata.types().forEach(this::writeType);

        metadata.namespaces().values().stream()
                .filter(n -> !n.methods().isEmpty())
                .forEach(namespace -> functionCodeWriter.writeFunctions(namespace, namespace.methods().values()));


        metadata.namespaces().values().stream()
                .filter(n -> !n.constants().isEmpty())
                .forEach(namespace -> constantCodeWriter.writeConstants(namespace, namespace.constants().values()));
    }

    private void writeType(Type type) {
        switch (type) {
            case Struct struct when struct.namespace() != null -> structCodeWriter.writeStructOrUnion(struct);
            case EnumType enumType -> enumCodeWriter.writeEnum(enumType);
            case Delegate delegate -> callbackFunctionCodeWriter.writeCallbackFunction(delegate);
            case ComInterface comInterface -> comInterfaceWriter.writeComInterface(comInterface);
            default -> {
                // nothing to do
            }
        }
    }
}
