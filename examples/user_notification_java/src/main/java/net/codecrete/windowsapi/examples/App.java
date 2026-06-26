package net.codecrete.windowsapi.examples;

import windows.win32.system.com.CLSCTX;
import windows.win32.system.com.COINIT;
import windows.win32.ui.shell.IQueryContinue;
import windows.win32.ui.shell.IUserNotification2;
import windows.win32.ui.shell.IUserNotificationCallback;

import java.lang.foreign.Arena;

import static java.lang.foreign.MemorySegment.NULL;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static net.codecrete.windowsapi.examples.Windows.checkHResult;
import static windows.win32.system.com.Apis.CoCreateInstance;
import static windows.win32.system.com.Apis.CoInitializeEx;
import static windows.win32.ui.shell.Constants.UserNotification;
import static windows.win32.ui.shell.NOTIFY_ICON_INFOTIP_FLAGS.NIIF_INFO;
import static windows.win32.ui.shell.NOTIFY_ICON_INFOTIP_FLAGS.NIIF_RESPECT_QUIET_TIME;

public class App {
    static void main() {
        // Initialize COM with apartment-threaded object concurrency
        checkHResult(CoInitializeEx(NULL, COINIT.APARTMENTTHREADED));

        try (var arena = Arena.ofConfined()) {

            // Create a user notification instance (implemented by Windows)
            var holder = arena.allocate(ADDRESS);
            checkHResult(CoCreateInstance(UserNotification(), NULL, CLSCTX.ALL, IUserNotification2.iid(), holder));

            // Wrap the COM instance in an easy-to-use Java object
            var notification = IUserNotification2.wrap(holder.get(ADDRESS, 0));

            // Configure a balloon info
            var title = arena.allocateFrom("Windows API", UTF_16LE);
            var text = arena.allocateFrom("Hello from Java", UTF_16LE);
            checkHResult(notification.SetBalloonInfo(title, text, NIIF_INFO | NIIF_RESPECT_QUIET_TIME));

            // Create an IQueryContinue instance (implemented in Java)
            var queryContinue = new QueryContinueObject();
            var queryContinueSegment = IQueryContinue.create(queryContinue, arena);
            queryContinue.setThisPointer(queryContinueSegment);

            // Create an IUserNotification instance (implemented in Java)
            var callback = new UserNotificationCallback();
            var callbackSegment = IUserNotificationCallback.create(callback, arena);
            callback.setThisPointer(callbackSegment);

            // Show the notification
            checkHResult(notification.Show(queryContinueSegment, 5000, callbackSegment));

            notification.Release();
        }
    }
}
