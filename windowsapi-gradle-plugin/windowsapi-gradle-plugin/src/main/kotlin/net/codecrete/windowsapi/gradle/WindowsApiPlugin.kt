//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPluginExtension


/**
 * Windows API Gradle Plug-in
 */
class WindowsApiPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.pluginManager.apply(JavaLibraryPlugin::class.java)

        // for each Java source set
        project.extensions.getByType(JavaPluginExtension::class.java).sourceSets.all { sourceSet ->

            // add the Windows API generator output directory to the source set
            val outputDirectory = project.layout.buildDirectory.dir("generated-src/windows-api/" + sourceSet.name)
            sourceSet.java.srcDir(outputDirectory)

            // create a Windows API task
            val taskName = sourceSet.getTaskName("generate", "WindowsApi")
            project.tasks.register(taskName, WindowsApiTask::class.java) { task ->
                task.description = "Generates Windows API bindings"
                task.outputDirectory.set(outputDirectory)
                task.basePackage.convention("")
            }

            // create a dependency of the Java compile task on the Windows API generation
            project.tasks.named(sourceSet.compileJavaTaskName) { task -> task.dependsOn(taskName) }
        }
    }
}
