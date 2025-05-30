//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class WindowsApiRunTest {

    @Test
    void dryRun_succeeds() {
        var generator = new WindowsApiRun();
        generator.setFunctions(Set.of(
                "WriteFileEx",
                "MessageBoxExW"
        ));
        generator.setStructs(Set.of(
                "SP_DEVINFO_DATA",
                "SP_DEVICE_INTERFACE_DETAIL_DATA_W"
        ));
        generator.setEnumerations(Set.of(
                "WIN32_ERROR"
        ));

        generator.setOutputDirectory(Path.of("target/generated-sources"));

        assertDoesNotThrow(generator::dryRun);
    }

    @Test
    void createDirectory_succeeds() throws IOException {
        var temporaryFolder = Files.createTempDirectory("temporary-folder");
        try {
            var outputDirectory = temporaryFolder.resolve("output");
            var run = new WindowsApiRun();
            run.createDirectory(outputDirectory);
            assertThat(outputDirectory).exists();

        } finally {
            Testing.deleteDirectory(temporaryFolder);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void cleanedDirectory_isEmpty() throws IOException {
        var temporaryFolder = Files.createTempDirectory("temporary-folder");
        try {
            var outputDirectory = temporaryFolder.resolve("output");
            var inner1Directory = outputDirectory.resolve("inner1");
            var inner2Directory = inner1Directory.resolve("inner2");
            inner2Directory.toFile().mkdirs();
            Files.writeString(inner1Directory.resolve("inner1.txt"), "Hello World");

            var run = new WindowsApiRun();
            run.setOutputDirectory(outputDirectory);
            run.cleanOutputDirectory();
            assertThat(outputDirectory).exists();
            // delete will only succeed if directory is empty
            assertDoesNotThrow(() -> Files.delete(outputDirectory));

        } finally {
            Testing.deleteDirectory(temporaryFolder);
        }
    }
}
