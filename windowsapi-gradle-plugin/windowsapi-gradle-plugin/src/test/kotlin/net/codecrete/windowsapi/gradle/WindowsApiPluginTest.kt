//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.gradle

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull

class WindowsApiPluginTest {
    @Test fun `plugin registers task`() {
        // Create a test project and apply the plugin
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("net.codecrete.windows-api")

        // Verify the result
        assertNotNull(project.tasks.findByName("generateWindowsApi"))
        assertNotNull(project.tasks.findByName("generateTestWindowsApi"))
    }
}
