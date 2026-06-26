//
// Windows API Generator for Java
// Copyright (c) 2026 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.examples.graalvm;

import windows.win32.foundation.RECT;
import windows.win32.system.com.Apis;
import windows.win32.system.com.CLSCTX;
import windows.win32.system.com.COINIT;
import windows.win32.ui.shell.ITaskbarList3;
import windows.win32.ui.shell.TBPFLAG;
import windows.win32.ui.windowsandmessaging.MESSAGEBOX_RESULT;
import windows.win32.ui.windowsandmessaging.MESSAGEBOX_STYLE;
import windows.win32.ui.windowsandmessaging.WINDOWINFO;
import windows.win32.ui.windowsandmessaging.WNDENUMPROC;

import java.io.File;
import java.lang.foreign.Arena;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

import static java.lang.foreign.Linker.Option.captureStateLayout;
import static java.lang.foreign.MemorySegment.NULL;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_CHAR;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static net.codecrete.windowsapi.examples.graalvm.Windows.checkErrorCode;
import static net.codecrete.windowsapi.examples.graalvm.Windows.checkHResult;
import static net.codecrete.windowsapi.examples.graalvm.Windows.checkResult;
import static windows.win32.storage.filesystem.Apis.GetVolumePathNameW;
import static windows.win32.system.com.Apis.CoCreateInstance;
import static windows.win32.system.com.Apis.CoInitializeEx;
import static windows.win32.system.registry.Apis.RegCloseKey;
import static windows.win32.system.registry.Apis.RegOpenKeyExW;
import static windows.win32.system.registry.Apis.RegQueryValueExW;
import static windows.win32.system.registry.Constants.HKEY_LOCAL_MACHINE;
import static windows.win32.system.registry.REG_SAM_FLAGS.KEY_QUERY_VALUE;
import static windows.win32.ui.shell.Apis.SHGetKnownFolderPath;
import static windows.win32.ui.shell.Constants.FOLDERID_CommonPrograms;
import static windows.win32.ui.shell.Constants.TaskbarList;
import static windows.win32.ui.windowsandmessaging.Apis.EnumWindows;
import static windows.win32.ui.windowsandmessaging.Apis.FindWindowW;
import static windows.win32.ui.windowsandmessaging.Apis.GetWindowInfo;
import static windows.win32.ui.windowsandmessaging.Apis.GetWindowTextW;
import static windows.win32.ui.windowsandmessaging.Apis.MessageBoxW;
import static windows.win32.ui.windowsandmessaging.WINDOW_STYLE.WS_VISIBLE;

/**
 * Run multiple actions using the Windows API
 */
@SuppressWarnings("java:S106")
public class App {
    private static final String WINDOW_TITLE = "Windows API Generator";

    private static final MemoryLayout errorStateLayout = Linker.Option.captureStateLayout();

    static void main() {
        enumerateWindows();
        getVolumePath();
        getCommonProgramsFolder();
        getWindowsVersion();
        startTaskBarProgress();
        showMessageBox();
    }

    static void enumerateWindows() {
        try (var arena = Arena.ofConfined()) {
            System.out.println("Windows:");
            var errorState = arena.allocate(errorStateLayout);
            var enumFuncUpcall = WNDENUMPROC.allocate(arena, App::windowEnumerationFunction);
            checkResult(EnumWindows(errorState, enumFuncUpcall, 0), errorState);
        }
    }

    private static int windowEnumerationFunction(MemorySegment windowHandle, long param) {
        try (var arena = Arena.ofConfined()) {
            var errorState = arena.allocate(errorStateLayout);

            var windowInfo = WINDOWINFO.allocate(arena);
            checkResult(GetWindowInfo(errorState, windowHandle, windowInfo), errorState);

            var dwStyle = WINDOWINFO.dwStyle(windowInfo);
            if ((dwStyle & WS_VISIBLE) == 0)
                return 1; // window is not visible

            var titleBarTextBuffer = arena.allocate(JAVA_CHAR, 300);
            var res = GetWindowTextW(errorState, windowHandle, titleBarTextBuffer, 300);
            if (res == 0)
                return 1; // window without title bar and likely size 0

            var titleBarText = titleBarTextBuffer.getString(0, UTF_16LE);

            var size = WINDOWINFO.rcWindow(windowInfo);
            var left = RECT.left(size);
            var top = RECT.top(size);
            var right = RECT.right(size);
            var bottom = RECT.bottom(size);

            System.out.printf("  \"%s\" at (%d, %d), size %d x %d%n",
                    titleBarText, left, top, right - left, bottom - top);
        }

        return 1;
    }

    static void getVolumePath() {
        try (var arena = Arena.ofConfined()) {
            var errorState = arena.allocate(captureStateLayout());

            String currentDirectory = new File("").getAbsolutePath();
            var filename = arena.allocateFrom(currentDirectory, UTF_16LE);
            var buffer = arena.allocate(JAVA_CHAR, 500);
            checkResult(GetVolumePathNameW(errorState, filename, buffer, 500), errorState);

            String volumePath = buffer.getString(0, UTF_16LE);
            System.out.println("Volume path: " + volumePath);
        }
    }

    static void getCommonProgramsFolder() {
        try (var arena = Arena.ofConfined()) {
            var out = arena.allocate(ADDRESS);
            checkHResult(SHGetKnownFolderPath(FOLDERID_CommonPrograms(), 0, NULL, out));
            var str = out.get(ADDRESS, 0).reinterpret(1000, arena, Apis::CoTaskMemFree);
            String folder = str.getString(0, UTF_16LE);
            System.out.println("Common programs folder: " + folder);
        }
    }

    static void getWindowsVersion() {
        try (var arena = Arena.ofConfined()) {
            var keyHandleHolder = arena.allocate(ADDRESS);
            checkErrorCode(
                    RegOpenKeyExW(
                        HKEY_LOCAL_MACHINE,
                        arena.allocateFrom("SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion", UTF_16LE),
                        0,
                        KEY_QUERY_VALUE,
                        keyHandleHolder
            ));

            var keyHandle = keyHandleHolder.get(ADDRESS, 0);

            var typeHolder = arena.allocate(JAVA_INT);
            var sizeHolder = arena.allocate(JAVA_INT);
            var data = arena.allocate(JAVA_LONG, 200);
            sizeHolder.set(JAVA_INT, 0L, (int) data.byteSize());
            checkErrorCode(
                    RegQueryValueExW(
                        keyHandle,
                        arena.allocateFrom("ProductName", UTF_16LE),
                        NULL,
                        typeHolder,
                        data,
                        sizeHolder
            ));

            var value = Windows.getUtf16String(data, 0, sizeHolder.get(JAVA_INT, 0));
            System.out.println("Windows version: " + value);

            checkErrorCode(RegCloseKey(keyHandle));
        }
    }

    static void startTaskBarProgress() {
        var thread = new Thread(App::longRunningTask);
        thread.setDaemon(true);
        thread.start();
    }

    private static void longRunningTask() {
        ITaskbarList3 taskbarList = null;

        try {
            try (var arena = Arena.ofConfined()) {
                sleep(300); // wait some time until the message box is visible
                // find the main application window
                var errorState = arena.allocate(captureStateLayout());
                var hwnd = FindWindowW(errorState, NULL, arena.allocateFrom(WINDOW_TITLE, UTF_16LE));
                if (hwnd.address() == 0L)
                    throw new IllegalStateException("Window titled " + WINDOW_TITLE + " not found.");

                // initialize COM (as this is a new thread)
                checkHResult(CoInitializeEx(NULL, COINIT.MULTITHREADED));

                // create instance of ITaskbarList, requesting interface ITaskbarList3
                var taskbarListOut = arena.allocate(ADDRESS);
                checkHResult(CoCreateInstance(TaskbarList(), NULL, CLSCTX.ALL, ITaskbarList3.iid(), taskbarListOut));

                // Wrap instance in easy-to-use Java object
                taskbarList = ITaskbarList3.wrap(taskbarListOut.get(ADDRESS, 0));

                // Initializes the taskbar list object. This method must be called before any other ITaskbarList methods can be called.
                checkHResult(taskbarList.HrInit());

                // Enable display of progress
                checkHResult(taskbarList.SetProgressState(hwnd, TBPFLAG.TBPF_NORMAL));

                for (int i = 0; i < 100; i += 5) {
                    // Set progress value
                    checkHResult(taskbarList.SetProgressValue(hwnd, i, 100));

                    // Sleep
                    sleep(200);
                }

                // Disable display of progress
                checkHResult(taskbarList.SetProgressState(hwnd, TBPFLAG.TBPF_NOPROGRESS));
            }
        } finally {
            if (taskbarList != null)
                taskbarList.Release();
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }

    static void showMessageBox() {
        var errorStateLayout = captureStateLayout();

        try (var arena = Arena.ofConfined()) {
            var errorState = arena.allocate(errorStateLayout);

            var result = checkResult(MessageBoxW(
                    errorState,
                    NULL,
                    arena.allocateFrom("Look, the taskbar...", UTF_16LE),
                    arena.allocateFrom(WINDOW_TITLE, UTF_16LE),
                    MESSAGEBOX_STYLE.MB_OKCANCEL
            ), errorState);

            switch (result) {
                case MESSAGEBOX_RESULT.IDOK:
                    System.out.println("Clicked 'OK'");
                    break;
                case MESSAGEBOX_RESULT.IDCANCEL:
                    System.out.println("Clicked 'Cancel'");
                    break;
                default:
                    System.out.println("Closed with result " + result);
            }
        }
    }
}
