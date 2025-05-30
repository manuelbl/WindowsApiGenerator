//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.special;

import net.codecrete.windowsapi.SimpleEventListener;
import net.codecrete.windowsapi.metadata.Metadata;
import net.codecrete.windowsapi.winmd.MetadataBuilder;
import net.codecrete.windowsapi.writer.CodeWriter;

import java.nio.file.Path;

public class FullBuild {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java " + FullBuild.class.getName() + " <output directory>");
            System.exit(1);
        }

        var outputDirectory = Path.of(args[0]);

        Metadata metadata = MetadataBuilder.load();

        var ouputDirectoryFile = outputDirectory.toFile();
        if (!ouputDirectoryFile.exists())
            ouputDirectoryFile.mkdirs();
        var codeWriter = new CodeWriter(metadata, outputDirectory, new SimpleEventListener());
        codeWriter.writeAll();
    }
}
