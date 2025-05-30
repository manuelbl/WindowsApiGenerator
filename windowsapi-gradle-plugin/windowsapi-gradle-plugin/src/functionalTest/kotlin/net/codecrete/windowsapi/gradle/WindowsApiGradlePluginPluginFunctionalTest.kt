//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.gradle

import java.io.File
import kotlin.test.assertTrue
import kotlin.test.Test
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.io.TempDir

class WindowsApiGradlePluginPluginFunctionalTest {

    @field:TempDir
    lateinit var projectDir: File

    private val buildFile by lazy { projectDir.resolve("build.gradle") }
    private val settingsFile by lazy { projectDir.resolve("settings.gradle") }

    @Test fun `can run task`() {
        // Set up the test build
        buildFile.writeText("""
        plugins {
            id('net.codecrete.windows-api')
        }
        generateWindowsApi {
            functions = [ "DispatchMessageW" ]
        }
        """.trimIndent())

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("--info", "build")
        runner.withProjectDir(projectDir)
        runner.build()

        // Verify the result
        assertTrue(projectDir.resolve("build/generated-src/windows-api/main/windows/win32/ui/windowsandmessaging/Apis.java").exists())
    }
}
