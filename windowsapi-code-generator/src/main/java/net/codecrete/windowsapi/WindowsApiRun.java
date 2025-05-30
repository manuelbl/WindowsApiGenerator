//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi;

import net.codecrete.windowsapi.events.Event;
import net.codecrete.windowsapi.events.EventListener;
import net.codecrete.windowsapi.winmd.MetadataBuilder;
import net.codecrete.windowsapi.writer.CodeWriter;
import net.codecrete.windowsapi.writer.GenerationException;
import net.codecrete.windowsapi.writer.Scope;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

/**
 * A single run to generate Java source code for accessing the Windows API.
 * <p>
 * To generate code, create an instance of this class, set the configuration arguments
 * and call {@link #generateCode()}.
 * </p>
 * <p>
 * The generated source code uses the Java Foreign Function and Memory API (FFM)
 * and requires Java 23 or higher.
 * </p>
 *
 * @see
 * <a href="https://docs.oracle.com/en/java/javase/23/core/foreign-function-and-memory-api.html">Foreign Function and Memory API</a>
 */
public class WindowsApiRun {
    private Path outputDirectory;
    private String basePackage = "";
    private EventListener eventListener = new NullEventListener();

    private Set<String> structs = new HashSet<>();
    private Set<String> functions = new HashSet<>();
    private Set<String> enumerations = new HashSet<>();
    private Set<String> callbackFunctions = new HashSet<>();
    private Set<String> comInterfaces = new HashSet<>();
    private Set<String> constants = new HashSet<>();

    /**
     * Gets the directory for generating the source code.
     * <p>
     * The specified directory is the root directory for packages and subpackages.
     * </p>
     *
     * @return the directory
     */
    public Path getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * Sets the directory for generating the source code.
     * <p>
     * The specified directory is the root directory for packages and subpackages.
     * </p>
     *
     * @param outputDirectory the directory
     */
    public void setOutputDirectory(Path outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    /**
     * Gets the base package for the generated code.
     * <p>
     * The generated code will have package names starting with this base package.
     * The Windows API data structures and functions are assigned to packages such
     * as {@code windows.win32.ui.windowsandmessaging}. The base package is prepended.
     * The default is an empty string, i.e., no additional package name.
     * </p>
     *
     * @return the base package name
     */
    public String getBasePackage() {
        return basePackage;
    }

    /**
     * Sets the base package for the generated code.
     * <p>
     * The generated code will have package names starting with this base package.
     * The Windows API data structures and functions are assigned to packages such
     * as {@code windows.win32.ui.windowsandmessaging}. The base package is prepended.
     * A valid package name looks like {@code com.company.product}.
     * The default is an empty string, i.e., no additional package name.
     * </p>
     *
     * @param basePackage base package name
     */
    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    /**
     * Gets the names of the C struct and union types to generate.
     *
     * @return struct/union names
     */
    public Set<String> getStructs() {
        return structs;
    }

    /**
     * Sets the names of the C struct and union types to generate.
     *
     * @param structs the struct/union names
     */
    public void setStructs(Set<String> structs) {
        this.structs = structs;
    }

    /**
     * Gets the names of the Windows API functions to generate.
     *
     * @return the function names
     */
    public Set<String> getFunctions() {
        return functions;
    }

    /**
     * Sets the names of the Windows API functions to generate.
     * <p>
     * Note that if an ANSI and Unicode version of a function exists, the exact version must be specified.
     * ANSI versions usually end with the uppercase A while Unicode versions end in the uppercase W.
     * </p>
     *
     * @param functions the function names
     */
    public void setFunctions(Set<String> functions) {
        this.functions = functions;
    }

    /**
     * Gets the names of the enumerations to generate.
     *
     * @return the enumeration names
     */
    public Set<String> getEnumerations() {
        return enumerations;
    }

    /**
     * Sets the names of the enumerations to generate.
     *
     * @param enumerations the enumeration names
     */
    public void setEnumerations(Set<String> enumerations) {
        this.enumerations = enumerations;
    }

    /**
     * Gets the names of the callback functions (function pointers) to generate.
     *
     * @return the callback function names
     */
    public Set<String> getCallbackFunctions() {
        return callbackFunctions;
    }

    /**
     * Sets the names of the callback functions (function pointers) to generate.
     *
     * @param callbackFunctions the callback function names
     */
    public void setCallbackFunctions(Set<String> callbackFunctions) {
        this.callbackFunctions = callbackFunctions;
    }

    /**
     * Gets the names of the COM interfaces to generate.
     *
     * @return the COM interface names
     */
    public Set<String> getComInterfaces() {
        return comInterfaces;
    }

    /**
     * Sets the names of the COM interfaces to generate.
     *
     * @param comInterfaces the COM interface names
     */
    public void setComInterfaces(Set<String> comInterfaces) {
        this.comInterfaces = comInterfaces;
    }

    /**
     * Gets the names of the constants to generate.
     *
     * @return the constant names
     */
    public Set<String> getConstants() {
        return constants;
    }

    /**
     * Sets the names of the constants to generate.
     *
     * @param constants the constant names
     */
    public void setConstants(Set<String> constants) {
        this.constants = constants;
    }

    /**
     * Gets the event listener.
     * <p>
     * The event listener is notified about events such as code generation progress or validation errors.
     * </p>
     *
     * @return the event listener
     */
    public EventListener getEventListener() {
        return eventListener;
    }

    /**
     * Sets the event listener.
     * <p>
     * The event listener is notified about events such as code generation progress or validation errors.
     * </p>
     *
     * @param eventListener the event listener
     */
    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    /**
     * Generates the code.
     */
    public void generateCode() {
        generate(false);
    }

    /**
     * Executes a dry run.
     * <p>
     * A dry run does not write or delete any files or directories.
     * </p>
     */
    public void dryRun() {
        generate(true);
    }

    private void generate(boolean isDryRun) {
        if (!isAnyWork())
            return;

        var metadata = MetadataBuilder.load();

        var scope = new Scope(metadata, eventListener);
        scope.addStructs(structs);
        scope.addEnums(enumerations);
        scope.addFunctions(functions);
        scope.addCallbackFunctions(callbackFunctions);
        scope.addComInterfaces(comInterfaces);
        scope.addConstants(constants);

        if (scope.hasInvalidArguments())
            throw new WindowsApiException("Invalid arguments specified for Windows API code generation");

        scope.buildTransitiveScope();

        var writer = new CodeWriter(metadata, outputDirectory, eventListener);
        writer.setDryRun(isDryRun);
        writer.setBasePackage(basePackage);
        writer.write(scope);
    }

    private boolean isAnyWork() {
        return !functions.isEmpty() || !structs.isEmpty() || !constants.isEmpty()
                || !enumerations.isEmpty() || !callbackFunctions.isEmpty() || !comInterfaces.isEmpty();
    }

    /**
     * Creates the specified directory.
     *
     * @param path path of directory
     */
    public void createDirectory(Path path) {
        var directory = path.toFile();
        if (directory.exists())
            return;

        if (!directory.mkdirs())
            throw new GenerationException("Unable to create directory " + path);

        eventListener.onEvent(new Event.DirectoryCreated(path));
    }

    /**
     * Cleans the output directory.
     * <p>
     * All files in the directory are deleted but not the directory itself.
     * </p>
     */
    public void cleanOutputDirectory() {
        if (!outputDirectory.toFile().exists())
            return;

        try {
            Files.walkFileTree(outputDirectory, new FileVisitor<>() {
                @Override
                @SuppressWarnings("NullableProblems")
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                @SuppressWarnings("NullableProblems")
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                @SuppressWarnings("NullableProblems")
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    throw new UncheckedIOException("Unable to clean output directory " + outputDirectory, exc);
                }

                @Override
                @SuppressWarnings("NullableProblems")
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (!dir.equals(outputDirectory))
                        Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });

            eventListener.onEvent(new Event.DirectoryCleaned(outputDirectory));

        } catch (IOException exc) {
            throw new UncheckedIOException("Unable to clean output directory " + outputDirectory, exc);
        }
    }

    static class NullEventListener implements EventListener {
        @Override
        public void onEvent(Event event) {
            // no output
        }
    }
}
