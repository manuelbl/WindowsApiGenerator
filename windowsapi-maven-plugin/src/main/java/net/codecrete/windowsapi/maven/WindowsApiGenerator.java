//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.maven;

import net.codecrete.windowsapi.WindowsApiRun;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;

/**
 * Generates Java bindings for the Windows API.
 * <p>
 * Code can be generated for Windows functions, the associated data structures
 * (C struct and union), enumerations, callback functions, and COM interfaces.
 * </p>
 * <p>
 * The generated code uses the Java Foreign Functions and Memory (FFM) API
 * for native access.
 * </p>
 */
@Mojo(name = "windows-api", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class WindowsApiGenerator extends AbstractMojo {
    /**
     * The Maven project context.
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject mavenProject;

    /**
     * A list of Windows API functions names to generate bindings for.
     */
    @Parameter(name = "functions")
    List<String> functions;

    /**
     * A list of Windows API struct and union names to generate bindings for.
     */
    @Parameter(name = "structs")
    List<String> structs;

    /**
     * A list of Windows API enumeration names to generate bindings for.
     */
    @Parameter
    List<String> enumerations;

    /**
     * A list of Windows API callback function names (function pointers) to generate bindings for.
     */
    @Parameter
    List<String> callbackFunctions;

    /**
     * A list of Windows API COM interfaces to generate bindings for.
     */
    @Parameter
    List<String> comInterfaces;

    /**
     * A list of Windows API constant names (function pointers) to generate bindings for.
     */
    @Parameter
    List<String> constants;

    /**
     * Location of the output directory for the generated code.
     */
    @Parameter(name = "outputDirectory", defaultValue = "${project.build.directory}/generated-sources/windows-api")
    Path outputDirectory;

    /**
     * The source directory within the output directory (relative path).
     * <p>
     * Use forward slashes to separate directory names.
     * </p>
     */
    @Parameter(name = "sourceDirectory", defaultValue = "src/main/java")
    String sourceDirectory;

    /**
     * The base package for the generated Java classes.
     * <p>
     * The base package is in addition to the packages assigned by Microsoft,
     * such as {@code windows.win32.ui.shell}. The default base package name is an empty string,
     * i.e., no additional names are prepended.
     * </p>
     */
    @Parameter(name = "basePackage")
    String basePackage;

    /**
     * If set to {@code true}, adds the source directory as a source root
     * so the generated code will be compiled and included in the resulting artifact.
     */
    @Parameter(name = "addAsSourceRoot", defaultValue = "true")
    boolean addAsSourceRoot;

    /**
     * If set to {@code true}, adds the source directory as a test source root
     * so the generated code will be compiled and included for test.
     */
    @Parameter(name = "addAsTestSourceRoot", defaultValue = "false")
    boolean addAsTestSourceRoot;

    /**
     * If set to {@code true}, the output directory will be cleaned before
     * code is generated.
     */
    @Parameter(name = "cleanOutputDirectory", defaultValue = "true")
    boolean cleanOutputDirectory;

    public void execute() throws MojoExecutionException {
        try {
            var sourceFolder = outputDirectory;
            if (sourceDirectory != null) {
                var sourceDirectoryPath = Path.of(sourceDirectory);
                sourceFolder = sourceFolder.resolve(sourceDirectoryPath);
            }

            var run = createRun(sourceFolder);

            if (cleanOutputDirectory)
                run.cleanOutputDirectory();

            run.createDirectory(sourceFolder.toAbsolutePath());

            if (addAsSourceRoot)
                mavenProject.addCompileSourceRoot(sourceFolder.toString());
            else if (addAsTestSourceRoot)
                mavenProject.addTestCompileSourceRoot(sourceFolder.toString());

            run.generateCode();

        } catch (Throwable t) {
            throw new MojoExecutionException("Failed to generate Windows API bindings", t);
        }
    }

    private WindowsApiRun createRun(Path sourceFolder) {
        var run = new WindowsApiRun();
        run.setEventListener(new EventLogger(getLog()));

        if (functions != null)
            run.setFunctions(new HashSet<>(functions));
        if (structs != null)
            run.setStructs(new HashSet<>(structs));
        if (enumerations != null)
            run.setEnumerations(new HashSet<>(enumerations));
        if (callbackFunctions != null)
            run.setCallbackFunctions(new HashSet<>(callbackFunctions));
        if (comInterfaces != null)
            run.setComInterfaces(new HashSet<>(comInterfaces));
        if (constants != null)
            run.setConstants(new HashSet<>(constants));

        run.setOutputDirectory(sourceFolder);
        run.setBasePackage(basePackage != null ? basePackage : "");
        return run;
    }
}
