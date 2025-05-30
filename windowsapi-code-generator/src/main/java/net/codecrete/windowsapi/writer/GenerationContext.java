//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.writer;

import net.codecrete.windowsapi.events.Event;
import net.codecrete.windowsapi.events.EventListener;
import net.codecrete.windowsapi.metadata.Metadata;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * Code generation context.
 * <p>
 * This common data is shared between all Java code writers.
 * </p>
 */
class GenerationContext {
    final Metadata metadata;
    protected Function<Path, PrintWriter> writerFactory;
    protected final EventListener eventListener;
    protected String basePackage = "";

    /**
     * Creates a new instance.
     *
     * @param metadata      the metadata
     * @param eventListener the event listener
     */
    GenerationContext(Metadata metadata, EventListener eventListener) {
        this.metadata = metadata;
        this.eventListener = eventListener;
    }

    /**
     * Gets the metadata.
     *
     * @return the metadata
     */
    Metadata metadata() {
        return metadata;
    }

    /**
     * Notifies the listener about an event.
     *
     * @param event the event
     */
    void notify(Event event) {
        eventListener.onEvent(event);
    }

    /**
     * Gets the base package name.
     *
     * @return the base package
     */
    String basePackage() {
        return basePackage;
    }

    /**
     * Sets the base package name.
     *
     * @param basePackage the base package
     */
    void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    /**
     * Sets the writer factory.
     * <p>
     * The writer factory takes a relative file name, creates a file relative to the
     * output directory and returns a writer to write to the file.
     * </p>
     *
     * @param writerFactory the writer factory
     */
    void setWriterFactory(Function<Path, PrintWriter> writerFactory) {
        this.writerFactory = writerFactory;
    }

    /**
     * Creates a new print writer for the specified relative path.
     *
     * @param path the path
     * @return the new print writer instance
     */
    PrintWriter createWriter(Path path) {
        return writerFactory.apply(path);
    }
}
