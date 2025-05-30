//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.gradle

import net.codecrete.windowsapi.WindowsApiException
import net.codecrete.windowsapi.WindowsApiRun
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * Generates Windows API bindings
 */
@CacheableTask
abstract class WindowsApiTask : DefaultTask() {

    /**
     * A list of Windows API functions names to generate bindings for.
     */
    @get:Input
    @get:Optional
    abstract val functions: ListProperty<String>

    /**
     * A list of Windows API struct and union names to generate bindings for.
     */
    @get:Input
    @get:Optional
    abstract val structs: ListProperty<String>

    /**
     * A list of Windows API enumeration names to generate bindings for.
     */
    @get:Input
    @get:Optional
    abstract val enumerations: ListProperty<String>

    /**
     * A list of Windows API callback function names (function pointers) to generate bindings for.
     */
    @get:Input
    @get:Optional
    abstract val callbackFunctions: ListProperty<String>

    /**
     * A list of Windows API COM interface names to generate bindings for.
     */
    @get:Input
    @get:Optional
    abstract val comInterfaces: ListProperty<String>

    /**
     * A list of Windows API constant names to generate bindings for.
     */
    @get:Input
    @get:Optional
    abstract val constants: ListProperty<String>

    /**
     * Output directory for generated Java code
     */
    @get:OutputDirectory
    @get:Optional
    abstract val outputDirectory: DirectoryProperty

    /**
     * The base package for the generated Java classes.
     *
     * The base package is in addition to the packages assigned by Microsoft,
     * such as `windows.win32.ui.shell`. The default base package name is an empty string,
     * i.e., no additional names are prepended.
     */
    @get:Input
    @get:Optional
    abstract val basePackage: Property<String>

    @TaskAction
    fun generateCode() {
        val run = WindowsApiRun()
        run.eventListener = EventLogger(logger)

        run.functions.addAll(functions.get())
        run.structs.addAll(structs.get())
        run.enumerations.addAll(enumerations.get())
        run.callbackFunctions.addAll(callbackFunctions.get())
        run.comInterfaces.addAll(comInterfaces.get())
        run.constants.addAll(constants.get())
        run.outputDirectory = outputDirectory.get().asFile.toPath()
        run.basePackage = basePackage.get()

        try {
            run.generateCode()
        } catch (_: WindowsApiException) {
            throw InvalidUserDataException("Windows API code generation failed as the arguments are invalid")
        }
    }
}