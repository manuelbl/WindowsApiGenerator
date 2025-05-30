# Example Project: Enumerate Windows

This example program enumerates all visible windows. It allocates a
[`WNDENUMPROC`](https://learn.microsoft.com/en-us/previous-versions/windows/desktop/legacy/ms633498(v=vs.85))
callback functions that is called by
[`EnumWindows`](https://learn.microsoft.com/en-gb/windows/win32/api/winuser/nf-winuser-enumwindows)
for each window. The callback function is implemented in Kotlin.

The project uses Gradle with the Kotlin DSL, and the code is in Kotlin.
The Windows API Generator plugin is configured to generate the code
for a number of functions. The additional elements such the `WNDENUMPROC`
callback function and the `WINDOWINFO` struct are automatically added
as they are parameters of the requested functions.
