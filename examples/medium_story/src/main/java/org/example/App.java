package org.example;

import windows.win32.system.com.Apis;

import java.io.File;
import java.lang.foreign.Arena;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;

import static java.lang.foreign.MemorySegment.NULL;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_CHAR;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static org.example.Windows.checkHResult;
import static org.example.Windows.checkResult;
import static windows.win32.storage.filesystem.Apis.GetVolumePathNameW;
import static windows.win32.ui.shell.Apis.SHGetKnownFolderPath;
import static windows.win32.ui.shell.Constants.FOLDERID_CommonPrograms;

@SuppressWarnings("java:S106")
public class App {
    static void main() {
        try (var arena = Arena.ofConfined()) {
            MemorySegment errorState = arena.allocate(Linker.Option.captureStateLayout());

            String currentDirectory = new File("").getAbsolutePath();
            MemorySegment filename = arena.allocateFrom(currentDirectory, UTF_16LE);
            MemorySegment buffer = arena.allocate(JAVA_CHAR, 500);
            checkResult(GetVolumePathNameW(errorState, filename, buffer, 500), errorState);

            String volumePath = buffer.getString(0, UTF_16LE);
            System.out.println("Volume path: " + volumePath);
        }

        try (var arena = Arena.ofConfined()) {
            MemorySegment out = arena.allocate(ADDRESS);
            checkHResult(SHGetKnownFolderPath(FOLDERID_CommonPrograms(), 0, NULL, out));
            MemorySegment str = out.get(ADDRESS, 0).reinterpret(1000, arena, Apis::CoTaskMemFree);
            String folder = str.getString(0, UTF_16LE);
            System.out.println("Common programs folder: " + folder);
        }
    }
}
