//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class MetadataFileTest {
    MetadataFile metadataFile;

    @BeforeEach
    void setUp() throws IOException {
        try (var stream = MetadataBuilder.class.getClassLoader().getResourceAsStream("Windows.Win32.winmd")) {
            metadataFile = new MetadataFile(stream);
        }
    }

    @Test
    void readHeader() {
        assertThat(metadataFile.getVersion()).isEqualTo("v4.0.30319");

        assertThat(metadataFile.getStreams()).extracting(MetadataFile.MetadataStream::name)
                .containsExactlyInAnyOrder("#GUID", "#~", "#US", "#Strings", "#Blob");

        assertThat(metadataFile.getStreams()).extracting(MetadataFile.MetadataStream::offset).isSorted();
    }

    @Test
    void readTypeDefinitions() {
        int count = 0;
        for (var ignored : metadataFile.getTypeDefs()) {
            count += 1;
        }
        assertThat(count).isGreaterThan(10000);
    }
}
