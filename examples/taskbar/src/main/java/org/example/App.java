package org.example;

import windows.win32.system.com.CLSCTX;
import windows.win32.system.com.COINIT;
import windows.win32.ui.shell.ITaskbarList3;
import windows.win32.ui.shell.TBPFLAG;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.foreign.Arena;

import static java.lang.foreign.Linker.Option.captureStateLayout;
import static java.lang.foreign.MemorySegment.NULL;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static org.example.Windows.checkHResult;
import static windows.win32.system.com.Apis.CoCreateInstance;
import static windows.win32.system.com.Apis.CoInitializeEx;
import static windows.win32.ui.shell.Constants.TaskbarList;
import static windows.win32.ui.windowsandmessaging.Apis.FindWindowW;

public class App {
    private static final String WINDOW_TITLE = "Windows API Generator";

    static void main() throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SwingUtilities.invokeLater(App::createAndShowWindow);
    }

    private static void createAndShowWindow() {
        // create Swing window
        var frame = new JFrame(WINDOW_TITLE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(400, 300));
        frame.setLocationByPlatform(true);

        // add button
        var button = new JButton("Start Task");
        button.addActionListener(App::startTask);
        frame.getContentPane().setLayout(new GridBagLayout());
        frame.getContentPane().add(button);

        // display the window
        frame.pack();
        frame.setVisible(true);
    }

    // called when button is clicked
    private static void startTask(ActionEvent event) {
        var thread = new Thread(App::longRunningTask);
        thread.setDaemon(true);
        thread.start();
    }

    private static void longRunningTask() {
        ITaskbarList3 taskbarList = null;

        try {
            try (var arena = Arena.ofConfined()) {
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

                for (int i = 0; i < 100; i += 10) {
                    // Set progress value
                    checkHResult(taskbarList.SetProgressValue(hwnd, i, 100));

                    // Sleep
                    sleep(200);
                }

                // Disable display of progress
                checkHResult(taskbarList.SetProgressState(hwnd, TBPFLAG.TBPF_NOPROGRESS));
            }
        } catch (Exception e) {
            e.printStackTrace();

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
}
